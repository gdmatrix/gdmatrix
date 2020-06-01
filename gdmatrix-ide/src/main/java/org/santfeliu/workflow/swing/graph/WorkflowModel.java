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

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.Port;
import org.santfeliu.workflow.Workflow;
import org.santfeliu.workflow.WorkflowNode;

/**
 *
 * @author realor
 */
public class WorkflowModel extends DefaultGraphModel implements Serializable
{
  private final Workflow workflow;
  private boolean updatingModel;

  public WorkflowModel(Workflow workflow)
  {
    this.workflow = workflow;
    updateGraph();
  }

  public Workflow getWorkflow()
  {
    return workflow;
  }

  private void updateGraph()
  {
    updatingModel = true;

    Rectangle2D defaultRect = new Rectangle2D.Double(10, 10, 100, 40);
    Map cellsMap = new HashMap();

    Map attributeMap = new HashMap();
    ConnectionSet cs = new ConnectionSet();

    WorkflowNode[] nodes = workflow.getNodes();
    for (int i = 0; i < nodes.length; i++)
    {
      WorkflowNode node = nodes[i];
      WorkflowVertex vertex = new WorkflowVertex(node);
      vertex.add(new DefaultPort());

      Map map = new HashMap();
      Rectangle2D rect = defaultRect;
      Point2D[] points = node.getPoints();
      if (points != null)
      {
        if (points.length >= 2)
        {
          double x = points[0].getX();
          double y = points[0].getY();
          double width = points[1].getX() - x;
          double height = points[1].getY() - y;
          rect = new Rectangle2D.Double(x, y, width, height);
        }
      }
      GraphConstants.setBounds(map, rect);
      GraphConstants.setResize(map, false);
      GraphConstants.setEditable(map, false);
      GraphConstants.setOpaque(map, true);
      GraphConstants.setBackground(map, Color.yellow);

      attributeMap.put(vertex, map);
      cellsMap.put(node, vertex);
    }

    for (int i = 0; i < nodes.length; i++)
    {
      WorkflowNode node = nodes[i];
      DefaultGraphCell sourceCell = (DefaultGraphCell)cellsMap.get(node);
      WorkflowNode.Transition transitions[] = node.getTransitions();
      for (int t = 0; t < transitions.length; t++)
      {
        WorkflowNode.Transition transition = transitions[t];
        WorkflowNode nextNode = transition.getNextNode();
        DefaultGraphCell targetCell = (DefaultGraphCell)cellsMap.get(nextNode);

        WorkflowEdge edge = new WorkflowEdge(transition);
        cs.connect(edge,
          (Port)sourceCell.getFirstChild(),
          (Port)targetCell.getFirstChild());

        cellsMap.put(edge, edge);

        Map emap = new HashMap();
        GraphConstants.setLineEnd(emap, GraphConstants.ARROW_TECHNICAL);
        GraphConstants.setLabelAlongEdge(emap, false);
        GraphConstants.setEditable(emap, true);
        GraphConstants.setLineStyle(emap, GraphConstants.STYLE_ORTHOGONAL);
        GraphConstants.setBendable(emap, true);
        Point2D[] points = transition.getPoints();
        if (points != null && points.length > 1)
        {
          ArrayList list = new ArrayList();
          for (int p = 0; p < points.length; p++)
          {
            list.add(points[p]);
          }
          GraphConstants.setPoints(emap, list);
        }
        if (transition.getOutcomePosition() != null)
        {
          GraphConstants.setLabelPosition(emap, transition.getOutcomePosition());
        }
        attributeMap.put(edge, emap);
      }
    }
    Collection col = cellsMap.values();
    Object[] cells = col.toArray(new Object[col.size()]);
    insert(cells, attributeMap, cs, null, null);
    updatingModel = false;
  }

  @Override
  protected Map handleAttributes(Map attributes)
  {
    if (!updatingModel)
    {
      if (attributes != null)
      {
        Iterator iter = attributes.keySet().iterator();
        while (iter.hasNext())
        {
          Object o = iter.next();
          Map map = (Map)attributes.get(o);
          if (o instanceof DefaultPort)
          {
            // do nothing
          }
          else if (o instanceof WorkflowEdge)
          {
            WorkflowEdge edge = (WorkflowEdge)o;
            WorkflowNode.Transition transition = edge.getTransition();
            List list = GraphConstants.getPoints(map);
            if (list != null)
            {
              Point2D[] points =
                (Point2D[])list.toArray(new Point2D[list.size()]);
              transition.setPoints(points);
            }
            Point2D outcomePosition = GraphConstants.getLabelPosition(map);
            if (outcomePosition != null)
            {
              transition.setOutcomePosition(outcomePosition);
            }
          }
          else if (o instanceof WorkflowVertex)
          {
            WorkflowVertex vertex = (WorkflowVertex)o;
            if (vertex != null)
            {
              WorkflowNode node = vertex.getNode();
              Rectangle2D rect = GraphConstants.getBounds(map);
              if (rect != null)
              {
                Point2D[] points = new Point2D[2];
                points[0] = new Point2D.Double(rect.getX(), rect.getY());
                points[1] = new Point2D.Double(rect.getX() + rect.getWidth(),
                                               rect.getY() + rect.getHeight());
                node.setPoints(points);
              }
            }
          }
        }
      }
    }
    return super.handleAttributes(attributes);
  }

