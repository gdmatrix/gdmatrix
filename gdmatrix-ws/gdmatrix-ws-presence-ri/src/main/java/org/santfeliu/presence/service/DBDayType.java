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

import org.santfeliu.presence.util.Utils;
import org.matrix.presence.DayType;
import org.santfeliu.jpa.JPAUtils;

/**
 *
 * @author realor
 */
public class DBDayType extends DayType
{
  public String getHolidaysValue()
  {
    return holidaysEnabled ? "T" : "F";
  }
  
  public void setHolidaysValue(String value)
  {
    holidaysEnabled = "T".equals(value);
  }

  public String getReductionsValue()
  {
    return reductionsEnabled ? "T" : "F";
  }
  
  public void setReductionsValue(String value)
  {
    reductionsEnabled = "T".equals(value);
  }
  
  public void copyFrom(DayType dayType)
  {
    JPAUtils.copy(dayType, this);
    this.holidaysEnabled = dayType.isHolidaysEnabled();
    this.reductionsEnabled = dayType.isReductionsEnabled();
  }

  public int getDuration1()
  {
    return Utils.getTimesDuration(inTime1, outTime1);
  }

  public int getDuration2()
  {
    return inTime2 == null ? 0 : Utils.getTimesDuration(inTime2, outTime2);
  }
  
  public int getDuration()
  {
    return getDuration1() + getDuration2();
  }
  
  public String getStartDateTimeFor(String date)
  {
    return date + inTime1;
  }

  public String getEndDateTimeFor(String date)
  {
    return outTime2 == null ? date + outTime1 : date + outTime2;
  }
  
  @Override
  public String toString()
  {
    return "(" + dayTypeId + (code != null ? " " + code : "") + 
      " " + inTime1 + "-" + outTime1 +
      " / " + inTime2 + "-" + outTime2 + ")";
  }
}
