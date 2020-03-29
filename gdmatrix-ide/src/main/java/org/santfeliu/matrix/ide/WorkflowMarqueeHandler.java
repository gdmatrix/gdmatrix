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
package org.santfeliu.matrix.ide;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.jgraph.JGraph;
import org.jgraph.graph.BasicMarqueeHandler;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.PortView;

import org.santfeliu.swing.undo.BeanUndoableEdit;
import org.santfeliu.workflow.WorkflowNode;
import org.santfeliu.workflow.swing.NodeEditor;
import org.santfeliu.workflow.swing.NodeEditorDialog;
import org.santfeliu.workflow.swing.NodeEditorFactory;
import org.santfeliu.matrix.ide.action.OpenDesignAction;
import org.santfeliu.swing.palette.Palette;
import org.santfeliu.workflow.swing.graph.WorkflowEdge;
import org.santfeliu.workflow.swing.graph.WorkflowVertex;


/**
 *
 * @author realor
 */
public class WorkflowMarqueeHandler extends BasicMarqueeHandler
{
  private WorkflowPanel workflowPanel;
  private Point startPoint;
  private Point endPoint;
  private PortView startPort;
  private PortView endPort;

  public WorkflowMarqueeHandler(WorkflowPanel workflowPanel)
  {
    this.workflowPanel = workflowPanel;
  }

