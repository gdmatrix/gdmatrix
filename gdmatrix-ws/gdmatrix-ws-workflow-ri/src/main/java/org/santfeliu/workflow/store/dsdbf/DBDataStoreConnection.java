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
package org.santfeliu.workflow.store.dsdbf;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.matrix.workflow.InstanceEvent;
import org.matrix.workflow.InstanceFilter;
import org.matrix.workflow.VariableChange;
import org.matrix.workflow.VariableFilter;
import org.matrix.workflow.WorkflowConstants;
import org.santfeliu.dbf.DBConnection;
import org.santfeliu.dbf.DBKey;
import org.santfeliu.dbf.util.DBUtils;
import org.santfeliu.util.Table;
import org.santfeliu.util.TextUtils;
import org.santfeliu.workflow.ValueChanges;
import org.santfeliu.workflow.WorkflowEvent;
import org.santfeliu.workflow.WorkflowException;
import org.santfeliu.workflow.store.DataStoreConnection;


/**
 *
 * @author realor
 */
public class DBDataStoreConnection implements DataStoreConnection
{
  private final String SEQUENCE_TABLE_NAME = "TABLESEQ";
  private final String INSTANCE_SEQUENCE_NAME = "workflow.instance";
  private final String SEQUENCE_COLUMN_NAME = "value";

  private final DBConnection conn;
  private final HashMap parameters = new HashMap(10);
  private final Set lockedInstances = new HashSet();

  private final SimpleDateFormat dateFormat =
    new SimpleDateFormat("yyyyMMddHHmmss");

  public DBDataStoreConnection(DBConnection conn)
    throws Exception
  {
    this.conn = conn;
    this.conn.setAutoCommit(false);
  }

  @Override
  public String createInstance(String workflowName) throws Exception
  {
    int instanceId = DBUtils.getSequenceValue(conn, SEQUENCE_TABLE_NAME,
      new DBKey(INSTANCE_SEQUENCE_NAME), SEQUENCE_COLUMN_NAME);

    String startDateTime =  TextUtils.formatDate(new Date(), "yyyyMMddHHmmss");

    parameters.clear();
    parameters.put("instanceid", String.valueOf(instanceId));
    parameters.put("workflow", workflowName);
    parameters.put("eventcount", 0);
    parameters.put("processable", "Y");
    parameters.put("startdt", startDateTime);
    conn.insert("wfw_instance", parameters);
    return String.valueOf(instanceId);
  }

  @Override
  public boolean destroyInstance(String instanceId)
    throws Exception
  {
    parameters.clear();
    parameters.put("instanceid", instanceId);
    conn.executeUpdate(
      "delete wfw_eventvar where instanceid = {instanceid}", parameters);
    conn.executeUpdate(
      "delete wfw_event where instanceid = {instanceid}", parameters);
    conn.executeUpdate(
      "delete wfw_variable where instanceid = {instanceid}", parameters);
    conn.executeUpdate(
      "delete wfw_timer where instanceid = {instanceid}", parameters);
    int numUpdated = conn.executeUpdate(
      "delete wfw_instance where instanceid = {instanceid}", parameters);
    return numUpdated == 1;
  }

  @Override
  public String findProcessableInstance(String agentName)
    throws Exception
  {
    String instanceId = null;
    parameters.clear();
    parameters.put("agentName", agentName);
    conn.setMaxRows(0);
    Table result = conn.executeQuery(
      "select i.instanceid from wfw_instance i, wfw_variable v where " +
      "i.instanceid = v.instanceid and i.processable = 'Y' and v.name = '" +
      WorkflowConstants.AGENT_NAME + "' and v.value = {agentName}",
      parameters);

    int count = result.getRowCount();
    if (count > 0)
    {
      int index = (int)(Math.random() * count);
      Number number = (Number)result.getElementAt(index, 0);
      instanceId = String.valueOf(number.intValue());
    }
    return instanceId;
  }

  @Override
  public void setInstanceProcessable(String instanceId, boolean processable)
    throws Exception
  {
    parameters.clear();
    parameters.put("instanceid", instanceId);
    parameters.put("processable", processable ? "Y" : "N");
    int numUpdated = conn.executeUpdate(
      "update wfw_instance set processable = {processable} " +
      "where instanceid = {instanceid}", parameters);
    if (numUpdated == 0)
      throw new WorkflowException("Instance " + instanceId + " not found");
  }

