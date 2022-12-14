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

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.City;
import org.matrix.kernel.CityFilter;
import org.matrix.kernel.Province;
import org.matrix.kernel.Street;
import org.matrix.kernel.StreetFilter;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.faces.ManualScoped;
import org.santfeliu.kernel.web.KernelConfigBean;
import org.santfeliu.web.obj.ControllerBean;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.NavigatorBean;
import org.santfeliu.webapp.NavigatorBean.BaseTypeInfo;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author blanquepa
 */
@Named
@ManualScoped
public class TerritoryObjectBean extends ObjectBean
{
  public static final String DEFAULT_CITY_NAME = "defaultCityName";
  
  private static final int MAX_STREET_RESULTS = 10;
  private static final int MAX_CITY_RESULTS = 10;
  
  @Inject
  NavigatorBean navigatorBean;
  
  private String streetId;
  
  public TerritoryObjectBean()
  {  
  }

  public String getStreetId()
  {
    return streetId;
  }

  public void setStreetId(String streetId)
  {
    this.streetId = streetId;
  }
  
  public String getObjectId(Object obj)
  {
    Street street = (Street) obj;
    if (street == null)
    {
      return "";
    }
    StringBuilder buffer = new StringBuilder();
    buffer.append(street.getName());
    //TODO
    return buffer.toString();
  }

  public String getDescription(String objectId)
  {
    if (ControllerBean.NEW_OBJECT_ID.equals(objectId))
    {
      return "";
    }
    try
    {
      Street street = KernelConfigBean.getPort().loadStreet(objectId);
      City city = KernelConfigBean.getPort().loadCity(street.getCityId());
      Province province = KernelConfigBean.getPort().loadProvince(
        city.getProvinceId());
      return getDescription(street, city, province);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return objectId;
  }

  public String getDescription(Object obj)
  {
    Street street = (Street) obj;
    return getDescription(street, null, null);
  }

  public String getDescription(Street street, City city, Province province)
  {
    String streetTypeId = street != null ? street.getStreetTypeId() : null;
    String streetName = street != null ? street.getName() : null;
    String cityName = city != null ? city.getName() : null;
    String provName = province != null ? province.getName() : null;

    return getDescription(streetTypeId, streetName, cityName, provName);
  }

  public String getDescription(String streetTypeId, String streetName,
    String cityName, String provinceName)
  {
    StringBuilder buffer = new StringBuilder();
    if (streetTypeId != null && streetName != null)
    {
      buffer.append(streetTypeId)
        .append(" ")
        .append(streetName);
    }
    if (cityName != null)
    {
      buffer.append(" - ").append(cityName);
    }
    if (provinceName != null)
    {
      buffer.append(" (").append(provinceName).append(")");
    }
    return buffer.toString();
  }

  public List<SelectItem> completeStreet(String query, String currentStreetId)
  {
    ArrayList<SelectItem> items = new ArrayList<>();

    //Add current item
    if (currentStreetId != null)
    {
      String description = getDescription(currentStreetId);
      items.add(new SelectItem(currentStreetId, description));
    }

    //Query search ("streetName" or "streetName,cityName")
    if (query != null && query.length() >= 3)
    {
      String streetName;
      String cityName;
      List<City> cities;

      String[] parts = query.split(",");
      streetName = parts[0].trim();
      if (parts.length > 1)
      {
        cityName = parts[1].trim();
      }
      else
      {
        cityName = WebUtils.getMenuItemProperty(DEFAULT_CITY_NAME);
      }

      if (cityName != null)
      {
        if (!StringUtils.isBlank(cityName) && cityName.length() > 3)
        {
          CityFilter cityFilter = new CityFilter();
          cityFilter.setCityName("%" + cityName + "%");

          cities = KernelConfigBean.getPort().findCities(cityFilter);
          int citiesSize = cities != null ? cities.size() : 0;
          if (citiesSize > MAX_CITY_RESULTS)
          {
            citiesSize = MAX_CITY_RESULTS;
            ResourceBundle bundle = ResourceBundle.getBundle(
              "org.santfeliu.web.resources.WebBundle", getLocale());
            String message = bundle.getString("incompleteResults");
            warn(message);
          }

          int i = 0;
          while (items.size() <= MAX_STREET_RESULTS && cities != null
            && i < citiesSize)
          {
            City city = cities.get(i);
            StreetFilter filter = new StreetFilter();
            filter.setStreetName("%" + streetName + "%");
            filter.setCityId(city.getCityId());
            List<Street> streets
              = KernelConfigBean.getPort().findStreets(filter);
            for (Street street : streets)
            {
              String description
                = getDescription(street, city, null);
              SelectItem item
                = new SelectItem(street.getStreetId(), description);
              items.add(item);
            }
            i++;
          }
        }
      }
      else
      {
        streetName = query.trim();
        StreetFilter filter = new StreetFilter();
        filter.setStreetName("%" + streetName + "%");
        filter.setMaxResults(MAX_STREET_RESULTS);
        List<Street> streets = KernelConfigBean.getPort().findStreets(filter);
        if (streets != null)
        {
          for (Street street : streets)
          {
            String description = getDescription(street);
            SelectItem item = new SelectItem(street.getStreetId(), description);
            items.add(item);
          }
        }
      }
    }
    else
    {
      //Add favorites
//      items.addAll(getFavorites());
    }

    return items;
  }

  @Override
  public FinderBean getFinderBean()
  {
    return null;
  }

  @Override
  public String getRootTypeId()
  {
    BaseTypeInfo baseTypeInfo = navigatorBean.getBaseTypeInfo();
    String typeId = baseTypeInfo.getBaseTypeId();
    Type type = TypeCache.getInstance().getType(typeId);
    return type.getRootTypeId();
  }

  @Override
  public String show()
  {
    String rootTypeId = getRootTypeId();
    if (DictionaryConstants.STREET_TYPE.equals(rootTypeId))
      return "/pages/kernel/street.xhtml";
    else if (DictionaryConstants.CITY_TYPE.equals(rootTypeId))
      return "/pages/kernel/city.xhtml";
    else if (DictionaryConstants.PROVINCE_TYPE.equals(rootTypeId))
      return "/pages/kernel/province.xhtml";
    else
      return "/pages/kernel/country.xhtml";
  }
  
}
