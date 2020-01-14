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

import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.DefaultCellViewFactory;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.PortRenderer;
import org.jgraph.graph.PortView;
import org.jgraph.graph.VertexView;

/**
 *
 * @author unknown
 */
public class WorkflowCellViewFactory extends DefaultCellViewFactory
{
  public WorkflowCellViewFactory()
  {
  }

  protected PortView createPortView(Object cell)
  {
    return new PortView(cell)
    {
      public CellViewRenderer getRenderer()
      {
        PortRenderer r = (PortRenderer)super.getRenderer();
        r.setForeground(Color.orange);
        return r;
      }
    };
  }

  protected VertexView createVertexView(Object cell)
  {
    if (cell instanceof WorkflowVertex)
    {
      return new WorkflowVertexView(cell);
    }
    else return super.createVertexView(cell);
  }

  protected EdgeView createEdgeView(Object cell)
  {
    if (cell instanceof WorkflowEdge)
    {
      return new WorkflowEdgeView(cell);
    }
    else return super.createEdgeView(cell);
  }
}