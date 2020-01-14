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
package org.santfeliu.util;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 *
 * @author unknown
 */
public class Table extends ArrayList implements Serializable
{
  private String columnNames[];
  private Map columnsMap;
  
  public Table(String ... columnNames)
  {
    this.columnNames = columnNames;
    this.columnsMap = new HashMap(columnNames.length * 2);
    for (int columnIndex = 0; columnIndex < columnNames.length; columnIndex++)
    {
      columnsMap.put(columnNames[columnIndex], new Integer(columnIndex));
    }
  }

  @Override
  public boolean add(Object o)
  {
    Row row = new Row();
    if (columnNames.length > 0)
    {
      row.data[0] = o;
    }
    return super.add(row);
  }

  public void addRow(Object ... values)
  {
    super.add(new Row(values));    
  }

  public void addRow()
  {
    super.add(new Row());
  }
  
  public Row getRow(int rowIndex)
  {
    return (Row)super.get(rowIndex);
  }

  public void removeRow(int rowIndex)
  {
    remove(rowIndex);
  }
  
  public void setElementAt(int rowIndex, int columnIndex, Object value)
  {
    Row row = (Row)get(rowIndex);
    row.data[columnIndex] = value;
  }
  
  public Object getElementAt(int rowIndex, int columnIndex)
  {
    Row row = (Row)get(rowIndex);
    return row.data[columnIndex];
  }
  
  public void setElementAt(int rowIndex, String columnName, Object value)
    throws Exception
  {
    Row row = (Row)get(rowIndex);
    int columnIndex = getColumnIndex(columnName);
    if (columnIndex == -1) 
      throw new Exception("invalid column name:" + columnName);
    row.data[columnIndex] = value;
  }
  
  public Object getElementAt(int rowIndex, String columnName)
    throws Exception
  {
    Row row = (Row)get(rowIndex);
    int columnIndex = getColumnIndex(columnName);
    if (columnIndex == -1) 
      throw new Exception("invalid column name:" + columnName);
    return row.data[columnIndex];
  }
  
  public void setColumnName(int columnIndex, String columnName)
  {
    String oldColumnName = columnNames[columnIndex];
    columnNames[columnIndex] = columnName;
    Integer index = (Integer)columnsMap.remove(oldColumnName);
    columnsMap.put(columnName, index);
  }
  
  public String getColumnName(int columnIndex)
  {
    return columnNames[columnIndex];
  }

  public int getColumnIndex(String columnName)
  {
    int columnIndex = ((Integer)columnsMap.get(columnName)).intValue();
    return columnIndex;
  }

  public int getColumnCount()
  {
    return columnNames.length;
  }
  
  public int getRowCount()
  {
    return size();
  }

  @Override
  public String toString()
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append("(");
    if (columnNames.length > 0)
    {
      buffer.append(columnNames[0]);
      for (int k = 1; k < columnNames.length; k++)
      {
        buffer.append(", ");
        buffer.append(columnNames[k]);
      }
    }
    buffer.append(")=\n");
    
