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
package org.santfeliu.doc.store;

import java.util.List;
import org.santfeliu.doc.service.DocumentManager;
import org.santfeliu.doc.store.docjpa.DBDocumentPK;
import org.santfeliu.doc.store.docjpa.OraFindSummariesQueryBuilder;
import org.santfeliu.jpa.QueryBuilder;

/**
 *
 * @author blanquepa
 */
public abstract class FindSummariesQueryBuilder extends QueryBuilder
{
  protected String searchExpression;
  protected List<DBDocumentPK> keys;

  public static FindSummariesQueryBuilder getInstance()
  {
    FindSummariesQueryBuilder queryBuilder =
      (FindSummariesQueryBuilder)QueryBuilder.getInstance(DocumentManager.class, "findSummaries");
    if (queryBuilder == null)
      queryBuilder = new OraFindSummariesQueryBuilder();

    return queryBuilder;
  }

  public List<DBDocumentPK> getKeys()
  {
    return keys;
  }

  public void setKeys(List<DBDocumentPK> keys)
  {
    this.keys = keys;
  }

  public String getSearchExpression()
  {
    return searchExpression;
  }

  public void setSearchExpression(String searchExpression)
  {
    this.searchExpression = searchExpression;
  }
}
