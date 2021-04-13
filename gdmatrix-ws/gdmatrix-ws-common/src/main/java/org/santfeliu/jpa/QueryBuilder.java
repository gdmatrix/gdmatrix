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
package org.santfeliu.jpa;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.santfeliu.util.FilterUtils;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author blanquepa
 */
public abstract class QueryBuilder
{
  protected Map<String, Object> parameters = new HashMap();
  protected int maxInValues = 1000;

  public static QueryBuilder getInstance(Class invokerClass, String queryName)
  {
    QueryBuilder queryBuilder = null;
    try
    {
      String queryBuilderClassName =
        MatrixConfig.getClassProperty(invokerClass, queryName + "QueryBuilder");
      if (queryBuilderClassName != null)
      {
        Class queryBuilderClass = Class.forName(queryBuilderClassName);
        queryBuilder = (QueryBuilder)queryBuilderClass.newInstance();
      }
    }
    catch (Exception ex)
    {
    }

    return queryBuilder;
  }

  public abstract Query getQuery(EntityManager em)
    throws Exception;

  protected void setParameters(Query query)
  {
    Set<Entry<String, Object>> paramSet = parameters.entrySet();
    for (Entry<String, Object> param : paramSet)
    {
      String name = param.getKey();
      Object value = param.getValue();
      query.setParameter(name, value);
    }
  }

  protected void appendOperator(StringBuilder buffer, String operator)
  {
    if (buffer.indexOf("WHERE") > 0)
      buffer.append(" " + operator +" ");
    else
      buffer.append(" WHERE ");
  }


  public void appendInOperator(StringBuilder buffer,
    String field, String paramPrefix, String paramName, List values)
  {
    int size = values.size();
    int from = 0;
    int to = maxInValues - 1;

    buffer.append("(");
    while (from < size)
    {
      if (to >= size) to = (size - 1);
      if (from > 0)
        buffer.append(" OR ");
      appendInValues(buffer, field, paramPrefix, paramName, values, from, to);
      from = from + maxInValues;
      to = to + maxInValues;
    }
    buffer.append(")");
  }

  private void appendInValues(StringBuilder buffer,
    String field, String paramPrefix, String paramName, List values,
    int from, int to)
  {
    if (buffer != null)
    {
      buffer.append(field).append(" IN (");
      for (int i = from; i <= to; i++)
      {
        if (i != from) buffer.append(",");
        String paramIndex = String.valueOf(i);
        String paramValue = values.get(i).toString();
        appendParameter(buffer, paramPrefix, paramName, paramIndex, paramValue);
      }
      buffer.append(")");
    }
  }
  
  protected void appendOperator(StringBuilder buffer, 
    String field, String paramPrefix, String paramName, List values)
  {
    if (buffer != null)
    {
      buffer.append("(");
      for (int i = 0; i < values.size(); i++)
      {
        boolean caseSensitive = true;
        if (i != 0) buffer.append(" or ");
        
        String op = " like ";
        String value = values.get(i).toString();
        if (value != null)
        {
          if (value.startsWith("$"))          
          {
            value = value.substring(1);
            int index = value.indexOf("$");
            if (index > 0)
            {
              op = " " + value.substring(0, index) + " ";
              value = value.substring(index + 1);
            }
          }
          if (value.startsWith("low("))
          {
            value = value.substring(4);
            int index = value.indexOf(")");
            if (index > 0)
            {
              value = value.substring(0, index);
              caseSensitive = false;
            }
          }
        }
        buffer.append(caseSensitive ? "" : "lower(").append(field).
          append(caseSensitive ? "" : ")").append(op);
        String paramIndex = String.valueOf(i);
        String paramValue = 
          (caseSensitive || value == null) ? value : value.toLowerCase();
        appendParameter(buffer, paramPrefix, paramName, paramIndex, paramValue);
      }
      buffer.append(")");
    }
  }  

  protected void appendLikeOperator(StringBuilder buffer,
    String field, String paramPrefix, String paramName, List values)
  {
    if (buffer != null)
    {
      buffer.append("(");
      for (int i = 0; i < values.size(); i++)
      {
        if (i != 0) buffer.append(" or ");
        buffer.append(field).append(" like ");
        String paramIndex = String.valueOf(i);
        String paramValue = values.get(i).toString();
        appendParameter(buffer, paramPrefix, paramName, paramIndex, paramValue);
      }
      buffer.append(")");
    }
  }
  
  protected String addPercent(String text)
  {
    if (text == null) return null;
    if ("".equals(text)) return "";
    return FilterUtils.addWildcards(text.toLowerCase());
  }
  
  protected void appendParameter(StringBuilder buffer, String paramPrefix, 
    String paramName, String paramIndex, String value)
  {
    if (paramName != null)
      paramName = TextUtils.normalizeName(paramName);
    buffer.append(paramPrefix).append(paramName).append(paramIndex);
    parameters.put(paramName + paramIndex, value);    
  }
}
