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


import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.agenda.Event;
import org.matrix.agenda.EventFilter;
import org.primefaces.event.schedule.ScheduleEntryMoveEvent;
import org.primefaces.event.schedule.ScheduleEntryResizeEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;
import org.santfeliu.faces.ManualScoped;
import org.santfeliu.util.BigList;
import org.santfeliu.util.TextUtils;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;

/**
 *
 * @author lopezrj-sf
 */
@Named
@ManualScoped
public class EventFinderBean extends FinderBean
{
  private String smartFilter;
  private EventFilter filter = new EventFilter();
  private List<Event> rows;
  private int firstRow;
  private boolean finding;
  private boolean outdated;

  private String searchEventTypeId;
  private String searchEventThemeId;

  //Schedule
  private ScheduleModel eventModel;
  private String serverTimeZone = ZoneId.systemDefault().toString();
  private String scheduleView;
  private LocalDate scheduleInitialDate;

  @Inject
  NavigatorBean navigatorBean;

  @Inject
  EventObjectBean eventObjectBean;

  @Inject
  EventTypeBean eventTypeBean;

  @PostConstruct
  public void init()
  {
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return eventObjectBean;
  }

  public String getSmartFilter()
  {
    return smartFilter;
  }

  public void setSmartFilter(String smartFilter)
  {
    this.smartFilter = smartFilter;
  }

  public EventFilter getFilter()
  {
    return filter;
  }

