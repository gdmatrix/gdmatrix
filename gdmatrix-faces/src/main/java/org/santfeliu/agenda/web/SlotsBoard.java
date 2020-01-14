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

