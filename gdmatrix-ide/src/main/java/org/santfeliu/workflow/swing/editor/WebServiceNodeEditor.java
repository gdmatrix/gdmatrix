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
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import org.santfeliu.matrix.ide.Options;

import org.santfeliu.swing.PropertiesPanel;
import org.santfeliu.swing.Utilities;
import org.santfeliu.swing.text.XMLEditorKit;
import org.santfeliu.workflow.WorkflowNode;
import org.santfeliu.workflow.node.WebServiceNode;
import org.santfeliu.workflow.swing.NodeEditor;
import org.santfeliu.workflow.swing.NodeEditorDialog;


/**
 *
 * @author unknown
 */
public class WebServiceNodeEditor extends JPanel
  implements NodeEditor
{
  private WebServiceNode webServiceNode;
  private JTabbedPane tabbedPane = new JTabbedPane();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel requestPanel = new JPanel();
  private JPanel responsePanel = new JPanel();
  private JPanel northPanel = new JPanel();
  private JTextField endpointTextField = new JTextField();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  private GridBagLayout gridBagLayout3 = new GridBagLayout();
  private JScrollPane scrollPane = new JScrollPane();
  private JLabel endpointLabel = new JLabel();
  private JTextPane requestMessageTextPane = new JTextPane();
  private JLabel requestMessageLabel = new JLabel();
  private PropertiesPanel expressionsPanel = new PropertiesPanel();
  private JPanel optionsPanel = new JPanel();
  private PropertiesPanel requestPropertiesPanel = new PropertiesPanel();
  private JLabel optionsLabel = new JLabel();
  private GridBagLayout gridBagLayout4 = new GridBagLayout();
  private JLabel responseLabel = new JLabel();
  private JButton testButton = new JButton();

  private JLabel usernameLabel = new JLabel();
  private JTextField usernameTextField = new JTextField();
  private JLabel passwordLabel = new JLabel();
  private JTextField passwordTextField = new JTextField();

  private JLabel connectTimeoutLabel = new JLabel();
  private JTextField connectTimeoutTextField = new JTextField();
  private JLabel readTimeoutLabel = new JLabel();
  private JTextField readTimeoutTextField = new JTextField();

  public WebServiceNodeEditor()
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
    this.webServiceNode = (WebServiceNode)node;
    endpointTextField.setText(webServiceNode.getEndpoint());
    requestMessageTextPane.setText(webServiceNode.getRequestMessage());
    requestPropertiesPanel.setProperties(webServiceNode.getRequestProperties());
    expressionsPanel.setProperties(webServiceNode.getExpressions());
    usernameTextField.setText(webServiceNode.getUsername());
    passwordTextField.setText(webServiceNode.getPassword());
    connectTimeoutTextField.setText("" + webServiceNode.getConnectTimeout());
    readTimeoutTextField.setText("" + webServiceNode.getReadTimeout());
    return this;
  }

  public void checkValues()
  {
  }

  public void stopEditing()
  {
    webServiceNode.setEndpoint(endpointTextField.getText());
    webServiceNode.setRequestMessage(requestMessageTextPane.getText());
    webServiceNode.getRequestProperties().clear();
    webServiceNode.getRequestProperties().putAll(
      requestPropertiesPanel.getProperties());
    webServiceNode.getExpressions().clear();
    webServiceNode.getExpressions().putAll(expressionsPanel.getProperties());
    webServiceNode.setUsername(usernameTextField.getText());
    webServiceNode.setPassword(passwordTextField.getText());
    try
    {
      int connectTimeout = Integer.parseInt(connectTimeoutTextField.getText());
      webServiceNode.setConnectTimeout(connectTimeout);
    }
    catch (Exception ex)
    {
    }
    try
    {
      int readTimeout = Integer.parseInt(readTimeoutTextField.getText());
      webServiceNode.setReadTimeout(readTimeout);
    }
    catch (Exception ex)
    {
    }
  }

  public void cancelEditing()
  {
  }

  private void jbInit()
    throws Exception
  {
    this.setLayout(borderLayout1);
    requestPanel.setLayout(gridBagLayout2);
    responsePanel.setLayout(gridBagLayout3);
    northPanel.setLayout(gridBagLayout1);
    optionsPanel.setLayout(gridBagLayout4);
    endpointLabel.setText("Endpoint URL:");
    requestMessageLabel.setText("SOAP Message:");
    scrollPane.getViewport().add(requestMessageTextPane, null);
    requestPanel.add(scrollPane, 
                     new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, 
                                            new Insets(0, 4, 4, 4), 0, 0));
    requestPanel.add(requestMessageLabel, 
                     new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 
                                            new Insets(4, 4, 4, 4), 0, 0));
    tabbedPane.addTab("Request", requestPanel);
    responsePanel.add(expressionsPanel, 
                      new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 
                                             new Insets(4, 4, 4, 4), 0, 
                                             0));
    responsePanel.add(responseLabel, 
                      new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
                                             new Insets(4, 4, 4, 4), 0, 
                                             0));
    tabbedPane.addTab("Response", responsePanel);
    optionsPanel.add(requestPropertiesPanel, 
                     new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 
                                            new Insets(4, 4, 4, 4), 0, 0));
    optionsPanel.add(optionsLabel, 
                     new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
                                            new Insets(4, 4, 4, 4), 0, 0));

    optionsPanel.add(usernameLabel,
                     new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                                            new Insets(4, 4, 4, 4), 0, 0));
    optionsPanel.add(usernameTextField,
                     new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                                            new Insets(4, 4, 4, 4), 0, 0));
    optionsPanel.add(passwordLabel,
                     new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                                            new Insets(4, 4, 4, 4), 0, 0));
    optionsPanel.add(passwordTextField,
                     new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                                            new Insets(4, 4, 4, 4), 0, 0));

    optionsPanel.add(connectTimeoutLabel,
                     new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                                            new Insets(4, 4, 4, 4), 0, 0));
    optionsPanel.add(connectTimeoutTextField, 
                     new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                                            new Insets(4, 4, 4, 4), 0, 0));
    optionsPanel.add(readTimeoutLabel, 
                     new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                                            new Insets(4, 4, 4, 4), 0, 0));
    optionsPanel.add(readTimeoutTextField, 
                     new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                                            new Insets(4, 4, 4, 4), 0, 0));

    tabbedPane.addTab("Options", optionsPanel);
    this.add(tabbedPane, BorderLayout.CENTER);
    northPanel.add(endpointLabel, 
                   new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 
                                          GridBagConstraints.WEST, 
                                          GridBagConstraints.BOTH, 
                                          new Insets(4, 2, 4, 2), 0, 0));
    northPanel.add(endpointTextField, 
                   new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, 
                                          GridBagConstraints.WEST, 
                                          GridBagConstraints.HORIZONTAL, 
                                          new Insets(4, 4, 4, 4), 0, 0));
    northPanel.add(testButton, 
                   new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, 
                                          GridBagConstraints.NONE, 
                                          new Insets(4, 4, 4, 4), 0, 0));
    this.add(northPanel, BorderLayout.NORTH);
    requestMessageTextPane.setFont(Options.getEditorFont());
    requestMessageTextPane.setEditorKitForContentType(
      "text/xml", new XMLEditorKit());
    requestMessageTextPane.setContentType("text/xml");
    requestMessageTextPane.setSelectionColor(new Color(198, 198, 198));
    expressionsPanel.setPropertyLabel("Variable");
    expressionsPanel.setValueLabel("XPath expression");    
    requestPropertiesPanel.setPropertyLabel("Variable");
    optionsLabel.setText("Enter HTTP request properties:");
    responseLabel.setText("Enter output variable XPath expressions:");
    testButton.setText("Test");
    testButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        testButton_actionPerformed(e);
      }
    });
    usernameLabel.setText("Username:");
    usernameTextField.setMinimumSize(new Dimension(200, 24));
    passwordLabel.setText("Password:");
    passwordTextField.setMinimumSize(new Dimension(200, 24));

    connectTimeoutLabel.setText("Connect timeout (sec.):");
    connectTimeoutTextField.setMinimumSize(new Dimension(60, 24));
    readTimeoutLabel.setText("Read timeout (sec.):");
    readTimeoutTextField.setMinimumSize(new Dimension(60, 24));
  }

  private void testButton_actionPerformed(ActionEvent e)
  {
    try
    {
      Dialog parent = (Dialog)SwingUtilities.getRoot(this);
      int connectTimeout = 10;
      int readTimeout = 10;
      try
      {
        connectTimeout = Integer.parseInt(connectTimeoutTextField.getText());
        readTimeout = Integer.parseInt(readTimeoutTextField.getText());
      }
      catch (NumberFormatException ex)
      {
      }
      String s;
      s = connectTimeoutTextField.getText();
      if (s != null)
      {
        try
        {
          connectTimeout = Integer.parseInt(s);
        }
        catch (NumberFormatException ex)
        {
        }
      }

      s = readTimeoutTextField.getText();
      if (s != null)
      {
        try
        {
          readTimeout = Integer.parseInt(s);
        }
        catch (NumberFormatException ex)
        {          
        }
      }
      WebServiceNodeTester testDialog = new WebServiceNodeTester(
        parent,
        endpointTextField.getText(),
        requestMessageTextPane.getText(),
        requestPropertiesPanel.getProperties(),
        expressionsPanel.getProperties(),
        connectTimeout, readTimeout);
      testDialog.setSize(800, 600);
      testDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      Utilities.centerWindow(this, testDialog);
      testDialog.setVisible(true);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }  
}
