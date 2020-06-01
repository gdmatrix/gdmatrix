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
package org.santfeliu.workflow.swing.graph;

import org.jgraph.graph.DefaultEdge;
import org.santfeliu.workflow.WorkflowNode;

/**
 *
 * @author realor
 */
public class WorkflowEdge extends DefaultEdge
{
  // Transition -> super.getUserObject
  private String outcome = WorkflowNode.ANY_OUTCOME;

  public WorkflowEdge()
  {
  }

  public WorkflowEdge(WorkflowNode.Transition transition)
  {
    super.setUserObject(transition);
    this.outcome = transition.getOutcome();
  }

  @Override
  public void setUserObject(Object o)
  {
    if (o instanceof WorkflowNode.Transition)
    {
      super.setUserObject(o);
      this.outcome = ((WorkflowNode.Transition)o).getOutcome();
    }
    else if (o instanceof String)
    {
      setOutcome((String)o);
    }
  }

  public WorkflowNode.Transition getTransition()
  {
    return (WorkflowNode.Transition)super.getUserObject();
  }

  // sometimes edge contains outcome but no Transition
  public void setOutcome(String outcome)
  {
    this.outcome = (outcome == null) ? WorkflowNode.ANY_OUTCOME : outcome;
    Object t = super.getUserObject();
    if (t instanceof WorkflowNode.Transition)
    {
      WorkflowNode.Transition transition = (WorkflowNode.Transition)t;
      transition.setOutcome(outcome);
    }
  }

  public String getOutcome()
  {
    return outcome;
  }
}
