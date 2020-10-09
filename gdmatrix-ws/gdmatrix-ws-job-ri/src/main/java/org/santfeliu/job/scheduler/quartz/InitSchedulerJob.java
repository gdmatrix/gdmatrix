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

import java.net.URL;
import java.util.Date;
import java.util.List;
import org.matrix.job.JobFilter;
import org.matrix.job.JobManagerPort;
import org.matrix.job.JobManagerService;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author blanquepa
 */
public class InitSchedulerJob implements org.quartz.Job
{

  @Override
  public void execute(JobExecutionContext context) 
    throws JobExecutionException
  {
    System.out.println("Initializing jobs...");
    
    JobManagerPort port = getJobManagerPort();
    JobFilter filter = new JobFilter();
    String now = TextUtils.formatDate(new Date(), "yyyyMMddHHmmss");
    filter.setFromDate(now);
    List<org.matrix.job.Job> jobs = port.findJobs(filter);
    for (org.matrix.job.Job job : jobs)
    {
      try
      {
        port.scheduleJob(job);
      }
      catch (Exception ex)
      {
        throw new JobExecutionException(ex);
      }        
    }
  }
  
  private JobManagerPort getJobManagerPort()
  {
    try
    {
      String contextPath = MatrixConfig.getProperty("contextPath");
      URL wsDirectoryURL = 
        new URL("http://localhost" + contextPath + "/wsdirectory");      
      WSDirectory wsDirectory = WSDirectory.getInstance(wsDirectoryURL);
      WSEndpoint endpoint =
        wsDirectory.getEndpoint(JobManagerService.class);

      return endpoint.getPort(JobManagerPort.class, 
        MatrixConfig.getProperty("adminCredentials.userId"),
        MatrixConfig.getProperty("adminCredentials.password"));
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }    
  }
   

}
