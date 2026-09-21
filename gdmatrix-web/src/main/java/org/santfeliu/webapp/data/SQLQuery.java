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
package org.santfeliu.webapp.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import org.santfeliu.webapp.data.SQLQuery.DataRow;

/**
 *
 * @author realor
 */
public class SQLQuery extends DataQuery<DataRow>
{
  String dataSource;
  String sql;
  Map<String, Object> params = new HashMap<>();
  int maxResults = 10;
  
  public SQLQuery(String dataSource, String sql)
  {
    this.dataSource = dataSource;
    this.sql = sql;
  }
  
  public SQLQuery param(String name, Object value)
  {
    params.put(name, value);
    return this;
  }
  
  @Override
  public SQLQuery maxResults(Number maxResults)
  {
    this.maxResults = maxResults.intValue();
    return this;
  }
  
  @Override
  public List<DataRow> execute()
  {
    StringBuilder sqlBuffer = new StringBuilder();
    StringBuilder paramBuffer = new StringBuilder();
    List<Object> paramList = new ArrayList<>();
    int state = 0;
    for (int i = 0; i < sql.length(); i++)
    {
      char ch = sql.charAt(i);
      if (state == 0)
      {
        if (ch == '{')
        {
          state = 1;  
        }
        else
        {
          sqlBuffer.append(ch);
        }
      }
      else // in var
      {
        if (ch == '}')
        {
          String name = paramBuffer.toString();
          state = 0;
          paramList.add(params.get(name));
          sqlBuffer.append("?");
          paramBuffer.setLength(0);
        }
        else
        {
          paramBuffer.append(ch);
        }        
      }
    }
    String sqlParams = sqlBuffer.toString();
    try
    {
      return executeInternal(sqlParams, paramList);
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  protected List<DataRow> executeInternal(String sqlParams, List<Object> paramList)
    throws Exception
  {
    List<DataRow> data = new ArrayList<>();
    
    try (Connection conn = getConnection())
    {
      try (PreparedStatement ps = conn.prepareStatement(sqlParams))
      {
        ps.setMaxRows(maxResults);
        int index = 1;
        for (Object value : paramList)
        {
          ps.setObject(index++, value);
        }
        try (ResultSet rs = ps.executeQuery())
        {
          ResultSetMetaData metaData = rs.getMetaData();
          while (rs.next())
          {
            DataRow row = new DataRow();
            for (int i = 1; i <= metaData.getColumnCount(); i++)
            {
              String name = metaData.getColumnName(i);
              Object value = rs.getObject(name);
              row.put(name, value);
            }
            data.add(row);
          }
        }
      }
    }
    return data;
  }
   
  protected Connection getConnection() throws Exception
  {
    javax.naming.Context initContext = new InitialContext();
    javax.naming.Context envContext  =
       (javax.naming.Context)initContext.lookup("java:/comp/env");
    DataSource ds = (DataSource)envContext.lookup(dataSource);
    return ds.getConnection();    
  }
  
  public static class DataRow extends HashMap<String, Object>
  {    
  }
}
