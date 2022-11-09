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
package org.matrix.pf.agenda;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import org.matrix.agenda.AgendaManagerPort;
import org.matrix.agenda.EventPlace;
import org.matrix.agenda.EventPlaceFilter;
import org.matrix.agenda.EventPlaceView;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.AddressFilter;
import org.matrix.kernel.AddressView;
import org.matrix.kernel.Room;
import org.matrix.kernel.RoomFilter;
import org.matrix.kernel.RoomView;
import org.matrix.pf.kernel.AddressBacking;
import org.matrix.pf.kernel.RoomBacking;
import org.matrix.pf.web.PageBacking;
import org.matrix.pf.web.helper.ResultListHelper;
import org.matrix.pf.web.helper.ResultListPage;
import org.matrix.pf.web.helper.TabHelper;
import org.matrix.pf.web.helper.TypedHelper;
import org.matrix.pf.web.helper.TypedTabPage;
import org.matrix.web.WebUtils;
import org.primefaces.event.SelectEvent;
import org.santfeliu.agenda.Place;
import org.santfeliu.agenda.web.AgendaConfigBean;
import org.santfeliu.kernel.web.KernelConfigBean;

/**
 *
 * @author lopezrj-sf
 */
@Named
public class EventPlacesBacking extends PageBacking 
  implements TypedTabPage, ResultListPage
{
  private static final String EVENT_BACKING = "eventBacking";
  private static final String ADDRESS_BACKING = "addressBacking";
  private static final String ROOM_BACKING = "roomBacking";
    
  private static final String OUTCOME = "pf_event_places";  
  
  private EventBacking eventBacking;
  
  //Helpers
  private TypedHelper typedHelper;
  private ResultListHelper<Place> resultListHelper;
  private TabHelper tabHelper;
  
  private EventPlace editing;
  
  private SelectItem addressSelectItem;
  private SelectItem roomSelectItem;
  
  public EventPlacesBacking()
  {
  }
  
  @PostConstruct
  public void init()
  {
    eventBacking = WebUtils.getBacking(EVENT_BACKING);   
    typedHelper = new TypedHelper(this);
    resultListHelper = new ResultListHelper(this);
    tabHelper = new TabHelper(this);
    populate();
  }
  
  public EventPlace getEditing()
  {
    return editing;
  }

  public void setEditing(EventPlace editing)
  {
    this.editing = editing;
  } 
  
  public boolean isNew()
  {
    return isNew(editing);
  }
  
  @Override
  public String getPageObjectId()
  {
    if (editing != null)
      return editing.getEventPlaceId();
    else
      return null;
  }

  public String getPageObjectDescription()
  {
    if (editing != null)
    {
      if (editing.getRoomId() != null)
      {
        RoomBacking roomBacking = WebUtils.getBacking(ROOM_BACKING);
        return roomBacking.getDescription(editing.getRoomId());
      }
      else
      {
        AddressBacking addressBacking = WebUtils.getBacking(ADDRESS_BACKING);
        return addressBacking.getDescription(editing.getAddressId());        
      }
    }
    return null;
  }
  
  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.EVENT_PLACE_TYPE;
  }

  @Override
  public EventBacking getObjectBacking()
  {
    return eventBacking;
  }
  
  @Override
  public String getTypeId()
  {
    return eventBacking.getTabTypeId();
  }
  
  @Override
  public ResultListHelper<Place> getResultListHelper()
  {
    return resultListHelper;
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
  
  public List<Place> getRows()
  {
    return resultListHelper.getRows();
  }

  //Address selection
  public SelectItem getAddressSelectItem()
  {
    return addressSelectItem;
  }
  
  public void setAddressSelectItem(SelectItem item)
  {
    addressSelectItem = item;
  }
  
  public void onAddressSelect(SelectEvent<SelectItem> event) 
  {
    SelectItem item = event.getObject();
    String addressId = (String)item.getValue();
    editing.setAddressId(addressId);
  }  
  
  public void onAddressClear()
  {
    editing.setAddressId(null);    
    editing.setRoomId(null);
    roomSelectItem = null;
  }  
  
  public List<SelectItem> completeAddress(String query)
  {
    return completeAddress(query, editing.getAddressId());
  }  

  //Room selection
  public SelectItem getRoomSelectItem()
  {
    return roomSelectItem;
  }
  
  public void setRoomSelectItem(SelectItem item)
  {
    roomSelectItem = item;
  }
  
  public void onRoomSelect(SelectEvent<SelectItem> event) 
  {
    SelectItem item = event.getObject();
    String roomId = (String)item.getValue();
    editing.setRoomId(roomId);    
    //update address field
    Room room = KernelConfigBean.getPort().loadRoom(roomId);
    String addressId = room.getAddressId();
    setSelectedAddress(addressId);    
  }
  
  public void onRoomClear()
  {
    editing.setRoomId(null);    
    editing.setAddressId(null);
    addressSelectItem = null;
  }  
  
  public List<SelectItem> completeRoom(String query)
  {
    return completeRoom(query, editing.getRoomId());
  }    
  
  public List<SelectItem> getFavorites()
  {
    AddressBacking addressBacking = WebUtils.getBacking(ADDRESS_BACKING);
    return addressBacking.getFavorites();     
  }      
  
  private List<SelectItem> completeAddress(String query, String addressId)
  {
    ArrayList<SelectItem> items = new ArrayList();
    AddressBacking addressBacking = WebUtils.getBacking(ADDRESS_BACKING);
    
    //Add current item
    if (!isNew(editing))
    {
      String description = "";
      if (addressId != null)
        description = addressBacking.getDescription(addressId);
      items.add(new SelectItem(addressId, description));
    }
        
    //Query search
    if (query != null && query.length() >= 3)
    {
      AddressFilter filter = new AddressFilter();
      filter.setDescription(query);
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

  private List<SelectItem> completeRoom(String query, String roomId)
  {
    ArrayList<SelectItem> items = new ArrayList();
    RoomBacking roomBacking = WebUtils.getBacking(ROOM_BACKING);
    
    //Add current item
    if (!isNew(editing))
    {
      String description = "";
      if (roomId != null)
        description = roomBacking.getDescription(roomId);
      items.add(new SelectItem(roomId, description));
    }
        
    //Query search
    if (query != null && query.length() >= 3)
    {
      RoomFilter filter = new RoomFilter();
      filter.setRoomName(query);
      filter.setMaxResults(10);
      List<RoomView> rooms = 
        KernelConfigBean.getPort().findRoomViews(filter);
      if (rooms != null)
      {       
        for (RoomView room : rooms)
        {
          String description = roomBacking.getDescription(room);
          SelectItem item = new SelectItem(room.getRoomId(), description);
          items.add(item);
        }
      }
    }
    else
    {
      //Add favorites
      items.addAll(roomBacking.getFavorites()); 
    }    
    return items;
  }  
  
  public void setSelectedAddress(String addressId)
  {
    editing.setAddressId(addressId);
    if (addressSelectItem == null || 
      !addressId.equals(addressSelectItem.getValue()))
    {
      AddressBacking addressBacking = WebUtils.getBacking(ADDRESS_BACKING);    
      String description = addressBacking.getDescription(addressId);
      addressSelectItem = new SelectItem(addressId, description);       
    }
    showDialog();
  }

  public void setSelectedRoom(String roomId)
  {
    editing.setRoomId(roomId);
    if (roomSelectItem == null || 
      !roomId.equals(roomSelectItem.getValue()))
    {
      RoomBacking roomBacking = WebUtils.getBacking(ROOM_BACKING);    
      String description = roomBacking.getDescription(roomId);
      roomSelectItem = new SelectItem(roomId, description);
      //update address field
      Room room = KernelConfigBean.getPort().loadRoom(roomId);
      String addressId = room.getAddressId();
      setSelectedAddress(addressId);    
    }
    showDialog();
  }  
  
  @Override
  public String show(String pageObjectId)
  {
    editPlace(pageObjectId);
    showDialog();
    return isEditing(pageObjectId) ? OUTCOME : show();
  }  
  
  @Override
  public String show()
  {
    populate();
    return OUTCOME;
  }
  
  public String editPlace(Place row)
  {
    String eventPlaceId = null;
    if (row != null)
      eventPlaceId = row.getEventPlaceId();
    return editPlace(eventPlaceId);
  } 
  
  public String createPlace()
  {
    editing = new EventPlace();
    return null;
  }  
  
  public String removePlace(Place row)
  {
    try
    {
      if (row == null)
        throw new Exception("PLACE_MUST_BE_SELECTED");
      
      String rowEventPlaceId = row.getEventPlaceId();
      
      if (editing != null && rowEventPlaceId.equals(editing.getEventPlaceId()))
        editing = null;
      
      AgendaManagerPort port = AgendaConfigBean.getPort();
      port.removeEventPlace(rowEventPlaceId);
      
      return show();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }  

  public String storePlace()
  {
    try
    {
      if (editing == null)
        return null;
      
      //Address must be selected
      if ((editing.getAddressId() == null || editing.getAddressId().isEmpty()) 
        && (editing.getRoomId() == null || editing.getRoomId().isEmpty()))
        throw new Exception("PLACE_MUST_BE_SELECTED"); 
                            
      String eventId = eventBacking.getObjectId();
      editing.setEventId(eventId);
                        
      AgendaManagerPort port = AgendaConfigBean.getPort();
      port.storeEventPlace(editing);
      
      cancel();
      hideDialog();
      return show();
    }
    catch (Exception ex)
    {
      error(ex);
      showDialog();
    }
    return null;
  }

  @Override
  public List<Place> getResults(int firstResult, int maxResults)
  {
    try
    {
      List<Place> result = new ArrayList();
      EventPlaceFilter filter = new EventPlaceFilter();
      filter.setEventId(eventBacking.getObjectId());        
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      List<EventPlaceView> auxList = 
        AgendaConfigBean.getPort().findEventPlaceViewsFromCache(filter);
      for (EventPlaceView eventPlaceView : auxList)
      {
        result.add(new Place(eventPlaceView));
      }
      return result;
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  @Override
  public String store()
  {
    return storePlace();
  }
  
  @Override
  public void load()
  {
    resultListHelper.search();
  }

  @Override
  public void create()
  {
    editing = new EventPlace();
  }
  
  @Override
  public String cancel()
  {
    editing = null;
    addressSelectItem = null;
    roomSelectItem = null;
    return null;
  }  
  
  @Override
  public void reset()
  {
    cancel();
    resultListHelper.reset();
  }
    
  private boolean isNew(EventPlace eventPlace)
  {
    return (eventPlace != null && eventPlace.getEventPlaceId() == null);
  }  
    
  private String editPlace(String eventPlaceId)
  {
    try
    {
      if (eventPlaceId != null && !isEditing(eventPlaceId))
      {
        editing = AgendaConfigBean.getPort().
          loadEventPlaceFromCache(eventPlaceId);
        loadAddressSelectItem();
        loadRoomSelectItem();
      }
      else if (eventPlaceId == null)
      {
        editing = new EventPlace();
      }
    }
    catch(Exception ex)
    {
      error(ex);
    }
    return null;
  }  

  private void loadAddressSelectItem()
  {
    if (editing != null)
    {
      AddressBacking addressBacking = WebUtils.getBacking(ADDRESS_BACKING);
      
      if (editing.getAddressId() != null)
      {
        String description = 
          addressBacking.getDescription(editing.getAddressId());
        addressSelectItem = 
          new SelectItem(editing.getAddressId(), description);
      }
    }
  }  

  private void loadRoomSelectItem()
  {
    if (editing != null)
    {
      RoomBacking roomBacking = WebUtils.getBacking(ROOM_BACKING);
      
      if (editing.getRoomId() != null)
      {
        String description = 
          roomBacking.getDescription(editing.getRoomId());
        roomSelectItem = 
          new SelectItem(editing.getRoomId(), description);
      }
    }
  }  

}