  @Override
  public void lockInstance(String instanceId) throws Exception
  {
    if (!lockedInstances.contains(instanceId))
    {
      parameters.clear();
      parameters.put("instanceid", instanceId);
      int numUpdated = conn.executeUpdate(
        "update wfw_instance set instanceid = instanceid " +
        "where instanceid = {instanceid}", parameters);
      if (numUpdated == 0)
        throw new WorkflowException("Instance " + instanceId + " not found");
      lockedInstances.add(instanceId);
    }
  }

  @Override
  public void pushEvent(WorkflowEvent event) throws Exception
  {
    int eventNum = DBUtils.getSequenceValue(conn, "wfw_instance",
      new DBKey(event.getInstanceId()), "eventcount");

    Date now = new Date();
    String nowString = dateFormat.format(now);

    parameters.clear();
    parameters.put("instanceid", event.getInstanceId());
    parameters.put("eventnum", eventNum);
    parameters.put("eventdate", nowString.substring(0, 8));
    parameters.put("eventhour", nowString.substring(8));
    parameters.put("actor", event.getActorName());
    conn.insert("wfw_event", parameters);

    // insert changed variables
    ValueChanges valueChanges = event.getValueChanges();
    Set entries = valueChanges.keySet();
    Iterator iter = entries.iterator();
    while (iter.hasNext())
    {
      String name = (String)iter.next();
      Object newValue = valueChanges.getNewValue(name);
      String type = getType(newValue); // if type is null: undefined type
      if (type != null)
      {
        Object oldValue = valueChanges.getOldValue(name);
        String sNewValue = (newValue == null) ?
          null : convertValueToString(newValue);
        String sOldValue = (oldValue == null) ?
          null : convertValueToString(oldValue);
        parameters.put("name", name);
        parameters.put("type", type);
        parameters.put("newvalue", sNewValue);
        parameters.put("oldvalue", sOldValue);
        conn.insert("wfw_eventvar", parameters);
      }
    }
  }

  @Override
  public WorkflowEvent popEvent(String instanceId) throws Exception
  {
    WorkflowEvent event = null;
    parameters.clear();
    parameters.put("instanceid", instanceId);
    conn.setMaxRows(1);
    Table result = conn.executeQuery(
      "select e.eventnum, e.eventdate, e.eventhour, e.actor " +
      "from wfw_instance i, wfw_event e " +
      "where i.instanceid = {instanceid} and i.instanceid = e.instanceid " +
      "and e.eventnum = i.eventcount and i.eventcount > 1 ", parameters);

    if (result.getRowCount() > 0)
    {
      int eventNum = ((Number)result.getElementAt(0, 0)).intValue();
      String sdate = (String)result.getElementAt(0, 1);
      String shour = (String)result.getElementAt(0, 2);
      String actorName = (String)result.getElementAt(0, 3);
      Date date = dateFormat.parse(sdate + shour);
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(date);

      // read valueChanges
      ValueChanges valueChanges = new ValueChanges();

      conn.setMaxRows(0);
      parameters.clear();
      parameters.put("instanceid", instanceId);
      parameters.put("eventnum", eventNum);
      result = conn.executeQuery(
        "select name, type, newvalue, oldvalue from wfw_eventvar where " +
        "instanceid = {instanceid} and eventnum = {eventnum}", parameters);

      for (int i = 0; i < result.getRowCount(); i++)
      {
        String name = (String)result.getElementAt(i, 0);
        String type = (String)result.getElementAt(i, 1);
        String sNewValue = (String)result.getElementAt(i, 2);
        String sOldValue = (String)result.getElementAt(i, 3);
        Object newValue = convertValueToJava(sNewValue, type);
        Object oldValue = convertValueToJava(sOldValue, type);
        valueChanges.registerChange(name, oldValue, newValue);
      }

      event = new WorkflowEvent(instanceId, eventNum, calendar,
        valueChanges, actorName);

      // remove event
      parameters.clear();
      parameters.put("instanceid", instanceId);
      parameters.put("eventnum", eventNum);
      conn.executeUpdate(
        "delete wfw_eventvar where " +
        "instanceid = {instanceid} and eventnum = {eventnum}",
        parameters);
      conn.executeUpdate(
        "delete wfw_event where " +
        "instanceid = {instanceid} and eventnum = {eventnum}",
        parameters);
      // decrement eventcount
      conn.executeUpdate(
        "update wfw_instance set eventcount = eventcount - 1 where " +
        "instanceid = {instanceid}",
        parameters);
    }
    return event;
  }

