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
public class DBClassPeriodPK implements Serializable
{
  private String classId;
  private String startDateTime;

  public DBClassPeriodPK(String classId, String startDateTime)
  {
    this.classId = classId;
    this.startDateTime = startDateTime;
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

  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }
    if (getClass() != obj.getClass())
    {
      return false;
    }
    final DBClassPeriodPK other = (DBClassPeriodPK) obj;
    if ((this.classId == null) ?
      (other.classId != null) : !this.classId.equals(other.classId))
    {
      return false;
    }
    if ((this.startDateTime == null) ?
      (other.startDateTime != null) :
        !this.startDateTime.equals(other.startDateTime))
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 5;
    hash = 97 * hash + (this.classId != null ? this.classId.hashCode() : 0);
    hash = 97 * hash + (this.startDateTime != null ? this.startDateTime.hashCode() : 0);
    return hash;
  }
}
