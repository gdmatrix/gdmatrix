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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import java.util.ArrayList;
import java.util.List;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
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

import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.DictionaryManagerPort;
import org.matrix.dic.DictionaryManagerService;
import org.matrix.security.AccessControl;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;

/**
 *
 * @author unknown
 */
public class DocumentSecurityPanel extends JPanel
{
  private List<String> actionItems;
  private List<AccessControl> accessControlList;
  private String typeId;
  private BorderLayout borderLayout1 = new BorderLayout();
  private JToolBar toolBar = new JToolBar();
  private JButton addButton = new JButton();
  private JButton removeButton = new JButton();
  private JScrollPane scrollPane = new JScrollPane();
  private JTable accessControlTable = new JTable();
  private JButton sortAscButton = new JButton();
  private JButton sortDescButton = new JButton();
  private ImageIcon addPropertyIcon;
  private ImageIcon removePropertyIcon;
  private ImageIcon sortAscIcon;
  private ImageIcon sortDescIcon;
  private boolean editable = true;

  private URL wsDirectoryURL;
  private String username;
  private String password;

  private ResourceBundle resourceBundle =
    loadResourceBundle(Locale.getDefault());

  private DefaultTableModel tableModel = new DefaultTableModel()
  {
    @Override
    public boolean isCellEditable(int row, int column)
    {
      return editable;
    }
  };

  public DocumentSecurityPanel()
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

  public String getPassword()
  {
    return password;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }

  public URL getWsDirectoryURL()
  {
    return wsDirectoryURL;
  }

  public void setWsDirectoryURL(URL wsDirectoryURL)
  {
    this.wsDirectoryURL = wsDirectoryURL;
  }

  public String getUsername()
  {
    return username;
  }

  public void setUsername(String username)
  {
    this.username = username;
  }

  public String getTypeId()
  {
    return typeId;
  }

  public void setTypeId(String typeId)
  {
    this.typeId = typeId;
  }

