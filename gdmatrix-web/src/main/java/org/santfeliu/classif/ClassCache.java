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
package org.santfeliu.classif;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import org.apache.commons.collections.map.LRUMap;
import org.matrix.classif.ClassFilter;
import org.matrix.classif.ClassificationManagerPort;
import org.matrix.classif.ClassificationManagerService;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.classif.web.ClassificationConfigBean;
import org.santfeliu.jmx.CacheMBean;
import org.santfeliu.jmx.JMXUtils;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author realor
 */
public class ClassCache
{
  private static HashMap<String, ClassCache> classCaches =
    new HashMap<String, ClassCache>();

  private final String dateTime;
  private final LRUMap classMap = new LRUMap(3000); // 3000 Classes
  private final HashMap<String, List> childrenMap = new HashMap<>();
  private final HashMap<String, String> parentsMap = new HashMap<>();

  // sync parameters for all classCache instances
  private static long lastSyncMillis = System.currentTimeMillis();
  private static long syncMillis = 10 * 1000; // 10 seconds

  private long lastPurgeMillis;
  private long purgeMillis = 60 * 1000; // 1 minute

  private long lastAccessMillis;
  private long abandonedMillis = 60 * 60 * 1000; // 60 minutes

  protected ClassCache(String dateTime)
  {
    this.dateTime = dateTime;
    long nowMillis = System.currentTimeMillis();
    this.lastPurgeMillis = nowMillis;
    this.lastAccessMillis = nowMillis;
  }

  public static synchronized ClassCache getInstance()
  {
    return getInstance(ClassificationConfigBean.getDefaultDateTime());
  }

  public static synchronized ClassCache getInstance(Date date)
  {
    return getInstance(TextUtils.formatDate(date, "yyyyMMddHHmmss"));
  }

  public static synchronized ClassCache getInstance(String dateTime)
  {
    purgeAbandonedInstances();
    if (dateTime == null) dateTime =
      ClassificationConfigBean.getDefaultDateTime();
    ClassCache cache = classCaches.get(dateTime);
    if (cache == null)
    {
      cache = new ClassCache(dateTime);
      classCaches.put(dateTime, cache);
      JMXUtils.registerMBean("ClassCache_" + dateTime, cache.getCacheMBean());
    }
    cache.lastAccessMillis = System.currentTimeMillis();
    return cache;
  }

  public String getDateTime()
  {
    return dateTime;
  }

  public synchronized Class getClass(String classId)
  {
    long nowMillis = System.currentTimeMillis();
    if (mustSyncInstances(nowMillis))
    {
      syncInstances(nowMillis);
    }
    if (mustPurgeParentChildren(nowMillis))
    {
      purgeParentChildren(nowMillis);
    }

    Class classObject = (Class)classMap.get(classId);
    if (classObject == null)
    {
      try
      {
        ClassFilter filter = new ClassFilter();
        filter.setClassId(classId);
        filter.setStartDateTime(dateTime);
        filter.setEndDateTime(dateTime);
        filter.setFirstResult(0);
        filter.setMaxResults(1);
        List<org.matrix.classif.Class> list = getPort().findClasses(filter);
        if (list.size() == 1)
        {
          classObject = new Class(this, list.get(0));
          classMap.put(classId, classObject);
        }
      }
      catch (Exception ex)
      {
        // class not found
      }
    }
    return classObject;
  }

  public List<String> getTerminalClassIds(String superClassId)
  {
    return getTerminalClassIds(superClassId, null);
  }

  public List<String> getTerminalClassIds(String superClassId,
    List<String> terminalClassIds)
  {
    if (terminalClassIds == null) terminalClassIds = new ArrayList<>();

    List<String> subClassIds = getSubClassIds(superClassId);
    if (subClassIds.isEmpty())
    {
      terminalClassIds.add(superClassId);
    }
    else
    {
      for (String subClassId : subClassIds)
      {
        getTerminalClassIds(subClassId, terminalClassIds);
      }
    }
    return terminalClassIds;
  }

  public synchronized List<String> getSubClassIds(String superClassId)
  {
    List<String> children = childrenMap.get(superClassId);
    if (children == null)
    {
      children = new ArrayList<>();
      childrenMap.put(superClassId, children);

      ClassFilter filter = new ClassFilter();
      filter.setSuperClassId(superClassId);
      filter.setStartDateTime(dateTime);
      filter.setEndDateTime(dateTime);
      filter.setFirstResult(0);
      filter.setMaxResults(0);
      List<org.matrix.classif.Class> subClasses = getPort().findClasses(filter);
      for (org.matrix.classif.Class subClass : subClasses)
      {
        children.add(subClass.getClassId());
        parentsMap.put(subClass.getClassId(), superClassId);
      }
    }
    return children;
  }

  public boolean containsClass(String classId)
  {
    return classMap.containsKey(classId);
  }

