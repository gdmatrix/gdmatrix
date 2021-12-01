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
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 *
 * @author blanquepa
 */
public abstract class AbstractJob implements org.quartz.Job
{
  protected Logger logger; 
//  protected File logFile;
  protected Boolean audit;
  protected String logFormat;
  protected String logVerbosity;
  
  public AbstractJob()
  {
  }
  
  public abstract void doExecute(JobExecutionContext context)
    throws JobExecutionException;   
  
  @Override
  public void execute(JobExecutionContext context)
    throws JobExecutionException
  {
    try
    {
      JobDataMap params = context.getJobDetail().getJobDataMap();
      audit = params.getBoolean("audit");
      if (audit)
      {
        String jobId = (String) params.get("jobId");
        logger = Logger.getLogger(jobId);        
//        logger = (Logger) params.get("logger");
      }
      
      doExecute(context);  
    }
    catch (Exception ex)
    {
      throw new JobExecutionException(ex);
    } 
    finally
    {
      if (audit && logger != null)
        context.getJobDetail().getJobDataMap().put("logger", logger);      
    }
  }
  
  protected void log(Level level, String msg)
  {
    if (audit && logger != null)
      logger.log(level, msg);
  }
  
  protected void log(Level level, String msg, Object[] params)
  {
    if (audit && logger != null)
    logger.log(level, msg, params);
  }
  
  protected boolean isAuditEnabled()
  {
    return audit;
  }
  
}
