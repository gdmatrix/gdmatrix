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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import org.santfeliu.matrix.ide.Options;
import org.santfeliu.swing.text.JavaScriptEditorKit;
import org.santfeliu.swing.text.SymbolHighlighter;
import org.santfeliu.swing.text.TextEditor;
import org.santfeliu.workflow.WorkflowNode;
import org.santfeliu.workflow.node.FormNode;
import org.santfeliu.workflow.swing.NodeEditor;
import org.santfeliu.workflow.swing.NodeEditorDialog;
import static java.awt.Color.LIGHT_GRAY;
import java.awt.Dimension;
import org.santfeliu.matrix.ide.MatrixIDE;

/**
 *
 * @author realor
 */
public class FormNodeEditor extends JPanel implements NodeEditor
{
  public static final ArrayList formTypes = new ArrayList();

  private FormNode formNode;
  private NodeEditorDialog dialog;
  private BorderLayout borderLayout = new BorderLayout();
  private JPanel northPanel = new JPanel();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel formTypeLabel = new JLabel();
  private JComboBox formTypeComboBox = new JComboBox();
  private JLabel groupLabel = new JLabel();
  private JTextField groupTextField = new JTextField();
  private JPanel parametersPanel = new JPanel();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JLabel buttonsLabel = new JLabel();
  private JCheckBox backwardCheckBox = new JCheckBox();
  private JCheckBox forwardCheckBox = new JCheckBox();
  private JTabbedPane tabbedPane = new JTabbedPane();
  private NodeEditor parametersEditor = null;
  private JPanel variablesPanel = new JPanel();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  private JLabel readVarsLabel = new JLabel();
  private JScrollPane readVarsScrollPane = new JScrollPane();
  private JLabel writeVarsLabel = new JLabel();
  private JScrollPane writeVarsScrollPane = new JScrollPane();
  private JTextArea readVarsTextArea = new JTextArea();
  private JTextArea writeVarsTextArea = new JTextArea();
  private JPanel buttonsPanel = new JPanel();
  private FlowLayout flowLayout = new FlowLayout();
  private BorderLayout borderLayout2 = new BorderLayout();
  private JPanel viewRolesPanel = new JPanel();
  private JScrollPane viewRolesScrollPane = new JScrollPane();
  private JLabel viewRolesLabel = new JLabel();
  private JTextArea viewRolesTextArea = new JTextArea();
  private JPanel editRolesPanel = new JPanel();
  private JScrollPane editRolesScrollPane = new JScrollPane();
  private JLabel editRolesLabel = new JLabel();
  private JTextArea editRolesTextArea = new JTextArea();
  private JPanel cancelPanel = new JPanel();
  private JLabel cancelLabel = new JLabel();
  private TextEditor cancelTextEditor = new TextEditor();
  private JPanel checkPanel = new JPanel();
  private BorderLayout borderLayout3 = new BorderLayout();
  private JLabel checkLabel = new JLabel();
  private TextEditor checkTextEditor = new TextEditor();
  private JPanel outcomePanel = new JPanel();
  private BorderLayout borderLayout4 = new BorderLayout();
  private JLabel outcomeLabel = new JLabel();
  private TextEditor outcomeTextEditor = new TextEditor();

  static
  {
    formTypes.add("address");
    formTypes.add("biometric");
    formTypes.add("custom");
    formTypes.add("dynamic");
    formTypes.add("flex");
    formTypes.add("identification");
    formTypes.add("inputNumber");
    formTypes.add("inputText");
    formTypes.add("login");
    formTypes.add("monitor");
    formTypes.add("outputText");
    formTypes.add("person");
    formTypes.add("selectMenu");
    formTypes.add("signature");
    formTypes.add("showDocument");
    formTypes.add("uploadDocuments");
    formTypes.add("scanDocument");
  }

  public FormNodeEditor()
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
  public Dimension getPreferredSize()
  {
    JLabel tabLabel = new JLabel();
    Dimension dim = super.getPreferredSize();
    dim.width = 0;
    for (int i = 0; i < tabbedPane.getTabCount(); i++)
    {
      String title = tabbedPane.getTitleAt(i);
      tabLabel.setText(title);
      Dimension tabDim = tabLabel.getPreferredSize();
      dim.width += (1.5 * tabDim.width);
    }
    return dim;
  }

