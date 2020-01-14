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
package org.santfeliu.agenda.client;

import java.net.URL;
import java.util.List;
import org.matrix.agenda.AgendaManagerPort;
import org.matrix.agenda.AgendaManagerService;
import org.matrix.agenda.Attendant;
import org.matrix.agenda.AttendantFilter;
import org.matrix.agenda.AttendantView;
import org.matrix.agenda.Event;
import org.matrix.agenda.EventDocument;
import org.matrix.agenda.EventDocumentFilter;
import org.matrix.agenda.EventDocumentView;
import org.matrix.agenda.EventFilter;
import org.matrix.agenda.EventPlace;
import org.matrix.agenda.EventPlaceFilter;
import org.matrix.agenda.EventPlaceView;
import org.matrix.agenda.EventTheme;
import org.matrix.agenda.EventThemeFilter;
import org.matrix.agenda.EventThemeView;
import org.matrix.agenda.EventView;
import org.matrix.agenda.Theme;
import org.matrix.agenda.ThemeFilter;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.ws.WSCallCache;

/**
 *
 * @author lopezrj
 */
public class AgendaManagerClient implements AgendaManagerPort
{
  AgendaManagerPort port;
  private static WSCallCache cache;

  public AgendaManagerClient()
  {
    WSDirectory wsDirectory = WSDirectory.getInstance();
    init(wsDirectory, null, null);
  }

  public AgendaManagerClient(URL wsDirectoryURL)
  {
    WSDirectory wsDirectory = WSDirectory.getInstance(wsDirectoryURL);
    init(wsDirectory, null, null);
  }

  public AgendaManagerClient(String userId, String password)
  {
    WSDirectory wsDirectory = WSDirectory.getInstance();
    init(wsDirectory, userId, password);
  }

  public AgendaManagerClient(URL wsDirectoryURL, String userId, String password)
  {
    WSDirectory wsDirectory = WSDirectory.getInstance(wsDirectoryURL);
    init(wsDirectory, userId, password);
  }

  public AgendaManagerClient(AgendaManagerPort port)
  {
    this.port = port;
  }

  public static WSCallCache getCache()
  {
    if (cache == null)
    {
      cache = new WSCallCache("agenda");
    }
    return cache;
  }  

  public AgendaManagerPort getPort()
  {
    return port;
  }

  //NON-CACHED METHODS
  
  @Override
  public Event storeEvent(Event event)
  {    
    Event e = port.storeEvent(event);
    getCache().clear();
    return e;
  }

  @Override
  public Event loadEvent(String eventId)
  {
    return port.loadEvent(eventId);        
  }

  @Override
  public boolean removeEvent(String eventId)
  {    
    boolean r = port.removeEvent(eventId);
    getCache().clear();
    return r;
  }

  @Override
  public int countEvents(EventFilter filter)
  {    
    return port.countEvents(filter);
  }

  @Override
  public List<Event> findEvents(EventFilter filter)
  {
    return port.findEvents(filter);
  }

  @Override
  public List<EventView> findEventViews(EventFilter filter)
  {
    return port.findEventViews(filter);    
  }

  @Override
  public EventDocument storeEventDocument(EventDocument eventDocument)
  {    
    EventDocument ed = port.storeEventDocument(eventDocument);
    getCache().clear();
    return ed;
  }

  @Override
  public EventDocument loadEventDocument(String eventDocumentId)
  {
    return port.loadEventDocument(eventDocumentId);    
  }

  @Override
  public boolean removeEventDocument(String eventDocument)
  {    
    boolean r = port.removeEventDocument(eventDocument);
    getCache().clear();
    return r;
  }

  @Override
  public List<EventDocument> findEventDocuments(EventDocumentFilter filter)
  {
    return port.findEventDocuments(filter);    
  }

  @Override
  public List<EventDocumentView> findEventDocumentViews(EventDocumentFilter filter)
  {
    return port.findEventDocumentViews(filter);
  }

  @Override
  public EventPlace storeEventPlace(EventPlace eventPlace)
  {    
    EventPlace ep = port.storeEventPlace(eventPlace);
    getCache().clear();    
    return ep;
  }

  @Override
  public EventPlace loadEventPlace(String eventPlaceId)
  {
    return port.loadEventPlace(eventPlaceId);        
  }

  @Override
  public boolean removeEventPlace(String eventPlace)
  {    
    boolean r = port.removeEventPlace(eventPlace);
    getCache().clear();
    return r;
  }

