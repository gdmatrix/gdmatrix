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

import org.santfeliu.workflow.WorkflowNode;
import org.santfeliu.workflow.node.ConditionNode;
import org.santfeliu.workflow.node.CreateInstanceNode;
import org.santfeliu.workflow.node.DocumentNode;
import org.santfeliu.workflow.node.FormNode;
import org.santfeliu.workflow.node.JavaNode;
import org.santfeliu.workflow.node.JavaScriptNode;
import org.santfeliu.workflow.node.ReturnNode;
import org.santfeliu.workflow.node.SQLNode;
import org.santfeliu.workflow.node.SendMailNode;
import org.santfeliu.workflow.node.SignatureNode;
import org.santfeliu.workflow.node.TerminateInstanceNode;
import org.santfeliu.workflow.node.WaitConditionNode;
import org.santfeliu.workflow.node.WaitNode;
import org.santfeliu.workflow.node.WebServiceNode;
import org.santfeliu.workflow.swing.editor.ConditionNodeEditor;
import org.santfeliu.workflow.swing.editor.CreateInstanceNodeEditor;
import org.santfeliu.workflow.swing.editor.DocumentNodeEditor;
import org.santfeliu.workflow.swing.editor.FormNodeEditor;
import org.santfeliu.workflow.swing.editor.JavaNodeEditor;
import org.santfeliu.workflow.swing.editor.JavaScriptNodeEditor;
import org.santfeliu.workflow.swing.editor.ReturnNodeEditor;
import org.santfeliu.workflow.swing.editor.SQLNodeEditor;
import org.santfeliu.workflow.swing.editor.SendMailNodeEditor;
import org.santfeliu.workflow.swing.editor.SignatureNodeEditor;
import org.santfeliu.workflow.swing.editor.TerminateInstanceNodeEditor;
import org.santfeliu.workflow.swing.editor.WaitConditionNodeEditor;
import org.santfeliu.workflow.swing.editor.WaitNodeEditor;
import org.santfeliu.workflow.swing.editor.WebServiceNodeEditor;


/**
 *
 * @author realor
 */
public class NodeEditorFactory
{
  public static NodeEditor getNodeEditor(WorkflowNode node)
  {
    NodeEditor editor = null;
    if (node instanceof JavaNode)
    {
      editor = new JavaNodeEditor();
    }
    else if (node instanceof JavaScriptNode)
    {
      editor = new JavaScriptNodeEditor();
    }
    else if (node instanceof ConditionNode)
    {
      editor = new ConditionNodeEditor();
    }
    else if (node instanceof WaitConditionNode)
    {
      editor = new WaitConditionNodeEditor();
    }
    else if (node instanceof DocumentNode)
    {
      editor = new DocumentNodeEditor();
    }
    else if (node instanceof FormNode)
    {
      editor = new FormNodeEditor();
    }
    else if (node instanceof SQLNode)
    {
      editor = new SQLNodeEditor();
    }
    else if (node instanceof SendMailNode)
    {
      editor = new SendMailNodeEditor();
    }
    else if (node instanceof SignatureNode)
    {
      editor = new SignatureNodeEditor();
    }
    else if (node instanceof CreateInstanceNode)
    {
      editor = new CreateInstanceNodeEditor();
    }
    else if (node instanceof ReturnNode)
    {
      editor = new ReturnNodeEditor();
    }
    else if (node instanceof TerminateInstanceNode)
    {
      editor = new TerminateInstanceNodeEditor();
    }
    else if (node instanceof WaitNode)
    {
      editor = new WaitNodeEditor();
    }
    else if (node instanceof WebServiceNode)
    {
      editor = new WebServiceNodeEditor();
    }
    else
    {
      try
      {
        String nodeName = node.getClass().getName();
        Class editorClass = Class.forName(nodeName + "Editor");
        editor = (NodeEditor)editorClass.newInstance();
      }
      catch (Exception ex)
      {
      }
    }
    return editor;
  }
}