  @Override
  protected Object[] handleInsert(Object[] objs)
  {
    if (!updatingModel && objs != null)
    {
      for (int i = 0; i < objs.length; i++)
      {
        if (objs[i] instanceof DefaultGraphCell)
        {
          DefaultGraphCell cell = (DefaultGraphCell)objs[i];
          if (cell.getUserObject() instanceof WorkflowNode)
          {
            WorkflowNode node = (WorkflowNode)cell.getUserObject();
            if (node.getId() == null)
            {
              int num = workflow.getNodesCount();
              String nodeId = String.valueOf(num);
              while (workflow.getNode(nodeId) != null)
              {
                num++;
                nodeId = String.valueOf(num);
              }
              node.setId(nodeId);
            }
            try
            {
              workflow.addNode(node);
              // TODO apply attributes !!!!
            }
            catch (Exception ex)
            {
              ex.printStackTrace();
            }
          }
        }
      }
    }
    return super.handleInsert(objs);
  }

  @Override
  protected Object[] handleRemove(Object[] objs)
  {
    if (!updatingModel && objs != null)
    {
      for (int i = 0; i < objs.length; i++)
      {
        if (objs[i] instanceof WorkflowVertex)
        {
          WorkflowVertex vertex = (WorkflowVertex)objs[i];
          WorkflowNode node = vertex.getNode();
          workflow.removeNode(node);
        }
        else if (objs[i] instanceof WorkflowEdge)
        {
          WorkflowEdge edge = (WorkflowEdge)objs[i];
          WorkflowNode.Transition transition = edge.getTransition();
          transition.remove();
        }
      }
    }
    return super.handleRemove(objs);
  }

  @Override
  protected ConnectionSet handleConnectionSet(ConnectionSet cs)
  {
    cs = super.handleConnectionSet(cs);
    if (!updatingModel)
    {
      if (cs != null)
      {
        Iterator iter = cs.connections();
        int i = 0;
        while (iter.hasNext())
        {
          ConnectionSet.Connection conn = (ConnectionSet.Connection)iter.next();
          WorkflowEdge edge = (WorkflowEdge)conn.getEdge();
          if (edge != null)
          {
            DefaultPort sourcePort = (DefaultPort)edge.getSource();
            DefaultPort targetPort = (DefaultPort)edge.getTarget();
            if (sourcePort != null && targetPort != null)
            {
              DefaultGraphCell sourceCell =
                (DefaultGraphCell)sourcePort.getParent();
              DefaultGraphCell targetCell =
                (DefaultGraphCell)targetPort.getParent();
              WorkflowNode sourceNode = (WorkflowNode)sourceCell.getUserObject();
              WorkflowNode targetNode = (WorkflowNode)targetCell.getUserObject();
              if (!sourceNode.hasTransitionTo(targetNode))
              {
                WorkflowNode.Transition transition =
                  sourceNode.addTransition(targetNode);
                ArrayList list = (ArrayList)edge.getAttributes().get(
                  GraphConstants.POINTS);
                if (list != null)
                {
                  Point2D[] points = (Point2D[])list.toArray(
                    new Point2D[list.size()]);
                  transition.setPoints(points);
                }
                transition.setOutcome(edge.getOutcome());
                edge.setUserObject(transition);
              }
            }
          }
          i++;
        }
      }
    }
    return cs;
  }

  @Override
  public Object cloneCell(Object cell)
  {
    try
    {
      if (cell instanceof WorkflowVertex)
      {
        WorkflowVertex vertex = (WorkflowVertex)cell;
        WorkflowNode node = vertex.getNode();
        WorkflowVertex newVertex = new WorkflowVertex();
        WorkflowNode newNode = (WorkflowNode)node.clone();
        newNode.setId(null); // force new id generation
        newVertex.setUserObject(newNode);
        return newVertex;
      }
      else if (cell instanceof WorkflowEdge)
      {
        WorkflowEdge edge = (WorkflowEdge)cell;
        WorkflowNode.Transition transition = edge.getTransition();
        WorkflowEdge newEdge = new WorkflowEdge();
        AttributeMap attributeMap = new AttributeMap();
        attributeMap.putAll(edge.getAttributes());
        newEdge.setOutcome(transition.getOutcome());
        newEdge.setAttributes(attributeMap);
        return newEdge;
      }
      else
      {
        return super.cloneCell(cell);
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return null;
  }
}
