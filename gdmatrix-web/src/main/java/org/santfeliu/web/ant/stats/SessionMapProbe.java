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
package org.santfeliu.web.ant.stats;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

/**
 *
 * @author unknown
 */
public class SessionMapProbe extends MapProbe
{

  @Override
  public void processLine(Line line)
  {
    try
    {
      Date dt = df.parse(line.getDate());
      calendar.setTime(dt);
      int dia = calendar.get(Calendar.DAY_OF_WEEK);
      int hour = calendar.get(Calendar.HOUR_OF_DAY);
      if (visits[dia - 1][hour] == null)
      {
        visits[dia - 1][hour] = new HashSet();
      }
      ((HashSet)visits[dia - 1][hour]).add(line.getSessionId());
    }
    catch (Exception ex)
    {
    }
  }

  protected int getCellValue(int day, int hour)
  {
    return ((HashSet)visits[day][hour]).size();
  }

}
