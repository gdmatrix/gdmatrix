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
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.City;
import org.matrix.kernel.CityFilter;
import org.matrix.kernel.KernelList;
import org.matrix.kernel.Street;
import org.matrix.kernel.StreetFilter;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.kernel.web.KernelConfigBean;
import org.santfeliu.webapp.TypeBean;
import org.santfeliu.webapp.setup.ObjectSetup;
import static org.santfeliu.webapp.modules.kernel.StreetObjectBean.DEFAULT_CITY_NAME;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author blanquepa
 */
@Named
@ApplicationScoped
public class StreetTypeBean extends TypeBean<Street, StreetFilter>
{
  private static final int MAX_STREET_RESULTS = 10;
  private static final int MAX_CITY_RESULTS = 10;

  @Inject
  CityTypeBean cityTypeBean;

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.STREET_TYPE;
  }

  @Override
  public String getObjectId(Street street)
  {
    return street.getStreetId();
  }

  @Override
  public String describe(Street street)
  {
    String cityName = cityTypeBean.getDescription(street.getCityId());
    int index = cityName.indexOf("(");
    if (index != -1) cityName = cityName.substring(0, index).trim();

    return street.getName() + " (" + cityName + ")";
  }

  @Override
  public Street loadObject(String objectId)
  {
    try
    {
      if (!StringUtils.isBlank(objectId))
      {
        return KernelModuleBean.getPort(true).loadStreet(objectId);
      }
    }
    catch (Exception ex)
    {
    }
    return null;
  }

  @Override
  public String getTypeId(Street street)
  {
    return DictionaryConstants.STREET_TYPE;
  }

  @Override
  public ObjectSetup createObjectSetup()
  {
    ObjectSetup objectSetup = new ObjectSetup();
    objectSetup.setViewId("/pages/kernel/street.xhtml");

    return objectSetup;
  }

  @Override
  public StreetFilter queryToFilter(String query, String typeId)
  {
    if (query == null)
      query = "";

    StreetFilter filter = new StreetFilter();
      if (!query.startsWith("%")) query = "%" + query;
      if (!query.endsWith("%")) query += "%";

    filter.setStreetName(query);

    return filter;
  }

  @Override
  public String filterToQuery(StreetFilter filter)
  {
    String query = filter != null && filter.getStreetName() != null ?
      filter.getStreetName() : "";

    if (query.startsWith("%")) query = query.substring(1);
    if (query.endsWith("%")) query = query.substring(0, query.length() - 1);

    return query;
  }

  @Override
  public List<Street> find(StreetFilter filter)
  {
    try
    {
      return KernelModuleBean.getPort(true).findStreets(filter);
    }
    catch (Exception ex)
    {
      return Collections.emptyList();
    }
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
              String description = describe(street);
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
            String description = describe(street);
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
  
  public List<SelectItem> getStreetTypeSelectItems()
  {
    List<SelectItem> selectItems = new ArrayList<>();    
    try
    {
      selectItems = FacesUtils.getListSelectItems(
        KernelModuleBean.getPort(false).listKernelListItems(
          KernelList.STREET_TYPE), "itemId", "description", true);
    }
    catch (Exception ex)
    {
      selectItems = Collections.EMPTY_LIST;
    }

    return selectItems;
  }  

}
