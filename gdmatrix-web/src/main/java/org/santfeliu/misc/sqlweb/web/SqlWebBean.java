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
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.faces.beansaver.Savable;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
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
  public static final String JDBC_DSN_PROPERTY = "jdbc_dsn";  
  @CMSProperty
  public static final String JDBC_DRIVER_PROPERTY = "jdbc_driver";
  @CMSProperty
  public static final String JDBC_URL_PROPERTY = "jdbc_url";
  @CMSProperty
  public static final String JDBC_USERNAME_PROPERTY = "jdbc_username";

  private String title;
  private String dsn;
  private String driver;
  private String url;
  private String username;
  private String password;
  private String sql;
  private int maxRows = 1000;
  private boolean showRowNumbers = true;
  private boolean editMode = true;
  private Column[] columns;
  private List<Object[]> rows;
  private int updateCount = -1;
  private long duration = 0;
  private String exception;
  private Map<String, String> columnDescriptionMap;
  private transient boolean autoExecute;  

  public SqlWebBean()
  {
    dsn = getProperty(JDBC_DSN_PROPERTY);
    driver = getProperty(JDBC_DRIVER_PROPERTY);
    url = getProperty(JDBC_URL_PROPERTY);
    username = getProperty(JDBC_USERNAME_PROPERTY);
  }

  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
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

  public String getDsn()
  {
    return dsn;
  }

  public void setDsn(String dsn)
  {
    this.dsn = dsn;
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

  public boolean isDsnEnabled()
  {
    return UserSessionBean.getCurrentInstance().isUserInRole("QUERY_EDITOR");
  }  
  
  public Map<String, String> getColumnDescriptionMap() 
  {
    if (columnDescriptionMap == null)
    {
      columnDescriptionMap = new HashMap();
    }
    return columnDescriptionMap;
  }

  public boolean isAutoExecute()
  {
    return autoExecute;
  }

  public void setAutoExecute(boolean autoExecute)
  {
    this.autoExecute = autoExecute;
  }

  public String getAutoExecuteCode()
  {
    return "<script type='text/javascript'>autoExecute=" +
      autoExecute + ";</script>";
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
    columns = null;
    rows = null;
    updateCount = -1;
    exception = null;
    long t0 = System.currentTimeMillis();
    try
    {
      Connection conn = getConnection();
      try
      {
        conn.setAutoCommit(false);
        Statement statement = conn.createStatement();
        try
        {
          statement.setMaxRows(maxRows);
          if (statement.execute(sql)) // is query
          {
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
              rows = new ArrayList();
              while (rs.next())
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
                  else if (value instanceof Blob || value instanceof Clob)
                  {
                    value = "(" + value.getClass().getSimpleName() + ")";
                  }
                  else if (!(value instanceof Serializable))
                  {
                    value = value.toString();
                  }
                  row[i - 1] = value;
                }
              }
            }
            finally
            {
              rs.close();
            }
          }
          else // is update
          {
            updateCount = statement.getUpdateCount();
          }
        }
        finally
        {
          statement.close();
        }
      }
      finally
      {
        conn.close();
      }
    }
    catch (ClassNotFoundException | SQLException | NamingException ex)
    {
      exception = ex.getMessage();
    }
    long t1 = System.currentTimeMillis();
    duration = t1 - t0;
  }

  public String getException()
  {
    return exception;
  }

  public long getDuration()
  {
    return duration;
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
    StringBuilder buffer = new StringBuilder();
    for (Column column : columns)
    {
      if (buffer.length() > 0) buffer.append(",");
      buffer.append("type_").append(column.getType());
    }
    return buffer.toString();
  }
  
  private Connection getConnection() 
    throws NamingException, SQLException, ClassNotFoundException
  {
    if (!StringUtils.isBlank(dsn))
    {
      javax.naming.Context initContext = new InitialContext();
      javax.naming.Context envContext  =
         (javax.naming.Context)initContext.lookup("java:/comp/env");
      DataSource ds = (DataSource)envContext.lookup(dsn);
      return ds.getConnection();      
    }
    else
    {
      Class.forName(driver);
      return DriverManager.getConnection(url, username, password);
    }
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
    
    public String getDescription()
    {
      return getColumnDescriptionMap().get(name);
    }
  }
}
