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

import java.util.ArrayList;
import java.util.List;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.kernel.Country;
import org.matrix.kernel.CountryFilter;
import org.matrix.kernel.KernelConstants;
import org.matrix.pf.web.ObjectBacking;
import org.matrix.pf.web.SearchBacking;
import org.matrix.web.WebUtils;
import org.santfeliu.kernel.web.KernelConfigBean;

/**
 *
 * @author blanquepa
 */
@Named("countryBacking")
public class CountryBacking extends ObjectBacking<Country>
{

  @Override
  public String getObjectId(Country obj)
  {
    return obj.getCountryId();
  }

  @Override
  public String getDescription(Country obj)
  {
    return obj.getName();
  }

  @Override
  public String getDescription(String objectId)
  {
    String countryId = super.getDescription(objectId);
    if (!StringUtils.isBlank(countryId))
    {
      Country country = KernelConfigBean.getPort().loadCountry(countryId);
      return getDescription(country);
    }
    return countryId;
  }

  @Override
  public String getAdminRole()
  {
    return KernelConstants.KERNEL_ADMIN_ROLE;
  }

  @Override
  public SearchBacking getSearchBacking()
  {
    return WebUtils.getBacking("countrySearchBacking");
  }

  @Override
  public boolean remove(String objectId)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
  public List<SelectItem> completeCountry(String query)
  {
    List<SelectItem> results = new ArrayList<>();
    
    //Query search
    List<SelectItem> countries = getCountrySelectItems();
    if (countries != null && !countries.isEmpty())
    {
      for (SelectItem item : countries)
      {
        String country = item.getLabel().toUpperCase();
        if (country.contains(query.toUpperCase()))
          results.add(item);
      }
    }

    return results;
  }

  public List<SelectItem> getCountrySelectItems()
  {
    List<SelectItem> countrySelectItems = new ArrayList<>();
    countrySelectItems.add(new SelectItem("", " "));
    try
    {
      CountryFilter filter = new CountryFilter();
      List<Country> countries = 
        KernelConfigBean.getPort().findCountries(filter);
      for (Country country : countries)
      {
        countrySelectItems.add(
          new SelectItem(country.getCountryId(), country.getName()));
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }

    return countrySelectItems;
  }  
  
}
