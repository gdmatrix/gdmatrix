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
package org.santfeliu.ant.ws;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.santfeliu.dic.util.DictionaryUtils;

/**
 *
 * @author blanquepa
 */
public class DefaultWSImporter implements WSImporter
{
  private final String methodName;
  private final Object[] parameters;
  private List<String> fullLog;
  private List<String> summaryLog;
  
  public DefaultWSImporter(String methodName, Object...parameters)
  {
    this.methodName = methodName;
    this.parameters = parameters;
  }
  
  public Object execute(Connection conn, Object port) throws Exception
  {
    Class portClass = port.getClass();
    Method[] methods = portClass.getMethods();
    for (Method method : methods)
    {
      if (methodName.equals(method.getName()))
      {
        if (parameters != null)
        {
          for (int i = 0; i < parameters.length; i++)
          {
            Object parameter = parameters[i];
            if (parameter instanceof Map)
            {
              Class paramClass = method.getParameterTypes()[i];
              Object paramInstance = paramClass.newInstance();
              Set keys = ((Map)parameter).keySet();
              for (Object key : keys)
              {
                DictionaryUtils.setProperty(paramInstance, String.valueOf(key), ((Map)parameter).get(key));
              }
              parameters[i] = paramInstance;
            }
          }
          return method.invoke(port, parameters);
        }
      }
    }
    return null;
  }

  public List<String> getFullLog()
  {
    return fullLog;
  }

  public List<String> getSummaryLog()
  {
    return summaryLog;
  }
  
}
