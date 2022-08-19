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
package org.santfeliu.workflow;

import org.santfeliu.workflow.processor.NodeProcessor;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.matrix.workflow.InstanceEvent;
import org.matrix.workflow.InstanceFilter;
import org.matrix.workflow.WorkflowConstants;
import org.santfeliu.util.Table;
import org.santfeliu.util.TextUtils;
import org.santfeliu.util.template.Template;
import org.santfeliu.workflow.store.DataStore;
import org.santfeliu.workflow.store.DataStoreConnection;
import org.santfeliu.workflow.store.WorkflowStore;

/**
 *
 * @author realor
 */
public class WorkflowEngine
{
  public static final String TIMER_AGENT_NAME = "cronos";
  private final WorkflowStore workflowStore;
  private final DataStore dataStore;
  private final Map agents = new HashMap();

  public WorkflowEngine(WorkflowStore workflowStore, DataStore dataStore)
  {
    this.workflowStore = workflowStore;
    this.dataStore = dataStore;
    TimerAgent agent = new TimerAgent(this, TIMER_AGENT_NAME);
    agents.put(TIMER_AGENT_NAME, agent);
    agent.start();
  }

  public synchronized WorkflowAgent createAgent(String agentName)
  {
    WorkflowAgent agent = (WorkflowAgent)agents.get(agentName);
    if (agent == null || !agent.isAlive())
    {
      agent = new GenericAgent(this, agentName);
      agents.put(agentName, agent);
      agent.start();
    }
    return agent;
  }

  public synchronized WorkflowAgent getAgent(String agentName)
  {
    return (WorkflowAgent)agents.get(agentName);
  }

  public synchronized void killAllAgents()
  {
    Collection col = agents.values();
    Iterator iter = col.iterator();
    while (iter.hasNext())
    {
      WorkflowAgent agent = (WorkflowAgent)iter.next();
      if (agent.isAlive())
      {
        agent.kill();
      }
    }
  }

  public synchronized void removeDeadAgents()
  {
    Collection col = agents.values();
    Iterator iter = col.iterator();
    while (iter.hasNext())
    {
      WorkflowAgent agent = (WorkflowAgent)iter.next();
      if (!agent.isAlive())
      {
        iter.remove();
      }
    }
  }

  public synchronized WorkflowAgent[] getAgents()
  {
    Collection col = agents.values();
    return (WorkflowAgent[])col.toArray(new WorkflowAgent[col.size()]);
  }

  public synchronized String assignAgent(String instanceId, String agentName)
    throws WorkflowException
  {
    try
    {
      DataStoreConnection conn = dataStore.getConnection();
      try
      {
        conn.lockInstance(instanceId);
        if (WorkflowConstants.ANY_AGENT.equals(agentName))
          agentName = findAgent();
        Map variables = new HashMap();
        variables.put(WorkflowConstants.AGENT_NAME, agentName);
        conn.storeVariables(instanceId, variables);

        if (agentName != null) wakeUpAgent(agentName);
      }
      catch (Exception e)
      {
        conn.rollback();
        throw e;
      }
      finally
      {
        conn.close();
      }
    }
    catch (Exception ex)
    {
      throw WorkflowException.createException(ex);
    }
    return agentName;
  }

  public Workflow getWorkflow(String workflowName, String version)
    throws WorkflowException
  {
    return workflowStore.getWorkflow(workflowName, version, false);
  }

