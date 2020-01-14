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
package org.santfeliu.presence.service;

import java.util.Date;
import org.santfeliu.presence.util.Utils;
import org.matrix.presence.PresenceEntry;
import org.santfeliu.jpa.JPAUtils;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author realor
 */
public class DBPresenceEntry extends PresenceEntry
{
  @Override
  public void setPersonId(String personId)
  {
    super.setPersonId(personId);
    updateEntryId();
  }

  @Override
  public void setStartDateTime(String startDateTime)
  {
    super.setStartDateTime(startDateTime);
    updateEntryId();
  }
  
  public String getManipulatedValue()
  {
    return isManipulated() ? "T" : "F";
  }
  
  public void setManipulatedValue(String value)
  {
    setManipulated("T".equals(value));
  }
  
  public void copyFrom(PresenceEntry presenceEntry)
  {
    JPAUtils.copy(presenceEntry, this);
    this.setManipulated(presenceEntry.isManipulated());
  }
  
  public void updateEntryId()
  {
    DBPresenceEntryPK pk = 
      new DBPresenceEntryPK(getPersonId(), getStartDateTime());
    setEntryId(pk.toString());    
  }
  
  public void updateDuration()
  {
    if (endDateTime != null)
    {
      duration = Utils.getDuration(startDateTime, endDateTime);
    }
    else
    {
      duration = 0;
    }
  }
  
  public void shrink(int seconds)
  {
    endDateTime = Utils.shrink(startDateTime, endDateTime, seconds);
  }
  
  public int getCurrentDuration()
  {
    if (endDateTime == null)
    {
      String nowDateTime = TextUtils.formatDate(new Date(), "yyyyMMddHHmmss");
      return Utils.getDuration(startDateTime, nowDateTime);
    }
    else
    {
      return Utils.getDuration(startDateTime, endDateTime);
    }
  }
}
