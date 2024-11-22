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
package org.santfeliu.swing.form.editor;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import org.santfeliu.swing.form.ComponentEditor;
import org.santfeliu.swing.form.ComponentView;
import org.santfeliu.swing.form.view.InputTextView;


/**
 *
 * @author realor
 */
public class InputTextEditor extends JPanel
  implements ComponentEditor
{
  private InputTextView textFieldView;
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel variableLabel = new JLabel();
  private JTextField variableTextField = new JTextField();
  private JLabel formatLabel = new JLabel();
  private JLabel maxLengthLabel = new JLabel();
  private JSpinner maxLengthSpinner = new JSpinner();
  private JLabel requiredLabel = new JLabel();
  private JCheckBox requiredCheckBox = new JCheckBox();
  private JLabel helpLabel = new JLabel();
  private JComboBox formatComboBox = new JComboBox();

  public InputTextEditor()
  {
    try
    {
      initComponents();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  @Override
  public Component getEditingComponent(ComponentView view)
  {
    this.textFieldView = (InputTextView)view;
    variableTextField.setText(textFieldView.getVariable());
    formatComboBox.setSelectedItem(textFieldView.getFormat());
    if (textFieldView.getMaxLength() == null)
    {
      maxLengthSpinner.setValue(0);
    }
    else
    {
      maxLengthSpinner.setValue(textFieldView.getMaxLength());
    }
    requiredCheckBox.setSelected(textFieldView.isRequired());
    return this;
  }

  @Override
  public void stopEditing() throws Exception
  {
    String variable = variableTextField.getText();
    if (variable == null || variable.trim().length() == 0)
      throw new Exception("variable is mandatory");

    textFieldView.setVariable(variableTextField.getText());
    textFieldView.setFormat((String)formatComboBox.getSelectedItem());
    Integer maxLength = (Integer)maxLengthSpinner.getValue();
    if (maxLength.intValue() == 0) maxLength = null;
    textFieldView.setMaxLength(maxLength);
    textFieldView.setRequired(requiredCheckBox.isSelected());
  }

  @Override
  public void cancelEditing()
  {
  }

  private void initComponents()
    throws Exception
  {
    this.setLayout(gridBagLayout1);
    this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    variableLabel.setText("Variable:");
    formatLabel.setText("Format:");
    maxLengthLabel.setText("Max length:");
    this.add(variableLabel,
      new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(2, 2, 2, 6), 0, 0));
    this.add(variableTextField,
      new GridBagConstraints(1, 0, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
        new Insets(2, 2, 2, 2), 0, 0));
    this.add(formatLabel,
      new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(2, 2, 2, 6), 0, 0));
    this.add(maxLengthLabel,
      new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(2, 2, 2, 6), 0, 0));
    this.add(maxLengthSpinner,
      new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(2, 2, 2, 2), 0, 0));

    this.add(requiredLabel,
      new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(2, 2, 2, 6), 0, 0));
    this.add(requiredCheckBox,
      new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(2, 2, 2, 2), 0, 0));
    this.add(helpLabel,
      new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 2, 2, 2), 0, 0));
    this.add(formatComboBox,
      new GridBagConstraints(1, 1, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
        new Insets(2, 2, 2, 2), 0, 0));
    maxLengthSpinner.setModel(new SpinnerNumberModel(0, 0, 1000, 1));
    requiredLabel.setText("Required:");
    helpLabel.setText("(0: any length)");
    formatComboBox.setEditable(true);
    formatComboBox.addItem("text");
    formatComboBox.addItem("text:<regex>");
    formatComboBox.addItem("number");
    formatComboBox.addItem("number:<min>,<max>");
    formatComboBox.addItem("boolean");
    formatComboBox.addItem("date");
    formatComboBox.addItem("date:dd/MM/yyyy");
    formatComboBox.addItem("date:MM-dd-yyyy");
    formatComboBox.addItem("time");
    formatComboBox.addItem("datetime");
    formatComboBox.addItem("datetime:dd/MM/yyyy HH:mm");
    formatComboBox.addItem("datetime:MM-dd-yyyy HH:mm");
  }
}
