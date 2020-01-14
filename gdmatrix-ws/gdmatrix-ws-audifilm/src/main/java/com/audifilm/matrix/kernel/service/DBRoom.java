package com.audifilm.matrix.kernel.service;

import org.matrix.kernel.Room;
import org.matrix.kernel.RoomView;
import org.santfeliu.jpa.JPAUtils;

/**
 *
 * @author blanquepa
 */
public class DBRoom extends Room
{
  private DBAddress address;

  public DBAddress getAddress()
  {
    return address;
  }

  public void setAddress(DBAddress address)
  {
    this.address = address;
  }
  
  public void copyTo(Room room)
  {
    JPAUtils.copy(this, room);
    room.setRoomId(this.addressId + KernelManager.PK_SEPARATOR + this.roomId);
  }

  public void copyTo(RoomView roomView)
  {
    roomView.setRoomId(this.addressId + KernelManager.PK_SEPARATOR + this.roomId);
    roomView.setDescription(this.name);
    roomView.setRoomTypeId(this.roomTypeId);
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
    {
      this.addressId = room.getAddressId();
    }
    this.roomTypeId = room.getRoomTypeId();
  }
}
