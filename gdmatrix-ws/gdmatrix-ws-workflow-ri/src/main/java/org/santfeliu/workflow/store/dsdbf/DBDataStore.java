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
package org.santfeliu.workflow.store.dsdbf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.santfeliu.dbf.DBConnection;
import org.santfeliu.dbf.DBException;
import org.santfeliu.dbf.DBRepository;
import org.santfeliu.workflow.WorkflowException;
import org.santfeliu.workflow.store.DataStore;
import org.santfeliu.workflow.store.DataStoreConnection;
import static org.santfeliu.dbf.DBConnection.ORACLE_VENDOR;


/**
 *
 * @author realor
 */
public class DBDataStore implements DataStore
{
  private static final Logger LOGGER = Logger.getLogger("DBDataStore");
  private static final String PKG = DBDataStore.class.getName() + ".";

  private String dataSourceName;
  private String driver;
  private String URL;
  private String username;
  private String password;
  private final DBRepository repository = new DBRepository();
  
  public DBDataStore()
  {
  }

  @Override
  public void init(Properties properties) throws WorkflowException
  {
    try
    {
      dataSourceName = properties.getProperty(PKG + "dsn");
      if (dataSourceName == null)
      {
        driver = properties.getProperty(PKG + "driver");
        URL = properties.getProperty(PKG + "URL");
        username = properties.getProperty(PKG + "username");
        password = properties.getProperty(PKG + "password");
      }
      String enableDDLGeneration = 
        properties.getProperty("enableDDLGeneration");
      if ("true".equalsIgnoreCase(enableDDLGeneration))
        createTables();
    }
    catch (Exception ex)
    {
      throw new WorkflowException(ex);
    }
  }

  @Override
  public DataStoreConnection getConnection() throws WorkflowException
  {
    DBConnection conn;
    try
    {
      if (dataSourceName != null)
      {
        conn = repository.getConnection(dataSourceName);
      }
      else
      {
        conn = repository.getConnection(driver, URL, username, password);
      }
      return new DBDataStoreConnection(conn);
    }
    catch (Exception ex)
    {
      throw new WorkflowException(ex);
    }
  }

  public void setDataSourceName(String dataSourceName)
  {
    this.dataSourceName = dataSourceName;
  }

  public String getDataSourceName()
  {
    return dataSourceName;
  }

  public void setDriver(String driver)
  {
    this.driver = driver;
  }

  public String getDriver()
  {
    return driver;
  }

  public void setURL(String URL)
  {
    this.URL = URL;
  }

  public String getURL()
  {
    return URL;
  }

  public void setUsername(String username)
  {
    this.username = username;
  }

  public String getUsername()
  {
    return username;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }

  public String getPassword()
  {
    return password;
  }
  
  private void createTables() throws DBException, IOException
  {
    DBConnection conn;      
    if (dataSourceName != null)
    {
      conn = repository.getConnection(dataSourceName);
    }
    else
    {
      conn = repository.getConnection(driver, URL, username, password);
    }
    try
    {
      String vendor = conn.getDatabaseVendor();
      switch (vendor)
      {
        case ORACLE_VENDOR:
          vendor = "oracle";
          break;
        default:
          vendor = "ansi";
      }
      LOGGER.log(Level.INFO, "Creating tables for database [{0}]", vendor);
      InputStream is = DBDataStore.class.getResourceAsStream(vendor + ".sql");
      String script = IOUtils.toString(is, "UTF-8");
      int errors = conn.executeScript(script, true);
      LOGGER.log(Level.INFO, "Table creation errors: {0}", errors);      
    }
    finally
    {
      conn.close();
    }    
  }
}
