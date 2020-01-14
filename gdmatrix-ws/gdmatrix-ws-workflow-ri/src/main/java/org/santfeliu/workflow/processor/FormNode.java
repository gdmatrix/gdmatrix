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
package org.santfeliu.workflow.processor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.matrix.workflow.WorkflowConstants;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.santfeliu.util.Properties;
import org.santfeliu.util.TextUtils;
import org.santfeliu.util.script.ScriptableBase;
import org.santfeliu.util.template.Template;
import org.santfeliu.workflow.WorkflowActor;
import org.santfeliu.workflow.WorkflowException;
import org.santfeliu.workflow.WorkflowInstance;
import org.santfeliu.workflow.WorkflowNode;
import static org.santfeliu.workflow.WorkflowNode.WAIT_OUTCOME;
import org.santfeliu.workflow.form.Form;
import org.santfeliu.workflow.form.FormFactory;


/**
 *
 * @author unknown
 */
public class FormNode extends org.santfeliu.workflow.node.FormNode 
  implements NodeProcessor
{
  
  @Override
  public String process(WorkflowInstance instance, WorkflowActor actor)
    throws Exception
  {
    String result;   
    String formVar = WorkflowConstants.FORM_PREFIX + getId();
    String formValue = (String)instance.get(formVar);
    Form form;
    
    if (isCancelled(instance)) // check for cancellation
    {
      String outcome = evaluateOutcome(instance);
      instance.put(formVar, null);
      addForwardVariable(instance, true); // for backward compatibility
      removeAccessRules(instance);
      result = outcome;
    }
    else if (formValue == null) // not created yet, create it
    {
      form = FormFactory.createInstance(formType);
      initForm(form, instance, "");
      instance.put(formVar, FormFactory.format(form));
      addAccessRules(instance, form);
      result = WAIT_OUTCOME;
    }
    else // it was created, check for user response
    {
      form = FormFactory.parse(formValue);
      form.setVariable(formVar);

      String state = form.getState();
      if (WorkflowConstants.SHOW_STATE.equals(state))
      {
        // wait for user response
        result = WAIT_OUTCOME;
      }
      else if (WorkflowConstants.FORWARD_STATE.equals(state))
      {
        addActorVariable(instance, actor);
        String error = checkValues(instance);
        if (error.length() > 0) // show form again
        {
          form = FormFactory.createInstance(formType);
          initForm(form, instance, error);
          instance.put(formVar, FormFactory.format(form));
          addAccessRules(instance, form);
          result = WAIT_OUTCOME;
        }
        else
        {
          String outcome = evaluateOutcome(instance);
          instance.put(formVar, null);
          addForwardVariable(instance, true); // for backward compatibility
          removeAccessRules(instance);
          result = outcome;
        }
      }
      else if (WorkflowConstants.BACKWARD_STATE.equals(state))
      {
        instance.put(formVar, null);
        addForwardVariable(instance, false); // for backward compatibility
        removeAccessRules(instance);
        result = BACKWARD_OUTCOME;
      }
      else throw new WorkflowException("Invalid form state");
    }
    return result;
  }

  public void initForm(Form form, WorkflowInstance instance, String error)
  {
    String formVar = WorkflowConstants.FORM_PREFIX + getId();
    
    // set form description
    String desc = getDescription();
    if (desc == null || desc.trim().length() == 0)
    {
      desc = getId() + " " + formType;
    }
    // evaluate form paramters
    Properties finalParams = new Properties();
    Template.merge(parameters, finalParams, instance);

    // prepare form
    form.setVariable(formVar);
    form.setDescription(desc);
    form.setType(getFormType());
    form.setForwardEnabled(isForwardEnabled());
    form.setBackwardEnabled(isBackwardEnabled());
    form.setParameters(finalParams);
    form.setGroup(group);
    form.setError(error);
    form.setState(WorkflowConstants.SHOW_STATE);
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
  
  public Set getRolesSet(WorkflowInstance instance)
  {
    return parseElements(roles, instance);
  }  
  
  private void addForwardVariable(WorkflowInstance instance, boolean value)
  {
    WorkflowNode.Transition transitions[] = getTransitions();
    boolean usesOutcomes = false;
    int i = 0;
    while (i < transitions.length && !usesOutcomes)
    {
      WorkflowNode.Transition transition = transitions[i++];
      usesOutcomes = !transition.isAnyOutcome() && 
        !transition.isErrorOutcome();
    }
    if (!usesOutcomes && isForwardEnabled() && isBackwardEnabled())
    {
      instance.put(FORM_FORWARD + "_" + getId(), value);
    }
  }
  
  private void addActorVariable(WorkflowInstance instance, WorkflowActor actor)
  {
    if (!StringUtils.isBlank(actorVariable))
    {
      instance.put(actorVariable, actor.getName());    
    }
  }

  private void addAccessRules(WorkflowInstance instance, Form form)
  {
    int ruleNum = 0;

    Set readVars = parseElements(readVariables, instance);
    Set writeVars = parseElements(writeVariables, instance);
   
    // add read/write automatic FORM vars
    readVars.addAll(form.getReadVariables());
    writeVars.addAll(form.getWriteVariables());
    
    Set rolesSet = getRolesSet(instance); // execution roles    
    Set viewRolesSet = parseElements(viewRoles, instance);
    Set editRolesSet = parseElements(editRoles, instance);
    if (viewRolesSet.isEmpty()) viewRolesSet = rolesSet;
    if (editRolesSet.isEmpty()) editRolesSet = rolesSet;
    
    // add edit rules
    Iterator iter = editRolesSet.iterator();
    while (iter.hasNext())
    {
      String role = (String)iter.next();
      // add FORM var rule
      addRule(instance, ruleNum++, true, role, 
        WorkflowConstants.FORM_PREFIX + getId());

      // add rules to instance
      Iterator readIter = readVars.iterator();
      while (readIter.hasNext())
      {
        String variable = (String)readIter.next();
        addRule(instance, ruleNum++, false, role, variable);
      }

      Iterator writeIter = writeVars.iterator();
      while (writeIter.hasNext())
      {
        String variable = (String)writeIter.next();
        addRule(instance, ruleNum++, true, role, variable);
      }
    }

    // add view rules    
    iter = viewRolesSet.iterator();
    while (iter.hasNext())
    {
      String role = (String)iter.next();
      if (!editRolesSet.contains(role))
      {
        // add FORM var rule
        addRule(instance, ruleNum++, false, role, 
          WorkflowConstants.FORM_PREFIX + getId());

        // add rules to instance
        Iterator readIter = readVars.iterator();
        while (readIter.hasNext())
        {
          String variable = (String)readIter.next();
          addRule(instance, ruleNum++, false, role, variable);
        }

        Iterator writeIter = writeVars.iterator();
        while (writeIter.hasNext())
        {
          String variable = (String)writeIter.next();
          addRule(instance, ruleNum++, false, role, variable);
        }
      }
    }
  }
  
  private void removeAccessRules(WorkflowInstance instance)
  {
    String readPrefix = WorkflowConstants.READ_ACCESS_PREFIX + 
      WorkflowConstants.FORM_PREFIX + getId() + "_";
    String writePrefix = WorkflowConstants.WRITE_ACCESS_PREFIX + 
      WorkflowConstants.FORM_PREFIX + getId() + "_";

    Object[] variables = instance.keySet().toArray();
    for (int i = 0; i < variables.length; i++)
    {
      String variable = (String)variables[i];
      if (variable.startsWith(readPrefix) ||
          variable.startsWith(writePrefix))
      {
        instance.put(variable, null);
      }
    }
  }
    
  private String checkValues(WorkflowInstance instance)
    throws Exception
  {
    String error = "";

    if (checkExpression != null && checkExpression.trim().length() > 0)
    {
      Context cx = ContextFactory.getGlobal().enterContext();
      try
      {
        String mexpr = Template.create(checkExpression).merge(instance);
        Scriptable scope = new ScriptableBase(cx, instance);
        Object result = cx.evaluateString(scope, mexpr, "<cond>", 1, null);
        if (result instanceof String)
        {
          error = result.toString();
        }
      }
      finally
      {
        Context.exit();
      }
    }
    return error;
  }
  
  private boolean isCancelled(WorkflowInstance instance)
    throws Exception
  {
    boolean cancelled = false;
  
    if (cancelExpression != null && cancelExpression.trim().length() > 0)
    {
      Context cx = ContextFactory.getGlobal().enterContext();
      try
      {
        String mcondition = Template.create(cancelExpression).merge(instance);
        Scriptable scope = new ScriptableBase(cx, instance);
        Object result = cx.evaluateString(scope, mcondition, "<cond>", 1, null);
        if (result instanceof Boolean)
        {
          cancelled = ((Boolean)result).booleanValue();
        }
      }
      finally
      {
        Context.exit();
      }
    }
    return cancelled;
  }

  private String evaluateOutcome(WorkflowInstance instance)
    throws Exception
  {
    String outcome = FORWARD_OUTCOME;
  
    if (outcomeExpression != null && outcomeExpression.trim().length() > 0)
    {
      Context cx = ContextFactory.getGlobal().enterContext();
      try
      {
        String mexpr = Template.create(outcomeExpression).merge(instance);
        Scriptable scope = new ScriptableBase(cx, instance);
        Object result = cx.evaluateString(scope, mexpr, "<cond>", 1, null);
        if (result != null)
        {
          outcome = result.toString();
        }
      }
      finally
      {
        Context.exit();
      }
    }
    return outcome;
  }
  
  private void addRule(WorkflowInstance instance, 
    int ruleNum, boolean write, String role, String var)
  {
    String ruleName = 
      WorkflowConstants.FORM_PREFIX + getId() + "_" + ruleNum;

    instance.addRule(ruleName, write, role, var);
  }
}
