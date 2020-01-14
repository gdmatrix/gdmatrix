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
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;

/**
 *
 * @author realor
 */
public class DBConnectionFunction extends BaseFunction
{
  protected static final Logger logger = Logger.getLogger("DBConnection");
  public static final String CONNECTION_VAR = "_connection_";

  private OpenFunction openFunction = new OpenFunction();
  private ExecuteFunction executeFunction = new ExecuteFunction();
  private CommitFunction commitFunction = new CommitFunction();
  private RollbackFunction rollbackFunction = new RollbackFunction();
  private CloseFunction closeFunction = new CloseFunction();

  @Override
  public Object call(Context cx, Scriptable scope, Scriptable thisObj,
    Object[] args)
  {
    if (thisObj == null) return null;

    System.out.println("new DBConnection()");
    thisObj.put("open", thisObj, openFunction);
    thisObj.put("execute", thisObj, executeFunction);
    thisObj.put("commit", thisObj, commitFunction);
    thisObj.put("rollback", thisObj, rollbackFunction);
    thisObj.put("close", thisObj, closeFunction);

    return thisObj;
  }

  class OpenFunction extends BaseFunction
  {
    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj,
      Object[] args)
    {
      if (args.length > 0)
      {
        try
        {
          Connection conn;
          if (args[0] instanceof String)
          {
            String dataSource = (String)args[0];
            logger.log(Level.INFO, "dataSource: {0}", dataSource);
            javax.naming.Context initContext = new InitialContext();
            javax.naming.Context envContext  =
               (javax.naming.Context)initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource)envContext.lookup(dataSource);
            conn = ds.getConnection();
          }
          else 
          conn = (Connection)args[0];
          conn.setAutoCommit(false);
          thisObj.put(CONNECTION_VAR, thisObj, conn);
          return true;
        }
        catch (Exception ex)
        {
          logger.log(Level.SEVERE, "Error: {0}", ex.toString());
          throw new JavaScriptException(ex.getMessage(), "DBConnection.open", 0);
        }
      }
      return false;
    }
  }

  class ExecuteFunction extends BaseFunction
  {
    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj,
      Object[] args)
    {
      Object result = null;
      if (args.length > 0)
      {
        try
        {
          String sql = (String)args[0];
          Scriptable parameters = null;
          if (args.length >= 2)
          {
            parameters = (Scriptable)args[1];
          }
          Connection conn = (Connection)thisObj.get(CONNECTION_VAR , scope);
          result = executeStatement(conn, sql, parameters, cx, scope);
        }
        catch (Exception ex)
        {
          logger.log(Level.SEVERE, "Error: {0}", ex.toString());
          throw new JavaScriptException(ex.getMessage(), "DBConnection.execute", 0);
        }
      }
      return result;
    }

    private Object executeStatement(Connection conn, String sql,
       Scriptable parameters, Context cx, Scriptable scope) throws Exception
    {
      logger.log(Level.INFO, "Executing {0}", sql);
      Object result = null;
      PreparedStatement stmt = conn.prepareStatement(sql);
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
            // TODO: fix conversion
            Object javaValue = Context.jsToJava(value, java.lang.String.class);
            logger.log(Level.INFO, "Parameter index {0} = {1}",
              new Object[]{paramIndex, javaValue});
            stmt.setObject(paramIndex, javaValue);
          }
        }
      }
    }
  }

  class CommitFunction extends BaseFunction
  {
    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj,
      Object[] args)
    {
      try
      {
        Connection conn = (Connection)thisObj.get(CONNECTION_VAR, scope);
        if (conn != null)
        {
          conn.commit();
          return true;
        }
      }
      catch (Exception ex)
      {
        logger.log(Level.SEVERE, "Error: {0}", ex.toString());
      }
      return false;
    }
  }

  class RollbackFunction extends BaseFunction
  {
    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj,
      Object[] args)
    {
      try
      {
        Connection conn = (Connection)thisObj.get(CONNECTION_VAR, scope);
        if (conn != null)
        {
          conn.rollback();
          return true;
        }
      }
      catch (Exception ex)
      {
        logger.log(Level.SEVERE, "Error: {0}", ex.toString());
      }
      return false;
    }
  }

  class CloseFunction extends BaseFunction
  {
    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj,
      Object[] args)
    {
      try
      {
        Connection conn = (Connection)thisObj.get(CONNECTION_VAR, scope);
        if (conn != null)
        {
          conn.close();
          thisObj.delete(CONNECTION_VAR);
          return true;
        }
      }
      catch (Exception ex)
      {
        logger.log(Level.SEVERE, "Error: {0}", ex.toString());
      }
      return false;
    }
  }
}

