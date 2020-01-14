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
 * @author unknown
 */
public class TextFormat
{
  public TextFormat()
  {
  }
  
  /**
   * Separates a text string in parts of less or equal length than specified in 
   * the <code>length</code> parameter. 
   * @param text 
   * @param length 
   * @param separator String separator of the splitted parts
   * @return The separated text String
   */
  public static String split(String text, int length, String separator)
  {
    if (text != null)
    {
      StringBuffer result = new StringBuffer();
      String[] words = text.split(separator);
      
      for (int i = 0; i < words.length; i++)
      {
        String word = words[i];
        while (word.length() > length)
        {
          result.append(word.substring(0, length) + separator);
          word = word.substring(length);
        }
        if (i == words.length - 1) separator = "";
        result.append(word + separator);
      }

      return result.toString();
    }
    return text;
  }
  
  public static String split(String text, int length)
  {
    return split(text, length, " ");
  }

  public static void main(String[] args)
  {
    String largeText = "";
    
    largeText = TextFormat.split(largeText, 4, " ");

    System.out.println(largeText);  
  }  
}
