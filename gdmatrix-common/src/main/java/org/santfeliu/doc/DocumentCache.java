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
package org.santfeliu.doc;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import org.apache.commons.collections.LRUMap;
import org.matrix.dic.DictionaryConstants;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentConstants;
import org.matrix.doc.RelatedDocument;
import org.matrix.doc.RelationType;
import org.matrix.security.AccessControl;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.jmx.CacheMBean;
import org.santfeliu.jmx.JMXUtils;
import org.santfeliu.security.User;
import org.santfeliu.security.UserCache;

/**
 *
 * @author blanquepa
 */
public class DocumentCache
{
  private static final int MAX_CACHE_SIZE = 1000;
  private Map map;
  static DocumentCache cache = new DocumentCache(MAX_CACHE_SIZE);

  public DocumentCache(int cacheSize)
  {
    map = Collections.synchronizedMap(new LRUMap(cacheSize));
    JMXUtils.registerMBean("DocumentCache", getCacheMBean());
  }

  public static void reset()
  {
    cache.clear();
  }

  public static void reset(String docId, String language)
  {
    cache.removeEntry(getKey(docId, language));
  }

  public static void reset(String key)
  {
    cache.removeEntry(key);
  }

  public static String getDocument(String docId,
    String language, String userId, String password, long cacheTime)
    throws Exception
  {
    String contentId = cache.getContentId(docId, language,
      userId, password, cacheTime);

    if (contentId == null)
    {
      DocumentManagerClient client = new DocumentManagerClient(userId, password);
      Document document = client.loadDocument(docId);
      if (document != null)
      {
        cache.putContentId(docId, document);

        if (language != null &&
           !language.equals(document.getLanguage()))
        {
          //Search translation of requested language
          Iterator it = document.getRelatedDocument().iterator();
          boolean translationFound = false;
          while (it.hasNext() && !translationFound)
          {
            RelatedDocument relDoc = (RelatedDocument)it.next();
            if (RelationType.TRANSLATION.equals(relDoc.getRelationType()) &&
              language.equals(relDoc.getName()))
            {
              String relDocId = relDoc.getDocId();
              int version = relDoc.getVersion();
              document = client.loadDocument(relDocId, version);
              if (document != null)
              {
                contentId = document.getContent().getContentId();
                cache.putRelDocument(docId, language, relDocId);
                cache.putContentId(relDocId, document);
              }
              translationFound = (contentId != null);
            }
          }

          if (!translationFound)
          {
            contentId = document.getContent().getContentId();
            cache.putRelDocument(docId, language, document.getDocId());
          }
        }
        else
        {
          contentId = document.getContent().getContentId();
          cache.putRelDocument(docId, language, docId);
        }
      }
    }

    return contentId;
  }

  //Private cache methods
  private void putContentId(String docId, Document document)
  {
    Entry entry = new Entry();
    entry.id = document.getContent().getContentId();
    entry.docTypeId = document.getDocTypeId();
    entry.acl = document.getAccessControl();
    cache.putEntry(docId, entry);
  }

  private void putRelDocument(String docId,
    String language, String relDocId)
  {
    Entry entry = new Entry();
    entry.id = relDocId;
    cache.putEntry(getKey(docId, language), entry);
  }

  private String getContentId(String docId,
    String language, String userId, String password, long cacheTime)
    throws Exception
  {
    String contentId = null;
    
    String key = getKey(docId, language);
    //Related (translation)
    Entry entry = cache.getEntry(key, userId, password, cacheTime);
    if (entry != null)
    {
      docId = entry.id;
      //Content
      entry = cache.getEntry(docId, userId, password, cacheTime);
      if (entry != null)
        contentId = entry.id;
    }

    return contentId;
  }

  //Private map methods
  private Entry getEntry(String key, String userId,
    String password, long cacheTime) throws Exception
  {
    Entry entry = (Entry)map.get(key);
    if (entry != null)
    {
      if (!entry.isValid(cacheTime))
      {
        reset(key);
        entry = null;
      }
      else
      {
        User user = UserCache.getUser(userId, password);
        if (!isKey(key) && !canUserReadDocument(entry.docTypeId, entry.acl, user))
        {
          entry = null;
          throw new Exception("ACTION_DENIED");
        }
      }
    }
    return entry;
  }

  private void putEntry(String key, Entry entry)
  {
    map.put(key, entry);
  }
  
  private void removeEntry(String key)
  {
    if (key == null)
      map.clear();
    else
      map.remove(key);
  }

  private void clear()
  {
    map.clear();
  }

  //Private auxiliar methods
  private boolean canUserReadDocument(String docTypeId, List<AccessControl> acl,
    User user)
  {
    Set<String> userRoles = user.getRoles();
    Type type = TypeCache.getInstance().getType(docTypeId);
    String action = DictionaryConstants.READ_ACTION;
    
    return userRoles.contains(DocumentConstants.DOC_ADMIN_ROLE)
      || DictionaryUtils.canPerformAction(action, userRoles, acl, type);
  }

  private static String getKey(String docId, String language)
  {
    return docId + ";" + language;
  }

  private static boolean isKey(String id)
  {
    return id != null && id.contains(";");
  }

  class Entry
  {
    String id; //contentId or docId
    String docTypeId;
    List<AccessControl> acl;
    long requestTime;

    public Entry()
    {
      this.requestTime = System.currentTimeMillis();
    }

    boolean isValid(long cacheTime)
    {
      return (System.currentTimeMillis() - requestTime) < cacheTime;
    }
  }

  private DocumentCacheMBean getCacheMBean()
  {
    try
    {
      return new DocumentCacheMBean();
    }
    catch (NotCompliantMBeanException ex)
    {
      return null;
    }
  }

  public class DocumentCacheMBean extends StandardMBean implements CacheMBean
  {
    public DocumentCacheMBean() throws NotCompliantMBeanException
    {
      super(CacheMBean.class);
    }

    public String getName()
    {
      return "DocumentCache";
    }

    public long getMaxSize()
    {
      return MAX_CACHE_SIZE;
    }

    public long getSize()
    {
      return map.size();
    }

    public String getDetails()
    {
      return "documentMapSize=" + getSize() + "/" + getMaxSize();
    }

    public void clear()
    {
      DocumentCache.reset();
    }

    public void update()
    {
    }
  }
}
