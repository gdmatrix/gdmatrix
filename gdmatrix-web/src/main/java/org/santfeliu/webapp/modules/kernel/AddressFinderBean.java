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
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.kernel.AddressFilter;
import org.matrix.kernel.AddressView;
import org.santfeliu.faces.ManualScoped;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.helpers.BigListHelper;
/**
 *
 * @author blanquepa
 */
@Named
@ManualScoped
public class AddressFinderBean extends FinderBean
{
  private String smartFilter;  
  private AddressFilter filter = new AddressFilter();
  private BigListHelper<AddressView> resultListHelper;
  private boolean isSmartFind;  
  
  @Inject
  NavigatorBean navigatorBean;
  
  @Inject
  AddressObjectBean addressObjectBean;
  
  @Inject
  AddressTypeBean addressTypeBean;  
  
  @PostConstruct
  public void init()
  {
    resultListHelper = new AddressBigListHelper();
  }
  
  public String getSmartFilter()
  {
    return smartFilter;
  }

  public void setSmartFilter(String smartFilter)
  {
    this.smartFilter = smartFilter;
  }  
    
  public AddressFilter getFilter()
  {
    return filter;
  }

  public void setFilter(AddressFilter filter)
  {
    this.filter = filter;
  }
    
  public List<String> getFilterAddressId()
  {
    return this.filter.getAddressIdList();
  }
  
  public void setFilterAddressId(List<String> addressIds)
  {
    this.filter.getAddressIdList().clear();
    if (addressIds != null && !addressIds.isEmpty())
      this.filter.getAddressIdList().addAll(addressIds);
  }  
  
  @Override
  public String getObjectId(int position)
  {
    return resultListHelper.getRows() == null ? NEW_OBJECT_ID : 
      resultListHelper.getRow(position).getAddressId();
  }

  @Override
  public int getObjectCount()
  {
    return resultListHelper.getRows() == null ? 0 : 
      resultListHelper.getRowCount();
  }  

  @Override
  public ObjectBean getObjectBean()
  {
    return addressObjectBean;
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
    filter = new AddressFilter();
    smartFilter = null;
    resultListHelper.clear();
    return null;
  }
  
  @Override
  public Serializable saveState()
  {
    return new Object[]{ isSmartFind, smartFilter, filter, 
      resultListHelper.getFirstRowIndex(), getObjectPosition() };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      isSmartFind = (Boolean)stateArray[0];
      smartFilter = (String)stateArray[1];
      filter = (AddressFilter)stateArray[2];
      resultListHelper.setFirstRowIndex((Integer)stateArray[3]); 
      setObjectPosition((Integer)stateArray[4]);

      doFind(false);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }    
  
  private void doFind(boolean autoLoad)
  {
    String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();

    if (isSmartFind)
    {
      filter = addressTypeBean.queryToFilter(smartFilter, baseTypeId);      
      setTabIndex(0);
    }
    else
    {      
      smartFilter = addressTypeBean.filterToQuery(filter);      
      setTabIndex(1);
    }    
    
    resultListHelper.find();
    
    if (autoLoad)
    {  
      if (resultListHelper.getRowCount() == 1)
      {
        AddressView addressView = 
          (AddressView) resultListHelper.getRows().get(0);
        navigatorBean.view(addressView.getAddressId());
        addressObjectBean.setSearchTabIndex(1);
      }
      else
      {
        addressObjectBean.setSearchTabIndex(0);
      }
    }    
  }
  
  private class AddressBigListHelper extends BigListHelper<AddressView>
  {
    @Override
    public int countResults()
    {
      try
      {
        return KernelModuleBean.getPort(false).countAddresses(filter);
      }
      catch (Exception ex)
      {
        error(ex);
      }
      return 0;
    }

    @Override
    public List<AddressView> getResults(int firstResult, int maxResults)
    {
      try
      {
        filter.setFirstResult(firstResult);
        filter.setMaxResults(maxResults);
        return KernelModuleBean.getPort(false).findAddressViews(filter);
      }
      catch (Exception ex)
      {
        error(ex);
      }
      return null;
    }
       
  }

}
