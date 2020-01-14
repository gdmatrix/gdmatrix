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

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.matrix.agenda.AgendaConstants;
import org.matrix.agenda.Event;
import org.matrix.agenda.SecurityMode;

import org.matrix.agenda.OrderByProperty;
import org.matrix.dic.Property;
import org.matrix.security.SecurityConstants;

/**
 *
 * @author blanquepa
 */
public class DefaultFindEventsQueryBuilder extends FindEventsQueryBuilder
{
  private boolean isLoadQuery;
  
  @Override
  public Query getQuery(EntityManager em) throws Exception
  {
    isLoadQuery = false;
    return createQuery(em);
  }
  
  public Event getEvent(EntityManager entityManager)
    throws Exception
  {
    Event event = null;

    isLoadQuery = true;
    Query query = createQuery(entityManager);
    Object[] eventArray = ((Object[])query.getSingleResult());
    if (eventArray != null)
    {
      event = new Event();
      copy(eventArray, event);
    }
    return event;
  }

  public List<Event> getEventList(EntityManager entityManager)
    throws Exception
  {
    List<Event> events = new ArrayList();

    isLoadQuery = false;
    Query query = createQuery(entityManager);
    List<Object[]> eventArrayList = query.getResultList();

    for (Object[] eventArray : eventArrayList)
    {
      Event event = new Event();
      copy(eventArray, event);
      events.add(event);
    }

    return events;
  }

  public int getEventCount(EntityManager entityManager)
    throws Exception
  {
    isLoadQuery = false;
    Query query = createQuery(entityManager);
    return ((Number)query.getSingleResult()).intValue();
  }
  
  private Query createQuery(EntityManager em)
    throws Exception
  {
    StringBuilder buffer = new StringBuilder();
    StringBuilder selectBuffer = new StringBuilder();
    StringBuilder fromBuffer = new StringBuilder();
    StringBuilder whereBuffer = new StringBuilder();

    appendMainStatement(selectBuffer, fromBuffer);
    if (!SecurityMode.HIDDEN.equals(filter.getSecurityMode()))
      appendSecurity(whereBuffer);
    appendEventIdFilter(whereBuffer);
    appendPersonIdFilter(whereBuffer);
    appendRoomIdFilter(whereBuffer);
    appendContentFilter(whereBuffer);
    appendEventDatesFilter(whereBuffer);
    appendChangeDatesFilter(whereBuffer);
    appendEventTypeIdFilter(fromBuffer, whereBuffer);
    appendThemeIdFilter(whereBuffer);
    appendPropertiesFilter(whereBuffer);

    buffer.append(selectBuffer);
    buffer.append(" ");
    buffer.append(fromBuffer);
    buffer.append(" ");
    buffer.append(whereBuffer);
      
    appendOrderBy(buffer);

    Query query = em.createQuery(buffer.toString());
    setParameters(query);

    if (!isCounterQuery())
    {
      query.setFirstResult(filter.getFirstResult());
      int maxResults = filter.getMaxResults();
      if (maxResults > 0) query.setMaxResults(maxResults);
    }
    else
    {
      query.setFirstResult(0);
      query.setMaxResults(1);
    }

    return query;
  }  

  private void appendMainStatement(StringBuilder selectBuffer,
    StringBuilder fromBuffer)
  {
    if (isCounterQuery())
    {
      selectBuffer.append("SELECT count(e)");
      fromBuffer.append("FROM DBEvent e");
    }
    else
    {
      selectBuffer.append("SELECT ");
      if (!isLoadQuery())
        selectBuffer.append("DISTINCT ");
      selectBuffer.append("e.eventId,e.eventTypeId,e.tipesdevcod," +
        "e.summary,e.description,e.comments,e.datainici,e.horainici,e.datafinal," +
        "e.horafinal,e.stddgr,e.stdhgr,e.creationUserId,e.stddmod,e.stdhmod," +
        "e.changeUserId,e.visibleassist");
      if (isLoadQuery())
        selectBuffer.append(",e.text");
      fromBuffer.append("FROM DBEvent e");
    }
  }
  
