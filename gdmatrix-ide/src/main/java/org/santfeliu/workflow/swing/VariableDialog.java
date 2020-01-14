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
import java.awt.Frame;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultCellEditor;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 *
 * @author unknown
 */
public class VariableDialog extends JDialog 
{
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel southPanel = new JPanel();
  private JButton acceptButton = new JButton();
  private JScrollPane scrollPane = new JScrollPane();
  private JTable variableTable = new JTable();
  private DefaultTableModel tableModel;
  private HashMap map = new HashMap();

  public VariableDialog()
  {
    this(null, "", true);
  }

  public VariableDialog(Frame parent, String title, boolean modal)
  {
    super(parent, title, modal);
    try
    {
      jbInit();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception
  {
    this.setSize(new Dimension(341, 382));
    this.getContentPane().setLayout(borderLayout1);
    acceptButton.setText("Accept");
    southPanel.add(acceptButton, null);
    this.getContentPane().add(southPanel, BorderLayout.SOUTH);
    scrollPane.getViewport().add(variableTable, null);
    this.getContentPane().add(scrollPane, BorderLayout.CENTER);
    variableTable.setAutoCreateColumnsFromModel(false);
    tableModel = new DefaultTableModel();
    acceptButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          acceptButton_actionPerformed(e);
        }
      });
    tableModel.addColumn("Variable:");
    tableModel.addColumn("Valor:");
    variableTable.setModel(tableModel);
    variableTable.setRowHeight(20);

    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
    renderer.setHorizontalAlignment(JLabel.LEFT);

    DefaultCellEditor editor = new DefaultCellEditor(new JTextField());
    editor.setClickCountToStart(1);

    JTextField textField = new JTextField();
    textField.setEditable(false);
    DefaultCellEditor editorNoEdit = new DefaultCellEditor(textField);
    
    variableTable.addColumn(new TableColumn(0, 100, renderer, editorNoEdit));
    variableTable.addColumn(new TableColumn(1, 100, renderer, editor));
  }
    
  public Map getValueMap()
  {
    return map;
  }

  public void centerOnFrame()
  {
    Frame owner = (Frame)this.getOwner();
    if (owner != null)
    {
      java.awt.Point location = owner.getLocation();
      Dimension frameDim = owner.getSize();
      Dimension dialogDim = this.getSize();
      int width = (frameDim.width - dialogDim.width) / 2;
      int height = (frameDim.height - dialogDim.height) / 2;      
      this.setLocation((int)(location.x + width), 
                       (int)(location.y + height));
    }
  }

  private void acceptButton_actionPerformed(ActionEvent e)
  {
    variableTable.editingStopped(new ChangeEvent(this));
    setVisible(false);
    dispose();
  }
}