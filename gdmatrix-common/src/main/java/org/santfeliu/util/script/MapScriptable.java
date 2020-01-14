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
package org.santfeliu.util.script;

import java.util.HashMap;
import java.util.Map;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 *
 * @author realor
 */
public class MapScriptable extends ScriptableObject
{
  public static final String DELETE_PREFIX = "delete_";
  private Map data;

  public MapScriptable(Scriptable parent, Map data)
  {
    super(parent, null);
    this.data = data;
  }

  @Override
  public String getClassName()
  {
    return "MapScriptable";
  }

  @Override
  public boolean has(String name, Scriptable start)
  {
    return data.containsKey(name);
  }

  @Override
  public Object get(String name, Scriptable start)
  {
    Object value = data.get(name);
    if (value == null) return null;
    return new org.mozilla.javascript.NativeJavaObject(this,
      value, value.getClass());
  }

  @Override
  public void put(String name, Scriptable start, Object value)
  {
    if (value == null)
    {
      data.remove(name);
    }
    else if (value instanceof NativeJavaObject)
    {
      NativeJavaObject nativeJavaObject = (NativeJavaObject)value;
      data.put(name, nativeJavaObject.unwrap());
    }
    else data.put(name, value);
  }

  @Override
  public void delete(String name)
  {
    data.remove(name);
  }

  public static void main(String[] args)
  {
    try
    {
      Context cx = ContextFactory.getGlobal().enterContext();
      try
      {
        HashMap entry = new HashMap();
        entry.put("ip", "10.1.0.120");
        entry.put("userid", "realor");
        entry.put("date", "20110801");
        entry.put("time", "121100");
        
        DefaultScriptable scriptable = new DefaultScriptable(cx);
        scriptable.put("entry", scriptable, new MapScriptable(scriptable, entry));

        cx.evaluateString(scriptable,
          "entry.datetime = entry.date + entry.time; " +
          "entry.date = null; entry.time = null",
          "test", 0, null);
        System.out.println(entry);
        for (Object key : entry.keySet())
        {
          Object value = entry.get(key);
          System.out.println(key + "=" + String.valueOf(value) + " " +
            (value == null ? "" : value.getClass()));
        }
      }
      finally
      {
        Context.exit();
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
