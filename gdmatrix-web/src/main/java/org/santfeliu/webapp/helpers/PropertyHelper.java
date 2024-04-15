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
package org.santfeliu.webapp.helpers;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.matrix.dic.Property;
import org.santfeliu.dic.util.DictionaryUtils;

/**
 * PropertyHelper
 *
 * A helper to manage dynamic properties of an object.
 *
 * Usage:
 * #{caseMainTabBean.propertyHelper.value.age}
 * #{caseMainTabBean.propertyHelper.value.sendDate}
 * #{caseMainTabBean.propertyHelper.values.languages}
 * #{caseMainTabBean.propertyHelper.values.items}
 *
 * @author realor
 */

public abstract class PropertyHelper
{
  public static final String OBJECT_KEY = "_object";

  public Object getObject()
  {
    return null;
  }

  public List<Property> getProperties()
  {
    Object object = getObject();
    return object == null ? null : DictionaryUtils.getDynamicProperties(object);
  }

  private final PropertyMap value = new PropertyMap(false);
  private final PropertyMap values = new PropertyMap(true);

  public class PropertyMap extends AbstractMap<String, Object>
  {
    boolean multiValued;

    PropertyMap(boolean multiValued)
    {
      this.multiValued = multiValued;
    }

    @Override
    public Object get(Object key)
    {
      if (OBJECT_KEY.equals(key))
      {
        return getObject();
      }

      String propertyName = key == null ? null : key.toString();
      List<Property> properties = getProperties();
      if (properties == null) return null;

      Property property =
        DictionaryUtils.getPropertyByName(properties, propertyName);
      if (property != null)
      {
        List<String> value = property.getValue();
        if (value == null || value.isEmpty()) return null;
        return multiValued ? value : value.get(0);
      }
      else
      {
        return multiValued ? Collections.EMPTY_LIST : null;
      }
    }

    @Override
    public Object put(String propertyName, Object value)
    {
      List<Property> properties = getProperties();
      if (properties != null)
      {
        DictionaryUtils.setPropertyValue(properties, propertyName, value, false);
      }
      return null;
    }

    @Override
    public int size()
    {
      int size = 0;

      List<Property> properties = getProperties();
      if (properties != null) size += properties.size();

      Object object = getObject();
      if (object != null) size++;
      return size;
    }

    @Override
    public Set<Map.Entry<String, Object>> entrySet()
    {
      List<Property> properties = getProperties();
      if (properties == null) return Collections.EMPTY_SET;

      Map<String, Object> map = new HashMap<>();
      for (Property property : properties)
      {
        List<String> valueList = property.getValue();
        if (!valueList.isEmpty())
        {
          if (multiValued)
          {
            map.put(property.getName(), property.getValue());
          }
          else
          {
            map.put(property.getName(), property.getValue().get(0));
          }
        }
      }
      Object object = getObject();
      if (object != null)
      {
        map.put(OBJECT_KEY, object);
      }
      return Collections.unmodifiableSet(map.entrySet());
    }
  };

  public Map<String, Object> getValue()
  {
    return value;
  }

  public Map<String, Object> getValues()
  {
    return values;
  }
}
