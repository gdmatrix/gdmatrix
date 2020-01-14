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
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.activation.FileTypeMap;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import net.iharder.dnd.FileDrop;
import org.matrix.doc.DocumentConstants;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.OrderByProperty;
import org.matrix.dic.Property;
import org.matrix.doc.State;

import org.santfeliu.doc.client.UUIDFileProvider;
import org.santfeliu.doc.swing.action.EditDocumentPropertiesAction;
import org.santfeliu.doc.swing.action.ExecuteDocumentAction;
import org.santfeliu.doc.swing.action.LockDocumentAction;
import org.santfeliu.doc.swing.action.NewDocumentAction;
import org.santfeliu.doc.swing.action.SaveAsDocumentAction;
import org.santfeliu.doc.swing.action.UnlockDocumentAction;
import org.santfeliu.util.FileConsumer;
import org.santfeliu.util.MimeTypeMap;
import org.santfeliu.util.ResourceLoader;

/**
 *
 * @author unknown
 */
public class DocumentListPanel extends DocumentBasePanel
{
  public static final String PROPERTY_NAME = "propertyname";
  public static final String PROPERTY_VALUE = "propertyvalue";
  private Map classes;
  private String previousUUID = null;
  private boolean loading = false;
  private boolean keepRowSelected = false;
  
  private String propname;
  private String value;
  
  /* Actions */
  private LockDocumentAction lockDocumentAction = 
    new LockDocumentAction(this);
  private UnlockDocumentAction unlockDocumentAction = 
    new UnlockDocumentAction(this);
  private EditDocumentPropertiesAction editDocumentPropertiesAction = 
    new EditDocumentPropertiesAction(this);   
  private ExecuteDocumentAction openDocumentAction = 
    new ExecuteDocumentAction(this, 
    getLocalizedText(ExecuteDocumentAction.OPEN));
  private ExecuteDocumentAction openWithDocumentAction = 
    new ExecuteDocumentAction(this, getLocalizedText(
    ExecuteDocumentAction.OPEN_WITH));  
  private ExecuteDocumentAction editDocumentAction = 
    new ExecuteDocumentAction(this, getLocalizedText(
    ExecuteDocumentAction.EDIT));
  private ExecuteDocumentAction editWithDocumentAction = 
    new ExecuteDocumentAction(this, getLocalizedText(
    ExecuteDocumentAction.EDIT_WITH));
  private SaveAsDocumentAction saveAsDocumentAction =
    new SaveAsDocumentAction(this);

  /* Components */
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel filterPanel = new JPanel();
  private JPanel centerPanel = new JPanel();
  private BorderLayout borderLayout2 = new BorderLayout();
  private JScrollPane scrollPane = new JScrollPane();
  private JTable documentsTable = new JTable();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel titleLabel = new JLabel();
  private JTextField titleTextField = new JTextField();
  private JLabel contentsLabel = new JLabel();
  private JTextField contentsTextField = new JTextField();
  private JPanel northPanel = new JPanel();
  private BorderLayout borderLayout3 = new BorderLayout();
  private JPanel buttonPanel = new JPanel();
  private JButton searchButton = new JButton();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  private JLabel docIdLabel = new JLabel();
  private JComboBox languageComboBox = new JComboBox();
  private JTextField docIdTextField = new JTextField();
  private JLabel languageLabel = new JLabel();
  private FlowLayout docFlowLayout = new FlowLayout();
  private DefaultTableModel tableModel = new DefaultTableModel()
  {
    @Override
    public boolean isCellEditable(int row, int column)
    {
      return false;
    }
  };
  private JLabel versionLabel = new JLabel();
  private JTextField versionTextField = new JTextField();
  private JPanel tableHeaderPanel = new JPanel();
  private JLabel documentsLabel = new JLabel();
  private JLabel countLabel = new JLabel();
  private GridBagLayout gridBagLayout3 = new GridBagLayout();
  private JCheckBox previewCheckBox = new JCheckBox();
  private JSplitPane splitPane = new JSplitPane();
  private DocumentPreviewPanel previewPanel = new DocumentPreviewPanel(this);
  private JLabel searchPropertyLabel = new JLabel();
  private JTextField searchPropertyTextField = new JTextField();
  private JLabel searchPropertyValueLabel = new JLabel();
  private JTextField searchPropertyValueTextField = new JTextField();
  private JLabel stateLabel = new JLabel();
  private JComboBox stateComboBox = new JComboBox();
  private JLabel searchContentIdLabel = new JLabel();
  private JTextField searchContentIdTextField = new JTextField();
  private JLabel searchDocTypeIdLabel = new JLabel();
  private JComboBox searchDocTypeIdComboBox = new JComboBox();

