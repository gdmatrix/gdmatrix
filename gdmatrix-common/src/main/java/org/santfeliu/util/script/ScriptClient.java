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

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;


/**
 * Client class to read and execute script documents.
 * Scripts executed with adminCredentials defined in MatrixConfig.
 *
 * @author blanquepa
 * @author realor
 */
public class ScriptClient
{
  private long refreshTime = 60 * 1000; //60 seconds

  protected static final Logger LOGGER = Logger.getLogger("ScriptClient");

  private Scriptable scope;

  public ScriptClient()
  {
  }

  public ScriptClient(Scriptable scope)
  {
    this.scope = scope;
  }

  public long getRefreshTime()
  {
    return refreshTime;
  }

  public void setRefreshTime(long refreshTime)
  {
    this.refreshTime = refreshTime;
  }

  public final Scriptable getScope()
  {
    if (scope == null)
    {
      Context context = Context.enter();
      try
      {
        scope = createScope(context);
      }
      finally
      {
        Context.exit();
      }
    }
    return scope;
  }

  /* Put objects into scope */
  public void put(String key, Object object)
  {
    getScope().put(key, getScope(), object);
  }

  public Object get(String key)
  {
    if (scope == null) return null;

    return scope.get(key, getScope());
  }

  public Object execute(String code)
  {
    Context context = Context.enter();
    try
    {
      if (scope == null) scope = createScope(context);

      Object result = context.evaluateString(scope, code, "", 1, null);
      return unwrap(result);
    }
    finally
    {
      Context.exit();
    }
  }
  
  public Object execute(Callable callable, Object... args)
  {
    Context context = Context.enter();
    try
    {
      LOGGER.log(Level.INFO, "Executing function {0}", 
        getFunctionName(callable));  
      
      if (scope == null) scope = createScope(context);

      Object result = callable.call(context, scope, scope, args);
      return ScriptClient.unwrap(result);
    }
    finally
    {
      Context.exit();
    }
  }
  
  public Object executeScript(String scriptName) throws Exception
  {
    return executeScript(scriptName, null);
  }

  public Object executeScript(String scriptName, String code) throws Exception
  {
    if (code == null)
    {
      LOGGER.log(Level.INFO, "Executing script {0}", scriptName);
    }
    else
    {
      LOGGER.log(Level.INFO, "Executing script {0}: {1}",
        new Object[]{scriptName, code});
    }

    long now = System.currentTimeMillis();
    if (ScriptCache.getLastRefresh() + refreshTime < now)
      ScriptCache.refresh();

    Context context = Context.enter();

    Script script = ScriptCache.getScript(scriptName);

    Object result;
    try
    {
      if (scope == null) scope = createScope(context);

      result = script.exec(context, scope);

      if (code != null)
        result = context.evaluateString(scope, code, scriptName, 1, null);

      result = unwrap(result);
    }
    catch (JavaScriptException ex)
    {
      throw new Exception(ex.getMessage());
    }
    finally
    {
      Context.exit();
    }
    return result;
  }

  public void refreshCache()
  {
    ScriptCache.refresh();
  }

  protected Scriptable createScope(Context context)
  {
    return new ScriptableBase(context);
  }

  public static Object unwrap(Object result)
  {
    if (result instanceof NativeJavaObject)
    {
      NativeJavaObject nat = (NativeJavaObject)result;
      result = nat.unwrap();
    }
    if (result instanceof Undefined)
      result = null;

    return result;
  }
  
  private String getFunctionName(Callable callable)
  {
    if (callable instanceof BaseFunction)
    {
      BaseFunction function = (BaseFunction)callable;
      String functionName = function.getFunctionName();
      return functionName.isEmpty() ? "anonymous" : functionName;
    }     
    return "";
  }  

}