  @Override
  public void loadVariables(String instanceId, Map variables)
    throws Exception
  {
    conn.setMaxRows(0);
    parameters.clear();
    parameters.put("instanceid", instanceId);
    Table result = conn.executeQuery(
      "select name, type, value from wfw_variable where " +
      "instanceid = {instanceid} ", parameters);

    if (result.isEmpty())
      throw new WorkflowException("Instance " + instanceId + " not found");

    for (int i = 0; i < result.getRowCount(); i++)
    {
      String name = (String)result.getElementAt(i, 0);
      String type = (String)result.getElementAt(i, 1);
      String svalue = (String)result.getElementAt(i, 2);
      Object value = convertValueToJava(svalue, type);
      variables.put(name, value);
    }
    // compatibility with ancient integer instanceId
    variables.put(WorkflowConstants.INSTANCE_ID, instanceId);
  }

  @Override
  public void storeVariables(String instanceId, Map variables)
    throws Exception
  {
    parameters.clear();
    parameters.put("instanceid", instanceId);

    Set entries = variables.entrySet();
    Iterator iter = entries.iterator();
    while (iter.hasNext())
    {
      Map.Entry entry = (Map.Entry)iter.next();
      String name = (String)entry.getKey();
      Object value = entry.getValue();
      String type = getType(value); // if type is null: undefined type

      if (type != null)
      {
        DBKey pk = new DBKey(instanceId, name);
        if (value == null)
        {
          conn.delete("wfw_variable", pk);
        }
        else
        {
          String svalue = convertValueToString(value);

          parameters.put("name", name);
          parameters.put("type", type);
          parameters.put("value", svalue);

          pk = conn.update("wfw_variable", pk, parameters);
          if (pk == null) // variable is new, create it.
          {
            conn.insert("wfw_variable", parameters);
          }
        }
      }
    }
  }

  @Override
  public void programTimer(String instanceId, String dateTime)
    throws Exception
  {
    Map alarm =
      conn.selectMap("wfw_timer", new DBKey(instanceId, dateTime));
    if (alarm == null)
    {
      alarm = new HashMap();
      alarm.put("instanceid", instanceId);
      alarm.put("datetime", dateTime);
      conn.insert("wfw_timer", alarm);
    }
  }

  @Override
  public void removeTimer(String instanceId, String dateTime)
    throws Exception
  {
    conn.delete("wfw_timer", new DBKey(instanceId, dateTime));
  }

  @Override
  public Table getTimers(String dateTime)
    throws Exception
  {
    Map parameters = new HashMap();
    parameters.put("datetime", dateTime);
    Table timers = conn.executeQuery(
      "select instanceid, datetime from wfw_timer " +
      "where datetime <= {datetime} order by datetime asc", parameters);
    for (int i = 0; i < timers.size(); i++)
    {
      Number id = (Number)timers.getElementAt(i, 0);
      String instanceId = String.valueOf(id.intValue());
      timers.setElementAt(i, 0, instanceId);
    }
    return timers;
  }

