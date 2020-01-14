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

import java.io.Serializable;
import java.util.List;
import org.matrix.dic.PropertyDefinition;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;

/**
 *
 * @author blanquepa
 */
public class InternalValueConverter implements Serializable
{
  private static final String INTERNAL_VALUE_PROPERTY = "internalValue";

  private String rootTypeId;

  public InternalValueConverter(String rootTypeId)
  {
    this.rootTypeId = rootTypeId;
  }

  public String fromTypeId(String typeId)
  {
    String value = typeId;
    Type type = TypeCache.getInstance().getType(typeId);
    if (type != null)
    {
      PropertyDefinition pd = 
        type.getPropertyDefinition(INTERNAL_VALUE_PROPERTY);
      if (pd != null && pd.getValue().size() > 0)
        value = pd.getValue().get(0);
    }

    return value;
  }

  public String getTypeId(String value)
  {
    String typeId = rootTypeId;

    if (value != null)
    {
      Type rootType = TypeCache.getInstance().getType(rootTypeId);

      List<Type> childTypes = rootType.getDerivedTypes(true);
      for (Type type : childTypes)
      {
        PropertyDefinition pd =
          type.getPropertyDefinition(INTERNAL_VALUE_PROPERTY);
        if (pd != null && pd.getValue().size() > 0 &&
            value.equals(pd.getValue().get(0)))
        {
          return type.getTypeId();
        }
      }
    }

    return typeId;
  }
}
