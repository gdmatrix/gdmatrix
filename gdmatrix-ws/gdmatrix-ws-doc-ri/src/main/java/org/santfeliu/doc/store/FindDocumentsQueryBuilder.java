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
import org.matrix.doc.DocumentFilter;
import org.santfeliu.doc.service.DocumentManager;
import org.santfeliu.doc.store.docjpa.OraFindDocumentsQueryBuilder;
import org.santfeliu.jpa.QueryBuilder;

/**
 *
 * @author blanquepa
 */
public abstract class FindDocumentsQueryBuilder extends QueryBuilder
{
  protected DocumentFilter filter;
  protected List<String> roles;
  protected boolean counterQuery;

  public static FindDocumentsQueryBuilder getInstance()
  {
    FindDocumentsQueryBuilder queryBuilder =
      (FindDocumentsQueryBuilder)QueryBuilder
        .getInstance(DocumentManager.class, "findDocuments");
    if (queryBuilder == null)
      queryBuilder = new OraFindDocumentsQueryBuilder();

    return queryBuilder;
  }

  public abstract boolean isNativeQuery();

  public boolean isCounterQuery()
  {
    return counterQuery;
  }

  public void setCounterQuery(boolean counterQuery)
  {
    this.counterQuery = counterQuery;
  }

  public DocumentFilter getFilter()
  {
    return filter;
  }

  public void setFilter(DocumentFilter filter)
  {
    this.filter = filter;
  }

  public List<String> getRoles()
  {
    return roles;
  }

  public void setRoles(List<String> roles)
  {
    this.roles = roles;
  }
}
