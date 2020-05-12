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
package org.santfeliu.web.obj;

import java.lang.reflect.Field;
import java.util.List;

/**
 *
 * @author unknown
 */
public class PageFinder
{
  public static int findFirstRowIndex(List objectList, int rowsPerPage,
    String fieldName, Object fieldValue)
  {
    boolean found = false;
    int rowIndex = 0;
    for (int i = 0; i < objectList.size() && !found; i++)
    {
      Object object = objectList.get(i);
      if (isObjectMatch(object, fieldName, fieldValue))
      {
        found = true;
        rowIndex = i;
      }
    }
    if (found)
    {
      return rowsPerPage * (rowIndex / rowsPerPage);
    }
    else return 0;
  }

  private static boolean isObjectMatch(Object object, String fieldName,
    Object fieldValue)
  {
    try
    {
      Class<?> c = object.getClass();
      Field f = c.getDeclaredField(fieldName);
      f.setAccessible(true);      
      return fieldValue.equals(f.get(object));
    }
    catch (NoSuchFieldException ex)
    {
      return false;
    }
    catch (IllegalAccessException ex)
    {
      return false;
    }
  }

}
