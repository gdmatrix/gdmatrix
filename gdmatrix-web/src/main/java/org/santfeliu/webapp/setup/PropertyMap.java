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
package org.santfeliu.webapp.setup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author realor
 */
public class PropertyMap extends HashMap<String, Object>
{
  public boolean getBoolean(String name)
  {
    Object value = get(name);
    if (value == null) return false;
    if (value instanceof Boolean) return ((Boolean) value);
    return Boolean.valueOf(value.toString());
  }

  public int getInteger(String name)
  {
    Object value = get(name);
    if (value instanceof Number)
    {
      return ((Number) value).intValue();
    }
    return 0;
  }

  public String getString(String name)
  {
    Object value = get(name);
    if (value == null) return null;
    return value.toString();
  }
  
  public List<String> getList(String name)
  {
    Object value = get(name);
    if (value == null) return null;
    if (value instanceof List)
    {
      List<String> result = new ArrayList();
      for (Object item : (List)value)
      {
        result.add(item.toString());
      }
      return result;
    }
    else
    {
      return Arrays.asList(value.toString().split(",")); 
    }
  }

}
