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
package org.santfeliu.doc.store.docjpa;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.santfeliu.doc.store.FindSummariesQueryBuilder;

/**
 *
 * @author blanquepa
 */
public class OraFindSummariesQueryBuilder extends FindSummariesQueryBuilder
{
  @Override
  public Query getQuery(EntityManager em) throws Exception
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append("SELECT d.docid, d.version, " +
      "snippet('ctxsys.cnt_internal_idx', i.uuid, ?searchExpression) as summary " +
      "FROM dom_document d , cnt_internal i " +
      "WHERE d.contentid = i.uuid " +
      "and i.data is not null");
    appendKeys(buffer);
    buffer.append(" UNION ");
    buffer.append("SELECT d.docid, d.version, " +
      "snippet('ctxsys.cnt_external_idx', e.uuid, ?searchExpression) as summary " +
      "FROM dom_document d , cnt_external e " +
      "WHERE d.contentid = e.uuid " +
      "and e.url is not null");
    appendKeys(buffer);

    parameters.put("searchExpression", searchExpression);

    Query query = em.createNativeQuery(buffer.toString());
    setParameters(query);

    return query;
  }

  protected void appendKeys(StringBuilder buffer)
  {
    if (buffer != null)
    {
      buffer.append(" AND (");
      for (int i = 0; i < keys.size(); i++)
      {
        DBDocumentPK key = keys.get(i);
        if (i != 0) buffer.append(" or ");
        buffer.append("(docid = ?docid" + String.valueOf(i));
        buffer.append(" and version = ?version" + String.valueOf(i) + ")");
        parameters.put("docid" + String.valueOf(i), 
          key.getDocId());
        parameters.put("version" + String.valueOf(i),
          String.valueOf(key.getVersion()));
      }
      buffer.append(")");
    }
  }
}
