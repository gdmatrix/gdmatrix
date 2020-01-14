package com.audifilm.matrix.kernel.service;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

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