  private void appendSecurity(StringBuilder whereBuffer)
  {
    List roles = user.getRolesList();
    String userId = user.getUserId();
    String userPersonId = user.getPersonId();
    if (roles.isEmpty())
        roles.add(SecurityConstants.EVERYONE_ROLE);

    if (!roles.contains(AgendaConstants.AGENDA_ADMIN_ROLE))
    {
      appendOperator(whereBuffer, "AND");

      whereBuffer.append("(");

      //1. User is event creator or modifier
      whereBuffer.append("(trim(e.changeUserId) = :userId)");
      appendOperator(whereBuffer, "OR");
      whereBuffer.append("(trim(e.creationUserId) = :userId)");
      parameters.put("userId", userId);

      //2. User is an attendant or a confidant of any attendant
      appendOperator(whereBuffer, "OR");
      whereBuffer.append("(EXISTS (SELECT at.eventId FROM DBAttendant at " +
                    "WHERE at.eventId = e.eventId " +
                    " AND (at.personId = :userPersonId ");
      if (trustors != null && trustors.size() > 0)
      {
        appendOperator(whereBuffer, "OR");
        appendInOperator(whereBuffer, "at.personId", ":", "trustorPersonId", trustors);
      }
      whereBuffer.append(")))");
      parameters.put("userPersonId", userPersonId);

      //3. User has read roles
      appendOperator(whereBuffer, "OR");
      appendRolesFilter(whereBuffer);

      whereBuffer.append(")");
    }
  }

  private void appendRolesFilter(StringBuilder whereBuffer)
  {
    whereBuffer.append(
      "(e.visibleassist != 'S' and (exists (select tacl.typeId from AccessControl tacl where " +
      "upper(tacl.typeId) = upper(e.eventTypeId) and tacl.action = 'Read' and ");
    appendInOperator(whereBuffer, "tacl.roleId", ":", "userRole", user.getRolesList());
    whereBuffer.append(")");
    whereBuffer.append("))");
  }

  private void appendEventIdFilter(StringBuilder whereBuffer)
  {
    List<String> eventIds = filter.getEventId();
    if (eventIds != null && eventIds.size() > 0)
    {
      appendOperator(whereBuffer, "AND");
      whereBuffer.append("(");
      for (int i = 0; i < eventIds.size(); i++)
      {
        if (i != 0) whereBuffer.append(" OR ");
        whereBuffer.append("e.eventId=:eventId" + i);
        parameters.put("eventId" + i, eventIds.get(i));
      }
      whereBuffer.append(")");
    }
  }

  private void appendContentFilter(StringBuilder whereBuffer)
  {
    String content = filter.getContent();
    if (content != null && content.length() > 0)
    {
      appendOperator(whereBuffer, "AND");
      whereBuffer.append("(");
      whereBuffer.append("lower(e.description) like :content");
      appendOperator(whereBuffer, "OR");
      whereBuffer.append("lower(e.summary) like :content");
      whereBuffer.append(")");
      parameters.put("content", addPercent(content));
    }
  }

  private void appendEventDatesFilter(StringBuilder whereBuffer)
  {
    String startDateTime = filter.getStartDateTime();
    String endDateTime = filter.getEndDateTime();
    String comparator = filter.getDateComparator();
    if (AgendaConstants.END_DATE_COMPARATOR.equals(comparator)) //data tancament
    {
      if (startDateTime != null && startDateTime.length() > 0)
      {
        appendOperator(whereBuffer, "AND");
        whereBuffer.append("concat(e.datafinal, e.horafinal) >= :startDateTime");
        parameters.put("startDateTime", startDateTime);
      }

      if (endDateTime != null && endDateTime.length() > 0)
      {
        appendOperator(whereBuffer, "AND");
        whereBuffer.append("concat(e.datafinal, e.horafinal) <= :endDateTime");
        parameters.put("endDateTime", endDateTime);
      }
    }
    else if (AgendaConstants.ACTIVE_DATE_COMPARATOR.equals(comparator)) //obertes durant
    {
      if (startDateTime != null && startDateTime.length() > 0)
      {
        appendOperator(whereBuffer, "AND");
        whereBuffer.append("concat(e.datafinal, e.horafinal) >= :startDateTime");
        parameters.put("startDateTime", startDateTime);
      }

      if (endDateTime != null && endDateTime.length() > 0)
      {
        appendOperator(whereBuffer, "AND");
        whereBuffer.append("concat(e.datainici, e.horainici) <= :endDateTime");
        parameters.put("endDateTime", endDateTime);
      }
    }
    else
    {
      if (startDateTime != null && startDateTime.length() > 0)
      {
        appendOperator(whereBuffer, "AND");
        whereBuffer.append("concat(e.datainici, e.horainici) >= :startDateTime");
        parameters.put("startDateTime", startDateTime);
      }

      if (endDateTime != null && endDateTime.length() > 0)
      {
        appendOperator(whereBuffer, "AND");
        whereBuffer.append("concat(e.datainici, e.horainici) <= :endDateTime");
        parameters.put("endDateTime", endDateTime);
      }
    }


  }

