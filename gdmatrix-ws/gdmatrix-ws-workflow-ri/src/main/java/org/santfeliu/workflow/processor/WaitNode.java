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
package org.santfeliu.workflow.processor;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.matrix.workflow.WorkflowConstants;
import org.santfeliu.util.template.Template;
import org.santfeliu.workflow.WorkflowActor;
import org.santfeliu.workflow.WorkflowEngine;
import org.santfeliu.workflow.WorkflowInstance;


/**
 *
 * @author realor
 */
public class WaitNode extends org.santfeliu.workflow.node.WaitNode 
  implements NodeProcessor
{
  @Override
  public String process(WorkflowInstance instance, WorkflowActor actor)
    throws Exception
  {
    String outcome = WAIT_OUTCOME;
    String waitVar = WorkflowConstants.WAIT_PREFIX + getId();
    String waitValue = (String)instance.get(waitVar);
    if (waitValue == null) // first processing
    {
      String dt = null;
      if (duration != null && duration.trim().length() > 0)
      {
        // duration -> dateTime
        dt = getDateTimeFromDuration(
          Template.create(duration).merge(instance));
      }
      else if (dateTime != null && dateTime.trim().length() > 0)
      {
        dt = Template.create(dateTime).merge(instance);
      }
      if (dt != null && dt.trim().length() == 14)
      {
        instance.put(waitVar, "#" + dt);

        // program timer
        WorkflowEngine engine = instance.getEngine();
        String instanceId = instance.getInstanceId();
        engine.programTimer(instanceId, dt);
      }
      else throw new Exception("Invalid dateTime: " + dt);
    }
    else
    {
      if (waitValue.charAt(0) == '#') // waitValue is dateTime
      {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        Date alarmDate = sdf.parse(waitValue.substring(1));
        Date now = new Date();
        if (now.compareTo(alarmDate) >= 0)
        {
          // remove wait variable
          instance.put(waitVar, null);
          outcome = CONTINUE_OUTCOME;
        }
      }
      else // waitValue is a outcome
      {
        // remove wait variable
        instance.put(waitVar, null);
        outcome = waitValue;
      }
    }
    return outcome;
  }
  
  private String getDateTimeFromDuration(String duration)
  {
    Calendar calendar = Calendar.getInstance();
    if (duration.endsWith("w"))
    {
      String weeks = duration.substring(0, duration.length() - 1).trim();
      calendar.add(Calendar.WEEK_OF_MONTH, (int)Double.parseDouble(weeks));
    }
    else if (duration.endsWith("d"))
    {
      String days = duration.substring(0, duration.length() - 1).trim();
      calendar.add(Calendar.DAY_OF_MONTH, (int)Double.parseDouble(days));
    }
    else if (duration.endsWith("m"))
    {
      String minutes = duration.substring(0, duration.length() - 1);
      calendar.add(Calendar.MINUTE, (int)Double.parseDouble(minutes));
    }
    Date date = calendar.getTime();
    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
    return sdf.format(date);
  }
}
