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
package org.santfeliu.util.script.function;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 *
 * @author realor
 */
/*
 * Usage: sqlExecuteQuery(String sqlQuery, String dataSource, Array parameters)
 *
 * returns a javascript array[N][M] with values returned by sqlExecuteQuery
 *
 * Example:
 *
 * data = sqlExecuteQuery("select nom, edat from persona where perscod=?",
 *  "jdbc/matrix", [33442]);
 *
 */
@Deprecated
public class SqlExecuteQueryFunction extends BaseFunction
{
  protected static final Logger logger = Logger.getLogger("sqlExecuteQuery");

  @Override
  public Object call(Context cx, Scriptable scope, Scriptable thisObj,
    Object[] args)
  {
    if (args.length >= 2)
    {
      String query = (args[0] == null) ? null : Context.toString(args[0]);
      String dataSource = (args[1] == null) ? null : Context.toString(args[1]);
      Scriptable parameters = null;
      if (args.length >= 3) parameters = (Scriptable)args[2];

      if (query != null && dataSource != null)
      {
        return executeQuery(cx, scope, query, dataSource, parameters);
      }
    }
    return null;
  }

  private Object executeQuery(Context cx, Scriptable scope,
    String query, String dataSource, Scriptable parameters)
  {
    Object result;
    try
    {
      javax.naming.Context initContext = new InitialContext();
      javax.naming.Context envContext  =
         (javax.naming.Context)initContext.lookup("java:/comp/env");
      DataSource ds = (DataSource)envContext.lookup(dataSource);
      Connection conn = ds.getConnection();
      try
      {
        logger.log(Level.INFO, "Executing {0}", query);
        PreparedStatement stmt = conn.prepareStatement(query);
        if (parameters != null) setParameters(scope, stmt, parameters);
        try
        {
          boolean resultSet = stmt.execute();
          if (resultSet)
          {
            result = readResultSet(cx, scope, stmt);
          }
          else
          {
            result = stmt.getUpdateCount();
          }
        }
        finally
        {
          stmt.close();
        }
      }
      finally
      {
        conn.close();
      }
    }
    catch (Exception ex)
    {
      logger.log(Level.SEVERE, "Error: {0}", ex.toString());
      result = cx.newArray(scope, 0);
    }
    return result;
  }

  private Scriptable readResultSet(Context cx, Scriptable scope,
    PreparedStatement stmt) throws Exception
  {
    Scriptable result;
    ArrayList<Object[]> table = new ArrayList();
    ResultSet rs = stmt.getResultSet();
    try
    {
      ResultSetMetaData md = rs.getMetaData();
      int columns = md.getColumnCount();
      while (rs.next())
      {
        Object[] row = new Object[columns];
        table.add(row);
        for (int i = 0; i < columns; i++)
        {
          Object value = rs.getObject(i + 1);
          row[i] = Context.javaToJS(value, scope);
        }
      }
      // convert list to js array
      result = cx.newArray(scope, table.size());
      for (int i = 0; i < table.size(); i++)
      {
        Object[] row = table.get(i);
        result.put(i, result, cx.newArray(scope, row));
      }
    }
    finally
    {
      rs.close();
    }
    return result;
  }

  private void setParameters(Scriptable scope,
    PreparedStatement stmt, Scriptable parameters) throws Exception
  {
    for (Object o : parameters.getIds())
    {
      if (o instanceof Integer)
      {
        int index = (Integer)o;
        Object value = parameters.get(index, scope);
        int paramIndex = index + 1;
        if (value == null)
        {
          stmt.setNull(paramIndex, java.sql.Types.VARCHAR);
        }
        else
        {
          Object javaValue = Context.jsToJava(value, java.lang.String.class);
          stmt.setObject(paramIndex, javaValue);
        }
      }
    }
  }

//  private void setParameters(Scriptable scope,
//    PreparedStatement stmt, Scriptable parameters) throws Exception
//  {
//    // NOTE: not all drivers support it!
//    ParameterMetaData paramMetaData = stmt.getParameterMetaData();
//    for (Object o : parameters.getIds())
//    {
//      if (o instanceof Integer)
//      {
//        int index = (Integer)o;
//        Object value = parameters.get(index, scope);
//        int paramIndex = index + 1;
//        if (value == null)
//        {
//          int paramType = paramMetaData.getParameterType(paramIndex);
//          stmt.setNull(paramIndex, paramType);
//        }
//        else
//        {
//          String paramClassName =
//            paramMetaData.getParameterClassName(paramIndex);
//          Class paramClass = Class.forName(paramClassName);
//          Object javaValue = Context.jsToJava(value, paramClass);
//          stmt.setObject(paramIndex, javaValue);
//        }
//      }
//    }
//  }
}
