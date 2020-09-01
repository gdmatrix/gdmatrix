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
package org.santfeliu.job.service;

import java.util.ArrayList;
import org.santfeliu.job.store.JobStore;
import org.santfeliu.job.store.JobStoreFactory;
import org.santfeliu.job.scheduler.Scheduler;
import org.santfeliu.job.scheduler.SchedulerFactory;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.DictionaryConstants;
import org.matrix.job.Job;
import org.matrix.job.JobConstants;
import org.matrix.job.JobFilter;
import org.santfeliu.security.User;
import org.santfeliu.security.UserCache;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.security.util.SecurityUtils;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.TextUtils;
import org.santfeliu.ws.WSExceptionFactory;

/**
 *
 * @author blanquepa
 */
@WebService(endpointInterface = "org.matrix.job.JobManagerPort")
public class JobManager
{
  @Resource
  WebServiceContext wsContext;

  private static final String WS_ENABLED = "enabled";
  protected static final Logger log = Logger.getLogger("Job");

  private boolean enabled;
  private Scheduler scheduler;
  private JobStore jobStore;

  public JobManager()
  {
    String wsEnabled = 
      MatrixConfig.getClassProperty(getClass(), WS_ENABLED);
    enabled = wsEnabled != null && wsEnabled.equalsIgnoreCase("true");
    if (enabled)
    {
      jobStore = JobStoreFactory.newJobStore();

      if (scheduler == null)
      {
        scheduler = SchedulerFactory.newScheduler(jobStore);
        try
        {
          scheduler.start();
        } 
        catch (JobException ex)
        {
          log.log(Level.SEVERE, "JobManager initialization failed");
          throw WSExceptionFactory.create(ex);
        }
      }
    }
  }

  public void scheduleJob(Job job)
  {
    if (!enabled)
      throw WSExceptionFactory.create("MODULE_DISABLED");  
    
    try
    {
      if (job != null)
      {
        Credentials credentials = SecurityUtils.getCredentials(wsContext);
        User user = UserCache.getUser(credentials);
        if (canUserDoAction(user, DictionaryConstants.WRITE_ACTION))         
          scheduler.scheduleJob(job);
        else
          throw WSExceptionFactory.create("INSUFFICIENT_PRIVILEGES");         
      }
    } 
    catch (JobException ex)
    {
      log.log(Level.SEVERE, "Job scheduling failed");
      throw WSExceptionFactory.create(ex);
    }
  }

  public void unscheduleJob(String jobId)
  {
    if (!enabled)
      throw WSExceptionFactory.create("MODULE_DISABLED");  
    
    try
    {
      Credentials credentials = SecurityUtils.getCredentials(wsContext);
      User user = UserCache.getUser(credentials);
      if (canUserDoAction(user, DictionaryConstants.WRITE_ACTION)) 
        scheduler.unscheduleJob(jobId);
      else
        throw WSExceptionFactory.create("INSUFFICIENT_PRIVILEGES");       
    } 
    catch (JobException ex)
    {
      log.log(Level.SEVERE, "Job unscheduling failed");
      throw WSExceptionFactory.create(ex);
    }
  }

  public void executeJob(Job job)
  {
    if (!enabled)
      throw WSExceptionFactory.create("MODULE_DISABLED");  
    
    try
    {
      Credentials credentials = SecurityUtils.getCredentials(wsContext);
      User user = UserCache.getUser(credentials);
      if (canUserDoAction(user, DictionaryConstants.WRITE_ACTION)) 
      {
        if (scheduler != null && !scheduler.isStarted())
        {
          scheduler.start();
        }
        scheduler.executeJob(job);
      }
      else
        throw WSExceptionFactory.create("INSUFFICIENT_PRIVILEGES");       
    } 
    catch (JobException ex)
    {
      log.log(Level.SEVERE, "Job execution failed");
      throw WSExceptionFactory.create(ex);
    }
  }
  
