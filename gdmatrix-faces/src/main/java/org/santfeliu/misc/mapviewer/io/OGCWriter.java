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
