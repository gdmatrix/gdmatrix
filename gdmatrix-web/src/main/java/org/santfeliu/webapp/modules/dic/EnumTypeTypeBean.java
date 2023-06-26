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
package org.santfeliu.webapp.modules.dic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.EnumType;
import org.matrix.dic.EnumTypeFilter;
import org.santfeliu.webapp.TypeBean;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.setup.ObjectSetup;


/**
 *
 * @author blanquepa
 */
@Named
@ApplicationScoped
public class EnumTypeTypeBean extends TypeBean<EnumType, EnumTypeFilter>
{
  private static final String BUNDLE_PREFIX = "$$dicBundle.";
  
  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.ENUM_TYPE_TYPE;
  }

  @Override
  public String getObjectId(EnumType enumType)
  {
    return enumType.getEnumTypeId();
  }

  @Override
  public String describe(EnumType enumType)
  {
    return enumType.getName();
  }

  @Override
  public EnumType loadObject(String objectId)
  {
    try
    {
      return DicModuleBean.getPort(false).loadEnumType(objectId);
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  @Override
  public String getTypeId(EnumType enumType)
  {
    return DictionaryConstants.ENUM_TYPE_TYPE;
  }

  @Override
  public ObjectSetup createObjectSetup()
  {
    ObjectSetup objectSetup = new ObjectSetup();
    objectSetup.setViewId("/pages/dic/enum_type.xhtml");

    List<EditTab> editTabs = new ArrayList<>();
    editTabs.add(new EditTab(BUNDLE_PREFIX + "tab_main", 
      "/pages/dic/enum_type_main.xhtml"));
    editTabs.add(new EditTab(BUNDLE_PREFIX + "tab_items", 
      "/pages/dic/enum_type_items.xhtml", 
      "enumTypeItemsTabBean"));
    objectSetup.setEditTabs(editTabs);

    return objectSetup;
  }

  @Override
  public EnumTypeFilter queryToFilter(String query, String typeId)
  {
    EnumTypeFilter filter = new EnumTypeFilter();
    
    // TODO: more intelligent search
    if (query != null && query.contains(":"))
    {
      filter.getEnumTypeId().clear();
      filter.getEnumTypeId().add(query);
    }
    else if (!StringUtils.isBlank(query))
    {
      if (!query.startsWith("%")) query = "%" + query;
      if (!query.endsWith("%")) query += "%";
      filter.setName(query);
    }
    
    return filter;
  }

  @Override
  public String filterToQuery(EnumTypeFilter filter)
  {
    String query = "";

    if (filter.getEnumTypeId() != null && !filter.getEnumTypeId().isEmpty())
    {
      query = filter.getEnumTypeId().get(0);
    }    
    else if (filter.getName() != null)
    {
      query = filter.getName();
      if (query.startsWith("%")) query = query.substring(1);
      if (query.endsWith("%")) query = query.substring(0, query.length() - 1);
    }
    return query;
  }

  @Override
  public List<EnumType> find(EnumTypeFilter filter)
  {
    try
    {
      return DicModuleBean.getPort(true).findEnumTypes(filter);
    }
    catch (Exception ex)
    {
    }

    return Collections.emptyList();
  }

}
