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
import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import org.matrix.kernel.Address;
import org.matrix.pf.web.PageBacking;
import org.matrix.pf.web.helper.TypedHelper;
import org.matrix.pf.web.helper.TypedTabPage;
import org.matrix.web.WebUtils;
import org.santfeliu.kernel.web.KernelConfigBean;

/**
 *
 * @author blanquepa
 */
@Named("addressMainBacking")
public class AddressMainBacking extends PageBacking 
  implements TypedTabPage
{
  public static final String OUTCOME = "pf_address_main";
    
  private Address address;
  private SelectItem streetSelectItem;
  
  //Helpers
  private TypedHelper typedHelper;
  
  //ObjectBacking
  AddressBacking addressBacking;
  
  public AddressMainBacking()
  {    
  }
  
  @PostConstruct
  public void init()
  {
    addressBacking = WebUtils.getBacking("addressBacking");
    typedHelper = new TypedHelper(this); 
    populate();
  }

  @Override
  public AddressBacking getObjectBacking()
  {
    return addressBacking;
  }

  @Override
  public String getRootTypeId()
  {
    return addressBacking.getRootTypeId();
  }

  @Override
  public String getTypeId()
  {
    return getMenuItemTypeId();
  }

  @Override
  public TypedHelper getTypedHelper()
  {
    return typedHelper;
  }

  public Address getAddress()
  {
    return address;
  }

  public void setAddress(Address address)
  {
    this.address = address;
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
      loadStreetSelectItem(streetId);      
    }
  }

  public void setStreetSelectItem(SelectItem streetSelectItem)
  {
    this.streetSelectItem = streetSelectItem;
    if (address != null && streetSelectItem != null)
      this.address.setStreetId((String) streetSelectItem.getValue());
  }
         
  @Override
  public String show(String pageId)
  {
    addressBacking.setObjectId(pageId);
    return show();
  }  
  
  @Override
  public String show()
  {
    populate();
    return OUTCOME;
  }
  
  @Override
  public String store()
  {
    try
    {
      KernelConfigBean.getPort().storeAddress(address);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return show();
  }
  
  public String remove()
  {
    error("Not implemented yet");
    return null;
  }
    
  @Override
  public String getPageObjectId()
  {
    return addressBacking.getObjectId();
  }

  @Override
  public void create()
  {
    address = new Address();
  }
          
  @Override
  public void load()
  {
    String addressId = getPageObjectId();
    if (addressId != null)
    {
      address = KernelConfigBean.getPort().loadAddress(addressId);
      loadStreetSelectItem(address.getStreetId());
    }
  }

  @Override
  public void reset()
  {
    if (addressBacking.isNew())
      create();
    else
      address = null;
    streetSelectItem = null;
  }
  
  @Override
  public String cancel()
  {
    reset();
    return null;
  }  
  
  public List<SelectItem> completeStreet(String query)
  {
    StreetBacking streetBacking = WebUtils.getBacking("streetBacking");
    return streetBacking.completeStreet(query, address.getStreetId());
  }
  
  private void loadStreetSelectItem(String streetId)
  {
    StreetBacking streetBacking = WebUtils.getBacking("streetBacking");

    if (address != null)
    {
      String description = getDescription(streetBacking, streetId);
      streetSelectItem = 
        new SelectItem(streetId, description);
    }    
  }
    
}
