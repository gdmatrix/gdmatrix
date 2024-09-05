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
import java.util.Calendar;
import java.util.Date;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.ConsString;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author realor
 */

/*
 * Usage: addDate(String internalDate (yyyyMMdd[HHmmss]),
                  Number incr, String units)
 * units:
 * 'y' : years
 * 'M' : months
 * 'd' : days
 * 'H' : hores
 * 'm' : minutes
 * 's' : segons
 *
 * returns: a String representing a internalDate (yyyyMMddHHmmss)
 *
 * Examples:
 *
 *   ${addDate(date, 5, "d")}
 *   ${addDate('20091011', 2, "M")}
 *   ${addDate('20091013124521', 8, "m")}
 */
public class AddDateFunction extends BaseFunction
{
  @Override
  public Object call(Context cx, Scriptable scope, Scriptable thisObj,
    Object[] args)
  {
    String dateString = null;
    if (args.length >= 2)
    {
      if (args[0] instanceof NativeJavaObject)
      {
        args[0] = Context.toString(args[0]);
      }
      if (args[1] instanceof NativeJavaObject)
      {
        args[1] = Context.toNumber(args[1]);
      }
      if (args[0] instanceof String && args[1] instanceof Number)
      {
        Date date = TextUtils.parseInternalDate(String.valueOf(args[0]));
        if (date != null)
        {
          int incr = ((Number)args[1]).intValue();

          Calendar calendar = Calendar.getInstance();
          calendar.setTime(date);
          calendar.add(getField(args), incr);
          date = calendar.getTime();

          // to internal format
          SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
          dateString = df.format(date);
        }
      }
    }
    return dateString;
  }

  private int getField(Object[] args)
  {
    int field = Calendar.DAY_OF_YEAR;
    if (args.length >= 3)
    {
      if (args[2] instanceof String || args[2] instanceof ConsString)
      {
        String fieldString = String.valueOf(args[2]);
        if (fieldString.equals("y"))
        {
          field = Calendar.YEAR;
        }
        else if (fieldString.equals("M"))
        {
          field = Calendar.MONTH;
        }
        else if (fieldString.equals("d"))
        {
          field = Calendar.DAY_OF_YEAR;
        }
        else if (fieldString.equals("H"))
        {
          field = Calendar.HOUR_OF_DAY;
        }
        else if (fieldString.equals("m"))
        {
          field = Calendar.MINUTE;
        }
        else if (fieldString.equals("s"))
        {
          field = Calendar.SECOND;
        }
      }
    }
    return field;
  }
}
