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
package org.santfeliu.dic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import org.apache.commons.collections.LRUMap;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.DictionaryManagerPort;
import org.matrix.dic.DictionaryManagerService;
import org.matrix.dic.TypeFilter;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.jmx.CacheMBean;
import org.santfeliu.jmx.JMXUtils;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author realor
 */
public class TypeCache
{
  private static final Map<String, TypeCache> typeCaches =
    Collections.synchronizedMap(new HashMap<>());

  private final int maxSize = 3000; // 3000 types
  private final Map typeMap = Collections.synchronizedMap(new LRUMap(maxSize));
  private final Map<String, List> childrenMap =
    Collections.synchronizedMap(new HashMap<>());
  private final Map<String, String> parentsMap =
    Collections.synchronizedMap(new HashMap<>());
  private final Map<String, List> actionsMap =
    Collections.synchronizedMap(new HashMap<>());

  private Credentials credentials;

  private long lastSyncMillis;
  private final long syncMillis = 10 * 1000; // 10 seconds

  private long lastPurgeMillis;
  private final long purgeMillis = 60 * 1000; // 1 minute

  /* by default, this method returns instance with admin credentials */
  public static synchronized TypeCache getInstance()
  {
    Credentials credentials;
    String userId = null;
    String password = null;

    userId = MatrixConfig.getProperty("adminCredentials.userId");
    if (userId != null)
    {
      password = MatrixConfig.getProperty("adminCredentials.password");
    }
    else
    {
      userId = MatrixConfig.getProperty("adminCredentials.userId");
      if (userId != null)
      {
        password = MatrixConfig.getProperty("adminCredentials.password");
      }
    }
    credentials = new Credentials(userId, password);
    return getInstance(credentials);
  }

  public static synchronized TypeCache getInstance(Credentials credentials)
  {
    String key = credentials.getUserId();
    TypeCache cache = typeCaches.get(key);
    if (cache == null)
    {
      cache = new TypeCache(credentials);
      typeCaches.put(key, cache);
      JMXUtils.registerMBean("TypeCache_" + key, cache.getCacheMBean());
    }
    return cache;
  }

  protected TypeCache(Credentials credentials)
  {
    long nowMillis = System.currentTimeMillis();
    this.lastPurgeMillis = nowMillis;
    this.lastSyncMillis = nowMillis;
    this.credentials = credentials;
  }

  public Credentials getCredentials()
  {
    return credentials;
  }

  public Type getType(String typeId)
  {
    long nowMillis = System.currentTimeMillis();
    if (mustSyncTypes(nowMillis)) // sync cache
    {
      sync(nowMillis);
    }
    if (mustPurgeParentChildren(nowMillis))
    {
      purgeParentChildren(nowMillis);
    }

    Type type = (Type)typeMap.get(typeId);
    if (type == null)
    {
      try
      {
        type = new Type(this, getPort().loadType(typeId));
        typeMap.put(typeId, type);
        String actualTypeId = type.getTypeId();
        typeMap.put(actualTypeId, type); //Ensure put id with prefix
        String superTypeId = type.getSuperTypeId();
        if (superTypeId != null)
        {
          parentsMap.put(typeId, superTypeId);
          parentsMap.put(actualTypeId, superTypeId);
          List children = childrenMap.get(superTypeId);
          if (children != null)
          {
            if (!children.contains(actualTypeId)) children.add(actualTypeId);
          }
        }
      }
      catch (Exception ex)
      {
        // type not found
      }
    }
    return type;
  }

  public List<String> getDerivedTypeIds(String superTypeId)
  {
    List<String> children = childrenMap.get(superTypeId);
    if (children == null)
    {
      children = new ArrayList<>();
      childrenMap.put(superTypeId, children);

      TypeFilter filter = new TypeFilter();
      filter.setSuperTypeId(superTypeId);
      List<org.matrix.dic.Type> derivedTypes = getPort().findTypes(filter);
      for (org.matrix.dic.Type derivedType : derivedTypes)
      {
        children.add(derivedType.getTypeId());
        parentsMap.put(derivedType.getTypeId(), superTypeId);
      }
    }
    return children;
  }

  public List<String> getActions(String typeId)
  {
    List<String> typeActions = actionsMap.get(typeId);
    if (typeActions == null)
    {
      typeActions = getPort().getTypeActions(typeId);
      actionsMap.put(typeId, typeActions);
    }
    return typeActions;
  }

  public boolean containsType(String typeId)
  {
    return typeMap.containsKey(typeId);
  }

