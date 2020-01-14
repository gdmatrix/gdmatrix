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
package org.santfeliu.util;

import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author realor
 */
public abstract class Task
{
  public static final String CREATED = "CREATED";
  public static final String RUNNING = "RUNNING";
  public static final String TERMINATED = "TERMINATED";
  private static final long RETAIN_MILLIS = 5000;
  private static long counter;  
  private static long lastPurge;
  private static final HashMap<String, Task> tasks = new HashMap<String, Task>();

  private String taskId;
  private int progress = -1;
  private String message;
  private String state = CREATED;
  private Exception exception;
  private Thread thread;
  private long startMillis;
  private long stopMillis;
  private long retainMillis = RETAIN_MILLIS;
  private boolean cancelled;

  public static Task getInstance(String taskId)
  {
    synchronized (tasks)
    {
      purgeTasks();
      return tasks.get(taskId);
    }
  }
  
  public String getTaskId()
  {
    return taskId;
  }

  public long getRetainMillis()
  {
    return retainMillis;
  }

  public void setRetainMillis(long retainMillis)
  {
    this.retainMillis = retainMillis;
  }
  
  public int getProgress()
  {
    return progress;
  }
  
  public String getMessage()
  {
    return message;
  }
  
  public String getState()
  {
    return state;
  }
  
  public boolean isRunning()
  {
    return state.equals(RUNNING);
  }

  public boolean isTerminated()
  {
    return state.equals(TERMINATED);
  }
  
  public boolean isCancelled()
  {
    return cancelled;
  }
  
  public Exception getException()
  {
    return exception;
  }
    
  public void start()
  {
    if (CREATED.equals(state))
    {
      taskId = String.valueOf(++counter);
      synchronized (tasks)
      {
        tasks.put(taskId, this);
      }
      thread = new Thread(new Runnable()
      {
        public void run()
        {
          startMillis = System.currentTimeMillis();
          state = RUNNING;
          exception = null;
          try
          {
            execute();
          }
          catch (Exception ex)
          {
            exception = ex;
            message = null;
          }
          thread = null;
          stopMillis = System.currentTimeMillis();
          state = TERMINATED;
          synchronized (Task.this)
          {
            Task.this.notifyAll();
          }
        }
      }, "task-" + taskId);
      thread.start();
    }
  }
  
  public void stop()
  {
    if (RUNNING.equals(state) && !cancelled)
    {
      if (cancel())
      {
        setProgress(-1);
        setMessage("Cancelling...");
        cancelled = true;
      }
    }
  }

  public long getDuration()
  {
    return stopMillis - startMillis;
  }
  
  public synchronized void waitForUpdate(long timeout)
  {
    if (!TERMINATED.equals(state))
    {
      try
      {
        wait(timeout);
      }
      catch (InterruptedException ex)
      {
      }
    }
  }

  public synchronized void waitForTermination(long timeout)
  {
    while (!TERMINATED.equals(state) && timeout > 0)
    {
      try
      {
        long millis = System.currentTimeMillis();
        wait(timeout);
        long ellapsed = System.currentTimeMillis() - millis;
        timeout -= ellapsed;
      }
      catch (InterruptedException ex)
      {
      }
    }
  }
  
  public abstract void execute() throws Exception;
  
  public boolean cancel()
  {    
    return false;
  }

  protected final synchronized void setProgress(int progress)
  {
    this.progress = progress;
    notifyAll();
  }  
  
  protected final synchronized void setMessage(String status)
  {   
    this.message = status;
    notifyAll();
  }
  
  private static void purgeTasks()
  {
    long now = System.currentTimeMillis();
    if (now - lastPurge > RETAIN_MILLIS)
    {
      Iterator<Task> iter = tasks.values().iterator();
      while (iter.hasNext())
      {
        Task task = iter.next();
        if (TERMINATED.equals(task.state) && 
          (now - task.stopMillis) > task.retainMillis)
        {
          iter.remove();
        }
      }
      lastPurge = now;
    }
  }
}
