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

import java.util.List;
import java.util.Map;
import org.matrix.dic.Property;
import org.santfeliu.util.PojoUtils;

/**
 * This class converts all properties (static and dynamic) of an object to/from 
 * properties map. It assumes that object has a getProperty method that returns
 * all DynamicProperties.
 *
 * @author blanquepa
 */
public class ObjectPropertiesConverter implements PropertiesConverter
{
  Object object;
  PojoPropertiesConverter pojoConverter;
  DynamicPropertiesConverter dynConverter;

  public ObjectPropertiesConverter(Object object)
  {
    this.object = object;
    pojoConverter = new PojoPropertiesConverter(object);

    Object properties = PojoUtils.getStaticProperty(object, "property");
    if (properties != null && properties instanceof List)
    {
      dynConverter = new DynamicPropertiesConverter(
        (List)properties, Property.class);
    }
  }

  public void toPropertiesMap(Map<String, Object> properties) throws Exception
  {
    if (dynConverter != null)
      dynConverter.toPropertiesMap(properties);
    pojoConverter.toPropertiesMap(properties);
  }

  public void fromPropertiesMap(Map<String, Object> properties) throws Exception
  {
    pojoConverter.fromPropertiesMap(properties);
    if (dynConverter != null)
      dynConverter.fromPropertiesMap(properties);
  }
}
