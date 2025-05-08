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

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.Property;
import org.matrix.doc.DocumentConstants;
import org.matrix.doc.OrderByProperty;
import org.matrix.doc.State;
import org.matrix.security.SecurityConstants;
import org.santfeliu.doc.store.FindDocumentsQueryBuilder;

/**
 *
 * @author blanquepa
 */
public class JPQLFindDocumentsQueryBuilder extends FindDocumentsQueryBuilder
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
    //validate
    if (filter.getContentSearchExpression() != null)
      throw new Exception("doc:CONTENT_SEARCH_NOT_SUPPORTED");
    if (filter.isSummary())
      throw new Exception("doc:SUMMARY_NOT_SUPPORTED");


    StringBuilder buffer = new StringBuilder();

    StringBuilder selectBuffer = new StringBuilder();
    StringBuilder fromBuffer = new StringBuilder();
    StringBuilder whereBuffer = new StringBuilder();

    appendMainStatement(selectBuffer, fromBuffer);
    appendRolesFilter(whereBuffer);
    appendContentIdFilter(whereBuffer);
    appendDocIdsFilter(whereBuffer);
    appendVersionFilter(whereBuffer);
    appendDocTypeIdFilter(fromBuffer, whereBuffer);
    appendLanguageFilter(whereBuffer);
    appendTitleFilter(whereBuffer);
    appendStartDateFilter(whereBuffer);
    appendEndDateFilter(whereBuffer);
    appendStatesFilter(whereBuffer);
    appendClassIdFilter(whereBuffer);
    appendPropertiesFilter(whereBuffer);
    appendMetadataSearchExpression(fromBuffer, whereBuffer);
    appendOrderByExpression(selectBuffer, fromBuffer, whereBuffer);

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
      selectBuffer.append("SELECT count(d) ");
      fromBuffer.append("FROM DBDocument d");
    }
    else
    {
      selectBuffer.append("SELECT d ");
      fromBuffer.append("FROM DBDocument d");
    }
  }

  private void appendRolesFilter(StringBuilder buffer)
  {
    if (roles.isEmpty())
      roles.add(SecurityConstants.EVERYONE_ROLE);

    if (!roles.contains(DocumentConstants.DOC_ADMIN_ROLE) && 
        !(filter.isRolesDisabled() && isCounterQuery()))
    { 
      appendOperator(buffer, "AND");

      buffer.append(
        "(exists (select acl.typeId from AccessControl acl where " +
        " acl.typeId = d.docTypeId and acl.action = 'Read' and ");
      appendInOperator(buffer, "acl.roleId", ":", "userRole", roles);
      buffer.append(")");

      buffer.append(" OR ");

      buffer.append(
        "exists (select dacl.docId from DocAccessControl dacl where " +
        " dacl.docId = d.docId and dacl.version = d.version and " +
        " dacl.action = 'Read' and ");
      appendInOperator(buffer, "dacl.roleId", ":", "userRole", roles);
      buffer.append("))");
    }
  }

  private void appendContentIdFilter(StringBuilder buffer)
  {
    String contentId = filter.getContentId();
    if (contentId != null && contentId.trim().length() > 0)
    {
      appendOperator(buffer, "AND");
      buffer.append("d.contentId = :contentId");
      parameters.put("contentId", contentId);
    }
  }

  private void appendDocTypeIdFilter(StringBuilder fromBuffer,
    StringBuilder whereBuffer)
  {
    String docTypeId = filter.getDocTypeId();
    if (docTypeId != null)
    {
      fromBuffer.append (", Type t ");

      appendOperator(whereBuffer, "AND");
      whereBuffer.append("d.docTypeId = t.typeId AND t.typePath like :docTypeId ESCAPE '\\'");

      if (docTypeId != null && docTypeId.length() > 0)
        docTypeId = docTypeId.replaceAll("_", "\\\\_") + "%";
      else
        docTypeId = null;
      parameters.put("docTypeId", docTypeId);
    }
  }

  private void appendDocIdsFilter(StringBuilder buffer)
  {
    List<String> docIds = filter.getDocId();
    if (docIds.size() > 0)
    {
      appendOperator(buffer, "AND");
      appendInOperator(buffer, "d.docId", ":", "docId", docIds);
    }
  }

  private void appendLanguageFilter(StringBuilder buffer)
  {
    String language = filter.getLanguage();
    if (language != null && language.trim().length() > 0)
    {
      appendOperator(buffer, "AND");
      buffer.append("d.language = :language");
      parameters.put("language", language);
    }
  }

  private void appendTitleFilter(StringBuilder buffer)
  {
    String title = filter.getTitle();
    if (title != null && title.trim().length() > 0)
    {
      appendOperator(buffer, "AND");
      buffer.append("UPPER(d.title) like :title");
      parameters.put("title", filter.getTitle().toUpperCase());
    }
  }

  private void appendStartDateFilter(StringBuilder buffer)
  {
    String startDate = filter.getStartDate();
    if (startDate != null && !StringUtils.isBlank(startDate))
    {
      String dateComparator = filter.getDateComparator();
      appendOperator(buffer, "AND");
      if (dateComparator == null || dateComparator.equals("1") ||
        dateComparator.equals("changeDateTime"))
      {
        buffer.append("d.").append("changeDateTime").append(" >= :startDate");
        parameters.put("startDate", startDate + "000000");
      }
      else if (dateComparator.equals("2") ||
        dateComparator.equals("captureDateTime"))
      {
        buffer.append("d.").append("captureDateTime").append(" >= :startDate");
        parameters.put("startDate", startDate + "000000");
      }
      else if (dateComparator.equals("3") ||
        dateComparator.equals("creationDate"))
      {
        buffer.append("d.").append("creationDate").append(" >= :startDate");
        parameters.put("startDate", startDate);
      }
      else
      {
        buffer.append("d.").append("changeDateTime").append(" >= :startDate");
        parameters.put("startDate", startDate + "000000");
      }
    }
  }

  private void appendEndDateFilter(StringBuilder buffer)
  {
    String endDate = filter.getEndDate();
    String startDate = filter.getStartDate();
    if (!StringUtils.isBlank(endDate))
    {
      if (StringUtils.isBlank(startDate) && "00000000".equals(endDate))
        endDate = null;
      else
      {
        if (!StringUtils.isBlank(startDate) &&
          ("00000000".equals(endDate) || startDate.compareTo(endDate) > 0))
        {
          endDate = filter.getStartDate();
        }

        String dateComparator = filter.getDateComparator();
        appendOperator(buffer, "AND");

        if (dateComparator == null || dateComparator.equals("1"))
        {
          buffer.append("d.").append("changeDateTime").append(" <= :endDate");
          parameters.put("endDate", endDate + "999999");
        }
        else if (dateComparator.equals("2"))
        {
          buffer.append("d.").append("captureDatTime").append(" <= :endDate");
          parameters.put("endDate", endDate + "999999");
        }
        else if (dateComparator.equals("3"))
        {
          buffer.append("d.").append("creationDate").append(" <= :endDate");
          parameters.put("endDate", endDate);
        }
        else
        {
          buffer.append("d.").append("changeDateTime").append(" <= :endDate");
          parameters.put("endDate", endDate + "999999");
        }
      }
    }
  }

  private void appendStatesFilter(StringBuilder buffer)
  {
    List<State> states = filter.getStates();
    if (states == null || states.isEmpty()) //exclude Deleted
    {
      appendOperator(buffer, "AND");
      buffer.append("d.state <> 'DELETED'");
    }    
    else
    {
      appendOperator(buffer, "AND");
      appendInOperator(buffer, "d.state", ":", "state", states);
    }
  }

  private void appendVersionFilter(StringBuilder buffer)
  {
    int version = filter.getVersion();
    if (version != -1)
    {
      appendOperator(buffer, "AND");
      if (version == 0)
      {
        buffer.append("d.version = (select max(x.version) from DBDocument x where x.docId = d.docId)");
      }
      else
      {
        buffer.append("d.version <= :version");
        parameters.put("version", version);
      }
    }
  }

  private void appendPropertiesFilter(StringBuilder buffer)
  {
    List<Property> properties = filter.getProperty();
    for (int i = 0; i < properties.size(); i++)
    {
      Property p = properties.get(i);
      appendPropertyFilter(buffer, p.getName(), p.getValue(), "p" + i);
    }
  }

  private void appendPropertyFilter(StringBuilder buffer, String name,
    List<String> values, String tablePrefix)
  {
    if (values != null && values.size() > 0)
    {
      appendOperator(buffer, "AND");

      StringBuilder aux = new StringBuilder();
      aux.append(
        "exists (select #.docId from DBProperty # where " +
        "d.docId = #.docId and d.version = #.version and #.name = :" +
        tablePrefix + "_pname and ");
      parameters.put(tablePrefix + "_pname", name);

      appendLikeOperator(aux, "#.value", ":", name, values);
      aux.append(")");

      String sAux = aux.toString();
      buffer.append(sAux.replaceAll("#", tablePrefix));
    }
  }

  private void appendClassIdFilter(StringBuilder buffer)
  {
    List<String> classIds = filter.getClassId();
    if (classIds != null && classIds.size() > 0)
    {
      appendPropertyFilter(buffer, "classId", classIds, "class");
    }
  }

  private void appendMetadataSearchExpression(StringBuilder fromBuffer,
    StringBuilder whereBuffer)
  {
    String expression = filter.getMetadataSearchExpression();
    if (expression != null && expression.length() > 0)
    {
      expression = expression.replaceAll("docId", "d.docId");
      expression = expression.replaceAll("version", "d.version");
      expression = expression.replaceAll("docTypeId", "d.docTypeId");
      expression = expression.replaceAll("title", "d.title");
      expression = expression.replaceAll("state", "d.state");
      expression = expression.replaceAll("language", "d.language");
      expression = expression.replaceAll("captureDateTime", "d.captureDateTime");
      expression = expression.replaceAll("captureUserId", "d.captureUserId");
      expression = expression.replaceAll("changeDateTime", "d.changeDateTime");
      expression = expression.replaceAll("changeUserId", "d.changeUserId");
      expression = expression.replaceAll("creationDate", "d.creationDate");
      expression = expression.replaceAll("lockUserId", "d.lockUserId");
      expression = expression.replaceAll("contentId", "d.contentId");

      if (expression.contains("typePath"))
      {
        expression = expression.replaceAll("typePath", "t.typePath");
        fromBuffer.append(", Type t");

        appendOperator(whereBuffer, "AND");
        whereBuffer.append("d.docTypeId = t.typeId");
      }

      appendOperator(whereBuffer, "AND");
      whereBuffer.append(expression);
    }
  }

  private void appendOrderByExpression(StringBuilder selectBuffer,
    StringBuilder fromBuffer, StringBuilder whereBuffer)
  {
    List<OrderByProperty> orderByProperties = filter.getOrderByProperty();
    if (orderByProperties.size() > 0)
    {
      StringBuilder obBuffer = new StringBuilder();
      int userPropsCount = 0;
      for (OrderByProperty orderByProperty : orderByProperties)
      {
        String orderPropName = getFieldName(orderByProperty.getName());
        if (orderPropName == null)
        {
          orderPropName = orderByProperty.getName();
          //Output property
          int idx = fromBuffer.indexOf("DBDocument d");
          fromBuffer.replace(idx, idx + 12, 
            "DBDocument d LEFT JOIN DBProperty o" + userPropsCount + 
            " ON (d.docId = o" + userPropsCount + ".docId and d.version = o" + 
            userPropsCount + ".version and o" + userPropsCount + 
            ".name = :propname" + userPropsCount + ")");          
          parameters.put("propname" + userPropsCount, orderPropName);
          orderPropName = "o" + userPropsCount + ".value";
          userPropsCount++;
        }

        if (orderPropName != null && !isCounterQuery())
        {
          if (orderByProperty.isDescending() && orderPropName != null)
            orderPropName += " desc";

          if (obBuffer.length() == 0)
            obBuffer.append(" ORDER BY " + orderPropName);
          else
            obBuffer.append("," + orderPropName);
        }
      }
      if (obBuffer.length() == 0 && !isCounterQuery())
        obBuffer.append(" ORDER BY d.docId");
      whereBuffer.append(obBuffer);
    }
  }

  private String getFieldName(String orderPropName)
  {
    if ("docId".equalsIgnoreCase(orderPropName))
      return "d.docId";
    else if ("version".equalsIgnoreCase(orderPropName))
      return "d.version";
    else if ("language".equalsIgnoreCase(orderPropName))
      return "d.language";
    else if ("title".equalsIgnoreCase(orderPropName))
      return "d.title";
    else if ("captureDate".equalsIgnoreCase(orderPropName))
      return "d.captureDateTime";
    else if ("captureUser".equalsIgnoreCase(orderPropName))
      return "d.captureUserId";
    else if ("docTypeId".equalsIgnoreCase(orderPropName))
      return "d.docTypeId";
    else if ("lockUser".equalsIgnoreCase(orderPropName))
      return "d.lockUserId";
    else if ("modifyDate".equalsIgnoreCase(orderPropName)) //deprecated
      return "d.changeDateTime";
    else if ("modifyDateTime".equalsIgnoreCase(orderPropName)) //deprecated
      return "d.changeDateTime";
    else if ("changeDateTime".equalsIgnoreCase(orderPropName))
      return "d.changeDateTime";
    else if ("modifyUser".equalsIgnoreCase(orderPropName)) //deprecated
      return "d.changeUserId";
    else if ("modifyUserId".equalsIgnoreCase(orderPropName)) //deprecated
      return "d.changeUserId";
    else if ("changeUserId".equalsIgnoreCase(orderPropName))
      return "d.changeUserId";
    else if ("creationDate".equalsIgnoreCase(orderPropName))
      return "d.creationDate";
    else if ("state".equalsIgnoreCase(orderPropName))
      return "d.state";
    else
      return null;
  }
  
}