  public String createInstance(String workflowName,
    Map variables, WorkflowActor actor) throws WorkflowException
  {
    String instanceId = null;
    try
    {
      Workflow workflow = workflowStore.getWorkflow(workflowName, null, true);
      String workflowVersion = workflow.getVersion();
      DataStoreConnection conn = dataStore.getConnection();
      try
      {
        instanceId = String.valueOf(conn.createInstance(workflowName));
        WorkflowInstance instance =
          new WorkflowInstance(workflowName, workflowVersion, instanceId);

        // set START_DATE_TIME variable
        variables.put(WorkflowConstants.START_DATE_TIME,
          TextUtils.formatDate(new Date(), "yyyyMMddHHmmss"));

        // set UPDATE_ONLY_FORM rule
        variables.put(WorkflowConstants.UPDATE_ONLY_PREFIX + "FORM",
          WorkflowConstants.FORM_PREFIX + "%");
        instance.putAll(variables);

        // set activeNodes with first nodeId
        String firstNodeId = workflow.getFirstNodeId();
        instance.setActiveNodes(" " + firstNodeId + " ");

        // assign agent
        String agentName = instance.getAgentName();
        if (WorkflowConstants.ANY_AGENT.equals(agentName))
        {
          agentName = findAgent();
        }
        instance.setAgentName(agentName);

        // set DESCRIPTION var
        String description =
          (String)instance.get(WorkflowConstants.DESCRIPTION);
        if (description == null)
        {
          description = workflow.getDescription();
          instance.put(WorkflowConstants.DESCRIPTION, description);
        }

        conn.storeVariables(instanceId, instance);
        WorkflowEvent event = new WorkflowEvent(instanceId,
          new ValueChanges(instance), actor.getName());
        if (workflow.isUndoable())
        {
          conn.pushEvent(event);
        }
        conn.commit();

        // wakeUp agent
        wakeUpAgent(instance.getAgentName());
      }
      catch (Exception e)
      {
        conn.rollback();
        throw e;
      }
      finally
      {
        conn.close();
      }
    }
    catch (Exception ex)
    {
      throw WorkflowException.createException(ex);
    }
    return instanceId;
  }

  public boolean destroyInstance(String instanceId)
    throws WorkflowException
  {
    boolean destroyed = false;
    try
    {
      DataStoreConnection conn = dataStore.getConnection();
      try
      {
        conn.lockInstance(instanceId);
        destroyed = conn.destroyInstance(instanceId);
        conn.commit();
      }
      catch (Exception ex)
      {
        conn.rollback();
      }
      finally
      {
        conn.close();
      }
    }
    catch (Exception ex)
    {
      throw WorkflowException.createException(ex);
    }
    return destroyed;
  }

  public String findProcessableInstance(String agentName)
    throws WorkflowException
  {
    String instanceId = null;
    try
    {
      DataStoreConnection conn = dataStore.getConnection();
      try
      {
        instanceId = conn.findProcessableInstance(agentName);
      }
      finally
      {
        conn.close();
      }
    }
    catch (Exception ex)
    {
      throw WorkflowException.createException(ex);
    }
    return instanceId;
  }

  public Map processInstance(String instanceId, Map variables,
    boolean returnVariables, WorkflowActor actor, int maxSteps)
    throws WorkflowException
  {
    WorkflowInstance instance = new WorkflowInstance();
    WorkflowEvent event = null;

    // phase 1: setVariables / doStep
    boolean moreSteps = maxSteps > 1;
    if (variables == null)
    {
      event = doStep(instanceId, actor, moreSteps);
    }
    else
    {
      event = setVariables(instanceId, variables, actor, moreSteps);
    }

    // phase 2: do steps
    int numSteps = 1;
    while (event != null && numSteps < maxSteps)
    {
      moreSteps = numSteps + 1 < maxSteps;
      event = doStep(instanceId, actor, moreSteps);
      numSteps++;
    }
    if (event != null) // infinite loop detected
    {
      variables = new HashMap();
      variables.put(WorkflowConstants.ACTIVE_NODES, null);
      variables.put(WorkflowConstants.ERROR_PREFIX + "0",
        "Infinite loop detected. Instance terminated.");
      variables.put(WorkflowConstants.ERRORS, " 0 ");
      setVariables(instanceId, variables, new WorkflowAdmin(), false);
    }

    // phase 3: return variables
    if (returnVariables)
    {
      getVariables(instanceId, instance, actor);
    }
    return instance;
  }