  @Override
  public Table findInstances(InstanceFilter filter, Set roles)
    throws Exception
  {
    parameters.clear();

    boolean innerQuery = false;
    QueryBuilder queryBuilder = new QueryBuilder();
    queryBuilder.addColumn("i.instanceid");
    queryBuilder.addTable("wfw_instance i");

    // filter by dates
    String startDate = filter.getStartDate();
    String endDate = filter.getEndDate();
    if (startDate != null || endDate != null)
    {
      if (startDate == null) startDate = "00000101";
      parameters.put("startDateTime", startDate + "000000");
      if (endDate == null) endDate = "99991231";
      parameters.put("endDateTime", endDate + "235959");

      queryBuilder.addCondition(
        "i.startdt between {startDateTime} and {endDateTime}");
      innerQuery = true;
    }

    // filter by variables
    int num = 0;
    Iterator iter = filter.getVariable().iterator();
    while (iter.hasNext())
    {
      VariableFilter variableFilter = (VariableFilter)iter.next();
      String name = variableFilter.getName();
      String value = variableFilter.getValue();
      if (name != null && value != null)
      {
        num++;
        parameters.put("name" + num, name);
        parameters.put("value" + num, value);
        queryBuilder.addTable("wfw_variable va" + num);

        queryBuilder.addCondition("va" + num + ".instanceid = i.instanceid");
        queryBuilder.addCondition(
          "va" + num + ".name like {name" + num + "} and " +
          "va" + num + ".value like {value" + num + "}");

        if (!variableFilter.isExtendedVisibility())
        {
          queryBuilder.addTable("wfw_variable vb" + num);
          queryBuilder.addCondition("vb" + num + ".instanceid = i.instanceid");
          queryBuilder.addCondition("(vb" + num + ".name like '" +
            WorkflowConstants.READ_ACCESS_PREFIX + "%' or vb" + num +
            ".name like '" +
            WorkflowConstants.WRITE_ACCESS_PREFIX + "%') and " +
            "va" + num + ".name like trim(substr(vb" + num + ".value, 21))");

          queryBuilder.addCondition(getRoleClause("vb" + num, roles));
        }
        innerQuery = true;
      }
    }

    String query = "select v.instanceid, v.name, v.value, v.type from " +
      "wfw_variable v where " +
      "v.name in ('" +
      WorkflowConstants.WORKFLOW_NAME + "', '" +
      WorkflowConstants.WORKFLOW_VERSION + "', '" +
      WorkflowConstants.ACTIVE_NODES + "', '" +
      WorkflowConstants.DESCRIPTION + "', '" +
      WorkflowConstants.STATE + "', '" +
      WorkflowConstants.START_DATE_TIME + "', '" +
      WorkflowConstants.SIMULATION + "', '" +
      WorkflowConstants.DESTROY_BUTTON_ENABLED + "')";

    if (innerQuery) query += " and v.instanceid in (" + queryBuilder + ")";

    query += " order by v.instanceid desc";

    System.out.println("-------------");
    System.out.println(query);
    System.out.println(parameters);

    conn.setMaxRows(filter.getMaxResults() * 8); // 8 variables
    long t0 = System.currentTimeMillis();
    Table table = conn.executeQuery(query, parameters);
    long t1 = System.currentTimeMillis();
    System.out.println("Time: " + (t1 - t0) / 1000.0);
    Table result = new Table(
      new String[]{"instanceid", "variables"});

    Map variables = null;
    String instanceId = null;
    int row = 0;
    int instanceCount = 0;
    while (row < table.getRowCount() && instanceCount <= filter.getMaxResults())
    {
      Number id = (Number)table.getElementAt(row, 0);
      String nextInstanceId = String.valueOf(id.intValue());
      if (!nextInstanceId.equals(instanceId))
      {
        // new instance
        instanceId = nextInstanceId;
        variables = new HashMap();
        instanceCount++;
        if (instanceCount <= filter.getMaxResults())
        {
          result.addRow(new Object[]{instanceId, variables});
        }
      }
      String varName = String.valueOf(table.getElementAt(row, 1));
      String svalue = String.valueOf(table.getElementAt(row, 2));
      String varType = String.valueOf(table.getElementAt(row, 3));
      Object varValue = convertValueToJava(svalue, varType);
      variables.put(varName, varValue);
      row++;
    }
    return result;
  }

