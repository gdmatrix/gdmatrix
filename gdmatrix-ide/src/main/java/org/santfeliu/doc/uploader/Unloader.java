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

/**
 *
 * @author realor
 */
import org.santfeliu.matrix.ide.DocumentUploaderPanel;
import java.util.List;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import org.matrix.cases.CaseDocumentFilter;
import org.matrix.cases.CaseDocumentView;
import org.matrix.cases.CaseManagerPort;
import org.santfeliu.doc.client.DocumentManagerClient;

public class Unloader extends SwingWorker<List<DocumentInfo>, DocumentInfo>
{
  DocumentUploaderPanel uploader;
  int docCount;
  int successCount;
  int errorCount;
  int publishedCount;
  DocumentManagerClient docClient;
  CaseManagerPort casePort;

  public Unloader(DocumentUploaderPanel uploader, DocumentManagerClient docClient,
    CaseManagerPort casesPort)
  {
    this.uploader = uploader;
    this.docClient = docClient;
    this.casePort = casesPort;
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
        unloadDocument(docInfo);
        successCount++;
      }
      catch (Exception ex)
      {
        UploadInfo uploadInfo = docInfo.getFile().getUploadInfo();
        uploadInfo.setError(ex.toString());
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
      FileInfo fileInfo = chunks.get(i).getFile();
      String state = fileInfo.getState();
      if (fileInfo.getUploadInfo().getError() != null)
      {
        state = "! " + state;
      }
      documentsTableModel.setValueAt(state, publishedCount + i, 0);
      publishedCount++;
    }
    JProgressBar progressBar = uploader.getProgressBar();
    progressBar.setValue(publishedCount);
    int percentCompleted = (int)(100 * progressBar.getPercentComplete());
    progressBar.setString("Unloading..." + percentCompleted + "%");
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
    uploader.setStatus(message + " " + successCount + " files unloaded. " +
       errorCount + " errors.");
    uploader.setButtonsEnabled(true);

    // deferred repaint
    uploader.showStatusPanel(200);
  }

  private void unloadDocument(DocumentInfo docInfo) throws Exception
  {
    FileInfo fileInfo = docInfo.getFile();
    UploadInfo uploadInfo = fileInfo.getUploadInfo();

    String docId = uploadInfo.getDocId();
    String caseId = uploadInfo.getCaseId();
    if (caseId != null)
    {
      CaseDocumentFilter filter = new CaseDocumentFilter();
      filter.setCaseId(caseId);
      filter.setDocId(docId);
      filter.setMaxResults(1);
      List<CaseDocumentView> caseDocs = casePort.findCaseDocumentViews(filter);
      if (!caseDocs.isEmpty())
      {
        casePort.removeCaseDocument(caseDocs.get(0).getCaseDocId());
      }
    }

    if (docId != null)
    {
      docClient.removeDocument(docId, 0);
      successCount++;
      uploadInfo.delete();
    }

    Thread.sleep(200);
  }
}
