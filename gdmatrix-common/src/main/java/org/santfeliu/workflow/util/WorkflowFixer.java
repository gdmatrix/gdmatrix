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
package org.santfeliu.workflow.util;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.santfeliu.workflow.WorkflowNode;
import org.santfeliu.workflow.util.WorkflowFixer.Rule.For;
import org.santfeliu.workflow.util.WorkflowFixer.Rule.Replace;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 *
 * @author realor
 */
public class WorkflowFixer
{
  static final Logger logger = Logger.getLogger("WorkflowFixer");
  static final Map<URL, WorkflowFixer> instances =
    Collections.synchronizedMap(new HashMap<URL, WorkflowFixer>());

  private long lastModified;
  private List<Rule> rules =
    Collections.synchronizedList(new ArrayList<Rule>());


  public static WorkflowFixer getInstance(URL url)
    throws Exception
  {
    URLConnection conn = url.openConnection();
    conn.setUseCaches(false);

    WorkflowFixer fixer = instances.get(url);
    if (fixer == null || fixer.lastModified != conn.getLastModified())
    {
      logger.log(Level.INFO,
        "Parsing workflow rules from {0}", url.toString());
      fixer = new WorkflowFixer();
      fixer.parse(conn.getInputStream());
      fixer.lastModified = conn.getLastModified();
      instances.put(url, fixer);
    }
    return fixer;
  }

  public List<Issue> check(WorkflowNode[] nodes)
  {
    logger.log(Level.FINEST, "Checking workflow nodes");
    List<Issue> issues = new ArrayList();
    for (WorkflowNode node : nodes)
    {
      for (Rule rule : rules)
      {
        rule.check(node, issues);
      }
    }
    return issues;
  }

  public class Issue
  {
    WorkflowNode node;
    String property;
    int occurrences;
    String regexp;
    String with;
    boolean script;
    String reason;
    String level;
    Method getter;
    Method setter;

    public WorkflowNode getNode()
    {
      return node;
    }

    public String getPropery()
    {
      return property;
    }

    public int getOccurrences()
    {
      return occurrences;
    }

    public String getReason()
    {
      return reason;
    }

    public String getRegexp()
    {
      return regexp;
    }

    public String getWith()
    {
      return with;
    }

    public boolean isScript()
    {
      return script;
    }

    public String getLevel()
    {
      return level;
    }

    public void fix()
    {
      try
      {
        if (with != null)
        {
          String oldValue = (String)getter.invoke(node, new Object[0]);
          String newValue;
          Pattern pattern = Pattern.compile(regexp);
          Matcher matcher = pattern.matcher(oldValue);
          if (script)
          {
            StringBuilder builder = new StringBuilder();
            int index = 0;
            while (matcher.find())
            {
              builder.append(oldValue.substring(index, matcher.start()));
              String match = oldValue.substring(matcher.start(), matcher.end());
              builder.append(eval(match));
              index = matcher.end();
            }
            builder.append(oldValue.substring(index));
            newValue = builder.toString();
          }
          else
          {
            // replace all occurrences with newValue
            newValue = matcher.replaceAll(with);
          }
          // replace property value
          setter.invoke(node, newValue);
          logger.log(Level.FINEST, "Fixed node {0}, property \"{1}\": {2}",
            new Object[]{node.getId(), property, newValue});
        }
      }
      catch (Exception ex)
      {
        logger.log(Level.SEVERE, "Fixing node {0}, property \"{1}\": {2}",
          new Object[]{node.getId(), property, ex.toString()});
      }
    }

    @Override
    public String toString()
    {
      StringBuilder builder = new StringBuilder();
      builder.append("Node ").append(node.getId()).
      append(" (").append(node.getType()).append("): property \"").
      append(property).append("\"");
      if (occurrences > 1)
      {
        builder.append(" (");
        builder.append(occurrences);
        builder.append(" occurrences)");
      }
      if (reason != null)
      {
        builder.append(": ").append(reason);
      }
      if (with != null)
      {
        builder.append(". Fix: replace \"").append(regexp).
        append("\" with ");
        if (script) builder.append("script.");
        else builder.append("\"").append(with).append("\".");
      }
      else
      {
        builder.append(". Fix: manual update.");
      }
      return builder.toString();
    }

    private String eval(String match)
    {
      Context cx = ContextFactory.getGlobal().enterContext();
      Object result = null;
      try
      {
        Scriptable scope = cx.initStandardObjects();
        scope.put("text", scope, match);
        result = cx.evaluateString(scope, with, "<code>", 1, null);
        return Context.toString(result);
      }
      finally
      {
        Context.exit();
      }
    }
  }

  /***** private methods and classes *****/

  class Rule
  {
    String name;
    List<For> fors = new ArrayList();
    List<Replace> replaces = new ArrayList();

    class For
    {
      String className;
      String property;
    }

    class Replace
    {
      String regexp;
      String with;
      boolean script;
      String reason;
      String level;
    }

