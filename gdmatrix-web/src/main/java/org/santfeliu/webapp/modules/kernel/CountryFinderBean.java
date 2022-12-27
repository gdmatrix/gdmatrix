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
import org.matrix.kernel.Country;
import org.matrix.kernel.CountryFilter;
import org.santfeliu.faces.ManualScoped;
import org.santfeliu.webapp.NavigatorBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.helpers.ResultListHelper;

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
    resultListHelper = new CountryResultListHelper();
  }
  
  @Override
  public ObjectBean getObjectBean()
  {
    return countryObjectBean;
  }  
  
  @Override
  public String getObjectId(int position)
  {
    return resultListHelper.getRows() == null ? NEW_OBJECT_ID : 
      resultListHelper.getRow(position).getCountryId();
  }

  @Override
  public int getObjectCount()
  {
    return resultListHelper.getRows() == null ? 0 : 
      resultListHelper.getRowCount();
  }    

  public void clear()
  {
    filter = new CountryFilter();
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
      filter = (CountryFilter)stateArray[2];
      
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
      resultListHelper.find();

      if (autoLoad)
      {
        if (resultListHelper.getRowCount() == 1)
        {
          navigatorBean.view(resultListHelper.getRow(0).getCountryId());
          countryObjectBean.setSearchTabIndex(1);
        }
        else
        {
          countryObjectBean.setSearchTabIndex(0);
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private class CountryResultListHelper extends ResultListHelper<Country>
  {

    @Override
    public List<Country> getResults(int firstResult, int maxResults)
    {
      List<Country> results = new ArrayList();       
      try
      {
        if (isSmartFind)
        {
          setTabIndex(0);
          filter = new CountryFilter();
          filter.setFirstResult(firstResult);
          filter.setMaxResults(maxResults);
          String baseTypeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
          filter = countryTypeBean.queryToFilter(smartFilter, baseTypeId);
        }
        else
        {
          smartFilter = countryTypeBean.filterToQuery(filter);          
          setTabIndex(1);
        } 
        results = KernelModuleBean.getPort(false).findCountries(filter);        
      }
      catch (Exception ex)
      {
        throw new RuntimeException(ex);
      }
      return results;
    }
  }
  
}
