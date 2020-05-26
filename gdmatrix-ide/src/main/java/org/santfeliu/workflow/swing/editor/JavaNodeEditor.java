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
import java.awt.Component;
import java.awt.Font;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import org.santfeliu.matrix.ide.MatrixIDE;
import org.santfeliu.swing.text.JavaScriptEditorKit;
import org.santfeliu.workflow.WorkflowNode;
import org.santfeliu.workflow.node.JavaNode;
import org.santfeliu.workflow.swing.NodeEditor;
import org.santfeliu.workflow.swing.NodeEditorDialog;

/**
 *
 * @author realor
 */
@Deprecated
public class JavaNodeEditor extends JPanel implements NodeEditor
{
  private JavaNode javaNode;
  private BorderLayout borderLayout = new BorderLayout();
  private JScrollPane scrollPane = new JScrollPane();
  private JTextPane textPane = new JTextPane();

  public JavaNodeEditor()
  {
    try
    {
      initComponents();
    }
    catch (Exception ex)
    {
      MatrixIDE.log(ex);
    }
  }
  
  @Override
  public Component getEditingComponent(NodeEditorDialog dialog, 
    WorkflowNode node)
  {
    javaNode = (JavaNode)node;
    textPane.setText(javaNode.getCode());
    textPane.setCaretPosition(0);    
    return this;
  }

  @Override
  public void checkValues() throws Exception
  {
    String text = textPane.getText();
    if (text == null || text.trim().length() == 0)
      throw new Exception("Code is empty");
  }

  @Override
  public void stopEditing() throws Exception
  {
    checkValues();
    javaNode.setCode(textPane.getText());
  }
  
  @Override
  public void cancelEditing()
  {
  }

  private void initComponents()
    throws Exception
  {
    this.setLayout(borderLayout);
    textPane.setFont(new Font("Monospaced", 0, 16));
    textPane.setEditorKitForContentType(
      "text/javascript", new JavaScriptEditorKit());
    textPane.setContentType("text/javascript");

    textPane.setSelectionColor(new Color(198, 198, 198));
    scrollPane.getViewport().add(textPane, null);
    this.add(scrollPane, BorderLayout.CENTER);
  }
}
