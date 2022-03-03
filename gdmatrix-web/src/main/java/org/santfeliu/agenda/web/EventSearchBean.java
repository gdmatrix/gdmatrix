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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import org.apache.commons.lang.StringUtils;
import org.apache.myfaces.custom.schedule.model.ScheduleDay;
import org.apache.myfaces.custom.schedule.model.ScheduleEntry;
import org.apache.myfaces.custom.schedule.model.ScheduleModel;
import org.apache.myfaces.custom.schedule.model.SimpleScheduleModel;
import org.matrix.agenda.AgendaConstants;
import org.matrix.agenda.Event;
import org.matrix.agenda.EventFilter;
import org.matrix.agenda.EventThemeFilter;
import org.matrix.agenda.EventThemeView;
import org.matrix.agenda.OrderByProperty;
import org.matrix.agenda.SecurityMode;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.Property;
import org.matrix.dic.TypeFilter;
import org.matrix.doc.Document;
import org.matrix.security.SecurityConstants;
import org.santfeliu.agenda.client.AgendaManagerClient;
import org.santfeliu.agenda.web.view.EventSearchView;
import org.santfeliu.agenda.web.view.ScheduleEventSearchView;
import org.santfeliu.agenda.web.view.ScheduleEventViewBean;
import org.santfeliu.cases.AddressDescriptionCache;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.web.TypeBean;
import org.santfeliu.dic.web.TypeSearchBean;
import org.santfeliu.doc.client.CachedDocumentManagerClient;
import org.santfeliu.doc.web.DocumentUrlBuilder;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.browser.HtmlBrowser;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.kernel.web.PersonBean;
import org.santfeliu.kernel.web.PersonSearchBean;
import org.santfeliu.kernel.web.RoomBean;
import org.santfeliu.kernel.web.RoomSearchBean;
import org.santfeliu.security.UserCache;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.util.PojoUtils;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.bean.CMSProperty;
import org.santfeliu.web.obj.ControllerBean;
import org.santfeliu.web.obj.DetailBean;

import org.santfeliu.web.obj.PageBean;
import org.santfeliu.web.obj.PropertiesFilter;
import org.santfeliu.web.obj.util.RequestParameters;

/**
 *
 * @author blanquepa
 */
@CMSManagedBean
public class EventSearchBean extends PageBean
{
  @CMSProperty
  public static final String EVENT_FILTER_NAME = "filterName";
  @CMSProperty
  public static final String EVENT_VIEW_NAME = "viewName";
  @CMSProperty
  public static final String EVENT_VIEW_BEAN_NAME = "viewBeanName";
  @CMSProperty
  public static final String EVENT_TEMPLATE_NAME = "templateName";
  @CMSProperty
  public static final String SEARCH_EVENT_TYPE = "searchEventType";
  @CMSProperty
  public static final String SORT_EVENT_TYPE = "sortEventType";
  @CMSProperty
  public static final String SEARCH_EVENT_THEME = "searchEventTheme";
  @CMSProperty
  public static final String SORT_EVENT_THEME = "sortEventTheme";
  @CMSProperty
  public static final String SEARCH_EVENT_ROOM = "searchEventRoom";
  @CMSProperty
  public static final String SORT_EVENT_ROOM = "sortEventRoom";
  @CMSProperty
  public static final String SEARCH_EVENT_PERSON = "searchEventPerson";
  @CMSProperty
  public static final String SORT_EVENT_PERSON = "sortEventPerson";
  @CMSProperty  
  public static final String SEARCH_EVENT_PROPERTY_NAME = 
    "searchEventPropertyName";
  @CMSProperty
  public static final String SEARCH_EVENT_PROPERTY_VALUE = 
    "searchEventPropertyValue";
  @CMSProperty
  public static final String PROPERTY_FIRSTLOAD = 
    "resultList.showOnFirstRequest";
  @CMSProperty
  public static final String ORDERBY = "orderBy";
  @CMSProperty
  public static final String SECURITY_MODE = "securityMode";
  @CMSProperty
  public static final String ATTENDANT_MODE = "attendantMode";
  @CMSProperty
  public static final String SCHEDULE_MODE = "scheduleMode";
  @CMSProperty
  public static final String HEADER_DOCUMENT_PROPERTY = "header.docId";
  @CMSProperty
  public static final String FOOTER_DOCUMENT_PROPERTY = "footer.docId";
  @CMSProperty
  public static final String SEARCH_DAYS = "searchDays";
  @CMSProperty
  public static final String RUN_AS_ADMIN_FOR_PROPERTY = "runAsAdminFor";
  @CMSProperty
  public static final String FIND_AS_ADMIN_FOR_PROPERTY = "findAsAdminFor";
  @CMSProperty
  public static final String ALL_USERS = "%";
  public static final String SCHEDULE_SELECT_MODE = "scheduleSelectMode";
  @CMSProperty
  public static final String SCHEDULE_DATE_FORMAT = "scheduleDateFormat";
  @CMSProperty
  public static final String SEARCH_DATE_COMPARATOR = "searchDateComparator";
  @CMSProperty
  public static final String SEARCH_START_DATE = "searchStartDate";
  @CMSProperty
  public static final String SEARCH_END_DATE = "searchEndDate";
  @CMSProperty
  public static final String SHOW_EVENT_SECURITY_SOURCE =
    "showEventSecuritySource";
  @CMSProperty
  public static final String MAIN_AGENDA_MID = "mainAgendaMid";  
  
  //Renderers
  @CMSProperty
  public static final String RENDER_FILTER_PANEL = "renderFilterPanel";
  @CMSProperty
  public static final String RENDER_CALENDAR_PANEL = "renderCalendarPanel";
  @CMSProperty
  public static final String RENDER_EVENTID_FILTER = "renderEventIdFilter";
  @CMSProperty
  public static final String RENDER_TYPE_FILTER = "renderTypeFilter";
  @CMSProperty
  public static final String RENDER_CONTENT_FILTER = "renderContentFilter";
  @CMSProperty
  public static final String RENDER_DATE_FILTER = "renderDateFilter";
  @CMSProperty
  public static final String RENDER_PERSON_FILTER = "renderPersonFilter";
  @CMSProperty
  public static final String RENDER_ROOM_FILTER = "renderRoomFilter";
  @CMSProperty
  public static final String RENDER_THEME_FILTER = "renderThemeFilter";
  @CMSProperty
  public static final String RENDER_INPUT_PROPERTIES_FILTER = 
    "renderInputPropertiesFilter";
  @CMSProperty
  public static final String RENDER_SELECT_PROPERTIES_FILTER = 
    "renderSelectPropertiesFilter";
  @CMSProperty
  public static final String RENDER_ACTIONS_BAR = "renderActionsBar";
  @CMSProperty
  public static final String RENDER_PUBLIC_ICON = "renderPublicIcon";
  @CMSProperty
  public static final String RENDER_ONLY_ATTENDANTS_ICON = 
    "renderOnlyAttendantsIcon";
  @CMSProperty
  public static final String RENDER_FILTER_INFO = "renderFilterInfo";
  @CMSProperty
  public static final String RENDER_PICKUP_BUTTON = "renderPickupButton";
  @Deprecated
  public static final String RENDER_PICK_BUTTON = "renderPickButton";

  
  public static final String DOC_SERVLET_URL = "/documents/";

