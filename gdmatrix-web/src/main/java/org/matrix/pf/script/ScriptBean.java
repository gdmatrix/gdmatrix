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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
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
public class ScriptBean extends FacesBean implements Map<String, Object>
{
  private final String scriptName;
  private WebScriptableBase scope;

  public ScriptBean(String scriptName)
  {
    initScope(scriptName);    
    this.scriptName = scriptName;
  }

  public Object call(String method, Object... args) throws Exception
  {
    String params = toString(args);      
    return execute(scriptName, method + params);   
  }
  
  //Map implementation methods
  public Object get(String key)
  {
    return ScriptClient.unwrap(scope.get(key, scope));
  }

  @Override
  public Object put(String key, Object value)
  {
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
    return scope.getAllIds().length;
  }

  @Override
  public boolean isEmpty()
  {
    return size() <= 0;
  }

  @Override
  public boolean containsKey(Object key)
  {
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
    throw new UnsupportedOperationException("Not supported yet.");
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
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Collection<Object> values()
  {
    List<Object> values = new ArrayList<>();
    for (int i = 0; i < size(); i++)
    {
      values.add(scope.get(i, scope));
    }
    return values;
  }

  @Override
  public Set<Map.Entry<String, Object>> entrySet()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }  
  
  //Private methods
  private void initScope(String scriptName)
  {
    if (scope == null)
    {
      try
      {
        execute(scriptName);
      }
      catch (Exception ex)
      {
        Logger.getLogger(ScriptBean.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }  
  
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
    
    ScriptClient client = new ScriptClient(context, scope);
    client.put("userSessionBean", UserSessionBean.getCurrentInstance());
    FacesContext facesContext = getFacesContext();
    if (facesContext != null)
    {
      client.put("facesContext", facesContext);
      client.put("externalContext", getExternalContext());
      client.put("request", getExternalContext().getRequest());
      client.put("application", getApplication());
    }
    
    if (methodExpr != null)
      return client.executeScript(scriptName, methodExpr);   
    else
      return client.executeScript(scriptName);
  }

}
