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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.matrix.workflow.WorkflowConstants;

/**
 *
 * @author realor
 */
public class WorkflowInstance extends HashMap
{
  private WorkflowEngine engine;
  private ValueChanges valueChanges = null;

  public WorkflowInstance()
  {
  }

  public WorkflowInstance(String workflowName,
    String workflowVersion, String instanceId)
  {
    this();
    put(WorkflowConstants.WORKFLOW_NAME, workflowName);
    put(WorkflowConstants.WORKFLOW_VERSION, workflowVersion);
    put(WorkflowConstants.INSTANCE_ID, instanceId);
  }

  public String getWorkflowName()
  {
    return (String)get(WorkflowConstants.WORKFLOW_NAME);
  }

  public String getWorkflowVersion()
  {
    return (String)get(WorkflowConstants.WORKFLOW_VERSION);
  }

  public void setWorkflowVersion(String version)
  {
    put(WorkflowConstants.WORKFLOW_VERSION, version);
  }

  public String getInstanceId()
  {
    return (String)get(WorkflowConstants.INSTANCE_ID);
  }

  public String getActiveNodes()
  {
    return (String)get(WorkflowConstants.ACTIVE_NODES);
  }

  public void setActiveNodes(String activeNodes)
  {
    put(WorkflowConstants.ACTIVE_NODES, activeNodes);
  }

  public String getAgentName()
  {
    return (String)get(WorkflowConstants.AGENT_NAME);
  }

  public void setAgentName(String agentName)
  {
    put(WorkflowConstants.AGENT_NAME, agentName);
  }

  public void setEngine(WorkflowEngine engine)
  {
    this.engine = engine;
  }

  public WorkflowEngine getEngine()
  {
    return engine;
  }

  @Override
  public final Object put(Object key, Object newValue)
  {
    Object oldValue = null;

    if ("".equals(newValue)) newValue = null; // for compatibility with Oracle
    if (isValidType(newValue))
    {
      if (newValue == null) oldValue = super.remove(key);
      else oldValue = super.put(key, newValue);

      if (valueChanges != null)
      {
        valueChanges.registerChange(key, oldValue, newValue);
      }
    }
    return oldValue;
  }

  @Override
  public void putAll(Map variables)
  {
    Set<Map.Entry> entrySet = variables.entrySet();
    for (Map.Entry entry : entrySet)
    {
      put(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public Object remove(Object key)
  {
    Object oldValue = super.remove(key);
    if (valueChanges != null)
    {
      valueChanges.registerChange(key, oldValue, null);
    }
    return oldValue;
  }

  public void setValueChanges(ValueChanges valueChanges)
  {
    this.valueChanges = valueChanges;
  }

  public ValueChanges getValueChanges()
  {
    return valueChanges;
  }

  public boolean isValidType(Object value)
  {
    if (value == null) return true;
    if (value instanceof String) return true;
    if (value instanceof Number) return true;
    if (value instanceof Boolean) return true;
    return  false;
  }

  public void addRule(String ruleName, boolean write, String role, String var)
  {
    String rule = (role + "                    ").substring(0, 20) + var;

    if (write)
    {
      put(WorkflowConstants.WRITE_ACCESS_PREFIX + ruleName, rule);
    }
    else
    {
      put(WorkflowConstants.READ_ACCESS_PREFIX + ruleName, rule);
    }
  }

  public boolean canReadVariable(String variable, Set roles)
  {
    return
      WorkflowConstants.WORKFLOW_NAME.equals(variable) ||
      WorkflowConstants.WORKFLOW_VERSION.equals(variable) ||
      WorkflowConstants.INSTANCE_ID.equals(variable) ||
      WorkflowConstants.ACTIVE_NODES.equals(variable) ||
      WorkflowConstants.DESCRIPTION.equals(variable) ||
      WorkflowConstants.STATE.equals(variable) ||
      WorkflowConstants.ERRORS.equals(variable) ||
      WorkflowConstants.HELP_BUTTON_URL.equals(variable) ||
      WorkflowConstants.EXIT_BUTTON_ENABLED.equals(variable) ||
      WorkflowConstants.DESTROY_BUTTON_ENABLED.equals(variable) ||
      WorkflowConstants.TERMINATION_MESSAGE.equals(variable) ||
      WorkflowConstants.TERMINATION_FORM.equals(variable) ||
      WorkflowConstants.TERMINATION_ICON.equals(variable) ||
      WorkflowConstants.FAIL_MESSAGE.equals(variable) ||
      WorkflowConstants.FAIL_FORM.equals(variable) ||
      WorkflowConstants.FAIL_ICON.equals(variable) ||
      WorkflowConstants.TRANSLATION_ENABLED.equals(variable) ||
      WorkflowConstants.START_DATE_TIME.equals(variable) ||
      WorkflowConstants.INVOKER_INSTANCE_ID.equals(variable) ||
      WorkflowConstants.INVOKER_NODE_ID.equals(variable) ||
      WorkflowConstants.HEADER_FORM.equals(variable) ||
      variable.startsWith(WorkflowConstants.INVOCATION_PREFIX) ||
      variable.startsWith(WorkflowConstants.WRITE_ACCESS_PREFIX +
        WorkflowConstants.FORM_PREFIX) ||
      checkVariableAccess(variable, true, true, roles);
  }

  public boolean canWriteVariable(String variable, Set roles)
  {
    return checkVariableAccess(variable, false, true, roles);
  }

  public boolean isUpdateOnlyVariable(String variable)
  {
    boolean updateOnly = false;
    Iterator iter = entrySet().iterator();
    while (iter.hasNext() && !updateOnly)
    {
      Map.Entry entry = (Map.Entry)iter.next();
      String key = (String)entry.getKey();
      if (key.startsWith(WorkflowConstants.UPDATE_ONLY_PREFIX))
      {
        String pattern = convertPattern((String)entry.getValue());
        updateOnly = variable.matches(pattern);
      }
    }
    return updateOnly;
  }

  private boolean checkVariableAccess(String variable,
    boolean read, boolean write, Set roles)
  {
    boolean enabled = roles.contains(WorkflowConstants.WORKFLOW_ADMIN_ROLE);

    Iterator iter = entrySet().iterator();
    while (iter.hasNext() && !enabled)
    {
      Map.Entry entry = (Map.Entry)iter.next();
      String key = (String)entry.getKey();
      Object value = entry.getValue();
      if (key.startsWith(WorkflowConstants.READ_ACCESS_PREFIX) && read ||
          key.startsWith(WorkflowConstants.WRITE_ACCESS_PREFIX) && write)
      {
        String rule = String.valueOf(value);
        String role = rule.substring(0, 20).trim();
        if (roles.contains(role))
        {
          String pattern = convertPattern(rule.substring(20));
          enabled = variable.matches(pattern);
        }
      }
    }
    return enabled;
  }

  private String convertPattern(String pattern)
  {
    pattern = pattern.trim();
    pattern = pattern.replaceAll("%", ".*");
    return pattern;
  }
}
