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
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import org.santfeliu.misc.mapviewer.expr.CQLExpression;
import org.santfeliu.misc.mapviewer.expr.CQLExpression.Operator;
import org.santfeliu.misc.mapviewer.expr.Expression;
import org.santfeliu.misc.mapviewer.expr.Function;
import org.santfeliu.misc.mapviewer.expr.Literal;
import org.santfeliu.misc.mapviewer.expr.NativePrinter;
import org.santfeliu.misc.mapviewer.expr.Property;

/**
 *
 * @author realor
 */
public class CQLReader
{
  private static final int CHAR_BUFFER_SIZE = 10;
  private static final int TOKEN_BUFFER_SIZE = 5;
  private Reader reader;
  private final int cbuffer[] = new int[CHAR_BUFFER_SIZE];
  private int cindex = 0;
  private final Token tbuffer[] = new Token[TOKEN_BUFFER_SIZE];
  private int tindex = 0;
  
  public CQLReader()
  {
  }
  
  public Expression fromString(String cql)
  {
    try
    {
      return read(new StringReader(cql));
    }
    catch (IOException ex)
    {    
      throw new RuntimeException(ex);
    }
  }
  
  public Expression read(Reader reader) throws IOException
  {
    cbuffer[0] = -1;
    this.reader = reader;
    return parseExpression(10);
  }
  
  private Expression parseExpression(int precedence) throws IOException
  {
    Token token = readToken();
    if (token.isLiteral()) // Literals
    {
      Expression expr = new Literal(token.type, token.value);      
      return parseRightExpression(expr, precedence);      
    }
    else if (token.isIdentifier()) // Properties or functions
    {
      Token identifier = token;
      
      token = readToken();
      if (token.isOperator())
      {
        unreadToken();
        Expression expr = new Property(identifier.value);
        return parseRightExpression(expr, precedence);
      }
      else if (token.isOpenParenthesis())
      {
        ArrayList<Expression> params = new ArrayList<Expression>();
        token = readToken();
        if (!token.isCloseParenthesis())
        {
          unreadToken();
          params.add(parseExpression(10));       
          
          token = readToken();
          while (token.isComma())
          {
            params.add(parseExpression(10));
            token = readToken();
          }
          if (!token.isCloseParenthesis()) 
            throw new RuntimeException("Unexpected token: " + token.value);
        }
        Expression function = new Function(identifier.value, params);
        return parseRightExpression(function, precedence);
      }
      else
      {
        unreadToken();
        return new Property(identifier.value);
      }
    }
    else if (token.isOperator()) // Unary operators
    {
      String symbol = token.value;
      Operator operator = CQLExpression.getUnaryCQLOperator(symbol);
      if (operator == null)
        throw new RuntimeException("Invalid operator: " + symbol);

      Expression expr = parseExpression(operator.getPrecedence());
      boolean isNegativeNumber = false; // special case
      if (symbol.equals("-") && expr instanceof Literal)
      {
        Literal literal = (Literal)expr;
        if (literal.getType() == Literal.NUMBER)
        {
          literal.setValue("-" + literal.getValue());
          isNegativeNumber = true;
        }
      }
      if (!isNegativeNumber)
      {
        String function = operator.getFunction();
        if (function != null)
        {
          expr = new Function(function, expr);
        }
      }
      return parseRightExpression(expr, precedence);
    }
    else if (token.isOpenParenthesis()) // grouping or lists
    {
      Expression expr = parseExpression(10);      
      token = readToken();
      if (token.isCloseParenthesis())
      {
        return parseRightExpression(expr, precedence);
      }
      else if (token.isComma())
      {
        ArrayList<Expression> list = new ArrayList<Expression>();
        list.add(expr);
        do
        {
          list.add(parseExpression(10));
          token = readToken();
        } while (token.isComma());
        if (!token.isCloseParenthesis()) 
          throw new RuntimeException("Unexpected token: " + token.value);
        return new Function(Function.LIST, list);
      }
      else throw new RuntimeException("Unexpected token: " + token.value);
    }
    return null;
  }
  
  private Expression parseRightExpression(Expression expr, int precedence) 
    throws IOException
  {
    Operator operator = parseOperator();    
    while (operator != null && operator.getPrecedence() < precedence)
    {
      String function = operator.getFunction();
      if (function.equals(Function.BETWEEN) || 
          function.equals(Function.NOT_BETWEEN))
      {
        expr = parseBetween(expr, function, precedence);
      }
      else if (function.equals(Function.IS_NULL) ||
               function.equals(Function.IS_NOT_NULL))
      {
        Token nullToken = readToken();
        if (!nullToken.value.equals("NULL"))
          throw new RuntimeException("Unexpected token: " + nullToken.value);
        expr = new Function(function, expr);
      }
      else // binary operator
      {
        Expression expr2 = parseExpression(operator.getPrecedence());
        expr = new Function(function, expr, expr2);
      }    
      operator = parseOperator();
    }
    if (operator != null)
    {
      unreadToken();
      if (operator.isCompound()) unreadToken();
    }
    return expr;
  }
  
