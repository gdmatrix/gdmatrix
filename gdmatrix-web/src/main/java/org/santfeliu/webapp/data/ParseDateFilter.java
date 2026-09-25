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
package org.santfeliu.webapp.data;

import io.pebbletemplates.pebble.error.PebbleException;
import io.pebbletemplates.pebble.extension.Filter;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 *
 * @author blanquepa
 */
public class ParseDateFilter implements Filter
{
  @Override
  public List<String> getArgumentNames()
  {
    return List.of("format");
  }

  @Override
  public Object apply(Object input, Map<String, Object> args,
    PebbleTemplate self, EvaluationContext context, int lineNumber)
    throws PebbleException
  {
    if (input == null)
    {
      return null;
    }

    String dateStr = input.toString();
    String formatPattern = (String) args.getOrDefault("format", "yyyyMMdd");

    try
    {
      SimpleDateFormat formatter = new SimpleDateFormat(formatPattern);
      return formatter.parse(dateStr);
    }
    catch (ParseException e)
    {
      throw new PebbleException(e, "Error parsing date '" + dateStr + 
        "' with '" + formatPattern + "' format", lineNumber, self.getName());
    }
  }
}
