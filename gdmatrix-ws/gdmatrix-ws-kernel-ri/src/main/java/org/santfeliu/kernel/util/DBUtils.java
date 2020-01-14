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
package org.santfeliu.kernel.util;

import java.util.Map;

import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;


/**
 *
 * @author unknown
 */
public class DBUtils
{
  public DBUtils()
  {
  }
  
  public static int getSequenceValue(EntityManager em, 
    String claupref, String claucod, String clauorigen,
    String claudesc)
    throws Exception
  {
    int sequenceValue;
  
    //update counter
    Query query = em.createNamedQuery("updateCounter");
    query.setParameter("claupref", claupref);
    query.setParameter("claucod", claucod);
    query.setParameter("clauorigen", clauorigen);
    int numUpdated = query.executeUpdate();
    
    
    if (numUpdated == 1) 
    {
      query = em.createNamedQuery("selectCounter");
      query.setParameter("claupref", claupref);
      query.setParameter("claucod", claucod);
      query.setParameter("clauorigen", clauorigen);
      Object value = query.getSingleResult();
      if (value instanceof Number)
      {
        Number num = (Number)value;
        sequenceValue = num.intValue();
      }
      else if (value instanceof String)
      {
        String s = (String)value;
        sequenceValue = Integer.parseInt(s);
      }
      else
      {
        throw new Exception("INVALID_COUNTER_TYPE");
      }
    }
    else // counter row do not exists, then create it.
    {
      sequenceValue = 0;
      Counter counter = new Counter(claupref, claucod, clauorigen, claudesc, sequenceValue);
      em.persist(counter);
    }
    return sequenceValue;
  }  
  
  public static int getMaxAutoNumber(EntityManager em, String blockQueryName, 
    String sqlName, Map parameters, int carry) throws Exception
  {
    if(blockQueryName != null && !"".equals(blockQueryName))
    {
      if(sqlName != null && !"".equals(sqlName))
      {
        Query query = em.createNamedQuery(blockQueryName);
        query = addParameters(query, parameters);
        query.executeUpdate();
        
        query = em.createNamedQuery(sqlName);
        query = addParameters(query, parameters);
        Object value = query.getSingleResult();
        if(value != null)
        {
          Number num = (Number)value;
          return num.intValue() + carry;      
        }      
        return 1;        
      }
    }
    throw new Exception("INVALID_QUERY");
  }
  
  public static Query addParameters(Query q, Map parameters) 
  {
    if(parameters != null && !parameters.isEmpty())
    {
      for(Map.Entry entry : (Set<Map.Entry>) parameters.entrySet())
      {
        q.setParameter(String.valueOf(entry.getKey()), (Object) entry.getValue());
      }      
    }
    return q;
  }
}