  private Operator parseOperator() throws IOException
  {
    Operator operator = null;
    Token token = readToken();
    if (token.isOperator())
    {
      if (token.value.equals("NOT")) // NOT 
      {
        Token token2 = readToken();
        if (token2.isOperator())
        {
          String symbol = "NOT " + token2.value; // LIKE, ILIKE, BETWEEN, IN
          operator = CQLExpression.getBinaryCQLOperator(symbol);
          if (operator == null)
            throw new RuntimeException("Invalid operator: " + symbol);
        }
        else
        {
          throw new RuntimeException("Invalid token: " + token2.value);
        }
      }
      else if (token.value.equals("IS")) // IS, IS NOT
      {
        Token token2 = readToken();
        if (token2.value.equals("NOT"))
        {
          operator = CQLExpression.getBinaryCQLOperator("IS NOT");
        }
        else 
        {
          operator = CQLExpression.getBinaryCQLOperator("IS");
          unreadToken();
        }
      }
      else // other binary operator
      {
        operator = CQLExpression.getBinaryCQLOperator(token.value);
        if (operator == null) 
          throw new RuntimeException("Invalid operator: " + token.value);
      }
    }
    else // not operator
    {
      unreadToken();
    }
    return operator;
  }
  
  private Expression parseBetween(Expression expr, String op, int precedence)
    throws IOException
  {
    Expression min = parseExpression(4);
    Token token = readToken(); // AND
    if (!token.value.equals("AND"))
      throw new RuntimeException("Unexpected token: " + token.value);
    Expression max = parseExpression(4);
    Expression between = new Function(op, expr, min, max);
    return parseRightExpression(between, precedence);
  }
    
  private void tokenize(String cql)
  {
    try
    {
      cbuffer[0] = -1;
      reader = new StringReader(cql);
      Token token = readToken();
      while (token != null)
      {
        System.out.println(token.value + " [" + token.type + "]");
        token = readToken();
      }
    }
    catch (IOException ex)
    {      
    }
  }
  
  private Token readToken() throws IOException
  {
    Token token = tbuffer[tindex];
    if (token == null)
    {
      token = readNewToken();
      tbuffer[tindex] = token;
      tindex++;
      if (tindex == TOKEN_BUFFER_SIZE) tindex = 0;
      tbuffer[tindex] = null;
    }
    else
    {
      tindex++;
      if (tindex == TOKEN_BUFFER_SIZE) tindex = 0;      
    }
    return token;    
  }
  
  private void unreadToken()
  {
    tindex--;
    if (tindex < 0) tindex = TOKEN_BUFFER_SIZE - 1;    
  }
  
