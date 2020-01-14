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
package org.santfeliu.ant.control;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Sequential;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.resources.FileResource;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.santfeliu.ant.js.AntScriptable;
import org.santfeliu.ant.js.ScriptableTask;

/**
 *
 * @author real
 */
public class ForEachTask extends ScriptableTask
{
  private String var;
  private String in;
  private String inVar;
  private FileSet fileSet;
  private Sequential _do;

  public String getVar()
  {
    return var;
  }

  public void setVar(String var)
  {
    this.var = var;
  }

  public String getIn()
  {
    return in;
  }

  public void setIn(String in)
  {
    this.in = in;
  }

  public String getInVar()
  {
    return inVar;
  }

  public void setInVar(String inVar)
  {
    this.inVar = inVar;
  }

  public void add(FileSet fileSet)
  {
    this.fileSet = fileSet;
  }

  public void addDo(Sequential _do)
  {
    this._do = _do;
  }

  @Override
  public void execute() throws BuildException
  {
    if (var == null) throw new BuildException("Attribute 'var' is required");
    if (_do == null)
      throw new BuildException("Nested element 'do' is required");
    if (in == null && inVar == null && fileSet == null)
      throw new BuildException(
        "Attribute 'in' or 'inVar' or nested element 'fileset' is required");

    AntScriptable scriptable = getScriptable();
    if (in != null)
    {
      String values[] = in.split(",");
      for (String value : values)
      {
        executeChildren(value, scriptable);
      }
    }
    else if (inVar != null)
    {
      Object value = toJava(scriptable.get(inVar, scriptable));
      if (value instanceof Collection)
      {
        Collection collection = (Collection)value;
        for (Object elem : collection)
        {
          executeChildren(elem, scriptable);
        }
      }
      else if (value.getClass().isArray())
      {
        int length = Array.getLength(value);
        for (int i = 0; i < length; i++)
        {
          Object item = Array.get(value, i);
          executeChildren(item, scriptable);
        }
      }
      else if (value instanceof NativeArray)
      {
        NativeArray array = (NativeArray)value;
        long length = array.getLength();
        for (int i = 0; i < length; i++)
        {
          Object item = array.get(i, scriptable);
          executeChildren(item, scriptable);
        }
      }
    }
    else if (fileSet != null)
    {
      Iterator iter = fileSet.iterator();
      while (iter.hasNext())
      {
        FileResource fileResource = (FileResource)iter.next();
        executeChildren(fileResource, scriptable);
      }
    }
  }

  private void executeChildren(Object elem, Scriptable scriptable)
  {
    scriptable.put(var, scriptable, elem);
    _do.perform();
  }
}
