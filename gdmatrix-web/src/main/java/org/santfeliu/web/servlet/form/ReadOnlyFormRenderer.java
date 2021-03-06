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
package org.santfeliu.web.servlet.form;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import org.santfeliu.form.View;

/**
 *
 * @author realor
 */
public class ReadOnlyFormRenderer extends EditableFormRenderer
{
  @Override
  protected void writeAttributes(View view, Writer writer,
    String ...excluded) throws IOException
  {
    Collection<String> propertyNames = view.getPropertyNames();
    for (String propertyName : propertyNames)
    {
      boolean isExcluded = propertyName.toLowerCase().equals("disabled") ||
        specialAttributes.contains(propertyName.toLowerCase());
      int i = 0;
      while (!isExcluded && i < excluded.length)
      {
        isExcluded = propertyName.equalsIgnoreCase(excluded[i]);
        i++;
      }
      if (!isExcluded)
      {
        String value = String.valueOf(view.getProperty(propertyName));
        writer.write(" " + propertyName + "=\"" + value + "\"");
      }
    }
    String tag = view.getNativeViewType().toLowerCase();
    if (tag.equals("input") || tag.equals("select") || tag.equals("textarea"))
    {
      writer.write(" disabled");
    }
  }
}
