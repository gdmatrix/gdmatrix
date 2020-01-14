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
package org.santfeliu.agenda.web;

import java.util.Collections;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.santfeliu.agenda.web.MeetingFinderBean.Room;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author blanquepa
 */
  public class ScheduleSlot implements Comparable, Serializable
  {
    private long startTime;
    private long endTime;
    private List<Room> availableRooms;
    
    public ScheduleSlot(long startTime, long endTime)
    {
      this.startTime = startTime;
      this.endTime = endTime;
    }

    public long getStartTime()
    {
      return startTime;
    }

    public void setStartTime(long startTime)
    {
      this.startTime = startTime;
    }

    public long getEndTime()
    {
      return endTime;
    }

    public void setEndTime(long endTime)
    {
      this.endTime = endTime;
    }
    
    public long getDuration()
    {
      return this.endTime - this.startTime;
    }
    
    public String getStartDateTime()
    {
      if (this.startTime == 0)
        return "";
      Date date = new Date(this.startTime);
      return TextUtils.formatDate(date, "E dd/MM/yyyy HH:mm");
    }
    
    public String getEndDateTime()
    {
      if (this.endTime == 0)
        return "";
      Date date = new Date(this.endTime);
      return TextUtils.formatDate(date, "E dd/MM/yyyy HH:mm");
    }    
    
    public LinkedList<ScheduleSlot> split(long duration)
    {
      LinkedList<ScheduleSlot> result = new LinkedList();
      for (long i = startTime; i <= (endTime - duration); i = i + duration)
      {
        result.add(new ScheduleSlot(i, i + duration));
      }
      return result;
    }
    
    public LinkedList<ScheduleSlot> split(long duration, long stepTime)
    {
      LinkedList<ScheduleSlot> result = new LinkedList();
      if (stepTime <= 0)
        stepTime = duration;
      for (long stime = startTime; stime < (startTime + duration); stime = stime + stepTime)
      {
        for (long i = stime; (i <= endTime - duration); i = i + duration)
        {
          result.add(new ScheduleSlot(i, i + duration));
        }
      }
      Collections.sort(result);
      return result;
    }    
    
    public boolean isIntersectedBy(ScheduleSlot slot)
    {
      if (slot == null)
        return false;
      return (slot.startTime <= this.endTime && slot.endTime >= this.startTime);
    }
    
    public String toString()
    {
      return "{"+ startTime + "," + endTime + "}";
    }

    public int compareTo(Object o)
    {
      ScheduleSlot slot = (ScheduleSlot)o;
      long result = (this.startTime - slot.startTime);
      if (result == 0)
        result = (this.endTime - slot.endTime);
      
      if (result < 0) 
        return -1;
      else
        return 1;
    }

    public List<Room> getAvailableRooms()
    {
      return availableRooms;
    }

    public void addAvailableRoom(Room availableRoom)
    {
      if (this.availableRooms == null)
        this.availableRooms = new ArrayList();
      this.availableRooms.add(availableRoom);
    }
    
    public String printAvailableRooms()
    {
      if (this.availableRooms != null)
        return this.availableRooms.toString();
      else
        return "[]";
    }
    
  }
