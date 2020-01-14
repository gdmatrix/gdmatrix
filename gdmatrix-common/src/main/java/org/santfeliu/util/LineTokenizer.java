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

import java.util.Vector;


/**
 *
 * @author unknown
 */
public class LineTokenizer
{
  protected static final String defaultDelimiters = " \t";
  protected Vector tokens;
  protected int token;

  public LineTokenizer(String line)
  {
    tokens = parseLine(line, defaultDelimiters, false);
    token = 0;
  }

  public LineTokenizer(String line, String delimiters)
  {
    tokens = parseLine(line, delimiters, false);
    token = 0;
  }

  public LineTokenizer(String line, String delimiters, boolean keepQuotes)
  {
    tokens = parseLine(line, delimiters, keepQuotes);
    token = 0;
  }

  public int countTokens()
  {
    return tokens.size();
  }

  public boolean hasMoreTokens()
  {
    return token < tokens.size();
  }

  public String nextToken()
  {
    return (String)tokens.elementAt(token++);
  }

  public String getTokenAt(int index)
  {
    return (String)tokens.elementAt(index);
  }

  private Vector parseLine(String line, String delimiters, boolean keepQuotes)
  {
    Vector vector = new Vector();
    StringBuffer buffer = new StringBuffer();

    int state = 0;
    int index = 0;
    while (index < line.length())
    {
      char ch = line.charAt(index);
      switch (state)
      {
        case 0: // skip delimiters, new argument
        {
          if (ch == '"')
          {
            state = 2;
            buffer.setLength(0);
            if (keepQuotes) buffer.append(ch);
          }
          else if (ch == '\'')
          {
            state = 3;
            buffer.setLength(0);
            if (keepQuotes) buffer.append(ch);
          }
          else if (delimiters.indexOf(ch) == -1) // is not a delimiter
          {
            state = 1;
            buffer.setLength(0);
            buffer.append(ch);
          }
        }; break;

        case 1:
        {
          if (ch == '"')
          {
            state = 2;
            if (keepQuotes) buffer.append(ch);
          }
          else if (ch == '\'')
          {
            state = 3;
            if (keepQuotes) buffer.append(ch);            
          }
          else if (delimiters.indexOf(ch) != -1) // is delimiter
          {
            state = 0;
            vector.addElement(buffer.toString());
            buffer.setLength(0);
          }
          else
          {
            buffer.append(ch);
          }
        }; break;

        case 2:
        {
          if (ch == '"')
          {
            state = 1;
            if (keepQuotes) buffer.append(ch);
          }
          else
          {
            buffer.append(ch);
          }
        }; break;

        case 3:
        {
          if (ch == '\'')
          {
            state = 1;
            if (keepQuotes) buffer.append(ch);
          }
          else
          {
            buffer.append(ch);
          }
        }; break;
      }
      index++;
    }
    if (buffer.length() > 0) vector.addElement(buffer.toString());
    return vector;
  }
  
  public static void main(String args[])
  {
    String line = " input'hol#a'#'hola' hhh";
    LineTokenizer tokenizer = new LineTokenizer(line, "#", true);
    while (tokenizer.hasMoreTokens())
    {
      System.out.println(tokenizer.nextToken());
    }
  }
}