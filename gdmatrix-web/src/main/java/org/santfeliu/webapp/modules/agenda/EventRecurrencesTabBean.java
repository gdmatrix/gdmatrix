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
package org.santfeliu.webapp.modules.agenda;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.agenda.Event;
import org.matrix.agenda.EventFilter;
import org.matrix.dic.Property;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.util.PojoUtils;
import org.santfeliu.util.TextUtils;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;

/**
 *
 * @author lopezrj-sf
 */
@Named
@ViewScoped
public class EventRecurrencesTabBean extends TabBean
{
  public static final String MASTER_EVENTID_PROPERTY = "masterEventId";
  public static final String FIRST_AVAILABLE_DATE = "00010102000000";

  private List<Event> rows;

  private int firstRow;

  private String deleteDateTime;
  private String deleteMode = "0";

  @Inject
  EventObjectBean eventObjectBean;

  @Inject
  EventCopyTabBean eventCopyTabBean;

  @Inject
  EventFinderBean eventFinderBean;

  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);
    load();
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return eventObjectBean;
  }

  public List<Event> getRows()
  {
    return rows;
  }

  public void setRows(List<Event> rows)
  {
    this.rows = rows;
  }

  public int getFirstRow()
  {
    return firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    this.firstRow = firstRow;
  }

  public String getDeleteMode()
  {
    return deleteMode;
  }

  public void setDeleteMode(String deleteMode)
  {
    this.deleteMode = deleteMode;
  }

  public String getDeleteDateTime()
  {
    return deleteDateTime;
  }

  public void setDeleteDateTime(String deleteDateTime)
  {
    this.deleteDateTime = deleteDateTime;
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
      catch (Exception ex)
      {
      }
    }
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
      catch (Exception ex)
      {
      }
    }
    return null;
  }

  public String deleteRecurrences()
  {
    try
    {
      int deleteCount = 0;
      if (deleteMode.equals("1"))
      {
        deleteCount = deleteAllRecurrences();
      }
      else if (deleteMode.equals("0"))
      {
        if (deleteDateTime == null)
          error("INVALID_DATE");
        else
          deleteCount = deleteFutureRecurrences(deleteDateTime);
      }
      if (deleteCount > 0)
      {
        eventFinderBean.outdate();
        growl("RECURRENCES_DELETED", new Object[]{deleteCount});
      }
      else
        growl("RECURRENCES_NOT_DELETED");
    }
    catch (Exception ex)
    {
      error(ex);
    }
    load();
    return null;
  }

  @Override
  public void load()
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
                AgendaModuleBean.getClient().findEventsFromCache(filter);
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
            AgendaModuleBean.getClient().findEventsFromCache(filter);
          for (Event e : events)
          {
            if (!e.getEventId().equals(getObjectId()) &&
              !e.getEventId().equals(filterEventId))
              rows.add(e);
          }
        }
        //Start a new recurrences schedule
        newSchedule();

        if (deleteDateTime == null)
          deleteDateTime = getEvent().getStartDateTime();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void newSchedule()
  {
    Event event = getEvent();
    if (eventCopyTabBean == null)
      eventCopyTabBean = new EventCopyTabBean();
    else
      eventCopyTabBean.reset();
    eventCopyTabBean.setEvent(event);
    eventCopyTabBean.setMasterEventId(getMasterEventId());
  }

  public void copyRecurrences()
  {
    if (eventCopyTabBean != null)
      eventCopyTabBean.copy();
    load();
    eventFinderBean.outdate();
  }

  private int deleteFutureRecurrences(String deleteDateTime)
    throws Exception
  {
    int deleteCount = 0;
    if (rows != null && !rows.isEmpty())
    {
      deleteCount = deleteRecurrences(rows, deleteDateTime);
    }
    return deleteCount;
  }

  private int deleteAllRecurrences()
    throws Exception
  {
    int deleteCount = 0;
    if (rows != null && !rows.isEmpty())
    {
      deleteCount = deleteRecurrences(rows);

      //if current event isn't the master then change it to master
      if (!isMasterEvent())
      {
        Event event = getEvent();
        DictionaryUtils.setProperty(event, MASTER_EVENTID_PROPERTY,
          event.getEventId());
        AgendaModuleBean.getClient().storeEvent(event);
      }
    }
    return deleteCount;
  }

  private int deleteRecurrences(List<Event> events)
    throws Exception
  {
    return deleteRecurrences(events, null);
  }

  private int deleteRecurrences(List<Event> events, String sdt)
    throws Exception
  {
    int deleteCount = 0;
    for (Event event : events)
    {
      if (!event.getEventId().equals(getObjectId()))
      {
        boolean delete = (sdt == null);
        if (sdt != null)
        {
          Date eventDate =
            TextUtils.parseInternalDate(event.getStartDateTime());
          Date startDate = TextUtils.parseInternalDate(sdt);
          delete = startDate.before(eventDate);
        }

        if (delete)
        {
          AgendaModuleBean.getClient().removeEvent(event.getEventId());
          deleteCount++;
        }
      }
    }
    return deleteCount;
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
          (List<String>) PojoUtils.getDynamicProperty(event.getProperty(),
            MASTER_EVENTID_PROPERTY);
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

  private Event getEvent()
  {
    return eventObjectBean.getEvent();
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
          (List<String>) PojoUtils.getDynamicProperty(event.getProperty(),
            MASTER_EVENTID_PROPERTY);
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

}
