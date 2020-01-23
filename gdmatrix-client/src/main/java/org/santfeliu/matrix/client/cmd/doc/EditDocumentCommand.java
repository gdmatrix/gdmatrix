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

import org.santfeliu.matrix.client.ui.SaveDialog;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.UUID;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import org.matrix.doc.Content;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.DocumentConstants;
import org.santfeliu.matrix.client.ui.MessageWindow;
import org.santfeliu.util.FileDataSource;
import org.santfeliu.util.IOUtils;
import org.santfeliu.util.MimeTypeMap;

/**
 *
 * @author realor
 */
public class EditDocumentCommand extends DocumentCommand
{
  private String docId;
//  private Document document;
//  private File file;
  private long lastModified;
//  private DocumentManagerClient docClient;
  private boolean editionWithLock;
  private String userId;
  
  public EditDocumentCommand()
  {
  }
  
  protected void init() throws MalformedURLException
  {
    super.init();
    docId = (String)properties.get("docId");
  }  
    
  @Override
  public void doWork() throws Exception
  {
    init();

    MessageWindow messageWindow = new MessageWindow();
    messageWindow.showWindow("Baixant document " + docId + "...");
    try
    {
      Logger.getLogger(getClass().getName()).info("loadDocument " + docId);
      document = docClient.loadDocument(docId, 
        DocumentConstants.LAST_VERSION, ContentInfo.METADATA);
      String lockUserId = document.getLockUserId();
      Logger.getLogger(getClass().getName()).info("LockUserId: " + lockUserId);
      if (lockUserId == null)
      {
        docClient.lockDocument(docId, 0);
        document.setLockUserId(lockUserId);
      }
      else if (!lockUserId.equals(userId))
      {
        throw new IOException("Locked by " + lockUserId);
      }
      Content content = document.getContent();
      content = docClient.loadContent(content.getContentId());
      String extension = 
        MimeTypeMap.getMimeTypeMap().getExtension(content.getContentType());
      String fileId = docId + "-" + UUID.randomUUID().toString();
      file = new File(getClient().getBaseDir(), fileId + "." + extension);
      IOUtils.writeToFile(content.getData(), file);
      lastModified = file.lastModified();
    }
    finally
    {
      messageWindow.hideWindow();
    }

    Desktop desktop = Desktop.getDesktop();
    try
    {
      desktop.edit(file);
    }
    catch(Exception ex)
    {
      desktop.open(file);
    }

    while (!isStopping())
    {
      try
      {
        boolean locked = isFileLocked(file);
        Logger.getLogger(getClass().getName()).info("Locked: " + locked);

        if (editionWithLock)
        {
          if (!locked)
          {
            stop();
          }
        }
        else if (locked)
        {
          this.editionWithLock = true;
          Logger.getLogger(getClass().getName()).info("Edition with lock.");
        }
        
        if (documentChanged())
        {
          messageWindow = new MessageWindow();
          messageWindow.showWindow("Desant document...");
          try
          {
            lastModified = file.lastModified();
            Logger.getLogger(getClass().getName()).info("Saving " + docId);
            Content content = new Content();
            content.setData(new DataHandler(new FileDataSource(file)));
            String contentType =
              MimeTypeMap.getMimeTypeMap().getContentType(file);
            if ("application/octet-stream".equals(contentType))
              contentType = null; // let service auto-detect content type
            content.setContentType(contentType);
            document.setContent(content);            
            document = docClient.storeDocument(document);
            Logger.getLogger(getClass().getName()).info("Done " + document.getContent().getContentId());
          }
          finally
          {
            messageWindow.hideWindow();
          }
          
          if (!editionWithLock)
          {
            SaveDialog saveDialog = new SaveDialog();
            saveDialog.showMessage("Has acabat l'edició del document?");
            int result = saveDialog.waitForResponse();
            if (result == SaveDialog.YES)
            {
              stop();
            }
          }
        }
        Thread.sleep(1000);
      }
      catch (InterruptedException ex)
      {        
      }
    }
    docClient.unlockDocument(docId, 0);
    Logger.getLogger(getClass().getName()).info("Edition terminated.");
  }
    
  private boolean isFileLocked(File file)
  {
    boolean locked = true;
    File renamedFile = new File(file.getName() + ".renamed");
    if (file.renameTo(renamedFile))
    {
      renamedFile.renameTo(file);
      locked = false;
    }
    return locked;
  }

  private boolean documentChanged()
  {
    return lastModified != file.lastModified();
  }
}
