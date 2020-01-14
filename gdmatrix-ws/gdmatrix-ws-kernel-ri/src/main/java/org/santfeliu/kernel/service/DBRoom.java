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

import org.matrix.kernel.Room;
import org.matrix.kernel.RoomView;
import org.santfeliu.jpa.JPAUtils;

/**
 *
 * @author blanquepa
 */
public class DBRoom extends Room
{
//  private InternalValueConverter typeIdConverter =
//    new InternalValueConverter(DictionaryConstants.ROOM_TYPE);

  public void copyTo(Room room)
  {
    JPAUtils.copy(this, room);
    room.setRoomId(this.addressId + KernelManager.PK_SEPARATOR + this.roomId);
    room.setRoomTypeId(this.roomTypeId);
//    room.setRoomTypeId(typeIdConverter.getTypeId(roomTypeId));
  }

  public void copyTo(RoomView roomView)
  {
    roomView.setRoomId(this.addressId + KernelManager.PK_SEPARATOR + this.roomId);
//    roomView.setRoomTypeId(typeIdConverter.getTypeId(roomTypeId));
    roomView.setRoomTypeId(this.roomTypeId);
    roomView.setDescription(this.name);
  }

  public void copyFrom(Room room)
  {
    JPAUtils.copy(room, this);
    String roomId = room.getRoomId();
    if (roomId != null)
    {
      String[] pk = roomId.split(KernelManager.PK_SEPARATOR);
      this.addressId = pk[0];
      this.roomId = pk[1];
    }
    else
      this.addressId = room.getAddressId();
//    String typeId = typeIdConverter.fromTypeId(room.getRoomTypeId());
//    this.roomTypeId = DictionaryConstants.ROOM_TYPE.equals(typeId) ? null : typeId;
    this.roomTypeId = room.getRoomTypeId();
  }

}
