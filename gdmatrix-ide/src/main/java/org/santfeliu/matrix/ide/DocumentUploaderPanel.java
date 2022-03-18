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

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.matrix.cases.CaseManagerPort;
import org.matrix.cases.CaseManagerService;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.doc.uploader.DocumentInfo;
import org.santfeliu.doc.uploader.FileInfo;
import org.santfeliu.doc.uploader.HelpDialog;
import org.santfeliu.doc.uploader.ImagePreviewPanel;
import org.santfeliu.doc.uploader.ImageProcessor;
import org.santfeliu.doc.uploader.ImageViewer;
import org.santfeliu.doc.uploader.Scanner;
import org.santfeliu.doc.uploader.Unloader;
import org.santfeliu.doc.uploader.UploadInfo;
import org.santfeliu.doc.uploader.Uploader;
import org.santfeliu.swing.text.JavaScriptEditorKit;
import org.santfeliu.swing.text.SymbolHighlighter;

/**
 *
 * @author blanquepa
 */
public class DocumentUploaderPanel extends DocumentPanel
{
  private Scanner scanner;
  private Uploader uploader;
  private Unloader unloader;
  private DefaultTableModel documentsTableModel;
  private DefaultTableModel propertiesTableModel;
  private List<DocumentInfo> documents = new ArrayList<>();

  /**
   * Creates new form JDocumentUploaderPanel
   */
  public DocumentUploaderPanel()
  {
    initComponents();
    setupComponents();
  }

  public List<DocumentInfo> getDocuments()
  {
    return documents;
  }

  public DefaultTableModel getDocumentsTableModel()
  {
    return documentsTableModel;
  }

  public JProgressBar getProgressBar()
  {
    return progressBar;
  }

  public void setStatus(String text)
  {
    statusLabel.setText(text);
  }

  public void showProgressPanel()
  {
    ((CardLayout)southPanel.getLayout()).show(southPanel, "progress");
  }

  public void showStatusPanel()
  {
    ((CardLayout)southPanel.getLayout()).show(southPanel, "status");
  }

  public void showStatusPanel(final long millis)
  {
    Thread thread = new Thread()
    {
      @Override
      public void run()
      {
        try
        {
          Thread.sleep(millis);
          SwingUtilities.invokeLater(() -> showStatusPanel());
        }
        catch (InterruptedException ex)
        {
        }
      }
    };
    thread.start();
  }

  public void setButtonsEnabled(boolean enabled)
  {
    scanButton.setEnabled(enabled);
    uploadButton.setEnabled(enabled);
    unloadButton.setEnabled(enabled);
    helpButton.setEnabled(enabled);
  }


  @Override
  public void open(InputStream is) throws Exception
  {
    Properties properties = new Properties();
    properties.load(is);
    // scan
    pathTextField.setText(properties.getProperty("path"));
    patternTextField.setText(properties.getProperty("pattern"));
    filterTextField.setText(properties.getProperty("filter"));
    propertiesEditor.getTextPane().setText(properties.getProperty("properties"));
    propertiesEditor.getTextPane().setCaretPosition(0);

    // general options
    removeFilesCheckBox.setSelected(
      "true".equals(properties.get("removeFiles")));

    // image options
    maxImageSizeCheckBox.setSelected(
      "true".equals(properties.getProperty("maxImageSize")));
    maxImageSizeSpinner.setEnabled(maxImageSizeCheckBox.isSelected());
    String svalue = properties.getProperty("maxImageSizeValue");
    try
    {
      int value = Integer.parseInt(svalue);
      maxImageSizeSpinner.setValue(value);
    }
    catch (NumberFormatException ex)
    {
      MatrixIDE.log(ex);
    }
    // connection
    wsDirectoryTextField.setText(properties.getProperty("wsdir"));
    usernameTextField.setText(properties.getProperty("username"));
    passwordField.setText(properties.getProperty("password"));
  }

