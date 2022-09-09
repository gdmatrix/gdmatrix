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
import org.matrix.kernel.AddressFilter;
import org.matrix.kernel.AddressView;
import org.matrix.kernel.KernelConstants;
import org.matrix.pf.cms.CMSContent;
import org.matrix.pf.web.ObjectBacking;
import org.matrix.pf.web.SearchBacking;
import org.matrix.web.WebUtils;
import org.santfeliu.kernel.web.KernelConfigBean;
import org.santfeliu.web.bean.CMSProperty;

/**
 *
 * @author blanquepa
 */
@CMSContent(typeId = "Address")
@Named("addressBacking")
public class AddressBacking extends ObjectBacking<AddressView>
{   
  @CMSProperty
  public static final String DEFAULT_CITY_NAME = "defaultCityName";
  
  public AddressBacking()
  {
    super();  
  }
 
  @Override
  public SearchBacking getSearchBacking()
  {
    return WebUtils.getBacking("addressSearchBacking");
  }

  @Override
  public String getObjectId(AddressView address)
  {
    return address.getAddressId();
  }
  
  @Override
  public boolean hasCustomHeader()
  {
    return true;
  }
  
  @Override
  public String getDescription(String objectId)
  {
    objectId = super.getDescription(objectId);
    try
    {
      if (objectId != null && objectId.contains(";"))
        return objectId;
      
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
  
  @Override
  public String getDescription(AddressView address)
  {
    if (address == null) return "";
    StringBuilder buffer = new StringBuilder();
    buffer.append(address.getDescription());
    String city = address.getCity();
    String province = address.getProvince();
    StreetBacking streetBacking = WebUtils.getBacking("streetBacking");
    streetBacking.getDescription(null, null, city, province);
    return buffer.toString();
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
