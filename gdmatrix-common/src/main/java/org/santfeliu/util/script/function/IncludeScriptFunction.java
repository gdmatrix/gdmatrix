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

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.script.ScriptClient;

/**
 *
 * @author blanquepa
 */

/*
 * Usage: include(String reference [, String language])
 *
 * returns: a String that contains the data of document referenced by
     reference and language.
 *
 *   reference format can be:
 *     js:<name>
 *
 * Examples:
 *
 *   ${include("js:math")}
 */

public class IncludeScriptFunction extends BaseFunction
{
  private static final String JS_PREFIX = "js:";
  private static final String JS_PROPERTY = "workflow.js";

  public IncludeScriptFunction()
  {
  }

  @Override
  public Object call(Context cx, Scriptable scope, Scriptable thisObj,
    Object[] args)
  {
    String result = "";
    try
    {
      if (args.length > 0)
      {
        String reference = Context.toString(args[0]);
        if (reference.startsWith(JS_PREFIX))
        {
          String jsName = reference.substring(JS_PREFIX.length());
          String userId = MatrixConfig.getProperty("adminCredentials.userId");
          String password = MatrixConfig.getProperty("adminCredentials.password");          
          ScriptClient client = new ScriptClient(userId, password);
          client.executeScript(jsName, scope);
        }
      }
    }
    catch (Exception ex)
    {
    }
    return result;
  }
}
