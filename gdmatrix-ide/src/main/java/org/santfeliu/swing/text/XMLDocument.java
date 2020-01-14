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
package org.santfeliu.swing.text;

import javax.swing.text.BadLocationException;

/**
 *
 * @author unknown
 */
public class XMLDocument extends HighlightedDocument
{
  public static final int UNKNOW = 0;
  public static final int TAG = 1;
  public static final int ATTRIBUTE = 2;  
  public static final int STRING = 3;
  public static final int NUMBER = 4;
  public static final int TEXT = 5;
  public static final int SYMBOL = 6;
  public static final int COMMENT = 7;

  public XMLDocument()
  {
  }
  
  @Override
  protected void parse()
  {
    try
    {
      tokens.clear();
      String text = getText(0, getLength());

      int state = 0;
      boolean inTag = false;
      boolean firstWord = false;
      Token token = newToken(0, UNKNOW);

      int index = 0;
      while (index < text.length())
      {
        char ch = text.charAt(index);
        switch (state)
        {
          case 0: // skip blanks
            if (inTag)
            {
              if (isSeparator(ch))
              {
                index++;
              }
              else if (Character.isLetter(ch))
              {
                if (firstWord)
                {
                  state = 1;
                  token = newToken(index++, TAG, ch);
                  firstWord = false;
                }
                else
                {
                  state = 1;
                  token = newToken(index++, ATTRIBUTE, ch);
                }
              }
              else if (Character.isDigit(ch))
              {
                state = 2;
                token = newToken(index++, NUMBER, ch);
              }
              else if (ch == '\'')
              {
                state = 3;
                token = newToken(index++, STRING);
              }
              else if (ch == '\"')
              {
                state = 4;
                token = newToken(index++, STRING);
              }
              else if (ch == '>')
              {
                token = newToken(index++, SYMBOL);
                inTag = false;
              }
              else
              {
                token = newToken(index++, SYMBOL);
              }
            }
            else // !inTag
            {
              if (isComming(index, "<!--"))
              {
                state = 6;
                token = newToken(index, COMMENT);
                token.setValue("<!--");
                index += 4;
              }
              else if (ch == '<')
              {
                inTag = true;
                firstWord = true;
                token = newToken(index++, SYMBOL);
              }
              else               
              {
                token = newToken(index++, TEXT, ch);
                state = 5;
              }
            }
            break;

          case 1: // process word
            if (isLetterOrDigit(ch) || ch == ':'  || ch == '-' || ch == '_')
            {
              token.append(ch);
              index++;
            }
            else
            {
              state = 0;
            }
            break;
            
          case 2: // process number
            if (isLetterOrDigit(ch) || ch == '.')
            {
              token.append(ch);
              index++;
            }
            else
            {
              state = 0;
            }
            break;

          case 3: // process string 'aaaa'
            if (ch == '\n' || ch == '\'')
            {
              state = 0;
              index++;
            }
            else
            {
              token.append(ch);
              index++;
            }
            break;

          case 4: // process alias "aaaa"
            if (ch == '\n' || ch == '\"')
            {
              state = 0;
              index++;
            }
            else
            {
              token.append(ch);
              index++;
            }
            break;
            
          case 5: // text
            if (ch == '<')
            {
              state = 0;
            }
            else
            {
              token.append(ch);
              index++;
            }
            break;

          case 6: // inside comment
            if (isComming(index, "-->"))
            {
              token.setValue("-->");
              index += 3;
              state = 0;
            }
            else
            {
              token.append(ch);
              index++;
            }
            break;
        }
      }
      newToken(getLength(), UNKNOW);
    }
    catch (Exception ex)
    {
    }
  }

  private boolean isSeparator(char ch)
  {
    return ch == ' ' || ch == '\n' || ch == '\t';
  }

  private boolean isLetterOrDigit(char ch)
  {
    return Character.isLetter(ch) || Character.isDigit(ch) || ch == '_';    
  }

  private boolean isComming(int index, String text)
  {
    if (getLength() >= index + text.length())
    {
      try
      {
        return text.equals(getText(index, text.length()));
      }
      catch (BadLocationException ex)
      {
      }
    }
    return false;
  }
}