  public WorkflowEvent doStep(String instanceId, WorkflowActor actor,
    boolean moreSteps) throws WorkflowException
  {
    WorkflowEvent event = null;
    try
    {
      DataStoreConnection conn = dataStore.getConnection();
      try
      {
        conn.lockInstance(instanceId);

        WorkflowInstance instance = new WorkflowInstance();
        conn.loadVariables(instanceId, instance);
        ValueChanges valueChanges = new ValueChanges();
        instance.setValueChanges(valueChanges);
        instance.setEngine(this);

        Workflow workflow = getWorkflow(instance);

        boolean pendentNodes =
          processActiveNodes(workflow, instance, actor, valueChanges);

        event = storeChanges(workflow, instance, actor, valueChanges, conn);

        boolean processable = pendentNodes || event != null;

        conn.setInstanceProcessable(instanceId, processable);
        conn.commit();

        // wakeUp agent
        if (processable && !(moreSteps && event != null))
          wakeUpAgent(instance.getAgentName());
      }
      catch (Exception ex)
      {
        conn.rollback();
        throw ex;
      }
      finally
      {
        conn.close();
      }
    }
    catch (Exception e)
    {
      throw WorkflowException.createException(e);
    }
    return event;
  }

  public boolean undoStep(String instanceId) throws WorkflowException
  {
    boolean undone = false;
    try
    {
      DataStoreConnection conn = dataStore.getConnection();
      try
      {
        conn.lockInstance(instanceId);
        WorkflowEvent event = conn.popEvent(instanceId);
        if (event != null)
        {
          Map oldValues = event.getValueChanges().getOldValues();
          conn.storeVariables(instanceId, oldValues);
          conn.setInstanceProcessable(instanceId, false);
          undone = true;
        }
      }
      catch (Exception ex)
      {
        conn.rollback();
      }
      finally
      {
        conn.close();
      }
    }
    catch (Exception ex)
    {
      throw WorkflowException.createException(ex);
    }
    return undone;
  }

  public Map getVariables(String instanceId, Map variables, WorkflowActor actor)
    throws WorkflowException
  {
    try
    {
      DataStoreConnection conn = dataStore.getConnection();
      try
      {
        WorkflowInstance instance = new WorkflowInstance();
        if (variables == null) variables = new HashMap();
        conn.loadVariables(instanceId, instance);
        filterVariables(instance, variables, actor.getRoles());
        return variables;
      }
      finally
      {
        conn.close();
      }
    }
    catch (Exception ex)
    {
      throw WorkflowException.createException(ex);
    }
  }

  public WorkflowEvent setVariables(String instanceId,
    Map variables, WorkflowActor actor, boolean moreSteps)
    throws WorkflowException
  {
    WorkflowEvent event = null;
    try
    {
      DataStoreConnection conn = dataStore.getConnection();
      try
      {
        conn.lockInstance(instanceId);

        WorkflowInstance instance = new WorkflowInstance();
        conn.loadVariables(instanceId, instance);

        // check access rules
        checkWriteAccess(instance, variables, actor.getRoles());

        ValueChanges valueChanges = new ValueChanges();
        instance.setValueChanges(valueChanges);
        instance.setEngine(this);
        instance.putAll(variables);

        Workflow workflow = getWorkflow(instance);

        boolean pendentNodes =
          processActiveNodes(workflow, instance, actor, valueChanges);

        event = storeChanges(workflow, instance, actor, valueChanges, conn);

        boolean processable = pendentNodes || event != null;

        conn.setInstanceProcessable(instanceId, processable);
        conn.commit();

        // wakeUp agent
        if (processable && !(moreSteps && event != null))
          wakeUpAgent(instance.getAgentName());
      }
      catch (Exception ex)
      {
        conn.rollback();
        throw ex;
      }
      finally
      {
        conn.close();
      }
    }
    catch (Exception e)
    {
      throw WorkflowException.createException(e);
    }
    return event;
  }

  public Table findInstances(InstanceFilter filter, WorkflowActor actor)
    throws WorkflowException
  {
    try
    {
      DataStoreConnection conn = dataStore.getConnection();
      try
      {
        return conn.findInstances(filter, actor.getRoles());
      }
      finally
      {
        conn.close();
      }
    }
    catch (Exception ex)
    {
      throw WorkflowException.createException(ex);
    }
  }

  public List<InstanceEvent> getInstanceEvents(String instanceId)
    throws WorkflowException
  {
    try
    {
      DataStoreConnection conn = dataStore.getConnection();
      try
      {
        return conn.getInstanceEvents(instanceId);
      }
      finally
      {
        conn.close();
      }
    }
    catch (Exception ex)
    {
      throw WorkflowException.createException(ex);
    }
  }

