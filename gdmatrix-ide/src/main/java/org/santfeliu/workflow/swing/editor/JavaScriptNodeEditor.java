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
package org.santfeliu.workflow.swing.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import static java.awt.Color.LIGHT_GRAY;
import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.LineBorder;
import org.santfeliu.matrix.ide.Options;
import org.santfeliu.swing.text.JavaScriptEditorKit;
import org.santfeliu.swing.text.SymbolHighlighter;
import org.santfeliu.swing.text.TextEditor;
import org.santfeliu.workflow.WorkflowNode;
import org.santfeliu.workflow.node.JavaScriptNode;
import org.santfeliu.workflow.swing.NodeEditor;
import org.santfeliu.workflow.swing.NodeEditorDialog;


/**
 *
 * @author unknown
 */
public class JavaScriptNodeEditor extends JPanel implements NodeEditor
{
  private JavaScriptNode javaScriptNode;
  private BorderLayout borderLayout = new BorderLayout();
  private JScrollPane scrollPane = new JScrollPane();
  private TextEditor textEditor = new TextEditor();

  public JavaScriptNodeEditor()
  {
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  @Override
  public Component getEditingComponent(NodeEditorDialog dialog, 
    WorkflowNode node)
  {
    javaScriptNode = (JavaScriptNode)node;
    textEditor.getTextPane().setText(javaScriptNode.getCode());
    textEditor.getTextPane().setCaretPosition(0);
    return this;
  }

  @Override
  public void checkValues() throws Exception
  {
    String text = textEditor.getTextPane().getText();
    if (text == null || text.trim().length() == 0)
      throw new Exception("Code is empty");
  }

  @Override
  public void stopEditing() throws Exception
  {
    checkValues();
    javaScriptNode.setCode(textEditor.getTextPane().getText());
  }
  
  public void cancelEditing()
  {
  }

  private void jbInit()
    throws Exception
  {
    this.setLayout(borderLayout);
    JTextPane textPane = textEditor.getTextPane();
    textPane.setFont(Options.getEditorFont());
    textPane.setEditorKitForContentType(
      "text/javascript", new JavaScriptEditorKit());
    textPane.setContentType("text/javascript");

    textPane.setSelectionColor(new Color(198, 198, 198));
    textEditor.setBorder(new LineBorder(LIGHT_GRAY));
    this.add(textEditor, BorderLayout.CENTER);
    
    SymbolHighlighter symbolHighlighter =
      new SymbolHighlighter(textPane, "({[", ")}]");
  }
}
