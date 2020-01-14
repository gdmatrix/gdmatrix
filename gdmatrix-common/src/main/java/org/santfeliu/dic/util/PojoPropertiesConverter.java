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
package org.santfeliu.dic.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.santfeliu.util.PojoUtils;

/**
 *
 * @author blanquepa
 */
public class PojoPropertiesConverter implements PropertiesConverter
{
  private Object object;

  public PojoPropertiesConverter(Object object)
  {
    this.object = object;
  }

  public void toPropertiesMap(Map<String, Object> properties) throws Exception
  {
    Class objClass = object.getClass();
    Method[] methods = objClass.getMethods();
    for (Method objMethod : methods)
    {
      String name = objMethod.getName();
      if (name.startsWith("get") &&
          objMethod.getParameterTypes().length == 0)
      {
        String propName = name.substring(3, 4).toLowerCase() + name.substring(4);
        Object value = objMethod.invoke(object, new Object[0]);
        if (isBasicType(objMethod.getReturnType()))
        {
          String sValue = null;
          if (value != null)
          {
            sValue = String.valueOf(value);
            if (sValue.length() == 0) //Avoid "" values
              sValue = null;
          }
          properties.put(propName, sValue);
        }
        else if (List.class.isAssignableFrom(objMethod.getReturnType()))
        {
          //Have items basic types?
          List items = new ArrayList();
          items.addAll((List)value);
          if (items != null && items.size() > 0 && items.get(0) != null
            && isBasicType(items.get(0).getClass()))
          properties.put(propName, items);
        }
      }
    }
  }
  
  public void fromPropertiesMap(Map<String, Object> properties) throws Exception
  {
    List<String> removeList = new ArrayList();
    for (Map.Entry property : properties.entrySet())
    {
      String name = (String)property.getKey();
      Object value = property.getValue();
      if (setProperty(object, name, value))
        removeList.add(name);
    }
    for (String removeProperty : removeList)
    {
      properties.remove(removeProperty);
    }
  }

  private boolean setProperty(Object object, String propertyName, Object value)
  {
    return PojoUtils.setStaticProperty(object, propertyName, value);
  }

  private static boolean isBasicType(Class cls)
  {
    if (cls == byte.class) return true;
    if (cls == char.class) return true;
    if (cls == short.class) return true;
    if (cls == int.class) return true;
    if (cls == long.class) return true;
    if (cls == float.class) return true;
    if (cls == double.class) return true;
    if (Number.class.isAssignableFrom(cls)) return true;
    if (cls == String.class) return true;
    if (cls == byte[].class) return true;
    if (Enum.class.isAssignableFrom(cls)) return true;
    return false;
  }
}
