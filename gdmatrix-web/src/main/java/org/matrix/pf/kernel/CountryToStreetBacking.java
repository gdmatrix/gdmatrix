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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.kernel.City;
import org.matrix.kernel.CityFilter;
import org.matrix.kernel.Country;
import org.matrix.kernel.CountryFilter;
import org.matrix.kernel.KernelConstants;
import org.matrix.kernel.KernelManagerPort;
import org.matrix.kernel.Province;
import org.matrix.kernel.ProvinceFilter;
import org.matrix.kernel.Street;
import org.matrix.kernel.StreetFilter;
import org.matrix.pf.web.WebBacking;
import org.santfeliu.kernel.web.KernelConfigBean;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.obj.ControllerBean;

/**
 *
 * @author blanquepa
 */
@Named("countryToStreetBacking")
@ViewScoped
public class CountryToStreetBacking extends WebBacking implements Serializable
{
  private List<SelectItem> countrySelectItems;
  private List<SelectItem> provinceSelectItems;
  private List<SelectItem> citySelectItems;
  private List<SelectItem> streetSelectItems;
  
  private Country country;
  private Province province;
  private City city;
  private Street street;

  private String countryId;
  private String provinceId;
  private String cityId;
  private String streetId;
  
  private boolean countryEditing;
  private boolean provinceEditing;
  private boolean cityEditing;
  private boolean streetEditing;
  
  public CountryToStreetBacking()
  {  
  }
  
  public boolean isCountryEditing()
  {
    return countryEditing;
  }

  public void setCountryEditing(boolean countryEditing)
  {
    this.countryEditing = countryEditing;
  }

  public boolean isProvinceEditing()
  {
    return provinceEditing;
  }

  public void setProvinceEditing(boolean provinceEditing)
  {
    this.provinceEditing = provinceEditing;
  }

  public boolean isCityEditing()
  {
    return cityEditing;
  }

  public void setCityEditing(boolean cityEditing)
  {
    this.cityEditing = cityEditing;
  }

  public boolean isStreetEditing()
  {
    return streetEditing;
  }

  public void setStreetEditing(boolean streetEditing)
  {
    this.streetEditing = streetEditing;
  }
  
  public List<SelectItem> getCountrySelectItems()
  {
    return countrySelectItems;
  }
  
  public void setCountrySelectItems(List<SelectItem> countrySelectItems)
  {
    this.countrySelectItems = countrySelectItems;
  }
  
  public List<SelectItem> getProvinceSelectItems()
  {
    return provinceSelectItems;
  }

  public void setProvinceSelectItems(List<SelectItem> provinceSelectItems)
  {
    this.provinceSelectItems = provinceSelectItems;
  }

  public List<SelectItem> getCitySelectItems()
  {
    return citySelectItems;
  }

  public void setCitySelectItems(List<SelectItem> citySelectItems)
  {
    this.citySelectItems = citySelectItems;
  }
    
  public List<SelectItem> getStreetSelectItems()
  {
    return streetSelectItems;
  }

  public void setStreetSelectItems(List<SelectItem> streetSelectItems)
  {
    this.streetSelectItems = streetSelectItems;
  }

  public Country getCountry()
  {
    return country;
  }

  public void setCountry(Country country)
  {
    this.country = country;
  }

  public Province getProvince()
  {
    return province;
  }

  public void setProvince(Province province)
  {
    this.province = province;
  }

  public City getCity()
  {
    return city;
  }

  public void setCity(City city)
  {
    this.city = city;
  }

  public Street getStreet()
  {
    return street;
  }

  public void setStreet(Street street)
  {
    this.street = street;
  }
  
  public String getCountryId()
  {
    return countryId;
  }

  public void setCountryId(String countryId)
  {
    this.countryId = countryId;
  }

  public String getProvinceId()
  {
    return provinceId;
  }

  public void setProvinceId(String provinceId)
  {
    this.provinceId = provinceId;
  }

