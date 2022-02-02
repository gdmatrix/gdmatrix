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
package org.matrix.pf.cms;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.santfeliu.util.ClassFinder;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSProperty;

/**
 *
 * @author realor
 */
public class CMSClassIntrospector
{
  private static Map<String, List<Class>> beanClasses = new HashMap();
  private String packageName;
  private String pattern;
  private Class clazz;
  
  public CMSClassIntrospector()
  {
    this("org", ".*", CMSContent.class);
  }
  
  public CMSClassIntrospector(String packageName, String pattern, Class clazz)
  {
    this.packageName = packageName;
    this.pattern = pattern;
    this.clazz = clazz;
  }
    
  public List<Class> getBeanClasses()
  {
    List<Class> beanList = beanClasses.get(packageName + ";" + pattern);
    if (beanList == null || beanList.isEmpty())
    {
      beanList = ClassFinder.findClasses(packageName, pattern, clazz);
      beanClasses.put(packageName + ";" + pattern, beanList); 
    }
    return beanList;
  }
    
  public Map<String, CMSAction> getActions(Class beanClass)
          throws Exception
  {
    HashMap<String, CMSAction> actions = new HashMap<String, CMSAction>();
    Method[] methods = beanClass.getMethods();
    for (Method method : methods)
    {
      CMSAction annotation = method.getAnnotation(CMSAction.class);
      if (annotation != null)
      {
        actions.put(method.getName(), annotation);
      }
    }
    return actions;
  }

  //With inheritance
  public Map<String, CMSProperty> getProperties(Class beanClass)
    throws Exception
  {
    HashMap<String, CMSProperty> properties =
      new HashMap<String, CMSProperty>();
    Field[] fields = beanClass.getFields();
    for (Field field : fields)
    {
      CMSProperty annotation = field.getAnnotation(CMSProperty.class);
      if (annotation != null)
      {
        Object value = field.get(null);
        if (value instanceof String)
        {
          String propertyName = (String) value;
          properties.put(propertyName, annotation);
        }
      }
    }
    return properties;
  }

  //Without inheritance
  public Map<String, CMSProperty> getDirectProperties(Class beanClass)
    throws Exception
  {
    HashMap<String, CMSProperty> properties =
      new HashMap<String, CMSProperty>();
    Field[] fields = beanClass.getDeclaredFields();
    for (Field field : fields)
    {
      CMSProperty annotation = field.getAnnotation(CMSProperty.class);
      if (annotation != null)
      {
        Object value = field.get(null);
        if (value instanceof String)
        {
          String propertyName = (String) value;
          properties.put(propertyName, annotation);
        }
      }
    }
    return properties;
  }

  public static void main(String[] args)
  {
    try
    {
      CMSClassIntrospector introspector = new CMSClassIntrospector();

      System.out.println(introspector.getBeanClasses());
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
