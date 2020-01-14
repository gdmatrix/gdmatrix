package org.santfeliu.misc.mapviewer.io;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import org.santfeliu.misc.mapviewer.expr.CQLExpression;
import org.santfeliu.misc.mapviewer.expr.CQLExpression.Operator;
import org.santfeliu.misc.mapviewer.expr.Expression;
import org.santfeliu.misc.mapviewer.expr.Function;
import org.santfeliu.misc.mapviewer.expr.Literal;
import org.santfeliu.misc.mapviewer.expr.Property;

/**
 *
 * @author realor
 */
public class CQLWriter
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
      Operator operator = CQLExpression.getBinaryNativeOperator(functionName);
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
        operator = CQLExpression.getUnaryNativeOperator(functionName);
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
