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
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.StyledEditorKit;
import org.santfeliu.matrix.ide.MatrixIDE;
import org.santfeliu.matrix.ide.Options;
import org.santfeliu.swing.text.XMLEditorKit;
import org.santfeliu.workflow.WorkflowNode;
import org.santfeliu.workflow.node.SignatureNode;
import org.santfeliu.workflow.swing.NodeEditor;
import org.santfeliu.workflow.swing.NodeEditorDialog;


/**
 *
 * @author realor
 */
public class SignatureNodeEditor extends JPanel implements NodeEditor
{
  private SignatureNode signatureNode;
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel northPanel = new JPanel();
  private JLabel serviceLabel = new JLabel();
  private JTextField serviceTextField = new JTextField();
  private JLabel docVarLabel = new JLabel();
  private JTextField docVarTextField = new JTextField();
  private JLabel operationLabel = new JLabel();
  private JComboBox operationComboBox = new JComboBox();
  private JPanel centerPanel = new JPanel();
  private CardLayout cardLayout = new CardLayout();
  private JPanel createPanel = new JPanel();
  private JPanel addDataPanel = new JPanel();
  private JPanel propertiesPanel = new JPanel();
  private JLabel typeLabel = new JLabel();
  private JTextField typeTextField = new JTextField();
  private JLabel dataTypeLabel = new JLabel();
  private JComboBox dataTypeComboBox = new JComboBox();
  private JTabbedPane tabbedPanel = new JTabbedPane();
  private JPanel contentPanel = new JPanel();
  private JPanel dataPropertiesPanel = new JPanel();
  private JScrollPane scrollPane1 = new JScrollPane();
  private JScrollPane scrollPane2 = new JScrollPane();
  private JTextPane contentTextPane = new JTextPane();
  private JTextArea addDataPropsTextArea = new JTextArea();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  private GridBagLayout gridBagLayout3 = new GridBagLayout();
  private GridBagLayout gridBagLayout4 = new GridBagLayout();
  private GridBagLayout gridBagLayout5 = new GridBagLayout();
  private GridBagLayout gridBagLayout6 = new GridBagLayout();
  private JPanel systemSignaturePanel = new JPanel();
  private JLabel keyAliasLabel = new JLabel();
  private JTextField keyAliasTextField = new JTextField();
  private JLabel propertiesLabel = new JLabel();
  private JScrollPane createPropsScrollPane = new JScrollPane();
  private JTextArea createPropsTextArea = new JTextArea();
  private JPanel emptyPanel = new JPanel();
  private GridBagLayout gridBagLayout7 = new GridBagLayout();
  private JLabel propertiesLabel2 = new JLabel();
  private JScrollPane propsScrollPane = new JScrollPane();
  private JTextArea propsTextArea = new JTextArea();
  private XMLEditorKit xmlEditorKit = new XMLEditorKit();
  private StyledEditorKit styledEditorKit = new StyledEditorKit();

  public SignatureNodeEditor()
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
    signatureNode = (SignatureNode)node;
    serviceTextField.setText(signatureNode.getServiceURL());
    docVarTextField.setText(signatureNode.getDocumentVariable());
    typeTextField.setText(signatureNode.getDocumentType());

