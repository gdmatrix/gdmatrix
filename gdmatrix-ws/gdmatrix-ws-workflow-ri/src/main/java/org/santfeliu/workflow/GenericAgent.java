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

import java.util.logging.Level;

/**
 *
 * @author unknown
 */
public class GenericAgent extends WorkflowAgent
{
  public static final long WAIT_TIME = 3600000; // each hour
  public static final int MAX_TIME = 1000; // max time per process in ms

  GenericAgent(WorkflowEngine engine, String name)
  {
    super(engine, name);
  }

  public void run()
  {
    log.log(Level.INFO, "agent {0} started.", getName());
    state = RUNNABLE;
    while (!end && !Thread.interrupted())
    {
      try
      {
        log.log(Level.INFO, "agent {0} looking for instance...", getName());
        String instanceId = engine.findProcessableInstance(getName());
        if (instanceId != null)
        {
          log.log(Level.INFO, "agent {0} processing instance {1}",
            new Object[]{getName(), instanceId});
          statistics.processCount++;
          statistics.lastProcessTime = System.currentTimeMillis();

          long initialTime = System.currentTimeMillis();
          long ellapsedTime = 0;
          WorkflowEvent event;
          do
          {
            event = engine.doStep(instanceId, this, true);
            ellapsedTime = System.currentTimeMillis() - initialTime;
          } while (event != null && ellapsedTime < MAX_TIME);
        }
        else
        {
          state = WAITING;
          log.log(Level.INFO, "agent {0} waiting for event...", getName());
          waitForEvent(WAIT_TIME);
        }
      }
      catch (Throwable error)
      {
        state = RECOVERING;
        log.log(Level.WARNING, "agent {0} recovering...", getName());
        lastError = error;
        statistics.errorCount++;
        recover(error);
      }
    }
    state = TERMINATED;
    log.log(Level.INFO, "agent {0} terminated.", getName());
  }
}
