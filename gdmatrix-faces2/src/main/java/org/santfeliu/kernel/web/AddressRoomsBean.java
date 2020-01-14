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

import javax.faces.model.SelectItem;

import org.matrix.kernel.KernelManagerPort;
import org.matrix.kernel.Room;
import org.matrix.kernel.RoomFilter;
import org.matrix.kernel.RoomView;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;

import org.santfeliu.web.obj.PageBean;


/**
 *
 * @author unknown
 */
public class AddressRoomsBean extends PageBean
{
  private String roomId;
  private List<RoomView> rows;

  public AddressRoomsBean()
  {
    load();
  }

  public void setRoomId(String roomId)
  {
    this.roomId = roomId;
  }

  public String getRoomId()
  {
    return roomId;
  }

  public void setRows(List<RoomView> rows)
  {
    this.rows = rows;
  }

  public List<RoomView> getRows()
  {
    return rows;
  }

  public boolean isModified()
  {
    return false;
  }
  
  public String show()
  {
    return "address_rooms";
  }

  public String store()
  {
    return show();
  }

  public String showRoom()
  {
    return getControllerBean().showObject("Room",
      (String)getValue("#{row.roomId}"));
  }

  public String searchRoom()
  {
    return getControllerBean().searchObject("Room",
      "#{addressRoomsBean.roomId}");
  }

  public String removeRoom()
  {
    try
    {
      RoomView row = (RoomView)getRequestMap().get("row");
      KernelManagerPort port = KernelConfigBean.getPort();
      port.removeRoom(row.getRoomId());
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String addRoom()
  {
    try
    {
      String addressId = getObjectId();
      KernelManagerPort port = KernelConfigBean.getPort();
      Room room = new Room();
      room.setRoomId(roomId);
      room.setAddressId(addressId);
      port.storeRoom(room);
      this.roomId = null;
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  public List<SelectItem> getRoomSelectItems()
  {
    RoomBean roomBean = (RoomBean)getBean("roomBean");
    return roomBean.getSelectItems(roomId);
  }
  
  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }

  private void load()
  {
    try
    {
      if (!isNew())
      {
        RoomFilter filter = new RoomFilter();
        filter.setAddressId(getObjectId());
        rows = KernelConfigBean.getPort().findRoomViews(filter);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public String getTypeDescription()
  {
    String description = null;
    RoomView row = (RoomView)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    String typeId = row.getRoomTypeId();
    if (typeId != null)
    {
      Type type = TypeCache.getInstance().getType(typeId);
      description = type != null && type.getDescription() != null ?
        type.getDescription() : typeId;
    }
    return description;
  }
}
