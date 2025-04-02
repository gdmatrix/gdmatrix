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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.agenda.Event;
import org.matrix.agenda.EventFilter;
import org.matrix.agenda.OrderByProperty;
import org.matrix.agenda.SecurityMode;
import org.matrix.dic.DictionaryConstants;
import org.matrix.security.SecurityConstants;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.LazyScheduleModel;
import org.primefaces.model.ScheduleModel;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.util.BigList;
import org.santfeliu.util.TextUtils;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.helpers.TablePropertyHelper;
import org.santfeliu.webapp.modules.dic.TypeTypeBean;
import org.santfeliu.webapp.setup.ActionObject;
import org.santfeliu.webapp.setup.TableProperty;
import org.santfeliu.webapp.setup.SearchTab;
import org.santfeliu.webapp.util.DataTableRow;

/**
 *
 * @author lopezrj-sf
 */
@Named
@RequestScoped
public class EventFinderBean extends FinderBean
{
  private static final DateTimeFormatter HOUR_FORMATTER =
    DateTimeFormatter.ofPattern("HHmmss");
  
  private static final String RENDER_PUBLIC_ICON = "renderPublicIcon";
  private static final String RENDER_ONLY_ATTENDANTS_ICON =
    "renderOnlyAttendantsIcon"; 

  private static final String SECURITY_MODE_PROPERTY = "securityMode";
  
  private String smartFilter;
  private EventFilter filter = new EventFilter();
  private List<EventDataTableRow> rows;
  private int firstRow;
  private boolean outdated;
  private String formSelector;

  private String searchEventThemeId;
  private String searchEventTypeId;

  //Schedule
  private ScheduleModel eventModel;
  private String serverTimeZone = ZoneId.systemDefault().toString();
  private String scheduleView;
  private LocalDate scheduleInitialDate;
  private boolean scheduleEdit = false;
  private boolean dateDblClick = false;

  @Inject
  NavigatorBean navigatorBean;

  @Inject
  EventObjectBean eventObjectBean;

  @Inject
  EventTypeBean eventTypeBean;

  @Inject
  TypeTypeBean typeTypeBean;

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

  @Override
  public EventFilter getFilter()
  {
    if (filter != null &&
      (filter.getEventTypeId() == null || filter.getEventTypeId().isEmpty()))
    {
      String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
      filter.getEventTypeId().clear();
      filter.getEventTypeId().add(baseTypeId);
    }
    return filter;
  }

  public void setFilter(EventFilter filter)
  {
    this.filter = filter;
  }

  @Override
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

  public List<TableProperty> getTableProperties()
  {
    try
    {
      List<TableProperty> tableProperties =
        getObjectSetup().getSearchTabs().get(0).getTableProperties();

      return tableProperties != null ?
        tableProperties : Collections.emptyList();
    }
    catch (Exception ex)
    {
      return Collections.emptyList();
    }
  }

