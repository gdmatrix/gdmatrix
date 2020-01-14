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
package org.santfeliu.util;

import java.util.Comparator;
import org.santfeliu.util.PojoUtils;

/**
 *
 * @author blanquepa
 */
public class PojoComparator implements Comparator<Object>
{
  private String[] sortProperties;

  public PojoComparator(String sortProperty)
  {
    sortProperties = sortProperty.split("\\.");
  }

  public int compare(Object o1, Object o2)
  {
    if (o1 != null && o2 != null)
    {
      for (int i = 0; i < sortProperties.length; i++)
      {
        String propName = sortProperties[i];
        if (propName != null)
        {
          if (PojoUtils.hasStaticProperty(o1.getClass(), propName))
          {
            o1 = PojoUtils.getStaticProperty(o1, propName);
            o2 = PojoUtils.getStaticProperty(o2, propName);
          }
        }
      }
      if (o1 != null && o2 != null)
        return String.valueOf(o1).compareTo(String.valueOf(o2));
      else
        return 0;
    }
    else
      return 0;
  }
}
