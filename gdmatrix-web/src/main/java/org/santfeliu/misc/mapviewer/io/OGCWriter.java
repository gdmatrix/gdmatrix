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
package org.santfeliu.misc.mapviewer.io;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import org.apache.commons.lang.StringEscapeUtils;
import org.santfeliu.misc.mapviewer.expr.Expression;
import org.santfeliu.misc.mapviewer.expr.Function;
import org.santfeliu.misc.mapviewer.expr.Literal;
import org.santfeliu.misc.mapviewer.expr.OGCExpression;
import org.santfeliu.misc.mapviewer.expr.Property;

/**
 *
 * @author realor
 */
public class OGCWriter
{
  private PrintWriter printer;
  private String prefix = "ogc";

  public String getPrefix()
  {
    return prefix;
  }

  public void setPrefix(String prefix)
  {
    this.prefix = prefix;
  }
  
  public String toString(Expression expression)
  {
    try
    {
      StringWriter writer = new StringWriter();
      write(expression, writer);
      return writer.toString();
    }
    catch (IOException ex)
    {
      throw new RuntimeException(ex);
    }
  }
  
  public void write(Expression expression, Writer writer) throws IOException
  {
    printer = new PrintWriter(writer);
    printExpression(expression, 0);
  }
  
  private void printExpression(Expression expression, int indent)
  {
    if (expression instanceof Literal)
    {
      Literal literal = (Literal)expression;
      indent(indent);
      openTag("Literal");
      printer.print(StringEscapeUtils.escapeXml(literal.getValue()));
      closeTag("Literal");
      printer.println();
    }
    else if (expression instanceof Property)
    {
      Property property = (Property)expression;
      indent(indent);
      openTag("PropertyName");
      printer.print(property.getName());
      closeTag("PropertyName");
      printer.println();
    }
    else if (expression instanceof Function)
    {
      Function function = (Function)expression;
      function = Function.expandFunction(function);
      String ogcFunction = OGCExpression.getOgcFunction(function.getName());
      
      indent(indent);
      if (ogcFunction == null)
        openTag("Function", "name=\"" + function.getName() + "\"");
      else
      {
        if (ogcFunction.equals("PropertyIsLike"))
        {
          openTag(ogcFunction, 
            "wildCard=\"%\" singleChar=\".\" escapeChar=\"!\"");          
        }
        else
        {
          openTag(ogcFunction);
        }
      }
      printer.println();

      List<Expression> arguments = function.getArguments();
      for (int i = 0; i < arguments.size(); i++)
      {
        printExpression(arguments.get(i), indent + 2);
      }
      
      indent(indent);
      if (ogcFunction == null)
        closeTag("Function");
      else 
        closeTag(ogcFunction);
      printer.println();
    }
  }
  
  private void openTag(String name)
  {
    openTag(name, null);
  }
  
  private void openTag(String name, String attributes)
  {
    printer.print("<");
    if (prefix != null) printer.print(prefix + ":");
    printer.print(name);
    if (attributes != null)
    {
      printer.print(" ");
      printer.print(attributes);
    }
    printer.print(">");
  }
  
  private void closeTag(String name)
  {
    printer.print("</");
    if (prefix != null) printer.print(prefix + ":");
    printer.print(name);
    printer.print(">");    
  }
  
  private void indent(int indent)
  {
    for (int i = 0; i < indent; i++)
    {
      printer.print(" ");
    }
  }
}
