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
package org.santfeliu.signature.cms;

import java.io.BufferedInputStream;
import java.io.InputStream;
import javax.activation.DataHandler;
import org.matrix.doc.Content;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.doc.transform.TransformationManager;
import org.santfeliu.doc.transform.TransformationRequest;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.util.MatrixConfig;

/**
 *
 * @author realor
 */
@Deprecated
public class CMSLoader
{
  public String getCMSContentId(String id, String propertyName)
    throws Exception
  {
    String contentId = null;
    DocumentManagerClient client = getDocumentManagerClient();
    if ("contentId".equals(propertyName))
    {
      contentId = id;
    }
    else if ("docId".equals(propertyName))
    {
      String docId = id;
      Document document = client.loadDocument(docId, 0, ContentInfo.ID);
      contentId = DocumentUtils.getContentId(document);
    }
    else
    {
      // find by property
      Document document = 
        client.loadDocumentByName(null, propertyName, id, null, 0);
      contentId = DocumentUtils.getContentId(document);
    }
    return contentId;
  }

  public CMSData loadCMSByContentId(String contentId) throws Exception
  {
    DocumentManagerClient client = getDocumentManagerClient();
    Content content = client.loadContent(contentId);
    if (content == null || content.getData() == null)
      throw new Exception("Document not found");

    DataHandler dh;

    String contentType = content.getContentType();
    if ("application/pdf".equals(contentType))
    {
      TransformationRequest request = new TransformationRequest();
      request.setTargetContentType("application/p7m");
      dh = TransformationManager.transform(content, request);
    }
    else
    {
      dh = content.getData();
    }
    InputStream is = new BufferedInputStream(dh.getInputStream());
    return new CMSData(is);
  }

  private DocumentManagerClient getDocumentManagerClient()
    throws Exception
  {
    String userId = MatrixConfig.getProperty("adminCredentials.userId");
    String password = MatrixConfig.getProperty("adminCredentials.password");

    return new DocumentManagerClient(userId, password);
  }

  public static void main(String[] args)
  {
    try
    {
      CMSLoader l = new CMSLoader();
      System.out.println(l.loadCMSByContentId("29decc99-dd79-4223-80cb-c4d4d238f6a3"));
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
