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
package org.santfeliu.webapp.data;

import java.util.List;
import org.santfeliu.security.util.Credentials;
import org.matrix.news.NewDocument;
import org.matrix.news.NewsManagerPort;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.webapp.modules.news.NewsModuleBean;

/**
 *
 * @author blanquepa
 */
public class NewDocumentsQuery extends DataQuery<NewDocument>
{
  private String newId;
  private String docType;
  private Number firstResult;
  private Number maxResults;

  public NewDocumentsQuery(Credentials credentials)
  {
    super(credentials);
  }

  public NewDocumentsQuery newId(String newId)
  {
    this.newId = newId;
    return this;
  }
  
  public NewDocumentsQuery docType(String docType)
  {
    this.docType = docType;
    return this;
  }  
    
  @Override
  public DataQuery<NewDocument> eq(String field, Object value)
  {
    return this;
  }
    
  @Override
  public NewDocumentsQuery orderBy(String field, boolean descending)
  {
    return this;
  }
  
  @Override
  public NewDocumentsQuery firstResult(Number firstResult)
  {
    this.firstResult = firstResult;
    return this;
  }
  
  @Override
  public NewDocumentsQuery maxResults(Number maxResults)
  {
    this.maxResults = maxResults;
    return this;
  }
  
  @Override
  public List<NewDocument> execute()
  {
    return execute(false);
  }
  
  public List<NewDocument> execute(boolean runAsAdmin)
  {              
    String userId = runAsAdmin ? 
      MatrixConfig.getProperty("adminCredentials.userId") : 
      credentials.getUserId();
    String password = runAsAdmin ?
      password = MatrixConfig.getProperty("adminCredentials.password") :
      credentials.getPassword();
    
    NewsManagerPort port = NewsModuleBean.getPort(userId, password);
    
    return port.findNewDocuments(newId, docType);
  }
}
