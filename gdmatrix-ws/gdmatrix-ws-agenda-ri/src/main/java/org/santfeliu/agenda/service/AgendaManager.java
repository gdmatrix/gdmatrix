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
package org.santfeliu.agenda.service;


import org.santfeliu.util.HTMLNormalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;

import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import org.apache.commons.lang.StringUtils;

import org.matrix.agenda.AgendaConstants;
import org.matrix.agenda.Attendant;
import org.matrix.agenda.AttendantFilter;
import org.matrix.agenda.AttendantView;
import org.matrix.agenda.Event;
import org.matrix.agenda.EventPlace;
import org.matrix.agenda.EventPlaceFilter;
import org.matrix.agenda.EventDocument;
import org.matrix.agenda.EventDocumentFilter;
import org.matrix.agenda.EventDocumentView;
import org.matrix.agenda.EventPlaceView;
import org.matrix.agenda.EventTheme;
import org.matrix.agenda.EventThemeView;
import org.matrix.agenda.EventThemeFilter;
import org.matrix.agenda.EventFilter;
import org.matrix.agenda.EventView;
import org.matrix.agenda.SecurityMode;
import org.matrix.agenda.Theme;
import org.matrix.agenda.ThemeFilter;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.Property;
import org.matrix.dic.PropertyDefinition;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.DocumentManagerPort;
import org.matrix.doc.DocumentManagerService;
import org.matrix.kernel.Address;
import org.matrix.kernel.AddressFilter;
import org.matrix.kernel.AddressView;
import org.matrix.kernel.KernelManagerPort;
import org.matrix.kernel.KernelManagerService;
import org.matrix.kernel.PersonFilter;
import org.matrix.kernel.PersonPersonFilter;
import org.matrix.kernel.PersonPersonView;
import org.matrix.kernel.PersonView;
import org.matrix.kernel.RoomFilter;
import org.matrix.kernel.RoomView;
import org.matrix.kernel.Street;
import org.matrix.security.SecurityConstants;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;


import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.dic.util.WSTypeValidator;
import org.santfeliu.jpa.JPAQuery;
import org.santfeliu.jpa.JPAUtils;
import org.santfeliu.kernel.service.DBEntityBase;
import org.santfeliu.security.User;
import org.santfeliu.security.UserCache;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.security.util.SecurityUtils;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.TextUtils;
import org.santfeliu.util.audit.Auditor;
import org.santfeliu.ws.WSExceptionFactory;
import org.santfeliu.ws.WSUtils;
import org.santfeliu.ws.annotations.Initializer;
import org.santfeliu.ws.annotations.MultiInstance;


/**
 *
 * @author blanquepa
 */
@WebService(endpointInterface = "org.matrix.agenda.AgendaManagerPort")
@HandlerChain(file="handlers.xml")
@MultiInstance
public class AgendaManager implements org.matrix.agenda.AgendaManagerPort
{
  @Resource
  WebServiceContext wsContext;
  
  @PersistenceContext(unitName="agenda_ri")
  public EntityManager entityManager;
  
  protected static final Logger LOGGER = Logger.getLogger("Agenda");
  
  static final String PK_SEPARATOR = ";";
  private static final String THEMES_SEPARATOR = ",";
  private static final String CONFIDENT_TYPEID = "ConfidentPersonPerson";

  public static final String HIDDEN_EVENT_STRING = "???";
  public static final String DELETED_EVENT_DATETIME = "00010101000000";

  public static final HashSet<String> INCREMENTAL_PROPERTIES = new HashSet();
  static
  {
    INCREMENTAL_PROPERTIES.add("exchangeId");
    INCREMENTAL_PROPERTIES.add("masterEventId");
  }

  public AgendaManager()
  {
  }
  
  @Initializer
  public void initialize(String endpointName)
  {
    initEventIdCounter();
  }    
  
  @Override
  public Event loadEvent(String eventId)
  {
    Event event = null;
    try
    {
      if (eventId == null)
        throw new WebServiceException("agenda:ID_NULL");

      Credentials credentials = SecurityUtils.getCredentials(wsContext);
      User user = UserCache.getUser(credentials);

      eventId = getEndpoint().toLocalId(Event.class, eventId);

      EventFilter eventFilter = new EventFilter();
      eventFilter.getEventId().add(eventId);
      event = loadEvent(eventFilter, user);
      if (event != null)
      {
        if (isEventHidden(event))
          throw new WebServiceException("agenda:ACTION_DENIED");
      }
      else
        throw new WebServiceException("agenda:EVENT_NOT_FOUND");

      //load event Properties
      Query query = entityManager.createNamedQuery("findProperties");
      query.setParameter("eventId", eventId);
      List<DBEventProperty> dbProperties = query.getResultList();
      for(DBEventProperty dbProperty : dbProperties)
      {
        addPropertyToEvent(dbProperty, event);
      }
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "loadEvent failed");
        throw WSExceptionFactory.create(ex);
    }