  public void clear()
  {
    typeMap.clear();
    childrenMap.clear();
    parentsMap.clear();
    actionsMap.clear();
  }

  public void sync()
  {
    // add 1 second to detect changes in the current second
    sync(System.currentTimeMillis() + 1000);
  }

  public static void reset()
  {
    for (String key : typeCaches.keySet())
    {
      JMXUtils.unregisterMBean("TypeCache_" + key);
    }
    typeCaches.clear();
  }

  public DictionaryManagerPort getPort()
  {
    try
    {
      WSDirectory wsDirectory = WSDirectory.getInstance();
      WSEndpoint endpoint =
        wsDirectory.getEndpoint(DictionaryManagerService.class);
      return endpoint.getPort(DictionaryManagerPort.class,
        credentials.getUserId(), credentials.getPassword());
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  // private methods

  private boolean mustSyncTypes(long nowMillis)
  {
    return nowMillis - lastSyncMillis > syncMillis;
  }

  private boolean mustPurgeParentChildren(long nowMillis)
  {
    return nowMillis - lastPurgeMillis > purgeMillis;
  }

  private void sync(long nowMillis)
  {
    String nowDateTime =
      TextUtils.formatDate(new Date(nowMillis), "yyyyMMddHHmmss");
    String lastSyncDateTime =
      TextUtils.formatDate(new Date(lastSyncMillis), "yyyyMMddHHmmss");
    List<String> modifiedTypeIdList =
      getPort().listModifiedTypes(lastSyncDateTime, nowDateTime);
    purgeModifiedTypes(modifiedTypeIdList);
    lastSyncMillis = nowMillis;
  }

  private synchronized void purgeParentChildren(long nowMillis)
  {
    int removed = 0;
    ArrayList<String> list = new ArrayList();
    list.addAll(childrenMap.keySet());
    for (String superTypeId : list)
    {
      if (!typeMap.containsKey(superTypeId))
      {
        clearDerivedTypeIds(superTypeId);
        removed++;
      }
    }
    lastPurgeMillis = nowMillis;
  }

  private synchronized void purgeModifiedTypes(List<String> typeIdList)
  {
    for (String typeId : typeIdList)
    {
      // remove type from typeMap
      typeMap.remove(typeId);
      // look for previous superTypeId
      String superTypeId = parentsMap.get(typeId);
      if (superTypeId != null)
      {
        // previous parent found: clear derived typeId list
        clearDerivedTypeIds(superTypeId);
      }
    }
  }

  private synchronized void clearDerivedTypeIds(String superTypeId)
  {
    List<String> children = childrenMap.remove(superTypeId);
    if (children != null)
    {
      for (String typeId : children)
      {
        parentsMap.remove(typeId);
      }
    }
  }

  private TypeCacheMBean getCacheMBean()
  {
    try
    {
      return new TypeCacheMBean();
    }
    catch (NotCompliantMBeanException ex)
    {
      return null;
    }
  }

  public class TypeCacheMBean extends StandardMBean implements CacheMBean
  {

    public TypeCacheMBean() throws NotCompliantMBeanException
    {
      super(CacheMBean.class);
    }

    @Override
    public String getName()
    {
      return "TypeCache(" + credentials.getUserId() + ")";
    }

    @Override
    public long getMaxSize()
    {
      return maxSize;
    }

    @Override
    public long getSize()
    {
      return typeMap.size();
    }

    @Override
    public String getDetails()
    {
      return "typeMapSize=" + getSize() + "/" + getMaxSize() + "," +
        "childrenMapSize=" + childrenMap.size() + "," +
        "parentsMapSize=" + parentsMap.size() + "," +
        "actionsMapSize=" + actionsMap.size();
    }

    @Override
    public void clear()
    {
      TypeCache.this.clear();
    }

    @Override
    public void update()
    {
      long nowMillis = System.currentTimeMillis();
      sync(nowMillis);
      purgeParentChildren(nowMillis);
    }

  }

  public static void main(String[] args)
  {
    try
    {
      TypeCache cache = TypeCache.getInstance();
      Type type = cache.getType(DictionaryConstants.DOCUMENT_TYPE);
      System.out.println("Type: " + type.formatTypePath(false, true, true));
      HashSet roles = new HashSet();
      roles.add("[realor]");
      roles.add("WEBMASTER");
      List<Type> lt = type.getDerivedTypes(true);
      for (Type t : lt)
      {
        if (t.canPerformAction(DictionaryConstants.READ_ACTION, roles))
        {
          System.out.println(t.formatTypePath(false, true, true));
        }
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
