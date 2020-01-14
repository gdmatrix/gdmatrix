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

import java.util.HashSet;
import java.util.Set;
import javax.swing.text.BadLocationException;

/**
 *
 * @author blanquepa
 */
public class ReportTemplateDocument extends HighlightedDocument
{
  public static final int UNKNOW = 0;
  public static final int HTML_TAG = 1;
  public static final int HTML_ATTRIBUTE = 2;  
  public static final int HTML_STRING = 3;
  public static final int HTML_NUMBER = 4;
  public static final int HTML_TEXT = 5;
  public static final int HTML_SYMBOL = 6;
  public static final int HTML_COMMENT = 7;  
  
  public static final int JS_KEYWORD = 11;
  public static final int JS_IDENTIFIER = 12;
  public static final int JS_NUMBER = 13;
  public static final int JS_STRING = 14;
  public static final int JS_OPERATOR = 15;
  public static final int JS_COMMENT = 16;
  public static final int SCRIPT = 17;
//  public static final int BRACKET = 18;
  
  public static final Set keywordsSet = new HashSet();
  
  static
  {
    keywordsSet.add("boolean");
    keywordsSet.add("break");
    keywordsSet.add("byte");
    keywordsSet.add("case");
    keywordsSet.add("catch");
    keywordsSet.add("char");
    keywordsSet.add("class");
    keywordsSet.add("const");    
    keywordsSet.add("continue");
    keywordsSet.add("default");
    keywordsSet.add("do");
    keywordsSet.add("double");
    keywordsSet.add("else");
    keywordsSet.add("extends");
    keywordsSet.add("false");
    keywordsSet.add("final");
    keywordsSet.add("finally");
    keywordsSet.add("float");
    keywordsSet.add("for");
    keywordsSet.add("function");
    keywordsSet.add("goto");
    keywordsSet.add("if");
    keywordsSet.add("implements");
    keywordsSet.add("import");
    keywordsSet.add("instanceof");
    keywordsSet.add("in");
    keywordsSet.add("int");
    keywordsSet.add("interface");
    keywordsSet.add("long");
    keywordsSet.add("native");
    keywordsSet.add("new");
    keywordsSet.add("null");
    keywordsSet.add("package");
    keywordsSet.add("private");
    keywordsSet.add("protected");
    keywordsSet.add("public");
    keywordsSet.add("return");
    keywordsSet.add("short");
    keywordsSet.add("static");
    keywordsSet.add("super");
    keywordsSet.add("switch");
    keywordsSet.add("synchronized");
    keywordsSet.add("this");
    keywordsSet.add("throw");
    keywordsSet.add("throws");
    keywordsSet.add("transient");
    keywordsSet.add("true");
    keywordsSet.add("try");
    keywordsSet.add("var");
    keywordsSet.add("void");
    keywordsSet.add("volatile");
    keywordsSet.add("while");
    keywordsSet.add("with");
  }

