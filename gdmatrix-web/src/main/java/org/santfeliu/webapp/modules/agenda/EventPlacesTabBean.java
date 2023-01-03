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
package org.santfeliu.webapp.modules.agenda;

import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.agenda.EventPlace;
import org.matrix.agenda.EventPlaceFilter;
import org.matrix.agenda.EventPlaceView;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.Address;
import org.matrix.kernel.Room;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.santfeliu.faces.ManualScoped;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.helpers.ReferenceHelper;
import org.santfeliu.webapp.helpers.ResultListHelper;
import org.santfeliu.webapp.modules.kernel.AddressObjectBean;
import org.santfeliu.webapp.modules.kernel.KernelModuleBean;
import org.santfeliu.webapp.modules.kernel.RoomObjectBean;

/**
 *
 * @author lopezrj-sf
 */
@Named
@ManualScoped
public class EventPlacesTabBean extends TabBean
{
  @Inject
  EventObjectBean eventObjectBean;

  @Inject
  AddressObjectBean addressObjectBean;

  @Inject
  RoomObjectBean roomObjectBean;  
  
  //Helpers
  private ResultListHelper<EventPlaceView> resultListHelper;
  ReferenceHelper<Address> addressReferenceHelper;   
  ReferenceHelper<Room> roomReferenceHelper;  
  
