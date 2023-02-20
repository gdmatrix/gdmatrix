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
package org.santfeliu.webapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.faces.model.SelectItem;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.web.WebBean;
import org.santfeliu.webapp.setup.ObjectSetup;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author realor
 * @param <T> the type managed by this bean
 * @param <F> the filter to find T objects
 */
public abstract class TypeBean<T, F> extends WebBean
{
  static final HashMap<String, TypeBean> instances = new HashMap<>();

  private final HashMap<String, String> descriptions = new HashMap<>();

  private ObjectSetup objectSetup;

  public static TypeBean getInstance(String typeId)
  {
    TypeBean typeBean = instances.get(typeId);
    if (typeBean == null)
    {
      Type type = TypeCache.getInstance().getType(typeId);
      if (type == null) return null;
      String rootTypeId = type.getRootTypeId();
      typeBean = instances.get(rootTypeId);
    }
    return typeBean;
  }

  public static void register(TypeBean typeBean)
  {
    instances.put(typeBean.getRootTypeId(), typeBean);
  }

  public static void unregister(TypeBean typeBean)
  {
    instances.remove(typeBean.getRootTypeId());
  }

  public void init(@Observes @Initialized(ApplicationScoped.class) Object init)
  {
    TypeBean.register(this);
    objectSetup = createObjectSetup();
  }

  public abstract String getRootTypeId();

  public abstract String getObjectId(T object);

  public abstract String getTypeId(T object);

  public abstract String describe(T object);

  public abstract T loadObject(String objectId);

  public String getViewId()
  {
    return objectSetup.getViewId();
  }

  public ObjectSetup getObjectSetup()
  {
    return objectSetup;
  }

  public synchronized String getDescription(String objectId)
  {
    String description = descriptions.get(objectId);
    if (description == null)
    {
      T object = loadObject(objectId);
      if (object == null)
      {
        description = getRootTypeId() + " " + objectId;
      }
      else
      {
        description = describe(object);
        if (description != null)
        {
          descriptions.put(objectId, description);
        }
      }
    }
    return description;
  }

  public synchronized void updateDescription(String objectId, T object)
  {
    if (object == null) return;

    String description = describe(object);
    if (description != null)
    {
      descriptions.put(objectId, description);
    }
  }

  public synchronized void clearDescriptions()
  {
    descriptions.clear();
  }

  public abstract ObjectSetup createObjectSetup();

  public abstract F queryToFilter(String query, String typeId);

  public abstract String filterToQuery(F filter);

  public abstract List<T> find(F filter);

  public List<T> findByQuery(String query)
  {
    return find(queryToFilter(query, getRootTypeId()));
  }

  public List<T> findByQuery(String query, String typeId)
  {
    return find(queryToFilter(query, typeId));
  }

  public List<SelectItem> getSelectItems()
  {
    return getSelectItems("", getRootTypeId(), false, true);
  }

  public List<SelectItem> getSelectItems(String typeId)
  {
    return getSelectItems("", typeId, false, true);
  }

  public List<SelectItem> getSelectItems(String query, String typeId,
    boolean addNavigatorItems, boolean sorted)
  {
    List<SelectItem> items = new ArrayList<>();

    if (StringUtils.isBlank(query) && addNavigatorItems)
    {
      if (typeId == null) typeId = getRootTypeId();
      addNavigatorItems(items, typeId);
    }
    else
    {
      List<T> objects = findByQuery(query, typeId);
      for (T object : objects)
      {
        String objectId = getObjectId(object);
        String description = describe(object);
        items.add(new SelectItem(objectId, description));
      }
    }

    if (sorted)
    {
      sortSelectItems(items);
    }

    return items;
  }

  protected void addNavigatorItems(List<SelectItem> items, String typeId)
  {
    NavigatorBean navigatorBean = WebUtils.getBean("navigatorBean");
    NavigatorBean.BaseTypeInfo baseTypeInfo =
      navigatorBean.getBaseTypeInfo(typeId);

    if (baseTypeInfo != null)
    {
      List<String> ids = new ArrayList();
      ids.addAll(baseTypeInfo.getRecentObjectIdList());
      List<String> favIds = baseTypeInfo.getFavoriteObjectIdList();
      if (!favIds.isEmpty())
      {
        favIds.stream().filter(id -> !ids.contains(id))
          .forEach(id -> ids.add(id));
      }
      if (!ids.isEmpty())
      {
        ids.stream().filter(id -> !StringUtils.isBlank(id))
          .forEach(id -> items.add(new SelectItem(id, getDescription(id))));
      }
    }
  }

  protected void sortSelectItems(List<SelectItem> items)
  {
    Collections.sort(items, (SelectItem i1, SelectItem i2) ->
    {
      if (i1 != null && i2 != null)
        return i1.getLabel().compareTo(i2.getLabel());
      else if (i1 == null)
        return 1;
      else
        return -1;
    });
  }

}
