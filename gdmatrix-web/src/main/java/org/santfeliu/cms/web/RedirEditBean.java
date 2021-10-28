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
package org.santfeliu.cms.web;

import java.io.InputStream;
import java.io.Serializable;
import javax.activation.DataHandler;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentConstants;
import org.santfeliu.doc.client.CachedDocumentManagerClient;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.doc.web.DocumentConfigBean;
import org.santfeliu.faces.FacesBean;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.MemoryDataSource;

/**
 *
 * @author lopezrj-sf
 */
public class RedirEditBean extends FacesBean implements Serializable
{  
  private String fileDocId;
  private String fileContent;  
  
  public RedirEditBean()
  {
    
  }

  public String getFileDocId() 
  {
    if (fileDocId == null)
    {
      String docId = 
        MatrixConfig.getProperty("org.santfeliu.web.filter.ShortcutFilter.url");
      if (docId != null)
      {
        docId = docId.trim();
        if (docId.contains("/documents/"))
        {
          docId = docId.substring(docId.indexOf("/documents/") + 11);
        }
        if (!docId.isEmpty())
        {
          try
          {
            Integer.parseInt(docId);
            fileDocId = docId;
          }
          catch (NumberFormatException ex)
          { 
          }
        }
      }            
    }
    return fileDocId;
  }

  public void setFileDocId(String fileDocId) 
  {
    this.fileDocId = fileDocId;
  }

  public String getFileContent() 
  {
    if (fileContent == null)
    {
      try 
      {
        String docId = getFileDocId();
        CachedDocumentManagerClient docClient = DocumentConfigBean.getClient();
        Document document = docClient.loadDocument(docId, 0);
        DataHandler dh = DocumentUtils.getContentData(document);
        long size = document.getContent().getSize();
        int iSize = (int)size;
        InputStream is = dh.getInputStream();
        byte[] byteArray = new byte[iSize];
        is.read(byteArray);
        fileContent = new String(byteArray);        
      }       
      catch (Exception ex) 
      {
      }
    }
    return fileContent;
  }

  public void setFileContent(String fileContent) 
  {
    this.fileContent = fileContent;
  }
  
  public String save()
  {
    try
    {
      Document document;
      CachedDocumentManagerClient docClient = DocumentConfigBean.getClient();
      MemoryDataSource ds = new MemoryDataSource(getFileContent().getBytes(),
        "data", "text/plain");
      Content content = new Content();
      content.setContentType("text/plain");
      content.setData(new DataHandler(ds));
      document = new Document();          
      document.setContent(content);        
      document.setDocId(getFileDocId());
      document.setIncremental(true);
      document.setVersion(DocumentConstants.NEW_VERSION);
      docClient.storeDocument(document);
      info("REDIR_FILE_UPDATED");      
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  public String revert()
  {
    fileContent = null;
    info("REDIR_FILE_REVERTED");
    return null;
  }

}
