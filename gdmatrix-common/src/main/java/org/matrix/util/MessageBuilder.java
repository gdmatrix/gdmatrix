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
package org.matrix.util;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 *
 * @author realor
 */
public class MessageBuilder
{
  private static final char MODULE_SEPARATOR = ':';

  public static String getMessage(String messageId)
    throws MissingResourceException
  {
    Locale locale = Locale.getDefault();
    return getMessage(messageId, locale);
  }

  public static String getMessage(String messageId, Locale locale)
    throws MissingResourceException
  {
    ResourceBundle bundle = null;
    String localMessageId = null;

    int index = messageId.indexOf(MODULE_SEPARATOR);
    if (index != -1)
    {
      String moduleName = messageId.substring(0, index);
      String bundleName = "org.matrix." + moduleName +
        ".resources.MessageBundle";
      bundle = ResourceBundle.getBundle(bundleName, locale);
      localMessageId = messageId.substring(index + 1);
    }
    if (bundle == null)
    {
      bundle = ResourceBundle.getBundle(
        "org.matrix.resources.MessageBundle", locale);
      localMessageId = messageId;
    }
    return bundle.getString(localMessageId);
  }
}
