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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.santfeliu.matrix.ide.MatrixIDE;
import org.santfeliu.util.Properties;
import org.santfeliu.workflow.WorkflowNode;
import org.santfeliu.workflow.node.FormNode;
import org.santfeliu.workflow.swing.NodeEditor;
import org.santfeliu.workflow.swing.NodeEditorDialog;

/**
 *
 * @author realor
 */
public class MonitorFormParametersEditor extends JPanel implements NodeEditor
{
  private FormNode formNode;
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  
  private JLabel messageLabel = new JLabel();
  private JTextArea messageTextArea = new JTextArea();
  
  private JLabel progressVarLabel = new JLabel();
  private JTextField progressVarTextField = new JTextField();

  private JLabel endVarLabel = new JLabel();
  private JTextField endVarTextField = new JTextField();

  private JLabel cancelVarLabel = new JLabel();
  private JTextField cancelVarTextField = new JTextField();

  private JLabel refreshTimeLabel = new JLabel();
  private JTextField refreshTimeTextField = new JTextField();

  private JScrollPane scrollPane = new JScrollPane();

  public MonitorFormParametersEditor()
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
    this.formNode = (FormNode)node;
    Properties parameters = formNode.getParameters();
    
    Object message = parameters.getProperty("message");
    if (message != null) messageTextArea.setText(message.toString());
    
    Object progressVar = parameters.getProperty("progressVar");
    if (progressVar != null) progressVarTextField.setText(progressVar.toString());
    
    Object endVar = parameters.getProperty("endVar");
    if (endVar != null) endVarTextField.setText(endVar.toString());
    
    Object cancelVar = parameters.getProperty("cancelVar");
    if (cancelVar != null) cancelVarTextField.setText(cancelVar.toString());

    Object refreshTime = parameters.getProperty("refreshTime");
    if (refreshTime != null)
      refreshTimeTextField.setText(refreshTime.toString());
    
    return this;
  }

  @Override
  public void checkValues() throws Exception
  {
  }

  @Override
  public void stopEditing() throws Exception
  {
    checkValues();
    Properties parameters = formNode.getParameters();
    parameters.clear();

    String message = messageTextArea.getText();
    if (message != null && message.length() > 0)
      parameters.setProperty("message", message);

    String progressVar = progressVarTextField.getText();
    if (progressVar != null && progressVar.length() > 0)
      parameters.setProperty("progressVar", progressVar);

    String endVar = endVarTextField.getText();
    if (endVar != null && endVar.length() > 0)
      parameters.setProperty("endVar", endVar);

    String cancelVar = cancelVarTextField.getText();
    if (cancelVar != null && cancelVar.length() > 0)
      parameters.setProperty("cancelVar", cancelVar);

    String refreshTime = refreshTimeTextField.getText();
    if (refreshTime != null && refreshTime.length() > 0)
      parameters.setProperty("refreshTime", refreshTime);
  }

  @Override
  public void cancelEditing()
  {
  }

  private void initComponents() throws Exception
  {
    this.setLayout(gridBagLayout1);
    messageLabel.setText("Message:");
    endVarLabel.setText("End variable:");
    progressVarLabel.setText("Progress variable:");
    cancelVarLabel.setText("Cancel variable:");
    refreshTimeLabel.setText("Refresh time (sec.):");

    Dimension size = new Dimension(120, 24);
    endVarTextField.setPreferredSize(size);
    progressVarTextField.setPreferredSize(size);
    cancelVarTextField.setPreferredSize(size);
    refreshTimeTextField.setPreferredSize(size);

    scrollPane.getViewport().add(messageTextArea);

    this.add(messageLabel,
      new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
        new Insets(4, 0, 4, 0), 0, 0));
    this.add(scrollPane,
      new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
        new Insets(4, 0, 4, 0), 0, 0));
    this.add(endVarLabel,
      new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(4, 0, 4, 0), 0, 0));
    this.add(endVarTextField,
      new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(4, 0, 4, 0), 0, 0));
    this.add(cancelVarLabel,
      new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(4, 0, 4, 0), 0, 0));
    this.add(cancelVarTextField,
      new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(4, 0, 4, 0), 0, 0));
    this.add(progressVarLabel,
      new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(4, 0, 4, 0), 0, 0));
    this.add(progressVarTextField,
      new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(4, 0, 4, 0), 0, 0));
    this.add(refreshTimeLabel,
      new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(4, 0, 4, 0), 0, 0));
    this.add(refreshTimeTextField,
      new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(4, 0, 4, 0), 0, 0));
  }
}

