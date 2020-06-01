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

import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import org.jgraph.JGraph;
import org.jgraph.graph.CellView;
import org.santfeliu.workflow.WorkflowNode;
import org.santfeliu.workflow.node.CreateInstanceNode;
import static org.santfeliu.workflow.swing.renderer.AbstractNodeRenderer.SELECTION_COLOR;


/**
 *
 * @author realor
 */
public class CreateInstanceNodeRenderer extends AbstractNodeRenderer
{
  private static final Color BACKGROUND = new Color(240, 240, 255);
  private static final Border BORDER =
    BorderFactory.createLineBorder(new Color(30, 30, 100), 2);

  public CreateInstanceNodeRenderer()
  {
  }

  @Override
  protected Border getNodeBorder(WorkflowNode node)
  {
    return BORDER;
  }

  @Override
  protected String getNodeText(WorkflowNode node)
  {
    CreateInstanceNode createInstanceNode = (CreateInstanceNode)node;
    String workflowName = createInstanceNode.getWorkflowName();
    StringBuilder buffer = new StringBuilder("CreateInstance");
    if (workflowName != null)
    {
      buffer.append(" ").append(workflowName);
    }
    return buffer.toString();
  }

  @Override
  public Component getRendererComponent(JGraph graph, CellView view,
                                        boolean selected, boolean focus,
                                        boolean preview)
  {
    Component comp =
      super.getRendererComponent(graph, view, selected, focus, preview);
    comp.setBackground(selected ? SELECTION_COLOR : BACKGROUND);
    return comp;
  }
}
