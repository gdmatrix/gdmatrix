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
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.kernel.City;
import org.matrix.kernel.CityFilter;
import org.santfeliu.faces.ManualScoped;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.helpers.ResultListHelper;
import org.santfeliu.webapp.modules.kernel.CityFinderBean.CityView;

/**
 *
 * @author blanquepa
 */
@Named
@ManualScoped
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
  
  public CityFinderBean()
  {
    filter = new CityFilter();
    resultListHelper = new CityResultListHelper();
  }
  
  @Override
  public ObjectBean getObjectBean()
  {
    return cityObjectBean;
  }  
  
  @Override
  public String getObjectId(int position)
  {
    return resultListHelper.getRows() == null ? NEW_OBJECT_ID : 
      resultListHelper.getRow(position).getCityId();
  }

  @Override
  public int getObjectCount()
  {
    return resultListHelper.getRows() == null ? 0 : 
      resultListHelper.getRowCount();
  }    

  public void clear()
  {
    filter = new CityFilter();
    resultListHelper.clear();
  }  
  
  @Override
  public Serializable saveState()
  {
    return new Object[]{ isSmartFind, smartFilter, filter, 
      resultListHelper.getFirstRowIndex(), getObjectPosition() };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      isSmartFind = (Boolean)stateArray[0];
      smartFilter = (String)stateArray[1];
      filter = (CityFilter)stateArray[2];
      
      doFind(false);

      resultListHelper.setFirstRowIndex((Integer) stateArray[3]);
      setObjectPosition((Integer)stateArray[4]);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }     
      
  @Override
  protected void doFind(boolean autoLoad)
  {
    try
    {
      if (isSmartFind)
      {
        setTabIndex(0);
        String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();        
        filter = cityTypeBean.queryToFilter(smartFilter, baseTypeId);
      }
      else
      {
        setTabIndex(1);
      }
      
      resultListHelper.find();
      
      if (autoLoad)
      {
        if (resultListHelper.getRowCount() == 1)
        {
          CityView view = (CityView) resultListHelper.getRow(0);
          navigatorBean.view(view.getCityId());
          cityObjectBean.setSearchTabIndex(1);
        }
        else
        {
          cityObjectBean.setSearchTabIndex(0);
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
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

  private class CityResultListHelper extends ResultListHelper<CityView>
  {
    @Override
    public List<CityView> getResults(int firstResult, int maxResults)
    {
      List<CityView> results = new ArrayList(); 
      try
      {
        List<City> cities =
          KernelModuleBean.getPort(false).findCities(filter);
        for (City city : cities)
        {
          CityView view = new CityView(city);
          results.add(view);
        }  
      }
      catch(Exception ex)
      {
        throw new RuntimeException(ex);
      }
      return results;
    }

//    @Override
//    public int countResults()
//    {
//      try
//      {
//        return KernelModuleBean.getPort(false).countCities(filter);
//      }
//      catch (Exception ex)
//      {
//        throw new RuntimeException(ex);
//      }
//    }
  }
  
}
