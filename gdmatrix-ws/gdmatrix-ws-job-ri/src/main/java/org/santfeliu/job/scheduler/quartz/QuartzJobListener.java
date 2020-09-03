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

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.santfeliu.job.service.JobException;
import org.santfeliu.job.service.JobResponse;
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
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context,
      JobExecutionException jobException)
    {
      JobDataMap params = context.getJobDetail().getJobDataMap();
      Boolean audit = params.getBoolean("audit");
      if (audit)
      {
        String startDateTime
          = TextUtils.formatDate(this.startDate, "yyyyMMddHHmmss");
        String endDateTime
          = TextUtils.formatDate(new Date(), "yyyyMMddHHmmss");
        String jobId = params.getString("jobId");
        JobResponse jobResponse = new JobResponse();
        jobResponse.setJobId(jobId);
        jobResponse.setStartDateTime(startDateTime);
        jobResponse.setEndDateTime(endDateTime);
        if (jobException != null)
        {
          jobResponse.setMessage("JOB_EXECUTION_FAILED: "
            + jobException.getMessage());
        }
        else
        {
          jobResponse.setMessage("SUCCESSFUL_JOB_EXECUTION: " 
            + context.getResult());
        }

        try
        {
          jobStore.storeJobResponse(jobResponse);
        }
        catch (JobException ex)
        {
          Logger.getLogger(
            QuartzScheduler.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
      else
      {
        if (jobException != null)
          Logger.getLogger(QuartzScheduler.class.getName()).log(
            Level.SEVERE, null, jobException);  
      }

    }

  }
