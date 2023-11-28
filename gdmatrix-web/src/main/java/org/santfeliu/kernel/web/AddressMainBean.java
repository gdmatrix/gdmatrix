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

import org.matrix.kernel.Address;
import org.matrix.kernel.KernelConstants;


import org.santfeliu.web.obj.ControllerBean;
import org.santfeliu.web.obj.TypifiedPageBean;


/**
 *
 * @author unknown
 */
public class AddressMainBean extends TypifiedPageBean
{
  private Address address;
  private transient List<SelectItem> streetSelectItems;

  public AddressMainBean()
  {
    super(DictionaryConstants.ADDRESS_TYPE, KernelConstants.KERNEL_ADMIN_ROLE);
    load();
  }

  public void setAddress(Address address)
  {
    this.address = address;
  }

  public Address getAddress()
  {
    if (address == null) address = new Address();
    return address;
  }

  public String show()
  {
    return "address_main";
  }

  public String store()
  {
    try
    {
      if (ControllerBean.NEW_OBJECT_ID.equals(address.getStreetId()))
      {
        address.setStreetId(null);
      }
      address = KernelConfigBean.getPort().storeAddress(address);
      setObjectId(address.getAddressId());
    }
    catch (Exception ex)
    {
      error(ex);
    }    
    return show();
  }

  public boolean isModified()
  {
    return true;
  }

  public String searchStreet()
  {
    streetSelectItems = null;
    return getControllerBean().searchObject("Street",
      "#{addressMainBean.address.streetId}");
  }

  public String showStreet()
  {
    return getControllerBean().showObject("Street", address.getStreetId());
  }
  
  public List<SelectItem> getStreetSelectItems()
  {
    if (streetSelectItems == null)
    {
      StreetBean streetBean = (StreetBean)getBean("streetBean");
      streetSelectItems = streetBean.getSelectItems(address.getStreetId());
    }
    return streetSelectItems;
  }

  public String showType()
  {
    return getControllerBean().showObject("Type",
      getAddress().getAddressTypeId());
  }

  public boolean isRenderShowTypeButton()
  {
    return getAddress().getAddressTypeId() != null &&
      getAddress().getAddressTypeId().trim().length() > 0;
  }
  
  private void load()
  {
    if (isNew())
    {
      initAddress();
    }
    else
    {
      try
      {
        this.address = KernelConfigBean.getPort().loadAddress(getObjectId());
      } // object was concurrently removed?
      catch (Exception ex)
      {
        getObjectBean().clearObject();
        error(ex); 
        initAddress();
      }
    }
  }
  
  private void initAddress()
  {
    this.address = new Address();
  }
}
