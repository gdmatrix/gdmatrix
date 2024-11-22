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

import java.net.URL;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.activation.FileTypeMap;

import javax.faces.component.UISelectItems;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.model.SelectItem;


import org.matrix.agenda.AgendaConstants;
import org.matrix.agenda.Event;
import org.matrix.agenda.EventDocument;
import org.matrix.agenda.EventDocumentFilter;

import org.matrix.agenda.EventFilter;
import org.matrix.agenda.EventPlace;
import org.matrix.agenda.EventPlaceFilter;

import org.matrix.agenda.OrderByProperty;
import org.matrix.agenda.SecurityMode;

import org.santfeliu.agenda.client.AgendaManagerClient;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.web.DictionaryConfigBean;
import org.santfeliu.faces.FacesBean;
import org.santfeliu.faces.browser.HtmlBrowser;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.util.MimeTypeMap;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;


/**
 *
 * @author unknown
 */
@Deprecated
//use EventSearchBean instead
public class AgendaBean
  extends FacesBean
{
  // Max number of rows rendered by default.
  // Only used if "body.maxRows" menuItem property not specified.
  int MAXROWS = 500;
  public static final String UUID = "uuid";
  public static final String MIMETYPE = "mimetype";

  private String selectedDay;
  //private Date lastDay;

  private EventFilter filter = new EventFilter();
  private Event currentEvent = new Event();

  private HtmlDataTable eventsTable;
  private UISelectItems eventTypes;

  private boolean showMessages = false;

  SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMdd");
  SimpleDateFormat sHourFormat = new SimpleDateFormat("HHmmss");
  SimpleDateFormat sDateHumanFormat = new SimpleDateFormat("dd/MM/yyyy");

  private List<EventPlace> eventPlaces;
  private List<EventDocument> eventDocuments;
  private int currentFirstPage;

  //public static long DEFAULT_DATES_INTERVAL_GAP = 30;
  public static final int DEFAULT_ROWSPERPAGE = 5;

  private boolean renderStartDateFilter;
  private boolean renderEndDateFilter;
  private boolean renderEventTypeFilter;
  private boolean renderContentFilter;

  private String eventType;
  private String eventContent;
  private String moreInfoLabel;
  private String imageURL;

  private HtmlBrowser headerBrowser = new HtmlBrowser();
  private HtmlBrowser footerBrowser = new HtmlBrowser();

  private final String FILTER_CONTENT_RENDER = "filterPanel.content.render";
  private final String FILTER_CONTENT_DEFVAL = "filterPanel.content.defVal";
  private final String FILTER_EVENTTYPE_RENDER = "filterPanel.eventType.render";
  private final String FILTER_EVENTTYPE_DEFVAL = "filterPanel.eventType.defVal";
  private final String FILTER_STARTDATE_RENDER = "filterPanel.startDate.render";
  private final String FILTER_STARTDATE_DEFVAL = "filterPanel.startDate.defVal";
  //private final String FILTER_ENDDATE_RENDER = "filterPanel.endDate.render";
  private final String FILTER_THEME = "filterPanel.theme";

  private final String BODY_MAXROWS = "resultList.maxRows";
  private final String BODY_ROWSPERPAGE = "resultList.rowsPerPage";

  private final String BODY_MOREINFOLABEL = "body.moreInfoLabel";
  @Deprecated
  public static final String HEADER_DOCUMENT_PROPERTY = "header.document";
  @Deprecated
  public static final String FOOTER_DOCUMENT_PROPERTY = "footer.document";
  public static final String HEADER_DOCID_PROPERTY = "header.docId";
  public static final String FOOTER_DOCID_PROPERTY = "footer.docId";
  public static final String DOC_SERVLET_PATH = "/documents/";
  public static final String ICON_PATH = "/common/doc/images/";

  private final String servletURL;

  public AgendaBean()
  {
    servletURL = getContextURL() + DOC_SERVLET_PATH;
  }

  // VALUE BINDINGS
  public void setSelectedDay(String selectedDay)
  {
    this.selectedDay = selectedDay;
  }

  /**
   * Getter of the user selected date. Null dates not supported, in this case
   * today Date is returned.
   *
   * @return String with internal format date
   */
  public String getSelectedDay() throws Exception
  {
    if (selectedDay == null)
    {
      MenuItemCursor mic =
        UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
      Object value = mic.getProperties().get(this.FILTER_STARTDATE_DEFVAL);
      if (value == null)
      {
        selectedDay = TextUtils.formatDate(new java.util.Date(),"yyyyMMdd");
        return selectedDay;
      }
      else
      {
        try
        {
          selectedDay =
            TextUtils.formatDate(TextUtils.parseUserDate(
              (String)value, "dd/MM/yyyy"),"yyyyMMdd");
        }
        catch (Exception ex)
        {
          selectedDay = TextUtils.formatDate(new java.util.Date(),"yyyyMMdd");
        }
        finally
        {
          return selectedDay;
        }
      }
    }
    else
    {
      return selectedDay;
    }
  }

  public void setFilter(EventFilter filter)
  {
    this.filter = filter;
  }

  public EventFilter getFilter()
  {
    return filter;
  }

/*  public void setLastDay(Date lastDay)
  {
    this.lastDay = lastDay;
  }

  public Date getLastDay()
  {
    if (lastDay == null)
    {
      MenuItemCursor mic =
        UserBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
      Object value = mic.getProperties().get(this.FILTER_ENDDATE_DEFVAL);
      if (value != null)
      {
        try
        {
          lastDay = sDateHumanFormat.parse((String)value);
        }
        catch (Exception ex)
        {
          lastDay = selectedDay;
        }
        finally
        {
          return lastDay;
        }
      }
      else
      {
        long dateMillis;
        if (selectedDay != null)
          dateMillis = selectedDay.getTime();
        else
          dateMillis = System.currentTimeMillis();

        long intervalGap = DEFAULT_DATES_INTERVAL_GAP;
        String intDaysProp =
          (String) mic.getProperties().get(this.BODY_INTERVALDAYS);
        if (intDaysProp != null)
          intervalGap = Long.valueOf(intDaysProp).longValue();

        dateMillis = dateMillis + (86400000 * intervalGap);
        Date date = new Date();
        date.setTime(dateMillis);
        return date;
      }
    }
    else
      return lastDay;
  }
*/
  public void setCurrentEvent(Event currentEvent)
  {
    this.currentEvent = currentEvent;
  }

  public Event getCurrentEvent()
  {
    return currentEvent;
  }

  public String getCurrentEventTypeDescription()
  {
    String description = "";
    if (currentEvent != null)
    {
      String typeId = currentEvent.getEventTypeId();
      Type eventType = TypeCache.getInstance().getType(typeId);
      if (eventType == null)
        description = typeId;
      else
        description = eventType.getDescription();
    }

    return description;
  }

  public boolean isShowMessages()
  {
    return showMessages;
  }

  public List getEventPlaces()
  {
    return eventPlaces;
  }

  public int getCurrentFirstPage()
  {
    return currentFirstPage;
  }

  public void setCurrentFirstPage(int currentFirstPage)
  {
    this.currentFirstPage = currentFirstPage;
  }

  public int getRowsPerPage()
  {
    MenuItemCursor mic =
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
    String rowsPerPage =
      (String)mic.getDirectProperties().get(BODY_ROWSPERPAGE);

    if (rowsPerPage != null)
      return Integer.valueOf(rowsPerPage);
    else
      return DEFAULT_ROWSPERPAGE;
  }

  //COMPONENT BINDINGS
  public void setEventsTable(HtmlDataTable eventsTable)
  {
    this.eventsTable = eventsTable;
  }

  public HtmlDataTable getEventsTable()
  {
    return eventsTable;
  }

  public List<SelectItem> getEventTypes()
  {
    return DictionaryConfigBean.getDerivedTypesSelectItems("Event");
  }

  public void setHeaderBrowser(HtmlBrowser headerBrowser)
  {
    this.headerBrowser = headerBrowser;
  }

  public HtmlBrowser getHeaderBrowser()
  {
    MenuItemCursor mic =
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
    String docId =
      (String) mic.getDirectProperties().get(HEADER_DOCID_PROPERTY);
    if (docId == null)
      docId = (String) mic.getDirectProperties().get(HEADER_DOCUMENT_PROPERTY);
    if (docId != null)
    {
      headerBrowser.setUrl(servletURL + docId);
      return headerBrowser;
    }
    else
      return null;
  }

  public void setFooterBrowser(HtmlBrowser footerBrowser)
  {
    this.footerBrowser = footerBrowser;
  }

  public HtmlBrowser getFooterBrowser()
  {
    MenuItemCursor mic =
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
    String docId =
      (String) mic.getDirectProperties().get(FOOTER_DOCID_PROPERTY);
    if (docId == null)
      docId = (String) mic.getDirectProperties().get(FOOTER_DOCUMENT_PROPERTY);
    if (docId != null)
    {
      footerBrowser.setUrl(servletURL + docId);
      return footerBrowser;
    }
    else
      return null;
  }

  public String getRowType()
  {
    String result = null;
    Event event = (Event)getValue("#{row}");
    if (event != null)
    {
      Type type = TypeCache.getInstance().getType(event.getEventTypeId());
      if (type != null)
        result = type.getDescription();
      else
        result = event.getEventTypeId();
    }

    return result;
  }

  // ACTIONS

  /**
   * Loads <code>eventsTable</code> and <code>eventTypes</code> with data.
   * Could be invoked in first request from menu or coming back from an event
   * description view.
   * In this second case, <code>currentFirstPage</code> is asigned to
   * <code>eventsTable</code><code>first</code> property to recover current
   * page of the scroller.
   *
   * @return outcome to the agenda (event list) view
   */
  public String nextEvents()
  {
    Map requestParameters = getExternalContext().getRequestParameterMap();
    String eventId = (String)requestParameters.get("eventid");
    if (eventId != null)
    {
      try
      {
        currentEvent = loadEvent(eventId);
        eventPlaces = findEventPlaces();
        eventDocuments = findEventDocuments();

        return "goEvent";
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
        getExternalContext().log(ex.getMessage());
      }
    }
    else
    {
      //Component constructions
      eventsTable = new HtmlDataTable();
      eventsTable.setValue(find());
      if (currentFirstPage != 0)
        eventsTable.setFirst(currentFirstPage);

      eventTypes = new UISelectItems();
//      eventTypes.setValue(findEventTypes());

    }
    return "agenda";
  }

  /**
   * Loads <code>eventsTable</code> with data. This action is like
   * <code>nextEvents</code> but this is invoked in post back and eventTypes
   * is not loaded.
   *
   * @return outcome null
   */
  public String searchEvents()
  {
    eventsTable.setValue(find());
    eventsTable.setFirst(0);

    return null;
  }

  /**
   * Gets the dataTable selected row and find event places to show in event description view.
   *
   * @return outcome to event description view.
   */
  public String goEvent()
  {
    currentFirstPage = eventsTable.getFirst();
    currentEvent = (Event) getValue("#{row}");
    eventPlaces = findEventPlaces();
    eventDocuments = findEventDocuments();

    return "goEvent";
  }


  // AUXILIAR METHODS
  /**
   * Calls <code>doAction</code> and gets a <code>Table</code> with all found events in the
   * agenda that matches <code>filter</code> properties. <code>filter</code> is a Map property
   * of the class.
   *
   * @return List of events in the agenda that matches <code>filter</code> properties.
   */
  public List find()
  {
    List result = null;

    try
    {
      addFindFilters(filter);
      filter.setSecurityMode(SecurityMode.FILTERED);
      OrderByProperty startDate = new OrderByProperty();
      startDate.setName("startDateTime");
      OrderByProperty endDate = new OrderByProperty();
      endDate.setName("endDateTime");
      filter.getOrderBy().add(startDate);
      filter.getOrderBy().add(endDate);
      result = getPort().findEventsFromCache(filter);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      showMessages = true;
      this.error(ex.getMessage());
    }
    return result;
  }


  /**
   * Auxiliar method to add filters to the filter map. This filters are result
   * of the user interaction and the menuItem configuration of the menu node.
   *
   * @param filterMap
   */
  private void addFindFilters(EventFilter filter)
    throws Exception
  {
    //User interaction
    filter.setStartDateTime(this.getSelectedDay());

    //Putting MenuItem Properties
    MenuItemCursor mic =
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();

    List<String> themeList =  mic.getMultiValuedProperty(FILTER_THEME);
    filter.getThemeId().clear();
    filter.getThemeId().addAll(themeList);

    String maxRows = (String) mic.getProperties().get(this.BODY_MAXROWS);
    if (maxRows != null && maxRows.length() > 0)
      filter.setMaxResults(Integer.valueOf(maxRows).intValue());
    else
      filter.setMaxResults(MAXROWS);

    filter.getEventTypeId().clear();
    filter.getEventTypeId().add(getEventType());
    filter.setContent(getEventContent());

    //Order by
    filter.getOrderBy().clear();
    List<String> orderby = mic.getMultiValuedProperty(AgendaConstants.ORDERBY);
    Iterator it = orderby.iterator();
    Vector fields = new Vector();
    while (it.hasNext())
    {
      String field = (String)it.next();
      fields.add(field);
      OrderByProperty orderBy = new OrderByProperty();
      orderBy.setName(field);
      filter.getOrderBy().add(orderBy);
    }
  }


  /**
   * Gets all event types that matches menu node configuration. The event types
   * collection returned is conformed by <code>SelectItem</code>.
   *
   * If node is configured to show events of one theme then only types of this theme are
   * returned. This uses findEventTypeInUse call.
   *
   * @return Vector of <code>SelectItem</code> event types.
   */
//  private Vector findEventTypes()
//  {
//    Vector types = null;
//    try
//    {
//      types = new Vector();
//
//      //Put map filters
//      MenuItemCursor mic =
//        UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
//      String temacod = (String) mic.getProperties().get(this.FILTER_THEME);
//      Table result = getEventTypeInUse(temacod);
//
//      //Select items construction
//      if (result != null)
//      {
//        for (int i = 0; i < result.getRowCount(); i++)
//        {
//          String id =
//            String.valueOf(result.getElementAt(i, "tipesdevcod"));
//          SelectItem row =
//            new SelectItem(id, (String) result.getElementAt(i, "tipesdevnom"));
//          types.add(row);
//        }
//      }
//    }
//    catch (Exception ex)
//    {
//      showMessages = true;
//      this.error(ex.getLocalizedMessage());
//      ex.printStackTrace();
//    }
//
//    return types;
//  }

  /**
   * Gets all the places where a <code>currentEvent</code> will be done.
   *
   * @return
   */
  private List findEventPlaces()
  {
    List result = null;

    try
    {
      EventPlaceFilter epFilter = new EventPlaceFilter();
      epFilter.setEventId(currentEvent.getEventId());
      result = getPort().findEventPlaceViewsFromCache(epFilter);
    }
    catch (Exception ex)
    {
      showMessages = true;
      this.error(ex.getLocalizedMessage());
      ex.printStackTrace();
    }

    return result;
  }


  private List findEventDocuments()
  {
    List result = null;

    try
    {
      EventDocumentFilter edFilter = new EventDocumentFilter();
      edFilter.setEventId(currentEvent.getEventId());
      result = getPort().findEventDocumentViewsFromCache(edFilter);
    }
    catch (Exception ex)
    {
      showMessages = true;
      this.error(ex.getLocalizedMessage());
      ex.printStackTrace();
    }

    return result;
  }
  /**
   * Uses <code>AgendaManagerClient</code> to call <code>AgendaManager</code> web service module
   * and invoke the <code>action</code> of the service. If action need user validation this sets
   * <code>username</code> and <code>password</code> to the service.
   *
   * @param action name
   * @param filter Map of filters
   * @param username
   * @param password
   * @return Table
   * @throws Exception
   */
//  private Table doAction (String action, Map filter, String username, String password)
//    throws Exception
//  {
//    AgendaManagerPort port = getPort(username, password);
//
//    Table result;
//
//    if (action.equals("find"))
//      result = EventTableConverter.toTable(
//        port.findEvents(EventFilterConverter.fromMap(filter)));
//    else
//      return null;
//
//    return result;
//  }

//   private Table getEventTypeInUse(String themeId)
//    throws Exception
//  {
//      AgendaManagerPort port = getPort();
//      return EventTypeTableConverter.toTable(port.findEventTypesInUse(themeId));
//   }

//  private Table getEventPlaces(String eventId)
//   throws Exception
//  {
//     AgendaManagerPort port = getPort();
//     return EventPlaceTableConverter.toTable(port.findEventPlaces(eventId));
//  }

  //Date&Hour Formaters
  //Value Binding
  public Date getFormatedInitialDate()
  {
    return this.convertDate(sDateFormat, "datainici");
  }

  public Date getFormatedFinalDate()
  {
    return this.convertDate(sDateFormat, "datafinal");
  }

  public Date getFormatedInitialHour()
  {
    return this.convertDate(sHourFormat, "horainici");
  }

  public Date getFormatedFinalHour()
  {
    return this.convertDate(sHourFormat, "horafinal");
  }


  /**
   * Convert <code>String</code> dates of an event to <code>Date</code> using
   * <code>format</code> formatting. Called by user interface to show
   * dates like <code>Date</code> objects. By this way data converters could be applied
   * to date fileds.
   *
   * This method is used in the result list of events and in the description of an event
   * view.
   *
   * @param format
   * @param dateKey field sql.
   * @return Date converted from
   */
  private Date convertDate(SimpleDateFormat format, String dateKey)
  {
    String date = null;
    Event event = (Event)getValue("#{row}");
    if (event == null)
      event = currentEvent;

    if (dateKey.equals("datainici") && event.getStartDateTime() != null)
      date = event.getStartDateTime().substring(0, 8);
    else if (dateKey.equals("datafinal") && event.getEndDateTime() != null)
      date = event.getEndDateTime().substring(0, 8);
    else if (dateKey.equals("horainici") && event.getStartDateTime() != null)
      date = event.getStartDateTime().substring(8, 14);
    else if (dateKey.equals("horafinal") && event.getEndDateTime() != null)
      date = event.getEndDateTime().substring(8, 14);

    if (date != null)
    {
      try
      {
        return format.parse(date);
      }
      catch (Exception e)
      {
        this.error(e.getLocalizedMessage());
        return null;
      }
    }
    else
      return null;
  }

  public boolean isRenderFilterPanel()
  {
    renderStartDateFilter = renderFilter(this.FILTER_STARTDATE_RENDER);
    renderEventTypeFilter = renderFilter(this.FILTER_EVENTTYPE_RENDER);
    renderContentFilter = renderFilter(this.FILTER_CONTENT_RENDER);

    return ((renderStartDateFilter) ||
            (renderEventTypeFilter) ||
            (renderContentFilter));
  }

  private AgendaManagerClient getPort() throws Exception
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    String userId = userSessionBean.getUsername();
    String password = userSessionBean.getPassword();
    return AgendaConfigBean.getPort(userId, password);
  }

  private boolean renderFilter(String filterName)
  {
    boolean result = true;
    MenuItemCursor mic =
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
    Object value = mic.getProperties().get(filterName);
    if (value != null)
    {
      return Boolean.parseBoolean((String)value);
    }
    return result;
  }

  public boolean isRenderStartDateFilter()
  {
    return renderStartDateFilter;
  }

  public boolean isRenderEndDateFilter()
  {
    return renderEndDateFilter;
  }

  public boolean isRenderEventTypeFilter()
  {
    return renderEventTypeFilter;
  }

  public boolean isRenderContentFilter()
  {
    return renderContentFilter;
  }

  public void setEventType(String eventType)
  {
    this.eventType = eventType;
  }

  public String getEventType()
  {
    if (eventType == null)
    {
      MenuItemCursor mic =
        UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
      Object value = mic.getProperties().get(this.FILTER_EVENTTYPE_DEFVAL);
      if (value != null)
      {
        return String.valueOf(value);
      }
      else
      {
        return null;
      }
    }
    else
    {
      return eventType;
    }
  }

  public void setEventContent(String eventContent)
  {
    this.eventContent = eventContent;
  }

  public String getEventContent()
  {
    if (eventContent == null)
    {
      MenuItemCursor mic =
        UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
      Object value = mic.getProperties().get(this.FILTER_CONTENT_DEFVAL);
      if (value != null)
      {
        return String.valueOf(value);
      }
      else
      {
        return null;
      }
    }
    else
    {
      return eventContent;
    }
  }

  public void setMoreInfoLabel(String moreInfoLabel)
  {
    this.moreInfoLabel = moreInfoLabel;
  }

  public String getMoreInfoLabel()
  {
    if (moreInfoLabel == null)
    {
      MenuItemCursor mic =
        UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
      Object value = mic.getProperties().get(this.BODY_MOREINFOLABEL);
      if (value == null)
      {
        moreInfoLabel = "+ info";
      }
      else
      {
        moreInfoLabel = String.valueOf(value);
      }
    }
    return moreInfoLabel;
  }


  public void setImageURL(String imageURL)
  {
    this.imageURL = imageURL;
  }

  public String getTranslationGroup()
  {
    String eventId = String.valueOf(getValue("#{row.eventId}"));
    return "event:" + eventId;
  }

  public String getEventTranslationGroup()
  {
    return "event:" + currentEvent.getEventId();
  }

  public String show()
  {
    Map requestParameters = getExternalContext().getRequestParameterMap();
    String eventId = (String)requestParameters.get("eventid");
    if (eventId != null)
    {
      try
      {
        currentEvent = loadEvent(eventId);

        return "goEvent";
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
        getExternalContext().log(ex.getMessage());
      }
    }

    return null;
  }

  private String getMimeTypeIcon(String mimeType)
  {
    String icon = ICON_PATH + "altre.gif";

    FileTypeMap map = MimeTypeMap.getDefaultFileTypeMap();
    if (map instanceof MimeTypeMap)
    {
      String ext = ((MimeTypeMap)map).getExtension(mimeType);
      String temp = ICON_PATH + ext + ".gif";
      try
      {
        URL url = getExternalContext().getResource(temp);
        if (url != null)
        {
          icon = temp;
        }
      }
      catch (Exception ex)
      {
      }
    }
    return icon;
  }

  private Event loadEvent(String eventId) throws Exception
  {
    return getPort().loadEventFromCache(eventId);
  }

}