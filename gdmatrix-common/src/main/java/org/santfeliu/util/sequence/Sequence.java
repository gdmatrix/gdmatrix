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
package org.santfeliu.util.sequence;

import java.util.HashMap;
import org.santfeliu.util.template.Template;

/**
 *
 * @author blanquepa
 */
public class Sequence
{
  protected String counter;
  protected String value;
  
  public Sequence()
  {
  }
  
  public Sequence(String counter)
  {
    this(counter, null);
  }
  
  public Sequence(String counter, String value)
  {
    this.counter = counter;
    this.value = value;
  }

  public String getCounter()
  {
    return counter;
  }

  public void setCounter(String counter)
  {
    this.counter = counter;
  }

  public String getValue()
  {
    return getValue(null);
  }
  
  /* 
    Return the sequence value formated by JS expression.
    Example: format = "${year\"00000000\".substring(0, 8 - number.length) + number}"; 
   */
  public String getValue(String format)
  {
    String result = value;

    if (value != null && format != null)
    {
      HashMap vars = new HashMap();
      if (format.contains("year"))
      {
        vars.put("year", value.substring(0, 4));
        vars.put("number", value.substring(4));        
      }
      else
        vars.put("number", value);        
      Template template = Template.create(format);
      result = template.merge(vars);
    }

    return result;
  }

  public void setValue(String value)
  {
    this.value = value;
  }

}
