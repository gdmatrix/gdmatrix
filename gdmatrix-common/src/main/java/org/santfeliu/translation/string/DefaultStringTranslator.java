/*
 * GDMatrix
 *  
 * Copyright (C) 2020, Ajuntament de Sant Feliu de Llobregat
 *  
 * This program is licensed and may be used, modified and redistributed under 
 * the terms of the European Public License (EUPL), either version 1.1 or (at 
 * your option) any later version as soon as they are approved by the European 
 * Commission.
 *  
 * Alternatively, you may redistribute and/or modify this program under the 
 * terms of the GNU Lesser General Public License as published by the Free 
 * Software Foundation; either  version 3 of the License, or (at your option) 
 * any later version. 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *    
 * See the licenses for the specific language governing permissions, limitations 
 * and more details.
 *    
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along 
 * with this program; if not, you may find them at: 
 *    
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * http://www.gnu.org/licenses/ 
 * and 
 * https://www.gnu.org/licenses/lgpl.txt
 */
package org.santfeliu.translation.string;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import org.matrix.translation.Translation;
import org.matrix.translation.TranslationManagerPort;
import org.matrix.translation.TranslationManagerService;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.jmx.CacheMBean;
import org.santfeliu.jmx.JMXUtils;
import org.santfeliu.translation.StringTranslator;
import org.santfeliu.translation.util.TranslationUtils;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author realor
 */
public class DefaultStringTranslator implements StringTranslator
{
  private final Map<String, Item> itemsById = new HashMap<String, Item>();
  private final Map<Key, Item> itemsByKey = new HashMap<Key, Item>();
  private final Map<String, Group> groups = new HashMap<String, Group>();

  private long lastSyncMillis;
  private long lastActivationMillis;
  private long lastPurgeMillis;

  // configuration parameters
  private static int minNumItems = 800;
  private static int maxNumItems = 1000;
  private static int maxNumItemsPerActivation = 10;
  private static long minItemSurviveMillis = 1 * 60 * 1000; // 1 minute
  private static long minItemActivationMillis = 15 * 60 * 1000; // 15 minutes
  private static long syncMillis = 10000; // 10 seconds
  private static long activationMillis = 1 * 60 * 1000; // 1 minute
  private static long purgeMillis = 1 * 60 * 1000; // 1 minute

  protected static final Logger logger =
    Logger.getLogger("DefaultStringTranslator");

  static
  {
    try
    {
      String baseName = DefaultStringTranslator.class.getName();
      String value;
      value = MatrixConfig.getProperty(baseName + ".minNumItems");
      if (value != null) minNumItems = Integer.parseInt(value);

      value = MatrixConfig.getProperty(baseName + ".maxNumItems");
      if (value != null) maxNumItems = Integer.parseInt(value);

      value = MatrixConfig.getProperty(baseName + ".maxNumItemsPerActivation");
      if (value != null) maxNumItemsPerActivation = Integer.parseInt(value);

      value = MatrixConfig.getProperty(baseName + ".minItemSurviveMillis");
      if (value != null) minItemSurviveMillis = Long.parseLong(value);

      value = MatrixConfig.getProperty(baseName + ".minItemActivationMillis");
      if (value != null) minItemActivationMillis = Long.parseLong(value);

      value = MatrixConfig.getProperty(baseName + ".syncMillis");
      if (value != null) syncMillis = Long.parseLong(value);

      value = MatrixConfig.getProperty(baseName + ".activationMillis");
      if (value != null) activationMillis = Long.parseLong(value);

      value = MatrixConfig.getProperty(baseName + ".purgeMillis");
      if (value != null) purgeMillis = Long.parseLong(value);
    }
    catch (Exception ex)
    {
      logger.log(Level.SEVERE, "init failed", ex);
    }
  }

  public DefaultStringTranslator()
  {
    long nowMillis =  System.currentTimeMillis();
    this.lastSyncMillis = nowMillis;
    this.lastActivationMillis = nowMillis;
    this.lastPurgeMillis = nowMillis;
    JMXUtils.registerMBean("TranslationCache", getCacheMBean());
  }

