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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.santfeliu.matrix.ide.MatrixIDE;
import org.santfeliu.util.Properties;
import org.santfeliu.workflow.WorkflowNode;
import org.santfeliu.workflow.node.FormNode;
import org.santfeliu.workflow.swing.NodeEditor;
import org.santfeliu.workflow.swing.NodeEditorDialog;

/**
 *
 * @author realor
 */
public class CustomFormParametersEditor extends JPanel implements NodeEditor
{

  private FormNode formNode;
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel warningLabel = new JLabel();
  private JLabel typeLabel = new JLabel();
  private JComboBox typeComboBox = new JComboBox();
  private JLabel valueLabel = new JLabel();
  private JTextField valueTextField = new JTextField();

  public CustomFormParametersEditor()
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
    this.formNode = (FormNode)node;
    Properties parameters = formNode.getParameters();
    String type = (String)parameters.getProperty("type");
    String ref = (String)parameters.getProperty("ref");

    if ("form".equals(type))
    {
      typeComboBox.setSelectedIndex(0);
    }
    else if ("html".equals(type))
    {
      typeComboBox.setSelectedIndex(1);
    }
    else if ("url".equals(type))
    {
      typeComboBox.setSelectedIndex(2);
    }
    else
    {
      typeComboBox.setSelectedIndex(0);
    }
    valueTextField.setText(ref);
    return this;
  }

  @Override
  public void checkValues() throws Exception
  {
  }

  @Override
  public void stopEditing() throws Exception
  {
    checkValues();
    int index = typeComboBox.getSelectedIndex();
    String type = "url";
    if (index == 0)
    {
      type = "form";
    }
    else if (index == 1)
    {
      type = "html";
    }
    else if (index == 2)
    {
      type = "url";
    }
    String value = valueTextField.getText();
    Properties parameters = formNode.getParameters();
    parameters.clear();
    parameters.setProperty("type", type);
    parameters.setProperty("ref", value);
  }

  @Override
  public void cancelEditing()
  {
  }

  private void initComponents() throws Exception
  {
    this.setLayout(gridBagLayout1);
    warningLabel.setText("This type of form is deprecated. Use 'dynamic' form instead.");
    warningLabel.setForeground(Color.RED);
    warningLabel.setFont(getFont().deriveFont(Font.BOLD));
    typeLabel.setText("Type:");
    valueLabel.setText("Value:");
    this.add(warningLabel,
      new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(4, 4, 20, 4), 0, 0));
    this.add(typeLabel,
      new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(4, 4, 4, 4), 0, 0));
    this.add(typeComboBox,
      new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(4, 0, 4, 0), 0, 0));
    this.add(valueLabel,
      new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(4, 4, 4, 4), 0, 0));
    this.add(valueTextField,
      new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(4, 0, 4, 4), 0, 0));

    typeComboBox.addItem("HTML visual form");
    typeComboBox.addItem("HTML document");
    typeComboBox.addItem("URL");
  }
}
