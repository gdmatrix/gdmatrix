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
package org.santfeliu.webapp.modules.sqlweb;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Struct;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.commons.lang.StringUtils;
import org.primefaces.component.export.CSVOptions;
import org.primefaces.component.export.ExporterOptions;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.web.bean.CMSProperty;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class SqlwebBean extends WebBean implements Serializable
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
  private boolean deferredExecution = false;
  private boolean showLobValues = false;
  private boolean showNullAsEmpty = false;
  private int firstRow = 0;

  public SqlwebBean()
  {
  }

  @PostConstruct
  public void init()
  {    
    restoreParameters();
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

  public int getFirstRow()
  {
    return firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    this.firstRow = firstRow;
  }

  public Map<String, String> getColumnDescriptionMap()
  {
    if (columnDescriptionMap == null)
    {
      columnDescriptionMap = new HashMap();
    }
    return columnDescriptionMap;
  }

  public boolean isDeferredExecution()
  {
    return deferredExecution;
  }

  public void setDeferredExecution(boolean deferredExecution)
  {
    this.deferredExecution = deferredExecution;
  }

  public boolean isShowLobValues()
  {
    return showLobValues;
  }

  public void setShowLobValues(boolean showLobValues)
  {
    this.showLobValues = showLobValues;
  }

  public boolean isShowNullAsEmpty()
  {
    return showNullAsEmpty;
  }

  public void setShowNullAsEmpty(boolean showNullAsEmpty)
  {
    this.showNullAsEmpty = showNullAsEmpty;
  }

  public boolean isDsnEnabled()
  {
    return UserSessionBean.getCurrentInstance().isUserInRole("QUERY_EDITOR");
  }
  
  public String getContent()
  {
    return "/pages/sqlweb/sqlweb.xhtml";
  }

  public String show()
  {
    String template = UserSessionBean.getCurrentInstance().getTemplate();
    return "/templates/" + template + "/template.xhtml";
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
    firstRow = 0;
    rows = null;
    updateCount = -1;
    exception = null;
    long t0 = System.currentTimeMillis();
    try
    {      
      try (Connection conn = getConnection())
      {
        conn.setAutoCommit(false);
        try (Statement statement = conn.createStatement())
        {
          statement.setMaxRows(maxRows);
          if (statement.execute(sql)) // is query
          {
            try (ResultSet rs = statement.getResultSet())
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
                    //nothing here
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
                  else if (value instanceof Blob)
                  {
                    value = getBlobPreview((Blob)value, 200);
                  }
                  else if (value instanceof Clob)
                  {
                    value = getClobPreview((Clob)value, 200);
                  }
                  else if (!(value instanceof Serializable))
                  {
                    value = value.toString();
                  }
                  row[i - 1] = value;
                }
              }
            }
          }
          else // is update
          {
            updateCount = statement.getUpdateCount();
          }          
          if (deferredExecution) 
            removeParameters();
          else
            saveParameters();
        }
      }
    }
    catch (ClassNotFoundException | SQLException | NamingException ex)
    {
      exception = ex.getMessage();
      error(exception);
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

  public String getTimeStamp()
  {
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");
    return df.format(new Date());
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
    Object value = row[column.getIndex()];
    if (value == null)
    {
      value = (showNullAsEmpty ? "" : "NULL");
    }
    return value;
  }
  
  public String getCellStyleClass()
  {
    Column column = (Column)getValue("#{column}");
    Object[] row = (Object[])getValue("#{row}");
    Object value = row[column.getIndex()];
    if ((column.isLob() && !showLobValues) || 
      (value == null && !showNullAsEmpty))
    {
      return "specialValue";
    }
    else
    {
      return "";
    }
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
    
    public boolean isLob()
    {
      return ("CLOB".equals(type) || "BLOB".equals(type));
    }
  }

  public ExporterOptions getCsvOptions()
  {
    return CSVOptions.EXCEL_NORTHERN_EUROPE;
  }

  private String getClobPreview(Clob clob, int numChars)
  {
    StringBuilder sb = new StringBuilder();
    try (Reader reader = clob.getCharacterStream();
       BufferedReader br = new BufferedReader(reader))      
    {
      char[] buffer = new char[numChars];
      br.read(buffer);
      sb.append(buffer);
    } 
    catch (Exception ex)
    {
    }
    String result = sb.toString();
    if (result.length() >= numChars) result = result + "...";
    return result;
  }
  
  private String getBlobPreview(Blob blob, int numChars)
  {
    byte[] bytes = new byte[numChars];
    int bytesRead = 0;
    try (InputStream is = blob.getBinaryStream();
      BufferedInputStream bis = new BufferedInputStream(is)) 
    {
      bytesRead = bis.read(bytes, 0, numChars);
    } 
    catch (Exception e) 
    {
    }
    byte[] trimmedBytes = new byte[bytesRead];
    System.arraycopy(bytes, 0, trimmedBytes, 0, bytesRead);
    String result = new String(trimmedBytes, StandardCharsets.UTF_8);
    if (result.length() >= numChars) result = result + "...";    
    return result;
  }
  
  private void restoreParameters()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    dsn = (String)userSessionBean.getAttribute("sqlweb.dsn");
    driver = (String)userSessionBean.getAttribute("sqlweb.driver");
    url = (String)userSessionBean.getAttribute("sqlweb.url");
    username = (String)userSessionBean.getAttribute("sqlweb.username");
    password = (String)userSessionBean.getAttribute("sqlweb.password");
    sql = (String)userSessionBean.getAttribute("sqlweb.sql");

    if (dsn == null) dsn = getProperty(JDBC_DSN_PROPERTY);    
    if (driver == null) driver = getProperty(JDBC_DRIVER_PROPERTY);
    if (url == null) url = getProperty(JDBC_URL_PROPERTY);
    if (username == null) username = getProperty(JDBC_USERNAME_PROPERTY);
  }

  private void saveParameters()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    userSessionBean.setAttribute("sqlweb.dsn", dsn);
    userSessionBean.setAttribute("sqlweb.driver", driver);
    userSessionBean.setAttribute("sqlweb.url", url);
    userSessionBean.setAttribute("sqlweb.username", username);
    userSessionBean.setAttribute("sqlweb.password", password);
    userSessionBean.setAttribute("sqlweb.sql", sql);
  }
  
  private void removeParameters()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    userSessionBean.getAttributes().remove("sqlweb.dsn");
    userSessionBean.getAttributes().remove("sqlweb.driver");
    userSessionBean.getAttributes().remove("sqlweb.url");
    userSessionBean.getAttributes().remove("sqlweb.username");
    userSessionBean.getAttributes().remove("sqlweb.password");
    userSessionBean.getAttributes().remove("sqlweb.sql");
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
}
