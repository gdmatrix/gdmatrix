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
package org.santfeliu.workflow.swing;

import org.santfeliu.matrix.ide.WorkflowPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.santfeliu.swing.text.RestrictedDocument;
import org.santfeliu.workflow.WorkflowNode;

/**
 *
 * @author realor
 */
public class NodeEditorDialog extends JDialog
{
  public static final int OK_OPTION = JOptionPane.OK_OPTION;
  public static final int CANCEL_OPTION = JOptionPane.CANCEL_OPTION;

  private int result = JOptionPane.CANCEL_OPTION;
  private WorkflowNode node;
  private NodeEditor editor;

  private BorderLayout borderLayout = new BorderLayout();
  private JPanel southPanel = new JPanel();
  private JPanel northPanel = new JPanel();
  private JButton acceptButton = new JButton();
  private JButton cancelButton = new JButton();
  private JLabel nodeIdLabel = new JLabel();
  private GridBagLayout gridBagLayout = new GridBagLayout();
  private JTextField nodeIdTextField = new JTextField();
  private JLabel descriptionLabel = new JLabel();
  private JTextField descriptionTextField = new JTextField();
  private JCheckBox immediateCheckBox = new JCheckBox();
  private JTabbedPane tabbedPane = new JTabbedPane();
  private JPanel propertiesPanel = new JPanel();
  private JPanel rolesPanel = new JPanel();
  private BorderLayout borderLayout1 = new BorderLayout();
  private BorderLayout borderLayout2 = new BorderLayout();
  private JLabel rolesLabel = new JLabel();
  private JScrollPane rolesScrollPane = new JScrollPane();
  private JTextArea rolesTextArea = new JTextArea();
  private JCheckBox hiddenCheckBox = new JCheckBox();
  private JLabel attributesLabel = new JLabel();

  public NodeEditorDialog(Frame parent)
  {
    super(parent, true);
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private void jbInit()
    throws Exception
  {
    this.setMinimumSize(new Dimension(600, 600));
    this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    this.getContentPane().setLayout(borderLayout);
    this.setModal(true);
    northPanel.setLayout(gridBagLayout);
    northPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    acceptButton.setText("Accept");
    acceptButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            acceptButton_actionPerformed(e);
          }
        });
    cancelButton.setText("Cancel");
    cancelButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            cancelButton_actionPerformed(e);
          }
        });
    nodeIdLabel.setText("NodeId:");
    nodeIdTextField.setPreferredSize(new Dimension(100, 24));
    nodeIdTextField.setMinimumSize(new Dimension(100, 24));
    descriptionLabel.setText("Description:");
    immediateCheckBox.setText("Immediate");
    tabbedPane.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    propertiesPanel.setLayout(borderLayout1);
    propertiesPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    rolesPanel.setLayout(borderLayout2);
    rolesPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    rolesLabel.setText("Enter roles separated by semi-colon or CR: (role1;role2;...)");
    rolesLabel.setPreferredSize(new Dimension(338, 24));
    hiddenCheckBox.setText("Hidden");
    attributesLabel.setText("Attributes:");
    southPanel.add(acceptButton, null);
    southPanel.add(cancelButton, null);
    this.getContentPane().add(southPanel, BorderLayout.SOUTH);
    northPanel.add(nodeIdLabel, 
                   new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
                                          new Insets(2, 0, 2, 6), 0, 0));
    northPanel.add(nodeIdTextField, 
                   new GridBagConstraints(1, 0, 2, 1, 1.0, 0.0,GridBagConstraints.WEST, GridBagConstraints.NONE, 
                                          new Insets(2, 0, 2, 0), 0, 0));
    northPanel.add(descriptionLabel, 
                   new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 
                                          new Insets(2, 0, 2, 6), 0, 0));
    northPanel.add(descriptionTextField, 
                   new GridBagConstraints(1, 1, 2, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
                                          new Insets(2, 0, 2, 0), 0, 0));
    northPanel.add(immediateCheckBox, 
                   new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 
                                          new Insets(2, 0, 2, 6), 0, 0));
    northPanel.add(hiddenCheckBox, 
                   new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,GridBagConstraints.WEST, GridBagConstraints.NONE, 
                                          new Insets(0, 0, 0, 0), 0, 0));
    northPanel.add(attributesLabel, 
                   new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
                                          new Insets(0, 0, 0, 0), 0, 0));
    this.getContentPane().add(northPanel, BorderLayout.NORTH);
    tabbedPane.addTab("Properties", propertiesPanel);
    rolesPanel.add(rolesLabel, BorderLayout.NORTH);
    rolesScrollPane.getViewport().add(rolesTextArea, null);
    rolesPanel.add(rolesScrollPane, BorderLayout.CENTER);
    tabbedPane.addTab("Execution roles", rolesPanel);
    this.getContentPane().add(tabbedPane, BorderLayout.CENTER);
    this.nodeIdTextField.setDocument(new RestrictedDocument("[a-zA-Z0-9_]+"));
  }
  
  public int editNode(WorkflowPanel panel, WorkflowNode node, NodeEditor editor)
  {
    this.node = node;
    this.editor = editor;
    
    String nodeId = node.getId();
    nodeIdTextField.setText(nodeId);
    String description = node.getDescription();
    if (description != null) description = description.trim();
    descriptionTextField.setText(description);
    immediateCheckBox.setSelected(node.isImmediate());
    hiddenCheckBox.setSelected(node.isHidden());
    setTitle(node.getType());
    rolesTextArea.setText(node.getRoles());
    tabbedPane.setTitleAt(0, node.getType());
    propertiesPanel.removeAll();

    if (editor != null)
    {
      Component editComponent = editor.getEditingComponent(this, node);
      if (editComponent != null)
      {
        propertiesPanel.add(editComponent, BorderLayout.CENTER);
      }
    }
    pack();
    
    // resize dialog if its size is greater than max window bounds
    Dimension size = getSize();
    Rectangle maxSize = GraphicsEnvironment.getLocalGraphicsEnvironment().
      getMaximumWindowBounds();
    
    if (size.width > maxSize.width) size.width = maxSize.width;
    if (size.height > maxSize.height) size.height = maxSize.height; 
    
    setSize(size);
    
    setLocationRelativeTo(null);
    setVisible(true);
    
    return result;
  }
  
  /* private methods */
  private void accept()
  {
    node.setId(nodeIdTextField.getText());
    String description = descriptionTextField.getText();
    node.setDescription(description);
    node.setImmediate(immediateCheckBox.isSelected());
    node.setHidden(hiddenCheckBox.isSelected());
    node.setRoles(rolesTextArea.getText());
    try
    {
      if (editor != null) editor.stopEditing();
      result = OK_OPTION;
      dispose();
    }
    catch (Exception ex)
    {
      JOptionPane.showMessageDialog(this, ex.getMessage(), 
        "ERROR", JOptionPane.ERROR_MESSAGE);
    }
  }
  
  private void cancel()
  {
    if (editor != null) editor.cancelEditing();
    result = CANCEL_OPTION;
    dispose();
  }

  private void acceptButton_actionPerformed(ActionEvent e)
  {
    accept();
  }

  private void cancelButton_actionPerformed(ActionEvent e)
  {
    cancel();
  }
}

