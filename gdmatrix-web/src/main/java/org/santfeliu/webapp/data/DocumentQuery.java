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

import org.matrix.dic.Property;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.OrderByProperty;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.webapp.modules.doc.DocModuleBean;

/**
 *
 * @author blanquepa
 */
public class DocumentQuery extends DataQuery<Document>
{
  DocumentFilter filter;

  public DocumentQuery(Credentials credentials)
  {
    super(credentials);
    filter = new DocumentFilter();
  }

  public DocumentQuery title(String title)
  {
    filter.setTitle(title);
    return this;
  }

  public DocumentQuery startDate(String startDate)
  {
    filter.setStartDate(startDate);
    return this;
  }

  public DocumentQuery endDate(String endDate)
  {
    filter.setEndDate(endDate);
    return this;
  }

  @Override
  public DataQuery<Document> eq(String field, Object value)
  {
    Property property = new Property();
    property.setName(field);
    property.getValue().add(String.valueOf(value));
    filter.getProperty().add(property);
    return this;
  }
  
  public DocumentQuery docId(String docId)
  {
    filter.getDocId().add(docId);
    return this;
  }  
     
  @Override
  public DocumentQuery orderBy(String field, boolean descending)
  {
    var orderBy = new OrderByProperty();
    orderBy.setName(field);
    orderBy.setDescending(descending);
    filter.getOrderByProperty().add(orderBy);
    return this;
  }

  @Override
  public DocumentQuery firstResult(Number firstResult)
  {
    filter.setFirstResult(firstResult.intValue());
    return this;
  }
  
  @Override
  public DocumentQuery maxResults(Number maxResults)
  {
    filter.setMaxResults(maxResults.intValue());
    return this;
  }
  
  @Override
  public List<Document> execute()
  {  
    DocumentManagerClient client = DocModuleBean
        .getClient(credentials.getUserId(), credentials.getPassword());
      return client.findDocuments(filter); 
  }  
}
