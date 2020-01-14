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
package org.santfeliu.doc.swing.action;

import java.awt.Cursor;
import java.awt.event.ActionEvent;

import java.io.File;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.activation.FileTypeMap;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.matrix.doc.DocumentConstants;

import org.matrix.doc.Content;
import org.matrix.doc.Document;

import org.santfeliu.doc.client.UUIDFileProvider;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.doc.file.MimeTypeAppAssociator;
import org.santfeliu.doc.file.OSMimeTypeAppAssociator;
import org.santfeliu.doc.file.PropertiesMimeTypeAppAssociator;
import org.santfeliu.doc.swing.AppChooserDialog;
import org.santfeliu.doc.swing.DocumentBasePanel;
import org.santfeliu.swing.Utilities;
import org.santfeliu.util.FileConsumer;
import org.santfeliu.util.MimeTypeMap;
import org.santfeliu.util.ResourceLoader;

/**
 *
 * @author unknown
 */
public class ExecuteDocumentAction extends AbstractAction
{
  private DocumentBasePanel documentPanel;
  public static final String OPEN = "open";
  public static final String OPEN_WITH = "openWith";
  public static final String EDIT = "edit";
  public static final String EDIT_WITH = "editWith";
  
  private String action;
  private boolean forceAppSelection;
  private String tempDirectory = System.getProperty("user.home") + "/foextemp/";
  
  public ExecuteDocumentAction(DocumentBasePanel documentPanel, String name)
  {
    this.documentPanel = documentPanel;
    this.putValue(Action.NAME, name);
    
    if (documentPanel.getLocalizedText(OPEN).equals(name) || documentPanel.getLocalizedText(OPEN_WITH).equals(name))
      this.action = MimeTypeAppAssociator.OPEN;
    else if (documentPanel.getLocalizedText(EDIT).equals(name) || documentPanel.getLocalizedText(EDIT_WITH).equals(name))
      this.action = MimeTypeAppAssociator.EDIT;
      
    if (documentPanel.getLocalizedText(EDIT_WITH).equals(name) || documentPanel.getLocalizedText(OPEN_WITH).equals(name))
      forceAppSelection = true;
  }

