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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 *
 * @author realor
 */
public class MapEditor
{
  private Map map;

  public MapEditor(Map map)
  {
    this.map = map;
  }

  public void parse(String string) throws Exception
  {
    Context cx = ContextFactory.getGlobal().enterContext();
    try
    {
      Scriptable scope = new Scope(cx);
      scope.put("set", scope, new SetFuntion());
      cx.evaluateString(scope, string, "properties", 1, null);
    }
    finally
    {
      Context.exit();
    }
  }

  public String format()
  {
    List keys = new ArrayList();
    keys.addAll(map.keySet());
    Collections.sort(keys);
    StringBuilder buffer = new StringBuilder();
    for (Object key : keys)
    {
      String name = String.valueOf(key);
      Object value = map.get(name);

      if (isValidIdentifier(name))
      {
        buffer.append(name);
        buffer.append(" = ");
        buffer.append(javaToString(value));
      }
      else
      {
        buffer.append("set(\"");
        buffer.append(name);
        buffer.append("\", ");
        buffer.append(javaToString(value));
        buffer.append(")");
      }
      buffer.append("\n");
    }
    return buffer.toString();
  }

  @Override
  public String toString()
  {
    return format();
  }

  // private methods

  private boolean isValidIdentifier(String identifier)
  {
    if (identifier == null) return false;
    String word = " " + identifier + " ";
    String keywords = " const if else export while for var switch do break " +
      "continue in function label let new this try catch throw void yield ";
    if (keywords.indexOf(word) != -1) return false;
    return identifier.matches("[a-zA-Z_]\\w*");
  }

  private Object jsToJava(Object object)
  {
    if (object instanceof Scriptable)
    {
      String className = ((Scriptable)object).getClassName();
      if (className.equals("Boolean"))
      {
        object = ((Scriptable)object).getDefaultValue(Boolean.class);
      }
      else if (className.equals("Number"))
      {
        object = Double.valueOf(object.toString());
      }
      else if (className.equals("String"))
      {
         object = object.toString();
      }
      else if (object instanceof NativeArray)
      {
        List list = new ArrayList();
        NativeArray array = (NativeArray)object;
        for (int i = 0; i < array.getLength(); i++)
        {
          Object elem = array.get(i, (Scriptable)object);
          list.add(jsToJava(elem));
        }
        object = list;
      }
      else if (object instanceof NativeJavaObject)
      {
        Object javaObject = ((NativeJavaObject)object).unwrap();
        if (javaObject instanceof Number)
        {
          object = ((Number)javaObject).doubleValue();
        }
        else if (javaObject instanceof String)
        {
          object = javaObject;
        }
        else if (javaObject instanceof Boolean)
        {
          object = javaObject;
        }
      }
    }
    return object;
  }

  private String javaToString(Object value)
  {
    String svalue = null;
    if (value == null) svalue = "null";
    else if (value instanceof List)
    {
      StringBuilder buffer = new StringBuilder();
      buffer.append("[");
      List list = (List)value;
      for (int i = 0; i < list.size(); i++)
      {
        if (buffer.length() > 1) buffer.append(", ");
        buffer.append(javaToString(list.get(i)));
      }
      buffer.append("]");
      svalue = buffer.toString();
    }
    else if (value instanceof String)
    {
      String s = value.toString();
      StringBuilder builder = new StringBuilder();
      int maxLength = 50;
      while (s.length() > maxLength)
      {
        String chunk = s.substring(0, maxLength);
        builder.append("\"");
        builder.append(quote(chunk));
        builder.append("\" +\n");
        s = s.substring(maxLength);
      }
      builder.append("\"");
      builder.append(quote(s));
      builder.append("\"");
      svalue = builder.toString();
    }
    else
    {
      svalue = value.toString();
    }
    return svalue;
  }

  private String quote(String text)
  {
    StringBuilder buffer = new StringBuilder();
    for (int pos = 0; pos < text.length(); pos++)
    {
      char ch = text.charAt(pos);
      if (ch == '"')
      {
        buffer.append('\\');
        buffer.append('"');
      }
      else if (ch == '\n')
      {
        buffer.append('\\');
        buffer.append('n');
      }
      else if (ch == '\r')
      {
        buffer.append('\\');
        buffer.append('r');
      }
      else if (ch == '\t')
      {
        buffer.append('\\');
        buffer.append('t');
      }
      else buffer.append(ch);
    }
    return buffer.toString();
  }

  private void setValue(String name, Object value)
  {
    Object cvalue = jsToJava(value);
    if (cvalue == null ||
      cvalue instanceof String || cvalue instanceof Number ||
      cvalue instanceof Boolean || cvalue instanceof List)
    {
      map.put(name, cvalue);
    }
  }

  class SetFuntion extends BaseFunction
  {
    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj,
      Object[] args)
    {
      if (args.length >= 2)
      {
        setValue(String.valueOf(args[0]), args[1]);
      }
      return null;
    }
  }

  class Scope extends ScriptableObject
  {
    public Scope(Context context)
    {
      ScriptRuntime.initStandardObjects(context, this, true);
    }

    @Override
    public String getClassName()
    {
      return "MapEditorScope";
    }

    @Override
    public void put(String name, Scriptable start, Object value)
    {
      super.put(name, start, value);
      if (!"NaN".equals(name) && !"Infinity".equals(name))
      {
        setValue(name, value);
      }
    }
  }

  public static void main(String[] args)
  {
    try
    {
      Map map = new HashMap();
      MapEditor editor = new MapEditor(map);
      map.put("type", null);
      map.put("alfa", 4);
      map.put("beta", "89");
      map.put("gamma", "HOLA");
      List list = new ArrayList();
      list.add("RICARD");
      list.add("REAL");
      map.put("nom", list);
      map.put("visible", true);
      map.put("workflow.name", true);

      String s = editor.toString();
      System.out.println(editor);

      map.clear();

      editor.parse(s);
      System.out.println("-------");
      System.out.println(editor);

    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
