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
package org.santfeliu.agenda.service;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.santfeliu.jpa.QueryBuilder;

/**
 *
 * @author lopezrj-sf
 */
public class FindThemesFromEventsQueryBuilder extends QueryBuilder
{
  private List<String> eventIdList;

  public List<String> getEventIdList()
  {
    return eventIdList;
  }

  public void setEventIdList(List<String> eventIdList)
  {
    this.eventIdList = eventIdList;
  }

  @Override
  public Query getQuery(EntityManager em) throws Exception
  {
    return createQuery(em);
  }
  
  public List<DBEventTheme> getResultList(EntityManager entityManager)
    throws Exception
  {
    Query query = createQuery(entityManager);
    return query.getResultList();
  }

  private Query createQuery(EntityManager em) throws Exception
  {
    if (eventIdList == null || eventIdList.isEmpty())
    {
      throw new Exception("agenda:ID_NULL");
    }
    
    StringBuilder buffer = new StringBuilder();
    StringBuilder selectBuffer = new StringBuilder();
    StringBuilder fromBuffer = new StringBuilder();
    StringBuilder whereBuffer = new StringBuilder();

    appendMainStatement(selectBuffer, fromBuffer);
    appendEventIdFilter(whereBuffer);

    buffer.append(selectBuffer);
    buffer.append(" ");
    buffer.append(fromBuffer);
    buffer.append(" ");
    buffer.append(whereBuffer);

    Query query = em.createQuery(buffer.toString());
    setParameters(query);

    return query;
  }  

  private void appendMainStatement(StringBuilder selectBuffer,
    StringBuilder fromBuffer)
  {
    selectBuffer.append("SELECT et");
    fromBuffer.append("FROM DBEventTheme et");
  }

  private void appendEventIdFilter(StringBuilder whereBuffer)
  {
    whereBuffer.append("WHERE ");
    appendInOperator(whereBuffer, "et.eventId", ":", "eventId", eventIdList);
  }
}
