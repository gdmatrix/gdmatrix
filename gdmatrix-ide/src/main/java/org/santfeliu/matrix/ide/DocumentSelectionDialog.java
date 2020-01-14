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
package org.santfeliu.matrix.ide;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.OrderByProperty;
import org.matrix.dic.Property;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.swing.SwingWorker;
import org.santfeliu.swing.layout.WrapLayout;

/**
 *
 * @author unknown
 */
public class DocumentSelectionDialog extends JDialog
{
  private String selectedDocId;
  private String selectedLanguage;
  private Integer selectedVersion;
  private DocumentType selectedDocumentType;
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel southPanel = new JPanel();
  private JPanel centerPanel = new JPanel();
  private JButton openButton = new JButton();
  private JButton cancelButton = new JButton();
  private JPanel filterPanel = new JPanel();
  private JScrollPane scrollPane = new JScrollPane();
  private JTable documentsTable = new JTable();
  private JLabel docTypeLabel = new JLabel();
  private JComboBox docTypesComboBox = new JComboBox();
  private JLabel nameLabel = new JLabel();  
  private JButton searchButton = new JButton();
  private JCheckBox versionsCheckBox = new JCheckBox();
  private MainPanel mainPanel;
  private DefaultTableModel tableModel = new DefaultTableModel()
  {
    @Override
    public boolean isCellEditable(int row, int column)
    {
      return false;
    }
  };
  private JTextField nameTextField = new JTextField()
  {
    @Override
    public Dimension getPreferredSize()
    {
      Dimension dim = super.getPreferredSize();
      return new Dimension(100, dim.height);
    }
  };

  public DocumentSelectionDialog(Frame owner, MainPanel mainPanel)
  {
    super(owner, true);
    this.mainPanel = mainPanel;
    try
    {
      initComponents();
    }
    catch (Exception ex)
    {
      MatrixIDE.log(ex);
    }
  }

