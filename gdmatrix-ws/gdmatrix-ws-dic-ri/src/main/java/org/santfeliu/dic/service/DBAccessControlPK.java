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

import java.io.Serializable;

/**
 *
 * @author realor
 */
public class DBAccessControlPK implements Serializable
{
  private String typeId;
  private String roleId;
  private String action;

  public String getTypeId()
  {
    return typeId;
  }

  public void setTypeId(String typeId)
  {
    this.typeId = typeId;
  }

  public String getRoleId()
  {
    return roleId;
  }

  public void setRoleId(String roleId)
  {
    this.roleId = roleId;
  }

  public String getAction()
  {
    return action;
  }

  public void setAction(String action)
  {
    this.action = action;
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
    final DBAccessControlPK other = (DBAccessControlPK) obj;
    return typeId.equals(other.typeId) && roleId.equals(other.roleId)
      && action.equals(other.action);
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 29 * hash + (this.typeId != null ? this.typeId.hashCode() : 0);
    hash = 29 * hash + (this.roleId != null ? this.roleId.hashCode() : 0);
    hash = 29 * hash + (this.action != null ? this.action.hashCode() : 0);
    return hash;
  }
}
