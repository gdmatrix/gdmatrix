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

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.Property;
import org.matrix.job.Job;
import org.matrix.job.LogType;
import org.quartz.CalendarIntervalScheduleBuilder;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.ScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.TriggerListener;
import org.quartz.impl.matchers.EverythingMatcher;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.job.scheduler.Scheduler;
import org.santfeliu.job.service.JobException;

import org.santfeliu.job.store.JobStore;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author blanquepa
 */
public class QuartzScheduler implements Scheduler
{
  public static final String SIMPLE_TRIGGER = "Simple";
  public static final String CALENDAR_TRIGGER = "Calendar";
  public static final String CRON_TRIGGER = "Cron";
  
  public static final String LOCK_ENABLED = "lockEnabled";
  
  private static final String TRIGGER_NAME_SUFFIX = "_trigger";
  private static final String DEFAULT_GROUP = "default";
  

  static org.quartz.Scheduler scheduler;
  private final JobStore jobStore;

  public QuartzScheduler(JobStore jobStore)
  {
    this.jobStore = jobStore;
  }

  @Override
  public Job scheduleJob(Job job) throws JobException
  {
    try
    {
      JobDetail jobDetail = buildJobDetail(job);
      Trigger trigger = buildTrigger(job);
      Date nextDate = scheduler.scheduleJob(jobDetail, trigger);     
      job.setStartDateTime(TextUtils.formatDate(nextDate, "yyyyMMddHHmmss"));
    }
    catch (Exception ex)
    {
      throw new JobException(ex);
    }

    return job;
  }

  @Override
  public boolean unscheduleJob(String jobId) throws JobException
  {
    boolean result = false;
    try
    {
      result = scheduler.unscheduleJob(getTriggerKey(jobId));
      if (result)
      {
        result = scheduler.deleteJob(JobKey.jobKey(jobId, DEFAULT_GROUP));
      }
    }
    catch (Exception ex)
    {
      throw new JobException(ex);
    }
    return result;
  }

  @Override
  public Job executeJob(Job job) throws JobException
  {
    try
    {
      JobDetail jobDetail = buildJobDetail(job);
      JobKey jobKey = jobDetail.getKey();
      if (!scheduler.checkExists(jobKey))
      {
        Trigger trigger = buildImmediateTrigger(job);
        scheduler.scheduleJob(jobDetail, trigger);
        Thread.sleep(1000); //let time to execute trigger before unschedule job
        scheduler.unscheduleJob(trigger.getKey());
      }
      else
      {
        scheduler.triggerJob(jobKey);
      }
    }
    catch (Exception ex)
    {
      throw new JobException(ex);
    }

    return job;
  }

  @Override
  public void start() throws JobException
  {
    try
    {
      org.quartz.SchedulerFactory schedFact
        = new org.quartz.impl.StdSchedulerFactory();
      scheduler = schedFact.getScheduler();
      scheduler.start();

      //Init listeners
      JobListener jobListener = new QuartzJobListener(jobStore);
      scheduler.getListenerManager().addJobListener(jobListener,
        EverythingMatcher.allJobs());
      
      String lockEnabledProp = 
        MatrixConfig.getClassProperty(QuartzScheduler.class, LOCK_ENABLED);      
      if (lockEnabledProp != null && lockEnabledProp.equalsIgnoreCase("true"))
      {
        TriggerListener startJobListener = new StartJobListener(jobStore);
        scheduler.getListenerManager().addTriggerListener(startJobListener,
          EverythingMatcher.allTriggers());
        
        JobListener endJobListener = new EndJobListener(jobStore);
        scheduler.getListenerManager().addJobListener(endJobListener,
          EverythingMatcher.allJobs());        
      }
      
      //Schedule initializer job
      scheduleJob(createInitJob());
    }
    catch (Exception ex)
    {
      throw new JobException(ex);
    }
  }

  @Override
  public void stop() throws JobException
  {
    try
    {
      scheduler.shutdown();
    }
    catch (Exception ex)
    {
      throw new JobException(ex);
    }
  }

