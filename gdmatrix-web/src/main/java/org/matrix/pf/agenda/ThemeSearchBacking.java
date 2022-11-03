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
package org.matrix.pf.agenda;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.agenda.Theme;
import org.matrix.agenda.ThemeFilter;
import org.matrix.pf.web.SearchBacking;
import org.matrix.pf.web.helper.TypedHelper;
import org.matrix.pf.web.helper.TypedPage;
import org.matrix.web.WebUtils;
import org.santfeliu.agenda.web.AgendaConfigBean;

/**
 *
 * @author lopezrj-sf
 */
@Named("themeSearchBacking")
public class ThemeSearchBacking extends SearchBacking 
  implements TypedPage
{  
  public static final String OUTCOME = "pf_theme_search";
  
  private ThemeBacking themeBacking;
  
  private ThemeFilter filter;
  private TypedHelper typedHelper;
  
  public ThemeSearchBacking()
  {   
  }
  
  @PostConstruct
  public void init()
  {
    themeBacking = WebUtils.getBacking("themeBacking");
    filter = new ThemeFilter();
    smartValue = null;
    typedHelper = new TypedHelper(this); 
  }
  
  public ThemeFilter getFilter()
  {
    return filter;
  }

  public void setFilter(ThemeFilter filter)
  {
    this.filter = filter;
  }

  public String getThemeId() 
  {
    return filter.getThemeId();
  }

  public void setThemeId(String themeId) 
  {
    filter.setThemeId(themeId);
  }

  @Override
  public ThemeBacking getObjectBacking()
  {
    return themeBacking;
  }
  
  @Override
  public String show()
  {
    String outcome = super.show();
    return outcome;
  }
     
  @Override
  public String search()
  {
    smartValue = convert(filter);
    return super.search();
  }
  
  @Override
  public String smartSearch()
  {
    filter = convert(smartValue);
    return super.search();
  }

  @Override
  public String clear()
  {
    filter = new ThemeFilter();
    smartValue = null;
    bigListHelper.reset();
    return null;
  }
  
  @Override
  public int countResults()
  {
    try
    {
      return AgendaConfigBean.getPort().countThemes(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return 0;
  }

  @Override
  public List<Theme> getResults(int firstResult, int maxResults)
  {
    List<Theme> results = null;
    try
    {
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      results = AgendaConfigBean.getPort().findThemes(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return results;
  }  
  
  private ThemeFilter convert(String smartValue)
  {
    filter = new ThemeFilter();
    if (smartValue != null)
    {
      try
      {
        Integer.valueOf(smartValue);
        filter.setThemeId(smartValue);
      }
      catch (NumberFormatException ex)
      {
        if (!StringUtils.isBlank(smartValue))
          filter.setDescription(smartValue);
      }
    }  
    return filter;
  }
    
  private String convert(ThemeFilter filter)
  {
    String value = null;
    if (!StringUtils.isBlank(filter.getThemeId()))
      value = filter.getThemeId();
    else if (!StringUtils.isBlank(filter.getDescription()))
    {
      value = filter.getDescription();      
    }
    return value;
  }

  @Override
  public String getTypeId()
  {
    return getMenuItemTypeId();
  }
  
  @Override
  public String getOutcome()
  {
    return OUTCOME;
  }
  
  @Override
  public String getRootTypeId()
  {
    return themeBacking.getRootTypeId();
  }
  
  @Override
  public String getConfigTypeId() 
  {
    return getMenuItemTypeId();
  }  

  @Override
  public TypedHelper getTypedHelper()
  {
    return typedHelper;
  }

}
