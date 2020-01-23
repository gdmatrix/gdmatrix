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
package org.santfeliu.matrix.client.cmd.doc;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.table.TableModel;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.matrix.dic.Property;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentConstants;
import org.matrix.doc.State;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.matrix.client.ui.util.SelectItem;
import org.santfeliu.matrix.client.ui.doc.DocumentFrame;
import org.santfeliu.matrix.client.ui.MessageWindow;
import org.santfeliu.matrix.client.ui.SaveDialog;

/**
 *
 * @author blanquepa
 */
public class SendDocumentCommand extends DocumentCommand
{
  public static final String NO_FILE = "NO_FILE";
  
  private static File exploreDir;
  
  
  @Override
  public void doWork() throws Exception
  {
    init();
    
    document = new Document();
    
    if (file == null)
    {
      boolean doSave = askDocumentProperties();
      if (doSave && file != null)
      {
        if (!validateFileSize(file))
          throw new Exception("INVALID_FILE_SIZE");
        
        MessageWindow messageWindow = new MessageWindow();
        messageWindow.showWindow("Desant document...");
        try
        {
          Logger.getLogger(getClass().getName()).info("Saving document");
          saveDocument();
          Logger.getLogger(getClass().getName()).info("Done " + document.getContent().getContentId());
          properties.put(RESULT, document.getDocId());
        }
        catch (Exception ex)
        {
          messageWindow.hideWindow();
          JOptionPane.showMessageDialog(null, "S'ha produït un error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }        
        finally
        {
          messageWindow.hideWindow();
        }
      }
      else
      {
        properties.put(EXCEPTION, NO_FILE);
      }
    }
    
    release();
  }
  
  protected void release()
  {
    properties.remove("docTypes");
  }
  
  protected boolean askDocumentProperties() throws InterruptedException
  {
    boolean doSave = false;
    DocumentFrame documentFrame = new DocumentFrame(this);
    
    //DocId & version
    if (document.getDocId() != null)
    {
      documentFrame.getDocIdTextField().setText(document.getDocId());
      documentFrame.getVersionTextField().setText(String.valueOf(document.getVersion()));
    }

    
    //Title
    String title = getCurrentTitle();
    if (title != null)
      documentFrame.getTitleTextArea().setText(title);

    //Types
    String defaultDocTypeId = (String)properties.get(DocumentConstants.DOCTYPEID);        
    String docTypeId = getCurrentTypeId();
    documentFrame.getTypeComboBox().addItem(new SelectItem("", ""));
    for (SelectItem item : getDocTypeSelectItems())
    {
      documentFrame.getTypeComboBox().addItem(item);
      if ((docTypeId != null && docTypeId.equals(item.getId())) || 
          (defaultDocTypeId != null && defaultDocTypeId.equals(item.getId())))
        documentFrame.getTypeComboBox().setSelectedItem(item);
    }
    
    String selected = ((SelectItem)documentFrame.getTypeComboBox().getSelectedItem()).getId();
    if (docTypeId != null && !docTypeId.equals(selected))
    {
      SelectItem item = new SelectItem(docTypeId, docTypeId);
      documentFrame.getTypeComboBox().addItem(item);
      documentFrame.getTypeComboBox().setSelectedItem(item);
    }
    
    //Languages
    String language = getCurrentLanguage();    
    for (SelectItem item : getLanguageSelectItems())
    {
      documentFrame.getLanguageComboBox().addItem(item);
      if (language != null && language.equals(item.getId()))
        documentFrame.getLanguageComboBox().setSelectedItem(item);
    }   
    
    //State
    State state = getCurrentState() != null ? getCurrentState() : State.COMPLETE;      
    for (SelectItem item : getStateSelectItems())
    {
      documentFrame.getStateComboBox().addItem(item);
      if (state != null && state.name().equals(item.getId()))
        documentFrame.getStateComboBox().setSelectedItem(item);
    }
    
    if (document.getDocId() != null)
    {
      //Properties
      List<Property> docProps = document.getProperty();
      documentFrame.setPropertyTableModel(docProps);

      //Audit
      String captureDate;
      String changeDate;
      try
      {
        captureDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                .format(new SimpleDateFormat("yyyyMMddHHmmss")
                        .parse(document.getCaptureDateTime()));
        documentFrame.getCaptureDateTextField().setText(captureDate);      
        changeDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                .format(new SimpleDateFormat("yyyyMMddHHmmss")
                        .parse(document.getChangeDateTime()));
        documentFrame.getChangeDateTextField().setText(changeDate);      
      }
      catch (java.text.ParseException ex)
      {
      }
      
      documentFrame.getCaptureUserTextField().setText(document.getCaptureUserId());
      documentFrame.getChangeUserTextField().setText(document.getChangeUserId());
      documentFrame.getLockedByTextField().setText(document.getLockUserId());
      if (document.getLockUserId() != null)
      {
        documentFrame.getLockUnlockButton().doClick();
        if (!document.getLockUserId().equals(properties.get("userId")))
          documentFrame.getLockUnlockButton().setEnabled(false);
      }
    }

    documentFrame.showFrame();

    //wait for user interaction
    int result = documentFrame.waitForResponse();
    try
    {
      if (result == SaveDialog.YES)
      {
        this.document.setTitle(documentFrame.getTitleTextArea().getText());
        SelectItem selectedTypeId = 
          (SelectItem) documentFrame.getTypeComboBox().getSelectedItem();
        this.document.setDocTypeId(selectedTypeId.getId());
        SelectItem selectedLanguage = 
          (SelectItem) documentFrame.getLanguageComboBox().getSelectedItem();
        this.document.setLanguage(selectedLanguage.getId());
        SelectItem selectedState = 
          (SelectItem)documentFrame.getStateComboBox().getSelectedItem();
        this.document.setState(State.fromValue(selectedState.getId()));
        if (this.document.getDocId() != null)
        {
          if ((documentFrame.getLockedByTextField().getText() == null 
            || documentFrame.getLockedByTextField().getText().length() == 0) &&
            this.document.getLockUserId() != null)
          {
            docClient.unlockDocument(this.document.getDocId(), this.document.getVersion());
          }
          else if ((documentFrame.getLockedByTextField().getText() != null 
            && documentFrame.getLockedByTextField().getText().length() > 0) &&
            (this.document.getLockUserId() == null || this.document.getLockUserId().length() == 0))
          {
            docClient.lockDocument(this.document.getDocId(), this.document.getVersion());
          }
        }
        
        TableModel tableModel = documentFrame.getPropertiesTable().getModel();
        if (tableModel instanceof DocumentFrame.PropertyTableModel)
        {
          this.document.getProperty().clear();
          this.document.getProperty()
            .addAll(((DocumentFrame.PropertyTableModel)tableModel).getValues());
        }
        
        this.file = documentFrame.getFile();
        doSave = true;
      }
    }
    finally
    {
      documentFrame.setVisible(false);
      documentFrame.dispose();
    }
    return doSave;
  }
  
  public void chooseFile(String title)
  {
    ResourceBundle bundle = 
      java.util.ResourceBundle.getBundle("org.santfeliu.matrix.client.ui.doc.FileChooserBundle"); // NOI18N
    
    UIManager.put("FileChooser.openButtonText",
      bundle.getString("FileChooserOpenButtonText"));
    UIManager.put("FileChooser.cancelButtonText",
      bundle.getString("FileChooserCancelButtonText"));
    UIManager.put("FileChooser.lookInLabelText",
      bundle.getString("FileChooserLookInLabelText"));
    UIManager.put("FileChooser.filesOfTypeLabelText",
      bundle.getString("FileChooserFilesOfTypeLabelText"));
    UIManager.put("FileChooser.fileNameLabelText",
      bundle.getString("FileChooserFileNameLabelText"));
    JFileChooser dialog = new JFileChooser();
    dialog.setDialogTitle(title);

    if (exploreDir != null)
      dialog.setCurrentDirectory(exploreDir);
    int result = dialog.showOpenDialog(null);
    if (result == JFileChooser.APPROVE_OPTION)
    {
      File selectedFile = dialog.getSelectedFile();
      exploreDir = selectedFile.getParentFile();
      file = dialog.getSelectedFile();
    }
  } 
  
  private List<SelectItem> getLanguageSelectItems()
  {
    List languages = new ArrayList();
    String[] languageArray = new String[]
      {"ca", "es", "en", "fr", "de", "it", "pt", "ru", "ar", "zh", "ro", "bg"};

    for (String lang : languageArray)
    {
      SelectItem selectItem = new SelectItem(lang,
        new Locale(lang).getDisplayLanguage().toUpperCase());
      languages.add(selectItem);
    }
    languages.add(new SelectItem("%%", "UNIVERSAL"));    
    
    return languages;
  }
  
  private List<SelectItem> getStateSelectItems()
  {
    ResourceBundle bundle = 
      java.util.ResourceBundle.getBundle("org.santfeliu.matrix.client.ui.doc.DocumentFrameBundle"); // NOI18N
    
    List states = new ArrayList();
    State[] statesArray = new State[]
      {State.DRAFT, State.COMPLETE, State.DELETED, State.RECORD};

    for (State state : statesArray)
    {
      SelectItem selectItem = new SelectItem(state.name(),
        bundle.getString("DocumentFrame.state." + state.value()));
      states.add(selectItem);
    }
    
    return states;
  }
  
  private List<SelectItem> getDocTypeSelectItems()
  {
    List<SelectItem> result = new ArrayList();
    Object types = properties.get("docTypes");
    if (types != null)
    {
      JSONParser parser = new JSONParser();
      try 
      {
        JSONObject map = (JSONObject)parser.parse((String)types);
        boolean hasFavorites = false;
        for (Object key : map.keySet())
        {
          SelectItem item = new SelectItem((String)key, (String) map.get(key));
          if (item.isFavorite() && !hasFavorites)
          {
            hasFavorites = true;
            result.add(new SelectItem("*", "------------------------"));
          }
          result.add(item);
        }
        Collections.sort(result);
      }
      catch (ParseException ex) 
      {
        result.add(new SelectItem("Document", "Document"));
      }
    }
    return result;
  }
  
  private String getCurrentTitle()
  {
    String title = (String) properties.get("title");
    if (title == null && document != null && document.getTitle() != null)
      title = document.getTitle();
    return title;
  }
  
  private String getCurrentTypeId()
  {
    String docTypeId = (String) properties.get("docTypeId");
    if (docTypeId == null && document != null && document.getDocTypeId() != null)
      docTypeId = document.getDocTypeId();
    return docTypeId;
  }
  
  private String getCurrentLanguage()
  {
    String language = (String) properties.get("language");
    if (language == null && document != null && document.getLanguage() != null)
      language = document.getLanguage();
    return language;
  }
  
  private State getCurrentState()
  {
    State state = null;
    String sstate = (String) properties.get("state");
    if (sstate != null)
      state = State.fromValue(sstate);
    if (state == null && document != null && document.getState() != null)
      state = document.getState();
    return state;
  }
  
  protected boolean validateFileSize(File file)
  {
    String sMaxFileSize = (String) properties.get("maxFileSize");
    if (sMaxFileSize != null)
    {
      long maxSizeAllowed = DocumentUtils.getSize(sMaxFileSize);
      if (file.length() > maxSizeAllowed)
        return false;
    }
    return true;
  }
  
}