  public void programTimer(String instanceId, String dateTime)
    throws WorkflowException
  {
    try
    {
      DataStoreConnection conn = dataStore.getConnection();
      try
      {
        conn.programTimer(instanceId, dateTime);
      }
      finally
      {
        conn.close();
      }
    }
    catch (Exception ex)
    {
      throw WorkflowException.createException(ex);
    }
  }

  public void removeTimer(String instanceId, String dateTime)
    throws WorkflowException
  {
    try
    {
      DataStoreConnection conn = dataStore.getConnection();
      try
      {
        conn.removeTimer(instanceId, dateTime);
      }
      finally
      {
        conn.close();
      }
    }
    catch (Exception ex)
    {
      throw WorkflowException.createException(ex);
    }
  }

  public Table getTimers(String dateTime)
    throws WorkflowException
  {
    try
    {
      DataStoreConnection conn = dataStore.getConnection();
      try
      {
        return conn.getTimers(dateTime);
      }
      finally
      {
        conn.close();
      }
    }
    catch (Exception ex)
    {
      throw WorkflowException.createException(ex);
    }
  }

  public Set parseElements(String values, WorkflowInstance instance)
  {
    HashSet set = new HashSet();
    if (values != null)
    {
      String mergedValues = Template.create(values).merge(instance);
      TextUtils.splitWords(mergedValues, set);
    }
    return set;
  }

  public Set getRolesSet(String roles, WorkflowInstance instance)
  {
    return parseElements(roles, instance);
  }

  /**
   *
   * @param instance
   * @return
   * @throws Exception
   */
  private Workflow getWorkflow(WorkflowInstance instance)
    throws Exception
  {
    Workflow workflow = null;
    String workflowName = instance.getWorkflowName();
    String workflowVersion = instance.getWorkflowVersion();
    try
    {
      boolean forceWorkflowReload =
        instance.containsKey(WorkflowConstants.ERRORS);

      // if workflow has errors, reload last workflow version
      workflow = workflowStore.getWorkflow(workflowName, workflowVersion,
        forceWorkflowReload);
    }
    catch (WorkflowException ex)
    {
      // if that version does not exist, try loading last version.
      if (workflowVersion != null)
      {
        workflow = workflowStore.getWorkflow(workflowName, null, true);
      }
      else throw ex;
    }
    return workflow;
  }

  /**
   * @param instance
   * @param actor
   * @param valueChanges
   * @return true if instance is still processable by another actor
   * @throws Exception
   */
  private boolean processActiveNodes(Workflow workflow,
    WorkflowInstance instance, WorkflowActor actor, ValueChanges valueChanges)
    throws Exception
  {
    boolean pendentNodes = false;

    if (instance.getActiveNodes() != null) // not terminated
    {
      String workflowVersion = workflow.getVersion();
      if (!workflowVersion.equals(instance.getWorkflowVersion()))
      {
        instance.setWorkflowVersion(workflowVersion);
      }
      String activeNodes = instance.getActiveNodes();
      Set activeNodeSet = parseActiveNodes(activeNodes);
      Set nextActiveNodeSet = new HashSet();

      pendentNodes = recursiveProcessActiveNodes(workflow, instance, actor,
        activeNodeSet, nextActiveNodeSet, true);

      // update ACTIVE_NODES variable, if not set manually
      if (valueChanges.contains(WorkflowConstants.ACTIVE_NODES))
      {
        // direct update of ACTIVE_NODES (TerminateInstance)
        nextActiveNodeSet = parseActiveNodes(instance.getActiveNodes());
      }
      else
      {
        String nextActiveNodes = formatActiveNodes(nextActiveNodeSet);
        instance.setActiveNodes(nextActiveNodes);
      }
      // update STATE variable, if not set manually
      if (!valueChanges.contains(WorkflowConstants.STATE))
      {
        String stateValue = describeActiveNodes(nextActiveNodeSet, workflow);
        instance.put(WorkflowConstants.STATE, stateValue);
      }
      // update ERRORS variable
      String errors = getErrors(instance);
      instance.put(WorkflowConstants.ERRORS, errors);
    }
    return pendentNodes;
  }