  @Override
  public Component getEditingComponent(NodeEditorDialog dialog,
    WorkflowNode node)
  {
    this.formNode = (FormNode)node;
    this.dialog = dialog;

    String formType = formNode.getFormType();
    if (formType != null)
    {
      if (formTypes.indexOf(formType) == -1)
      {
        if (formType.trim().length() > 0)
        {
          formTypeComboBox.addItem(formType);
        }
      }
    }
    formTypeComboBox.setSelectedItem(formType);
    groupTextField.setText(formNode.getGroup());
    forwardCheckBox.setSelected(formNode.isForwardEnabled());
    backwardCheckBox.setSelected(formNode.isBackwardEnabled());
    readVarsTextArea.setText(formNode.getReadVariables());
    writeVarsTextArea.setText(formNode.getWriteVariables());
    viewRolesTextArea.setText(formNode.getViewRoles());
    editRolesTextArea.setText(formNode.getEditRoles());
    checkTextEditor.getTextPane().setText(formNode.getCheckExpression());
    cancelTextEditor.getTextPane().setText(formNode.getCancelExpression());
    outcomeTextEditor.getTextPane().setText(formNode.getOutcomeExpression());
    viewRolesTextArea.setCaretPosition(0);
    editRolesTextArea.setCaretPosition(0);
    checkTextEditor.getTextPane().setCaretPosition(0);
    cancelTextEditor.getTextPane().setCaretPosition(0);
    outcomeTextEditor.getTextPane().setCaretPosition(0);
    return this;
  }

  @Override
  public void checkValues() throws Exception
  {
    String formType = (String)formTypeComboBox.getSelectedItem();
    if (formType == null || formType.trim().length() == 0)
      throw new Exception("Undefined form type");

    if (parametersEditor == null)
    {
      parametersEditor.checkValues();
    }
  }

  @Override
  public void stopEditing() throws Exception
  {
    checkValues();
    parametersEditor.stopEditing();
    formNode.setFormType(formTypeComboBox.getSelectedItem().toString());
    formNode.setGroup(groupTextField.getText());
    formNode.setForwardEnabled(forwardCheckBox.isSelected());
    formNode.setBackwardEnabled(backwardCheckBox.isSelected());
    formNode.setReadVariables(readVarsTextArea.getText());
    formNode.setWriteVariables(writeVarsTextArea.getText());
    formNode.setViewRoles(viewRolesTextArea.getText());
    formNode.setEditRoles(editRolesTextArea.getText());
    formNode.setCheckExpression(checkTextEditor.getTextPane().getText());
    formNode.setCancelExpression(cancelTextEditor.getTextPane().getText());
    formNode.setOutcomeExpression(outcomeTextEditor.getTextPane().getText());
  }

  @Override
  public void cancelEditing()
  {
    if (parametersEditor != null)
      parametersEditor.cancelEditing();
  }

  private void initComponents() throws Exception
  {
    this.setLayout(borderLayout);
    northPanel.setLayout(gridBagLayout1);
    formTypeLabel.setText("Type:");
    formTypeComboBox.setEditable(true);
    groupLabel.setText("Group:");
    parametersPanel.setLayout(borderLayout1);
    parametersPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

    buttonsLabel.setText("Buttons:");
    backwardCheckBox.setText("<< backward");
    forwardCheckBox.setText("forward >>");
    variablesPanel.setLayout(gridBagLayout2);
    variablesPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    readVarsLabel.setText("Enter read variables separated by semi-colon:");
    writeVarsLabel.setText("Enter write variables separated by semi-colon:");
    northPanel.add(formTypeLabel,
      new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    northPanel.add(formTypeComboBox,
      new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(2, 0, 2, 4), 0, 0));

    northPanel.add(groupLabel,
      new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    northPanel.add(groupTextField,
      new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 0, 2, 0), 0, 0));

