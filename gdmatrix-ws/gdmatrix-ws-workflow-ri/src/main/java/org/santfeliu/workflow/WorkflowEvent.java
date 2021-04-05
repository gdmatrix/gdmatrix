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

import java.util.Calendar;

/**
 *
 * @author realor
 */
public class WorkflowEvent
{
  private final String instanceId;
  private final int eventNum;
  private final Calendar time;
  private final ValueChanges valueChanges;
  private final String actorName;

  public WorkflowEvent(String instanceId, ValueChanges valueChanges, 
    String actorName)
  {
    this.instanceId = instanceId;
    this.eventNum = -1;
    this.time = null;
    this.valueChanges = valueChanges;
    this.actorName = actorName;
  }

  public WorkflowEvent(String instanceId, int eventNum, 
    Calendar time, ValueChanges valueChanges, String actorName)
  {
    this.instanceId = instanceId;    
    this.eventNum = eventNum;
    this.time = time;
    this.valueChanges = valueChanges;
    this.actorName = actorName;
  }
  
  public String getInstanceId()
  {
    return instanceId;
  }
  
  public int getEventNum()
  {
    return eventNum;
  }
  
  public Calendar getTime()
  {
    return time;
  }
  
  public String getActorName()
  {
    return actorName;
  }
  
  public ValueChanges getValueChanges()
  {
    return valueChanges;
  }
  
  @Override
  public String toString()
  {
    return "[" + instanceId + "/" + eventNum + "] (" + time + ", " +
      valueChanges + ", " + actorName + ")";
  }
}
