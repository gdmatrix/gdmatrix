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
package org.santfeliu.ant.js;

import java.util.Date;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.santfeliu.util.script.FunctionFactory;

/**
 *
 * @author realor
 */
public abstract class ScriptableTask extends Task
{
  public static final String DEFAULT_SCRIPTABLE = "scriptable";

  private String refid = DEFAULT_SCRIPTABLE;

  public String getRefid()
  {
    return refid;
  }

  public void setRefid(String refid)
  {
    this.refid = refid;
  }

  public boolean hasVariable(String name)
  {
    AntScriptable scriptable = getScriptable();
    return scriptable.has(name, scriptable);
  }
  
  public void setVariable(String name, Object value)
  {
    AntScriptable scriptable = getScriptable();
    scriptable.put(name, scriptable, value);
  }

  public Object getVariable(String name)
  {
    AntScriptable scriptable = getScriptable();
    Object value = scriptable.get(name, scriptable);
    return toJava(value);
  }

  public void deleteVariable(String name)
  {
    AntScriptable scriptable = getScriptable();
    scriptable.delete(name);
  }

  public Object eval(String expression)
  {
    Object result;
    Context cx = ContextFactory.getGlobal().enterContext();
    try
    {
      AntScriptable scriptable = getScriptable(cx);
      result = toJava(cx.evaluateString(
        scriptable, expression, "<expression>", 1, null));
    }
    finally
    {
      Context.exit();
    }
    return result;
  }

  public AntScriptable getScriptable()
  {
    return getScriptable(null);
  }

  public AntScriptable getScriptable(Context cx)
  {
    Project taskProject = getProject();
    AntScriptable scriptable = (AntScriptable)taskProject.getReference(refid);

    if (scriptable == null)
    {
      // create new AntScriptable
      scriptable = new AntScriptable();      
      if (cx == null)
      {
        // init AntScriptable
        cx = ContextFactory.getGlobal().enterContext();
        ScriptRuntime.initStandardObjects(cx, scriptable, true);        
        Context.exit();
      }
      else
      {
        // init AntScriptable
        ScriptRuntime.initStandardObjects(cx, scriptable, true);
      }
      // init matrix functions
      FunctionFactory.initFunctions(scriptable);

      // update reference to AntScriptable
      if (!taskProject.getReferences().containsKey(refid))
      {
        taskProject.addReference(refid, scriptable);
      }
    }
    // update reference to project
    scriptable.setProject(taskProject);
    return scriptable;
  }

  public Object toJava(Object object)
  {
    if (object instanceof Scriptable)
    {
      String className = ((Scriptable)object).getClassName();
      if (className.equals("Boolean"))
      {
        object = Context.jsToJava(object, Boolean.class);
      }
      else if (className.equals("Number"))
      {
        object =  Context.jsToJava(object, Double.class);
      }
      else if (className.equals("String"))
      {
        object = object.toString();
      }
      else if (className.equals("Date"))
      {
        object = Context.jsToJava(object, Date.class);
      }
      else if (object instanceof NativeJavaObject)
      {
        object = ((NativeJavaObject)object).unwrap();
      }
    }
    return object;
  }
}
