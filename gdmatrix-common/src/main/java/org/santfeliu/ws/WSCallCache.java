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
package org.santfeliu.ws;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import org.apache.commons.collections.LRUMap;
import org.santfeliu.jmx.CacheMBean;
import org.santfeliu.jmx.JMXUtils;
import org.santfeliu.util.MatrixConfig;

/**
 *
 * @author lopezrj
 */
public class WSCallCache
{      
  private String moduleName;
  private Map<String, Object> map; //input_serialization -> object/list(objects)
  private long lastClearMillis = System.currentTimeMillis();
  private long clearInterval; //in millis  
  private int maxSize;
  
  private static long DEFAULT_CLEAR_INTERVAL = 10 * 60 * 1000; //10 minutes
  private static int DEFAULT_MAX_SIZE = 200; //items in cache
  private static String UNKNOWN_USER = "WS_UNKNOWN_USER"; 

  public WSCallCache(String moduleName)
  {    
    this(moduleName, getClearInterval(moduleName), getMaxSize(moduleName));
  }

  public WSCallCache(String moduleName, long clearInterval, int maxSize)
  {
    this.moduleName = moduleName;
    this.clearInterval = clearInterval;
    this.maxSize = maxSize;
    this.map = Collections.synchronizedMap(new LRUMap(maxSize));
    JMXUtils.registerMBean("WSCallCache_" + moduleName, getCacheMBean());
  }
  
  public String getModuleName()
  {
    return moduleName;
  }

  public void clear()
  {    
    map.clear();
    lastClearMillis = System.currentTimeMillis();
  }

  @Override
  public String toString() //TODO
  {
    return map.toString();
  }

  public Object getCallResult(Object port, String methodName, Object[] params)
  {
    return getCallResult(port, methodName, params, null);
  }

  public Object getCallResult(Object port, String methodName, Object[] params, 
    Object[] modifiedParams)
  {
    if (mustClear())
    {
      clear();
    }
    Map requestContext = ((BindingProvider)port).getRequestContext();
    String userId = 
      (String)requestContext.get(BindingProvider.USERNAME_PROPERTY);

    String key = getKey(port, userId, methodName, 
      (modifiedParams == null ? params : modifiedParams));
    if (!map.containsKey(key))
    {
      try
      {
        Class portClass = port.getClass();
        Method[] methods = portClass.getDeclaredMethods();    
        for (Method method : methods)
        {     
          if (method.getName() != null && method.getName().equals(methodName))
          {
            Object result = method.invoke(port, params);
            map.put(key, result);
          }
        }                
      }
      catch (Exception ex)
      {
        throw new WebServiceException(ex);
      }
    }
    return map.get(key);    
  }    

  private static long getClearInterval(String moduleName)
  {
    String baseName = WSCallCache.class.getName();
    String value = MatrixConfig.getProperty(baseName + "." + moduleName + 
      ".clearInterval");
    return (value == null ? DEFAULT_CLEAR_INTERVAL : Long.parseLong(value));
  }

  private static int getMaxSize(String moduleName)
  {
    String baseName = WSCallCache.class.getName();
    String value = MatrixConfig.getProperty(baseName + "." + moduleName + 
      ".maxSize");
    return (value == null ? DEFAULT_MAX_SIZE : Integer.parseInt(value));
  }  
  
  private boolean mustClear()
  {
    return (System.currentTimeMillis() > lastClearMillis + clearInterval);
  }

  private String getKey(Object port, String userId, String methodName, 
    Object[] params)
  {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    try
    {
      ObjectOutputStream oos = new ObjectOutputStream(buffer);
      oos.writeObject(port.getClass().getName());
      oos.writeChars(userId != null ? userId : UNKNOWN_USER);
      oos.writeChars(methodName);
      oos.writeObject(params);      
      oos.close();
    }
    catch (Exception ex) { }
    return buffer.toString();
  }
  
  /* Caché info */
  
  WSCallCacheMBean getCacheMBean()
  {
    try
    {
      return new WSCallCacheMBean();
    }
    catch (NotCompliantMBeanException ex)
    {
      return null;
    }
  }

  public class WSCallCacheMBean extends StandardMBean implements CacheMBean
  {
    public WSCallCacheMBean() throws NotCompliantMBeanException
    {
      super(CacheMBean.class);
    }

    public String getName()
    {
      return "WSCallCache_" + getModuleName();
    }

    public long getMaxSize()
    {
      return maxSize;
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
    
    private String getNextCleaningInfo()
    {    
      try
      {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date d = new Date(lastClearMillis + clearInterval);
        return format.format(d);
      }
      catch (Exception ex)
      {
        return "";
      }    
    }      

  }  
  
}  
