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

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.santfeliu.util.Properties;
import org.santfeliu.workflow.WorkflowNode;
import org.santfeliu.workflow.node.FormNode;
import org.santfeliu.workflow.swing.NodeEditor;
import org.santfeliu.workflow.swing.NodeEditorDialog;

/**
 *
 * @author unknown
 */
public class SignatureFormParametersEditor extends JPanel
  implements NodeEditor
{
  private FormNode formNode;
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel sigIdLabel = new JLabel();
  private JLabel urlLabel = new JLabel();
  private JLabel messageLabel = new JLabel();
  private JTextField sigIdTextField = new JTextField();
  private JTextField urlTextField = new JTextField();
  private JTextArea messageTextArea = new JTextArea();
  private JScrollPane scrollPane = new JScrollPane();
  private JLabel showIFrameLabel = new JLabel();
  private JCheckBox showIFrameCheckBox = new JCheckBox();

  public SignatureFormParametersEditor()
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

    Object document = parameters.get("document");
    if (document != null && document.toString().trim().length() > 0)
    {
      sigIdTextField.setText(document.toString());
    }      
    Object url = parameters.get("url");
    if (url != null)
    {
      urlTextField.setText(url.toString());
    }
    Object message = parameters.get("message");
    if (message != null)
    {
      messageTextArea.setText(String.valueOf(message));
      messageTextArea.setCaretPosition(0);
    }
    Object iframe = parameters.get("iframe");
    if (iframe != null)
    {
      showIFrameCheckBox.setSelected("true".equals(iframe));
    }
    return this;
  }

  public void checkValues() throws Exception
  {
  }

  public void stopEditing() throws Exception
  {
    Properties parameters = new Properties();
    parameters.setProperty("document", sigIdTextField.getText());

    String value = urlTextField.getText();
    if (value != null && value.trim().length() > 0)
    {
      parameters.setProperty("url", urlTextField.getText());
    }
    String message = messageTextArea.getText();
    if (message != null && message.trim().length() > 0)
    {
      parameters.setProperty("message", messageTextArea.getText());
    }
    parameters.put("iframe",
      String.valueOf(showIFrameCheckBox.isSelected()));
    formNode.setParameters(parameters);
  }

  public void cancelEditing()
  {
  }

  private void jbInit()
    throws Exception
  {
    this.setLayout(gridBagLayout1);
    sigIdLabel.setText("Signature Id.:");
    urlLabel.setText("Signature URL:");
    messageLabel.setText("Message:");
    sigIdTextField.setPreferredSize(new Dimension(140, 24));
    sigIdTextField.setMinimumSize(new Dimension(140, 24));
    urlTextField.setPreferredSize(new Dimension(140, 24));
    urlTextField.setMinimumSize(new Dimension(140, 24));
    messageTextArea.setFont(new Font("Dialog", 0, 14));
    messageTextArea.setLineWrap(true);
    messageTextArea.setWrapStyleWord(true);
    showIFrameLabel.setText("Show in IFrame:");
    
    this.add(sigIdLabel, 
             new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
                                    new Insets(2, 4, 2, 4), 0, 0));
    this.add(urlLabel, 
             new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
                                    new Insets(2, 4, 2, 4), 0, 0));
    this.add(messageLabel, 
             new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, 
                                    new Insets(2, 4, 2, 4), 0, 0));
    this.add(sigIdTextField, 
             new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 
                                    new Insets(2, 4, 2, 4), 0, 0));
    this.add(urlTextField, 
             new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
                                    new Insets(2, 4, 2, 4), 0, 0));
    this.add(scrollPane, 
             new GridBagConstraints(1, 2, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 
                                    new Insets(2, 4, 2, 4), 0, 0));
    this.add(showIFrameLabel,
             new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                    new Insets(2, 4, 2, 4), 0, 0));
    this.add(showIFrameCheckBox,
             new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                    new Insets(2, 4, 2, 4), 0, 0));

    scrollPane.getViewport().add(messageTextArea);
  }
}