  public synchronized void clear()
  {
    classMap.clear();
    parentsMap.clear();
    childrenMap.clear();
  }

  public static synchronized void syncInstances()
  {
    // add 1 second to detect changes in the current second
    syncInstances(System.currentTimeMillis() + 1000);
  }

  public static ClassificationManagerPort getPort()
  {
    try
    {
      WSDirectory wsDirectory = WSDirectory.getInstance();
      WSEndpoint endpoint =
        wsDirectory.getEndpoint(ClassificationManagerService.class);
      return endpoint.getPort(ClassificationManagerPort.class,
        MatrixConfig.getProperty("adminCredentials.userId"),
        MatrixConfig.getProperty("adminCredentials.password"));
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  // private methods

  private void clearSubClassIds(String superClassId)
  {
    List<String> children = childrenMap.remove(superClassId);
    if (children != null)
    {
      for (String classId : children)
      {
        parentsMap.remove(classId);
      }
    }
  }

  private static synchronized void purgeAbandonedInstances()
  {
    long nowMillis = System.currentTimeMillis();
    Iterator<ClassCache> iter = classCaches.values().iterator();
    while (iter.hasNext())
    {
      ClassCache classCache = iter.next();
      if (classCache.isAbandoned(nowMillis))
      {
        JMXUtils.unregisterMBean("ClassCache_" + classCache.getDateTime());
        classCache.clear();
        iter.remove();
      }
    }
  }

  private static synchronized void syncInstances(long nowMillis)
  {
    String nowDateTime =
      TextUtils.formatDate(new Date(nowMillis), "yyyyMMddHHmmss");
    String lastSyncDateTime =
      TextUtils.formatDate(new Date(lastSyncMillis), "yyyyMMddHHmmss");
    List<String> modifiedClassIdList =
      ClassCache.getPort().listModifiedClasses(lastSyncDateTime, nowDateTime);
    for (ClassCache classCache : classCaches.values())
    {
      classCache.purgeModifiedClasses(modifiedClassIdList);
    }
    lastSyncMillis = nowMillis;
  }

  private static boolean mustSyncInstances(long nowMillis)
  {
    return nowMillis - lastSyncMillis > syncMillis;
  }

  private boolean mustPurgeParentChildren(long nowMillis)
  {
    return nowMillis - lastPurgeMillis > purgeMillis;
  }

  private boolean isAbandoned(long nowMillis)
  {
    return nowMillis - lastAccessMillis > abandonedMillis;
  }

  private void purgeModifiedClasses(List<String> classIdList)
  {
    for (String classId : classIdList)
    {
      // remove class from cache
      classMap.remove(classId);
      // get previous superClassId
      String superClassId = parentsMap.get(classId);
      if (superClassId != null)
      {
        clearSubClassIds(superClassId);
      }
      // try to load class
      try
      {
        ClassFilter filter = new ClassFilter();
        filter.setClassId(classId);
        filter.setStartDateTime(dateTime);
        filter.setEndDateTime(dateTime);
        filter.setFirstResult(0);
        filter.setMaxResults(1);
        List<org.matrix.classif.Class> list = getPort().findClasses(filter);
        if (list.size() == 1)
        {
          Class classObject = new Class(this, list.get(0));
          classMap.put(classId, classObject);
          superClassId = classObject.getSuperClassId();
          if (superClassId != null)
          {
            clearSubClassIds(superClassId);
          }
        }
      }
      catch (Exception ex)
      {
        // class was removed
      }
    }
  }

  private void purgeParentChildren(long nowMillis)
  {
    int removed = 0;
    ArrayList<String> list = new ArrayList<>();
    list.addAll(childrenMap.keySet());
    for (String superClassId : list)
    {
      if (!classMap.containsKey(superClassId))
      {
        clearSubClassIds(superClassId);
        removed++;
      }
    }
    lastPurgeMillis = nowMillis;
  }

  private ClassCacheMBean getCacheMBean()
  {
    try
    {
      return new ClassCacheMBean();
    }
    catch (NotCompliantMBeanException ex)
    {
      return null;
    }
  }

  public class ClassCacheMBean extends StandardMBean implements CacheMBean
  {
    public ClassCacheMBean() throws NotCompliantMBeanException
    {
      super(CacheMBean.class);
    }

    @Override
    public String getName()
    {
      return "ClassCache(" + dateTime + ")";
    }

    @Override
    public long getMaxSize()
    {
      return classMap.maxSize();
    }

    @Override
    public long getSize()
    {
      return classMap.size();
    }

    @Override
    public String getDetails()
    {
      return "classMapSize=" + getSize() + "/" + getMaxSize();
    }

    @Override
    public void clear()
    {
      ClassCache.this.clear();
    }

    @Override
    public void update()
    {
      long nowMillis = System.currentTimeMillis();
      syncInstances(nowMillis);
      purgeParentChildren(nowMillis);
    }
  }
}
