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
package org.santfeliu.doc.util.authcopy;

import java.util.List;
import javax.activation.DataHandler;
import org.matrix.dic.Type;
import org.matrix.doc.Content;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.DocumentFilter;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.doc.client.CachedDocumentManagerClient;
import org.santfeliu.util.MatrixConfig;


/**
 *
 * @author blanquepa
 */
public class DocumentManagerSource implements Source
{
  public static final String SIGID = "sigId";
  
  private Document document;
  
  public DocumentManagerSource()
  {
  }
  
  public Document getDocument(String sigId)
  {
    String username = MatrixConfig.getProperty("adminCredentials.userId");
    String password = MatrixConfig.getProperty("adminCredentials.password");    
    CachedDocumentManagerClient client = new CachedDocumentManagerClient(username, password);
    if (document == null)
    {
      DocumentFilter filter = new DocumentFilter();
      DictionaryUtils.setProperty(filter, SIGID, sigId);
      List<org.matrix.doc.Document> documents = client.findDocuments(filter);
      if (documents != null && !documents.isEmpty())
      {
        org.matrix.doc.Document doc = client.loadDocument(documents.get(0).getDocId(), 0, ContentInfo.ALL);
        document = createAuthcopyDocument(doc);
      }
    }
    
    return document;
  }
  
  private Document createAuthcopyDocument(org.matrix.doc.Document doc)
  {
    Document result = new Document();
    result.setTitle(doc.getTitle());
    Type type = TypeCache.getInstance().getType(doc.getDocTypeId());
    result.setDocType(type.getDescription());
    Content content = doc.getContent();
    if (content != null)
    {
      result.setContentType(content.getContentType());
      if (content.getData() != null)
      {
        DataHandler dataHandler = content.getData();
        if (dataHandler != null)
        {
          result.setData(dataHandler);
        }
      }
    }
    result.getProperties().addAll(doc.getProperty());
    return result;
  }
 
}
