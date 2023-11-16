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
package org.santfeliu.webapp.modules.geo.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.webapp.modules.geo.sld.SldNode;

/**
 *
 * @author realor
 */
public class SldWriter
{
  public static final String SLD_CHARSET = "ISO-8859-1";
  private int indentSize = 2;
  private boolean pretty = true;

  public int getIndentSize()
  {
    return indentSize;
  }

  public void setIndentSize(int indentSize)
  {
    this.indentSize = indentSize;
  }

  public boolean isPretty()
  {
    return pretty;
  }

  public void setPretty(boolean pretty)
  {
    this.pretty = pretty;
  }

  public void write(SldNode node, OutputStream os)
    throws IOException
  {
    cleanNode(node);

    BufferedWriter writer =
      new BufferedWriter(new OutputStreamWriter(os, SLD_CHARSET));
    try
    {
      writer.write("<?xml version=\"1.0\" encoding=\"" + SLD_CHARSET + "\"?>");
      if (pretty) writer.write('\n');
      writeNode(node, writer, 0);
    }
    finally
    {
      writer.close();
    }
  }

  public void writeNode(SldNode node, Writer writer, int indent)
    throws IOException
  {
    if (node.getName() == null)
    {
      writer.write(StringEscapeUtils.escapeXml(node.getTextValue()));
    }
    else
    {
      if (pretty) indent(indent, writer);
      writer.write("<");
      if (node.getPrefix() != null)
      {
        writer.write(node.getPrefix() + ":");
      }
      writer.write(node.getName());
      writeAttributes(node, writer);
      writer.write(">");

      boolean inline = isTextChildren(node);
      if (!inline && pretty) writer.write('\n');
      String textValue = node.getTextValue();
      if (textValue != null)
        writer.write(StringEscapeUtils.escapeXml(textValue));

      writeChildren(node, writer, indent);

      if (!inline && pretty) indent(indent, writer);
      writer.write("</");
      if (node.getPrefix() != null)
      {
        writer.write(node.getPrefix() + ":");
      }
      writer.write(node.getName());
      writer.write(">");
      if (pretty) writer.write('\n');
    }
  }

  public void writeChildren(SldNode node, Writer writer, int indent)
    throws IOException
  {
    for (int i = 0; i < node.getChildCount(); i++)
    {
      SldNode child = node.getChild(i);
      writeNode(child, writer, indent + indentSize);
    }
  }

  private boolean isTextChildren(SldNode node)
  {
    boolean isText = true;
    int i = 0;
    while (isText && i < node.getChildCount())
    {
      SldNode child = node.getChild(i);
      isText = (child.getName() == null);
      i++;
    }
    return isText;
  }

  private void writeAttributes(SldNode node, Writer writer)
    throws IOException
  {
    Map<String, String> attributes = node.getAttributes();
    Set<String> attributeNames = attributes.keySet();
    for (String attributeName : attributeNames)
    {
      String value = attributes.get(attributeName);
      writer.write(" ");
      writer.write(attributeName);
      writer.write("=\"");
      writer.write(value);
      writer.write("\"");
    }
  }

  private void indent(int indent, Writer writer) throws IOException
  {
    for (int i = 0; i < indent; i++)
    {
      writer.write(" ");
    }
  }

  private void cleanNode(SldNode node)
  {
    int i = 0;
    while (i < node.getChildCount())
    {
      SldNode child = node.getChild(i);
      cleanNode(child);
      if (StringUtils.isBlank(child.getTextValue()))
      {
        if (child.getAttributes().isEmpty())
        {
          if (child.getChildCount() == 0)
          {
            node.removeChild(i);
          }
          else i++;
        }
        else i++;
      }
      else i++;
    }
  }
}
