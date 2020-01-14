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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Comment;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.tidy.Tidy;

/**
 *
 * @author blanquepa
 */
public class HTMLNormalizer
{
  private static final List<String> HTML_LEGAL_TAGS =
    getHtmlLegalTags();
  private static final List<String> HTML_LEGAL_NON_CLOSED_TAGS =
    getHtmlLegalNonClosedTags();

  public static String normalize(String text)
  {
    if (StringUtils.isBlank(text))
      return null;

    String result = cleanHTML(text, false);
    return replaceSpecialChars(result);
  }

  public static String cleanHTML(String text, boolean fullClean)
  {
    try
    {
      StringBuffer buffer = new StringBuffer();
      if (text != null)
      {
        org.w3c.dom.Document document = parseDocument(text);
        Node bodyNode = document.getElementsByTagName("body").item(0);
        writeNode(bodyNode, buffer, fullClean);
      }
      return buffer.toString();
    }
    catch (Exception ex)
    {

    }
    return "";
  }

  public static String replaceSpecialChars(String text)
  {
    StringBuffer sBuffer = new StringBuffer();
    char ch = 0;
    for (int i = 0; i < text.length(); i++)
    {
      ch = text.charAt(i);
      ch = replaceSpecialChar(ch);
      sBuffer.append(ch);
    }
    return sBuffer.toString();
  }

  private static char replaceSpecialChar(char ch)
  {
    if (ch == '‘') return '\'';
    if (ch == '’') return '\'';
    if (ch == '“') return '"';
    if (ch == '”') return '"';
    if (ch == '´') return '\'';
    if (ch == '`') return '\'';
    if (ch == '•') return '·';

    return ch;
  }

  private static org.w3c.dom.Document parseDocument(String content) throws IOException
  {
    Tidy tidy = new Tidy();
    tidy.setOnlyErrors(true);
    tidy.setShowWarnings(false);
    tidy.setInputEncoding("utf-8");
    ByteArrayInputStream bi =
      new ByteArrayInputStream(content.getBytes("utf-8"));
    org.w3c.dom.Document document = tidy.parseDOM(bi, null);
    return document;
  }

  private static void writeNode(Node node, StringBuffer buffer, boolean fullClean)
    throws IOException
  {
    if (node != null)
    {
      if ((node instanceof Text) || (node instanceof Comment))
      {
        if (fullClean && 
          buffer.length() > 0 && 
          buffer.charAt(buffer.length() - 1) != ' ')
        {
          buffer.append(" ");
        }
        buffer.append(node.getNodeValue());
      }
      else
      {
        String nodeName = node.getNodeName();
        if (nodeName != null)
        {
          String tag = nodeName.toLowerCase();
          if (HTML_LEGAL_TAGS.contains(tag) && !fullClean)
          {
            buffer.append("<").append(tag);
            writeAttributes(node, buffer);
            buffer.append(">");
            writeChildren(node, buffer, fullClean);
            if (!HTML_LEGAL_NON_CLOSED_TAGS.contains(tag))
            {
              buffer.append("</").append(tag).append(">");
            }
          }
          else
          {
            writeChildren(node, buffer, fullClean);
          }
        }
      }
    }
  }

  private static void writeChildren(Node node, StringBuffer buffer, boolean fullClean) throws IOException
  {
    // render children
    Node child = node.getFirstChild();
    while (child != null)
    {
      writeNode(child, buffer, fullClean);
      child = child.getNextSibling();
    }
  }

  private static void writeAttributes(Node node, StringBuffer buffer)
    throws IOException
  {
    NamedNodeMap map = node.getAttributes();
    int count = map.getLength();
    for (int i = 0; i < count; i++)
    {
      Node attribute = map.item(i);
      String name = attribute.getNodeName();
      String value = attribute.getNodeValue();
      buffer.append(" " + name + (value == null ? "" : ("=\"" + value + "\"")));
    }
  }

  private static List<String> getHtmlLegalTags()
  {
    List<String> result = new ArrayList<String>();
    result.add("a");
    result.add("p");
    result.add("strong");
    result.add("em");
    result.add("u");
    result.add("b");
    result.add("i");
    result.add("br");
    result.add("ul");
    result.add("ol");
    result.add("li");
    result.add("h1");
    result.add("h2");
    result.add("h3");
    result.add("h4");
    result.add("h5");
    result.add("h6");
    result.add("strike");
    result.add("blockquote");
    result.add("table");
    result.add("tbody");
    result.add("tr");
    result.add("td");
    result.add("caption");
    result.add("img");
    result.add("hr");
    result.add("iframe");
    result.add("span");
    return result;
  }

  private static List<String> getHtmlLegalNonClosedTags()
  {
    List<String> result = new ArrayList<String>();
    result.add("br");
    result.add("img");
    result.add("hr");
    return result;
  }
}
