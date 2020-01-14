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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.persistence.Query;

/**
 *
 * @author blanquepa
 */
public class JPAQuery
{
  private static final int MAX_STRING_LENGTH = 4000;
  
  private javax.persistence.Query query;
  private String idParamName = null;
  private List<String> idParamValues;
  private Map<String, String> listParameters;
  private int maxStringLength = MAX_STRING_LENGTH;
  private int firstResult = 0;
  private int maxResults = 0;

  public JPAQuery()
  {
  }

  public JPAQuery(javax.persistence.Query query)
  {
    this.query = query;
  }

  public Query getQuery()
  {
    return query;
  }

  public void setQuery(Query query)
  {
    this.query = query;
  }

  public void setParameter(String name, Object value) throws Exception
  {
    if (value instanceof List)
    {
      setListParameter(name, (List)value);
    }
    else
    {
      query.setParameter(name, value);
    }
  }

  public void setIdParameter(String name, List values) throws Exception
  {
    if (values != null)
    {
      List<String> strValues = listToString(values, maxStringLength);
      if (strValues.size() > 0)
      {
        if (idParamName != null)
          throw new Exception("MORE_THAN_ONE_ID_PARAMETER_DEFINED");
        else
        {
          idParamName = name;
          idParamValues = strValues;
        }
      }
      else
        query.setParameter(name, null);
    }
    else
      query.setParameter(name, null);
  }

  private void setListParameter(String name, List values) throws Exception
  {
    List<String> strValues = listToString(values, maxStringLength);
    if (strValues.size() > 1)
      throw new Exception("PARAMETER_LENGTH_TOO_LARGE");

    if (listParameters == null)
      listParameters = new HashMap();

    if (strValues.isEmpty())
      listParameters.put(name, null);
    else
      listParameters.put(name, strValues.get(0));
  }

  public List getResultList()
  {
    List result;

    if (listParameters != null)
    {
      for (Map.Entry<String,String> entry : listParameters.entrySet())
      {
        query.setParameter(entry.getKey(), entry.getValue());
      }
    }
    
    if (idParamName != null)
    {
      result = new ArrayList();
      for (String value : idParamValues)
      {
        if (result.size() < maxResults || maxResults == 0)
        {
          query.setParameter(idParamName, value);
          List part = query.getResultList();
          if (part != null && !part.isEmpty())
          {
            for (Object item : part)
            {
              if (firstResult <= 0 && (result.size() < maxResults || maxResults == 0))
                result.add(item);
              else
                firstResult--;
            }
          }
        }
      }
    }
    else
    {
      query.setFirstResult(firstResult);
      query.setMaxResults(maxResults);
      result = query.getResultList();
    }


    return result;
  }

  public void executeUpdate()
  {
    if (listParameters != null)
    {
      for (Map.Entry<String,String> entry : listParameters.entrySet())
      {
        query.setParameter(entry.getKey(), entry.getValue());
      }
    }

    if (idParamName != null)
    {
      for (String value : idParamValues)
      {
        query.setParameter(idParamName, value);
        query.executeUpdate();
      }
    }
    else query.executeUpdate();

  }

  public int getResultCount()
  {
    int count = 0;

    if (listParameters != null)
    {
      for (Map.Entry<String,String> entry : listParameters.entrySet())
      {
        query.setParameter(entry.getKey(), entry.getValue());
      }
    }

    if (idParamName != null)
    {
      for (String value : idParamValues)
      {
        query.setParameter(idParamName, value);
        Object part = query.getSingleResult();
        if (part != null)
        {
          if (part instanceof Vector)
            count += ((Number)((Vector)part).get(0)).intValue();
          else
            count += ((Number)part).intValue();
        }
      }
    }
    else
    {
      Object result = query.getSingleResult();
      if (result instanceof Vector)
        count += ((Number)((Vector)result).get(0)).intValue();
      else
        count += ((Number)result).intValue();
    }
    return count;
  }

  public int getMaxStringLength()
  {
    return maxStringLength;
  }

  public void setMaxStringLength(int maxStringLength)
  {
    this.maxStringLength = maxStringLength;
  }

  public void setFirstResult(int result)
  {
    this.firstResult = result;
  }

  public void setMaxResults(int result)
  {
    this.maxResults = result;
  }

  private List<String> listToString(List collection, int maxLength)
  {
    List<String> result = new ArrayList();

    if (collection != null && collection.size() > 0)
    {
      StringBuffer sb = new StringBuffer();
      sb.append(",");
      for (int i = 0; i < collection.size(); i++)
      {
        String value = String.valueOf(collection.get(i));
        if (sb.length() + value.length() + 1 >= maxLength)
        {
          sb.deleteCharAt(sb.lastIndexOf(","));
          result.add(sb.toString());
          sb = new StringBuffer();
        }
        sb.append(value);
        sb.append(",");
      }
      result.add(sb.toString());
    }
    return result;
  }
}