  private void appendChangeDatesFilter(StringBuilder whereBuffer)
  {
    String startChangeDateTime = filter.getStartChangeDateTime();
    if (startChangeDateTime != null && startChangeDateTime.length() > 0)
    {
      appendOperator(whereBuffer, "AND");
      whereBuffer.append("concat(e.stddmod, e.stdhmod) >= :startChangeDateTime");
      parameters.put("startChangeDateTime", startChangeDateTime);
    }

    String endChangeDateTime = filter.getEndChangeDateTime();
    if (endChangeDateTime != null && endChangeDateTime.length() > 0)
    {
      appendOperator(whereBuffer, "AND");
      whereBuffer.append("concat(e.stddmod, e.stdhmod) <= :endChangeDateTime");
      parameters.put("endChangeDateTime", endChangeDateTime);
    }

  }

  private void appendEventTypeIdFilter(StringBuilder fromBuffer,
    StringBuilder whereBuffer)
  {
    List<String> eventTypeIdList = filter.getEventTypeId();
    if (eventTypeIdList.size() > 0 &&
      eventTypeIdList.get(0) != null && eventTypeIdList.get(0).length() > 0)
    {
//      fromBuffer.append(" LEFT JOIN e.eventType t");
      fromBuffer.append(", Type t");

      appendOperator(whereBuffer, "AND");
      whereBuffer.append("e.eventTypeId = t.typeId");
      
      appendOperator(whereBuffer, "AND");
      whereBuffer.append("(");
      for (int i = 0; i < eventTypeIdList.size(); i++)
      {
        if (i != 0) whereBuffer.append(" OR ");
        whereBuffer.append("t.typePath like :eventTypeId" + i);

        String eventTypeId = eventTypeIdList.get(i);
        if (eventTypeId != null && eventTypeId.trim().length() > 0)
          eventTypeId = "%/" + eventTypeId + "/%";
        else eventTypeId = null;
        parameters.put("eventTypeId" + i, eventTypeId);
      }
      whereBuffer.append(")");
    }
  }

  private void appendThemeIdFilter(StringBuilder whereBuffer)
  {
    List<String> themeIds = filter.getThemeId();
    if (themeIds != null && themeIds.size() > 0)
    {
      appendOperator(whereBuffer, "AND");
      whereBuffer.append("exists (");
      whereBuffer.append("SELECT et.eventId FROM DBEventTheme et WHERE e.eventId = et.eventId");
      appendOperator(whereBuffer, "AND");
      appendInOperator(whereBuffer, "et.themeId", ":", "themeId", themeIds);
      whereBuffer.append(")");
    }
  }

  private void appendPersonIdFilter(StringBuilder whereBuffer)
  {
    String personId = filter.getPersonId();
    if (personId != null && personId.length() > 0)
    {
      appendOperator(whereBuffer, "AND");
      whereBuffer.append("(");
      String[] personIdArray = null;
      if (personId != null)
      {
        personIdArray = personId.split(AgendaManager.PK_SEPARATOR);
        personId = personIdArray[0];
      }
      
      whereBuffer.append("EXISTS (SELECT attendants.personId FROM DBAttendant attendants WHERE attendants.eventId = e.eventId and attendants.personId = :personId)");
      whereBuffer.append(")");
      parameters.put("personId", personId);
    }
  }

  private void appendRoomIdFilter(StringBuilder whereBuffer)
  {
    String roomId = filter.getRoomId();
    if (roomId != null && roomId.length() > 0)
    {
      appendOperator(whereBuffer, "AND");
      whereBuffer.append("(");

      String[] roomIdArray = null;
      if (roomId != null)
        roomIdArray = roomId.split(AgendaManager.PK_SEPARATOR);
      if (roomIdArray.length > 1)
      {
        whereBuffer.append("EXISTS (SELECT ep.salacod FROM DBEventPlace ep WHERE ep.eventId = e.eventId and ep.domcod = :domcod AND ep.salacod = :salacod)");
        parameters.put("domcod", roomId != null ? roomIdArray[0] : null);
        parameters.put("salacod", roomId != null ? roomIdArray[1] : null);
      }
      else if (roomIdArray.length == 1)
      {
        whereBuffer.append("EXISTS (SELECT ep.salacod FROM DBEventPlace ep WHERE ep.eventId = e.eventId and ep.domcod = :domcod)");
        parameters.put("domcod", roomId != null ? roomIdArray[0] : null);
      }
      whereBuffer.append(")");
    }
  }


