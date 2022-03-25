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
package org.santfeliu.job.store.cases;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import org.matrix.cases.Case;
import org.matrix.dic.Property;
import org.matrix.job.Job;
import static org.santfeliu.dic.util.DictionaryUtils.*;

/**
 *
 * @author blanquepa
 */
public class JobCaseConverter
{

  public static Case jobToCase(Job job)
  {
    Case cas = null;
    if (job != null)
    {
      cas = new Case();
      
      cas.setCaseId(job.getJobId());
      cas.setCaseTypeId(job.getJobType());      
      cas.setTitle(job.getName());
      cas.setDescription(job.getDescription());
      String startDateTime = job.getStartDateTime();
      if (startDateTime != null)
      {  
        cas.setStartDate(startDateTime.substring(0, 8));
        cas.setStartTime(startDateTime.substring(8));
      }
      String endDateTime = job.getEndDateTime();
      if (endDateTime != null)
      {
        cas.setEndDate(endDateTime.substring(0, 8));
        cas.setEndTime(endDateTime.substring(8));
      }
      setProperty(cas, "dayOfMonth", job.getDayOfMonth());
      setProperty(cas, "dayOfWeek", job.getDayOfWeek());
      setProperty(cas, "interval", job.getInterval());
      setProperty(cas, "repetitions", job.getRepetitions());
      setProperty(cas, "unitOfTime", job.getUnitOfTime());
      setProperty(cas, "audit", String.valueOf(job.isAudit()));
      setProperty(cas, "locked", String.valueOf(job.isLocked()));
      List<Property> jobProperties = job.getProperty();
      removeSystemProperties(jobProperties);
      cas.getProperty().addAll(jobProperties);
    }
    return cas;
  }

  public static Job caseToJob(Case cas)
  {
    Job job = null;
    if (cas != null)
    {
      job = new Job();

      job.setJobId(cas.getCaseId());
      job.setName(cas.getTitle());
      job.setDescription(cas.getDescription());
      String startDateTime = 
        (cas.getStartDate() != null && cas.getStartTime() != null ? 
         cas.getStartDate() + cas.getStartTime() : null);
      job.setStartDateTime(startDateTime);
      String endDateTime = 
        (cas.getEndDate() != null && cas.getEndTime() != null ? 
         cas.getEndDate() + cas.getEndTime() : null);      
      job.setEndDateTime(endDateTime);
      job.setJobType(cas.getCaseTypeId());

      List<Property> properties = cas.getProperty();
      String dayOfMonth = getPropertyValue(properties, "dayOfMonth");
      if (dayOfMonth != null)
        job.setDayOfMonth(dayOfMonth);
      Property dayOfWeekProp = getProperty(cas, "dayOfWeek");
      if (dayOfWeekProp != null)
      {
        job.getDayOfWeek().retainAll(Collections.EMPTY_LIST);
        job.getDayOfWeek().addAll(dayOfWeekProp.getValue());
      }
      String interval = getPropertyValue(properties, "interval");
      if (interval != null)
        job.setInterval(Integer.valueOf(interval));
      String repetitions = getPropertyValue(properties, "repetitions");
      if (repetitions != null)
        job.setRepetitions(Integer.parseInt(repetitions));
      job.setUnitOfTime(getPropertyValue(properties, "unitOfTime"));
      String audit = getPropertyValue(properties, "audit");
      job.setAudit((audit != null && audit.equalsIgnoreCase("true")));
      String locked = getPropertyValue(properties, "locked");
      job.setLocked((locked != null && locked.equalsIgnoreCase("true")));
      removeSystemProperties(properties);
      job.getProperty().addAll(properties);
    }

    return job;
  }
  
  private static void removeSystemProperties(List<Property> properties)
  {
    Field[] fields = Job.class.getDeclaredFields();
    for (Field field : fields)
    {
      properties.remove(getPropertyByName(properties, field.getName()));
    }    
  }
}