  public String getCityId()
  {
    return cityId;
  }

  public void setCityId(String cityId)
  {
    this.cityId = cityId;
  }

  public String getStreetId()
  {
    return streetId;
  }

  public void setStreetId(String streetId)
  {
    this.streetId = streetId;
  }
  
  public void showCountry(String countryId)
  {
    this.countryId = countryId;    
    showCountry(KernelConfigBean.getPort(), countryId);
  }
  
  public void showProvince(String provinceId)
  {
    if (StringUtils.isBlank(provinceId))
      showCountry(null);
    else
    {
      this.provinceId = provinceId;
      showProvince(KernelConfigBean.getPort(), provinceId);
    }
  }   
  
  public void showCity(String cityId)
  {
    if (StringUtils.isBlank(cityId))
      showCountry(null);
    else
    {
      this.cityId = cityId;
      showCity(KernelConfigBean.getPort(), cityId);
    }
  }   

  public void showStreet(String streetId)
  {
    if (StringUtils.isBlank(streetId))
      showCountry(null);
    else
    {
      this.streetId = streetId;
      showStreet(KernelConfigBean.getPort(), streetId);
    }
  } 
  
  public boolean isAdminUser()
  {
    return UserSessionBean.getCurrentInstance()
      .isUserInRole(KernelConstants.KERNEL_ADMIN_ROLE);
  }

  //Editing actions
  public void editCountry()
  {
    countryEditing = true;
    provinceEditing = false;
    cityEditing = false;
    streetEditing = false;    
  }
  
  public void editProvince()
  {
    countryEditing = false;
    provinceEditing = true;
    cityEditing = false;
    streetEditing = false;    
  }  
  
  public void editCity()
  {
    countryEditing = false;
    provinceEditing = false;
    cityEditing = true;
    streetEditing = false;    
  } 
  
  public void editStreet()
  {
    countryEditing = false;
    provinceEditing = false;
    cityEditing = false;
    streetEditing = true;    
  }  
  
  public void addCountry()
  {
    country = new Country();
    editCountry();
  }
  
  public void addProvince()
  {
    province = new Province();
    editProvince();
  }  
  
  public void addCity()
  {
    city = new City();
    editCity();
  } 
  
  public void addStreet()
  {
    street = new Street();
    editStreet();
  }   
  
  public void deleteCountry()
  {
  }
  
  public void deleteProvince()
  {
  }

  public void deleteCity()
  {
  }

  public void deleteStreet()
  {
  }  
  
  public void store()
  {
    KernelManagerPort port = KernelConfigBean.getPort();
    if (countryEditing)
    {
      port.storeCountry(country);
      countryEditing = false;   
      loadCountrySelectItems(port);
    }
    else if (provinceEditing)
    {
      port.storeProvince(province);
      provinceEditing = false;
      loadProvinceSelectItems(port, province.getCountryId());
    }
    else if (cityEditing)
    {
      port.storeCity(city);
      cityEditing = false;
      loadCitySelectItems(port, city.getProvinceId());
    }
    else if (streetEditing)
    {
      port.storeStreet(street);
      streetEditing = false;
      loadStreetSelectItems(port, street.getCityId());
    }
  }
  
  public void cancel()
  {
    countryEditing = false;
    provinceEditing = false;
    cityEditing = false;
    streetEditing = false;
  }
  
  public void onCountryChange()
  {
    KernelManagerPort port = KernelConfigBean.getPort();
    country = port.loadCountry(countryId);
    loadProvinceSelectItems(port, countryId);
    resetProvince();
  }    
  
  public void onProvinceChange()
  {
    KernelManagerPort port = KernelConfigBean.getPort();
    province = port.loadProvince(provinceId);
    loadCitySelectItems(port, provinceId);
    resetCity();
    streetSelectItems = null;
  }
  
