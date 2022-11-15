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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Frame;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.activation.DataHandler;
import javax.swing.JOptionPane;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.Property;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.State;
import org.matrix.security.AccessControl;
import org.matrix.translation.TranslationConstants;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.swing.text.RestrictedDocument;
import org.santfeliu.util.MapEditor;
import org.santfeliu.util.MemoryDataSource;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author realor
 */
public class SaveDocumentDialog extends javax.swing.JDialog
{
  private final MainPanel mainPanel;

  public SaveDocumentDialog(Frame owner, MainPanel mainPanel)
  {
    super(owner, true);
    this.mainPanel = mainPanel;
    initComponents();

    languageComboBox.addItem(TranslationConstants.UNIVERSAL_LANGUAGE);
    languageComboBox.addItem("ca");
    languageComboBox.addItem("es");
    languageComboBox.addItem("en");
    languageComboBox.addItem("fr");
    languageComboBox.addItem("it");
    languageComboBox.addItem("de");
    RestrictedDocument document = new RestrictedDocument();
    document.setMaxLength(32);
    document.setPattern("[a-zA-Z][a-zA-Z0-9_]*");
    nameTextField.setDocument(document);

    metadataTextArea.setFont(Options.getEditorFont());
  }

  public void showForm()
  {
    boolean urlChange = false;
    DocumentPanel panel = mainPanel.getActivePanel();
    String panelUrl = panel.getConnectionUrl();
    String activeUrl =
      mainPanel.getConnectionPanel().getSelectedConnection().getURL();
    String url = activeUrl;
    if (panelUrl != null && !panelUrl.equals(activeUrl))
    {
      urlChange = true;
      url = panelUrl + " -> " + activeUrl;
      urlValueLabel.setBackground(Color.YELLOW);
      urlValueLabel.setOpaque(true);
    }
    urlValueLabel.setText(url);

    DocumentType documentType = panel.getDocumentType();
    typeValueLabel.setText(documentType.getDisplayName());
    typeValueLabel.setIcon(documentType.getIcon());
    nameTextField.setText(panel.getDisplayName());
    String description = panel.getDescription();
    if (description == null) description = "";
    else description = description.trim();
    descriptionTextField.setText(description);
    languageComboBox.setSelectedItem(panel.getLanguage());

    Map<String, Object> metadata = new HashMap<>();
    if (panel.getDocId() != null)
    {
      // load document metadata & roles

      docIdLabel.setVisible(true);
      docIdValueLabel.setVisible(true);
      newVersionCheckBox.setVisible(true);

      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      try
      {
        Document document;
        DocumentManagerClient client = mainPanel.getDocumentManagerClient();
        if (urlChange)
        {
          //Load source document
          ConnectionParameters fromConnParams =
            mainPanel.getConnectionPanel().getConnectionByUrl(panelUrl);
          DocumentManagerClient fromClient =
            mainPanel.getDocumentManagerClient(fromConnParams);
          document = fromClient.loadDocument(panel.getDocId(),
            panel.getVersion());
          docIdValueLabel.setText(getTargetDocIdValueLabel(panel, client));
        }
        else
        {
          document = client.loadDocument(panel.getDocId(), panel.getVersion());
          docIdValueLabel.setText(panel.getDocId() + " / " +
            panel.getVersion());
        }
        for (org.matrix.dic.Property property : document.getProperty())
        {
          String propertyName = property.getName();
          List propertyValue = property.getValue();
          if (propertyValue.size() == 1)
          {
            metadata.put(propertyName, propertyValue.get(0));
          }
          else
          {
            metadata.put(propertyName, propertyValue);
          }
        }
        String propertyName = documentType.getPropertyName();
        metadata.remove(propertyName);
        Map fixedProperties = documentType.getFixedProperties();
        if (fixedProperties != null)
        {
          metadata.keySet().removeAll(fixedProperties.keySet());
        }
        List<String> readRoles = new ArrayList<>();
        List<String> updateRoles = new ArrayList<>();
        List<String> executeRoles = new ArrayList<>();

        for (AccessControl ac : document.getAccessControl())
        {
          if (DictionaryConstants.READ_ACTION.equals(ac.getAction()))
          {
            String readRole = ac.getRoleId();
            readRoles.add(readRole);
          }
          else if (DictionaryConstants.WRITE_ACTION.equals(ac.getAction()))
          {
            String updateRole = ac.getRoleId();
            updateRoles.add(updateRole);
          }
          else if (DictionaryConstants.EXECUTE_ACTION.equals(ac.getAction()))
          {
            String executeRole = ac.getRoleId();
            executeRoles.add(executeRole);
          }
        }
        readRolesTextField.setText(TextUtils.joinWords(readRoles, ","));
        updateRolesTextField.setText(TextUtils.joinWords(updateRoles, ","));
        executeRolesTextField.setText(TextUtils.joinWords(executeRoles, ","));
      }
      catch (Exception ex)
      {
        // ignore
      }
      finally
      {
        setCursor(Cursor.getDefaultCursor());
      }
    }
    else
    {
      docIdLabel.setVisible(false);
      docIdValueLabel.setVisible(false);
      newVersionCheckBox.setVisible(false);
    }
    MapEditor mapEditor = new MapEditor(metadata);
    metadataTextArea.setText(mapEditor.format());
    pack();
    setVisible(true);
  }

