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
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.agenda.EventPlace;
import org.matrix.agenda.EventPlaceFilter;
import org.matrix.agenda.EventPlaceView;
import org.matrix.kernel.Room;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.modules.kernel.AddressObjectBean;
import org.santfeliu.webapp.modules.kernel.KernelModuleBean;
import org.santfeliu.webapp.modules.kernel.RoomObjectBean;

/**
 *
 * @author lopezrj-sf
 */
@Named
@RequestScoped
public class EventPlacesTabBean extends TabBean
{
  private List<EventPlaceView> rows;

  private int firstRow;
  private EventPlace editing;

  @Inject
  EventObjectBean eventObjectBean;

  @Inject
  AddressObjectBean addressObjectBean;

  @Inject
  RoomObjectBean roomObjectBean;

  @Override
  public ObjectBean getObjectBean()
  {
    return eventObjectBean;
  }

  public EventPlace getEditing()
  {
    return editing;
  }

  public void setEditing(EventPlace editing)
  {
    this.editing = editing;
  }

  public List<EventPlaceView> getRows()
  {
    return rows;
  }

  public void setRows(List<EventPlaceView> rows)
  {
    this.rows = rows;
  }

  public void setAddressId(String addressId)
  {
    editing.setAddressId(addressId);
    editing.setRoomId(null);
  }

  public String getAddressId()
  {
    return editing.getAddressId();
  }

  public void setRoomId(String roomId)
  {
    try
    {
      editing.setRoomId(roomId);
      if (!StringUtils.isBlank(roomId))
      {
        Room room = KernelModuleBean.getPort(false).loadRoom(roomId);
        String addressId = room.getAddressId();
        editing.setAddressId(addressId);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public String getRoomId()
  {
    return editing.getRoomId();
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
      else if (editing.getAddressId() != null)
      {
        return addressObjectBean.getDescription(editing.getAddressId());
      }
      else
      {
        return editing.getComments();
      }
    }
    return null;
  }

  public String edit(EventPlaceView row)
  {
    executeTabAction("preTabEdit", row);
    String eventPlaceId = null;
    if (row != null)
      eventPlaceId = row.getEventPlaceId();
    String result = editPlace(eventPlaceId);
    executeTabAction("postTabEdit", editing);
    return result;
  }

  @Override
  public void load()
  {
    executeTabAction("preTabLoad", null);
    if (!isNew())
    {
      try
      {
        EventPlaceFilter filter = new EventPlaceFilter();
        filter.setEventId(eventObjectBean.getObjectId());
        rows = AgendaModuleBean.getClient(false).findEventPlaceViews(filter);
        executeTabAction("postTabLoad", null);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    else
    {
      rows = Collections.EMPTY_LIST;
      firstRow = 0;
    }
  }

  public void create()
  {
    executeTabAction("preTabEdit", null);
    editing = new EventPlace();
    executeTabAction("postTabEdit", editing);
  }

  @Override
  public void store()
  {
    try
    {
      if (editing != null)
      {
        //Person must be selected
        if ((editing.getAddressId() == null || editing.getAddressId().isEmpty())
          && (editing.getRoomId() == null || editing.getRoomId().isEmpty()))
        {
          throw new Exception("PLACE_MUST_BE_SELECTED");
        }

        String eventId = eventObjectBean.getObjectId();
        editing.setEventId(eventId);
        editing = (EventPlace) executeTabAction("preTabStore", editing);
        AgendaModuleBean.getClient(false).storeEventPlace(editing);
        executeTabAction("postTabStore", editing);
        editing = null;
        load();
        growl("STORE_OBJECT");
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void remove(EventPlaceView row)
  {
    try
    {
      if (row == null)
        throw new Exception("PLACE_MUST_BE_SELECTED");

      String rowEventPlaceId = row.getEventPlaceId();

      if (editing != null &&
        rowEventPlaceId.equals(editing.getEventPlaceId()))
      {
        editing = null;
      }

      row = (EventPlaceView)executeTabAction("preTabRemove", row);
      AgendaModuleBean.getClient(false).removeEventPlace(rowEventPlaceId);
      executeTabAction("postTabRemove", row);

      load();
      growl("REMOVE_OBJECT");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void cancel()
  {
    editing = null;
  }

  @Override
  public boolean isDialogVisible()
  {
    return (editing != null);
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

      if (!isNew()) load();
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
        editing = AgendaModuleBean.getClient(false).
          loadEventPlace(eventPlaceId);
      }
      else if (eventPlaceId == null)
      {
        editing = new EventPlace();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  private boolean isEditing(String pageObjectId)
  {
    if (editing == null)
      return false;

    String eventPlaceId = editing.getEventPlaceId();
    return eventPlaceId != null && eventPlaceId.equals(pageObjectId);
  }

}
