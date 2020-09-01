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

import org.matrix.job.Job;

/**
 *
 * @author blanquepa
 */
public class JobException extends Exception
{
  private Job job;
  
  public JobException()
  {
    super();
  }
  
  public JobException(String message, Job...job)
  {
    super(message);
    if (job != null && job.length > 0)
      this.job = job[0];    
  }
  
  public JobException(Throwable cause, Job...job)
  {
    super(cause);
    if (job != null && job.length > 0)
      this.job = job[0];
  }

  public Job getJob()
  {
    return job;
  }
  
  @Override
  public String getMessage()
  {
    String message = super.getMessage();
    if (job != null)
      message = message + " [" + job.getJobId() + "]";
    return message;
  }
 
}
