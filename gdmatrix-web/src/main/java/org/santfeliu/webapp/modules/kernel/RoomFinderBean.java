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
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.Address;
import org.matrix.kernel.RoomFilter;
import org.matrix.kernel.RoomView;
import org.primefaces.event.SelectEvent;
import org.santfeliu.dic.web.TypeBean;
import org.santfeliu.faces.ManualScoped;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.helpers.BigListHelper;
import org.santfeliu.webapp.helpers.ReferenceHelper;
import org.santfeliu.webapp.util.WebUtils;
/**
 *
 * @author lopezrj-sf
 */
@Named
@ManualScoped
public class RoomFinderBean extends FinderBean
{
  private String smartFilter;  
  private RoomFilter filter = new RoomFilter();
  private int firstRow;
  private BigListHelper<RoomView> resultListHelper;
  private boolean isSmartFind;  
  
  ReferenceHelper<Address> addressReferenceHelper; 
  
  @Inject
  NavigatorBean navigatorBean;
  
  @Inject
  RoomTypeBean roomTypeBean;

  @Inject
  RoomObjectBean roomObjectBean;

  @PostConstruct
  public void init()
  {
    resultListHelper = new RoomBigListHelper();
    addressReferenceHelper = 
      new AddressReferenceHelper(DictionaryConstants.ADDRESS_TYPE);
  }
  
  @Override
  public ObjectBean getObjectBean()
  {
    return roomObjectBean;
  }  

  public ReferenceHelper<Address> getAddressReferenceHelper() 
  {
    return addressReferenceHelper;
  }
  
  public String getSmartFilter()
  {
    return smartFilter;
  }

  public void setSmartFilter(String smartFilter)
  {
    this.smartFilter = smartFilter;
  }  
    
  public RoomFilter getFilter()
  {
    return filter;
  }

  public void setFilter(RoomFilter filter)
  {
    this.filter = filter;
  }

  public int getFirstRow()
  {
    return firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    this.firstRow = firstRow;
  }

  @Override
  public String getObjectId(int position)
  {    
    RoomView roomView = (RoomView)resultListHelper.getRow(position);
    return roomView == null ? NEW_OBJECT_ID : roomView.getRoomId();
  }

  @Override
  public int getObjectCount()
  {
    return resultListHelper.getRowCount();
  }  
  
  public List<String> getRoomId()
  {
    return filter.getRoomIdList();
  }

  public void setRoomId(List<String> roomIds)
  {
    filter.getRoomIdList().clear();
    if (roomIds != null && !roomIds.isEmpty())
      filter.getRoomIdList().addAll(roomIds);
  }  
  
  public SelectItem getAddressSelectItem() 
  {
    return addressReferenceHelper.getSelectItem(filter.getAddressId());
  }

  public void setAddressSelectItem(SelectItem selectItem) 
  {
    if (selectItem != null)
      filter.setAddressId((String)selectItem.getValue());
    else
      filter.setAddressId(null);
  }

  public void onAddressSelect(SelectEvent<SelectItem> event) 
  {
    SelectItem item = event.getObject();
    String addressId = (String)item.getValue();
    filter.setAddressId(addressId);
  }

  public void onAddressClear() 
  {
    filter.setAddressId(null);
  }

  public BigListHelper getResultListHelper()
  {
    return resultListHelper;
  }
     
  @Override
  public void smartFind()
  {
    isSmartFind = true;    
    doFind(true);
  }
  
  public void smartClear()
  {
    smartFilter = null;
    resultListHelper.clear();
  }

  @Override
  public void find()
  {
    isSmartFind = false;    
    doFind(true);
  }

  public String clear()
  {
    filter = new RoomFilter();
    smartFilter = null;
    resultListHelper.clear();
    return null;
  }
  
  public String getRoomTypeDescription(RoomView roomView)
  {
    TypeBean typeBean = WebUtils.getBean("typeBean");
    return ((roomView == null || roomView.getRoomTypeId() == null) ? "" : 
      typeBean.getDescription(roomView.getRoomTypeId()));
  }
  
  @Override
  public Serializable saveState()
  {
    return new Object[]{ isSmartFind, filter, firstRow, getObjectPosition() };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      isSmartFind = (Boolean)stateArray[0];
      filter = (RoomFilter)stateArray[1];

      doFind(false);

      firstRow = (Integer)stateArray[2];
      setObjectPosition((Integer)stateArray[3]);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }    
  
  private void doFind(boolean autoLoad)
  {
    firstRow = 0;
    String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();

    if (isSmartFind)
    {
      filter = roomTypeBean.queryToFilter(smartFilter, baseTypeId);
      setTabIndex(0);
    }
    else
    {
      smartFilter = roomTypeBean.filterToQuery(filter);
      setTabIndex(1);
    }
    
    resultListHelper.find();
    
    if (autoLoad)
    {  
      if (resultListHelper.getRowCount() == 1)
      {
        RoomView roomView = (RoomView)resultListHelper.getRows().get(0);
        navigatorBean.view(roomView.getRoomId());
        roomObjectBean.setSearchTabIndex(1);
      }
      else
      {
        roomObjectBean.setSearchTabIndex(0);
      }
    }    
  }
    
  private class RoomBigListHelper extends BigListHelper<RoomView>
  {
    @Override
    public int countResults()
    {
      try
      {
        return KernelModuleBean.getPort(false).countRooms(filter);
      }
      catch (Exception ex)
      {
        error(ex);
      }
      return 0;
    }

    @Override
    public List<RoomView> getResults(int firstResult, int maxResults)
    {
      try
      {
        filter.setFirstResult(firstResult);
        filter.setMaxResults(maxResults);
        return KernelModuleBean.getPort(false).findRoomViews(filter);
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

}
