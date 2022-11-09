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
package org.matrix.pf.kernel;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import org.matrix.kernel.AddressFilter;
import org.matrix.kernel.AddressView;
import org.matrix.kernel.Room;
import org.matrix.pf.web.PageBacking;
import org.matrix.pf.web.MainPage;
import org.matrix.pf.web.helper.TabHelper;
import org.matrix.pf.web.helper.TypedHelper;
import org.matrix.pf.web.helper.TypedTabPage;
import org.matrix.web.WebUtils;
import org.primefaces.event.SelectEvent;
import org.santfeliu.kernel.web.KernelConfigBean;

/**
 *
 * @author lopezrj-sf
 */
@Named("roomMainBacking")
public class RoomMainBacking extends PageBacking 
  implements TypedTabPage, MainPage
{
  public static final String OUTCOME = "pf_room_main";
  
  private static final String ADDRESS_BACKING = "addressBacking";  
    
  private Room room;
  private SelectItem addressSelectItem;
  
  //Helpers
  private TypedHelper typedHelper;
  private TabHelper tabHelper;
  
  //ObjectBacking
  RoomBacking roomBacking;
  
  public RoomMainBacking()
  {    
  }
  
  @PostConstruct
  public void init()
  {
    roomBacking = WebUtils.getBacking("roomBacking");
    typedHelper = new TypedHelper(this); 
    tabHelper = new TabHelper(this);
    populate();
  }

  @Override
  public RoomBacking getObjectBacking()
  {
    return roomBacking;
  }

  @Override
  public String getRootTypeId()
  {
    return roomBacking.getRootTypeId();
  }

  @Override
  public String getTypeId()
  {
    return room.getRoomTypeId();
  }

  @Override
  public TypedHelper getTypedHelper()
  {
    return typedHelper;
  }

  @Override
  public TabHelper getTabHelper()
  {
    return tabHelper;
  }

  public Room getRoom()
  {
    return room;
  }

  public void setRoom(Room room)
  {
    this.room = room;
  }

  public SelectItem getAddressSelectItem()
  {
    return addressSelectItem;
  }
  
  public void setAddressSelectItem(SelectItem addressSelectItem)
  {
    this.addressSelectItem = addressSelectItem;
    if (room != null && addressSelectItem != null)
      this.room.setAddressId((String)addressSelectItem.getValue());
  }

  public void selectAddress(String addressId)
  {
    if (room != null)
    {
      room.setAddressId(addressId);  
      loadAddressSelectItem(addressId);      
    }
  }  
  
  @Override
  public String show(String pageId)
  {
    roomBacking.setObjectId(pageId);
    return show();
  }  
  
  @Override
  public String show()
  {
    populate();
    return OUTCOME;
  }
  
  @Override
  public String store()
  {
    try
    {
      room = KernelConfigBean.getPort().storeRoom(room);
      roomBacking.setObjectId(room.getRoomId());
      info("STORE_OBJECT");
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return show();
  }
  
  public String remove()
  {
    error("Not implemented yet");
    return null;
  }
    
  @Override
  public String getPageObjectId()
  {
    return roomBacking.getObjectId();
  }

  @Override
  public void create()
  {
    room = new Room();
  }
          
  @Override
  public void load()
  {
    String roomId = getPageObjectId();
    if (roomId != null)
    {
      room = KernelConfigBean.getPort().loadRoom(roomId);
      loadAddressSelectItem(room.getAddressId());
    }
  }

  @Override
  public void reset()
  {
    if (roomBacking.isNew())
      create();
    else
      room = null;
    addressSelectItem = null;
  }
  
  @Override
  public String cancel()
  {
    populate();
    info("CANCEL_OBJECT");    
    return null;
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
  
  public List<SelectItem> completeAddress(String query)
  {
    return completeAddress(query, room.getAddressId());
  }
  
  private List<SelectItem> completeAddress(String query, String addressId)
  {
    ArrayList<SelectItem> items = new ArrayList();
    AddressBacking addressBacking = WebUtils.getBacking(ADDRESS_BACKING);
    
    //Add current item
    if (!roomBacking.isNew())
    {
      String description = "";
      if (addressId != null)
        description = getDescription(addressBacking, addressId);
      items.add(new SelectItem(addressId, description));
    }
        
    //Query search
    if (query != null && query.length() >= 3)
    {
      AddressFilter filter = new AddressFilter();
      filter.setStreetName("\"" + query + "\"");
      filter.setMaxResults(10);
      List<AddressView> addresses = 
        KernelConfigBean.getPort().findAddressViews(filter);
      if (addresses != null)
      {       
        for (AddressView address : addresses)
        {
          String description = addressBacking.getDescription(address);
          SelectItem item = new SelectItem(address.getAddressId(), description);
          items.add(item);
        }
      }
    }
    else
    {
      //Add favorites
      items.addAll(addressBacking.getFavorites()); 
    }    
    return items;
  }  
  
  private void loadAddressSelectItem(String addressId)
  {
    AddressBacking addressBacking = WebUtils.getBacking("addressBacking");
    if (room != null)
    {
      String description = getDescription(addressBacking, addressId);
      addressSelectItem = new SelectItem(addressId, description);
    }
  }

}
