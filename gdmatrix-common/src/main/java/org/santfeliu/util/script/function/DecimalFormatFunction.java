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

import java.text.DecimalFormat;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 *
 * @author realor
 */

/*
 * Usage: decimalFormat(Number number, String decimalFormat)
 *
 * returns: a String representing number in format decimalFormat
 */
public class DecimalFormatFunction extends BaseFunction
{
  public DecimalFormatFunction()
  {
  }

  @Override
  public Object call(Context cx, Scriptable scope, Scriptable thisObj,
    Object[] args)
  {
    if (args.length >= 1)
    {
      Object value = args[0];
      if (args.length >= 2 && value instanceof Number)
      {
        Object fmt = args[1];
        if (fmt == null)
        {
          return value.toString();
        }
        else
        {
          double number = ((Number)value).doubleValue();
          DecimalFormat df = new DecimalFormat(fmt.toString());
          return df.format(number);
        }
      }
      else if (value != null)
      {
        return value.toString();
      }
    }
    return "";
  }
}
