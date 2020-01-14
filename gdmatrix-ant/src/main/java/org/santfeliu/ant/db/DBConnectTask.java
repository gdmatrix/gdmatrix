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

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;
import org.santfeliu.ant.js.ScriptableTask;

/**
 *
 * @author realor
 */
public class DBConnectTask extends ScriptableTask implements TaskContainer
{
  private String connVar;
  private String driver;
  private String url;
  private String username;
  private String password;
  private ArrayList<Task> tasks = new ArrayList();

  public String getConnVar()
  {
    return connVar;
  }

  public void setConnVar(String connVar)
  {
    this.connVar = connVar;
  }

  public String getDriver()
  {
    return driver;
  }

  public void setDriver(String driver)
  {
    this.driver = driver;
  }

  public String getPassword()
  {
    return password;
  }

  public void setPassword(String password)
  {
    this.password = password;
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

  public void addTask(Task task)
  {
    tasks.add(task);
  }

  @Override
  public void execute() throws BuildException
  {
    if (connVar == null) 
      throw new BuildException("Attribute 'connVar' is required");
    if (url == null) 
      throw new BuildException("Attribute 'url' is required");
    if (driver == null) 
      throw new BuildException("Attribute 'driver' is required");
    try
    {
      Class.forName(driver);
      Connection conn = DriverManager.getConnection(url, username, password);
      try
      {
        conn.setAutoCommit(false);
        setVariable(connVar, conn);        
        for (Task task : tasks) task.perform();
        conn.commit();
      }
      catch (Exception ex)
      {
        conn.rollback();
        throw ex;
      }
      finally
      {
        deleteVariable(connVar);
        conn.close();
      }
    }
    catch (Exception ex)
    {
      throw new BuildException(ex);
    }
  }
}