  private Job createInitJob()
  {
    Job initJob = new Job();
    initJob.setJobId("initjob");
    initJob.setName("Initializer job");
    initJob.setDescription("Initializes scheduler and stored jobs");
    initJob.setJobType("JobsInitializer");
    initJob.setRepetitions(1);
    initJob.setInterval(1);
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.MINUTE, 1);
    String startDate = TextUtils.formatDate(cal.getTime(), "yyyyMMddHHmmss");
    initJob.setStartDateTime(startDate);
    String className = InitSchedulerJob.class.getCanonicalName();
    DictionaryUtils.addProperty(initJob.getProperty(), "jobClass", className);
    return initJob;
  }

  private JobDetail buildJobDetail(Job job) throws Exception
  {
    JobBuilder jobBuilder = JobBuilder.newJob(getJobClass(job))
      .withIdentity(job.getJobId(), DEFAULT_GROUP);

    //Map job main properties
    jobBuilder.usingJobData("jobId", job.getJobId());    
    jobBuilder.usingJobData("name", job.getName());
    jobBuilder.usingJobData("description", job.getDescription());
    
    //Map logging
    jobBuilder.usingJobData("audit", job.isAudit());
    
    String logVerbosity = null;
    String logFormat = null;
    LogType logType = null;
    List<Property> jobProperties = job.getProperty();
    if (jobProperties != null && !jobProperties.isEmpty())
    {
      for (Property prop : jobProperties)
      {
        String propName = prop.getName();
        String propValue = String.valueOf(prop.getValue().get(0));
        if ("logVerbosity".equals(propName))
          logVerbosity = propValue;
        else if ("logFormat".equals(propName))
          logFormat = propValue;
        else if ("logType".equals(propName))
          logType = LogType.valueOf(propValue.toUpperCase());
        else
          jobBuilder.usingJobData(propName, propValue);
      }
    }    
    
    if (job.isAudit() != null && job.isAudit())
    {   
      //Created without handler
      LogUtils.createNewLogger(job.getJobId(), logVerbosity);

      JobDataMap jobDataMap = new JobDataMap();
      jobDataMap.put("logType", logType);
      jobDataMap.put("logFormat", logFormat);     
      jobBuilder.usingJobData(jobDataMap);
    }

    return jobBuilder.build();
  }
  
  private Trigger buildImmediateTrigger(Job job) throws Exception
  {
    Trigger trigger;
    job.setRepetitions(1);   
    TriggerKey triggerKey = getTriggerKey(job.getJobId());
    if (!scheduler.checkExists(triggerKey))
    {
      TriggerBuilder triggerBuilder = TriggerBuilder.newTrigger()
        .withIdentity(triggerKey); 
      trigger = triggerBuilder.build();      
    }   
    else
    {
      trigger = scheduler.getTrigger(triggerKey);
    }
    
    return trigger;
  }

  private Trigger buildTrigger(Job job) 
    throws Exception
  {
    Trigger trigger;
    TriggerKey triggerKey = getTriggerKey(job.getJobId());
    if (!scheduler.checkExists(triggerKey))
    {
      TriggerBuilder triggerBuilder = TriggerBuilder.newTrigger()
        .withIdentity(triggerKey);
      
      //When starts
      if (job.getStartDateTime() == null)
      {
        triggerBuilder.startNow();
      }
      else
      {
        Date startDate
          = TextUtils.parseInternalDate(job.getStartDateTime());
        triggerBuilder.startAt(startDate);
      }

      //When ends
      if (job.getEndDateTime() != null)
      {
        Date endDate
          = TextUtils.parseInternalDate(job.getEndDateTime());
        triggerBuilder.endAt(endDate);
      }

      //Trigger type resolver
      String triggerType;
      if (!StringUtils.isBlank(job.getDayOfMonth()) || 
        (job.getDayOfWeek() != null && !job.getDayOfWeek().isEmpty()))
        triggerType = CRON_TRIGGER;
      else if (job.getUnitOfTime() != null && 
        (job.getUnitOfTime().equals(Scheduler.DAYS)
        || job.getUnitOfTime().equals(Scheduler.WEEKS)
        || job.getUnitOfTime().equals(Scheduler.MONTHS)
        || job.getUnitOfTime().equals(Scheduler.YEARS)))
        triggerType = CALENDAR_TRIGGER;
      else 
        triggerType = SIMPLE_TRIGGER;

      //Firing once time doesn't need schedule
      if (job.getRepetitions() == null
        || (job.getRepetitions() != null && job.getRepetitions() != 1))
      {
        ScheduleBuilder scheduleBuilder = 
          createScheduleBuilder(job, triggerType);
        triggerBuilder.withSchedule(scheduleBuilder);
      }
      //Build    
      trigger = triggerBuilder.build();
    }
    else
    {
      trigger = scheduler.getTrigger(triggerKey);
    }

    return trigger;
  }

  private ScheduleBuilder createScheduleBuilder(Job job, String triggerType)
  {
    switch (triggerType)
    {
      case CALENDAR_TRIGGER:
        return createCalendarSchedulerBuilder(job);
      case CRON_TRIGGER:
        return createCronScheduleBuilder(job);
      default:
        return createSimpleScheduleBuilder(job);
    }
  }
  
  private SimpleScheduleBuilder createSimpleScheduleBuilder(Job job)
  {
    SimpleScheduleBuilder scheduleBuilder
      = SimpleScheduleBuilder.simpleSchedule();

    //Repetitions
    if (job.getRepetitions() == null || job.getRepetitions() <= 0)
    {
      scheduleBuilder.repeatForever();
    }
    else
    {
      if (job.getRepetitions() != null && job.getRepetitions() > 1)
      {
        //Additional firings (totalFirings = withRepeatCount + 1); 
        scheduleBuilder.withRepeatCount(job.getRepetitions() - 1);
      }
    }

    //Interval
    Integer interval = job.getInterval();
    if (interval == null)
      interval = 0;
    
    String unitOfTime = job.getUnitOfTime();
    if (unitOfTime != null && unitOfTime.equals(Scheduler.SECONDS))
      scheduleBuilder.withIntervalInSeconds(interval);
    else if (unitOfTime != null && unitOfTime.equals(Scheduler.HOURS))
      scheduleBuilder.withIntervalInHours(interval);
    else
      scheduleBuilder.withIntervalInMinutes(interval);
    
    //Misfire
    scheduleBuilder.withMisfireHandlingInstructionNextWithRemainingCount();

    return scheduleBuilder;
  }
  
  private CalendarIntervalScheduleBuilder createCalendarSchedulerBuilder(Job job)
  {
    CalendarIntervalScheduleBuilder scheduleBuilder =
      CalendarIntervalScheduleBuilder.calendarIntervalSchedule();
    
    //Interval
    Integer interval = job.getInterval();
    if (interval == null)
      interval = 0;        
    String unitOfTime = job.getUnitOfTime();
    if (unitOfTime == null)
      unitOfTime = Scheduler.DAYS;
    switch (unitOfTime)
    {
      case Scheduler.DAYS:
        scheduleBuilder.withIntervalInDays(interval);
        break;
      case Scheduler.MONTHS:
        scheduleBuilder.withIntervalInMonths(interval);
        break;
      case Scheduler.WEEKS:
        scheduleBuilder.withIntervalInWeeks(interval);
        break;
      case Scheduler.YEARS:
        scheduleBuilder.withIntervalInYears(interval);
        break;
      default:
        break;
    }
    
    //Misfire
    scheduleBuilder.withMisfireHandlingInstructionDoNothing();
    
    return scheduleBuilder;
  }
  
  private CronScheduleBuilder createCronScheduleBuilder(Job job)
  {
    //Cron expression construction

    StringBuilder exp = new StringBuilder();
   
    String seconds = job.getStartDateTime().substring(12);
    String minutes = job.getStartDateTime().substring(10, 12);
    String hours = job.getStartDateTime().substring(8, 10);  
    String month = "*";
    String year = "*";
   
    Integer interval = job.getInterval();
    String unitOfTime = job.getUnitOfTime();    
    
    if (interval != null && unitOfTime != null)
    {
      switch (unitOfTime)
      {
        case Scheduler.SECONDS:
          seconds = "0/" + interval;
          minutes = "*";
          hours = "*";
          break;
        case Scheduler.MINUTES:
          //seconds = "0";
          minutes = "0/" + interval;
          hours = "*";
          break;
        case Scheduler.HOURS:
          //seconds = "0";
          //minutes = "0";
          hours = "0/" + interval;
          break;     
        case Scheduler.MONTHS:
          month = job.getStartDateTime().substring(4, 6);
          month = month + "/" + interval;
          break;
        case Scheduler.YEARS:
          month = job.getStartDateTime().substring(4, 6);
          year = job.getStartDateTime().substring(0, 4);
          year = year + "/" + interval;
          break;          
        default:
          break;
      }
    }
    
    exp.append(seconds);
    exp.append(" ").append(minutes); 
    exp.append(" ").append(hours); 
    
    String dayOfMonth = job.getDayOfMonth();
    if (StringUtils.isBlank(dayOfMonth))
      dayOfMonth = "?";
    exp.append(" ").append(dayOfMonth);
    
    exp.append(" ").append(month);
      
    appendDayOfWeek(exp, job.getDayOfWeek());
    
    exp.append(" ").append(year);
   
    return CronScheduleBuilder.cronSchedule(exp.toString())
      .withMisfireHandlingInstructionDoNothing();
  }
  
  private void appendDayOfWeek(StringBuilder expression, List<String> dayOfWeek)
  {
    if (dayOfWeek == null || dayOfWeek.isEmpty())
      expression.append(" ").append("?");
    else
    {
      expression.append(" ");
      boolean first = true;
      for (String day : dayOfWeek)
      {
        if (!first)
          expression.append(",");
        else
          first = false;
    
        expression.append(day);
      }
    }    
  }

  private Class getJobClass(Job job) throws Exception
  {
    Class jobClass = TestJob.class;
    if (job != null)
    {
      String jobClassName = 
        DictionaryUtils.getPropertyValue(job.getProperty(), "jobClass");
      if (jobClassName != null)
      {
        jobClass = Class.forName(jobClassName);
      }
    }
    return jobClass;
  }

  @Override
  public boolean isStarted()
  {
    try
    {
      return scheduler.isStarted();
    }
    catch (Exception ex)
    {
      return false;
    }
  }

  @Override
  public Date getNextFiring(String jobId) throws JobException
  {
    Date nextDate = null;
    try
    {
      TriggerKey tk = getTriggerKey(jobId);
      Trigger trigger = scheduler.getTrigger(tk);
      if (trigger != null)
        nextDate = trigger.getNextFireTime();
      else
        throw new JobException("UNSCHEDULED_JOB");
    }
    catch (Exception ex)
    {
      throw new JobException(ex);
    }
    
    return nextDate;
  }
  
  private TriggerKey getTriggerKey(String jobId)
  {
    return TriggerKey.triggerKey(jobId + TRIGGER_NAME_SUFFIX, DEFAULT_GROUP);   
  }

}