  public void setFilter(EventFilter filter)
  {
    this.filter = filter;
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

  public String getSearchEventTypeId()
  {
    return searchEventTypeId;
  }

  public void setSearchEventTypeId(String searchEventTypeId)
  {
    this.searchEventTypeId = searchEventTypeId;
  }

  public String getSearchEventThemeId()
  {
    return searchEventThemeId;
  }

  public void setSearchEventThemeId(String searchEventThemeId)
  {
    this.searchEventThemeId = searchEventThemeId;
  }

  public ScheduleModel getEventModel()
  {
    if (eventModel == null)
    {
      eventModel = loadEventModel();
    }
    return eventModel;
  }

  public String getServerTimeZone()
  {
    return serverTimeZone;
  }

  public void setServerTimeZone(String serverTimeZone)
  {
    this.serverTimeZone = serverTimeZone;
  }

  public String getScheduleView()
  {
    return scheduleView;
  }

  public void setScheduleView(String scheduleView)
  {
    this.scheduleView = scheduleView;
  }

  public LocalDate getScheduleInitialDate()
  {
    if (scheduleInitialDate == null)
    {
      if (filter.getStartDateTime() != null)
      {
        scheduleInitialDate =
          toLocalDateTime(filter.getStartDateTime()).toLocalDate();
      }
      else
      {
        scheduleInitialDate = LocalDate.now();
      }
    }
    return scheduleInitialDate;
  }

  public void setScheduleInitialDate(LocalDate scheduleInitialDate)
  {
    this.scheduleInitialDate = scheduleInitialDate;
  }

  @Override
  public String getObjectId(int position)
  {
    return rows == null ? NEW_OBJECT_ID : rows.get(position).getEventId();
  }

  @Override
  public int getObjectCount()
  {
    return rows == null ? 0 : rows.size();
  }

  public Date getFromDate()
  {
    if (filter.getStartDateTime() != null)
      return TextUtils.parseInternalDate(filter.getStartDateTime());
    else
      return null;
  }

  public void setFromDate(Date date)
  {
    if (date != null)
      filter.setStartDateTime(TextUtils.formatDate(date, "yyyyMMddHHmmss"));
    else
      filter.setStartDateTime(null);
  }

  public Date getToDate()
  {
    if (filter.getEndDateTime() != null)
      return TextUtils.parseInternalDate(filter.getEndDateTime());
    else
      return null;
  }

  public void setToDate(Date date)
  {
    if (date != null)
      filter.setEndDateTime(TextUtils.formatDate(date, "yyyyMMddHHmmss"));
    else
      filter.setEndDateTime(null);
  }

  public List<String> getEventId()
  {
    return filter.getEventId();
  }

  public void setEventId(List<String> eventIds)
  {
    filter.getEventId().clear();
    if (eventIds != null && !eventIds.isEmpty())
      filter.getEventId().addAll(eventIds);
  }

  //TODO Move to superclass
  public String getLanguage()
  {
    return getLocale().getLanguage();
  }

  public void onPersonClear()
  {
    filter.setPersonId(null);
  }

  public void onRoomClear()
  {
    filter.setRoomId(null);
  }

  public void onEventSelect(SelectEvent<ScheduleEvent<?>> selectEvent)
  {
    ScheduleEvent<?> event = selectEvent.getObject();
    navigatorBean.view(event.getId());
  }

  public void onDateSelect(SelectEvent<LocalDateTime> selectEvent)
  {
    LocalDateTime localDateTime = selectEvent.getObject();
    info(localDateTime.toString());
  }

  public void onEventMove(ScheduleEntryMoveEvent moveEvent)
  {
    int minutes = (moveEvent.getDayDelta() * 24 * 60) +
      moveEvent.getMinuteDelta();
    LocalDateTime ldtStart = moveEvent.getScheduleEvent().getStartDate();
    ldtStart.plusMinutes(minutes);
    LocalDateTime ldtEnd = moveEvent.getScheduleEvent().getEndDate();
    ldtEnd.plusMinutes(minutes);
    String eventId = moveEvent.getScheduleEvent().getId();
    Event event = AgendaModuleBean.getClient(true).loadEventFromCache(eventId);
    event.setStartDateTime(toDateString(ldtStart));
    event.setEndDateTime(toDateString(ldtEnd));
    updateEvent(event);
    info("Esdeveniment mogut amb èxit");
  }

  public void onEventResize(ScheduleEntryResizeEvent resizeEvent)
  {
    int minutes = resizeEvent.getMinuteDeltaEnd();
    LocalDateTime ldtEnd = resizeEvent.getScheduleEvent().getEndDate();
    ldtEnd.plusMinutes(minutes);
    String eventId = resizeEvent.getScheduleEvent().getId();
    Event event = AgendaModuleBean.getClient(true).loadEventFromCache(eventId);
    event.setEndDateTime(toDateString(ldtEnd));
    updateEvent(event);
    info("Esdeveniment modificat amb èxit");
  }
  
  public void onViewChange(SelectEvent selectEvent)
  {
    String view = (String)selectEvent.getObject();
    setScheduleView(view);
  }

  @Override
  public void smartFind()
  {
    finding = true;
    setTabIndex(0);
    String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
    filter = eventTypeBean.queryToFilter(smartFilter, baseTypeId);
    setFromDate(new Date());
    filter.setDateComparator("1");
    searchEventTypeId = null;
    doFind(true);
    firstRow = 0;
  }

  @Override
  public void find()
  {
    finding = true;
    setTabIndex(1);
    smartFilter = eventTypeBean.filterToQuery(filter);
    filter.getEventTypeId().clear();
    if (!StringUtils.isBlank(searchEventTypeId))
    {
      filter.getEventTypeId().clear();
      filter.getEventTypeId().add(searchEventTypeId);
    }
    if (filter.getStartDateTime() == null)
    {
      setFromDate(new Date());
      filter.setDateComparator("1");
    }
    if (!StringUtils.isBlank(searchEventThemeId))
    {
      filter.getThemeId().clear();
      filter.getThemeId().add(searchEventThemeId);
    }

    doFind(true);
    firstRow = 0;
  }

  public void outdate()
  {
    this.outdated = true;
  }

  public void update()
  {
    if (outdated)
    {
      doFind(false);
    }
  }

  public void clear()
  {
    filter = new EventFilter();
    smartFilter = null;
    setFromDate(new Date());
    filter.setDateComparator("1");
    searchEventTypeId = null;
    searchEventThemeId = null;
    rows = null;
    finding = false;
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ finding, getTabIndex(), filter, firstRow,
      searchEventTypeId, searchEventThemeId, getObjectPosition(),
      scheduleInitialDate, scheduleView };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      finding = (Boolean)stateArray[0];
      setTabIndex((Integer)stateArray[1]);
      filter = (EventFilter)stateArray[2];

      doFind(false);

      firstRow = (Integer)stateArray[3];
      searchEventTypeId = (String)stateArray[4];
      searchEventThemeId = (String)stateArray[5];
      setObjectPosition((Integer)stateArray[6]);
      scheduleInitialDate = (LocalDate)stateArray[7];
      scheduleView = (String)stateArray[8];
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private void doFind(boolean autoLoad)
  {
    try
    {
      if (!finding)
      {
        rows = Collections.EMPTY_LIST;
      }
      else
      {
        rows = new BigList(20, 10)
        {
          @Override
          public int getElementCount()
          {
            try
            {
              return AgendaModuleBean.getClient(false).
                countEventsFromCache(filter);
            }
            catch (Exception ex)
            {
              error(ex);
              return 0;
            }
          }

          @Override
          public List getElements(int firstResult, int maxResults)
          {
            try
            {
              filter.setFirstResult(firstResult);
              filter.setMaxResults(maxResults);
              return AgendaModuleBean.getClient(false).
                findEventsFromCache(filter);
            }
            catch (Exception ex)
            {
              error(ex);
              return null;
            }
          }
        };

        outdated = false;

        if (autoLoad)
        {
          scheduleInitialDate = null;
          if (rows.size() == 1)
          {
            navigatorBean.view(rows.get(0).getEventId());
            eventObjectBean.setSearchTabIndex(eventObjectBean.
              getEditionTabIndex());
          }
          else
          {
            if (eventObjectBean.getSearchTabIndex() ==
              eventObjectBean.getEditionTabIndex())
            {
              eventObjectBean.setSearchTabIndex(0);
            }
          }
        }
      }
      eventModel = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private ScheduleModel loadEventModel()
  {
    eventModel = new DefaultScheduleModel();
    if (rows != null)
    {
      for (Event row : rows)
      {
        DefaultScheduleEvent<?> event = DefaultScheduleEvent.builder()
          .id(row.getEventId())
          .title(row.getSummary())
          .startDate(toLocalDateTime(row.getStartDateTime()))
          .endDate(toLocalDateTime(row.getEndDateTime()))
          .description(row.getSummary())
          .build();
        eventModel.addEvent(event);
      }
    }
    return eventModel;
  }

  private LocalDateTime toLocalDateTime(String dateString)
  {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    return LocalDateTime.parse(dateString, formatter);
  }

  private String toDateString(LocalDateTime localDateTime)
  {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    return localDateTime.format(formatter);
  }

  private void updateEvent(Event event)
  {
    AgendaModuleBean.getClient(true).storeEvent(event);
    outdate();
    if (event.getEventId().equals(eventObjectBean.getEvent().getEventId()))
    {
      //Refresh edition tab
      try
      {
        eventObjectBean.loadObject();
      }
      catch (Exception ex) { }
    }
  }

}
