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
package org.santfeliu.job.scheduler;

import java.lang.reflect.Constructor;
import org.santfeliu.job.scheduler.quartz.QuartzScheduler;
import org.santfeliu.job.store.JobStore;
import org.santfeliu.util.MatrixConfig;

/**
 *
 * @author blanquepa
 */
public class SchedulerFactory
{
  public static final String SCHEDULER_CLASS = "schedulerClass";
  
  public static Scheduler newScheduler(JobStore jobStore)
  {
    String schedulerClassName = 
      MatrixConfig.getClassProperty(SchedulerFactory.class, SCHEDULER_CLASS);
    if (schedulerClassName != null)
    {
      try
      {
        Class schedulerClass = Class.forName(schedulerClassName);
        Constructor constructor = schedulerClass.getConstructor(JobStore.class);
        return (Scheduler) constructor.newInstance(jobStore);
      }
      catch (Exception ex)
      {
      }
    }
    return new QuartzScheduler(jobStore);
  }
}
