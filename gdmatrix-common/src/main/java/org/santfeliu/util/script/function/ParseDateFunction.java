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

import java.text.SimpleDateFormat;
import java.util.Date;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author realor
 */

/*
 * Usage: parseDate(String userDate, String inputPattern)
 *
 * returns: a String internalDate (yyyyMMddHHmmss) representing userDate.
 *   (see SimpleDateFormat)
 */
public class ParseDateFunction extends BaseFunction
{
  @Override
  public Object call(Context cx, Scriptable scope, Scriptable thisObj,
    Object[] args)
  {
    String dateString = null;
    if (args.length >= 1)
    {
      String value = Context.toString(args[0]);
      Date date = TextUtils.parseUserDate(value, getPattern(args));
      if (date != null)
      {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        dateString = df.format(date);
      }
    }
    return dateString;
  }

  private String getPattern(Object[] args)
  {
    String pattern = "dd/MM/yyyy";
    if (args.length >= 2)
    {
      pattern = Context.toString(args[1]);
    }
    return pattern;
  }
}