    operationComboBox.addItem("createDocument");
    operationComboBox.addItem("addData");
    operationComboBox.addItem("setDocumentProperties");
    operationComboBox.addItem("addSystemSignature");
    operationComboBox.addItem("endDocument");
    operationComboBox.addItem("abortDocument");
    operationComboBox.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        String operation = (String)operationComboBox.getSelectedItem();
        if (operation == null)
        {
          cardLayout.show(centerPanel, "empty");
        }
        else if (operation.equals("createDocument"))
        {
          cardLayout.show(centerPanel, "createDocument");
        }
        else if (operation.equals("addData"))
        {
          cardLayout.show(centerPanel, "addData");
        }
        else if (operation.equals("setDocumentProperties"))
        {
          cardLayout.show(centerPanel, "properties");
        }
        else if (operation.equals("addSystemSignature"))
        {
          cardLayout.show(centerPanel, "addSystemSignature");
        }
        else if (operation.equals("endDocument"))
        {
          cardLayout.show(centerPanel, "properties");
        }
        else
        {
          cardLayout.show(centerPanel, "empty");
        }
      }
    });
    String operation = signatureNode.getOperation();
    operationComboBox.setSelectedItem(operation);
    if (operation == null)
    {
    }
    else if (operation.equals("createDocument"))
    {
      createPropsTextArea.setText(
        signatureNode.getProperties().saveToString());
    }
    else if (operation.equals("addData"))
    {
      addDataPropsTextArea.setText(
        signatureNode.getProperties().saveToString());
    }
    else if (operation.equals("endDocument") ||
             operation.equals("setDocumentProperties"))
    {
      propsTextArea.setText(
        signatureNode.getProperties().saveToString());
    }
    dataTypeComboBox.addItem("xml");
    dataTypeComboBox.addItem("text");
    dataTypeComboBox.addItem("binary");
    dataTypeComboBox.addItem("url");
    dataTypeComboBox.setSelectedItem(signatureNode.getDataType());

    contentTextPane.setText(signatureNode.getContent());
    contentTextPane.setCaretPosition(0);
    keyAliasTextField.setText(signatureNode.getKeyAlias());

    return this;
  }

  @Override
  public void checkValues() throws Exception
  {
    String variable = docVarTextField.getText();
    if (variable == null || variable.trim().length() == 0)
      throw new Exception("Variable is mandatory");

    String operation = (String)operationComboBox.getSelectedItem();
    if (operation == null || operation.trim().length() == 0)
      throw new Exception("Undefined operation");
  }

  @Override
  public void stopEditing() throws Exception
  {
    checkValues();
    signatureNode.setServiceURL(serviceTextField.getText());
    signatureNode.setDocumentVariable(docVarTextField.getText().trim());
    signatureNode.setOperation((String)operationComboBox.getSelectedItem());
    signatureNode.setDocumentType((String)typeTextField.getText());
    signatureNode.setDataType((String)dataTypeComboBox.getSelectedItem());
    signatureNode.setContent(contentTextPane.getText());
    signatureNode.setKeyAlias(keyAliasTextField.getText());
    String operation = (String)operationComboBox.getSelectedItem();
    if (operation.equals("createDocument"))
    {
      String sproperties = createPropsTextArea.getText();
      signatureNode.getProperties().clear();
      signatureNode.getProperties().loadFromString(sproperties);
    }
    else if (operation.equals("addData"))
    {
      String sproperties = addDataPropsTextArea.getText();
      signatureNode.getProperties().clear();
      signatureNode.getProperties().loadFromString(sproperties);
    }
    else if (operation.equals("setDocumentProperties") ||
             operation.equals("endDocument"))
    {
      String sproperties = propsTextArea.getText();
      signatureNode.getProperties().clear();
      signatureNode.getProperties().loadFromString(sproperties);
    }
  }

  @Override
  public void cancelEditing()
  {
  }

  private void initComponents() throws Exception
  {
    this.setLayout(borderLayout1);
    northPanel.setLayout(gridBagLayout1);
    serviceLabel.setText("Service URL:");
    docVarLabel.setText("Document variable:");
    operationLabel.setText("Operation:");
    centerPanel.setLayout(cardLayout);
    centerPanel.setBorder(BorderFactory.createEmptyBorder(8, 2, 2, 2));
    createPanel.setLayout(gridBagLayout2);
    addDataPanel.setLayout(gridBagLayout3);
    propertiesPanel.setLayout(gridBagLayout7);
    typeLabel.setText("Document type:");
    dataTypeLabel.setText("Data type:");
    dataTypeComboBox.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        dataTypeComboBox_actionPerformed(e);
      }
    });
    contentPanel.setLayout(gridBagLayout5);
    contentTextPane.setFont(Options.getEditorFont());
    createPropsTextArea.setFont(Options.getEditorFont());
    addDataPropsTextArea.setFont(Options.getEditorFont());
    propsTextArea.setFont(Options.getEditorFont());

    dataPropertiesPanel.setLayout(gridBagLayout4);
    systemSignaturePanel.setLayout(gridBagLayout6);
    keyAliasLabel.setText("KeyAlias:");
    propertiesLabel.setText("Document properties:");
    propertiesLabel2.setText("Document Properties:");
    northPanel.add(serviceLabel,
      new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(2, 0, 2, 4), 0, 0));
    northPanel.add(serviceTextField,
      new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 0, 2, 0), 0, 0));
    northPanel.add(docVarLabel,
      new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(2, 0, 2, 4), 0, 0));
    northPanel.add(docVarTextField,
      new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(2, 0, 2, 0), 0, 0));
    northPanel.add(operationLabel,
      new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(2, 0, 2, 0), 0, 0));
    northPanel.add(operationComboBox,
      new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(2, 0, 2, 0), 0, 0));
    this.add(northPanel, BorderLayout.NORTH);
    createPanel.add(typeLabel,
      new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(2, 0, 2, 6), 0, 0));
    createPanel.add(typeTextField,
      new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
        new Insets(2, 0, 2, 0), 0, 0));
    createPanel.add(propertiesLabel,
      new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 0, 2, 0), 0, 0));
    createPropsScrollPane.getViewport().add(createPropsTextArea, null);
    createPanel.add(createPropsScrollPane,
      new GridBagConstraints(0, 2, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(2, 0, 2, 0), 0, 0));
    centerPanel.add(createPanel, "createDocument");
    addDataPanel.add(dataTypeLabel,
      new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
        GridBagConstraints.WEST,
        GridBagConstraints.NONE,
        new Insets(10, 0, 2, 6), 0,
        0));
    addDataPanel.add(dataTypeComboBox,
      new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
        GridBagConstraints.WEST,
        GridBagConstraints.NONE,
        new Insets(10, 0, 0, 0), 0,
        0));
    scrollPane1.getViewport().add(contentTextPane, null);
    contentPanel.add(scrollPane1,
      new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
        GridBagConstraints.CENTER,
        GridBagConstraints.BOTH,
        new Insets(4, 4, 4, 4), 0, 0));
    tabbedPanel.addTab("Content", contentPanel);
    scrollPane2.getViewport().add(addDataPropsTextArea, null);
    dataPropertiesPanel.add(scrollPane2,
      new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
        GridBagConstraints.BOTH,
        new Insets(4, 4, 4, 4), 0, 0));
    tabbedPanel.addTab("Content properties", dataPropertiesPanel);
    addDataPanel.add(tabbedPanel,
      new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0,
        GridBagConstraints.CENTER,
        GridBagConstraints.BOTH,
        new Insets(6, 0, 0, 0), 0, 0));
    centerPanel.add(addDataPanel, "addData");
    propertiesPanel.add(propertiesLabel2,
      new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
        new Insets(2, 0, 2, 0), 0,
        0));
    propsScrollPane.getViewport().add(propsTextArea, null);
    propertiesPanel.add(propsScrollPane,
      new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(2, 0, 2, 0), 0,
        0));
    centerPanel.add(propertiesPanel, "properties");
    systemSignaturePanel.add(keyAliasLabel,
      new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
        GridBagConstraints.CENTER,
        GridBagConstraints.NONE,
        new Insets(2, 6, 2, 6),
        0, 0));
    systemSignaturePanel.add(keyAliasTextField,
      new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
        GridBagConstraints.CENTER,
        GridBagConstraints.HORIZONTAL,
        new Insets(2, 0, 2, 6),
        0, 0));
    centerPanel.add(systemSignaturePanel, "addSystemSignature");
    centerPanel.add(emptyPanel, "empty");
    this.add(centerPanel, BorderLayout.CENTER);
  }

  private void dataTypeComboBox_actionPerformed(ActionEvent e)
  {
    String text = contentTextPane.getText();
    String dataType = (String) dataTypeComboBox.getSelectedItem();
    if ("xml".equals(dataType))
    {
      contentTextPane.setContentType("text/xml");
      contentTextPane.setEditorKit(xmlEditorKit);
    }
    else
    {
      contentTextPane.setContentType("text/plain");
      contentTextPane.setEditorKit(styledEditorKit);
    }
    contentTextPane.setText(text);
  }
}
