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
package org.santfeliu.workflow.swing.renderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;
import org.jgraph.JGraph;
import org.jgraph.graph.CellView;
import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.VertexView;
import org.santfeliu.workflow.WorkflowNode;
import org.santfeliu.workflow.swing.WrapLabel;
import org.santfeliu.swing.border.SouthBorder;


public abstract class AbstractNodeRenderer extends JPanel
  implements CellViewRenderer, Serializable
{
  protected static Color selectionColor = new Color(255, 255, 200);
  protected static Font headerFont = new Font("Dialog", Font.PLAIN, 12);
  protected static Font defaultFont = new Font("Dialog", Font.PLAIN, 12);

  private Border defaultBorder = 
    BorderFactory.createLineBorder(Color.black, 2);
  
  private WrapLabel defaultComponent = new WrapLabel();
  private Component centerComponent;
  
  private SouthBorder headerBorder = new SouthBorder(Color.gray, false);
  private BorderLayout borderLayout = new BorderLayout();
  private WrapLabel headerLabel = new WrapLabel();

  public AbstractNodeRenderer()
  {
  }

  public Component getRendererComponent(JGraph graph, CellView view, 
                                        boolean selected, boolean focus, 
                                        boolean preview)
  {
    setBackground(selected ? selectionColor : graph.getBackground());
    setOpaque(true);
    this.setLayout(borderLayout);
    this.removeAll();
    this.add(headerLabel, BorderLayout.NORTH);

    Object cell = view.getCell();
    if (cell instanceof DefaultGraphCell)
    {
      DefaultGraphCell dcell = (DefaultGraphCell) cell;
      Object userObject = dcell.getUserObject();
      if (userObject instanceof WorkflowNode)
      {
        WorkflowNode node = (WorkflowNode)userObject;
        String text = node.getId();
        String description = node.getDescription();
        if (description != null && description.trim().length() > 0)
        {
          description = description.replaceAll("\n", " ").trim();
          text += ": " + description;
        }
        else
        {
          text += ": " + node.getType();
        }
        headerLabel.setAlignment(WrapLabel.LEFT);
        headerLabel.setFont(headerFont);
        headerLabel.setText(text);
        headerLabel.setBorder(headerBorder);
        setBorder(getNodeBorder(node));

        centerComponent = getNodeComponent(node);
        centerComponent.setFont(getNodeFont(node));
        this.add(centerComponent, BorderLayout.CENTER);
      }
    }
    return this;
  }

  protected Component getNodeComponent(WorkflowNode node)
  {
    defaultComponent.setText(getNodeText(node));
    return defaultComponent;
  }

  protected Font getNodeFont(WorkflowNode node)
  {
    return defaultFont;
  }

  protected String getNodeText(WorkflowNode node)
  {
    return node.getType();
  }

  protected Border getNodeBorder(WorkflowNode node)
  {
    return defaultBorder;
  }

  public Point2D getPerimeterPoint(VertexView view, Point2D source, 
                                   Point2D p)
  {
    Rectangle2D bounds = view.getBounds();
    double x = bounds.getX();
    double y = bounds.getY();
    double width = bounds.getWidth();
    double height = bounds.getHeight();
    double xCenter = x + width / 2;
    double yCenter = y + height / 2;
    double dx = p.getX() - xCenter; // Compute Angle
    double dy = p.getY() - yCenter;
    double alpha = Math.atan2(dy, dx);
    double xout = 0, yout = 0;
    double pi = Math.PI;
    double pi2 = Math.PI / 2.0;
    double beta = pi2 - alpha;
    double t = Math.atan2(height, width);
    if (alpha < -pi + t || alpha > pi - t)
    { // Left edge
      xout = x;
      yout = yCenter - width * Math.tan(alpha) / 2;
    }
    else if (alpha < -t)
    { // Top Edge
      yout = y;
      xout = xCenter - height * Math.tan(beta) / 2;
    }
    else if (alpha < t)
    { // Right Edge
      xout = x + width;
      yout = yCenter + width * Math.tan(alpha) / 2;
    }
    else
    { // Bottom Edge
      yout = y + height;
      xout = xCenter + height * Math.tan(beta) / 2;
    }
    return new Point2D.Double(xout, yout);
  }

  public void revalidate()
  {
  }

  public void repaint(long tm, int x, int y, int width, int height)
  {
  }

  public void repaint(Rectangle r)
  {
  }
}
