package org.santfeliu.agenda;

import java.io.Serializable;
import org.matrix.agenda.EventPlaceView;
import org.matrix.kernel.AddressView;
import org.matrix.kernel.RoomView;

/**
 *
 * @author blanquepa
 */
public class Place implements Serializable
{
  private String eventPlaceId;
  private RoomView roomView;
  private AddressView addressView;
  private String comments;

  public Place(EventPlaceView eventPlaceView)
  {
    eventPlaceId = eventPlaceView.getEventPlaceId();
    this.roomView = eventPlaceView.getRoomView();
    if (roomView == null)
      addressView = eventPlaceView.getAddressView();
    comments = eventPlaceView.getComments();
  }

  public String getEventPlaceId()
  {
    return this.eventPlaceId;
  }

  public String getDescription()
  {
    if (isRoom())
      return roomView.getDescription();
    else if (isAddress())
      return addressView.getDescription();
    else if (comments != null)
      return comments;
    else
      return "";
  }

  public String getPlaceId()
  {
    if (isRoom())
      return roomView.getRoomId();
    else
      return addressView.getAddressId();
  }

  public AddressView getAddressView()
  {
    if (isRoom())
      return roomView.getAddressView();
    else
      return addressView;
  }

  public RoomView getRoomView()
  {
    return roomView;
  }

  public String getComments()
  {
    return comments;
  }

  public boolean isRoom()
  {
    return (roomView != null);
  }

  public boolean isAddress()
  {
    return (addressView != null);
  }

}
