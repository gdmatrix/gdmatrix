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
import java.util.Locale;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.ConsString;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author realor
 */

/*
 * Usage: formatDate(String internalDate (yyyyMMdd[HHmmss]),
                     String outputPattern, String languageTag)
 *
 * returns: a String representing internalDate in outputPattern
 *   (see SimpleDateFormat)
 */
public class FormatDateFunction extends BaseFunction
{
  @Override
  public Object call(Context cx, Scriptable scope, Scriptable thisObj,
    Object[] args)
  {
    String dateString = null;
    if (args.length >= 1)
    {
      if (args[0] != null)
      {
        Date date = TextUtils.parseInternalDate(Context.toString(args[0]));
        if (date != null)
        {
          SimpleDateFormat df = new SimpleDateFormat(getPattern(args), getLocale(args));
          dateString = df.format(date);
        }
      }
    }
    return dateString;
  }

  private String getPattern(Object[] args)
  {
    String pattern = "dd/MM/yyyy";
    if (args.length >= 2)
    {
      Object arg = args[1];
      if (arg instanceof String || arg instanceof ConsString)
      {
        pattern = String.valueOf(arg);
      }
    }
    return pattern;
  }
  
  private Locale getLocale(Object[] args)
  {
    if (args.length >= 3)
    {
      Object arg = args[2];
      if (arg instanceof String || arg instanceof ConsString)
      {
        String langTag = String.valueOf(arg);
        return Locale.forLanguageTag(langTag);
      }      
    }
    return Locale.getDefault();
  }
}