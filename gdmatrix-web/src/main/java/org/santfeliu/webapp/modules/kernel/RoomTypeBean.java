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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.Room;
import org.matrix.kernel.RoomFilter;
import org.matrix.kernel.RoomView;
import org.santfeliu.webapp.TypeBean;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.setup.ObjectSetup;
import static org.santfeliu.webapp.modules.kernel.KernelModuleBean.getPort;

/**
 *
 * @author lopezrj-sf
 */
@Named
@ApplicationScoped
public class RoomTypeBean extends TypeBean<Room, RoomFilter>
{
  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.ROOM_TYPE;
  }

  @Override
  public String getObjectId(Room room)
  {
    return room.getRoomId();
  }

  @Override
  public String describe(Room room)
  {
    try
    {
      RoomFilter filter = new RoomFilter();
      filter.getRoomIdList().add(room.getRoomId());
      List<RoomView> roomViews = getPort(true).findRoomViews(filter);
      if (!roomViews.isEmpty())
      {
        RoomView roomView = roomViews.get(0);
        return roomView.getDescription();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return room.getRoomId();
  }

  @Override
  public Room loadObject(String objectId)
  {
    try
    {
      return getPort(true).loadRoom(objectId);
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  @Override
  public String getTypeId(Room room)
  {
    return room.getRoomTypeId();
  }

  @Override
  public ObjectSetup createObjectSetup()
  {
    ObjectSetup objectSetup = new ObjectSetup();
    objectSetup.setViewId("/pages/kernel/room.xhtml");

    List<EditTab> editTabs = new ArrayList<>();
    editTabs.add(new EditTab("Principal", "/pages/kernel/room_main.xhtml"));
    objectSetup.setEditTabs(editTabs);

    return objectSetup;
  }

  @Override
  public RoomFilter queryToFilter(String query, String typeId)
  {
    if (query == null) query = "";

    RoomFilter filter = new RoomFilter();
    if (checkRoomIdValues(query))
    {
      filter.getRoomIdList().addAll(Arrays.asList(query.split(",")));
    }
    else
    {
      try //address
      {
        Integer.parseInt(query);
        filter.setAddressId(query);
      }
      catch (NumberFormatException ex) //description
      {
        filter.setRoomName(query);
      }
    }
    return filter;
  }

  @Override
  public String filterToQuery(RoomFilter filter)
  {
    String value;

    if (!filter.getRoomIdList().isEmpty())
      value = String.join(",", filter.getRoomIdList());
    else
      value = filter.getRoomName();
    return value;
  }

  @Override
  public List<Room> find(RoomFilter filter)
  {
    try
    {
      return getPort(true).findRooms(filter);
    }
    catch (Exception ex)
    {
      return Collections.EMPTY_LIST;
    }
  }

  private boolean checkRoomIdValues(String s)
  {
    String[] roomSplit = s.split(",");
    for (String roomItem : roomSplit)
    {
      try
      {
        String[] fkSplit = roomItem.split(";");
        if (fkSplit.length == 2)
        {
          Integer.valueOf(fkSplit[0]); //domcod
          Integer.valueOf(fkSplit[1]); //salacod
        }
        else
        {
          return false;
        }
      }
      catch (NumberFormatException ex)
      {
        return false;
      }
    }
    return true;
  }


}
