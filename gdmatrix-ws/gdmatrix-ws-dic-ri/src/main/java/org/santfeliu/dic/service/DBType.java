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
package org.santfeliu.dic.service;

import org.matrix.dic.Type;
import org.santfeliu.jpa.JPAUtils;

/**
 *
 * @author realor
 */
public class DBType extends Type
{ 
  private String instantiableValue;
  private String restrictedValue;
  private String removed = "F";

  public DBType()
  {
  }
  
  public DBType(Type type)
  {
    copyFrom(type);
  }

  public String getInstantiableValue()
  {
    return instantiableValue;
  }

  public void setInstantiableValue(String instantiableValue)
  {
    this.instantiableValue = instantiableValue;
  }

  public String getRestrictedValue()
  {
    return restrictedValue;
  }

  public void setRestrictedValue(String restrictedValue)
  {
    this.restrictedValue = restrictedValue;
  }

  public String getRemoved()
  {
    return removed;
  }

  public void setRemoved(String removed)
  {
    this.removed = removed;
  }

  public boolean isRemoved()
  {
    return (removed != null && "T".equalsIgnoreCase(removed));
  }

  public void copyTo(Type type)
  {
    JPAUtils.copy(this, type);
    type.setInstantiable("T".equals(instantiableValue));
    type.setRestricted("T".equals(restrictedValue));
  }

  public void copyFrom(Type type)
  {
    setTypeId(type.getTypeId());
    setSuperTypeId(type.getSuperTypeId());
    setDescription(type.getDescription());
    setDetail(type.getDetail());
    setInstantiable(type.isInstantiable());
    instantiableValue = type.isInstantiable() ? "T" : "F";
    restrictedValue = type.isRestricted() ? "T" : "F";
  }
}
