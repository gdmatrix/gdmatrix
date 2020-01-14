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

import org.matrix.presence.PresenceEntryType;

/**
 *
 * @author realor
 */
public class DBPresenceEntryType extends PresenceEntryType
{
  public String getEnabledValue()
  {
    return isEnabled() ? "T" : "F";
  }

  public void setEnabledValue(String value)
  {
    setEnabled("T".equals(value));
  }
  
  public String getRealWorkValue()
  {
    return isRealWork() ? "T" : "F";
  }

  public void setRealWorkValue(String value)
  {
    setRealWork("T".equals(value));
  }

  public String getConsolidableValue()
  {
    return isConsolidable() ? "T" : "F";
  }

  public void setConsolidableValue(String value)
  {
    setConsolidable("T".equals(value));
  }
  
  public String getAbsenceValue()
  {
    return isAbsence() ? "T" : "F";
  }

  public void setAbsenceValue(String value)
  {
    setAbsence("T".equals(value));
  }

  public void copyFrom(PresenceEntryType presenceEntryType)
  {
    this.entryTypeId = presenceEntryType.getEntryTypeId();
    this.label = presenceEntryType.getLabel();
    this.description = presenceEntryType.getDescription();
    this.maxWorkedTime = presenceEntryType.getMaxWorkedTime();
    this.enabled = presenceEntryType.isEnabled();
    this.realWork = presenceEntryType.isRealWork();
    this.consolidable = presenceEntryType.isConsolidable();
    this.absence = presenceEntryType.isAbsence();
    this.position = presenceEntryType.getPosition();
    this.color = presenceEntryType.getColor();
    this.filter = presenceEntryType.getFilter();
    this.code = presenceEntryType.getCode();
  }
}
