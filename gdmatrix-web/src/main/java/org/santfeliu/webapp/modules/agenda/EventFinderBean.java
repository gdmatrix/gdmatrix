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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.agenda.Event;
import org.matrix.agenda.EventFilter;
import org.primefaces.event.schedule.ScheduleEntryMoveEvent;
import org.primefaces.event.schedule.ScheduleEntryResizeEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.LazyScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;
import org.santfeliu.util.BigList;
import org.santfeliu.util.TextUtils;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.modules.dic.TypeTypeBean;
import org.santfeliu.webapp.setup.Column;
import org.santfeliu.webapp.util.DataTableRow;

/**
 *
 * @author lopezrj-sf
 */
@Named
@ViewScoped
public class EventFinderBean extends FinderBean
{
  private String smartFilter;
  private EventFilter filter = new EventFilter();
  private List<EventDataTableRow> rows;
  private int firstRow;
  private boolean finding;
  private boolean outdated;
  private String formSelector;

  private String searchEventThemeId;

  //Schedule
  private ScheduleModel eventModel;
  private String serverTimeZone = ZoneId.systemDefault().toString();
  private String scheduleView;
  private LocalDate scheduleInitialDate;
  private LocalDateTime scheduleStart;

  @Inject
  NavigatorBean navigatorBean;

  @Inject
  EventObjectBean eventObjectBean;

  @Inject
  EventTypeBean eventTypeBean;

  @Inject
  TypeTypeBean typeTypeBean;

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

  public List getRows()
  {
    return rows;
  }

  public void setRows(List rows)
  {
    this.rows = rows;
  }

  public String getFormSelector()
  {
    return formSelector;
  }

  public void setFormSelector(String formSelector)
  {
    this.formSelector = formSelector;
  }

  public List<Column> getColumns()
  {
    try
    {
      if (objectSetup == null)
        loadObjectSetup();

      return objectSetup.getSearchTabs().get(0).getColumns();
    }
    catch (Exception ex)
    {
      return Collections.emptyList();
    }
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
    if (filter.getEventTypeId().isEmpty())
    {
      String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
      filter.getEventTypeId().add(baseTypeId);
    }
    return filter.getEventTypeId().get(0);
  }

  public void setSearchEventTypeId(String searchEventTypeId)
  {
    filter.getEventTypeId().clear();
    if (!StringUtils.isBlank(searchEventTypeId))
    {
      filter.getEventTypeId().add(searchEventTypeId);
    }
    else
    {
      String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
      filter.getEventTypeId().add(baseTypeId);
    }
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
    return rows == null ? NEW_OBJECT_ID : rows.get(position).getRowId();
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
    if ("dayGridMonth".equals(view) && scheduleStart.getDayOfMonth() != 1)
    {
      TemporalAdjuster ta = TemporalAdjusters.firstDayOfNextMonth();
      LocalDate firstDayOfNextMonth = scheduleStart.with(ta).toLocalDate();
      setScheduleInitialDate(firstDayOfNextMonth);
    }
    else
    {
      setScheduleInitialDate(scheduleStart.toLocalDate());
    }
  }

  @Override
  public void smartFind()
  {
    finding = true;
    setFilterTabSelector(0);
    String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
    filter = eventTypeBean.queryToFilter(smartFilter, baseTypeId);
    setFromDate(new Date());
    filter.setDateComparator("1");
    setSearchEventTypeId(null);
    doFind(true);
    firstRow = 0;
  }

  @Override
  public void find()
  {
    finding = true;
    setFilterTabSelector(1);
    smartFilter = eventTypeBean.filterToQuery(filter);
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
    setSearchEventTypeId(null);
    searchEventThemeId = null;
    rows = null;
    finding = false;
    formSelector = null;
  }

  public String getEventTypeDescription(EventDataTableRow row)
  {
    if (row != null && row.getTypeId() != null)
    {
      return typeTypeBean.getDescription(row.getTypeId());
    }
    return "";
  }

  public boolean isDateChange(int rowIndex)
  {
    if (rowIndex == 0) return true;
    else
    {
      try
      {
        EventDataTableRow row1 = rows.get(rowIndex - 1);
        EventDataTableRow row2 = rows.get(rowIndex);
        Date date1 = TextUtils.parseInternalDate(row1.getStartDateTime());
        Date date2 = TextUtils.parseInternalDate(row2.getStartDateTime());
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        return !fmt.format(date1).equals(fmt.format(date2));
      }
      catch (Exception ex)
      {
        return false;
      }
    }
  }

