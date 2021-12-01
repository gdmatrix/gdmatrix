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
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.santfeliu.job.service.JobFiring;
import org.matrix.job.LogType;
import org.matrix.job.ResponseType;
import org.santfeliu.job.service.JobException;
import org.santfeliu.job.store.JobStore;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author blanquepa
 */
public class QuartzJobListener implements JobListener
{

  private final JobStore jobStore;
  private Date startDate;

  public QuartzJobListener(JobStore jobStore)
  {
    this.jobStore = jobStore;
  }

  @Override
  public String getName()
  {
    return "QuartzJobListener";
  }

  @Override
  public void jobToBeExecuted(JobExecutionContext context)
  {
    this.startDate = new Date();
  }

  @Override
  public void jobExecutionVetoed(JobExecutionContext context)
  {
    JobDataMap params = context.getJobDetail().getJobDataMap();
    String jobId = params.getString("jobId");
    Logger.getLogger(QuartzJobListener.class.getName()).log(
      Level.INFO, "{0} job execution locked", jobId);

    Boolean audit = params.getBoolean("audit");
    if (audit)
    {
      try
      {
        storeJobFiring(params, ResponseType.ERROR, "JOB_EXECUTION_LOCKED");
      }
      catch (JobException ex)
      {
        Logger.getLogger(
          QuartzJobListener.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

  @Override
  public void jobWasExecuted(JobExecutionContext context,
    JobExecutionException jobException)
  {
    JobDataMap params = context.getJobDetail().getJobDataMap();
    
    if (jobException != null)
      Logger.getLogger(QuartzJobListener.class.getName()).log(
        Level.SEVERE, null, jobException);
    else
      Logger.getLogger(QuartzJobListener.class.getName()).log(
        Level.INFO, "Successful job execution");

    Boolean audit = params.getBoolean("audit");
    if (audit)
    {    
      try
      {
        ResponseType responseType = null;
        String message = "";
        if (jobException != null)
        {
          message = "JOB_EXECUTION_FAILED" + ":" + jobException.getMessage();
          responseType = ResponseType.ERROR;
        }
        else
        {
          message = "SUCCESSFUL_JOB_EXECUTION" + (context.getResult() != null ? 
            ":" + context.getResult() : "");
          responseType = ResponseType.SUCCESS;
        }
        storeJobFiring(params, responseType, message);
      }
      catch (JobException ex)
      {
        Logger.getLogger(
          QuartzJobListener.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }
  
  private void storeJobFiring(JobDataMap params, ResponseType responseType, 
    String message) throws JobException
  {
    String jobId = params.getString("jobId");    
    String startDateTime
      = TextUtils.formatDate(this.startDate, "yyyyMMddHHmmss");
    String endDateTime
      = TextUtils.formatDate(new Date(), "yyyyMMddHHmmss");

    JobFiring jobFiring = new JobFiring();
    jobFiring.setJobId(jobId);
    jobFiring.setStartDateTime(startDateTime);
    jobFiring.setEndDateTime(endDateTime);
    
    jobFiring.setMessage(message);
    jobFiring.setResponseType(responseType);    

    LogType logType = (LogType) params.get("logType");
    if (logType == null)
      logType = LogType.MULTIPLE;
    jobFiring.setLogType(logType);        

    File logFile = LogUtils.getLogFile(jobId);
    jobFiring.setLogFile(logFile);
    
    jobStore.storeJobFiring(jobFiring);      
  }
  
}
