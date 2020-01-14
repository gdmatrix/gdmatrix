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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;


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
public class IdentificationFormParametersEditor extends JPanel
  implements NodeEditor
{
  private FormNode formNode;
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel prefixLabel = new JLabel();
  private JLabel messageLabel = new JLabel();
  private JTextField varPrefixTextField = new JTextField();
  private JTextArea messageTextArea = new JTextArea();
  private JScrollPane scrollPane = new JScrollPane();
  private JLabel helpLabel = new JLabel();

  public IdentificationFormParametersEditor()
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

  public Component getEditingComponent(NodeEditorDialog dialog, 
    WorkflowNode node)
  {
    this.formNode = (FormNode)node;
    Properties parameters = formNode.getParameters();
    Object value = parameters.get("prefix");
    if (value != null)
    {
      varPrefixTextField.setText(value.toString());
    }
    Object message = parameters.get("message");
    if (message != null)
    {
      messageTextArea.setText(message.toString());
      messageTextArea.setCaretPosition(0);
    }
    updateHelpLabel();
    return this;
  }

  public void checkValues() throws Exception
  {
  }

  public void stopEditing() throws Exception
  {
    checkValues();
    Properties parameters = new Properties();
    String prefix = varPrefixTextField.getText();
    if (prefix != null && prefix.trim().length() > 0)
    {
      parameters.setProperty("prefix", prefix);
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
    prefixLabel.setText("Variable prefix:");
    messageLabel.setText("Message:");
    varPrefixTextField.setPreferredSize(new Dimension(100, 24));
    varPrefixTextField.setMinimumSize(new Dimension(100, 24));
    messageTextArea.setFont(new Font("Dialog", 0, 14));
    messageTextArea.setLineWrap(true);
    messageTextArea.setWrapStyleWord(true);
    
    helpLabel.setText("()");
    this.add(prefixLabel,
             new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
                                    new Insets(2, 4, 2, 4), 0, 0));
    this.add(messageLabel, 
             new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, 
                                    new Insets(2, 4, 2, 4), 0, 0));
    this.add(varPrefixTextField, 
             new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 
                                    new Insets(2, 4, 2, 4), 0, 0));
    this.add(scrollPane, 
             new GridBagConstraints(1, 1, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 
                                    new Insets(2, 4, 2, 4), 0, 0));
    this.add(helpLabel, 
             new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
                                    new Insets(0, 0, 0, 0), 0, 0));
    scrollPane.getViewport().add(messageTextArea);
    this.varPrefixTextField.setDocument(new RestrictedDocument("[a-zA-Z_][a-zA-Z0-9_]*"));
    varPrefixTextField.addKeyListener(new KeyAdapter()
    {
      public void keyPressed(KeyEvent e)
      {
        updateHelpLabel();
      }

      public void keyReleased(KeyEvent e)
      {
        updateHelpLabel();
      }

      public void keyTyped(KeyEvent e)
      {
        updateHelpLabel();
      }
    });
  }
  
  private void updateHelpLabel()
  {
    String varPrefix = varPrefixTextField.getText();
    if (varPrefix == null || varPrefix.trim().length() == 0)
    {
      helpLabel.setText("(name, surname1, surname2, etc.)");
    }
    else
    {
      helpLabel.setText(
        "(" + varPrefix + "_name, " + 
        varPrefix + "_surname1, " + 
        varPrefix + "_surname2, etc.)");
    }
  }
}
