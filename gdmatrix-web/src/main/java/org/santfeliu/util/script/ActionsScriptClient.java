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

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 *
 * @author blanquepa
 */
public class ActionsScriptClient extends ScriptClient
{
  public static final String ACTION_PARAM = "action";
  private static final String REMOTE_PREFIX = "remote";

  @Override
  protected Scriptable createScope(Context context)
  {
    return new WebScriptableBase(context);
  }

  public Object executeAction(String action)
    throws Exception
  {
    Object result = null;
    if (action != null)
    {
      String scriptName;
      action = action.substring(action.indexOf(":") + 1); //supress prefix
      if (action.contains("."))
      {
        scriptName = action.substring(0, action.indexOf("."));
        action = action.substring(action.indexOf(".") + 1);
        if (action.contains("?"))
        {
          String[] parts = action.split("\\?");
          action = parts[0];
          String[] params = parts[1].split("&");
          for (String param : params)
          {
            String[] pparts = param.split("=");
            String name = pparts[0];
            String value = pparts[1];
            put(name, value);
          }
        }
        put(ACTION_PARAM, action);
      }
      else
      {
        scriptName = action;
      }
      result = executeScript(scriptName);
    }
    return result;
  }

  public Object executeRemoteAction(String action)
    throws Exception
  {
    Object result = null;
    if (action != null)
    {
      String scriptName;
      action = action.substring(action.indexOf(":") + 1); //supress prefix
      if (action.contains("."))
      {
        scriptName = action.substring(0, action.indexOf("."));
        action = action.substring(action.indexOf(".") + 1);
        if (action != null && action.startsWith(REMOTE_PREFIX))
        {
          executeScript(scriptName);
          Object[] params = null;
          if (action.contains("?"))
          {
            String[] parts = action.split("\\?");
            action = parts[0];
            params = parts[1].split("\\|");
          }
          Callable callable = (Callable) get(action);
          result = execute((Callable) callable, params);
        }
      }
    }
    return result;
  }
}
