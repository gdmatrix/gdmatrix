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
package org.santfeliu.cases.service;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.apache.commons.lang.StringUtils;
import org.matrix.cases.CaseConstants;
import org.matrix.dic.Property;
import org.matrix.security.SecurityConstants;

/**
 *
 * @author blanquepa
 */
public class JPAFindInterventionsQueryBuilder extends FindInterventionsQueryBuilder
{
  private boolean containsOrderBy = false;
  private String dynamicOrderBy;

  @Override
  public Query getQuery(EntityManager em)
  {
    StringBuilder buffer = new StringBuilder();

    appendMainStatement(buffer);
    appendRolesFilter(buffer);
    appendCaseIdFilter(buffer);
    appendIntIdFilter(buffer);
    appendIntTypeIdFilter(buffer);
    appendPersonIdFilter(buffer);
    appendCommentsFilter(buffer);
    appendDateFilter(buffer);
    appendPropertiesFilter(buffer);
    appendOrderByExpression(buffer);

    System.out.println(buffer);
    
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

  protected void appendMainStatement(StringBuilder buffer)
  {
    if (isCounterQuery())
      buffer.append("SELECT count(i) FROM Intervention i LEFT JOIN i.intType t");
    else
    {
      if (dynamicOrderBy != null)
      {
        buffer.append("SELECT i FROM Intervention i JOIN FETCH i.intType t, InterventionProperty ip");
        appendOperator(buffer, "AND");
        buffer.append("ip.id = i.intId");
      }
      else
        buffer.append("SELECT i FROM Intervention i LEFT JOIN i.intType t");
    }
  }

  protected void appendIntTypeIdFilter(StringBuilder buffer)
  {
    String intTypeId = filter.getIntTypeId();
    if (intTypeId != null && intTypeId.length() > 0)
    {
      appendOperator(buffer, "AND");
      buffer.append("t.typePath like :intTypeId");

      if (intTypeId != null && intTypeId.trim().length() > 0)
        intTypeId = filter.getIntTypeId() + "%";
      else intTypeId = null;
      parameters.put("intTypeId", intTypeId);
    }
  }
  
  protected void appendPersonIdFilter(StringBuilder buffer)
  {
    String personId = filter.getPersonId();
    if (personId != null && personId.length() > 0)
    {
      appendOperator(buffer, "AND");
      buffer.append("i.personId = :personId");
      parameters.put("personId", personId);
    }    
  }

  protected void appendCommentsFilter(StringBuilder buffer)
  {
    String comments = filter.getComments();
    if (comments != null && comments.length() > 0)
    {
      appendOperator(buffer, "AND");
      buffer.append("lower(i.comments) like :comments");
      parameters.put("comments", comments.toLowerCase());
    }
  }

  protected void appendDateFilter(StringBuilder buffer)
  {
    String fromDate = filter.getFromDate();
    String toDate = filter.getToDate();
    String comparator = filter.getDateComparator();
    if (comparator != null && comparator.length() > 0)
    {
      if ("1".equals(comparator)) //data obertura
      {
        if (fromDate != null && fromDate.length() > 0)
        {
          appendOperator(buffer, "AND");
          buffer.append("i.startDate >= :fromDate");
          parameters.put("fromDate", fromDate);
        }
        if (toDate != null && toDate.length() > 0)
        {
          appendOperator(buffer, "AND");
          buffer.append("i.startDate <= :toDate");
          parameters.put("toDate", toDate);
        }
      }
      else if ("2".equals(comparator)) //data tancament
      {
        if (fromDate != null && fromDate.length() > 0)
        {
          appendOperator(buffer, "AND");
          buffer.append("i.endDate >= :fromDate");
          parameters.put("fromDate", fromDate);
        }
        if (toDate != null && toDate.length() > 0)
        {
          appendOperator(buffer, "AND");
          buffer.append("i.endDate <= :toDate");
          parameters.put("toDate", toDate);
        }
      }
      else if ("3".equals(comparator)) //obertes durant
      {
        if (fromDate != null && fromDate.length() > 0)
        {
          appendOperator(buffer, "AND");
          buffer.append("(i.endDate >= :fromDate OR i.endDate IS NULL)");
          parameters.put("fromDate", fromDate);
        }
        if (toDate != null && toDate.length() > 0)
        {
          appendOperator(buffer, "AND");
          buffer.append("i.startDate <= :toDate");
          parameters.put("toDate", toDate);
        }
      }
    }
  }

  protected void appendIntIdFilter(StringBuilder buffer)
  {
    List<String> intIds = filter.getIntId();
    if (intIds != null && intIds.size() > 0)
    {
      appendOperator(buffer, "AND");
      buffer.append("(");
      for (int i = 0; i < intIds.size(); i++)
      {
        if (i != 0) buffer.append(" OR ");
        buffer.append("i.intId=:intId").append(i);
        parameters.put("intId" + i, intIds.get(i));
      }
      buffer.append(")");
    }
  }
  
  protected void appendCaseIdFilter(StringBuilder buffer)
  {
    String caseId = filter.getCaseId();
    if (!StringUtils.isBlank(caseId))
    {
      appendOperator(buffer, "AND");
      buffer.append("(");
      buffer.append("i.caseId=:caseId");
      parameters.put("caseId", caseId);
      buffer.append(")");
    }
  }  

  protected void appendRolesFilter(StringBuilder buffer)
  {
    if (roles.isEmpty())
        roles.add(SecurityConstants.EVERYONE_ROLE);

    if (!roles.contains(CaseConstants.CASE_ADMIN_ROLE))
    {
      appendOperator(buffer, "AND");

      buffer.append(
        "(exists (select tacl.typeId from Case ct, AccessControl tacl where "
          + "ct.caseId = i.caseId " +
        "and upper(tacl.typeId) = upper(ct.caseTypeId) and tacl.action = 'Read' and ");
      appendInOperator(buffer, "tacl.roleId", ":", "userRole", roles);
      buffer.append(")");

      buffer.append(" OR ");

      buffer.append(
        "exists (select cacl.caseId from Case cc, CaseAccessControl cacl where " +
        "cc.caseId = i.caseId and cacl.caseId = cc.caseId and cacl.action = 'Read' and ");
      appendInOperator(buffer, "cacl.roleId", ":", "userRole", roles);
      buffer.append("))");
    }
  }

  protected void appendPropertiesFilter(StringBuilder buffer)
  {
    List<Property> properties = filter.getProperty();
    for (int i = 0; i < properties.size(); i++)
    {
      Property p = properties.get(i);
      appendPropertyFilter(buffer, p.getName(), p.getValue(), "p" + i);
    }
  }

  protected void appendPropertyFilter(StringBuilder buffer, String name,
    List<String> values, String tablePrefix)
  {
    if (values != null && values.size() > 0)
    {
      appendOperator(buffer, "AND");

      StringBuilder aux = new StringBuilder();
      aux.append("exists (select #.value from InterventionProperty # where " + "i.intId = #.id and #.name = '")
        .append(name).append("' and ");
//      appendInOperator(aux, "#.value", ":", name, values);
      appendLikeOperator(aux, "#.value", ":", tablePrefix + "_" + name, values);
      aux.append(")");
      
      String sAux = aux.toString();
      buffer.append(sAux.replaceAll("#", tablePrefix));
    }
  }
  
  protected void appendOrderByExpression(StringBuilder buffer)
  {
    buffer.append(" ORDER BY i.startDate desc, i.startTime desc, i.endDate desc, i.endTime desc ");
  }
  
  protected void parseOrderByExpression(String expression)
  {
    containsOrderBy =
      (expression != null && expression.toUpperCase().contains("ORDER BY"));

    if (containsOrderBy)
    {
      String[] props = expression.split(",");
      for (String prop : props)
      {
        prop = prop.replaceAll("ORDER BY", "");
        prop = prop.replaceAll(" desc ", "");
        prop = prop.trim();
        if (!prop.equalsIgnoreCase("caseId") && !prop.equalsIgnoreCase("caseTypeId")
         && !prop.equalsIgnoreCase("description") && !prop.equalsIgnoreCase("title")
         && !prop.equalsIgnoreCase("state") && !prop.equalsIgnoreCase("startDate")
         && !prop.equalsIgnoreCase("startTime") && !prop.equalsIgnoreCase("endDate")
         && !prop.equalsIgnoreCase("source") && !prop.equalsIgnoreCase("comments")
         && !prop.equalsIgnoreCase("creationDateTime") && !prop.equalsIgnoreCase("changeDateTime")
         && !prop.equalsIgnoreCase("changeUserId"))
        {
          dynamicOrderBy = prop;
          return;
        }
      }
    }
  }
}
