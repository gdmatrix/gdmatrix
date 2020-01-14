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
package org.santfeliu.ant.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Sequential;
import org.santfeliu.ant.js.ScriptableTask;

/**
 *
 * @author realor
 */
public class DBExecuteTask extends ScriptableTask
{
  private String connVar;
  private Statement statement;
  private boolean commit;
  private String updateCountVar;
  private File script;
  private Sequential read;
  private int maxRows;
  private File result;
  private String rootTag = "result";
  private String rowTag = "row";
  private String resultsVar;

  public String getConnVar()
  {
    return connVar;
  }

  public void setConnVar(String connVar)
  {
    this.connVar = connVar;
  }

  public boolean isCommit()
  {
    return commit;
  }

  public void setCommit(boolean commit)
  {
    this.commit = commit;
  }

  public int getMaxRows()
  {
    return maxRows;
  }

  public void setMaxRows(int maxRows)
  {
    this.maxRows = maxRows;
  }

  public String getUpdateCountVar()
  {
    return updateCountVar;
  }

  public void setUpdateCountVar(String updateCountVar)
  {
    this.updateCountVar = updateCountVar;
  }

  public void addStatement(Statement statement)
  {
    this.statement = statement;
  }

  public void addRead(Sequential seq)
  {
    this.read = seq;
  }

  public File getScript()
  {
    return script;
  }

  public void setScript(File script)
  {
    this.script = script;
  }

  public File getResult()
  {
    return result;
  }

  public void setResult(File result)
  {
    this.result = result;
  }

  public String getRootTag()
  {
    return rootTag;
  }

  public void setRootTag(String rootTag)
  {
    this.rootTag = rootTag;
  }

  public String getRowTag()
  {
    return rowTag;
  }

  public void setRowTag(String rowTag)
  {
    this.rowTag = rowTag;
  }

  public String getResultsVar()
  {
    return resultsVar;
  }

  public void setResultsVar(String resultsVar)
  {
    this.resultsVar = resultsVar;
  }

  @Override
  public void execute() throws BuildException
  {
    if (connVar == null) 
      throw new BuildException("Attribtue 'connVar' is required");
    if (script == null && statement == null) throw new BuildException(
        "Attribute 'script' or nested element 'statement' is required");
    try
    {
      Connection conn = (Connection)getVariable(connVar);
      if (conn != null)
      {
        if (script != null)
        {
          executeScript(conn);
        }
        if (statement != null)
        {
          executeStatement(conn);
        }        
      }
      else throw new BuildException("connection undefined");
    }
    catch (BuildException ex)
    {
      throw ex;
    }
    catch (Exception ex)
    {
      throw new BuildException(ex);
    }
  }

  private void executeScript(Connection conn) throws Exception
  {
    StringBuilder builder = new StringBuilder();
    if (!script.exists()) throw new BuildException("script not found");

    java.sql.Statement stmt = conn.createStatement();
    try
    {
      BufferedReader reader = new BufferedReader(new InputStreamReader(
        new FileInputStream(script)));
      try
      {
        String line = reader.readLine();
        while (line != null)
        {
          line = line.trim();
          if (line.length() > 0 && !line.startsWith("--"))
          {
            line = getProject().replaceProperties(line); // replace props
            if (line.endsWith(";"))
            {
              line = line.substring(0, line.length() - 1); // remove ;
              builder.append(line);
              String sql = builder.toString();
              log("Adding batch sql: " + sql);
              stmt.addBatch(sql);
              builder.setLength(0);
            }
            else
            {
              builder.append(line).append(" "); // sql continues next line
            }
          }
          line = reader.readLine();
        }
      }
      finally
      {
        reader.close();
      }
      log("Executing script: " + script.getAbsolutePath());
      stmt.executeBatch();
    }
    finally
    {
      stmt.close();
    }
  }

