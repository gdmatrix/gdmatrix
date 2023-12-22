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
package org.santfeliu.webapp.modules.translation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import org.matrix.dic.DictionaryConstants;
import org.matrix.translation.Translation;
import org.matrix.translation.TranslationFilter;
import org.santfeliu.webapp.TypeBean;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.setup.ObjectSetup;
import static org.santfeliu.webapp.modules.translation.TranslationModuleBean.getPort;
import org.santfeliu.webapp.setup.SearchTab;

/**
 *
 * @author blanquepa
 */
@Named
@ApplicationScoped
public class TranslationTypeBean extends TypeBean<Translation, TranslationFilter>
{
  private static final String BUNDLE_PREFIX = "$$translationBundle.";

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.TRANSLATION_TYPE;
  }

  @Override
  public String getObjectId(Translation translation)
  {
    return translation.getTransId();
  }

  @Override
  public String describe(Translation translation)
  {
    return translation.getLanguage().toUpperCase() 
      + " " 
      + describeText(translation.getText());
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
  public Translation loadObject(String objectId)
  {
    try
    {
      return getPort(true).loadTranslation(objectId);
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  @Override
  public String getTypeId(Translation translation)
  {
    return getRootTypeId();
  }

  @Override
  public ObjectSetup createObjectSetup()
  {
    ObjectSetup objectSetup = new ObjectSetup();
    objectSetup.setViewId("/pages/translation/translation.xhtml");

    List<SearchTab> searchTabs = new ArrayList();
    SearchTab searchTab =
      new SearchTab("List", "/pages/translation/translation_list.xhtml");

    searchTabs.add(searchTab);

    objectSetup.setSearchTabs(searchTabs);

    List<EditTab> editTabs = new ArrayList<>();
    editTabs.add(new EditTab(BUNDLE_PREFIX + "tab_main", "/pages/translation/translation_main.xhtml"));
    objectSetup.setEditTabs(editTabs);

    return objectSetup;
  }

  @Override
  public TranslationFilter queryToFilter(String query, String typeId)
  {
    if (query == null) query = "";

    TranslationFilter filter = new TranslationFilter();

    if (!query.startsWith("%")) query = "%" + query;
    if (!query.endsWith("%")) query += "%";      
    filter.setText(query);

    filter.setMaxResults(10);

    return filter;
  }

  @Override
  public String filterToQuery(TranslationFilter filter)
  {
    String query = filter.getText();
    if (query.startsWith("%")) query = query.substring(1);
    if (query.endsWith("%")) query = query.substring(0, query.length() - 1);
    return query;
  }

  @Override
  public List<Translation> find(TranslationFilter filter)
  {
    try
    {
      return getPort(true).findTranslations(filter);
    }
    catch (Exception ex)
    {
      return Collections.EMPTY_LIST;
    }
  }

  public String getValue(Object object)
  {
    return String.valueOf(object);
  }
  
}
