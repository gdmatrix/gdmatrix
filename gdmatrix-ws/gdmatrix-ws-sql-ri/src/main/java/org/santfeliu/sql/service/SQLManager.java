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
package org.santfeliu.sql.service;

import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import org.matrix.sql.QueryParameters;
import org.matrix.sql.QueryTable;
import static org.matrix.sql.SQLConstants.SQL_ADMIN_ROLE;
import org.matrix.sql.SQLManagerPort;
import org.santfeliu.dbf.DBConnection;
import org.santfeliu.dbf.DBRepository;
import org.santfeliu.security.UserCache;
import org.santfeliu.sql.QueryParametersConverter;
import org.santfeliu.sql.QueryTableConverter;
import org.santfeliu.sql.store.ConnectionStore;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.Table;
import org.santfeliu.ws.WSExceptionFactory;
import org.santfeliu.ws.annotations.SingleInstance;
import org.santfeliu.ws.annotations.Initializer;
import org.santfeliu.ws.annotations.Disposer;


/**
 *
 * @author realor
 */
@WebService(endpointInterface = "org.matrix.sql.SQLManagerPort")
@HandlerChain(file="handlers.xml")
@SingleInstance
public class SQLManager implements SQLManagerPort
{
  private static final Logger LOGGER = Logger.getLogger("SQL");

  public static final String CONNECTION_STORE = "connectionStore";

  @Resource
  WebServiceContext wsContext;
  
  // connection store
  ConnectionStore connStore;
  
  // dbrepository to execute sql statements
  DBRepository repository;
  
  @Initializer
  public void initialize(String endpointName)
  {
    try
    {
      LOGGER.log(Level.INFO, "Initializing SQLManager");
      //Set up ConnectionStore
      String connectionStoreClassName = 
        MatrixConfig.getClassProperty(SQLManager.class, CONNECTION_STORE);
      if (connectionStoreClassName == null)
        throw new RuntimeException("UNDEFINED_STORE_CLASS_NAME");

      Class connStoreClass = Class.forName(connectionStoreClassName);
      connStore = (ConnectionStore)connStoreClass.newInstance();
      Properties properties = MatrixConfig.getClassProperties(connStoreClass);
      
      String ddlGeneration = MatrixConfig.getProperty("enableDDLGeneration");
      if (ddlGeneration != null)
        properties.setProperty("enableDDLGeneration", ddlGeneration);
      
      connStore.init(properties);
      
      repository = new DBRepository();
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "SQLManager init failed", ex);
      throw WSExceptionFactory.create(ex);
    }    
  }

  @Disposer
  public void dispose(String endpointName)
  {
    LOGGER.log(Level.INFO, "SQLManager disposed.");
  }
  
  @Override
  public QueryTable executeDriverQuery(String sql, QueryParameters parameters, 
    String driver, String url, String username, String password)
  {
    checkPermissions();
    
    try
    {      
      QueryTable result = null;
      DBConnection conn = 
        repository.getConnection(driver, url, username, password);

      if (conn != null)
        result = QueryTableConverter.fromTable(
          doExecuteQuery(conn, sql, QueryParametersConverter.toMap(parameters)));
    
      return result;
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "executeDriverQuery", ex);
      throw WSExceptionFactory.create(ex);
    }
  }

  @Override
  public QueryTable executeAliasQuery(String sql, QueryParameters parameters, 
    String alias, String username, String password)
  {
    checkPermissions();
    
    try
    {
      QueryTable result = null;
    
      DBConnection conn = 
        connStore.getConnection(alias, username, password);

      if (conn != null)
        result = QueryTableConverter.fromTable(
          doExecuteQuery(conn, sql, QueryParametersConverter.toMap(parameters)));
    
      return result;
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "executeAliasQuery", ex);
      throw WSExceptionFactory.create(ex);
    }    
  }
  
  @Override
  public int executeDriverUpdate(String sql, QueryParameters parameters, 
    String driver, String url, String username, String password)
  {
    checkPermissions();
    
    try
    {
      int result = 0;    

      DBConnection conn = 
        repository.getConnection(driver, url, username, password);

      if (conn != null)
      {
        result = 
          doExecuteUpdate(conn, sql, QueryParametersConverter.toMap(parameters));
      }
      
      return result;
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "executeDriverUpdate", ex);
      throw WSExceptionFactory.create(ex);
    }
  }
  
  @Override
  public int executeAliasUpdate(String sql, QueryParameters parameters, 
    String alias, String username, String password)
  {
    checkPermissions();

    try
    {
      int result = 0;
    
      DBConnection conn = 
        connStore.getConnection(alias, username, password);
    
      if (conn != null)
        result = 
          doExecuteUpdate(conn, sql, QueryParametersConverter.toMap(parameters));
      
      return result;
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "executeAliasUpdate", ex);
      throw WSExceptionFactory.create(ex);
    }
  }
  
  @Override
  public void createConnection(String alias, String driver, String url)
  {
    checkPermissions();

    try
    {
      connStore.addConnection(alias, driver, url);
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "createConnection", ex);
      throw WSExceptionFactory.create(ex);
    }
  }
  
  @Override
  public void removeConnection(String alias)
  {
    checkPermissions();

    try
    {
      connStore.removeConnection(alias);
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "removeConnection", ex);
      throw WSExceptionFactory.create(ex);
    }
  }
  
  //Private methods
  private void checkPermissions()
  {
    if (!UserCache.getUser(wsContext).isInRole(SQL_ADMIN_ROLE))
      throw new WebServiceException("ACCESS_DENIED");
  }
    
  private Table doExecuteQuery(DBConnection conn, String sql, Map parameters)
    throws Exception
  {
    try
    {
      Table result = conn.executeQuery(sql, parameters);
      return result;
    }
    catch (Exception ex)
    {
      conn.rollback();
      throw ex;
    }
    finally
    {
      conn.close();
    }
  }  
  
  private int doExecuteUpdate(DBConnection conn, String sql, Map parameters)
    throws Exception
  {
    try
    {
      int result = conn.executeUpdate(sql, parameters);
      return result;
    }
    catch (Exception ex)
    {
      conn.rollback();
      throw ex;
    }
    finally
    {
      conn.close();
    }
  }   
}
