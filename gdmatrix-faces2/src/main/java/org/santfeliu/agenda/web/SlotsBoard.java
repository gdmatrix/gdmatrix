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
import java.util.LinkedList;

/**
 *
 * @author blanquepa
 */
public class SlotsBoard
{
  private LinkedList<ScheduleSlot> busySlots = null;
 
  public void addBusySlot(long startDate, long endDate)
  {
    ScheduleSlot newSlot = new ScheduleSlot(startDate, endDate);
    if (busySlots == null)
    {
      busySlots = new LinkedList();
      busySlots.add(newSlot);
    }
    else
    {
      LinkedList<ScheduleSlot> slotsToMerge = new LinkedList<ScheduleSlot>();
      int i = 0;
      for  (i = 0; i < busySlots.size(); i++)
      {
        ScheduleSlot slot = busySlots.get(i);
        if (slot.isIntersectedBy(newSlot)) //Intersection of slots
        {
          slotsToMerge.add(slot);
          
          if (slot.getStartTime() < newSlot.getStartTime())
            newSlot.setStartTime(slot.getStartTime());
          if (slot.getEndTime() >= newSlot.getEndTime()) //Is last slot to merge
          {
            newSlot.setEndTime(slot.getEndTime());            
            busySlots.add(i, newSlot);
            break;
          }
        }
        else if (slot.getStartTime() > newSlot.getEndTime()) 
        {
          if (slotsToMerge.isEmpty())
          {
            busySlots.add(i, newSlot);
            break;
          }
        }
        else if (getEndTime() <= newSlot.getStartTime()) //Adds to last position
        {
          //if (slotsToMerge.isEmpty()) //Only adds slot if previously not found slots merge to
          busySlots.add(newSlot);
          break;
        }
      }
      if (!busySlots.contains(newSlot))
        busySlots.add(newSlot);
      
      if (!slotsToMerge.isEmpty())
      {
        busySlots.removeAll(slotsToMerge);
      }
    }
    
    Collections.sort(busySlots);
  }
  
  public LinkedList<ScheduleSlot> getFreeSlots(long startDate, long endDate, long slotTime, long stepTime)
  {
    LinkedList<ScheduleSlot> freeSlots = new LinkedList<ScheduleSlot>();
    long d1 = startDate;
    long d2 = endDate;
    if (busySlots != null)
    {
      for (ScheduleSlot slot : busySlots)
      {
        d2 = slot.getStartTime();
        ScheduleSlot freeSlot = new ScheduleSlot(d1 + 1, d2);
        freeSlots.addAll(freeSlot.split(slotTime, stepTime));
        d1 = slot.getEndTime();
      }
    }
    if (d1 < endDate)
    {
      ScheduleSlot freeSlot = new ScheduleSlot(d1, endDate);
      freeSlots.addAll(freeSlot.split(slotTime));
    }
    return freeSlots;
  }

  private long getEndTime()
  {
    if (busySlots == null || busySlots.isEmpty())
      return 0;
    else
      return busySlots.peekLast().getEndTime();
  }  
  
  @Override
  public String toString()
  {
    return busySlots.toString();
  }
     
}

