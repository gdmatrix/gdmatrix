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
package org.santfeliu.workflow.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import javax.jws.HandlerChain;
import javax.jws.WebService;

import javax.xml.ws.WebServiceContext;

import org.matrix.workflow.InstanceFilter;
import org.matrix.workflow.InstanceView;
import org.matrix.workflow.Variable;
import org.matrix.workflow.WorkflowConstants;
import org.matrix.workflow.WorkflowManagerPort;

import org.santfeliu.security.User;
import org.santfeliu.security.UserCache;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.Table;
import org.santfeliu.workflow.store.DataStore;
import org.santfeliu.workflow.VariableListConverter;
import org.santfeliu.workflow.WorkflowEngine;
import org.santfeliu.workflow.WorkflowEvent;
import org.santfeliu.workflow.store.WorkflowStore;
import org.santfeliu.workflow.WorkflowUser;
import org.santfeliu.ws.WSExceptionFactory;


/**
 *
 * @author realor
 */
@WebService(endpointInterface = "org.matrix.workflow.WorkflowManagerPort")
@HandlerChain(file="handlers.xml")
public class WorkflowManager implements WorkflowManagerPort
{
  @Resource
  WebServiceContext wsContext;

  protected static final Logger log = Logger.getLogger("Workflow");

  protected WorkflowEngine engine;
  protected int maxSteps = 50;

  // Matrix config properties
  public static final String DATA_STORE = "dataStore";
  public static final String WORKFLOW_STORE = "workflowStore";
  public static final String AGENTS = "agents";
  public static final String MAX_STEPS = "maxSteps";

