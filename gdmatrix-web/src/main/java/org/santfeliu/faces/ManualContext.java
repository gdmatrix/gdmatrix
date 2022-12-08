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
package org.santfeliu.faces;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import javax.enterprise.context.spi.AlterableContext;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.faces.context.FacesContext;

/**
 *
 * @author realor
 */
public class ManualContext implements AlterableContext, Serializable
{
  public static final String SCOPE_MAP_KEY = ":ManualScopeMap:";

  @Override
  public Class<? extends Annotation> getScope()
  {
    return ManualScoped.class;
  }

  @Override
  public <T> T get(Contextual<T> contextual)
  {
    Bean bean = (Bean)contextual;
    Map<String, T> map = getMap();
    String key = getKey(bean);
    return map.get(key);
  }

  @Override
  public <T> T get(Contextual<T> contextual, CreationalContext<T> context)
  {
    Bean<T> bean = (Bean<T>)contextual;
    Map<String, T> map = getMap();
    String key = getKey(bean);
    T instance = map.get(key);
    if (instance == null)
    {
      instance = bean.create(context);
      map.put(key, instance);
    }
    return instance;
  }

  @Override
  public void destroy(Contextual<?> contextual)
  {
    Bean bean = (Bean)contextual;
    Map<String, Object> map = getMap();
    String key = getKey(bean);
    map.remove(key);
  }

  public void destroy(Predicate<Object> predicate)
  {
    Map<String, Object> map = getMap();
    List<Map.Entry<String, Object>> entries = new ArrayList<>(map.entrySet());
    entries.forEach(entry ->
    {
      String key = entry.getKey();
      Object instance = entry.getValue();
      if (predicate.test(instance))
      {
        map.remove(key);
      }
    });
  }

  @Override
  public boolean isActive()
  {
    return true;
  }

  private String getKey(Bean bean)
  {
    return bean.getBeanClass().getName();
  }

  private <T> Map<String, T> getMap()
  {
    Map<String, T> map;

    FacesContext fc = FacesContext.getCurrentInstance();
    Map<String, Object> sessionMap = fc.getExternalContext().getSessionMap();
    map = (Map<String, T>)sessionMap.get(SCOPE_MAP_KEY);
    if (map == null)
    {
      map = new HashMap<>();
      sessionMap.put(SCOPE_MAP_KEY, map);
    }
    return map;
  }
}
