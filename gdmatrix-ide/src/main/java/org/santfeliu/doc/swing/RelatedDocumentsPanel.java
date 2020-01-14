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

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
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

import org.matrix.doc.RelatedDocument;
import org.matrix.doc.RelationType;

/**
 *
 * @author unknown
 */
public class RelatedDocumentsPanel extends JPanel
{
  //private List<Vector<String>> documents;
  private List<RelatedDocument> documents;
  private BorderLayout borderLayout1 = new BorderLayout();
  private JToolBar toolBar = new JToolBar();
  private JButton addButton = new JButton();
  private JButton removeButton = new JButton();
  private JScrollPane scrollPane = new JScrollPane();
  private JTable documentsTable = new JTable();
  private JButton sortAscButton = new JButton();
  private JButton sortDescButton = new JButton();
  private ImageIcon addPropertyIcon;
  private ImageIcon removePropertyIcon;
  private ImageIcon sortAscIcon;
  private ImageIcon sortDescIcon;
  private boolean editable = true;
  private DefaultTableModel tableModel = new DefaultTableModel()
  {
    public boolean isCellEditable(int row, int column)
    {
      return editable;
    }
  };

  public RelatedDocumentsPanel()
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
    scrollPane.getViewport().add(documentsTable, null);
    scrollPane.getViewport().setBackground(documentsTable.getBackground());
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    this.add(scrollPane, BorderLayout.CENTER);

    documentsTable.setAutoCreateColumnsFromModel(false);
    tableModel.addColumn("DocId");
    tableModel.addColumn("Version");
    tableModel.addColumn("RelType");
    tableModel.addColumn("RelName");
    documentsTable.setModel(tableModel);    
    UIManager.put("Table.focusCellHighlightBorder", 
      new LineBorder(Color.gray, 1));
    documentsTable.setSelectionBackground(documentsTable.getBackground());
    documentsTable.setSelectionForeground(documentsTable.getForeground());

    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
    renderer.setHorizontalAlignment(JLabel.LEFT);

    JTextField textField = new JTextField();
    textField.setHorizontalAlignment(JTextField.LEFT);
    textField.setMargin(new Insets(0, 0, 0, 0));      
    DefaultCellEditor editor = new DefaultCellEditor(textField);
    editor.setClickCountToStart(1);

    documentsTable.addColumn(new TableColumn(0, 100, renderer, editor));
    documentsTable.addColumn(new TableColumn(1, 100, renderer, editor));
    documentsTable.addColumn(new TableColumn(2, 300, renderer, editor));
    documentsTable.addColumn(new TableColumn(3, 300, renderer, editor));

    documentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    documentsTable.setRowHeight(24);
    documentsTable.getTableHeader().setReorderingAllowed(false);
    
  }

/*
  public void setPropertyLabel(String propertyLabel)
  {
    documentsTable.getTableHeader().getColumnModel().
      getColumn(0).setHeaderValue(propertyLabel);
  }

  public void setValueLabel(String valueLabel)
  {
    documentsTable.getTableHeader().getColumnModel().
      getColumn(1).setHeaderValue(valueLabel);
  }

  public void setEditable(boolean editable)
  {
    this.editable = editable;
    addButton.setVisible(editable);
    removeButton.setVisible(editable);
  }
*/  
  public void setEnabled(boolean enabled)
  {
    super.setEnabled(enabled);
    documentsTable.setEnabled(enabled);
    addButton.setEnabled(enabled);
    removeButton.setEnabled(enabled);
    sortAscButton.setEnabled(enabled);
    sortDescButton.setEnabled(enabled);
  }
