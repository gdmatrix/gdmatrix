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
package org.santfeliu.signature;

import java.util.HashMap;
import java.util.Map;

import org.matrix.signature.Property;
import org.matrix.signature.PropertyList;

/**
 *
 * @author unknown
 */
public class PropertyListConverter
{
  public static Map toMap(PropertyList propertyList)
  {
    if (propertyList == null) return null;
    HashMap properties = new HashMap();
    for (Property property : propertyList.getProperty())
    {
      properties.put(property.getName(), property.getValue());
    }
    return properties;
  }
  
  public static PropertyList toPropertyList(Map properties)
  {
    if (properties == null) return null;
    PropertyList propertyList = new PropertyList();
    for (Object e : properties.entrySet())
    {
      Map.Entry entry = (Map.Entry)e;
      Property property = new Property();
      property.setName((String)entry.getKey());
      property.setValue((String)entry.getValue());
      propertyList.getProperty().add(property);
    }
    return propertyList;
  }
}
