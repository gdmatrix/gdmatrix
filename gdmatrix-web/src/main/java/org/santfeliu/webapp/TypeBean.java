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

import java.util.HashMap;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.web.WebBean;

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

  public static TypeBean getInstance(String typeId)
  {
    TypeBean typeBean = instances.get(typeId);
    if (typeBean == null)
    {
      Type type = TypeCache.getInstance().getType(typeId);
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
  }

  public abstract String getRootTypeId();

  public abstract String describe(T object);

  public abstract T loadObject(String objectId);

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

  public abstract F queryToFilter(String query, String typeId);

  public abstract String filterToQuery(F filter);

  public abstract List<T> find(F filter);

  public List<T> find(String query)
  {
    return find(queryToFilter(query, getRootTypeId()));
  }

  public List<T> find(String query, String typeId)
  {
    return find(queryToFilter(query, typeId));
  }

}
