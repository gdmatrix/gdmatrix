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
      List<String> details = WSExceptionFactory.getDetails(ex);
      if (details.size() > 0) error(details);
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
