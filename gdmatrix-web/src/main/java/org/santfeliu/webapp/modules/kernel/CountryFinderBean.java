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
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.kernel.Country;
import org.matrix.kernel.CountryFilter;
import org.santfeliu.faces.ManualScoped;
import org.santfeliu.util.BigList;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TypeBean;

/**
 *
 * @author blanquepa
 */
@Named
@ManualScoped
public class CountryFinderBean 
  extends TerritoryFinderBean<CountryFilter, Country>
{  
  @Inject
  NavigatorBean navigatorBean;
  
  @Inject
  CountryObjectBean countryObjectBean;
  
  @Inject
  CountryTypeBean countryTypeBean;
  
  public CountryFinderBean()
  {
    filter = new CountryFilter();
  }
  
  @Override
  public ObjectBean getObjectBean()
  {
    return countryObjectBean;
  }  
  
  @Override
  public String getObjectId(int position)
  {
    return rows == null ? NEW_OBJECT_ID : 
      rows.get(position).getCountryId();
  }

  @Override
  public int getObjectCount()
  {
    return rows == null ? 0 : rows.size();
  }    

  @Override
  public CountryFilter createFilter()
  {
    return new CountryFilter();
  }  
  
  @Override
  public void smartFind()
  {
    findMode = 1;
    String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
    filter = countryTypeBean.queryToFilter(smartFilter, baseTypeId);
    doFind(true);
    firstRow = 0;
  }

  @Override
  public void find()
  {
    findMode = 2;
    smartFilter = countryTypeBean.filterToQuery(filter);
    doFind(true);
    firstRow = 0;
  }    
  
  @Override
  protected TypeBean getTypeBean()
  {
    return countryTypeBean;
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
              return KernelModuleBean.getPort(false).countCountries(filter);
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
            List<Country> results;            
            try
            {
              filter.setFirstResult(firstResult);
              filter.setMaxResults(maxResults);
              
              results = KernelModuleBean.getPort(false).findCountries(filter);
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
            navigatorBean.view(rows.get(0).getCountryId());
            countryObjectBean.setSearchTabIndex(
              countryObjectBean.getEditionTabIndex());
          }
          else
          {
            countryObjectBean.setSearchTabIndex(0);
          }
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

}
