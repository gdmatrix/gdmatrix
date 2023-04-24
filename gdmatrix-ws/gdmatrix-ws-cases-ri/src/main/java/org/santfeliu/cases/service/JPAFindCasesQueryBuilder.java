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

import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.apache.commons.lang.StringUtils;
import org.matrix.cases.CaseConstants;
import org.matrix.dic.Property;
import org.matrix.security.SecurityConstants;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author blanquepa
 */
public class JPAFindCasesQueryBuilder extends FindCasesQueryBuilder
{
  private boolean containsOrderBy = false;
  private String dynamicOrderBy;

  @Override
  public Query getQuery(EntityManager em)
  {
    StringBuilder buffer = new StringBuilder();

    parseOrderByExpression(filter.getSearchExpression());
    appendMainStatement(buffer);
    appendRolesFilter(buffer);
    appendCaseIdFilter(buffer);
    appendCaseTypeIdFilter(buffer);
    appendDescriptionFilter(buffer);
    appendTitleFilter(buffer);
    appendStateFilter(buffer);
    appendDateFilter(buffer);
    appendClassIdFilter(buffer);
    appendPersonFilter(buffer);
    appendPropertiesFilter(buffer);
    appendSearchExpression(buffer);

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
      buffer.append("SELECT count(c) FROM Case c LEFT JOIN c.caseType t");
    else
    {
      if (dynamicOrderBy != null)
        buffer.append("SELECT c FROM Case c LEFT JOIN c.caseType t, CaseProperty cp");
      else
        buffer.append("SELECT c FROM Case c LEFT JOIN c.caseType t");
    }
  }

  protected void appendCaseTypeIdFilter(StringBuilder buffer)
  {
    String caseTypeId = filter.getCaseTypeId();
    if (caseTypeId != null && caseTypeId.length() > 0)
    {
      appendOperator(buffer, "AND");
      buffer.append("t.typePath like :caseTypeId");

      if (caseTypeId != null && caseTypeId.trim().length() > 0)
        caseTypeId = filter.getCaseTypeId() + "%";
      else caseTypeId = null;
      parameters.put("caseTypeId", caseTypeId);
    }
  }

  protected void appendDescriptionFilter(StringBuilder buffer)
  {
    String description = filter.getDescription();
    if (description != null && description.length() > 0)
    {
      appendOperator(buffer, "AND");
      buffer.append("lower(c.description) like :description");
      parameters.put("description", description.toLowerCase());
    }
  }

  protected void appendTitleFilter(StringBuilder buffer)
  {
    String title = filter.getTitle();
    if (title != null && title.length() > 0)
    {
      appendOperator(buffer, "AND");
      buffer.append("lower(c.title) like :title");
      parameters.put("title", title.toLowerCase());
    }
  }

