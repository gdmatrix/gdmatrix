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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.santfeliu.util.script.ScriptableBase;

/**
 *
 * @author realor
 */
public class JSTemplate
{
  private String source;
  private String script;
  private Set<String> variables;
  private Class scriptableBaseClass;

  public JSTemplate(Reader reader) throws IOException
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

  public JSTemplate(String s)
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

  public JSTemplate(Reader reader, Class scriptableBaseClass) throws IOException
  {
    this(reader);
    this.scriptableBaseClass = scriptableBaseClass;
  }

  public JSTemplate(String s, Class scriptableBaseClass)
  {
    this(s);
    this.scriptableBaseClass = scriptableBaseClass;
  }

  public static JSTemplate create(String text)
  {
    return new JSTemplate(text);
  }

  public static JSTemplate create(String text, Class scriptableBaseClass)
  {
    return new JSTemplate(text, scriptableBaseClass);
  }

  public static JSTemplate create(Reader reader) throws IOException
  {
    return new JSTemplate(reader);
  }

  public static JSTemplate create(Reader reader, Class scriptableBaseClass) throws IOException
  {
    return new JSTemplate(reader, scriptableBaseClass);
  }

  public static JSTemplate create(File file) throws IOException
  {
    return new JSTemplate(new InputStreamReader(new FileInputStream(file)));
  }

  public static JSTemplate create(File file, Class scriptableBaseClass) throws IOException
  {
    return new JSTemplate(new InputStreamReader(new FileInputStream(file)), scriptableBaseClass);
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
        JSTemplate template = JSTemplate.create((String)value);
        value = template.merge(variables);
      }
      dest.put(key, value);
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
    Context context = ContextFactory.getGlobal().enterContext();
    try
    {
      Scriptable scope = createScriptable(context, variables);
      scope.put("_out_", scope, writer);
      context.evaluateString(scope, script, "code", 0, null);
    }
    finally
    {
      Context.exit();
    }
  }

  public String getSource()
  {
    return source;
  }

  public String getScript()
  {
    return script;
  }

  public Set<String> getReferencedVariables()
  {
    if (variables == null)
    {
      org.mozilla.javascript.Parser parser =
        new org.mozilla.javascript.Parser(new CompilerEnvirons(), null);
      variables = new HashSet<>();
      AstRoot node = parser.parse(script, "", 1);
      exploreVariables(node, variables);
      variables.remove("_out_");
      variables.remove("String");
    }
    return variables;
  }

  public void write(Writer writer) throws IOException
  {
    writer.write(source);
  }

  protected Scriptable createScriptable(Context cx, Map variables)
  {
    Scriptable scriptable;
    try
    {
      if (scriptableBaseClass == null)
        scriptableBaseClass = ScriptableBase.class;
      scriptable = (Scriptable) scriptableBaseClass.
        getConstructor(Context.class, Map.class).newInstance(cx, variables);
    }
    catch (Exception ex)
    {
      scriptable = new ScriptableBase(cx, variables);
    }
    return scriptable;
  }

  private void exploreVariables(AstRoot node, final Collection variables)
  {
    node.visit((AstNode n) ->
    {
      if (n.getType() == Token.NAME)
      {
        String variable = n.getString();
        variables.add(variable);
      }
      return true;
    });
  }

  private void parse(Reader reader) throws IOException
  {
    StringBuilder sourceBuffer = new StringBuilder();
    StringBuilder scriptBuffer = new StringBuilder();
    StringBuilder buffer = new StringBuilder();

    int state = 0;
    int ich = reader.read();
    while (ich != -1)
    {
      char ch = (char)ich;
      sourceBuffer.append(ch);
      switch (state)
      {
        case 0: // text
          if (ch == '<') state = 1;
          else if (ch == '$') state = 4;
          else buffer.append(ch);
          break;
        case 1: // <
          if (ch == '%')
          {
            appendText(buffer.toString(), scriptBuffer);
            buffer.setLength(0);
            state = 2;
          }
          else
          {
            buffer.append('<');
            buffer.append(ch);
            state = 0;
          }
          break;
        case 2: // <%
          if (ch == '%')
          {
            state = 3;
          }
          else
          {
            buffer.append(ch);
          }
          break;
        case 3: // <% ... %
          if (ch == '>')
          {
            if (buffer.charAt(0) == '=') // <%= expr %>
            {
              appendExpr(buffer.toString().substring(0, 1), scriptBuffer);
            }
            else
            {
              String stmt = buffer.toString();
              if (stmt.equals("#o")) // escape <%#o%> = <%
              {
                appendText("<%", scriptBuffer);
              }
              else if (stmt.equals("#c")) // escape <%#c%> = %>
              {
                appendText("%>", scriptBuffer);
              }
              else // <% statement %>
              {
                appendStmt(stmt, scriptBuffer);
              }
            }
            buffer.setLength(0);
            state = 0;
          }
          else if (ch == '%')
          {
            buffer.append('%');
          }
          else
          {
            buffer.append('%');
            buffer.append(ch);
            state = 2;
          }
          break;
        case 4: // $
          if (ch == '{')
          {
            appendText(buffer.toString(), scriptBuffer);
            buffer.setLength(0);
            state = 5;
          }
          else if (ch == '$')
          {
            buffer.append('$');
          }
          else
          {
            buffer.append('$');
            buffer.append(ch);
            state = 0;
          }
          break;
        case 5: // ${
          if (ch == '}')
          {
            appendExpr(buffer.toString(), scriptBuffer);
            buffer.setLength(0);
            state = 0;
          }
          else
          {
            buffer.append(ch);
          };
          break;
      }
      ich = reader.read();
    }
    if (buffer.length() > 0)
    {
      appendText(buffer.toString(), scriptBuffer);
    }

    source = sourceBuffer.toString();
    script = scriptBuffer.toString();
  }

  protected void appendText(String text, StringBuilder scriptBuffer)
  {
    scriptBuffer.append("_out_.write(\"");
    text = text.replace("\\", "\\\\");
    text = text.replace("\n", "\\n");
    text = text.replace("\r", "\\r");
    text = text.replace("\t", "\\t");
    text = text.replace("\"", "\\\"");
    scriptBuffer.append(text);
    scriptBuffer.append("\");\n");
  }

  protected void appendExpr(String expr, StringBuilder scriptBuffer)
  {
    scriptBuffer.append("_out_.write(String(");
    scriptBuffer.append(expr);
    scriptBuffer.append("));\n");
  }

  protected void appendStmt(String stmt, StringBuilder scriptBuffer)
  {
    scriptBuffer.append(stmt);
    scriptBuffer.append('\n');
  }

  public static void main(String[] args)
  {
    try
    {
      File file = new File("/test.jsp");
      JSTemplate t = JSTemplate.create(file);
      //System.out.println("\n" + t.getSource());
      System.out.println("\n" + t.getScript());
      System.out.println(t.getReferencedVariables());

      Writer w = new PrintWriter(System.out);
      w.write("\n---------------\n");
      //t.write(w);
      w.write("\n---------------\n");
      //t.merge(new HashMap(), w);
      w.flush();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
