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
package org.santfeliu.dbf;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.santfeliu.util.Table;

/**
 *
 * @author realor
 */
public class DBConnection
{
  private static final Logger LOGGER = Logger.getLogger("DBConnection");
  
  public static final String ORACLE_VENDOR = "oracle";
  public static final String SQLSERVER_VENDOR = "sqlserver";
  public static final String MYSQL_VENDOR = "mysql";
  public static final String POSTGRES_VENDOR = "postgres";
  public static final String OTHER_VENDOR = "other";

  private final DBRepository repository;
  private final Connection jdbcConn;
  private final HashMap objectVars;
  private boolean smartUpdate;
  private boolean returnLobsAsFiles;
  private int maxRows;

  DBConnection(DBRepository repository, Connection jdbcConn)
  {
    this.repository = repository;
    this.jdbcConn = jdbcConn;
    this.objectVars = new HashMap();
  }

  public String getDatabaseVendor() throws DBException
  {
    try
    {
      String vendor;
      String productName = jdbcConn.getMetaData().getDatabaseProductName();
      productName = productName.toLowerCase();
      if (productName.contains("oracle"))
      {
        vendor = ORACLE_VENDOR;
      }
      else if (productName.contains("mysql") || productName.contains("mariadb"))
      {
        vendor = MYSQL_VENDOR;
      }
      else if (productName.contains("postgres"))
      {
        vendor = POSTGRES_VENDOR;
      }
      else if (productName.contains("sqlserver"))
      {
        vendor = SQLSERVER_VENDOR;
      }
      else
      {
        vendor = OTHER_VENDOR;
      }
      return vendor;
    }
    catch (SQLException ex)
    {
      throw DBException.createException(ex);
    }
  }

  public String getTableName(Object object) throws DBException
  {
    throw new DBException("NOT_IMPLEMENTED_YET");
  }

  /**
   * Returns the metadata of a table.
   * @param tableName is the name of the table. A schema prefix
   * can be specified: <code>schema.tableName</code>.
   * @return a <code>DBTableMetaData</code> for table <code>tableName</code> or
   * <code>null</code> if <code>tableName</code> was not found.
   * @throws DBException
   */
  public DBTableMetaData getMetaData(String tableName) throws DBException
  {
    DBTableMetaData metaData = null;
    tableName = tableName.toUpperCase();
    metaData = repository.getTableMetaData(tableName);
    if (metaData == null)
    {
      try
      {
        String query = "SELECT * FROM " + tableName + " WHERE 0=1";
        Statement stmt = jdbcConn.createStatement();
        try
        {
          ResultSet rs = stmt.executeQuery(query);
          try
          {
            ResultSetMetaData rsMetaData = rs.getMetaData();
            String[] columnNames = new String[rsMetaData.getColumnCount()];
            Class[] columnClasses = new Class[rsMetaData.getColumnCount()];
            readMetaData(rsMetaData, columnNames, columnClasses);

            int[] primaryKeyColumnIndices = readPrimaryKeyColumnIndices(
              jdbcConn.getMetaData(), tableName, columnNames);

            if (primaryKeyColumnIndices.length == 0)
              throw new DBException("Primary key undefined: " + tableName);

            metaData = new DBTableMetaData(tableName,
              columnNames, columnClasses, primaryKeyColumnIndices);
            repository.metaDatas.put(tableName, metaData);
          }
          finally
          {
            rs.close();
          }
        }
        finally
        {
          stmt.close();
        }
      }
      catch (Exception ex)
      {
        throw DBException.createException(ex);
      }
    }
    return metaData;
  }

