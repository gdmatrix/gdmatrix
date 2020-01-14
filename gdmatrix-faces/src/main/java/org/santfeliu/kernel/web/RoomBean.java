package org.santfeliu.kernel.web;


import java.util.List;
import org.matrix.kernel.Room;
import org.matrix.kernel.RoomFilter;
import org.matrix.kernel.RoomView;
import org.santfeliu.web.obj.ObjectBean;

public class RoomBean extends ObjectBean
{
  public RoomBean()
  {
  }

  public String getObjectTypeId()
  {
    return "Room";
  }
  
  @Override
  public String getDescription()
  {
    RoomMainBean roomMainBean = (RoomMainBean)getBean("roomMainBean");
    Room room = roomMainBean.getRoom();
    return getRoomDescription(room);
  }
  
  @Override
  public String getDescription(String objectId)
  {
    StringBuilder buffer = new StringBuilder();
    try
    {
      RoomFilter filter = new RoomFilter();
      filter.getRoomIdList().add(objectId);
      List<RoomView> roomViews =
        KernelConfigBean.getPort().findRoomViews(filter);
      if (roomViews.size() > 0)
      {
        RoomView roomView = roomViews.get(0);
        buffer.append(roomView.getDescription());
        return buffer.toString();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return objectId;
  }

  private String getRoomDescription(Room room)
  {
    StringBuilder buffer = new StringBuilder();
    if (room != null)
    {
      buffer.append(room.getName());
      buffer.append(" (");
      buffer.append(room.getRoomId());
      buffer.append(")");
    }
    return buffer.toString();
  }

  @Override
  public String remove()
  {
    try
    {
      if (!isNew())
      {
        KernelConfigBean.getPort().removeRoom(getObjectId());
        removed();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return getControllerBean().show();
  }
}
