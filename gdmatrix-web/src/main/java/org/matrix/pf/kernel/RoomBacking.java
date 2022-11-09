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
package org.matrix.pf.kernel;

import java.util.List;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import org.matrix.kernel.KernelConstants;
import org.matrix.kernel.RoomFilter;
import org.matrix.kernel.RoomView;
import org.matrix.pf.cms.CMSContent;
import org.matrix.pf.web.ObjectBacking;
import org.matrix.pf.web.SearchBacking;
import org.matrix.web.WebUtils;
import org.santfeliu.kernel.web.KernelConfigBean;
import org.santfeliu.web.bean.CMSProperty;

/**
 *
 * @author lopezrj-sf
 */
@CMSContent(typeId = "Room")
@Named("roomBacking")
public class RoomBacking extends ObjectBacking<RoomView>
{
  @CMSProperty
  public static final String DEFAULT_CITY_NAME = "defaultCityName";
  
  public RoomBacking()
  {
    super();  
  }
 
  @Override
  public SearchBacking getSearchBacking()
  {
    return WebUtils.getBacking("roomSearchBacking");
  }

  @Override
  public String getObjectId(RoomView room)
  {
    return room.getRoomId();
  }
  
  @Override
  public boolean hasCustomHeader()
  {
    return true;
  }
  
  @Override
  public String getDescription(String objectId)
  {
    try
    {      
      RoomFilter filter = new RoomFilter();
      filter.getRoomIdList().add(objectId);      
      List<RoomView> roomViews = 
        KernelConfigBean.getPortAsAdmin().findRoomViews(filter);
      if (!roomViews.isEmpty())
      {
        RoomView roomView = roomViews.get(0);
        return getDescription(roomView);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return objectId;
  }    
  
  @Override
  public String getDescription(RoomView room)
  {
    if (room == null) 
      return "";
    else 
      return room.getDescription();
  }
  
  @Override
  public List<SelectItem> getFavorites()
  {
    return getFavorites(getRootTypeId());
  }  

  @Override
  public String show()
  {
    return super.show();
  }

  @Override
  public String getAdminRole()
  {
    return KernelConstants.KERNEL_ADMIN_ROLE;
  }
  
  @Override
  public boolean remove(String objectId)
  {
    throw new UnsupportedOperationException("Not supported yet."); 
  }
     
}