  public ReportTemplateDocument()
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
                  token = newToken(index++, HTML_TAG, ch);
                  firstWord = false;
                }
                else
                {
                  state = 1;
                  token = newToken(index++, HTML_ATTRIBUTE, ch);
                }
              }
              else if (Character.isDigit(ch))
              {
                state = 2;
                token = newToken(index++, HTML_NUMBER, ch);
              }
              else if (ch == '\'')
              {
                state = 3;
                token = newToken(index++, HTML_STRING);
              }
              else if (ch == '\"')
              {
                state = 4;
                token = newToken(index++, HTML_STRING);
              }
              else if (ch == '>')
              {
                token = newToken(index++, HTML_SYMBOL);
                inTag = false;
              }
              else
              {
                token = newToken(index++, HTML_SYMBOL);
              }
            }
            else // !inTag
            {
              if (isComming(index, "<!--"))
              {
                state = 6;
                token = newToken(index, HTML_COMMENT);
                token.setValue("<!--");
                index += 4;
              }
              else if (isComming(index, "<%"))
              {
                state = 10;
                token = newToken(index, SCRIPT);
                token.setValue("<%");
                index += 2;
              }
              else if (ch == '<')
              {
                inTag = true;
                firstWord = true;
                token = newToken(index++, HTML_SYMBOL);
              }
              else               
              {
                token = newToken(index++, HTML_TEXT, ch);
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
          case 10: //scripting
            if (isComming(index, "%>"))
            {
              token = newToken(index, SCRIPT, ch);
              token.setValue("%>");
              index += 2;
              state = 0;
            }
            else
            {
              if (isSeparator(ch))
              {
                index++;
              }
              else if (Character.isLetter(ch))
              {
                state = 11;
                token = newToken(index++, JS_IDENTIFIER, ch);
              }
              else if (Character.isDigit(ch))
              {
                state = 12;
                token = newToken(index++, JS_NUMBER, ch);
              }
              else if (ch == '\'')
              {
                state = 13;
                token = newToken(index++, JS_STRING);
              }            
              else if (ch == '\"')
              {
                state = 14;
                token = newToken(index++, JS_STRING);
              }
              else if (ch == '/')
              {
                state = 15;
                token = newToken(index++, SCRIPT);
              }
              else
              {
                state = 16;
                token = newToken(index++, JS_OPERATOR, ch); 
              }
            }
            break;
            case 11: // process word
              if (isSeparator(ch))
              {
                state = 10;
                index++;              
              }
              else if (isLetterOrDigit(ch))
              {
                token.append(ch);
                index++;
              }
              else
              {
                state = 10;
              }
              if (keywordsSet.contains(token.getValue())) token.type = JS_KEYWORD;
              else token.type = JS_IDENTIFIER;
              break;

            case 12: // process number
              if (isSeparator(ch))
              {
                state = 10;
                index++;
              }
              else if (isLetterOrDigit(ch) || ch == '.')
              {
                token.append(ch);
                index++;
              }
              else
              {
                state = 16;
                token = newToken(index++, JS_OPERATOR, ch);              
              }
              break;

            case 13: // process string 'aaaa'
              if (ch == '\n' || ch == '\'')
              {
                state = 10;
                index++;
              }
              else
              {
                token.append(ch);
                index++;
              }
              break;

            case 14: // process string "aaaa"
              if (ch == '\n' || ch == '\"')
              {
                state = 10;
                index++;
              }
              else
              {
                token.append(ch);
                index++;
              }
              break;

            case 15: // process possible comments
              if (ch == '/')
              {
                state = 17;
                token.type = JS_COMMENT;
                index++;
              }
              else if (ch == '*')
              {
                state = 18;
                token.type = JS_COMMENT;
                index++;              
              }
              else
              {
                token.type = JS_OPERATOR;
                token.setValue("/");
                state = 10;
              }
              break;

            case 16: // process operators
              if (isSeparator(ch))
              {
                state = 10;
                index++;
              }
              else if (isLetterOrDigit(ch))
              {
                state = 10;
              }
              else if (ch == '\'' || ch == '\"' || ch == '/' || ch == '\n')
              {
                state = 10;
              }
              else
              {
                token.append(ch);
                index++;
              }
              break;

            case 17:
              if (ch == '\n')
              {
                state = 10;
                index++;
              }
              else
              {
                token.append(ch);
                index++;
              }
              break;

            case 18:
              if (ch == '*')
              {
                state = 19;
                index++;
              }
              else
              {
                token.append(ch);
                index++;              
              }
              break;

            case 19:
              if (ch == '/')
              {
                state = 10;
                index++;
              }
              else if (ch == '*')
              {
                index++;
              }
              else
              {
                state = 18;
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
  
  @Override
  public void forceRepaint()
  {
    if (System.currentTimeMillis() > nextRepaint)
    {
      this.nextRepaint = System.currentTimeMillis() + repaintInterval;
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