  protected void appendStateFilter(StringBuilder buffer)
  {
    String state = filter.getState();
    if (state != null && state.length() > 0)
    {
      appendOperator(buffer, "AND");
      buffer.append("lower(c.state) = :state");
      parameters.put("state", state.toLowerCase());
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
          buffer.append("c.startDate >= :fromDate");
          parameters.put("fromDate", fromDate);
        }
        if (toDate != null && toDate.length() > 0)
        {
          appendOperator(buffer, "AND");
          buffer.append("c.startDate <= :toDate");
          parameters.put("toDate", toDate);
        }
      }
      else if ("2".equals(comparator)) //data tancament
      {
        if (fromDate != null && fromDate.length() > 0)
        {
          appendOperator(buffer, "AND");
          buffer.append("c.endDate >= :fromDate");
          parameters.put("fromDate", fromDate);
        }
        if (toDate != null && toDate.length() > 0)
        {
          appendOperator(buffer, "AND");
          buffer.append("c.endDate <= :toDate");
          parameters.put("toDate", toDate);
        }
      }
      else if ("3".equals(comparator)) //obertes durant
      {
        if (fromDate != null && fromDate.length() > 0)
        {
          appendOperator(buffer, "AND");
          buffer.append("(c.endDate >= :fromDate OR c.endDate IS NULL)");
          parameters.put("fromDate", fromDate);
        }
        if (toDate != null && toDate.length() > 0)
        {
          appendOperator(buffer, "AND");
          buffer.append("c.startDate <= :toDate");
          parameters.put("toDate", toDate);
        }
      }
    }
  }

  protected void appendCaseIdFilter(StringBuilder buffer)
  {
    List<String> caseIds = filter.getCaseId();
    if (caseIds != null && caseIds.size() > 0)
    {
      appendOperator(buffer, "AND");
      buffer.append("(");
      for (int i = 0; i < caseIds.size(); i++)
      {
        if (i != 0) buffer.append(" OR ");
        buffer.append("c.caseId=:caseId").append(i);
        parameters.put("caseId" + i, caseIds.get(i));
      }
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
        "(exists (select tacl.typeId from AccessControl tacl where " +
        "upper(tacl.typeId) = upper(c.caseTypeId) and tacl.action = 'Read' and ");
      appendInOperator(buffer, "tacl.roleId", ":", "userRole", roles);
      buffer.append(")");

      buffer.append(" OR ");

      buffer.append(
        "exists (select cacl.caseId from CaseAccessControl cacl where " +
        "cacl.caseId = c.caseId and cacl.action = 'Read' and ");
      appendInOperator(buffer, "cacl.roleId", ":", "userRole", roles);
      buffer.append("))");
    }
  }
  
  protected void appendPersonFilter(StringBuilder buffer)
  {
    if (filter.getPersonId() != null)
    {
      appendOperator(buffer, "AND");
      buffer.append(
        "exists (" +
          "select cp.caseId from CasePerson cp where " +
          "cp.caseId = c.caseId and cp.personId = :personId "); 
      
      if (filter.getPersonFlag() != null && "1".equals(filter.getPersonFlag()))
      {
        buffer.append(" and cp.startDate <= :today and (cp.endDate >= :today or cp.endDate is null) ");
        parameters.put("today", TextUtils.formatDate(new Date(), "yyyyMMdd"));
      }
      else if (filter.getPersonFlag() != null && "0".equals(filter.getPersonFlag()))
      {
        buffer.append(" and cp.endDate < :today ");
        parameters.put("today", TextUtils.formatDate(new Date(), "yyyyMMdd"));
      }
      else if (filter.getPersonFlag() != null && "2".equals(filter.getPersonFlag()))
      {
        buffer.append(" and cp.startDate > :today ");
        parameters.put("today", TextUtils.formatDate(new Date(), "yyyyMMdd"));
      }        
                
      buffer.append(")");
      parameters.put("personId", filter.getPersonId());
    }
  }  

  protected void appendClassIdFilter(StringBuilder buffer)
  {
    List<String> classIds = filter.getClassId();
    if (classIds != null && classIds.size() > 0)
    {
      appendPropertyFilter(buffer, "classId", classIds, "clsprop");
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
      aux.append("exists (select #.value from CaseProperty # where " + "c.caseId = #.id and #.name = '")
        .append(name).append("' and ");
//      appendInOperator(aux, "#.value", ":", name, values);
      appendOperator(aux, "#.value", ":", tablePrefix + "_" + name, values);
      aux.append(")");
      
      String sAux = aux.toString();
      buffer.append(sAux.replaceAll("#", tablePrefix));
    }
  }

  protected void appendSearchExpression(StringBuilder buffer)
  {
    String expression = filter.getSearchExpression();
//    containsOrderBy =
//      (expression == null || expression.toUpperCase().contains("ORDER BY"));
    if (expression != null && expression.length() > 0)
    {
      expression = expression.replaceAll("caseId", "c.caseId");
      expression = expression.replaceAll("caseTypeId", "c.caseTypeId");
      expression = expression.replaceAll("description", "upper(c.description)");
      expression = expression.replaceAll("title", "upper(c.title)");
      expression = expression.replaceAll("state", "c.state");
      expression = expression.replaceAll("startDate", "c.startDate");
      expression = expression.replaceAll("startTime", "c.startTime");
      expression = expression.replaceAll("endDate", "c.endDate");
      expression = expression.replaceAll("source", "c.source");
      expression = expression.replaceAll("comments", "upper(c.comments)");
      expression = expression.replaceAll("creationDateTime", "c.creationDateTime");
      expression = expression.replaceAll("creationUserId", "c.creationUserId");
      expression = expression.replaceAll("changeDateTime", "c.changeDateTime");
      expression = expression.replaceAll("changeUserId", "c.changeUserId");


      if (isCounterQuery() && containsOrderBy)
      { //clear orderBy
        expression = expression.substring(0,
          expression.toUpperCase().indexOf("ORDER BY", 0));
        containsOrderBy = false;
      }

      if (!StringUtils.isBlank(expression) && 
          !expression.toUpperCase().trim().startsWith("ORDER BY"))
      {
        appendOperator(buffer, "AND");
      }

      if (containsOrderBy && dynamicOrderBy != null)
      {
        appendOperator(buffer, "AND");
        buffer.append(" ").append("c.caseId = cp.id AND cp.name = :name");
        parameters.put("name", dynamicOrderBy);
        expression = expression.replaceAll(dynamicOrderBy, "upper(cp.value)");
      }
      buffer.append(" ").append(expression);
    }
    
    if (!isCounterQuery() && (expression == null || !containsOrderBy))
      buffer.append(" ORDER BY c.caseId ");
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