/*
  public static void main(String args[])
  {
    HashMap properties = new HashMap();
    properties.put("name", "RICARD");
    properties.put("color.name", "RICARD");
    properties.put("set.name", "RICARD");
    properties.put("name.nodeid", null);
  
    RelatedDocumentsPanel panel = new RelatedDocumentsPanel();
    panel.setProperties(properties);

    
    JFrame frame = new JFrame();
    frame.setSize(500, 500);
    frame.getContentPane().add(panel);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
  }
*/
  public void setDocuments(List<RelatedDocument> documents)
  {
    this.documents = documents;
    putDocuments(documents, 0);
  }

  public void setDocuments(List<RelatedDocument> documents, int sort)
  {
    documentsTable.editingCanceled(new ChangeEvent(this));
    this.documents = documents;
    putDocuments(documents, sort);
  }

  public List<RelatedDocument> getDocuments()
  {
    List<RelatedDocument> result = new ArrayList<RelatedDocument>();
    documentsTable.editingStopped(new ChangeEvent(this));
    for (int i = 0; i < tableModel.getRowCount(); i++)
    {
      String docId = (String)tableModel.getValueAt(i, 0);
      if (docId != null && docId.trim().length() > 0)
      {
        docId = docId.trim();
        String version = (String)tableModel.getValueAt(i, 1);
        String relType = (String)tableModel.getValueAt(i, 2);
        String relName = (String)tableModel.getValueAt(i, 3);
        RelatedDocument relatedDoc = new RelatedDocument();
        relatedDoc.setDocId(docId);
        relatedDoc.setVersion(Integer.parseInt(version));
        relatedDoc.setRelationType(RelationType.fromValue(relType));
        relatedDoc.setName(relName);
        result.add(relatedDoc);
      }
    }    
    return result;
  }

/*
  public Map getChangedProperties(Object deleteValue)
  {
    Map newProperties = new HashMap();
    Map changedProperties = new HashMap();
    documentsTable.editingStopped(new ChangeEvent(this));
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
*/

/*  
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
*/

  private void putDocuments(List<RelatedDocument> documents, int sort)
  {
    tableModel.setRowCount(0);
    if (documents != null)
    {
      for (RelatedDocument relatedDoc : documents)
      {
        String docId = relatedDoc.getDocId();
        String version = String.valueOf(relatedDoc.getVersion());
        String relType = relatedDoc.getRelationType().value();
        String relName = relatedDoc.getName();
        JComboBox relTypeComboBox = new JComboBox();
        for (RelationType relTypeItem : RelationType.values())
        {
          relTypeComboBox.addItem(relTypeItem.value());
        }
        relTypeComboBox.setSelectedItem(relType);
        TableColumn column = documentsTable.getColumnModel().getColumn(2);
        column.setCellEditor(new DefaultCellEditor(relTypeComboBox));
        
        if (sort == 0)
        {
          tableModel.addRow(new Object[]{docId, version, relType, 
            relName});
        }
        else
        {
          int row = findInsertRow(docId, sort);
          if (row < tableModel.getRowCount())
            tableModel.insertRow(row, new Object[]{docId, version, relType, 
              relName});
          else
            tableModel.addRow(new Object[]{docId, version, relType, relName});
        }
      }
    }
  }

  private int findInsertRow(String insertDocId, int sort)
  {
    boolean found = false;
    int i = 0;
    while (!found && i < tableModel.getRowCount())
    {
      String docId = (String)tableModel.getValueAt(i, 0);
      int result = insertDocId.compareTo(docId);
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
    JComboBox relTypeComboBox = new JComboBox();
    for (RelationType relType : RelationType.values())
    {
      relTypeComboBox.addItem(relType.value());
    }
    TableColumn column = documentsTable.getColumnModel().getColumn(2);
    column.setCellEditor(new DefaultCellEditor(relTypeComboBox));
    
    tableModel.addRow(new Object[]{null, null, RelationType.COMPONENT.value(), 
      null});
    int lastRow = tableModel.getRowCount() - 1;
    documentsTable.requestFocus();
    documentsTable.getSelectionModel().setSelectionInterval(lastRow, lastRow);
    documentsTable.editCellAt(lastRow, 0);      
  }

  private void removeButton_actionPerformed(ActionEvent e)
  {
    int selRow = documentsTable.getSelectedRow();
    if (selRow >= 0 && selRow < tableModel.getRowCount())
    {
      if (documentsTable.isEditing())
      {
        documentsTable.getCellEditor().cancelCellEditing();
      }
      tableModel.removeRow(selRow);
    }
  }
  
  private void sortAscButton_actionPerformed(ActionEvent e)
  {
    if (isTableCompleted())
    {
      putDocuments(getDocuments(), 1);
    }
  }

  private void sortDescButton_actionPerformed(ActionEvent e)
  {
    if (isTableCompleted())
    {
      putDocuments(getDocuments(), -1);
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
      Object docId = tableModel.getValueAt(i, 0);
      Object version = tableModel.getValueAt(i, 1);
      Object relType = tableModel.getValueAt(i, 2);
      Object relName = tableModel.getValueAt(i, 3);
      if ((docId == null) || 
          (version == null) || 
          (relType == null) || 
          (relName == null))
      {
        return false;
      }
    }
    return true;
  }
  
}
