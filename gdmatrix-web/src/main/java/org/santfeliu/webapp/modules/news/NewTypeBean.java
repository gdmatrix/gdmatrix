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
import org.matrix.dic.DictionaryConstants;
import org.matrix.news.New;
import org.matrix.news.NewsFilter;
import org.santfeliu.webapp.TypeBean;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.setup.ObjectSetup;

/**
 *
 * @author blanquepa
 */
@Named
@ApplicationScoped
public class NewTypeBean extends TypeBean<New, NewsFilter>
{
  private static final String BUNDLE_PREFIX = "$$newsBundle."; 

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.NEW_TYPE;
  }

  @Override
  public String getObjectId(New _new)
  {
    return _new.getNewId();
  }

  @Override
  public String describe(New _new)
  {
    return describeText(_new.getHeadline());
  }
    
  public String describeText(String text)
  {
    if (text == null)
      return "";
    
    String description = text.substring(0, Math.min(text.length(), 80));

    if (text.length() > 80)
      description += "...";
    
    return description;
  }  

  @Override
  public New loadObject(String objectId)
  {
    try
    {
      return NewsModuleBean.getPort(false).loadNew(objectId);
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  @Override
  public String getTypeId(New _new)
  {
    return getRootTypeId();
  }

  @Override
  public ObjectSetup createObjectSetup()
  {
    ObjectSetup objectSetup = new ObjectSetup();
    objectSetup.setViewId("/pages/news/new.xhtml");

    List<EditTab> editTabs = new ArrayList<>();
    editTabs.add(new EditTab(BUNDLE_PREFIX + "tab_main", "pi pi-megaphone",
      "/pages/news/new_main.xhtml"));
    editTabs.add(new EditTab(BUNDLE_PREFIX + "tab_sections", "pi pi-hashtag",
      "/pages/news/new_sections.xhtml", "newSectionsTabBean"));
    editTabs.add(new EditTab(BUNDLE_PREFIX + "tab_documents", "pi pi-file-o", 
      "/pages/news/new_documents.xhtml", 
      "newDocumentsTabBean", "docs1",
      "/pages/news/new_documents_dialog.xhtml"));  
    objectSetup.setEditTabs(editTabs);
    
    return objectSetup;
  }

  @Override
  public NewsFilter queryToFilter(String query, String typeId)
  {
    NewsFilter filter = new NewsFilter();
    if (!StringUtils.isBlank(query))
    {
      filter.setContent(query);
    }
      
    return filter;
  }

  @Override
  public String filterToQuery(NewsFilter filter)
  {
    String value = null;
    if (filter.getContent() != null)
      value = filter.getContent();

    return value;
  }

  @Override
  public List<New> find(NewsFilter filter)
  {
    try
    {
      filter.setContent(setWildcards(filter.getContent())); 
      filter.setMaxResults(50);
      return NewsModuleBean.getPort(true).findNews(filter);
    }
    catch (Exception ex)
    {
      return Collections.EMPTY_LIST;
    }
  }
  
  private String setWildcards(String text)
  {
    if (text != null && !text.startsWith("\"") && !text.endsWith("\""))
      text = "%" + text.replaceAll("^%|%$", "") + "%" ;
    else if (text != null && text.startsWith("\"") && text.endsWith("\""))
      text = text.replaceAll("^\"|\"$", "");
    return text;
  } 
      
}
