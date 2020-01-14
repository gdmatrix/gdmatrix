package org.santfeliu.kernel.web;


import java.util.List;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import org.matrix.dic.DictionaryConstants;

import org.matrix.kernel.KernelConstants;
import org.matrix.kernel.Room;

import org.santfeliu.web.obj.ControllerBean;
import org.santfeliu.web.obj.TypifiedPageBean;


public class RoomMainBean extends TypifiedPageBean
{
  private Room room;
  private boolean modified;
  private transient List<SelectItem> addressSelectItems;

  public RoomMainBean()
  {
    super(DictionaryConstants.ROOM_TYPE, KernelConstants.KERNEL_ADMIN_ROLE);
    load();
  }

  public void setRoom(Room room)
  {
    this.room = room;
  }

  public Room getRoom()
  {
    if (room == null) room = new Room();
    return room;
  }

  public String show()
  {
    return "room_main";
  }

  public String showAddress()
  {
    return getControllerBean().showObject("Address", room.getAddressId());
  }

  public String searchAddress()
  {
    return getControllerBean().searchObject("Address",
      "#{roomMainBean.room.addressId}");
  }

  public String searchType()
  {
    return searchType("#{roomMainBean.currentTypeId}");
  }

  public String store()
  {
    try
    {
      if (ControllerBean.NEW_OBJECT_ID.equals(room.getRoomId()))
      {
        room.setRoomId(null);
      }
      room = KernelConfigBean.getPort().storeRoom(room);
      setObjectId(room.getRoomId());
    }
    catch (Exception ex)
    {
      error(ex);
    }    
    return show();
  }

  @Override
  public boolean isModified()
  {
    return modified;
  }

  public String showType()
  {
    return getControllerBean().showObject("Type",
      getRoom().getRoomTypeId());
  }

  public boolean isRenderShowTypeButton()
  {
    return getRoom().getRoomTypeId() != null &&
      getRoom().getRoomTypeId().trim().length() > 0;
  }

  public List<SelectItem> getAddressSelectItems()
  {
    if (addressSelectItems == null)
    {
      AddressBean addressBean = (AddressBean)getBean("addressBean");
      addressSelectItems = addressBean.getSelectItems(room.getAddressId());
    }

    return addressSelectItems;
  }

  public void valueChanged(ValueChangeEvent event)
  {
    modified = true;
  }
  
  private void load()
  {
    if (isNew())
    {
      this.room = new Room();
    }
    else
    {
      try
      {
        this.room = KernelConfigBean.getPort().loadRoom(getObjectId());
      } 
      catch (Exception ex)
      {
        getObjectBean().clearObject();
        error(ex); 
        this.room = new Room();
      }
    }
  }
}
