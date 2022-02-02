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
package org.santfeliu.web.obj;

import java.io.Serializable;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import org.apache.commons.collections.LRUMap;
import org.apache.commons.lang.StringUtils;
import org.matrix.web.Describable;
import org.santfeliu.jmx.CacheMBean;
import org.santfeliu.jmx.JMXUtils;

/**
 *
 * @author realor
 */
public class ObjectDescriptionCache implements Serializable
{
  private static final int MAX_SIZE = 200;
  private static final ObjectDescriptionCache defaultInstance = 
    new ObjectDescriptionCache();

  private final LRUMap map;

  public ObjectDescriptionCache()
  {
    map = new LRUMap(MAX_SIZE);
    try
    {
      JMXUtils.registerMBean("ObjectDescriptionCache", new MBean());
    }
    catch (Exception ex)
    {
    }
  }

  public static ObjectDescriptionCache getInstance()
  {
    return defaultInstance;
  }

  public String getDescription(Describable objectBean, String objectId)
  {
    if (ControllerBean.NEW_OBJECT_ID.equals(objectId))
    {
      return "";
    }
    else
    {
      String typeId = objectBean.getObjectTypeId();
      String key = getKey(typeId, objectId);
      synchronized (map)
      {
        String description = (String)map.get(key);
        if (StringUtils.isBlank(description))
        {
          description = loadDescription(objectBean, objectId);
          map.put(key, description);
        }
        return description;
      }
    }
  }

  public void clearDescription(Describable objectBean, String objectId)
  {
    String typeId = objectBean.getObjectTypeId();
    String key = getKey(typeId, objectId);
    synchronized (map)
    {
      map.remove(key);
    }
  }

  // private methods

  private String getKey(String typeId, String objectId)
  {
    StringBuilder builder = new StringBuilder(typeId);
    builder.append(":");
    builder.append(objectId);
    return builder.toString();
  }

  private String loadDescription(Describable objectBean, String objectId)
  {
    String description;
    String currentObjectId = objectBean.getObjectId();
    if (currentObjectId != null && currentObjectId.equals(objectId))
    {
      description = objectBean.getDescription();
    }
    else
    {
      description = objectBean.getDescription(objectId);
    }
    return description;
  }

  public class MBean extends StandardMBean implements CacheMBean
  {
    public MBean() throws NotCompliantMBeanException
    {
      super(CacheMBean.class);
    }

    @Override
    public String getName()
    {
      return "ObjectDescriptionCache";
    }

    @Override
    public long getMaxSize()
    {
      return map.getMaximumSize();
    }

    @Override
    public long getSize()
    {
      return map.size();
    }

    @Override
    public String getDetails()
    {
      return "ObjectDescriptionCache: " +
        map.size() + "/" + map.getMaximumSize();
    }

    @Override
    public void clear()
    {
      synchronized (map)
      {
        map.clear();
      }
    }

    @Override
    public void update()
    {
      synchronized (map)
      {
        map.clear();
      }
    }
  }
}
