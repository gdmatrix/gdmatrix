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
package org.santfeliu.misc.sqlweb.web;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;
import org.santfeliu.faces.beansaver.Savable;
import org.santfeliu.web.WebBean;
import org.santfeliu.util.Task;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.bean.CMSProperty;

/**
 *
 * @author realor
 */
@CMSManagedBean
public class SqlWebBean extends WebBean implements Savable
{
  @CMSProperty
  public static final String JDBC_DRIVER_PROPERTY = "jdbc_driver";
  @CMSProperty
  public static final String JDBC_URL_PROPERTY = "jdbc_url";
  @CMSProperty
  public static final String JDBC_USERNAME_PROPERTY = "jdbc_username";

  private static int RETAIN_MILLIS = 20000;
  private String driver;
  private String url;
  private String username;
  private String password;
  private String sql;
  private int maxRows = 1000;
  private boolean showRowNumbers = true;
  private String taskId;
  private boolean editMode = true;

  public SqlWebBean()
  {
    driver = getProperty(JDBC_DRIVER_PROPERTY);
    url = getProperty(JDBC_URL_PROPERTY);
    username = getProperty(JDBC_USERNAME_PROPERTY);
  }

  public boolean isShowRowNumbers()
  {
    return showRowNumbers;
  }

  public void setShowRowNumbers(boolean showRowNumbers)
  {
    this.showRowNumbers = showRowNumbers;
  }
  
  public boolean isEditMode()
  {
    return editMode;
  }
  
  public void setEditMode(boolean editMode)
  {
    this.editMode = editMode;
  }
  
  public String getDriver()
  {
    return driver;
  }

  public void setDriver(String driver)
  {
    this.driver = driver;
  }

  public String getUrl()
  {
    return url;
  }

  public void setUrl(String url)
  {
    this.url = url;
  }

  public String getUsername()
  {
    return username;
  }

  public void setUsername(String username)
  {
    this.username = username;
  }

  public String getPassword()
  {
    return password;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }

  public String getSql()
  {
    return sql;
  }

  public void setSql(String sql)
  {
    this.sql = sql;
  }
  
  public int getMaxRows()
  {
    return maxRows;
  }

  public void setMaxRows(int maxRows)
  {
    this.maxRows = maxRows;
  }

  public String getTaskId()
  {
    return taskId;
  }

  public SQLTask getTask()
  {
    return taskId != null ? (SQLTask)Task.getInstance(taskId) : null;
  }
    
  public String show()
  {
    return "sqlweb";
  }

  public String showFullscreen()
  {
    return "sqlweb_fs";
  }

  public void showResult()
  {
  }
  
  public void execute()
  {
    SQLTask task = new SQLTask();
    task.start();
    taskId = task.getTaskId();
    task.waitForTermination(1000);
  }

  public void abort()
  {
    Task task = getTask();
    if (task != null)
    {
      task.stop();
      task.waitForTermination(1000);
    }
  }

  public class SQLTask extends Task
  {
    private Column[] columns;
    private List<Object[]> rows;
    private int updateCount = -1;
    private Connection connection;
    private Statement statement;

    public SQLTask()
    {
      setRetainMillis(RETAIN_MILLIS);
    }

    @Override
    public void execute() throws Exception
    {
      setMessage("Connecting to database...");
      columns = null;
      rows = null;
      updateCount = -1;
      Class.forName(driver);
      connection = DriverManager.getConnection(url, username, password);
      try
      {
        connection.setAutoCommit(false);
        statement = connection.createStatement();
        try
        {
          statement.setMaxRows(maxRows);
          setMessage("Executing statement...");
          if (statement.execute(sql))
          {
            if (!isCancelled())
            {
              setMessage("Reading data...");
              ResultSet rs = statement.getResultSet();
              try
              {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                columns = new Column[columnCount];
                for (int i = 1; i <= columnCount; i++)
                {
                  columns[i - 1] = new Column(i - 1,
                    metaData.getColumnName(i),
                    metaData.getColumnTypeName(i));
                }
                rows = new ArrayList<Object[]>();
                while (rs.next() && !isCancelled())
                {
                  Object[] row = new Object[columnCount];
                  rows.add(row);
                  for (int i = 1; i <= columnCount; i++)
                  {
                    Object value = rs.getObject(i);
                    if (value == null)
                    {
                      value = "NULL";
                    }
                    else if (value instanceof java.sql.Date ||
                      value instanceof java.sql.Timestamp)
                    {
                      value = rs.getString(i);
                    }
                    else if (value instanceof Struct)
                    {
                      Struct struct = (Struct)value;
                      value = struct.getSQLTypeName();
                    }
                    else if (!(value instanceof Serializable))
                    {
                      value = value.toString();
                    }
                    row[i - 1] = value;
                  }
                }
                if (isCancelled())
                {
                  setMessage("Execution cancelled.");                
                }
                else
                {
                  setMessage("Execution completed, sending result...");
                }
              }
              finally
              {
                rs.close();
              }
            }
          }
          else if (!isCancelled()) 
          {
            updateCount = statement.getUpdateCount();
          }
        }
        finally
        {
          statement.close();
          statement = null;
        }
      }
      finally
      {
        try
        {
          if (isCancelled())
          {
            connection.rollback();
          }
          else
          {
            connection.commit();
          }
          connection.close();
        }
        finally
        {
          connection = null;
        }
      }
    }
    
    @Override
    public boolean cancel()
    {
      try
      {
        if (statement != null && !statement.isClosed())
        {
          statement.cancel();
          setMessage("Statement cancelled.");
        }
      }
      catch (Throwable t)
      {
      }
      return true;
    }

    public Column[] getColumns()
    {
      return columns;
    }

    public List<Object[]> getRows()
    {
      return rows;
    }

    public int getRowCount()
    {
      return rows == null ? 0 : rows.size();
    }
    
    public int getUpdateCount()
    {
      return updateCount;
    }
  }
  
  public Object getColumnValue()
  {
    Column column = (Column)getValue("#{column}");
    Object[] row = (Object[])getValue("#{row}");
    return row[column.getIndex()];
  }

  public boolean isColumnLinkRendered()
  {
    Column column = (Column)getValue("#{column}");
    Object[] row = (Object[])getValue("#{row}");
    Object value = row[column.getIndex()];
    if (value instanceof String)
    {
      return ((String)value).startsWith("http");
    }
    return false;
  }
    
  public String getColumnType()
  {
    Column column = (Column)getValue("#{column}");
    return column.getType();
  }

  public String getColumnClasses()
  {
    SQLTask task = getTask();
    if (task == null) return null;
    StringBuilder buffer = new StringBuilder();
    for (Column column : task.columns)
    {
      if (buffer.length() > 0) buffer.append(",");
      buffer.append("type_").append(column.getType());
    }
    return buffer.toString();
  }

  public class Column implements Serializable
  {
    private final int index;
    private final String name;
    private final String type;

    public Column(int index, String name, String type)
    {
      this.index = index;
      this.name = name;
      this.type = type;
    }

    public int getIndex()
    {
      return index;
    }

    public String getName()
    {
      return name;
    }

    public String getType()
    {
      return type;
    }
  }
}
