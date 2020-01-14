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
package org.santfeliu.presence.util;

import java.util.Calendar;
import java.util.Date;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author realor
 */
public class Utils
{
  public static int getDuration(String startDateTime, String endDateTime)
  {
    Date date1 = TextUtils.parseInternalDate(startDateTime);
    Date date2 = TextUtils.parseInternalDate(endDateTime);
    return (int)((date2.getTime() - date1.getTime()) / 1000L);
  }
  
  public static int getTimesDuration(String inTime, String outTime)
  {
    String startDate = "20000101";
    String endDate = inTime.compareTo(outTime) < 0 ? startDate : "20000102";
    
    Date date1 = TextUtils.parseInternalDate(startDate + inTime);
    Date date2 = TextUtils.parseInternalDate(endDate + outTime);
    return (int)((date2.getTime() - date1.getTime()) / 1000L);        
  }
  
  public static int compareDates(String date1, String date2)
  {
    if (date1 == null && date2 == null) return 0;
    if (date1 == null) return 1;
    if (date2 == null) return -1;
    return date1.compareTo(date2);
  }

  public static String addDate(String date, int days)
  {
    Date d = TextUtils.parseInternalDate(date);
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(d);
    calendar.add(Calendar.DAY_OF_MONTH, days);
    d = calendar.getTime();
    return TextUtils.formatDate(d, "yyyyMMdd");
  }  

  public static String addDateTime(String dateTime, int seconds)
  {
    Date d = TextUtils.parseInternalDate(dateTime);
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(d);
    calendar.add(Calendar.SECOND, seconds);
    d = calendar.getTime();
    return TextUtils.formatDate(d, "yyyyMMddHHmmss");
  }
  
  public static String shrink(String startDateTime, String endDateTime, 
    int seconds)
  {
    String dateTime = Utils.addDateTime(endDateTime, -seconds);
    if (Utils.compareDates(startDateTime, dateTime) < 0)
    {
      endDateTime = dateTime;
    }
    return endDateTime;
  }
}
