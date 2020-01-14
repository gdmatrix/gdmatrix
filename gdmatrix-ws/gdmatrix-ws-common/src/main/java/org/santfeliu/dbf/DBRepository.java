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

import java.io.IOException;
import java.io.InputStream;

import java.sql.Connection;
import java.sql.DriverManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;

import javax.sql.DataSource;


/**
 *
 * @author unknown
 */
public class DBRepository 
{
  HashMap statements;
  HashMap metaDatas;

  public DBRepository()
  {
    statements = new HashMap();
    metaDatas = new HashMap();
  }
  
  public void loadConfig(InputStream is)
    throws IOException, DBException
  {
    Properties properties = new Properties();
    properties.load(is);
    loadConfig(properties);
  }
  
  public void loadConfig(Properties properties)
    throws DBException
  {
    Set entries = properties.entrySet();
    Iterator iter = entries.iterator();
    while (iter.hasNext())
    {
      Map.Entry entry = (Map.Entry)iter.next();
      String stmtName = (String)entry.getKey();
      String statement = (String)entry.getValue();
      DBStatement dbStmt = new DBStatement(statement);
      statements.put(stmtName, dbStmt);
    }
  }
  
  public DBConnection getConnection(String dataSourceName)
    throws DBException
  {
    try
    {
      Context initContext = new InitialContext();
      Context envContext  = (Context)initContext.lookup("java:/comp/env");
      DataSource ds = (DataSource)envContext.lookup(dataSourceName);
      Connection conn = ds.getConnection();
      conn.setAutoCommit(false);
      return new DBConnection(this, conn);
    }
    catch (Exception ex)
    {
      throw DBException.createException(ex);
    }
  }

  public DBConnection getConnection(String driver, String url, 
                                    String username, String password)
    throws DBException
  {
    try
    {
      Class.forName(driver);
      Connection conn = DriverManager.getConnection(url, username, password);
      conn.setAutoCommit(false);
      return new DBConnection(this, conn);
    }
    catch (Exception ex)
    {
      throw DBException.createException(ex);
    }
  }
  
  DBStatement getStatement(String statement)
    throws DBException
  {
    DBStatement dbStat = (DBStatement)statements.get(statement);
    if (dbStat == null)
    {
      dbStat = new DBStatement(statement);
      statements.put(statement, dbStat);
    }
    return dbStat;
  }

  DBTableMetaData getTableMetaData(String tableName)
  {
    DBTableMetaData metaData = (DBTableMetaData)metaDatas.get(tableName);
    return metaData;
  }

  public Collection getStatements()
  {
    return statements.values();
  }
  
  public Collection getMetaDatas()
  {
    return metaDatas.values();
  }
  
  public void clear()
  {
    statements.clear();
    metaDatas.clear();
  }
}