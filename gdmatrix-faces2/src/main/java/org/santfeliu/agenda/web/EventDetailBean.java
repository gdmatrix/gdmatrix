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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import javax.faces.context.ExternalContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.matrix.agenda.AttendantFilter;
import org.matrix.agenda.AttendantView;
import org.matrix.agenda.Event;
import org.matrix.agenda.EventDocumentFilter;
import org.matrix.agenda.EventDocumentView;
import org.matrix.agenda.EventFilter;
import org.matrix.agenda.EventPlaceFilter;
import org.matrix.agenda.EventPlaceView;
import org.matrix.dic.Property;
import org.matrix.dic.PropertyDefinition;
import org.matrix.doc.Document;
import org.matrix.kernel.Address;
import org.matrix.kernel.AddressView;
import org.santfeliu.agenda.Place;
import org.santfeliu.agenda.client.AgendaManagerClient;
import org.santfeliu.agenda.web.view.EventSearchView;
import org.santfeliu.agenda.web.view.ScheduleEventSearchView;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.PropertyConverter;
import org.santfeliu.doc.web.DocumentBean;
import org.santfeliu.doc.web.DocumentUrlBuilder;
import org.santfeliu.faces.beansaver.Savable;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.form.Form;
import org.santfeliu.form.FormDescriptor;
import org.santfeliu.form.FormFactory;
import org.santfeliu.form.builder.TypeFormBuilder;
import org.santfeliu.kernel.web.KernelConfigBean;
import org.santfeliu.util.TextUtils;
import org.santfeliu.util.script.WebScriptableBase;
import org.santfeliu.util.template.Template;
import org.santfeliu.util.template.WebTemplate;
import org.santfeliu.web.HttpUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.ShareableWebBean;

/**
 *
 * @author blanquepa
 */
public class EventDetailBean extends ShareableWebBean implements Savable
{
  private static final String DETAILS_ATTENDANTS_RENDER =
    "details.attendants.render";
  private static final String DETAILS_FORM_SELECTORS =
    "details.formSelectors";
  private static final String DETAILS_IMAGE_RENDER =
    "details.image.render";
  private static final String DETAILS_IMAGE_HEIGHT_PROPERTY =
    "details.image.height";
  private static final String DETAILS_IMAGE_WIDTH_PROPERTY =
    "details.image.width";
  private static final String DETAILS_DATE_FORMAT =
    "details.dateFormat";

  private static final String LOCAL_MAP_URL = "localMapURL";
  private static final String EXTERNAL_MAP_URL = "externalMapURL";
  private static final String LOCAL_CITY_NAME = "localCityName";

  private static final String ICON_PATH = "/common/doc/images/extensions/";

  private String eventId;

  private Event event;
  private List<Place> places;
  private List<EventDocumentView> eventDocuments;
  private List<AttendantView> attendants;

  //FormBuilder
  private Map data;
  private String selector;
  private List<SelectItem> formSelectItems;
  private boolean resetSelector = true;

  public EventDetailBean()
  {
  }

  public EventDetailBean(String eventId)
  {
    this.eventId = eventId;
  }

  public String show()
  {
    try
    {
      AgendaManagerClient port = AgendaConfigBean.getPort();
      event = port.loadEventFromCache(eventId);
      resetSelector();

      EventPlaceFilter eventPlaceFilter = new EventPlaceFilter();
      eventPlaceFilter.setEventId(eventId);
      List<EventPlaceView> eventPlaceViews = port.findEventPlaceViewsFromCache(eventPlaceFilter);
      if (eventPlaceViews != null)
      {
        places = new ArrayList();
        for (EventPlaceView eventPlaceView : eventPlaceViews)
        {
          Place place = new Place(eventPlaceView);
          places.add(place);
        }
      }

      if (isRenderAttendants())
      {
        AttendantFilter attendantFilter = new AttendantFilter();
        attendantFilter.setEventId(eventId);
        attendants = port.findAttendantViewsFromCache(attendantFilter);
        if (attendants != null)
        {
          for (AttendantView attendant : attendants)
          {
            String typeId = attendant.getAttendantTypeId();
            if (typeId != null)
            {
              Type type = TypeCache.getInstance().getType(typeId);
              if (type != null)
                attendant.setAttendantTypeId(type.getDescription());
            }
          }
        }
      }

      EventDocumentFilter eventDocumentFilter = new EventDocumentFilter();
      eventDocumentFilter.setEventId(eventId);
      eventDocuments = port.findEventDocumentViewsFromCache(eventDocumentFilter);

      setFormDataFromProperties(event.getProperty());
      data.put("_objectId", eventId);
    }
    catch (Exception ex)
    {
      error("EVENT_LOAD_ERROR");
      return "event_search";
    }
    return "event_detail";
  }

