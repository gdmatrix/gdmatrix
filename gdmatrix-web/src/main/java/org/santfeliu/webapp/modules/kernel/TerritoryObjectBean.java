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

import org.matrix.kernel.City;
import org.matrix.kernel.Country;
import org.matrix.kernel.KernelManagerPort;
import org.matrix.kernel.Province;
import org.matrix.kernel.Street;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TypeBean;

/**
 *
 * @author blanquepa
 */

public abstract class TerritoryObjectBean extends ObjectBean
{      
  protected Country country = new Country();
  protected Province province = new Province();
  protected City city = new City();
  protected Street street = new Street();

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
  
  public abstract void createObject();
  
  public void create()
  {
    createObject();
  }

  @Override
  public void cancel()
  {
    load();
    info("CANCEL_OBJECT");    
  }
      
  @Override
  public void store()
  {
    try
    {
      storeObject();

      Object object = getObject();

      getBaseTypeInfo().visit(objectId);

      TypeBean typeBean = getTypeBean();
      if (typeBean != null)
      {
        typeBean.updateDescription(objectId, object);
      }

      info("STORE_OBJECT");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public abstract void removeObject() throws Exception;
   
  @Override
  public void remove()
  {
    try
    {
      removeObject();    
      info("REMOVE_OBJECT");
    }
    catch(Exception ex)
    {
      error(ex);
    }
  }
  
  protected void loadStreet(KernelManagerPort port, String streetId)
  {
    street = port.loadStreet(streetId);
    if (!street.getCityId().equals(city.getCityId()))
      loadCity(port, street.getCityId());
  }  
  
  protected void loadCity(KernelManagerPort port, String cityId)
  {
    city = port.loadCity(cityId);
    if (!city.getProvinceId().equals(province.getProvinceId()))
      loadProvince(port, city.getProvinceId());
  }  
  
  protected void loadProvince(KernelManagerPort port, String provinceId)
  {
    province = port.loadProvince(provinceId);
    if (!province.getCountryId().equals(country.getCountryId()))
      loadCountry(port, province.getCountryId());
  }
  
  protected void loadCountry(KernelManagerPort port, String countryId)
  {
    country = port.loadCountry(province.getCountryId());    
  } 
  
}
