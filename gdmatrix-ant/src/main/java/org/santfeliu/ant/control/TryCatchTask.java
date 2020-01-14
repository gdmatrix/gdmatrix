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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Sequential;

/**
 *
 * @author realor
 */
public class TryCatchTask extends Task
{
  private String property;
  private Sequential _try;
  private Sequential _catch;
  private Sequential _finally;

  public String getProperty()
  {
    return property;
  }

  public void setProperty(String property)
  {
    this.property = property;
  }

  public void addTry(Sequential seq)
  {
    this._try = seq;
  }

  public void addCatch(Sequential seq)
  {
    this._catch = seq;
  }

  public void addFinally(Sequential seq)
  {
    this._finally = seq;
  }

  @Override
  public void execute() throws BuildException
  {
    if (_try == null) 
      throw new BuildException("Nested element 'try' is required");
    try
    {
      _try.perform();
    }
    catch (Exception ex)
    {
      if (property != null)
      {
        getProject().setProperty(property, ex.toString());
      }
      if (_catch != null) _catch.perform();
    }
    finally
    {
      if (_finally != null) _finally.perform();
    }
  }
}
