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
package org.santfeliu.util.script.function;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;

/**
 *
 * @author blanquepa
 *
 * Usage examples:
 *        var json = new Json();
  *        // Stringify (JS object --> string)
 *        var jsonString = json.stringify({atr1: value1, atr2: value2});
  *        // Parse (string --> JS Object)
 *        var object = json.parse("{atr1: value1, atr2: value2}");
 */
@Deprecated
public class JSONFunction extends BaseFunction
{
  private final StringifyFunction stringifyFunction = new StringifyFunction();
  private final ParseFunction parseFunction = new ParseFunction();

  @Override
  public Object call(Context cx, Scriptable scope, Scriptable thisObj,
    Object[] args)
  {
    if (thisObj == null) return null;

    thisObj.put("stringify", thisObj, stringifyFunction);
    thisObj.put("parse", thisObj, parseFunction);

    return thisObj;
  }

  private static class StringifyFunction extends BaseFunction
  {

    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj,
      Object[] args)
    {
      try
      {
        if (args != null && args.length == 1)
        {
          Object object  = jsToJava(args[0]);
          Object jsonObject = toJSON(object);
          if (jsonObject instanceof JSONAware)
            return ((JSONAware) jsonObject).toJSONString();
          else
            return jsonObject.toString();
        }
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
      return false;
    }

    private Object toJSON(Object object)
    {
      if (object instanceof Object[])
      {
        JSONArray jsonArray = new JSONArray();
        for (Object obj : (Object[])object)
        {
          jsonArray.add(toJSON(obj));
        }
        return jsonArray;
      }
      else if (object instanceof Map)
      {
        JSONObject jsonObject = new JSONObject();
        for (Object key : ((Map)object).keySet())
        {
          Object value = ((Map)object).get(key);
          jsonObject.put(key, toJSON(value));
        }
        return jsonObject;
      }
      else if (object != null)
        return object;
      else
        return "";
    }

    private Object jsToJava(Object jsObject)
    {
      if (jsObject == null)   return null;
      if (jsObject == org.mozilla.javascript.Context.getUndefinedValue())   return null;
      if (jsObject instanceof String)   return jsObject;
      if (jsObject instanceof Boolean)   return jsObject;
      if (jsObject instanceof Integer)   return jsObject;
      if (jsObject instanceof Long)   return jsObject;
      if (jsObject instanceof Float)   return jsObject;
      if (jsObject instanceof Double)   return jsObject;
      if (jsObject instanceof NativeArray)   return convertArray((NativeArray)jsObject);
      if (jsObject instanceof NativeObject)   return convertObject((NativeObject)jsObject);
      if (jsObject instanceof NativeJavaObject)   return ((NativeJavaObject)jsObject).unwrap();
      return jsObject;
    }

    private Object[] convertArray(NativeArray jsArray)
    {
        Object[] ids = jsArray.getIds();
        Object[] result = new Object[ids.length];
        for (int i = 0; i < ids.length; i++)
        {
            Object id = ids[i];
            int index = (Integer) id;
            Object jsValue = jsArray.get(index, jsArray);
            result[i] = jsToJava(jsValue);
        }
        return result;
    }

    private Object convertObject(NativeObject jsObject)
    {
      Object[] ids = jsObject.getIds();
      Map result = new HashMap(ids.length);
      for (Object id : ids)
      {
        if (id instanceof String)
        {
          Object jsValue = jsObject.get((String)id, jsObject);
          result.put(id, jsToJava(jsValue));
        }
        else if (id instanceof Integer)
        {
          Object jsValue = jsObject.get((Integer)id,jsObject);
          result.put(id, jsToJava(jsValue));
        }
        else
          throw new AssertionError();
      }
      return result;
    }

  }

  private static class ParseFunction extends BaseFunction
  {
    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj,
      Object[] args)
    {
      try
      {
        if (args != null && args.length == 1)
        {
          JSONParser parser = new JSONParser();
          Object obj = parser.parse((String)args[0]);
          Object jsObj = toJSObject(cx, scope, (JSONObject)obj);
          return jsObj;
        }
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
      return false;
    }

    private Scriptable toJSObject(Context cx, Scriptable scope, JSONObject jsonObject)
    {
      Scriptable object = cx.newObject(scope);
      Iterator<String> keys = jsonObject.keySet().iterator();
      while (keys.hasNext())
      {
        String key = (String)keys.next();
        Object value = jsonObject.get(key);
        if (value instanceof JSONObject)
        {
          object.put(key, object, toJSObject(cx, scope, (JSONObject)value));
        }
        else
        {
          object.put(key, object, value);
        }
      }

      return object;
    }
  }

  public static void main(String args[])
  {
    JSONObject obj = new JSONObject();
    obj.put("key1", new String("value1"));

    Object[] array = new Object[3];
    array[0] = new String("AA");
    array[1] = new String("BB");
    array[2] = new String("CC");
    obj.put("key2", array);
    System.out.println(obj.toJSONString());

  }


}