  /**
   * Selects the row from table <code>tableName</code> identified by
   * <code>primaryKey</code> Object.
   * @param tableName is the name of the table to read from. A schema prefix
   * can be specified: <code>schema.tableName</code>.
   * @param primaryKey is a DBKey object that identifies the
   * row to be selected.
   * @return a <code>Map</code> containing one entry
   * {<code>columnName</code>, <code>value</code>} for each column in the
   * table, where <code>columnName</code> is a <code>String</code> in
   * lowercase and <code>value</code> is an object of class <code>DBConnection.
   * getMetaData(tableName).getColumnClass(columnName)</code>
   * or returns <code>null</code> if there is no row identified by this <code>
   * primaryKey</code>.
   * @throws DBException
   */
  public Map selectMap(String tableName, DBKey primaryKey)
    throws DBException
  {
    try
    {
      DBTableMetaData metaData = getMetaData(tableName);
      objectVars.clear();
      putKey(objectVars, primaryKey);
      Map object = executeSelectMap(metaData.selectStatement, objectVars);
      return object;
    }
    catch (Exception ex)
    {
      throw DBException.createException(ex);
    }
  }

  public Object selectBean(String tableName, DBKey primaryKey)
    throws DBException
  {
    throw new DBException("NOT_IMPLEMENTED_YET");

  }

  public Object[] selectBeans(String tableName, String filter)
    throws DBException
  {
    throw new DBException("NOT_IMPLEMENTED_YET");
  }

  /**
   * Inserts a row in table <code>tableName</code> with values contained in
   * <code>object</code>.
   * @param tableName is the name of the table to insert into. A schema prefix
   * can be specified: <code>schema.tableName</code>.
   * @param object is a <code>Map</code> containing one entry
   * {<code>columnName</code>, <code>value</code>} for each column of
   * <code>tableName</code> where <code>columnName</code> is a
   * <code>String</code> in lowercase and <code>value</code> is an instance
   * of a class valid for that column. Most JDBC drivers do automatic type
   * conversion when possible and do not require that the type of
   * <code>value</code> exactly match the type of the column. Values of type
   * <code>java.io.InputStream</code> or <code>java.io.File</code> are
   * accepted for LOB columns.
   * @return a <code>DBKey</code> object that holds the primary key of
   * the inserted row.
   * @throws DBException
   */
  public DBKey insert(String tableName, Map object) throws DBException
  {
    try
    {
      DBTableMetaData metaData = getMetaData(tableName);
      objectVars.clear();
      objectVars.put("o", object);
      int numUpdated = executeUpdate(metaData.insertStatement, objectVars);
      return getPrimaryKey(tableName, object);
    }
    catch (Exception ex)
    {
      throw DBException.createException(ex);
    }
  }

  public DBKey insert(Object object) throws DBException
  {
    throw new DBException("NOT_IMPLEMENTED_YET");
  }

  /**
   * Updates the row of table <code>tableName</code> identified by
   * <code>primaryKey</code> with values contained in <code>object</code>.
   * When smartUpdate is enabled, only will be updated that columns that
   * have an entry {<code>columnName</code>, <code>value</code>}
   * inside <code>object</code>. If smartUpdate is disabled,
   * <code>object</code> must contain such an entry for each column in
   * the table. <code>columnName</code> is a <code>String</code> in lowercase
   * and <code>value</code> is an instance of a class valid for that column.
   * Most JDBC drivers do automatic type conversion when possible and do not
   * require that the type of <code>value</code> exactly match the type of
   * the column. Values of type <code>java.io.InputStream</code> or
   * <code>java.io.File</code> are accepted for LOB columns.
   * @param tableName is the name of the table to update. A schema prefix
   * can be specified: <code>schema.tableName</code>.
   * @param primaryKey is the <code>DBKey</code> object that identifies the
   * row to update.
   * @param object is a <code>Map</code> with the values to update.
   * @return a DBKey that identifies the updated row or <code>null</code> if no
   * row was updated.
   * @throws DBException
   */
  public DBKey update(String tableName, DBKey primaryKey, Map object)
    throws DBException
  {
    try
    {
      DBTableMetaData metaData = getMetaData(tableName);
      objectVars.clear();
      objectVars.put("o", object);
      putKey(objectVars, primaryKey);
      DBStatement statement = smartUpdate ?
        buildSmartUpdateStatement(metaData, object) :
        metaData.updateStatement;
      if (statement == null) //nothing to update
      {
        return primaryKey;
      }
      else
      {
        int numUpdated = executeUpdate(statement, objectVars);
        return (numUpdated == 0) ? null :
          getPrimaryKey(metaData, object, primaryKey);
      }
    }
    catch (Exception ex)
    {
      throw DBException.createException(ex);
    }
  }

