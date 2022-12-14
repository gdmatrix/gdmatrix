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
import java.util.List;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.Address;
import org.matrix.kernel.AddressFilter;
import org.matrix.kernel.AddressView;
import org.santfeliu.faces.ManualScoped;
import org.santfeliu.kernel.web.KernelConfigBean;

import org.santfeliu.webapp.FinderBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.Tab;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author blanquepa
 */
@Named
@ManualScoped
public class AddressObjectBean extends ObjectBean
{    
  private Address address = new Address();
  private SelectItem streetSelectItem;
  
  @Inject
  AddressFinderBean addressFinderBean;
  
  public AddressObjectBean()
  {
  }

  public Address getAddress()
  {
    return address;
  }

  public void setAddress(Address address)
  {
    this.address = address;
  }
  
  //TODO: AddressDescriptor
  public String getDescription(String objectId)
  {
    try
    {      
      AddressFilter filter = new AddressFilter();
      filter.getAddressIdList().add(objectId);      
      List<AddressView> addressViews = 
        KernelConfigBean.getPortAsAdmin().findAddressViews(filter);
      if (!addressViews.isEmpty())
      {
        AddressView addressView = addressViews.get(0);
        return getDescription(addressView);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return objectId;
  }    
  
  public String getDescription(AddressView address)
  {
    if (address == null) return "";
    return address.getDescription();
  }   

  @Override
  public String show()
  {
    return "/pages/kernel/address.xhtml";
  }

  @Override
  public FinderBean getFinderBean()
  {
    return addressFinderBean;
  }

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.ADDRESS_TYPE;
  }
  
  @Override
  public void loadObject() throws Exception
  {
    if (!NEW_OBJECT_ID.equals(objectId))
    {
      try
      {
        address = KernelConfigBean.getPort().loadAddress(objectId);
        String streetId = address.getStreetId();
        streetSelectItem = createStreetSelectItem(streetId);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    else 
    {
      address = new Address();
      streetSelectItem = null;
    }       
  }

  @Override
  public void loadTabs()
  {
    super.loadTabs();

    if (tabs.isEmpty())
    {
      tabs = new ArrayList<>(); // empty list may be read only
      tabs.add(new Tab("Main", "/pages/kernel/address_main.xhtml"));
    }    
  } 
  
  public SelectItem getStreetSelectItem()
  {
    return streetSelectItem;
  }
  
  public void selectStreet(String streetId)
  {
    if (address != null)
    {
      address.setStreetId(streetId);  
      createStreetSelectItem(streetId);      
    }
  }

  public void setStreetSelectItem(SelectItem streetSelectItem)
  {
    this.streetSelectItem = streetSelectItem;
    if (address != null && streetSelectItem != null)
      this.address.setStreetId((String) streetSelectItem.getValue());
  }

  private SelectItem createStreetSelectItem(String streetId)
  {
    String description = streetId;
      
    TerritoryObjectBean territoryObjectBean = 
      WebUtils.getBean("territoryObjectBean");
    if (address != null)
      description = territoryObjectBean.getDescription(streetId); 

    return new SelectItem(streetId, description);    
  }
  
  public List<SelectItem> completeStreet(String query)
  {
    TerritoryObjectBean territoryObjectBean = 
      WebUtils.getBean("territoryObjectBean");
    return territoryObjectBean.completeStreet(query, address.getStreetId());
  }

  public void onStreetClear() 
  {
    address.setStreetId(null);
  }    
  
  @Override
  public Serializable saveState()
  {
    return address;
  }

  @Override
  public void restoreState(Serializable state)
  {
    this.address = (Address)state;
  }    
     
}
