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

import org.matrix.agenda.Theme;
import org.santfeliu.web.obj.PageBean;

/**
 *
 * @author blanquepa
 */
public class ThemeMainBean extends PageBean
{
  private Theme theme;
  private boolean modified;

  public ThemeMainBean()
  {
  }

  //Accessors
  public Theme getTheme()
  {
    return theme;
  }

  public void setTheme(Theme theme)
  {
    this.theme = theme;
  }

  @Override
  public boolean isModified()
  {
    return true;
  }

  public void setModified(boolean modified)
  {
    this.modified = modified;
  }

  //Actions
  public String show()
  {
    load();
    return "theme_main";
  }

  @Override
  public String store()
  {
    try
    {
      theme = AgendaConfigBean.getPort().storeTheme(theme);
      setObjectId(theme.getThemeId());
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return show();
  }

  protected void load()
  {
    if (isNew())
    {
      theme = new Theme();
    }
    else
    {
      try
      {
        theme = AgendaConfigBean.getPort().loadThemeFromCache(getObjectId());
      }
      catch (Exception ex)
      {
        getObjectBean().clearObject();
        theme = new Theme();
        error(ex);
      }
    }
  }
}
