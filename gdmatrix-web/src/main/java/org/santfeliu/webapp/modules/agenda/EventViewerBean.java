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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.inject.Named;
import org.matrix.agenda.Event;
import org.matrix.agenda.EventDocumentFilter;
import org.matrix.agenda.EventDocumentView;
import org.matrix.agenda.EventPlaceFilter;
import org.matrix.agenda.EventPlaceView;
import org.matrix.doc.Document;
import org.santfeliu.agenda.Place;
import org.santfeliu.agenda.client.AgendaManagerClient;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.doc.web.DocumentUrlBuilder;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import static org.santfeliu.webapp.modules.agenda.AgendaModuleBean.DETAILS_IMAGE_TYPE;

/**
 *
 * @author blanquepa
 */
@CMSManagedBean
@Named
@RequestScoped
public class EventViewerBean extends WebBean implements Serializable
{  
  public static final String EVENTID_PARAMETER = "eventid";

  private static final String OUTCOME = "/pages/agenda/event_viewer.xhtml";

  Event event;
  String imageContentId;
  List<Place> places;
  List<Document> documents;

  public Event getEvent()
  {
    return event;
  }

  public void setEvent(Event event)
  {
    this.event = event;
  }

  public String getImageContentId()
  {
    return imageContentId;
  }

  public void setImageContentId(String imageContentId)
  {
    this.imageContentId = imageContentId;
  }

  public List<Place> getPlaces()
  {
    return places;
  }

  public void setPlaces(List<Place> places)
  {
    this.places = places;
  }

  public List<Document> getDocuments()
  {
    return documents;
  }

  public void setDocuments(List<Document> documents)
  {
    this.documents = documents;
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

  public String getContent()
  {
    return OUTCOME;
  }

  public String getStartDate() throws Exception
  {
    return getUserDate(event.getStartDateTime());
  }

  public String getEndDate() throws Exception
  {
    return getUserDate(event.getEndDateTime());
  }  
  
  private String getUserDate(String sysDate)
  {
    try
    {
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
      SimpleDateFormat dateHumanFormat = 
        new SimpleDateFormat("EEE dd MMMM yyyy, HH.mm 'h'");

      if ((sysDate == null) || (sysDate.length() == 0))
        return "";
      else
        return dateHumanFormat.format(dateFormat.parse(sysDate));
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;    
  }  
  
  public String getDates()
  { 
    String result = "";
      
    try
    {
      if (event.getStartDateTime() != null)
      {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat dayFormat =  new SimpleDateFormat("yyyyMMdd");

        Date startDate = dateTimeFormat.parse(event.getStartDateTime());
        Date endDate = dateTimeFormat.parse(event.getEndDateTime());

        String startDay = dayFormat.format(startDate);
        String endDay = dayFormat.format(endDate);
      
        Locale locale = getFacesContext().getViewRoot().getLocale();      
        SimpleDateFormat dateHumanFormat = 
          new SimpleDateFormat("EEE dd MMMM yyyy, HH.mm 'h'", locale);  

        result = dateHumanFormat.format(startDate);
        if (endDay != null && !endDay.equals(startDay))
        {
          result = result + " - " + dateHumanFormat.format(endDate);
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }

    return result;
  }  
  
  public String getDocumentURL(Document document)
  {
    return DocumentUrlBuilder.getDocumentUrl(document);    
  }

  // action methods
  @CMSAction
  public String show()
  {
    try
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      ExternalContext externalContext = getExternalContext();
      Map<String, String> parameterMap = externalContext.getRequestParameterMap();
      String eventId = parameterMap.get(EVENTID_PARAMETER);
      if (eventId != null)
      {
        AgendaManagerClient client = AgendaModuleBean.getClient(
          userSessionBean.getUserId(), userSessionBean.getPassword());
        event = client.loadEventFromCache(eventId);

        //Documents
        EventDocumentFilter filter = new EventDocumentFilter();
        filter.setEventId(eventId);
        List<EventDocumentView> eventDocuments = 
          client.findEventDocumentViewsFromCache(filter);

        this.imageContentId = null;
        for (EventDocumentView eventDocument : eventDocuments)
        {
          String typeId = eventDocument.getEventDocTypeId();          
          if (DETAILS_IMAGE_TYPE.equals(typeId))
          {
            this.imageContentId = 
              eventDocument.getDocument().getContent().getContentId();
          }
          else
          {
            if (documents == null)
              documents = new ArrayList();
            documents.add(eventDocument.getDocument());
          }
        }
        
        //Places
        EventPlaceFilter eventPlaceFilter = new EventPlaceFilter();
        eventPlaceFilter.setEventId(eventId);
        List<EventPlaceView> eventPlaceViews =
          client.findEventPlaceViewsFromCache(eventPlaceFilter);
        if (eventPlaceViews != null)
        {
          places = new ArrayList();
          for (EventPlaceView eventPlaceView : eventPlaceViews)
          {
            Place place = new Place(eventPlaceView);
            places.add(place);
          }
        }        
      }
      else
      {
        event = null;
      }
      String template = userSessionBean.getTemplate();
      return "/templates/" + template + "/template.xhtml";
    }
    catch (Exception ex)
    {
      error(ex);
      return null;
    }
  }
}
