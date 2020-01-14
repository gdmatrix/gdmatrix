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

import java.io.CharArrayWriter;
import java.io.FileInputStream;
import java.io.PrintWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 *
 * @author unknown
 */
public class XMLPrinter
{
  private static final int INDENT_SIZE = 2;
  private static final int MAX_TEXT_SIZE = 128;
  private static final int MAX_OUTPUT_SIZE = Integer.MAX_VALUE;

  private boolean truncateLongTexts = true;
  private boolean singleLine = false;
  private boolean printComments = false;
  private boolean printAttributes = true;
  private boolean printNamespaces = true;
  private int indentSize = INDENT_SIZE;
  private int maxTextSize = MAX_TEXT_SIZE;
  private int maxOutputSize = MAX_OUTPUT_SIZE;
  
  public XMLPrinter()
  {
  }

  public int getIndentSize()
  {
    return indentSize;
  }

  public void setIndentSize(int indentSize)
  {
    this.indentSize = indentSize;
  }

  public int getMaxTextSize()
  {
    return maxTextSize;
  }

  public void setMaxTextSize(int maxTextSize)
  {
    this.maxTextSize = maxTextSize;
  }

  public int getMaxOutputSize()
  {
    return maxOutputSize;
  }

  public void setMaxOutputSize(int maxOutputSize)
  {
    this.maxOutputSize = maxOutputSize;
  }

  public boolean isPrintComments()
  {
    return printComments;
  }

  public void setPrintComments(boolean printComments)
  {
    this.printComments = printComments;
  }

  public boolean isPrintNamespaces()
  {
    return printNamespaces;
  }

  public void setPrintNamespaces(boolean printNamespaces)
  {
    this.printNamespaces = printNamespaces;
  }

  public boolean isPrintAttributes()
  {
    return printAttributes;
  }

  public void setPrintAttributes(boolean printAttributes)
  {
    this.printAttributes = printAttributes;
  }

  public boolean isTruncateLongTexts()
  {
    return truncateLongTexts;
  }

  public void setTruncateLongTexts(boolean truncateLongTexts)
  {
    this.truncateLongTexts = truncateLongTexts;
  }

  public boolean isSingleLine()
  {
    return singleLine;
  }

  public void setSingleLine(boolean singleLine)
  {
    this.singleLine = singleLine;
  }

  public void print(Node node, PrintWriter writer)
  {
    printNode(node, writer, 0, 0);
  }

  public String format(Node node)
  {
    CharArrayWriter chWriter = new CharArrayWriter();
    PrintWriter writer = new PrintWriter(chWriter);
    printNode(node, writer, 0, 0);
    return new String(chWriter.toCharArray());
  }

  // ----- private methods ------
  
  private int printNode(Node node, PrintWriter writer, int indent, int size)
  {
    if (node instanceof Text)
    {
      Text textNode = (Text)node;
      String s = encodeText(textNode.getData());
      while (s.length() > 0 && size < maxOutputSize)
      {
        size = indent(writer, indent, size);
        if (s.length() > maxTextSize)
        {
          writer.print(s.substring(0, maxTextSize));
          size += maxTextSize;
          if (!singleLine) writer.println();
          s = s.substring(maxTextSize);
        }
        else
        {
          writer.print(s);
          size += s.length();
          if (!singleLine) writer.println();          
          s = "";
        }
      }
    }
    else if (node instanceof Comment)
    {
      if (printComments)
      {
        Comment comment = (Comment)node;
        // TODO: print comments
      }
    }
    else
    {
      String nodeName = getNodeName(node);
      if (node instanceof Element)
      {
        size = indent(writer, indent, size);
        writer.print("<" + nodeName);
        if (printAttributes)
        {
          size = printAttributes(node, writer, size);
        }
        writer.print(">");
        size += nodeName.length() + 2;
      }
      // if element contains only text
      Node child = node.getFirstChild();
      if (child instanceof Text && node.getLastChild() == child && 
        ((Text)child).getData().length() < maxTextSize)
      {
        // inline short text printing
        Text textNode = (Text)child;
        String s = encodeText(textNode.getData());
        writer.write(s);
        size += s.length();
      }
      else if (child instanceof Text && node.getLastChild() == child && 
        truncateLongTexts)
      {
        // inline long text printing. truncated
         Text textNode = (Text)child;
         String s = encodeText(textNode.getData().substring(0, maxTextSize));
         writer.write(s + "...");
         size += s.length() + 3;
      }
      else
      {
        if (!singleLine) writer.println();
        int newIndent = singleLine ? 0 : indent + indentSize;
        while (child != null && size < maxOutputSize)
        {
          size = printNode(child, writer, newIndent, size);
          child = child.getNextSibling();
        }
        if (child != null) writer.print("...");
        size = indent(writer, indent, size);
      }
      if (node instanceof Element)
      {
        writer.print("</" + nodeName + ">");
        size += nodeName.length() + 3;
        if (!singleLine) writer.println();
      }
    }
    return size;
  }

  private int printAttributes(Node node, PrintWriter writer, int size)
  {
    NamedNodeMap map = node.getAttributes();
    if (map != null)
    {
      for (int i = 0; i < map.getLength(); i++)
      {
        Node attribute = map.item(i);
        String name = attribute.getNodeName();
        String value = attribute.getNodeValue();
        String attr = " " + name + "=\"" + value + "\"";
        writer.print(attr);
        size += attr.length();
      }
    }
    return size;
  }
  
  private int indent(PrintWriter writer, int indent, int size)
  {
    for (int i = 0; i < indent; i++) writer.print(" ");
    return size + indent;
  }

  private String getNodeName(Node node)
  {
    String nodeName = node.getNodeName();
    if (!printNamespaces)
    {
      int index = nodeName.indexOf(":");
      if (index != -1)
      {
        nodeName = nodeName.substring(index + 1);
      }
    }
    return nodeName;
  }

  private String encodeText(String text)
  {
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < text.length(); i++)
    {
      char ch = text.charAt(i);
      if (ch == '<') buffer.append("&lt;");
      else if (ch == '>') buffer.append("&gt;");
      else buffer.append(ch);
    }
    return buffer.toString().trim();
  }
  
  public static void main(String args[])
  {
    try
    {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = dbf.newDocumentBuilder();
      Document docDades = docBuilder.parse(
        new FileInputStream("c:/tomcat/conf/web.xml"));
      XMLPrinter xmlPrinter = new XMLPrinter();
//      xmlPrinter.setIndentSize(2);
      xmlPrinter.setSingleLine(true);
      System.out.println(xmlPrinter.format(docDades.getDocumentElement()));
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
