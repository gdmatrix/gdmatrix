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
package org.santfeliu.misc.widget;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import org.santfeliu.jmx.CacheMBean;
import org.santfeliu.jmx.JMXUtils;

/**
 *
 * @author lopezrj
 */
public class WidgetCache
{  
  private static final long CLEAR_TIME = 10 * 60 * 1000; // 10 minutes  
  
  private static WidgetCache cache; 
  
  //nodeId;username -> objects
  private Map<String, List> map = new HashMap<String, List>();   
  private long lastClearMillis;
  
  public static synchronized WidgetCache getInstance()
  {
    if (cache == null)
    {
      cache = new WidgetCache();
      JMXUtils.registerMBean("WidgetCache", cache.getCacheMBean());      
    }
    return cache;
  }
  
  public List getWidgetObjects(String workspaceId, String nodeId)
  {
    return getWidgetObjects(workspaceId, nodeId, null);
  }

  public List getWidgetObjects(String workspaceId, String nodeId, String username)
  {
    if (mustClear())
    {
      clear();
    }
    String key = getKey(workspaceId, nodeId, username);
    return map.get(key);
  }

  public void setWidgetObjects(String workspaceId, String nodeId, List objectList)
  {
    setWidgetObjects(workspaceId, nodeId, null, objectList);
  }
  
  public void setWidgetObjects(String workspaceId, String nodeId, String username, List objectList)
  {
    String key = getKey(workspaceId, nodeId, username);
    map.put(key, objectList);
  }

  public void clear()
  {
    map.clear();
    lastClearMillis = System.currentTimeMillis();
  }  
  
  private String getKey(String workspaceId, String nodeId, String username)
  {
    if (username == null) username = "";
    return workspaceId + ";" + nodeId + ";" + username;
  }
  
  private boolean mustClear()
  {
    return (System.currentTimeMillis() > lastClearMillis + CLEAR_TIME);
  }

  private String getNextCleaningInfo()
  {    
    try
    {
      SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
      Date d = new Date(lastClearMillis + CLEAR_TIME);
      return format.format(d);
    }
    catch (Exception ex)
    {
      return "";
    }    
  }
    
  /* Cache Info */  
  
  WidgetCacheMBean getCacheMBean()
  {
    try
    {
      return new WidgetCacheMBean();
    }
    catch (NotCompliantMBeanException ex)
    {
      return null;
    }
  }

  public class WidgetCacheMBean extends StandardMBean implements CacheMBean
  {
    public WidgetCacheMBean() throws NotCompliantMBeanException
    {
      super(CacheMBean.class);
    }

    public String getName()
    {
      return "WidgetCache";
    }

    public long getMaxSize()
    {
      return Long.MAX_VALUE;
    }

    public long getSize()
    {
      return map.size();
    }

    public String getDetails()
    {
      return "Map size: " + getSize() + " / " + "Next clear: " + 
        getNextCleaningInfo();
    }

    public void clear()
    {
      map.clear();
    }

    public void update()
    {
      map.clear();
    }

  }
  
}
