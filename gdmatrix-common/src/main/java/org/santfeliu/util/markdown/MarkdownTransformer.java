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
package org.santfeliu.util.markdown;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 *
 * @author realor
 */
public class MarkdownTransformer
{
  private static final char EOF = (char)-1;
  private final Stack<Character> stack = new Stack();
  private final List<String> words = new ArrayList<>();
  private final StringBuilder readBuffer = new StringBuilder();
  private final StringBuilder wordBuffer = new StringBuilder();

  private BufferedReader input;
  private PrintWriter output;

  public String transform(String markdown)
  {
    StringReader reader = new StringReader(markdown);
    StringWriter writer = new StringWriter();
    try
    {
      transform(reader, writer);
    }
    catch (Exception ex)
    {
      // ignore
    }
    return writer.toString();
  }

  public void transform(Reader reader, Writer writer) throws IOException
  {
    stack.clear();
    this.input = new BufferedReader(reader);
    this.output = new PrintWriter(writer);

    char ch = read();
    while (ch != EOF)
    {
      if (ch == '\n')
      {
        printBreakLine();
      }
      else if (ch == '!' || ch != '[' || ch != '<' || ch != '`')
      {
        unread(ch);
        if (matches("![?](?)"))
        {
          String alt = words.get(0);
          String src = words.get(1);
          printImage(src, alt);
        }
        else if (matches("[?](?)"))
        {
          String title = words.get(0);
          String url = words.get(1);
          printLink(url, title);
        }
        else if (matches("<?>"))
        {
          String url = words.get(0);
          printLink(url, url);
        }
        else if (matches("```?```"))
        {
          String code = words.get(0);
          printCodeBlock(code);
        }
        else if (matches("`?`"))
        {
          String code = words.get(0);
          printCodeInline(code);
        }
        else
        {
          ch = read();
          printText(ch);
        }
      }
      else
      {
        printText(ch);
      }
      ch = read();
    }
    output.flush();
    words.clear();
  }

  protected void printBreakLine()
  {
    printText("\n");
  }

  protected void printImage(String src, String alt)
  {
    printText("![" + alt + "](" + src + ")");
  }

  protected void printLink(String url, String label)
  {
    printText("[" + label + "](" + url + ")");
  }

  protected void printCodeBlock(String code)
  {
    printText("```" + code + "```");
  }

  protected void printCodeInline(String code)
  {
    printText("`" + code + "`");
  }

  protected void printText(char ch)
  {
    output.print(ch);
  }

  protected void printText(String text)
  {
    output.print(text);
  }

  private boolean matches(String pattern) throws IOException
  {
    words.clear();
    readBuffer.setLength(0);
    wordBuffer.setLength(0);
    int index = 0;
    char ch = read();
    readBuffer.append(ch);

    while (ch != EOF && index < pattern.length())
    {
      char pch = pattern.charAt(index);

      if (pch == '?')
      {
        if (ch == pattern.charAt(index + 1))
        {
          words.add(wordBuffer.toString());
          wordBuffer.setLength(0);
          index += 2;
        }
        else
        {
          wordBuffer.append(ch);
        }
        ch = read();
        readBuffer.append(ch);
      }
      else if (pch == ch)
      {
        ch = read();
        readBuffer.append(ch);
        index++;
      }
      else
      {
        break;
      }
    }
    boolean matches = index == pattern.length();
    if (matches)
    {
      if (ch != EOF)
      {
        unread(ch);
      }
    }
    else
    {
      unread(readBuffer);
    }
    return matches;
  }

  private char read() throws IOException
  {
    if (stack.isEmpty())
    {
      return (char)input.read();
    }
    return stack.pop();
  }

  private void unread(char ch)
  {
    stack.push(ch);
  }

  private void unread(StringBuilder buffer)
  {
    for (int i = buffer.length() - 1; i >= 0; i--)
    {
      stack.push(buffer.charAt(i));
    }
  }
}
