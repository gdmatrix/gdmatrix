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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.matrix.ide.Options;
import org.santfeliu.swing.form.ComponentEditor;
import org.santfeliu.swing.form.ComponentView;
import org.santfeliu.swing.form.view.SelectBoxView;
import org.santfeliu.swing.text.SQLEditorKit;
import org.santfeliu.swing.text.SymbolHighlighter;
import org.santfeliu.swing.text.TextEditor;


/**
 *
 * @author unknown
 */
public class SelectBoxEditor extends JPanel
  implements ComponentEditor
{
  private SelectBoxView comboBoxView;
  private JScrollPane scrollPane = new JScrollPane();
  private JTable optionsTable = new JTable();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  private JButton addButton = new JButton();
  private JButton insertButton = new JButton();
  private JButton deleteButton = new JButton();
  private DefaultTableModel tableModel = new DefaultTableModel();
  private JLabel variableLabel = new JLabel();
  private JTextField variableTextField = new JTextField();
  private TextEditor sqlEditor = new TextEditor();
  private JLabel staticLabel = new JLabel();
  private JLabel dynamicLabel = new JLabel();
  private JLabel sqlLabel = new JLabel();
  private JLabel connLabel = new JLabel();
  private JLabel connInfoLabel = new JLabel();
  private JTextField connTextField = new JTextField();
  private JLabel usernameLabel = new JLabel();
  private JTextField usernameTextField = new JTextField();
  private JLabel passwordLabel = new JLabel();
  private JPasswordField passwordField = new JPasswordField();
  private JLabel datarefLabel = new JLabel();
  private JTextField datarefTextField = new JTextField();

  public SelectBoxEditor()
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
    this.comboBoxView = (SelectBoxView)view;
    variableTextField.setText(comboBoxView.getVariable());
    connTextField.setText(comboBoxView.getConnection());
    sqlEditor.getTextPane().setText(comboBoxView.getSql());
    sqlEditor.getTextPane().setCaretPosition(0);
    usernameTextField.setText(comboBoxView.getUsername());
    passwordField.setText(comboBoxView.getPassword());
    datarefTextField.setText(comboBoxView.getDataref());

    tableModel.setRowCount(0);
    Vector options = comboBoxView.getOptions();
    for (int i = 0; i < options.size(); i++)
    {
      String[] option = (String[])options.elementAt(i);
      tableModel.addRow(option);
    }
    return this;
  }

  @Override
  public void stopEditing() throws Exception
  {
    String variable = variableTextField.getText();
    if (variable == null || variable.trim().length() == 0)
      throw new Exception("variable is mandatory");

    comboBoxView.setVariable(variable);
    comboBoxView.setConnection(connTextField.getText());
    comboBoxView.setSql(sqlEditor.getTextPane().getText());
    comboBoxView.setUsername(usernameTextField.getText());
    comboBoxView.setPassword(new String(passwordField.getPassword()));
    comboBoxView.setDataref(datarefTextField.getText());

    optionsTable.editingStopped(new ChangeEvent(this));
    Vector options = comboBoxView.getOptions();
    options.clear();
    for (int i = 0; i < tableModel.getRowCount(); i++)
    {
      String label = (String)tableModel.getValueAt(i, 0);
      String code = (String)tableModel.getValueAt(i, 1);
      if (StringUtils.isBlank(label)) label = "";
      if (StringUtils.isBlank(code)) code = "";
      options.add(new String[]{label, code});
    }
  }

  @Override
  public void cancelEditing()
  {
    optionsTable.editingCanceled(new ChangeEvent(this));
  }

  private void initComponents()
    throws Exception
  {
    this.setLayout(gridBagLayout2);
    this.setSize(new Dimension(400, 340));
    this.setMinimumSize(new Dimension(400, 340));
    addButton.setText("Add");
    addButton.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            addButton_actionPerformed(e);
          }
        });
    insertButton.setText("Insert");
    insertButton.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            insertButton_actionPerformed(e);
          }
        });
    deleteButton.setText("Delete");
    deleteButton.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            deleteButton_actionPerformed(e);
          }
        });
    JTextPane sqlTextPane = sqlEditor.getTextPane();
    sqlTextPane.setEditorKitForContentType("text/sql", new SQLEditorKit());
    sqlTextPane.setContentType("text/sql");
    sqlTextPane.setSelectionColor(new Color(198, 198, 198));
    sqlTextPane.setFont(Options.getEditorFont());

    scrollPane.getViewport().add(optionsTable, null);
    scrollPane.setPreferredSize(new Dimension(400, 150));
    sqlEditor.setPreferredSize(new Dimension(300, 100));
    sqlEditor.setMinimumSize(new Dimension(300, 100));
    sqlEditor.setBorder(new LineBorder(Color.LIGHT_GRAY));
    
    SymbolHighlighter symbolHighlighter = 
      new SymbolHighlighter(sqlEditor.getTextPane(), "({[", ")}]");
    
    datarefLabel.setText("Dataref:");

    this.add(scrollPane,
             new GridBagConstraints(1, 2, 2, 3, 1.0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                    new Insets(4, 4, 4, 4), 0, 0));
    this.add(addButton,
             new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                                    new Insets(4, 4, 4, 4), 0, 0));
    this.add(insertButton,
             new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                                    new Insets(4, 4, 4, 4), 0, 0));
    this.add(deleteButton,
             new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                                    new Insets(4, 4, 4, 4), 0, 0));

    this.add(variableLabel,
             new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                    new Insets(4, 4, 4, 4), 0, 0));
    this.add(variableTextField,
             new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                                    new Insets(4, 4, 4, 4), 0, 0));
    this.add(sqlEditor,
             new GridBagConstraints(1, 7, 2, 1, 0.0, 0.5, GridBagConstraints.WEST, GridBagConstraints.BOTH,
                                    new Insets(4, 4, 4, 4), 0, 0));
    this.add(staticLabel,
             new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                                    new Insets(4, 4, 4, 4), 0, 0));
    this.add(dynamicLabel,
             new GridBagConstraints(0, 5, 4, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                                    new Insets(4, 4, 4, 4), 0, 0));
    this.add(sqlLabel,
             new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                                    new Insets(4, 4, 4, 4), 0, 0));
    this.add(connLabel,
             new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                    new Insets(4, 4, 4, 4), 0, 0));
    this.add(connTextField,
             new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                                    new Insets(4, 4, 4, 4), 0, 0));
    this.add(connInfoLabel,
             new GridBagConstraints(2, 6, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                    new Insets(4, 4, 4, 4), 0, 0));
    this.add(usernameLabel,
             new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                                    new Insets(4, 4, 4, 4), 0, 0));
    this.add(usernameTextField,
             new GridBagConstraints(1, 8, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                                    new Insets(4, 4, 4, 4), 0, 0));
    this.add(passwordLabel,
             new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                    new Insets(4, 4, 4, 4), 0, 0));
    this.add(passwordField,
             new GridBagConstraints(1, 9, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                                    new Insets(4, 4, 4, 4), 0, 0));
    this.add(datarefLabel,
             new GridBagConstraints(0, 10, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                    new Insets(4, 4, 4, 4), 0, 0));
    this.add(datarefTextField,
             new GridBagConstraints(1, 10, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                    new Insets(4, 4, 4, 4), 0, 0));


    optionsTable.setAutoCreateColumnsFromModel(false);
    tableModel.addColumn("Label");
    tableModel.addColumn("Value");
    optionsTable.setModel(tableModel);
    DefaultCellEditor editor = new DefaultCellEditor(new JTextField());
    editor.setClickCountToStart(2);

    passwordField.setMinimumSize(new Dimension(150, 24));
    passwordField.setPreferredSize(new Dimension(150, 24));
    passwordLabel.setText("Password:");
    usernameTextField.setMinimumSize(new Dimension(150, 24));
    usernameTextField.setPreferredSize(new Dimension(150, 24));
    usernameLabel.setText("Username:");
    connLabel.setText("Connection:");
    connInfoLabel.setText("DSN or alias");
    connTextField.setMinimumSize(new Dimension(150, 24));
    connTextField.setPreferredSize(new Dimension(150, 24));
    sqlLabel.setText("SQL (id, label):");
    dynamicLabel.setText("Dynamic options:");
    staticLabel.setText("Static options:");
    variableLabel.setText("Variable:");
    optionsTable.addColumn(new TableColumn(0, 300, null, editor));
    optionsTable.addColumn(new TableColumn(1, 100, null, editor));
    optionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    optionsTable.setRowHeight(20);
    optionsTable.getTableHeader().setReorderingAllowed(false);
  }

  private void addButton_actionPerformed(ActionEvent e)
  {
    optionsTable.editingStopped(new ChangeEvent(this));
    tableModel.addRow(new Object[]{"", ""});
    int row = tableModel.getRowCount() - 1;
    optionsTable.setRowSelectionInterval(row, row);
    optionsTable.requestFocus();
    optionsTable.editCellAt(row, 0);
  }

  private void insertButton_actionPerformed(ActionEvent e)
  {
    optionsTable.editingStopped(new ChangeEvent(this));
    int row = optionsTable.getSelectedRow();
    if (row != -1)
    {
      tableModel.insertRow(row, new Object[]{"", ""});
      optionsTable.setRowSelectionInterval(row, row);
      optionsTable.requestFocus();
      optionsTable.editCellAt(row, 0);
    }
  }

  private void deleteButton_actionPerformed(ActionEvent e)
  {
    optionsTable.editingCanceled(new ChangeEvent(this));
    int row = optionsTable.getSelectedRow();
    if (row != -1)
    {
      tableModel.removeRow(row);
    }
  }
}
