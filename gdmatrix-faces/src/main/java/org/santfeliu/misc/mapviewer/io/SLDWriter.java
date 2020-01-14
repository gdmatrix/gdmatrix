package org.santfeliu.misc.mapviewer.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.misc.mapviewer.sld.SLDNode;

/**
 *
 * @author realor
 */
public class SLDWriter
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

  public void write(SLDNode node, OutputStream os)
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

  public void writeNode(SLDNode node, Writer writer, int indent)
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

  public void writeChildren(SLDNode node, Writer writer, int indent)
    throws IOException
  {
    for (int i = 0; i < node.getChildCount(); i++)
    {
      SLDNode child = node.getChild(i);
      writeNode(child, writer, indent + indentSize);
    }
  }

  private boolean isTextChildren(SLDNode node)
  {
    boolean isText = true;
    int i = 0;
    while (isText && i < node.getChildCount())
    {
      SLDNode child = node.getChild(i);
      isText = (child.getName() == null);
      i++;
    }
    return isText;
  }

  private void writeAttributes(SLDNode node, Writer writer)
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

  private void cleanNode(SLDNode node)
  {
    int i = 0;
    while (i < node.getChildCount())
    {
      SLDNode child = node.getChild(i);
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