 /**
  *
  * @param workflow
  * @param instance
  * @param actor
  * @param valueChanges
  * @param conn
  * @return
  * @throws java.lang.Exception
  */
  private WorkflowEvent storeChanges(Workflow workflow,
    WorkflowInstance instance, WorkflowActor actor,
    ValueChanges valueChanges, DataStoreConnection conn)
    throws Exception
  {
    WorkflowEvent event = null;
    String instanceId = instance.getInstanceId();

    if (!valueChanges.isEmpty()) // some changes were made
    {
      Map newValues = valueChanges.getNewValues();
      conn.storeVariables(instanceId, newValues);

      // generate event
      event = new WorkflowEvent(instanceId, valueChanges, actor.getName());
      if (workflow.isUndoable())
      {
        conn.pushEvent(event);
      }
    }
    return event;
  }

  /**
   *
   * @param workflow
   * @param instance
   * @param actor
   * @param nodeSet
   * @param nextNodeSet
   * @param firstStep
   * @return true if instance is still processable by another actor
   */
  private boolean recursiveProcessActiveNodes(Workflow workflow,
    WorkflowInstance instance, WorkflowActor actor,
    Set nodeSet, Set nextNodeSet, boolean firstStep)
  {
    boolean pendentNodes = false;
    Iterator iter = nodeSet.iterator();
    while (iter.hasNext()) // for each active node
    {
      String nodeId = (String)iter.next();
      WorkflowNode node = workflow.getNode(nodeId);
      if (canActorExecuteNode(actor, node, instance))
      {
        Set newNodeSet = new HashSet();
        boolean wait = true;
        if (node.isImmediate() || firstStep)
        {
          wait = processActiveNode(node, instance, actor, newNodeSet);
        }
        if (wait) // wait outcome
        {
          nextNodeSet.add(nodeId);
        }
        else if (newNodeSet.isEmpty()) // end outcome
        {
          // nothing
        }
        else // other outcomes
        {
          pendentNodes = recursiveProcessActiveNodes(workflow, instance, actor,
            newNodeSet, nextNodeSet, false) || pendentNodes;
        }
      }
      else
      {
        nextNodeSet.add(nodeId);
        pendentNodes = true;
      }
    }
    return pendentNodes;
  }

  /**
   *
   * @param node
   * @param instance
   * @param actor
   * @param newNodeSet
   * @return true if node returns 'wait' outcome
   */
  private boolean processActiveNode(WorkflowNode node,
    WorkflowInstance instance, WorkflowActor actor, Set newNodeSet)
  {
    boolean wait = false;
    try
    {
      // clear error
      instance.put(WorkflowConstants.ERROR_PREFIX + node.getId(), null);
      String outcome = ((NodeProcessor)node).process(instance, actor); // process node
      if (WorkflowNode.END_OUTCOME.equals(outcome))
      {
        // end this branch of execution
      }
      else if (WorkflowNode.WAIT_OUTCOME.equals(outcome))
      {
        wait = true;
      }
      else // continue to next nodes
      {
        addNextNodes(node, outcome, newNodeSet);
      }
    }
    catch (Exception ex)
    {
      // set error
      instance.put(WorkflowConstants.ERROR_PREFIX + node.getId(), ex.toString());
      addNextNodes(node, WorkflowNode.ERROR_OUTCOME, newNodeSet);
    }
    return wait;
  }

  private String describeActiveNodes(Set nodeSet, Workflow workflow)
  {
    StringBuffer buffer = new StringBuffer();
    Iterator iter = nodeSet.iterator();
    while (iter.hasNext())
    {
      String nodeId = (String)iter.next();
      WorkflowNode node = workflow.getNode(nodeId);
      if (node != null)
      {
        if (!node.isHidden())
        {
          String description = node.getDescription();
          if (description != null)
          {
            description = description.trim();
            if (description.length() > 0)
            {
              if (buffer.length() > 0) buffer.append(" / ");
              buffer.append(description);
            }
          }
        }
      }
    }
    return buffer.length() == 0 ? null : buffer.toString();
  }

  private void addNextNodes(WorkflowNode node, String outcome, Set set)
  {
    WorkflowNode.Transition transitions[] = node.getTransitions();
    for (WorkflowNode.Transition transition : transitions)
    {
      if (matchOutcome(outcome, transition.getOutcome()) ||
        (transition.isAnyOutcome() &&
        !WorkflowNode.ERROR_OUTCOME.equals(outcome)))
      {
        String nextNodeId = transition.getNextNodeId();
        set.add(nextNodeId);
      }
    }
  }

