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
import java.awt.Font;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.Insets;


import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import javax.swing.JTextField;

import org.santfeliu.swing.text.RestrictedDocument;
import org.santfeliu.util.Properties;
import org.santfeliu.workflow.WorkflowNode;
import org.santfeliu.workflow.node.FormNode;
import org.santfeliu.workflow.swing.NodeEditor;
import org.santfeliu.workflow.swing.NodeEditorDialog;

/**
 *
 * @author unknown
 */
public class InputFormParametersEditor extends JPanel
  implements NodeEditor
{
  private FormNode formNode;
  private boolean number;
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel varLabel = new JLabel();
  private JLabel valueLabel = new JLabel();
  private JLabel messageLabel = new JLabel();
  private JTextField varTextField = new JTextField();
  private JTextField valueTextField = new JTextField();
  private JTextArea messageTextArea = new JTextArea();
  private JScrollPane scrollPane = new JScrollPane();

  public InputFormParametersEditor(boolean number)
  {
    this.number = number;
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public Component getEditingComponent(NodeEditorDialog dialog, 
    WorkflowNode node)
  {
    this.formNode = (FormNode)node;
    Properties parameters = formNode.getParameters();
    Object var = parameters.get("var");
    if (var != null && String.valueOf(var).trim().length() > 0)
    {
      varTextField.setText(var.toString());
    }
    Object value = parameters.get("value");
    if (value != null)
    {
      valueTextField.setText(value.toString());
    }
    Object message = parameters.get("message");
    if (message != null)
    {
      messageTextArea.setText(message.toString());
      messageTextArea.setCaretPosition(0);
    }
    return this;
  }

  public void checkValues() throws Exception
  {
    String variable = varTextField.getText();
    if (variable == null || variable.trim().length() == 0)
      throw new Exception("Variable is mandatory");
  }

  public void stopEditing() throws Exception
  {
    checkValues();
    Properties parameters = new Properties();
    parameters.setProperty("var", varTextField.getText());
    String svalue = valueTextField.getText();
    if (svalue != null && svalue.trim().length() > 0)
    {
      Object value = svalue;
      if (number)
      {
        try
        {
          value = new Double(svalue);
        }
        catch (NumberFormatException ex)
        {
        }
      }
      parameters.setProperty("value", value);
    }
    String message = messageTextArea.getText();
    if (message != null && message.trim().length() > 0)
    {
      parameters.setProperty("message", messageTextArea.getText());
    }
    formNode.setParameters(parameters);
  }

  public void cancelEditing()
  {
  }

  private void jbInit()
    throws Exception
  {
    this.setLayout(gridBagLayout1);
    varLabel.setText("Variable:");
    valueLabel.setText("Initial value:");
    messageLabel.setText("Message:");
    varTextField.setPreferredSize(new Dimension(140, 24));
    varTextField.setMinimumSize(new Dimension(140, 24));
    valueTextField.setPreferredSize(new Dimension(140, 24));
    valueTextField.setMinimumSize(new Dimension(140, 24));
    messageTextArea.setFont(new Font("Dialog", 0, 14));
    messageTextArea.setLineWrap(true);
    messageTextArea.setWrapStyleWord(true);
    this.add(varLabel, 
             new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
                                    new Insets(2, 4, 2, 4), 0, 0));
    this.add(valueLabel, 
             new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
                                    new Insets(2, 4, 2, 4), 0, 0));
    this.add(messageLabel, 
             new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, 
                                    new Insets(2, 4, 2, 4), 0, 0));
    this.add(varTextField, 
             new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 
                                    new Insets(2, 4, 2, 4), 0, 0));
    this.add(valueTextField, 
             new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 
                                    new Insets(2, 4, 2, 4), 0, 0));
    this.add(scrollPane, 
             new GridBagConstraints(1, 2, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 
                                    new Insets(2, 4, 2, 4), 0, 0));
    scrollPane.getViewport().add(messageTextArea);
    this.varTextField.setDocument(new RestrictedDocument("[a-zA-Z_][a-zA-Z0-9_]*"));
  }
}
