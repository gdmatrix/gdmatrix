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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import net.iharder.dnd.FileDrop;

import org.matrix.dic.DictionaryConstants;
import org.matrix.security.AccessControl;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentConstants;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.OrderByProperty;
import org.matrix.dic.Property;
import org.matrix.doc.RelatedDocument;
import org.matrix.doc.State;
import org.santfeliu.doc.swing.action.DeleteDocumentAction;
import org.santfeliu.doc.swing.action.LockDocumentAction;
import org.santfeliu.doc.swing.action.SaveAsDocumentAction;
import org.santfeliu.doc.swing.action.SaveDocumentAction;
import org.santfeliu.doc.swing.action.UnlockDocumentAction;
import org.santfeliu.swing.PropertiesPanel;

/**
 *
 * @author unknown
 */
public class DocumentSimplePanel extends DocumentBasePanel
{
  public static final String NEW_DOCUMENT = "NEW_DOCUMENT";
  public static final String ORIGINAL_LANGUAGE = "ORIGINAL_LANGUAGE";
  public static final String TITLE = "title";
  public static final String CREATION_DATE = "creationDate";
  public static final int LAST_VERSION = 0;
  public static final String DOCID = "docid";
  public static final String LANGUAGE = "language";
  public static final String VERSION = "version";
  public static final String ORIGLANGUAGE = "origlanguage";
  public static final String LOCKUSER = "lockuser";
  //public static final String UUID = "uuid";
  public static final String FILETYPE = "filetype";
  public static final String MIMETYPE = "mimetype";
  public static final String SIZE = "size";
  public static final String INITDATE = "initdate";
  public static final String ENDDATE = "enddate";
  public static final String INITHOUR = "inithour";
  public static final String ENDHOUR = "endhour";
  public static final String FORMATDESC = "formatdesc";
  public static final String FORMATID = "formatid";
  public static final String CAPTUREDATE = "capturedate";
  public static final String CHANGEDATE = "changedate";
  public static final String STATE = "state";
  public static final String DOCUMENT_TYPE = "documenttype";

  /* State variables */
  private boolean fireActionEvents = true;
  private static File exploreDir;
  //private static File saveDir;

  /* Actions */
  private LockDocumentAction lockDocumentAction = 
    new LockDocumentAction(this);
  private UnlockDocumentAction unlockDocumentAction = 
    new UnlockDocumentAction(this);
  //private PurgeDocumentAction purgeDocumentAction = 
  //  new PurgeDocumentAction(this);
  //private CommitDocumentAction commitDocumentAction = 
  //  new CommitDocumentAction(this);
  //private AbortDocumentAction abortDocumentAction = 
  //  new AbortDocumentAction(this);
  private SaveDocumentAction saveDocumentAction = 
    new SaveDocumentAction(this);
  private DeleteDocumentAction deleteDocumentAction = 
    new DeleteDocumentAction(this);
  private SaveAsDocumentAction saveAsDocumentAction = 
    new SaveAsDocumentAction(this);

  /* Components */
  private BorderLayout borderLayout1 = new BorderLayout();
  //private BorderLayout borderLayout3 = new BorderLayout();

  private GridBagLayout centerPanelGridBagLayout = new GridBagLayout();

  private JPanel generalPanel = new JPanel();
  private JPanel contentPanel = new JPanel();
  private JPanel southPanel = new JPanel();
  private PropertiesPanel propertiesPanel = new PropertiesPanel();
  private RelatedDocumentsPanel relatedDocumentsPanel = 
    new RelatedDocumentsPanel();
  private DocumentSecurityPanel documentSecurityPanel =
    new DocumentSecurityPanel();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  private JLabel titleLabel = new JLabel();
  private JTextField titleTextField = new JTextField();
  private JLabel creationDateLabel = new JLabel();
  private JTextField creationDateTextField = new JTextField();
  private JLabel fileLabel = new JLabel();
  //private JButton commitButton = new JButton();
  //private JButton abortButton = new JButton();
  private JButton lockButton = new JButton();
  private JButton unlockButton = new JButton();
  private JButton saveButton = new JButton();
  private JButton deleteButton = new JButton();
  //private JButton purgeButton = new JButton();
  private JButton saveFileButton = new JButton();
  private JButton exploreFileButton = new JButton();
  private JLabel captureDateLabel = new JLabel();
  private JLabel captureDateValueLabel = new JLabel();
  private JLabel changeDateLabel = new JLabel();
  private JLabel changeDateValueLabel = new JLabel();
  private JPanel centerPanel = new JPanel();
  private JPanel northPanel = new JPanel();
  private JLabel docIdLabel = new JLabel();
  private JLabel docIdValueLabel = new JLabel();
  private JLabel languageLabel = new JLabel();
  private JComboBox languageComboBox = new JComboBox();
  private JComboBox versionComboBox = new JComboBox();
  private JComboBox docTypeIdComboBox = new JComboBox();
  private JLabel versionLabel = new JLabel();
  private JLabel lockUserLabel = new JLabel();
  private JTextField fileTextField = new JTextField();
  private JLabel mimeTypeLabel = new JLabel();
  private JLabel mimeTypeValueLabel = new JLabel();
  private JLabel formatLabel = new JLabel();
  private JLabel formatValueLabel = new JLabel();
  private JLabel sizeLabel = new JLabel();
  private JLabel sizeValueLabel = new JLabel();
  private JLabel stateLabel = new JLabel();
  private JComboBox stateComboBox = new JComboBox();
  private JCheckBox newVersionCheckBox = new JCheckBox();
  private JTabbedPane tabbedPane = new JTabbedPane();
  private JLabel documentTypeLabel = new JLabel();
  //private JTextField documentTypeTextField = new JTextField();
  private JLabel lockUserValueLabel = new JLabel();

