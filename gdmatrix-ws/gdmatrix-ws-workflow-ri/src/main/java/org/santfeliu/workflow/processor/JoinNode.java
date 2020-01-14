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

import java.util.StringTokenizer;

import org.matrix.workflow.WorkflowConstants;

import org.santfeliu.workflow.Workflow;
import org.santfeliu.workflow.WorkflowActor;
import org.santfeliu.workflow.WorkflowEngine;
import org.santfeliu.workflow.WorkflowInstance;
import org.santfeliu.workflow.WorkflowNode;

/**
 *
 * @author unknown
 */
public class JoinNode extends org.santfeliu.workflow.node.JoinNode 
  implements NodeProcessor
{
  
  @Override
  public String process(WorkflowInstance instance, WorkflowActor actor)
    throws Exception
  {
    String activeNodes = (String)instance.get(WorkflowConstants.ACTIVE_NODES);
    if (activeNodes == null) return END_OUTCOME;

    WorkflowEngine engine = instance.getEngine();
    Workflow workflow = engine.getWorkflow(
      instance.getWorkflowName(), instance.getWorkflowVersion());

    WorkflowNode splitNode = null;

    if (splitNodeId != null)
    {
      if (splitNodeId.trim().length() > 0)
      {
        splitNode = workflow.getNode(splitNodeId);
      }
    }

    StringTokenizer tokenizer = new StringTokenizer(activeNodes);
    boolean found = false;
    while (tokenizer.hasMoreTokens() && !found)
    {
      String nodeId = tokenizer.nextToken();
      WorkflowNode node = workflow.getNode(nodeId);
      if (node != this)
      {
        found = node.existsPathTo(this);
        if (found && splitNode != null)
        {
          found = splitNode.existsPathTo(node);
        }
      }
    }
    return found ? WAIT_OUTCOME : CONTINUE_OUTCOME;
  }
}
