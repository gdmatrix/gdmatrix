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
package org.santfeliu.faces.convert;

import java.io.Serializable;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

/**
 *
 * @author realor
 */
public class IntervalConverter implements Converter, Serializable
{
  public Object getAsObject(FacesContext fc, UIComponent uic, String value)
  {
    if (value == null) return null;
    int totalSeconds = 0;
    value = value.trim();
    boolean negative;
    if (value.startsWith("-"))
    {
      value = value.substring(1);
      negative = true;
    }
    else
    {
      negative = false;
    }
    
    String[] parts = value.split(" ");
    for (String part : parts)
    {
      try
      {
        if (part.endsWith("h"))
        {
          part = part.substring(0, part.length() - 1);
          totalSeconds += Integer.parseInt(part) * 3600;
        }
        else if (part.endsWith("m"))
        {
          part = part.substring(0, part.length() - 1);
          totalSeconds += Integer.parseInt(part) * 60;
        }
        else if (part.endsWith("s"))
        {
          part = part.substring(0, part.length() - 1);
          totalSeconds += Integer.parseInt(part);
        }
      }
      catch (NumberFormatException ex)
      {
        // Ignore
      }
    }
    return negative ? -totalSeconds : totalSeconds;
  }

  public String getAsString(FacesContext fc, UIComponent uic, Object value)
  {
    if (value == null) return null;
    
    int totalSeconds = ((Number)value).intValue();
    if (totalSeconds == 0) return "0";
    boolean negative;
    if (totalSeconds < 0) 
    {
      totalSeconds = -totalSeconds;
      negative = true;
    }
    else 
    {
      negative = false;
    }
    int hours = totalSeconds / 3600;
    int minutes = (totalSeconds % 3600) / 60;
    int seconds = (totalSeconds % 3600) % 60;
    
    StringBuilder buffer = new StringBuilder();
    boolean timeSet = false;
    if (negative)
    {
      buffer.append("-");
    }
    else
    {
      String positive = (String)uic.getAttributes().get("positiveSignum");
      if ("true".equals(positive))
      {
        buffer.append("+");
      }
    }
    if (hours > 0)
    {
      buffer.append(hours);
      buffer.append("h");
      timeSet = true;
    }
    if (minutes > 0)
    {
      if (timeSet) buffer.append(" ");
      else timeSet = true;
      buffer.append(minutes);
      buffer.append("m");
    }
    if (seconds > 0)
    {
      if (timeSet) buffer.append(" ");
      buffer.append(seconds);
      buffer.append("s");      
    }
    return buffer.toString();
  }
}
