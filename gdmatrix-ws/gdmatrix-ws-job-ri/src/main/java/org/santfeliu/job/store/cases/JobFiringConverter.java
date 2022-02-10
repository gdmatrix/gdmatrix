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

import java.util.List;
import org.matrix.cases.Intervention;
import org.matrix.cases.InterventionView;
import org.matrix.dic.Property;
import org.matrix.job.ResponseType;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.job.service.JobFiring;

import static org.santfeliu.job.store.cases.CasesJobStore
  .ERROR_INTERVENTION_TYPE;
import static org.santfeliu.job.store.cases.CasesJobStore
  .SUCCESS_INTERVENTION_TYPE;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author blanquepa
 */
public class JobFiringConverter
{
  public static JobFiring intToJobFiring(Intervention intervention)
  {
    JobFiring jobFiring = null;
    if (intervention != null)
    {
      jobFiring = new JobFiring();
      jobFiring.setJobFiringId(intervention.getIntId());
      jobFiring.setJobId(intervention.getCaseId());
      String sDate = intervention.getStartDate() + intervention.getStartTime();
      jobFiring.setStartDateTime(sDate);
      String eDate = intervention.getEndDate() + intervention.getEndTime();
      jobFiring.setEndDateTime(eDate);
      jobFiring.setMessage(intervention.getComments());
      String intTypeId = intervention.getIntTypeId();
      ResponseType responseType = SUCCESS_INTERVENTION_TYPE.equals(intTypeId) ?
        ResponseType.SUCCESS : ResponseType.ERROR;
      jobFiring.setResponseType(responseType);
      
      List<Property> props = intervention.getProperty();
      String logDocId = 
        DictionaryUtils.getPropertyValue(props, "logDocId");
      jobFiring.setLogId(logDocId);
      String logTitle = 
        DictionaryUtils.getPropertyValue(props, "logTitle");
      jobFiring.setLogTitle(logTitle);      
    }
    
    return jobFiring;
  }
  
  public static JobFiring intToJobFiring(InterventionView intView)
  {
    JobFiring jobFiring = null;
    if (intView != null)
    {
      jobFiring = new JobFiring();
      jobFiring.setJobFiringId(intView.getIntId());
      jobFiring.setJobId(intView.getCaseId());
      String sDate = intView.getStartDate() + intView.getStartTime();
      jobFiring.setStartDateTime(sDate);
      String eDate = intView.getEndDate() + intView.getEndTime();
      jobFiring.setEndDateTime(eDate);
      jobFiring.setMessage(intView.getComments());
      String intTypeId = intView.getIntTypeId();
      ResponseType responseType = SUCCESS_INTERVENTION_TYPE.equals(intTypeId) ?
        ResponseType.SUCCESS : ResponseType.ERROR;
      jobFiring.setResponseType(responseType);  
      
      List<Property> props = intView.getProperty();      
      String logDocId = 
        DictionaryUtils.getPropertyValue(props, "logDocId");
      jobFiring.setLogId(logDocId);
      String logTitle = 
        DictionaryUtils.getPropertyValue(props, "logTitle");
      jobFiring.setLogTitle(logTitle);
    }
    
    return jobFiring;
  }  
  
  public static Intervention jobFiringToInt(JobFiring jobFiring)
  {
    Intervention intervention = null;
    if (jobFiring != null)
    {
      intervention = new Intervention();
      
      if (jobFiring.getMessage() != null)
        intervention.setComments(jobFiring.getMessage());
      
      ResponseType type = jobFiring.getResponseType();
      if (type == ResponseType.ERROR)
        intervention.setIntTypeId(ERROR_INTERVENTION_TYPE);
      else
        intervention.setIntTypeId(SUCCESS_INTERVENTION_TYPE);
      
      intervention.setCaseId(jobFiring.getJobId());
      String startDateTime = jobFiring.getStartDateTime();
      intervention.setStartDate(
        TextUtils.formatInternalDate(startDateTime, "yyyyMMdd"));
      intervention.setStartTime(
        TextUtils.formatInternalDate(startDateTime, "HHmmss"));
      String endDateTime = jobFiring.getEndDateTime();
      intervention.setEndDate(
        TextUtils.formatInternalDate(endDateTime, "yyyyMMdd"));
      intervention.setEndTime(
        TextUtils.formatInternalDate(endDateTime, "HHmmss"));     
  
      if (jobFiring.getLogId() != null)
      {
        DictionaryUtils.setProperty(intervention, "logDocId", 
          jobFiring.getLogId());  
      }
      
      if (jobFiring.getLogTitle() != null)
      {
        DictionaryUtils.setProperty(intervention, "logTitle", 
          jobFiring.getLogTitle());
      }
    }
    
    return intervention;
  }
}
