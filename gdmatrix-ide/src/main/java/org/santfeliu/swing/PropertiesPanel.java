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
package org.santfeliu.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 *
 * @author realor
 */
public class PropertiesPanel extends JPanel
{
  private Map properties;
  private BorderLayout borderLayout1 = new BorderLayout();
  private JToolBar toolBar = new JToolBar();
  private JButton addButton = new JButton();
  private JButton removeButton = new JButton();
  private JScrollPane scrollPane = new JScrollPane();
  private JTable propertiesTable = new JTable();
  private JButton sortAscButton = new JButton();
  private JButton sortDescButton = new JButton();
  private ImageIcon addPropertyIcon;
  private ImageIcon removePropertyIcon;
  private ImageIcon sortAscIcon;
  private ImageIcon sortDescIcon;
  private boolean editable = true;
  private DefaultTableModel tableModel = new DefaultTableModel()
  {
    @Override
    public boolean isCellEditable(int row, int column)
    {
      return editable;
    }
  };

  public PropertiesPanel()
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

  private void initComponents()
    throws Exception
  {
    addPropertyIcon = loadIcon(
      "/org/santfeliu/swing/resources/images/add_property.png");
    removePropertyIcon = loadIcon(
      "/org/santfeliu/swing/resources/images/remove_property.png");
    sortAscIcon = loadIcon(
      "/org/santfeliu/swing/resources/images/sort_asc.png");
    sortDescIcon = loadIcon(
      "/org/santfeliu/swing/resources/images/sort_desc.png");

    this.setSize(new Dimension(420, 323));
    this.setLayout(borderLayout1);
    toolBar.setFloatable(false);

    addButton.setToolTipText("Add");
    addButton.setIcon(addPropertyIcon);
    addButton.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            addButton_actionPerformed(e);
          }
        });
    removeButton.setToolTipText("Remove");
    removeButton.setIcon(removePropertyIcon);
    removeButton.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            removeButton_actionPerformed(e);
          }
        });
    sortAscButton.setToolTipText("Sort ascending");
    sortAscButton.setIcon(sortAscIcon);
    sortAscButton.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            sortAscButton_actionPerformed(e);
          }
        });
    sortDescButton.setToolTipText("Sort descending");
    sortDescButton.setIcon(sortDescIcon);
    sortDescButton.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            sortDescButton_actionPerformed(e);
          }
        });
    toolBar.add(addButton, null);
    toolBar.add(removeButton, null);
    toolBar.add(sortAscButton, null);
    toolBar.add(sortDescButton, null);
    this.add(toolBar, BorderLayout.NORTH);
    scrollPane.getViewport().add(propertiesTable, null);
    scrollPane.getViewport().setBackground(propertiesTable.getBackground());
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    int preferredHeight = propertiesTable.getRowHeight() * 10;
    scrollPane.setPreferredSize(new Dimension(400, preferredHeight));
    this.add(scrollPane, BorderLayout.CENTER);

    propertiesTable.setAutoCreateColumnsFromModel(false);
    tableModel.addColumn("Property");
    tableModel.addColumn("Value");
    propertiesTable.setModel(tableModel);
    UIManager.put("Table.focusCellHighlightBorder",
      new LineBorder(Color.gray, 1));
    propertiesTable.setSelectionBackground(propertiesTable.getBackground());
    propertiesTable.setSelectionForeground(propertiesTable.getForeground());

    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
    renderer.setHorizontalAlignment(JLabel.LEFT);

    JTextField textField = new JTextField();
    textField.setHorizontalAlignment(JTextField.LEFT);
    textField.setMargin(new Insets(0, 0, 0, 0));
    textField.setBorder(null);
    DefaultCellEditor editor = new DefaultCellEditor(textField);
    editor.setClickCountToStart(1);

    propertiesTable.addColumn(new TableColumn(0, 100, renderer, editor));
    propertiesTable.addColumn(new TableColumn(1, 300, renderer, editor));

    propertiesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    propertiesTable.getTableHeader().setReorderingAllowed(false);
  }

  public void setPropertyLabel(String propertyLabel)
  {
    propertiesTable.getTableHeader().getColumnModel().
      getColumn(0).setHeaderValue(propertyLabel);
  }

  public void setValueLabel(String valueLabel)
  {
    propertiesTable.getTableHeader().getColumnModel().
      getColumn(1).setHeaderValue(valueLabel);
  }

  public void setEditable(boolean editable)
  {
    this.editable = editable;
    addButton.setVisible(editable);
    removeButton.setVisible(editable);
  }

  @Override
  public void setEnabled(boolean enabled)
  {
    super.setEnabled(enabled);
    propertiesTable.setEnabled(enabled);
    addButton.setEnabled(enabled);
    removeButton.setEnabled(enabled);
    sortAscButton.setEnabled(enabled);
    sortDescButton.setEnabled(enabled);
  }

  public static void main(String args[])
  {
    HashMap properties = new HashMap();
    properties.put("name", "RICARD");
    properties.put("color.name", "RICARD");
    properties.put("set.name", "RICARD");
    properties.put("name.nodeid", null);

    PropertiesPanel panel = new PropertiesPanel();
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
      String propertyName = (String)tableModel.getValueAt(i, 0);
      if (propertyName != null && propertyName.trim().length() > 0)
      {
        propertyName = propertyName.trim();
        Object propertyValue = tableModel.getValueAt(i, 1);
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
        Object propertyValue = entry.getValue();
        if (sort == 0)
        {
          tableModel.addRow(new Object[]{propertyName, propertyValue});
        }
        else
        {
          int row = findInsertRow(propertyName, sort);
          if (row < tableModel.getRowCount())
            tableModel.insertRow(row, new Object[]{propertyName, propertyValue});
          else
            tableModel.addRow(new Object[]{propertyName, propertyValue});
        }
      }
    }
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

  private void addButton_actionPerformed(ActionEvent e)
  {
    tableModel.addRow(new Object[]{null, null});
    int lastRow = tableModel.getRowCount() - 1;
    propertiesTable.getSelectionModel().setSelectionInterval(lastRow, lastRow);
    propertiesTable.editCellAt(lastRow, 0);
    propertiesTable.getEditorComponent().requestFocus();
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
      }
    });
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
    putProperties(getProperties(), 1);
  }

  private void sortDescButton_actionPerformed(ActionEvent e)
  {
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
}