  public void actionPerformed(ActionEvent e)
  {
    String uuid = documentPanel.getDocUUID();
    documentPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    UUIDFileProvider uuidFileProvider = null;
    if (documentPanel.getWsDirectoryURL() != null)
      uuidFileProvider = new UUIDFileProvider(documentPanel.getWsDirectoryURL());
    else
      uuidFileProvider = new UUIDFileProvider(documentPanel.getWsdlLocation());
    ResourceLoader.requestResource(uuid, uuidFileProvider,
      new FileConsumer()
      {
        @Override
        public void fileLoadCompleted(String UUID, File file)
        {
          try
          {
            boolean executeCommand = true;
            boolean makeDefault = false;

            documentPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            String mimeType = getFileMimeType(file);

            MimeTypeAppAssociator associator =
              new PropertiesMimeTypeAppAssociator();
            String appPath =
              associator.getApplicationPath(mimeType, action);

            if (appPath == null && !forceAppSelection)
            {
              try
              {
                MimeTypeAppAssociator osAssociator =
                  new OSMimeTypeAppAssociator();
                appPath = osAssociator.getApplicationPath(mimeType,
                  action);
                if (appPath != null) makeDefault = true;
              }
              catch (Throwable t)
              {
                //If OSAssociator fails application must continue
                t.printStackTrace();
                System.out.println("OS MimeType Associator ignored");
              }
            }

            //Show Application chooser dialog
            if (appPath == null || forceAppSelection)
            {
              String app = getApplication(appPath);
              AppChooserDialog appChooser =
                openAppChooser(app);
              String selectedOption = appChooser.getSelectedOption();
              executeCommand = (selectedOption != null);
              appPath = appChooser.getAppPath();
              makeDefault = appChooser.isDefaultChecked();
            }

            if (executeCommand)
            {
              if (documentPanel.isDocumentLocked() &&
                !documentPanel.isDocumentLockedByUser())
                file.setReadOnly();
              String docPath =  file.getAbsolutePath();
              System.out.println("appPath: " + appPath);
              System.out.println("docPath: " + docPath);
              String cmdargs[] = getCommand(appPath, docPath);
              System.out.print("Invoking: ");
              for (String cmd : cmdargs)
              {
                System.out.print(cmd);
                System.out.print(" ");
              }
              System.out.println();

              if (makeDefault)
                associator.setApplicationPath(mimeType, action, appPath);

              String docId = documentPanel.getDocId();
              String language = documentPanel.getDocLanguage();
              long previousLastMod = file.lastModified();

              //Execute app
              Process process = Runtime.getRuntime().exec(cmdargs);
              System.out.println("Waiting application to be closed");
              process.waitFor();
              System.out.println("Application closed");

              if (previousLastMod != file.lastModified())
                saveInDocumentManager(file, docId, language);
            }
          }
          catch (Exception ex)
          {
            ex.printStackTrace();
            documentPanel.showError(ex);
          }
        }

      @Override
        public void loadFailed(String resourceRef, Exception ex)
        {
          System.out.println("load failed");
          documentPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }

      @Override
        public void loadAborted(String resourceRef)
        {
          System.out.println("load aborted");
          documentPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }, false);
  }
  
  @Override
  public boolean isEnabled()
  {
    return (documentPanel.existsDocument() && (MimeTypeAppAssociator.OPEN.equals(action) || 
      !documentPanel.isDocumentLocked() || 
      (documentPanel.isDocumentLockedByUser() && documentPanel.existsDocument())));
  }
  
  
  //Private
  private String getFileMimeType(File file)
  {
    FileTypeMap typeMap = MimeTypeMap.getDefaultFileTypeMap();
    return typeMap.getContentType(file);
  }  
  
  private AppChooserDialog openAppChooser(String defaultAppPath)
  {
    AppChooserDialog appChooser = new AppChooserDialog();
    Utilities.centerWindow(documentPanel, appChooser);
    if (defaultAppPath != null)
      appChooser.setAppPath(defaultAppPath);
    else
      appChooser.setExploreOnlyMode();
    appChooser.setVisible(true);
    return appChooser;
  }
  
  private void saveInDocumentManager(File file, String docId, String language)
    throws Exception
  {
    FileDataSource dataSource = new FileDataSource(file);
    DataHandler dh = new DataHandler(dataSource);

    DocumentManagerClient client = documentPanel.getClient();
    System.out.println(documentPanel.getUsername());

    if (documentPanel.existsDocument())
    {
      Document document = new Document();
      document.setDocId(docId);
      document.setVersion(Integer.valueOf(documentPanel.getDocVersion()));
      document.setIncremental(true);
      Content content = new Content();
      content.setData(dh);
      content.setContentType(dataSource.getContentType());
      content.setLanguage(documentPanel.getDocLanguage());
      document.setContent(content);
      client.storeDocument(document);
    }
    documentPanel.loadDocument(docId, DocumentConstants.LAST_VERSION);
  }
  
  private String[] getCommand(String appPath, String docPath)
  {
    if (appPath != null)
    {
      String token = " ";
      if (appPath.indexOf("%1") < 0 && appPath.indexOf("%U") < 0 && 
        appPath.indexOf("%u") < 0)
        appPath = appPath + token + "%1";

      String[] result = appPath.split(token);
      for (int i = 0; i < result.length; i++) 
      {
        String element = result[i];
        element = element.replace("%1", docPath);
        element = element.replace("%U", docPath);
        element = element.replace("%u", docPath);
        result[i] = element;
        System.out.println(result[i]);
      }
      
      return result;
    }
    
    return null;    
  }
  
  private String getApplication(String appPath)
  {
    String result = null;
    if (appPath != null)
    {
      String token;
    
      if (appPath.indexOf("\"") >= 0) token = "\"";
      else token = " ";
    
      String[] split = appPath.trim().split(token);
      result = ("".equals(split[0].trim()) ? split[1] : split[0]);
    }
    return result;
  }

}
