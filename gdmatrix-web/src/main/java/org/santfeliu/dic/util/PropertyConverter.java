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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
  private final Type type;
  private boolean htmlFixing = true;

  public static Object toObject(PropertyType type, String svalue)
  {
    Object value = null;
    if (svalue != null)
    {
      if (null != type)
      switch (type)
      {
        case TEXT:
          value = svalue;
          break;
        case NUMERIC:
          try
          {
            value = Double.valueOf(svalue);
          }
          catch (NumberFormatException ex)
          {
            value = null;
          }
          break;
        case BOOLEAN:
          value = Boolean.valueOf(svalue);
          break;
        case DATE:
          // dates are always internally represented as yyyyMMddHHmmss
          value = svalue;
          break;
        default:
          break;
      }
    }
    return value;
  }

  public static String toString(PropertyType type, Object value)
  {
    return value == null ? null : value.toString();
  }

  public PropertyConverter()
  {
    this(null);
  }

  public PropertyConverter(Type type)
  {
    this.type = type;
  }

  public boolean isHtmlFixing()
  {
    return htmlFixing;
  }

  public void setHtmlFixing(boolean htmlFixing)
  {
    this.htmlFixing = htmlFixing;
  }

  public Map toPropertyMap(List<Property> properties)
  {
    Map map = new TreeMap();
    for (Property property : properties)
    {
      String propertyName = property.getName();
      Object value;

      if (type == null)
      {
        List<String> values = property.getValue();
        if (values.size() > 1)
        {
          map.put(propertyName, property.getValue());
        }
        else if (values.size() == 1)
        {
          map.put(propertyName, property.getValue().get(0));
        }
      }
      else
      {
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

      if (type == null)
      {
        Property property = new Property();
        property.setName(propertyName);
        if (value instanceof List)
        {
          List valueList = (List)value;
          for (Object item : valueList)
          {
            property.getValue().add(String.valueOf(item));
          }
          propertyList.add(property);
        }
        else if (value instanceof String ||
                 value instanceof Number ||
                 value instanceof Boolean)
        {
          property.getValue().add(String.valueOf(value));
          propertyList.add(property);
        }
      }
      else
      {
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
              if (htmlFixing && isHtml(svalue))
                svalue = fixHtml(svalue);
              property.getValue().add(svalue);
            }
          }
        }
        else if (value != null)
        {
          String svalue = toString(pt, value);
          if (htmlFixing && isHtml(svalue))
            svalue = fixHtml(svalue);
          property.getValue().add(svalue);
        }

        if (!property.getValue().isEmpty())
        {
          propertyList.add(property);
        }
      }
    }
    return propertyList;
  }

  private boolean isHtml(String value)
  {
    return value != null && value.trim().startsWith("<") && 
      value.trim().endsWith(">");
  }

  private String fixHtml(String svalue)
  {
      String scriptName = MatrixConfig.getProperty("htmlFixer.script");
      if (scriptName != null)
      {
        HtmlFixer htmlFixer = new HtmlFixer(scriptName);
        return htmlFixer.fixCode(svalue);
      }
      else
        return svalue;
  }  
}
