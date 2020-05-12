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
package org.santfeliu.kernel.web;

import java.util.List;
import javax.faces.model.SelectItem;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.TypeFilter;
import org.matrix.kernel.KernelConstants;

import org.matrix.kernel.RoomFilter;
import org.matrix.kernel.RoomView;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.web.TypeBean;
import org.santfeliu.dic.web.TypeSearchBean;
import org.santfeliu.web.obj.BasicSearchBean;


/**
 *
 * @author unknown
 */
public class RoomSearchBean extends BasicSearchBean
{
  private List<SelectItem> typeSelectItems;
  private RoomFilter filter;
  private Integer capacity;
  private String addressId;

  public RoomSearchBean()
  {
    typeSelectItems = null;
    filter = new RoomFilter();
  }
  
  public void setFilter(RoomFilter filter)
  {
    this.filter = filter;
  }

  public RoomFilter getFilter()
  {
    return filter;
  }

  public Integer getCapacity()
  {
    return capacity;
  }

  public void setCapacity(Integer capacity)
  {
    this.capacity = capacity;
  }

  public void setRoomId(String addressId)
  {
    this.addressId = addressId;
  }

  public String getRoomId()
  {
    return addressId;
  }

  public int countResults()
  {
    try
    {
      filter.setCapacity(capacity != null ? capacity.intValue() : 0);
      return KernelConfigBean.getPort().countRooms(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return 0;
  }

  public List getResults(int firstResult, int maxResults)
  {
    try
    {
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      filter.setCapacity(capacity != null ? capacity.intValue() : 0);
      return KernelConfigBean.getPort().findRoomViews(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String showRoom()
  {
    return getControllerBean().showObject("Room",
      (String)getValue("#{row.roomId}"));
  }

  public String selectRoom()
  {
    return getControllerBean().select((String)getValue("#{row.roomId}"));
  }

  public String show()
  {
    return "room_search";
  }

  public String searchType()
  {
    TypeSearchBean typeSearchBean = (TypeSearchBean)getBean("typeSearchBean");
    if (typeSearchBean == null)
      typeSearchBean = new TypeSearchBean();

    typeSearchBean.setRootTypeId(DictionaryConstants.ROOM_TYPE);
    typeSearchBean.setFilter(new TypeFilter());
    typeSearchBean.search();

    return getControllerBean().searchObject("Type",
      "#{roomSearchBean.filter.roomTypeId}");
  }

  public List<SelectItem> getTypeSelectItems()
  {
    try
    {
      TypeBean typeBean = (TypeBean)getBean("typeBean");
      String[] actions = {DictionaryConstants.READ_ACTION};
      typeSelectItems = typeBean.getAllSelectItems(DictionaryConstants.ROOM_TYPE,
        KernelConstants.KERNEL_ADMIN_ROLE, actions, true);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return typeSelectItems;
  }

  public String getTypeDescription()
  {
    String description = null;
    RoomView row = (RoomView)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    if (row != null)
    {
      String typeId = row.getRoomTypeId();
      if (typeId != null)
      {
        Type type = TypeCache.getInstance().getType(typeId);
        description = type != null && type.getDescription() != null ?
          type.getDescription() : typeId;
      }
    }
    return description;
  }

}
