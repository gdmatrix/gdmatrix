package org.santfeliu.web.bean;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.santfeliu.util.ClassFinder;

/**
 *
 * @author realor
 */
public class CMSManagedBeanIntrospector
{
  private static List<Class> beanClasses;

  public List<Class> getBeanClasses()
  {
    if (beanClasses == null)
    {
      beanClasses = ClassFinder.findClasses("org.santfeliu", ".*Bean",
        CMSManagedBean.class);
    }
    return beanClasses;
  }

  public List<String> getBeanNames()
  {
    ArrayList<String> beanNames = new ArrayList<String>();    
    List<Class> classes = getBeanClasses();
    for (Class cls : classes)
    {
      String className = cls.getSimpleName();
      String beanName = className.substring(0, 1).toLowerCase() +
        className.substring(1);
      beanNames.add(beanName);
    }
    return beanNames;
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
      CMSManagedBeanIntrospector introspector = new CMSManagedBeanIntrospector();

      System.out.println(introspector.getBeanClasses());
      System.out.println(introspector.getBeanNames());
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
