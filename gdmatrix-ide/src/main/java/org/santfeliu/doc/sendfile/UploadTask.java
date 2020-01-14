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
package org.santfeliu.doc.sendfile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import org.matrix.doc.DocumentConstants;
import org.santfeliu.swing.ProgressDialog;
import org.santfeliu.util.MimeTypeMap;

/**
 *
 * @author realor
 */
public class UploadTask extends BaseHeavyTask
{
  private String docId;
  private Map document;
  private File file;
  private boolean showOverlay = true;
  private int totalSent;
  private String command;
  private ProgressDialog progressDialog;
  private int CHUNK_TIME = 1000; // 1 second
  private int MIN_CHUNK_SIZE = 4096;
  private int MAX_CHUNK_SIZE = 2000000;
  private static File exploreDir;

  public UploadTask(SendFileApplet applet)
  {
    super(applet);
  }

  public String getDocId()
  {
    return docId;
  }

  public void setDocId(String docId)
  {
    this.docId = docId;
  }

  public File getFile()
  {
    return file;
  }

  public void setFile(File file)
  {
    this.file = file;
  }

  public String getCommand()
  {
    return command;
  }

  public void setCommand(String command)
  {
    this.command = command;
  }

  public boolean isShowOverlay()
  {
    return showOverlay;
  }

  public void setShowOverlay(boolean showOverlay)
  {
    this.showOverlay = showOverlay;
  }

  @Override
  public void execute()
  {
    boolean fileSent = false;
    System.out.println("UploadTask started");
    try
    {
      if (showOverlay) applet.showOverlay();
      if (docId == null) // new document
      {
        document = new HashMap();
      }
      else // update document, load previous properties
      {
        document = client.loadDocument(docId);
        if ("updateFile".equals(command))
          document.put("version", DocumentConstants.NEW_VERSION);
      }
      // set properties from page
      document.putAll(applet.getDocumentProperties());
      
      if (file == null)
      {
        invokeAndWait("askDocumentProperties");
      }

      if (file != null)
      {
        invokeAndWait("initProgressDialog");
        try
        {
          BufferedInputStream is = new BufferedInputStream(
            new FileInputStream(file));
          try
          {
            int chunkSize = MIN_CHUNK_SIZE;
            String contentId = UUID.randomUUID().toString();
            boolean end = false;
            while (!end && !progressDialog.isCanceled())
            {
              long millis0 = System.currentTimeMillis();
              int sent = client.sendBytes(contentId, is, chunkSize);
              long millis1 = System.currentTimeMillis();
              totalSent += sent;
              invokeLater("notifyProgress");
              long ellapsed = millis1 - millis0;
              chunkSize = getNextChunkSize(sent, ellapsed);
              end = (sent == 0);
            }
            if (!progressDialog.isCanceled())
            {
              invokeAndWait("registerDocument");
              // store document
              String contentType =
                MimeTypeMap.getMimeTypeMap().getContentType(file);
              if ("application/octet-stream".equals(contentType))
                contentType = null; // let service auto-detect content type
              document.put("contentId", contentId);
              document.put("contentType", contentType);
              document = client.storeDocument(document);
              docId = (String)document.get("docId");
              fileSent = true;
              invokeAndWait("progressCompleted");
              Thread.sleep(2000); // let the user see the end of transmission
            }
            else System.out.println("upload canceled");
          }
          finally
          {
            is.close();
          }
        }
        finally
        {
          invokeLater("closeProgressDialog");
        }
        if (fileSent && command != null)
        {
          applet.notifyEndOfTransmission(command, docId);
          showOverlay = false;
          // at this point, the applet is going down in few millis
        }
      }
    }
    catch (Exception ex)
    {
      showError(ex);
    }
    finally
    {
      if (showOverlay) applet.hideOverlay();
    }
  }

  public void askDocumentProperties()
  {
    //Step 1 - Choose file
    String dialogTitle = applet.getLocalizedMessage("ChooseFile");
    chooseFile(dialogTitle);
    if (file == null) return;

    //Step 2 - Set title
    String title = null;
    Object tit = document.get("title");
    if (tit instanceof String)
      title = (String)tit;
    else
    {
      List titleValues = (List)tit;
      if (titleValues != null) title = (String)titleValues.get(0);
    }

    if (title == null)
    {
      dialogTitle = applet.getLocalizedMessage("DocumentTitle");
      String filename = file.getName();
      if (filename != null && filename.contains("."))
        filename = filename.substring(0, filename.lastIndexOf("."));

      title = (String)JOptionPane.showInputDialog(null,
        applet.getLocalizedMessage("DocumentTitle"), dialogTitle,
        JOptionPane.QUESTION_MESSAGE, null, null, filename);
      if (title == null)
      {
        file = null;
        return;
      }
    }
    document.put("title", title);

    //Step 3 - Set document type
    String docTypeId = null;
    Object type = document.get("docTypeId");
    if (type instanceof String)
      docTypeId = (String)type;
    else
    {
      List typeValues = (List)type;
      if (typeValues != null) docTypeId = (String)typeValues.get(0);
    }

    if (docTypeId == null)
    {
      List<SelectItem> documentTypes = applet.getDocumentTypes();
      if (documentTypes.isEmpty())
      {
        document.put("docTypeId", "Document"); // default value
      }
      else if (documentTypes.size() == 1)
      {
        document.put("docTypeId", documentTypes.get(0).getId());
      }
      else
      {
        Collections.sort(documentTypes);
        Object[] docTypesArray = applet.getDocumentTypes().toArray();
        dialogTitle = applet.getLocalizedMessage("SelectTypes");
        SelectItem documentType = (SelectItem)JOptionPane.showInputDialog(
          null,
          applet.getLocalizedMessage("SelectTypes"),
          dialogTitle,
          JOptionPane.PLAIN_MESSAGE,
          null,
          docTypesArray,
          null);

        if (documentType == null)
        {
          file = null;
          return;
        }
        docTypeId = documentType.getId();
        document.put("docTypeId", docTypeId);
      }
    }

    //Step 4 - Set language
    String language = null;
    Object lang = document.get("language");
    if (lang instanceof String)
      language = (String)lang;
    else
    {
      List langValues = (List)lang;
      if (langValues != null) language = (String)langValues.get(0);
    }

    if (language == null)
    {
      dialogTitle = applet.getLocalizedMessage("SelectLanguages");
      SelectItem langItem = (SelectItem)JOptionPane.showInputDialog(
        null,
        applet.getLocalizedMessage("SelectLanguages"),
        dialogTitle,
        JOptionPane.PLAIN_MESSAGE,
        null,
        applet.getLanguages().toArray(),
        null);
      if (langItem == null)
      {
        file = null;
        return;
      }
      language = langItem.getId();
      document.put("language", language);
    }
  }