  public String editEvent()
  {
    EventSearchBean eventSearchBean =
      (EventSearchBean) getBean("eventSearchBean");
    if (eventSearchBean != null)
    {
      return eventSearchBean.showEvent(eventId);
    }
    return null;
  }

  public String close()
  {
    return "event_search";
  }

  public Event getEvent()
  {
    return event;
  }

  public List<Property> getEventProperties()
  {
    List<Property> result = new ArrayList();

    Type type = TypeCache.getInstance().getType(event.getEventTypeId());
    if (type != null)
    {
      for (Property prop : event.getProperty())
      {
        PropertyDefinition pd = type.getPropertyDefinition(prop.getName());
        if (pd != null && !pd.isHidden())
        {
          prop.setName(pd.getDescription());
          result.add(prop);
        }
      }
    }
    else
      result.addAll(event.getProperty());

    return result;
  }

  public Form getForm()
  {
    try
    {
      if (selector != null)
      {
        FormFactory formFactory = FormFactory.getInstance();
        // update form only in render phase
        boolean updated = getFacesContext().getRenderResponse();
        return formFactory.getForm(selector, getData(), updated);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public List<SelectItem> getFormSelectItems()
  {
    if (formSelectItems == null)
    {
      findForms();
    }
    return formSelectItems;
  }

  public boolean isRenderFormSelectItems()
  {
    return getFormSelectItems().size() > 1;
  }

  public Map getData()
  {
    return data;
  }

  public void setData(Map data)
  {
    this.data = data;
  }

  public String getSelector()
  {
    if (getFacesContext().getRenderResponse() && resetSelector)
    {
      // selector reset don't takes place until render phase
      getFormSelectItems();
      // take first value of formSelectItems
      if (!formSelectItems.isEmpty())
      {
        selector = (String)formSelectItems.get(0).getValue();
      }
      else
      {
        selector = null;
      }
      resetSelector = false;
    }
    return selector;
  }

  public void setSelector(String selector)
  {
    this.selector = selector;
  }

  public void resetSelector()
  {
    if (isTypeUndefined())
    {
      formSelectItems = null;
      resetSelector = true;
    }
  }

  public boolean isTypeUndefined()
  {
    return event == null || event.getEventTypeId() == null || event.getEventTypeId().length() == 0;
  }

  public String updateForm()
  {
    if (selector != null)
    {
      FormFactory factory = FormFactory.getInstance();
      factory.clearForm(selector);
    }
    return show();
  }



  public void setEvent(Event event)
  {
    this.event = event;
  }

  public List<Place> getPlaces()
  {
    return places;
  }

  public void setPlaces(List<Place> places)
  {
    this.places = places;
  }

  public List<EventDocumentView> getEventDocuments()
  {
    return eventDocuments;
  }

  public List<AttendantView> getAttendants()
  {
    return attendants;
  }

  public void setAttendants(List<AttendantView> attendants)
  {
    this.attendants = attendants;
  }

  public void setEventDocuments(List<EventDocumentView> eventDocuments)
  {
    this.eventDocuments = eventDocuments;
  }

  public String getEventType()
  {
    String eventType = null;
    if (event != null && event.getEventTypeId() != null)
    {
      Type type = TypeCache.getInstance().getType(event.getEventTypeId());
      if (type != null)
        eventType = type.getDescription();
      else
        eventType = event.getEventTypeId();
    }

    return eventType;
  }

  public Date getStartDateTime()
  {
    if (event != null)
    {
      String startDateTime = event.getStartDateTime();
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
    if (event != null)
    {
      String endDateTime = event.getEndDateTime();
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

  public String getWhen()
  {
    String result = "";
    Date startDT = getStartDateTime();
    Date endDT = getEndDateTime();

    Locale locale = getFacesContext().getViewRoot().getLocale();

    if (startDT != null)
    {
      SimpleDateFormat df = new SimpleDateFormat(getDateFormat(), locale);
      result = df.format(startDT);
      if (endDT != null && !isSameDay(startDT, endDT))
      {
        result = result + " - " + df.format(endDT);
      }
    }

    return result;
  }

  private boolean isSameDay(Date d1, Date d2)
  {
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
    return d1 != null && d2 != null && df.format(d1).equals(df.format(d2));
  }

  public String getDetailImageDocId()
  {
    String docId = null;
    if (eventDocuments != null)
    {
      for (EventDocumentView docView : eventDocuments)
      {
        if (AgendaConfigBean.DETAILS_IMAGE_TYPE.equals(docView.getEventDocTypeId()) ||
            AgendaConfigBean.LIST_AND_DETAILS_IMAGE_TYPE.equals(docView.getEventDocTypeId()))
        {
          docId = docView.getDocument().getDocId();
        }
      }
    }
    return docId;
  }

  public String getDetailImageHeight()
  {
    String height = getProperty(DETAILS_IMAGE_HEIGHT_PROPERTY);
    return (height != null ? height : "100px");
  }

  public String getDetailImageWidth()
  {
    String width = getProperty(DETAILS_IMAGE_WIDTH_PROPERTY);
    return (width != null ? width : "100px");
  }

  public boolean isRenderDetailImage()
  {
    String render = getProperty(DETAILS_IMAGE_RENDER);
    return (render == null || render.equalsIgnoreCase("true"));
  }

  public String getDocumentUrl()
  {
    String url = "";

    Document doc = (Document)getValue("#{doc}");
    if (doc != null)
      url = DocumentUrlBuilder.getDocumentUrl(doc);

    return url;
  }

  public String getMimeTypePath()
  {
    Document doc = (Document)getValue("#{doc}");
    return DocumentBean.getContentTypeIcon(getContextPath() + ICON_PATH,
      doc.getContent().getContentType());
  }

  public List<Document> getExtendedInfoDocs()
  {
    List<Document> result = new ArrayList();
    if (eventDocuments != null)
    {
      for (EventDocumentView docView : eventDocuments)
      {
        if (AgendaConfigBean.EXTENDED_INFO_TYPE.equals(docView.getEventDocTypeId()))
        {
          result.add(docView.getDocument());
        }
      }
    }
    return result;
  }

  public boolean isRenderAttendants()
  {
    MenuItemCursor menuItem =
      UserSessionBean.getCurrentInstance().getSelectedMenuItem();
    String value = menuItem.getProperty(DETAILS_ATTENDANTS_RENDER);
    return (value != null ? value.equals("true") : false);
  }

  public String getMapURL()
  {
    String mapURL = null;
    MenuItemCursor menuItem =
      UserSessionBean.getCurrentInstance().getSelectedMenuItem();
    Place place = (Place)getValue("#{place}");
    if (place != null && (place.isRoom() || place.isAddress()))
    {
      AddressView addressView = place.getAddressView();
      if (addressView != null)
      {
        String localCityName = menuItem.getProperty(LOCAL_CITY_NAME);
        String city = addressView.getCity();
        if (city != null && city.equalsIgnoreCase(localCityName))
        {
          //local
          mapURL = menuItem.getProperty(LOCAL_MAP_URL);
          if (mapURL != null)
          {
            String addressId = addressView.getAddressId();
            Properties properties = new Properties();
            properties.setProperty("addressId", addressId);

            if (mapURL.indexOf("reference") > 0)
            {
              Address address =
                KernelConfigBean.getPort().loadAddress(addressId);
              String gisReference = address.getGisReference();
              String streetName = "";
              String streetNumber = "";
              if (gisReference == null)
              {
                streetName = 
                  getStreetName(addressView.getDescription(), address.getNumber1());
                streetNumber = 
                  address.getNumber1() != null ? address.getNumber1() : "";
                gisReference = "";
              }
              properties.setProperty("reference", gisReference);
              properties.setProperty("streetName", streetName);
              properties.setProperty("streetNumber", streetNumber);
              
            }
            mapURL = WebTemplate.create(mapURL).merge(properties);
          }
        }
        if (city != null && !city.equalsIgnoreCase(localCityName))
        {
          //external
          mapURL = menuItem.getProperty(EXTERNAL_MAP_URL);
          if (mapURL != null)
          {
            String description = addressView.getDescription() + "," +
              addressView.getCity() + "," +
              addressView.getCountry();
            Properties properties = new Properties();
            properties.setProperty("description", description);
            mapURL = WebTemplate.create(mapURL).merge(properties);
          }
        }
      }
    }
    else if (place.getDescription() != null)
    {
      mapURL = menuItem.getProperty(EXTERNAL_MAP_URL);
      Properties properties = new Properties();
      properties.setProperty("description", place.getDescription());
      mapURL = WebTemplate.create(mapURL).merge(properties);
    }
    
    return mapURL;
  }

  public String getDateTimeParameters()
  {
    StringBuilder sb = new StringBuilder();
    sb.append(getBaseURL());
    sb.append("&startdatetime=");
    sb.append(event.getStartDateTime());
    sb.append(getEventFilterParameters("datetime"));
    return sb.toString();
  }

  public String getTypeIdParameters()
  {
    StringBuilder sb = new StringBuilder();
    sb.append(getBaseURL());
    sb.append("&eventtypeid=");
    sb.append(event.getEventTypeId());
    sb.append(getEventFilterParameters("eventtypeid"));
    return sb.toString();
  }

  public String getRoomParameters()
  {
    StringBuilder sb = new StringBuilder();
    sb.append(getBaseURL());
    sb.append("&roomid=");
    if (places != null && !places.isEmpty())
      sb.append(places.get(0).getPlaceId());
    sb.append(getEventFilterParameters("roomid"));
    return sb.toString();
  }

  public String getBaseURL()
  {
    return "go.faces?xmid=" + UserSessionBean.getCurrentInstance().getSelectedMenuItem().getMid();
  }

  public String getEventFilterParameters(String main)
  {
    EventSearchBean eventSearchBean = (EventSearchBean)getBean("eventSearchBean");
    EventFilter eventFilter = eventSearchBean.getEventFilter();
    StringBuilder sb = new StringBuilder();
    EventSearchView eventViewBean = eventSearchBean.getEventViewBean();
    if (!"datetime".equals(main) && eventViewBean instanceof ScheduleEventSearchView)
    {
      ScheduleEventSearchView searchView = (ScheduleEventSearchView)eventViewBean;
      Date selectedDate = searchView.getSelectedDate();
      String startDateTime = TextUtils.formatDate(selectedDate, "yyyyMMdd000000");
      sb.append("&startdatetime=").append(startDateTime);
    }
    else
    {
      if (!StringUtils.isBlank(eventFilter.getStartDateTime()) && !"datetime".equals(main))
        sb.append("&startdatetime=").append(eventFilter.getStartDateTime());
      if (!StringUtils.isBlank(eventFilter.getEndDateTime()) && !"datetime".equals(main))
        sb.append("&enddatetime=").append(eventFilter.getEndDateTime());
    }
    if (!StringUtils.isBlank(eventSearchBean.getPropertiesFilter().getCurrentTypeId()) && !"eventtypeid".equals(main))
      sb.append("&eventtypeid=").append(eventSearchBean.getPropertiesFilter().getCurrentTypeId());
    if (!StringUtils.isBlank(eventFilter.getRoomId()) && !"roomid".equals(main))
      sb.append("&roomid=").append(eventFilter.getRoomId());
    if (!StringUtils.isBlank(eventFilter.getContent()))
      sb.append("&content=").append(eventFilter.getContent());
    if (!StringUtils.isBlank(eventFilter.getPersonId()))
      sb.append("&personid=").append(eventFilter.getPersonId());
    if (!StringUtils.isBlank(eventSearchBean.getThemeId()))
      sb.append("&themeid=").append(eventSearchBean.getThemeId());
    sb.append("&oc=");
    sb.append(eventSearchBean.getOnlyCurrentDate());

    return sb.toString();
  }

  @Override
  protected List<String> getShareURLList()
  {
    List<String> result = new ArrayList<String>();
    if (event != null)
    {
      MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
        getMenuModel().getSelectedMenuItem();    
      Map<String, String> propertyMap = new HashMap();
      propertyMap.put("xmid", menuItem.getMid());
      propertyMap.put("eventid", event.getEventId());
      String language =
        getFacesContext().getViewRoot().getLocale().getLanguage();
      propertyMap.put("language", language);
      String summary = translatePlainText(event.getSummary(),
        event.getEventId(), language);
      propertyMap.put("info", summary);    
      propertyMap.put("idparam", "eventid");
      propertyMap.put("idvalue", event.getEventId());
      result = getShareURLList(propertyMap);
    }
    return result;
  }
  
  @Override
  protected String getEmailDefaultSubject()
  {    
    return event.getSummary() != null ? event.getSummary() : "";
  }

  @Override
  protected String getEmailDefaultBody()
  {
    return getEventURL();    
  }  
  
  private String getEventURL()
  {
    ExternalContext extContext = getFacesContext().getExternalContext();
    HttpServletRequest request = (HttpServletRequest)extContext.getRequest();
    String serverName = HttpUtils.getServerName(request);    
    String serverURL = "http://" + serverName;    
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String language = getFacesContext().getViewRoot().getLocale().getLanguage();
    return serverURL + "/go.faces?xmid=" + menuItem.getMid() +
      "&eventid=" + event.getEventId() + "&language=" + language;    
  }  
  
  private void findForms()
  {
    try
    {
      formSelectItems = new ArrayList();
      if (!isTypeUndefined())
      {
        UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
        MenuItemCursor menuItem = userSessionBean.getSelectedMenuItem();
        List<String> formSelectors =
          menuItem.getMultiValuedProperty(DETAILS_FORM_SELECTORS);

        String selectorBase =
          TypeFormBuilder.PREFIX + ":" + event.getEventTypeId() +
          TypeFormBuilder.USERID + userSessionBean.getUserId() +
          TypeFormBuilder.PASSWORD + userSessionBean.getPassword();
        FormFactory formFactory = FormFactory.getInstance();

        List<FormDescriptor> descriptors = formFactory.findForms(selectorBase);
        for (FormDescriptor descriptor : descriptors)
        {
          if (formSelectors != null &&
            matches(formSelectors, descriptor.getSelector()))
          {
            SelectItem selectItem = new SelectItem();
            selectItem.setValue(descriptor.getSelector());
            selectItem.setDescription(descriptor.getTitle());
            selectItem.setLabel(descriptor.getTitle());
            formSelectItems.add(selectItem);
          }
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private boolean matches(List<String> formSelectors, String selector)
  {
    if (selector != null && formSelectors != null && !formSelectors.isEmpty())
    {
      for (String formSelector : formSelectors)
      {
        if (selector.matches(formSelector))
          return true;
      }
    }
    return false;
  }

  private void setFormDataFromProperties(List<Property> properties)
  {
    if (properties != null)
    {
      Type type = TypeCache.getInstance().getType(event.getEventTypeId());
      if (type != null)
      {
        PropertyConverter converter = new PropertyConverter(type);
        data = converter.toPropertyMap(properties);
      }
      else data = new HashMap();
    }
  }

  private String translatePlainText(String text, String eventId, String language)
  {
    return translatePlainText(text, eventId, language, "event");
  }

  private String getDateFormat()
  {
    String dateFormat = getProperty(DETAILS_DATE_FORMAT);
    if (dateFormat == null)
      dateFormat = "EEE dd MMMM yyyy, HH.mm 'h'";
    return dateFormat;
  }
  
  private String getStreetName(String description, String number)
  {
    if (description == null)
      return "";
    if (number == null)
      return description;
    
    return description.substring(description.indexOf(number));
  }
}