  private JPanel lockButtonsPanel = new JPanel();
  private GridBagLayout lockButtonsPanelLayout = new GridBagLayout();

  private boolean internalDocument = false;

  public DocumentSimplePanel()
  {
    try
    {
      jbInit();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  private void jbInit()
    throws Exception
  {
    fireActionEvents = false;
    
    this.setLayout(borderLayout1);
    this.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

    this.add(northPanel, BorderLayout.NORTH);
    this.add(centerPanel, BorderLayout.CENTER);
    this.add(southPanel, BorderLayout.SOUTH);

    northPanel.add(docIdLabel, null);
    northPanel.add(docIdValueLabel, null);

    northPanel.add(versionLabel, null);
    northPanel.add(versionComboBox, null);

    centerPanel.setLayout(centerPanelGridBagLayout);
    centerPanel.add(generalPanel, 
                    new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 
                                           new Insets(0, 0, 0, 0), 0, 0));
                                  
    //centerPanel.add(propertiesPanel, BorderLayout.CENTER);
    
    centerPanel.add(contentPanel, 
                    new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, 
                                           new Insets(0, 0, 0, 0), 0, 0));
                                  
    centerPanel.add(tabbedPane, 
                    new GridBagConstraints(0, 2, 1, 1, 1.0, 2.0,GridBagConstraints.WEST, GridBagConstraints.BOTH, 
                                           new Insets(0, 0, 0, 0), 0, 0));
                                
    tabbedPane.addTab(getLocalizedText("properties"), propertiesPanel);
    tabbedPane.addTab(getLocalizedText("relatedDocuments"), relatedDocumentsPanel);
    tabbedPane.addTab(getLocalizedText("security"), documentSecurityPanel);

    generalPanel.setLayout(gridBagLayout1);
    generalPanel.setBorder(BorderFactory.createTitledBorder(getLocalizedText("mainProperties")));

    contentPanel.setLayout(gridBagLayout2);
    contentPanel.setBorder(BorderFactory.createTitledBorder(getLocalizedText("content")));

    docIdLabel.setText(getLocalizedText("docId") + ":");
    docIdValueLabel.setText(NEW_DOCUMENT);
    docIdValueLabel.setFont(new Font("Dialog", 1, 13));
    languageLabel.setText(getLocalizedText("language") + ":");
    //languageLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    //languageLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
    //languageComboBox.setPreferredSize(new Dimension(60, 24));
    /*
    languageComboBox.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            languageComboBox_actionPerformed(e);
          }
        });
*/
    languageComboBox.setMinimumSize(new Dimension(100, 21));
    languageComboBox.setPreferredSize(new Dimension(100, 21));
    languageComboBox.setRenderer(new LanguageRenderer());

    formatLabel.setText(getLocalizedText("format") + ":");
    formatValueLabel.setText("");

    versionLabel.setText(getLocalizedText("version") + ":");
    versionLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    versionLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
    //versionComboBox.setPreferredSize(new Dimension(60, 24));

    versionComboBox.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            versionComboBox_actionPerformed(e);
          }
        });

    lockUserLabel.setText(getLocalizedText("lockUser") + ":");

    mimeTypeLabel.setText(getLocalizedText("mimeType") + ":");
    sizeLabel.setText(getLocalizedText("size") + ":");
    saveButton.setText(getLocalizedText("save"));
    saveButton.setAction(saveDocumentAction);

    deleteButton.setText(getLocalizedText("delete"));
    deleteButton.setAction(deleteDocumentAction);

    //unlockButton.setText(getLocalizedText("unlock"));
    unlockButton.setAction(unlockDocumentAction);
    unlockButton.setText(null);
    unlockButton.setIcon(new ImageIcon(getClass().getResource("/org/santfeliu/doc/swing/resources/icon/security-lock-open.png")));

    //lockButton.setText(getLocalizedText("lock"));
    lockButton.setAction(lockDocumentAction);
    lockButton.setText(null);
    lockButton.setIcon(new ImageIcon(getClass().getResource("/org/santfeliu/doc/swing/resources/icon/security-lock.png")));
    /*
    purgeButton.setText(getLocalizedText("purge"));
    purgeButton.setAction(purgeDocumentAction);

    commitButton.setText(getLocalizedText("commit"));
    commitButton.setAction(commitDocumentAction);

    abortButton.setText(getLocalizedText("abort"));
    abortButton.setAction(abortDocumentAction);
*/
    saveFileButton.setText(getLocalizedText("saveAs"));
    saveFileButton.setAction(saveAsDocumentAction);

    exploreFileButton.setText(getLocalizedText("explore"));
    exploreFileButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            exploreFileButton_actionPerformed(e);
          }
        });
    propertiesPanel.setBorder(BorderFactory.createTitledBorder(getLocalizedText("customProperties")));

    relatedDocumentsPanel.setBorder(BorderFactory.createTitledBorder(getLocalizedText("relatedDocuments")));

    documentSecurityPanel.setBorder(BorderFactory.createTitledBorder(getLocalizedText("security")));

    lockButtonsPanel.setLayout(lockButtonsPanelLayout);

    lockButtonsPanel.add(lockUserValueLabel, 
                         new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 
                                                GridBagConstraints.WEST, 
                                                GridBagConstraints.NONE, 
                                                new Insets(0, 0, 0, 0), 0, 
                                                0));

    lockButtonsPanel.add(lockButton, 
                         new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, 
                                                GridBagConstraints.WEST, 
                                                GridBagConstraints.NONE, 
                                                new Insets(0, 0, 0, 2), 0, 
                                                0));

    lockButtonsPanel.add(unlockButton, 
                         new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, 
                                                GridBagConstraints.WEST, 
                                                GridBagConstraints.NONE, 
                                                new Insets(0, 2, 0, 0), 0, 
                                                0));

    generalPanel.add(titleLabel, 
                     new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 
                                            GridBagConstraints.CENTER, 
                                            GridBagConstraints.HORIZONTAL, 
                                            new Insets(0, 0, 0, 4), 0, 8));

    generalPanel.add(titleTextField, 
                     new GridBagConstraints(1, 0, 3, 1, 1.0, 0.0, 
                                            GridBagConstraints.CENTER, 
                                            GridBagConstraints.HORIZONTAL, 
                                            new Insets(0, 0, 0, 4), 0, 0));

    generalPanel.add(languageLabel, 
                     new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, 
                                            GridBagConstraints.WEST, 
                                            GridBagConstraints.NONE, 
                                            new Insets(0, 0, 0, 4), 0, 8));

    generalPanel.add(languageComboBox, 
                     new GridBagConstraints(1, 1, 4, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 
                                            new Insets(2, 0, 0, 4), 0, 0));

    generalPanel.add(documentTypeLabel, 
                     new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, 
                                            GridBagConstraints.WEST, 
                                            GridBagConstraints.NONE, 
                                            new Insets(0, 0, 0, 4), 0, 8));

    generalPanel.add(docTypeIdComboBox, 
                     new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 
                                            new Insets(2, 0, 0, 4), 0, 0));

    generalPanel.add(creationDateLabel,
                     new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
                                            GridBagConstraints.CENTER,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 4), 0, 8));

    generalPanel.add(creationDateTextField,
                     new GridBagConstraints(1, 5, 1, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(2, 0, 0, 4), 0, 0));
