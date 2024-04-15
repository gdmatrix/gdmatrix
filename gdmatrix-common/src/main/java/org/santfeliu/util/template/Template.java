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
package org.santfeliu.util.template;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;

import java.io.StringWriter;
import java.io.Writer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import java.util.Set;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ScriptOrFnNode;
import org.mozilla.javascript.Scriptable;

import org.mozilla.javascript.Token;
import org.santfeliu.util.script.ScriptableBase;

/**
 *
 * @author realor
 */
public class Template
{
  public static final char EXPR_CHAR = '$';
  public static final char OPEN_EXPR_CHAR = '{';
  public static final char CLOSE_EXPR_CHAR = '}';
  private static final int TEXT_FRAGMENT = 0;
  private static final int EXPRESSION_FRAGMENT = 1;
  private ArrayList<Fragment> fragments = new ArrayList<>();
  private int expressionFragmentCount = 0;

  public Template(Reader reader) throws IOException
  {
    try
    {
      parse(reader);
    }
    finally
    {
      reader.close();
    }
  }

  public Template(String s)
  {
    if (s != null)
    {
      try
      {
        parse(new StringReader(s));
      }
      catch (IOException ex)
      {
      }
    }
  }

  public String merge(Map variables)
  {
    try
    {
      StringWriter writer = new StringWriter();
      merge(variables, writer);
      return writer.toString();
    }
    catch (IOException ex)
    {
      throw new RuntimeException(ex);
    }
  }

  public void merge(Map variables, Writer writer) throws IOException
  {
    ContextFactory cxfactory = ContextFactory.getGlobal();
    Context cx = cxfactory.enterContext();
    try
    {
      Scriptable scope = createScriptable(cx, variables);
      for (Fragment fragment : fragments)
      {
        if (fragment.type == TEXT_FRAGMENT) // fragment.type == TEXT_FRAGMENT
        {
          writer.write(fragment.text);
        }
        else // fragment.type == EXPRESSION_FRAGMENT
        {
          Object result =
            cx.evaluateString(scope, fragment.text, "<expr>", 1, null);
          writer.write(Context.toString(result));
        }
      }
    }
    finally
    {
      Context.exit();
    }
  }

  public void write(Writer writer) throws IOException
  {
    for (Fragment fragment : fragments)
    {
      if (fragment.type == TEXT_FRAGMENT) // fragment.type == TEXT_FRAGMENT
      {
        writer.write(fragment.text);
      }
      else // fragment.type == EXPRESSION_FRAGMENT
      {
        writer.write("${" + fragment.text + "}");
      }
    }
  }

  @Override
  public String toString()
  {
    StringWriter sw = new StringWriter();
    try
    {
      write(sw);
    }
    catch (IOException ex)
    {
    }
    return sw.toString();
  }

  public int getFragmentCount()
  {
    return fragments.size();
  }

  public int getExpressionFragmentCount()
  {
    return expressionFragmentCount;
  }

  public int getTextFragmentCount()
  {
    return fragments.size() - expressionFragmentCount;
  }

  public Set getReferencedVariables()
  {
    HashSet variables = new HashSet();
    loadReferencedVariables(variables);
    return variables;
  }

  public void loadReferencedVariables(Collection variables)
  {
    Parser parser = new Parser(new CompilerEnvirons(), null);
    for (Fragment fragment : fragments)
    {
      if (fragment.type == EXPRESSION_FRAGMENT)
      {
        String expression = fragment.text;
        ScriptOrFnNode node = parser.parse(expression, "", 1);
        exploreVariables(node, variables);
      }
    }
  }


  // static methods

  public static Template create(String s)
  {
    return new Template(s);
  }

  public static Template create(Reader reader) throws IOException
  {
    return new Template(reader);
  }

  public static Template create(File file) throws IOException
  {
    return new Template(new InputStreamReader(new FileInputStream(file)));
  }

  public static void merge(Map source, Map dest, Map variables)
  {
    Iterator iter = source.entrySet().iterator();
    while (iter.hasNext())
    {
      Map.Entry entry = (Map.Entry)iter.next();
      Object key = entry.getKey();
      Object value = entry.getValue();
      if (value instanceof String)
      {
        Template template = new Template((String)value);
        value = template.merge(variables);
      }
      dest.put(key, value);
    }
  }

  // protected methods

  protected Scriptable createScriptable(Context cx, Map variables)
  {
    Scriptable scriptable = new ScriptableBase(cx, variables);
    return scriptable;
  }

  // private methods

  class Fragment
  {
    int type;
    String text;

