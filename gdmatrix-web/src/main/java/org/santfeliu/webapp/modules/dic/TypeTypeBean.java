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
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.DictionaryConstants;
import static org.matrix.dic.DictionaryConstants.TYPE_PATH_SEPARATOR;
import static org.matrix.dic.DictionaryConstants.TYPE_TYPE;
import org.matrix.dic.Type;
import org.matrix.dic.TypeFilter;
import org.matrix.security.SecurityConstants;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.webapp.NavigatorBean;
import org.santfeliu.webapp.TypeBean;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.setup.ObjectSetup;
import org.santfeliu.webapp.util.WebUtils;


/**
 *
 * @author blanquepa
 */
@Named
@ApplicationScoped
public class TypeTypeBean extends TypeBean<Type, TypeFilter>
{
  private static final String BUNDLE_PREFIX = "$$dicBundle.";

  private List<SelectItem> rootTypeIdSelectItems;

  @PostConstruct
  public void init()
  {
    rootTypeIdSelectItems = new ArrayList<>();
    ArrayList<String> rootTypeIds = new ArrayList<>();
    rootTypeIds.addAll(DictionaryConstants.rootTypeIds);
    Collections.sort(rootTypeIds);

    for (String typeId : rootTypeIds)
    {
      SelectItem selectItem = new SelectItem();
      selectItem.setLabel(typeId);
      selectItem.setValue(typeId);
      rootTypeIdSelectItems.add(selectItem);
    }
  }

  public List<SelectItem> getRootTypeIdSelectItems()
  {
    return rootTypeIdSelectItems;
  }

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
    if (isPublicType(type.getTypeId()))
    {
      ObjectSetup objectSetup = getObjectSetup(type.getTypeId());
      if (objectSetup != null)
      {
        String publicTypeSymbol = 
          objectSetup.getProperties().getString("publicTypeSymbol");
        if (publicTypeSymbol != null)
        {
          return type.getDescription() + " " + publicTypeSymbol;
        }
      }
    }
    return type.getDescription();
  }

  @Override
  public Type loadObject(String objectId)
  {
    return TypeCache.getInstance().getType(objectId);
  }

  @Override
  public String getTypeId(Type type)
  {
    return DictionaryConstants.TYPE_TYPE;
  }

  @Override
  public ObjectSetup createObjectSetup()
  {
    ObjectSetup objectSetup = new ObjectSetup();
    objectSetup.setViewId("/pages/dic/type.xhtml");

    List<EditTab> editTabs = new ArrayList<>();
    editTabs.add(new EditTab(BUNDLE_PREFIX + "tab_main",
      "/pages/dic/type_main.xhtml"));
    editTabs.add(new EditTab(BUNDLE_PREFIX + "tab_properties",
      "/pages/dic/type_properties.xhtml", "typePropertiesTabBean"));
    editTabs.add(new EditTab(BUNDLE_PREFIX + "tab_acl",
      "/pages/dic/type_acl.xhtml", "typeACLTabBean"));
    objectSetup.setEditTabs(editTabs);

    return objectSetup;
  }

  @Override
  public TypeFilter queryToFilter(String query, String baseTypeId)
  {
    TypeFilter filter = new TypeFilter();
    String typePath = null;

    if (!StringUtils.isBlank(baseTypeId))
    {
      typePath = TYPE_PATH_SEPARATOR + baseTypeId + TYPE_PATH_SEPARATOR + "%";
      org.santfeliu.dic.Type baseType =
        TypeCache.getInstance().getType(baseTypeId);
      if (baseType != null && !baseType.isRootType())
        typePath = "%" + typePath;
    }

    // TODO: more intelligent search
    if (query != null && query.contains(":"))
    {
      filter.setTypeId(query);
    }
    else
      filter.setDescription(query);

    filter.setTypePath(typePath);

    return filter;
  }

  @Override
  public String filterToQuery(TypeFilter filter)
  {
    String query = "";

    if (!StringUtils.isBlank(filter.getTypeId()))
    {
      query = filter.getTypeId();
    }
    else if (filter.getDescription() != null)
    {
      query = filter.getDescription();
    }
    return query;
  }

  @Override
  public List<Type> find(TypeFilter filter)
  {
    List<Type> types = Collections.EMPTY_LIST;
    try
    {
      String typeId = filter.getTypeId();
      if (typeId != null)
      {
        Type type = TypeCache.getInstance().getType(typeId);
        if (type != null)
        {
          types = Collections.singletonList(type);
        }
      }
      else
      {
        types = DicModuleBean.getPort(true).findTypes(filter);
      }
    }
    catch (Exception ex)
    {
      // return empty list
    }
    return types;
  }

  @Override
  public List<SelectItem> getSelectItems(String query, String typeId,
    boolean addNavigatorItems, boolean sorted)
  {
    List<SelectItem> items = new ArrayList<>();
    
    if (StringUtils.isBlank(query) && addNavigatorItems)
    {
      if (typeId == null) typeId = getRootTypeId();
      addNavigatorItems(items, typeId);
    }
    else if (StringUtils.isBlank(query))
    {
      org.santfeliu.dic.Type type = TypeCache.getInstance().getType(typeId);
      for (org.santfeliu.dic.Type derived : type.getDerivedTypes(true))
      {
        String objectId = derived.getTypeId();
        String description = getDescription(objectId) +
          " (" + objectId + ")";
        items.add(new SelectItem(objectId, description));
      }
    }    
    else
    {
      List<Type> types = findByQuery(query, typeId);
      for (Type type : types)
      {
        String objectId = getObjectId(type);
        String description = getDescription(type.getTypeId()) +
          " (" + type.getTypeId() + ")";
        items.add(new SelectItem(objectId, description));
      }
    }

    if (sorted)
    {
      sortSelectItems(items);
    }

    return items;
  }

  @Override
  protected void addNavigatorItems(List<SelectItem> items, String typeId)
  {
    NavigatorBean navigatorBean = WebUtils.getBean("navigatorBean");
    NavigatorBean.BaseTypeInfo baseTypeInfo =
      navigatorBean.getBaseTypeInfo(TYPE_TYPE);

    if (baseTypeInfo != null)
    {
      List<String> ids = new ArrayList(baseTypeInfo.getRecentObjectIdList());
      List<String> favIds = baseTypeInfo.getFavoriteObjectIdList();
      if (!favIds.isEmpty())
      {
        favIds.stream().filter(id -> !ids.contains(id))
          .forEach(id -> ids.add(id));
      }

      for (String id : ids)
      {
        if (!StringUtils.isBlank(id))
        {
          org.santfeliu.dic.Type type = TypeCache.getInstance().getType(id);
          if (type.isDerivedFrom(typeId) || id.equals(typeId))
          {
            items.add(new SelectItem(id, getDescription(id)));
          }
        }
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

  private boolean isPublicType(String typeId)
  {
    if (!StringUtils.isBlank(typeId))
    {
      org.santfeliu.dic.Type type = TypeCache.getInstance().getType(typeId);
      if (type != null)
      {
        return type.canPerformAction(DictionaryConstants.READ_ACTION,
            Collections.singleton(SecurityConstants.EVERYONE_ROLE));
      }
    }
    return false;
  }

  private ObjectSetup getObjectSetup(String typeId)
  {
    NavigatorBean navigatorBean = WebUtils.getBean("navigatorBean");
    NavigatorBean.BaseTypeInfo baseTypeInfo = 
      navigatorBean.getBaseTypeInfo(typeId);
    if (baseTypeInfo != null)
    {
      try
      {
        return baseTypeInfo.getObjectSetup();
      }
      catch (Exception ex) { }
    }
    return null;    
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
