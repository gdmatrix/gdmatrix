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
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.kernel.Province;
import org.matrix.kernel.ProvinceFilter;
import org.santfeliu.faces.ManualScoped;
import org.santfeliu.util.BigList;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TypeBean;
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
  ProvinceTypeBean provinceTypeBean;
  
  @Inject
  CountryTypeBean countryTypeBean;
  
  public ProvinceFinderBean()
  {
    filter = new ProvinceFilter();
  }
  
  @Override
  public ObjectBean getObjectBean()
  {
    return provinceObjectBean;
  } 
  
  @Override
  public String getObjectId(int position)
  {
    return rows == null ? NEW_OBJECT_ID : rows.get(position).getProvinceId();
  }

  @Override
  public int getObjectCount()
  {
    return rows == null ? 0 : rows.size();
  }    

  @Override
  public ProvinceFilter createFilter()
  {
    return new ProvinceFilter();
  } 
  
  @Override
  public void smartFind()
  {
    findMode = 1;
    String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
    filter = provinceTypeBean.queryToFilter(smartFilter, baseTypeId);
    doFind(true);
    firstRow = 0;
  }

  @Override
  public void find()
  {
    findMode = 2;
    smartFilter = provinceTypeBean.filterToQuery(filter);
    doFind(true);
    firstRow = 0;
  }   

  @Override
  protected TypeBean getTypeBean()
  {
    return provinceTypeBean;
  }
         
  @Override
  protected void doFind(boolean autoLoad)
  {
    try
    {
      if (findMode == 0)
      {
        rows = Collections.EMPTY_LIST;
      }
      else
      {
        if (findMode == 1)
        {
          setTabIndex(0);
        }
        else
        {
          setTabIndex(1);
        }

        rows = new BigList(20, 10)
        {
          @Override
          public int getElementCount()
          {
            try
            {
              return KernelModuleBean.getPort(false).countProvinces(filter);
            }
            catch (Exception ex)
            {
              error(ex);
              return 0;
            }
          }

          @Override
          public List getElements(int firstResult, int maxResults)
          {
            List<ProvinceView> results = new ArrayList();            
            try
            {
              filter.setFirstResult(firstResult);
              filter.setMaxResults(maxResults);
              
              List<Province> streets =
                KernelModuleBean.getPort(false).findProvinces(filter);
              for (Province street : streets)        
              {
                ProvinceView view = new ProvinceView(street);
                results.add(view);
              }
            }
            catch (Exception ex)
            {
              error(ex);
              return null;
            }
            return results;
          }
        };

        outdated = false;

        if (autoLoad)
        {
          if (rows.size() == 1)
          {
            navigatorBean.view(rows.get(0).getProvinceId());
            provinceObjectBean.setSearchTabIndex(
              provinceObjectBean.getEditionTabIndex());
          }
          else
          {
            provinceObjectBean.setSearchTabIndex(0);
          }
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
}
