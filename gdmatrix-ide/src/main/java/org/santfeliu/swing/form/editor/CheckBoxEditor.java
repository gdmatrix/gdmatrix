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

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import org.santfeliu.swing.form.ComponentEditor;
import org.santfeliu.swing.form.ComponentView;
import org.santfeliu.swing.form.view.CheckBoxView;

/**
 *
 * @author realor
 */
public class CheckBoxEditor extends JPanel
  implements ComponentEditor
{
  private CheckBoxView checkBoxView;
  private BorderLayout borderLayout = new BorderLayout();
  private JLabel variableLabel = new JLabel();
  private JTextField variableTextField = new JTextField();

  public CheckBoxEditor()
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
    this.checkBoxView = (CheckBoxView)view;
    String variable = checkBoxView.getVariable();
    variableTextField.setText(variable);
    variableTextField.setCaretPosition(0);
    return this;
  }

  @Override
  public void stopEditing()
  {
    checkBoxView.setVariable(variableTextField.getText());
  }

  @Override
  public void cancelEditing()
  {
  }

  private void initComponents()
    throws Exception
  {
    this.setLayout(borderLayout);
    this.setBorder(new EmptyBorder(10, 10, 10, 10));
    variableLabel.setText("Variable:");
    this.add(variableLabel, BorderLayout.NORTH);
    this.add(variableTextField, BorderLayout.CENTER);
  }
}
