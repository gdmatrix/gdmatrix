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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.kernel.AddressFilter;
import org.matrix.kernel.AddressView;
import org.matrix.kernel.KernelList;
import org.matrix.pf.web.SearchBacking;
import org.matrix.web.WebUtils;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.kernel.web.KernelConfigBean;
/**
 *
 * @author blanquepa
 */
@Named("addressSearchBacking")
public class AddressSearchBacking extends SearchBacking
{
  public static final String OUTCOME = "pf_address_search";  
  
  private AddressFilter filter;
  private List<SelectItem> streetTypeSelectItems;
  
  AddressBacking addressBacking;
  
  public AddressSearchBacking()
  {
    super();
  }
  
  @PostConstruct
  @Override
  public void init()
  {
    addressBacking = WebUtils.getBacking("addressBacking");
    filter = new AddressFilter();
    loadStreetTypeSelectItems();
    smartValue = null;
  }

  public AddressFilter getFilter()
  {
    return filter;
  }

  public void setFilter(AddressFilter filter)
  {
    this.filter = filter;
  }
  
  public List<String> getAddressId()
  {
    return this.filter.getAddressIdList();
  }
  
  public void setAddressId(List<String> addressIds)
  {
    this.filter.getAddressIdList().clear();
    if (addressIds != null && !addressIds.isEmpty())
      this.filter.getAddressIdList().addAll(addressIds);
  }

  @Override
  public AddressBacking getObjectBacking()
  {
    return addressBacking;
  }
     
  @Override
  public String search()
  {
    smartValue = convert(filter);
    super.search();
    return OUTCOME;
  }
  
  @Override
  public String smartSearch()
  {
    filter = convert(smartValue);  
    super.search();
    return OUTCOME;
  }

  @Override
  public String clear()
  {
    filter = new AddressFilter();
    smartValue = null;
    return null;
  }
  
  @Override
  public int countResults()
  {
    try
    {
      return KernelConfigBean.getPort().countAddresses(filter);
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
      return KernelConfigBean.getPort().findAddressViews(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  } 
  
  @Override
  public String getObjectId(Object row)
  {
    AddressView addressView = (AddressView) row;
    return addressView.getAddressId();
  }

  @Override
  public String getDescription(Object row)
  {
    AddressView addressView = (AddressView) row;
    return addressView.getDescription();
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
    String value = null;
    if (!filter.getAddressIdList().isEmpty())
      value = filter.getAddressIdList().get(0);
    else 
    {
      if (!StringUtils.isBlank(filter.getStreetTypeId()))
        value = filter.getStreetTypeId();
      if (!StringUtils.isBlank(filter.getStreetName()))
        value = value + " " + filter.getStreetName();
      if (!StringUtils.isBlank(filter.getNumber()))
        value = value + ", " + filter.getNumber();
    }
    return value;
  }
  
  private void loadStreetTypeSelectItems()
  {
    streetTypeSelectItems = FacesUtils.getListSelectItems(
      KernelConfigBean.getPort().listKernelListItems(
      KernelList.STREET_TYPE),
      "itemId", "description", true);
  }

  @Override
  public String getFilterTypeId()
  {
    return addressBacking.getRootTypeId();
  }

  @Override
  public String getOutcome()
  {
    return OUTCOME;
  }
    
}
