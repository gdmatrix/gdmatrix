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
package org.santfeliu.web.obj.util;

import java.io.Serializable;
import java.util.List;
import org.matrix.dic.Property;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.util.PojoUtils;

/**
 *
 * @author blanquepa
 */
public class DefaultColumnRenderer extends ColumnRenderer implements Serializable
{
  @Override
  public Object getValue(String columnName, Object row)
  {
    Object value = null;
    if (columnName.contains(".") || (columnName.contains("[") && columnName.contains("]")))
    {
      value = PojoUtils.getDeepStaticProperty(row, columnName);
      if (value instanceof List)
        value = ((List)value).get(0);
    }
    else
    {
      Property property = DictionaryUtils.getProperty(row, columnName);
      if (property != null)
      {
        List values = property.getValue();
        if (!values.isEmpty() && values.size() == 1)
          value = values.get(0);
        else
          value = values;
      }
    }
    return value;
  }

  @Override
  public boolean isValueEscaped()
  {
    return false;
  }
}
