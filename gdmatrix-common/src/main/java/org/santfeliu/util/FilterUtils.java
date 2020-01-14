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
package org.santfeliu.util;

/**
 *
 * @author realor
 */
public class FilterUtils
{
  public static final String WILDCARD = "%";
  public static final String REMOVE_SYMBOLS = "ÁAÀAÄAÉEÈEËEÍIÌIÏIÓOÒOÖOÚUÙUÜU";
  public static final String ADD_WILDCARD = "Á_À_Ä_É_È_Ë_Í_Ì_Ï_Ó_Ò_Ö_Ú_Ù_Ü_";

  public static String addWildcards(String pattern)
  {
    pattern = pattern.trim();
    if (pattern.startsWith("\"") && pattern.endsWith("\""))
    {
      pattern = pattern.substring(1, pattern.length() - 1);
    }
    else // add wildcards: %pattern%
    {
      if (!pattern.startsWith(WILDCARD))
      {
        pattern = WILDCARD + pattern;
      }
      if (!pattern.endsWith(WILDCARD))
      {
        pattern = pattern + WILDCARD;
      }
    }
    return pattern;
  }

  public static String replacePattern(String pattern, String replace)
  {
    pattern = pattern.trim().toUpperCase();

    char[] buffer = pattern.toCharArray();
    for (int i = 0; i < buffer.length; i++)
    {
      char ch = buffer[i];
      int index = 0;
      boolean found = false;
      while (index < replace.length() && !found)
      {
        if (replace.charAt(index) == ch) found = true;
        else index += 2;
      }
      if (found) buffer[i] = replace.charAt(index + 1);
    }
    pattern = new String(buffer);
    return pattern;
  }
}