  private String getTargetDocIdValueLabel(DocumentPanel panel,
    DocumentManagerClient targetClient)
  {
    DocumentType documentType = panel.getDocumentType();
    String propName = documentType.getPropertyName();
    String docTypeId = documentType.getDocTypeId();
    DocumentFilter filter = new DocumentFilter();
    filter.setDocTypeId(docTypeId);
    filter.setVersion(0); // last version
    filter.setRolesDisabled(true); // disable role check
    Property prop = new Property();
    prop.setName(propName);
    if (panel.getDisplayName() != null &&
      panel.getDisplayName().trim().length() > 0)
    {
      prop.getValue().add(panel.getDisplayName());
    }
    filter.getProperty().add(prop);
    List<Document> docList = targetClient.findDocuments(filter);
    int count = docList.size();
    if (count == 1) // document already exists with that name
    {
      return docList.get(0).getDocId() + " / " +
        docList.get(0).getVersion();
    }
    else if (count > 1)
    {
      saveButton.setEnabled(false);
      return "MULTIPLE DOCUMENTS";
    }
    else
    {
      return "NEW DOCUMENT";
    }
  }

  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {
    java.awt.GridBagConstraints gridBagConstraints;

    propertiesPanel = new javax.swing.JPanel();
    urlLabel = new javax.swing.JLabel();
    urlValueLabel = new javax.swing.JLabel();
    typeLabel = new javax.swing.JLabel();
    typeValueLabel = new javax.swing.JLabel();
    nameLabel = new javax.swing.JLabel();
    nameTextField = new javax.swing.JTextField();
    descriptionLabel = new javax.swing.JLabel();
    descriptionTextField = new javax.swing.JTextField();
    languageLabel = new javax.swing.JLabel();
    languageComboBox = new javax.swing.JComboBox<>();
    readRolesLabel = new javax.swing.JLabel();
    readRolesTextField = new javax.swing.JTextField();
    updateRolesLabel = new javax.swing.JLabel();
    updateRolesTextField = new javax.swing.JTextField();
    metadataLabel = new javax.swing.JLabel();
    metadataScrollPane = new javax.swing.JScrollPane();
    metadataTextArea = new javax.swing.JTextArea();
    docIdLabel = new javax.swing.JLabel();
    docIdValueLabel = new javax.swing.JLabel();
    newVersionCheckBox = new javax.swing.JCheckBox();
    executeRolesLabel = new javax.swing.JLabel();
    executeRolesTextField = new javax.swing.JTextField();
    southPanel = new javax.swing.JPanel();
    saveButton = new javax.swing.JButton();
    cancelButton = new javax.swing.JButton();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    setTitle("Save into Document Manager");
    setMinimumSize(new java.awt.Dimension(500, 400));

    propertiesPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
    propertiesPanel.setLayout(new java.awt.GridBagLayout());

    urlLabel.setText("URL:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    propertiesPanel.add(urlLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    propertiesPanel.add(urlValueLabel, gridBagConstraints);

    typeLabel.setText("Type:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    propertiesPanel.add(typeLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    propertiesPanel.add(typeValueLabel, gridBagConstraints);

    nameLabel.setText("Name:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    propertiesPanel.add(nameLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    propertiesPanel.add(nameTextField, gridBagConstraints);

    descriptionLabel.setText("Description:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    propertiesPanel.add(descriptionLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    propertiesPanel.add(descriptionTextField, gridBagConstraints);

    languageLabel.setText("Language:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    propertiesPanel.add(languageLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    propertiesPanel.add(languageComboBox, gridBagConstraints);

    readRolesLabel.setText("Read roles:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    propertiesPanel.add(readRolesLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    propertiesPanel.add(readRolesTextField, gridBagConstraints);

    updateRolesLabel.setText("Update roles:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    propertiesPanel.add(updateRolesLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    propertiesPanel.add(updateRolesTextField, gridBagConstraints);

    metadataLabel.setText("Metadata:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    propertiesPanel.add(metadataLabel, gridBagConstraints);

    metadataTextArea.setColumns(20);
    metadataTextArea.setRows(4);
    metadataTextArea.setTabSize(2);
    metadataScrollPane.setViewportView(metadataTextArea);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 9;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    propertiesPanel.add(metadataScrollPane, gridBagConstraints);

    docIdLabel.setText("DocId / version:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 10;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    propertiesPanel.add(docIdLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 10;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    propertiesPanel.add(docIdValueLabel, gridBagConstraints);

    newVersionCheckBox.setText("Save as new version");
    newVersionCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 11;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    propertiesPanel.add(newVersionCheckBox, gridBagConstraints);

    executeRolesLabel.setText("Execute roles:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    propertiesPanel.add(executeRolesLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    propertiesPanel.add(executeRolesTextField, gridBagConstraints);

    getContentPane().add(propertiesPanel, java.awt.BorderLayout.CENTER);

    saveButton.setText("Save");
    saveButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        saveButtonActionPerformed(evt);
      }
    });
    southPanel.add(saveButton);

    cancelButton.setText("Cancel");
    cancelButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        cancelButtonActionPerformed(evt);
      }
    });
    southPanel.add(cancelButton);

    getContentPane().add(southPanel, java.awt.BorderLayout.PAGE_END);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void saveButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_saveButtonActionPerformed
  {//GEN-HEADEREND:event_saveButtonActionPerformed
    DocumentPanel panel = mainPanel.getActivePanel();
    if (panel == null) return;

    String oldName = panel.getName();
    String oldDescription = panel.getDescription();

    try
    {
      String name = nameTextField.getText();
      String description = descriptionTextField.getText();
      String language = (String) languageComboBox.getSelectedItem();
      String readRolesString = readRolesTextField.getText();
      String updateRolesString = updateRolesTextField.getText();
      String executeRolesString = executeRolesTextField.getText();

      DocumentType documentType = panel.getDocumentType();
      String propertyName = documentType.getPropertyName();
      String docTypeId = documentType.getDocTypeId();
      Map fixedProperties = documentType.getFixedProperties();

      // check if a document with that type, name and language already exists
      DocumentManagerClient client = mainPanel.getDocumentManagerClient();
      DocumentFilter filter = new DocumentFilter();
      filter.setDocTypeId(docTypeId);
      filter.setLanguage(language);
      filter.setVersion(0); // last version
      filter.setRolesDisabled(true); // disable role check
      Property property = new Property();
      property.setName(propertyName);
      if (name != null && name.trim().length() > 0)
      {
        property.getValue().add(name);
      }
      filter.getProperty().add(property);
      // find documents with that name
      List<Document> docList = client.findDocuments(filter);
      int count = docList.size();

      Document document = new Document();

      if (count == 1) // document already exists with that name
      {
        if (panel.getDocId() == null || !panel.getDisplayName().equals(name))
        {
          // new name but that name already exists
          int option = JOptionPane.showConfirmDialog(this,
            "A document with that name already exists. Save anyway?", "Warning",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
          if (option == JOptionPane.NO_OPTION)
          {
            panel.setName(oldName);
            panel.setDescription(oldDescription);
            return;
          }
        }
        Document prevDocument = docList.get(0);
        document.setDocId(prevDocument.getDocId());

        if (newVersionCheckBox.isSelected())
        {
          document.setVersion(-1); // same name, new version
        }
        else
        {
          // same name & version
         document.setVersion(prevDocument.getVersion());
        }
      }
      else if (count > 1)
      {
        throw new Exception("Several documents with that name already exist.");
      }

      panel.setDisplayName(name);
      panel.setDescription(description);

      // fill Document object
      document.setTitle(name + ": " + description);
      document.setDocTypeId(docTypeId);
      document.setLanguage(language);
      document.setState(State.COMPLETE);

      // add propertyName to document
      property = new Property();
      property.setName(propertyName);
      property.getValue().add(name);
      document.getProperty().add(property);

      // read metadata from textArea
      Map<String, Object> metadata = new HashMap<>();
      MapEditor mapEditor = new MapEditor(metadata);
      mapEditor.parse(metadataTextArea.getText());
      // remove doc propertyName from metadata
      metadata.remove(propertyName);

      // add fixed properties
      if (fixedProperties != null)
      {
        // remove fixed properties from metadata
        metadata.keySet().removeAll(fixedProperties.keySet());

        // add fixed properties to document
        List<Property> props =
          DictionaryUtils.getPropertiesFromMap(fixedProperties);
        document.getProperty().addAll(props);
      }

      // add metatada properties to document
      List<Property> props =
        DictionaryUtils.getPropertiesFromMap(metadata);
      if (props != null)
      {
        document.getProperty().addAll(props);
      }

      // add read & update roles to document
      Collection<String> readRoles = TextUtils.splitWords(readRolesString);
      Collection<String> updateRoles = TextUtils.splitWords(updateRolesString);
      Collection<String> executeRoles = TextUtils.splitWords(executeRolesString);
      for (String readRole : readRoles)
      {
        AccessControl ac = new AccessControl();
        ac.setRoleId(readRole);
        ac.setAction(DictionaryConstants.READ_ACTION);
        document.getAccessControl().add(ac);
      }
      for (String updateRole : updateRoles)
      {
        AccessControl ac = new AccessControl();
        ac.setRoleId(updateRole);
        ac.setAction(DictionaryConstants.WRITE_ACTION);
        document.getAccessControl().add(ac);
      }
      for (String executeRole : executeRoles)
      {
        AccessControl ac = new AccessControl();
        ac.setRoleId(executeRole);
        ac.setAction(DictionaryConstants.EXECUTE_ACTION);
        document.getAccessControl().add(ac);
      }

      // save document source into DataHandler
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      panel.save(bos);
      panel.setConnectionUrl(mainPanel.getConnectionPanel().
        getSelectedConnection().getURL());
      MemoryDataSource dataSource = new MemoryDataSource(
        bos.toByteArray(), "data", documentType.getMimeType());
      DataHandler dh = new DataHandler(dataSource);
      Content content = new Content();
      content.setData(dh);
      content.setContentType(dh.getContentType());
      document.setContent(content);

      document = client.storeDocument(document);

      panel.setDocId(document.getDocId());
      panel.setVersion(document.getVersion());
      panel.setModified(false);
      mainPanel.updateActions();
      mainPanel.showStatus(panel);
    }
    catch (Exception ex)
    {
      panel.setName(oldName);
      panel.setDescription(oldDescription);
      JOptionPane.showMessageDialog(this, ex.getMessage(), "ERROR",
        JOptionPane.ERROR_MESSAGE);
    }
    finally
    {
      setVisible(false);
      dispose();
    }
  }//GEN-LAST:event_saveButtonActionPerformed

  private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
  {//GEN-HEADEREND:event_cancelButtonActionPerformed
    setVisible(false);
    dispose();
  }//GEN-LAST:event_cancelButtonActionPerformed


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton cancelButton;
  private javax.swing.JLabel descriptionLabel;
  private javax.swing.JTextField descriptionTextField;
  private javax.swing.JLabel docIdLabel;
  private javax.swing.JLabel docIdValueLabel;
  private javax.swing.JLabel executeRolesLabel;
  private javax.swing.JTextField executeRolesTextField;
  private javax.swing.JComboBox<String> languageComboBox;
  private javax.swing.JLabel languageLabel;
  private javax.swing.JLabel metadataLabel;
  private javax.swing.JScrollPane metadataScrollPane;
  private javax.swing.JTextArea metadataTextArea;
  private javax.swing.JLabel nameLabel;
  private javax.swing.JTextField nameTextField;
  private javax.swing.JCheckBox newVersionCheckBox;
  private javax.swing.JPanel propertiesPanel;
  private javax.swing.JLabel readRolesLabel;
  private javax.swing.JTextField readRolesTextField;
  private javax.swing.JButton saveButton;
  private javax.swing.JPanel southPanel;
  private javax.swing.JLabel typeLabel;
  private javax.swing.JLabel typeValueLabel;
  private javax.swing.JLabel updateRolesLabel;
  private javax.swing.JTextField updateRolesTextField;
  private javax.swing.JLabel urlLabel;
  private javax.swing.JLabel urlValueLabel;
  // End of variables declaration//GEN-END:variables
}
