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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

/**
 *
 * @author realor
 */
public class SimpleSchedule
{
  private int startHour = 8;
  private int startMinute = 0;
  private int endHour = 15;
  private int endMinute = 0;
  private final HashSet<String> holidays = new HashSet<String>();
  private long ellapsed;
  private int workingDays;
  private int intervalDays;

  public SimpleSchedule()
  {
    holidays.add("0101");
    holidays.add("0601");
    holidays.add("0105");
    holidays.add("2406");
    holidays.add("1508");
    holidays.add("1109");
    holidays.add("1010");
    holidays.add("0111");
    holidays.add("0612");
    holidays.add("0812");
    holidays.add("2512");
    holidays.add("2612");
  }
  
  public HashSet<String> getHolidays()
  {
    return holidays;
  }
  
  public int getStartHour()
  {
    return startHour;
  }

  public void setStartHour(int startHour)
  {
    this.startHour = startHour;
  }

  public int getStartMinute()
  {
    return startMinute;
  }

  public void setStartMinute(int startMinute)
  {
    this.startMinute = startMinute;
  }

  public int getEndHour()
  {
    return endHour;
  }

  public void setEndHour(int endHour)
  {
    this.endHour = endHour;
  }

  public int getEndMinute()
  {
    return endMinute;
  }

  public void setEndMinute(int endMinute)
  {
    this.endMinute = endMinute;
  }

  public void setInterval(String startDateTime, String endDateTime)
  {
    Date start = TextUtils.parseInternalDate(startDateTime);
    Date end = TextUtils.parseInternalDate(endDateTime);
    setInterval(start, end);
  }

  public void setInterval(Date start, Date end)
  {
    long dif = end.getTime() - start.getTime();
    if (dif < 0)
    {
      ellapsed = 0;
      intervalDays = 0;
      workingDays = 0;      
    }
    else if (dif < 14L * 3600L * 1000L) // 14 hours
    {
      ellapsed = dif;
      intervalDays = sameDay(start, end) ? 1 : 2;
      workingDays = intervalDays;
    }
    else
    {
      intervalDays = 0;
      ellapsed = 0;
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(start);

      workingDays = 0;

      while (calendar.getTime().compareTo(end) < 0)
      {
        long millis = calendar.getTimeInMillis();
        workingDays++;
        intervalDays++;

        calendar.set(Calendar.HOUR_OF_DAY, endHour);
        if (calendar.getTime().compareTo(end) > 0)
        {
          calendar.setTime(end);
        }
        long millis2 = calendar.getTimeInMillis();
        dif = millis2 - millis;
        if (dif > 0)
        {
          ellapsed += dif;
        }
        
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, startHour);

        while (isHoliday(calendar) && 
          calendar.getTime().compareTo(end) < 0)
        {
          intervalDays++;
          calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
      }
    }
  }

  public boolean sameDay(Date start, Date end)
  {
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
    String dstart = df.format(start);
    String dend = df.format(end);
    return dstart.equals(dend);
  }
  
  public long getEllapsedMillis()
  {
    return ellapsed;
  }

  public double getEllapsedHours()
  {
    return (double)ellapsed / (1000.0 * 3600.0);
  }
  
  public int getWorkingDays()
  {
    return workingDays;
  }

  public int getIntervalDays()
  {
    return intervalDays;
  }
  
  public boolean isHoliday(Calendar calendar)
  {
    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
    if (dayOfWeek == Calendar.SATURDAY) return true;
    if (dayOfWeek == Calendar.SUNDAY) return true;
    Date date = calendar.getTime();
    SimpleDateFormat df = new SimpleDateFormat("ddMM");
    String dayMonth = df.format(date);
    return holidays.contains(dayMonth);
  }
  
  public static void main(String[] args)
  {
    SimpleSchedule schedule = new SimpleSchedule();
    schedule.setInterval("20140101080000", "20140109150000");
    System.out.println("Hores: " + schedule.getEllapsedHours());
    System.out.println("Days: " + schedule.getWorkingDays());
    System.out.println("Interval Days: " + schedule.getIntervalDays());
  }
}
