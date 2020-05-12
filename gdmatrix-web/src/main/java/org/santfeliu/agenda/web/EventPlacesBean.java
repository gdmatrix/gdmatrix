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
package org.santfeliu.agenda.web;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;
import org.matrix.agenda.AgendaManagerPort;
import org.matrix.agenda.EventPlace;
import org.matrix.agenda.EventPlaceFilter;
import org.matrix.agenda.EventPlaceView;


import org.matrix.dic.DictionaryConstants;
import org.santfeliu.agenda.Place;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;

import org.santfeliu.kernel.web.AddressBean;
import org.santfeliu.kernel.web.RoomBean;
import org.santfeliu.web.obj.PageBean;
import org.santfeliu.ws.WSExceptionFactory;


/**
 *
 * @author unknown
 */
public class EventPlacesBean extends PageBean
{
  private EventPlace editingEventPlace;
//  private List<EventPlaceView> rows;
  private List<Place> rows;
  
  public EventPlacesBean()
  {
    load();
  }

  public EventPlace getEditingEventPlace()
  {
    return editingEventPlace;
  }

  public void setEditingEventPlace(EventPlace editingEventPlace)
  {
    this.editingEventPlace = editingEventPlace;
  }

  public List<Place> getRows()
  {
    return rows;
  }

  public void setRows(List<Place> rows)
  {
    this.rows = rows;
  }

  public String show()
  {
    return "event_places";
  }

  @Override
  public String store()
  {
    if (editingEventPlace != null)
    {
      storeEventPlace();
    }
    else
    {
      load();
    }
    return show();
  }
  
  public String showPlace()
  {
    Place place = (Place)getValue("#{row}");
    if (place.isRoom())
    {
      return getControllerBean().showObject("Room",
       (String)getValue("#{row.roomView.roomId}"));
    }
    else if (place.isAddress())
    {
      return getControllerBean().showObject("Address",
       (String)getValue("#{row.addressView.addressId}"));
    }
    else
      return null;
  }
  
  public String searchAddress()
  {
    return getControllerBean().searchObject("Address",
      "#{eventPlacesBean.editingEventPlace.addressId}");
  }

  public String searchRoom()
  {
    return getControllerBean().searchObject("Room",
      "#{eventPlacesBean.editingRoomId}");
  }

  public void setEditingRoomId(String roomId)
  {
    if (roomId != null && roomId.length() > 0 && editingEventPlace != null)
    {
      editingEventPlace.setRoomId(roomId);
      String[] pk = roomId.split(";");
      editingEventPlace.setAddressId(pk[0]);
    }
    else
      editingEventPlace.setRoomId(roomId);
  }

  public String getEditingRoomId()
  {
    return editingEventPlace.getRoomId();
  }

  public String removeEventPlace()
  {
    try
    {
      Place row = (Place)getRequestMap().get("row");
      AgendaManagerPort port = AgendaConfigBean.getPort();
      port.removeEventPlace(row.getEventPlaceId());
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String storeEventPlace()
  {
    try
    {
      String eventId = getObjectId();
      editingEventPlace.setEventId(eventId);
      
//      preStore();
      AgendaManagerPort port = AgendaConfigBean.getPort();
      port.storeEventPlace(editingEventPlace);
//      postStore();
      
      editingEventPlace = null;
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String createEventPlace()
  {
    editingEventPlace = new EventPlace();
    return null;
  }
  
  public String editEventPlace()
  {
    try
    {
      Place row = (Place)getExternalContext().
        getRequestMap().get("row");   
      String eventPlaceId = row.getEventPlaceId();
      if (eventPlaceId != null)
        editingEventPlace =
          AgendaConfigBean.getPort().loadEventPlaceFromCache(eventPlaceId);
    }
    catch(Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  public String cancelEventPlace()
  {
    editingEventPlace = null;
    return null;
  }
  
  public List<SelectItem> getAddressSelectItems()
  {
    AddressBean addressBean = (AddressBean)getBean("addressBean");
    return addressBean.getSelectItems(editingEventPlace.getAddressId());
  }

  public List<SelectItem> getRoomSelectItems()
  {
    RoomBean roomBean = (RoomBean)getBean("roomBean");
    return roomBean.getSelectItems(editingEventPlace.getRoomId());
  }
  
  private void load()
  {
    try
    {
      if (!isNew())
      {
        EventPlaceFilter filter = new EventPlaceFilter();
        filter.setEventId(getObjectId());
        rows = new ArrayList();
        List<EventPlaceView> placeViews =
          AgendaConfigBean.getPort().findEventPlaceViewsFromCache(filter);
        for (EventPlaceView placeView : placeViews)
        {
          Place place = new Place(placeView);
          rows.add(place);
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }

  @Override
  protected Type getSelectedType()
  {
    return TypeCache.getInstance().getType(DictionaryConstants.EVENT_PLACE_TYPE);
  }
}
