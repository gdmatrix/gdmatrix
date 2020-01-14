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
package org.santfeliu.matrix.client;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author realor
 */
public abstract class Command
{
  public static final String COMMAND = "command";
  public static final String COMMAND_ID = "commandId";
  public static final String STATUS = "status";
  public static final String EXCEPTION = "exception";
  public static final String RESULT = "result";
  
  public static final String CREATED_STATUS = "created";
  public static final String RUNNING_STATUS = "running";
  public static final String STOPPING_STATUS = "stopping";
  public static final String TERMINATED_STATUS = "terminated";
  
  private MatrixClient client;
  protected final Map properties = new HashMap();
  private Thread thread;
  private boolean stop;
  
  public Command()
  {
    properties.put(STATUS, CREATED_STATUS);
  }
  
  public void setClient(MatrixClient client)
  {
    this.client = client;
  }
  
  public MatrixClient getClient()
  {
    return client;
  }
  
  public String getId()
  {
    return (String)properties.get(COMMAND_ID);
  }
    
  public Map getProperties()
  {
    return properties;
  }
  
  public void execute()
  {
    if (thread == null)
    {
      thread = new Thread(new Runnable()
      {
        public void run()
        {
          try
          {
            properties.put(STATUS, RUNNING_STATUS);
            doWork();
          }
          catch (Exception ex)
          {
            properties.put(EXCEPTION, ex.getMessage());
          }
          finally
          {
            properties.put(STATUS, TERMINATED_STATUS);            
            thread = null;
            terminateCommand();
          }
        }
      });
      thread.start();
    }
  }
  
  protected abstract void doWork() throws Exception;
  
  public void stop()
  {
    stop = true;
    properties.put(STATUS, STOPPING_STATUS);
  }
  
  public boolean isRunning()
  {
    return thread != null;
  }
  
  public boolean isStopping()
  {
    return stop;
  }
  
  public boolean isTerminated()
  {
    return thread == null;
  }
          
  public String getException()
  {
    return (String)properties.get(EXCEPTION);
  }
  
  public String getStatus()
  {    
    return (String)properties.get(STATUS);
  }
    
  private void terminateCommand()
  {
    try
    {
      client.terminateCommand(properties);
    }
    catch (Exception ex)
    {      
    }
  }
}