  public Date getRowStartDate()
  {
    try
    {
      EventDataTableRow row = (EventDataTableRow)getValue("#{row}");
      return TextUtils.parseInternalDate(row.getStartDateTime());
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  public Date getRowEndDate()
  {
    try
    {
      EventDataTableRow row = (EventDataTableRow)getValue("#{row}");
      return TextUtils.parseInternalDate(row.getEndDateTime());
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ finding, getFilterTabSelector(), filter, firstRow,
      searchEventThemeId, getObjectPosition(),
      scheduleInitialDate, scheduleView, formSelector, rows };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      finding = (Boolean)stateArray[0];
      setFilterTabSelector((Integer)stateArray[1]);
      filter = (EventFilter)stateArray[2];
      smartFilter = eventTypeBean.filterToQuery(filter);
      firstRow = (Integer)stateArray[3];
      searchEventThemeId = (String)stateArray[4];
      setObjectPosition((Integer)stateArray[5]);
      scheduleInitialDate = (LocalDate)stateArray[6];
      scheduleView = (String)stateArray[7];
      formSelector = (String)stateArray[8];
      rows = (List<EventDataTableRow>)stateArray[9];
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
        rows = new BigList(100, 50)
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
              filter.setIncludeMetadata(true);
              List<Event> events = AgendaModuleBean.getClient(false).
                findEventsFromCache(filter);
              return toDataTableRows(events);
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
            navigatorBean.view(rows.get(0).getRowId());
            eventObjectBean.setSearchTabSelector(
              eventObjectBean.getEditModeSelector());
          }
          else
          {
            if (eventObjectBean.getSearchTabSelector() ==
              eventObjectBean.getEditModeSelector())
            {
              eventObjectBean.setSearchTabSelector(0);
            }
          }
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private List<EventDataTableRow> toDataTableRows(List<Event> events)
    throws Exception
  {
    List<EventDataTableRow> convertedRows = new ArrayList();
    for (Event event : events)
    {
      EventDataTableRow row = new EventDataTableRow(event.getEventId(),
        event.getEventTypeId());
      row.setValues(this, event, getColumns());
      row.setStartDateTime(event.getStartDateTime());
      row.setEndDateTime(event.getEndDateTime());
      row.setSummary(event.getSummary());
      convertedRows.add(row);
    }
    return convertedRows;
  }

  private ScheduleModel loadEventModel()
  {
    eventModel = new LazyScheduleModel()
    {
      @Override
      public void loadEvents(LocalDateTime start, LocalDateTime end)
      {

        if (rows != null)
        {
          for (EventDataTableRow row : rows)
          {
            if (mustIncludeEvent(row, start, end))
            {
              DefaultScheduleEvent<?> event = DefaultScheduleEvent.builder()
                .id(row.getRowId())
                .title(row.getSummary())
                .startDate(toLocalDateTime(row.getStartDateTime()))
                .endDate(toLocalDateTime(row.getEndDateTime()))
                .description(row.getSummary())
                .build();
              addEvent(event);
            }
          }
        }
        scheduleStart = start;
      }
    };
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

  private boolean mustIncludeEvent(EventDataTableRow row,
    LocalDateTime viewStart, LocalDateTime viewEnd)
  {
    Date eventStartDate = TextUtils.parseInternalDate(row.getStartDateTime());
    Date eventEndDate = TextUtils.parseInternalDate(row.getEndDateTime());
    Date viewStartDate = TextUtils.parseInternalDate(toDateString(viewStart));
    Date viewEndDate = TextUtils.parseInternalDate(toDateString(viewEnd));
    return (!eventStartDate.after(viewEndDate) &&
      eventEndDate.after(viewStartDate));
  }

  public class EventDataTableRow extends DataTableRow
  {
    private String startDateTime;
    private String endDateTime;
    private String summary;

    public EventDataTableRow(String rowId, String typeId)
    {
      super(rowId, typeId);
    }

    public String getStartDateTime()
    {
      return startDateTime;
    }

    public void setStartDateTime(String startDateTime)
    {
      this.startDateTime = startDateTime;
    }

    public String getEndDateTime()
    {
      return endDateTime;
    }

    public void setEndDateTime(String endDateTime)
    {
      this.endDateTime = endDateTime;
    }

    public String getSummary()
    {
      return summary;
    }

    public void setSummary(String summary)
    {
      this.summary = summary;
    }
  }

}
