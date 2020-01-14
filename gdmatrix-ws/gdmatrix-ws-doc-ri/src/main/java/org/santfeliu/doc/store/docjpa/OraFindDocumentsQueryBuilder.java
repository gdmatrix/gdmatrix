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

import java.util.ArrayList;
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
import org.santfeliu.util.FilterUtils;

/**
 *
 * @author blanquepa
 */
public class OraFindDocumentsQueryBuilder extends FindDocumentsQueryBuilder
{
  @Override
  public boolean isNativeQuery()
  {
    return true;
  }
  
  @Override
  public Query getQuery(EntityManager em) throws Exception
  {
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

    StringBuilder buffer = new StringBuilder();
    if (filter.getContentSearchExpression() != null)
    {
      appendContentSearchFilter(selectBuffer, fromBuffer, whereBuffer, buffer);
      appendOrderByExpression(buffer);
    }
    else
    {
      appendOrderByExpression(selectBuffer, fromBuffer, whereBuffer);
      buffer.append(selectBuffer);
      buffer.append(fromBuffer);
      buffer.append(whereBuffer);
    }

    //    System.out.println(buffer);
    
    Query query = em.createNativeQuery(buffer.toString());
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
      selectBuffer.append("SELECT count(*) as num");
      fromBuffer.append(" FROM dom_document d ");
    }
    else
    {
      selectBuffer.append("SELECT d.docId, d.version, d.title, d.doctypeid, " +
          "d.state, d.language as dlanguage, d.capturedate as dcapturedate, "
          + "d.captureuser as dcaptureuser, d.modifydate, " +
          "d.modifyuser, d.lockuser, " +
          "null as summary, " +
          "d.contentId, " +
          "d.creationDate");

      fromBuffer.append(" FROM dom_document d");
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
        "(exists (select acl.typeId from dic_acl acl where " +
        " acl.typeId = d.docTypeId and acl.action = 'Read' and ");
      appendInOperator(buffer, "acl.roleId", "?", "userRole", roles);
      buffer.append(")");

      buffer.append(" OR ");

