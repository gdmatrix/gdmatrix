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
package org.santfeliu.forum.store;

import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author lopezrj
 */
public class JPQLFindForumsInfoQueryBuilder extends FindForumsInfoQueryBuilder
{
  @Override
  public boolean isNativeQuery()
  {
    return false;
  }

  @Override
  public Query getQuery(EntityManager em)
    throws Exception
  {
    parameters.clear();
    StringBuilder buffer = new StringBuilder();

    StringBuilder selectBuffer = new StringBuilder();
    StringBuilder fromBuffer = new StringBuilder();
    StringBuilder whereBuffer = new StringBuilder();

    appendMainStatement(selectBuffer, fromBuffer);
    appendForumIdFilter(whereBuffer);
    appendVisibilityFilter(whereBuffer);
    appendGroupByExpression(whereBuffer);
    
    buffer.append(selectBuffer);
    buffer.append(fromBuffer);
    buffer.append(whereBuffer);

    System.out.println(buffer.toString());
    
    Query query = em.createQuery(buffer.toString());
    setParameters(query);

    return query;
  }

  private void appendMainStatement(StringBuilder selectBuffer,
    StringBuilder fromBuffer)
  {
    if (QUESTIONS.equals(element))
    {
      selectBuffer.append("SELECT f.forumId, COUNT(DISTINCT q.questionId) ");
      fromBuffer.append("FROM Forum f LEFT JOIN f.questions q ");
    }
    else if (ANSWERS.equals(element))
    {
      selectBuffer.append("SELECT f.forumId, COUNT(DISTINCT a.answerId) ");
      fromBuffer.append("FROM Forum f LEFT JOIN f.questions q "
        + "LEFT JOIN q.answers a");
    }
    else if (PENDENT_QUESTIONS.equals(element))
    {
      selectBuffer.append("SELECT f.forumId, COUNT(DISTINCT q.questionId) ");
      fromBuffer.append("FROM Forum f LEFT JOIN f.questions q "
        + "LEFT JOIN q.answers a");
    }
  }

  private void appendForumIdFilter(StringBuilder buffer)
  {
    if (!forumIdList.isEmpty())
    {
      appendOperator(buffer, "AND");
      appendInOperator(buffer, "f.forumId", ":", "forumId", forumIdList);
    }
  }

  private void appendVisibilityFilter(StringBuilder buffer)
  {
    if (QUESTIONS.equals(element))
    {
      appendOperator(buffer, "AND");
      buffer.append("(q.strVisible = 'Y' OR q.creationUserId = :userId OR ");
      buffer.append(" :userSuperAdmin = 'Y' OR ");
      appendInOperator(buffer, "f.adminRoleId", ":", "adminRoleId", userRolesList);
      buffer.append(")");
      parameters.put("userId", userId);
      parameters.put("userSuperAdmin", userSuperAdmin ? "Y" : "N");
    }
    else if (ANSWERS.equals(element))
    {
      appendOperator(buffer, "AND");
      buffer.append("(q.strVisible = 'Y' AND a.text IS NOT NULL)");
    }
    else if (PENDENT_QUESTIONS.equals(element))
    {
      appendOperator(buffer, "AND");
      buffer.append("(q.creationUserId = :userId AND ");
      buffer.append(" (q.strVisible = 'N' OR a.answerId IS NULL)");
      buffer.append(")");
      parameters.put("userId", userId);
    }
  }

  private void appendGroupByExpression(StringBuilder buffer)
  {
    buffer.append(" GROUP BY f.forumId");
  }
}
