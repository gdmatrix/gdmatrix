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
package org.matrix.pf.kernel;

import java.util.List;
import javax.annotation.PostConstruct;
import org.matrix.kernel.CountryFilter;
import org.matrix.pf.web.SearchBacking;
import org.matrix.web.WebUtils;
import org.santfeliu.kernel.web.KernelConfigBean;

/**
 *
 * @author blanquepa
 */
public class CountrySearchBacking extends SearchBacking
{
  private static final String OUTCOME = "pf_country_search"; 
  
  private CountryFilter filter;
  
  private CountryBacking countryBacking;
  
  @PostConstruct
  public void init()
  {
    countryBacking = WebUtils.getBacking("countryBacking");
  }

  public CountryFilter getFilter()
  {
    return filter;
  }

  public void setFilter(CountryFilter filter)
  {
    this.filter = filter;
  }

  public CountryBacking getCountryBacking()
  {
    return countryBacking;
  }

  public void setCountryBacking(CountryBacking countryBacking)
  {
    this.countryBacking = countryBacking;
  }
  
  @Override
  public String smartSearch()
  {
    filter = new CountryFilter(); 
    filter.setCountryName("%" + smartValue + "%");
    return search();
  }

  @Override
  public String clear()
  {     
    filter = new CountryFilter();
    smartValue = null;
    return null;  
  }

  @Override
  public String getFilterTypeId()
  {
    return countryBacking.getRootTypeId();
  }

  @Override
  public String getOutcome()
  {
    return OUTCOME;
  }

  @Override
  public int countResults()
  {
    return KernelConfigBean.getPort().countCountries(filter);
  }

  @Override
  public List<?> getResults(int firstResult, int maxResults)
  {
    return KernelConfigBean.getPort().findCountries(filter);
  }

  @Override
  public CountryBacking getObjectBacking()
  {
    return countryBacking;
  }
  
}
