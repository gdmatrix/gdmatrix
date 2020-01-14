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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.santfeliu.workflow.WorkflowNode;
import org.santfeliu.workflow.node.DocumentNode;
import org.santfeliu.workflow.swing.NodeEditor;
import org.santfeliu.workflow.swing.NodeEditorDialog;

/**
 *
 * @author unknown
 */
public class DocumentNodeEditor extends JPanel
  implements NodeEditor 
{
  public static ArrayList operations = new ArrayList();

  private DocumentNode documentNode;
  private BorderLayout borderLayout = new BorderLayout();
  private JPanel northPanel = new JPanel();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel serviceLabel = new JLabel();
  private JLabel operationLabel = new JLabel();
  private JTextField serviceTextField = new JTextField();
  private JComboBox operationComboBox = new JComboBox();
  private JPanel centerPanel = new JPanel();
  private JScrollPane scrollPane = new JScrollPane();
  private JTextArea textArea = new JTextArea();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JLabel fileVarLabel = new JLabel();  
  private JTextField fileVarTextField = new JTextField();
  private JLabel filePathLabel = new JLabel();
  private JTextField filePathTextField = new JTextField();
  private JLabel fileURLLabel = new JLabel();
  private JTextField fileURLTextField = new JTextField();
  private JLabel documentVarLabel = new JLabel();
  private JTextField documentVarTextField = new JTextField();
  private JLabel emptyLabel = new JLabel();
  private JLabel propertiesLabel = new JLabel();
  private JLabel referenceLabel = new JLabel();
  private JTextField referenceTextField = new JTextField();

  public DocumentNodeEditor()
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
    documentNode = (DocumentNode)node;
    serviceTextField.setText(documentNode.getServiceURL());
    
    operationComboBox.addItem(DocumentNode.UPLOAD);
    operationComboBox.addItem(DocumentNode.DOWNLOAD);
    operationComboBox.addItem(DocumentNode.UPDATE_PROPERTIES);
    operationComboBox.addItem(DocumentNode.LOCK);
    operationComboBox.addItem(DocumentNode.UNLOCK);
    operationComboBox.addItem(DocumentNode.DELETE);
    operationComboBox.addItem(DocumentNode.COMMIT);
    operationComboBox.addItem(DocumentNode.ABORT);
    operationComboBox.addItem(DocumentNode.COMMIT_AND_LOCK);

    String operation = documentNode.getOperation();
    if (operation != null)
      operationComboBox.setSelectedItem(operation);

    operationComboBox.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent event)
      {
        updateControls();
      }
    });
    updateControls();

    documentVarTextField.setText(documentNode.getDocumentVar());
    fileVarTextField.setText(documentNode.getFileVar());
    filePathTextField.setText(documentNode.getFilePath());
    fileURLTextField.setText(documentNode.getFileURL());
    referenceTextField.setText(documentNode.getReference());
    textArea.setText(documentNode.getProperties().saveToString());
    return this;
  }

  public void checkValues() throws Exception
  {
  }

  public void stopEditing() throws Exception
  {
    checkValues();
    documentNode.setServiceURL(serviceTextField.getText());
    documentNode.setOperation((String)operationComboBox.getSelectedItem());
    documentNode.setDocumentVar(documentVarTextField.getText());
    documentNode.setFileVar(fileVarTextField.getText());
    documentNode.setFilePath(filePathTextField.getText());
    documentNode.setFileURL(fileURLTextField.getText());
    documentNode.setReference(referenceTextField.getText());
    String sprops = textArea.getText();
    documentNode.getProperties().clear();
    documentNode.getProperties().loadFromString(sprops);
  }
  
  public void cancelEditing()
  {
  }

  private void updateControls()
  {
    String operation = (String)operationComboBox.getSelectedItem();
    if (DocumentNode.UPLOAD.equals(operation))
    {
      textArea.setVisible(true);
      documentVarTextField.setEnabled(true);
      fileVarTextField.setEnabled(true);
      filePathTextField.setEnabled(true);
      fileURLTextField.setEnabled(true);
      referenceTextField.setEnabled(false);
      referenceTextField.setText(null);
    }
    else if (DocumentNode.DOWNLOAD.equals(operation))
    {
      textArea.setVisible(false);
      documentVarTextField.setEnabled(true);
      fileVarTextField.setEnabled(true);
      filePathTextField.setEnabled(true);
      fileURLTextField.setEnabled(false);
      referenceTextField.setEnabled(false);
      referenceTextField.setText(null);
    }
    else if (DocumentNode.UPDATE_PROPERTIES.equals(operation))
    {
      textArea.setVisible(true);
      documentVarTextField.setEnabled(true);
      referenceTextField.setEnabled(true);
      filePathTextField.setEnabled(false);
      fileURLTextField.setEnabled(false);
      fileVarTextField.setEnabled(false);
      filePathTextField.setText(null);
      fileVarTextField.setText(null);
    }
    else
    {
      textArea.setVisible(false);
      documentVarTextField.setEnabled(true);
      referenceTextField.setEnabled(true);
      fileVarTextField.setEnabled(false);
      filePathTextField.setEnabled(false);
      fileURLTextField.setEnabled(false);
      fileVarTextField.setText(null);
      filePathTextField.setText(null);
    }
  }

  private void jbInit()
    throws Exception
  {
    this.setLayout(borderLayout);
    northPanel.setLayout(gridBagLayout1);
    serviceLabel.setText("Service URL:");
    operationLabel.setText("Operation:");
    serviceTextField.setPreferredSize(new Dimension(100, 24));
    centerPanel.setLayout(borderLayout1);
    centerPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    textArea.setFont(new Font("Monospaced", 0, 16));
    documentVarLabel.setText("docId variable:");
    fileVarLabel.setText("contentId variable:");
    filePathLabel.setText("File path:");
    fileURLLabel.setText("File URL:");    
    propertiesLabel.setText("Document properties:");
    referenceLabel.setText("Reference:");
    northPanel.add(serviceLabel, 
                   new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                          new Insets(2, 4, 2, 4), 0, 0));
    northPanel.add(serviceTextField, 
                   new GridBagConstraints(1, 0, 3, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
                                          new Insets(2, 0, 2, 4), 0, 0));
    northPanel.add(operationLabel,
                   new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
                                          new Insets(2, 4, 2, 4), 0, 0));
    northPanel.add(operationComboBox, 
                   new GridBagConstraints(1, 1, 1, 1, 0.4, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                          new Insets(2, 0, 2, 4), 0, 0));    
    northPanel.add(emptyLabel, 
                   new GridBagConstraints(2, 1, 1, 1, 0.6, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                          new Insets(0, 0, 0, 0), 0, 0));
    northPanel.add(documentVarLabel,
                   new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                                          new Insets(2, 4, 2, 4), 0, 0));
    northPanel.add(documentVarTextField,
                   new GridBagConstraints(1, 2, 1, 1, 0.4, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                          new Insets(2, 0, 2, 4), 0, 0));
    northPanel.add(fileVarLabel,
                   new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                          new Insets(2, 4, 2, 4), 0, 0));
    northPanel.add(fileVarTextField, 
                   new GridBagConstraints(1, 3, 1, 1, 0.4, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                          new Insets(2, 0, 2, 4), 0, 0));
    northPanel.add(referenceLabel, 
                   new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                          new Insets(2, 4, 2, 4), 0, 0));
    northPanel.add(referenceTextField, 
                   new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                          new Insets(2, 0, 2, 4), 0, 0));
    northPanel.add(filePathLabel,
                   new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,GridBagConstraints.WEST, GridBagConstraints.NONE, 
                                          new Insets(2, 4, 2, 4), 0, 0));
    northPanel.add(filePathTextField, 
                   new GridBagConstraints(1, 5, 3, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                          new Insets(2, 0, 2, 4), 0, 0));
    northPanel.add(fileURLLabel,
                   new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0,GridBagConstraints.WEST, GridBagConstraints.NONE,
                                          new Insets(2, 4, 2, 4), 0, 0));
    northPanel.add(fileURLTextField,
                   new GridBagConstraints(1, 6, 3, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                          new Insets(2, 0, 2, 4), 0, 0));
    northPanel.add(propertiesLabel,
                   new GridBagConstraints(0, 7, 3, 1, 0.0, 0.0,GridBagConstraints.WEST, GridBagConstraints.BOTH,
                                          new Insets(2, 4, 2, 4), 0, 0));
    this.add(northPanel, BorderLayout.NORTH);
    scrollPane.getViewport().add(textArea, null);
    centerPanel.add(scrollPane, BorderLayout.CENTER);
    this.add(centerPanel, BorderLayout.CENTER);
  }
}
