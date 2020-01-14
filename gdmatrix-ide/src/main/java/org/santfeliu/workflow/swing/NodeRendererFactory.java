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
package org.santfeliu.workflow.swing;

import java.util.HashMap;
import org.jgraph.graph.CellViewRenderer;
import org.santfeliu.workflow.WorkflowNode;
import org.santfeliu.workflow.swing.renderer.AbstractNodeRenderer;
import org.santfeliu.workflow.swing.renderer.ConditionNodeRenderer;
import org.santfeliu.workflow.swing.renderer.DocumentNodeRenderer;
import org.santfeliu.workflow.swing.renderer.FormNodeRenderer;
import org.santfeliu.workflow.swing.renderer.JavaNodeRenderer;
import org.santfeliu.workflow.swing.renderer.JavaScriptNodeRenderer;
import org.santfeliu.workflow.swing.renderer.NopNodeRenderer;
import org.santfeliu.workflow.swing.renderer.SignatureNodeRenderer;
import org.santfeliu.workflow.swing.renderer.WaitConditionNodeRenderer;
import org.santfeliu.workflow.swing.renderer.WaitNodeRenderer;


/**
 *
 * @author unknown
 */
public class NodeRendererFactory
{
  static AbstractNodeRenderer defaulRenderer = new AbstractNodeRenderer(){};
  static final HashMap<String, CellViewRenderer> renderers = new HashMap();

  static
  {
    renderers.put("Condition", new ConditionNodeRenderer());
    renderers.put("Document", new DocumentNodeRenderer());
    renderers.put("Form", new FormNodeRenderer());
    renderers.put("Java", new JavaNodeRenderer());
    renderers.put("JavaScript", new JavaScriptNodeRenderer());
    renderers.put("Nop", new NopNodeRenderer());
    renderers.put("Signature", new SignatureNodeRenderer());
    renderers.put("WaitCondition", new WaitConditionNodeRenderer());
    renderers.put("Wait", new WaitNodeRenderer());
  }

  public static CellViewRenderer getNodeRenderer(WorkflowNode node)
  {
    String nodeType = node.getType();
    CellViewRenderer renderer = renderers.get(nodeType);
    if (renderer == null) renderer = defaulRenderer;

    return renderer;
  }
}
