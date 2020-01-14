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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.apache.commons.collections.LRUMap;

/**
 *
 * @author realor
 */
public abstract class BigList<T> implements List<T>, Serializable
{
  private int size = -1; // total number of elements
  private LRUMap cache = new LRUMap(20); // Map<index as String, T>
  private int blockSize = 5; // number of rows to read in block

  public BigList()
  {
  }

  public BigList(int cacheSize)
  {
    cache.setMaximumSize(cacheSize);
  }

  public BigList(int cacheSize, int blockSize)
  {
    cache.setMaximumSize(cacheSize);
    this.blockSize = blockSize;
  }

  public int getCacheSize()
  {
    return cache.getMaximumSize();
  }

  public void setCacheSize(int cacheSize)
  {
    this.cache.setMaximumSize(cacheSize);
  }

  public int getBlockSize()
  {
    return blockSize;
  }

  public void setBlockSize(int blockSize)
  {
    this.blockSize = blockSize;
  }

  // returns the total number of rows
  public abstract int getElementCount();

  // returns <maxResults> rows where first row is at <firstResult> index
  public abstract List<T> getElements(int firstResult, int maxResults);

  public int size()
  {
    if (size == -1)
    {
      size = getElementCount();
    }
    return size;
  }

  public boolean isEmpty()
  {
    return size() == 0;
  }

  public T get(int index)
  {
    if (!loadElementAt(index)) throw new IndexOutOfBoundsException("" + index);
    return (T)cache.get(String.valueOf(index));
  }

  public Iterator<T> iterator()
  {
    return new BLIterator();
  }

  public void clearCache()
  {
    size = -1;
    cache.clear();
  }

  public void clear()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean contains(Object o)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Object[] toArray()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public <T> T[] toArray(T[] a)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean add(T e)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean remove(Object o)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean containsAll(Collection<?> c)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean addAll(Collection<? extends T> c)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean addAll(int index, Collection<? extends T> c)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean removeAll(Collection<?> c)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean retainAll(Collection<?> c)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public T set(int index, T element)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void add(int index, T element)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public T remove(int index)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public int indexOf(Object o)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public int lastIndexOf(Object o)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public ListIterator<T> listIterator()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public ListIterator<T> listIterator(int index)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public List<T> subList(int fromIndex, int toIndex)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  // *************** private methods *****************

  // return true if index is valid
  private boolean loadElementAt(int index)
  {
    if (index < 0 || index >= size()) return false;

    if (!cache.containsKey(String.valueOf(index))) // element not in cache
    {
      List<T> elements;
      if (index + blockSize > size())
      {
        elements = getElements(index, size() - index + 1);
      }
      else
      {
        elements = getElements(index, blockSize);
      }
      if (elements != null)
      {
        // add elements in reverse order to ensure element at index is not
        // evicted from cache
        for (int i = elements.size() - 1; i >= 0; i--)
        {
          T element = elements.get(i);
          String key = String.valueOf(index + i);
          if (cache.get(key) == null) cache.put(key, element);
        }
      }
    }
    return true;
  }

  public class BLIterator<T> implements Iterator<T>
  {
    private int index = 0;

    public boolean hasNext()
    {
      return index < size();
    }

    public T next()
    {
      if (!loadElementAt(index)) return null;
      T value = (T)cache.get(String.valueOf(index));
      index++;
      return value;
    }

    public void remove()
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }
  }
}
