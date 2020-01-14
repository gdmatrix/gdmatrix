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

import java.io.Serializable;
import java.util.Comparator;
import org.matrix.dic.PropertyDefinition;

/**
 *
 * @author blanquepa
 */
public class TypePropertyComparator implements Comparator<String>, Serializable
{
  private String propName;

  public TypePropertyComparator(String propName)
  {
    this.propName = propName;
  }

  public int compare(String o1, String o2)
  {
    if (o1 != null && o2 != null)
    {
      Type t1 = TypeCache.getInstance().getType(o1);
      Type t2 = TypeCache.getInstance().getType(o2);

      if (t1 != null && t2 != null)
      {
        PropertyDefinition p1 = t1.getPropertyDefinition(propName);
        String v1 =
          (p1 != null && !p1.getValue().isEmpty() ? p1.getValue().get(0) : "");
        PropertyDefinition p2 = t2.getPropertyDefinition(propName);
          String v2 =
          (p2 != null && !p2.getValue().isEmpty() ? p2.getValue().get(0) : "");

        return v1.compareTo(v2);
      }
      else if (t1 == null)
        return -1;
      else if (t2 == null)
        return 1;
    }

    return 0;
  }
}
