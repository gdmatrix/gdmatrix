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
package org.santfeliu.presence.service;

import java.util.Calendar;
import org.matrix.presence.WeekType;

/**
 *
 * @author realor
 */
public class DBWeekType extends WeekType
{  
  public void copyFrom(WeekType weekType)
  {
    this.weekTypeId = weekType.getWeekTypeId();
    this.label = weekType.getLabel();
    this.mondayTypeId = weekType.getMondayTypeId();
    this.tuesdayTypeId = weekType.getTuesdayTypeId();
    this.wednesdayTypeId = weekType.getWednesdayTypeId();
    this.thursdayTypeId = weekType.getThursdayTypeId();
    this.fridayTypeId = weekType.getFridayTypeId();
    this.saturdayTypeId = weekType.getSaturdayTypeId();
    this.sundayTypeId = weekType.getSundayTypeId();
  }

  @Override
  public String toString()
  {
    return "(" + weekTypeId + " " + 
      mondayTypeId + " " + 
      tuesdayTypeId + " " +  
      wednesdayTypeId + " " +  
      thursdayTypeId + " " +  
      fridayTypeId + " " + 
      saturdayTypeId + " " + 
      sundayTypeId + ")";
  }
  
  public String getDayTypeId(int dayOfWeek)
  {
    String dayTypeId = null;
    switch (dayOfWeek)
    {
      case Calendar.MONDAY: 
        dayTypeId = mondayTypeId; 
        break;
      case Calendar.TUESDAY: 
        dayTypeId = tuesdayTypeId; 
        break;
      case Calendar.WEDNESDAY: 
        dayTypeId = wednesdayTypeId; 
        break;
      case Calendar.THURSDAY: 
        dayTypeId = thursdayTypeId; 
        break;
      case Calendar.FRIDAY: 
        dayTypeId = fridayTypeId; 
        break;
      case Calendar.SATURDAY: 
        dayTypeId = saturdayTypeId; 
        break;
      case Calendar.SUNDAY: 
        dayTypeId = sundayTypeId; 
        break;
    }
    return dayTypeId;
  }
}
