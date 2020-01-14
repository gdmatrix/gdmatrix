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

import org.matrix.presence.AbsenceCounting;
import org.matrix.presence.AbsenceType;

/**
 *
 * @author realor
 */
public class DBAbsenceType extends AbsenceType
{
  public String getAuthorizableValue()
  {
    return isAuthorizable() ? "T" : "F";
  }

  public void setAuthorizableValue(String value)
  {
    setAuthorizable("T".equals(value));
  }

  public String getJustificableValue()
  {
    return isJustificable() ? "T" : "F";
  }

  public void setJustificableValue(String value)
  {
    setJustificable("T".equals(value));
  }

  public String getCounterVisibleValue()
  {
    return isCounterVisible() ? "T" : "F";
  }

  public void setCounterVisibleValue(String value)
  {
    setCounterVisible("T".equals(value));
  }
  
  public String getHolidayValue()
  {
    return isHoliday() ? "T" : "F";
  }

  public void setHolidayValue(String value)
  {
    setHoliday("T".equals(value));
  }

  public String getEnabledValue()
  {
    return isEnabled() ? "T" : "F";
  }

  public void setEnabledValue(String value)
  {
    setEnabled("T".equals(value));
  }

  public String getCarryValue()
  {
    return isCarry() ? "T" : "F";
  }

  public void setCarryValue(String value)
  {
    setCarry("T".equals(value));
  }
  
  public String getCountingValue()
  {
    String result;
    if (AbsenceCounting.FRACTIONABLE_DAYS.equals(counting)) result = "F";
    else if (AbsenceCounting.HOURS.equals(counting)) result = "H";
    else result = "I";
    
    return result;
  }
  
  public void setCountingValue(String value)
  {
    if ("F".equals(value)) setCounting(AbsenceCounting.FRACTIONABLE_DAYS);
    else if ("H".equals(value)) setCounting(AbsenceCounting.HOURS);
    else setCounting(AbsenceCounting.DAYS);
  }
  
  public void copyFrom(AbsenceType absenceType)
  {
    this.absenceTypeId = absenceType.getAbsenceTypeId();
    this.label = absenceType.getLabel();
    this.description = absenceType.getDescription();
    this.holiday = absenceType.isHoliday();
    this.enabled = absenceType.isEnabled();
    this.counterVisible = absenceType.isCounterVisible();
    this.justificable = absenceType.isJustificable();
    this.authorizable = absenceType.isAuthorizable();
    this.counting = absenceType.getCounting();
    this.defaultTime = absenceType.getDefaultTime();
    this.carry = absenceType.isCarry();
    this.position = absenceType.getPosition();
    this.entryTypeId = absenceType.getEntryTypeId();
  }
}
