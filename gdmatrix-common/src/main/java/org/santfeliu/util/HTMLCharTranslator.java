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

import java.util.HashSet;
import java.util.Set;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class HTMLCharTranslator
{
  private static final String translationTable[][] =
  new String[][]
  {
    { "á", "&aacute;"},
    { "é", "&eacute;"},
    { "í", "&iacute;"},
    { "ó", "&oacute;"},
    { "ú", "&uacute;"},
    { "Á", "&Aacute;"},
    { "É", "&Eacute;"},
    { "Í", "&Iacute;"},
    { "Ó", "&Oacute;"},
    { "Ú", "&Uacute;"},
    { "à", "&agrave;"},
    { "è", "&egrave;"},
    { "ì", "&igrave;"},
    { "ò", "&ograve;"},
    { "ù", "&ugrave;"},
    { "À", "&Agrave;"},
    { "È", "&Egrave;"},
    { "Ì", "&Igrave;"},
    { "Ò", "&Ograve;"},
    { "Ù", "&Ugrave;"},
    { "ï", "&iuml;"},
    { "ü", "&uuml;"},
    { "Ï", "&Iuml;"},
    { "Ü", "&Uuml;"},
    { "·", "&middot;"},
    { "ñ", "&ntilde;"},
    { "Ñ", "&Ntilde;"},
    { "ç", "&ccedil;"},
    { "Ç", "&Ccedil;"},
    { "\"", "&quot;"},
    { "&", "&amp;"},
    { "<", "&lt;"},
    { ">", "&gt;"}
  };

  public HTMLCharTranslator()
  {
  }

  public static String toHTMLText(String ansiText)
  {
    return toHTMLText(ansiText, null);
  }

  public static String toHTMLText(String ansiText, Set<Character> ignoredChars)
  {
    if (ignoredChars == null) ignoredChars = new HashSet<Character>();
    StringBuffer buffer = new StringBuffer();
    for (int index = 0; index < ansiText.length(); index++)
    {
      char ch = ansiText.charAt(index);
      if ((ch >= 'A' && ch <= 'Z') ||
          (ch >= 'a' && ch <= 'z') ||
          (ch >= '0' && ch <= '9') ||
          (ignoredChars.contains(Character.valueOf(ch))))
      {
        buffer.append(ch);
      }
      else
      {
        buffer.append(translateChar(ch));
      }
    }
    return buffer.toString();
  }

  private static String translateChar(char ch)
  {
    boolean found = false;
    int index = 0;
    while (!found && index < translationTable.length)
    {
      if (translationTable[index][0].charAt(0) == ch)
      {
        found = true;
      }
      else
      {
        index++;
      }
    }
    return found? translationTable[index][1] :
                  new String(new char[]{ch});
  }
  

  public static String replaceCarryReturn(String ansiText)
  {
    StringBuffer buffer = new StringBuffer();
    for (int index = 0; index < ansiText.length(); index++)
    {
      char ch = ansiText.charAt(index);
      if ((ch >= 'A' && ch <= 'Z') ||
          (ch >= 'a' && ch <= 'z') ||
          (ch >= '0' && ch <= '9'))
      {
        buffer.append(ch);
      }
      else
      {
        buffer.append(replaceChar(ch));
      }
    }
    return buffer.toString();    
  }
  
  private static String replaceChar(char ch)
  {
    if (ch == '\n') return "<br>";
    else if (ch == '<') return "&lt;";
    else if (ch == '>') return "&gt;";      
    else return new String(new char[] {ch});
  }
  
  public static void main(String[] args)
  {
    System.out.println(HTMLCharTranslator.toHTMLText("&#8217;´a09ei&ouáéíóú"));
  }
}
