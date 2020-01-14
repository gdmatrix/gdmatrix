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
package org.santfeliu.kernel.service;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author unknown
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RoomPK", propOrder = {
    "addressId",
    "roomId"
})
public class DBRoomPK
{
  private String addressId;
  private String roomId;
  
  public DBRoomPK(String roomPK)
  {
    String[] pk = roomPK.split(KernelManager.PK_SEPARATOR);
    this.addressId = pk[0];
    this.roomId = pk[1];
  }

  public void setAddressId(String addressId)
  {
    this.addressId = addressId;
  }

  public String getAddressId()
  {
    return addressId;
  }

  public void setRoomId(String roomId)
  {
    this.roomId = roomId;
  }

  public String getRoomId()
  {
    return roomId;
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
    final DBRoomPK other = (DBRoomPK) obj;
    if ((this.addressId == null) ? (other.addressId != null) : !this.addressId.equals(other.addressId))
    {
      return false;
    }
    if ((this.roomId == null) ? (other.roomId != null) : !this.roomId.equals(other.roomId))
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 97 * hash + (this.addressId != null ? this.addressId.hashCode() : 0);
    hash = 97 * hash + (this.roomId != null ? this.roomId.hashCode() : 0);
    return hash;
  }

  
}
