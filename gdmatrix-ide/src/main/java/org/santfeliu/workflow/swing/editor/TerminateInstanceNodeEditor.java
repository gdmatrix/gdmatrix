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
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.santfeliu.matrix.ide.MatrixIDE;
import org.santfeliu.workflow.WorkflowNode;
import org.santfeliu.workflow.node.TerminateInstanceNode;
import org.santfeliu.workflow.swing.NodeEditor;
import org.santfeliu.workflow.swing.NodeEditorDialog;

/**
 *
 * @author realor
 */
public class TerminateInstanceNodeEditor extends JPanel implements NodeEditor
{
  private BorderLayout borderLayout1 = new BorderLayout();
  private JLabel messageLabel = new JLabel();
  private TerminateInstanceNode terminateInstanceNode;
  private JScrollPane scrollPane = new JScrollPane();
  private JTextArea messageTextArea = new JTextArea();

  public TerminateInstanceNodeEditor()
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
    terminateInstanceNode = (TerminateInstanceNode)node;
    messageTextArea.setText(terminateInstanceNode.getTerminationMessage());
    return this;
  }

  @Override
  public void checkValues() throws Exception
  {
  }

  @Override
  public void stopEditing() throws Exception
  {
    String message = messageTextArea.getText();
    if (message != null && message.trim().length() == 0) message = null;
    terminateInstanceNode.setTerminationMessage(message);
  }

  @Override
  public void cancelEditing()
  {
  }

  private void initComponents() throws Exception
  {
    this.setLayout(borderLayout1);
    messageLabel.setText("Termination message:");
    this.add(messageLabel, BorderLayout.NORTH);
    scrollPane.getViewport().add(messageTextArea, null);
    this.add(scrollPane, BorderLayout.CENTER);
  }
}
