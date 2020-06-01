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

/**
 *
 * @author realor
 */
public class SQLDocument extends HighlightedDocument
{
  public static final int UNKNOW = 0;
  public static final int KEYWORD = 1;
  public static final int IDENTIFIER = 2;
  public static final int NUMBER = 3;
  public static final int STRING = 4;
  public static final int OPERATOR = 5;
  public static final int COMMENT = 6;
  public static final int ALIAS = 7;
  public static final int VARIABLE = 8;

  public static final Set keywordsSet = new HashSet();

  static
  {
    keywordsSet.add("select");
    keywordsSet.add("from");
    keywordsSet.add("where");
    keywordsSet.add("or");
    keywordsSet.add("and");
    keywordsSet.add("not");
    keywordsSet.add("in");
    keywordsSet.add("into");
    keywordsSet.add("insert");
    keywordsSet.add("update");
    keywordsSet.add("delete");
    keywordsSet.add("between");
    keywordsSet.add("like");
    keywordsSet.add("values");
    keywordsSet.add("call");
    keywordsSet.add("drop");
    keywordsSet.add("cascade");
    keywordsSet.add("grant");
    keywordsSet.add("alter");
    keywordsSet.add("create");
    keywordsSet.add("table");
    keywordsSet.add("view");
    keywordsSet.add("lock");
    keywordsSet.add("group");
    keywordsSet.add("by");
    keywordsSet.add("order");
    keywordsSet.add("asc");
    keywordsSet.add("desc");
    keywordsSet.add("set");
  }

  public SQLDocument()
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
      Token token = newToken(0, UNKNOW);

      int index = 0;
      while (index < text.length())
      {
        char ch = text.charAt(index);
        switch (state)
        {
          case 0: // skip blanks
            if (isSeparator(ch))
            {
              index++;
            }
            else if (Character.isLetter(ch))
            {
              state = 1;
              token = newToken(index++, IDENTIFIER, ch);
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
              token = newToken(index++, ALIAS);
            }
            else if (ch == '/')
            {
              state = 5;
              token = newToken(index++, UNKNOW);
            }
            else if (ch == '{')
            {
              state = 10;
              token = newToken(index++, VARIABLE, ch);
            }
            else
            {
              state = 6;
              token = newToken(index++, OPERATOR, ch);
            }
            break;

          case 1: // process word
            if (isSeparator(ch))
            {
              state = 0;
              index++;
            }
            else if (isLetterOrDigit(ch))
            {
              token.append(ch);
              index++;
            }
            else
            {
              state = 0;
            }
            if (keywordsSet.contains(token.getValue().toLowerCase()))
              token.type = KEYWORD;
            else token.type = IDENTIFIER;
            break;

          case 2: // process number
            if (isSeparator(ch))
            {
              state = 0;
              index++;
            }
            else if (isLetterOrDigit(ch) || ch == '.')
            {
              token.append(ch);
              index++;
            }
            else
            {
              state = 6;
              token = newToken(index++, OPERATOR, ch);
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

          case 5: // process possible comments
            if (ch == '/')
            {
              state = 7;
              token.type = COMMENT;
              index++;
            }
            else if (ch == '*')
            {
              state = 8;
              token.type = COMMENT;
              index++;
            }
            else
            {
              token.type = OPERATOR;
              token.setValue("/");
              state = 0;
            }
            break;

          case 6: // process operators
            if (isSeparator(ch))
            {
              state = 0;
              index++;
            }
            else if (isLetterOrDigit(ch))
            {
              state = 0;
            }
            else if (ch == '\'' || ch == '\"' || ch == '/')
            {
              state = 0;
            }
            else if (ch == '{')
            {
              state = 0;
            }
            else
            {
              token.append(ch);
              index++;
            }
            break;

          case 7:
            if (ch == '\n')
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

          case 8:
            if (ch == '*')
            {
              state = 9;
              index++;
            }
            else
            {
              token.append(ch);
              index++;
            }
            break;

          case 9:
            if (ch == '/')
            {
              state = 0;
              index++;
            }
            else if (ch == '*')
            {
              index++;
            }
            else
            {
              state = 8;
              index++;
            }
            break;

          case 10:
            if (ch == '}')
            {
              state = 0;
              index++;
            }
            else
            {
              token.append(ch);
              index++;
            }
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
}
