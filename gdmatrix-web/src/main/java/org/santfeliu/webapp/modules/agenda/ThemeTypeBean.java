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

import java.util.Collections;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.agenda.Theme;
import org.matrix.agenda.ThemeFilter;
import org.matrix.dic.DictionaryConstants;
import org.santfeliu.webapp.TypeBean;
import static org.santfeliu.webapp.modules.agenda.AgendaModuleBean.getClient;

/**
 *
 * @author lopezrj-sf
 */
@Named
@ApplicationScoped
public class ThemeTypeBean extends TypeBean<Theme, ThemeFilter>
{
  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.THEME_TYPE;
  }

  @Override
  public String describe(Theme theme)
  {
    return theme.getDescription();
  }

  @Override
  public Theme loadObject(String objectId)
  {
    try
    {
      return getClient(true).loadThemeFromCache(objectId);
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  @Override
  public ThemeFilter queryToFilter(String query, String typeId)
  {
    if (query == null) query = "";

    ThemeFilter filter = new ThemeFilter();
    if (checkIntegerValues(query))
    {        
      filter.setThemeId(query);
    }
    else        
    {
      if (!StringUtils.isBlank(query)) filter.setDescription(query);
    }
    return filter;
  }

  @Override
  public String filterToQuery(ThemeFilter filter)
  {
    String value = "";
    if (!StringUtils.isBlank(filter.getThemeId()))
    {
      value = filter.getThemeId();
    }
    else if (!StringUtils.isBlank(filter.getDescription()))
    {
      value = filter.getDescription();
    }
    return value;
  }

  @Override
  public List<Theme> find(ThemeFilter filter)
  {
    try
    {
      return getClient(true).findThemesFromCache(filter);
    }
    catch (Exception ex)
    {
      return Collections.EMPTY_LIST;
    }
  }
  
  private boolean checkIntegerValues(String s)
  {
    String[] split = s.split(",");
    for (String item : split)
    {
      try
      {
        Integer.valueOf(item);
      }
      catch (NumberFormatException ex)
      {
        return false;
      }      
    }
    return true;
  }  

}