//                     new GridBagConstraints(1, 5, 1, 1, 1.0, 0.0,
//                                            GridBagConstraints.CENTER,
//                                            GridBagConstraints.HORIZONTAL,
//                                            new Insets(0, 0, 0, 4), 0, 0));

    generalPanel.add(captureDateLabel,
                     new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0,
                                            GridBagConstraints.CENTER, 
                                            GridBagConstraints.HORIZONTAL, 
                                            new Insets(0, 0, 0, 4), 0, 8));

    generalPanel.add(captureDateValueLabel, 
                     new GridBagConstraints(1, 6, 3, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                            new Insets(2, 0, 0, 4), 0, 0));

    generalPanel.add(changeDateLabel,
                     new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0,
                                            GridBagConstraints.CENTER, 
                                            GridBagConstraints.HORIZONTAL, 
                                            new Insets(0, 0, 0, 4), 0, 8));

    generalPanel.add(changeDateValueLabel,
                     new GridBagConstraints(1, 7, 3, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                            new Insets(2, 0, 0, 4), 0, 0));

    generalPanel.add(stateLabel, 
                     new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0,
                                            GridBagConstraints.CENTER, 
                                            GridBagConstraints.HORIZONTAL, 
                                            new Insets(0, 0, 0, 4), 0, 8));

    generalPanel.add(stateComboBox, 
                     new GridBagConstraints(1, 8, 4, 1, 1.0, 0.0,GridBagConstraints.WEST, GridBagConstraints.NONE,
                                            new Insets(2, 0, 0, 4), 0, 0));

    generalPanel.add(lockUserLabel, 
                     new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0,
                                            GridBagConstraints.CENTER, 
                                            GridBagConstraints.HORIZONTAL, 
                                            new Insets(2, 0, 0, 4), 0, 8));

    generalPanel.add(lockButtonsPanel, 
                     new GridBagConstraints(1, 9, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                                            new Insets(2, 0, 0, 4), 0, 0));

