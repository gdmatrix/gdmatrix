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
package org.santfeliu.webapp.modules.kernel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.Address;
import org.matrix.kernel.KernelConstants;
import org.matrix.kernel.Room;
import org.matrix.web.WebUtils;
import org.primefaces.event.SelectEvent;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.web.TypeBean;
import org.santfeliu.faces.ManualScoped;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.Tab;
import org.santfeliu.webapp.helpers.ReferenceHelper;

/**
 *
 * @author lopezrj-sf
 */
@Named
@ManualScoped
public class RoomObjectBean extends ObjectBean
{
  private static final String TYPE_BEAN = "typeBean";  
  
  private Room room = new Room();

  ReferenceHelper<Address> addressReferenceHelper;  
  
  @Inject
  RoomTypeBean roomTypeBean;

  @Inject
  RoomFinderBean roomFinderBean;

  public RoomObjectBean()
  {
    addressReferenceHelper = 
      new AddressReferenceHelper(DictionaryConstants.ADDRESS_TYPE);    
  }

  public ReferenceHelper<Address> getAddressReferenceHelper() 
  {
    return addressReferenceHelper;
  }  
  
  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.ROOM_TYPE;
  }

  @Override
  public RoomTypeBean getTypeBean()
  {
    return roomTypeBean;
  }  
  
  @Override
  public Room getObject()
  {
    return isNew() ? null : room;
  }  
  
  @Override
  public String getDescription()
  {
    return isNew() ? "" : getDescription(room.getRoomId());
  }
  
  public String getDescription(String roomId)
  {
    return getTypeBean().getDescription(roomId);
  }  
  
  @Override
  public RoomFinderBean getFinderBean()
  {
    return roomFinderBean;
  }  
  
  public Room getRoom() 
  {
    return room;
  }

  public void setRoom(Room room) 
  {
    this.room = room;
  }

  @Override
  public String show()
  {
    return "/pages/kernel/room.xhtml";
  }
  
  @Override
  public void loadObject() throws Exception
  {
    if (!NEW_OBJECT_ID.equals(objectId))
    {
      try
      {
        room = KernelModuleBean.getPort(false).loadRoom(objectId);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    else 
    {
      room = new Room();
    }       
  }

  @Override
  public void loadTabs()
  {
    super.loadTabs();

    if (tabs.isEmpty())
    {
      tabs = new ArrayList<>(); // empty list may be read only
      tabs.add(new Tab("Main", "/pages/kernel/room_main.xhtml"));
    }    
  }
  
  @Override
  public void storeObject()
  {    
    try
    {
      room = KernelModuleBean.getPort(false).storeRoom(room);
      setObjectId(room.getRoomId());
      roomFinderBean.outdate();
      info("STORE_OBJECT");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }  
  
  public void onAddressSelect(SelectEvent<SelectItem> event) 
  {
    SelectItem item = event.getObject();
    String addressId = (String)item.getValue();
    room.setAddressId(addressId);
  }

  public void onAddressClear() 
  {
    room.setAddressId(null);
  }
      
  public String getAdminRole() 
  {
    return KernelConstants.KERNEL_ADMIN_ROLE;
  }

  //TODO Use TypedHelper  
  public List<Type> getAllTypes()
  {
    TypeCache tc = TypeCache.getInstance();
    List<Type> types = new ArrayList<>();
    List<SelectItem> items = getAllTypeItems();
    for (SelectItem i : items)
    {
      types.add(tc.getType(String.valueOf(i.getValue())));
    }
    return types;
  }
  
  @Override
  public Serializable saveState()
  {
    return room;
  }

  @Override
  public void restoreState(Serializable state)
  {
    this.room = (Room)state;
  }
  
  private List<SelectItem> getAllTypeItems()
  {
    return getAllTypeItems(DictionaryConstants.READ_ACTION,
      DictionaryConstants.CREATE_ACTION, DictionaryConstants.WRITE_ACTION);
  }

  private List<SelectItem> getAllTypeItems(String... actions)
  {
    try
    {
      String typeId = getRootTypeId();
      TypeBean typeBean = WebUtils.getBacking(TYPE_BEAN);
      String adminRole = getAdminRole();
      return typeBean.getAllSelectItems(typeId, adminRole, actions, true);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return Collections.emptyList();
  }
  
  private class AddressReferenceHelper extends ReferenceHelper<Address>
  {
    public AddressReferenceHelper(String typeId)
    {
      super(typeId);
    }

    @Override
    public String getId(Address address)
    {
      return address.getAddressId();
    }

    @Override
    public String getSelectedId()
    {
      return room != null ? room.getAddressId() : "";
    }

    @Override
    public void setSelectedId(String value)
    {
      if (room != null)
        room.setAddressId(value);
    }
  }
  
}