  private static final String DEFAULT_EVENT_FILTER_NAME = "classic";
  private static final String DEFAULT_EVENT_VIEW_NAME = "table";
  private static final String DEFAULT_EVENT_TEMPLATE_NAME = "minimal";

  private static final String EVENT_SECURITY_SOURCE = "event";
  private static final String NODE_AND_EVENT_SECURITY_SOURCE = "node_event";

  private transient List<SelectItem> typeSelectItems;
  private transient List<SelectItem> personSelectItems;
  private transient List<SelectItem> roomSelectItems;
  private transient List<SelectItem> themeSelectItems;
  
  //User filter
  private EventFilter eventFilter;
  private String eventId;
  private String themeId;
  private String roomId;
  private String personId;
  private PropertiesFilter propertiesFilter;
  private String startDateFilter;
  private String endDateFilter;
  private boolean isAttendantMode;
  private String onlyCurrentDate = "false";

  private EventSearchView eventViewBean;

  private transient HtmlBrowser headerBrowser = new HtmlBrowser();
  private transient HtmlBrowser infoBrowser = new HtmlBrowser();  
  private transient HtmlBrowser footerBrowser = new HtmlBrowser();

  private String eventTypeIdParam = null;
  private String roomIdParam = null;
  private String startDateTimeParam = null;
  private String endDateTimeParam = null;


  private Event selectedEvent;

  public EventSearchBean()
  {
    eventFilter = new EventFilter();
    propertiesFilter = new PropertiesFilter();
  }

  //Accessors
  public String getEventId()
  {
    return eventId;
  }

  public void setEventId(String eventId)
  {
    this.eventId = eventId;
  }

  public String getThemeId()
  {
    return themeId;
  }

  public void setThemeId(String themeId)
  {
    this.themeId = themeId;
  }

  public String getThemeDescription()
  {
    ThemeBean themeBean = (ThemeBean)getBean("themeBean");
    if (themeId != null && themeId.trim().length() > 0)
      return themeBean.getDescription(themeId);
    else
      return null;
  }

  public String getRoomId()
  {
    return roomId;
  }

  public void setRoomId(String roomId)
  {
    this.roomId = roomId;
  }

  public String getPersonId()
  {
    return personId;
  }

  public void setPersonId(String personId)
  {
    this.personId = personId;
  }

  public EventFilter getEventFilter()
  {
    return eventFilter;
  }

  public void setEventFilter(EventFilter eventFilter)
  {
    this.eventFilter = eventFilter;
  }

  public PropertiesFilter getPropertiesFilter()
  {
    return propertiesFilter;
  }

  public void setPropertiesFilter(PropertiesFilter propertiesFilter)
  {
    this.propertiesFilter = propertiesFilter;
  }

  public String getEndDateFilter()
  {
    return endDateFilter;
  }

  public void setEndDateFilter(String endDateFilter)
  {
    this.endDateFilter = endDateFilter;
  }

  public String getStartDateFilter()
  {
    return startDateFilter;
  }

  public void setStartDateFilter(String startDateFilter)
  {
    this.startDateFilter = startDateFilter;
  }

  public HtmlBrowser getHeaderBrowser()
  {
    if (this.headerBrowser == null)
    {
      this.headerBrowser = new HtmlBrowser();
    }

    MenuItemCursor mic =
      UserSessionBean.getCurrentInstance().getSelectedMenuItem();
    String docId =
      (String) mic.getDirectProperties().get(HEADER_DOCUMENT_PROPERTY);

    if (docId != null)
    {
      headerBrowser.setUrl(getContextPath() + DOC_SERVLET_URL + docId);
      return headerBrowser;
    }
    else
    {
      return null;
    }
  }

  public void setHeaderBrowser(HtmlBrowser headerBrowser)
  {
    this.headerBrowser = headerBrowser;
  }

  public HtmlBrowser getFooterBrowser()
  {
    if (this.footerBrowser == null)
    {
      this.footerBrowser = new HtmlBrowser();
    }

    MenuItemCursor mic =
      UserSessionBean.getCurrentInstance().getSelectedMenuItem();
    String docId =
      (String) mic.getDirectProperties().get(FOOTER_DOCUMENT_PROPERTY);

    if (docId != null)
    {
      footerBrowser.setUrl(getContextPath() + DOC_SERVLET_URL + docId);
      return footerBrowser;
    }
    else
    {
      return null;
    }
  }

  public void setFooterBrowser(HtmlBrowser footerBrowser)
  {
    this.footerBrowser = footerBrowser;
  }

  public HtmlBrowser getInfoBrowser()
  {
    if (infoBrowser == null || infoBrowser.getUrl() == null)
      return null;
    else
      return infoBrowser;
  }

  public void setInfoBrowser(HtmlBrowser infoBrowser)
  {
    this.infoBrowser = infoBrowser;
  }

