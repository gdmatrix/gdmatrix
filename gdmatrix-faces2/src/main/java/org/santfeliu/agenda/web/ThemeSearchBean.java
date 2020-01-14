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
package org.santfeliu.agenda.web;

import java.util.List;
import org.matrix.agenda.Theme;
import org.matrix.agenda.ThemeFilter;
import org.santfeliu.web.obj.BasicSearchBean;

/**
 *
 * @author blanquepa
 */
public class ThemeSearchBean extends BasicSearchBean
{
  private ThemeFilter filter;

  public ThemeSearchBean()
  {
    filter = new ThemeFilter();
  }

  //Getters & Setters
  public void setFilter(ThemeFilter filter)
  {
    this.filter = filter;
  }

  public ThemeFilter getFilter()
  {
    return filter;
  }

  //User actions
  public int countResults()
  {
    try
    {
      return AgendaConfigBean.getPort().countThemesFromCache(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return 0;
  }

  public List getResults(int firstResult, int maxResults)
  {
    try
    {
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      List results = AgendaConfigBean.getPort().findThemesFromCache(filter);

      return results;
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String show()
  {
    return "theme_search";
  }

  public String showTheme()
  {
    return getControllerBean().showObject("Theme",
      (String)getValue("#{row.themeId}"));
  }

  public String selectTheme()
  {
    Theme row = (Theme)getExternalContext().getRequestMap().get("row");
    String themeId = row.getThemeId();
    return getControllerBean().select(themeId);
  }
}