  @Override
  public List<EventPlace> findEventPlaces(EventPlaceFilter filter)
  {
    return port.findEventPlaces(filter);    
  }

  @Override
  public List<EventPlaceView> findEventPlaceViews(EventPlaceFilter filter)
  {
    return port.findEventPlaceViews(filter);    
  }

  @Override
  public Attendant storeAttendant(Attendant attendant)
  {
    Attendant a = port.storeAttendant(attendant); 
    getCache().clear();
    return a;
  }

  @Override
  public Attendant loadAttendant(String attendantId)
  {
    return port.loadAttendant(attendantId);        
  }

  @Override
  public boolean removeAttendant(String attendantId)
  {    
    boolean r = port.removeAttendant(attendantId);
    getCache().clear();
    return r;
  }

  @Override
  public List<Attendant> findAttendants(AttendantFilter filter)
  {
    return port.findAttendants(filter);    
  }

  @Override
  public List<AttendantView> findAttendantViews(AttendantFilter filter)
  {
    return port.findAttendantViews(filter);
  }

  @Override
  public List<Attendant> findAttendantsOccupancy(String eventId)
  {
    return port.findAttendantsOccupancy(eventId);    
  }

  @Override
  public Theme storeTheme(Theme theme)
  {        
    Theme t = port.storeTheme(theme); 
    getCache().clear();
    return t;
  }

  @Override
  public Theme loadTheme(String themeId)
  {
    return port.loadTheme(themeId);    
  }

  @Override
  public boolean removeTheme(String themeId)
  {    
    boolean r = port.removeTheme(themeId);
    getCache().clear();
    return r;
  }

  @Override
  public int countThemes(ThemeFilter filter)
  {    
    return port.countThemes(filter);    
  }

  @Override
  public List<Theme> findThemes(ThemeFilter filter)
  {
    return port.findThemes(filter);    
  }

  @Override
  public EventTheme storeEventTheme(EventTheme eventTheme)
  {        
    EventTheme et = port.storeEventTheme(eventTheme);
    getCache().clear();
    return et;
  }

  @Override
  public EventTheme loadEventTheme(String eventThemeId)
  {
    return port.loadEventTheme(eventThemeId);    
  }

  @Override
  public boolean removeEventTheme(String eventThemeId)
  {    
    boolean r = port.removeEventTheme(eventThemeId);    
    getCache().clear();
    return r;
  }

  @Override
  public List<EventTheme> findEventThemes(EventThemeFilter filter)
  {   
    return port.findEventThemes(filter);
  }

  @Override
  public List<EventThemeView> findEventThemeViews(EventThemeFilter filter)
  {    
    return port.findEventThemeViews(filter);
  }

  //CACHED METHODS

  public Event loadEventFromCache(String eventId)
  {
    return (Event)getCache().getCallResult(port, "loadEvent", 
      new Object[]{eventId});
  }

  public int countEventsFromCache(EventFilter filter)
  {    
    return (Integer)getCache().getCallResult(port, "countEvents", 
      new Object[]{filter}, new Object[]{getModifiedFilter(filter)});
  }

  public List<Event> findEventsFromCache(EventFilter filter)
  {
    return (List<Event>)getCache().getCallResult(port, "findEvents", 
      new Object[]{filter}, new Object[]{getModifiedFilter(filter)});
  }

  public List<EventView> findEventViewsFromCache(EventFilter filter)
  {
    return (List<EventView>)getCache().getCallResult(port, "findEventViews", 
      new Object[]{filter}, new Object[]{getModifiedFilter(filter)});
  }

  public EventDocument loadEventDocumentFromCache(String eventDocumentId)
  {
    return (EventDocument)getCache().getCallResult(port, "loadEventDocument", 
      new Object[]{eventDocumentId});
  }

  public List<EventDocument> findEventDocumentsFromCache(EventDocumentFilter filter)
  {
    return (List<EventDocument>)getCache().getCallResult(port, "findEventDocuments", 
      new Object[]{filter});    
  }

  public List<EventDocumentView> findEventDocumentViewsFromCache(EventDocumentFilter filter)
  {
    return (List<EventDocumentView>)getCache().getCallResult(port, "findEventDocumentViews", 
      new Object[]{filter});    
  }

  public EventPlace loadEventPlaceFromCache(String eventPlaceId)
  {
    return (EventPlace)getCache().getCallResult(port, "loadEventPlace", 
      new Object[]{eventPlaceId});    
  }

