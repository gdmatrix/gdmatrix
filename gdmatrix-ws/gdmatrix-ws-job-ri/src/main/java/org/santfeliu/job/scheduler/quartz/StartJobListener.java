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
package org.santfeliu.job.scheduler.quartz;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.matrix.job.Job;
import org.matrix.job.LogType;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.santfeliu.job.service.JobException;
import org.santfeliu.job.service.JobFiring;
import org.santfeliu.job.store.JobStore;

/**
 *
 * @author blanquepa
 */
public class StartJobListener implements TriggerListener 
{
  private final JobStore jobStore;
  
  public StartJobListener(JobStore jobStore)
  {
    this.jobStore = jobStore;
  }
  
  @Override
  public String getName()
  {
    return "StartJobListener";
  }

  @Override
  public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context)
  {
    if (context.getJobInstance() instanceof InitSchedulerJob)
      return false;
    else
      return isLocked(context);
  }

  private boolean isLocked(JobExecutionContext context)
  {
    Object lock = context.get("lock");   
    return lock != null && (Boolean)lock;
  }

  @Override
  public void triggerFired(Trigger trigger, JobExecutionContext context)
  {
    try
    {
      if (!(context.getJobInstance() instanceof InitSchedulerJob))
      {
        setLogger(context);
        lock(context);
      }
    }
    catch (JobException ex)
    {
      Logger.getLogger(StartJobListener.class.getName()).log(
        Level.SEVERE, null, ex);
    }
  }

  @Override
  public void triggerMisfired(Trigger trigger)
  {
  }

  @Override
  public void triggerComplete(Trigger trigger, JobExecutionContext context, 
    Trigger.CompletedExecutionInstruction triggerInstructionCode)
  {
  }
  
  private void lock(JobExecutionContext context) throws JobException
  {
    JobDataMap map = context.getJobDetail().getJobDataMap();
    String jobId = (String) map.get("jobId"); 
    Job job = jobStore.loadJob(jobId);
    if (job.isLocked())
    {
      context.put("lock", true); //already locked
      LogUtils.log(jobId, "Job already locked", Level.INFO);      
    }
    else
    {
      job.setLocked(true);
      jobStore.storeJob(job);  
      LogUtils.log(jobId, "Job locked", Level.INFO);
    } 
  } 
  
  private void setLogger(JobExecutionContext context) throws JobException
  {
    JobDataMap params = context.getJobDetail().getJobDataMap();
    Boolean audit = params.getBoolean("audit");
    if (audit)    
    {
      String jobId = params.getString("jobId");      
      LogType logType = (LogType) params.get("logType");
      String logFormat = (String) params.get("logFormat");
      File logFile = null;
      if (LogType.CONTINUOUS.equals(logType))
      {
        JobFiring jobFiring = jobStore.getLastJobFiring(jobId);
        if (jobFiring != null)
          logFile = jobFiring.getLogFile();
      }
      try      
      {
        LogUtils.setHandler(jobId, logFile, logFormat, logType);
        LogUtils.log(jobId, "Job execution logger initialized", Level.INFO);
      }
      catch (IOException ex)
      {
        throw new JobException(ex); 
      }
    }
  }
  
}
