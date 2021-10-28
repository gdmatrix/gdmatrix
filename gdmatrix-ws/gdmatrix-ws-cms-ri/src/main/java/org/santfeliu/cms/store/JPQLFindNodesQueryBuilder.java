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

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.matrix.cms.Property;

/**
 *
 * @author lopezrj
 */
public class JPQLFindNodesQueryBuilder extends FindNodesQueryBuilder
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
    appendNameFilter(whereBuffer);
    appendChangeDateTime1Filter(whereBuffer);
    appendChangeDateTime2Filter(whereBuffer);
    appendIndexFilter(whereBuffer);
    appendWorkspaceIdFilter(whereBuffer);
    appendNodeIdFilter(whereBuffer);
    appendPathNodeIdFilter(whereBuffer);
    appendParentNodeIdFilter(whereBuffer);
    appendChangeUserIdFilter(whereBuffer);
    appendPropertiesFilter(whereBuffer);

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
      selectBuffer.append("SELECT COUNT(n) ");
    }
    else
    {
      selectBuffer.append("SELECT n ");
    }
    fromBuffer.append("FROM Node n");
  }

  private void appendWorkspaceIdFilter(StringBuilder buffer)
  {
    List<String> workspaceIds = filter.getWorkspaceId();
    if (workspaceIds.size() > 0)
    {
      appendOperator(buffer, "AND");
      appendInOperator(buffer, "n.workspaceId", ":", "workspaceId", workspaceIds);
    }
  }

  private void appendNodeIdFilter(StringBuilder buffer)
  {
    List<String> nodeIds = filter.getNodeId();
    if (nodeIds.size() > 0)
    {
      appendOperator(buffer, "AND");
      appendInOperator(buffer, "n.nodeId", ":", "nodeId", nodeIds);
    }
  }  
  
  private void appendPathNodeIdFilter(StringBuilder buffer)
  {
    List<String> pathNodeIds = filter.getPathNodeId();
    if (pathNodeIds.size() > 0)
    {
      appendOperator(buffer, "AND");
      appendLikeOperator(buffer, "n.path", ":", "path", 
        stringListToPathItemList(pathNodeIds));
    }
  }

  private void appendParentNodeIdFilter(StringBuilder buffer)
  {
    List<String> parentNodeIds = filter.getParentNodeId();
    List<String> auxParentNodeIds = new ArrayList();
    boolean rootSearch = false;
    for (String parentNodeId : parentNodeIds)
    {
      if ("-".equals(parentNodeId))
      {
        rootSearch = true;
      }
      else
      {
        auxParentNodeIds.add(parentNodeId);
      }
    }
    boolean parentSearch = (auxParentNodeIds.size() > 0);
    if (rootSearch && !parentSearch)
    {      
      appendOperator(buffer, "AND");
      buffer.append("(n.parentNodeId IS NULL)");
    }
    else if(!rootSearch && parentSearch)
    {
      appendOperator(buffer, "AND");
      appendInOperator(buffer, "n.parentNodeId", ":", "parentNodeId",
        auxParentNodeIds);
    }
    else if(rootSearch && parentSearch)
    {
      appendOperator(buffer, "AND");
      buffer.append("((n.parentNodeId IS NULL) OR ");
      appendInOperator(buffer, "n.parentNodeId", ":", "parentNodeId",
        auxParentNodeIds);
      buffer.append(")");
    }
/*
    if (parentNodeIds.size() > 0)
    {
      appendOperator(buffer, "AND");
      appendInOperator(buffer, "n.parentNodeId", ":", "parentNodeId", parentNodeIds);
      if (parentNodeIds.contains("-"))
      {
        buffer.append(" OR n.parentNodeId IS NULL)");
      }
    }
 */
  }

  private void appendChangeUserIdFilter(StringBuilder buffer)
  {
    List<String> userIds = filter.getChangeUserId();
    if (userIds.size() > 0)
    {
      appendOperator(buffer, "AND");
      appendInOperator(buffer, "n.changeUserId", ":", "changeUserId", userIds);
    }
  }

  private void appendNameFilter(StringBuilder buffer)
  {
    String name = filter.getName();
    if (name != null && name.trim().length() > 0)
    {
      appendOperator(buffer, "AND");
      buffer.append("(UPPER(n.name) LIKE :name)");
      parameters.put("name", name.toUpperCase());
    }
  }

  private void appendChangeDateTime1Filter(StringBuilder buffer)
  {
    String changeDateTime1 = filter.getChangeDateTime1();
    if (changeDateTime1 != null && changeDateTime1.trim().length() > 0)
    {
      appendOperator(buffer, "AND");
      buffer.append("(n.changeDateTime >= :changeDateTime1)");
      parameters.put("changeDateTime1", changeDateTime1);
    }
  }

  private void appendChangeDateTime2Filter(StringBuilder buffer)
  {
    String changeDateTime2 = filter.getChangeDateTime2();
    if (changeDateTime2 != null && changeDateTime2.trim().length() > 0)
    {
      appendOperator(buffer, "AND");
      buffer.append("(n.changeDateTime < :changeDateTime2)");
      parameters.put("changeDateTime2", changeDateTime2);
    }
  }

  private void appendIndexFilter(StringBuilder buffer)
  {
    appendOperator(buffer, "AND");
    buffer.append("(n.index >= 0)");
  }

  private void appendPropertiesFilter(StringBuilder buffer)
  {
    List<Property> properties = filter.getProperty();
    for (int i = 0; i < properties.size(); i++)
    {
      Property p = properties.get(i);
      appendPropertyFilter(buffer, p.getName(), p.getValue(), "p", i);
    }
  }

  private void appendPropertyFilter(StringBuilder buffer, String name,
    List<String> values, String prefix, int index)
  {
    StringBuilder aux = new StringBuilder();
    String tablePrefix = prefix + index;    
    aux.append(" AND EXISTS (SELECT # FROM Property # WHERE #.nodeId = n.nodeId "
      + "AND #.workspaceId = n.workspaceId AND ");
    if (filter.isPropertyCaseSensitive())
    {
      aux.append("#.name LIKE :propertyName");
      aux.append(index);
      parameters.put("propertyName" + index, name);
    }
    else
    {
      aux.append("upper(#.name) LIKE :propertyName");
      aux.append(index);
      parameters.put("propertyName" + index, name.toUpperCase());
    }    
    if (values != null && values.size() > 0)
    {
      aux.append(" AND ");
      if (filter.isPropertyCaseSensitive())
      {
        appendLikeOperator(aux, "#.value", ":", "propertyValue" + index + "_",
          values);
      }
      else
      {
        appendLikeOperator(aux, "UPPER(#.value)", ":", 
          "propertyValue" + index + "_", stringListToUpperCase(values));
      }
    }
    aux.append(")");
    String sAux = aux.toString();
    buffer.append(sAux.replaceAll("#", tablePrefix));
  }

  private void appendOrderByExpression(StringBuilder whereBuffer)
  {
    whereBuffer.append(" ORDER BY n.workspaceId, n.nodeId");
  }

  private List<String> stringListToUpperCase(List<String> list)
  {
    List<String> result = new ArrayList();
    for (String s : list)
    {
      result.add(s.toUpperCase());
    }
    return result;
  }

  private List<String> stringListToPathItemList(List<String> list)
  {
    List<String> result = new ArrayList();
    for (String s : list)
    {
      result.add("%/" + s + "/%");      
    }
    return result;
  }
  
}
