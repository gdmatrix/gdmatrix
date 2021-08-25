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
package org.santfeliu.form.type.html;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import org.santfeliu.form.View;
import org.santfeliu.util.enc.HtmlEncoder;

/**
 *
 * @author realor
 */
public class HtmlPrinter
{
  private HtmlForm form;
  private int indentSize = 2;

  public HtmlPrinter(HtmlForm form)
  {
    this.form = form;
  }

  public int getIndentSize()
  {
    return indentSize;
  }

  public void setIndentSize(int indentSize)
  {
    this.indentSize = indentSize;
  }

  public void print(Writer writer)
  {
    print(new PrintWriter(writer));
  }
  
  public void print(PrintWriter writer)
  {
    try
    {
      HtmlView rootView = form.rootView;
      if (rootView != null)
      {
        printDocType(writer);
        writer.println("<html>");
        printHeadSection(writer);
        printView(rootView, writer, indentSize);
        writer.println("</html>");
      }
    }
    finally
    {
      writer.close();
    }
  }

  private void printView(HtmlView view, PrintWriter writer, int indent)
  {
    if (View.TEXT.equals(view.getViewType()))
    {
      String text = (String)view.getProperty("text");
      if (text != null) writer.println(HtmlEncoder.encode(text));
    }
    else if (view.getChildren().size() == 1 &&
      View.TEXT.equals(view.getChildren().get(0).getViewType()))
    {
      View label = view.getChildren().get(0);
      printIndent(writer, indent);
      writer.print("<" + view.getNativeViewType());
      printAttributes(view, writer);
      writer.print(">");
      String text = (String)label.getProperty("text");
      if (text != null) writer.print(HtmlEncoder.encode(text));
      writer.println("</" + view.getNativeViewType() + ">");
    }
    else if (view.getChildren().isEmpty())
    {
      if ("textarea".equalsIgnoreCase(view.getNativeViewType()))
      {
        // special case
        printIndent(writer, indent);
        writer.print("<textarea");
        printAttributes(view, writer);
        writer.println("></textarea>");
      }
      else
      {
        printIndent(writer, indent);
        writer.print("<" + view.getNativeViewType());
        printAttributes(view, writer);
        writer.println("/>");
      }
    }
    else
    {
      printIndent(writer, indent);
      writer.print("<" + view.getNativeViewType());
      printAttributes(view, writer);
      writer.println(">");
      for (View child : view.getChildren())
      {
        printView((HtmlView)child, writer, indent + indentSize);
      }
      printIndent(writer, indent);
      writer.println("</" + view.getNativeViewType() + ">");
    }
  }

  private void printIndent(PrintWriter writer, int indent)
  {
    for (int i = 0; i < indent; i++)
    {
      writer.print(" ");
    }
  }

  private void printAttributes(HtmlView view, PrintWriter writer)
  {
    for (String property : view.getPropertyNames())
    {
      writer.print(" " + property);
      writer.print("=\"");
      writer.print(view.getProperty(property));
      writer.print("\"");
    }
  }

  private void printDocType(PrintWriter writer)
  {
    writer.println("<!DOCTYPE HTML PUBLIC " +
      "\"-//W3C//DTD HTML 4.01 Transitional//EN\" " +
      "\"http://www.w3.org/TR/html4/loose.dtd\">");
  }

  private void printHeadSection(PrintWriter writer)
  {
    writer.println("  <head>");
    writer.println("    <meta http-equiv=\"Content-Type\" " +
      "content=\"text/html; charset=" + form.getEncoding() + "\"/>");
    if (form.getTitle() != null)
    {
      writer.println("    <title>" + form.getTitle() + "</title>");
    }
    writer.println("  </head>"); 
  }

  public static void main(String args[])
  {
    try
    {
      HtmlForm form = new HtmlForm();
      form.read(new FileInputStream("c:/sample1.html"));
      form.write(new FileOutputStream("c:/out.html"), null);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
