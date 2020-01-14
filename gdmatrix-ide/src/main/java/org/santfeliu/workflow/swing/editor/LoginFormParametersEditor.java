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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.santfeliu.util.Properties;
import org.santfeliu.workflow.WorkflowNode;
import org.santfeliu.workflow.node.FormNode;
import org.santfeliu.workflow.swing.NodeEditor;
import org.santfeliu.workflow.swing.NodeEditorDialog;

/**
 *
 * @author realor
 */
public class LoginFormParametersEditor extends JPanel
        implements NodeEditor
{
  private FormNode formNode;
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel messageLabel = new JLabel();
  private JTextArea messageTextArea = new JTextArea();
  private JScrollPane scrollPane = new JScrollPane();
  private JCheckBox passwordCheckBox = new JCheckBox();
  private JCheckBox certificateCheckBox = new JCheckBox();
  private JCheckBox validCheckBox = new JCheckBox();
  private JCheckBox mobileidCheckBox = new JCheckBox();
  private JLabel methodsLabel = new JLabel();
  private JPanel methodsPanel = new JPanel();

  public LoginFormParametersEditor()
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
    this.formNode = (FormNode)node;
    Properties parameters = formNode.getParameters();
    Object message = parameters.get("message");
    if (message != null)
    {
      messageTextArea.setText(message.toString());
      messageTextArea.setCaretPosition(0);
    }
    Object password = parameters.get("password");
    passwordCheckBox.setSelected("true".equals(password));
    
    Object certificate = parameters.get("certificate");
    certificateCheckBox.setSelected("true".equals(certificate));
    
    Object valid = parameters.get("valid");
    validCheckBox.setSelected("true".equals(valid));
    
    Object mobileid = parameters.get("mobileid");
    mobileidCheckBox.setSelected("true".equals(mobileid));
    
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
    Properties parameters = new Properties();
    String message = messageTextArea.getText();
    if (message != null && message.trim().length() > 0)
    {
      parameters.setProperty("message", message);
    }
    parameters.put("password", String.valueOf(passwordCheckBox.isSelected()));
    parameters.put("certificate", String.valueOf(certificateCheckBox.isSelected()));
    parameters.put("valid", String.valueOf(validCheckBox.isSelected()));
    parameters.put("mobileid", String.valueOf(mobileidCheckBox.isSelected()));

    formNode.setParameters(parameters);
  }

  @Override
  public void cancelEditing()
  {
  }

  private void jbInit() throws Exception
  {
    this.setLayout(gridBagLayout1);
    messageLabel.setText("Message:");
    messageTextArea.setFont(new Font("Dialog", 0, 14));
    messageTextArea.setLineWrap(true);
    messageTextArea.setWrapStyleWord(true);
    
    this.add(messageLabel,
      new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
      GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
      new Insets(2, 4, 2, 4), 0, 0));
    this.add(scrollPane,
      new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0,
      GridBagConstraints.CENTER, GridBagConstraints.BOTH,
      new Insets(2, 4, 2, 4), 0, 0));
    scrollPane.getViewport().add(messageTextArea);
 
    passwordCheckBox.setText("Password");
    certificateCheckBox.setText("Certificate");
    validCheckBox.setText("VALid");
    mobileidCheckBox.setText("MobileId");
    
    methodsLabel.setText("Methods:");
    this.add(methodsLabel,
      new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0,
      GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
      new Insets(2, 4, 2, 4), 0, 0));    
    this.add(methodsPanel,
      new GridBagConstraints(1, 1, 2, 1, 0.0, 0.0,
      GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
      new Insets(2, 4, 2, 4), 0, 0));
    methodsPanel.setLayout(new GridLayout(4, 1));    
    methodsPanel.add(passwordCheckBox);
    methodsPanel.add(certificateCheckBox);
    methodsPanel.add(validCheckBox);
    methodsPanel.add(mobileidCheckBox);
  }
}
