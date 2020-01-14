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
package org.santfeliu.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author blanquepa
 */
public class PojoUtils
{
  public static boolean setStaticProperty(Object pojo, String name, Object value)
  {
    //System properties
    Class docClass = pojo.getClass();
    Method[] docClassMethods = docClass.getDeclaredMethods();
    boolean propertyFound = false;
    int i = 0;
    while (i < docClassMethods.length && !propertyFound)
    {
      Method docClassMethod = docClassMethods[i];
      String methodName = docClassMethod.getName();
      if (methodName.equalsIgnoreCase("set" + name))
      {
        Class[] parameterTypes = docClassMethod.getParameterTypes();
        Class parameterType = parameterTypes[0];
        try
        {
          if (!(List.class.isAssignableFrom(parameterType)))
          {
            if (value != null)
            {
              if (parameterType.equals(value.getClass()))
                docClassMethod.invoke(pojo, new Object[]{value});
              else if (List.class.isAssignableFrom(value.getClass()))
              {
                List valueList = (List)value;
                if (valueList != null && valueList.size() > 0)
                  docClassMethod.invoke(pojo, new Object[]{valueList.get(0)});
                else
                  docClassMethod.invoke(pojo, new Object[]{null});
              }
            }
            else
              docClassMethod.invoke(pojo, new Object[]{null});
          }
          propertyFound = true;
        }
        catch (Exception e)
        {
          propertyFound = false;
        }
      }
      else if (methodName.equalsIgnoreCase("get" + name))
      {
        Class returnType = docClassMethod.getReturnType();
        if (List.class.isAssignableFrom(returnType))
        {
          try
          {
            List values =
              (List)docClassMethod.invoke(pojo, new Object[]{});
            values.clear();
            if (value != null && List.class.isAssignableFrom(value.getClass()))
            {
              for (int j = 0; j < ((List)value).size(); j++)
              {
                values.add(((List)value).get(j));
              }
            }
            else if (value != null)
              values.add(value);
            propertyFound = true;
          }
          catch (Exception e)
          {
            propertyFound = false;
          }
        }
      }
      i++;
    }

    return propertyFound;
  }

  public static Object getStaticProperty(Object pojo, String name)
  {
    Object value = null;

    if (name != null)
    {
      Class pojoClass = pojo.getClass();

      String methodName = name.substring(0, 1).toUpperCase() + name.substring(1);
      Method method = null;
      try
      {
        method = pojoClass.getMethod("get" + methodName);
      }
      catch (Exception ex1)
      {
        try
        {
          method = pojoClass.getMethod("is" + methodName);
        }
        catch (Exception ex2)
        {
        }
      }

      if (method != null)
      {
        try
        {
          value = method.invoke(pojo, new Object[0]);
        }
        catch(Exception ex)
        {
        }
      }
    }

    return value;
  }

  public static Object getDeepStaticProperty(Object pojo, String name)
  {
    String[] props = name.split("\\.");
    if (props.length > 1)
    {
      Object obj = pojo;
      for (int i = 0; i < props.length; i++)
      {
        if (obj == null)
          return null;
        else if (props[i].contains("[") && props[i].contains("]"))
          obj = getListValue(obj, props[i]);
        else
          obj = getStaticProperty(obj, props[i]);
      }
      return obj;
    }
    else if (name.contains("[") && name.contains("]"))
      return getListValue(pojo, name);
    else
      return getStaticProperty(pojo, name);
  }

  private static Object getListValue(Object obj, String propName)
  {
    String name = propName.substring(0, propName.indexOf("["));
    String index = propName.substring(propName.indexOf("[") + 1, propName.indexOf("]"));
    obj = PojoUtils.getStaticProperty(obj, name);
    if (obj != null && obj instanceof List)
    {
      try
      {
        int i = Integer.parseInt(index);
        return ((List)obj).get(i);
      }
      catch (NumberFormatException ex)
      {
        try
        {
          return getDynamicProperty((List) obj, index);
        }
        catch (Exception ex1)
        {
          return null;
        }
      }
    }
    return null;
  }

  public static boolean hasStaticProperty(Class pojoClass, String name)
  {
    boolean result = false;

    if (name != null)
    {
      String methodName = name.substring(0, 1).toUpperCase() + name.substring(1);
      Method method = null;
      try
      {
        method = pojoClass.getMethod("get" + methodName);
        if (method != null)
          result = true;
      }
      catch (Exception ex1)
      {
        try
        {
          method = pojoClass.getMethod("is" + methodName);
          if (method != null)
            result = true;
        }
        catch (Exception ex2)
        {
        }
      }
    }
    return result;
  }