  public void chooseFile(String title)
  {
    UIManager.put("FileChooser.openButtonText",
      applet.getLocalizedMessage("FileChooserOpenButtonText"));
    UIManager.put("FileChooser.cancelButtonText",
      applet.getLocalizedMessage("FileChooserCancelButtonText"));
    UIManager.put("FileChooser.lookInLabelText",
      applet.getLocalizedMessage("FileChooserLookInLabelText"));
    UIManager.put("FileChooser.filesOfTypeLabelText",
      applet.getLocalizedMessage("FileChooserFilesOfTypeLabelText"));
    UIManager.put("FileChooser.fileNameLabelText",
      applet.getLocalizedMessage("FileChooserFileNameLabelText"));
    JFileChooser dialog = new JFileChooser();
    dialog.setDialogTitle(title);

    if (exploreDir != null)
      dialog.setCurrentDirectory(exploreDir);
    int result = dialog.showOpenDialog(null);
    if (result == JFileChooser.APPROVE_OPTION)
    {
      File selectedFile = dialog.getSelectedFile();
      exploreDir = selectedFile.getParentFile();
      if (!validFileExtension(selectedFile))
      {
        showError(applet.getLocalizedMessage("InvalidFileExtension") +
          " (" + applet.getLocalizedMessage("ValidExtensions") + ": " +
          applet.getParameter("validExtensions") + ")");
      }
      else if (!validFileSize(selectedFile))
      {
        showError(applet.getLocalizedMessage("InvalidFileSize") +
          " (" + applet.getLocalizedMessage("MaxFileSize") + ": " +
          applet.getParameter("maxFileSize") + ")");
      }
      else
      {
        file = dialog.getSelectedFile();
      }
    }
  }

  public void initProgressDialog()
  {
    String title = applet.getLocalizedMessage("Uploading");
    if (docId != null) title += " " + docId;
    progressDialog = new ProgressDialog(title, 400, 120);
    progressDialog.setMinimum(0);
    progressDialog.setMaximum((int)file.length());
    progressDialog.setStatus(applet.getLocalizedMessage("Uploading") + "...");
    progressDialog.setLocationRelativeTo(null);
    progressDialog.setVisible(true);
  }

  public void notifyProgress()
  {
    progressDialog.setValue(totalSent);
    progressDialog.setStatus(applet.getLocalizedMessage("Uploading") + " (" +
      getSizeString(totalSent) + " / " + getSizeString(file.length()) + ")...");
  }

  public void registerDocument()
  {
    progressDialog.setStatus(applet.getLocalizedMessage("Registering") + "...");
  }

  public void progressCompleted()
  {
    progressDialog.setValue((int)file.length());
    progressDialog.setStatus(applet.getLocalizedMessage("SendingOk"));
    progressDialog.setCancelEnabled(false);
  }

  public void closeProgressDialog()
  {
    progressDialog.setVisible(false);
    progressDialog.dispose();
  }

  private int getNextChunkSize(int sent, long ellapsed)
  {
    if (ellapsed <= 0) ellapsed = 1;
    int chunkSize = (int)((sent * CHUNK_TIME) / ellapsed);
    if (chunkSize > MAX_CHUNK_SIZE || chunkSize < 0) chunkSize = MAX_CHUNK_SIZE;
    else if (chunkSize < MIN_CHUNK_SIZE) chunkSize = MIN_CHUNK_SIZE;
    return chunkSize;
  }

  private boolean validFileExtension(File selectedFile)
  {
    Set validExtensions = applet.getValidExtensions();
    if (validExtensions.size() > 0)
    {
      String filename = selectedFile.getName();
      int index = filename.lastIndexOf(".");
      if (index != -1)
      {
        String extension = filename.substring(index + 1).toLowerCase();
        return validExtensions.contains(extension);
      }
      return false;
    }
    return true;
  }

  private boolean validFileSize(File selectedFile)
  {
    int maxFileSize = applet.getMaxFileSize();
    return maxFileSize == 0 || maxFileSize >= (int)selectedFile.length();
  }
}
