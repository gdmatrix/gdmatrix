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
package org.santfeliu.doc.store.cnt.jdbc;

import java.io.InputStream;

import java.sql.Connection;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;

import org.santfeliu.doc.store.ContentStore;
import org.santfeliu.util.MatrixConfig;


/**
 *
 * @author blanquepa
 */
public class JdbcContentStore implements ContentStore
{
  private static final Logger LOGGER = 
    Logger.getLogger(JdbcContentStore.class.getName());  
  private static final String JDBC_FILENAME = "jdbc.properties";
  
  private final Properties config = new Properties();
  
  public JdbcContentStore()
  {
  }


  @Override
  public void init()
    throws Exception
  {
    try
    {
      loadConfiguration(JDBC_FILENAME);
      String enableDDLGeneration =
        MatrixConfig.getProperty("enableDDLGeneration");   
      if ("true".equalsIgnoreCase(enableDDLGeneration))
        createTables();
    }
    catch(Exception ex)
    {
      LOGGER.log(Level.SEVERE, "Error initializing", ex);      
      throw new RuntimeException(ex);
    }
  }

  @Override
  public JdbcContentStoreConnection getConnection()
    throws Exception
  {
    try
    {
      Context initContext = new InitialContext();
      Context envContext  = (Context)initContext.lookup("java:/comp/env");
      String dataSourceName = MatrixConfig.getProperty("cnt.dataSource");
      javax.sql.DataSource ds = null;      
      try
      {
        ds = (javax.sql.DataSource)envContext.lookup(dataSourceName);
      }
      catch(NameNotFoundException ex)
      {
        dataSourceName = MatrixConfig.getProperty("global.dataSource");  
        ds = (javax.sql.DataSource)envContext.lookup(dataSourceName);        
      }
      
      Connection conn = ds.getConnection();
      conn.setAutoCommit(false);
      
      return getContentStoreConnection(conn, config);
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "Error getting connection", ex);
      throw new Exception(ex);
    }
  }
  
  protected JdbcContentStoreConnection getContentStoreConnection(
    Connection conn, Properties config)
  {
    return new JdbcContentStoreConnection(conn, config);     
  }
   
  private String getConfigurationPath(Class cls)
  {
    String className = cls.getName();
    int index = className.lastIndexOf(".");
    return "/" + className.substring(0, index).replace('.', '/');    
  }
  
  private void loadProperties(String path, String filename) throws Exception
  {
    String name = path + "/" + filename;
    try (InputStream is = getClass().getResourceAsStream(name))
    {
      LOGGER.log(Level.INFO, "Loading {0}", name);
      config.load(is);
    }    
  }  
  
  protected void loadConfiguration(String filename) 
  {
    try
    {
      String path = getConfigurationPath(getClass());
      try
      {
        loadProperties(path, filename);
      }
      catch (Exception ex)
      {
        LOGGER.log(Level.WARNING, "Can not load config file: {0}/{1}: {2}", 
          new String[]{path, filename, ex.getMessage()});
        loadProperties(getConfigurationPath(JdbcContentStore.class), filename);
      }
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "Can not load ContentStore configuration", ex);
    }
  }
  
  protected void createTables() throws Exception
  {
    JdbcContentStoreConnection conn = getConnection();
    try 
    {
      conn.createTables();
      conn.commit();
    }
    catch(Exception ex)
    {
      LOGGER.log(Level.WARNING, "Tables creation not succeded: {0}", 
        ex.getMessage());
      conn.rollback();
    }
    finally
    {
      conn.close();
    }    
  }
  

}
