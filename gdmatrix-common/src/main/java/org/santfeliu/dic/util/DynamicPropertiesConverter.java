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
import java.util.List;
import java.util.Map;
import org.santfeliu.util.PojoUtils;

/**
 *
 * @author blanquepa
 */
public class DynamicPropertiesConverter implements PropertiesConverter
{
  private List properties;
  private Class propertyClass;
  private String namePropertyName = "name";
  private String valuePropertyName = "value";

  public DynamicPropertiesConverter(List properties, Class propertyClass)
  {
    this.properties = properties;
    this.propertyClass = propertyClass;
  }

  public DynamicPropertiesConverter(List properties, Class propertyClass,
    String namePropertyName, String valuePropertyName)
  {
    this(properties, propertyClass);
    this.namePropertyName = namePropertyName;
    this.valuePropertyName = valuePropertyName;
  }

  public void toPropertiesMap(Map<String, Object> map) throws Exception
  {
    for (Object property : properties)
    {
      Class objClass = property.getClass();
      Method nameGetter = objClass.getMethod(getMethodName("get", namePropertyName));
      Method valueGetter = objClass.getMethod(getMethodName("get", valuePropertyName));

      String propName = String.valueOf(nameGetter.invoke(property, new Object[0]));
      Object propValue = valueGetter.invoke(property, new Object[0]);

      if (isBasicType(valueGetter.getReturnType()))
        map.put(propName, (propValue != null ? String.valueOf(propValue) : null));
      else if (List.class.isAssignableFrom(valueGetter.getReturnType()))
      {
        //Have items basic types?
        List items = (List)propValue;
        if (items != null && items.size() > 0 && items.get(0) != null
          && isBasicType(items.get(0).getClass()))
        map.put(propName, propValue);
      }
    }
  }
  
  public void fromPropertiesMap(Map<String, Object> map) throws Exception
  {
    for (Map.Entry property : map.entrySet())
    {
      String name = (String)property.getKey();
      Object value = property.getValue();
      setProperty(name, value);
    }
  }

  private String getMethodName(String prefix, String propName)
  {
    String methodName = null;
    if (propName != null)
      methodName = prefix + propName.substring(0, 1).toUpperCase() +
        propName.substring(1);

    return methodName;
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

  private void setProperty(String propertyName, Object value) throws Exception
  {
    PojoUtils.setDynamicProperty(properties, propertyName, value,
      propertyClass, namePropertyName, valuePropertyName);
  }
}