    return getEndpoint().toGlobal(Event.class, event);
  }

  @Override
  public Event storeEvent(Event event)
  {
    event = getEndpoint().toLocal(Event.class, event);

    User user = UserCache.getUser(wsContext);

    String eventTypeId = event.getEventTypeId();
    eventTypeId =
      getEndpoint().toGlobalId(org.matrix.dic.Type.class, eventTypeId);
    Type eventType = TypeCache.getInstance().getType(eventTypeId);
    
    Event aux = new Event();
    JPAUtils.copy(event, aux);
    validateEvent(aux, eventType); //Validate an auxiliar event to avoid blank properties transformed to null.

    event.setDetail(HTMLNormalizer.normalize(event.getDetail()));
    
    DBEvent dbEvent = new DBEvent(event);
    String eventId = event.getEventId();
    if (eventId == null) //Create
    {
      if (!canUserCreateEvent(user, event))
        throw new WebServiceException("agenda:ACTION_DENIED");

      PropertyDefinition pd = eventType.getPropertyDefinition("tipesdevcod");
      if (pd != null)
      {
        String tipesdevcod = pd.getValue().get(0);
        if (tipesdevcod != null)        
          dbEvent.setTipesdevcod(tipesdevcod);
      }

      Auditor.auditCreation(dbEvent, user.getUserId());

      entityManager.persist(dbEvent);
      event.setEventId(dbEvent.getEventId());

      //Store properties
      storeEventProperties(event, null);

      //Store themes defined in eventType
      PropertyDefinition themeId = eventType.getPropertyDefinition("themeId");
      if (themeId != null)
      {
        String themeIdValue = themeId.getValue().get(0);
        if (themeIdValue != null)
        {
          String[] themes = themeIdValue.split(THEMES_SEPARATOR);
          for (int i = 0; i < themes.length; i++)
          {
            EventTheme eventTheme = new EventTheme();
            eventTheme.setEventId(dbEvent.getEventId());
            eventTheme.setThemeId(themes[i]);
            storeEventTheme(eventTheme);
          }
        }
      }
    }
    else //Update
    {
      DBEvent curDbEvent = entityManager.find(DBEvent.class, eventId);      
      
      if (isRescheduled(curDbEvent, dbEvent))
        checkRoomAvailability(dbEvent);

      if (!canUserModifyEvent(user, curDbEvent))
        throw new WebServiceException("agenda:ACTION_DENIED");

      dbEvent.merge(curDbEvent);
      PropertyDefinition pd = eventType.getPropertyDefinition("tipesdevcod");
      String tipesdevcod = pd.getValue().get(0);
      dbEvent.setTipesdevcod(tipesdevcod);

      Auditor.auditChange(dbEvent, user.getUserId());

      dbEvent = entityManager.merge(dbEvent);
      dbEvent.getProperty().addAll(event.getProperty());

      //Store properties
      Query query = entityManager.createNamedQuery("findProperties");
      query.setParameter("eventId", eventId);
      List<DBEventProperty> curDbProperties = query.getResultList();
      storeEventProperties(dbEvent, curDbProperties);
    }

    dbEvent.copyTo(event);
    return getEndpoint().toGlobal(Event.class, event);
  }

  @Override
  public boolean removeEvent(String eventId)
  {
    eventId = getEndpoint().toLocalId(Event.class, eventId);

    User user = UserCache.getUser(wsContext);
    DBEvent dbEvent = entityManager.find(DBEvent.class, eventId);
    if (!canUserDeleteEvent(user, dbEvent))
      throw new WebServiceException("agenda:ACTION_DENIED");

    Query query = entityManager.createNamedQuery("deleteAttendants");
    query.setParameter("eventId", eventId);
    query.executeUpdate();

    query = entityManager.createNamedQuery("deleteEventPlaces");
    query.setParameter("eventId", eventId);
    query.executeUpdate();

    query = entityManager.createNamedQuery("deleteEventThemes");
    query.setParameter("eventId", eventId);
    query.executeUpdate();

    query = entityManager.createNamedQuery("deleteEventDocuments");
    query.setParameter("eventId", eventId);
    query.executeUpdate();

    if (dbEvent.getStartDateTime().equals(DELETED_EVENT_DATETIME))
    {
      query = entityManager.createNamedQuery("deleteProperties");
      query.setParameter("eventId", eventId);
      query.executeUpdate();

      entityManager.remove(dbEvent);
    }
    else
    {
      dbEvent.setStartDateTime(DELETED_EVENT_DATETIME);
      dbEvent.setEndDateTime(DELETED_EVENT_DATETIME);
      Auditor.auditChange(dbEvent, user.getUserId());

      entityManager.merge(dbEvent);
    }


    return true;
  }

  @Override
  public int countEvents(EventFilter filter)
  {
    User user = UserCache.getUser(wsContext);
    try
    {
      filter = getEndpoint().toLocal(EventFilter.class, filter);
      return countEvents(filter, user);
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "countEvents failed");
        throw WSExceptionFactory.create(ex);
    }
  }

  @Override
  public List<Event> findEvents(EventFilter filter)
  {
    List<Event> globalEvents = new ArrayList<Event>();
    User user = UserCache.getUser(wsContext);

    try
    {
      validateEventFilter(filter);
      
      filter = getEndpoint().toLocal(EventFilter.class, filter);

      List<Event> events = new ArrayList<Event>();
      if (SecurityMode.HIDDEN.equals(filter.getSecurityMode()))
        events = findHiddenEvents(filter, user);
      else
        events = findFilteredEvents(filter, user);
      
      if (events.size() > 0)
      {
        Map<String, List<DBEventProperty>> dbProperties = null;
        if (filter.isIncludeMetadata() != null &&
          filter.isIncludeMetadata().booleanValue())
        {
          dbProperties = findProperties(events);
        }

        for (Event event : events)
        {
          if (dbProperties != null && !dbProperties.isEmpty()
            && filter.isIncludeMetadata() != null
            && filter.isIncludeMetadata().booleanValue())
          {
            List<DBEventProperty> dbEventProperties =
              dbProperties.get(event.getEventId());
            if (dbEventProperties != null)
            {
              for(DBEventProperty dbEventProperty : dbEventProperties)
              {
                addPropertyToEvent(dbEventProperty, event);
              }
            }
          }
          globalEvents.add(getEndpoint().toGlobal(Event.class, event));
        }
      }
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "findEvents failed");
        throw WSExceptionFactory.create(ex);
    }

    return globalEvents;
  }

  @Override
  public List<EventView> findEventViews(EventFilter filter)
  {
    List<EventView> eventViews = new ArrayList<EventView>();
    User user = UserCache.getUser(wsContext);

    try
    {
      validateEventFilter(filter);
      
      filter = getEndpoint().toLocal(EventFilter.class, filter);

      List<Event> events = new ArrayList();
      if (SecurityMode.HIDDEN.equals(filter.getSecurityMode()))
        events = findHiddenEvents(filter, user);
      else
        events = findFilteredEvents(filter, user);

      LinkedHashMap<String, EventView> eventsMap = getLinkedEventViewMap(events);
      Set<String> eventIdSet = eventsMap.keySet();

      if (filter.isReducedInfo() == null || 
        (filter.isReducedInfo() != null && !filter.isReducedInfo().booleanValue()))
      {
        //Attendants
        List<AttendantView> attendantViews = findAttendantViews(eventIdSet);
        for (AttendantView attendantView : attendantViews)
        {
          EventView eventView = eventsMap.get(attendantView.getEvent().getEventId());
          if (!isEventViewHidden(eventView))
            eventView.getAttendants().add(attendantView);
        }
        //EventPlaces
        List<EventPlaceView> eventPlaceViews = findEventPlaceViews(eventIdSet);
        for (EventPlaceView eventPlaceView : eventPlaceViews)
        {
          EventView eventView =
            eventsMap.get(eventPlaceView.getEvent().getEventId());
          if (!isEventViewHidden(eventView))
          eventView.getPlaces().add(eventPlaceView);
        }
      }

      if (eventsMap.size() > 0)
      {
        Map<String, List<DBEventProperty>> dbProperties = null;
        //Properties
        if (filter.isIncludeMetadata() != null && filter.isIncludeMetadata())
        {
          dbProperties = findProperties(events);
        }
        
        for (EventView eventView : eventsMap.values())
        {
          if (dbProperties != null && !dbProperties.isEmpty() && 
            filter.isIncludeMetadata() != null && 
            filter.isIncludeMetadata())
          {
            List<DBEventProperty> dbEventProperties =
              dbProperties.get(eventView.getEventId());
            if (dbEventProperties != null)
            {
              for(DBEventProperty dbEventProperty : dbEventProperties)
              {
                addPropertyToEvent(dbEventProperty, eventView);
              }
            }
          }
          eventViews.add(getEndpoint().toGlobal(EventView.class, eventView));
        }
      }
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "findEvents failed");
        throw WSExceptionFactory.create(ex);
    }

    return eventViews;
  }

  private int countEvents(EventFilter filter, User user) throws Exception
  {
    boolean isAdminUser =
      user.getRoles().contains(AgendaConstants.AGENDA_ADMIN_ROLE);

    FindEventsQueryBuilder queryBuilder = FindEventsQueryBuilder.getInstance();
    queryBuilder.setCounterQuery(true);
    queryBuilder.setFilter(filter);
    queryBuilder.setUser(user);

    if (!isAdminUser)
    {
      List trustors = getTrustors(user);
      queryBuilder.setTrustors(trustors);
    }

    return queryBuilder.getEventCount(entityManager);
  }

  private Event loadEvent(EventFilter filter, User user) throws Exception
  {
    Event event = null;
    boolean isAdminUser =
      user.getRoles().contains(AgendaConstants.AGENDA_ADMIN_ROLE);

    FindEventsQueryBuilder queryBuilder = FindEventsQueryBuilder.getInstance();
    queryBuilder.setCounterQuery(false);
    filter.setSecurityMode(SecurityMode.HIDDEN);
    queryBuilder.setFilter(filter);
    queryBuilder.setUser(user);
    if (!isAdminUser)
      queryBuilder.setTrustors(getTrustors(user));

    try
    {
      event = queryBuilder.getEvent(entityManager);
    }
    catch(NoResultException ex)
    {
      return null;
    }

    //Find events with access allowed by trustors
    if (!isAdminUser && event != null)
    {
      queryBuilder.getFilter().setSecurityMode(SecurityMode.FILTERED);
      try
      {
        queryBuilder.getEvent(entityManager);
      }
      catch(NoResultException ex)
      {
        event.setSummary(HIDDEN_EVENT_STRING);
        event.setDescription(HIDDEN_EVENT_STRING);
      }
    }

    return event;
  }

  private List<Event> findHiddenEvents(EventFilter filter, User user)
    throws Exception
  {
    List<Event> events = new ArrayList();
    boolean isAdminUser =
      user.getRoles().contains(AgendaConstants.AGENDA_ADMIN_ROLE);

    FindEventsQueryBuilder queryBuilder = FindEventsQueryBuilder.getInstance();
    queryBuilder.setCounterQuery(false);
    queryBuilder.setFilter(filter);
    queryBuilder.setUser(user);
    List<Event> allEvents = queryBuilder.getEventList(entityManager);

    //Find events with access allowed by trustors
    if (!isAdminUser && allEvents != null)
    {
      queryBuilder.getFilter().setSecurityMode(SecurityMode.FILTERED);
      queryBuilder.setTrustors(getTrustors(user));
      List<Event> filteredEvents = queryBuilder.getEventList(entityManager);

      boolean isFilteredEmpty =
        (filteredEvents == null || filteredEvents.isEmpty());
      int i = 0;
      int f = 0;

      while (i < allEvents.size())
      {
        Event event = allEvents.get(i);
        String eventId = event.getEventId();

        if (isFilteredEmpty || f >= filteredEvents.size()
          || !eventId.equals(filteredEvents.get(f).getEventId()))
        {
          event.setSummary(HIDDEN_EVENT_STRING);
          event.setDescription(HIDDEN_EVENT_STRING);
        }
        else f++;
        
        events.add(event);
        i++;
      }
    }
    else if (isAdminUser && allEvents != null)
      events.addAll(allEvents);

    return events;
  }

  private List<Event> findFilteredEvents(EventFilter filter, User user)
    throws Exception
  {
    List<Event> events = new ArrayList();
    boolean isAdminUser =
      user.getRoles().contains(AgendaConstants.AGENDA_ADMIN_ROLE);

    FindEventsQueryBuilder queryBuilder = FindEventsQueryBuilder.getInstance();
    queryBuilder.setCounterQuery(false);
    queryBuilder.setFilter(filter);
    queryBuilder.setUser(user);
    if (!isAdminUser)
      queryBuilder.setTrustors(getTrustors(user));

    events = queryBuilder.getEventList(entityManager);

    return events;
  }

  private Map<String, List<DBEventProperty>> findProperties(List<Event> events)
    throws Exception
  {
    Map<String, List<DBEventProperty>> result = new HashMap();
    StringBuilder buffer = new StringBuilder("SELECT p FROM DBEventProperty p ");
    for (int i = 0; i < events.size(); i++)
    {
      if (i == 0)
        buffer.append(" WHERE ");
      else
        buffer.append(" OR ");
      
      buffer.append("p.eventId = :eventId").append(i);
    }
    Query query = entityManager.createQuery(buffer.toString());
    JPAQuery jpaQuery = new JPAQuery(query);

    for (int i = 0; i < events.size(); i++)
    {
      jpaQuery.setParameter("eventId" + i, events.get(i).getEventId());
    }
    List<DBEventProperty> dbProperties = jpaQuery.getResultList();

    for (DBEventProperty dbProperty : dbProperties)
    {
      List<DBEventProperty> dbEventProperties = result.get(dbProperty.getEventId());
      if (dbEventProperties == null)
      {
        dbEventProperties = new ArrayList();
        result.put(dbProperty.getEventId(), dbEventProperties);
      }
      dbEventProperties.add(dbProperty);
    }
    return result;
  }

  private List<String> getTrustors(User user)
  {
    List<String> trustors = new ArrayList();
    if (user.getPersonId() != null)
      trustors.add(user.getPersonId());  //User always trust itself

    PersonPersonFilter personPersonFilter = new PersonPersonFilter();
    personPersonFilter.setRelPersonId(user.getPersonId());
    List<PersonPersonView> personPersonList =
      getKernelManagerPort().findPersonPersonViews(personPersonFilter);

    for (PersonPersonView personPersonView : personPersonList)
    {
      String relTypeId = personPersonView.getPersonPersonTypeId();
      if (relTypeId != null)
      {
        relTypeId = getEndpoint().toLocalId(Type.class, relTypeId);
        if (CONFIDENT_TYPEID.equals(relTypeId))
          trustors.add(personPersonView.getPersonView().getPersonId());
      }
    }

    return trustors;
  }  

  @Override
  public EventDocument storeEventDocument(EventDocument eventDocument)
  {
    if (eventDocument == null)
      throw new WebServiceException("agenda:INVALID_EVENT_DOCUMENT"); 
    
    eventDocument = getEndpoint().toLocal(EventDocument.class, eventDocument);
    
    if (StringUtils.isBlank(eventDocument.getDocId()))
      throw new WebServiceException("agenda:INVALID_DOCUMENT"); 
    if (StringUtils.isBlank(eventDocument.getEventId()))
      throw new WebServiceException("agenda:INVALID_EVENT");    

    DBEventDocument dbEventDocument = new DBEventDocument(eventDocument);
    String eventDocId = eventDocument.getEventDocId();
    if (eventDocId == null)
    {
      entityManager.persist(dbEventDocument);
    }
    else
    {
      dbEventDocument = entityManager.merge(dbEventDocument);
    }

    auditEventChange(entityManager, dbEventDocument.getEventId());

    dbEventDocument.copyTo(eventDocument);
    eventDocument = getEndpoint().toGlobal(EventDocument.class, eventDocument);

    return eventDocument;
  }

  @Override
  public EventDocument loadEventDocument(String eventDocId)
  {
    if (eventDocId == null)
      throw new WebServiceException("agenda:ID_NULL");

    eventDocId = getEndpoint().toLocalId(EventDocument.class, eventDocId);

    DBEventDocument dbEventDocument =
      entityManager.find(DBEventDocument.class, eventDocId);

    if (dbEventDocument == null)
      throw new WebServiceException("agenda:OBJECT_NOT_FOUND");

    return getEndpoint().toGlobal(EventDocument.class, dbEventDocument);
  }

  @Override
  public boolean removeEventDocument(String eventDocId)
  {
    eventDocId = getEndpoint().toLocalId(EventDocument.class, eventDocId);

    DBEventDocument dbEventDocument =
      entityManager.find(DBEventDocument.class, eventDocId);
    entityManager.remove(dbEventDocument);

    return true;
  }

  @Override
  public List<EventDocument> findEventDocuments(EventDocumentFilter filter)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<EventDocumentView> findEventDocumentViews(EventDocumentFilter filter)
  {
    List<EventDocumentView> eventDocuments = new ArrayList<EventDocumentView>();
    
    if (StringUtils.isBlank(filter.getEventId()) && 
        StringUtils.isBlank(filter.getDocId()))
      throw new WebServiceException("FILTER_NOT_ALLOWED");
    
    Query query =
      entityManager.createNamedQuery("findEventDocuments");

    query.setParameter("eventId", filter.getEventId());
    query.setParameter("docId", filter.getDocId());
    query.setFirstResult(filter.getFirstResult());
    int maxResults = filter.getMaxResults();
    if (maxResults > 0) query.setMaxResults(maxResults);

    List<DBEventDocument> dbEventDocuments = query.getResultList();
    if (dbEventDocuments != null && !dbEventDocuments.isEmpty())
    {
      DocumentFilter documentFilter = new DocumentFilter();
      HashMap<String, List<EventDocumentView>> docIdMap = new HashMap();
      for (DBEventDocument dbEventDocument : dbEventDocuments)
      {
        EventDocumentView eventDocumentView = new EventDocumentView();
        eventDocumentView.setEventDocId(dbEventDocument.getEventDocId());
        eventDocumentView.setEventDocTypeId(dbEventDocument.getEventDocTypeId());
        Event event = new Event();
        dbEventDocument.getDbEvent().copyTo(event);
        eventDocumentView.setEvent(event);

        String docId = dbEventDocument.getDocId();
        if (docId != null) //Set DocumentView
        {
          List list = docIdMap.get(docId);
          if (list == null) list = new ArrayList();
          list.add(eventDocumentView);
          docIdMap.put(docId, list);
          documentFilter.getDocId().add(docId);
        }
      }

      //Invokes document WS findDocument
      try
      {
        DocumentManagerPort port = getDocumentManagerPort();
        documentFilter.setIncludeContentMetadata(true);
        documentFilter.setFirstResult(0);
        documentFilter.setMaxResults(docIdMap.size());
        List<Document> documents = port.findDocuments(documentFilter);
        for (Document document : documents)
        {
          List<EventDocumentView> eventDocumentList = docIdMap.get(document.getDocId());
          for (EventDocumentView eventDocumentView : eventDocumentList)
          {
            eventDocumentView.setDocument(document);
            eventDocuments.add(eventDocumentView);
          }
        }
      }
      catch (Exception e)
      {
        throw new WebServiceException(e);
      }
    }

    return eventDocuments;
  }

  @Override
  public EventPlace storeEventPlace(EventPlace eventPlace)
  {
    if (eventPlace == null)
      throw new WebServiceException("agenda:INVALID_EVENT_PLACE"); 
    
    eventPlace = getEndpoint().toLocal(EventPlace.class, eventPlace);    
    DBEventPlace dbEventPlace = new DBEventPlace(eventPlace);

    if (eventPlace.getRoomId() != null && eventPlace.getRoomId().length() > 0)
      checkRoomAvailability(dbEventPlace);

    if (dbEventPlace.isRoom() || dbEventPlace.isAddress())
    {
      Address address =
        getKernelManagerPort().loadAddress(eventPlace.getAddressId());
      String streetId = address.getStreetId();
      if (streetId != null)
      {
        String[] parts = streetId.split(PK_SEPARATOR);
        if (parts != null && parts.length > 1)
        {
          dbEventPlace.setPaiscod(parts[0]);
          dbEventPlace.setProvcod(parts[1]);
        }
      }
    }

    String eventPlaceId = eventPlace.getEventPlaceId();
    if (eventPlaceId == null)
    {
      int numordre = 1;
      Query query = entityManager.createNamedQuery("getEventPlaceNumordre");
      query.setParameter("eventId", dbEventPlace.getEventId());
      Object counter = query.getSingleResult();
      if (counter != null)
        numordre = ((Number)query.getSingleResult()).intValue();
      dbEventPlace.setNumordre(String.valueOf(numordre));

      entityManager.persist(dbEventPlace);
    }
    else
    {
      dbEventPlace = entityManager.merge(dbEventPlace);
    }

    auditEventChange(entityManager, dbEventPlace.getEventId());

    dbEventPlace.copyTo(eventPlace);
    eventPlace = getEndpoint().toGlobal(EventPlace.class, eventPlace);

    return eventPlace;
  }

  @Override
  public EventPlace loadEventPlace(String eventPlaceId)
  {
    if (eventPlaceId == null)
      throw new WebServiceException("agenda:ID_NULL");

    eventPlaceId = getEndpoint().toLocalId(EventPlace.class, eventPlaceId);

    DBEventPlacePK pk = new DBEventPlacePK(eventPlaceId);
    DBEventPlace dbEventPlace =
      entityManager.find(DBEventPlace.class, pk);
    if (dbEventPlace == null)
      throw new WebServiceException("agenda:OBJECT_NOT_FOUND");
    
    return getEndpoint().toGlobal(EventPlace.class, dbEventPlace);
  }

  @Override
  public boolean removeEventPlace(String eventPlaceId)
  {
    eventPlaceId = getEndpoint().toLocalId(EventPlace.class, eventPlaceId);

    DBEventPlacePK pk = new DBEventPlacePK(eventPlaceId);
    DBEventPlace dbEventPlace =
      entityManager.find(DBEventPlace.class, pk);
    entityManager.remove(dbEventPlace);

    auditEventChange(entityManager, dbEventPlace.getEventId());

    return true;
  }

  @Override
  public List<EventPlace> findEventPlaces(EventPlaceFilter filter)
  {
    List<EventPlace> result = new ArrayList();
    
    if (StringUtils.isBlank(filter.getAddressId()) &&
        StringUtils.isBlank(filter.getRoomId()) &&
        StringUtils.isBlank(filter.getEventId()))
      throw new WebServiceException("FILTER_NOT_ALLOWED");    
    
    Query query =
      entityManager.createNamedQuery("findEventPlaces");

    query.setParameter("eventId", filter.getEventId());

    String roomId = filter.getRoomId();
    String[] roomIdArray = null;
    if (roomId != null)
      roomIdArray = roomId.split(AgendaManager.PK_SEPARATOR);
    query.setParameter("domcod", roomId != null ? roomIdArray[0] : null);
    query.setParameter("salacod", roomId != null ? roomIdArray[1] : null);

    String addressId = filter.getAddressId();
    String[] addressIdArray = null;
    if (addressId != null)
      addressIdArray = addressId.split(AgendaManager.PK_SEPARATOR);
    query.setParameter("paiscod", addressId != null ? addressIdArray[0] : null);
    query.setParameter("provcod", addressId != null ?  addressIdArray[1] : null);
    query.setParameter("municod", addressId != null ?  addressIdArray[2] : null);
    query.setParameter("carcod", addressId != null ?  addressIdArray[3] : null);
    if (roomId == null)
      query.setParameter("domcod",  addressId != null ?  addressIdArray[4] : null);
    query.setFirstResult(filter.getFirstResult());
    int maxResults = filter.getMaxResults();
    if (maxResults > 0) query.setMaxResults(maxResults);

    List<DBEventPlace> dbEventPlaces = query.getResultList();
    for (DBEventPlace dbEventPlace : dbEventPlaces)
    {
      EventPlace eventPlace = new EventPlace();
      dbEventPlace.copyTo(eventPlace);
      result.add(eventPlace);
    }
    
    return result;
  }

  @Override
  public List<EventPlaceView> findEventPlaceViews(EventPlaceFilter filter)
  {
    List<EventPlaceView> eventPlaces = new ArrayList<EventPlaceView>();
    
    if (StringUtils.isBlank(filter.getAddressId()) &&
        StringUtils.isBlank(filter.getRoomId()) &&
        StringUtils.isBlank(filter.getEventId()))
      throw new WebServiceException("FILTER_NOT_ALLOWED");
    
    Query query =
      entityManager.createNamedQuery("findEventPlaces");

    query.setParameter("eventId", filter.getEventId());

    String roomId = filter.getRoomId();
    String[] roomIdArray = null;
    if (roomId != null)
      roomIdArray = roomId.split(AgendaManager.PK_SEPARATOR);
    query.setParameter("domcod", roomId != null ? roomIdArray[0] : null);
    query.setParameter("salacod", roomId != null ? roomIdArray[1] : null);

    String addressId = filter.getAddressId();
    String[] addressIdArray = null;
    if (addressId != null)
      addressIdArray = addressId.split(AgendaManager.PK_SEPARATOR);
    query.setParameter("paiscod", addressId != null ? addressIdArray[0] : null);
    query.setParameter("provcod", addressId != null ?  addressIdArray[1] : null);
    query.setParameter("municod", addressId != null ?  addressIdArray[2] : null);
    query.setParameter("carcod", addressId != null ?  addressIdArray[3] : null);
    if (roomId == null)
      query.setParameter("domcod",  addressId != null ?  addressIdArray[4] : null);

    query.setFirstResult(filter.getFirstResult());
    int maxResults = filter.getMaxResults();
    if (maxResults > 0) query.setMaxResults(maxResults);
    List<DBEventPlace> dbEventPlaces = query.getResultList();

    if (dbEventPlaces != null && !dbEventPlaces.isEmpty())
    {
      eventPlaces = createEventPlaceViews(dbEventPlaces);
    }

    return eventPlaces;
  }

  @Override
  public Attendant storeAttendant(Attendant attendant)
  {
    if (attendant == null)
      throw new WebServiceException("agenda:INVALID_ATTENDANT"); 
      
    attendant = getEndpoint().toLocal(Attendant.class, attendant);
    
    if (StringUtils.isBlank(attendant.getPersonId()))
      throw new WebServiceException("agenda:INVALID_PERSON"); 
    if (StringUtils.isBlank(attendant.getEventId()))
      throw new WebServiceException("agenda:INVALID_EVENT");

    String attendantTypeId = attendant.getAttendantTypeId();
    attendantTypeId =
      getEndpoint().toGlobalId(org.matrix.dic.Type.class, attendantTypeId);
    Type attendantType = TypeCache.getInstance().getType(attendantTypeId);

    DBAttendant dbAttendant = new DBAttendant(attendant);
    if (attendantType != null)
      dbAttendant.setRol(attendantType.getDescription());

    String attendantId = attendant.getAttendantId();
    if (attendantId == null)
    {
      DBAttendantPK pk = new DBAttendantPK();
      pk.setEventId(attendant.getEventId());
      pk.setPersonId(attendant.getPersonId());
      if (entityManager.find(DBAttendant.class, pk) != null)
        throw new WebServiceException("agenda:DUPLICATED_ATTENDANT");
      entityManager.persist(dbAttendant);
    }
    else
    {
      dbAttendant = entityManager.merge(dbAttendant);
    }

    auditEventChange(entityManager, dbAttendant.getEventId());

    dbAttendant.copyTo(attendant);
    attendant = getEndpoint().toGlobal(Attendant.class, attendant);

    return attendant;
  }

  @Override
  public Attendant loadAttendant(String attendantId)
  {
    if (attendantId == null)
      throw new WebServiceException("agenda:ID_NULL");

    attendantId = getEndpoint().toLocalId(Attendant.class, attendantId);

    DBAttendantPK attendantPK = new DBAttendantPK(attendantId);
    DBAttendant dbAttendant =
      entityManager.find(DBAttendant.class, attendantPK);
    if (dbAttendant == null)
      throw new WebServiceException("agenda:OBJECT_NOT_FOUND");

    return getEndpoint().toGlobal(Attendant.class, dbAttendant);
  }

  @Override
  public boolean removeAttendant(String attendantId)
  {
    attendantId = getEndpoint().toLocalId(Attendant.class, attendantId);

    DBAttendantPK attendantPK = new DBAttendantPK(attendantId);
    DBAttendant dbAttendant =
      entityManager.find(DBAttendant.class, attendantPK);
    entityManager.remove(dbAttendant);

    auditEventChange(entityManager, dbAttendant.getEventId());

    return true;
  }

  @Override
  public List<Attendant> findAttendants(AttendantFilter filter)
  {
    List<Attendant> globalAttendants = new ArrayList<Attendant>();
    String eventId = filter.getEventId();
    String personId = filter.getPersonId();
    
    if (!StringUtils.isBlank(eventId) || !StringUtils.isBlank(personId))
    {
      Query query = entityManager.createNamedQuery("findAttendants");
      query.setParameter("eventId", filter.getEventId());
      query.setParameter("personId", filter.getPersonId());
      query.setParameter("userId", getUserId());
      query.setFirstResult(filter.getFirstResult());
      int maxResults = filter.getMaxResults();
      if (maxResults > 0) query.setMaxResults(maxResults);

      User user = UserCache.getUser(wsContext);
      query.setParameter("userPersonId", user.getPersonId());
      boolean isAdminUser =
        user.getRoles().contains(AgendaConstants.AGENDA_ADMIN_ROLE);
      query.setParameter("isAdmin", isAdminUser ? "S" : "N");
      List trustors = getTrustors(user);
      if (trustors != null)
        query.setParameter("trustors", "," + TextUtils.collectionToString(trustors) + ",");
      else
        query.setParameter("trustors", ",");
      
      List<DBAttendant> dbAttendants = query.getResultList();

      if (dbAttendants.size() > 0)
      {
        for (DBAttendant dbAttendant : dbAttendants)
        {
          Attendant attendant = new Attendant();
          dbAttendant.copyTo(attendant);
          globalAttendants.add(getEndpoint().toGlobal(Attendant.class, attendant));
        }
      }
    }
    else
      throw new WebServiceException("FILTER_NOT_ALLOWED");      

    return globalAttendants;
  }


  @Override
  public List<AttendantView> findAttendantViews(AttendantFilter filter)
  {
    List<AttendantView> attendants = new ArrayList<AttendantView>();
    String eventId = filter.getEventId();
    String personId = filter.getPersonId();

    if (!StringUtils.isBlank(eventId) || !StringUtils.isBlank(personId))
    {
      User user = UserCache.getUser(wsContext);
      Query query = entityManager.createNamedQuery("findAttendants");
      query.setParameter("eventId", eventId);
      query.setParameter("personId", personId);
      String userId = user.getUserId();
      query.setParameter("userId", userId != null ? userId.trim() : userId);
      query.setFirstResult(filter.getFirstResult());
      int maxResults = filter.getMaxResults();
      if (maxResults > 0) query.setMaxResults(maxResults);

      query.setParameter("userPersonId", user.getPersonId());
      boolean isAdminUser =
        user.getRoles().contains(AgendaConstants.AGENDA_ADMIN_ROLE);
      query.setParameter("isAdmin", isAdminUser ? "S" : "N");
      List trustors = getTrustors(user);
      if (trustors != null)
        query.setParameter("trustors", "," + TextUtils.collectionToString(trustors) + ",");
      else
        query.setParameter("trustors", ",");


      List<DBAttendant> dbAttendants = query.getResultList();
      if (dbAttendants != null && !dbAttendants.isEmpty())
      {
        attendants = createAttendantViews(dbAttendants, eventId != null);
      }
    }
    else
      throw new WebServiceException("FILTER_NOT_ALLOWED");       

    return attendants;
  }

  @Override
  public List<Attendant> findAttendantsOccupancy(String eventId)
  {
    List<Attendant> attendants = new ArrayList<Attendant>();

    if (!StringUtils.isBlank(eventId))
    {
      JPAQuery query = new JPAQuery(
        entityManager.createNamedQuery("findAttendantsOccupancy"));
      try
      {
        query.setParameter("eventId", eventId);
        attendants = query.getResultList();
      }
      catch (Exception ex)
      {
        throw new WebServiceException(ex);
      }
    }

    return attendants;
  }

  @Override
  public Theme storeTheme(Theme theme)
  {
    theme = getEndpoint().toLocal(Theme.class, theme);

    DBTheme dbTheme = new DBTheme(theme);
    String themeId = theme.getThemeId();
    if (themeId == null)
    {
      entityManager.persist(dbTheme);
    }
    else
    {
      dbTheme = entityManager.merge(dbTheme);
    }

    dbTheme.copyTo(theme);
    theme = getEndpoint().toGlobal(Theme.class, theme);

    return theme;
  }

  @Override
  public Theme loadTheme(String themeId)
  {
    if (themeId == null)
      throw new WebServiceException("agenda:ID_NULL");

    themeId = getEndpoint().toLocalId(Theme.class, themeId);

    DBTheme dbTheme =
      entityManager.find(DBTheme.class, themeId);
    if (dbTheme == null)
      throw new WebServiceException("agenda:OBJECT_NOT_FOUND");

    return getEndpoint().toGlobal(Theme.class, dbTheme);
  }

  @Override
  public boolean removeTheme(String themeId)
  {
    themeId = getEndpoint().toLocalId(Theme.class, themeId);

    DBTheme dbTheme =
      entityManager.find(DBTheme.class, themeId);
    entityManager.remove(dbTheme);

    return true;
  }

  @Override
  public int countThemes(ThemeFilter filter)
  {
    Query query =
      entityManager.createNamedQuery("countThemes");

    query.setParameter("themeId", filter.getThemeId());
    String description = filter.getDescription();
    query.setParameter("description", description != null ?
      "%" + description.toLowerCase() + "%" : description);

    return ((Number)query.getSingleResult()).intValue();
  }

  @Override
  public List<Theme> findThemes(ThemeFilter filter)
  {
    Query query =
      entityManager.createNamedQuery("findThemes");

    query.setParameter("themeId", filter.getThemeId());
    String description = filter.getDescription();
    query.setParameter("description", description != null ?
      "%" + description.toLowerCase() + "%" : description);
    query.setFirstResult(filter.getFirstResult());
    int maxResults = filter.getMaxResults();
    if (maxResults > 0) query.setMaxResults(maxResults);

    return query.getResultList();
  }

  @Override
  public EventTheme storeEventTheme(EventTheme eventTheme)
  {
    if (eventTheme == null)
      throw new WebServiceException("agenda:INVALID_EVENT_THEME"); 
    
    eventTheme = getEndpoint().toLocal(EventTheme.class, eventTheme);
    
    if (StringUtils.isBlank(eventTheme.getThemeId()))
      throw new WebServiceException("agenda:INVALID_THEME"); 
    if (StringUtils.isBlank(eventTheme.getEventId()))
      throw new WebServiceException("agenda:INVALID_EVENT");

    DBEventTheme dbEventTheme = new DBEventTheme(eventTheme);
    String eventThemeId = eventTheme.getEventThemeId();
    if (eventThemeId == null)
    {
      DBEventThemePK pk =
        new DBEventThemePK(eventTheme.getEventId(), eventTheme.getThemeId());
      DBEventTheme currentEventTheme =
        entityManager.find(DBEventTheme.class, pk);
      if (currentEventTheme == null)
        entityManager.persist(dbEventTheme);
      else
        entityManager.merge(dbEventTheme);
    }
    else
    {
      dbEventTheme = entityManager.merge(dbEventTheme);
    }

    auditEventChange(entityManager, dbEventTheme.getEventId());

    dbEventTheme.copyTo(eventTheme);
    eventTheme = getEndpoint().toGlobal(EventTheme.class, eventTheme);

    return eventTheme;
  }

  @Override
  public EventTheme loadEventTheme(String eventThemeId)
  {
    if (eventThemeId == null)
      throw new WebServiceException("agenda:ID_NULL");

    eventThemeId = getEndpoint().toLocalId(EventTheme.class, eventThemeId);

    DBEventThemePK pk = new DBEventThemePK(eventThemeId);
    DBEventTheme dbEventTheme =
      entityManager.find(DBEventTheme.class, pk);
    if (dbEventTheme == null)
      throw new WebServiceException("agenda:OBJECT_NOT_FOUND");

    return getEndpoint().toGlobal(EventTheme.class, dbEventTheme);
  }

  @Override
  public boolean removeEventTheme(String eventThemeId)
  {
    eventThemeId = getEndpoint().toLocalId(EventTheme.class, eventThemeId);

    DBEventThemePK pk = new DBEventThemePK(eventThemeId);
    DBEventTheme dbEventTheme =
      entityManager.find(DBEventTheme.class, pk);
    entityManager.remove(dbEventTheme);

    auditEventChange(entityManager, dbEventTheme.getEventId());

    return true;
  }

  @Override
  public List<EventTheme> findEventThemes(EventThemeFilter filter)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<EventThemeView> findEventThemeViews(EventThemeFilter filter)
  {
    List<EventThemeView> eventThemeViews = new ArrayList<EventThemeView>();
    String eventId = filter.getEventId();
    String themeId = filter.getThemeId();

    if (eventId != null || themeId != null)
    {
      Query query = entityManager.createNamedQuery("findEventThemes");
      query.setParameter("eventId", eventId);
      query.setParameter("themeId", themeId);
      query.setFirstResult(filter.getFirstResult());
      int maxResults = filter.getMaxResults();
      if (maxResults > 0) query.setMaxResults(maxResults);

      Vector<Object[]> results = (Vector)query.getResultList();

      if (results != null && !results.isEmpty())
      {
        for (Object[] result : results)
        {
          DBEventTheme dbEventTheme = (DBEventTheme)result[0];
          DBTheme dbTheme = (DBTheme)result[1];
          EventThemeView eventThemeView = new EventThemeView();
          eventThemeView.setEventThemeId(dbEventTheme.getEventId() +
            AgendaManager.PK_SEPARATOR + dbEventTheme.getThemeId());
          eventThemeView.setEventId(dbEventTheme.getEventId());
          eventThemeView.setThemeId(dbEventTheme.getThemeId());
          eventThemeView.setDescription(dbTheme.getDescription());

          eventThemeViews.add(eventThemeView);
        }
      }
    }
    else
      throw new WebServiceException("FILTER_NOT_ALLOWED");

    return eventThemeViews;
  }


  private WSEndpoint getEndpoint()
  {
    String endpointName = WSUtils.getServletAdapter(wsContext).getName();
    return WSDirectory.getInstance().getEndpoint(endpointName);
  }

  private boolean canUserCreateEvent(User user, Event event)
  {
    return checkTypeACL(user.getRoles(), event, DictionaryConstants.CREATE_ACTION);
  }

  private boolean canUserModifyEvent(User user, Event event)
  {
    return canUserDoAction(user, event, DictionaryConstants.WRITE_ACTION);
  }

  private boolean canUserDeleteEvent(User user, Event event)
  {
    return canUserDoAction(user, event, DictionaryConstants.DELETE_ACTION);
  }

  private boolean canUserDoAction(User user, Event event, String action)
  {
    Set<String> userRoles = user.getRoles();
    if (checkTypeACL(userRoles, event, action))
      return true;
    else
      return false;
  }

  private boolean checkTypeACL(Set<String> userRoles, Event event, String action)
  {
    String eventTypeId = event.getEventTypeId();
    try
    {
      eventTypeId = getEndpoint().toGlobalId(org.matrix.dic.Type.class, eventTypeId);
      Type eventType = TypeCache.getInstance().getType(eventTypeId);
      return (eventType.canPerformAction(action, userRoles)
        || userRoles.contains(AgendaConstants.AGENDA_ADMIN_ROLE));
    }
    catch (Exception ex)
    {
      return userRoles.contains(AgendaConstants.AGENDA_ADMIN_ROLE);
    }
  }

  private boolean isPublicEvent(Event event)
  {
    if (event.isOnlyAttendants() != null && event.isOnlyAttendants().booleanValue())
      return false;

    Set set = new HashSet();
    set.add(SecurityConstants.EVERYONE_ROLE);

    String eventTypeId = event.getEventTypeId();
    try
    {
      eventTypeId = getEndpoint().toGlobalId(org.matrix.dic.Type.class, eventTypeId);
      Type eventType = TypeCache.getInstance().getType(eventTypeId);
      return (eventType.canPerformAction(DictionaryConstants.READ_ACTION, set));
    }
    catch (Exception ex)
    {
    }

    return false;
  }

  private void validateEvent(Event event, Type eventType)
  {
    String startDateTime = event.getStartDateTime();
    String endDateTime = event.getEndDateTime();

    if (startDateTime == null || endDateTime == null)
      throw new WebServiceException("agenda:INVALID_DATES");

    if (startDateTime.compareTo(endDateTime) >= 0)
      throw new WebServiceException("agenda:INVALID_DATES");

    //Module constraints validation
    if (eventType == null)
      throw new WebServiceException("dic:TYPE_NOT_FOUND");

    //Dictionary properties validation
    WSTypeValidator validator = new WSTypeValidator(eventType);
    validator.validate(event, "eventId");
  }

  private void checkRoomAvailability(DBEventPlace eventPlace)
  {
    String eventId = eventPlace.getEventId();
    String domcod = eventPlace.getDomcod();
    String salacod = eventPlace.getSalacod();

    Query query = entityManager.createNamedQuery("checkEventPlaceRoomAvailability");
    query.setParameter("eventId", eventId);
    query.setParameter("domcod", domcod);
    query.setParameter("salacod", salacod);

    List events = query.getResultList();

    if (events != null && events.size() > 0)
      throw WSExceptionFactory.create("agenda:ROOM_UNAVAILABLE");
  }
  
  private boolean isRescheduled(DBEvent curEvent, DBEvent newEvent)
  {
    return !((curEvent == null && newEvent == null)
      || (curEvent != null && newEvent != null 
        && curEvent.getStartDateTime().equals(newEvent.getStartDateTime())
        && curEvent.getEndDateTime().equals(newEvent.getEndDateTime())));
  }

  private void checkRoomAvailability(DBEvent event)
  {
    String eventId = event.getEventId();
    String datainici = event.getDatainici();
    String datafinal = event.getDatafinal();
    String horainici = event.getHorainici();
    String horafinal = event.getHorafinal();

    Query query = entityManager.createNamedQuery("checkEventRoomAvailability");
    query.setParameter("eventId", eventId);
    query.setParameter("datainici", datainici);
    query.setParameter("datafinal", datafinal);
    query.setParameter("horainici", horainici);
    query.setParameter("horafinal", horafinal);

    List events = query.getResultList();

    if (events != null && events.size() > 0)
      throw WSExceptionFactory.create("agenda:ROOM_UNAVAILABLE");
  }

  private void addPropertyToEvent(DBEventProperty srcDbProperty, Event event)
  {
    List<Property> dstProperties = event.getProperty();
    Property eventProperty =
      getPropertyByName(srcDbProperty.getName(), dstProperties);
    if (eventProperty == null)
    {
      eventProperty = new Property();
      eventProperty.setName(srcDbProperty.getName());
      event.getProperty().add(eventProperty);
    }
    else
      eventProperty.getValue().clear();

    eventProperty.getValue().add(srcDbProperty.getValue());
  }

  private void addPropertyToEvent(DBEventProperty srcDbProperty, EventView event)
  {
    List<Property> dstProperties = event.getProperty();
    Property eventProperty =
      getPropertyByName(srcDbProperty.getName(), dstProperties);
    if (eventProperty == null)
    {
      eventProperty = new Property();
      eventProperty.setName(srcDbProperty.getName());
      event.getProperty().add(eventProperty);
    }
    else
      eventProperty.getValue().clear();

    eventProperty.getValue().add(srcDbProperty.getValue());
  }

  private Property getPropertyByName(String name, List<Property> properties)
  {
    Iterator it = properties.iterator();
    while (it.hasNext())
    {
      Property prop = (Property)it.next();
      if (prop.getName().equals(name))
        return prop;
    }

    return null;
  }

  private void storeEventProperties(Event event,
    List<DBEventProperty> curDbProperties)
  {
    String eventId = event.getEventId();

    ArrayList<Property> properties = new ArrayList<Property>();
    properties.addAll(event.getProperty());

    if (curDbProperties != null)
    {
      for (DBEventProperty dbEventProperty : curDbProperties)
      {
        String propertyName = dbEventProperty.getName();
        Property property =
           getPropertyByName(propertyName, properties);
        if (property == null)
        {
          if (!INCREMENTAL_PROPERTIES.contains(propertyName))
          {
            // remove property
            entityManager.remove(dbEventProperty);
          }
        }
        else
        {
          properties.remove(property);
          // update property
          for (String value : property.getValue())
          {
            dbEventProperty.setName(property.getName());
            dbEventProperty.setValue(value);
            entityManager.merge(dbEventProperty);
          }
        }
      }
    }

    // insert new property
    for (Property property : properties)
    {
      for (String value : property.getValue())
      {
        DBEventProperty dbProperty = new DBEventProperty();
        dbProperty.setEventId(eventId);
        dbProperty.setName(property.getName());
        dbProperty.setValue(value);
        entityManager.persist(dbProperty);
      }
    }
  }

  private KernelManagerPort getKernelManagerPort()
  {
    try
    {
      WSDirectory wsDirectory = WSDirectory.getInstance();
      WSEndpoint endpoint =
        wsDirectory.getEndpoint(KernelManagerService.class);

      Credentials credentials = SecurityUtils.getCredentials(wsContext);
      return endpoint.getPort(KernelManagerPort.class, credentials.getUserId(),
        credentials.getPassword());
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  private DocumentManagerPort getDocumentManagerPort()
  {
    try
    {
      WSDirectory wsDirectory = WSDirectory.getInstance();
      WSEndpoint endpoint =
        wsDirectory.getEndpoint(DocumentManagerService.class);

      Credentials credentials = SecurityUtils.getCredentials(wsContext);
      return endpoint.getPort(DocumentManagerPort.class, credentials.getUserId(),
        credentials.getPassword());
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }
  
//  private SQLManagerPort getSQLManagerPort()
//  {
//    try
//    {
//      WSDirectory wsDirectory = WSDirectory.getInstance();
//      WSEndpoint endpoint =
//        wsDirectory.getEndpoint(SQLManagerService.class);
//
//      String userId = MatrixConfig.getProperty("adminCredentials.userId");
//      String password = MatrixConfig.getProperty("adminCredential.password");
//      return endpoint.getPort(SQLManagerPort.class, userId, password);
//    }
//    catch (Exception ex)
//    {
//      throw new RuntimeException(ex);
//    }
//  }  

  private List<EventPlaceView> createEventPlaceViews(
    List<DBEventPlace> dbEventPlaces)
  {
    List<EventPlaceView> viewList = new ArrayList<EventPlaceView>();
    Map<String, List<EventPlaceView>> addressMap = new HashMap();
    Map<String, List<EventPlaceView>> roomMap = new HashMap();

    //List all EventPlaceViews sorted and put in maps by place identifier as key
    //to access later to them
    for (DBEventPlace dbEventPlace : dbEventPlaces)
    {
      EventPlaceView view = new EventPlaceView();
      view.setEventPlaceId(dbEventPlace.getEventPlaceId());
      view.setComments(dbEventPlace.getComments());
      DBEvent dbEvent = dbEventPlace.getDbEvent();
      Event event = new Event();
      dbEvent.copyTo(event);
      view.setEvent(event);

      if (dbEventPlace.isRoom())
      {
        String roomId = dbEventPlace.getRoomId();
        List list = roomMap.get(roomId);
        if (list == null)
          list = new ArrayList();
        list.add(view);
        roomMap.put(roomId, list);
      }
      else if (dbEventPlace.isAddress())
      {
        String addressId = dbEventPlace.getAddressId();
        if (addressId != null)
        {
          List list = addressMap.get(addressId);
          if (list == null)
            list = new ArrayList();
          list.add(view);
          addressMap.put(addressId, list);
        }
      }
      else if (dbEventPlace.isStreet()) //set as comment
      {
        String streetId = dbEventPlace.getStreetId();
        String number = dbEventPlace.getNumero();
        String comments = dbEventPlace.getComments();
        if (streetId != null)
        {
          Street street = getKernelManagerPort().loadStreet(streetId);
          if (street != null)
          {
            String streetDesc =
              street.getStreetTypeId() + " " +
              street.getName() + (number != null ? ", " + number : "");
            comments =  streetDesc  + (comments != null ? " " + comments : "");
            view.setComments(comments);
          }
        }
      }

      viewList.add(view);
    }

    //Set kernel data (AddressView and RoomView) to EventPlaceView. Get mapped
    //views and set with kernel data.
    KernelManagerPort port = getKernelManagerPort();
    //Addresses
    Set<String> addressIdSet = addressMap.keySet();
    if (addressIdSet != null && addressIdSet.size() > 0)
    {
      AddressFilter addressFilter = new AddressFilter();
      addressFilter.getAddressIdList().addAll(addressIdSet);
      List<AddressView> addressViewList = port.findAddressViews(addressFilter);
      for (AddressView addressView : addressViewList)
      {
        List<EventPlaceView> eventPlaceViewList = addressMap.get(addressView.getAddressId());
        for (EventPlaceView eventPlaceView : eventPlaceViewList)
        {
          eventPlaceView.setAddressView(addressView);
        }
      }
    }
    //Rooms
    Set<String> roomIdSet = roomMap.keySet();
    if (roomIdSet != null && roomIdSet.size() > 0)
    {
      RoomFilter roomFilter = new RoomFilter();
      roomFilter.getRoomIdList().addAll(roomIdSet);
      List<RoomView> roomViewList = port.findRoomViews(roomFilter);
      for (RoomView roomView : roomViewList)
      {
        List<EventPlaceView> eventPlaceViewList = roomMap.get(roomView.getRoomId());
        for (EventPlaceView eventPlaceView : eventPlaceViewList)
        {
          eventPlaceView.setRoomView(roomView);
        }
      }
    }

    return viewList;
  }

  private LinkedHashMap<String, EventView> getLinkedEventViewMap(List<Event> events)
  {
    LinkedHashMap<String, EventView> map = new LinkedHashMap();
    if (events != null && events.size() > 0)
    {
      for (Event event : events)
      {
        EventView eventView = new EventView();
        eventView.setEventId(event.getEventId());
        eventView.setEventTypeId(event.getEventTypeId());
        String eventTypeId =
          getEndpoint().toGlobalId(org.matrix.dic.Type.class, event.getEventTypeId());
        Type type = TypeCache.getInstance().getType(eventTypeId);
        if (type != null)
          eventView.setEventTypeName(type.getDescription());
        eventView.setEditable(
          canUserModifyEvent(UserCache.getUser(wsContext), event));
        eventView.setPublic(isPublicEvent(event));
        eventView.setSummary(event.getSummary());
        eventView.setDescription(event.getDescription());
        eventView.setStartDateTime(event.getStartDateTime());
        eventView.setEndDateTime(event.getEndDateTime());
        eventView.setChangeDateTime(event.getChangeDateTime());
        eventView.setChangeUserId(event.getChangeUserId());
        eventView.setOnlyAttendants(event.isOnlyAttendants() == null ? false : event.isOnlyAttendants());
        map.put(event.getEventId(), eventView);
      }
    }
    return map;
  }

  private List<AttendantView> findAttendantViews(Set<String> eventIds)
    throws Exception
  {
    List<AttendantView> attendants = new ArrayList<AttendantView>();
    List<String> eventIdList = new ArrayList();
    eventIdList.addAll(eventIds);
    JPAQuery query = new JPAQuery(entityManager.createNamedQuery("findPersonsFromEvents"));
    query.setIdParameter("eventId", eventIdList);

    List<DBAttendant> dbAttendants = query.getResultList();
    if (dbAttendants != null && !dbAttendants.isEmpty())
    {
      attendants = createAttendantViews(dbAttendants, true);
    }

    return attendants;
  }

  private List<EventPlaceView> findEventPlaceViews(Set<String> eventIds)
    throws Exception
  {
    List<EventPlaceView> eventPlaceViews = new ArrayList<EventPlaceView>();
    List<String> eventIdList = new ArrayList();
    eventIdList.addAll(eventIds);
    JPAQuery query =
      new JPAQuery(entityManager.createNamedQuery("findPlacesFromEvents"));
    query.setIdParameter("eventId", eventIdList);

    List<DBEventPlace> dbEventPlaces = query.getResultList();
    if (dbEventPlaces != null && !dbEventPlaces.isEmpty())
    {
      eventPlaceViews = createEventPlaceViews(dbEventPlaces);
    }

    return eventPlaceViews;
  }

  private List<AttendantView> createAttendantViews(List<DBAttendant> dbAttendants,
    boolean personsData)
  {
    List<AttendantView> attendantViews = new ArrayList<AttendantView>();

    HashMap<String, List<AttendantView>> personIdMap = new HashMap();
    for (DBAttendant dbAttendant : dbAttendants)
    {
      AttendantView attendantView = new AttendantView();
      attendantView.setAttendantId(dbAttendant.getAttendantId());
      attendantView.setPersonId(dbAttendant.getPersonId());
      attendantView.setEventId(dbAttendant.getEventId());
      attendantView.setAttendantTypeId(dbAttendant.getAttendantTypeId());
      attendantView.setComments(dbAttendant.getComments());
      attendantView.setHidden(dbAttendant.isHidden());
      attendantView.setAttended(dbAttendant.getAttended());
      Event event = new Event();
      dbAttendant.getDbEvent().copyTo(event);
      attendantView.setEvent(event);
      attendantViews.add(attendantView);

      if (personsData) //Prepares find persons to kernel (only if filtering by eventId)
      {
        String pId = dbAttendant.getPersonId();
        if (pId != null) //Set PersonView
        {
          List list = personIdMap.get(pId);
          if (list == null) list = new ArrayList();
          list.add(attendantView);
          personIdMap.put(pId, list);
        }
      }
    }

    if (personsData) //Only find persons if filtering by eventId
    {
      //Invokes kernel WS findPersons
      try
      {
        KernelManagerPort port = getKernelManagerPort();

        PersonFilter personFilter = new PersonFilter();
        personFilter.getPersonId().addAll(personIdMap.keySet());
        personFilter.setFirstResult(0);
        personFilter.setMaxResults(personIdMap.size());
        List<PersonView> personViewList = port.findPersonViews(personFilter);
        for (PersonView personView : personViewList)
        {
          List<AttendantView> attendantList = personIdMap.get(personView.getPersonId());
          for (AttendantView attendantView : attendantList)
          {
            attendantView.setPersonView(personView);
          }
        }
      }
      catch (Exception e)
      {
        throw new WebServiceException(e);
      }
    }

    return attendantViews;
  }

  private String getUserId()
  {
    try
    {
      User user = UserCache.getUser(wsContext);
      return user.getUserId();
    }
    catch (Exception ex)
    {
      throw new WebServiceException(ex);
    }
  }

  private String getDateString(Date date)
  {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    return sdf.format(date);
  }

  private String getTimeString(Date date)
  {
    SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
    return sdf.format(date);
  }

  private void auditCreation(DBEntityBase base)
  {
    String userId = getUserId();
    base.setStdugr(userId);
    base.setStdumod(userId);

    Date now = new Date();
    String nowDate = getDateString(now);
    String nowTime = getTimeString(now);

    base.setStddgr(nowDate);
    base.setStdhgr(nowTime);
    base.setStddmod(nowDate);
    base.setStdhmod(nowTime);
  }

  private void auditEventChange(EntityManager entityManager, String eventId)
  {
    String userId = getUserId();
    Date now = new Date();
    String nowDate = getDateString(now);
    String nowTime = getTimeString(now);

    Query query = entityManager.createNamedQuery("auditEventChange");
    query.setParameter("changeUserId", userId);
    query.setParameter("stddmod", nowDate);
    query.setParameter("stdhmod", nowTime);
    query.setParameter("eventId", eventId);
    query.executeUpdate();
  }

  private boolean isEventHidden(Event event)
  {
    return (HIDDEN_EVENT_STRING.equals(event.getSummary()) &&
      HIDDEN_EVENT_STRING.equals(event.getDescription()));
  }

  private boolean isEventViewHidden(EventView eventView)
  {
    return (HIDDEN_EVENT_STRING.equals(eventView.getSummary()) &&
      HIDDEN_EVENT_STRING.equals(eventView.getDescription()));
  }
  
  private void validateEventFilter(EventFilter filter) throws Exception
  {
    if (filter.getEventId().isEmpty() &&
        filter.getEventTypeId().isEmpty() &&
        filter.getProperty().isEmpty() &&
        StringUtils.isBlank(filter.getContent()) &&
        StringUtils.isBlank(filter.getStartDateTime()) &&
        StringUtils.isBlank(filter.getEndDateTime()) && 
        StringUtils.isBlank(filter.getStartChangeDateTime()) &&
        StringUtils.isBlank(filter.getEndChangeDateTime()) &&       
        StringUtils.isBlank(filter.getPersonId()) && 
        StringUtils.isBlank(filter.getRoomId()) &&
        filter.getMaxResults() == 0)
      throw new Exception("FILTER_NOT_ALLOWED");    
  }
  

  private void initEventIdCounter()
  {
    String initValue = 
      MatrixConfig.getClassProperty(getClass(), "eventIdInitialValue");
    if (initValue != null)
    {     
      Query query = entityManager.createNamedQuery("initEventIdSequence");
      query.setParameter("initValue", initValue);
      query.executeUpdate();
      entityManager.flush();
      LOGGER.log(Level.INFO, "Event id sequence counter updated to value: {0}", 
        initValue);
    }    
  }
}