      buffer.append(
        "exists (select dacl.docId from dom_acl dacl where " +
        " dacl.docId = d.docId and dacl.version = d.version and " +
        " dacl.action = 'Read' and ");
      appendInOperator(buffer, "dacl.roleId", "?", "userRole", roles);
      buffer.append("))");
    }
  }

  private void appendContentIdFilter(StringBuilder buffer)
  {
    String contentId = filter.getContentId();
    if (contentId != null && contentId.trim().length() > 0)
    {
      appendOperator(buffer, "AND");
      buffer.append("d.contentId = ?contentId");
      parameters.put("contentId", contentId);
    }
  }

  private void appendDocTypeIdFilter(StringBuilder fromBuffer,
    StringBuilder whereBuffer)
  {
    String typePath = filter.getDocTypeId();
    if (typePath != null && typePath.trim().length() != 0)
    {
      fromBuffer.append(", dic_type t");

      appendOperator(whereBuffer, "AND");
      whereBuffer.append("d.docTypeId = t.typeId AND t.typePath like ?typePath ESCAPE '\\'");
      typePath = typePath.replaceAll("_", "\\\\_") + "%";
      parameters.put("typePath", typePath);
    }
  }

  private void appendDocIdsFilter(StringBuilder buffer)
  {
    List<String> docIds = filter.getDocId();
    if (docIds.size() > 0)
    {
      appendOperator(buffer, "AND");
      appendInOperator(buffer, "d.docId", "?", "docId", docIds);
    }
  }

  private void appendLanguageFilter(StringBuilder buffer)
  {
    String language = filter.getLanguage();
    if (language != null && language.trim().length() > 0)
    {
      appendOperator(buffer, "AND");
      buffer.append("d.language = ?language");
      parameters.put("language", language);
    }
  }

  private void appendTitleFilter(StringBuilder buffer)
  {
    String title = filter.getTitle();
    if (title != null && title.trim().length() > 0)
    {
      appendOperator(buffer, "AND");
      buffer.append("TRANSLATE(UPPER(TITLE), 'ÁÀÉÈÍÓÒÚÏÜ', 'AAEEIOOUIU') like ?title");
      parameters.put("title", FilterUtils.replacePattern(
        filter.getTitle(), FilterUtils.REMOVE_SYMBOLS));
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
        buffer.append("d.").append("modifydate").append(" >= ?startDate");
        parameters.put("startDate", startDate + "000000");
      }
      else if (dateComparator.equals("2") ||
        dateComparator.equals("captureDateTime"))
      {
        buffer.append("d.").append("capturedate").append(" >= ?startDate");
        parameters.put("startDate", startDate + "000000");
      }
      else if (dateComparator.equals("3") ||
        dateComparator.equals("creationDate"))
      {
        buffer.append("d.").append("creationdate").append(" >= ?startDate");
        parameters.put("startDate", startDate);
      }
      else
      {
        buffer.append("d.").append("modifydate").append(" >= ?startDate");
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
          buffer.append("d.").append("modifydate").append(" <= ?endDate");
          parameters.put("endDate", endDate + "999999");
        }
        else if (dateComparator.equals("2"))
        {
          buffer.append("d.").append("capturedate").append(" <= ?endDate");
          parameters.put("endDate", endDate + "999999");
        }
        else if (dateComparator.equals("3"))
        {
          buffer.append("d.").append("creationdate").append(" <= ?endDate");
          parameters.put("endDate", endDate);
        }
        else
        {
          buffer.append("d.").append("modifydate").append(" <= ?endDate");
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
      appendInOperator(buffer, "d.state", "?", "state", states);
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
        buffer.append("d.lastversion = 'T'");
      }
      else
      {
        buffer.append("d.version = ?version");
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
    if (values == null)
      values = new ArrayList();
      
    if (values.isEmpty() && name != null)
      values.add("%");
      
    if (values.size() > 0)
    {
      appendOperator(buffer, "AND");

      StringBuilder aux = new StringBuilder();
      aux.append(
        "exists (select 'PROP' from dom_metadata # where " +
        "d.docId = #.docId and d.version = #.version and #.propname = ?" +
        tablePrefix + "_pname and ");
      parameters.put(tablePrefix + "_pname", name);
      appendLikeOperator(aux, "#.value", "?", name, values);
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

  private void appendContentSearchFilter(StringBuilder selectBuffer,
    StringBuilder fromBuffer, StringBuilder whereBuffer,
    StringBuilder resultBuffer)
  {
    String contentExpression = filter.getContentSearchExpression();
    if (contentExpression != null)
    {
      //Internal
      StringBuilder auxSelectBuffer = new StringBuilder(selectBuffer);
      StringBuilder auxFromBuffer = new StringBuilder(fromBuffer);
      StringBuilder auxWhereBuffer = new StringBuilder(whereBuffer);

      String score = contentExpression != null && !isCounterQuery()?
          ", score(1) as score " : "";
      auxSelectBuffer.append(score);

      auxFromBuffer.append(", cnt_internal ci");

      appendOperator(auxWhereBuffer, "AND");
      auxWhereBuffer.append("d.contentid = ci.UUID (+)");
      auxWhereBuffer.append(" AND ci.DATA is not null and contains(ci.DATA, ?searchExpression, 1) > 0");

      resultBuffer.append(auxSelectBuffer);
      resultBuffer.append(auxFromBuffer);
      resultBuffer.append(auxWhereBuffer);

      //External
      resultBuffer.append(" UNION ALL ");

      auxFromBuffer = new StringBuilder(fromBuffer);
      auxWhereBuffer = new StringBuilder(whereBuffer);

      auxFromBuffer.append(", cnt_external ce");

      appendOperator(auxWhereBuffer, "AND");
      auxWhereBuffer.append("d.contentid = ce.UUID (+)");
      auxWhereBuffer.append(" AND ce.URL is not null and contains(ce.URL, ?searchExpression, 1) > 0");

      resultBuffer.append(auxSelectBuffer);
      resultBuffer.append(auxFromBuffer);
      resultBuffer.append(auxWhereBuffer);

      //Insert in Select count
      if (isCounterQuery())
      {
        resultBuffer.insert(0, "SELECT sum(num) FROM (");
        resultBuffer.append(")");
      }
      
      parameters.put("searchExpression", contentExpression);
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
      expression = expression.replaceAll("changeDateTime", "d.modifyDate");
      expression = expression.replaceAll("changeUserId", "d.modifyUser");
      expression = expression.replaceAll("captureDateTime", "d.captureDate");
      expression = expression.replaceAll("captureUserId", "d.captureUser");
      expression = expression.replaceAll("changeUserId", "d.modifyUser");
      expression = expression.replaceAll("creationDate", "d.creationDate");
      expression = expression.replaceAll("lockUserId", "d.lockUser");
      expression = expression.replaceAll("contentId", "d.contentId");

      if (expression.contains("typePath"))
      {
        expression = expression.replaceAll("typePath", "t.typePath");
        fromBuffer.append(", dic_type t");

        appendOperator(whereBuffer, "AND");
        whereBuffer.append("d.docTypeId = t.typeId (+)");
      }

      appendOperator(whereBuffer, "AND");
      whereBuffer.append(expression);
    }
  }

  private void appendOrderByExpression(StringBuilder buffer)
  {
    List<OrderByProperty> orderByProperties = filter.getOrderByProperty();
    if (orderByProperties.size() > 0)
    {
      StringBuilder obBuffer = new StringBuilder();
      StringBuilder selectBuffer = new StringBuilder();
      StringBuilder fromBuffer = new StringBuilder();
      StringBuilder whereBuffer = new StringBuilder();
      int userPropsCount = 0;
      boolean orderByDynamic = orderByDynamic(orderByProperties);
      if (orderByDynamic)
      {
        selectBuffer.append("SELECT d.*");
        fromBuffer.append(" FROM (").append(buffer).append(") d ");
      }
      for (OrderByProperty orderByProperty : orderByProperties)
      {
        String orderPropName = getFieldName(orderByProperty.getName());
        if (orderPropName == null)
        {
          orderPropName = orderByProperty.getName();
          if ("score".equalsIgnoreCase(orderPropName)
            && filter.getContentSearchExpression() != null)
            orderPropName = "score";
          else
          {
            //Output property
            if (!isCounterQuery())
              selectBuffer.append(",o" + userPropsCount + ".value ");
            fromBuffer.append(", dom_metadata o" + userPropsCount + " ");
            appendOperator(whereBuffer, "AND");
            whereBuffer.append("d.docid = o" + userPropsCount +
              ".docid (+) AND " + "d.version = o" + userPropsCount +
              ".version (+) AND o" + userPropsCount + ".propname = ?propname" + userPropsCount + " ");
            parameters.put("propname" + userPropsCount, orderPropName);
            orderPropName = "o" + userPropsCount + ".value";
            userPropsCount++;
          }
        }
        
        if (orderPropName != null && !isCounterQuery())
        {
          if (orderByDynamic && !orderPropName.contains("."))
            orderPropName = "d." + orderPropName;
          if (orderByProperty.isDescending() && orderPropName != null)
            orderPropName += " desc";
          if (obBuffer.length() == 0)
            obBuffer.append(" ORDER BY " + orderPropName);
          else
            obBuffer.append("," + orderPropName);
        }
      }

      if (selectBuffer.length() > 0 && !isCounterQuery())
      {
        buffer.delete(0, buffer.length());
        buffer.append(selectBuffer.append(fromBuffer).append(whereBuffer));
      }

      if (obBuffer.length() == 0 && !isCounterQuery())
        obBuffer.append(" ORDER BY d.docId");
      buffer.append(obBuffer);
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
          if ("score".equalsIgnoreCase(orderPropName)
            && filter.getContentSearchExpression() != null)
            orderPropName = "score";
          else
          {
            //Output property
            if (!isCounterQuery())
              selectBuffer.append(",o" + userPropsCount + ".value ");
            fromBuffer.append(", dom_metadata o" + userPropsCount + " ");

            appendOperator(whereBuffer, "AND");
            whereBuffer.append("d.docid = o" + userPropsCount +
              ".docid (+) AND " + "d.version = o" + userPropsCount +
              ".version (+) AND o" + userPropsCount + ".propname = ?propname" + userPropsCount + " ");
            parameters.put("propname" + userPropsCount, orderPropName);
            orderPropName = "o" + userPropsCount + ".value";
            userPropsCount++;
          }
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

  private boolean orderByDynamic(List<OrderByProperty> properties)
  {
    if (properties != null && !properties.isEmpty())
    {
      for (OrderByProperty property : properties)
      {
        if (getFieldName(property.getName()) == null)
          return true;
      }
    }
    return false;
  }

  private String getFieldName(String propName)
  {
    if ("docId".equalsIgnoreCase(propName))
      return "docid";
    else if ("version".equalsIgnoreCase(propName))
      return "version";
    else if ("language".equalsIgnoreCase(propName))
      return "dlanguage";
    else if ("title".equalsIgnoreCase(propName))
      return "title";
    else if ("captureDate".equalsIgnoreCase(propName))
      return "dcapturedate";
    else if ("captureDateTime".equalsIgnoreCase(propName))
      return "dcapturedate";
    else if ("captureUser".equalsIgnoreCase(propName))
      return "dcaptureuser";
    else if ("captureUserId".equalsIgnoreCase(propName))
      return "dcaptureuser";
    else if ("docTypeId".equalsIgnoreCase(propName))
      return "docTypeId";
    else if ("lockUser".equalsIgnoreCase(propName))
      return "lockuser";
    else if ("lockUserId".equalsIgnoreCase(propName))
      return "lockuser";
    else if ("modifyDate".equalsIgnoreCase(propName)) //deprecated
      return "modifydate";
    else if ("modifyDateTime".equalsIgnoreCase(propName)) //deprecated
      return "modifydate";
    else if ("changeDateTime".equalsIgnoreCase(propName))
      return "modifydate";
    else if ("modifyUser".equalsIgnoreCase(propName)) //deprecated
      return "modifyuser";
    else if ("modifyUserId".equalsIgnoreCase(propName)) //deprecated
      return "modifyuser";
    else if ("changeUserId".equalsIgnoreCase(propName))
      return "modifyuser";
    else if ("creationDate".equalsIgnoreCase(propName))
      return "creationdate";
    else if ("state".equalsIgnoreCase(propName))
      return "state";
    else
      return null;
  }
}