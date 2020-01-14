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
package org.santfeliu.web.obj.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.santfeliu.web.WebBean;

/**
 *
 * @author blanquepa
 */

public class ParametersManager extends WebBean implements Serializable
{
  private ArrayList<ParametersProcessor> processors;
  
  public ParametersManager()
  {
    this.processors = new ArrayList();
  }
  
  public void addProcessor(ParametersProcessor processor)
  {
    this.processors.add(processor);
  }
    
  public String processParameters()
  {
    String outcome = null;
    Map parameters = getRequestParameters();
    for (ParametersProcessor processor : this.processors)
    {
      outcome = processor.processParameters(parameters);
      if (outcome != null) return outcome;
    }
    return outcome;
  }
    
  private Map getRequestParameters()
  {
    // Discard POST parameters
    HashMap qsMap = new HashMap();
    
    Map requestMap = getExternalContext().getRequestParameterMap();
    HttpServletRequest request = 
      (HttpServletRequest)getExternalContext().getRequest();
    String qs = request.getQueryString();

    if (qs != null)
    {
      for (Object key : requestMap.keySet())
      {
        if (qs.contains("?" + String.valueOf(key) + "=") || 
            qs.contains("&" + String.valueOf(key) + "="))
          qsMap.put(key, requestMap.get(key));
      }
    }    
    return qsMap;
  } 
}
