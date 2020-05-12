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


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.matrix.agenda.Event;
import org.matrix.agenda.EventFilter;
import org.matrix.dic.Property;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.util.PojoUtils;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.obj.PageBean;

/**
 *
 * @author blanquepa
 */
public class EventRecurrencesBean extends PageBean
{
  public static final String MASTER_EVENTID_PROPERTY = "masterEventId";
  public static final String FIRST_AVAILABLE_DATE = "00010102000000";
  private List<Event> rows;

  private boolean currentCollapsed = false;
  private boolean newCollapsed = true;

  public EventRecurrencesBean()
  {
    load();
  }

  @Override
  public String show()
  {
    return "event_recurrences";
  }
  
  @Override
  public String store()
  {
    //If exists recurrences preview refresh this with new event data
    EventCopyBean eventCopyBean = (EventCopyBean)getBean("eventCopyBean");
    if (eventCopyBean != null && eventCopyBean.getEvent() != null)
      newSchedule();
    return show();
  }  

  public List<Event> getRows()
  {
    return rows;
  }

  public void setRows(List<Event> rows)
  {
    this.rows = rows;
  }

  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }

  public boolean isCurrentCollapsed()
  {
    return currentCollapsed;
  }

  public void setCurrentCollapsed(boolean currentCollapsed)
  {
    this.currentCollapsed = currentCollapsed;
  }

  public boolean isNewCollapsed()
  {
    return newCollapsed;
  }

  public void setNewCollapsed(boolean newCollapsed)
  {
    this.newCollapsed = newCollapsed;
  }

  public Date getStartDateTime()
  {
    Event row = (Event) getValue("#{row}");
    if (row != null)
    {
      String startDateTime = row.getStartDateTime();
      try
      {
        return TextUtils.parseInternalDate(startDateTime);
      }
      catch (Exception e)
      {
        return null;
      }
    }
    else
      return null;
  }


  public Date getEndDateTime()
  {
    Event row = (Event) getValue("#{row}");
    if (row != null)
    {
      String endDateTime = row.getEndDateTime();
      try
      {
        return TextUtils.parseInternalDate(endDateTime);
      }
      catch (Exception e)
      {
        return null;
      }
    }
    else
      return null;
  }

  public String deleteAllRecurrences()
  {
    try
    {
      if (rows != null && !rows.isEmpty())
      {
        for (Event row : rows)
        {
          if (!row.getEventId().equals(getObjectId()))
          {
            AgendaConfigBean.getPort().removeEvent(row.getEventId());
          }
        }
        
        //if current event isn't the master then change it to master
        if (!isMasterEvent())
        {
          Event event = getEvent();
          DictionaryUtils.setProperty(event, MASTER_EVENTID_PROPERTY, event.getEventId());
          AgendaConfigBean.getPort().storeEvent(event);
        }
        
        load();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }

    return null;
  }

  public final void newSchedule()
  {
    Event event = getEvent();
    EventCopyBean eventCopyBean = (EventCopyBean)getBean("eventCopyBean");
    if (eventCopyBean == null)
      eventCopyBean = new EventCopyBean();
    else
      eventCopyBean.reset();
    eventCopyBean.setEvent(event);
    eventCopyBean.setMasterEventId(getMasterEventId());
  }

  public String copyRecurrences()
  {
    EventCopyBean eventCopyBean = (EventCopyBean)getBean("eventCopyBean");
    if (eventCopyBean != null)
      eventCopyBean.copy();
    load();

    this.currentCollapsed = false;
    this.newCollapsed = true;

    return show();
  }



  private Event getEvent()
  {
    EventMainBean mainBean = (EventMainBean)getBean("eventMainBean");
    return mainBean.getEvent();
  }

  private String getMasterEventId()
  {
    Event event = getEvent();
    if (event != null)
    {
      List<String> masterEventValue;
      try
      {
        masterEventValue =
          (List<String>) PojoUtils.getDynamicProperty(event.getProperty(), MASTER_EVENTID_PROPERTY);
          if (masterEventValue != null)
            return masterEventValue.get(0);
      }
      catch (Exception ex)
      {
      }

      return event.getEventId();
    }
    return null;
  }

  private boolean isMasterEvent()
  {
    Event event = getEvent();
    if (event != null)
    {
      List<String> masterEventValue;
      try
      {
        masterEventValue =
          (List<String>) PojoUtils.getDynamicProperty(event.getProperty(), MASTER_EVENTID_PROPERTY);
          if (masterEventValue != null)
            return masterEventValue.get(0).equals(event.getEventId());
      }
      catch (Exception ex)
      {
      }
      return true;
    }
    else
      return false;
  }

  private void load()
  {
    try
    {
      if (!isNew())
      {
        String filterEventId = null;
        Event masterEvent = null;
        if (!isMasterEvent()) //Is a recurring event
        {
          filterEventId = getMasterEventId();
          if (filterEventId != null)
          {
            try
            {
              EventFilter filter = new EventFilter();
              filter.getEventId().add(filterEventId);
              filter.setStartDateTime(FIRST_AVAILABLE_DATE);
              List<Event> events =
                AgendaConfigBean.getPort().findEventsFromCache(filter);
              if (events != null && events.size() > 0)
                masterEvent = events.get(0);
            }
            catch(Exception ex)
            {
              masterEvent = null;
            }
          }
        }
        else
          filterEventId = getEvent().getEventId();

        rows = new ArrayList();
        if (masterEvent != null)
          rows.add(masterEvent);

        if (filterEventId != null)
        {
          EventFilter filter = new EventFilter();
          Property masterEventProperty = new Property();
          masterEventProperty.setName(MASTER_EVENTID_PROPERTY);
          masterEventProperty.getValue().add(filterEventId);
          filter.getProperty().add(masterEventProperty);
          filter.setStartDateTime(FIRST_AVAILABLE_DATE);

          List<Event> events =
            AgendaConfigBean.getPort().findEventsFromCache(filter);
          for (Event e : events)
          {
            if (!e.getEventId().equals(getObjectId()) && !e.getEventId().equals(filterEventId))
              rows.add(e);
          }
        }
        //Start a new recurrences schedule
        newSchedule();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  

}
