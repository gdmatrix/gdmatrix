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
package org.santfeliu.workflow.store;


import java.util.HashMap;

import org.santfeliu.workflow.Workflow;
import org.santfeliu.workflow.WorkflowException;


public abstract class BaseWorkflowStore implements WorkflowStore
{
  protected HashMap cache = new HashMap();
  protected long lastCacheClear = System.currentTimeMillis();
  protected long clearCacheTime = 6000000; // 100 minutes by default

  public BaseWorkflowStore()
  {
  }

  /* if workflowVersion == null returns the last version */
  public synchronized Workflow getWorkflow(String workflowName, 
    String workflowVersion) throws WorkflowException
  {
    refresh();
    Workflow workflow = null;
    try
    {
      String key = workflowName;
      if (workflowVersion != null)
      {
        key += "@" + workflowVersion;
        workflow = (Workflow)cache.get(key);
      }
      if (workflow == null)
      {
        System.out.print("loading workflow " + key + "...");
        workflow = loadWorkflow(workflowName, workflowVersion);
        key = workflowName + "@" + workflow.getVersion();
        cache.put(key, workflow);
        System.out.println("done.");
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      throw WorkflowException.createException(ex);
    }
    return workflow;
  }

  public synchronized void clearCache()
  {
    cache.clear();
  }
  
  synchronized void refresh()
  {
    long millis = System.currentTimeMillis();
    if ((millis - lastCacheClear) > clearCacheTime)
    {
      cache.clear();
      lastCacheClear = millis;
    }
  }
  
  /* if workflowVersion == null returns the last version */
  abstract protected Workflow loadWorkflow(
    String workflowName, String workflowVersion) throws WorkflowException;

}
