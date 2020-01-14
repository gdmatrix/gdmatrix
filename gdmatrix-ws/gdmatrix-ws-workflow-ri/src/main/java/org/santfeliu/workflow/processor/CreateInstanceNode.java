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

import java.util.HashMap;
import java.util.Map;

import org.matrix.workflow.WorkflowConstants;

import org.santfeliu.util.Properties;
import org.santfeliu.util.template.Template;
import org.santfeliu.workflow.WorkflowActor;
import org.santfeliu.workflow.WorkflowAdmin;
import org.santfeliu.workflow.WorkflowEngine;
import org.santfeliu.workflow.WorkflowException;
import org.santfeliu.workflow.WorkflowInstance;


/**
 *
 * @author unknown
 */
public class CreateInstanceNode extends org.santfeliu.workflow.node.CreateInstanceNode 
  implements NodeProcessor
{

  

  @Override
  public String process(WorkflowInstance instance, WorkflowActor actor)
    throws Exception
  {
    String outcome;
    String newInstanceId =
      (String)instance.get(WorkflowConstants.INVOCATION_PREFIX + getId());
    // first process
    if (newInstanceId == null)
    {
      String wn = Template.create(workflowName).merge(instance);
      Map map = getFinalParameters(instance);
      map.put(WorkflowConstants.INVOKER_INSTANCE_ID, instance.getInstanceId());
      map.put(WorkflowConstants.INVOKER_NODE_ID, getId());
      
      WorkflowEngine engine = instance.getEngine();
      newInstanceId = engine.createInstance(wn, map, actor);
      if (waitResult)
      {
        instance.put(WorkflowConstants.INVOCATION_PREFIX + getId(), 
          newInstanceId);
        outcome = WAIT_OUTCOME;
      }
      else
      {
        outcome = CONTINUE_OUTCOME;
      }
    }
    else // subsequent process
    {
      WorkflowAdmin admin = new WorkflowAdmin();
      HashMap variables = new HashMap();
      instance.getEngine().getVariables(newInstanceId, variables, admin);
      // newInstanceId terminated?
      if (variables.get(WorkflowConstants.ACTIVE_NODES) == null)
      {
        instance.put(WorkflowConstants.INVOCATION_PREFIX + getId(), null);
        if (variables.get(WorkflowConstants.ERRORS) != null)
          throw new WorkflowException(
            "Child instance [" + newInstanceId + "] terminated with errors");
        outcome = CONTINUE_OUTCOME;
      }
      else
      {
        outcome = WAIT_OUTCOME;
      }
    }
    return outcome;
  }
  
  private Map getFinalParameters(WorkflowInstance instance)
  {
    Properties finalParameters = new Properties();
    Template.merge(parameters, finalParameters, instance);
    return finalParameters;
  }
}
