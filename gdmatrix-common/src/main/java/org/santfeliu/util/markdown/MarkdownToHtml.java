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

/**
 *
 * @author realor
 */
public class MarkdownToHtml extends MarkdownTransformer
{
  @Override
  protected void printBreakLine()
  {
    printText("<br>");
  }

  @Override
  protected void printImage(String src, String alt)
  {
    startTag("img");
    setAttribute("src", src);
    setAttribute("alt", alt);
    endAttributes();
    endTag("img");
  }

  @Override
  protected void printLink(String url, String label)
  {
    startTag("a");
    setAttribute("href", url);
    setAttribute("target", "_blank");
    endAttributes();
    printText(label);
    endTag("a");
  }

  @Override
  protected void printCodeBlock(String code)
  {
    String language = null;
    int index = code.indexOf("\n");
    if (index != -1)
    {
      language = code.substring(0, index).trim();
      if (language.length() == 0) language = null;
      else code = code.substring(index + 1);
    }
    startTag("pre");
    endAttributes();
    startTag("code");
    if (language != null)
    {
      setAttribute("class", "language-" + language);
    }
    endAttributes();
    printText(code);
    endTag("code");
    endTag("pre");
  }

  @Override
  protected void printCodeInline(String code)
  {
    startTag("code");
    endAttributes();
    printText(code);
    endTag("code");
  }

  protected void startTag(String tag)
  {
    printText("<" + tag);
  }

  protected void setAttribute(String name, String value)
  {
    printText(" " + name + "=\"" + value + "\"");
  }

  protected void endAttributes()
  {
    printText(">");
  }

  protected void endTag(String tag)
  {
    printText("</" + tag + ">");
  }
}
