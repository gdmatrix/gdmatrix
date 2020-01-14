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
package org.santfeliu.kernel.web;


import java.util.List;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import org.matrix.dic.DictionaryConstants;

import org.matrix.kernel.KernelConstants;
import org.matrix.kernel.Room;

import org.santfeliu.web.obj.ControllerBean;
import org.santfeliu.web.obj.TypifiedPageBean;


/**
 *
 * @author unknown
 */
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