  @Override
  public boolean isForceMarqueeEvent(MouseEvent event)
  {
    JGraph graph = (JGraph)event.getSource();

    if (workflowPanel.getMainPanel().
        getPalette().getSelectedElement() != null)
      return true;

    if (event.getClickCount() >= 2 &&
      (event.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == 
        MouseEvent.CTRL_DOWN_MASK)
    {
      return true;
    }

    Object cell = graph.getFirstCellForLocation(event.getX(), event.getY());

    if (cell instanceof WorkflowEdge)
    {
      return false;
    }

    if (event.getClickCount() >= 2)
      return true;

    startPort = getSourcePortAt(graph, event.getPoint());
    if (startPort != null && graph.isPortsVisible())
      return true;

    if (SwingUtilities.isRightMouseButton(event))
      return true;
  
    return super.isForceMarqueeEvent(event);
  }

  @Override
  public void mousePressed(java.awt.event.MouseEvent event) 
  {
    JGraph graph = (JGraph)event.getSource();
    if (isAddingNode())
    {
      workflowPanel.addNode(event.getPoint());
      getPalette().clearSelectedElement();
    }
    else
    {
      if (startPort != null && graph.isPortsVisible())
      {
        startPoint = event.getPoint();
      }
      else super.mousePressed(event);
    }
  }

  @Override
  public void mouseReleased(java.awt.event.MouseEvent event)
  {
    JGraph graph = (JGraph)event.getSource();
    if (event.getClickCount() >= 2)
    {
      Object cell = graph.getFirstCellForLocation(event.getX(), event.getY());
      if (cell instanceof WorkflowEdge)
      {
        if ((event.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == 
            MouseEvent.CTRL_DOWN_MASK)
        {
          WorkflowEdge edge = (WorkflowEdge)cell;
          WorkflowNode.Transition transition = edge.getTransition();
          if (transition.isErrorOutcome())
          {
            transition.setOutcome(WorkflowNode.ANY_OUTCOME);
          }
          else
          {
            transition.setOutcome(WorkflowNode.ERROR_OUTCOME);
          }
        }
      }
      else if (cell instanceof WorkflowVertex)
      {
        WorkflowVertex vertex = (WorkflowVertex)cell;
        final WorkflowNode node = vertex.getNode();
        if ((event.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) == 
            MouseEvent.SHIFT_DOWN_MASK)
        {
          OpenDesignAction action = new OpenDesignAction();
          action.setIDE(workflowPanel.getMainPanel().getIDE());
          action.actionPerformed(new ActionEvent(this, 0, "openDesign"));
        }
        else
        {
          SwingUtilities.invokeLater(new Runnable()
          {
            public void run()
            {
              editNode(node);
            }
          });
        }
      }
    }
    else if (startPort != null && endPoint != null && graph.isPortsVisible())
    {
      endPort = graph.getPortViewAt(endPoint.x, endPoint.y);
      if (startPort != null && endPort != null && startPort != endPort)
      {
        DefaultPort p1 = (DefaultPort)startPort.getCell();
        DefaultPort p2 = (DefaultPort)endPort.getCell();
        WorkflowVertex sourceCell = (WorkflowVertex)p1.getParent();
        WorkflowVertex targetCell = (WorkflowVertex)p2.getParent();
        if (canConnect(sourceCell, targetCell))
        {
          if (targetCell.getNode().getRoles() == null || 
              targetCell.getNode().getRoles().trim().length() == 0)
          {
            targetCell.getNode().setRoles(sourceCell.getNode().getRoles());
          }
          ConnectionSet cs = new ConnectionSet();
          WorkflowEdge edge = new WorkflowEdge();
          cs.connect(edge, p1, p2);

          Map attributes = new HashMap();
          Map emap = new HashMap();
          GraphConstants.setLineEnd(emap, GraphConstants.ARROW_TECHNICAL);
          GraphConstants.setLabelAlongEdge(emap, false);
          GraphConstants.setEditable(emap, true);
          GraphConstants.setLineStyle(emap, GraphConstants.STYLE_ORTHOGONAL);
          GraphConstants.setBendable(emap, true);
          attributes.put(edge, emap);
          graph.getGraphLayoutCache().insert(new Object[]
                                             { edge }, attributes, cs, null,
                                             null);          
        }
        else
        {
          graph.repaint();
          JOptionPane.showMessageDialog(graph, "Nodes already connected");
        }
      }
      else
      {
        graph.repaint();
      }
    }
    super.mouseReleased(event);
    startPort = null;
    endPort = null;
    startPoint = null;
    endPoint = null;
  }

  @Override
  public void mouseDragged(MouseEvent event)
  {
    if (startPoint != null)
    {
      JGraph graph = (JGraph)event.getSource();
      Graphics g = graph.getGraphics();
      g.setXORMode(Color.yellow);
      if (endPoint != null)
      {
        g.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
      }
      endPoint = event.getPoint();
      g.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);

      PortView newPort = graph.getPortViewAt(endPoint.x, endPoint.y);
      if (newPort != startPort)
      {
        // enter in new cell
        if (newPort != null && newPort != endPort)
        {
          paintPort(graph, g, newPort);
        }
        // exit from cell
        else if (newPort == null && endPort != null)
        {
          paintPort(graph, g, endPort);
        }
        endPort = newPort;
      }
    }
    else super.mouseDragged(event);
  }

  public PortView getSourcePortAt(JGraph graph, Point2D point)
  {
    graph.setJumpToDefaultPort(false);
    PortView result;
    try
    {
      result = graph.getPortViewAt(point.getX(), point.getY());
    }
    finally
    {
      graph.setJumpToDefaultPort(true);
    }
    return result;
  }

  protected boolean canConnect(DefaultGraphCell sc, DefaultGraphCell tc)
  {
    WorkflowNode sourceNode = (WorkflowNode)sc.getUserObject();
    WorkflowNode targetNode = (WorkflowNode)tc.getUserObject();
    if (sourceNode.hasTransitionTo(targetNode))
    {
      return false;
    }
    return true;
  }

  protected void paintPort(JGraph graph, Graphics g, PortView port)
  {
    if (port != null)
    {
      boolean o = (GraphConstants.getOffset(port.getAttributes()) != null);
      Rectangle2D r = (o) ? port.getBounds() : 
                            port.getParentView().getBounds();
      r = graph.toScreen((Rectangle2D) r.clone());
      r.setFrame(r.getX() - 3, r.getY() - 3, 
                 r.getWidth() + 6, r.getHeight() + 6);
      ((Graphics2D)g).draw(r);
    }
  }
  
  protected void editNode(WorkflowNode node)
  {
    NodeEditor editor = NodeEditorFactory.getNodeEditor(node);
    Frame frame = 
      (Frame)SwingUtilities.getWindowAncestor(workflowPanel);

    BeanUndoableEdit edit = new BeanUndoableEdit(node);
    edit.beforeChange();
    NodeEditorDialog dialog = new NodeEditorDialog(frame);
    int result = dialog.editNode(workflowPanel, node, editor);
    if (result == NodeEditorDialog.OK_OPTION)
    {
      workflowPanel.getGraph().repaint();
      workflowPanel.getMainPanel().setEditObject(node);
      workflowPanel.setModified(true);
      edit.afterChange();
      workflowPanel.getUndoManager().addEdit(edit);
    }
  }

  protected boolean isAddingNode()
  {
    return getPalette().getSelectedElement() != null;
  }

  protected Palette getPalette()
  {
    return workflowPanel.getMainPanel().getPalette();
  }
}
