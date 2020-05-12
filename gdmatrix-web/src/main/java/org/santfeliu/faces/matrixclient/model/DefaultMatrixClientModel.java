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
package org.santfeliu.faces.matrixclient.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author blanquepa
 */
public class DefaultMatrixClientModel implements Serializable, HtmlMatrixClientModel
{
  protected static final String EXCEPTION = "exception";
  protected static final String RESULT = "result";
  
  Map<String,Object> parameters;
  Map<String,Object> result;

  public DefaultMatrixClientModel()
  {
    parameters = new HashMap();
    result = new HashMap();
  }

  public Map getParameters()
  {
    return parameters;
  }
  
  public Object getParameter(String name)
  {
    return parameters.get(name);
  }
  
  public void putParameter(String name, Object value)
  {
    if (parameters == null)
      parameters = new HashMap();
    
    parameters.put(name, value);
  }
  
  public void putParameters(Map parameters)
  {
    if (parameters == null)
      parameters = new HashMap();
    
    this.parameters.putAll(parameters);
  }

  public Map getResult()
  {
    return result;
  }

  public void setResult(Map result)
  {
    this.result = result;
  }
  
  public Object parseResult() throws Exception
  {
    if (result != null)
    {
      String exception = (String)result.get(EXCEPTION);
      if (exception != null)
        throw new Exception(exception);
      return result.get(RESULT);
    }
    else
      return null;
  }   
  
  public void reset()
  {
    parameters.clear();
    result.clear();
  }
}
