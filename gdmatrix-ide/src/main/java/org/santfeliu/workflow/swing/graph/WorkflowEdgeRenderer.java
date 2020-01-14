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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Point2D;
import org.jgraph.graph.EdgeRenderer;
import org.santfeliu.workflow.WorkflowNode;

/**
 *
 * @author unknown
 */
public class WorkflowEdgeRenderer
  extends EdgeRenderer
{
  private static final Color ANY_OUTCOME_COLOR = Color.black;
  private static final Color SUCCESS_OUTCOME_COLOR = new Color(0, 128, 0);
  private static final Color ERROR_OUTCOME_COLOR = Color.red;

  public WorkflowEdgeRenderer()
  {
  }

  public void paint(Graphics g)
  {
    if (view.isLeaf())
    {
      Object cell = view.getCell();
      if (cell instanceof WorkflowEdge)
      {
        WorkflowEdge edge = (WorkflowEdge)cell;
        WorkflowNode.Transition transition = edge.getTransition();
        endFill = !transition.getNextNode().isImmediate();
        Graphics2D g2 = (Graphics2D)g;
        
        // paint line
        Shape edgeShape = view.getShape();
        if (edgeShape != null)
        {
          g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                              RenderingHints.VALUE_STROKE_PURE);
          setOpaque(false);
          translateGraphics(g);
          if (transition.isAnyOutcome())
            g.setColor(ANY_OUTCOME_COLOR);
          else if (transition.isErrorOutcome())
            g.setColor(ERROR_OUTCOME_COLOR);
          else
            g.setColor(SUCCESS_OUTCOME_COLOR);

          if (view.beginShape != null)
          {
            if (beginFill) g2.fill(view.beginShape);
            g2.draw(view.beginShape);
          }
          if (view.endShape != null)
          {
            if (endFill) g2.fill(view.endShape);
            g2.draw(view.endShape);
          }
          if (transition.isErrorOutcome())
          {
            g2.setStroke(new BasicStroke(1.0F, BasicStroke.CAP_SQUARE, 
                                         BasicStroke.JOIN_MITER, 
                                         10F, new float[]{4.0F, 4.0F}, 0.0F));
          }
          else
          {
            g2.setStroke(new BasicStroke(1.0F));
          }
          if (view.lineShape != null)
          {
            g2.draw(view.lineShape);
          }
        }
        
        // paint outcome
        if (!transition.isErrorOutcome())
        {
          g.setColor(Color.black);
          String outcome = transition.getOutcome();
          Point2D outcomePosition = getLabelPosition(view);
          paintLabel(g2, outcome, outcomePosition, true);
        }
      }
    }
  }
}
