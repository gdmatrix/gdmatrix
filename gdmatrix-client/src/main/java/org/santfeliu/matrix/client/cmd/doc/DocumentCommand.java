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
import java.net.MalformedURLException;
import java.net.URL;
import javax.activation.DataHandler;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentConstants;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.matrix.client.Command;
import org.santfeliu.util.FileDataSource;
import org.santfeliu.util.MimeTypeMap;

/**
 *
 * @author blanquepa
 */
public abstract class DocumentCommand extends Command
{
  public static final String DEFAULT_MIME_TYPE = "application/octet-stream";
  
  protected DocumentManagerClient docClient;
  protected Document document;
  protected File file;
  
  protected void init() throws MalformedURLException
  {
    String wsdir = (String)properties.get("wsdir");
    String userId = (String)properties.get("userId");
    String password = (String)properties.get("password");

    URL wsurl = new URL(wsdir);
    docClient = new DocumentManagerClient(wsurl, userId, password);
  }  
  
  protected void saveDocument()
  {
    if (file != null)
    {
      Content content = new Content();
      content.setData(new DataHandler(new FileDataSource(file)));
      String contentType =
        MimeTypeMap.getMimeTypeMap().getContentType(file);
      if (DEFAULT_MIME_TYPE.equals(contentType))
        contentType = null; 
      content.setContentType(contentType);
      document.setContent(content);        
      document.setVersion(DocumentConstants.NEW_VERSION);
    }
    document = docClient.storeDocument(document);
  }
  
  public void setFile(File file)
  {
    this.file = file;
  }
  
//  protected DocumentManagerClient getDocumentManagerClient(URL wsUrl,
//    String userId, String password)
//  {
//    javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
//    new javax.net.ssl.HostnameVerifier()
//    {
//      public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) 
//      {
//        if (hostname.equals("localhost")) 
//          return true;
//        else
//          return false;
//      }
//    });
//    return new DocumentManagerClient(wsUrl, userId, password);
//  }
}
