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
import java.util.List;
import java.util.Map;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import org.apache.commons.collections.LRUMap;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.DictionaryManagerPort;
import org.matrix.dic.DictionaryManagerService;
import org.matrix.dic.EnumTypeItem;
import org.matrix.dic.EnumTypeItemFilter;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.jmx.CacheMBean;
import org.santfeliu.jmx.JMXUtils;
import org.santfeliu.util.MatrixConfig;

/**
 *
 * @author lopezrj-sf
 */
public class EnumTypeCache
{
  private static EnumTypeCache cache;
  private static final long CLEAR_MILLIS = 60 * 60 * 1000; // 1 hour
  private static final int MAX_SIZE = 300; // 300 types  
  
  //Map of enumTypeId -> List<EnumTypeItem>
  private final Map<String, List<EnumTypeItem>> map =
    Collections.synchronizedMap(new LRUMap(MAX_SIZE));
  private long lastClearMillis = System.currentTimeMillis();

  public static synchronized EnumTypeCache getInstance()
  {
    if (cache == null)
    {
      cache = new EnumTypeCache();
      JMXUtils.registerMBean("EnumTypeCache", cache.getCacheMBean());
    }
    return cache;
  }

  public List<EnumTypeItem> getItems(String enumTypeId)
  {
    if (enumTypeId == null) return null;

    long nowMillis = System.currentTimeMillis();
    if (mustClearItems(nowMillis)) // clear cache
    {
      clear(nowMillis);
    }
    List<EnumTypeItem> items = map.get(enumTypeId);
    if (items == null)
    {
      try
      {
        DictionaryManagerPort dicPort = getPort();
        dicPort.loadEnumType(enumTypeId); //Check enumtype
        EnumTypeItemFilter filter = new EnumTypeItemFilter();
        filter.setEnumTypeId(enumTypeId);
        items = dicPort.findEnumTypeItems(filter);
        map.put(enumTypeId, items);
      }
      catch (Exception ex)
      {
        //type not found
        return null;
      }
    }
    return items;
  }

  public List<EnumTypeItem> getItemsByValue(String enumTypeId, String value)
  {
    if (enumTypeId == null) return null;
    
    List<EnumTypeItem> items = getItems(enumTypeId);
    if (items != null)
    {
      String searchValue = StringUtils.defaultString(value);
      List<EnumTypeItem> result = new ArrayList<>();
      for (EnumTypeItem item : items)
      {
        String itemValue = StringUtils.defaultString(item.getValue());
        if (itemValue.equals(searchValue)) result.add(item);
      }
      return result;
    }
    return null; //type not found
  }

  public EnumTypeItem getItem(String enumTypeItemId)
  {
    if (enumTypeItemId == null) return null;
    
    List<String> enumTypeIds = new ArrayList(map.keySet());
    for (String enumTypeId : enumTypeIds)
    {
      List<EnumTypeItem> items = getItems(enumTypeId);
      if (items != null)
      {
        for (EnumTypeItem item : items)
        {
          if (enumTypeItemId.equals(item.getEnumTypeItemId()))
          {
            return item;
          }
        }
      }
    }
    
    //Item is not in cache -> Load EnumType
    try
    {
      EnumTypeItem item = getPort().loadEnumTypeItem(enumTypeItemId);
      String enumTypeId = item.getEnumTypeId();
      getItems(enumTypeId); //load enumtype in cache
      return item;
    }
    catch (Exception ex)
    {
      return null; //item not found
    }
  }

  
  public void clear()
  {
    clear(System.currentTimeMillis());
  }

  public void clear(String enumTypeId)
  {
    map.remove(enumTypeId);
  }

  // Private methods  
  
  private DictionaryManagerPort getPort()
  {
    try
    {
      WSDirectory wsDirectory = WSDirectory.getInstance();
      WSEndpoint endpoint =
        wsDirectory.getEndpoint(DictionaryManagerService.class);
      String userId = MatrixConfig.getProperty("adminCredentials.userId");
      String password = MatrixConfig.getProperty("adminCredentials.password");
      return endpoint.getPort(DictionaryManagerPort.class, userId, password);
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  private boolean mustClearItems(long nowMillis)
  {
    return nowMillis - lastClearMillis > CLEAR_MILLIS;
  }

  private void clear(long nowMillis)
  {
    map.clear();
    lastClearMillis = nowMillis;
  }

  private EnumTypeCacheMBean getCacheMBean()
  {
    try
    {
      return new EnumTypeCacheMBean();
    }
    catch (NotCompliantMBeanException ex)
    {
      return null;
    }
  }

  public class EnumTypeCacheMBean extends StandardMBean implements CacheMBean
  {
    public EnumTypeCacheMBean() throws NotCompliantMBeanException
    {
      super(CacheMBean.class);
    }

    @Override
    public String getName()
    {
      return "EnumTypeCache";
    }

    @Override
    public long getMaxSize()
    {
      return MAX_SIZE;
    }

    @Override
    public long getSize()
    {
      return map.size();
    }

    @Override
    public String getDetails()
    {
      return "Map size=" + getSize() + "/" + getMaxSize();
    }

    @Override
    public void clear()
    {
      EnumTypeCache.this.clear();
    }

    @Override
    public void update()
    {
    }
  }

}