  public List<SelectItem> getRoomSelectItems()
  {
    try
    {
      if (roomSelectItems == null)
      {
        roomSelectItems = createRoomSelectItems(roomId);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return roomSelectItems;
  }
  public void setRoomSelectItems(List<SelectItem> roomSelectItems)
  {
    this.roomSelectItems = roomSelectItems;
  }

  public List<SelectItem> getTypeSelectItems()
  {
    try
    {
      String currentTypId = propertiesFilter.getCurrentTypeId();
      typeSelectItems = createTypeSelectItems(currentTypId);
      
      if (isRenderPublicIcon())
      {
        for (SelectItem typeItem : typeSelectItems)
        {
          String typeId = (String)typeItem.getValue();
          if (!StringUtils.isBlank(typeId))
          {
            Type type = TypeCache.getInstance().getType(typeId);
            if (type.canPerformAction(DictionaryConstants.READ_ACTION, 
              Collections.singleton(SecurityConstants.EVERYONE_ROLE)))
            {
              typeItem.setLabel(typeItem.getLabel() + " " + ((char)0x24CC) + " ");
            }
          }
        }      
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return typeSelectItems;
  }

  public void setTypeSelectItems(List<SelectItem> typeSelectItems)
  {
    this.typeSelectItems = typeSelectItems;
  }

  public List<SelectItem> getPersonSelectItems()
  {
    try
    {
      if (personSelectItems == null)
      {
        PersonBean personBean = (PersonBean)getBean("personBean");
        List<String> filteredPersons = getNodePersonIdList();
        if (filteredPersons != null && !filteredPersons.isEmpty())
        {
          personSelectItems = personBean.getSelectItems(filteredPersons, 
            eventFilter.getPersonId());
        }
        else
        {
          personSelectItems = 
            personBean.getSelectItems(eventFilter.getPersonId());
        }
        
        if (!personSelectItems.isEmpty())
        {
          for (SelectItem item : personSelectItems)
          {
            if (item.getLabel().equals(item.getValue()))
            {
              String id = (String)item.getValue();
              if (!StringUtils.isBlank(id))
              {
                String description = id;
                if (id.contains("\"") && id.endsWith("\""))
                {
                  int idx = id.indexOf("\"");
                  int lastIdx = id.lastIndexOf("\"");
                  description = id.substring(idx + 1, lastIdx);
                }

                if (description != null)
                {
                  item.setLabel(description);
                  item.setDescription(description);
                }
              }
            }
            String label = item.getLabel();
            item.setLabel(label.replaceAll("\\((\\d)+;(\\d)+\\)", ""));
          }
        }        
        
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return personSelectItems;
  }

  public void setPersonSelectItems(List<SelectItem> personSelectItems)
  {
    this.personSelectItems = personSelectItems;
  }

  public List<SelectItem> getThemeSelectItems()
  {
    try
    {
      themeSelectItems = createThemeSelectItems(themeId);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return themeSelectItems;
  }

  public void setThemeSelectItems(List<SelectItem> themeSelectItems)
  {
    this.themeSelectItems = themeSelectItems;
  }

  public EventSearchView getEventViewBean()
  {
    return eventViewBean;
  }

  public void setEventViewBean(EventSearchView eventViewBean)
  {
    this.eventViewBean = eventViewBean;
  }

  public void setEventViewBean(String viewName)
  {
    String beanName = getProperty(EVENT_VIEW_BEAN_NAME);
    if (beanName == null)
      beanName = viewName + "EventViewBean";

    eventViewBean = (EventSearchView)getBean(beanName);
    if (eventViewBean == null)
      eventViewBean =
        (EventSearchView)getBean(DEFAULT_EVENT_VIEW_NAME + "EventViewBean");
  }

  public String getFilterName()
  {
    String filterName = getProperty(EVENT_FILTER_NAME);
    if (filterName == null)
      filterName = DEFAULT_EVENT_FILTER_NAME;
    return filterName;
  }

  public String getViewName()
  {
    String viewName = getProperty(EVENT_VIEW_NAME);
    if (viewName == null)
      viewName = DEFAULT_EVENT_VIEW_NAME;
    return viewName;
  }

  public String getTemplateName()
  {
    String templateName = getProperty(EVENT_TEMPLATE_NAME);
    if (templateName == null)
      templateName = DEFAULT_EVENT_TEMPLATE_NAME;
    return templateName;
  }

  public List<String> getNodeRoomIdList()
  {
    return getSelectedMenuItem().getMultiValuedProperty(SEARCH_EVENT_ROOM);
  }

  public boolean isRenderSearchRoom()
  {
    return getNodeRoomIdList().isEmpty();
  }
  
  public List<String> getNodePersonIdList()
  {
    return getSelectedMenuItem().getMultiValuedProperty(SEARCH_EVENT_PERSON);
  }

  public boolean isRenderSearchPerson()
  {
    return getNodePersonIdList().isEmpty();
  }  

  public List<String> getNodeTypeIdList()
  {
    return getSelectedMenuItem().getMultiValuedProperty(SEARCH_EVENT_TYPE);
  }

  public boolean isRenderSearchType()
  {
    return getNodeTypeIdList().isEmpty();
  }
  
  public List<String> getNodeThemeIdList()
  {
    return getSelectedMenuItem().getMultiValuedProperty(SEARCH_EVENT_THEME);
  }
  
  public List<String> getNodePropertyValueList()
  {
    return
      getSelectedMenuItem().getMultiValuedProperty(SEARCH_EVENT_PROPERTY_VALUE);
  }

  public String getNodeDateComparator()
  {
    return getProperty(SEARCH_DATE_COMPARATOR);
  }
  
  public String getNodeStartDate()
  {
    return getProperty(SEARCH_START_DATE);
  }
  
  public String getNodeEndDate()
  {
    return getProperty(SEARCH_END_DATE);
  }

  public boolean isRenderSearchTheme()
  {
    return getNodeThemeIdList().isEmpty();
  }  

  public String getScheduleMode()
  {
    return getProperty(SCHEDULE_MODE);
  }

  //User actions
  @CMSAction
  public String show()
  {
    setEventViewBean(getViewName());

    clearEventFilter();
    initEventFilter();

    RequestParameters requestParameters = getRequestParameters();
    //Main params
    roomIdParam = 
      (String)requestParameters.getParameterValue("roomid");
    eventTypeIdParam = 
      (String)requestParameters.getParameterValue("eventtypeid");
    startDateTimeParam = 
      (String)requestParameters.getParameterValue("startdatetime");
    endDateTimeParam = 
      (String)requestParameters.getParameterValue("enddatetime");
    
    //Secondary params
    String oc = (String)requestParameters.getParameterValue("oc");
    if (oc != null)
      this.onlyCurrentDate = oc;
    String content = (String)requestParameters.getParameterValue("content");
    if (content != null)
      eventFilter.setContent(content);
    String personId = (String)requestParameters.getParameterValue("personid");
    if (personId != null)
      eventFilter.setPersonId(personId);
    String th = (String)requestParameters.getParameterValue("themeid");
    if (th != null)
    {
      this.themeId = th;
      eventFilter.getThemeId().add(th);
    }

    String outcome = jumpManager.execute(requestParameters);
    if (outcome != null)
      return outcome;    
    else
    {
      propertiesFilter.createPropDefSelectItems();
      if (eventTypeIdParam != null || roomIdParam != null)
      {
        String infoBrowserUrl = null;
        if (eventTypeIdParam != null)
        {
          propertiesFilter.setCurrentTypeId(eventTypeIdParam);
          infoBrowserUrl = getInfoDocumentUrl("eventTypeId", eventTypeIdParam);
        }
        if (roomIdParam != null)
        {
          eventFilter.setRoomId(roomIdParam);
          infoBrowserUrl = getInfoDocumentUrl("roomId", roomIdParam);
        }
        if (infoBrowserUrl != null)
        {
          if (infoBrowser == null) infoBrowser = new HtmlBrowser();
          infoBrowser.setUrl(infoBrowserUrl);
        }
        else
          infoBrowser = null;
      }
      else
        infoBrowser = null;
      if (startDateTimeParam != null)
      {
        startDateTimeParam = startDateTimeParam.substring(0, 8);
        startDateFilter = startDateTimeParam + "000000";
        endDateFilter = startDateTimeParam + "235959";
      }
      if (endDateTimeParam != null)
      {
        endDateTimeParam = endDateTimeParam.substring(0, 8);
        endDateFilter = endDateTimeParam + "235959";
      }
      if (eventTypeIdParam == null && roomIdParam == null)
      {
        if (infoBrowser == null) infoBrowser = new HtmlBrowser();
        infoBrowser.setUrl(null);
      }

      if (!isRenderTypeFilter() && !isRenderSearchType())
      {
        List<String> nodeTypeList = getNodeTypeIdList();
        if (nodeTypeList != null && nodeTypeList.size() == 1)
          propertiesFilter.setCurrentTypeId(nodeTypeList.get(0));
        propertiesFilter.createPropDefSelectItems();
      }

      if (hasFirstLoadProperty())
      {
        //EventId
        setEventIdFilter(eventFilter);
        //TypeId
        setEventTypeIdFilter(eventFilter);
        //Themes
        setThemeIdFilter(eventFilter);
        //Rooms
        setRoomIdFilter(eventFilter);
        //AttendantMode & Persons
        setAttendantModeFilter(eventFilter);
        //SecurityMode
        String securityMode = getProperty(SECURITY_MODE);
        if (securityMode != null && 
          (!StringUtils.isBlank(eventFilter.getRoomId()) ||
          !StringUtils.isBlank(eventFilter.getPersonId())))
        {
          SecurityMode sm = SecurityMode.valueOf(securityMode.toUpperCase());
          eventFilter.setSecurityMode(sm);
        }
        else
          eventFilter.setSecurityMode(SecurityMode.FILTERED);
        //Properties
        setDynamicPropertiesFilter(eventFilter, propertiesFilter);
        //Dates
        setDefaultDatesFilter();
        setDatesFilter(eventFilter);
        //Order by
        setOrderBy(eventFilter);
        
        eventViewBean.search(eventFilter);
      }

      resetEventFilter();      
      ((EventBean)getObjectBean()).setRenderMainHeading(true);      
      return "event_search";
    }
  }
  
  public String search()
  {
    initEventFilter();

    //EventId
    setEventIdFilter(eventFilter);
    //TypeId
    setEventTypeIdFilter(eventFilter);
    //Themes
    setThemeIdFilter(eventFilter);
    //Rooms
    setRoomIdFilter(eventFilter);
    //SecurityMode
    String securityMode = getProperty(SECURITY_MODE);
    if (securityMode != null && 
      (!StringUtils.isBlank(eventFilter.getRoomId()) ||
      !StringUtils.isBlank(eventFilter.getPersonId())))
    {
      SecurityMode sm = SecurityMode.valueOf(securityMode.toUpperCase());
      eventFilter.setSecurityMode(sm);
    }
    else
      eventFilter.setSecurityMode(SecurityMode.FILTERED);

    //AttendantMode & persons
    setAttendantModeFilter(eventFilter);
    //Properties
    setDynamicPropertiesFilter(eventFilter, propertiesFilter);
    //Dates
    setDatesFilter(eventFilter);
    //Order by
    setOrderBy(eventFilter);

    eventViewBean.search(eventFilter);

    clearInfoBrowser(eventFilter);
    resetEventFilter();


    return "event_search";
  }

  public String reset()
  {
    return null;
  }

  public String searchType()
  {
    TypeSearchBean typeSearchBean = (TypeSearchBean)getBean("typeSearchBean");
    if (typeSearchBean == null)
      typeSearchBean = new TypeSearchBean();

    typeSearchBean.setRootTypeId(DictionaryConstants.EVENT_TYPE);
    typeSearchBean.setFilter(new TypeFilter());
    typeSearchBean.search();

    return searchObject("Type",
      "#{eventSearchBean.propertiesFilter.selectedTypeId}");
  }

  public String searchTheme()
  {
    ThemeSearchBean themeSearchBean = 
      (ThemeSearchBean)getBean("themeSearchBean");
    if (themeSearchBean == null)
      themeSearchBean = new ThemeSearchBean();

    themeSearchBean.search();

    return searchObject("Theme", "#{eventSearchBean.themeId}");
  }

  public String searchPerson()
  {
    PersonSearchBean personSearchBean = 
      (PersonSearchBean)getBean("personSearchBean");
    if (personSearchBean == null)
      personSearchBean = new PersonSearchBean();

    personSearchBean.search();

    return searchObject("Person", "#{eventSearchBean.selectedPerson}");
  }

  public String searchRoom()
  {
    RoomSearchBean roomSearchBean = (RoomSearchBean)getBean("roomSearchBean");
    if (roomSearchBean == null)
      roomSearchBean = new RoomSearchBean();

    roomSearchBean.search();

    return searchObject("Room", "#{eventSearchBean.selectedRoom}");
  }
  
  public void searchSelectedDay(ValueChangeEvent event)
  {
    Object newValue = event.getNewValue();
    if (newValue != null) //selectedDate changed
    {
      setSelectedDate((Date)newValue);
      search();
    }
  }

  public void setSelectedDate(Date selectedDate)
  {
    if (selectedDate != null)
    {
      startDateFilter = 
        TextUtils.formatDate(selectedDate, "yyyyMMdd") + "000000";
      endDateFilter = 
        TextUtils.formatDate(selectedDate, "yyyyMMdd") + "235959";
    }
  }

  public Date getSelectedDate()
  {
    Date selectedDate = null;
    if (eventViewBean instanceof ScheduleEventSearchView)
    {
      selectedDate = ((ScheduleEventSearchView)eventViewBean).getSelectedDate();
    }
    else
    {
      if (startDateFilter != null)
        selectedDate = TextUtils.parseInternalDate(startDateFilter);
    }

    return selectedDate;
  }

  public String getOnlyCurrentDate()
  {
    return onlyCurrentDate;
  }

  public void setOnlyCurrentDate(String onlyCurrentDate)
  {
    this.onlyCurrentDate = onlyCurrentDate;
  }

  public boolean filterOnlyCurrentDate()
  {
    return "true".equalsIgnoreCase(onlyCurrentDate);
  }

  public String showEvent()
  {
    return showEvent((String)getValue("#{row.eventId}"));
  }

  public String showEvent(String eventId)
  {
    ControllerBean controllerBean = getControllerBean();
    return controllerBean.showObject(DictionaryConstants.EVENT_TYPE, eventId);
  }
  
  @Override
  public DetailBean getDetailBean()
  {
    return new EventDetailBean();
  }  
  
  @Override
  public boolean isDetailViewConfigured()
  {
    return true;
  }

  public String showDetail()    
  {
    return showDetail((String)getValue("#{row.eventId}"));
  }

  public String selectEvent()
  {
    return getControllerBean().select((String)getValue("#{row.eventId}"));
  }
  
  public String searchObject(String typeId, String valueBinding)
  {
    return getControllerBean().searchObject(typeId, valueBinding);
  }

  public String createPropDefSelectItems()
  {
    propertiesFilter.createPropDefSelectItems();
    return reset();
  }

  public String pickUpEvent()
  {
    selectedEvent = null;
    return getControllerBean().searchObject("Event",
      "#{eventSearchBean.selectedEventId}");
  }

  public Event getSelectedEvent()
  {
    return selectedEvent;
  }

  public void setSelectedEventId(String eventId)
  {
    try
    {
      Event event = AgendaConfigBean.getPort().loadEventFromCache(eventId);
      //Check configured filters
      checkConfiguredFilters(event);
      selectedEvent = event;

      // reset content filter
      eventFilter.setContent(null);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void addSelectedEvent()
  {
    try
    {
      //Dynamic Property
      String nodePropName = getProperty(SEARCH_EVENT_PROPERTY_NAME);
      if (nodePropName != null)
      {
        List<String> nodePropValues = getSelectedMenuItem().
          getMultiValuedProperty(SEARCH_EVENT_PROPERTY_VALUE);

        // Set property to event
        PojoUtils.setDynamicProperty(selectedEvent.getProperty(), nodePropName,
          nodePropValues, Property.class);
        AgendaConfigBean.getPort().storeEvent(selectedEvent);

        // reload events
        search();

        // select stored event in schedule
        ScheduleEventViewBean scheduleEventViewBean =
          (ScheduleEventViewBean)getValue("#{scheduleEventViewBean}");
        ScheduleModel model = scheduleEventViewBean.getModel();
        Iterator it = ((SimpleScheduleModel)model).iterator();
        while (it.hasNext())
        {
          ScheduleDay scheduleDay = (ScheduleDay)it.next();
          Iterator it2 = scheduleDay.iterator();
          while (it2.hasNext())
          {
            ScheduleEntry entry = (ScheduleEntry)it2.next();
            if (entry.getId().equals(selectedEvent.getEventId()))
            {
              model.setSelectedEntry(entry);
            }
          }
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    finally
    {
      selectedEvent = null;
    }
  }

  public void discardSelectedEvent()
  {
    selectedEvent = null;
  }

  //Renderers
  public boolean isRender(String name, boolean defValue)
  {
    String value = getProperty(name);
    if (value == null)
      return defValue;
    else
      return Boolean.valueOf(value);
  }

  public boolean isRender(String name)
  {
    return isRender(name, true);
  }

  public boolean isRenderFilterPanel()
  {
    return isRender(RENDER_FILTER_PANEL);
  }

  public boolean isRenderCalendarPanel()
  {
    return isRender(RENDER_CALENDAR_PANEL);
  }

  public boolean isRenderEventIdFilter()
  {
    return isRender(RENDER_EVENTID_FILTER);
  }

  public boolean isRenderContentFilter()
  {
    return isRender(RENDER_CONTENT_FILTER);
  }

  public boolean isRenderPersonFilter()
  {
    return isRender(RENDER_PERSON_FILTER);
  }

  public boolean isRenderRoomFilter()
  {
    return isRender(RENDER_ROOM_FILTER);
  }

  public boolean isRenderFilterInfo()
  {
    return isRender(RENDER_FILTER_INFO);
  }

  public boolean isRoomFilterActive()
  {
    boolean active = false;
    String filteredRoom = eventFilter.getRoomId();
    if (!StringUtils.isBlank(filteredRoom))
    {
      List<String> roomIdList = getNodeRoomIdList();
      if (isEmpty(roomIdList) || roomIdList.size() > 1)
        active = true;
    }
    return active;
  }
  
  public boolean isPersonFilterActive()
  {
    boolean active = false;
    String filteredPerson = eventFilter.getPersonId();
    if (!StringUtils.isBlank(filteredPerson))
    {
      List<String> personIdList = getNodePersonIdList();
      if (isEmpty(personIdList) || personIdList.size() > 1)
        active = true;
    }
    return active;
  }  

  public boolean isRenderThemeFilter()
  {
    return isRender(RENDER_THEME_FILTER);
  }

  public boolean isRenderDateFilter()
  {
    return isRender(RENDER_DATE_FILTER);
  }

  public boolean isRenderTypeFilter()
  {
    return isRender(RENDER_TYPE_FILTER);
  }

  public boolean isTypeFilterActive()
  {
    boolean active = false;
    String filteredTypeId = propertiesFilter.getCurrentTypeId();
    if (!StringUtils.isBlank(filteredTypeId) &&
        !DictionaryConstants.EVENT_TYPE.equals(filteredTypeId))
    {
      active = true;
    }
    return active;
  }

  public boolean isRenderInputPropertiesFilter()
  {
    return isRender(RENDER_INPUT_PROPERTIES_FILTER);
  }

  public boolean isRenderSelectPropertiesFilter()
  {
    return isRender(RENDER_SELECT_PROPERTIES_FILTER);
  }

  public boolean isRenderActionsBar()
  {
    return isRender(RENDER_ACTIONS_BAR);
  }

  public boolean isRenderPublicIcon()
  {
    return isRender(RENDER_PUBLIC_ICON);
  }
  
  public boolean isRenderOnlyAttendantsIcon()
  {
    return isRender(RENDER_ONLY_ATTENDANTS_ICON, false);
  }  

  public boolean isRenderPickUpButton()
  {
    return isRender(RENDER_PICKUP_BUTTON, isRender(RENDER_PICK_BUTTON, false));
  }

  public boolean isEditorUser() throws Exception
  {
    // is permanent agenda admin
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    Set userRoles = userSessionBean.getRoles();
    if (userRoles.contains(AgendaConstants.AGENDA_ADMIN_ROLE))
      return true;

    // is restricted agenda admin
    if (AgendaConfigBean.isRunAsAdmin()) return true;

    // is user in updates roles
    MenuItemCursor mic = userSessionBean.getSelectedMenuItem();
    List<String> rolesUpdate = 
      mic.getMultiValuedProperty(ROLES_UPDATE_PROPERTY);
    if (rolesUpdate != null && rolesUpdate.size() > 0)
    {
      for (String roleUpdate : rolesUpdate)
      {
        if (userRoles.contains(roleUpdate))
        {
          return true;
        }
      }
      return false;
    }
    else
    {
      return true;
    }
  }

  public boolean isShowEventAllowed() throws Exception
  {
    String securitySource = getProperty(SHOW_EVENT_SECURITY_SOURCE);
    
    Boolean rowEditable = ((Boolean)getValue("#{row.editable}"));
    boolean isEventEditable = 
      (rowEditable != null ? rowEditable.booleanValue() : false);

    if (EVENT_SECURITY_SOURCE.equalsIgnoreCase(securitySource))
      return isEventEditable;
    else if (NODE_AND_EVENT_SECURITY_SOURCE.equalsIgnoreCase(securitySource))
      return isEditorUser() && isEventEditable;
    else
      return isEditorUser();
  }

  public String getScheduleSelectMode()
  {
    return getProperty(SCHEDULE_SELECT_MODE);
  }

  public String getScheduleDateFormat()
  {
    String format = getProperty(SCHEDULE_DATE_FORMAT);
    return format != null ? format : "EE dd/MM/yyyy";
  }
  
  public String getFilterCalendarPattern()
  {
    return TextUtils.getStandaloneMonthPattern("LLLL yyyy");
  }
  
  private void initEventFilter()
  {
    if (propertiesFilter.getCurrentTypeId() != null &&
        propertiesFilter.getCurrentTypeId().length() > 0)
    {
      eventFilter.getEventTypeId().add(propertiesFilter.getCurrentTypeId());
    }
    if (themeId != null && themeId.length() > 0)
    {
      eventFilter.getThemeId().add(themeId);
    }
    if (roomId != null)
    {
      eventFilter.setRoomId(extractRoomId(roomId));
    }
    if (personId != null)
    {
      eventFilter.setPersonId(personId);
    }
  }

  private void clearEventFilter()
  {
    if (!isRenderFilterPanel() || !isRenderContentFilter())
      eventFilter.setContent(null);
    if (!isRenderFilterPanel() || !isRenderEventIdFilter())
    {
      eventFilter.getEventId().clear();
      eventId = null;
    }
    if (!isRenderFilterPanel() || !isRenderPersonFilter())
    {
      personId = null;
      eventFilter.setPersonId(null);
    }
    if (!isRenderFilterPanel() || !isRenderRoomFilter())
    {
      roomId = null;
      eventFilter.setRoomId(null);
      infoBrowser = null;
    }
    if (!isRenderFilterPanel() || !isRenderThemeFilter())
    {
      eventFilter.getThemeId().clear();
      themeId = null;
    }
    if (!isRenderFilterPanel() || !isRenderDateFilter())
    {
      eventFilter.setStartDateTime(null);
      eventFilter.setEndDateTime(null);
      themeId = null;
    }    
    if (!isRenderFilterPanel() || !isRenderTypeFilter())
    {
      eventFilter.getEventTypeId().clear();
      propertiesFilter.setCurrentTypeId(null);
      infoBrowser = null;
    }
    if (!isRenderFilterPanel() || !isRenderInputPropertiesFilter()
      || !isRenderSelectPropertiesFilter())
    {
      eventFilter.getProperty().clear();
      propertiesFilter.setInputValue(null);
      propertiesFilter.setSelectValue(null);
    }
  }

  private void resetEventFilter()
  {
    eventFilter.getEventId().clear();
    eventFilter.getEventTypeId().clear();
    eventFilter.getThemeId().clear();
    if (isAttendantMode)
      eventFilter.setPersonId(null);
  }

  private void setEventIdFilter(EventFilter filter)
  {
    if (eventId != null && eventId.trim().length() > 0)
    {
      filter.getEventId().clear();
      filter.getEventId().add(eventId);
    }
  }

  private void setEventTypeIdFilter(EventFilter filter)
  {
    List<String> eventTypes = getNodeTypeIdList();

    boolean isNodeFilterSet = (eventTypes != null && eventTypes.size() > 0);
    boolean isUserFilterSet = (propertiesFilter.getCurrentTypeId() != null &&
      propertiesFilter.getCurrentTypeId().length() > 0);

    if (isNodeFilterSet && !isUserFilterSet)
    {
      filter.getEventTypeId().clear();
      filter.getEventTypeId().addAll(eventTypes);
    }
    else if (isUserFilterSet)
    {
      filter.getEventTypeId().add(propertiesFilter.getCurrentTypeId());
    }
  }

  private void setAttendantModeFilter(EventFilter filter)
  {
    String attendantMode = getProperty(ATTENDANT_MODE);

    boolean isNodeFilterSet = attendantMode != null;
    isAttendantMode = isNodeFilterSet && attendantMode.equalsIgnoreCase("true");
    boolean isUserFilterSet = filter.getPersonId() != null;

    if (isNodeFilterSet)
    {
      if (isAttendantMode)
      {
        Credentials credentials =
          UserSessionBean.getCurrentInstance().getCredentials();
        if (credentials != null)
          filter.setPersonId(UserCache.getUser(credentials).getPersonId());
      }
    }
    else if (isUserFilterSet) //attendantMode == false
    {
      filter.setPersonId(filter.getPersonId());
    }
    else
    {
      setPersonIdFilter(filter);
    }
  }

  private void setThemeIdFilter(EventFilter filter)
  {
    List<String> eventThemes = getNodeThemeIdList();

    boolean isNodeFilterSet = (eventThemes != null && eventThemes.size() > 0);
    boolean isUserFilterSet = !isEmpty(filter.getThemeId());
    String themeId = null;

    if (isUserFilterSet)
      themeId = filter.getThemeId().get(0);

    if (isNodeFilterSet)
    {
      if (isUserFilterSet && eventThemes.contains(themeId))
        filter.getThemeId().add(themeId);
      else if (!isUserFilterSet)
        filter.getThemeId().addAll(eventThemes);
    }
    else if (isUserFilterSet)
    {
      filter.getThemeId().add(themeId);
    }
  }

  private void setRoomIdFilter(EventFilter filter)
  {
    List<String> eventRooms = getNodeRoomIdList();

    boolean isNodeFilterSet = (eventRooms != null && eventRooms.size() > 0);
    boolean isUserFilterSet = filter.getRoomId() != null;
    String roomId = null;

    if (isUserFilterSet)
      roomId = filter.getRoomId();

    if (isNodeFilterSet)
    {
      if (eventRooms.size() == 1)
        filter.setRoomId(eventRooms.get(0));
      else if (isUserFilterSet && eventRooms.contains(roomId))
        filter.setRoomId(roomId);
    }
    else if (isUserFilterSet)
    {
      filter.setRoomId(roomId);
    }
  }
  
  private void setPersonIdFilter(EventFilter filter)
  {
    List<String> eventPersons = getNodePersonIdList();

    boolean isNodeFilterSet = (eventPersons != null && eventPersons.size() > 0);
    boolean isUserFilterSet = filter.getPersonId() != null;
    String personId = null;

    if (isUserFilterSet)
      personId = filter.getPersonId();

    if (isNodeFilterSet)
    {
      if (eventPersons.size() == 1)
        filter.setPersonId(eventPersons.get(0));
      else if (isUserFilterSet && eventPersons.contains(personId))
        filter.setPersonId(personId);
    }
    else if (isUserFilterSet)
    {
      filter.setPersonId(personId);
    }
  }  

  private void clearInfoBrowser(EventFilter filter)
  {
    if (infoBrowser != null 
      && (roomIdParam != null || eventTypeIdParam != null))
    {
      boolean keepTypeId = filter.getEventTypeId() != null
        && !filter.getEventTypeId().isEmpty()
        && filter.getEventTypeId().contains(eventTypeIdParam);
      boolean keepRoomId = filter.getRoomId() != null
        && roomIdParam != null && roomIdParam.equals(filter.getRoomId());

      if (!keepTypeId && !keepRoomId)
      {
        infoBrowser = null;
        roomIdParam = null;
        eventTypeIdParam = null;
      }
    }
  }

  private void setDynamicPropertiesFilter(EventFilter filter, PropertiesFilter
    propertiesFilter)
  {
    String nodePropName = getProperty(SEARCH_EVENT_PROPERTY_NAME);
    filter.getProperty().clear();

    if (!propertiesFilter.getInputProps().isEmpty())
      setDynamicPropertiesFilter(filter, propertiesFilter.getInputProps());
    if (!propertiesFilter.getSelectProps().isEmpty())
      setDynamicPropertiesFilter(filter, propertiesFilter.getSelectProps());

    if (nodePropName != null)
    {
      Property nodeProp = null;
      for (Property prop : filter.getProperty())
      {
        if (nodePropName.equals(prop.getName()))
          nodeProp = prop;
      }
      if (nodeProp != null)
        filter.getProperty().remove(nodeProp);

      nodeProp = new Property();
      nodeProp.setName(nodePropName);
      nodeProp.getValue().addAll(getNodePropertyValueList());
      filter.getProperty().add(nodeProp);
    }
  }
  
  private void setDefaultDatesFilter()
  {
    if (!StringUtils.isBlank(getNodeStartDate()))
    {
      if (isRenderCalendarPanel())
        setSelectedDate(TextUtils.parseInternalDate(getNodeStartDate()));
      else
        startDateFilter = getNodeStartDate() + "000000";
    }
    
    if (!StringUtils.isBlank(getNodeStartDate()) && !isRenderCalendarPanel())
    {
      endDateFilter = getNodeEndDate() + "235959";
    }
  }

  private void setDatesFilter(EventFilter filter)
  {
    if (StringUtils.isBlank(startDateFilter))
    {
      String now = new SimpleDateFormat("yyyyMMdd").format(new Date());
      startDateFilter = now + "000000";
      filter.setStartDateTime(startDateFilter);
      if (isRenderCalendarPanel() && filterOnlyCurrentDate())
        endDateFilter = now + "235959";
    }
    else
    {
      String searchDays = getProperty(SEARCH_DAYS);
      if (searchDays != null)
      {
        Integer intDays = Integer.parseInt(searchDays);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 0 - intDays.intValue());
        Date minDate = cal.getTime();
        Date startDate = TextUtils.parseInternalDate(startDateFilter);
        if (minDate.after(startDate))
        {
          String startDT = TextUtils.formatDate(minDate, "yyyyMMdd") + "000000";
          filter.setStartDateTime(startDT);
        }
        else
          filter.setStartDateTime(startDateFilter);
      }
      else
        filter.setStartDateTime(startDateFilter);
    }

    if (!StringUtils.isBlank(getNodeDateComparator()))
    {
      filter.setDateComparator(getNodeDateComparator());
    }

    //Set end date
    if (isRenderCalendarPanel() && filterOnlyCurrentDate())
    {
      String endDT = filter.getStartDateTime().substring(0, 8) + "235959";
      filter.setEndDateTime(endDT);
    }
    else if (isRenderCalendarPanel() && !filterOnlyCurrentDate())
      filter.setEndDateTime(null);
    else
    {
      if (!StringUtils.isBlank(endDateFilter))
        filter.setEndDateTime(endDateFilter.substring(0, 8) + "235959");
      else
        filter.setEndDateTime(null);
    }

    //Schedule sync
    if (eventViewBean instanceof ScheduleEventSearchView)
    {
      Date selectedDate = 
        TextUtils.parseInternalDate(filter.getStartDateTime());
      ((ScheduleEventSearchView)eventViewBean).setSelectedDate(selectedDate);
    }
  }

  private List<SelectItem> createRoomSelectItems(String roomId)
  {
    List<SelectItem> result = new ArrayList();
    RoomBean roomBean = (RoomBean)getBean("roomBean");
    List roomIdList = getNodeRoomIdList();
    if (roomIdList.isEmpty())
      result = roomBean.getSelectItems(roomId);
    else
      result = roomBean.getSelectItems(roomIdList, roomId);

    if (!result.isEmpty())
    {
      for (SelectItem item : result)
      {
        String id = (String)item.getValue();
        String label = (String)item.getLabel();
        if (label.equals(id)) 
        { 
          //is not a room, is probably an address
          if (!StringUtils.isBlank(id))
          {
            String description = id;
            if (id.contains("\"") && id.endsWith("\""))
            {
              int idx = id.indexOf("\"");
              int lastIdx = id.lastIndexOf("\"");
              description = id.substring(idx + 1, lastIdx);
            }
            else
            {
              description =
                AddressDescriptionCache.getInstance().getAddressDescription(id);
            }

            if (description != null)
            {
              if (description.equals("NOT_FOUND"))
              {
                ResourceBundle bundle = ResourceBundle.getBundle(
                  "org.santfeliu.web.resources.MessageBundle", getLocale());
                description = bundle.getString(description) + " (" + id + ")";
              }
              item.setLabel(description);
              item.setDescription(description);
            }
          }
        }
      }
    }

    String sort = getProperty(SORT_EVENT_ROOM);
    if (sort != null && !"false".equals(sort))
      result = FacesUtils.sortSelectItems(result);

    return result;
  }

  private String extractRoomId(String description)
  {
    String roomId = description;
    if (description != null && description.contains("\"") 
      && description.endsWith("\""))
    {
      roomId = description.substring(0, description.indexOf("\"")).trim();
    }

    return roomId;
  }

  private List<SelectItem> createTypeSelectItems(String eventTypeId)
  {
    String[] actions = {DictionaryConstants.READ_ACTION};
    List<SelectItem> result = new ArrayList();
    TypeBean typeBean = (TypeBean)getBean("typeBean");
    List typeIdList = getNodeTypeIdList();
    if (typeIdList.isEmpty())
    {
      if (!isRenderTypeFilter() && isTypeFilterActive())
        result = typeBean.getSelectItems(new ArrayList(), eventTypeId);
      else
      {
        result.add(new SelectItem(ControllerBean.NEW_OBJECT_ID, " "));
        result.addAll(typeBean.getAllSelectItems(DictionaryConstants.EVENT_TYPE,
          AgendaConstants.AGENDA_ADMIN_ROLE, actions, false));
      }
    }
    else
    {
      result = typeBean.getSelectItems(
        typeIdList, AgendaConstants.AGENDA_ADMIN_ROLE, actions, false);
      String sort = getProperty(SORT_EVENT_TYPE);
      if (sort != null && !"false".equals(sort))
        result = FacesUtils.sortSelectItems(result);
    }
    return result;
  }

  private List<SelectItem> createThemeSelectItems(String themeId)
  {
    List<SelectItem> result = new ArrayList();
    ThemeBean themeBean = (ThemeBean)getBean("themeBean");
    List themeIdList = getNodeThemeIdList();
    if (themeIdList.isEmpty())
      result = themeBean.getAllSelectItems(themeId);
    else
    {
      result = themeBean.getSelectItems(themeIdList, themeId);
      String sort = getProperty(SORT_EVENT_THEME);
      if (sort != null && !"false".equals(sort))
        result = FacesUtils.sortSelectItems(result);
    }

    return result;
  }

  private void setOrderBy(EventFilter filter)
  {
    filter.getOrderBy().clear();
    List<String> orderby = 
      getSelectedMenuItem().getMultiValuedProperty(ORDERBY);
    Iterator it = orderby.iterator();
    while (it.hasNext())
    {
      String orderByElement = (String) it.next();
      String[] parts = orderByElement.split(":");
      OrderByProperty orderByProperty = new OrderByProperty();
      orderByProperty.setName(parts[0]);
      if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1]))
      {
        orderByProperty.setDescending(true);
      }
      filter.getOrderBy().add(orderByProperty);
    }
  }

  private boolean isEmpty(List<String> list)
  {
    if (list == null || list.isEmpty() || 
      (list.size() == 1 && list.get(0).length() == 0))
    {
      return true;
    }
    else
      return false;
  }

  private boolean hasFirstLoadProperty()
  {
    MenuItemCursor cursor = getSelectedMenuItem();
    String firstLoad = cursor.getProperty(PROPERTY_FIRSTLOAD);
    return "true".equals(firstLoad);
  }

  private void setDynamicPropertiesFilter(EventFilter filter,
    List<Property> props)
  {
    for (Property dp : props)
    {
      if (dp.getName() != null && dp.getName().length() > 0)
      {
        filter.getProperty().add(dp);
      }
    }
  }
  
  private String getInfoDocumentUrl(String propName, String propValue)
  {
    String url = null;
    Credentials credentials =
      UserSessionBean.getCurrentInstance().getCredentials();
    CachedDocumentManagerClient client = new CachedDocumentManagerClient(
      credentials.getUserId(), credentials.getPassword());
    Document document =
      client.loadDocumentByName("Document", propName, propValue, null, 0);
    if (document != null)
      url = DocumentUrlBuilder.getDocumentUrl(document);

    return url;
  }

  private void checkConfiguredFilters(Event event) throws Exception
  {
    checkConfiguredType(event);
    checkConfiguredThemes(event);
  }

  private void checkConfiguredType(Event event) throws Exception
  {
    String eventTypeId = getProperty(SEARCH_EVENT_TYPE);
    if (eventTypeId != null && !eventTypeId.equals(event.getEventTypeId()))
      throw new Exception("EVENTTYPEID_NOT_MATCH");
  }

  private void checkConfiguredThemes(Event event) throws Exception
  {
    List<String> nodeThemes =
      getSelectedMenuItem().getMultiValuedProperty(SEARCH_EVENT_THEME);
    if (nodeThemes != null && nodeThemes.size() > 0)
    {
      EventThemeFilter eventThemeFilter = new EventThemeFilter();
      eventThemeFilter.setEventId(event.getEventId());
      AgendaManagerClient client = AgendaConfigBean.getPort();
      List<EventThemeView> eventThemes =
        client.findEventThemeViewsFromCache(eventThemeFilter);
      if (eventThemes == null || eventThemes.isEmpty())
        throw new Exception("EVENTTHEME_NOT_MATCH");

      boolean match = false;
      for (int i = 0; i < eventThemes.size() && !match; i++)
      {
        String theme = eventThemes.get(i).getThemeId();
        if (theme != null && nodeThemes.contains(theme))
          match = true;
      }
      if (!match)
        throw new Exception("EVENTTHEME_NOT_MATCH");
    }
  }
  
  public void setSelectedPerson(String personId)
  {
    this.personSelectItems = null;
    eventFilter.setPersonId(personId);
  }
  
  public String getSelectedPerson()
  {
    return eventFilter.getPersonId();
  }
  
  public void setSelectedRoom(String roomId)
  {
    this.roomSelectItems = null;
    eventFilter.setRoomId(roomId);
  }  
  
  public String getSelectedRoom()
  {
    return eventFilter.getRoomId();
  }

}
