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
package org.santfeliu.doc.uploader;

import org.santfeliu.matrix.ide.DocumentUploaderPanel;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.activation.DataHandler;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import org.matrix.cases.CaseDocument;
import org.matrix.cases.CaseDocumentFilter;
import org.matrix.cases.CaseManagerPort;
import org.matrix.dic.DictionaryConstants;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.util.FileDataSource;

/**
 *
 * @author real
 */
public class Uploader extends SwingWorker<List<DocumentInfo>, DocumentInfo>
{    
  DocumentUploaderPanel uploader;
  int docCount;
  int successCount;
  int errorCount;
  int publishedCount;
  int maxImageSize;
  boolean removeFiles;
  DocumentManagerClient docClient;
  CaseManagerPort casePort;

  public Uploader(DocumentUploaderPanel uploader, DocumentManagerClient docClient,
    CaseManagerPort casesPort)
  {
    this.uploader = uploader;
    this.docClient = docClient;
    this.casePort = casesPort;
  }

  public boolean isRemoveFiles()
  {
    return removeFiles;
  }

  public void setRemoveFiles(boolean removeFiles)
  {
    this.removeFiles = removeFiles;
  }

  public int getMaxImageSize()
  {
    return maxImageSize;
  }

  public void setMaxImageSize(int maxImageSize)
  {
    this.maxImageSize = maxImageSize;
  }
  
  @Override
  protected List<DocumentInfo> doInBackground() throws Exception
  {
    List<DocumentInfo> documents = uploader.getDocuments();
    docCount = 0;
    while (docCount < documents.size() && !isCancelled())
    {
      DocumentInfo docInfo = documents.get(docCount);
      try
      {
        uploadDocument(docInfo);
        successCount++;
      }
      catch (Exception ex)
      {
        UploadInfo uploadInfo = docInfo.getFile().getUploadInfo();
        uploadInfo.setError(ex);
        uploadInfo.write();
        errorCount++;
      }
      publish(docInfo);
      docCount++;
    }
    return documents;
  }

  @Override
  protected void process(List<DocumentInfo> chunks)
  {
    DefaultTableModel documentsTableModel = uploader.getDocumentsTableModel();
    for (int i = 0; i < chunks.size(); i++)
    {
      String state = "";
      FileInfo fileInfo = chunks.get(i).getFile();
      if (removeFiles)
      {
        state = fileInfo.getUploadInfo().getError() == null ?
          "UPLOADED" : "ERROR";
      }
      else
      {
        state = fileInfo.getState();
        if (fileInfo.getUploadInfo().getError() != null)
        {
          state = "! " + state;
        }
      }
      documentsTableModel.setValueAt(state, publishedCount, 0);
      publishedCount++;
    }
    JProgressBar progressBar = uploader.getProgressBar();
    progressBar.setValue(publishedCount);
    int percentCompleted = (int)(100 * progressBar.getPercentComplete());
    progressBar.setString("Uploading..." + percentCompleted + "%");
  }

  @Override
  public void done()            
  {
    String message;
    if (isCancelled())
    {       
      message = "Cancelled.";
    }
    else
    {
      message = "Done.";
    }
    uploader.setStatus(message + " " + successCount + " files uploaded. " + 
      errorCount + " errors.");
    uploader.setButtonsEnabled(true);
    
    // deferred repaint
    uploader.showStatusPanel(200);
  }

  private void uploadDocument(DocumentInfo docInfo) throws Exception
  {
    FileInfo fileInfo = docInfo.getFile();
    UploadInfo uploadInfo = fileInfo.getUploadInfo();
    
    // upload
    File fileToUpload = fileInfo.getFile();
    if (fileInfo.isImage() && maxImageSize > 0)
    {
      ImageProcessor processor = new ImageProcessor(fileInfo.getFile());
      processor.setMaxSize(maxImageSize);
      fileToUpload = processor.processImageAndSave();
    }
    String docId = uploadInfo.getDocId();
    Document document = new Document();
    document.setDocId(docId);
    Map metadata = docInfo.getMetadata();
    List list = new ArrayList(metadata.keySet());
    for (Object key : list)
    {
      if (metadata.get(key) == null)
      {
        metadata.remove(key);
      }
    }
    DocumentUtils.setProperties(document, metadata);
    Content content = new Content();
    content.setData(new DataHandler(new FileDataSource(fileToUpload)));
    document.setContent(content);
    document = docClient.storeDocument(document);

    uploadInfo.setDocId(document.getDocId());
    uploadInfo.setLastModified(fileInfo.getLastModified());

    String caseId = (String)metadata.get("caseId");
    if (caseId != null)
    {
      String caseDocTypeId = (String)metadata.get("caseDocTypeId");
      if (caseDocTypeId == null)
      {
        caseDocTypeId = DictionaryConstants.CASE_DOCUMENT_TYPE;
      }
      CaseDocumentFilter filter = new CaseDocumentFilter();
      filter.setCaseId(caseId);
      filter.setDocId(docId);
      if (casePort.countCaseDocuments(filter) == 0)
      {
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setCaseId(caseId);
        caseDocument.setCaseDocTypeId(caseDocTypeId);
        caseDocument.setDocId(document.getDocId());
        casePort.storeCaseDocument(caseDocument);
        uploadInfo.setCaseId(caseId);
      }
    }

    if (removeFiles)
    {
      fileInfo.getFile().delete();
    }
    else
    {
      uploadInfo.write();
    }
    Thread.sleep(200);
  }
}
