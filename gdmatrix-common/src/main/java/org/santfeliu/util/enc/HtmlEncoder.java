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
package org.santfeliu.util.enc;

public abstract class HtmlEncoder
{
  /**
   * Variant of {@link #encode} where encodeNewline is false and encodeNbsp is true.
   */
  public static String encode(String string)
  {
    return encode(string, false, true);
  }

  /**
   * Variant of {@link #encode} where encodeNbsp is true.
   */
  public static String encode(String string, boolean encodeNewline)
  {
    return encode(string, encodeNewline, true);
  }

  /**
   * Variant of {@link #encode} where encodeNbsp and encodeNonLatin are true
   */
  public static String encode(String string, boolean encodeNewline, 
                              boolean encodeSubsequentBlanksToNbsp)
  {
    return encode(string, encodeNewline, encodeSubsequentBlanksToNbsp, 
                  true);
  }

  /**
   * Encodes the given string, so that it can be used within a html page.
   * @param string the string to convert
   * @param encodeNewline if true newline characters are converted to &lt;br&gt;'s
   * @param encodeSubsequentBlanksToNbsp if true subsequent blanks are converted to &amp;nbsp;'s
   * @param encodeNonLatin if true encode non-latin characters as numeric character references
   */
  public static String encode(String string, boolean encodeNewline, 
                              boolean encodeSubsequentBlanksToNbsp, 
                              boolean encodeNonLatin)
  {
    if (string == null)
    {
      return "";
    }

    StringBuffer sb = null; //create later on demand
    String app;
    char c;
    for (int i = 0; i < string.length(); ++i)
    {
      app = null;
      c = string.charAt(i);
      boolean isSurrogatePair = false;
      switch (c)
      {
        case '"':
          app = "&quot;";
          break; //"
        case '&':
          app = "&amp;";
          break; //&
        case '<':
          app = "&lt;";
          break; //<
        case '>':
          app = "&gt;";
          break; //>
        case ' ':
          if (encodeSubsequentBlanksToNbsp && 
              (i == 0 || (i - 1 >= 0 && string.charAt(i - 1) == ' ')))
          {
            //Space at beginning or after another space
            app = "&#160;";
          }
          break;
        case '\n':
          if (encodeNewline)
          {
            app = "<br/>";
          }
          break;


        default:
          if (encodeNonLatin)
            switch (c)
            {
                //german umlauts
              case '\u00E4':
                app = "&auml;";
                break;
              case '\u00C4':
                app = "&Auml;";
                break;
              case '\u00F6':
                app = "&ouml;";
                break;
              case '\u00D6':
                app = "&Ouml;";
                break;
              case '\u00FC':
                app = "&uuml;";
                break;
              case '\u00DC':
                app = "&Uuml;";
                break;
              case '\u00DF':
                app = "&szlig;";
                break;

                //misc
                //case 0x80: app = "&euro;"; break;  sometimes euro symbol is ascii 128, should we suport it?
              case '\u20AC':
                app = "&euro;";
                break;
              case '\u00AB':
                app = "&laquo;";
                break;
              case '\u00BB':
                app = "&raquo;";
                break;
              case '\u00A0':
                app = "&#160;";
                break;

              default:
                if (((int) c) >= 0x80)
                {
                  //encode all non basic latin characters                
                  if (i < string.length() - 1 && 
                    Character.isSurrogatePair(c, string.charAt(i + 1)))
                  {
                    app = "&#x" + Long.toHexString(string.codePointAt(i)) + ";";
                    isSurrogatePair = true;
                  }
                  else
                  {
                    app = "&#" + ((int) c) + ";";
                  }
                }
                break;
            }
          break;
      }
      if (app != null)
      {
        if (sb == null)
        {
          sb = new StringBuffer(string.substring(0, i));
        }
        sb.append(app);
      }
      else
      {
        if (sb != null)
        {
          sb.append(c);
        }
      }
      if (isSurrogatePair) i++;
    }
    if (sb == null)
    {
      return string;
    }
    else
    {
      return sb.toString();
    }
  }
  
  public static void main(String args[])
  {
    System.out.println(HtmlEncoder.encode("Això és una > que üliop"));
  }
}