  private boolean matchOutcome(String outcome, String transitionOutcome)
  {
    boolean match = false;
    outcome = outcome.trim();
    StringTokenizer tokenizer = new StringTokenizer(transitionOutcome, "|");
    while (!match && tokenizer.hasMoreTokens())
    {
      String token = tokenizer.nextToken();
      match = token.trim().equals(outcome);
    }
    return match;
  }

  private Set parseActiveNodes(String s)
  {
    HashSet set = new HashSet();
    if (s == null) return set;

    StringTokenizer tokenizer = new StringTokenizer(s, " ", false);
    while (tokenizer.hasMoreTokens())
    {
      String nodeId = tokenizer.nextToken();
      set.add(nodeId);
    }
    return set;
  }

  private String formatActiveNodes(Set activeNodes)
  {
    if (activeNodes.isEmpty()) return null;
    StringBuilder buffer = new StringBuilder(" ");
    Iterator iter = activeNodes.iterator();
    while (iter.hasNext())
    {
      String nodeId = (String)iter.next();
      buffer.append(nodeId).append(" ");
    }
    return buffer.toString();
  }

  private String getErrors(WorkflowInstance instance)
  {
    StringBuilder buffer = new StringBuilder();
    Iterator iter = instance.keySet().iterator();
    while (iter.hasNext())
    {
      String variable = (String)iter.next();
      if (variable.startsWith(WorkflowConstants.ERROR_PREFIX))
      {
        String suffix = variable.substring(
          WorkflowConstants.ERROR_PREFIX.length());
        buffer.append(" ").append(suffix);
      }
    }
    return buffer.length() == 0 ? null : buffer.toString() + " ";
  }

  private void wakeUpAgent(String agentName)
  {
    WorkflowAgent agent = (WorkflowAgent)agents.get(agentName);
    if (agent != null)
    {
      agent.wakeUp();
    }
  }

  private synchronized String findAgent()
  {
    WorkflowAgent bestAgent = null;
    Collection col = agents.values();
    Iterator iter = col.iterator();
    while (iter.hasNext())
    {
      WorkflowAgent agent = (WorkflowAgent)iter.next();
      if (agent instanceof GenericAgent)
      {
        if (bestAgent == null) bestAgent = agent;
        else if (bestAgent.getStatistics().getLastProcessTime() <
          agent.getStatistics().getLastProcessTime())
        {
          bestAgent = agent;
        }
      }
    }
    return bestAgent == null ? null : bestAgent.getName();
  }

  private void filterVariables(WorkflowInstance instance,
    Map variables, Set roles)
  {
    Iterator iter = instance.entrySet().iterator();
    while (iter.hasNext())
    {
      Map.Entry entry = (Map.Entry)iter.next();
      String key = (String)entry.getKey();
      Object value = entry.getValue();
      if (instance.canReadVariable(key, roles))
      {
        variables.put(key, value);
      }
    }
  }

  private void checkWriteAccess(WorkflowInstance instance,
    Map variables, Set roles) throws Exception
  {
    Iterator iter = variables.entrySet().iterator();
    while (iter.hasNext())
    {
      Map.Entry entry = (Map.Entry)iter.next();
      String variable = (String)entry.getKey();

      if (!instance.containsKey(variable) &&
          instance.isUpdateOnlyVariable(variable))
        throw new WorkflowException(
          "Can't set variable '" + variable + "': updatable only");

      if (!instance.canWriteVariable(variable, roles))
        throw new WorkflowException(
          "Can't set variable '"+ variable + "': insufficient privileges");
    }
  }

  private boolean canActorExecuteNode(WorkflowActor actor,
    WorkflowNode node, WorkflowInstance instance)
  {
    Set nodeRoles = getRolesSet(node.getRoles(), instance);

    boolean onlyAgents = (nodeRoles.size() == 1 &&
      nodeRoles.contains(WorkflowConstants.WORKFLOW_AGENT_ROLE));

    return actor.hasAnyRole(nodeRoles) ||
      (actor.isWorkflowAdministrator() && !onlyAgents);
  }
}
