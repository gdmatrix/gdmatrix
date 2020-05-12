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
