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
package org.santfeliu.faces.widget;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author realor
 */
public class WidgetLayout
{
  private List<List<String>> widgetIds = new ArrayList();

  public WidgetLayout(String layout)
  {
    read(layout);
  }

  public WidgetLayout(int columns)
  {
    dim(columns);
  }

  public WidgetLayout(int columns, String layout)
  {
    dim(columns);
    read(layout);
  }

  public int getColumns()
  {
    return widgetIds.size();
  }

  public List<String> getWidgetIds(int column)
  {
    return column < widgetIds.size() ? widgetIds.get(column) : null;
  }

  public List<String> getWidgetIds()
  {
    ArrayList<String> list = new ArrayList();
    for (List<String> columnWidgetIds : widgetIds)
    {
      list.addAll(columnWidgetIds);
    }
    return list;
  }

  public int getColumn(String widgetId)
  {
    int column = -1;
    for (int col = 0; col < widgetIds.size(); col++)
    {
      if (widgetIds.get(col).contains(widgetId)) column = col;
    }
    return column;
  }

  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < widgetIds.size(); i++)
    {
      if (i > 0) builder.append("|");
      for (int j = 0; j < widgetIds.get(i).size(); j++)
      {
        if (j > 0) builder.append(",");
        builder.append(widgetIds.get(i).get(j));
      }
    }
    return builder.toString();
  }

  private void read(String layout)
  {
    if (layout == null) return;
    String columnArray[] = layout.split("\\|");
    if (widgetIds.isEmpty()) dim(columnArray.length);
    
    int column = 0;
    while (column < columnArray.length && column < widgetIds.size())
    {
      List<String> columnWidgetIds = widgetIds.get(column);
      String ids[] = columnArray[column].split(",");
      for (String id : ids)
      {
        id = id.trim();
        if (id.length() > 0)
        {
          columnWidgetIds.add(id);
        }
      }
      column++;
    }
  }

  private void dim(int columns)
  {
    for (int i = 0; i < columns; i++)
    {
      widgetIds.add(new ArrayList<String>());
    }
  }

  public static void main(String args[])
  {
    WidgetLayout layout = new WidgetLayout("a ,   b | c , d |  p");
    layout.getWidgetIds(0).add("pepe");
    layout.getWidgetIds(1).add("pepe1");
    layout.getWidgetIds(1).add("pepe2");
    System.out.println(layout);
    System.out.println(layout.getWidgetIds());
    System.out.println(">>" + layout.getColumn("p"));
  }
}
