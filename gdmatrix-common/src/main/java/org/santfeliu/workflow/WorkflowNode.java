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


import java.awt.geom.Point2D;

import java.text.FieldPosition;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import org.santfeliu.workflow.util.PointsFormat;


public abstract class WorkflowNode implements Cloneable
{
  public static final String ANY_OUTCOME = "";
  public static final String END_OUTCOME = "end";
  public static final String WAIT_OUTCOME = "wait";
  public static final String ERROR_OUTCOME = "error";
  public static final String CONTINUE_OUTCOME = "continue";
  
  Workflow workflow;
    
  protected String id;
  protected String description;
  protected boolean immediate;
  protected boolean hidden;
  protected String roles;
  protected LinkedList transitions;
  protected Point2D[] points;

  public WorkflowNode()
  {
    transitions = new LinkedList();
  }
  
  public abstract String getType();
  
  public Workflow getWorkflow()
  {
    return workflow;
  }

  public final void setId(String nodeId)
  {
    this.id = nodeId;
  }

  public final String getId()
  {
    return id;
  }

  public final void setDescription(String description)
  {
    this.description = description;
  }

  public final String getDescription()
  {
    return description;
  }

  public final boolean isImmediate()
  {
    return immediate;
  }
  
  public final void setImmediate(boolean immediate)
  {
    this.immediate = immediate;
  }

  public void setHidden(boolean hidden)
  {
    this.hidden = hidden;
  }

  public boolean isHidden()
  {
    return hidden;
  }

  public void setRoles(String roles)
  {
    this.roles = roles;
  }

  public String getRoles()
  {
    return roles;
  }

  public void setPoints(Point2D[] points)
  {
    this.points = points;
  }

  public Point2D[] getPoints()
  {
    return points;
  }

  public final Transition addTransition(WorkflowNode nextNode)
  {
    return addTransition(ANY_OUTCOME, nextNode, null, null);
  }

  public final Transition addTransition(String outcome, WorkflowNode nextNode, 
    Point2D outcomePosition, Point2D[] points)
  {
    Transition transition = 
      new Transition(outcome, nextNode, outcomePosition, points);
    transitions.add(transition);
    return transition;
  }
  
  public final void removeTransition(WorkflowNode node)  
  {
    transitions.remove(node);
  }
  
  public final boolean hasTransitionTo(WorkflowNode node)
  {
    boolean found = false;
    Iterator iter = transitions.iterator();
    while (iter.hasNext() && !found)
    {
      Transition transition = (Transition)iter.next();
      if (transition.getNextNode() == node) found = true;
    }
    return found;
  }
  
  public final WorkflowNode.Transition[] getTransitions()
  {
    return (WorkflowNode.Transition[])transitions.toArray(
      new Transition[transitions.size()]);
  }
  
  public final boolean existsPathTo(WorkflowNode node)
  {
    return findNode(this, node, new HashSet());
  }
  
  public boolean containsText(String text)
  {
    if (id != null && id.equalsIgnoreCase(text)) return true;
    if (description != null && description.contains(text)) return true;
    return false;
  }

  public String toString()
  {
    StringBuffer buffer = new StringBuffer();
    buffer.append("{id=\"");
    buffer.append(id);
    buffer.append("\", type=");
    buffer.append(this.getClass().getName());
    buffer.append(", description=\"");
    buffer.append(description == null ? "" : description);
    buffer.append("\", points={");
    if (points != null)
    {
      PointsFormat format = new PointsFormat();
      format.format(points, buffer, new FieldPosition(0));
    }
    buffer.append("}, nextnodes={");
    Iterator iter = transitions.iterator();
    if (iter.hasNext())
    {
      Transition transition = (Transition)iter.next();
      buffer.append("\"");
      buffer.append(transition);
      buffer.append("\"");
      while (iter.hasNext())
      {
        buffer.append(", ");
        transition = (Transition)iter.next();
        buffer.append("\"");
        buffer.append(transition);
        buffer.append("\"");
      }
    }
    buffer.append("}}");
    return buffer.toString();
  }

  /**
 *
 * @author unknown
 */
public class Transition
  {
    String outcome;
    WorkflowNode nextNode;
    Point2D outcomePosition;
    Point2D[] points;

    Transition(WorkflowNode nextNode)
    {
      this(ANY_OUTCOME, nextNode, null, null);
    }

    Transition(String outcome, WorkflowNode nextNode, 
      Point2D outcomePosition, Point2D[] points)
    {
      this.outcome = outcome;
      this.nextNode = nextNode;
      this.outcomePosition = outcomePosition;
      this.points = points;
    }
    
    public WorkflowNode getSourceNode()
    {
      return WorkflowNode.this;
    }

    public WorkflowNode getNextNode()
    {
      return nextNode;
    }
    
    public String getOutcome()
    {
      return outcome;
    }
    
    public void setOutcome(String outcome)
    {
      this.outcome = outcome;
    }
    
    public String getNextNodeId()
    {
      return nextNode.getId();
    }
    
    public void setPoints(Point2D[] points)
    {
      this.points = points;
    }
    
    public Point2D[] getPoints()
    {
      return points;
    }

    public void setOutcomePosition(Point2D position)
    {
      this.outcomePosition = position;
    }
    
    public Point2D getOutcomePosition()
    {
      return outcomePosition;
    }

    public boolean isAnyOutcome()
    {
      return ANY_OUTCOME.equals(outcome);
    }

    public boolean isErrorOutcome()
    {
      return ERROR_OUTCOME.equals(outcome);
    }

    public void remove()
    {
      transitions.remove(this);
    }

    public String toString()
    {
      return outcome;
    }
  }


  // protected methods
  
  protected boolean findNode(WorkflowNode node, 
    WorkflowNode findNode, Set explored)
  {
    if (node == findNode) return true;
    
    explored.add(node);
    boolean found = false;
    Iterator iter = node.transitions.iterator();
    while (iter.hasNext() && !found)
    {
      Transition transition = (Transition)iter.next();
      WorkflowNode nextNode = transition.getNextNode();
      if (!explored.contains(nextNode))
      {
        found = findNode(nextNode, findNode, explored);
      }
    }
    return found;
  }
  

  @Override
  public Object clone() throws CloneNotSupportedException
  {
    WorkflowNode newNode = (WorkflowNode)super.clone();
    newNode.workflow = null;
    newNode.transitions = new LinkedList();
    return newNode;
  }
}