  public void onCityChange()
  {
    KernelManagerPort port = KernelConfigBean.getPort();
    city = port.loadCity(cityId);    
    loadStreetSelectItems(port, cityId);
    resetStreet();
  }  
  
  private void resetProvince()
  {
    resetCity();
    provinceId = null;
    province = null;
    citySelectItems = null; 
  }
  
  private void resetCity()
  {
    resetStreet();
    cityId = null;
    city = null;
    streetSelectItems = null;       
  }
  
  private void resetStreet()
  {
    streetId = null;
    street = null;
  }
    
  private void loadCountrySelectItems(KernelManagerPort port)
  {
    if (countrySelectItems == null)
    {
      countrySelectItems = new ArrayList<>();
      try
      {
        CountryFilter filter = new CountryFilter();
        List<Country> countries = port.findCountries(filter);
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
    }
  }  
  
  private void loadProvinceSelectItems(KernelManagerPort port, String countryId)
  {
    if (countryId != null)
    {
      provinceSelectItems = new ArrayList<>();
      try
      {
        ProvinceFilter filter = new ProvinceFilter();
        filter.setCountryId(countryId);
        if (!ControllerBean.NEW_OBJECT_ID.equals(countryId))
        {
          List<Province> provinces = port.findProvinces(filter);
          for (Province province : provinces)
          {
            provinceSelectItems.add(
              new SelectItem(province.getProvinceId(), province.getName()));
          }
        }
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }    
  }  
  
  private void loadCitySelectItems(KernelManagerPort port, String provinceId)
  {
    if (provinceId != null)
    {
      citySelectItems = new ArrayList<>();
      try
      {
        CityFilter filter = new CityFilter();
        filter.setProvinceId(provinceId);
        if (!ControllerBean.NEW_OBJECT_ID.equals(provinceId))
        {
          List<City> cities = port.findCities(filter);
          for (City city : cities)
          {
            citySelectItems.add(
              new SelectItem(city.getCityId(), city.getName()));
          }
        }
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }    
  }  
  
  private void loadStreetSelectItems(KernelManagerPort port, String cityId)
  {
   if (cityId != null)
    {
      streetSelectItems = new ArrayList<>();
      try
      {
        StreetFilter filter = new StreetFilter();
        filter.setCityId(cityId);
        if (!ControllerBean.NEW_OBJECT_ID.equals(cityId))
        {
          List<Street> streets = port.findStreets(filter);
          for (Street street : streets)
          {
            String label = street.getName();
            if (street.getStreetTypeId() != null)
            {
              label += " (" + street.getStreetTypeId() + ")";
            }
            streetSelectItems.add(new SelectItem(street.getStreetId(), label));
          }
        }
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }    
  }  
  
  private void showCountry(KernelManagerPort port, String countryId)
  {
    if (!StringUtils.isBlank(countryId))
    {
      this.countryId = countryId;
      country = port.loadCountry(countryId);
    }
    loadCountrySelectItems(port);
  }

  private void showProvince(KernelManagerPort port, String provinceId)
  {
    this.provinceId = provinceId;
    province = port.loadProvince(provinceId);
    if (countryId == null || !countryId.equals(province.getCountryId()))
    {
      loadProvinceSelectItems(port, province.getCountryId());
      showCountry(port, province.getCountryId());
    }
  }  
  
  private void showCity(KernelManagerPort port, String cityId)
  {
    this.cityId = cityId;
    city = port.loadCity(cityId);
    if (provinceId == null || !provinceId.equals(city.getProvinceId()))
    {
      loadCitySelectItems(port, city.getProvinceId());
      showProvince(port, city.getProvinceId());
    }
  }    
  
  private void showStreet(KernelManagerPort port, String streetId)
  {  
    this.streetId = streetId;
    street = port.loadStreet(streetId);
    if (cityId == null || !cityId.equals(street.getCityId()))
    {
      loadStreetSelectItems(port, street.getCityId());
      showCity(port, street.getCityId());
    }
  }
  
}
