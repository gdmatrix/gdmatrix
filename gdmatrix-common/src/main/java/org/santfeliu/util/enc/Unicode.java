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

/**
 *
 * @author realor
 */
public class Unicode
{
  public static String encode(String text)
  {
    return encode(text, false);
  }

  public static String encode(String text, boolean extendedASCII)
  {
    return (extendedASCII ? encode(text, 0, 255) : encode(text, 32, 127));
  }

  public static String encode(String text, int ignoreCodeMin, int ignoreCodeMax)
  {
    if (text == null) return null;

    StringBuffer buffer = new StringBuffer();
    for (char ch : text.toCharArray())
    {
      if (ch == '\\')
      {
        buffer.append("\\\\");
      }
      else if (ch >= ignoreCodeMin && ch <= ignoreCodeMax)
      {
        buffer.append(ch);
      }
      else
      {
        String hexCode = "0000" + Integer.toHexString(ch);
        hexCode = hexCode.substring(hexCode.length() - 4);
        buffer.append("\\u" + hexCode);
      }
    }
    return buffer.toString();
  }

  public static String decode(String encodedText)
  {
    if (encodedText == null) return null;

    int state = 0;
    StringBuffer buffer = new StringBuffer();
    StringBuffer number = new StringBuffer();
    for (char ch : encodedText.toCharArray())
    {
      if (state == 0) // looking for \
      {
        if (ch == '\\') state = 1;
        else buffer.append(ch);
      }
      else if (state == 1) // looking for u
      {
        if (ch == 'u')
        {
          state = 2;
          number.setLength(0);
        }
        else
        {
          buffer.append('\\');
          if (ch != '\\')
          {
            buffer.append(ch);
          }
          state = 0;
        }
      }
      else if (state == 2) // looking number
      {
        number.append(ch);
        if (number.length() == 4)
        {
          try
          {
            int code = Integer.parseInt(number.toString(), 16);
            buffer.append((char)code);
            state = 0;
          }
          catch (Exception ex)
          {
          }
        }
      }
    }
    return buffer.toString();
  }

  public static void main(String[] args)
  {
    String s;
    s = Unicode.encode("Aixòòò és \\o &una [\u0456] prova´`", true);
    System.out.println(s);
    s = Unicode.decode(s);
    System.out.println(s);
  }
}
