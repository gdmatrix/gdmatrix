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
import javax.faces.model.SelectItem;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.DictionaryConstants;
import static org.matrix.dic.DictionaryConstants.TYPE_PATH_SEPARATOR;
import static org.matrix.dic.DictionaryConstants.TYPE_TYPE;
import org.matrix.dic.Type;
import org.matrix.dic.TypeFilter;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.webapp.NavigatorBean;
import org.santfeliu.webapp.TypeBean;
import org.santfeliu.webapp.util.WebUtils;


/**
 *
 * @author blanquepa
 */
@Named
@ApplicationScoped
public class TypeTypeBean extends TypeBean<Type, TypeFilter>
{
  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.TYPE_TYPE;
  }

  @Override
  public String getObjectId(Type type)
  {
    return type.getTypeId();
  }

  @Override
  public String describe(Type type)
  {
    return type.getDescription() + " (" + type.getTypeId() + ")";
  }

  @Override
  public Type loadObject(String objectId)
  {
    return TypeCache.getInstance().getType(objectId);
  }

  @Override
  public TypeFilter queryToFilter(String query, String typeId)
  {
    TypeFilter filter = new TypeFilter();

    // TODO: more intelligent search
    filter.setDescription(query);

    if (!StringUtils.isBlank(typeId))
    {
      String typePath = TYPE_PATH_SEPARATOR + typeId + TYPE_PATH_SEPARATOR + "%";
      org.santfeliu.dic.Type type = TypeCache.getInstance().getType(typeId);
      if (type != null && !type.isRootType())
      {
        typePath = "%" + typePath;
      }
      filter.setTypePath(typePath);
    }

    return filter;
  }

  @Override
  public String filterToQuery(TypeFilter filter)
  {
    String query = "";

    if (filter.getDescription() != null)
    {
      query = filter.getDescription();
      if (query.startsWith("%")) query = query.substring(1);
      if (query.endsWith("%")) query = query.substring(query.length() - 1);
    }
    return query;
  }

  @Override
  public List<Type> find(TypeFilter filter)
  {
    List<Type> types;
    try
    {
      String typeId = filter.getTypeId();
      if (typeId != null)
      {
        types = Collections.singletonList(
          TypeCache.getInstance().getType(filter.getTypeId()));
      }
      else
      {
        types = DicModuleBean.getPort(true).findTypes(filter);
        System.out.println("typeId: " + filter.getTypeId());
        System.out.println("desc: " + filter.getDescription());
        System.out.println("typePath: " + filter.getTypePath());
        System.out.println("superTypeId: " + filter.getSuperTypeId());
        System.out.println("Total: " + types.size());
      }
    }
    catch (Exception ex)
    {
      return Collections.EMPTY_LIST;
    }
    return types;
  }

  @Override
  protected void addNavigatorItems(List<SelectItem> items, String typeId)
  {
    NavigatorBean navigatorBean = WebUtils.getBean("navigatorBean");
    NavigatorBean.BaseTypeInfo baseTypeInfo =
      navigatorBean.getBaseTypeInfo(TYPE_TYPE);

    if (baseTypeInfo != null)
    {
      List<String> ids = new ArrayList();
      ids.addAll(baseTypeInfo.getRecentObjectIdList());
      List<String> favIds = baseTypeInfo.getFavoriteObjectIdList();
      if (!favIds.isEmpty())
      {
        // TODO: only add descendant Types from typeId
        favIds.stream().filter(id -> !ids.contains(id))
          .forEach(id -> ids.add(id));
      }
      if (!ids.isEmpty())
      {
        // TODO: only add descendant Types from typeId
        ids.stream().filter(id -> !StringUtils.isBlank(id))
          .forEach(id -> items.add(new SelectItem(id, getDescription(id))));
      }
    }
  }

  protected List<Type> getFromTypeCache(String superTypeId, int maxResults)
  {
    List<Type> results = new ArrayList();
    org.santfeliu.dic.Type type = TypeCache.getInstance().getType(superTypeId);
    if (type != null)
    {
      if (type.isInstantiable())
        results.add(type);

      List<org.santfeliu.dic.Type> derived = type.getDerivedTypes();
      if (maxResults == 0)
        maxResults = derived.size();

      for (int i = 0; i < maxResults; i++)
      {
        Type child = derived.get(i);
        if (child.isInstantiable())
          results.add(derived.get(i));
      }
    }
    return results;
  }

  public static void main(String[] args)
  {
    TypeTypeBean bean = new TypeTypeBean();
    String query = "sf:Familia";
    List<Type> list = bean.findByQuery(query);
    for (Type item : list)
    {
      System.out.println(item.getDescription());
    }
  }

}