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
package org.santfeliu.ws.ser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author unknown
 */
public class MapSerializer
{
  public static byte[] toByteArray(Map map)
  {
    try
    {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream os = new ObjectOutputStream(bos);
      os.writeObject(map);
      os.close();
      return bos.toByteArray();
    }
    catch (Exception ex)
    {
      return new byte[0]; // empty array
    }
  }

  public static Map toMap(byte[] byteArray)
  {
    Map map = null;
    try
    {
      ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
      ObjectInputStream ois = new ObjectInputStream(bis);
      map = (Map)ois.readObject();
      ois.close();
    }
    catch (Exception ex)
    {
      map = new HashMap(); // empty hashmap
    }
    return map;
  }

  public static void main(String[] args)
  {
    Map map = new HashMap();

    map.put("name", "xxxxxx");
    map.put("surname", "yyyyyyy");
    map.put("nif", "NNNNNNNNX");
    map.put("size", 34.9);

    byte[] a = MapSerializer.toByteArray(map);
    map = MapSerializer.toMap(a);

    System.out.println(map);
  }
}