  private void jbInit()
    throws Exception
  {
    addPropertyIcon = loadIcon(
      "/org/santfeliu/swing/resources/icon/add_property.png");
    removePropertyIcon = loadIcon(
      "/org/santfeliu/swing/resources/icon/remove_property.png");
    sortAscIcon = loadIcon(
      "/org/santfeliu/swing/resources/icon/sort_asc.png");
    sortDescIcon = loadIcon(
      "/org/santfeliu/swing/resources/icon/sort_desc.png");
  
    this.setSize(new Dimension(420, 323));
    this.setLayout(borderLayout1);
    toolBar.setFloatable(false);
    
    addButton.setToolTipText("Add");
    addButton.setIcon(addPropertyIcon);
    addButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            addButton_actionPerformed(e);
          }
        });
    removeButton.setToolTipText("Remove");
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
    toolBar.add(addButton, null);
    toolBar.add(removeButton, null);
    toolBar.add(sortAscButton, null);
    toolBar.add(sortDescButton, null);
    this.add(toolBar, BorderLayout.NORTH);
    scrollPane.getViewport().add(accessControlTable, null);
    scrollPane.getViewport().setBackground(accessControlTable.getBackground());
    scrollPane.setVerticalScrollBarPolicy(
      ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    this.add(scrollPane, BorderLayout.CENTER);

    accessControlTable.setAutoCreateColumnsFromModel(false);
    tableModel.addColumn("RoleId");
    tableModel.addColumn("Action");
    accessControlTable.setModel(tableModel);
    UIManager.put("Table.focusCellHighlightBorder", 
      new LineBorder(Color.gray, 1));
    accessControlTable.setSelectionBackground(
      accessControlTable.getBackground());
    accessControlTable.setSelectionForeground(
      accessControlTable.getForeground());

    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
    renderer.setHorizontalAlignment(JLabel.LEFT);

    JTextField textField = new JTextField();
    textField.setHorizontalAlignment(JTextField.LEFT);
    textField.setMargin(new Insets(0, 0, 0, 0));      
    DefaultCellEditor editor = new DefaultCellEditor(textField);
    editor.setClickCountToStart(1);

    accessControlTable.addColumn(new TableColumn(0, 300, renderer, editor));
    accessControlTable.addColumn(new TableColumn(1, 300, renderer, editor));

    accessControlTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    accessControlTable.setRowHeight(24);
    accessControlTable.getTableHeader().setReorderingAllowed(false);
    
    TableColumn actionColumn = accessControlTable.getColumnModel().getColumn(1);
    actionColumn.setCellRenderer(new DefaultTableCellRenderer()
      {
        public Component getTableCellRendererComponent(JTable table, 
          Object value, boolean isSelected, boolean hasFocus, int row, 
          int column) 
        {
          setValue(getLocalizedText((String)value));
          return this;
        }
      }
    );
    
  }

  public void setEnabled(boolean enabled)
  {
    super.setEnabled(enabled);
    accessControlTable.setEnabled(enabled);
    addButton.setEnabled(enabled);
    removeButton.setEnabled(enabled);
    sortAscButton.setEnabled(enabled);
    sortDescButton.setEnabled(enabled);
  }

  public void setAccessControlList(List<AccessControl> accessControlList)
  {
    this.accessControlList = accessControlList;
    putAccessControlList(accessControlList, 0);
  }

  public void setAccessControlList(List<AccessControl> accessControlList,
    int sort)
  {
    accessControlTable.editingCanceled(new ChangeEvent(this));
    this.accessControlList = accessControlList;
    putAccessControlList(accessControlList, sort);
  }

  public List<AccessControl> getAccessControlList()
  {
    List<AccessControl> result = new ArrayList<AccessControl>();
    accessControlTable.editingStopped(new ChangeEvent(this));
    for (int i = 0; i < tableModel.getRowCount(); i++)
    {
      String roleId = (String)tableModel.getValueAt(i, 0);
      if (roleId != null && roleId.trim().length() > 0)
      {
        roleId = roleId.trim();
        String action = (String)tableModel.getValueAt(i, 1);
        AccessControl accessControl = new AccessControl();
        accessControl.setRoleId(roleId);
        accessControl.setAction(action);
        result.add(accessControl);
      }
    }    
    return result;
  }

  private void putAccessControlList(List<AccessControl> accessControlList,
    int sort)
  {
    tableModel.setRowCount(0);
    if (accessControlList != null)
    {
      for (AccessControl accessControl : accessControlList)
      {
        String roleId = accessControl.getRoleId();
        String action = accessControl.getAction();
        JComboBox actionComboBox = getActionComboBox();
        actionComboBox.setSelectedItem(action);
        TableColumn column = accessControlTable.getColumnModel().getColumn(1);
        column.setCellEditor(new DefaultCellEditor(actionComboBox));
        
        if (sort == 0)
        {
          tableModel.addRow(new Object[]{roleId, action});
        }
        else
        {
          int row = findInsertRow(roleId, sort);
          if (row < tableModel.getRowCount())
            tableModel.insertRow(row, new Object[]{roleId, action});
          else
            tableModel.addRow(new Object[]{roleId, action});
        }
      }
    }
  }

  private int findInsertRow(String insertRoleId, int sort)
  {
    boolean found = false;
    int i = 0;
    while (!found && i < tableModel.getRowCount())
    {
      String roleId = (String)tableModel.getValueAt(i, 0);
      int result = insertRoleId.compareTo(roleId);
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
    JComboBox actionComboBox = getActionComboBox();

    TableColumn column = accessControlTable.getColumnModel().getColumn(1);
    column.setCellEditor(new DefaultCellEditor(actionComboBox));
    
    tableModel.addRow(new Object[]{null, getActionItems().get(0)});
    int lastRow = tableModel.getRowCount() - 1;
    accessControlTable.requestFocus();
    accessControlTable.getSelectionModel().setSelectionInterval(lastRow,
      lastRow);
    accessControlTable.editCellAt(lastRow, 0);
  }

  private void removeButton_actionPerformed(ActionEvent e)
  {
    int selRow = accessControlTable.getSelectedRow();
    if (selRow >= 0 && selRow < tableModel.getRowCount())
    {
      if (accessControlTable.isEditing())
      {
        accessControlTable.getCellEditor().cancelCellEditing();
      }
      tableModel.removeRow(selRow);
    }
  }
  
  private void sortAscButton_actionPerformed(ActionEvent e)
  {
    if (isTableCompleted())
    {
      putAccessControlList(getAccessControlList(), 1);
    }
  }

  private void sortDescButton_actionPerformed(ActionEvent e)
  {
    if (isTableCompleted())
    {
      putAccessControlList(getAccessControlList(), -1);
    }
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
  
  private boolean isTableCompleted()
  {
    for (int i = 0; i < tableModel.getRowCount(); i++)
    {
      Object roleId = tableModel.getValueAt(i, 0);
      Object action = tableModel.getValueAt(i, 1);
      if ((roleId == null) ||
          (action == null))
      {
        return false;
      }
    }
    return true;
  }

  private JComboBox getActionComboBox()
  {
    JComboBox actionComboBox = new JComboBox();
    for (String actionItem : getActionItems())
    {
      actionComboBox.addItem(actionItem);
    }
    actionComboBox.setRenderer
    (
      new DefaultListCellRenderer()
      {
        public Component getListCellRendererComponent(JList list, Object value,
          int index, boolean isSelected, boolean cellHasFocus) 
        {
          if (isSelected) {
              setBackground(list.getSelectionBackground());
              setForeground(list.getSelectionForeground());
          } else {
              setBackground(list.getBackground());
              setForeground(list.getForeground());
          }
          setText(getLocalizedText((String)value));
          return this;
        }      
      }
    );
    return actionComboBox;
  }

  public void setActionItems(List<String> actionItems)
  {
    this.actionItems = actionItems;
  }
  
  public List<String> getActionItems()
  {
    if (actionItems == null)
    {
      actionItems = new ArrayList<String>();
      actionItems.addAll(DictionaryConstants.standardActions);
      try
      {
        if (wsDirectoryURL != null)
        {
          WSDirectory wsDirectory = WSDirectory.getInstance(wsDirectoryURL);
          WSEndpoint endpoint = wsDirectory.getEndpoint(DictionaryManagerService.class);
          DictionaryManagerPort dicPort = endpoint.getPort(DictionaryManagerPort.class);
          List<String> typeActions = dicPort.getTypeActions(typeId);
          actionItems.addAll(typeActions);
        }
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
    return actionItems;
  }

  private ResourceBundle loadResourceBundle(Locale locale)
  {
    return ResourceBundle.getBundle(
      "org.santfeliu.doc.swing.resources.DocumentPanelBundle", locale);
  }

  private String getLocalizedText(String text)
  {
    String result = null;
    try
    {
      result = resourceBundle.getString(text);
    }
    catch (MissingResourceException ex)
    {
      result = text;
    }
    return result;
  }

}
