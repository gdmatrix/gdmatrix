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
package org.santfeliu.webapp.modules.geo.expr;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

/**
 *
 * @author realor
 */
public class NativePrinter
{
  private PrintWriter printer;

  public String toString(Expression expression)
  {
    try
    {
      StringWriter writer = new StringWriter();
      print(expression, writer);
      return writer.toString();
    }
    catch (IOException ex)
    {
      throw new RuntimeException(ex);
    }
  }

  public void print(Expression expression, Writer writer) throws IOException
  {
    printer = new PrintWriter(writer);
    printExpression(expression);
  }

  private void printExpression(Expression expression)
  {
    if (expression instanceof Literal)
    {
      Literal literal = (Literal)expression;
      if (literal.getType() == Literal.STRING) printer.print("'");
      printer.print(literal.getValue());
      if (literal.getType() == Literal.STRING) printer.print("'");
    }
    else if (expression instanceof Property)
    {
      Property property = (Property)expression;
      printer.print(property.getName());
    }
    else if (expression instanceof Function)
    {
      Function function = (Function)expression;
      String functionName = function.getName();
      List<Expression> arguments = function.getArguments();
      {
        printer.print(functionName);
        printer.print("(");
        for (int i = 0; i < arguments.size(); i++)
        {
          printExpression(arguments.get(i));
          if (i < arguments.size() - 1) printer.print(", ");
        }
        printer.print(")");
      }
    }
  }
}

