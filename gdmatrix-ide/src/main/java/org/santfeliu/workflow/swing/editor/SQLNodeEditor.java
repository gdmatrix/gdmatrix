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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.LineBorder;
import org.santfeliu.matrix.ide.MatrixIDE;
import org.santfeliu.matrix.ide.Options;
import org.santfeliu.swing.text.SQLEditorKit;
import org.santfeliu.swing.text.TextEditor;
import org.santfeliu.workflow.WorkflowNode;
import org.santfeliu.workflow.node.SQLNode;
import org.santfeliu.workflow.swing.NodeEditor;
import org.santfeliu.workflow.swing.NodeEditorDialog;

/**
 *
 * @author realor
 */
public class SQLNodeEditor extends JPanel implements NodeEditor
{
  private SQLNode sqlNode;
  private BorderLayout borderLayout = new BorderLayout();
  private JPanel northPanel = new JPanel();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel dsnLabel = new JLabel();
  private JTextField dsnTextField = new JTextField();
  private JPanel centerPanel = new JPanel();
  private TextEditor textEditor = new TextEditor();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JLabel maxRowsLabel = new JLabel();
  private JSpinner maxRowsSpinner = new JSpinner();
  private SpinnerNumberModel spinnerModel = new SpinnerNumberModel();

  public SQLNodeEditor()
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
    sqlNode = (SQLNode)node;
    JTextPane textPane = textEditor.getTextPane();
    textPane.setText(sqlNode.getStatements());
    textPane.setCaretPosition(0);
    dsnTextField.setText(sqlNode.getDsn());
    spinnerModel.setValue(sqlNode.getMaxRows());
    return this;
  }

  @Override
  public void checkValues() throws Exception
  {
    String text = textEditor.getTextPane().getText();
    if (text == null || text.trim().length() == 0)
      throw new Exception("SQL statement is mandatory");
  }

  @Override
  public void stopEditing() throws Exception
  {
    checkValues();
    sqlNode.setDsn(dsnTextField.getText());
    sqlNode.setStatements(textEditor.getTextPane().getText());
    sqlNode.setMaxRows(spinnerModel.getNumber().intValue());
  }
  
  @Override
  public void cancelEditing()
  {
  }

  private void initComponents() throws Exception
  {
    this.setLayout(borderLayout);
    northPanel.setLayout(gridBagLayout1);
    dsnLabel.setText("DSN:");
    dsnTextField.setPreferredSize(new Dimension(200, 24));
    centerPanel.setLayout(borderLayout1);
    centerPanel.setBorder(BorderFactory.createTitledBorder("SQL statements"));
    JTextPane textPane = textEditor.getTextPane();
    textPane.setFont(Options.getEditorFont());
    northPanel.add(dsnLabel,
      new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(2, 4, 2, 4), 0, 0));
    northPanel.add(dsnTextField,
      new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(2, 0, 2, 6), 0, 0));
    northPanel.add(maxRowsLabel,
      new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(2, 4, 2, 4), 0, 0));
    northPanel.add(maxRowsSpinner,
      new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(2, 0, 2, 6), 0, 0));
    this.add(northPanel, BorderLayout.NORTH);
    textEditor.setBorder(new LineBorder(Color.LIGHT_GRAY));
    centerPanel.add(textEditor, BorderLayout.CENTER);
    this.add(centerPanel, BorderLayout.CENTER);
    textPane.setEditorKitForContentType("text/sql", new SQLEditorKit());
    textPane.setContentType("text/sql");
    textPane.setSelectionColor(new Color(198, 198, 198));
    maxRowsLabel.setText("Max. rows:");
    maxRowsSpinner.setPreferredSize(new Dimension(70, 24));
    spinnerModel.setStepSize(1);
    spinnerModel.setMinimum(1);
    maxRowsSpinner.setModel(spinnerModel);
  }
}
