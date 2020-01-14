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
package org.santfeliu.sql;

import java.util.List;
import java.util.Set;

import org.matrix.sql.QueryRow;
import org.matrix.sql.QueryTable;
import org.santfeliu.util.Table;

/**
 *
 * @author unknown
 */
public class QueryTableConverter
{
  public QueryTableConverter()
  {
  }
  
  public static QueryTable fromTable(Table table)
  {
    QueryTable result = new QueryTable();
    if (table != null)
    {
      int columnCount = table.getColumnCount();
      for (int i = 0; i < columnCount; i++)
      {
        result.getColumnName().add(table.getColumnName(i));
      }

      for (int i = 0; i < table.getRowCount(); i++)
      {
        QueryRow row = new QueryRow();
        for (int j = 0; j < columnCount; j++)
        {
          row.getValues().add(table.getElementAt(i, j));
        }
        result.getQueryRow().add(row);
      }
    }
    
    return result;        
  }
  
  public static QueryRow fromRow(Table.Row row)
  {
    QueryRow result = new QueryRow();
    if (row != null)
    {
      Set<Table.Row.Entry> entries = row.entrySet();
      for (Table.Row.Entry entry : entries)
      {
        result.getValues().add(entry.getValue());
      }
    }
    
    return result;
  }
  
  public static Table toTable(QueryTable queryTable)
  {
    Table result = new Table(getColumnNames(queryTable));
    
    List<QueryRow> rows = queryTable.getQueryRow();
    for (QueryRow row : rows)
    {
      List values = row.getValues();
      result.addRow(values.toArray());
    }
    
    return result;
  }
  
  private static String[] getColumnNames(QueryTable queryTable)
  {
    List<String> queryNames = queryTable.getColumnName();
    return queryNames.toArray(new String[queryNames.size()]);
  }
}