  public DocumentListPanel()
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

  public void setPropname(String propname)
  {
    this.propname = propname;
  }

  public String getPropname()
  {
    return propname;
  }

  public void setValue(String value)
  {
    this.value = value;
  }

  public String getValue()
  {
    return value;
  }

  public void setClasses(Map classes)
  {
    this.classes = classes;
  }

  public void setFilter(Map filter)
  {
    String title = (String)filter.get(DocumentConstants.TITLE);
    titleTextField.setText(title);
    
    String propertyName = (String)filter.get(PROPERTY_NAME);
    searchPropertyTextField.setText(propertyName);

    String propertyValue = (String)filter.get(PROPERTY_VALUE);
    searchPropertyValueTextField.setText(propertyValue);
  }

  public DocumentFilter getFilter()
  {
    DocumentFilter filter = new DocumentFilter();
    String docId = docIdTextField.getText();
    if (docId != null && docId.trim().length() > 0)
      filter.getDocId().add(docId);
    
    String title = titleTextField.getText();
    if (title != null && title.trim().length() > 0)
      filter.setTitle("%" + title + "%");
    
    String contents = contentsTextField.getText();
    if (contents != null && contents.trim().length() > 0) 
      filter.setContentSearchExpression(contents);

    String language = (String)languageComboBox.getSelectedItem();
    if (language != null) 
      filter.setLanguage(language);

    String version = versionTextField.getText();
    if (version != null && version.trim().length() > 0)
      filter.setVersion(new Integer(version));
      
    String state = (String)stateComboBox.getSelectedItem();
    if (!state.equalsIgnoreCase(getLocalizedText("all")))
    {
      filter.getStates().add(State.fromValue(state));      
    }

    String contentId = searchContentIdTextField.getText();
    if (contentId != null && contentId.trim().length() > 0)
      filter.setContentId(contentId);

    String docTypeId = (String)searchDocTypeIdComboBox.getSelectedItem();
    if (docTypeId != null && docTypeId.trim().length() > 0)
      filter.setDocTypeId(docTypeId);

    String propertyName = searchPropertyTextField.getText();
    if (propertyName != null && propertyName.trim().length() > 0)
    {
      Property p = new Property();
      p.setName(propertyName);
      String propertyValue = searchPropertyValueTextField.getText();
      if (propertyValue != null && propertyValue.trim().length() > 0)
        p.getValue().add(propertyValue);
      else
        p.getValue().add("%");
      filter.getProperty().add(p);      
    }
    
    OrderByProperty order = new OrderByProperty();
    order.setName("docid");
    order.setDescending(false);
    filter.getOrderByProperty().add(order);
    order = new OrderByProperty();
    order.setName("version");
    order.setDescending(false);
    filter.getOrderByProperty().add(order);
    
    return filter;    
  }