  public DBKey update(DBKey primaryKey, Object object) throws DBException
  {
    throw new DBException("NOT_IMPLEMENTED_YET");
  }

  /**
   * Deletes the row of <code>tableName</code> identified by
   * <code>primaryKey</code>.
   * @param tableName is the name of the table to delete from. A schema prefix
   * can be specified: <code>schema.tableName</code>.
   * @param primaryKey is a <code>DBKey</code> object that identifies
   * the row to delete.
   * @return <code>true</code> if row was deleted, or <code>false</code>
   * otherwise.
   * @throws DBException
   */
  public boolean delete(String tableName, DBKey primaryKey) throws DBException
  {
    try
    {
      DBTableMetaData metaData = getMetaData(tableName);
      objectVars.clear();
      putKey(objectVars, primaryKey);
      int numUpdated = executeUpdate(metaData.deleteStatement, objectVars);
      return numUpdated > 0;
    }
    catch (Exception ex)
    {
      throw DBException.createException(ex);
    }
  }

  /**
   * Executes a custom SQL select statement with parameters.
   * <p>
   * Example:<br>
   * <code>
   * ...<br>
   * Map parameters = new HashMap();<br>
   * parameters.put("name", "JOHN");<br>
   * Table table = conn.executeQuery(<br>
   *   "select * from emp where empname = {name}", parameters);<br>
   * ...
   * </code>
   * </p>
   * @param statement is a <code>String</code> with a valid SQL statement with
   * parameters. A parameter is expressed between braces and suports this
   * formats:
   * <ul>
   * <li>{<code>key</code>}: where <code>key</code> is the key in Map
   * <code>parameters</code> that contains the value of this parameter.
   * <li>{<code>mapKey.key</code>}: where <code>mapKey</code>
   * references a <code>Map</code> in <code>parameters</code> that has a key
   * <code>key</code> that contains the value of this parameter.
   * <li>{<code>beanKey.propertyName</code>}: where <code>beanKey</code>
   * references a java bean in <code>parameters</code> that has a property
   * named <code>propertyName</code> that returns the value of this parameter.
   * </ul>
   * @param parameters contains the values of the parameters referenced in
   * <code>statement</code>.
   * @return a <code>Table</code> that contains the rows returned by this
   * <code>statement</code>.
   * @throws DBException
   */
  public Table executeQuery(String statement, Map parameters) throws DBException
  {
    try
    {
      DBStatement dbStmt = repository.getStatement(statement);
      Table table = executeSelectTable(dbStmt, parameters);
      return table;
    }
    catch (Exception ex)
    {
      throw DBException.createException(ex);
    }
  }

  /**
   * Executes a custom SQL insert/update/delete statement with parameters.
   * <p>
   * Example:<br>
   * <code>
   * ...<br>
   * Map parameters = new HashMap();<br>
   * parameters.put("dept", "SALES");<br>
   * Table table = conn.executeQuery(<br>
   *   "update emp set income = income * 2 where dept = {dept}", parameters);<br>
   * ...
   * </code>
   * </p>
   * @param statement is a <code>String</code> with a valid SQL statement with
   * parameters. A parameter is expressed between braces and suports this
   * formats:
   * <ul>
   * <li>{<code>key</code>}: where <code>key</code> is the key in Map
   * <code>parameters</code> that contains the value of this parameter.
   * <li>{<code>mapKey.key</code>}: where <code>mapKey</code>
   * references a <code>Map</code> in <code>parameters</code> that has a key
   * <code>key</code> that contains the value of this parameter.
   * <li>{<code>beanKey.propertyName</code>}: where <code>beanKey</code>
   * references a java bean in <code>parameters</code> that has a property
   * named <code>propertyName</code> that returns the value of this parameter.
   * </ul>
   * @param parameters contains the values of the parameters referenced in
   * <code>statement</code>.
   * @return the number of rows updated.
   * @throws DBException
   */
  public int executeUpdate(String statement, Map parameters) throws DBException
  {
    try
    {
      DBStatement dbStmt = repository.getStatement(statement);
      int numUpdated = executeUpdate(dbStmt, parameters);
      return numUpdated;
    }
    catch (Exception ex)
    {
      throw DBException.createException(ex);
    }
  }