  public static boolean setDynamicProperty(List properties,
    String propertyName, Object value, Class propertyClass) 
    throws Exception
  {
    return setDynamicProperty(properties, propertyName, value, propertyClass, 
      "name", "value");
  }

  public static boolean setDynamicProperty(List properties,
    String propertyName, Object value, Class propertyClass,
    String namePropertyName, String valuePropertyName)
    throws Exception
  {
    boolean propertyFound = false;

    if (value != null)
    {
      for (Object property : properties)
      {
        Method nameGetter =
          propertyClass.getMethod(getMethodName("get", namePropertyName));
        String propName = (String)nameGetter.invoke(property, new Object[0]);
        if (propertyName.equals(propName))
        {
          propertyFound = true;
          Method valueGetter =
            propertyClass.getMethod(getMethodName("get", valuePropertyName));
          if (List.class.isAssignableFrom(valueGetter.getReturnType()))
          {
            List propValue = (List)valueGetter.invoke(property, new Object[0]);
            List result = new ArrayList();
            if (List.class.isAssignableFrom(value.getClass()))
              result.addAll((List)value);
            else
              result.add(value);
            propValue.clear();
            propValue.addAll(result);
          }
          else
          {
            Method valueSetter =
              propertyClass.getMethod(getMethodName("set", valuePropertyName));
            if (valueSetter != null)
            valueSetter.invoke(property, new Object[]{value});
          }
        }
      }

      if (!propertyFound)
      {
        Object property = propertyClass.newInstance();
        Method nameSetter =
          propertyClass.getMethod(getMethodName("set", namePropertyName), String.class);
        nameSetter.invoke(property, new Object[]{propertyName});

        Method valueGetter =
          propertyClass.getMethod(getMethodName("get", valuePropertyName));
        if (List.class.isAssignableFrom(valueGetter.getReturnType()))
        {
          List propValue = (List)valueGetter.invoke(property, new Object[0]);
          if (List.class.isAssignableFrom(value.getClass()))
            propValue.addAll((List)value);
          else
            propValue.add(value);
        }
        else
        {
          Method valueSetter =
            propertyClass.getMethod(getMethodName("set", valuePropertyName), value.getClass());
          if (valueSetter != null)
          valueSetter.invoke(property, new Object[]{value});
        }

        properties.add(property);
      }
    }

    return propertyFound;
  }

  public static Object getDynamicProperty(List properties, String name)
    throws Exception
  {
    return getDynamicProperty(properties, name, "name", "value");
  }

  public static Object getDynamicProperty(List properties, String name,
    String namePropertyName, String valuePropertyName)
    throws Exception
  {
    Object result = null;
    Iterator it = properties.iterator();
    boolean found = false;
    while (it.hasNext() && !found)
    {
      Object property = it.next();
      Class objClass = property.getClass();
      Method nameGetter = objClass.getMethod(getMethodName("get", namePropertyName));
      Method valueGetter = objClass.getMethod(getMethodName("get", valuePropertyName));

      String propName = String.valueOf(nameGetter.invoke(property, new Object[0]));
      if (name.equals(propName))
      {
        result = valueGetter.invoke(property, new Object[0]);
        found = true;
      }
    }

    return result;
  }
  
  public static void copy(Object src, Object dest)
  {
    Class srcClass = src.getClass();
    Class destClass = dest.getClass();
    Method[] methods = srcClass.getMethods();
    for (Method srcMethod : methods)
    {
      String name = srcMethod.getName();
      //System.out.println(name);
      if (name.startsWith("get") && 
          srcMethod.getParameterTypes().length == 0 && 
          isBasicType(srcMethod.getReturnType()))
      {
        try
        {
          Method destMethod = destClass.getMethod(
            "set" + name.substring(3), 
            new Class[]{srcMethod.getReturnType()});          
          Object value = srcMethod.invoke(src, new Object[0]);
          destMethod.invoke(dest, new Object[]{value});
        }
        catch (NoSuchMethodException e)
        {
        }
        catch (Exception ex)
        {
          ex.printStackTrace();
        }
      }
    }
  }

  public static void copyList(List srcList, List destList, Class destItemClass)
  {
    for(Object srcItem : srcList)
    {
      Object destItem;
      try
      {
        destItem = destItemClass.newInstance();
        PojoUtils.copy(srcItem, destItem);
        destList.add(destItem);
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
  }  

  private static String getMethodName(String prefix, String propName)
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
}
