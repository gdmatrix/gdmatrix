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

import java.util.Comparator;
import java.util.List;
import org.santfeliu.webapp.setup.TableProperty;
import org.santfeliu.webapp.util.DataTableRow.CustomProperty;
import org.santfeliu.webapp.util.DataTableRow.Value;

/**
 *
 * @author lopezrj-sf
 */
public class DataTableRowComparator implements Comparator<DataTableRow>
{
  private final List<TableProperty> columns;
  private final List<String> orderBy;

  public DataTableRowComparator(List<TableProperty> columns,
    List<String> orderBy)
  {
    this.columns = columns;
    this.orderBy = orderBy;
  }

  @Override
  public int compare(DataTableRow row1, DataTableRow row2)
  {
    for (String orderByItem : orderBy)
    {
      boolean desc = false;
      int compare = 0;
      if (orderByItem.endsWith(":desc"))
      {
        orderByItem = orderByItem.substring(0, orderByItem.length() - 5);
        desc = true;
      }
      else if (orderByItem.endsWith(":asc"))
      {
        orderByItem = orderByItem.substring(0, orderByItem.length() - 4);
      }
      Value val1 = row1.getValueByPropertyName(columns, orderByItem);
      Object sort1 = (val1 == null ? null : val1.getSorted());      
      Value val2 = row2.getValueByPropertyName(columns, orderByItem);
      Object sort2 = (val2 == null ? null : val2.getSorted());
      if (sort1 != null || sort2 != null)
      {
        if (sort1 == null && sort2 != null)
          return 1;
        if (sort1 != null && sort2 == null)
          return -1;
        else
        {
          if (sort1 instanceof Double && sort2 instanceof Double)
          {
            double d1 = (Double)sort1;
            double d2 = (Double)sort2;
            if (d1 != d2) compare = (d1 > d2 ? 1 : -1);
          }
          else
          {
            String s1 = String.valueOf(sort1).toLowerCase().trim();
            String s2 = String.valueOf(sort2).toLowerCase().trim();
            if (s1.isEmpty() || s2.isEmpty())
            {
              compare = s2.length() - s1.length(); //non-nulls first
            }
            else
            {
              compare = s1.compareTo(s2);
            }
          }
        }
      }
      if (compare != 0) return (desc ? -compare : compare);
    }
    return 0;
  }

}

