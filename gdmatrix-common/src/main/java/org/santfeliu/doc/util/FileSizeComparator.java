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
package org.santfeliu.doc.util;

import java.io.Serializable;
import java.util.Comparator;

/**
 *
 * @author blanquepa
 */
public class FileSizeComparator implements Comparator, Serializable
{
  public int compare(Object o1, Object o2)
  {
    int result = 0;

    if (o1 == null && o2 != null)
        result = -1;
    else if (o1 == null && o2 == null)
        result = 0;
    else if (o1 != null && o2 == null)
        result = 1;
    else
    {
      if (o1 instanceof String && o2 instanceof String)
      {
        String s1 = (String)o1;
        String s2 = (String)o2;

        Long size1 = Long.valueOf(DocumentUtils.getSize(s1));
        Long size2 = Long.valueOf(DocumentUtils.getSize(s2));
        result = size1.compareTo(size2);
      }
      else if (o1 instanceof Long && o2 instanceof Long)
      {
        result = ((Long)o1).compareTo((Long)o2);
      }
    }
    return result;
  }

}
