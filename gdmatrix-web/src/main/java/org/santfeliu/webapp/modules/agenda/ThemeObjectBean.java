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
package org.santfeliu.webapp.modules.agenda;

import java.io.Serializable;
import java.util.ArrayList;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.agenda.Theme;
import org.matrix.dic.DictionaryConstants;
import org.santfeliu.faces.ManualScoped;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.Tab;

/**
 *
 * @author lopezrj-sf
 */
@Named
@ManualScoped
public class ThemeObjectBean extends ObjectBean
{
  private Theme theme = new Theme();

  @Inject
  ThemeTypeBean themeTypeBean;

  @Inject
  ThemeFinderBean themeFinderBean;
  
  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.THEME_TYPE;
  }

  @Override
  public ThemeTypeBean getTypeBean()
  {
    return themeTypeBean;
  }

  @Override
  public Theme getObject()
  {
    return isNew() ? null : theme;
  }

  @Override
  public String getDescription()
  {
    return isNew() ? "" : getDescription(theme.getThemeId());
  }

  public String getDescription(String themeId)
  {
    return getTypeBean().getDescription(themeId);
  }

  @Override
  public ThemeFinderBean getFinderBean()
  {
    return themeFinderBean;
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
  public String show()
  {
    return "/pages/agenda/theme.xhtml";
  }

  @Override
  public void loadObject() throws Exception
  {
    if (!NEW_OBJECT_ID.equals(objectId))
    {
      theme = AgendaModuleBean.getClient(false).loadThemeFromCache(objectId);
    }
    else theme = new Theme();
  }

  @Override
  public void loadTabs()
  {
    super.loadTabs();

    if (tabs.isEmpty())
    {
      tabs = new ArrayList<>(); // empty list may be read only
      tabs.add(new Tab("Main", "/pages/agenda/theme_main.xhtml"));
    }
  }

  @Override
  public void storeObject()
  {
    try
    {
      theme = AgendaModuleBean.getClient(false).storeTheme(theme);
      setObjectId(theme.getThemeId());
      themeFinderBean.outdate();
      info("STORE_OBJECT");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  @Override
  public Serializable saveState()
  {
    return theme;
  }

  @Override
  public void restoreState(Serializable state)
  {
    this.theme = (Theme)state;
  }

}
