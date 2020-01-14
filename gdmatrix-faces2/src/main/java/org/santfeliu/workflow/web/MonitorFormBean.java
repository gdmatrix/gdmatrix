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
package org.santfeliu.workflow.web;

import java.util.HashMap;
import java.util.Map;
import org.santfeliu.util.Properties;
import org.santfeliu.workflow.form.Form;

/**
 *
 * @author realor
 */
public class MonitorFormBean extends FormBean
{
  private static int MIN_REFRESH_TIME = 2; // in seconds
  private String message;
  private int refreshTime = MIN_REFRESH_TIME;
  private String progressVarName;
  private String endVarName;
  private String cancelVarName;
  private boolean cancelled = false;

  public String getMessage()
  {
    return message;
  }
  
  public String getProgress()
  {
    InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
    Object value = instanceBean.getVariables().get(progressVarName);
    return value == null ? null : value.toString();
  }

  public boolean isCancelButtonRendered()
  {
    return cancelVarName != null && endVarName != null;
  }

  public int getRefreshTime() // in millis
  {
    return refreshTime * 1000;
  }

  public String refresh()
  {
    InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
    if (endVarName == null)
    {
      return instanceBean.forward();
    }
    else
    {
      String outcome = instanceBean.updateInstance();
      Object value = instanceBean.getVariables().get(endVarName);
      boolean end = false;
      if (value instanceof Boolean)
      {
        end = ((Boolean)value).booleanValue();
      }
      else if (value instanceof String)
      {
        end = "true".equalsIgnoreCase(value.toString());
      }
      if (end && "monitor_form".equals(outcome))
      {
        return instanceBean.forward();
      }
      return outcome;
    }
  }

  public String cancel()
  {
    cancelled = true;

    InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
    return instanceBean.forward();
  }

  @Override
  public String show(Form form)
  {
    InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
    instanceBean.setForwardEnabled(false);
    instanceBean.setBackwardEnabled(false);
    
    Properties parameters = form.getParameters();

    Object value;
    value = parameters.get("message");
    if (value != null) message = String.valueOf(value);

    value = parameters.get("refreshTime");
    if (value != null)
    {
      try
      {
        refreshTime = (int)Double.parseDouble(value.toString());
        if (refreshTime < MIN_REFRESH_TIME) refreshTime = MIN_REFRESH_TIME;
      }
      catch (NumberFormatException ex)
      {
      }
    }

    value = parameters.get("progressVar");
    if (value != null) progressVarName = String.valueOf(value);

    value = parameters.get("endVar");
    if (value != null) endVarName = String.valueOf(value);

    value = parameters.get("cancelVar");
    if (value != null) cancelVarName = String.valueOf(value);

    return "monitor_form";
  }

  @Override
  public Map submit()
  {
    HashMap variables = new HashMap();
    if (cancelVarName != null)
    {
      variables.put(cancelVarName, Boolean.valueOf(cancelled));
    }
    return variables;
  }
}
