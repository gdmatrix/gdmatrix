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
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.City;
import org.matrix.kernel.KernelManagerPort;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.NavigatorBean;

/**
 *
 * @author blanquepa
 */
@Named
@ViewScoped
public class CityObjectBean extends TerritoryObjectBean
{
  @Inject
  CityFinderBean cityFinderBean;
      
  @Inject
  CityTypeBean cityTypeBean;
  
  @Inject
  NavigatorBean navigatorBean;
  
  
  @Override
  public FinderBean getFinderBean()
  {
    return cityFinderBean;
  }

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.CITY_TYPE;
  }

  @Override
  public City getObject()
  {
    return isNew() ? null : city;
  }
  
  @Override
  public CityTypeBean getTypeBean()
  {
    return cityTypeBean;
  }   
  
  @Override
  public String getDescription()
  {
    return isNew() ? "" : getDescription(city.getCityId());
  }  
  
  public String getDescription(String cityId)
  {
    return getTypeBean().getDescription(cityId);
  }   
  
  @Override
  public void createObject()
  {
    city = new City();
    if (province != null)
      city.setProvinceId(province.getProvinceId());
    setObjectId(NavigatorBean.NEW_OBJECT_ID);
  }

  @Override
  public void loadObject() throws Exception
  {
    if (objectId != null && !isNew())
    {
      KernelManagerPort port = KernelModuleBean.getPort(false);
      loadCity(port, objectId);
    }
    else
    {
      createObject();
    }
  }
  
  @Override
  public void storeObject() throws Exception
  {
    String provinceId = province.getProvinceId();
    if (!provinceId.equals(city.getProvinceId()))
        city.setProvinceId(provinceId);
    
    city = KernelModuleBean.getPort(false).storeCity(city);
    setObjectId(city.getCityId());
    
    cityFinderBean.outdate();    
  } 

  
  @Override
  public void removeObject() throws Exception
  {
    KernelModuleBean.getPort(false).removeCity(objectId);
    cityFinderBean.doFind(false);
    info("REMOVE_OBJECT");    
    navigatorBean.view("");
  }    

    
//  public List<SelectItem> getCitySelectItems()
//  {
//    if (citySelectItems == null)
//    {
//      try
//      {
//        loadCitySelectItems();
//      }
//      catch (Exception ex)
//      {
//        error(ex);
//      }
//    }
//
//    return citySelectItems;
//  }

//  public void loadCitySelectItems() throws Exception
//  {
//    citySelectItems = new ArrayList<>();
//    CityFilter filter = new CityFilter();
//    String provinceId = provinceObjectBean.getProvince().getProvinceId();
//
//    if (!StringUtils.isBlank(provinceId))
//    {
//      filter.setProvinceId(provinceId);
//
//      List<City> cities
//        = KernelModuleBean.getPort(false).findCities(filter);
//
//      for (City c : cities)
//      {
//        SelectItem item
//          = new SelectItem(c.getCityId(), c.getName());
//        citySelectItems.add(item);
//      }
//    }
//  }

  @Override
  public Serializable saveState()
  {
    return city;
  }

  @Override
  public void restoreState(Serializable state)
  {
    this.city = (City) city;
  }
}
