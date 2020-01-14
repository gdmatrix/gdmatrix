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

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.matrix.forum.OrderBy;
import org.matrix.forum.ShowAnswered;
import org.matrix.forum.ShowVisible;

/**
 *
 * @author lopezrj
 */
public class JPQLFindQuestionsQueryBuilder extends FindQuestionsQueryBuilder
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
    StringBuilder buffer = new StringBuilder();

    StringBuilder selectBuffer = new StringBuilder();
    StringBuilder fromBuffer = new StringBuilder();
    StringBuilder whereBuffer = new StringBuilder();

    appendMainStatement(selectBuffer, fromBuffer);
    appendForumIdFilter(whereBuffer);
    appendQuestionIdFilter(whereBuffer);
    appendShowAnsweredFilter(whereBuffer);
    appendShowVisibleFilter(whereBuffer);
    appendCreationUserIdFilter(whereBuffer);

    if (!isCounterQuery())
    {
      appendOrderByExpression(whereBuffer);
    }

    buffer.append(selectBuffer);
    buffer.append(fromBuffer);
    buffer.append(whereBuffer);

    System.out.println(buffer.toString());
    
    Query query = em.createQuery(buffer.toString());
    setParameters(query);

    if (!isCounterQuery())
    {
      query.setFirstResult(filter.getFirstResult());
      int maxResults = filter.getMaxResults();
      if (maxResults > 0) query.setMaxResults(maxResults);
    }
    else
    {
      query.setFirstResult(0);
      query.setMaxResults(1);
    }    
    return query;
  }

  private void appendMainStatement(StringBuilder selectBuffer,
    StringBuilder fromBuffer)
  {
    if (isCounterQuery())
    {
      selectBuffer.append("SELECT COUNT(DISTINCT q.questionId) ");
    }
    else
    {
      selectBuffer.append("SELECT DISTINCT q ");
    }
    fromBuffer.append("FROM Question q LEFT JOIN q.answers a");
  }

  private void appendForumIdFilter(StringBuilder buffer)
  {
    // forumId is mandatory
    appendOperator(buffer, "AND");
    buffer.append("(q.forumId = :forumId) ");
    parameters.put("forumId", filter.getForumId());
  }

  private void appendQuestionIdFilter(StringBuilder buffer)
  {
    List<String> questionIds = filter.getQuestionId();
    if (questionIds.size() > 0)
    {
      appendOperator(buffer, "AND");
      appendInOperator(buffer, "q.questionId", ":", "questionId", questionIds);
    }
  }

  private void appendShowAnsweredFilter(StringBuilder buffer)
  {
    if (filter.getShowAnswered() != null)
    {
      appendOperator(buffer, "AND");
      buffer.append("((:showAnswered = 'Y' AND " +
       "(a.text IS NOT NULL AND (q.strVisible = 'Y' OR :userForumAdmin = 'Y')))" +
       " OR (:showAnswered = 'N' AND " +
       "(a.text IS NULL OR (q.strVisible = 'N' AND :userForumAdmin = 'N'))))");
      if (filter.getShowAnswered().equals(ShowAnswered.YES))
      {
        parameters.put("showAnswered", "Y");
      }
      else
      {
        parameters.put("showAnswered", "N");
      }
      parameters.put("userForumAdmin", userForumAdmin ? "Y" : "N");
    }
  }

  private void appendShowVisibleFilter(StringBuilder buffer)
  {
    appendOperator(buffer, "AND");
    buffer.append("(q.strVisible = :showVisible OR :showVisible IS NULL)");

    // Do not show invisible questions when user is not forum admin or
    // the question was not asked by him
    if (!userForumAdmin)
    {
      buffer.append(" AND (q.strVisible = 'Y' OR q.creationUserId = :userId) ");
      parameters.put("userId", userId);
    }    
    if (filter.getShowVisible() == null)
    {
      parameters.put("showVisible", null);
    }
    else if (ShowVisible.YES.equals(filter.getShowVisible()))
    {
      parameters.put("showVisible", "Y");
    }
    else
    {
      parameters.put("showVisible", "N");
    }
  }

  private void appendCreationUserIdFilter(StringBuilder buffer)
  {
    String creationUserId = filter.getCreationUserId();
    if (creationUserId != null)
    {
      appendOperator(buffer, "AND");
      buffer.append("(q.creationUserId = :creationUserId)");
      parameters.put("creationUserId", creationUserId);
    }
  }

  private void appendOrderByExpression(StringBuilder buffer)
  {
    if (filter.getOrderBy() != null)
    {
      if (filter.getOrderBy().equals(OrderBy.ACTIVITY))
      {
        buffer.append(" ORDER BY q.activityDateTime desc");
      }
      else if (filter.getOrderBy().equals(OrderBy.INPUTINDEX))
      {
        buffer.append(" ORDER BY q.inputIndex");
      }
      else if (filter.getOrderBy().equals(OrderBy.OUTPUTINDEX))
      {
        buffer.append(" ORDER BY q.outputIndex, q.inputIndex");
      }
      else if (filter.getOrderBy().equals(OrderBy.USER))
      {
        buffer.append(" ORDER BY q.creationUserId, q.inputIndex");
      }
    }
    else
    {
      buffer.append(" ORDER BY q.activityDateTime desc");
    }
  }
}
