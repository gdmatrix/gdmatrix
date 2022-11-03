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
import javax.faces.model.SelectItem;
import javax.inject.Named;
import org.matrix.agenda.AgendaConstants;
import org.matrix.agenda.Theme;
import org.matrix.agenda.ThemeFilter;
import org.matrix.pf.cms.CMSContent;
import org.matrix.pf.web.ObjectBacking;
import org.matrix.web.WebUtils;
import org.santfeliu.agenda.web.AgendaConfigBean;
import org.santfeliu.util.MatrixConfig;

/**
 *
 * @author lopezrj-sf
 */
@CMSContent(typeId = "Theme")
@Named("themeBacking")
public class ThemeBacking extends ObjectBacking<Theme>
{   
  public ThemeBacking()
  {
    super();  
  }
 
  @Override
  public ThemeSearchBacking getSearchBacking()
  {
    return WebUtils.getBacking("themeSearchBacking");
  }
  
  @Override
  public String getObjectId(Theme theme)
  {
    return theme.getThemeId();
  }

  @Override
  public String getDescription()
  {
    ThemeMainBacking mainBacking = WebUtils.getBacking("themeMainBacking");
    if (mainBacking != null)
      return getDescription(mainBacking.getTheme().getThemeId());
    else
      return super.getDescription();
  }
  
  @Override
  public String getDescription(String objectId)
  {
    try
    {
      if ((objectId != null && objectId.contains(";")) || "".equals(objectId))
        return objectId;
      
      ThemeFilter filter = new ThemeFilter();
      filter.setThemeId(objectId);
      String userId = MatrixConfig.getProperty("adminCredentials.userId");
      String password = MatrixConfig.getProperty("adminCredentials.password");      
      List<Theme> themes = 
        AgendaConfigBean.getPort(userId, password).findThemesFromCache(filter);
      
      if (themes != null && !themes.isEmpty())
        return getDescription(themes.get(0));      
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return objectId;
  }    
  
  @Override
  public String getDescription(Theme theme)
  {
    if (theme == null) return "";    
    return theme.getDescription();
  }  
  
  @Override
  public List<SelectItem> getFavorites()
  {
    return getFavorites(getRootTypeId());
  }  

  @Override
  public String show()
  {
    return super.show();
  }

  @Override
  public String getAdminRole()
  {
    return AgendaConstants.AGENDA_ADMIN_ROLE;
  }
  
  @Override
  public boolean remove(String objectId)
  {
    try
    {
      return AgendaConfigBean.getPort().removeTheme(objectId);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return false;
  }
     
}