  private void appendPropertiesFilter(StringBuilder whereBuffer)
  {
    List<Property> properties = filter.getProperty();
    for (int i = 0; i < properties.size(); i++)
    {
      Property p = properties.get(i);
      if (p.getName() != null && p.getName().length() > 0)
        appendPropertyFilter(whereBuffer, p.getName(), p.getValue(), "p" + i);
    }
  }

  private void appendPropertyFilter(StringBuilder whereBuffer, String name,
    List<String> values, String tablePrefix)
  {
    appendOperator(whereBuffer, "AND");

    StringBuilder aux = new StringBuilder();

    aux.append(
      "EXISTS (SELECT #.value FROM DBEventProperty # WHERE " +
      "e.eventId = #.eventId AND #.name = '" + name + "'");

    if (values != null && (values.size() > 1 || (values.size() == 1 && values.get(0).length() > 0)))
    {
      appendOperator(aux, "AND");
      appendLikeOperator(aux, "#.value", ":", name, values);
    }
    aux.append(")");

    String sAux = aux.toString();
    whereBuffer.append(sAux.replaceAll("#", tablePrefix));
  }

  private void appendOrderBy(StringBuilder buffer)
  {
    buffer.append(" ORDER BY ");

    List<OrderByProperty> orderBy = filter.getOrderBy();
    if (orderBy != null && orderBy.size() > 0)
    {
      for (int i = 0; i < orderBy.size(); i++)
      {
        if (i != 0) buffer.append(",");
        buffer.append(toSortColumn(orderBy.get(i)));
      }
    }
    else
      buffer.append("e.datainici, e.horainici, e.datafinal, e.horafinal, e.eventId");
  }
  
  private boolean isLoadQuery()
  {
    return isLoadQuery;
  }

  private String toSortColumn(OrderByProperty property)
  {
    String name = property.getName();
    boolean descending = property.isDescending();

    for (OrderBy orderBy : OrderBy.values())
    {
      if (orderBy.equals(OrderBy.valueOf(name.toUpperCase())))
        return orderBy.getColumnName() + " " + (descending ? "desc" : "");
    }

    return name;
  }

  private void copy(Object[] eventArray, Event event)
  {
    DBEvent dbEvent = new DBEvent();
    dbEvent.setEventId((String)eventArray[0]);
    dbEvent.setEventTypeId((String)eventArray[1]);
    dbEvent.setTipesdevcod((String)eventArray[2]);
    dbEvent.setSummary((String)eventArray[3]);
    dbEvent.setDescription((String)eventArray[4]);
    dbEvent.setComments((String)eventArray[5]);
    dbEvent.setDatainici((String)eventArray[6]);
    dbEvent.setHorainici((String)eventArray[7]);
    dbEvent.setDatafinal((String)eventArray[8]);
    dbEvent.setHorafinal((String)eventArray[9]);
    dbEvent.setStddgr((String)eventArray[10]);
    dbEvent.setStdhgr((String)eventArray[11]);
    dbEvent.setCreationUserId((String)eventArray[12]);
    dbEvent.setStddmod((String)eventArray[13]);
    dbEvent.setStdhmod((String)eventArray[14]);
    dbEvent.setChangeUserId((String)eventArray[15]);
    dbEvent.setVisibleassist((String)eventArray[16]);
    if (isLoadQuery)
      dbEvent.setText((byte[])eventArray[17]); //BLOB

    dbEvent.copyTo(event);
  }


  enum OrderBy
  {
    EVENTID ("e.eventId"),
    DESCRIPTION ("e.description"),
    SUMMARY ("e.summary"),
    STARTDATETIME ("concat(e.datainici, e.horainici)"),
    ENDDATETIME ("concat(e.datafinal, e.horafinal)"),
    CREATIONDATETIME ("concat(e.stddgr, e.stdhgr)"),
    CHANGEDATETIME ("concat(e.stddmod, e.stdhmod)"),

    @Deprecated
    INITDATE ("concat(e.datainici, e.horainici)"), //Deprecated agenda v1.0 compatibility
    @Deprecated
    ENDDATE ("concat(e.datafinal, e.horafinal)"); //Deprecated agenda v1.0 compatibility
      
    private String columnName;

    OrderBy(String columnName)
    {
      this.columnName = columnName;
    }

    public String getColumnName()
    {
      return columnName;
    }
  }
}