    northPanel.add(buttonsLabel,
      new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    northPanel.add(buttonsPanel,
      new GridBagConstraints(1, 2, 2, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(0, 0, 0, 0), 0, 0));

    flowLayout.setAlignment(FlowLayout.LEFT);
    cancelPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    cancelPanel.setLayout(borderLayout2);
    viewRolesPanel.setLayout(new BorderLayout());
    editRolesPanel.setLayout(new BorderLayout());
    cancelLabel.setText("Cancel expression:");
    cancelLabel.setBorder(new EmptyBorder(4, 0, 4, 0));
    buttonsPanel.setLayout(flowLayout);
    buttonsPanel.add(backwardCheckBox);
    buttonsPanel.add(forwardCheckBox);

    this.add(northPanel, BorderLayout.NORTH);
    this.add(tabbedPane, BorderLayout.CENTER);

    variablesPanel.add(readVarsLabel,
      new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
        GridBagConstraints.WEST,
        GridBagConstraints.NONE,
        new Insets(4, 0, 4, 0), 0,
        0));
    readVarsScrollPane.getViewport().add(readVarsTextArea, null);
    variablesPanel.add(readVarsScrollPane,
      new GridBagConstraints(0, 1, 1, 1, 1.0, 0.5,
        GridBagConstraints.CENTER,
        GridBagConstraints.BOTH,
        new Insets(0, 0, 0, 0), 0,
        0));
    variablesPanel.add(writeVarsLabel,
      new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
        GridBagConstraints.WEST,
        GridBagConstraints.NONE,
        new Insets(4, 0, 4, 0), 0,
        0));
    writeVarsScrollPane.getViewport().add(writeVarsTextArea, null);
    variablesPanel.add(writeVarsScrollPane,
      new GridBagConstraints(0, 3, 1, 1, 1.0, 0.5,
        GridBagConstraints.CENTER,
        GridBagConstraints.BOTH,
        new Insets(0, 0, 0, 0), 0,
        0));
    cancelTextEditor.setBorder(new LineBorder(LIGHT_GRAY));
    cancelPanel.add(cancelTextEditor, BorderLayout.CENTER);
    cancelPanel.add(cancelLabel, BorderLayout.NORTH);

    cancelTextEditor.getTextPane().setFont(Options.getEditorFont());
    cancelTextEditor.getTextPane().setEditorKitForContentType(
      "text/javascript", new JavaScriptEditorKit());
    cancelTextEditor.getTextPane().setContentType("text/javascript");

    SymbolHighlighter cancelMatcher
      = new SymbolHighlighter(cancelTextEditor.getTextPane(), "({[", ")}]");

    viewRolesLabel.setText("Enter roles separated by semi-colon or CR: (role1;role2;...)");
    viewRolesLabel.setBorder(new EmptyBorder(4, 0, 4, 0));
    viewRolesScrollPane.getViewport().add(viewRolesTextArea, null);
    viewRolesPanel.add(viewRolesScrollPane, BorderLayout.CENTER);
    viewRolesPanel.add(viewRolesLabel, BorderLayout.NORTH);
    viewRolesPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    viewRolesTextArea.setFont(new Font("Monospaced", 0, 14));

    editRolesLabel.setText("Enter roles separated by semi-colon or CR: (role1;role2;...)");
    editRolesLabel.setBorder(new EmptyBorder(4, 0, 4, 0));
    editRolesScrollPane.getViewport().add(editRolesTextArea, null);
    editRolesPanel.add(editRolesScrollPane, BorderLayout.CENTER);
    editRolesPanel.add(editRolesLabel, BorderLayout.NORTH);
    editRolesPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    editRolesTextArea.setFont(new Font("Monospaced", 0, 14));

    checkPanel.setLayout(borderLayout3);
    checkPanel.add(checkLabel, BorderLayout.NORTH);
    checkTextEditor.setBorder(new LineBorder(LIGHT_GRAY));
    checkPanel.add(checkTextEditor, BorderLayout.CENTER);

    checkPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    checkLabel.setText("Check expression:");
    checkLabel.setBorder(new EmptyBorder(4, 0, 4, 0));

    checkTextEditor.getTextPane().setFont(Options.getEditorFont());
    checkTextEditor.getTextPane().setEditorKitForContentType(
      "text/javascript", new JavaScriptEditorKit());
    checkTextEditor.getTextPane().setContentType("text/javascript");

    SymbolHighlighter checkMatcher
      = new SymbolHighlighter(checkTextEditor.getTextPane(), "({[", ")}]");

    outcomePanel.setLayout(borderLayout4);
    outcomePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    outcomeLabel.setText("Outcome expression:");
    outcomeLabel.setBorder(new EmptyBorder(4, 0, 4, 0));

    outcomeTextEditor.getTextPane().setFont(Options.getEditorFont());
    outcomeTextEditor.getTextPane().setEditorKitForContentType(
      "text/javascript", new JavaScriptEditorKit());
    outcomeTextEditor.getTextPane().setContentType("text/javascript");

    SymbolHighlighter outcomeMatcher
      = new SymbolHighlighter(outcomeTextEditor.getTextPane(), "({[", ")}]");

    tabbedPane.addTab("Parameters", parametersPanel);
    tabbedPane.addTab("Variables", variablesPanel);
    tabbedPane.addTab("View roles", viewRolesPanel);
    tabbedPane.addTab("Edit roles", editRolesPanel);
    tabbedPane.addTab("Check", checkPanel);
    tabbedPane.addTab("Cancel", cancelPanel);
    tabbedPane.addTab("Outcome", outcomePanel);

    outcomeTextEditor.setBorder(new LineBorder(LIGHT_GRAY));
    outcomePanel.add(outcomeTextEditor, BorderLayout.CENTER);
    outcomePanel.add(outcomeLabel, BorderLayout.NORTH);
    Iterator iter = formTypes.iterator();
    while (iter.hasNext())
    {
      String type = (String)iter.next();
      formTypeComboBox.addItem(type);
    }
    this.formTypeComboBox.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent event)
      {
        loadParametersEditor();
      }
    });
  }

  private void loadParametersEditor()
  {
    String formType = String.valueOf(formTypeComboBox.getSelectedItem());
    if (formType == null || formType.trim().length() == 0)
    {
      parametersEditor = null;
    }
    else if ("address".equals(formType))
    {
      parametersEditor = new AddressFormParametersEditor();
    }
    else if ("person".equals(formType))
    {
      parametersEditor = new PersonFormParametersEditor();
    }
    else if ("login".equals(formType))
    {
      parametersEditor = new LoginFormParametersEditor();
    }
    else if ("inputText".equals(formType))
    {
      parametersEditor = new InputFormParametersEditor(false);
    }
    else if ("inputNumber".equals(formType))
    {
      parametersEditor = new InputFormParametersEditor(true);
    }
    else if ("selectMenu".equals(formType))
    {
      parametersEditor = new SelectMenuFormParametersEditor();
    }
    else if ("biometric".equals(formType))
    {
      parametersEditor = new BiometricFormParametersEditor();
    }
    else if ("uploadDocuments".equals(formType))
    {
      parametersEditor = new UploadFormParametersEditor();
    }
    else if ("signature".equals(formType))
    {
      parametersEditor = new SignatureFormParametersEditor();
    }
    else if ("identification".equals(formType))
    {
      parametersEditor = new IdentificationFormParametersEditor();
    }
    else if ("monitor".equals(formType))
    {
      parametersEditor = new MonitorFormParametersEditor();
    }
    else if ("outputText".equals(formType))
    {
      parametersEditor = new OutputFormParametersEditor();
    }
    else if ("showDocument".equals(formType))
    {
      parametersEditor = new ShowDocumentFormParametersEditor();
    }
    else if ("custom".equals(formType))
    {
      parametersEditor = new CustomFormParametersEditor();
    }
    else if ("dynamic".equals(formType))
    {
      parametersEditor = new DynamicFormParametersEditor();
    }
    else if ("flex".equals(formType))
    {
      parametersEditor = new FlexFormParametersEditor();
    }
    else if ("scanDocument".equals(formType))
    {
      parametersEditor = new ScanFormParametersEditor();
    }
    else parametersEditor = new DefaultFormParametersEditor();

    if (parametersPanel.getComponentCount() > 0)
    {
      parametersPanel.remove(0);
    }
    if (parametersEditor != null)
    {
      Component comp = parametersEditor.getEditingComponent(dialog, formNode);
      parametersPanel.add(comp, BorderLayout.CENTER);
    }
    dialog.adjustSizeAndPosition();
    dialog.repaint();
  }
}
