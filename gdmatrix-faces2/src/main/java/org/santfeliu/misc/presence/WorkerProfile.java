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
package org.santfeliu.misc.presence;

import java.io.Serializable;

/**
 *
 * @author realor
 */
public class WorkerProfile implements Serializable
{
  private String userId;
  private String personId;
  private String displayName;
  private String caseId;
  private int workingTime; // in seconds for week
  private int bonusTime; // in seconds
  private String bonusStartDate; // apply bonus only after this date

  public String getUserId()
  {
    return userId;
  }

  public void setUserId(String userId)
  {
    this.userId = userId;
  }

  public String getPersonId()
  {
    return personId;
  }

  public void setPersonId(String personId)
  {
    this.personId = personId;
  }

  public String getDisplayName()
  {
    return displayName;
  }

  public void setDisplayName(String displayName)
  {
    this.displayName = displayName;
  }

  public String getCaseId()
  {
    return caseId;
  }

  public void setCaseId(String caseId)
  {
    this.caseId = caseId;
  }

  public int getWorkingTime()
  {
    return workingTime;
  }

  public void setWorkingTime(int workingTime)
  {
    this.workingTime = workingTime;
  }

  public int getBonusTime()
  {
    return bonusTime;
  }

  public void setBonusTime(int bonusTime)
  {
    this.bonusTime = bonusTime;
  }

  public String getBonusStartDate()
  {
    return bonusStartDate;
  }

  public void setBonusStartDate(String bonusStartDate)
  {
    this.bonusStartDate = bonusStartDate;
  }

  @Override
  public String toString()
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append("userId: ").append(userId).append("\n");
    buffer.append("personId: ").append(personId).append("\n");
    buffer.append("displayName: ").append(displayName).append("\n");
    buffer.append("caseId: ").append(caseId).append("\n");
    buffer.append("workingTime: ").append(workingTime).append("\n");
    buffer.append("bonusTime: ").append(bonusTime).append("\n");
    buffer.append("bonusStartDate: ").append(bonusStartDate).append("\n");

    return buffer.toString();
  }
}
