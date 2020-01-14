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
package org.santfeliu.workflow;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author unknown
 */
public class ValueChanges
{
  private HashMap changes = new HashMap();

  public ValueChanges()
  {
  }

  public ValueChanges(Map variables)
  {
    registerChanges(variables);
  }

  public void registerChanges(Map variables)
  {
    Set entries = variables.entrySet();
    Iterator iter = entries.iterator();
    while (iter.hasNext())
    {
      Map.Entry entry = (Map.Entry)iter.next();
      Object key = entry.getKey();
      Object value = entry.getValue();
      registerChange(key, value);
    }    
  }

  public void registerChange(Object key, Object newValue)
  {
    registerChange(key, null, newValue);
  }

  public void registerChange(Object key, Object oldValue, Object newValue)
  {
    if (!equals(oldValue, newValue))
    {
      Change change = (Change)changes.get(key);
      if (change == null) // first time that this key is modified
      {
        change = new Change();
        change.oldValue = oldValue;
        change.newValue = newValue;
        changes.put(key, change);
      }
      else // change was previously registered
      {
        if (equals(change.oldValue, newValue))
        {
          // newValue is oldValue again, remove change
          changes.remove(key);
        }
        else
        {
          // update newValue but keep oldValue.
          change.newValue = newValue;
        }
      }
    }
  }

  public Set keySet()
  {
    return changes.keySet();
  }

  public boolean contains(Object key)
  {
    return changes.containsKey(key);
  }

  public Object getOldValue(Object key)
  {
    Change change = (Change)changes.get(key);
    return (change == null) ? null : change.oldValue;
  }

  public Object getNewValue(Object key)
  {
    Change change = (Change)changes.get(key);
    return (change == null) ? null : change.newValue;
  }

  public boolean isEmpty()
  {
    return changes.isEmpty();
  }
  
  public Map getNewValues()
  {
    Map map = new HashMap();
    Set entries = changes.entrySet();
    Iterator iter = entries.iterator();
    while (iter.hasNext())
    {
      Map.Entry entry = (Map.Entry)iter.next();      
      Object key = entry.getKey();
      Change change = (Change)entry.getValue();
      map.put(key, change.newValue);
    }
    return map;
  }
  
  public Map getOldValues()
  {
    Map map = new HashMap();
    Set entries = changes.entrySet();
    Iterator iter = entries.iterator();
    while (iter.hasNext())
    {
      Map.Entry entry = (Map.Entry)iter.next();      
      Object key = entry.getKey();
      Change change = (Change)entry.getValue();
      map.put(key, change.oldValue);
    }
    return map;
  }
  
  public boolean equals(Object a, Object b)
  {
    if (a == null) return b == null;
    if (b == null) return a == null;

    if (a instanceof Number && 
        b instanceof Number)
    {
      Number numbera = (Number)a;
      Number numberb = (Number)b;
      return numbera.doubleValue() == numberb.doubleValue();
    }
    else return a.equals(b);
  }
  
  class Change
  {
    private Object oldValue;
    private Object newValue;
  }  
}