  @Override
  public void save(OutputStream os) throws Exception
  {
    Properties properties = new Properties();
    // scan
    properties.put("path", pathTextField.getText());
    properties.put("pattern", patternTextField.getText());
    properties.put("filter", filterTextField.getText());
    properties.put("properties", propertiesEditor.getTextPane().getText());

    // general options
    properties.put("removeFiles",
      String.valueOf(removeFilesCheckBox.isSelected()));

    // image options
    properties.put("maxImageSize",
      String.valueOf(maxImageSizeCheckBox.isSelected()));
    properties.put("maxImageSizeValue",
      String.valueOf(maxImageSizeSpinner.getValue()));

    // connection
    properties.put("wsdir", wsDirectoryTextField.getText());
    properties.put("username", usernameTextField.getText());
    properties.put("password", new String(passwordField.getPassword()));


    properties.store(os, "DocumentLoader");
  }

  @Override
  public void setMainPanel(MainPanel panel)
  {
    super.setMainPanel(panel);

    if (wsDirectoryTextField.getText().equals("")
      && usernameTextField.getText().equals("")
      && passwordField.getText().equals(""))
    {
      ConnectionParameters connParams =
        panel.getConnectionPanel().getSelectedConnection();
      if (connParams != null)
      {
        wsDirectoryTextField.setText(connParams.getURL());
        usernameTextField.setText(connParams.getUsername());
        passwordField.setText(connParams.getPassword());
      }
    }
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {
    java.awt.GridBagConstraints gridBagConstraints;

    toolBar = new javax.swing.JToolBar();
    scanButton = new javax.swing.JButton();
    uploadButton = new javax.swing.JButton();
    unloadButton = new javax.swing.JButton();
    helpButton = new javax.swing.JButton();
    mainSplitPane = new org.santfeliu.swing.FlatSplitPane();
    tabbedPane = new javax.swing.JTabbedPane();
    scanPanel = new javax.swing.JPanel();
    pathLabel = new javax.swing.JLabel();
    pathTextField = new javax.swing.JTextField();
    selectPathButton = new javax.swing.JButton();
    patternLabel = new javax.swing.JLabel();
    patternTextField = new javax.swing.JTextField();
    wildcardsLabel = new javax.swing.JLabel();
    filterLabel = new javax.swing.JLabel();
    filterTextField = new javax.swing.JTextField();
    propertiesLabel = new javax.swing.JLabel();
    filterInfoLabel = new javax.swing.JLabel();
    propertiesEditor = new org.santfeliu.swing.text.TextEditor();
    uploadOptionsPanel = new javax.swing.JPanel();
    removeFilesCheckBox = new javax.swing.JCheckBox();
    padPanel5 = new javax.swing.JPanel();
    imageOptionsPanel = new javax.swing.JPanel();
    maxImageSizeSpinner = new javax.swing.JSpinner();
    maxImageSizeCheckBox = new javax.swing.JCheckBox();
    pixelsLabel = new javax.swing.JLabel();
    padPanel3 = new javax.swing.JPanel();
    padPanel4 = new javax.swing.JPanel();
    pdfOptionsPanel = new javax.swing.JPanel();
    connectionPanel = new javax.swing.JPanel();
    wsDirectoryLabel = new javax.swing.JLabel();
    wsDirectoryTextField = new javax.swing.JTextField();
    usernameLabel = new javax.swing.JLabel();
    usernameTextField = new javax.swing.JTextField();
    passwordField = new javax.swing.JPasswordField();
    passwordLabel = new javax.swing.JLabel();
    padLabel1 = new javax.swing.JLabel();
    padLabel2 = new javax.swing.JLabel();
    resultSplitPane = new org.santfeliu.swing.FlatSplitPane();
    tableScrollPane = new javax.swing.JScrollPane();
    documentsTable = new javax.swing.JTable();
    previewPanel = new javax.swing.JPanel();
    propertiesTableScrollPane = new javax.swing.JScrollPane();
    propertiesTable = new javax.swing.JTable();
    imagePanel = new ImagePreviewPanel();
    southPanel = new javax.swing.JPanel();
    statusPanel = new javax.swing.JPanel();
    statusLabel = new javax.swing.JLabel();
    progressPanel = new javax.swing.JPanel();
    progressBar = new javax.swing.JProgressBar();
    cancelButton = new javax.swing.JButton();

    setPreferredSize(new java.awt.Dimension(860, 600));
    setLayout(new java.awt.BorderLayout());

    toolBar.setFloatable(false);
    toolBar.setRollover(true);

    scanButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/santfeliu/doc/uploader/resources/images/scan.png"))); // NOI18N
    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/santfeliu/doc/uploader/resources/DocumentUploader"); // NOI18N
    scanButton.setText(bundle.getString("scan")); // NOI18N
    scanButton.setFocusable(false);
    scanButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    scanButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    scanButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        scanButtonActionPerformed(evt);
      }
    });
    toolBar.add(scanButton);

    uploadButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/santfeliu/doc/uploader/resources/images/upload.png"))); // NOI18N
    uploadButton.setText(bundle.getString("upload")); // NOI18N
    uploadButton.setFocusable(false);
    uploadButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    uploadButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    uploadButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        uploadButtonActionPerformed(evt);
      }
    });
    toolBar.add(uploadButton);

    unloadButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/santfeliu/doc/uploader/resources/images/unload.png"))); // NOI18N
    unloadButton.setText(bundle.getString("unload")); // NOI18N
    unloadButton.setFocusable(false);
    unloadButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    unloadButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    unloadButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        unloadButtonActionPerformed(evt);
      }
    });
    toolBar.add(unloadButton);

    helpButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/santfeliu/doc/uploader/resources/images/help.png"))); // NOI18N
    helpButton.setText(bundle.getString("help")); // NOI18N
    helpButton.setFocusable(false);
    helpButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    helpButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    helpButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        helpButtonActionPerformed(evt);
      }
    });
    toolBar.add(helpButton);

    add(toolBar, java.awt.BorderLayout.NORTH);

    mainSplitPane.setDividerLocation(220);
    mainSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
    mainSplitPane.setOneTouchExpandable(true);

    tabbedPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

    scanPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
    scanPanel.setPreferredSize(new java.awt.Dimension(362, 20));
    scanPanel.setLayout(new java.awt.GridBagLayout());

    pathLabel.setText(bundle.getString("path")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    scanPanel.add(pathLabel, gridBagConstraints);

    pathTextField.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 0.7;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    scanPanel.add(pathTextField, gridBagConstraints);

    selectPathButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/santfeliu/doc/uploader/resources/images/folder.png"))); // NOI18N
    selectPathButton.setText(bundle.getString("selectPath")); // NOI18N
    selectPathButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
    selectPathButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        selectPathButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    scanPanel.add(selectPathButton, gridBagConstraints);

    patternLabel.setText(bundle.getString("pattern")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    scanPanel.add(patternLabel, gridBagConstraints);

    patternTextField.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 0.7;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    scanPanel.add(patternTextField, gridBagConstraints);

    wildcardsLabel.setText(bundle.getString("regExp")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    scanPanel.add(wildcardsLabel, gridBagConstraints);

    filterLabel.setText(bundle.getString("filter")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    scanPanel.add(filterLabel, gridBagConstraints);

    filterTextField.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 0.7;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    scanPanel.add(filterTextField, gridBagConstraints);

    propertiesLabel.setText(bundle.getString("properties")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    scanPanel.add(propertiesLabel, gridBagConstraints);

    filterInfoLabel.setText(bundle.getString("fileVarExp")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.ipadx = 31;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    scanPanel.add(filterInfoLabel, gridBagConstraints);

    propertiesEditor.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    scanPanel.add(propertiesEditor, gridBagConstraints);

    tabbedPane.addTab(bundle.getString("scanOptions"), scanPanel); // NOI18N

    uploadOptionsPanel.setLayout(new java.awt.GridBagLayout());

    removeFilesCheckBox.setText(bundle.getString("removeFilesAfterUpload")); // NOI18N
    removeFilesCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    uploadOptionsPanel.add(removeFilesCheckBox, gridBagConstraints);

    javax.swing.GroupLayout padPanel5Layout = new javax.swing.GroupLayout(padPanel5);
    padPanel5.setLayout(padPanel5Layout);
    padPanel5Layout.setHorizontalGroup(
      padPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 793, Short.MAX_VALUE)
    );
    padPanel5Layout.setVerticalGroup(
      padPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 155, Short.MAX_VALUE)
    );

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    uploadOptionsPanel.add(padPanel5, gridBagConstraints);

    tabbedPane.addTab(bundle.getString("uploadOptions"), uploadOptionsPanel); // NOI18N

    imageOptionsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
    imageOptionsPanel.setLayout(new java.awt.GridBagLayout());

    maxImageSizeSpinner.setEnabled(false);
    maxImageSizeSpinner.setValue(1000);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.ipadx = 50;
    imageOptionsPanel.add(maxImageSizeSpinner, gridBagConstraints);

    maxImageSizeCheckBox.setText(bundle.getString("maxImageSize")); // NOI18N
    maxImageSizeCheckBox.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        maxImageSizeCheckBoxActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    imageOptionsPanel.add(maxImageSizeCheckBox, gridBagConstraints);

    pixelsLabel.setText("px");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    imageOptionsPanel.add(pixelsLabel, gridBagConstraints);

    javax.swing.GroupLayout padPanel3Layout = new javax.swing.GroupLayout(padPanel3);
    padPanel3.setLayout(padPanel3Layout);
    padPanel3Layout.setHorizontalGroup(
      padPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 0, Short.MAX_VALUE)
    );
    padPanel3Layout.setVerticalGroup(
      padPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 0, Short.MAX_VALUE)
    );

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
    gridBagConstraints.weighty = 1.0;
    imageOptionsPanel.add(padPanel3, gridBagConstraints);

    javax.swing.GroupLayout padPanel4Layout = new javax.swing.GroupLayout(padPanel4);
    padPanel4.setLayout(padPanel4Layout);
    padPanel4Layout.setHorizontalGroup(
      padPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 0, Short.MAX_VALUE)
    );
    padPanel4Layout.setVerticalGroup(
      padPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 0, Short.MAX_VALUE)
    );

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridheight = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    imageOptionsPanel.add(padPanel4, gridBagConstraints);

    tabbedPane.addTab(bundle.getString("imageOptions"), imageOptionsPanel); // NOI18N

    pdfOptionsPanel.setLayout(new java.awt.GridBagLayout());
    tabbedPane.addTab(bundle.getString("pdfOptions"), pdfOptionsPanel); // NOI18N

    connectionPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
    connectionPanel.setLayout(new java.awt.GridBagLayout());

    wsDirectoryLabel.setText(bundle.getString("wsDirURL")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    connectionPanel.add(wsDirectoryLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.7;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    connectionPanel.add(wsDirectoryTextField, gridBagConstraints);

    usernameLabel.setText(bundle.getString("username")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    connectionPanel.add(usernameLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.ipadx = 140;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    connectionPanel.add(usernameTextField, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.ipadx = 140;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    connectionPanel.add(passwordField, gridBagConstraints);

    passwordLabel.setText(bundle.getString("password")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    connectionPanel.add(passwordLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weighty = 1.0;
    connectionPanel.add(padLabel1, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.weightx = 0.3;
    connectionPanel.add(padLabel2, gridBagConstraints);

    tabbedPane.addTab(bundle.getString("serverConnection"), connectionPanel); // NOI18N

    mainSplitPane.setLeftComponent(tabbedPane);
    tabbedPane.getAccessibleContext().setAccessibleName("");

    resultSplitPane.setResizeWeight(0.7);

    tableScrollPane.setBorder(null);

    documentsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    documentsTable.addMouseListener(new java.awt.event.MouseAdapter()
    {
      public void mouseClicked(java.awt.event.MouseEvent evt)
      {
        documentsTablemouseClickedOnTable(evt);
      }
    });
    tableScrollPane.setViewportView(documentsTable);

    resultSplitPane.setLeftComponent(tableScrollPane);

    previewPanel.setLayout(new java.awt.BorderLayout());

    propertiesTableScrollPane.setBorder(null);
    propertiesTableScrollPane.setViewportView(propertiesTable);

    previewPanel.add(propertiesTableScrollPane, java.awt.BorderLayout.CENTER);

    javax.swing.GroupLayout imagePanelLayout = new javax.swing.GroupLayout(imagePanel);
    imagePanel.setLayout(imagePanelLayout);
    imagePanelLayout.setHorizontalGroup(
      imagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 248, Short.MAX_VALUE)
    );
    imagePanelLayout.setVerticalGroup(
      imagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 100, Short.MAX_VALUE)
    );

    previewPanel.add(imagePanel, java.awt.BorderLayout.PAGE_START);

    resultSplitPane.setRightComponent(previewPanel);

    mainSplitPane.setRightComponent(resultSplitPane);

    add(mainSplitPane, java.awt.BorderLayout.CENTER);

    southPanel.setLayout(new java.awt.CardLayout());

    statusPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

    statusLabel.setText(bundle.getString("pressToStartScan")); // NOI18N
    statusPanel.add(statusLabel);

    southPanel.add(statusPanel, "status");

    progressPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
    progressPanel.setLayout(new java.awt.BorderLayout(4, 0));

    progressBar.setString("");
    progressBar.setStringPainted(true);
    progressPanel.add(progressBar, java.awt.BorderLayout.CENTER);

    cancelButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/santfeliu/doc/uploader/resources/images/cancel.png"))); // NOI18N
    cancelButton.setText(bundle.getString("cancel")); // NOI18N
    cancelButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
    cancelButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        cancelButtonActionPerformed(evt);
      }
    });
    progressPanel.add(cancelButton, java.awt.BorderLayout.EAST);

    southPanel.add(progressPanel, "progress");

    add(southPanel, java.awt.BorderLayout.SOUTH);
  }// </editor-fold>//GEN-END:initComponents

  private void setupComponents()
  {
    documentsTableModel = new DefaultTableModel()
    {
      @Override
      public Class getColumnClass(int columnIndex)
      {
        if (columnIndex == 1) return Integer.class;
        if (columnIndex == 3) return Long.class;
        return String.class;
      }

      @Override
      public boolean isCellEditable(int row, int column)
      {
        return false;
      }
    };
    resultSplitPane.setDividerLocation(0.7);

    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle(
      "org/santfeliu/doc/uploader/resources/DocumentUploader"); // NOI18N

    ;
    documentsTableModel.addColumn(bundle.getString("state"));
    documentsTableModel.addColumn(bundle.getString("position"));
    documentsTableModel.addColumn(bundle.getString("filename"));
    documentsTableModel.addColumn(bundle.getString("size"));
    documentsTable.setAutoCreateColumnsFromModel(false);
    documentsTable.getTableHeader().setReorderingAllowed(false);
    documentsTable.setModel(documentsTableModel);
    TableCellRenderer stateRenderer = new DefaultTableCellRenderer()
    {
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column)
      {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
          row, column);
        String state = String.valueOf(value);
        if (state.startsWith("!")) this.setForeground(Color.RED);
        else this.setForeground(Color.BLACK);
        return this;
      }
    };
    TableCellRenderer sizeRenderer = new DefaultTableCellRenderer()
    {
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column)
      {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
          row, column);
        Long size = (Long)value;
        DecimalFormat df = new DecimalFormat("###,###,###,###,##0");
        String svalue = df.format(size);
        setHorizontalAlignment(JLabel.RIGHT);
        setText(svalue);
        return this;
      }
    };
    documentsTable.addColumn(new TableColumn(0, 150, stateRenderer, null));
    documentsTable.addColumn(new TableColumn(1, 100, null, null));
    documentsTable.addColumn(new TableColumn(2, 600, null, null));
    documentsTable.addColumn(new TableColumn(3, 200, sizeRenderer, null));
    documentsTable.getSelectionModel().addListSelectionListener(
      new ListSelectionListener()
    {
      @Override
      public void valueChanged(ListSelectionEvent e)
      {
        if (e.getValueIsAdjusting()) return;
        int selectedRow = documentsTable.getSelectedRow();
        if (selectedRow != -1)
        {
          DocumentInfo docInfo = documents.get(selectedRow);
          Vector dataVector = propertiesTableModel.getDataVector();
          dataVector.clear();
          propertiesTableModel.fireTableDataChanged();
          Map metadata = docInfo.getMetadata();
          ArrayList list = new ArrayList();
          list.addAll(metadata.keySet());
          Collections.sort(list);
          for (Object key : list)
          {
            String propertyName = (String)key;
            Object value = metadata.get(propertyName);
            propertiesTableModel.addRow(new Object[]{propertyName, value});
          }
          String extension = docInfo.getFile().getExtension().toLowerCase();
          if (extension.equals("jpg") || extension.equals("jpeg") ||
              extension.equals("png") || extension.equals("gif"))
          {
            ((ImagePreviewPanel)imagePanel).setImageFile(docInfo.getFile().getFile());
          }
          else
          {
            ((ImagePreviewPanel)imagePanel).setImageFile(null);
          }
        }
      }
    });

    propertiesTableModel = new DefaultTableModel()
    {
      @Override
      public boolean isCellEditable(int row, int column)
      {
        return false;
      }
    };
    propertiesTableModel.addColumn(bundle.getString("property"));
    propertiesTableModel.addColumn(bundle.getString("value"));
    propertiesTable.setAutoCreateColumnsFromModel(false);
    propertiesTable.getTableHeader().setReorderingAllowed(false);
    propertiesTable.setModel(propertiesTableModel);
    propertiesTable.addColumn(new TableColumn(0, 100, null, null));
    propertiesTable.addColumn(new TableColumn(1, 100, null, null));

    JTextPane propertiesPane = propertiesEditor.getTextPane();
    propertiesPane.setFont(Options.getEditorFont());
    propertiesPane.setEditorKit(new JavaScriptEditorKit());

    SymbolHighlighter symbolHighlighter =
      new SymbolHighlighter(propertiesPane, "({[", ")}]");
  }

  private void scanButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_scanButtonActionPerformed
  {//GEN-HEADEREND:event_scanButtonActionPerformed
    if (scanner == null || scanner.isDone() || scanner.isCancelled())
    {
      try
      {
        File dir = new File(pathTextField.getText());
        if (!dir.exists()) throw new IOException("Invalid path");
        String pattern = patternTextField.getText();
        String filter = filterTextField.getText();
        String properties = propertiesEditor.getTextPane().getText();

        documentsTableModel.getDataVector().clear();
        documentsTableModel.fireTableDataChanged();
        propertiesTableModel.getDataVector().clear();
        propertiesTableModel.fireTableDataChanged();
        ((ImagePreviewPanel)imagePanel).setImageFile(null);
        ((ImagePreviewPanel)imagePanel).clearCache();
        scanner = new Scanner(this, dir, pattern, filter, properties);
        scanner.execute();
        setButtonsEnabled(false);
        showProgressPanel();
        progressBar.setString("Scanning...");
        progressBar.setValue(0);
      }
      catch (Exception ex)
      {
        JOptionPane.showMessageDialog(this, ex, "ERROR",
          JOptionPane.ERROR_MESSAGE);
      }
    }
  }//GEN-LAST:event_scanButtonActionPerformed

  private void uploadButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_uploadButtonActionPerformed
  {//GEN-HEADEREND:event_uploadButtonActionPerformed
    try
    {
      if (!documents.isEmpty() &&
        (uploader == null || uploader.isDone() || uploader.isCancelled()))
      {
        progressBar.setValue(0);
        progressBar.setMinimum(0);
        progressBar.setMaximum(documents.size());
        progressBar.setString("Uploading...");
        uploader = new Uploader(this, getDocumentManagerClient(),
            getCaseManagerPort());
        if (maxImageSizeCheckBox.isSelected())
        {
          uploader.setMaxImageSize((Integer)maxImageSizeSpinner.getValue());
        }
        uploader.setRemoveFiles(removeFilesCheckBox.isSelected());
        uploader.execute();
        setButtonsEnabled(false);
        showProgressPanel();
      }
    }
    catch (Exception ex)
    {
      JOptionPane.showMessageDialog(this, ex.toString(), "ERROR",
        JOptionPane.ERROR_MESSAGE);
    }
  }//GEN-LAST:event_uploadButtonActionPerformed

  private void unloadButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_unloadButtonActionPerformed
  {//GEN-HEADEREND:event_unloadButtonActionPerformed
    try
    {
      if (!documents.isEmpty() &&
        (unloader == null || unloader.isDone() || unloader.isCancelled()))
      {
        progressBar.setValue(0);
        progressBar.setMinimum(0);
        progressBar.setMaximum(documents.size());
        progressBar.setString("Unloading...");
        unloader = new Unloader(this, getDocumentManagerClient(),
            getCaseManagerPort());
        unloader.execute();
        setButtonsEnabled(false);
        showProgressPanel();
      }
    }
    catch (Exception ex)
    {
      JOptionPane.showMessageDialog(this, ex.toString(), "ERROR",
        JOptionPane.ERROR_MESSAGE);
    }
  }//GEN-LAST:event_unloadButtonActionPerformed

  private void helpButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_helpButtonActionPerformed
  {//GEN-HEADEREND:event_helpButtonActionPerformed
    HelpDialog dialog = new HelpDialog(getMainPanel().getIDE(), false);
    dialog.setSize(600, 600);
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
  }//GEN-LAST:event_helpButtonActionPerformed

  private void selectPathButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_selectPathButtonActionPerformed
  {//GEN-HEADEREND:event_selectPathButtonActionPerformed
    JFileChooser chooser = new JFileChooser();
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    String path = pathTextField.getText();
    File dir;
    if (path != null && path.length() > 0)
    {
      dir = new File(path);
      if (dir.exists() && dir.isDirectory()) chooser.setCurrentDirectory(dir);
    }
    int result = chooser.showDialog(this, "Select");
    if (result == JFileChooser.APPROVE_OPTION)
    {
      dir = chooser.getSelectedFile();
      pathTextField.setText(dir.getAbsolutePath());
    }
  }//GEN-LAST:event_selectPathButtonActionPerformed

  private void maxImageSizeCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_maxImageSizeCheckBoxActionPerformed
  {//GEN-HEADEREND:event_maxImageSizeCheckBoxActionPerformed
    maxImageSizeSpinner.setEnabled(maxImageSizeCheckBox.isSelected());
  }//GEN-LAST:event_maxImageSizeCheckBoxActionPerformed

  private void documentsTablemouseClickedOnTable(java.awt.event.MouseEvent evt)//GEN-FIRST:event_documentsTablemouseClickedOnTable
  {//GEN-HEADEREND:event_documentsTablemouseClickedOnTable
    if (evt.getClickCount() > 1)
    {
      if (evt.isShiftDown())
      {
        // show uploadinfo
        showUploadInfo();
      }
      else
      {
        showDocument();
      }
    }
  }//GEN-LAST:event_documentsTablemouseClickedOnTable

  private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
  {//GEN-HEADEREND:event_cancelButtonActionPerformed
    cancelTasks();
  }//GEN-LAST:event_cancelButtonActionPerformed


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton cancelButton;
  private javax.swing.JPanel connectionPanel;
  private javax.swing.JTable documentsTable;
  private javax.swing.JLabel filterInfoLabel;
  private javax.swing.JLabel filterLabel;
  private javax.swing.JTextField filterTextField;
  private javax.swing.JButton helpButton;
  private javax.swing.JPanel imageOptionsPanel;
  private javax.swing.JPanel imagePanel;
  private javax.swing.JSplitPane mainSplitPane;
  private javax.swing.JCheckBox maxImageSizeCheckBox;
  private javax.swing.JSpinner maxImageSizeSpinner;
  private javax.swing.JLabel padLabel1;
  private javax.swing.JLabel padLabel2;
  private javax.swing.JPanel padPanel3;
  private javax.swing.JPanel padPanel4;
  private javax.swing.JPanel padPanel5;
  private javax.swing.JPasswordField passwordField;
  private javax.swing.JLabel passwordLabel;
  private javax.swing.JLabel pathLabel;
  private javax.swing.JTextField pathTextField;
  private javax.swing.JLabel patternLabel;
  private javax.swing.JTextField patternTextField;
  private javax.swing.JPanel pdfOptionsPanel;
  private javax.swing.JLabel pixelsLabel;
  private javax.swing.JPanel previewPanel;
  private javax.swing.JProgressBar progressBar;
  private javax.swing.JPanel progressPanel;
  private org.santfeliu.swing.text.TextEditor propertiesEditor;
  private javax.swing.JLabel propertiesLabel;
  private javax.swing.JTable propertiesTable;
  private javax.swing.JScrollPane propertiesTableScrollPane;
  private javax.swing.JCheckBox removeFilesCheckBox;
  private javax.swing.JSplitPane resultSplitPane;
  private javax.swing.JButton scanButton;
  private javax.swing.JPanel scanPanel;
  private javax.swing.JButton selectPathButton;
  private javax.swing.JPanel southPanel;
  private javax.swing.JLabel statusLabel;
  private javax.swing.JPanel statusPanel;
  private javax.swing.JTabbedPane tabbedPane;
  private javax.swing.JScrollPane tableScrollPane;
  private javax.swing.JToolBar toolBar;
  private javax.swing.JButton unloadButton;
  private javax.swing.JButton uploadButton;
  private javax.swing.JPanel uploadOptionsPanel;
  private javax.swing.JLabel usernameLabel;
  private javax.swing.JTextField usernameTextField;
  private javax.swing.JLabel wildcardsLabel;
  private javax.swing.JLabel wsDirectoryLabel;
  private javax.swing.JTextField wsDirectoryTextField;
  // End of variables declaration//GEN-END:variables



  private void cancelTasks()
  {
    if (scanner != null)
    {
      scanner.cancel(false);
    }
    if (uploader != null)
    {
      uploader.cancel(false);
    }
    if (unloader != null)
    {
      unloader.cancel(false);
    }
  }

  private void showDocument()
  {
    int row = documentsTable.getSelectedRow();
    if (row != -1)
    {
      DocumentInfo docInfo = documents.get(row);
      FileInfo fileInfo = docInfo.getFile();
      if (fileInfo.isImage())
      {
        ImageProcessor processor = new ImageProcessor(fileInfo.getFile());
        if (maxImageSizeCheckBox.isSelected())
        {
          processor.setMaxSize((Integer)maxImageSizeSpinner.getValue());
        }
        ImageViewer imageViewer = new ImageViewer(getMainPanel().getIDE());
        imageViewer.setSize(800, 600);
        imageViewer.setLocationRelativeTo(null);
        imageViewer.processImage(processor);
        imageViewer.setVisible(true);
      }
      else
      {
        try
        {
          Desktop desktop = Desktop.getDesktop();
          desktop.open(fileInfo.getFile());
        }
        catch (Exception ex)
        {
          JOptionPane.showMessageDialog(this, ex.toString(),
            "ERROR", JOptionPane.ERROR_MESSAGE);
        }
      }
    }
  }

  private void showUploadInfo()
  {
    int row = documentsTable.getSelectedRow();
    if (row != -1)
    {
      DocumentInfo docInfo = documents.get(row);
      FileInfo fileInfo = docInfo.getFile();
      UploadInfo uploadInfo = fileInfo.getUploadInfo();
      StringBuilder builder = new StringBuilder();
      if (uploadInfo.getDocId() != null)
      {
        builder.append("docId: ").append(uploadInfo.getDocId()).append("\n");
        if (uploadInfo.getLastModified() > 0)
        {
          Date date = new Date(uploadInfo.getLastModified());
          builder.append("lastModified: ").append(date).append("\n");
        }
        if (uploadInfo.getCaseId() != null)
        {
          builder.append("caseId: ").append(uploadInfo.getCaseId()).append("\n");
        }
      }
      else builder.append("Not uploaded yet.\n");

      String error = uploadInfo.getError();
      if (error != null)
      {
        builder.append("Error: ").append(error).append("\n");
      }
      JOptionPane.showMessageDialog(this, builder.toString());
    }
  }

  FileFilter fileFilter = new FileFilter()
  {
    @Override
    public boolean accept(File file)
    {
      return file.getName().toLowerCase().endsWith(".upl");
    }

    @Override
    public String getDescription()
    {
      return "DocumentUploader setup (*.upl)";
    }
  };

  private DocumentManagerClient getDocumentManagerClient() throws Exception
  {
    URL wsDirURL = new URL(wsDirectoryTextField.getText());
    String username = this.usernameTextField.getText();
    String password = new String(passwordField.getPassword());

    DocumentManagerClient docClient = new DocumentManagerClient(wsDirURL,
      username, password);
    return docClient;
  }

  private CaseManagerPort getCaseManagerPort() throws Exception
  {
    URL wsDirURL = new URL(wsDirectoryTextField.getText());
    String username = this.usernameTextField.getText();
    String password = new String(passwordField.getPassword());
    WSDirectory wsDir = WSDirectory.getInstance(wsDirURL);
    WSEndpoint endpoint = wsDir.getEndpoint(CaseManagerService.class);
    return endpoint.getPort(CaseManagerPort.class, username, password);
  }
}
