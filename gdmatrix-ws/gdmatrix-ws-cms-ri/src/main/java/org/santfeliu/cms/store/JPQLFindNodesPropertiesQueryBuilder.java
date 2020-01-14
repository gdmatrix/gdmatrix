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
package org.santfeliu.cms.store;

import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author lopezrj
 */
public class JPQLFindNodesPropertiesQueryBuilder
  extends FindNodesPropertiesQueryBuilder
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
    appendWorkspaceIdFilter(whereBuffer);
    appendNodeIdFilter(whereBuffer);

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
    selectBuffer.append("SELECT p ");
    fromBuffer.append("FROM Property p");
  }

  private void appendWorkspaceIdFilter(StringBuilder buffer)
  {    
    if (workspaceIdList != null && workspaceIdList.size() > 0)
    {
      appendOperator(buffer, "AND");
      appendInOperator(buffer, "p.workspaceId", ":", "workspaceId",
        workspaceIdList);
    }
  }

  private void appendNodeIdFilter(StringBuilder buffer)
  {
    if (nodeIdList != null && nodeIdList.size() > 0)
    {
      appendOperator(buffer, "AND");
      appendInOperator(buffer, "p.nodeId", ":", "nodeId",
        nodeIdList);
    }
  }

  
}
