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
package org.santfeliu.doc.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;


/**
 *
 * @author unknown
 */
public class MimeTypesPanel extends JPanel
{
  public static final String DEFAULT_OP_SEPARATOR = "*";
  
  private Map properties;
  private DefaultTableModel tableModel = new DefaultTableModel();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JToolBar toolBar = new JToolBar();
  private JButton removeButton = new JButton();
  private JScrollPane scrollPane = new JScrollPane();
  private JTable propertiesTable = new JTable();
  private JButton sortAscButton = new JButton();
  private JButton sortDescButton = new JButton();
  private ImageIcon removePropertyIcon;
  private ImageIcon sortAscIcon;
  private ImageIcon sortDescIcon;
  private String editOpLabel;
  private String openOpLabel;
  private String opSeparator = DEFAULT_OP_SEPARATOR;

  public MimeTypesPanel()
  {
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private void jbInit()
    throws Exception
  {
    removePropertyIcon = loadIcon(
      "/org/santfeliu/swing/resources/icon/remove_property.png");
    sortAscIcon = loadIcon(
      "/org/santfeliu/swing/resources/icon/sort_asc.png");
    sortDescIcon = loadIcon(
      "/org/santfeliu/swing/resources/icon/sort_desc.png");
  
    this.setSize(new Dimension(420, 323));
    this.setLayout(borderLayout1);
    toolBar.setFloatable(false);

    removeButton.setToolTipText("Remove property");
    removeButton.setIcon(removePropertyIcon);
    removeButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            removeButton_actionPerformed(e);
          }
        });
    sortAscButton.setToolTipText("Sort ascending");
    sortAscButton.setIcon(sortAscIcon);
    sortAscButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            sortAscButton_actionPerformed(e);
          }
        });
    sortDescButton.setToolTipText("Sort descending");
    sortDescButton.setIcon(sortDescIcon);
    sortDescButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            sortDescButton_actionPerformed(e);
          }
        });
    toolBar.add(removeButton, null);
    toolBar.add(sortAscButton, null);
    toolBar.add(sortDescButton, null);
    this.add(toolBar, BorderLayout.NORTH);
    scrollPane.getViewport().add(propertiesTable, null);
    scrollPane.getViewport().setBackground(propertiesTable.getBackground());
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    this.add(scrollPane, BorderLayout.CENTER);

    propertiesTable.setAutoCreateColumnsFromModel(false);
    tableModel.addColumn("MimeType"); //TODO
    tableModel.addColumn("Operation");    
    tableModel.addColumn("Application path");
    propertiesTable.setModel(tableModel);    
    UIManager.put("Table.focusCellHighlightBorder", 
      new LineBorder(Color.gray, 1));

    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
    renderer.setHorizontalAlignment(JLabel.LEFT);
    
    MimeTypeRenderer mimeTypeRenderer = new MimeTypeRenderer();

    JTextField textField = new JTextField();
    textField.setHorizontalAlignment(JTextField.LEFT);
    textField.setMargin(new Insets(0, 0, 0, 0));      
    DefaultCellEditor nonEditor = new DefaultCellEditor(textField)
    {
      public boolean isCellEditable(EventObject anEvent)
      {
        return false;
      }
    };
    DefaultCellEditor editor = new DefaultCellEditor(textField);
    editor.setClickCountToStart(1);

    propertiesTable.addColumn(new TableColumn(0, 100, mimeTypeRenderer, nonEditor));
    propertiesTable.addColumn(new TableColumn(1, 25, renderer, nonEditor));    
    propertiesTable.addColumn(new TableColumn(2, 275, renderer, editor));

    propertiesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    propertiesTable.setRowHeight(24);
    propertiesTable.getTableHeader().setReorderingAllowed(false);
  }
  
  public void setEnabled(boolean enabled)
  {
    super.setEnabled(enabled);
    propertiesTable.setEnabled(enabled);
    removeButton.setEnabled(enabled);
    sortAscButton.setEnabled(enabled);
    sortDescButton.setEnabled(enabled);
  }

  public static void main(String args[])
  {
    HashMap properties = new HashMap();
    properties.put("text/plain.open", "1");
    properties.put("application/msword.edit", "2");
    properties.put("image/jpeg.edit", "3");
  
    MimeTypesPanel panel = new MimeTypesPanel();
    panel.setProperties(properties);

    
    JFrame frame = new JFrame();
    frame.setSize(500, 500);
    frame.getContentPane().add(panel);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
  }

  public void setProperties(Map properties)
  {
    this.properties = properties;
    putProperties(properties, 0);
  }

  public void setProperties(Map properties, int sort)
  {
    propertiesTable.editingCanceled(new ChangeEvent(this));
    this.properties = properties;
    putProperties(properties, sort);
  }

  public Map getProperties()
  {
    Map newProperties = new HashMap();
    propertiesTable.editingStopped(new ChangeEvent(this));
    for (int i = 0; i < tableModel.getRowCount(); i++)
    {
      String propertyName = (String)tableModel.getValueAt(i, 0) + opSeparator +
        (String)tableModel.getValueAt(i, 1);
      if (propertyName != null && propertyName.trim().length() > 0)
      {
        propertyName = propertyName.trim();
        Object propertyValue = tableModel.getValueAt(i, 2);
        if ("".equals(propertyValue)) propertyValue = null;
        newProperties.put(propertyName, propertyValue);
      }
    }
    return newProperties;
  }
  
  public Map getChangedProperties(Object deleteValue)
  {
    Map newProperties = new HashMap();
    Map changedProperties = new HashMap();
    propertiesTable.editingStopped(new ChangeEvent(this));
    for (int i = 0; i < tableModel.getRowCount(); i++)
    {
      String propertyName = (String)tableModel.getValueAt(i, 0);
      if (propertyName != null && propertyName.trim().length() > 0)
      {
        propertyName = propertyName.trim();
        Object newPropertyValue = tableModel.getValueAt(i, 1);
        if ("".equals(newPropertyValue)) newPropertyValue = null;
        boolean existsOldProperty = properties.containsKey(propertyName);
        Object oldPropertyValue = properties.get(propertyName);
      
        newProperties.put(propertyName, newPropertyValue);
        if (newPropertyValue == null)
        {
          if (oldPropertyValue != null || !existsOldProperty)
          {
            changedProperties.put(propertyName, newPropertyValue);
          }
        }
        else if (!newPropertyValue.equals(oldPropertyValue))
        {
          changedProperties.put(propertyName, newPropertyValue);
        }
      }
    }
    Set entries = properties.entrySet();
    Iterator iter = entries.iterator();
    while (iter.hasNext())
    {
      Map.Entry entry = (Map.Entry)iter.next();
      String propertyName = (String)entry.getKey();
      if (!newProperties.containsKey(propertyName))
      {
        changedProperties.put(propertyName, deleteValue);
      }
    }
    return changedProperties;
  }
  
  public void setMimeTypeColumnLabel(String label)
  {
    setColumnLabel(0, label);
  }

  public void setOperationColumnLabel(String label)
  {
    setColumnLabel(1, label);
  }
  
  public void setAppPathColumnLabel(String label)
  {
    setColumnLabel(2, label);
  }
  
  public void setOperationValueLabels(String open, String edit)
  {
    this.editOpLabel = edit;
    this.openOpLabel = open;
  }
  
  private String getOperation(String operation)
  {
    if (operation != null)
    {
      if ("EDIT".equals(operation.toUpperCase()))
        return this.editOpLabel;
      else if ("OPEN".equals(operation.toUpperCase()))
        return this.openOpLabel;
    }
    
    return "";
  }
  
  private void setColumnLabel(int columnIndex, String label)
  {
    TableColumn column = propertiesTable.getColumnModel().getColumn(columnIndex);
    column.setHeaderValue(label);    
  }
  
  private void putProperties(Map properties, int sort)
  {
    tableModel.setRowCount(0);
    if (properties != null)
    {
      Set entries = properties.entrySet();
      Iterator iter = entries.iterator();
      while (iter.hasNext())
      {
        Map.Entry entry = (Map.Entry)iter.next();
        String propertyName = (String)entry.getKey();
        String mimeTypeName = propertyName;
        String mimeTypeOperation = "";

        propertyName = transformOldVersionProperty(propertyName);
        if (propertyName.contains(opSeparator))
        {
          int dotidx = propertyName.indexOf(opSeparator);
          mimeTypeName = propertyName.substring(0, dotidx);
          mimeTypeOperation = propertyName.substring(dotidx + 1);
        }
        Object propertyValue = entry.getValue();
        if (sort == 0)
        {
          tableModel.addRow(new Object[]{mimeTypeName, mimeTypeOperation, propertyValue});
        }
        else
        {
          int row = findInsertRow(propertyName, sort);
          if (row < tableModel.getRowCount())
            tableModel.insertRow(row, new Object[]{mimeTypeName, mimeTypeOperation, propertyValue});
          else
            tableModel.addRow(new Object[]{mimeTypeName, mimeTypeOperation, propertyValue});
        }
      }
    }
  }

  //transforms old versions property format of filetypes.properties to new one.
  private String transformOldVersionProperty(String propertyName)
  {
    if (propertyName != null)
    {
      int idx = propertyName.toUpperCase().indexOf(".EDIT");
      if (idx > 0)
      {
        String oldPropertyName = propertyName.substring(0, idx);
        propertyName = oldPropertyName +  "*edit";
      }
      else
      {
        idx = propertyName.toUpperCase().indexOf(".OPEN");
        if (idx > 0)
        {
          String oldPropertyName = propertyName.substring(0, idx);
          propertyName = oldPropertyName +  "*open";
        }
      }
    }
    
    return propertyName;
  }
  private int findInsertRow(String insertPropertyName, int sort)
  {
    boolean found = false;
    int i = 0;
    while (!found && i < tableModel.getRowCount())
    {
      String propertyName = (String)tableModel.getValueAt(i, 0);
      int result = insertPropertyName.compareTo(propertyName);
      if (sort == 1)
      {
        found = result < 0;
      }
      else if (sort == -1)
      {
        found = result > 0;
      }
      if (!found) i++;
    }
    return i;
  }

  private void removeButton_actionPerformed(ActionEvent e)
  {
    int selRow = propertiesTable.getSelectedRow();
    if (selRow >= 0 && selRow < tableModel.getRowCount())
    {
      if (propertiesTable.isEditing())
      {
        propertiesTable.getCellEditor().cancelCellEditing();
      }
      tableModel.removeRow(selRow);
    }
  }
  
  private void sortAscButton_actionPerformed(ActionEvent e)
  {
    System.out.println(getChangedProperties("[DELETE]"));
    putProperties(getProperties(), 1);
  }

  private void sortDescButton_actionPerformed(ActionEvent e)
  {
    System.out.println(getChangedProperties("[DELETE]"));
    putProperties(getProperties(), -1);
  }

  private ImageIcon loadIcon(String path)
  {
    try
    {
      return new ImageIcon(getClass().getResource(path));
    }
    catch (Exception ex)
    {
      return null;
    }
  }


  public void setOpSeparator(String opSeparator)
  {
    this.opSeparator = opSeparator;
  }

  public String getOpSeparator()
  {
    return opSeparator;
  }
}
