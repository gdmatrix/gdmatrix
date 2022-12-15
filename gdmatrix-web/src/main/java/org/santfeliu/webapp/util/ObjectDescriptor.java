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
package org.santfeliu.webapp.util;

import java.util.HashMap;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;

/**
 *
 * @author realor
 * @param <T> the object type to describe
 */
public abstract class ObjectDescriptor<T>
{
  static HashMap<String, ObjectDescriptor> instances = new HashMap<>();

  private final HashMap<String, String> descriptions = new HashMap<>();

  public static synchronized ObjectDescriptor getInstance(String typeId)
  {
    ObjectDescriptor descriptor = instances.get(typeId);
    if (descriptor == null)
    {
      Type type = TypeCache.getInstance().getType(typeId);
      String rootTypeId = type.getRootTypeId();
      descriptor = instances.get(rootTypeId);
      if (descriptor == null)
      {
        descriptor = new DefaultObjectDescriptor(rootTypeId);
        instances.put(rootTypeId, descriptor);
      }
    }
    return descriptor;
  }

  public static synchronized void register(ObjectDescriptor descriptor)
  {
    instances.put(descriptor.getRootTypeId(), descriptor);
  }

  public static synchronized void unregister(ObjectDescriptor descriptor)
  {
    instances.remove(descriptor.getRootTypeId());
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

  public synchronized void clear()
  {
    descriptions.clear();
  }
}
