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

import java.util.Collections;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.City;
import org.matrix.kernel.CityFilter;
import org.santfeliu.webapp.TypeBean;

/**
 *
 * @author blanquepa
 */
@Named
@ApplicationScoped
public class CityTypeBean extends TypeBean<City, CityFilter>
{
  @Inject
  ProvinceTypeBean provinceTypeBean;

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.CITY_TYPE;
  }

  @Override
  public String getObjectId(City city)
  {
    return city.getCityId();
  }

  @Override
  public String describe(City city)
  {
    String provinceName = provinceTypeBean.getDescription(city.getProvinceId());
    int index = provinceName.indexOf("(");
    if (index != -1) provinceName = provinceName.substring(0, index).trim();

    return city.getName() + " (" + provinceName + ")";
  }

  @Override
  public City loadObject(String objectId)
  {
    try
    {
      if (!StringUtils.isBlank(objectId))
      {
        return KernelModuleBean.getPort(true).loadCity(objectId);
      }
    }
    catch (Exception ex)
    {
    }
    return null;
  }

  @Override
  public CityFilter queryToFilter(String query, String typeId)
  {
    CityFilter filter = new CityFilter();
    if (!StringUtils.isBlank(query))
      filter.setCityName("%" + query + "%");

    return filter;
  }

  @Override
  public String filterToQuery(CityFilter filter)
  {
    String query = "";
    if (filter != null)
      query = filter.getCityName();

    return query;
  }

  @Override
  public List<City> find(CityFilter filter)
  {
    try
    {
      return KernelModuleBean.getPort(true).findCities(filter);
    }
    catch (Exception ex)
    {
    }

    return Collections.emptyList();
  }
}