  public Job storeJob(Job job)
  {
    if (!enabled)
      throw WSExceptionFactory.create("MODULE_DISABLED");  
    
    Job result = null;
    try
    {
      Credentials credentials = SecurityUtils.getCredentials(wsContext);
      User user = UserCache.getUser(credentials);
      if (canUserDoAction(user, DictionaryConstants.WRITE_ACTION)) 
      {
        if (validateJob(job))
          result = jobStore.storeJob(job);
        if (result != null)
        {
          //Reschedule to refresh changes
          scheduler.unscheduleJob(result.getJobId());
          result = scheduler.scheduleJob(result);
        }
      }
      else
        throw WSExceptionFactory.create("INSUFFICIENT_PRIVILEGES");       
    } 
    catch (JobException ex)
    {
      log.log(Level.SEVERE, "storeJob failed");
      throw WSExceptionFactory.create(ex);
    }
    return result;
  }

  public Job loadJob(String jobId)
  {
    if (!enabled)
      throw WSExceptionFactory.create("MODULE_DISABLED");  
    
    Job result = null;
    try
    {
      Credentials credentials = SecurityUtils.getCredentials(wsContext);
      User user = UserCache.getUser(credentials);
      if (canUserDoAction(user, DictionaryConstants.READ_ACTION))       
        result = jobStore.loadJob(jobId);
      else
        throw WSExceptionFactory.create("INSUFFICIENT_PRIVILEGES");        
    } 
    catch (JobException ex)
    {
      log.log(Level.SEVERE, "loadJob failed");
      throw WSExceptionFactory.create("JOB_NOT_FOUND", ex.getMessage());
    }
    return result;
  }

  public boolean removeJob(String jobId)
  {
    if (!enabled)
      throw WSExceptionFactory.create("MODULE_DISABLED");  
    
    boolean result = false;
    try
    {
      Credentials credentials = SecurityUtils.getCredentials(wsContext);
      User user = UserCache.getUser(credentials);
      if (canUserDoAction(user, DictionaryConstants.DELETE_ACTION))     
      {
        result = jobStore.removeJob(jobId);
        if (result)
          scheduler.unscheduleJob(jobId);
      }
      else
        throw WSExceptionFactory.create("INSUFFICIENT_PRIVILEGES");       
    } 
    catch (JobException ex)
    {
      log.log(Level.SEVERE, "removeJob failed");
      throw WSExceptionFactory.create(ex);
    }
    return result;
  }
  
  public List<Job> findJobs(JobFilter filter)
  {
    if (!enabled)
      throw WSExceptionFactory.create("MODULE_DISABLED");  
    
    List<Job> result = new ArrayList();
    try
    {
      Credentials credentials = SecurityUtils.getCredentials(wsContext);
      User user = UserCache.getUser(credentials);
      if (canUserDoAction(user, DictionaryConstants.READ_ACTION))       
        result = jobStore.findJobs(filter);
      else
        throw WSExceptionFactory.create("INSUFFICIENT_PRIVILEGES");       
    }
    catch (JobException ex)
    {
      log.log(Level.SEVERE, "findJobs failed");
      throw WSExceptionFactory.create(ex);      
    }
    return result;
  }
  
  public String nextFiring(String jobId)
  {
    if (!enabled)
      throw WSExceptionFactory.create("MODULE_DISABLED");  
    
    String result = null;    
    try
    {
      Date nextDate = scheduler.getNextFiring(jobId);
      if (nextDate != null)
        result = TextUtils.formatDate(nextDate, "yyyyMMddHHmmss");
    }
    catch (JobException ex)
    {
      log.log(Level.SEVERE, "nextFiring");
      throw WSExceptionFactory.create(ex);
    }
    return result;    
  }
  
  private boolean canUserDoAction(User user, String action)
  {
    //TODO: Implement Job ACLs
    Set<String> userRoles = user.getRoles();
    return userRoles.contains(JobConstants.JOB_ADMIN_ROLE);
  }

  private boolean validateJob(Job job) throws JobException
  {
    if (StringUtils.isBlank(job.getName()))
      throw new JobException("INVALID_NAME", job);
    
    if (StringUtils.isBlank(job.getStartDateTime()))
      throw new JobException("INVALID_START_DATE", job);
    
    if (StringUtils.isBlank(job.getJobType()))
      throw new JobException("INVALID_JOB_TYPE", job);
    
    return true;
  }    

}
