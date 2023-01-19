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
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.Country;
import org.santfeliu.faces.ManualScoped;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.NavigatorBean;

/**
 *
 * @author blanquepa
 */
@Named
@ManualScoped
public class CountryObjectBean extends TerritoryObjectBean
{
  private Country country = new Country();
  
  @Inject
  CountryFinderBean countryFinderBean;
  
  @Inject
  ProvinceObjectBean provinceObjectBean;
  
  @Inject
  CountryTypeBean countryTypeBean;
  
  @Inject
  NavigatorBean navigatorBean;
  
  private List<SelectItem> countrySelectItems;

  public Country getCountry()
  {
    return country;
  }

  public void setCountry(Country country)
  {
    this.country = country;
  }
  
  @Override
  public FinderBean getFinderBean()
  {
    return countryFinderBean;
  }

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.COUNTRY_TYPE;
  }

  @Override
  public String show()
  {
    return "/pages/kernel/country.xhtml";
  }
  
  @Override
  public Country getObject()
  {
    return isNew() ? null : country;
  }
  
  @Override
  public CountryTypeBean getTypeBean()
  {
    return countryTypeBean;
  }   
  
  @Override
  public String getDescription()
  {
    return isNew() ? "" : getDescription(country.getCountryId());
  }  
  
  public String getDescription(String countryId)
  {
    return getTypeBean().getDescription(countryId);
  }  
  
  @Override
  public void createObject()
  {
    country = new Country();
    setObjectId(NavigatorBean.NEW_OBJECT_ID);
  }
  
  @Override
  public void loadObject() throws Exception
  {
    if (objectId != null && !isNew())
    {
      country = KernelModuleBean.getPort(false).loadCountry(objectId);
    }
    else 
    {
      createObject();
    }       
  }  

  @Override
  public void storeObject() throws Exception
  {
    country = KernelModuleBean.getPort(false).storeCountry(country);
    setObjectId(country.getCountryId());
    countrySelectItems = null;
    countryFinderBean.outdate();    
  }
  
  @Override
  public void removeObject() throws Exception
  {
    KernelModuleBean.getPort(false).removeCountry(objectId);
    countryFinderBean.doFind(false);    
    navigatorBean.view("");
  }  

  /*
  public List<SelectItem> getCountrySelectItems()
  {
    if (countrySelectItems == null)
    {
      countrySelectItems = new ArrayList<>();
      try
      {
        CountryFilter filter = new CountryFilter();
        List<Country> countries = 
          KernelModuleBean.getPort(false).findCountries(filter);
        
        for (Country c : countries)
        {
          SelectItem item = 
            new SelectItem(c.getCountryId(), c.getName());
          countrySelectItems.add(item);
        }        
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    
    return countrySelectItems;
  }   
  */
  public void onCountryChange()
  {
    try
    {
      provinceObjectBean.loadProvinceSelectItems();  
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }      
  
  @Override
  public Serializable saveState()
  {
    return country;
  }

  @Override
  public void restoreState(Serializable state)
  {
    this.country = (Country)country;
  }  
}
