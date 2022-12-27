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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.kernel.AddressFilter;
import org.matrix.kernel.AddressView;
import org.matrix.kernel.KernelList;
import org.matrix.pf.kernel.AddressBacking;
import org.santfeliu.faces.FacesUtils;
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
  
  private List<SelectItem> streetTypeSelectItems;  
  
  @Inject
  NavigatorBean navigatorBean;
  
  @Inject
  AddressObjectBean addressObjectBean;
  
  @PostConstruct
  public void init()
  {
    resultListHelper = new AddressBigListHelper();
    try
    {
      loadStreetTypeSelectItems();    
    }
    catch (Exception ex)
    {
      error(ex);
    }
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
    filter = convert(smartFilter);
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
    smartFilter = convert(filter);
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
  
  private void loadStreetTypeSelectItems() throws Exception
  {
    streetTypeSelectItems = FacesUtils.getListSelectItems(
      KernelModuleBean.getPort(false).listKernelListItems(
        KernelList.STREET_TYPE), "itemId", "description", true);
  }  
  
  private AddressFilter convert(String smartValue)
  {
    filter = new AddressFilter();
    if (smartValue != null)
    {
      if (smartValue.matches("\\d+"))
        filter.getAddressIdList().add(smartValue);
      else
      {
        String[] parts = smartValue.split("\\s");
        if (parts != null && parts.length > 1)
        {
          //TODO: search in street types.
          List<SelectItem> streetTypes = streetTypeSelectItems.stream()
            .filter(item -> item.getLabel().equalsIgnoreCase(parts[0]))
            .collect(Collectors.toList());

          if (streetTypes != null && !streetTypes.isEmpty())
          {
            filter.setStreetTypeId((String) streetTypes.get(0).getValue());
            smartValue = smartValue.substring(parts[0].length() + 1);
          }
        }
        
        Pattern pattern = 
          Pattern.compile("([[a-zA-Z]|\\s|ç|']+)\\W*(\\d*)\\W*\\w*");
        Matcher matcher = pattern.matcher(smartValue);
        if (matcher.find())
        {
          String streetName = matcher.group(1);  
          if (streetName != null)
            filter.setStreetName(streetName.trim());
          
          String streetNumber = matcher.group(2);
          if (streetNumber != null)
            filter.setNumber(streetNumber.trim());
        }
        
        String defaultCityName = getProperty(AddressBacking.DEFAULT_CITY_NAME);
        if (defaultCityName != null)
          filter.setCityName(defaultCityName);
      }
    }
      
    return filter;
  }
    
  private String convert(AddressFilter filter)
  {
    String value;
    if (!filter.getAddressIdList().isEmpty())
      value = filter.getAddressIdList().get(0);
    else 
    {
      StringBuilder sbValue = new StringBuilder();
      if (!StringUtils.isBlank(filter.getStreetTypeId()))
        sbValue.append(filter.getStreetTypeId());
      if (!StringUtils.isBlank(filter.getStreetName()))
        sbValue.append(" ").append(filter.getStreetName());
      if (!StringUtils.isBlank(filter.getNumber()))
        sbValue.append(", ").append(filter.getNumber());
      value = sbValue.toString().trim();
    }
    return value;
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