  public List<TableProperty> getColumns()
  {
    return TablePropertyHelper.getColumnTableProperties(getTableProperties());
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

  public void onEventSelect()
  {
    String eventId = getFacesContext().
      getExternalContext().getRequestParameterMap().get("eventId");
    Date startDate = getDateFromScheduleParam("eventStartStr");
    LocalDate startLocalDate = 
      startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();        
    setScheduleInitialDate(startLocalDate);
    navigatorBean.view(eventId);
  }

  public void onDateSelect(SelectEvent<LocalDateTime> selectEvent)
  {
    if (!dateDblClick) //ignore if double click
    {
      if ("dayGridMonth".equals(scheduleView) ||
        "timeGridWeek".equals(scheduleView))
      {
        LocalDateTime localDateTime = selectEvent.getObject();
        setScheduleInitialDate(localDateTime.toLocalDate());
        setScheduleView("timeGridDay");
      }
      PrimeFaces.current().ajax().update("mainform:search_tabs:schedule");
    }
  }

  public void onDateDblSelect(SelectEvent<LocalDateTime> selectEvent)
  {    
    navigatorBean.view(NEW_OBJECT_ID);
    LocalDateTime startLocalDateTime = selectEvent.getObject();
    LocalDateTime endLocalDateTime = startLocalDateTime.plusHours(2);    
    eventObjectBean.setStartDate(startLocalDateTime.toLocalDate());
    eventObjectBean.setStartTime(
      startLocalDateTime.toLocalTime().format(HOUR_FORMATTER));
    eventObjectBean.setEndDate(endLocalDateTime.toLocalDate());
    eventObjectBean.setEndTime(
      endLocalDateTime.toLocalTime().format(HOUR_FORMATTER));
    setScheduleInitialDate(startLocalDateTime.toLocalDate());    
    dateDblClick = true;
  }

  public void onEventMove()
  {
    boolean moved = true;
    try
    {
      //Params from component
      String eventId = getFacesContext().
        getExternalContext().getRequestParameterMap().get("eventId");
      Date startDate = getDateFromScheduleParam("eventStartStr");
      Date endDate = getDateFromScheduleParam("eventEndStr");      
      String startDateTime = TextUtils.formatDate(startDate, "yyyyMMddHHmmss");
      String endDateTime = TextUtils.formatDate(endDate, "yyyyMMddHHmmss");
      updateEvent(eventId, startDateTime, endDateTime);
      scheduleEdit = true;
      growl("EVENT_MOVED");      
    }
    catch (Exception ex)
    {
      moved = false;
      growl("EVENT_MOVE_ERROR", null, FacesMessage.SEVERITY_ERROR);      
    }
    //Return value
    PrimeFaces.current().ajax().addCallbackParam("moved", moved);    
  }

  public void onEventResize()
  {
    boolean resized = true;
    try
    {
      //Params from component
      String eventId = getFacesContext().
        getExternalContext().getRequestParameterMap().get("eventId");
      Date endDate = getDateFromScheduleParam("eventEndStr");
      String endDateTime = TextUtils.formatDate(endDate, "yyyyMMddHHmmss");    
      updateEvent(eventId, null, endDateTime);
      scheduleEdit = true;
      growl("EVENT_RESIZED");
    }
    catch (Exception ex)
    {
      resized = false;
      growl("EVENT_RESIZE_ERROR", null, FacesMessage.SEVERITY_ERROR);
    }
    //Return value
    PrimeFaces.current().ajax().addCallbackParam("resized", resized);        
  }  
  
  @Override
  public void smartFind()
  {
    setFinding(true);
    setFilterTabSelector(0);
    String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
    filter = eventTypeBean.queryToFilter(smartFilter, baseTypeId);
    setFromDate(getDefaultFromDate());
    filter.setDateComparator("S");
    setSearchEventTypeId(null);
    setSearchEventThemeId(null);
    doFind(true);
    firstRow = 0;
  }

  @Override
  public void find()
  {
    setFinding(true);
    setFilterTabSelector(1);
    smartFilter = eventTypeBean.filterToQuery(filter);

    if (filter.getStartDateTime() == null)
    {
      setFromDate(getDefaultFromDate());
      filter.setDateComparator("S");
    }

    filter.getEventTypeId().clear();
    if (StringUtils.isBlank(searchEventTypeId))
    {
      String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
      filter.getEventTypeId().add(baseTypeId);
    }
    else
    {
      filter.getEventTypeId().add(searchEventTypeId);
    }

    filter.getThemeId().clear();
    if (!StringUtils.isBlank(searchEventThemeId))
    {
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
    dateDblClick = false;
    if (outdated) //update all search pages, including schedule
    {
      doFind(false);
      PrimeFaces.current().ajax().update(getClientIdList(true));
      scheduleEdit = false;
    }
    else if (scheduleEdit) //update only list search pages
    {
      doFind(false);
      PrimeFaces.current().ajax().update(getClientIdList(false));
      scheduleEdit = false;
    }
    else //update only list search pages
    {
      PrimeFaces.current().ajax().update(getClientIdList(false));
    }
  }

  @Override
  public void clear()
  {
    super.clear();
    filter = new EventFilter();
    smartFilter = null;
    setFromDate(getDefaultFromDate());
    filter.setDateComparator("S");
    searchEventTypeId = null;
    searchEventThemeId = null;
    rows = null;
    setFinding(false);
    formSelector = null;
    scheduleInitialDate = null;
    scheduleView = "dayGridMonth";
  }

  public String getEventTypeDescription(EventDataTableRow row)
  {
    if (row != null && row.getTypeId() != null)
    {
      return typeTypeBean.getTypeDescription(row.getTypeId());
    }
    return "";
  }

  public String getEventStyleClass(String eventId)
  {
    return "event_" + eventId;
  }

  public String getEventTypeStyleClass(String eventTypeId)
  {
    return "et_" + eventTypeId.replace(':', '_').replace('.', '_');
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

  public boolean isRenderPublicIcon()
  {
    return isRender(RENDER_PUBLIC_ICON, true);
  }

  public boolean isRenderOnlyAttendantsIcon()
  {
    return isRender(RENDER_ONLY_ATTENDANTS_ICON, false);
  }

  public boolean isRenderSchedule()
  {
    return (
      !filter.getEventId().isEmpty() ||
      !StringUtils.isBlank(filter.getContent()) ||
      !StringUtils.isBlank(filter.getPersonId()) ||
      (
        !filter.getEventTypeId().isEmpty() &&
        !filter.getEventTypeId().contains("Event")
      ) ||
      !filter.getThemeId().isEmpty() ||
      !StringUtils.isBlank(filter.getRoomId()) ||
      !filter.getProperty().isEmpty()
    );
  }

  public int getScheduleTabIndex()
  {
    List<SearchTab> tabs = eventObjectBean.getSearchTabs();
    for (int i = 0; i < tabs.size(); i++)
    {
      String viewId = tabs.get(i).getViewId();
      if ("/pages/agenda/event_schedule.xhtml".equals(viewId))
      {
        return i;
      }
    }
    return -1;
  }

  public int getDefaultSearchTabSelector()
  {
    int searchTabSelector =
      navigatorBean.getBaseTypeInfo().getDefaultSearchTabSelector();
    if (searchTabSelector == -1) searchTabSelector = 0;
    return searchTabSelector;
  }

  public String getPublicTypeSymbol()
  {
    try
    {
      String publicTypeSymbol =
        getObjectSetup().getProperties().getString("publicTypeSymbol");
      if (publicTypeSymbol != null) return publicTypeSymbol;
    }
    catch (Exception ex) { }

    return String.valueOf((char)0x24CC);
  }

  @Override
  protected void setActionResult(ActionObject actionObject)
  {
    filter = (EventFilter) actionObject.getObject();
    if (filter != null)
    {
      if (!filter.getEventTypeId().isEmpty())
        searchEventTypeId = filter.getEventTypeId().get(0);
      if (!filter.getThemeId().isEmpty())
        searchEventThemeId = filter.getThemeId().get(0);
    }
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ isFinding(), getFilterTabSelector(), filter, firstRow,
      searchEventThemeId, getObjectPosition(),
      scheduleInitialDate, scheduleView, formSelector, rows,
      searchEventTypeId, outdated, scheduleEdit, getPageSize() };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      setFinding((Boolean)stateArray[0]);
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
      searchEventTypeId = (String)stateArray[10];
      outdated = (Boolean)stateArray[11];
      scheduleEdit = (Boolean)stateArray[12];
      if (outdated || scheduleEdit)
      {
        doFind(false);
        scheduleEdit = false;
      }
      setPageSize((Integer)stateArray[13]);
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
      if (!isFinding())
      {
        rows = Collections.EMPTY_LIST;
      }
      else
      {
        String securityMode = getProperty(SECURITY_MODE_PROPERTY);
        if (securityMode != null && 
          (!StringUtils.isBlank(filter.getRoomId()) ||
          !StringUtils.isBlank(filter.getPersonId())))
        {
          SecurityMode sm = SecurityMode.valueOf(securityMode.toUpperCase());
          filter.setSecurityMode(sm);
        }
        else
          filter.setSecurityMode(SecurityMode.FILTERED);
        
        rows = new BigList(2 * getPageSize() + 1, getPageSize())
        {
          @Override
          public int getElementCount()
          {
            try
            {
              return AgendaModuleBean.getClient().countEventsFromCache(filter);
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
              setOrderBy(filter);
              List<Event> events = AgendaModuleBean.getClient().
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
          if (rows.isEmpty())
          {
            scheduleInitialDate = null;
          }
          else
          {
            scheduleInitialDate =
              toLocalDateTime(rows.get(0).getStartDateTime()).toLocalDate();
          }
          scheduleView = "dayGridMonth";
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
              eventObjectBean.setSearchTabSelector(
                getDefaultSearchTabSelector());
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
      row.setValues(this, event, getTableProperties());
      row.setStartDateTime(event.getStartDateTime());
      row.setEndDateTime(event.getEndDateTime());
      row.setSummary(event.getSummary());
      row.setPublicRow(isPublicEvent(event));
      row.setOnlyAttendants(event.isOnlyAttendants() == null ? false :
        event.isOnlyAttendants());
      convertedRows.add(row);
    }
    return convertedRows;
  }

  private void setOrderBy(EventFilter filter) throws Exception
  {
    filter.getOrderBy().clear();

    int tabSelector = eventObjectBean.getSearchTabSelector();
    tabSelector =
      tabSelector < getObjectSetup().getSearchTabs().size() ? tabSelector : 0;
    List<String> orderBy =
      getObjectSetup().getSearchTabs().get(tabSelector).getOrderBy();

    if (orderBy != null && !orderBy.isEmpty())
    {
      for (String column : orderBy)
      {
        String[] parts = column.split(":");
        OrderByProperty orderByProperty = new OrderByProperty();
        orderByProperty.setName(parts[0]);
        if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1]))
        {
          orderByProperty.setDescending(true);
        }
        filter.getOrderBy().add(orderByProperty);
      }
    }
  }

  private ScheduleModel loadEventModel()
  {
    String securityMode = getProperty(SECURITY_MODE_PROPERTY);
    if (securityMode != null && 
      (!StringUtils.isBlank(filter.getRoomId()) ||
      !StringUtils.isBlank(filter.getPersonId())))
    {
      SecurityMode sm = SecurityMode.valueOf(securityMode.toUpperCase());
      filter.setSecurityMode(sm);
    }
    else
      filter.setSecurityMode(SecurityMode.FILTERED);
    
    eventModel = new LazyScheduleModel()
    {
      @Override
      public void loadEvents(LocalDateTime start, LocalDateTime end)
      {
        EventFilter scheduleFilter = cloneEventFilter(filter);
        scheduleFilter.setFirstResult(0);
        scheduleFilter.setMaxResults(Integer.MAX_VALUE);
        scheduleFilter.setStartDateTime(toDateString(start));
        scheduleFilter.setEndDateTime(toDateString(end));
        try
        {
          List<Event> events = AgendaModuleBean.getClient().
            findEventsFromCache(scheduleFilter);
          for (Event row : events)
          {
            if (mustIncludeEvent(row, filter.getStartDateTime(),
              filter.getEndDateTime()))
            {
              boolean currentEvent = row.getEventId().equals(
                eventObjectBean.getEvent().getEventId());
              DefaultScheduleEvent<?> event = DefaultScheduleEvent.builder()
                .id(row.getEventId())
                .title(row.getSummary())
                .startDate(toLocalDateTime(row.getStartDateTime()))
                .endDate(toLocalDateTime(row.getEndDateTime()))
                .description(row.getSummary())
                .overlapAllowed(true)
                .styleClass(getEventStyleClass(row.getEventId()) + " " +
                  getEventTypeStyleClass(row.getEventTypeId()) +
                  (currentEvent ? " current" : ""))
                .build();
              addEvent(event);
            }
          }
        }
        catch (Exception ex)
        {
        }
        setScheduleInitialDate(start.toLocalDate());
      }
    };
    return eventModel;
  }

  private EventFilter cloneEventFilter(EventFilter filter)
  {
    EventFilter cloneFilter = new EventFilter();
    cloneFilter.getEventId().addAll(filter.getEventId());
    cloneFilter.setContent(filter.getContent());
    cloneFilter.setDateComparator(filter.getDateComparator());
    cloneFilter.setPersonId(filter.getPersonId());
    cloneFilter.setRoomId(filter.getRoomId());
    cloneFilter.getProperty().addAll(filter.getProperty());
    cloneFilter.getEventTypeId().addAll(filter.getEventTypeId());
    cloneFilter.setStartDateTime(filter.getStartDateTime());
    cloneFilter.setEndDateTime(filter.getEndDateTime());
    cloneFilter.getThemeId().addAll(filter.getThemeId());
    cloneFilter.setSecurityMode(filter.getSecurityMode());    
    return cloneFilter;
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

  private void updateEvent(String eventId, String startDateTime,
    String endDateTime)
  {
    Event event = AgendaModuleBean.getClient(true).loadEvent(eventId);
    executeAction("preMove", event);
    if (startDateTime != null) event.setStartDateTime(startDateTime);
    if (endDateTime != null) event.setEndDateTime(endDateTime);
    event = AgendaModuleBean.getClient(true).storeEvent(event);
    executeAction("postMove", event);
    if (eventId.equals(eventObjectBean.getEvent().getEventId()))
    {
      //Refresh edition tab
      try
      {
        eventObjectBean.loadObject();
      }
      catch (Exception ex) { }
    }
  }

  private boolean mustIncludeEvent(Event event,
    String filterStartDateTime, String filterEndDateTime)
  {
    Date eventStartDate = TextUtils.parseInternalDate(event.getStartDateTime());
    Date eventEndDate = TextUtils.parseInternalDate(event.getEndDateTime());
    Date viewStartDate = TextUtils.parseInternalDate(
      StringUtils.defaultString(filterStartDateTime, "19000101000000"));
    Date viewEndDate = TextUtils.parseInternalDate(
      StringUtils.defaultString(filterEndDateTime, "99991231000000"));
    return (!eventStartDate.after(viewEndDate) &&
      eventEndDate.after(viewStartDate));
  }

  private boolean isRender(String name, boolean defValue)
  {
    String value = getProperty(name);
    if (value == null)
      return defValue;
    else
      return Boolean.valueOf(value);
  }

  private Date getDefaultFromDate()
  {
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    return cal.getTime();
  }

  private boolean isPublicEvent(Event event)
  {
    if (event.isOnlyAttendants() != null && event.isOnlyAttendants())
      return false;

    String eventTypeId = event.getEventTypeId();
    if (!StringUtils.isBlank(eventTypeId))
    {
      Type type = TypeCache.getInstance().getType(eventTypeId);
      if (type != null)
      {
        return type.canPerformAction(DictionaryConstants.READ_ACTION,
          Collections.singleton(SecurityConstants.EVERYONE_ROLE));
      }
    }
    return false;
  }

  private List<String> getClientIdList(boolean includeSchedule)
  {
    List<String> clientIdList = new ArrayList();
    int scheduleTabIndex = getScheduleTabIndex();
    List<SearchTab> tabs = eventObjectBean.getSearchTabs();
    for (int i = 0; i < tabs.size(); i++)
    {
      if (includeSchedule || i != scheduleTabIndex)
      {
        clientIdList.add("mainform:search_tabs:result_list_" + i);
      }
    }
    return clientIdList;
  }
  
  private Date getDateFromScheduleParam(String paramName)
  {
    String paramValue = getFacesContext().getExternalContext().
      getRequestParameterMap().get(paramName);
    return TextUtils.parseUserDate(paramValue, "yyyy-MM-dd'T'HH:mm:ssXXX");    
  }  

  public class EventDataTableRow extends DataTableRow
  {
    private String startDateTime;
    private String endDateTime;
    private String summary;
    private boolean publicRow;
    private boolean onlyAttendants;

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

    public boolean isPublicRow()
    {
      return publicRow;
    }

    public void setPublicRow(boolean publicRow)
    {
      this.publicRow = publicRow;
    }

    public boolean isOnlyAttendants()
    {
      return onlyAttendants;
    }

    public void setOnlyAttendants(boolean onlyAttendants)
    {
      this.onlyAttendants = onlyAttendants;
    }
  }

}