  public String translate(String text, String language, String group)
  {
    String translation;
    try
    {
      long nowMillis = System.currentTimeMillis();

      // database synchonization
      removeModifiedItems(nowMillis);

      // trivial translation?
      translation = TranslationUtils.directTranslate(language, text);
      if (translation == null)
      {
        // database translation lookup
        Key key = new Key(language, text);
        Item item = null;
        synchronized (this) { item = itemsByKey.get(key); }
        if (item == null)
        {
          List<Translation> translations =
            findTranslations(text, language, group);
          registerItems(translations, group, nowMillis);
          synchronized (this) { item = itemsByKey.get(key); }
        }
        item.lastReadMillis = nowMillis;
        translation = item.translation;
      }

      // activate items
      activateItems(nowMillis);

      // purge old items
      purgeItems(nowMillis);
    }
    catch (Exception ex)
    {
      translation = null;
      logger.log(Level.SEVERE, "translating '" + text + "'", ex);
    }
    return translation;
  }

  public synchronized void clear()
  {
    itemsByKey.clear();
    itemsById.clear();
    groups.clear();
  }

  public synchronized void print(PrintWriter out) throws IOException
  {
    out.println("TranslationCache:");
    out.println("  minNumItems: " + minNumItems);
    out.println("  maxNumItems: " + maxNumItems);
    out.println("  maxNumItemsPerActivation: " + maxNumItemsPerActivation);
    out.println("  minItemSurviveMillis: " + minItemSurviveMillis);
    out.println("  minItemActivationMillis: " + minItemActivationMillis);
    out.println("  syncMillis = 10000: " + syncMillis);
    out.println("  activationMillis: " + activationMillis);
    out.println("  purgeMillis: " + purgeMillis);
    out.println("Number of items: " + itemsByKey.size());
    Collection<Item> items = itemsByKey.values();
    int i = 1;
    for (Item item : items)
    {
      out.println("Item #" + i + ":");
      out.println("  transId: " + item.transId);
      out.println("  Language: " + item.key.language);
      out.println("  Text: " + item.key.text);
      out.println("  Translation: " + item.translation);
      out.println("  group: " + item.group);
      out.println("  creationMillis: " + new Date(item.creationMillis));
      out.println("  databaseReadMillis: " + new Date(item.databaseReadMillis));
      out.println("  lastReadMillis: " + new Date(item.lastReadMillis));
      out.println("  needsActivation: " + item.needsActivation());
      i++;
    }
    i = 0;
    for (Group group : groups.values())
    {
      out.println("Group #" + i + ":");
      out.println("  language: " + group.language);
      out.println("  group: " + group.group);
      out.println("  count: " + group.count);
      out.println("  maxCount: " + group.maxCount);
      i++;
    }
    out.flush();
  }

  private void removeModifiedItems(long nowMillis)
  {
    if (mustSyncItems(nowMillis))
    {
      doSyncItems(nowMillis);
    }
  }
  
  private void doSyncItems(long nowMillis)
  {
    String lastSyncDateTime = getDateTime(lastSyncMillis);
    String nowDateTime = getDateTime(nowMillis);
    lastSyncMillis = nowMillis;
    
    List<String> transIds =
      getPort().listModifiedTranslations(null, lastSyncDateTime, nowDateTime);

    synchronized (this)
    {
      for (String transId : transIds)
      {
        Item item = itemsById.remove(transId);
        if (item != null)
        {
          itemsByKey.remove(item.key);
        }
      }
    }
  }

  private List<Translation> findTranslations(
    String text, String language, String group)
  {
    TranslationManagerPort port = getPort();
    ArrayList<Translation>  translations = new ArrayList<Translation>();
    if (group == null || !shouldLoadGroup(language, group))
    {
      // do not load entire group
      translations.add(port.translate(language, text, group));
    }
    else
    {
      // load entire group
      translations.addAll(port.translateGroup(language, text, group));
    }
    return translations;
  }