  /**
   * Gets the primary key of <code>object</code> that contains a row from
   * table <code>tableName</code>.
   * @param tableName is the name of the table. A schema prefix
   * can be specified: <code>schema.tableName</code>.
   * @param object is a <code>Map</code> that contains a row of
   * <code>tableName</code>.
   * @return a <code>DBKey</code> that is the primary key of
   * <code>object</code>.
   * @throws DBException
   */
  public DBKey getPrimaryKey(String tableName, Map object) throws DBException
  {
    try
    {
      DBTableMetaData metaData = getMetaData(tableName);
      return getPrimaryKey(metaData, object, null);
    }
    catch (Exception ex)
    {
      throw DBException.createException(ex);
    }
  }

  public DBKey getPrimaryKey(Object object) throws DBException
  {
    throw new DBException("NOT_IMPLEMENTED_YET");
  }

  /**
   * Sets the max number of rows that an executeQuery will return.
   * @param maxRows
   */
  public void setMaxRows(int maxRows)
  {
    this.maxRows = maxRows;
  }

  /**
   * Gets the max number of rows that an executeQuery will return.
   * @return the max number of rows that will be returned.
   */
  public int getMaxRows()
  {
    return maxRows;
  }

  /**
   * Enables or disables the smartUpdate mode. When enabled, only
   * columns inside Map will be updated in database. If disabled, Map
   * objects must contain all columns in the table. If any column is absent,
   * then a null value will be set.
   * @param smartUpdate
   */
  public void setSmartUpdate(boolean smartUpdate)
  {
    this.smartUpdate = smartUpdate;
  }

  /**
   * Returns smartUpdate mode.
   * @return true if smartUpdate is enabled. Returns false otherwise.
   */
  public boolean isSmartUpdate()
  {
    return smartUpdate;
  }

  /**
   * Sets the way LOBs (large objects) will be returned. If returnLobsAsFiles
   * is enabled, then LOBs will be stored in temporary files and a
   * <code>java.util.File</code> will be returned inside Maps or Tables. The
   * invoker is responsible of removing this file when finished.
   * If returnAsFiles is disabled, then LOBS will be returned as
   * <code>byte[]</code> for BLOB columns or <code>String</code> for CLOB
   * columns. By default, returnLobsAsFiles is disabled.
   * @param asFiles must be set to true to return LOBs as files.
   */
  public void setReturnLobsAsFiles(boolean asFiles)
  {
    this.returnLobsAsFiles = asFiles;
  }

  /**
   * Returns the returnLobsAsFiles property.
   * @return true if returnLobsAsFiles is enabled. Returns false otherwise.
   */
  public boolean isReturnLobsAsFiles()
  {
    return returnLobsAsFiles;
  }

  public void setAutoCommit(boolean autoCommit) throws DBException
  {
    try
    {
      jdbcConn.setAutoCommit(autoCommit);
    }
    catch (SQLException ex)
    {
      throw DBException.createException(ex);
    }
  }

  public boolean getAutoCommit() throws DBException
  {
    try
    {
      return jdbcConn.getAutoCommit();
    }
    catch (SQLException ex)
    {
      throw DBException.createException(ex);
    }
  }

  public void commit() throws DBException
  {
    try
    {
      jdbcConn.commit();
    }
    catch (SQLException ex)
    {
      throw DBException.createException(ex);
    }
  }

  public void rollback() throws DBException
  {
    try
    {
      jdbcConn.rollback();
    }
    catch (SQLException ex)
    {
      throw DBException.createException(ex);
    }
  }

  public void close() throws DBException
  {
    try
    {
      jdbcConn.commit();
      jdbcConn.close();
    }
    catch (SQLException ex)
    {
      throw DBException.createException(ex);
    }
  }

