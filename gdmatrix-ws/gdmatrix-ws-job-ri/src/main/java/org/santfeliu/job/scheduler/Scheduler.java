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

import java.util.Date;
import org.matrix.job.Job;
import org.santfeliu.job.service.JobException;
import org.santfeliu.job.store.JobStore;

/**
 *
 * @author blanquepa

 */
public interface Scheduler
{  
  public static final String SECONDS = "s";
  public static final String MINUTES = "m";
  public static final String HOURS = "h";
  public static final String DAYS = "d";
  public static final String WEEKS = "w";
  public static final String MONTHS = "M";
  public static final String YEARS = "y";
    
  
  public void start() throws JobException;
  
  public void stop() throws JobException;
  
  public boolean isStarted();
  
  public Job scheduleJob(Job job) throws JobException;
  
  public boolean unscheduleJob(String jobId) throws JobException;
  
  public Date getNextFiring(String jobId) throws JobException;
  
  public Job executeJob(Job job) throws JobException;
}
