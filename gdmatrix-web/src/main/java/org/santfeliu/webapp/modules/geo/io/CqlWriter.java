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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import org.santfeliu.webapp.modules.geo.expr.CqlExpression;
import org.santfeliu.webapp.modules.geo.expr.CqlExpression.Operator;
import org.santfeliu.webapp.modules.geo.expr.Expression;
import org.santfeliu.webapp.modules.geo.expr.Function;
import org.santfeliu.webapp.modules.geo.expr.Literal;
import org.santfeliu.webapp.modules.geo.expr.Property;

/**
 *
 * @author realor
 */
public class CqlWriter
{
  private PrintWriter printer;

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
    printExpression(expression, null);
  }

  private void printExpression(Expression expression,
    Operator parentOperator)
  {
    printExpression(expression, parentOperator, -1);
  }

  private void printExpression(Expression expression,
    Operator parentOperator, int position)
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
      Operator operator = CqlExpression.getBinaryNativeOperator(functionName);
      if (operator != null) // binary operator
      {
        boolean parenthesis =
          needParenthesis(operator, parentOperator, position);

        if (parenthesis) printer.print("(");
        String symbol = operator.getSymbol();
        if (symbol.equals("IS") || symbol.equals("IS NOT"))
        {
          printExpression(arguments.get(0), operator);
          printer.print(" " + symbol + " NULL");
        }
        else if (symbol.equals("BETWEEN") || symbol.equals("NOT BETWEEN"))
        {
          printExpression(arguments.get(0), operator);
          printer.print(" " + symbol + " ");
          printExpression(arguments.get(1), operator);
          printer.print(" AND ");
          printExpression(arguments.get(2), operator);
        }
        else if (arguments.size() == 2)
        {
          printExpression(arguments.get(0), operator, 0);
          printer.print(" " + symbol + " ");
          printExpression(arguments.get(1), operator, 1);
        }
        else throw new RuntimeException("Invalid number of arguments");
        if (parenthesis) printer.print(")");
      }
      else
      {
        operator = CqlExpression.getUnaryNativeOperator(functionName);
        if (operator != null && !arguments.isEmpty())
        {
          String symbol = operator.getSymbol();
          printer.print(symbol);
          if (!symbol.equals("-")) printer.print(" ");
          printExpression(arguments.get(0), operator);
        }
        else // function
        {
          if (!functionName.equals(Function.LIST))
          {
            printer.print(functionName);
          }
          printer.print("(");
          for (int i = 0; i < arguments.size(); i++)
          {
            printExpression(arguments.get(i), null);
            if (i < arguments.size() - 1) printer.print(", ");
          }
          printer.print(")");
        }
      }
    }
  }

  private boolean needParenthesis(Operator operator,
    Operator parentOperator, int position)
  {
    if (parentOperator == null) return false;
    if (operator.getPrecedence() > parentOperator.getPrecedence()) return true;
    // same preference

    String symbol = parentOperator.getSymbol();
    if (symbol.equals("-") || symbol.equals("/"))
    {
      // not associative operators
      if (position > 0) return true; // not first argument
    }
    return false;
  }
}