/*
    generalPanel.add(newVersionCheckBox, 
                     new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0, 
                                            GridBagConstraints.WEST, 
                                            GridBagConstraints.HORIZONTAL, 
                                            new Insets(0, 0, 0, 0), 0, 0));
*/
    contentPanel.add(fileLabel, 
                     new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0, 
                                            GridBagConstraints.CENTER, 
                                            GridBagConstraints.HORIZONTAL, 
                                            new Insets(0, 0, 0, 4), 0, 8));

    contentPanel.add(fileTextField, 
                     new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, 
                                            GridBagConstraints.CENTER, 
                                            GridBagConstraints.HORIZONTAL, 
                                            new Insets(0, 0, 0, 4), 0, 0));

    contentPanel.add(exploreFileButton, 
                     new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, 
                                            GridBagConstraints.CENTER, 
                                            GridBagConstraints.NONE, 
                                            new Insets(0, 0, 0, 4), 0, 0));

    contentPanel.add(saveFileButton, 
                     new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, 
                                            GridBagConstraints.CENTER, 
                                            GridBagConstraints.NONE, 
                                            new Insets(0, 0, 0, 4), 0, 0));

    contentPanel.add(mimeTypeLabel, 
                     new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                                            GridBagConstraints.WEST, 
                                            GridBagConstraints.HORIZONTAL, 
                                            new Insets(0, 0, 0, 4), 0, 8));

    contentPanel.add(mimeTypeValueLabel, 
                     new GridBagConstraints(1, 1, 3, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST, 
                                            GridBagConstraints.HORIZONTAL, 
                                            new Insets(0, 0, 0, 4), 0, 0));

    contentPanel.add(formatLabel,
                     new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 4), 0, 8));

    contentPanel.add(formatValueLabel,
                     new GridBagConstraints(1, 2, 3, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 4), 0, 0));

    contentPanel.add(sizeLabel, 
                     new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                                            GridBagConstraints.WEST, 
                                            GridBagConstraints.HORIZONTAL, 
                                            new Insets(0, 0, 0, 4), 0, 8));

    contentPanel.add(sizeValueLabel, 
                     new GridBagConstraints(1, 3, 3, 1, 1.0, 0.0,
                                            GridBagConstraints.WEST, 
                                            GridBagConstraints.HORIZONTAL, 
                                            new Insets(0, 0, 0, 4), 0, 0));


    //southPanel.add(lockButton, null);
    //southPanel.add(unlockButton, null);
    southPanel.add(newVersionCheckBox, null);
    southPanel.add(saveButton, null);
    southPanel.add(deleteButton, null);
    //southPanel.add(commitButton, null);
    //southPanel.add(abortButton, null);
    //southPanel.add(purgeButton, null);

    captureDateValueLabel.setText(null);
    captureDateValueLabel.setHorizontalAlignment(SwingConstants.LEFT);
    captureDateLabel.setText(getLocalizedText("captureDate") + ":");
    changeDateValueLabel.setText(null);
    changeDateLabel.setText(getLocalizedText("changeDate") + ":");

    fileLabel.setText(getLocalizedText("file") + ":");
    titleLabel.setText(getLocalizedText("title") + ":");
    creationDateLabel.setText(getLocalizedText("creationDate") + ":");