  private int firstRow;
  private EventPlace editing;
  
  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);
    resultListHelper = new EventPlaceResultListHelper();
    addressReferenceHelper = 
      new AddressReferenceHelper(DictionaryConstants.ADDRESS_TYPE);
    roomReferenceHelper = 
      new RoomReferenceHelper(DictionaryConstants.ROOM_TYPE);    
  }  
  
  @Override
  public ObjectBean getObjectBean()
  {
    return eventObjectBean;
  }

  public ReferenceHelper<Address> getAddressReferenceHelper() 
  {
    return addressReferenceHelper;
  }
  
  public ReferenceHelper<Room> getRoomReferenceHelper() 
  {
    return roomReferenceHelper;
  }

  public EventPlace getEditing() 
  {
    return editing;
  }

  public void setEditing(EventPlace editing) 
  {
    this.editing = editing;
  }

  public ResultListHelper<EventPlaceView> getResultListHelper()
  {
    return resultListHelper;
  }     
  
  public int getFirstRow()
  {
    return firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    this.firstRow = firstRow;
  }

  public String getPageObjectDescription()
  {
    if (editing != null && !isNew(editing))
    {
      if (editing.getRoomId() != null)
      {
        return roomObjectBean.getDescription(editing.getRoomId());
      }
      else
      {
        return addressObjectBean.getDescription(editing.getAddressId());        
      }
    }
    return null;
  }  
  
  public SelectItem getAddressSelectItem() 
  {
    return addressReferenceHelper.getSelectItem(editing.getAddressId());    
  }

  public void setAddressSelectItem(SelectItem selectItem)
  {
    if (selectItem != null)
      editing.setAddressId((String)selectItem.getValue());
    else
      editing.setAddressId(null);    
  }

  public SelectItem getRoomSelectItem() 
  {
    return roomReferenceHelper.getSelectItem(editing.getRoomId());    
  }

  public void setRoomSelectItem(SelectItem selectItem)
  {
    if (selectItem != null)
      editing.setRoomId((String)selectItem.getValue());
    else
      editing.setRoomId(null);
  }
  
  public void onAddressSelect(SelectEvent<SelectItem> event) 
  {
    SelectItem item = event.getObject();
    String addressId = (String)item.getValue();
    editing.setAddressId(addressId);
    //Reset room
    editing.setRoomId(null);
  }  
  
  public void onAddressClear()
  {
    editing.setAddressId(null);
    //Reset room
    editing.setRoomId(null);
  }
  
  public void setSelectedAddress(String addressId)
  {
    editing.setAddressId(addressId);
    showDialog();
  }  
  
  public void onRoomSelect(SelectEvent<SelectItem> event) 
  {
    try
    {
      SelectItem item = event.getObject();
      String roomId = (String)item.getValue();
      editing.setRoomId(roomId);
      //update address field
      Room room = KernelModuleBean.getPort(false).loadRoom(roomId);
      String addressId = room.getAddressId();
      editing.setAddressId(addressId);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public void onRoomClear()
  {
    editing.setRoomId(null);    
    editing.setAddressId(null);
  }  
    
  public void setSelectedRoom(String roomId)
  {
    try
    {
      editing.setRoomId(roomId);
      //update address field
      Room room = KernelModuleBean.getPort(false).loadRoom(roomId);
      String addressId = room.getAddressId();
      editing.setAddressId(addressId);      
      showDialog();
    }
    catch (Exception ex)
    {
      error(ex);
    }    
  }  

  public String edit(EventPlaceView row)
  {
    String eventPlaceId = null;
    if (row != null)
      eventPlaceId = row.getEventPlaceId();

    return editPlace(eventPlaceId);
  }  
  
  @Override
  public void load()
  {
    resultListHelper.find();
  }
  
  public void create()
  {
    editing = new EventPlace();
  }    

  @Override
  public void store()
  {
    storePlace();
    resultListHelper.find();
  }
  
  public void remove(EventPlaceView row)
  {
    removePlace(row);
    resultListHelper.find();
  }
  
  public String cancel()
  {
    editing = null;
    info("CANCEL_OBJECT");
    return null;
  }  
  
  public void reset()
  {
    cancel();
    resultListHelper.clear();
  }
  
  @Override
  public Serializable saveState()
  {
    return new Object[]{ editing };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      editing = (EventPlace)stateArray[0];
      resultListHelper.find();
    }
    catch (Exception ex)
    {
      error(ex);
    }
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
        editing = 
          AgendaModuleBean.getClient(false).loadEventPlace(eventPlaceId);
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
  
  private void storePlace()
  {
    try
    {
      if (editing != null)
      {
        //Person must be selected        
        if ((editing.getAddressId() == null || editing.getAddressId().isEmpty()) 
          && (editing.getRoomId() == null || editing.getRoomId().isEmpty()))
          throw new Exception("PLACE_MUST_BE_SELECTED");         

        String eventId = eventObjectBean.getObjectId();
        editing.setEventId(eventId);
        AgendaModuleBean.getClient(false).storeEventPlace(editing);
        editing = null;
        info("STORE_OBJECT");
        hideDialog();
      }
    }
    catch (Exception ex)
    {
      error(ex);
      showDialog();
    }
  }  
  
  private String removePlace(EventPlaceView row)
  {
    try
    {
      if (row == null)
        throw new Exception("PLACE_MUST_BE_SELECTED");
      
      String rowEventPlaceId = row.getEventPlaceId();
      
      if (editing != null && 
        rowEventPlaceId.equals(editing.getEventPlaceId()))
        editing = null;
            
      AgendaModuleBean.getClient(false).removeEventPlace(rowEventPlaceId);
      
      info("REMOVE_OBJECT");      
      return null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }    
  
  private void showDialog()
  {
    PrimeFaces current = PrimeFaces.current();
    current.executeScript("PF('placeDataDialog').show();");    
  }  

  private void hideDialog()
  {
    PrimeFaces current = PrimeFaces.current();
    current.executeScript("PF('placeDataDialog').hide();");    
  }  
  
  private boolean isEditing(String pageObjectId)
  {
    if (editing == null)
      return false;
    
    String eventPlaceId = editing.getEventPlaceId();
    return eventPlaceId != null && eventPlaceId.equals(pageObjectId);
  }
  
  private class EventPlaceResultListHelper 
    extends ResultListHelper<EventPlaceView>
  {
    @Override
    public List<EventPlaceView> getResults(int firstResult, int maxResults)
    {
      try
      {
        EventPlaceFilter filter = new EventPlaceFilter();
        filter.setEventId(eventObjectBean.getObjectId());
        filter.setFirstResult(firstResult);
        filter.setMaxResults(maxResults);        
        return AgendaModuleBean.getClient(false).findEventPlaceViews(filter);
      }
      catch (Exception ex)
      {
        error(ex);
      }
      return null;
    }
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
  }  

  private class RoomReferenceHelper extends ReferenceHelper<Room>
  {
    public RoomReferenceHelper(String typeId)
    {
      super(typeId);
    }

    @Override
    public String getId(Room room)
    {
      return room.getRoomId();
    }
  }  
  
}