  private void executeStatement(Connection conn) throws Exception
  {
    String parse = null;
    Vector<String> params = null;

    String name = getTaskName();
    String parseName = (name == null) ? null : name + "$parse";
    String paramsName = (name == null) ? null : name + "$params";

    if (name != null)
    {
      if (hasVariable(parseName)) parse = (String)getVariable(parseName);
      if (hasVariable(paramsName)) params = (Vector)getVariable(paramsName);
    }

    if (parse == null || params == null)
    {
      params = new Vector();
      String sql = getProject().replaceProperties(statement.getSql());
      parse = parse(sql, params);
    }

    if (name != null)
    {
      setVariable(parseName, parse);
      setVariable(paramsName, params);
    }

    // execute
    PreparedStatement prepStmt = conn.prepareStatement(parse);
    try
    {
      setParameters(prepStmt, params);
      prepStmt.setMaxRows(maxRows);
      boolean isQuery = prepStmt.execute();
      if (commit) conn.commit();
      if (isQuery)
      {
        readResultSet(prepStmt);
      }
      else // is update
      {
        if (updateCountVar != null)
        {
          int updateCount = prepStmt.getUpdateCount();
          setVariable(updateCountVar, updateCount);
        }
      }
    }
    finally
    {
      prepStmt.close();
    }
  }

  private void setParameters(PreparedStatement prepStmt,
    Vector<String> params) throws SQLException
  {
    for (int i = 0; i < params.size(); i++)
    {
      String var = params.get(i);
      Object value = getVariable(var);      
      if (value == null)
      {
        prepStmt.setNull(i + 1, java.sql.Types.VARCHAR);
      }
      else
      {
        System.out.println(var + " = " + value + " " + value.getClass().getName());
        prepStmt.setObject(i + 1, value);
      }
    }
  }

  private void readResultSet(PreparedStatement prepStmt)
    throws Exception
  {
    PrintWriter writer = null;
    if (result != null)
    {
      writer = new PrintWriter(result, "UTF-8");
      writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      writer.println("<" + rootTag + ">");
    }
    try
    {
      // query
      ResultSet rs = prepStmt.getResultSet();
      try
      {
        ResultSetMetaData md = rs.getMetaData();
        while (rs.next())
        {
          if (writer != null)
          {
            writer.println("  <" + rowTag + ">");
          }
          // read fields
          Map row = new HashMap();
          for (int i = 0; i < md.getColumnCount(); i++)
          {
            Object value = rs.getObject(i + 1);
            String columnName = md.getColumnName(i + 1);
            setVariable(columnName, value);
            row.put(columnName, value);
            if (writer != null)
            {
              writer.print("    <" + columnName + ">");
              if (value != null) writer.print(value.toString().trim());
              writer.println("</" + columnName + ">");
            }
          }
          
          if (!row.isEmpty())
            setVariable(resultsVar, row);
          
          // execute inner statements
          if (read != null) read.perform();
          if (writer != null)
          {
            writer.println("  </" + rowTag + ">");
          }
        }
      }
      finally
      {
        rs.close();
      }
    }
    finally
    {
      if (writer != null)
      {
        writer.println("</" + rootTag + ">");
        writer.close();
      }
    }
  }

  private String parse(String statement, Vector<String> params) throws Exception
  {
    StringBuilder bufferSql = new StringBuilder();
    StringBuilder bufferParam = new StringBuilder();
    int index = 0;
    int state = 0;
    while (index < statement.length())
    {
      char ch = statement.charAt(index++);
      switch (state)
      {
        case 0:
          if (ch == '{')
          {
            bufferParam.setLength(0);
            state = 1;
          }
          else
          {
            bufferSql.append(ch);
            if (ch == '\'') state = 2;
            else if (ch == '\"') state = 3;
          }
          break;
        case 1:
          if (ch == '}')
          {
            bufferSql.append("?");
            params.add(bufferParam.toString());
            state = 0;
          }
          else bufferParam.append(ch);
          break;
        case 2:
          if (ch == '\'')
          {
            state = 0;
          }
          bufferSql.append(ch);
          break;
        case 3:
          if (ch == '\"')
          {
            state = 0;
          }
          bufferSql.append(ch);
          break;
      }
    }
    if (state != 0) throw new Exception("PARSE_ERROR");
    return bufferSql.toString();
  }
}
