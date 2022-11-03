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

import javax.annotation.PostConstruct;
import javax.inject.Named;
import org.matrix.agenda.Theme;
import org.matrix.pf.web.MainPage;
import org.matrix.pf.web.PageBacking;
import org.matrix.pf.web.helper.TabHelper;
import org.matrix.pf.web.helper.TabPage;
import org.matrix.web.WebUtils;
import org.santfeliu.agenda.web.AgendaConfigBean;

/**
 *
 * @author lopezrj-sf
 */
@Named("themeMainBacking")
public class ThemeMainBacking extends PageBacking 
  implements TabPage, MainPage
{
  public static final String OUTCOME = "pf_theme_main";  
  
  private Theme theme;
  private TabHelper tabHelper;
  
  private ThemeBacking themeBacking;

  public ThemeMainBacking()
  {
    //Let to super class constructor.  
  }
  
  @PostConstruct
  public void init()
  {
    themeBacking = WebUtils.getBacking("themeBacking");
    tabHelper = new TabHelper(this);
  }

  @Override
  public ThemeBacking getObjectBacking()
  {
    return themeBacking;
  }

  @Override
  public TabHelper getTabHelper()
  {
    return tabHelper;
  }

  public Theme getTheme()
  {
    return theme;
  }

  public void setTheme(Theme theme)
  {
    this.theme = theme;
  }
  
  @Override
  public String getPageObjectId()
  {
    return themeBacking.getObjectId();
  }
  
  @Override
  public String show(String pageId)
  {
    themeBacking.setObjectId(pageId);
    return show();
  }
  
  @Override
  public String show()
  {
    populate(); 
    return OUTCOME;
  }
  
  @Override
  public void reset()
  {
    create();
  }
  
  @Override
  public void create()
  {
    theme = new Theme();   
  }
  
  @Override
  public void load()
  {
    String themeId = getPageObjectId();
    if (themeId != null)
    {
      try
      {
        theme = AgendaConfigBean.getPort().loadTheme(themeId);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
  }  
  
  @Override
  public String store()
  {
    try
    {
      theme = AgendaConfigBean.getPort().storeTheme(theme);
      themeBacking.setObjectId(theme.getThemeId());      
      info("STORE_OBJECT");
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
    
  @Override
  public String cancel()
  {
    populate();
    info("CANCEL_OBJECT");    
    return null;
  }
  
}