  private void initComponents() throws Exception
  {
    this.setTitle("Open from Document Manager...");
    this.setLayout(borderLayout1);    
    this.add(centerPanel, BorderLayout.CENTER);
    this.add(southPanel, BorderLayout.SOUTH);

    centerPanel.setLayout(new BorderLayout());
    centerPanel.add(filterPanel, BorderLayout.NORTH);
    centerPanel.add(scrollPane, BorderLayout.CENTER);
    centerPanel.setBorder(
      BorderFactory.createEmptyBorder(4, 6, 4, 6));

    centerPanel.setPreferredSize(new Dimension(640, 400));

    openButton.setText("Open");
    openButton.setEnabled(false);
    openButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            acceptButton_actionPerformed(e);
          }
        });
    cancelButton.setText("Cancel");
    cancelButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            cancelButton_actionPerformed(e);
          }
        });
    docTypeLabel.setText("Document type:");
    nameLabel.setText("Name:");
    searchButton.setText("Search");
    searchButton.setMargin(new Insets(2, 2, 2, 2));
    searchButton.setIcon(new ImageIcon(getClass().getResource(
      "/org/santfeliu/matrix/ide/resources/icon/find.gif")));
    searchButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            searchButton_actionPerformed(e);
          }
        });
    versionsCheckBox.setText("All versions");
    southPanel.add(openButton, null);
    southPanel.add(cancelButton, null);

    filterPanel.setLayout(new WrapLayout());
    filterPanel.add(docTypeLabel, null);
    filterPanel.add(docTypesComboBox, null);
    filterPanel.add(nameLabel, null);
    filterPanel.add(nameTextField, null);
    filterPanel.add(versionsCheckBox, null);
    filterPanel.add(searchButton, null);
    scrollPane.getViewport().add(documentsTable, null);
    
    docTypesComboBox.setRenderer(new DocumentTypeCellRenderer());
    setDocumentTypes();

    documentsTable.setAutoCreateColumnsFromModel(false);
    tableModel.addColumn("Name");
    tableModel.addColumn("Description");
    tableModel.addColumn("DocId");
    tableModel.addColumn("Ver.");
    tableModel.addColumn("Lang.");
    documentsTable.setModel(tableModel);
    documentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
    renderer.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);

    documentsTable.addColumn(new TableColumn(0, 200, null, null));
    documentsTable.addColumn(new TableColumn(1, 400, null, null));
    documentsTable.addColumn(new TableColumn(2, 60, renderer, null));
    documentsTable.addColumn(new TableColumn(3, 40, renderer, null));
    documentsTable.addColumn(new TableColumn(4, 40, renderer, null));
    documentsTable.getTableHeader().setReorderingAllowed(false);
    documentsTable.getSelectionModel().addListSelectionListener(
      new ListSelectionListener()
    {
      public void valueChanged(ListSelectionEvent e)
      {
        openButton.setEnabled(documentsTable.getSelectedRow() != -1);
      }
    });
  }

  private void setDocumentTypes()
  {
    ArrayList documentTypes = mainPanel.getDocumentTypes();
    DefaultComboBoxModel model = new DefaultComboBoxModel();
    for (int i = 0; i < documentTypes.size(); i++)
    {
      model.addElement(documentTypes.get(i));
    }
    docTypesComboBox.setModel(model);
  }

  public void showDialog(Component parent)
  {
    pack();
    setLocationRelativeTo(parent);
    setVisible(true);
  }

  private void searchButton_actionPerformed(ActionEvent e)
  {
    openButton.setEnabled(false);
    searchButton.setEnabled(false);
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    tableModel.setRowCount(0);

    SwingWorker worker = new SwingWorker()
    {
      List<Document> documentList;
      Throwable error;

      @Override
      protected void doWork()
      {
        try
        {
          DocumentType documentType =
           (DocumentType)docTypesComboBox.getSelectedItem();
          DocumentManagerClient client = mainPanel.getDocumentManagerClient();
          DocumentFilter filter = new DocumentFilter();
          filter.setDocTypeId(documentType.getDocTypeId());
          String name = nameTextField.getText();          
          if (name == null) name = "";
          Property property = new Property();
          property.setName(documentType.getPropertyName());
          property.getValue().add("%" + name.trim() + "%");
          filter.getProperty().add(property);
          if (documentType.getFixedProperties() != null)
            filter.getProperty().addAll(DictionaryUtils
              .getPropertiesFromMap(documentType.getFixedProperties()));
          if (!versionsCheckBox.isSelected())
          {
            filter.setVersion(0);
          }
          else filter.setVersion(-1);
          
          OrderByProperty order1 = new OrderByProperty();
          order1.setName("title");
          filter.getOrderByProperty().add(order1);
          OrderByProperty order2 = new OrderByProperty();
          order2.setName("version");
          filter.getOrderByProperty().add(order2);

          documentList = client.findDocuments(filter);
        }
        catch (Throwable ex)
        {
          error = ex;
        }
      }

      // Event dispatch thread
      @Override
      protected void doFinished()
      {
        searchButton.setEnabled(true);
        setCursor(Cursor.getDefaultCursor());        
        if (error != null)
        {
          JOptionPane.showMessageDialog(DocumentSelectionDialog.this,
           error.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
        else if (documentList.isEmpty())
        {
          JOptionPane.showMessageDialog(DocumentSelectionDialog.this,
           "No documents found.", "INFO", JOptionPane.INFORMATION_MESSAGE);
        }
        else
        {
          for (Document document : documentList)
          {
            String title = document.getTitle();
            String wfName = null;
            String wfDesc = null;
            int index = title.indexOf(":");
            if (index != -1)
            {
              wfName = title.substring(0, index);
              wfDesc = title.substring(index + 1).trim();
            }
            else
            {
              wfName = " ";
              wfDesc = title.trim();
            }
            String docId = document.getDocId();
            String language = document.getLanguage();
            Integer version = document.getVersion();
            tableModel.addRow(
              new Object[]{wfName, wfDesc, docId, version, language});
          }
        }
      }
    };
    worker.construct();
  }


  private void acceptButton_actionPerformed(ActionEvent e)
  {
    try
    {
      int index = documentsTable.getSelectedRow();
      if (index != -1)
      {
        selectedDocId = (String)tableModel.getValueAt(index, 2);
        selectedVersion = (Integer)tableModel.getValueAt(index, 3);
        selectedLanguage = (String)tableModel.getValueAt(index, 4);
        selectedDocumentType = (DocumentType)docTypesComboBox.getSelectedItem();
      
        setVisible(false);
        dispose();
      }
    }
    catch (Exception ex)
    {
      MatrixIDE.log(ex);
    }
  }

  private void cancelButton_actionPerformed(ActionEvent e)
  {
    setVisible(false);
    dispose();
  }
  
  public String getSelectedDocId()
  {
    return selectedDocId;
  }

  public String getSelectedLanguage()
  {
    return selectedLanguage;
  }

  public Integer getSelectedVersion()
  {
    return selectedVersion;
  }

  public DocumentType getSelectedDocumentType()
  {
    return selectedDocumentType;
  }
}
