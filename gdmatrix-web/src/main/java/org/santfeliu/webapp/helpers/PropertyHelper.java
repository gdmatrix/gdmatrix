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
  public abstract List<Property> getProperties();

  final Map<String, Object> value = new AbstractMap<String, Object>()
  {
    @Override
    public Object get(Object key)
    {
      List<Property> properties = getProperties();
      String name = String.valueOf(key);
      Property property = DictionaryUtils.getPropertyByName(properties, name);
      if (property == null || property.getValue().isEmpty()) return null;

      return property.getValue().get(0);
    }

    @Override
    public Object put(String name, Object value)
    {
      List<Property> properties = getProperties();
      Property property = DictionaryUtils.getPropertyByName(properties, name);
      if (value == null || isEmptyString(value))
      {
        if (property != null)
        {
          properties.remove(property);
        }
      }
      else
      {
        if (property == null)
        {
          property = new Property();
          property.setName(name);
          properties.add(property);
        }
        else
        {
          property.getValue().clear();
        }
        property.getValue().add(String.valueOf(value));
      }

      return null;
    }

    @Override
    public int size()
    {
      return getProperties().size();
    }

    @Override
    public Set<Map.Entry<String, Object>> entrySet()
    {
      throw new UnsupportedOperationException();
    }
  };

  Map<String, List<? extends Object>> values =
    new AbstractMap<String, List<? extends Object>>()
  {
    @Override
    public List<? extends Object> get(Object key)
    {
      List<Property> properties = getProperties();
      String name = String.valueOf(key);
      Property property = DictionaryUtils.getPropertyByName(properties, name);
      if (property == null || property.getName().isEmpty()) return null;

      return property.getValue();
    }

    @Override
    public List<? extends Object> put(String name,
      List<? extends Object> values)
    {
      List<Property> properties = getProperties();
      Property property = DictionaryUtils.getPropertyByName(properties, name);
      if (values == null || values.isEmpty())
      {
        if (property != null)
        {
          properties.remove(property);
        }
      }
      else
      {
        if (property == null)
        {
          property = new Property();
          property.setName(name);
          properties.add(property);
        }
        else
        {
          property.getValue().clear();
        }
        for (Object value : values)
        {
          if (value != null && !isEmptyString(value))
          {
            property.getValue().add(String.valueOf(value));
          }
        }
      }
      return null;
    }

    @Override
    public int size()
    {
      return getProperties().size();
    }

    @Override
    public Set<Map.Entry<String, List<? extends Object>>> entrySet()
    {
      throw new UnsupportedOperationException();
    }
  };

  public Map<String, Object> getValue()
  {
    return value;
  }

  public Map<String, List<? extends Object>> getValues()
  {
    return values;
  }

  private boolean isEmptyString(Object value)
  {
    return value instanceof String && ((String)value).trim().length() == 0;
  }
}
