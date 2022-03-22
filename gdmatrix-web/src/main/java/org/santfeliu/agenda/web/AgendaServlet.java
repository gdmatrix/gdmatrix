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

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyFactory;
import net.fortuna.ical4j.model.PropertyFactoryImpl;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.LastModified;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import org.matrix.agenda.AgendaConstants;
import org.matrix.agenda.Attendant;
import org.matrix.agenda.AttendantView;
import org.matrix.agenda.Event;
import org.matrix.agenda.EventFilter;
import org.matrix.agenda.EventPlaceView;
import org.matrix.agenda.EventView;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.AddressView;
import org.matrix.kernel.RoomView;
import org.matrix.security.SecurityConstants;
import org.matrix.security.User;
import org.santfeliu.agenda.client.AgendaManagerClient;
import org.santfeliu.security.UserCache;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.security.util.SecurityUtils;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author unknown
 */
public class AgendaServlet extends HttpServlet
{
  static final int MAX_EVENTS = 500;
  static final Logger logger = Logger.getLogger("AgendaServlet");

  private HashMap<String, Requestor> requestors = new HashMap();

  public AgendaServlet()
  {
  }

  @Override
  public void init(ServletConfig config)
  {
    logger.log(Level.INFO, "Initializing AgendaServlet...");
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    logger.log(Level.INFO, "Parameters: {0}", request.getQueryString());
    long now = System.currentTimeMillis();

    Credentials credentials = getCredentials(request);
    String userId = credentials.getUserId();
    String startChangeDateTime = toLocalDateTime(request.getParameter(
      AgendaConstants.STARTCHANGEDATETIME));
    String endChangeDateTime = toLocalDateTime(request.getParameter(
      AgendaConstants.ENDCHANGEDATETIME));
    String clear = request.getParameter("clear");

    if (startChangeDateTime == null)
    {
      if (clear != null) requestors.clear();
      writeRequestorInfo(response);
      return;
    }

    // normal processing: get event list in ical format
    List<EventView> events;
    try
    {
      getRequestor(userId).registerRequest(request);
      String personId = request.getParameter(AgendaConstants.PERSONID);
      if (personId == null)
      {
        User user = UserCache.getUser(credentials);
        personId = user.getPersonId();
        if (personId == null)
        {
          logger.log(Level.SEVERE, "personId is mandatory");
          response.sendError(HttpServletResponse.SC_BAD_REQUEST);
          return;
        }
      }
      events = executeFindEvents(personId,
        startChangeDateTime, endChangeDateTime, credentials);
    }
    catch (Exception ex)
    {
      getRequestor(userId).registerError(ex.toString());
      logger.log(Level.SEVERE, ex.toString());
      sendError(response, ex);
      return;
    }

    // convert and write events to response
    logger.log(Level.INFO, "Events found: {0}", events.size());
    if (events.isEmpty())
    {
      // return empty calendar
      response.setContentType("text/calendar");
      response.setCharacterEncoding("UTF-8");
      response.setDateHeader("Last-Modified", now);
      getRequestor(userId).registerRead(0);
    }
    else
    {
      try
      {
        writeICal4jResponse(events, now, response);
        getRequestor(userId).registerRead(events.size());
      }
      catch (IOException ex)
      {
        getRequestor(userId).registerError(ex.toString());
        logger.log(Level.SEVERE, ex.toString());
        throw ex;
      }
      catch (Exception ex)
      {
        getRequestor(userId).registerError(ex.toString());
        logger.log(Level.SEVERE, ex.toString());
        throw new IOException(ex);
      }
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    Credentials credentials = getCredentials(request);
    String userId = credentials.getUserId();

    if (SecurityConstants.ANONYMOUS.equals(credentials.getUserId()))
    {
      logger.log(Level.SEVERE, "Anonymous access");
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    try
    {
      String action = request.getParameter("action");
      if ("store".equals(action))
      {
        // store new event
        getRequestor(userId).registerStore();
        String uid = request.getParameter("UID");
        String summary = request.getParameter("SUMMARY");
        String comments = request.getParameter("DESCRIPTION");
        String startDateTime = toLocalDateTime(request.getParameter("DTSTART"));
        String endDateTime = toLocalDateTime(request.getParameter("DTEND"));
        uid = storeEvent(uid, summary, comments,
          startDateTime, endDateTime, credentials);
        // return event uid
        writeUid(response, uid);
      }
      else if ("remove".equals(action))
      {
        getRequestor(userId).registerRemove();
        String uid = request.getParameter("UID");
        logger.log(Level.INFO, "Removing event: uid={0}, userId={1}",
          new Object[]{uid, userId});
        // return event uid, but remove nothing
        writeUid(response, uid);
      }
    }
    catch (IOException ex)
    {
      getRequestor(userId).registerError(ex.toString());
      throw ex;
    }
    catch (Exception ex)
    {
      getRequestor(userId).registerError(ex.toString());
      logger.log(Level.SEVERE, ex.toString());
      sendError(response, ex);
    }
  }

  private String storeEvent(String uid, String summary, String description,
    String startDateTime, String endDateTime, Credentials credentials)
    throws Exception
  {
    boolean isNewEvent = (uid == null);
    String eventId = null;
    if (uid != null)
    {
      int index = uid.indexOf("@");
      eventId = (index == -1) ? uid : uid.substring(0, index);
    }    
    logger.log(Level.INFO, "Storing event: eventId={0}, summary={1}, "
      + "startDateTime={2}, endDateTime={3}, description={4}",
      new Object[]{eventId, summary, startDateTime, endDateTime, description});
    // call ws
    AgendaManagerClient port = getAgendaManagerPort(credentials);
    Event event;
    if (isNewEvent)
    {
      event = new Event();
      event.setEventTypeId(DictionaryConstants.EVENT_TYPE);
      event.setOnlyAttendants(true);
    }
    else
    {
      event = port.loadEventFromCache(eventId);
    }
    event.setSummary(summary);
    event.setDescription(description);
    event.setStartDateTime(startDateTime);
    event.setEndDateTime(endDateTime);    
    event = port.storeEvent(event);
    eventId = event.getEventId();
    if (isNewEvent)
    {
      Attendant attendant = new Attendant();
      attendant.setEventId(eventId);
      attendant.setPersonId(UserCache.getUser(credentials).getPersonId());
      attendant.setAttendantTypeId(DictionaryConstants.ATTENDANT_TYPE);
      port.storeAttendant(attendant);
    }    
    return eventId + "@" + getEventUidSuffix();
  }

  private List<EventView> executeFindEvents(String personId,
    String startChangeDateTime, String endChangeDateTime,
    Credentials credentials) throws Exception
  {
    AgendaManagerClient port = getAgendaManagerPort(credentials);
    EventFilter filter = new EventFilter();
    filter.setPersonId(personId);
    filter.setStartChangeDateTime(startChangeDateTime);
    filter.setEndChangeDateTime(endChangeDateTime);
    filter.setMaxResults(MAX_EVENTS);
    filter.setReducedInfo(Boolean.FALSE);
    List<EventView> events = port.findEventViewsFromCache(filter);
    return events;
  }

  private void writeUid(HttpServletResponse response, String uid)
    throws IOException
  {
    response.setContentType("text/plain");
    response.setCharacterEncoding("UTF-8");
    response.getWriter().print(uid);
  }

  private void writeICal4jResponse(List<EventView> eventViewList, long now,
    HttpServletResponse response) throws Exception
  {
    // set response header fields
    response.setContentType("text/calendar");
    response.setCharacterEncoding("UTF-8");
    response.setDateHeader("Last-Modified", now);

    // export to ical
    Calendar calendar = new Calendar();
    calendar.getProperties().add(Version.VERSION_2_0);
    calendar.getProperties().add(
      new ProdId("-//Agenda " + getEventUidSuffix() + "//iCal4j 1.0//CA"));
    calendar.getProperties().add(Method.PUBLISH);
    for (EventView eventView : eventViewList)
    {
      try
      {
        VEvent vEvent = createVEvent(eventView);
        calendar.getComponents().add(vEvent);
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
        logger.log(Level.SEVERE, ex.toString());
      }
    }
    CalendarOutputter outputter = new CalendarOutputter();
    PrintWriter writer = response.getWriter();
    try
    {
      outputter.output(calendar, writer);
    }
    catch (Exception ex)
    {
      throw ex;
    }
    finally
    {
      writer.close();
    }
  }

  private void writeRequestorInfo(HttpServletResponse response)
    throws IOException
  {
    response.setContentType("text/html");
    PrintWriter writer = response.getWriter();
    try
    {
      SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
      writer.println("<html>");
      writer.println("<head><title>AgendaServlet</title></head>");
      writer.println("<body>");
      writer.println("<h3>AgendaServlet</h3>");
      for (Requestor requestor : requestors.values())
      {
        writer.println("<p>");
        writer.println("<b>User: " + requestor.userId + "</b>");
        writer.println("<ul>");
        writer.println("<li>User agent: " + requestor.userAgent + "</li>");
        writer.println("<li>Remote IP address: " + requestor.remoteIp + "</li>");
        writer.println("<li>Protocol: " + requestor.protocol + "</li>");
        writer.println("<li>lastRequestTime: " +
           df.format(new Date(requestor.lastRequestTime)) + "</li>");
        writer.println("<li>requestCount: " + requestor.requestCount + "</li>");
        writer.println("<li>readCount: " + requestor.readCount + "</li>");
        writer.println("<li>lastReadCount: " + requestor.lastReadCount + "</li>");
        writer.println("<li>interval: " + requestor.interval + " min.</li>");
        writer.println("<li>storeCount: " + requestor.storeCount + "</li>");
        writer.println("<li>removeCount: " + requestor.removeCount + "</li>");
        writer.println("<li>errorCount: " + requestor.errorCount + "</li>");
        if (requestor.lastError != null)
        {
          writer.println("<li>lastErrorTime: " +
            df.format(new Date(requestor.lastErrorTime)) + "</li>");
          writer.println("<li>lastError: " + requestor.lastError + "</li>");
        }
        writer.println("</ul>");
        writer.println("</p>");
      }
      writer.println("</body>");
      writer.println("</html>");
    }
    finally
    {
      writer.close();
    }
  }

  private String getEventUidSuffix()
  {
    return MatrixConfig.getClassProperty(getClass(), "eventUidSuffix");
  }

  private AgendaManagerClient getAgendaManagerPort(Credentials credentials)
    throws Exception
  {
    return AgendaConfigBean.getPort(credentials.getUserId(), 
      credentials.getPassword());
  }

  private VEvent createVEvent(EventView eventView) throws Exception
  {
    PropertyFactory pf = PropertyFactoryImpl.getInstance();

    // set event uid
    VEvent vEvent = new VEvent();
    String uid = eventView.getEventId() + "@" + getEventUidSuffix();
    vEvent.getProperties().add(new Uid(uid));

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    // set event startDateTime
    Date date = dateFormat.parse(eventView.getStartDateTime());
    DateTime startDateTime = new DateTime(date.getTime());
    startDateTime.setUtc(true);
    vEvent.getProperties().add(new DtStart(startDateTime));

    // set event endDateTime
    date = dateFormat.parse(eventView.getEndDateTime());
    DateTime endDateTime = new DateTime(date.getTime());
    endDateTime.setUtc(true);
    vEvent.getProperties().add(new DtEnd(endDateTime));

    // set event lastModifiedTime
    String changeDateTime = eventView.getChangeDateTime();
    date = dateFormat.parse(changeDateTime);
    DateTime lastModifiedDateTime = new DateTime(date.getTime());
    lastModifiedDateTime.setUtc(true);
    vEvent.getProperties().add(new LastModified(lastModifiedDateTime));

    // set event type
    String eventTypeName = eventView.getEventTypeName();
    Property eventType = pf.createProperty("X-MATRIX-EVENT-TYPE");
    eventType.setValue(eventTypeName);
    vEvent.getProperties().add(eventType);

    // set event summary
    String eventSummary = eventView.getSummary();
    vEvent.getProperties().add(new Summary(eventSummary));

    // set event description
    String description = eventView.getDescription();
    if (description != null)
    {
      vEvent.getProperties().add(new Description(description));
    }

    // set event buffer
    if (!eventView.getAttendants().isEmpty())
    {
      StringBuilder buffer = new StringBuilder();
      for (AttendantView eventAttendant : eventView.getAttendants())
      {
        if (buffer.length() > 0) buffer.append("\n");
        buffer.append(eventAttendant.getPersonView().getFullName());
      }
      Property attendants = pf.createProperty("X-MATRIX-ATTENDANTS");
      attendants.setValue(buffer.toString());
      vEvent.getProperties().add(attendants);
    }

    // set event organizer
    vEvent.getProperties().add(new Organizer());

    // set event locations
    StringBuilder sbLocation = new StringBuilder();
    if (!eventView.getPlaces().isEmpty())
    {
      Iterator<EventPlaceView> iter = eventView.getPlaces().iterator();
      EventPlaceView place = iter.next();
      sbLocation.append(placeToString(place));
      while (iter.hasNext())
      {
        sbLocation.append("\n");
        place = iter.next();
        sbLocation.append(placeToString(place));
      }
      vEvent.getProperties().add(new Location(sbLocation.toString()));
    }
    return vEvent;
  }

  private String toLocalDateTime(String dateTime)
  {
    if (dateTime == null) return null;
    if (dateTime.length() == 14)
      return dateTime; // already local: yyyyMMddHHmmss
    if (dateTime.length() == 16) // yyyyMMddTHHmmssZ
    {
      String year = dateTime.substring(0, 4);
      String month = dateTime.substring(4, 6);
      String day = dateTime.substring(6, 8);
      String hour = dateTime.substring(9, 11);
      String minute = dateTime.substring(11, 13);
      String second = dateTime.substring(13, 15);

      java.util.Calendar cal =
         java.util.Calendar.getInstance(TimeZone.getTimeZone("GMT"));
      cal.set(java.util.Calendar.YEAR, Integer.parseInt(year));
      cal.set(java.util.Calendar.MONTH, Integer.parseInt(month) - 1);
      cal.set(java.util.Calendar.DAY_OF_MONTH, Integer.parseInt(day));
      cal.set(java.util.Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
      cal.set(java.util.Calendar.MINUTE, Integer.parseInt(minute));
      cal.set(java.util.Calendar.SECOND, Integer.parseInt(second));
      cal.set(java.util.Calendar.MILLISECOND, 0);
      Date date = cal.getTime();
      SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
      return df.format(date);
    }
    return null;
  }

  private String placeToString(EventPlaceView place)
  {
    StringBuilder buffer = new StringBuilder();
    RoomView roomView = place.getRoomView();
    if (roomView != null)
    {
      String roomDescription = roomView.getDescription();
      if (roomDescription != null) buffer.append(roomDescription);
    }
    AddressView addressView = place.getAddressView();
    if (addressView != null)
    {
      String description = addressView.getDescription();      
      if (description != null)
      {
        if (buffer.length() > 0) buffer.append(", ");
        buffer.append(description);
      }
    }
    if (place.getComments() != null)
    {
      if (buffer.length() > 0) buffer.append(", ");
      buffer.append(place.getComments());
    }

    return buffer.toString();
  }

  private Credentials getCredentials(HttpServletRequest request)
  {
    Credentials credentials = SecurityUtils.getCredentials(request, false);
    if (credentials == null)
    {
      credentials = UserSessionBean.getCredentials(request);
    }
    return credentials;
  }

  private void sendError(HttpServletResponse response, Exception ex)
    throws IOException
  {
    String message = ex.toString();
    if (message.indexOf("ACTION_DENIED") != -1)
    {
      response.sendError(HttpServletResponse.SC_FORBIDDEN,
        ex.toString());
    }
    else if (message.indexOf("INVALID_IDENTIFICATION") != -1)
    {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
        ex.toString());
    }
    else if (message.indexOf("INVALID_USERNAME") != -1)
    {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
        ex.toString());
    }
    else if (message.indexOf("INVALID_PASSWORD") != -1)
    {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
        ex.toString());
    }
    else
    {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        ex.toString());
    }
  }

  private Requestor getRequestor(String userId)
  {
    Requestor requestor = requestors.get(userId);
    if (requestor == null)
    {
      requestor = new Requestor();
      requestor.userId = userId;
      requestors.put(userId, requestor);
    }
    return requestor;
  }

  class Requestor
  {
    String userId;
    String userAgent;
    String remoteIp;
    String protocol;
    long lastRequestTime;
    int readCount;
    int lastReadCount;
    int requestCount;
    int storeCount;
    int removeCount;
    int errorCount;
    long lastErrorTime;
    long interval;
    String lastError;

    void registerRequest(HttpServletRequest request)
    {
      long now = System.currentTimeMillis();
      if (lastRequestTime > 0)
        interval = (now - lastRequestTime) / 60000; // minutes
      lastRequestTime = now;
      userAgent = request.getHeader("User-Agent");
      remoteIp = request.getRemoteAddr();
      protocol = request.isSecure() ? "HTTPS" : "HTTP";
      requestCount++;
    }

    void registerRead(int count)
    {
      lastReadCount = count;
      readCount += count;
    }

    void registerStore()
    {
      storeCount++;
    }

    void registerRemove()
    {
      removeCount++;
    }

    void registerError(String message)
    {
      lastError = message;
      lastErrorTime = System.currentTimeMillis();
      errorCount++;
    }
  }
}