    public void check(WorkflowNode node, List<Issue> issues)
    {
      Class nodeClass = node.getClass();
      String className = node.getClass().getName();

      for (For _for : fors)
      {
        if (_for.className.equals(className))
        {
          String capitalizedProperty = capitalize(_for.property);
          String getterName = "get" + capitalizedProperty;
          String setterName = "set" + capitalizedProperty;
          try
          {
            Method setter = nodeClass.getMethod(setterName, 
              new Class[]{String.class});
            Method getter = nodeClass.getMethod(getterName, new Class[0]);
            String value = (String)getter.invoke(node, new Object[0]);             
            if (value != null)
            {
              for (Replace replace : replaces)
              {
                Pattern pattern = Pattern.compile(replace.regexp);
                Matcher matcher = pattern.matcher(value);
                int occurrences = 0;
                while (matcher.find())
                {
                  occurrences++;
                }
                if (occurrences > 0)
                {
                  Issue issue = new Issue();
                  issue.node = node;
                  issue.property = _for.property;
                  issue.occurrences = occurrences;
                  issue.regexp = replace.regexp;
                  issue.with = replace.with;
                  issue.script = replace.script;
                  issue.reason = replace.reason;
                  issue.level = replace.level;
                  issue.getter = getter;
                  issue.setter = setter;
                  issues.add(issue);
                }
              }
            }
          }
          catch (Exception ex)
          {
          }
        }
      }
    }

    For newFor()
    {
      For _for = new For();
      fors.add(_for);
      return _for;
    }

    Replace newReplace()
    {
      Replace replace = new Replace();
      replaces.add(replace);
      return replace;
    }

    @Override
    public String toString()
    {
      StringBuilder builder = new StringBuilder();
      String n = name == null ? "?" : name;
      builder.append("\nRule ").append(n).append(":\n");
      for (For _for : fors)
      {
        builder.append("[").append(_for.className).append(", ").
          append(_for.property).append("]\n");
      }
      for (Replace replace : replaces)
      {
        builder.append("{[").append(replace.regexp).
          append("], [").append(replace.with).append("], [").
          append(replace.reason).append("]}\n");
      }
      return builder.toString();
    }
  }
  
  private String capitalize(String name)
  {
    return name.toUpperCase().substring(0, 1) + name.substring(1);
  }

  //**** parsing routines ****

  private void parse(InputStream is) throws Exception
  {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.parse(is);

    Node node = document.getFirstChild();
    while (!(node instanceof Element)) node = node.getNextSibling();

    node = node.getFirstChild();
    while (node != null)
    {
      if (node instanceof Element)
      {
        Element element = (Element)node;
        String tag = element.getTagName();
        if ("rule".equals(tag))
        {
          Rule rule = parseRule(element);
          rules.add(rule);
        }
      }
      node = node.getNextSibling();
    }
    is.close();
  }
  
  private Rule parseRule(Element element)
  {
    String name = element.getAttribute("name");
    Rule rule = new Rule();
    rule.name = name;
    Node node = element.getFirstChild();
    while (node != null)
    {
      if (node instanceof Element)
      {
        Element child = (Element)node;
        String tag = child.getTagName();
        if (tag.equals("for"))
        {
          parseFor(rule, child);
        }
        else if (tag.equals("replace"))
        {
          parseReplace(rule, child);
        }
      }
      node = node.getNextSibling();
    }
    return rule;
  }

  private void parseFor(Rule rule, Element element)
  {
    Rule.For _for = rule.newFor();
    _for.className = element.getAttribute("node");
    _for.property = element.getAttribute("property");
  }

  private void parseReplace(Rule rule, Element element)
  {
    Rule.Replace replace = rule.newReplace();
    Node node = element.getFirstChild();
    while (node != null)
    {
      if (node instanceof Element)
      {
        Element child = (Element)node;
        String tag = child.getTagName();
        if (tag.equals("regexp"))
        {
          replace.regexp = child.getTextContent();
        }
        else if (tag.equals("with"))
        {
          replace.with = child.getTextContent();
          String script = child.getAttribute("script");
          replace.script = "true".equals(script);
        }
        else if (tag.equals("reason"))
        {
          replace.reason = child.getTextContent();
        }
        else if (tag.equals("level"))
        {
          replace.level = child.getTextContent();
          if (!"error".equals(replace.level))
            replace.level = "warning";
        }
      }
      node = node.getNextSibling();
    }
  }

  public static void main(String[] args)
  {
    Pattern pattern = Pattern.compile("a[a-z]*c");
    String text = "Esto es abc abbbc pero abc no es dsd abc. Ok";
    Matcher matcher = pattern.matcher(text);
    StringBuilder builder = new StringBuilder();
    int index = 0;
    while (matcher.find())
    {
      builder.append(text.substring(index, matcher.start()));
      String match = text.substring(matcher.start(), matcher.end());
      builder.append("[" + match.toUpperCase() + "]");
      index = matcher.end();
    }
    builder.append(text.substring(index));
    System.out.println(builder);
  }
}
