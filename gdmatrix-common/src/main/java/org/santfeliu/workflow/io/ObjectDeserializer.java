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
package org.santfeliu.workflow.io;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Base64;
import org.santfeliu.util.Properties;

/**
 *
 * @author realor
 */
public class ObjectDeserializer
{
  public ObjectDeserializer()
  {
  }

  public Object deserialize(String str, Class valueClass)
    throws Exception
  {
    Object value = null;
    if (valueClass == String.class)
    {
      value = str;
    }
    else if (str == null)
    {
      value = null;
    }
    else if (str.trim().length() == 0)
    {
      value = null;
    }
    else if (valueClass == Double.class ||
             valueClass == double.class)
    {
      value = new Double(str);
    }
    else if (valueClass == Float.class ||
             valueClass == float.class)
    {
      value = new Float(str);
    }
    else if (valueClass == Integer.class ||
             valueClass == int.class)
    {
      value = new Integer(str);
    }
    else if (valueClass == Long.class ||
             valueClass == long.class)
    {
      value = new Long(str);
    }
    else if (valueClass == Short.class ||
             valueClass == short.class)
    {
      value = new Short(str);
    }
    else if (valueClass == Boolean.class ||
             valueClass == boolean.class)
    {
      value = new Boolean(str);
    }
    else if (valueClass == org.santfeliu.util.Properties.class)
    {
      Properties properties = new Properties();
      properties.loadFromString(str);
      value = properties;
    }
    else if (str.startsWith(ObjectSerializer.JAVA_SER_B64))
    {
      str = str.substring(ObjectSerializer.JAVA_SER_B64.length());
      byte[] data = Base64.getMimeDecoder().decode(str);

      ByteArrayInputStream bis = new ByteArrayInputStream(data);
      try (ObjectInputStream is = new ObjectInputStream(bis))
      {
        value = is.readObject();
      }
    }
    else
    {
      throw new Exception("Invalid property class");
    }
    return value;
  }
}
