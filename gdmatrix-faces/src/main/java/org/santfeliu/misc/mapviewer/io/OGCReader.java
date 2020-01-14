package org.santfeliu.misc.mapviewer.io;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
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
public class OGCReader
{
  private Expression root;
  private final Stack<Expression> stack = new Stack<Expression>();
  
  public Expression fromString(String ogcExpression)
  {
    try
    {
      return read(new StringReader(ogcExpression));
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }
  
  public Expression read(Reader reader) throws Exception
  {
    try
    {
      stack.clear();
      boolean inTag = false;
      int ch = reader.read();
      StringBuilder buffer = new StringBuilder();
      while (ch != -1)
      {
        if (inTag)
        {
          if (ch == '>')
          {
            processTag(buffer.toString());
            buffer.setLength(0);
            inTag = false;
          }
          else
          {
            buffer.append((char)ch);
          }
        }
        else // in text
        {
          if (ch == '<') 
          {
            String text = StringEscapeUtils.unescapeXml(buffer.toString());
            processText(text);
            inTag = true;
            buffer.setLength(0);            
          }
          else
          {
            buffer.append((char)ch);
          }
        }
        ch = reader.read();
      }
      if (!inTag && buffer.length() > 0 && root == null) // remaining text
      {
        String text = StringEscapeUtils.unescapeXml(buffer.toString());
        processText(text);
      }
    }
    finally
    {
      reader.close();
    }
    return root;
  }
  
  private void processTag(String value)
  {
    if (value.startsWith("?")) return;
    boolean end = value.charAt(0) == '/';
    if (end) value = value.substring(1);

    String prefix = null;
    String tag;
    String attrString = null;
    Map<String, String> attributes = null;
    int index = value.indexOf(" ");
    if (index != -1)
    {
      tag = value.substring(0, index);
      attrString = value.substring(index + 1);
    }
    else tag = value;
    
    index = tag.indexOf(":");
    if (index != -1)
    {
      prefix = tag.substring(0, index);
      tag = tag.substring(index + 1);
    }
    
    if (!end && attrString != null)
    {
      attributes = parseAttributes(attrString);
    }
    if (end) processEndTag(prefix, tag);
    else processStartTag(prefix, tag, attributes);
  }
  
  private void processStartTag(String prefix, String tag, 
    Map<String, String> attributes)
  {
    Expression expression;
    if (tag.equalsIgnoreCase("Literal"))
    {
      expression = new Literal();
    }
    else if (tag.equalsIgnoreCase("PropertyName"))
    {
      expression = new Property();
    }
    else if (tag.equalsIgnoreCase("Function"))
    {
      String functionName = attributes.get("name");
      expression = new Function(functionName);        
    }
    else
    {
      String functionName = OGCExpression.getNativeFunction(tag);
      if (functionName == null) functionName = tag;
      expression = new Function(functionName);
    }
    if (stack.isEmpty())
    {
      root = expression;
    }
    else
    {
      Function function = (Function)stack.peek();
      function.getArguments().add(expression);
    }
    stack.push(expression);
  }
  
  private void processEndTag(String prefix, String tag)
  {
    Expression expression = stack.pop();
    // check match expression tag
  }
  
  private void processText(String value)
  {
    if (!stack.isEmpty())
    {
      Expression expression = stack.peek();
      if (expression instanceof Literal)
      {
        Literal literal = (Literal)expression;
        literal.setValue(value);
        literal.detectType();
      }
      else if (expression instanceof Property)
      {
        Property property = (Property)expression;
        property.setName(value);
      }
      // else error
    }
    else // text
    {
      Literal literal = new Literal();
      literal.setValue(value);
      literal.detectType();
      root = literal;
    }
  }

  private Map<String, String> parseAttributes(String attrString)
  {
    HashMap<String, String> attributes = new HashMap<String, String>();
    boolean inName = false;
    boolean inValue = false;
    StringBuilder nameBuffer = new StringBuilder();
    StringBuilder valueBuffer = new StringBuilder();
    for (int i = 0; i < attrString.length(); i++)
    {
      char ch = attrString.charAt(i);
      if (inName)
      {
        if (ch == '=' || isDummy(ch)) 
        {
          inName = false;
        }
        else nameBuffer.append(ch);
      }
      else if (inValue)
      {
        if (ch == '"')
        {
          inValue = false;
          attributes.put(nameBuffer.toString(), valueBuffer.toString());
          nameBuffer.setLength(0);
          valueBuffer.setLength(0);
        }
        else valueBuffer.append(ch);
      }
      else
      {
        if (nameBuffer.length() == 0 && !isDummy(ch))
        {
          inName = true;
          nameBuffer.append(ch);
        }
        else if (valueBuffer.length() == 0 && ch == '"')
        {
          inValue = true;
        }
      }      
    }
    return attributes;
  }

  private boolean isDummy(char ch)
  {
    return ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r';
  }
}
