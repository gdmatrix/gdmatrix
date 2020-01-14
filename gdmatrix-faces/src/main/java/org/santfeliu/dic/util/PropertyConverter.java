package org.santfeliu.dic.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.matrix.dic.Property;
import org.matrix.dic.PropertyDefinition;
import org.matrix.dic.PropertyType;
import org.santfeliu.dic.Type;
import org.santfeliu.doc.util.HtmlFixer;
import org.santfeliu.util.MatrixConfig;

/**
 *
 * @author realor
 */

/*
 * converts from internal String representation (Property) to
 * internal Object representation (typed) and viceversa
 */
public class PropertyConverter
{
  private Type type;
 
  public static Object toObject(PropertyType type, String svalue)
  {
    Object value = null;
    if (svalue != null)
    {
      if (PropertyType.TEXT.equals(type))
      {
        value = svalue;
      }
      else if (PropertyType.NUMERIC.equals(type))
      {
        try
        {
          value = Double.valueOf(svalue);
        }
        catch (NumberFormatException ex)
        {
          value = null;
        }
      }
      else if (PropertyType.BOOLEAN.equals(type))
      {
        value = Boolean.valueOf(svalue);
      }
      else if (PropertyType.DATE.equals(type))
      {
        // dates are always internally represented as yyyyMMddHHmmss
        value = svalue;
      }
    }
    return value;
  }

  public static String toString(PropertyType type, Object value)
  {
    return value == null ? null : value.toString();
  }

  public PropertyConverter(Type type)
  {
    this.type = type;
  }

  public Map toPropertyMap(List<Property> properties)
  {
    Map map = new HashMap();
    for (Property property : properties)
    {
      String propertyName = property.getName();
      Object value;

      PropertyDefinition pd = type.getPropertyDefinition(propertyName);
      if (pd == null) // free property, no definition
      {
        // assume type is TEXT
        if (property.getValue().size() == 1)
        {
          String svalue = property.getValue().get(0);
          value = svalue;
        }
        else
        {
          // set all values
          value = property.getValue();
        }
      }
      else // defined property
      {
        int maxOccurs = pd.getMaxOccurs();
        if (maxOccurs == 1) // single value property
        {
          if (property.getValue().isEmpty())
          {
            value = null;
          }
          else
          {
            String svalue = property.getValue().get(0);
            value = toObject(pd.getType(), svalue);
          }
        }
        else
        {
          List valueList = new ArrayList();
          for (String svalue : property.getValue())
          {
            valueList.add(toObject(pd.getType(), svalue));
          }
          value = valueList;
        }
      }
      map.put(propertyName, value);
    }
    return map;
  }

  public List<Property> toPropertyList(Map map)
  {
    List<Property> propertyList = new ArrayList();
    Iterator<Map.Entry> iter = map.entrySet().iterator();
    while (iter.hasNext())
    {
      Map.Entry entry = iter.next();
      String propertyName = (String)entry.getKey();
      Object value = entry.getValue();
      PropertyDefinition pd = type.getPropertyDefinition(propertyName);
      PropertyType pt = (pd == null) ? PropertyType.TEXT : pd.getType();
      Property property = new Property();
      property.setName(propertyName);
      if (value instanceof List)
      {
        List valueList = (List)value;
        for (Object v : valueList)
        {
          if (v != null)
          {
            String svalue = toString(pt, v);
            if (isHtml(svalue))
              svalue = fixHtml(svalue);
            property.getValue().add(svalue);
          }
        }
      }
      else if (value != null)
      {
        String svalue = toString(pt, value);
        if (isHtml(svalue))
          svalue = fixHtml(svalue);        
        property.getValue().add(svalue);
      }
      
      if (!property.getValue().isEmpty())
      {
        propertyList.add(property);
      }
    }
    return propertyList;
  }
  
  private boolean isHtml(String value)
  {
    return value != null && value.startsWith("<") && value.endsWith(">");
  }
  
  private String fixHtml(String svalue)
  {
      String scriptName = MatrixConfig.getProperty("htmlFixer.script");
      if (scriptName != null)
      {
        String userId = MatrixConfig.getProperty("adminCredentials.userId");
        String password = MatrixConfig.getProperty("adminCredentials.password");
        HtmlFixer htmlFixer = new HtmlFixer(scriptName, userId, password);
        return htmlFixer.fixCode(svalue);
      }
      else
        return svalue;
  }
}
