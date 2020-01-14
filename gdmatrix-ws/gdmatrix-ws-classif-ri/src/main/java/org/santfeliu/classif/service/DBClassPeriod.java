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
package org.santfeliu.classif.service;

import java.io.Serializable;

/**
 *
 * @author realor
 */
public class DBClassPeriod implements Serializable
{
  private String classId;
  private String startDateTime;
  private String endDateTime;
  private String classTypeId;
  private String title;
  private String description;
  private String location;
  private String offlineDownloadValue;
  private String superClassId;
  private String accessControlUserId;
  private String changeDateTime;
  private String changeUserId;
  private String changeReason;
  private Integer periodId;

  public DBClassPeriod()
  {
  }

  public String getAccessControlUserId()
  {
    return accessControlUserId;
  }

  public void setAccessControlUserId(String accessControlUserId)
  {
    this.accessControlUserId = accessControlUserId;
  }

  public String getClassId()
  {
    return classId;
  }

  public void setClassId(String classId)
  {
    this.classId = classId;
  }

  public String getStartDateTime()
  {
    return startDateTime;
  }

  public void setStartDateTime(String startDateTime)
  {
    this.startDateTime = startDateTime;
  }

  public String getEndDateTime()
  {
    return endDateTime;
  }

  public void setEndDateTime(String endDateTime)
  {
    this.endDateTime = endDateTime;
  }

  public String getClassTypeId()
  {
    return classTypeId;
  }

  public void setClassTypeId(String classTypeId)
  {
    this.classTypeId = classTypeId;
  }

  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getLocation()
  {
    return location;
  }

  public void setLocation(String location)
  {
    this.location = location;
  }

  public String getOfflineDownloadValue()
  {
    return offlineDownloadValue;
  }

  public void setOfflineDownloadValue(String offlineDownloadValue)
  {
    this.offlineDownloadValue = offlineDownloadValue;
  }

  public String getSuperClassId()
  {
    return superClassId;
  }

  public void setSuperClassId(String superClassId)
  {
    this.superClassId = superClassId;
  }

  public String getChangeDateTime()
  {
    return changeDateTime;
  }

  public void setChangeDateTime(String changeDateTime)
  {
    this.changeDateTime = changeDateTime;
  }

  public String getChangeUserId()
  {
    return changeUserId;
  }

  public void setChangeUserId(String changeUserId)
  {
    this.changeUserId = changeUserId;
  }

  public String getChangeReason()
  {
    return changeReason;
  }

  public void setChangeReason(String changeReason)
  {
    this.changeReason = changeReason;
  }

  public Integer getPeriodId()
  {
    return periodId;
  }

  public void setPeriodId(Integer periodId)
  {
    this.periodId = periodId;
  }

  public void copyTo(org.matrix.classif.Class classObject)
  {
    classObject.setClassId(classId);
    classObject.setStartDateTime(startDateTime);
    classObject.setEndDateTime(endDateTime);
    classObject.setClassTypeId(classTypeId);
    classObject.setTitle(title);
    classObject.setDescription(description);
    classObject.setLocation(location);
    classObject.setOfflineDownload("T".equals(offlineDownloadValue));
    classObject.setSuperClassId(superClassId);
    classObject.setAccessControlUserId(accessControlUserId);
    classObject.setChangeDateTime(changeDateTime);
    classObject.setChangeUserId(changeUserId);
    classObject.setChangeReason(changeReason);
  }

  public void copyFrom(org.matrix.classif.Class classObject)
  {
    classId = classObject.getClassId();
    classTypeId = classObject.getClassTypeId();
    title = classObject.getTitle();
    description = classObject.getDescription();
    location = classObject.getLocation();
    offlineDownloadValue = classObject.isOfflineDownload() ? "T" : "F";
    superClassId = classObject.getSuperClassId();
    accessControlUserId = classObject.getAccessControlUserId();
    changeReason = classObject.getChangeReason();
  }
}