  public List<EventPlace> findEventPlacesFromCache(EventPlaceFilter filter)
  {    
    return (List<EventPlace>)getCache().getCallResult(port, "findEventPlaces", 
      new Object[]{filter});    
  }

  public List<EventPlaceView> findEventPlaceViewsFromCache(EventPlaceFilter filter)
  {    
    return (List<EventPlaceView>)getCache().getCallResult(port, "findEventPlaceViews", 
      new Object[]{filter});    
  }

  public Attendant loadAttendantFromCache(String attendantId)
  {
    return (Attendant)getCache().getCallResult(port, "loadAttendant", 
      new Object[]{attendantId});    
  }

  public List<Attendant> findAttendantsFromCache(AttendantFilter filter)
  {
    return (List<Attendant>)getCache().getCallResult(port, "findAttendants", 
      new Object[]{filter});
  }

  public List<AttendantView> findAttendantViewsFromCache(AttendantFilter filter)
  {
    return (List<AttendantView>)getCache().getCallResult(port, "findAttendantViews", 
      new Object[]{filter});
  }

  public List<Attendant> findAttendantsOccupancyFromCache(String eventId)
  {
    return (List<Attendant>)getCache().getCallResult(port, "findAttendantsOccupancy", 
      new Object[]{eventId});    
  }

  public Theme loadThemeFromCache(String themeId)
  {
    return (Theme)getCache().getCallResult(port, "loadTheme", 
      new Object[]{themeId});
  }

  public int countThemesFromCache(ThemeFilter filter)
  {    
    return (Integer)getCache().getCallResult(port, "countThemes", 
      new Object[]{filter});    
  }

  public List<Theme> findThemesFromCache(ThemeFilter filter)
  {
    return (List<Theme>)getCache().getCallResult(port, "findThemes", 
      new Object[]{filter});
  }

  public EventTheme loadEventThemeFromCache(String eventThemeId)
  {
    return (EventTheme)getCache().getCallResult(port, "loadEventTheme", 
      new Object[]{eventThemeId});
  }
  
  public List<EventTheme> findEventThemesFromCache(EventThemeFilter filter)
  {   
    return (List<EventTheme>)getCache().getCallResult(port, "findEventThemes", 
      new Object[]{filter});    
  }

  public List<EventThemeView> findEventThemeViewsFromCache(EventThemeFilter filter)
  {    
    return (List<EventThemeView>)getCache().getCallResult(port, "findEventThemeViews", 
      new Object[]{filter});    
  }

  //PRIVATE METHODS

  private void init(WSDirectory wsDirectory, String userId, String password)
  {
    WSEndpoint endpoint = wsDirectory.getEndpoint(AgendaManagerService.class);
    port = endpoint.getPort(AgendaManagerPort.class, userId, password);
  }
  
  private EventFilter getModifiedFilter(EventFilter filter)
  {
    EventFilter auxFilter = new EventFilter();
    auxFilter.getEventId().addAll(filter.getEventId());
    auxFilter.setContent(filter.getContent());
    auxFilter.setPersonId(filter.getPersonId());
    auxFilter.setRoomId(filter.getRoomId());
    auxFilter.setStartDateTime(filter.getStartDateTime() == null ? null : 
      filter.getStartDateTime().substring(0, 12));
    auxFilter.setEndDateTime(filter.getEndDateTime() == null ? null : 
      filter.getEndDateTime().substring(0, 12));
    auxFilter.getEventTypeId().addAll(filter.getEventTypeId());
    auxFilter.getThemeId().addAll(filter.getThemeId());
    auxFilter.setStartChangeDateTime(filter.getStartChangeDateTime() == null ? null : 
      filter.getStartChangeDateTime().substring(0, 12));
    auxFilter.setEndChangeDateTime(filter.getEndChangeDateTime() == null ? null : 
      filter.getEndChangeDateTime().substring(0, 12));
    auxFilter.getProperty().addAll(filter.getProperty());
    auxFilter.getOrderBy().addAll(filter.getOrderBy());
    auxFilter.setSecurityMode(filter.getSecurityMode());
    auxFilter.setIncludeMetadata(filter.isIncludeMetadata());
    auxFilter.setReducedInfo(filter.isReducedInfo());
    auxFilter.setFirstResult(filter.getFirstResult());
    auxFilter.setMaxResults(filter.getMaxResults());    
    auxFilter.setDateComparator(filter.getDateComparator());
    return auxFilter;
  }
  
}
