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
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.KernelConstants;
import org.matrix.kernel.Room;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.Tab;

/**
 *
 * @author lopezrj-sf
 */
@Named
@ViewScoped
public class RoomObjectBean extends ObjectBean
{
  private Room room = new Room();

  @Inject
  RoomTypeBean roomTypeBean;

  @Inject
  RoomFinderBean roomFinderBean;

  @PostConstruct
  public void init()
  {
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
      tabs.add(new Tab("Principal", "/pages/kernel/room_main.xhtml"));
    }
  }

  @Override
  public void storeObject() throws Exception
  {
    room = KernelModuleBean.getPort(false).storeRoom(room);
    setObjectId(room.getRoomId());
    roomFinderBean.outdate();
  }

  public void onAddressClear()
  {
    room.setAddressId(null);
  }

  public String getAdminRole()
  {
    return KernelConstants.KERNEL_ADMIN_ROLE;
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
}
