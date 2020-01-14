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
import java.util.Date;
import java.util.List;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author realor
 */
public class PresenceEntry implements Serializable
{
  private String dateTime;
  private String personId;  
  private String type;
  private String reason;
  private boolean manipulated;
  private String creationDateTime;
  private String ipAddress;
  private int duration;
  private int workedTime;
  private int bonusTime;

  public String getDateTime()
  {
    return dateTime;
  }

  public void setDateTime(String dateTime)
  {
    this.dateTime = dateTime;
  }

  public String getPersonId()
  {
    return personId;
  }

  public void setPersonId(String personId)
  {
    this.personId = personId;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String getCreationDateTime()
  {
    return creationDateTime;
  }

  public void setCreationDateTime(String creationDateTime)
  {
    this.creationDateTime = creationDateTime;
  }

  public boolean isManipulated()
  {
    return manipulated;
  }

  public void setManipulated(boolean manipulated)
  {
    this.manipulated = manipulated;
  }

  public boolean isTimeAltered()
  {
    return !dateTime.equals(creationDateTime);
  }

  public boolean isBonified()
  {
    return bonusTime > 0;
  }

  public String getReason()
  {
    return reason;
  }

  public void setReason(String reason)
  {
    this.reason = reason;
  }

  public Date getDate()
  {
    return TextUtils.parseInternalDate(dateTime);
  }

  public void setDate(Date date)
  {
    dateTime = TextUtils.formatDate(date, "yyyyMMddHHmmss");
  }

  public Date getCreationDate()
  {
    return TextUtils.parseInternalDate(creationDateTime);
  }

  public String getIpAddress()
  {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress)
  {
    this.ipAddress = ipAddress;
  }

  public int getDuration()
  {
    return duration;
  }

  public int getDuration(Date nowDate)
  {
    int durationAtTime = duration;
    if (durationAtTime == 0) // last entry
    {
      Date entryDate = getDate();
      durationAtTime = (int)((nowDate.getTime() - entryDate.getTime()) / 1000L);
    }
    return durationAtTime;
  }

  public void setDuration(int duration)
  {
    this.duration = duration;
  }

  public int getWorkedTime()
  {
    return workedTime;
  }

  public void setWorkedTime(int workedTime)
  {
    this.workedTime = workedTime;
  }

  public int getBonusTime()
  {
    return bonusTime;
  }

  public void setBonusTime(int bonusTime)
  {
    this.bonusTime = bonusTime;
  }

  public PresenceEntryType getEntryType(List<PresenceEntryType> entryTypes)
  {
    PresenceEntryType entryType = null;
    int i = 0;
    while (entryType == null && i < entryTypes.size())
    {
      PresenceEntryType current = entryTypes.get(i);
      if (type.equals(current.getType()))
      {
        entryType = current;
      }
      else i++;
    }
    return entryType;
  }
}