//    languageComboBox.addItem(null);
    languageComboBox.addItem(DocumentConstants.UNIVERSAL_LANGUAGE);
    languageComboBox.addItem("ca");
    languageComboBox.addItem("es");
    languageComboBox.addItem("fr");
    languageComboBox.addItem("en");
    languageComboBox.addItem("it");
    languageComboBox.addItem("de");

    docTypeIdComboBox.addItem(DictionaryConstants.DOCUMENT_TYPE);
    docTypeIdComboBox.addItem("WORKFLOW");
    docTypeIdComboBox.addItem("FORM");
    docTypeIdComboBox.addItem("TEMPLATE");
    docTypeIdComboBox.addItem("REPORT");
    docTypeIdComboBox.addItem("MAP");
    docTypeIdComboBox.addItem("IMAGE");
    docTypeIdComboBox.setEditable(true);

    stateLabel.setText(getLocalizedText("state") + ":");

    stateComboBox.setPreferredSize(new Dimension(90, 21));
    stateComboBox.setMinimumSize(new Dimension(90, 21));
    stateComboBox.addItem(State.DRAFT.value());
    stateComboBox.addItem(State.COMPLETE.value());
    stateComboBox.addItem(State.RECORD.value());
    stateComboBox.addItem(State.DELETED.value());

    newVersionCheckBox.setText(getLocalizedText("createNewVersion"));
    newVersionCheckBox.setSelected(false);
    newVersionCheckBox.setEnabled(false);

    documentTypeLabel.setText(getLocalizedText("documentType") + ":");

    lockUserValueLabel.setText(null);
    //    lockUserValueLabel.setMinimumSize(new Dimension(100, 15));
    //    lockUserValueLabel.setPreferredSize(new Dimension(100, 24));
    lockUserValueLabel.setHorizontalAlignment(SwingConstants.LEFT);
    lockUserValueLabel.setHorizontalTextPosition(SwingConstants.LEFT);
    
    creationDateTextField.setPreferredSize(new Dimension(80, 21));
    creationDateTextField.setMinimumSize(new Dimension(80, 21));

    new FileDrop(this, new FileDrop.Listener()
        {
          public void filesDropped(File[] files)
          {
            File file = files[0];
            Map values = getFormValues();
            values.put(TITLE, file.getName());
            values.put(DocumentConstants.CONTENTID, file.getAbsolutePath());
            setFormValues(values);
          }
        });

    fireActionEvents = true;


  }

  public void loadDocument(String docId, int version)
  {
    try
    {
      fireActionEvents = false;
      documentExists = false;

      //boolean translationExists = false;
      boolean versionExists = false;
      //DocumentManagerClient client = getDocumentManagerClient();
      // search translations and versions for docId
      //Map filter = new HashMap();
      //filter.put(DocumentConstants.DOCID, docId);
      versionComboBox.removeAllItems();
      //byte[] result = client.findDocuments(MapSerializer.toByteArray(filter));

      DocumentFilter filter = new DocumentFilter();
      filter.getDocId().add(docId);
      filter.setVersion(-1);
      OrderByProperty order = new OrderByProperty();
      order.setName("version");
      order.setDescending(false);
      filter.getOrderByProperty().add(order);
      List<Document> documentList = getClient().findDocuments(filter);

      if (documentList.size() > 0)
      {
        //newVersionCheckBox.setSelected(false);
        for (Document document: documentList)
        {
          //if (document.getLanguage().equalsIgnoreCase(language))
          //{
          //translationExists = true;
          versionComboBox.addItem(String.valueOf(document.getVersion()));
          if (LAST_VERSION == version || 
              String.valueOf(version).equalsIgnoreCase(String.valueOf(document.getVersion())))
          {
            versionExists = true;
          }
          //}
        }
        //if (translationExists)

        newVersionCheckBox.setEnabled(true);
        if (!versionExists)
        {
          version = LAST_VERSION;
        }
        Document doc = getClient().loadDocument(docId, version, ContentInfo.METADATA);
        //doc.getContent().getData().writeTo(System.out);
        //byte[] doc = client.selectDocument(docId, language, version);
        //Map map = MapSerializer.toMap(doc);          
        documentExists = true;
        Map map = new HashMap();
        map.put(DOCID, doc.getDocId());
        map.put(LANGUAGE, doc.getLanguage());
        map.put(VERSION, doc.getVersion());
        map.put(TITLE, doc.getTitle());
        if (doc.getContent() != null)
        {
          map.put(DocumentConstants.CONTENTID, doc.getContent().getContentId());
          map.put(MIMETYPE, 
                  doc.getContent().getContentType());
          map.put(SIZE, doc.getContent().getSize());
          map.put(FORMATDESC,
            doc.getContent().getFormatDescription());
          map.put(FORMATID, doc.getContent().getFormatId());
          internalDocument = (doc.getContent().getUrl() == null);
        }
        map.put(LOCKUSER, doc.getLockUserId());
        map.put(STATE, doc.getState().value());
        map.put(DOCUMENT_TYPE, doc.getDocTypeId());
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar cal = null;
        Calendar cal2 = null;
        if (doc.getCaptureDateTime() != null)
        {
          Date captureDate = df.parse(doc.getCaptureDateTime());
          cal = new GregorianCalendar();
          cal.setTime(captureDate);
        }
        if (doc.getChangeDateTime() != null)
        {
          Date changeDate = df.parse(doc.getChangeDateTime());
          cal2 = new GregorianCalendar();
          cal2.setTime(changeDate);
        }
        map.put(CAPTUREDATE, cal);
        map.put(CHANGEDATE, cal2);
        String creationDate = doc.getCreationDate();
        if (creationDate != null)
        {
          creationDate = new SimpleDateFormat("dd/MM/yyyy").format(
            new SimpleDateFormat("yyyyMMdd").parse(creationDate));
        }
        map.put(CREATION_DATE, creationDate);

        for (Property p: doc.getProperty())
        {
          if (p.getValue().size() > 1)
          {
            for (int i = 0; i < p.getValue().size(); i++)
            {
              String key = p.getName();
              if (!"readRole".equals(key) && !"writeRole".equals(key))
              {
                key = key + (i > 0 ? ":" + i : "");
                String value = p.getValue().get(i);
                map.put(key, value);
              }
            }
          }
          else if (p.getValue().size() == 1)
          {
            map.put(p.getName(), p.getValue().get(0));
          }
        }
        /*
        List<Vector<String>> relatedDocList = new ArrayList<Vector<String>>();
        for (RelatedDocument relatedDocument : doc.getRelatedDocument())
        {
          Vector<String> vDoc = new Vector<String>();
          vDoc.add(0, relatedDocument.getDocId());
          vDoc.add(1, String.valueOf(relatedDocument.getVersion()));
          vDoc.add(2, relatedDocument.getRelationType().value());
          vDoc.add(3, relatedDocument.getName());
          relatedDocList.add(vDoc);
        }
*/
        map.put(DocumentConstants.RELATED_DOC_LIST, doc.getRelatedDocument());
        putMultiValuedFields(doc, map);
        map.put(DocumentConstants.ACL_LIST, doc.getAccessControl());
        setFormValues(map);
        //}
        /*
      else
      {
        docIdValueLabel.setText(docId);
        languageComboBox.setSelectedItem(language);
        clearFormValues();
        newVersionCheckBox.setEnabled(false);
      }
*/

        /*
    Table table = TableSerializer.toTable(result);
    if (table.getRowCount() > 0)
    {
      for (int i = 0; i < table.getRowCount(); i++)
      {
        String nextLanguage =
          (String)table.getElementAt(i, DocumentConstants.LANGUAGE);
        if (nextLanguage.equals(language))
        {
          translationExists = true;

          String nextVersion =
            String.valueOf(table.getElementAt(i, DocumentConstants.VERSION));

          versionComboBox.addItem(nextVersion);
          if (DocumentConstants.LAST_VERSION == version ||
              String.valueOf(version).equals(nextVersion))
          {
            versionExists = true;
          }
        }
      }
      if (translationExists)
      {
        if (!versionExists)
        {
          version = DocumentConstants.LAST_VERSION;
        }
        byte[] doc = client.selectDocument(docId, language, version);
        Map map = MapSerializer.toMap(doc);

        documentExists = true;
        setFormValues(map);
      }
      else
      {
        docIdValueLabel.setText(docId);
        languageComboBox.setSelectedItem(language);
        clearFormValues();
      }
    }
    else // docId do not exists
    {
      docIdValueLabel.setText(DocumentConstants.NEW_DOCUMENT);
      languageComboBox.setSelectedItem(language);
      clearFormValues();
    }
*/
      }
      else
      {
        documentExists = false;
        clearFormValues();
      }
    }
    catch (Exception ex)
    {
      documentExists = false;
      clearFormValues();
      showError(ex);
    }
    finally
    {
      fireActionEvents = true;
    }
  }

  public void setFormValues(Map map)
  {
//    documentSecurityPanel.setServiceURL(wsdlLocation.substring(0,
//      wsdlLocation.lastIndexOf("/")));
    documentSecurityPanel.setWsDirectoryURL(wsDirectoryURL);
    documentSecurityPanel.setUsername(username);
    documentSecurityPanel.setPassword(password);
    documentSecurityPanel.setActionItems(null);

    formatValueLabel.setText("");
    //    documentDraft = false;
    documentLocked = false;
    documentLockedByUser = false;
    //    documentActive = false;
    HashMap properties = new HashMap();

    fireActionEvents = false;
    List<AccessControl> accessControlList = new ArrayList<AccessControl>();
    Iterator iter = map.entrySet().iterator();
    while (iter.hasNext())
    {
      Map.Entry entry = (Map.Entry) iter.next();
      String property = (String) entry.getKey();
      Object value = entry.getValue();
      if (property.equals(DOCID))
      {
        docIdValueLabel.setText(String.valueOf(value));
        if (NEW_DOCUMENT.equals(value))
        {
          documentExists = false;
        }
      }
      else if (property.equals(LANGUAGE))
      {
        languageComboBox.setSelectedItem(
          value == null ? null : String.valueOf(value));
      }
      else if (property.equals(VERSION))
      {
        versionComboBox.setSelectedItem(String.valueOf(value));
      }
      else if (property.equals(TITLE))
      {
        titleTextField.setText((String)value);
      }
      else if (property.equals(CREATION_DATE))
      {
        creationDateTextField.setText((String)value);
      }
      else if (property.equals(DocumentConstants.CONTENTID))
      {
        fileTextField.setText((String)value);
      }
      else if (property.equals(MIMETYPE))
      {
        String mimeType = (String) value;
        mimeTypeValueLabel.setText(mimeType);
        ImageIcon icon = IconUtilities.getIcon(mimeType);
        mimeTypeValueLabel.setIcon(icon);
      }
      else if (property.equals(FILETYPE))
      {
      }
      else if (property.equals(FORMATID))
      {
        String formatId = "";
        if (value != null)
        {
          formatId = " (" + (String)value + ")";
        }
        formatValueLabel.setText(formatValueLabel.getText() + formatId);
      }
      else if (property.equals(FORMATDESC))
      {
        String formatDesc = "";
        if (value != null)
        {
          formatDesc = (String)value;
        }
        formatValueLabel.setText(formatDesc + formatValueLabel.getText());
      }
      else if (property.equals(STATE))
      {
        stateComboBox.setSelectedItem((String) value);
      }
      else if (property.equals(DOCUMENT_TYPE))
      {
        docTypeIdComboBox.setSelectedItem((String) value);
        documentSecurityPanel.setTypeId((String)value);
      }
      else if (property.equals(SIZE))
      {
        long size = 0;
        if (value != null)
          size = Long.parseLong(String.valueOf(value));
        DecimalFormat bytesFormat = new DecimalFormat("#,###,###,##0");
        DecimalFormat decimalFormat = new DecimalFormat("#,###,##0.00");
        String bytesString = "(" + bytesFormat.format(size) + " bytes)";
        String compactString;
        if (size < 1048576)
        {
          compactString = 
              decimalFormat.format((double) size / 1024.0) + " kB";
        }
        else
        {
          compactString = 
              decimalFormat.format((double) size / 1048576.0) + " MB";
        }
        sizeValueLabel.setText(compactString + " " + bytesString);
      }
      else if (property.equals(LOCKUSER))
      {
        String lockUser = (String) value;
        if (lockUser != null)
        {
          documentLocked = true;
          lockUser += " ";
        }
        lockUserValueLabel.setText(lockUser);
        //if (value != null) documentLocked = true;
        if (username.equals(value))
          documentLockedByUser = true;
      }
      else if (property.equals(CAPTUREDATE))
      {
        Calendar date = (Calendar) value;
        if (date != null)
          captureDateValueLabel.setText(getDateString(date));
        else
          captureDateValueLabel.setText("");
        /*
        if (DocumentConstants.FUTURE.getTimeInMillis() == date.getTimeInMillis())
          documentDraft = true;
*/
      }
      else if (property.equals(CHANGEDATE))
      {
        Calendar date = (Calendar) value;
        if (date != null)
          changeDateValueLabel.setText(getDateString(date));
        else
          changeDateValueLabel.setText("");
        /*
        if (date == null) documentActive = true;
        else if (DocumentConstants.FUTURE.getTimeInMillis() ==
                 date.getTimeInMillis())
          documentDraft = true;
*/
      }
      else if (property.equals("origlanguage"))
      {
      }
      else if (property.equals("lastversion"))
      {
      }
      else if (property.equalsIgnoreCase(DocumentConstants.RELATED_DOC_LIST))
      {
        relatedDocumentsPanel.setDocuments((List<RelatedDocument>) value);
      }
      else if (property.equalsIgnoreCase(DocumentConstants.ACL_LIST))
      {
        accessControlList = (List<AccessControl>)value;
      }
      else // custom property
      {
        properties.put(property, value);
      }
    }
    documentSecurityPanel.setAccessControlList(accessControlList);
    //    boolean formEnabled = documentActive && 
    //      (!documentLocked || documentLockedByUser) || !documentExists;
    boolean formEnabled = 
      (!documentLocked || documentLockedByUser) || !documentExists;

    propertiesPanel.setProperties(properties, 1);
    titleTextField.setEditable(formEnabled);
    fileTextField.setEditable(formEnabled);
    creationDateTextField.setEditable(formEnabled);
    exploreFileButton.setEnabled(formEnabled);
    saveFileButton.setEnabled(fileTextField.getText() != null && 
                              documentExists && internalDocument);
    propertiesPanel.setEnabled(formEnabled);
    relatedDocumentsPanel.setEnabled(formEnabled);
    documentSecurityPanel.setEnabled(formEnabled);
    //  lockButton.setEnabled(!documentLocked && documentExists && documentActive);
    lockButton.setEnabled(!documentLocked && documentExists);
    unlockButton.setEnabled(documentLockedByUser && documentExists);
    saveButton.setEnabled(formEnabled);
    //    deleteButton.setEnabled((!documentLocked || documentLockedByUser) && 
    //      documentExists && documentActive);
    deleteButton.setEnabled((!documentLocked || documentLockedByUser) && 
                            documentExists);
    /*
    commitButton.setEnabled((!documentLocked || documentLockedByUser) &&
      documentDraft && documentExists);
    abortButton.setEnabled(!documentLocked && documentDraft && documentExists);
*/
    //purgeButton.setEnabled(documentExists);
    fireActionEvents = true;
  }

  public void clearFormValues()
  {
    fireActionEvents = false;
    versionComboBox.setSelectedItem(null);
    versionComboBox.removeAllItems();
    languageComboBox.setSelectedItem(DocumentConstants.UNIVERSAL_LANGUAGE);
    docTypeIdComboBox.setSelectedItem("Document");
    stateComboBox.setSelectedItem(State.DRAFT.value());

    titleTextField.setText(null);
    fileTextField.setText(null);
    creationDateTextField.setText(null);
    mimeTypeValueLabel.setIcon(null);
    mimeTypeValueLabel.setText(null);
    sizeValueLabel.setText(null);
    captureDateValueLabel.setText(null);
    changeDateValueLabel.setText(null);
    formatValueLabel.setText(null);
    docIdValueLabel.setText(null);
    lockUserValueLabel.setText(null);
    mimeTypeValueLabel.setText(null);

    propertiesPanel.setProperties(new HashMap());
    propertiesPanel.setEnabled(true);
    relatedDocumentsPanel.setDocuments(new ArrayList<RelatedDocument>());
    relatedDocumentsPanel.setEnabled(true);
    documentSecurityPanel.setAccessControlList(new ArrayList<AccessControl>());
    documentSecurityPanel.setEnabled(true);

    titleTextField.setEnabled(true);
    titleTextField.setEditable(true);
    creationDateTextField.setEditable(true);
    fileTextField.setEditable(true);
    exploreFileButton.setEnabled(true);
    saveFileButton.setEnabled(false);
    lockButton.setEnabled(false);
    unlockButton.setEnabled(false);
    saveButton.setEnabled(true);
    deleteButton.setEnabled(false);

    newVersionCheckBox.setSelected(true);
    newVersionCheckBox.setEnabled(false);

    fireActionEvents = true;
  }

  public Map getFormValues()
  {
    Map newProperties = propertiesPanel.getProperties();
    String title = titleTextField.getText();
    String uuid = fileTextField.getText();

    newProperties.put(TITLE, title);
    newProperties.put(DocumentConstants.CONTENTID, uuid);
    newProperties.put(DocumentConstants.RELATED_DOC_LIST,
      relatedDocumentsPanel.getDocuments());
    newProperties.put(DocumentConstants.ACL_LIST,
      documentSecurityPanel.getAccessControlList());
    return newProperties;
  }

  public String getDocId()
  {
    return docIdValueLabel.getText();
  }

  public String getDocLanguage()
  {
    return (String) languageComboBox.getSelectedItem();
  }

  public String getDocVersion()
  {
    return (String) versionComboBox.getSelectedItem();
  }

  public String getDocState()
  {
    return (String) stateComboBox.getSelectedItem();
  }

  public String getDocCreationDate()
  {
    return creationDateTextField.getText();
  }

  public boolean isDocCreateNewVersion()
  {
    return newVersionCheckBox.isSelected();
  }

  public String getDocUUID()
  {
    return fileTextField.getText();
  }

  public String getFileRef()
  {
    return fileTextField.getText();
  }

  public void setFileRef(String fileRef)
  {
    fileTextField.setText(fileRef);
  }

  /*
  private void languageComboBox_actionPerformed(ActionEvent e)
  {
    if (fireActionEvents)
    {
      String docId = docIdValueLabel.getText();
      if (!DocumentConstants.NEW_DOCUMENT.equals(docId))
      {
        String language = (String)languageComboBox.getSelectedItem();
        loadDocument(docId, language, DocumentConstants.LAST_VERSION);
      }
      else clearFormValues();
    }
  }
*/

  private void versionComboBox_actionPerformed(ActionEvent e)
  {
    if (fireActionEvents)
    {
      String docId = docIdValueLabel.getText();
      if (!NEW_DOCUMENT.equals(docId))
      {
        //String language = (String)languageComboBox.getSelectedItem();
        String ver = (String) versionComboBox.getSelectedItem();
        if (ver != null)
        {
          int version = Integer.parseInt(ver);
          loadDocument(docId, version);
        }
      }
      else
        clearFormValues();
    }
  }

  private void exploreFileButton_actionPerformed(ActionEvent e)
  {
    JFileChooser dialog = new JFileChooser();
    if (exploreDir != null)
    {
      dialog.setCurrentDirectory(exploreDir);
    }
    DocumentPreviewPanel preview = new DocumentPreviewPanel();
    dialog.setAccessory(preview);
    dialog.addPropertyChangeListener(preview);

    int result = dialog.showDialog(this, getLocalizedText("open"));
    if (result == JFileChooser.APPROVE_OPTION)
    {
      File file = dialog.getSelectedFile();
      if (file != null)
      {
        fileTextField.setText(file.getPath());
        exploreDir = file.getParentFile();
      }
    }
  }

  /*  private void saveFileButton_actionPerformed(ActionEvent e)
  {
    JFileChooser dialog = new JFileChooser();
    if (saveDir != null)
    {
      dialog.setCurrentDirectory(saveDir);
    }
    int result = dialog.showDialog(this, "Save");
    if (result == JFileChooser.APPROVE_OPTION)
    {
      File file = dialog.getSelectedFile();
      if (file != null)
      {
        saveDir = file.getParentFile();
        try
        {
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          DocumentManagerClient client = getDocumentClient();
          client.setUsername(username);
          client.setPassword(password);
          String uuid = fileTextField.getText();
          DataHandler dh = client.selectFileData(uuid);
          String filename = file.getAbsolutePath();
          MimeTypeMap typeMap =
            (MimeTypeMap)FileTypeMap.getDefaultFileTypeMap();
          String extension = typeMap.getExtension(dh.getContentType());
          if (extension != null) filename += "." + extension;
          FileOutputStream fos = new FileOutputStream(filename);
          dh.writeTo(fos);
          fos.close();
        }
        catch (Exception ex)
        {
          ex.printStackTrace();
          showError(ex);
        }
        finally
        {
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
      }
    }
  }
*/

  public void putMultiValuedFields(Document doc, Map map)
  {
    int size = doc.getAuthorId().size();
    for (int i = 0; i < size; i++)
    {
      String name = "authorId" + (size > 1 ? ":" + i: "");
      String value = doc.getAuthorId().get(i);
      map.put(name, value);
    }
    size = doc.getClassId().size();
    for (int i = 0; i < size; i++)
    {
      String name = "classId" + (size > 1 ? ":" + i: "");
      String value = doc.getClassId().get(i);
      map.put(name, value);
    }
    size = doc.getCaseId().size();
    for (int i = 0; i < size; i++)
    {
      String name = "caseId" + (size > 1 ? ":" + i: "");
      String value = doc.getCaseId().get(i);
      map.put(name, value);
    }
    /*
    int readCount = 0;
    int writeCount = 0;
    for (AccessControl ac : doc.getAccessControl())
    {
      if (DictionaryConstants.READ_ACTION.equals(ac.getAction()))
      {
        String name = "readRole" + (readCount > 0 ? ":" + readCount : "");
        String value = ac.getRoleId();
        readCount++;
        map.put(name, value);
      }
      else if (DictionaryConstants.WRITE_ACTION.equals(ac.getAction()))
      {
        String name = "writeRole" + (writeCount > 0 ? ":" + writeCount : "");
        String value = ac.getRoleId();
        writeCount++;
        map.put(name, value);
      }
    }
    */
  }

  private String getDateString(Calendar date)
  {
    Date time = date.getTime();
    SimpleDateFormat df = 
      new SimpleDateFormat("EEEE, dd/MM/yyyy HH:mm:ss");
    return df.format(time);
  }

  public void setVersionComboBox(JComboBox versionComboBox)
  {
    this.versionComboBox = versionComboBox;
  }

  public JComboBox getVersionComboBox()
  {
    return versionComboBox;
  }

  public void setNewVersionCheckBox(JCheckBox newVersionCheckBox)
  {
    this.newVersionCheckBox = newVersionCheckBox;
  }

  public JCheckBox getNewVersionCheckBox()
  {
    return newVersionCheckBox;
  }

  public void setDocTypeIdComboBox(JComboBox docTypeIdComboBox)
  {
    this.docTypeIdComboBox = docTypeIdComboBox;
  }

  public JComboBox getDocTypeIdComboBox()
  {
    return docTypeIdComboBox;
  }

  public DocumentSecurityPanel getDocumentSecurityPanel()
  {
    return documentSecurityPanel;
  }

  public void setDocumentSecurityPanel(
    DocumentSecurityPanel documentSecurityPanel)
  {
    this.documentSecurityPanel = documentSecurityPanel;
  }

}
