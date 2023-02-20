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
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.Country;
import org.matrix.kernel.CountryFilter;
import org.santfeliu.webapp.TypeBean;
import javax.enterprise.context.ApplicationScoped;
import org.santfeliu.webapp.setup.ObjectSetup;

/**
 *
 * @author blanquepa
 */
@Named
@ApplicationScoped
public class CountryTypeBean extends TypeBean<Country, CountryFilter>
{
  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.COUNTRY_TYPE;
  }

  @Override
  public String getObjectId(Country country)
  {
    return country.getCountryId();
  }

  @Override
  public String describe(Country country)
  {
    return country.getName();
  }

  @Override
  public Country loadObject(String objectId)
  {
    try
    {
      if (!StringUtils.isBlank(objectId))
      {
        return KernelModuleBean.getPort(true).loadCountry(objectId);
      }
    }
    catch (Exception ex)
    {
    }
    return null;
  }

  @Override
  public String getTypeId(Country country)
  {
    return DictionaryConstants.COUNTRY_TYPE;
  }

  @Override
  public ObjectSetup createObjectSetup()
  {
    ObjectSetup objectSetup = new ObjectSetup();
    objectSetup.setViewId("/pages/kernel/country.xhtml");

    return objectSetup;
  }

  @Override
  public CountryFilter queryToFilter(String query, String typeId)
  {
    if (query == null)
      query = "";

    CountryFilter filter = new CountryFilter();
      if (!query.startsWith("%")) query = "%" + query;
      if (!query.endsWith("%")) query += "%";

    filter.setCountryName(query);

    return filter;
  }

  @Override
  public String filterToQuery(CountryFilter filter)
  {
    String query = filter != null && filter.getCountryName() != null
      ? filter.getCountryName() : "";

    if (query.startsWith("%")) query = query.substring(1);
    if (query.endsWith("%")) query = query.substring(0, query.length() - 1);

    return query;
  }

  @Override
  public List<Country> find(CountryFilter filter)
  {
    try
    {
      return KernelModuleBean.getPort(true).findCountries(filter);
    }
    catch (Exception ex)
    {
      return Collections.emptyList();
    }
  }

}
