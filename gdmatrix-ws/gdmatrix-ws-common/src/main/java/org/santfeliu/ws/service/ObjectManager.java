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
package org.santfeliu.ws.service;

import java.util.Map;

import javax.annotation.Resource;

import org.santfeliu.dbf.DBConnection;
import org.santfeliu.dbf.DBKey;
import org.santfeliu.dbf.DBRepository;
import org.santfeliu.util.Table;

import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.santfeliu.util.MatrixConfig;


public abstract class ObjectManager
{
  
  @Resource
  WebServiceContext wsContext;
  
  private static String DATA_SOURCE = 
    MatrixConfig.getProperty("global.dataSource");
  
  protected String tableName = "TESTDB";
  //TODO:  get repository from jndi registry !!!
  protected static DBRepository repository = new DBRepository(); 

  public ObjectManager()
  {
  }
  
  // public methods
  public Map insert(Map object) throws Exception
  {
    DBConnection conn = openConnection();
    try
    {
      return doInsert(conn, object);
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
  
  public Map update(String id, Map object) throws Exception
  {
    DBConnection conn = openConnection();
    try
    {
      return doUpdate(conn, id, object);
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
     
  public String delete(String id) throws Exception
  {
    DBConnection conn = openConnection();
    try
    {
      return doDelete(conn, id);
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

  public Map select(String id) throws Exception
  {
    DBConnection conn = openConnection();
    try
    {
      return doSelect(conn, id);
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

  public Table find(Map parameters) throws Exception
  {
    DBConnection conn = openConnection();
    try
    {
      return doFind(conn, parameters);    
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
  
  // protected methods
  protected Map doSelect(DBConnection conn, String id) throws Exception
  {
    Map object = conn.selectMap(tableName, DBKey.fromString(id));
    return object;
  }
  
  protected Map doInsert(DBConnection conn, Map parameters) throws Exception
  {
    DBKey pk = conn.insert(tableName, parameters);
    parameters.put("_id", pk.toString());
    return parameters;
  }
  
  protected Map doUpdate(DBConnection conn, String id, Map parameters) 
    throws Exception
  {
    DBKey pk = conn.update(tableName, DBKey.fromString(id), parameters);
    parameters.put("_id", pk.toString());
    return parameters;
  }
  
  protected String doDelete(DBConnection conn, String id) throws Exception
  {
    boolean deleted = conn.delete(tableName, DBKey.fromString(id));
    return deleted ? "object deleted." : "object not deleted.";
  }

  protected Table doFind(DBConnection conn, Map parameters) throws Exception
  {
    return null;
  }
  
  protected MessageContext getMessageContext()
  {
    return wsContext.getMessageContext();
  }
      
  protected DBConnection openConnection() throws Exception
  {
    return repository.getConnection(DATA_SOURCE);
  }
  
  protected void closeConnection(DBConnection conn)
  {
    if (conn != null)
    {
      try
      {
        conn.close();
      }
      catch (Exception ex)
      {
      }
    }    
  }
}
