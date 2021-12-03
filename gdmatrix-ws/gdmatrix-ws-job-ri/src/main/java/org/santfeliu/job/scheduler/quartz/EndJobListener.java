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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.matrix.job.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.santfeliu.job.service.JobException;
import org.santfeliu.job.store.JobStore;

/**
 *
 * @author blanquepa
 */
public class EndJobListener implements JobListener
{
  private final JobStore jobStore;
  
  public EndJobListener(JobStore jobStore)
  {
    this.jobStore = jobStore;
  }

  @Override
  public String getName()
  {
    return "EndJobListener";
  }

  @Override
  public void jobToBeExecuted(JobExecutionContext context)
  {
  }

  @Override
  public void jobExecutionVetoed(JobExecutionContext context)
  {
    if (!(context.getJobInstance() instanceof InitSchedulerJob))
    {
      Boolean lock = (Boolean)context.get("lock");
      if (!lock)
      {
        JobDataMap map = context.getJobDetail().getJobDataMap();
        String jobId = (String) map.get("jobId");        
        LogUtils.removeHandler(jobId); 
      }
    }
  }

  @Override
  public void jobWasExecuted(JobExecutionContext context, 
    JobExecutionException jobException)
  {
    JobDataMap map = context.getJobDetail().getJobDataMap();
    String jobId = (String) map.get("jobId");   
    if (!(context.getJobInstance() instanceof InitSchedulerJob))
    {
      try       
      { 
        unlock(jobId);    
      }
      catch (JobException ex)
      {
        Logger.getLogger(EndJobListener.class.getName()).log(
          Level.SEVERE, null, ex);
      }
      finally
      {
        LogUtils.removeHandler(jobId);        
      }
    }
  }
  
  private void unlock(String jobId) throws JobException
  {
    Job job = jobStore.loadJob(jobId);
    if (job.isLocked())
    {
      job.setLocked(false);
      jobStore.storeJob(job);
      LogUtils.log(jobId, "Job unlocked", Level.INFO);      
    }         
  }  
}
