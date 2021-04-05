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
package org.santfeliu.workflow;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.matrix.workflow.WorkflowConstants;

/**
 *
 * @author realor
 */
public abstract class WorkflowAgent extends WorkflowActor
  implements Runnable
{
  public static final long RECOVERY_TIME = 30000; // 30 seconds

  public static final String NEW = "NEW";
  public static final String RUNNABLE = "RUNNABLE";
  public static final String WAITING = "WAITING";
  public static final String RECOVERING = "RECOVERING";
  public static final String TERMINATED = "TERMINATED";
  public static final Logger LOGGER = Logger.getLogger("WorkflowAgent");

  protected WorkflowEngine engine;
  protected String state;
  protected boolean end = false;
  protected Statistics statistics;
  protected Throwable lastError;
  protected Thread thread;
  
  WorkflowAgent(WorkflowEngine engine, String name)
  {
    this.engine = engine;
    this.state = NEW;
    this.statistics = new Statistics();
    setName(name);
    getRoles().add(WorkflowConstants.WORKFLOW_ADMIN_ROLE);
    getRoles().add(WorkflowConstants.WORKFLOW_AGENT_ROLE);
    thread = new Thread(this, name);
  }

  @Override
  public boolean hasAnyRole(Set rolesSet)
  {
    return true;
  }

  public String getAgentState()
  {
    if (!thread.isAlive()) state = TERMINATED;
    return state;
  }

  public void start()
  {
    thread.start();
  }

  public void wakeUp()
  {
    synchronized (this)
    {
      notify();
    }
  }
   
  public void kill()
  {
    end = true;
    thread.interrupt();
    synchronized (this)
    {
      notify();
    }
  }

  public boolean isAlive()
  {
    return thread.isAlive();
  }

  public Statistics getStatistics()
  {
    return statistics;
  }

  public Throwable getLastError()
  {
    return lastError;
  }

  public class Statistics
  {
    long creationTime;
    long lastProcessTime;
    int processCount;
    int errorCount;
    
    public Statistics()
    {
      creationTime = System.currentTimeMillis();
    }
    
    public long getCreationTime()
    {
      return creationTime;
    }

    public long getLastProcessTime()
    {
      return lastProcessTime;
    }
    
    public int getProcessCount()
    {
      return processCount;
    }
    
    public int getErrorCount()
    {
      return errorCount;
    }
  }
  
  protected void recover(Throwable error)
  {
    try
    {
      LOGGER.log(Level.SEVERE, error.toString());
      Thread.sleep(RECOVERY_TIME); // wait for 10 seconds to recover
    }
    catch (InterruptedException ex)
    {
      LOGGER.log(Level.INFO, "agent {0} interrupted.", getName());
      end = true;
    }
    finally
    {
      state = RUNNABLE;      
    }
  }
  
  // private methods
  protected void waitForEvent(long time)
  {
    try
    {
      synchronized (this)
      {
        wait(time);
      }
    }
    catch (InterruptedException ex)
    {
      LOGGER.log(Level.INFO, "agent {0} interrupted.", getName());
      end = true;
    }
    finally
    {
      state = RUNNABLE;
    }
  }  
}
