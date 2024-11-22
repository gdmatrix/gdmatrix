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
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mozilla.javascript.ConsString;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.UniqueTag;

/**
 *
 * @author realor
 */
public class ScriptableBase extends ScriptableObject
{
  private static final String ARRAY_PREFIX = "_ARRAY_";
  private Map persistentVariables;
  private String nonPersistentPrefix;
  private static Pattern pattern = Pattern.compile("[0-9]+(_[0-9]+)*");

  public ScriptableBase(Context context)
  {
    this(context, null);
  }

  public ScriptableBase(Context context, Map persistentVariables)
  {
    this(context, persistentVariables, "_");
  }

  public ScriptableBase(Context context, Map persistentVariables,
    String nonPersistentPrefix)
  {
    // init standard objects
    ScriptRuntime.initStandardObjects(context, this, true);
    if (persistentVariables == null)
    {
      this.persistentVariables = new HashMap();
    }
    else
    {
      this.persistentVariables = persistentVariables;
      // init persistent variables
      initPersistentVariables();
    }
    this.nonPersistentPrefix = nonPersistentPrefix;
    // init built-in functions
    FunctionFactory.initFunctions(this);
  }

  @Override
  public String getClassName()
  {
    return "ScriptableBase";
  }

  @Override
  public Object get(String name, Scriptable start)
  {
    Object o = super.get(name, start);
    if (o == UniqueTag.NOT_FOUND)
    {
      o = super.get(ARRAY_PREFIX + name, start);
      if (o == UniqueTag.NOT_FOUND)
      {
        o = null;
      }
    }
    return o;
  }

  @Override
  public void put(String name, Scriptable start, Object value)
  {
    // set value in ScriptableObject
    if (value == null)
    {
      if (super.get(name, start) != UniqueTag.NOT_FOUND && getAttributes(name) == PERMANENT)
        super.put(name, start, value);
      else
        super.delete(name);
      dimArrayVariable(name, false);
    }
    else
    {
      super.put(name, start, value);
      dimArrayVariable(name, true);
    }

    // save value in persistentVariables
    if (nonPersistentPrefix == null || !name.startsWith(nonPersistentPrefix))
    {
      value = toJavaPrimitive(value);
      if (value == null)
      {
        persistentVariables.remove(name);
      }
      else if (isBasicType(value) &&
           !name.equals("NaN") &&
           !name.equals("Infinity"))
      {
        persistentVariables.put(name, value);
      }
    }
  }

  public Map getPersistentVariables()
  {
    return persistentVariables;
  }

  public Object toJavaPrimitive(Object object)
  {
    if (object instanceof ConsString)
    {
      object = object.toString();
    }
    else if (object instanceof Scriptable)
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

  /***** private methods ******/

  private boolean isBasicType(Object value)
  {
    return value instanceof Number ||
      value instanceof String ||
      value instanceof Boolean;
  }

  private void dimArrayVariable(String name, boolean set)
  {
    int pos = name.indexOf("_");
    if (pos > 0)
    {
      String indexs = name.substring(pos + 1);
      Matcher matcher = pattern.matcher(indexs);
      if (matcher.matches())
      {
        if (set)
        {
          String base = name.substring(0, pos);
          String[] tokens = indexs.split("_");
          int i = 0;
          do
          {
            Object obj = super.get(ARRAY_PREFIX + base, this);
            if (obj == UniqueTag.NOT_FOUND)
            {
              Array array = new Array(base);
              super.put(ARRAY_PREFIX + base, this, array);
            }
            base = base + "_" + tokens[i];
            i++;
          } while (i < tokens.length);
        }
        else
        {
          super.delete(ARRAY_PREFIX + name);
        }
      }
    }
  }

  private void initPersistentVariables()
  {
    Iterator iter = persistentVariables.entrySet().iterator();
    while (iter.hasNext())
    {
      Map.Entry entry = (Map.Entry)iter.next();
      String name = String.valueOf(entry.getKey());
      Object value = entry.getValue();

      if (value == null)
      {
        super.delete(name);
        dimArrayVariable(name, false);
      }
      else
      {
        super.put(name, this, value);
        dimArrayVariable(name, true);
      }
    }
  }

  public class Array implements Scriptable
  {
    private String base;

    Array(String base)
    {
      this.base = base;
    }

    @Override
    public String getClassName() { return "ScriptableBaseArray"; }

    @Override
    public Object get(String name, Scriptable scriptable) { return null; }

    @Override
    public Object get(int i, Scriptable scriptable)
    {
      return ScriptableBase.this.get(base + "_" + i,
        ScriptableBase.this);
    }

    @Override
    public boolean has(String name, Scriptable scriptable) { return false; }

    @Override
    public boolean has(int i, Scriptable scriptable)
    {
      return ScriptableBase.this.has(base + "_" + i,
        ScriptableBase.this);
    }

    @Override
    public void put(String s, Scriptable scriptable, Object obj) { }

    @Override
    public void put(int i, Scriptable scriptable, Object obj)
    {
      ScriptableBase.this.put(base + "_" + i,
        ScriptableBase.this, obj);
    }

    @Override
    public void delete(String s) {}

    @Override
    public void delete(int i)
    {
      ScriptableBase.this.delete(base + "_" + i);
    }

    @Override
    public Scriptable getPrototype() { return null; }

    @Override
    public void setPrototype(Scriptable scriptable) {}

    @Override
    public Scriptable getParentScope() { return null; }

    @Override
    public void setParentScope(Scriptable scriptable) {}

    @Override
    public Object[] getIds() { return null; }

    @Override
    public Object getDefaultValue(Class class1) { return null; }

    @Override
    public boolean hasInstance(Scriptable scriptable) { return false; }
  }

  /**** main ****/

  public static final void main(String[] args)
  {
    System.out.println("start...");
    Context cx = ContextFactory.getGlobal().enterContext();
    try
    {
      HashMap persistentVariables = new HashMap();
//      persistentVariables.put("a", new Double(8));
      persistentVariables.put("nom", "Ricard");
      persistentVariables.put("cognom", "Real");
      persistentVariables.put("preu", Double.valueOf(80));
      //String code = "t = blankNull(a + 1) + trim('  gg   ')";
      //String code = "t = 2 + 6 + 8 - 2";
      //String code = "a = decimalFormat(a, '###0.00')";
      //String code = "a = htmlEncode('això és una & prova > 8')";
      //String code = "a = xmlEncode('això és una & prova > 8')";
//      String code = "a = include('form:demo_include2')";
//      String code = "a = urlEncode('això és una & prova')";
      String code = "var a = 1;a;a=null";

      ScriptableBase scope = new ScriptableBase(cx, persistentVariables);
      Object result = cx.evaluateString(scope, code, "<code>", 1, null);
      System.out.println("result = " + result);
      if (result != null) System.out.println(result.getClass().getName());
      System.out.println(persistentVariables);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    finally
    {
      Context.exit();
    }
  }
}