  // register items in cache from translation list (Thread safe)
  private synchronized void registerItems(
    List<Translation> translations, String group, long nowMillis)
  {
    int numAdded = 0;
    int numUpdated = 0;
    for (Translation translation : translations)
    {
      String language = translation.getLanguage();
      String normalizedText = translation.getText();
      Key key = new Key(language, normalizedText);
      Item item = itemsByKey.get(key);
      if (item == null)
      {
        item = new Item();
        item.key = key;
        item.transId = translation.getTransId();
        item.group = translation.getGroup();
        getGroup(language, item.group).increment();
        itemsByKey.put(key, item);
        itemsById.put(translation.getTransId(), item);
        numAdded++;
      }
      else numUpdated++;

      long readMillis = getMillis(translation.getReadDateTime());
      item.translation = translation.getTranslation();
      item.creationMillis = nowMillis;
      item.databaseReadMillis = readMillis;
      item.lastReadMillis = readMillis;
    }
    logger.log(Level.INFO, "Registering items: group: {0}, read: {1}, added: {2}, updated: {3}, cache size: {4}",
      new Object[]{group, translations.size(), numAdded, numUpdated, itemsByKey.size()});
  }

  private void activateItems(long nowMillis)
  {
    if (mustActivateItems(nowMillis))
    {
      doActivateItems(nowMillis);
    }
  }

  private void doActivateItems(long nowMillis)
  {
    lastActivationMillis = nowMillis;

    List<Translation> translations = new ArrayList<Translation>();
    synchronized (this)
    {
      Iterator<Item> iter = itemsById.values().iterator();
      while (iter.hasNext() && translations.size() < maxNumItemsPerActivation)
      {
        Item item = iter.next();
        if (item.needsActivation())
        {
          // create translation with minimal information: transId, lastReadMillis
          Translation translation = new Translation();
          translation.setTransId(item.transId);
          translation.setReadDateTime(getDateTime(item.lastReadMillis));
          translations.add(translation);
          item.databaseReadMillis = item.lastReadMillis;
        }
      }
    }
    if (translations.size() > 0)
    {
      getPort().setActiveTranslations(translations);    
      logger.log(Level.INFO, "Activating {0} items",
        new Object[]{translations.size()});
    }
  }

  // remove older items
  private void purgeItems(long nowMillis)
  {
    if (mustPurgeItems(nowMillis))
    {
      doPurgeItems(nowMillis);
    }
  }

  private synchronized void doPurgeItems(long nowMillis)
  {
    if (itemsByKey.size() <= minNumItems) return; // purge not necessary

    logger.log(Level.INFO, "Purge started");

    lastPurgeMillis = nowMillis;
    
    Collection<Item> items = itemsByKey.values();
    Item[] itemArray = new Item[items.size()];
    items.toArray(itemArray);
    Arrays.sort(itemArray);

    int i = 0;
    int numRemoved = 0;
    while (i < itemArray.length && itemsByKey.size() > minNumItems)
    {
      Item item = itemArray[i++];
      if (nowMillis - item.creationMillis > minItemSurviveMillis)
      {
        itemsByKey.remove(item.key);
        itemsById.remove(item.transId);
        getGroup(item.key.language, item.group).decrement();
        numRemoved++;
      }
    }
    logger.log(Level.INFO, "Purged items: {0}, total: {1}",
      new Object[]{numRemoved, itemsByKey.size()});
  }

  private boolean mustSyncItems(long nowMillis)
  {
    return nowMillis - lastSyncMillis > syncMillis;
  }

  private boolean mustActivateItems(long nowMillis)
  {
    return nowMillis - lastActivationMillis > activationMillis;
  }

  private boolean mustPurgeItems(long nowMillis)
  {
    return nowMillis - lastPurgeMillis > purgeMillis &&
      itemsByKey.size() > maxNumItems;
  }

