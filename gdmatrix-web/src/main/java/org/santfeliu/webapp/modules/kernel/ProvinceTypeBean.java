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
import javax.enterprise.context.ApplicationScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.Province;
import org.matrix.kernel.ProvinceFilter;
import org.santfeliu.webapp.TypeBean;
import org.santfeliu.webapp.setup.ObjectSetup;

/**
 *
 * @author blanquepa
 */
@Named
@ApplicationScoped
public class ProvinceTypeBean extends TypeBean<Province, ProvinceFilter>
{
  @Inject
  CountryTypeBean countryTypeBean;

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.PROVINCE_TYPE;
  }

  @Override
  public String getObjectId(Province province)
  {
    return province.getProvinceId();
  }

  @Override
  public String describe(Province province)
  {
    String countryName = countryTypeBean.getDescription(province.getCountryId());

    return province.getName() + " (" + countryName + ")";
  }

  @Override
  public Province loadObject(String objectId)
  {
    try
    {
      if (!StringUtils.isBlank(objectId))
        return KernelModuleBean.getPort(true).loadProvince(objectId);
    }
    catch (Exception ex)
    {
    }
    return null;
  }

  @Override
  public String getTypeId(Province province)
  {
    return DictionaryConstants.PROVINCE_TYPE;
  }

  @Override
  public ObjectSetup createObjectSetup()
  {
    ObjectSetup objectSetup = new ObjectSetup();
    objectSetup.setViewId("/pages/kernel/province.xhtml");

    return objectSetup;
  }

  @Override
  public ProvinceFilter queryToFilter(String query, String typeId)
  {
    if (query == null)
      query = "";

    ProvinceFilter filter = new ProvinceFilter();
      if (!query.startsWith("%")) query = "%" + query;
      if (!query.endsWith("%")) query += "%";

    filter.setProvinceName(query);

    return filter;
  }

  @Override
  public String filterToQuery(ProvinceFilter filter)
  {
    String query = filter != null && filter.getProvinceName() != null
      ? filter.getProvinceName() : "";

    if (query.startsWith("%")) query = query.substring(1);
    if (query.endsWith("%")) query = query.substring(0, query.length() - 1);

    return query;
  }

  @Override
  public List<Province> find(ProvinceFilter filter)
  {
    try
    {
      return KernelModuleBean.getPort(true).findProvinces(filter);
    }
    catch (Exception ex)
    {
      return Collections.emptyList();
    }
  }

  public List<SelectItem> getProvinceSelectItems(String countryId)
  {
    List<SelectItem> selectItems = new ArrayList<>();
    ProvinceFilter filter = new ProvinceFilter();

    if (!StringUtils.isBlank(countryId))
    {
      filter.setCountryId(countryId);
      List<Province> provinces = find(filter);

      for (Province p : provinces)
      {
        SelectItem item =
          new SelectItem(p.getProvinceId(), p.getName());
        selectItems.add(item);
      }
    }

    return selectItems;
  }

}