  public int executeScript(String script, boolean ignoreErrors)
    throws DBException
  {
    int errors = 0;
    String[] statements = script.split(";(?=(?:[^\']*\'[^\']*\')*[^\']*$)");
    for (String statement : statements)
    {
      try
      {
        statement = statement.trim();
        if (statement.length() > 0)
        {
          LOGGER.log(Level.FINEST, "Execute statement: {0}", 
            statement);
          executeUpdate(statement, Collections.EMPTY_MAP);
        }
      }
      catch (DBException ex)
      {
        LOGGER.log(Level.FINEST, "Error executing statement: {0}", 
          ex.toString());
        
        if (!ignoreErrors) throw ex;
        errors++;
      }
    }
    return errors;
  }

  /* private methods */

  /* return null if object do not exists */
  private Map executeSelectMap(DBStatement dbStmt,
                               Map variables) throws Exception
  {
    HashMap object = null;
    PreparedStatement prepStmt = prepareStatement(dbStmt, variables);
    try
    {
      ResultSet rs = prepStmt.executeQuery();
      try
      {
        ResultSetMetaData rsMetaData = rs.getMetaData();
        if (rs.next())
        {
          object = new HashMap();
          for (int column = 0; column < rsMetaData.getColumnCount(); column++)
          {
            String columnName =
              rsMetaData.getColumnName(column + 1).toLowerCase();
            object.put(columnName, readColumn(rs, column + 1));
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
      prepStmt.close();
    }
    return object;
  }

  private Table executeSelectTable(DBStatement dbStmt, Map variables)
    throws Exception
  {
    PreparedStatement prepStmt = prepareStatement(dbStmt, variables);
    try
    {
      ResultSet rs = prepStmt.executeQuery();
      try
      {
        ResultSetMetaData rsMetaData = rs.getMetaData();
        String columnNames[] = new String[rsMetaData.getColumnCount()];
        for (int column = 0; column < columnNames.length; column++)
        {
          columnNames[column] =
            rsMetaData.getColumnName(column + 1).toLowerCase();
        }
        Table table = new Table(columnNames);
        int row = 0;
        while (rs.next())
        {
          table.addRow();
          for (int column = 0; column < rsMetaData.getColumnCount(); column++)
          {
            table.setElementAt(row, column, readColumn(rs, column + 1));
          }
          row++;
        }
        return table;
      }
      finally
      {
        rs.close();
      }
    }
    finally
    {
      prepStmt.close();
    }
  }

  private Object executeSelectBean(DBStatement dbStmt, Map variables)
  {
    return null;
  }

  private Object[] executeSelectBeans(DBStatement dbStmt, Map variables)
  {
    return null;
  }

  private int executeUpdate(DBStatement dbStmt, Map variables)
    throws Exception
  {
    int numUpdated = 0;
    PreparedStatement prepStmt = prepareStatement(dbStmt, variables);
    try
    {
      numUpdated = prepStmt.executeUpdate();
    }
    finally
    {
      prepStmt.close();
    }
    return numUpdated;
  }

  private PreparedStatement prepareStatement(DBStatement stmt, Map variables)
    throws Exception
  {
    String sql = stmt.getSql();
    //System.out.println(">>" + stmt);
    String[] params = stmt.getParameters();
    Class[] paramClasses = stmt.getParameterClasses();
    PreparedStatement prepStmt = jdbcConn.prepareStatement(sql);
    prepStmt.setMaxRows(maxRows);
    for (int i = 0; i < params.length; i++)
    {
      String param = params[i];
      String key;
      String property;
      // parse param: {key.property}
      int dotIndex = param.indexOf(".");
      if (dotIndex == -1)
      {
        key = param;
        property = null;
      }
      else
      {
        key = param.substring(0, dotIndex);
        property = param.substring(dotIndex + 1);
      }
      // get object value
      Object object = variables.get(key);
      if (object == null)
      {
        setParameter(prepStmt, i + 1, null, paramClasses[i]);
      }
      else if (object instanceof Map) // is a Map
      {
        if (property == null)
        {
          setParameter(prepStmt, i + 1, null, paramClasses[i]);
        }
        else
        {
          Map map = (Map)object;
          setParameter(prepStmt, i + 1, map.get(property), paramClasses[i]);
        }
      }
      else // is a bean property or variable
      {
        if (property == null) // variable
        {
          setParameter(prepStmt, i + 1, object, paramClasses[i]);
        }
        else // bean property
        {
          String methodName = "get" + property.substring(0, 1).toUpperCase() +
                              property.substring(1);
          Method method = object.getClass().getMethod(methodName, new Class[0]);
          Object result = method.invoke(object, new Object[0]);
          setParameter(prepStmt, i + 1, result, paramClasses[i]);
        }
      }
    }
    return prepStmt;
  }

  private void setParameter(PreparedStatement prepStmt,
                            int index, Object value, Class paramClass)
                            throws SQLException, IOException
  {
    InputStream is = null;
    if (value instanceof File)
    {
      is = new FileInputStream((File)value);
    }
    else if (value instanceof InputStream)
    {
      is = (InputStream)value;
    }
    if (paramClass != null)
    {
      //System.out.println(">> setParameter[" + index + "]: " + value + " " + paramClass.getName());
    }
    if (is == null)
    {
      prepStmt.setObject(index, value);
      /*
      if (value == null)
      {
        if (String.class.equals(paramClass))
        {
          prepStmt.setNull(index, Types.VARCHAR);
        }
        else
        {
          prepStmt.setObject(index, null);
        }
      }
      else
      {
        prepStmt.setObject(index, value);
      }
      */
    }
    else
    {
      // what if is a CLOB or LONGVARCHAR?
      prepStmt.setBinaryStream(index, is, is.available());
    }
    //System.out.println(">> end setParameter");
  }

  private Object readColumn(ResultSet rs, int column)
    throws SQLException, IOException
  {
    Object result = rs.getObject(column);
    if (result instanceof Blob)
    {
      Blob blob = (Blob)result;
      if (returnLobsAsFiles)
      {
        InputStream is = blob.getBinaryStream();
        File tempFile = flushToDisk(is);
        result = tempFile;
      }
      else
      {
        result = blob.getBytes(1, (int)blob.length()); // result: byte[]
      }
    }
    else if (result instanceof Clob)
    {
      Clob clob = (Clob)result;
      if (returnLobsAsFiles)
      {
        InputStream is = clob.getAsciiStream();
        File tempFile = flushToDisk(is);
        result = tempFile;
      }
      else
      {
        result = clob.getSubString(1, (int)clob.length()); // result: String
      }
    }
    return result;
  }

  private File flushToDisk(InputStream is) throws IOException
  {
    File temp = File.createTempFile("lob", ".tmp");
    temp.deleteOnExit();
    BufferedOutputStream bos = new BufferedOutputStream(
      new FileOutputStream(temp));
    try
    {
      BufferedInputStream bis = new BufferedInputStream(is);
      try
      {
        int ch = bis.read();
        while (ch != -1)
        {
          bos.write(ch);
          ch = bis.read();
        }
      }
      finally
      {
        bis.close();
      }
    }
    finally
    {
      bos.close();
    }
    return temp;
  }

  private void putKey(Map map, DBKey key)
  {
    for (int i = 0; i < key.values.length; i++)
    {
      map.put("k" + i, key.values[i]);
    }
  }

  private void readMetaData(ResultSetMetaData rsMetaData,
                            String[] columnNames, Class[] columnClasses)
    throws SQLException, ClassNotFoundException
  {
    for (int column = 0; column < rsMetaData.getColumnCount(); column++)
    {
      columnNames[column] = rsMetaData.getColumnName(column + 1).toLowerCase();
      columnClasses[column] = Class.forName(
        rsMetaData.getColumnClassName(column + 1));
    }
  }

  private DBStatement buildSmartUpdateStatement(
    DBTableMetaData metaData, Map values) throws DBException
  {
    Set keys = values.keySet();
    HashSet columnsSet = new HashSet(values.size());
    Iterator iter = keys.iterator();
    while (iter.hasNext()) // remove invalid columnNames
    {
      String columnName = (String)iter.next();
      if (metaData.getColumnIndex(columnName) != -1)
      {
        columnsSet.add(columnName);
      }
    }
    String[] columnNames = (String[])columnsSet.toArray(
      new String[columnsSet.size()]);

    if (columnNames.length > 0)
    {
      //sort to construct statement always the same way
      Arrays.sort(columnNames);
      return metaData.buildUpdateStatement(columnNames);
    }
    else return null; // nothing to update
  }

  private DBKey getPrimaryKey(
    DBTableMetaData metaData, Map object, DBKey primaryKey)
  {
    String columnNames[] = metaData.getPrimaryKeyColumnNames();
    Object values[] = new Object[columnNames.length];
    for (int i = 0; i < values.length; i++)
    {
      String columnName = (String)columnNames[i];
      if (object.containsKey(columnName))
      {
        values[i] = object.get(columnNames[i]);
      }
      else if (primaryKey != null)
      {
        values[i] = primaryKey.getColumnValue(i);
      }
    }
    return new DBKey(values);
  }

  private int[] readPrimaryKeyColumnIndices(
    DatabaseMetaData dbMetaData, String tableName, String[] columnNames)
    throws SQLException
  {
    int[] keyColumnIndices = null;

    String schema = null;
    int dotIndex = tableName.lastIndexOf(".");
    if (dotIndex != -1)
    {
      schema = tableName.substring(0, dotIndex);
      tableName = tableName.substring(dotIndex + 1);
    }
    Vector keyColumnNames = new Vector();
    ResultSet rs = dbMetaData.getPrimaryKeys(null, schema, tableName);
    try
    {
      while (rs.next())
      {
        int seq = rs.getInt("KEY_SEQ");
        String name = rs.getString("COLUMN_NAME").toLowerCase();
        if (seq > keyColumnNames.size())
        {
          keyColumnNames.setSize(seq);
        }
        keyColumnNames.set(seq - 1, name);
      }
      keyColumnIndices = new int[keyColumnNames.size()];
      for (int i = 0; i < keyColumnNames.size(); i++)
      {
        String columnName = (String)keyColumnNames.elementAt(i);
        int j = 0;
        while (!columnNames[j].equals(columnName)) j++;
        keyColumnIndices[i] = j;
      }
    }
    finally
    {
      rs.close();
    }
    return keyColumnIndices;
  }

  public static void main(String[] args)
  {
    try
    {
      DBRepository rep = new DBRepository();
      DBConnection conn = rep.getConnection(
        "oracle.jdbc.driver.OracleDriver",
        "jdbc:oracle:thin:@*****:*****:*****",
        "*****", "*****");
      try
      {
        conn.setSmartUpdate(true);
        System.out.println(conn.getMetaData("prova2"));
        /*
        Map map = new HashMap();
        map.put("titol", "PROVA");
        map.put("doccod", "99989");
        map.put("data", new File("c:/demo.xml"));
        DBKey pk =
          conn.update("DOC_INTERN", new DBKey(new Integer(182), "ES"), map);

        System.out.println(pk);

        conn.setReturnLobsAsFiles(true);
        Map map = new HashMap();
        map.put("doccod", new Integer(186));
        map.put("idioma", "ES");
        map.put("titol", "Prova182");
        map.put("data", new File("c:/demo.xml"));
        conn.insert("DOC_INTERN", map);

        Map map2 = conn.selectMap("DOC_INTERN", new DBKey(new Integer(186), "ES"));
        System.out.println(map2);
         */

        //System.out.println(new String(blob.getBytes(0, 4)));
/*
        int num = DBUtils.getSequenceValue(conn, "GENESYS5.NCL_CLAU",
          DBKey.fromString("MENUITEM;MENUITEM;APL "), "clauvnum");
        System.out.println(num);

        DBKey pk = DBKey.fromString("14;ABCD");
        Map persona = conn.selectMap("testdb", pk);
        persona.put("sou", "3,8899");
        System.out.println(persona);
        conn.update("testdb", pk, persona);

        Map vars = new HashMap();
        vars.put("persona", persona);
        Table t = conn.executeQuery(
          "select * from testdb where trim(nom) = trim({persona.nom})", vars);
        System.out.println(t);
*/
        conn.commit();
      }
      finally
      {
        conn.close();
      }
    }
    catch (Exception ex2)
    {
      ex2.printStackTrace();
    }
  }
}