  @Override
  public List<InstanceEvent> getInstanceEvents(String instanceId)
    throws Exception
  {
    parameters.clear();
    parameters.put("instanceId", instanceId);

    String query = "select e.eventnum, eventdate, eventhour, e.actor, " +
      "name, type, oldvalue, newvalue " +
      "from wfw_event e, wfw_eventvar ev " +
      "where e.instanceid = ev.instanceid and e.eventnum = ev.eventnum and " +
      "e.instanceid = {instanceId} order by e.eventnum, name";

    Table table = conn.executeQuery(query, parameters);
    List<InstanceEvent> instanceEvents = new ArrayList<>();
    InstanceEvent instanceEvent = null;
    int eventNum = -1;

    for (int i = 0; i < table.getRowCount(); i++)
    {
      int nextEventNum = ((Number)table.getElementAt(i, 0)).intValue();
      if (nextEventNum != eventNum || instanceEvent == null)
      {
        eventNum = nextEventNum;
        String date = (String)table.getElementAt(i, 1);
        String time = (String)table.getElementAt(i, 2);
        String actorName = (String)table.getElementAt(i, 3);
        String dateTime = date + time;
        instanceEvent = new InstanceEvent();
        instanceEvent.setInstanceId(instanceId);
        instanceEvent.setEventNum(eventNum);
        instanceEvent.setDateTime(dateTime);
        instanceEvent.setActorName(actorName);
        instanceEvents.add(instanceEvent);
      }
      VariableChange variableChange = new VariableChange();
      variableChange.setName((String)table.getElementAt(i, 4));
      variableChange.setType((String)table.getElementAt(i, 5));
      variableChange.setOldValue((String)table.getElementAt(i, 6));
      variableChange.setNewValue((String)table.getElementAt(i, 7));
      instanceEvent.getVariableChange().add(variableChange);
    }
    return instanceEvents;
  }

  @Override
  public void rollback() throws Exception
  {
    conn.rollback();
    lockedInstances.clear();
  }

  @Override
  public void commit() throws Exception
  {
    conn.commit();
    lockedInstances.clear();
  }

  @Override
  public void close() throws Exception
  {
    conn.close();
    lockedInstances.clear();
  }

  private String getRoleClause(String variableName, Set<String> roles)
  {
    StringBuilder clause = new StringBuilder();
    clause.append("trim(substr(");
    clause.append(variableName).append(".value, 1, 20))");
    clause.append(" in (");
    int i = 0;
    for (String roleId : roles)
    {
      if (i > 0) clause.append(", ");
      clause.append("'").append(roleId).append("'");
      i++;
    }
    clause.append(")");
    return clause.toString();
  }

  private String getType(Object value)
  {
    String type = null;
    if (value == null || value instanceof String)
    {
      type = WorkflowConstants.TEXT_TYPE;
    }
    else if (value instanceof Number)
    {
      type = WorkflowConstants.NUMBER_TYPE;
    }
    else if (value instanceof Boolean)
    {
      type = WorkflowConstants.BOOLEAN_TYPE;
    }
    return type;
  }

  private String convertValueToString(Object value)
  {
    if (value instanceof Number)
    {
      Number number = (Number)value;
      if (Math.round(number.doubleValue()) == number.doubleValue())
      {
        value = String.valueOf(number.intValue());
      }
    }
    return String.valueOf(value);
  }

  private Object convertValueToJava(String svalue, String type)
  {
    Object value = null;

    if (svalue == null)
    {
      value = null;
    }
    else if (WorkflowConstants.NUMBER_TYPE.equals(type))
    {
      try
      {
        value = new Double(svalue);
      }
      catch (NumberFormatException ex)
      {
        value = 0.0;
      }
    }
    else if (WorkflowConstants.TEXT_TYPE.equals(type))
    {
      value = svalue;
    }
    else if (WorkflowConstants.BOOLEAN_TYPE.equals(type))
    {
      value = Boolean.valueOf(svalue);
    }
    return value;
  }

  class QueryBuilder
  {
    StringBuilder selectClause = new StringBuilder();
    StringBuilder fromClause = new StringBuilder();
    StringBuilder whereClause = new StringBuilder();

    void addColumn(String expr)
    {
      if (selectClause.length() > 0) selectClause.append(", ");
      selectClause.append(expr);
    }

    void addTable(String expr)
    {
      if (fromClause.length() > 0) fromClause.append(", ");
      fromClause.append(expr);
    }

    void addCondition(String expr)
    {
      if (whereClause.length() > 0) whereClause.append(" and");
      whereClause.append(" (").append(expr).append(") ");
    }

    @Override
    public String toString()
    {
      return "select " + selectClause +
              " from " + fromClause +
              " where" + whereClause;
    }
  }
}