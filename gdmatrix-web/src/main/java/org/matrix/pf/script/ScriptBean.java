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
package org.matrix.pf.script;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import org.mozilla.javascript.Context;
import org.santfeliu.faces.FacesBean;
import org.santfeliu.util.script.ScriptClient;
import org.santfeliu.util.script.WebScriptableBase;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author blanquepa
 */
public class ScriptBean extends FacesBean
  implements Map<String, Object>, Serializable
{
  private final String scriptName;
  private WebScriptableBase scope;
  private final Map<String, Object> injectionMap;

  public static final Logger LOGGER =
    Logger.getLogger(ScriptBean.class.getName());

  public ScriptBean(String scriptName)
  {
    this.scriptName = scriptName;

    this.injectionMap = new HashMap();
    injectionMap.put("userSessionBean", UserSessionBean.getCurrentInstance());
    FacesContext facesContext = getFacesContext();
    if (facesContext != null)
    {
      injectionMap.put("facesContext", facesContext);
      injectionMap.put("externalContext", getExternalContext());
      injectionMap.put("request", getExternalContext().getRequest());
      injectionMap.put("application", getApplication());
      injectionMap.put("logger", LOGGER);
    }
  }

  /**
   * Initialize scope and force script compilation.
   * @throws Exception
   */
  public void initScope() throws Exception
  {
    if (scriptName != null && scope == null)
      execute(scriptName);
  }

  public String getScriptName()
  {
    return scriptName;
  }

  public void inject(String name, Object object)
  {
    injectionMap.put(name, object);
  }

  public Object call(String method, Object... args) throws Exception
  {
    String params = toString(args);
    return execute(scriptName, method + params);
  }

  //Map implementation methods
  public Object get(String key)
  {
    if (scope == null)
      return null;

    return ScriptClient.unwrap(scope.get(key, scope));
  }

  @Override
  public Object put(String key, Object value)
  {
    if (scope != null)
      scope.put(key, scope, value);
    return null;
  }

  @Override
  public Object get(Object key)
  {
    return get((String) key);
  }

  @Override
  public int size()
  {
    if (scope == null || scope.getPersistentVariables().isEmpty())
      return 0;

    return scope.getPersistentVariables().size();
  }

  @Override
  public boolean isEmpty()
  {
    return size() <= 0;
  }

  @Override
  public boolean containsKey(Object key)
  {
    if (scope == null)
      return false;

    return scope.has((String) key, scope);
  }

  @Override
  public boolean containsValue(Object value)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Object remove(Object key)
  {
    if (scope == null)
      return null;

    if (scope.has((String) key, scope));
      scope.delete((String) key);
    return key;
  }

  @Override
  public void putAll(Map<? extends String, ? extends Object> m)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void clear()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Set<String> keySet()
  {
    if (scope != null)
      return scope.getPersistentVariables().keySet();
    else
      return Collections.emptySet();
  }

  @Override
  public Collection<Object> values()
  {
    if (scope != null && !scope.getPersistentVariables().isEmpty())
      return scope.getPersistentVariables().values();
    else
      return Collections.emptyList();
  }

  @Override
  public Set<Map.Entry<String, Object>> entrySet()
  {
    if (scope != null)
      return scope.getPersistentVariables().entrySet();
    else
      return Collections.emptySet();
  }

  //Private methods


  private String toString(Object[] args)
  {
    String result = "";
    if (args != null)
    {
      StringBuilder sb = new StringBuilder();
      sb.append("(");
      String prefix = "";
      for (Object arg : args)
      {
        sb.append(prefix);
        if (arg instanceof Number || arg instanceof Boolean)
          sb.append(arg);
        else
          sb.append("'").append(arg).append("'");
        prefix = ",";
      }
      sb.append(")");
      result = sb.toString();
    }
    return result;
  }

  private Object execute(String scriptName) throws Exception
  {
    return execute(scriptName, null);
  }

  private Object execute(String scriptName, String methodExpr) throws Exception
  {
    Context context = Context.enter();
    if (scope == null)
      scope = new WebScriptableBase(context);

    ScriptClient client = new ScriptClient(scope);
    for (String key : injectionMap.keySet())
    {
      client.put(key, injectionMap.get(key));
    }

    Object result;

    try
    {
      if (methodExpr != null)
        result = client.executeScript(scriptName, methodExpr);
      else
        result = client.executeScript(scriptName);
    }
    finally
    {
      Context.exit();
    }
    return result;
  }

}
