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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.City;
import org.matrix.kernel.CityFilter;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TypeBean;
import org.santfeliu.webapp.modules.kernel.CityFinderBean.CityView;

/**
 *
 * @author blanquepa
 */
@Named
@RequestScoped
public class CityFinderBean extends TerritoryFinderBean<CityFilter, CityView>
{
  @Inject
  NavigatorBean navigatorBean;

  @Inject
  CityObjectBean cityObjectBean;

  @Inject
  CityTypeBean cityTypeBean;

  @Inject
  ProvinceTypeBean provinceTypeBean;

  private List<SelectItem> provinceSelectItems;

  public CityFinderBean()
  {
    filter = new CityFilter();
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return cityObjectBean;
  }

  @Override
  public String getObjectId(int position)
  {
    return rows == null ? NEW_OBJECT_ID : rows.get(position).getCityId();
  }

  @Override
  public int getObjectCount()
  {
    return rows == null ? 0 : rows.size();
  }

  @Override
  public CityFilter createFilter()
  {
    return new CityFilter();
  }

  @Override
  public void smartFind()
  {
    setFinding(true);
    setFilterTabSelector(0);
    filter =
      cityTypeBean.queryToFilter(smartFilter, DictionaryConstants.CITY_TYPE);
    doFind(true);
    resetWildcards(filter);
    firstRow = 0;
  }

  @Override
  public void find()
  {
    setFinding(true);
    setFilterTabSelector(1);
    smartFilter = cityTypeBean.filterToQuery(filter);
    doFind(true);
    firstRow = 0;
  }

  @Override
  protected TypeBean getTypeBean()
  {
    return cityTypeBean;
  }

  @Override
  protected void doFind(boolean autoLoad)
  {
    try
    {
      if (!isFinding())
      {
        rows = Collections.EMPTY_LIST;
      }
      else
      {
        String name = filter.getCityName();
        filter.setCityName(setWildcards(name));
        rows = new ArrayList();
        List<City> cities =
          KernelModuleBean.getPort(false).findCities(filter);
        for (City city : cities)
        {
          CityView view = new CityView(city);
          rows.add(view);
        }
        filter.setCityName(name);

        outdated = false;

        if (autoLoad)
        {
          if (rows.size() == 1)
          {
            navigatorBean.view(rows.get(0).getCityId());
            cityObjectBean.setSearchTabSelector(
              cityObjectBean.getEditModeSelector());
          }
          else
          {
            cityObjectBean.setSearchTabSelector(0);
          }
        }
      }
    }
    catch(Exception ex)
    {
      error(ex);
    }
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ isFinding(), getFilter(), firstRow, getObjectPosition(), provinceSelectItems };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      setFinding((Boolean)stateArray[0]);
      setFilter((CityFilter) stateArray[1]);
      smartFilter = getTypeBean().filterToQuery(filter);

      doFind(false);

      firstRow = (Integer)stateArray[2];
      setObjectPosition((Integer)stateArray[3]);
      provinceSelectItems = (List<SelectItem>) stateArray[4];
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void onCountryChange()
  {
    provinceSelectItems =
      provinceTypeBean.getProvinceSelectItems(filter.getCountryId());
  }

  public List<SelectItem> getProvinceSelectItems()
  {
    return provinceSelectItems;
  }


  public class CityView implements Serializable
  {
    private String cityId;
    private String name;
    private String provinceId;
    private String province;

    public CityView(City city)
    {
      this.cityId = city.getCityId();
      this.name = city.getName();
      this.provinceId = city.getProvinceId();
      this.province = provinceTypeBean.getDescription(provinceId);
    }

    public String getProvinceId()
    {
      return provinceId;
    }

    public void setProvinceId(String provinceId)
    {
      this.provinceId = provinceId;
    }

    public String getName()
    {
      return name;
    }

    public void setName(String name)
    {
      this.name = name;
    }

    public String getCityId()
    {
      return cityId;
    }

    public void setCityId(String cityId)
    {
      this.cityId = cityId;
    }

    public String getProvince()
    {
      return province;
    }

    public void setProvince(String province)
    {
      this.province = province;
    }

  }

  private String setWildcards(String text)
  {
    if (text != null && !text.startsWith("\"") && !text.endsWith("\""))
      text = "%" + text.replaceAll("^%|%$", "") + "%" ;
    else if (text != null && text.startsWith("\"") && text.endsWith("\""))
      text = text.replaceAll("^\"|\"$", "");
    return text;
  }

  private void resetWildcards(CityFilter filter)
  {
    String title = filter.getCityName();
    if (title != null && !title.startsWith("\"") && !title.endsWith("\""))
      title = title.replaceAll("^%+|%+$", "");
    filter.setCityName(title);
  }

}