  public WorkflowManager()
  {
    try
    {
      log.info("WorkflowManager init");
      Properties properties = MatrixConfig.getProperties();
      
      // setup workflowStore
      String workflowStoreClassName = 
        MatrixConfig.getClassProperty(WorkflowManager.class, WORKFLOW_STORE);
      WorkflowStore ws = (WorkflowStore)
        Class.forName(workflowStoreClassName).newInstance();      
      ws.init(properties);
      
      // setup dataStore
      String dataStoreClassName = 
        MatrixConfig.getClassProperty(WorkflowManager.class, DATA_STORE);
      DataStore ds = (DataStore)
        Class.forName(dataStoreClassName).newInstance(); 
      ds.init(properties);

      // create engine
      engine = new WorkflowEngine(ws, ds);

      // create agents
      String agentNamesString = 
        MatrixConfig.getClassProperty(WorkflowManager.class, AGENTS);
      if (agentNamesString != null)
      {
        String agentNames[] = agentNamesString.split(",");
        for (String agentName : agentNames)
        {
          engine.createAgent(agentName);
        }
      }

      // set maxSteps
      String sMaxSteps = 
        MatrixConfig.getClassProperty(WorkflowManager.class, MAX_STEPS);
      if (sMaxSteps != null)
      {
        maxSteps = Integer.parseInt(sMaxSteps);
      }
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "WorkflowManager init failed", ex);
      throw new RuntimeException(ex);
    }
  }
  
  @Override
  public String createInstance(String workflowName, List<Variable> variables)
  {
    try
    {
      return String.valueOf(engine.createInstance(workflowName, 
        VariableListConverter.toMap(variables),
        getWorkflowUser()));
    }
    catch (Exception ex)
    {
      throw WSExceptionFactory.create(ex);
    }
  }

  @Override
  public boolean destroyInstance(String instanceId)
  {
    try
    {
      return engine.destroyInstance(instanceId);
    }
    catch (Exception ex)
    {
      throw WSExceptionFactory.create(ex);
    }
  }

  @Override
  public String assignAgent(String instanceId, String agentName)
  {
    try
    {
      return engine.assignAgent(instanceId, agentName);
    }
    catch (Exception ex)
    {
      throw WSExceptionFactory.create(ex);
    }
  }

  @Override
  public List<Variable> processInstance(String instanceId,
    List<Variable> variables, boolean returnVariables)
  {
    try
    {
      Map map = engine.processInstance(instanceId, 
        VariableListConverter.toMap(variables),
        returnVariables, getWorkflowUser(), maxSteps);
      return VariableListConverter.toList(map);
    }
    catch (Exception ex)
    {
      throw WSExceptionFactory.create(ex);
    }
  }

  @Override
  public boolean doStep(String instanceId)
  {
    try
    {
      WorkflowEvent event = engine.doStep(instanceId, 
        getWorkflowUser(), false);
      return event != null;
    }
    catch (Exception ex)
    {
      throw WSExceptionFactory.create(ex);
    }
  }

  @Override
  public boolean undoStep(String instanceId)
  {
    try
    {
      return engine.undoStep(instanceId);
    }
    catch (Exception ex)
    {
      throw WSExceptionFactory.create(ex);
    }
  }

  @Override
  public int setVariables(String instanceId, List<Variable> variables)
  {
    try
    {
      engine.setVariables(instanceId,
        VariableListConverter.toMap(variables),
        getWorkflowUser(), false);
      return variables.size();
    }
    catch (Exception ex)
    {
      throw WSExceptionFactory.create(ex);
    }
  }
  
  @Override
  public List<Variable> getVariables(String instanceId)
  {
    try
    {
      Map map = new HashMap();
      engine.getVariables(instanceId, map, getWorkflowUser());
      return VariableListConverter.toList(map);
    }
    catch (Exception ex)
    {
      throw WSExceptionFactory.create(ex);
    }
  }

  @Override
  public List<InstanceView> findInstances(InstanceFilter filter)
  {
    try
    {  
      Table table = engine.findInstances(filter, getWorkflowUser());

      ArrayList<InstanceView> instanceList = new ArrayList<InstanceView>();
      for (int i = 0; i < table.getRowCount(); i++)
      {
        InstanceView instanceView = new InstanceView();
        Table.Row row = table.getRow(i);
        instanceView.setInstanceId(getStringValue(row.get("instanceid")));
        Map variables = (Map)row.get("variables");
        instanceView.setName(
          getStringValue(variables.get(WorkflowConstants.WORKFLOW_NAME)));
        instanceView.setVersion(
          getStringValue(variables.get(WorkflowConstants.WORKFLOW_VERSION)));
        instanceView.setDescription(
          getStringValue(variables.get(WorkflowConstants.DESCRIPTION)));
        instanceView.setState(
          getStringValue(variables.get(WorkflowConstants.STATE)));
        instanceView.setActiveNodes(
          getStringValue(variables.get(WorkflowConstants.ACTIVE_NODES)));
        instanceView.setStartDateTime(
          getStringValue(variables.get(WorkflowConstants.START_DATE_TIME)));
        instanceView.setSimulation(
          getBooleanValue(variables.get(WorkflowConstants.SIMULATION)));
        instanceView.setDestroyButtonEnabled(
          getBooleanValue(variables.get(WorkflowConstants.DESTROY_BUTTON_ENABLED)));

        instanceList.add(instanceView);
      }
      return instanceList;
    }
    catch (Exception ex)
    {
      throw WSExceptionFactory.create(ex);
    }
  }

  @PreDestroy
  public void destroy()
  {
    log.log(Level.INFO, "Destroying WorkflowManager, killing agents...");
    engine.killAllAgents();
  }
  
  /**** private methods ****/

  private WorkflowUser getWorkflowUser()
  {
    try
    {
      User user = UserCache.getUser(wsContext);
      WorkflowUser wfUser = new WorkflowUser();
      wfUser.setName(user.getUserId());
      wfUser.setPassword(user.getPassword());
      wfUser.getRoles().addAll(user.getRoles());
      return wfUser;
    }
    catch (Exception ex)
    {
      throw WSExceptionFactory.create(ex);
    }
  }

  private String getStringValue(Object value)
  {
    if (value == null) return null;
    else return String.valueOf(value);
  }

  private boolean getBooleanValue(Object value)
  {
    if (value == null) return false;
    else if (value instanceof Boolean) return ((Boolean)value).booleanValue();
    else return false;
  }
}

