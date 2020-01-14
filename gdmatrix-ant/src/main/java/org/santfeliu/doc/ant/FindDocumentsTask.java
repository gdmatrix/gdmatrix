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
package org.santfeliu.doc.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import javax.activation.DataHandler;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Sequential;
import org.matrix.cases.CaseDocumentFilter;
import org.matrix.cases.CaseDocumentView;
import org.matrix.cases.CaseManagerPort;
import org.matrix.cases.CaseManagerService;
import org.matrix.doc.Content;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.DocumentManagerPort;
import org.matrix.doc.DocumentManagerService;
import org.matrix.util.WSEndpoint;
import org.santfeliu.ant.db.Statement;
import org.santfeliu.ant.ws.WSTask;

/**
 *
 * @author lopezrj
 */
public class FindDocumentsTask extends WSTask
{
  //Input
  private String docIdVar;
  private String caseIdVar;
  private File file;
  //Output
  private String docVar;
  //Nested elements
  private Statement metadataFilter;
  private Statement contentFilter;
  private Sequential forEachDocument;

  public File getFile()
  {
    return file;
  }

  public void setFile(File file)
  {
    this.file = file;
  }

  public String getDocIdVar()
  {
    return docIdVar;
  }

  public void setDocIdVar(String docIdVar)
  {
    this.docIdVar = docIdVar;
  }

  public String getCaseIdVar()
  {
    return caseIdVar;
  }

  public void setCaseIdVar(String caseIdVar)
  {
    this.caseIdVar = caseIdVar;
  }

  public String getDocVar()
  {
    return docVar;
  }

  public void setDocVar(String docVar)
  {
    this.docVar = docVar;
  }

  public void addMetadataFilter(Statement metadataFilter)
  {
    this.metadataFilter = metadataFilter;
  }

  public void addContentFilter(Statement contentFilter)
  {
    this.contentFilter = contentFilter;
  }

  public void addForEachDocument(Sequential forEachDocument)
  {
    this.forEachDocument = forEachDocument;
  }

  @Override
  public void execute()
  {
    if (forEachDocument == null)
      throw new BuildException("Nested element 'forEachDocument' is required");    
    
    DocumentFilter docFilter = new DocumentFilter();
    if (caseIdVar != null) // search documents from caseId
    {
      Object caseId = getVariable(caseIdVar);
      if (caseId != null)
      {
        WSEndpoint casesEndpoint = getEndpoint(CaseManagerService.class);
        CaseManagerPort casesPort = casesEndpoint.getPort(CaseManagerPort.class,
          getUsername(), getPassword());

        CaseDocumentFilter caseDocFilter = new CaseDocumentFilter();
        caseDocFilter.setCaseId(String.valueOf(caseId));
        List<CaseDocumentView> views =
          casesPort.findCaseDocumentViews(caseDocFilter);
        if (views.isEmpty()) return; //no documents
        for (CaseDocumentView view : views)
        {
          docFilter.getDocId().add(view.getDocument().getDocId());
        }
      }
    }
    else if (docIdVar != null)
    {
      Object docId = getVariable(docIdVar);
      if (docId != null) docFilter.getDocId().add(String.valueOf(docId));
    }
    if (metadataFilter != null)
    {
      String parsedSQL = getProject().replaceProperties(
        metadataFilter.getSql());
      docFilter.setMetadataSearchExpression(parsedSQL);
    }
    if (contentFilter != null)
    {
      String parsedSQL = getProject().replaceProperties(
        contentFilter.getSql());
      docFilter.setContentSearchExpression(parsedSQL);
    }

    WSEndpoint docEndpoint = getEndpoint(DocumentManagerService.class);
    DocumentManagerPort port = docEndpoint.getPort(DocumentManagerPort.class,
      getUsername(), getPassword());

    docFilter.setFirstResult(0);
    docFilter.setMaxResults(0);
    List<Document> documents = port.findDocuments(docFilter);
    for (Document document : documents)
    {
      document = port.loadDocument(document.getDocId(), 0, ContentInfo.METADATA);      
      setVariable(docVar, document);
      if (file != null)
      {
        try
        {
          Content content = document.getContent();
          if (content != null)
          {
            String contentId = content.getContentId();
            content = port.loadContent(contentId);
            writeFile(file, content.getData());
          }          
        }
        catch (Exception ex)
        {
          log("Error writing in FileOutputStream document " +
            document.getDocId(), Project.MSG_ERR);
        }
      }      
      forEachDocument.perform();
    }
    if (file != null) file.delete();
  }

  private void writeFile(File file, DataHandler dh) throws Exception
  {
    FileOutputStream fos = new FileOutputStream(file);
    dh.writeTo(fos);
    if (fos != null) fos.close();
  }

}
