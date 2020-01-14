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
package org.santfeliu.dic;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.PropertyDefinition;
import org.matrix.dic.PropertyType;
import org.matrix.dic.Type;
/**
 *
 * @author realor
 */
public class RootTypeFactory
{
  private static final HashMap<String, Type> rootTypes = new HashMap();

  public static Type getRootType(String rootTypeId)
  {
    Class typeClass = DictionaryConstants.rootTypeClasses.get(rootTypeId);
    if (typeClass == null) return null; // not a root type

    Type type = rootTypes.get(rootTypeId);
    if (type == null)
    {
      type = new Type();
      type.setTypeId(rootTypeId);
      type.setDescription(rootTypeId);
      type.setInstantiable(true);
      type.setRestricted(false);
      type.setTypePath(DictionaryConstants.TYPE_PATH_SEPARATOR +
        rootTypeId + DictionaryConstants.TYPE_PATH_SEPARATOR);
      type.setCreationDateTime("20100101000000");
      type.setCreationUserId("admin");
      type.setChangeDateTime("20100101000000");
      type.setChangeUserId("admin");

      try
      {
        BeanInfo beanInfo = Introspector.getBeanInfo(typeClass);
        PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor descriptor : descriptors)
        {
          String name = descriptor.getName();
          Class propertyClass = descriptor.getPropertyType();
          boolean isAttribute;
          try
          {
            isAttribute = typeClass.getDeclaredField(name).
              isAnnotationPresent(XmlAttribute.class);
          }
          catch (Exception ex)
          {
            isAttribute = false;
          }

          if (!isAttribute)
          {
            if (propertyClass == String.class)
            {
              PropertyDefinition pd = new PropertyDefinition();
              pd.setName(descriptor.getName());
              pd.setDescription(descriptor.getName());
              if (name.endsWith("Date"))
                pd.setType(PropertyType.DATE);
              else if (name.endsWith("DateTime"))
                pd.setType(PropertyType.DATE); // future: change to DATETIME
              else
                pd.setType(PropertyType.TEXT);
              pd.setMinOccurs(0);
              pd.setMaxOccurs(1);
              type.getPropertyDefinition().add(pd);
            }
            else if (propertyClass == int.class ||
              propertyClass == Integer.class ||
              propertyClass == double.class ||
              propertyClass == Double.class)
            {
              PropertyDefinition pd = new PropertyDefinition();
              pd.setName(descriptor.getName());
              pd.setDescription(descriptor.getName());
              pd.setType(PropertyType.NUMERIC);
              pd.setMinOccurs(0);
              pd.setMaxOccurs(1);
              type.getPropertyDefinition().add(pd);
            }
            else if (propertyClass == boolean.class ||
              propertyClass == Boolean.class)
            {
              PropertyDefinition pd = new PropertyDefinition();
              pd.setName(descriptor.getName());
              pd.setDescription(descriptor.getName());
              pd.setType(PropertyType.BOOLEAN);
              pd.setMinOccurs(0);
              pd.setMaxOccurs(1);
              type.getPropertyDefinition().add(pd);
            }
            else if (propertyClass == List.class &&
              name.endsWith("Id"))
            {
              PropertyDefinition pd = new PropertyDefinition();
              pd.setName(descriptor.getName());
              pd.setDescription(descriptor.getName());
              pd.setType(PropertyType.TEXT);
              pd.setMinOccurs(0);
              pd.setMaxOccurs(0);
              type.getPropertyDefinition().add(pd);
            }
          }
        }
      }
      catch (Exception ex)
      {
        // ignore properties
      }
    }
    return type;
  }
}
