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
package org.santfeliu.webapp.modules.news;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.news.Source;
import org.matrix.news.SourceFilter;
import org.matrix.dic.DictionaryConstants;
import org.santfeliu.webapp.TypeBean;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.setup.ObjectSetup;

/**
 *
 * @author blanquepa
 */
@Named
@ApplicationScoped
public class SourceTypeBean extends TypeBean<Source, SourceFilter>
{
  private static final String BUNDLE_PREFIX = "$$newsBundle.";

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.SOURCE_TYPE;
  }

  @Override
  public String getObjectId(Source source)
  {
    return source.getId();
  }

  @Override
  public String describe(Source source)
  {
    return source.getName();
  }

  @Override
  public Source loadObject(String objectId)
  {
    try
    {
      return NewsModuleBean.getPort(true).loadSource(objectId);
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  @Override
  public String getTypeId(Source source)
  {
    return DictionaryConstants.THEME_TYPE;
  }

  @Override
  public ObjectSetup createObjectSetup()
  {
    ObjectSetup objectSetup = new ObjectSetup();
    objectSetup.setViewId("/pages/news/source.xhtml");

    List<EditTab> editTabs = new ArrayList<>();
    editTabs.add(new EditTab(BUNDLE_PREFIX + "tab_main",
      "/pages/news/source_main.xhtml"));
    objectSetup.setEditTabs(editTabs);

    return objectSetup;
  }

  @Override
  public SourceFilter queryToFilter(String query, String typeId)
  {
    if (query == null) query = "";

    SourceFilter filter = new SourceFilter();
    if (query.startsWith("http"))
    {
      filter.setUrl(query);
    }
    else
    {
      if (!StringUtils.isBlank(query)) filter.setName(query);
    }
    return filter;
  }

  @Override
  public String filterToQuery(SourceFilter filter)
  {
    String value = "";
    if (!StringUtils.isBlank(filter.getUrl()))
    {
      value = filter.getUrl();
    }
    else if (!StringUtils.isBlank(filter.getName()))
    {
      value = filter.getName();
    }
    return value;
  }

  @Override
  public List<Source> find(SourceFilter filter)
  {
    try
    {
      return NewsModuleBean.getPort(true).findSources(filter);
    }
    catch (Exception ex)
    {
      return Collections.EMPTY_LIST;
    }
  }

}
