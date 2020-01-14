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
package org.santfeliu.swing;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.swing.SwingUtilities;

/**
 *
 * @author realor
 */
public class HeavyTask
{
  private boolean aborted = false;
  private Thread thread;
  protected boolean privileged = false;

  public boolean isPrivileged()
  {
    return privileged;
  }

  public void setPrivileged(boolean privileged)
  {
    this.privileged = privileged;
  }

  public void execute()
  {
  }

  public void executeInBackground()
  {
    if (thread == null)
    {
      Runnable runnable = new Runnable()
      {
        public void run()
        {
          internalStart();
        }
      };
      thread = new Thread(runnable);
      thread.start();
    }
  }

  public void abort()
  {
    aborted = true;
  }

  public boolean isAborted()
  {
    return aborted;
  }

  public void invokeAndWait(String methodName) throws Exception
  {
    invoke(methodName, false);
  }

  public void invokeLater(String methodName) throws Exception
  {
    invoke(methodName, true);
  }

  private void invoke(String methodName, boolean later) throws Exception
  {
    final Method method =
      HeavyTask.this.getClass().getMethod(methodName);

    if (SwingUtilities.isEventDispatchThread())
    {
      // execute method in EventDispatchThread
      method.invoke(HeavyTask.this);
    }
    else
    {
      Runnable runnable = new Runnable()
      {
        public void run()
        {
          try
          {
            method.invoke(HeavyTask.this);
          }
          catch (Exception ex)
          {
            throw new RuntimeException(ex);
          }
        }
      };
      // execute method in EventDispatchThread
      if (later)
        SwingUtilities.invokeLater(runnable);
      else
        SwingUtilities.invokeAndWait(runnable);
    }
  }

  private void internalStart()
  {
    if (privileged)
    {
      PrivilegedAction action = new PrivilegedAction()
      {
        public Object run()
        {
          execute();
          return null;
        }
      };
      AccessController.doPrivileged(action);
    }
    else // no privileged
    {
      execute();
    }
  }
}