    public Fragment(int type, String text)
    {
      this.type = type;
      this.text = text;
    }
  }

  private void printFragments()
  {
    for (Fragment fragment : fragments)
    {
      System.out.println("[" + fragment.type + "," + fragment.text + "]");
    }
  }

  private void parse(Reader reader) throws IOException
  {
    fragments.clear();
    expressionFragmentCount = 0;
    StringBuilder buffer = new StringBuilder();
    int state = 0;
    int level = 0;
    int ich = reader.read();
    while (ich != -1)
    {
      char ch = (char)ich;
      switch (state)
      {
        case 0: // look for EXPR_CHAR
          if (ch == EXPR_CHAR)
          {
            state = 1;
          }
          else
          {
            buffer.append(ch);
          }
          break;

        case 1: // look for OPEN_EXPR_CHAR
          if (ch == OPEN_EXPR_CHAR)
          {
            state = 2;
            level = 1;
            if (buffer.length() > 0)
            {
              fragments.add(new Fragment(TEXT_FRAGMENT, buffer.toString()));
            }
            buffer.setLength(0);
          }
          else if (ch == EXPR_CHAR)
          {
            buffer.append(EXPR_CHAR);
          }
          else
          {
            state = 0;
            buffer.append(EXPR_CHAR);
            buffer.append(ch);
          }
          break;

        case 2: // look for CLOSE_EXPR_CHAR
          if (ch == OPEN_EXPR_CHAR)
          {
            level++;
            buffer.append(ch);
          }
          else if (ch == CLOSE_EXPR_CHAR)
          {
            level--;
            if (level == 0)
            {
              String expr = buffer.toString();
              expr = expr.trim();
              if (expr.length() == 0) // escape
              {
                fragments.add(
                  new Fragment(TEXT_FRAGMENT, "" + EXPR_CHAR + OPEN_EXPR_CHAR));
              }
              else
              {
                fragments.add(
                  new Fragment(EXPRESSION_FRAGMENT, buffer.toString()));
                expressionFragmentCount++;
              }
              buffer.setLength(0);
              state = 0;
            }
            else
            {
              buffer.append(ch);
            }
          }
          else
          {
            if (ch == '\'')
            {
              state = 3;
            }
            else if (ch == '"')
            {
              state = 4;
            }
            buffer.append(ch);
          }
          break;

        case 3:
          if (ch == '\'')
          {
            state = 2;
          }
          buffer.append(ch);
          break;

        case 4:
          if (ch == '\"')
          {
            state = 2;
          }
          buffer.append(ch);
          break;
      }
      ich = reader.read();
    }
    if (buffer.length() > 0)
    {
      fragments.add(new Fragment(TEXT_FRAGMENT, buffer.toString()));
    }
  }

  private void exploreVariables(Node node, Collection variables)
  {
    Node n = node.getFirstChild();
    while (n != null)
    {
      if (n.getType() == Token.NAME)
      {
        String variable = n.getString();
        variables.add(variable);
      }
      exploreVariables(n, variables);
      n = n.getNext();
    }
  }

  public static void main(String[] args)
  {
    try
    {
      //String s = "aaa$${}${decimalFormat(alfa, '#0.00')}bbb${alfa + 1}";
      //String s = "${(alfa > 9) ? 'aa' : 'bb'; Math.sin(alfa) + (b + 4) + blankNull() + b + a}";
      String s = "esto es cuest ${if (alfa > 0) {alfa = '{2'} else {alfa = 0}} fff";
      Template template = new Template(s);
      template.printFragments();
      HashMap vars = new HashMap();
      vars.put("alfa", 8.7854);
      System.out.println(template.merge(vars));
      System.out.println(template.getReferencedVariables());

      HashMap vars2 = new HashMap();
      HashMap vars3 = new HashMap();
      vars2.put("beta", "aaa${2 + 2}");
      vars2.put("gamma", "aaa${alfa + 2}");
      Template.merge(vars2, vars3, vars);
      System.out.println(vars);
      System.out.println(vars2);
      System.out.println(vars3);
      System.out.println("[" + Template.create("").merge(vars) + "]");

      vars.clear();
      vars.put("precio", 56.6);
      Template t = Template.create("esto cuesta ${precio} euros ${precio}");
      System.out.println(t.merge(vars));
      System.out.println(t.getExpressionFragmentCount());
      System.out.println(t.getFragmentCount());
      System.out.println(t.getReferencedVariables());

      PrintWriter w = new PrintWriter(System.out);
      t.write(w);
      w.flush();
      w.close();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