  private Token readNewToken() throws IOException
  {
    StringBuilder builder = new StringBuilder();
    int ch = read();
    if (ch == -1) return new Token(Token.EOF, "<EOF>");
    while (ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t') ch = read();

    if (Character.isDigit(ch))
    {
      builder.append((char)ch);
      ch = read();
      while (Character.isDigit(ch))
      {
        builder.append((char)ch);
        ch = read();
      }
      if (ch == '.')
      {
        builder.append('.');
        ch = read();
        while (Character.isDigit(ch))
        {
          builder.append((char)ch);
          ch = read();
        }
      }
      unread();
      String value = builder.toString();
      return new Token(Token.NUMBER, value);
    }
    else if (Character.isLetter(ch) || ch == '_')
    {
      builder.append((char)ch);
      ch = read();
      while (Character.isLetter(ch) || Character.isDigit(ch) || ch == '_')
      {
        builder.append((char)ch);
        ch = read();
      }
      unread();
      String value = builder.toString();
      if ("NULL".equalsIgnoreCase(value))
        return new Token(Token.NULL, "NULL");
      if ("TRUE".equalsIgnoreCase(value))
        return new Token(Token.BOOLEAN, "TRUE");
      if ("FALSE".equalsIgnoreCase(value))
        return new Token(Token.BOOLEAN, "FALSE");
      if ("IS".equalsIgnoreCase(value))
        return new Token(Token.OPERATOR, "IS");
      if ("NOT".equalsIgnoreCase(value))
        return new Token(Token.OPERATOR, "NOT");
      if ("AND".equalsIgnoreCase(value))
        return new Token(Token.OPERATOR, "AND");
      if ("OR".equalsIgnoreCase(value))
        return new Token(Token.OPERATOR, "OR");
      if ("IN".equalsIgnoreCase(value))
        return new Token(Token.OPERATOR, "IN");
      if ("LIKE".equalsIgnoreCase(value))
        return new Token(Token.OPERATOR, "LIKE");
      if ("ILIKE".equalsIgnoreCase(value))
        return new Token(Token.OPERATOR, "ILIKE");
      if ("BETWEEN".equalsIgnoreCase(value))
        return new Token(Token.OPERATOR, "BETWEEN");
      
      return new Token(Token.IDENTIFIER, value);
    }
    else if (ch == '\'')
    {
      ch = read();
      while (ch != '\'' && ch != -1)
      {
        builder.append((char)ch);
        ch = read();
      }
      String value = builder.toString();
      return new Token(Token.STRING, value);
    }
    else if (ch == '>')
    {
      ch = read();
      if (ch == '=')
      {
        return new Token(Token.OPERATOR, ">=");
      }
      else 
      {
        unread();
        return new Token(Token.OPERATOR, ">");
      }     
    }
    else if (ch == '<')
    {
      ch = read();
      if (ch == '=')
      {
        return new Token(Token.OPERATOR, "<=");
      }
      else if (ch == '>')
      {
        return new Token(Token.OPERATOR, "<>");
      }
      else 
      {
        unread();
        return new Token(Token.OPERATOR, "<");
      }           
    }
    else if (ch == '=')
    {
      return new Token(Token.OPERATOR, "=");
    }
    else if (ch == '+')
    {
      return new Token(Token.OPERATOR, "+");      
    }
    else if (ch == '-')
    {
      return new Token(Token.OPERATOR, "-");      
    }
    else if (ch == '*')
    {
      return new Token(Token.OPERATOR, "*");
    }
    else if (ch == '/')
    {
      return new Token(Token.OPERATOR, "/");      
    }
    else if (ch == '(')
    {
      return new Token(Token.OPEN_PAR, "(");      
    }
    else if (ch == ')')
    {
      return new Token(Token.CLOSE_PAR, ")");     
    }
    else if (ch == ',')
    {
      return new Token(Token.COMMA, ",");     
    }    
    throw new RuntimeException("Unexpected char: '" + (char)ch + "'");
  }
    
  private int read() throws IOException
  {
    int ch = cbuffer[cindex];
    if (ch == -1)
    {
      ch = reader.read();
      cbuffer[cindex] = ch;
      cindex++;
      if (cindex == CHAR_BUFFER_SIZE) cindex = 0;
      cbuffer[cindex] = -1;
    }
    else
    {
      cindex++;
      if (cindex == CHAR_BUFFER_SIZE) cindex = 0;      
    }
    return ch;
  }
  
  private void unread()
  {
    cindex--;
    if (cindex < 0) cindex = CHAR_BUFFER_SIZE - 1;
  }
    
  class Token
  {
    static final int EOF = -1;
    static final int NULL = 0;
    static final int STRING = 1;
    static final int NUMBER = 2;
    static final int BOOLEAN = 3;
    static final int DATE = 4;
    static final int OPERATOR = 5;
    static final int IDENTIFIER = 6;
    static final int OPEN_PAR = 7;
    static final int CLOSE_PAR = 8;
    static final int COMMA = 9;
    
    int type;
    String value;
    
    Token(int type, String value)
    {
      this.type = type;
      this.value = value;
    }
    
    public boolean isOperator()
    {
      return type == OPERATOR;
    }
    
    public boolean isIdentifier()
    {
      return type == IDENTIFIER;
    }
    
    public boolean isLiteral()
    {
      return type == NULL || type == NUMBER || 
             type == STRING || type == BOOLEAN;
    }
    
    public boolean isOpenParenthesis()
    {
      return type == OPEN_PAR;
    }

    public boolean isCloseParenthesis()
    {
      return type == CLOSE_PAR;
    }
    
    public boolean isComma()
    {
      return type == COMMA;
    }    
  }
  
  public static void main(String[] args)
  {
    CQLReader parser = new CQLReader();
    //parser.tokenize("NOM = 45");
    //String cql = "NOT NOT FREE AND NOM NOT BETWEEN 8+2 AND 7*8 OR EDAT NOT IN (4,5) OR TEST IS NULL";
    //String cql = "NOM ILIKE 'Pepe%' OR TRUE";
    //String cql = "EDAT BETWEEN 3 AND 5";
    //String cql = "NOT FREE OR EDAT NOT IN (4,5,6)";
    //String cql = "NOM IS NOT NULL OR a = 7*4+2*strLen(5,8)";
    //String cql = "NOM > 'aaaa";
    String cql = "MAXC >= -2*strLength(NOM) OR NAME NOT BETWEEN 56 AND K+89";
    System.out.println(cql);
    Expression expression = parser.fromString(cql);
    NativePrinter nativePrinter = new NativePrinter();
    System.out.println("NAT: " + nativePrinter.toString(expression));
    CQLWriter cqlPrinter = new CQLWriter();
    System.out.println("CQL: " + cqlPrinter.toString(expression));
    OGCWriter ogcPrinter = new OGCWriter();
    String ogc = ogcPrinter.toString(expression);
    System.out.println("OGC:\n" + ogc);
    OGCReader ogcParser = new OGCReader();
    expression = ogcParser.fromString(ogc);
    System.out.println("NAT: " + nativePrinter.toString(expression));
  }
}
