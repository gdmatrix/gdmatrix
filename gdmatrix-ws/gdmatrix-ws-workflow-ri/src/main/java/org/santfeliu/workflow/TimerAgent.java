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

import java.text.SimpleDateFormat;
import java.util.logging.Level;
import org.santfeliu.util.Table;
import static org.santfeliu.workflow.WorkflowAgent.LOGGER;

/**
 *
 * @author realor
 */
public class TimerAgent extends WorkflowAgent
{
  public static final long WAIT_TIME = 60000; // 1 minute

  TimerAgent(WorkflowEngine engine, String name)
  {
    super(engine, name);
  }

  @Override
  public void run()
  {
    LOGGER.log(Level.INFO, "TimerAgent {0} started.", getName());
    state = RUNNABLE;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    do
    {
      try
      {
        statistics.processCount++;
        statistics.lastProcessTime = System.currentTimeMillis();

        String nowDateTime = sdf.format(new java.util.Date());
        Table alarms = engine.getTimers(nowDateTime);
        for (int i = 0; i < alarms.size(); i++)
        {
          String instanceId = (String)alarms.getElementAt(i, 0);
          String dateTime = (String)alarms.getElementAt(i, 1);
          try
          {
            LOGGER.log(Level.INFO, "TimerAgent {0} awakening instance {1}",
              new Object[]{getName(), instanceId});
            engine.doStep(instanceId, this, true);
          }
          catch (Exception ex)
          {
            // instance destroyed, ignore.
          }
          engine.removeTimer(instanceId, dateTime);
        }
        state = WAITING;
        waitForEvent(WAIT_TIME);
      }
      catch (Throwable error)
      {
        state = RECOVERING;
        LOGGER.log(Level.WARNING, "TimerAgent {0} recovering...", getName());
        lastError = error;
        statistics.errorCount++;
        recover(error);
      }
    } while (!end && !Thread.interrupted());

    state = TERMINATED;
    LOGGER.log(Level.INFO, "TimerAgent {0} terminated.", getName());
  }
}
