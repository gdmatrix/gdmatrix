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
public class DBClassPeriodPropertyValuePK implements Serializable
{
  private int periodId;
  private String name;
  private int index;
  
  public DBClassPeriodPropertyValuePK(
    int periodId, String name, int index)
  {
    this.periodId = periodId;
    this.name = name;
    this.index = index;
  }

  public int getPeriodId()
  {
    return periodId;
  }

  public void setPeriodId(int periodId)
  {
    this.periodId = periodId;
  }
  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public int getIndex()
  {
    return index;
  }

  public void setIndex(int index)
  {
    this.index = index;
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
    final DBClassPeriodPropertyValuePK other = (DBClassPeriodPropertyValuePK) obj;
    if (this.periodId != other.periodId)
    {
      return false;
    }
    if ((this.name == null) ?
      (other.name != null) : !this.name.equals(other.name))
    {
      return false;
    }
    if (this.index != other.index)
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 83 * hash + this.periodId;
    hash = 83 * hash + (this.name != null ? this.name.hashCode() : 0);
    hash = 83 * hash + this.index;
    return hash;
  }
}