    buffer.append("{\n");
    Iterator iter = iterator();
    while (iter.hasNext())
    {
      Row row = (Row)iter.next();
      buffer.append(" {");
      Iterator colIter = row.values().iterator();
      if (colIter.hasNext())
      {
        buffer.append(colIter.next());
        while (colIter.hasNext())
        {
          buffer.append(", ");
          buffer.append(colIter.next());
        }
      }
      buffer.append("}\n");
    }
    buffer.append("}\n");
    return buffer.toString();   
  }
  
  
  /***** inner class Table.Row *****/
  public class Row implements java.util.Map, Serializable
  {
    private Object[] data;
  
    Row()
    {
      data = new Object[columnNames.length];
    }
    
    Row(Object[] values)
    {
      if (values.length == columnNames.length)
      {
        data = values;
      }
      else
      {
        data = new Object[columnNames.length];
        System.arraycopy(values, 0, data, 0, 
          Math.min(data.length, values.length));
      }
    }
    
    // Map methods
    public int size()
    {
      return data.length;
    }
    
    public boolean isEmpty()
    {
      return data.length == 0;
    }
    
    public boolean containsKey(Object key)
    {
      return columnsMap.containsKey(key);
    }

    public boolean containsValue(Object value)
    {
      boolean found = false;
      int columnIndex = 0;
      while (columnIndex < data.length && !found)
      {
        if (value == null)
        {
          found = data[columnIndex] == null;
        }
        else
        {
          found = value.equals(data[columnIndex]);
        }
        columnIndex++;
      }
      return found;      
    }
    
    public Object get(Object key)
    {
      if (!(key instanceof String)) return null;
      String columnName = (String)key;
      int columnIndex = getColumnIndex(columnName);
      if (columnIndex == -1) return null;
      return data[columnIndex];
    }
    
    public Object put(Object key, Object value)
      throws UnsupportedOperationException
    {
      if (!(key instanceof String)) 
        throw new UnsupportedOperationException();
      String columnName = (String)key;
      int columnIndex = getColumnIndex(columnName);
      if (columnIndex == -1) return new UnsupportedOperationException();
      Object oldValue = data[columnIndex];
      data[columnIndex] = value;
      return oldValue;
    }

    public Object remove(Object key) throws UnsupportedOperationException
    {
      throw new UnsupportedOperationException();
    }

    public void putAll(Map map) throws UnsupportedOperationException
    {
      Set entrySet = map.entrySet();
      Iterator iter = entrySet.iterator();
      while (iter.hasNext())
      {
        Map.Entry entry = (Map.Entry)iter.next();
        Object key = entry.getKey();
        Object value = entry.getValue();
        put(key, value);
      }
    }

    public void clear() throws UnsupportedOperationException
    {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o)
    {
      if (o instanceof Object[])
        return Arrays.equals(data, (Object[])o);
        
      return false;
    }
    
    @Override
    public int hashCode()
    {
      return data.hashCode();
    }
    
    public Set keySet()
    {
      return Collections.unmodifiableSet(columnsMap.keySet());
    }

    public java.util.Collection values()
    {
      return new Table.Row.Collection();
    }
    
    public Set entrySet()
    {
      HashSet set = new HashSet();
      for (int columnIndex = 0; columnIndex < columnNames.length; columnIndex++)
      {
        Entry entry = new Table.Row.Entry(columnIndex);
        set.add(entry);
      }
      return Collections.unmodifiableSet(set);
    }


    /***** inner class Table.Row.Entry *****/
    public class Entry implements Map.Entry
    {
      private int columnIndex;
          
      public Entry(int columnIndex)
      {
        this.columnIndex = columnIndex;
      }
      
      public Object getKey()
      {
        return columnNames[columnIndex];
      }

      public Object getValue()
      {
        return data[columnIndex];
      }

      @Override
      public boolean equals(Object o)
      {
        if (!(o instanceof Map.Entry)) return false;
        Map.Entry e = (Map.Entry)o;
        return (this.getKey() == null ?
                 e.getKey() == null : this.getKey().equals(e.getKey())) &&
               (this.getValue() == null ?
                 e.getValue() == null : this.getValue().equals(e.getValue()));        
      }

      @Override
      public int hashCode()
      {
        return data.hashCode();
      }

      public Object setValue(Object value)
      {
        Object oldValue = data[columnIndex];
        data[columnIndex] = value;
        return oldValue;
      }
    }

    /***** inner class Table.Row.Collection *****/
    public class Collection implements java.util.Collection
    {
      public int size()
      {
        return data.length;
      }
    
      public boolean isEmpty()
      {
        return data.length == 0;
      }
    
      public boolean contains(Object value)
      {
        return Row.this.containsValue(value);
      }
    
      public Object[] toArray()
      {
        Object[] array = new Object[data.length];
        System.arraycopy(data, 0, array, 0, data.length);
        return array;
      }

      public Object[] toArray(Object[] array)
      {
        int size = columnNames.length;
        if (array.length < size)
        {
          array = (Object[])java.lang.reflect.Array.newInstance(
            array.getClass().getComponentType(), size);
        }
        System.arraycopy(data, 0, array, 0, size);

        if (array.length > size)
        {
          array[size] = null;
        }
        return array;
      }
    
      public boolean add(Object o)
        throws UnsupportedOperationException
      {
        throw new UnsupportedOperationException();
      }

      public boolean remove(Object o)
        throws UnsupportedOperationException
      {
        throw new UnsupportedOperationException();
      }

      public void clear()
        throws UnsupportedOperationException
      {
        throw new UnsupportedOperationException();
      }
    
      public boolean addAll(java.util.Collection col)
        throws UnsupportedOperationException
      {
        throw new UnsupportedOperationException();
      }
    
      public boolean removeAll(java.util.Collection c)
        throws UnsupportedOperationException
      {
        throw new UnsupportedOperationException();
      }
    
      public boolean retainAll(java.util.Collection c)
        throws UnsupportedOperationException
      {
        throw new UnsupportedOperationException();
      }

      public boolean containsAll(java.util.Collection c)
      {
        boolean containsAll = true;
        Iterator iter = c.iterator();
        while (containsAll && iter.hasNext())
        {
          Object value = iter.next();
          containsAll = contains(value);
        }
        return containsAll;
      }
    
      public Iterator iterator()
      {
        return new Iterator()
        {
          private int columnIndex = -1;
        
          public boolean hasNext()
          {
            return columnIndex + 1 < data.length;
          }
        
          public Object next()
          {
            return data[++columnIndex];
          }
        
          public void remove() throws UnsupportedOperationException
          {
            throw new UnsupportedOperationException();
          }
        };
      }    
    }
  }
  
  public static void main(String[] args)
  {
    try
    {
      String[] columnNames = {"NOM", "COGNOM", "EDAT"};
      Table table = new Table(columnNames);
      table.addRow();
      table.addRow();
      table.setElementAt(0, 0, "TEST");
      table.setElementAt(0, 1, "HOLA");
      table.setElementAt(1, 1, "BYE");
      Map map = (Map)table.get(0);
      map.put("NOM", "RICARD");
      System.out.println(map.get("NOM"));

      //System.out.println(table.getElementAt(0, "COGNOM"));
      System.out.println(table);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