  private void jbInit() throws Exception
  {
    tableHeaderPanel.setLayout(gridBagLayout3);
  
    docFlowLayout.setHgap(0);
    docFlowLayout.setVgap(0);
    docFlowLayout.setAlignment(FlowLayout.LEFT);
    
    this.setLayout(borderLayout1);
    this.setSize(new Dimension(705, 475));
    this.add(northPanel, BorderLayout.NORTH);
    this.add(centerPanel, BorderLayout.CENTER);
    
    northPanel.setLayout(borderLayout3);
    northPanel.setBorder(BorderFactory.createTitledBorder(
      getLocalizedText("filter")));
    northPanel.add(filterPanel, BorderLayout.CENTER);
    northPanel.add(buttonPanel, BorderLayout.EAST);

    centerPanel.setLayout(borderLayout2);
    centerPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

    tableHeaderPanel.add(documentsLabel, 
                         new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 
                                                GridBagConstraints.WEST, 
                                                GridBagConstraints.HORIZONTAL, 
                                                new Insets(0, 0, 0, 0), 0, 
                                                0));
    tableHeaderPanel.add(countLabel, 
                         new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, 
                                                GridBagConstraints.WEST, 
                                                GridBagConstraints.HORIZONTAL, 
                                                new Insets(0, 0, 0, 0), 0, 
                                                0));
    tableHeaderPanel.add(previewCheckBox, 
                         new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, 
                                                GridBagConstraints.CENTER, 
                                                GridBagConstraints.HORIZONTAL, 
                                                new Insets(0, 0, 0, 0), 0, 
                                                0));
    centerPanel.add(tableHeaderPanel, BorderLayout.NORTH);
    centerPanel.add(splitPane, BorderLayout.CENTER);
    filterPanel.setLayout(gridBagLayout1);
    splitPane.add(scrollPane, JSplitPane.LEFT);

    splitPane.add(previewPanel, JSplitPane.RIGHT);
    scrollPane.getViewport().add(documentsTable, null);
    scrollPane.getViewport().setBackground(documentsTable.getBackground());

    titleLabel.setText(getLocalizedText("title") + ":");
    titleTextField.setPreferredSize(new Dimension(50, 21));
    titleTextField.setMinimumSize(new Dimension(50, 21));
    contentsLabel.setText(getLocalizedText("contents") + ":");

    contentsTextField.setPreferredSize(new Dimension(50, 21));
    contentsTextField.setMinimumSize(new Dimension(50, 21));
    buttonPanel.setLayout(gridBagLayout2);
    docIdLabel.setText(getLocalizedText("docId") + ":");
    languageComboBox.setPreferredSize(new Dimension(100, 21));
    languageComboBox.setMinimumSize(new Dimension(10, 21));
    languageComboBox.setRenderer(new LanguageRenderer());
    languageComboBox.addItem(null);
    languageComboBox.addItem(DocumentConstants.UNIVERSAL_LANGUAGE);
    languageComboBox.addItem("ca");
    languageComboBox.addItem("es");
    languageComboBox.addItem("fr");
    languageComboBox.addItem("en");
    languageComboBox.addItem("it");
    languageComboBox.addItem("de");

    searchDocTypeIdComboBox.addItem("WORKFLOW");
    searchDocTypeIdComboBox.addItem("FORM");
    searchDocTypeIdComboBox.addItem("TEMPLATE");
    searchDocTypeIdComboBox.addItem("REPORT");
    searchDocTypeIdComboBox.addItem("MAP");
    searchDocTypeIdComboBox.addItem("IMAGE");
    searchDocTypeIdComboBox.setEditable(true);
    searchDocTypeIdComboBox.setSelectedItem("");

    docIdTextField.setPreferredSize(new Dimension(50, 21));
    docIdTextField.setMinimumSize(new Dimension(10, 21));
    languageLabel.setText(getLocalizedText("language") + ":");
    languageLabel.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));

    versionLabel.setText(getLocalizedText("version") + ":");
    versionLabel.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
    versionTextField.setPreferredSize(new Dimension(50, 21));
    versionTextField.setMinimumSize(new Dimension(10, 21));

    stateLabel.setText(getLocalizedText("state") + ":");
    stateLabel.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));

    stateComboBox.addItem(getLocalizedText("all"));
    stateComboBox.addItem(State.DRAFT.value());
    stateComboBox.addItem(State.COMPLETE.value());
    stateComboBox.addItem(State.RECORD.value());
    stateComboBox.addItem(State.DELETED.value());

    stateComboBox.setSelectedIndex(0);

    searchContentIdLabel.setText(getLocalizedText("contentId") + ":");
    searchContentIdLabel.setMinimumSize(new Dimension(3, 15));
    
    searchDocTypeIdLabel.setText(getLocalizedText("documentType") + ":");
    searchDocTypeIdLabel.setMinimumSize(new Dimension(3, 15));
    
    filterPanel.add(docIdTextField, 
                    new GridBagConstraints(1, 2, 1, 1, 0.2, 0.0, 
                                           GridBagConstraints.WEST, 
                                           GridBagConstraints.HORIZONTAL, 
                                           new Insets(2, 0, 2, 0), 0, 0));
    filterPanel.add(languageLabel, 
                    new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, 
                                           GridBagConstraints.EAST, 
                                           GridBagConstraints.NONE, 
                                           new Insets(2, 0, 2, 4), 0, 0));
    filterPanel.add(languageComboBox, 
                    new GridBagConstraints(3, 2, 1, 1, 0.2, 0.0, 
                                           GridBagConstraints.WEST, 
                                           GridBagConstraints.HORIZONTAL, 
                                           new Insets(2, 0, 2, 0), 0, 0));
    filterPanel.add(versionLabel, 
                    new GridBagConstraints(4, 2, 1, 1, 0.0, 0.0, 
                                           GridBagConstraints.EAST, 
                                           GridBagConstraints.NONE, 
                                           new Insets(2, 0, 2, 4), 0, 0));
    filterPanel.add(versionTextField, 
                    new GridBagConstraints(5, 2, 1, 1, 0.2, 0.0, 
                                           GridBagConstraints.WEST, 
                                           GridBagConstraints.HORIZONTAL, 
                                           new Insets(2, 0, 2, 4), 0, 0));

    filterPanel.add(stateLabel, 
                    new GridBagConstraints(6, 2, 1, 1, 0.0, 0.0, 
                                           GridBagConstraints.EAST, 
                                           GridBagConstraints.NONE, 
                                           new Insets(2, 0, 2, 4), 0, 0));
    filterPanel.add(stateComboBox, 
                    new GridBagConstraints(7, 2, 1, 1, 0.4, 0.0, 
                                           GridBagConstraints.EAST, 
                                           GridBagConstraints.HORIZONTAL, 
                                           new Insets(2, 0, 2, 0), 0, 0));
    buttonPanel.add(searchButton, 
                    new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 
                                           GridBagConstraints.CENTER, 
                                           GridBagConstraints.HORIZONTAL, 
                                           new Insets(0, 16, 0, 8), 2, 2));
    filterPanel.add(titleLabel, 
                    new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 
                                           GridBagConstraints.CENTER, 
                                           GridBagConstraints.HORIZONTAL, 
                                           new Insets(2, 0, 2, 2), 0, 0));
    filterPanel.add(titleTextField, 
                    new GridBagConstraints(1, 0, 7, 1, 1.0, 0.0, 
                                           GridBagConstraints.CENTER, 
                                           GridBagConstraints.HORIZONTAL, 
                                           new Insets(2, 0, 2, 0), 0, 0));
    filterPanel.add(contentsLabel, 
                    new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, 
                                           GridBagConstraints.CENTER, 
                                           GridBagConstraints.HORIZONTAL, 
                                           new Insets(2, 0, 2, 2), 0, 0));
    filterPanel.add(contentsTextField, 
                    new GridBagConstraints(1, 1, 7, 1, 1.0, 0.0, 
                                           GridBagConstraints.CENTER, 
                                           GridBagConstraints.HORIZONTAL, 
                                           new Insets(2, 0, 2, 0), 0, 0));

    filterPanel.add(docIdLabel, 
                    new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, 
                                           GridBagConstraints.CENTER, 
                                           GridBagConstraints.HORIZONTAL, 
                                           new Insets(2, 0, 2, 2), 0, 0));
    filterPanel.add(searchPropertyLabel, 
                    new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, 
                                           GridBagConstraints.CENTER, 
                                           GridBagConstraints.HORIZONTAL, 
                                           new Insets(2, 0, 2, 2), 0, 0));
    filterPanel.add(searchPropertyTextField, 
                    new GridBagConstraints(1, 3, 3, 1, 0.0, 0.0,
                                           GridBagConstraints.CENTER, 
                                           GridBagConstraints.HORIZONTAL, 
                                           new Insets(2, 0, 2, 0), 0, 0));
    filterPanel.add(searchPropertyValueLabel, 
                    new GridBagConstraints(4, 3, 1, 1, 0.0, 0.0, 
                                           GridBagConstraints.EAST, 
                                           GridBagConstraints.NONE, 
                                           new Insets(2, 0, 2, 4), 0, 0));
    filterPanel.add(searchPropertyValueTextField, 
                    new GridBagConstraints(5, 3, 3, 1, 0.0, 0.0, 
                                           GridBagConstraints.CENTER, 
                                           GridBagConstraints.HORIZONTAL, 
                                           new Insets(0, 0, 0, 0), 0, 0));

    filterPanel.add(searchContentIdLabel, 
                    new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, 
                                           GridBagConstraints.CENTER, 
                                           GridBagConstraints.HORIZONTAL, 
                                           new Insets(2, 0, 2, 2), 0, 0));
    filterPanel.add(searchContentIdTextField, 
                    new GridBagConstraints(1, 4, 7, 1, 1.0, 0.0, 
                                           GridBagConstraints.CENTER, 
                                           GridBagConstraints.HORIZONTAL, 
                                           new Insets(2, 0, 2, 0), 0, 0));

    filterPanel.add(searchDocTypeIdLabel, 
                    new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, 
                                           GridBagConstraints.CENTER, 
                                           GridBagConstraints.HORIZONTAL, 
                                           new Insets(2, 0, 2, 2), 0, 0));
    filterPanel.add(searchDocTypeIdComboBox, 
                    new GridBagConstraints(1, 5, 7, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 
                                           new Insets(2, 0, 2, 0), 0, 0));

    documentsTable.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseClicked(MouseEvent e)
      {
        if (e.getClickCount() == 2)
        {
           Point point = e.getPoint();
           int row = documentsTable.rowAtPoint(point);
           setStateVariables(row);
           setLockVariables(row);
          
          openDocumentAction.actionPerformed(
            new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Open"));
        }
      }
      
      @Override
      public void mousePressed(MouseEvent event)
      {
        if (event.getButton() != MouseEvent.BUTTON1)
        {
          Point point = event.getPoint();
          int row = documentsTable.rowAtPoint(point);
          setStateVariables(row);
          setLockVariables(row);
          
          ListSelectionModel listSelectionModel = documentsTable.getSelectionModel();
          listSelectionModel.setSelectionInterval(row, row);
          
          showContextMenu(documentsTable, point);
        }
      }  
      
      private void setStateVariables(int row)
      {
        documentExists = true;
        documentLocked = false;
        documentLockedByUser = false;
      }
      
      private void setLockVariables(int row)
      {
        String lockuser = (String)documentsTable.getValueAt(row, 6);
        if (lockuser != null)
        {
          documentLocked = true;
          if (username.equals(lockuser))
            documentLockedByUser = true;
        }
      }
    });
    
    scrollPane.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mousePressed(MouseEvent event)
      {
        if (event.getButton() != MouseEvent.BUTTON1)
        {
          Point point = event.getPoint();
          showContextMenu(scrollPane, point);
        }
      }
    });
    
    new FileDrop(scrollPane, new FileDrop.Listener()
    {
      public void filesDropped(File[] files)
      {
        for (int i = 0; i < files.length; i++)
        {
          File file = files[i];
          HashMap values = new HashMap();
          values.put(DocumentConstants.TITLE, file.getName());
          values.put(DocumentConstants.CONTENTID, file.getAbsolutePath());
          dropFile(values);
        }
      }
    }); 
    
    documentsTable.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent e)
      {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
        {
          openDocumentAction.actionPerformed(
            new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "open"));
        }
      }
    });
    
    documentsTable.setAutoCreateColumnsFromModel(false);
    tableModel.addColumn("docId");
    tableModel.addColumn(getLocalizedText("language"));
    tableModel.addColumn(getLocalizedText("version"));
    tableModel.addColumn(getLocalizedText("title"));
    tableModel.addColumn(getLocalizedText("size"));
    tableModel.addColumn("contentId");
    tableModel.addColumn(getLocalizedText("locked"));
    tableModel.addColumn(getLocalizedText("state"));
    tableModel.addColumn(getLocalizedText("documentType"));
    documentsTable.setModel(tableModel);    
    documentsTable.getSelectionModel().addListSelectionListener(
      new ListSelectionListener()
      {
        public void valueChanged(ListSelectionEvent e)
        {
          refreshPreview();
        }
      });
    DocumentRenderer documentRenderer = new DocumentRenderer();

    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer()
    {
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column)
      {
        if (isSelected || hasFocus)
        {
          super.setForeground(table.getSelectionForeground());
          super.setBackground(table.getSelectionBackground());
        }
        else
        {
          super.setForeground(table.getForeground());
          super.setBackground(table.getBackground());
        }
        setFont(table.getFont());
        setValue(value); 
        return this;
      }
    };

    DefaultTableCellRenderer sizeRenderer = new DefaultTableCellRenderer()
    {
      private DecimalFormat format = new DecimalFormat("#,###,##0.00");

      @Override
      public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column)
      {
        if (isSelected)
        {
          super.setForeground(table.getSelectionForeground());
          super.setBackground(table.getSelectionBackground());
        }
        else
        {
          super.setForeground(table.getForeground());
          super.setBackground(table.getBackground());
        }
        setFont(table.getFont());
        double size = Double.parseDouble(value.toString()) / 1024.0;
        String sizeString = format.format(size);
        setValue(sizeString + " kB ");
        return this;
      }
    };
    renderer.setHorizontalAlignment(JLabel.LEFT);
    sizeRenderer.setHorizontalAlignment(JLabel.RIGHT);

    searchPropertyLabel.setText(getLocalizedText("property") + ":");
    searchPropertyValueLabel.setText(getLocalizedText("value") + ":");
    documentsLabel.setText(getLocalizedText("foundDocuments") + ":");

    DefaultCellEditor editor = null;
    documentsTable.addColumn(new TableColumn(0, 50, renderer, editor));
    documentsTable.addColumn(new TableColumn(1, 50, renderer, editor));
    documentsTable.addColumn(new TableColumn(2, 50, renderer, editor));
    documentsTable.addColumn(new TableColumn(3, 200, documentRenderer, editor));
    documentsTable.addColumn(new TableColumn(4, 40, sizeRenderer, editor));
    documentsTable.addColumn(new TableColumn(5, 0, renderer, editor));
    documentsTable.addColumn(new TableColumn(6, 40, renderer, editor));
    documentsTable.addColumn(new TableColumn(7, 40, renderer, editor));
    documentsTable.addColumn(new TableColumn(8, 40, renderer, editor));

    documentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    documentsTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
    documentsTable.setRowHeight(24);
    documentsTable.getTableHeader().setReorderingAllowed(false);
    documentsTable.setRowSelectionAllowed(true);
    documentsTable.setDragEnabled(true);

    previewCheckBox.setText(getLocalizedText("preview"));
    previewCheckBox.setSelected(true);
    previewCheckBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          previewCheckBox_actionPerformed(e);
        }
      });
    splitPane.setOneTouchExpandable(true);
    splitPane.setDividerSize(8);
    splitPane.setDividerLocation(500);

    searchButton.setText(getLocalizedText("search"));
    searchButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          searchButton_actionPerformed(e);
        }
      });
  }

  private void searchButton_actionPerformed(ActionEvent e)
  {
    search();
  }
  
  public void search()
  {
    try
    {
      this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      int selectedRow = 0;
      if (keepRowSelected)
        selectedRow = documentsTable.getSelectedRow();
      tableModel.setRowCount(0);

      DocumentFilter filter = getFilter();
      filter.setIncludeContentMetadata(true);
      filter.setFirstResult(0);
      filter.setMaxResults(100);
      
      List<Document> documentList = getClient().findDocuments(filter);
      for (Document doc : documentList)
      {
        String docId = (doc.getDocId() == null ? "" : doc.getDocId());
        String language = (doc.getLanguage() == null ? "" : doc.getLanguage());
        Integer version = doc.getVersion();
        String title = (doc.getTitle() == null ? "" : doc.getTitle());
        String contentType = "";
        Long size = new Long(0);
        String contentId = "";
        String docTypeId = (doc.getDocTypeId() == null ? "" : 
          doc.getDocTypeId());
        if (doc.getContent() != null)
        {
          contentType = (doc.getContent().getContentType() == null ? "" : 
            doc.getContent().getContentType());
          size = (doc.getContent().getSize() == null ? new Long(0) : 
            doc.getContent().getSize());
          contentId = (doc.getContent().getContentId() == null ? "" : 
            doc.getContent().getContentId());
        }
        String lockUser = doc.getLockUserId();
        String state = (doc.getState() == null ? "" : doc.getState().value());

        tableModel.addRow(new Object[]{docId, language, version, 
          new String[]{title, contentType}, size, contentId, 
          lockUser, state, docTypeId});                
      }
      if (documentList.size() > 0)
      {
        documentsTable.getSelectionModel().setSelectionInterval(
          selectedRow, selectedRow);
        documentsTable.scrollRectToVisible(new Rectangle(0, 0, 10, 10));
      }
      countLabel.setText(String.valueOf(documentList.size()));
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      showError(ex);
    }
    finally
    {
      this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));      
    }
  }
  
  private DocumentRow getDocumentRow(Document doc)
    throws Exception
  {
    DocumentRow row = new DocumentRow();
    row.mimeType = doc.getContent().getContentType();
    if (row.mimeType == null) row.mimeType = "application/octet-stream";
    row.docId = doc.getDocId();
    row.language = doc.getLanguage();
    row.version = String.valueOf(doc.getVersion());
    row.title = doc.getTitle();
    row.size = String.valueOf(doc.getContent().getSize());
    row.uuid = doc.getContent().getContentId();
    row.lockuser = doc.getLockUserId();
    row.state = doc.getState().value();
    row.docTypeId = doc.getDocTypeId();
    return row;
  }
  
  private String getFileMimeType(File file)
  {
    FileTypeMap typeMap = MimeTypeMap.getDefaultFileTypeMap();
    return typeMap.getContentType(file);
  }

  private void previewCheckBox_actionPerformed(ActionEvent e)
  {
    previousUUID = null;
    refreshPreview();
  }
  
  private void refreshPreview()
  {
    if (previewCheckBox.isSelected())
    {
      int row = documentsTable.getSelectedRow();
      if (row >= 0)
      {
        String UUID = (String)documentsTable.getValueAt(row, 5);
        if (UUID == null || UUID.length() == 0)
        {
          previousUUID = null;
          previewPanel.showMessage("");
          return;
        }

        Object titleMime = (Object)documentsTable.getValueAt(row, 3);
        if (titleMime instanceof String[])
        {
          String mimeType = ((String[])titleMime)[1];
          if (!mimeType.startsWith("image/"))
          {
            previousUUID = null;
            previewPanel.showMessage(getLocalizedText("previewNotSupported"));
            return;
          }
        }
        else
        {
          previousUUID = null;
          previewPanel.showMessage(getLocalizedText("unknowDocumentFormat"));
          return;
        }

        if (UUID.equals(previousUUID) || loading) return;

        loading = true;
        previewPanel.showMessage(
          getLocalizedText("loadingDocument") + "...");
        UUIDFileProvider uuidFileProvider = null;
        if (wsDirectoryURL != null)
          uuidFileProvider = new UUIDFileProvider(wsDirectoryURL);
        else
          uuidFileProvider = new UUIDFileProvider(wsdlLocation);
        ResourceLoader.requestResource(UUID, uuidFileProvider,
          new FileConsumer()
          {
          @Override
            public void fileLoadCompleted(String UUID, File file)
            {
              //EDT
              loading = false;
              previousUUID = UUID;

              PropertyChangeEvent event = new PropertyChangeEvent(this,
                JFileChooser.SELECTED_FILE_CHANGED_PROPERTY, null, file);
              previewPanel.propertyChange(event);
              refreshPreview();
            }
          }, true);
      }
    }
    else
    {
      PropertyChangeEvent event = new PropertyChangeEvent(this, 
        JFileChooser.SELECTED_FILE_CHANGED_PROPERTY, null, null);
      previewPanel.propertyChange(event);                    
    }
  }
  
  private void showContextMenu(Component component, Point point)
  {
    JMenu contextMenu = new JMenu();
    JMenu templatesMenu = getTemplatesMenu();
    contextMenu.add(templatesMenu);
    if (component instanceof JTable)
    {
     contextMenu.add(new JSeparator());
     contextMenu.add(new JMenuItem(openDocumentAction));
     contextMenu.add(new JMenuItem(openWithDocumentAction));
     contextMenu.add(new JMenuItem(editDocumentAction));
     contextMenu.add(new JMenuItem(editWithDocumentAction));
     contextMenu.add(new JMenuItem(saveAsDocumentAction));
     contextMenu.add(new JSeparator());
     contextMenu.add(new JMenuItem(lockDocumentAction));
     contextMenu.add(new JMenuItem(unlockDocumentAction));
     contextMenu.add(new JSeparator());
     contextMenu.add(new JMenuItem(editDocumentPropertiesAction));
    }

    JPopupMenu popupMenu = contextMenu.getPopupMenu();
    popupMenu.pack();
    popupMenu.show(component, point.x, point.y);
  }
 
  private JMenu getTemplatesMenu()
  {
    NewDocumentAction newDocumentAction = new NewDocumentAction(this);
    newDocumentAction.putValue(Action.NAME, getLocalizedText("noTemplate"));    
    JMenu templatesMenu = new JMenu(getLocalizedText("new"));
    templatesMenu.add(new JMenuItem(newDocumentAction));
    if (documentsTable.getSelectedRow() != -1)
    {
      NewDocumentAction newTranslationAction = new NewDocumentAction(this, null, 
        NewDocumentAction.NEW_TRANSLATION);
      newTranslationAction.putValue(Action.NAME, 
        getLocalizedText("newTranslation"));    
      NewDocumentAction newComponentAction = new NewDocumentAction(this, null, 
        NewDocumentAction.NEW_COMPONENT);
      newComponentAction.putValue(Action.NAME, 
        getLocalizedText("newComponent"));    
      NewDocumentAction newRelatedAction = new NewDocumentAction(this, null, 
        NewDocumentAction.NEW_RELATED);
      newRelatedAction.putValue(Action.NAME, getLocalizedText("newRelated"));    
      NewDocumentAction newRedactionAction = new NewDocumentAction(this, null, 
        NewDocumentAction.NEW_REDACTION);
      newRedactionAction.putValue(Action.NAME, 
        getLocalizedText("newRedaction"));    
      NewDocumentAction newCopyAction = new NewDocumentAction(this, null, 
        NewDocumentAction.NEW_COPY);
      newCopyAction.putValue(Action.NAME, getLocalizedText("newCopy"));    
      templatesMenu.add(new JSeparator());
      templatesMenu.add(new JMenuItem(newTranslationAction));
      templatesMenu.add(new JMenuItem(newComponentAction));
      templatesMenu.add(new JMenuItem(newRelatedAction));
      templatesMenu.add(new JMenuItem(newRedactionAction));
      templatesMenu.add(new JSeparator());
      templatesMenu.add(new JMenuItem(newCopyAction));
    }
    if (classes != null)
    {
      templatesMenu.add(new JSeparator());
      Set set = classes.keySet();
      Iterator iter = set.iterator();
      while (iter.hasNext())
      {
        String className = (String)iter.next();
        NewDocumentAction action = new NewDocumentAction(this, className, null);
        action.setClasses(classes);
        templatesMenu.add(action);
      }
    }
    return templatesMenu;
  }
 
  public String getDocId()
  {
    int row = documentsTable.getSelectedRow();
    return (String)tableModel.getValueAt(row, 0);
  }

  public String getDocLanguage()
  {
    int row = documentsTable.getSelectedRow();
    return (String)tableModel.getValueAt(row, 1);
  }
  
  public String getDocVersion()
  {
    int row = documentsTable.getSelectedRow();
    return String.valueOf(tableModel.getValueAt(row, 2));
  }
  

  public String getDocUUID()
  {
    int row = documentsTable.getSelectedRow();
    return (String)tableModel.getValueAt(row, 5);
  }


  public String getDocTypeId()
  {
    int row = documentsTable.getSelectedRow();
    return (String)tableModel.getValueAt(row, 8);
  }

  public void loadDocument(String docId, int version)
  {
    DocumentRow document;
    try
    {
      int rowi = documentsTable.getSelectedRow();
      document = selectDocument(docId, version);
      if (document != null)
      {
        document.setModel(documentsTable.getModel(), rowi);
        documentsTable.getSelectionModel().setSelectionInterval(rowi, rowi);        
      }
      else
      {
        search();
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

  }

  private DocumentRow selectDocument(String docId, int version)
    throws Exception
  {
    DocumentRow documentRow = null;

    Document document = getClient().loadDocument(docId, version, ContentInfo.ALL);
    if (document != null)
    {
      documentRow = getDocumentRow(document);
    }
    return documentRow;
  }
  
  public Map getClasses()
  {
    return classes;
  }
  
  public void dropFile(Map values)
  {
    NewDocumentAction newAction = new NewDocumentAction(this);
    newAction.setValues(values);
    newAction.actionPerformed(new ActionEvent(this, 
      ActionEvent.ACTION_PERFORMED, "New"));
  }
  
  static
  {
    try
    {
      FileTypeMap.setDefaultFileTypeMap(new MimeTypeMap()); 
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  public void setSearchPropertyTextField(JTextField searchPropertyTextField)
  {
    this.searchPropertyTextField = searchPropertyTextField;
  }

  public JTextField getSearchPropertyTextField()
  {
    return searchPropertyTextField;
  }

  public void setSearchPropertyValueTextField(
    JTextField searchPropertyValueTextField)
  {
    this.searchPropertyValueTextField = searchPropertyValueTextField;
  }

  public JTextField getSearchPropertyValueTextField()
  {
    return searchPropertyValueTextField;
  }

  private class DocumentRow
  {
    public String docId;
    public String language;
    public String version;
    public String title;
    public String size = "0";
    public String uuid;
    public String lockuser = "";
    public String state;
    public String mimeType;
    public String docTypeId;
   
    public DocumentRow()
    {      
    }
    
    public DocumentRow(Vector row) 
    {
      if (row != null)
      {
        docId = (String)row.get(0);
        language = (String)row.get(1);
        version = (String)row.get(2);
        title = (String)row.get(3);
        size = (String)row.get(4);
        uuid = (String)row.get(5);
        lockuser = (String)row.get(6);
        state = (String)row.get(7);
        docTypeId = (String)row.get(8);
      }
    }
    
    public void setModel(TableModel tableModel, int rowi)
    {
      if (tableModel != null)
      {
        tableModel.setValueAt(docId, rowi, 0);
        tableModel.setValueAt(language, rowi, 1);
        tableModel.setValueAt(version, rowi, 2);
        tableModel.setValueAt(new String[]{title, mimeType}, rowi, 3);
        tableModel.setValueAt(size, rowi, 4);
        tableModel.setValueAt(uuid, rowi, 5);
        tableModel.setValueAt(lockuser, rowi, 6);
        tableModel.setValueAt(state, rowi, 7);
        tableModel.setValueAt(docTypeId, rowi, 8);
      }
    }
  }
}
