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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.santfeliu.matrix.ide.MatrixIDE;
import org.santfeliu.workflow.WorkflowNode;
import org.santfeliu.workflow.node.WaitNode;
import org.santfeliu.workflow.swing.NodeEditor;
import org.santfeliu.workflow.swing.NodeEditorDialog;

/**
 *
 * @author realor
 */
public class WaitNodeEditor extends JPanel implements NodeEditor
{
  private WaitNode waitNode;
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel durationLabel = new JLabel();
  private JTextField durationTextField = new JTextField();
  private JLabel dateTimeLabel = new JLabel();
  private JTextField dateTimeTextField = new JTextField();
  private JLabel exampleLabel2 = new JLabel();
  private JLabel exampleLabel1 = new JLabel();

  public WaitNodeEditor()
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
    waitNode = (WaitNode)node;
    durationTextField.setText(waitNode.getDuration());
    dateTimeTextField.setText(waitNode.getDateTime());
    return this;
  }

  @Override
  public void checkValues() throws Exception
  {
  }

  @Override
  public void stopEditing() throws Exception
  {
    waitNode.setDuration(durationTextField.getText());
    waitNode.setDateTime(dateTimeTextField.getText());
  }

  @Override
  public void cancelEditing()
  {
  }

  private void initComponents() throws Exception
  {
    this.setLayout(gridBagLayout1);
    durationLabel.setText("Duration:");
    dateTimeLabel.setText("Date/Time:");
    exampleLabel2.setText("(yyyyMMddHHmmss)");
    exampleLabel1.setText("(examples: 2w, 20d, 45m)");
    this.add(durationLabel,
      new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(2, 2, 2, 2), 0, 0));
    this.add(durationTextField,
      new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
        new Insets(2, 2, 2, 2), 0, 0));
    this.add(dateTimeLabel,
      new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(2, 2, 2, 2), 0, 0));
    this.add(dateTimeTextField,
      new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
        new Insets(2, 2, 2, 2), 0, 0));
    this.add(exampleLabel2,
      new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(2, 2, 2, 2), 0, 0));
    this.add(exampleLabel1,
      new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(2, 2, 2, 2), 0, 0));
  }
}
