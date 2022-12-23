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
import org.apache.commons.lang.StringUtils;
import org.matrix.kernel.Province;
import org.matrix.kernel.ProvinceFilter;
import org.santfeliu.faces.ManualScoped;
import org.santfeliu.webapp.NavigatorBean;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.helpers.BigListHelper;
import org.santfeliu.webapp.modules.kernel.ProvinceFinderBean.ProvinceView;

/**
 *
 * @author blanquepa
 */
@Named
@ManualScoped
public class ProvinceFinderBean 
  extends TerritoryFinderBean<ProvinceFilter, ProvinceView>
{    
  @Inject
  NavigatorBean navigatorBean;
  
  @Inject
  ProvinceObjectBean provinceObjectBean;
  
  @Inject
  CountryTypeBean countryTypeBean;
  
  public ProvinceFinderBean()
  {
    filter = new ProvinceFilter();
    resultListHelper = new ProvinceResultListHelper();
  }
  
  @Override
  public ObjectBean getObjectBean()
  {
    return provinceObjectBean;
  }  

  public void clear()
  {
    filter = new ProvinceFilter();
    resultListHelper.clear();
  }  
      
  @Override
  protected void doFind(boolean autoLoad)
  {
    try
    {
      if (isSmartFind)
      {
        setTabIndex(0);
        //TODO: Set Country
        if (!StringUtils.isBlank(smartFilter))
          filter.setProvinceName("%" + smartFilter + "%");
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
          ProvinceView view = (ProvinceView) resultListHelper.getRow(0);
          navigatorBean.view(view.getProvinceId());
          provinceObjectBean.setSearchTabIndex(1);
        }
        else
        {
          provinceObjectBean.setSearchTabIndex(0);
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public class ProvinceView implements Serializable
  {
    private String provinceId;
    private String name;
    private String countryId;
    private String country;
    
    public ProvinceView(Province province)
    {
      this.provinceId = province.getProvinceId();      
      this.name = province.getName();
      this.countryId = province.getCountryId(); 
      this.country = countryTypeBean.getDescription(countryId);
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

    public String getCountryId()
    {
      return countryId;
    }

    public void setCountryId(String countryId)
    {
      this.countryId = countryId;
    }   

    public String getCountry()
    {
      return country;
    }

    public void setCountry(String country)
    {
      this.country = country;
    }

  }

  private class ProvinceResultListHelper extends BigListHelper<ProvinceView>
  {
    @Override
    public int countResults()
    {
      try
      {
        return KernelModuleBean.getPort(false).countProvinces(filter);
      }
      catch (Exception ex)
      {
        throw new RuntimeException(ex);
      }
    }
    
    @Override
    public List<ProvinceView> getResults(int firstResult, int maxResults)
    {
      try
      {
        List<ProvinceView> results = new ArrayList<>();
        List<Province> provinces =
          KernelModuleBean.getPort(false).findProvinces(filter);
        for (Province province : provinces)
        {
          ProvinceView view = new ProvinceView(province);
          results.add(view);
        }
        return results;
      }
      catch (Exception ex)
      {
        throw new RuntimeException(ex);
      }
    }
  }
  
}
