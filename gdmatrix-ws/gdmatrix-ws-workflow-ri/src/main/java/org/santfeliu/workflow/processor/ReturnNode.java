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

import java.util.Map;
import org.matrix.workflow.WorkflowConstants;
import org.santfeliu.util.Properties;
import org.santfeliu.util.template.Template;
import org.santfeliu.workflow.WorkflowActor;
import org.santfeliu.workflow.WorkflowAdmin;
import org.santfeliu.workflow.WorkflowEngine;
import org.santfeliu.workflow.WorkflowInstance;


/**
 *
 * @author realor
 */
public class ReturnNode extends org.santfeliu.workflow.node.ReturnNode 
  implements NodeProcessor
{    
  @Override
  public String process(WorkflowInstance instance, WorkflowActor actor)
    throws Exception
  {
    String invokerInstanceId =
      (String)instance.get(WorkflowConstants.INVOKER_INSTANCE_ID);
    String invokerNodeId =
      (String)instance.get(WorkflowConstants.INVOKER_NODE_ID);

    if (invokerInstanceId != null && invokerNodeId != null)
    {
      WorkflowEngine engine = instance.getEngine();
      Map variables = getFinalResult(instance);
      // setVariables must be perfomed by admin user to avoid security issues
      WorkflowAdmin admin = new WorkflowAdmin();
      engine.setVariables(invokerInstanceId, variables, admin, false);
    }
    // terminate instance
    instance.setActiveNodes(null);
    return END_OUTCOME;
  }
  
  private Map getFinalResult(WorkflowInstance instance)
  {
    Properties finalResult = new Properties();
    Template.merge(result, finalResult, instance);
    return finalResult;
  }
}
