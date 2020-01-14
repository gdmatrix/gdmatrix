
package org.santfeliu.misc.mapviewer.expr;

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

