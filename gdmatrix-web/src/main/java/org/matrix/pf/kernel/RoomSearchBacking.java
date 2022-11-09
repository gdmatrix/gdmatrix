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
import org.matrix.kernel.RoomFilter;
import org.matrix.kernel.RoomView;
import org.matrix.pf.web.SearchBacking;
import org.matrix.pf.web.helper.TypedHelper;
import org.matrix.pf.web.helper.TypedPage;
import org.matrix.web.WebUtils;
import org.primefaces.event.SelectEvent;
import org.santfeliu.kernel.web.KernelConfigBean;
/**
 *
 * @author lopezrj-sf
 */
@Named("roomSearchBacking")
public class RoomSearchBacking extends SearchBacking implements TypedPage
{
  private static final String ADDRESS_BACKING = "addressBacking";  
  public static final String OUTCOME = "pf_room_search";  
  
  private RoomFilter filter;  
  private SelectItem addressSelectItem; 
  
  private RoomBacking roomBacking;
  private TypedHelper typedHelper;  
  
  public RoomSearchBacking()
  {
    super();
  }
  
  @PostConstruct
  public void init()
  {
    roomBacking = WebUtils.getBacking("roomBacking");
    filter = new RoomFilter();
    smartValue = null;
    typedHelper = new TypedHelper(this);    
  }

  public RoomFilter getFilter()
  {
    return filter;
  }

  public void setFilter(RoomFilter filter)
  {
    this.filter = filter;
  }

  public List<String> getRoomId()
  {
    return filter.getRoomIdList();
  }

  public void setRoomId(List<String> roomIds)
  {
    filter.getRoomIdList().clear();
    if (roomIds != null && !roomIds.isEmpty())
      filter.getRoomIdList().addAll(roomIds);
  }  
  
  public SelectItem getAddressSelectItem() 
  {
    return addressSelectItem;
  }

  public void setAddressSelectItem(SelectItem addressSelectItem) 
  {
    this.addressSelectItem = addressSelectItem;
  }

  public void onAddressSelect(SelectEvent<SelectItem> event) 
  {
    SelectItem item = event.getObject();
    String addressId = (String)item.getValue();
    filter.setAddressId(addressId);
  }

  public void onAddressClear() 
  {
    filter.setAddressId(null);
  }
  
  public List<SelectItem> completeAddress(String query)
  {
    ArrayList<SelectItem> items = new ArrayList();
    AddressBacking addressBacking = WebUtils.getBacking(ADDRESS_BACKING);
    
      //Query search
    if (query != null && query.length() >= 3)
    {
      AddressFilter addressFilter = new AddressFilter();
      addressFilter.setStreetName(query);
      addressFilter.setMaxResults(10);
      List<AddressView> addresses = 
        KernelConfigBean.getPort().findAddressViews(addressFilter);
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

  public void setSelectedAddress(String addressId)
  {
    filter.setAddressId(addressId);
    if (addressSelectItem == null || 
      !addressId.equals(addressSelectItem.getValue()))
    {
      AddressBacking addressBacking = WebUtils.getBacking(ADDRESS_BACKING);
      String description = getDescription(addressBacking, addressId);
      addressSelectItem = new SelectItem(addressId, description);       
    }    
  }  
  
  @Override
  public RoomBacking getObjectBacking()
  {
    return roomBacking;
  }
     
  @Override
  public String search()
  {
    smartValue = convert(filter);
    return super.search();
  }
  
  @Override
  public String smartSearch()
  {
    filter = convert(smartValue);  
    return super.search();
  }

  @Override
  public String clear()
  {
    filter = new RoomFilter();
    addressSelectItem = null;     
    smartValue = null;
    return null;
  }
  
  @Override
  public int countResults()
  {
    try
    {
      return KernelConfigBean.getPort().countRooms(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return 0;
  }

  @Override
  public List getResults(int firstResult, int maxResults)
  {
    try
    {
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      return KernelConfigBean.getPort().findRoomViews(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  } 

  @Override
  public String getObjectId(Object row)
  {
    RoomView roomView = (RoomView)row;
    return roomView.getRoomId();
  }

  @Override
  public String getDescription(Object row)
  {
    RoomView roomView = (RoomView)row;
    return roomView.getDescription();
  }
    
  private RoomFilter convert(String smartValue)
  {
    filter = new RoomFilter();
    addressSelectItem = null;
    if (smartValue != null)
    {
      if (smartValue.contains(";")) //room
      {
        filter.getRoomIdList().add(smartValue);
      }
      else
      {
        try //address
        {
          Integer.parseInt(smartValue);
          setSelectedAddress(smartValue);          
        }
        catch (NumberFormatException ex) //description
        {
          filter.setRoomName(smartValue);
        }        
      }
    }
    return filter;
  }

  private String convert(RoomFilter filter)
  {
    String value;
    if (!filter.getRoomIdList().isEmpty())
      value = filter.getRoomIdList().get(0);
    else 
      value = filter.getRoomName();
    return value;
  }

  @Override
  public String getTypeId()
  {
    return filter.getRoomTypeId() != null ? 
      filter.getRoomTypeId() : 
      getMenuItemTypeId();    
  }

  @Override
  public String getOutcome()
  {
    return OUTCOME;
  }

  @Override
  public String getRootTypeId()
  {
    return roomBacking.getRootTypeId();
  }
  
  @Override
  public String getConfigTypeId() 
  {
    return getMenuItemTypeId();
  }  

  @Override
  public TypedHelper getTypedHelper()
  {
    return typedHelper;
  }

    
}