  private synchronized Group getGroup(String language, String group)
  {
    String key = language + ":" + group;
    Group gr = groups.get(key);
    if (gr == null)
    {
      gr = new Group(language, group);
      groups.put(key, gr);
    }
    return gr;
  }

  private synchronized boolean shouldLoadGroup(String language, String group)
  {
    String key = language + ":" + group;
    Group gr = groups.get(key);
    
    if (gr == null) return true; // not loaded
    if (gr.maxCount <= 3) return true; // few entries, load again
    if (gr.count < gr.maxCount / 2) return true;
    return false;
  }

  private long getMillis(String dateTime)
  {
    Date date = TextUtils.parseInternalDate(dateTime);
    return date == null ? 0 : date.getTime();
  }

  private String getDateTime(long millis)
  {
    return TextUtils.formatDate(new Date(millis), "yyyyMMddHHmmss");
  }

  private TranslationManagerPort getPort()
  {
    try
    {
      WSDirectory dir = WSDirectory.getInstance();
      WSEndpoint endpoint = dir.getEndpoint(TranslationManagerService.class);
      return endpoint.getPort(TranslationManagerPort.class);
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  private TranslationCacheMBean getCacheMBean()
  {
    try
    {
      return new TranslationCacheMBean();
    }
    catch (NotCompliantMBeanException ex)
    {
      return null;
    }
  }

  public class TranslationCacheMBean extends StandardMBean implements CacheMBean
  {
    public TranslationCacheMBean() throws NotCompliantMBeanException
    {
      super(CacheMBean.class);
    }

    public String getName()
    {
      return "TranslationCache";
    }

    public long getMaxSize()
    {
      return maxNumItems;
    }

    public long getSize()
    {
      return itemsById.size();
    }

    public String getDetails()
    {
      return "itemsByIdSize=" + itemsById.size() + "/" + getMaxSize() + "," +
        "itemsByKeySize=" + itemsByKey.size() + "/" + getMaxSize() + "," +
        "groupsSize=" + groups.size();
    }

    public void clear()
    {
      DefaultStringTranslator.this.clear();
    }

    public void update()
    {
      long nowMillis = System.currentTimeMillis();
      doSyncItems(nowMillis);
      doActivateItems(nowMillis);
      doPurgeItems(nowMillis);
    }
  }

  // ---------- key in itemsByKey ----------
  class Key
  {
    String language;
    String text;

    Key(String language, String text)
    {
      this.language = language;
      this.text = text;
    }

    @Override
    public boolean equals(Object o)
    {
      if (o instanceof Key)
      {
        Key key = (Key)o;
        return language.equals(key.language) && text.equals(key.text);
      }
      return false;
    }

    @Override
    public int hashCode()
    {
      int hash = 3;
      hash = 89 * hash + language.hashCode();
      hash = 89 * hash + text.hashCode();
      return hash;
    }
  }

  // ---------- value in itemsByKey ----------
  class Item implements Comparable
  {
    Key key;
    String transId;
    String translation;
    String group;
    long creationMillis;
    long databaseReadMillis; // readMillis in database for this translation
    long lastReadMillis; // readMillis in cache

    public int compareTo(Object o)
    {
      if (o instanceof Item)
      {
        Item item = (Item)o;
        if (lastReadMillis == item.lastReadMillis) return 0;
        return  lastReadMillis < item.lastReadMillis ? -1 : 1;
      }
      return 0;
    }

    boolean needsActivation()
    {
      return lastReadMillis - databaseReadMillis > minItemActivationMillis;
    }
  }

  // --------- group in groups ---------
  class Group
  {
    String language;
    String group;
    long count;
    long maxCount;

    Group(String language, String group)
    {
      this.language = language;
      this.group = group;
    }

    void increment()
    {
      count++;
      if (count > maxCount) maxCount = count;
    }

    void decrement()
    {
      count--;
      if (count <= 0) groups.remove(language + ":" + group);
    }
  }
}
