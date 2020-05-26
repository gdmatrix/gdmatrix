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
import org.apache.commons.lang.StringUtils;
import org.santfeliu.matrix.ide.MatrixIDE;
import org.santfeliu.matrix.ide.Options;
import org.santfeliu.workflow.WorkflowNode;
import org.santfeliu.workflow.node.CreateInstanceNode;
import org.santfeliu.workflow.swing.NodeEditor;
import org.santfeliu.workflow.swing.NodeEditorDialog;

/**
 *
 * @author realor
 */
public class CreateInstanceNodeEditor extends JPanel implements NodeEditor
{
  private CreateInstanceNode createInstanceNode;
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel wfNameLabel = new JLabel();
  private JLabel valueLabel = new JLabel();
  private JLabel parametersLabel = new JLabel();
  private JTextField wfNameTextField = new JTextField();
  private JTextArea parametersTextArea = new JTextArea();
  private JScrollPane scrollPane = new JScrollPane();
  private JCheckBox waitResultCheckBox = new JCheckBox();

  public CreateInstanceNodeEditor()
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
    this.createInstanceNode = (CreateInstanceNode)node;
    Object wfName = createInstanceNode.getWorkflowName();
    if (wfName != null && String.valueOf(wfName).trim().length() > 0)
    {
      wfNameTextField.setText(wfName.toString());
    }
    boolean waitResult = createInstanceNode.isWaitResult();
    waitResultCheckBox.setSelected(waitResult);

    parametersTextArea.setText(
      createInstanceNode.getParameters().saveToString());
    parametersTextArea.setCaretPosition(0);
    return this;
  }

  @Override
  public void checkValues() throws Exception
  {
    String wfName = wfNameTextField.getText();
    if (StringUtils.isBlank(wfName))
    {
      throw new Exception("Workflow name is mandatory");
    }
  }

  @Override
  public void stopEditing() throws Exception
  {
    checkValues();
    createInstanceNode.setWorkflowName(wfNameTextField.getText());
    createInstanceNode.setWaitResult(waitResultCheckBox.isSelected());
    createInstanceNode.getParameters().clear();
    createInstanceNode.getParameters().loadFromString(
      parametersTextArea.getText());
  }

  @Override
  public void cancelEditing()
  {
  }

  private void initComponents() throws Exception
  {
    this.setLayout(gridBagLayout1);
    wfNameLabel.setText("Workflow name:");
    valueLabel.setText("Wait for result:");
    parametersLabel.setText("Parameters:");
    wfNameTextField.setPreferredSize(new Dimension(140, 24));
    wfNameTextField.setMinimumSize(new Dimension(140, 24));
    parametersTextArea.setFont(Options.getEditorFont());
    this.add(wfNameLabel,
      new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(valueLabel,
      new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(parametersLabel,
      new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(wfNameTextField,
      new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(scrollPane,
      new GridBagConstraints(1, 2, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(2, 4, 2, 4), 0, 0));
    this.add(waitResultCheckBox,
      new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(2, 4, 2, 4), 0, 0));
    scrollPane.getViewport().add(parametersTextArea);
  }
}
