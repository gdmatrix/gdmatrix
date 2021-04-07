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
package org.santfeliu.dbf;

import java.util.StringTokenizer;

/**
 *
 * @author realor
 */
public class DBKey
{
  Object values[];

  public DBKey(Object... values)
  {
    this.values = values;
  }

  public static DBKey fromString(String primaryKeyString)
  {
    StringTokenizer tokenizer = new StringTokenizer(primaryKeyString, ";");
    Object values[] = new Object[tokenizer.countTokens()];
    int i = 0;
    while (tokenizer.hasMoreTokens())
    {
      values[i++] = tokenizer.nextToken();
    }
    return new DBKey(values);
  }

  public Object getColumnValue(int index)
  {
    return values[index];
  }

  public int getColumnCount()
  {
    return values.length;
  }

  @Override
  public String toString()
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append(values[0]);
    for (int i = 1; i < values.length; i++)
    {
      buffer.append(";");
      buffer.append(values[i]);
    }
    return buffer.toString();
  }

  public static void main(String[] args)
  {
    DBKey key = DBKey.fromString("56;asd ;99;22");
    System.out.println(key);
  }
}
