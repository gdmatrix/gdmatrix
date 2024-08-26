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
package org.santfeliu.misc.query.io;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.misc.query.Query;
import org.santfeliu.misc.query.QueryInstance;
import org.santfeliu.misc.query.QueryInstance.Expression;
import org.santfeliu.misc.query.QueryInstance.Operator;
import org.santfeliu.misc.query.QueryInstance.Predicate;

/**
 *
 * @author realor
 */
public class QueryWriter
{
  private QueryFinder queryFinder;
  private PrintWriter writer;
  private int indent;

  public QueryWriter()
  {
  }

  public QueryFinder getQueryFinder()
  {
    return queryFinder;
  }

  public void setQueryFinder(QueryFinder queryFinder)
  {
    this.queryFinder = queryFinder;
  }

  public void writeQuery(Query query, OutputStream os) throws Exception
  {
    Query baseQuery = null;
    if (!StringUtils.isBlank(query.getBase()))
    {
      QueryReader reader = new QueryReader();
      reader.setQueryFinder(queryFinder);
      reader.setReadInstances(false);
      baseQuery =
        reader.readQuery(queryFinder.getQueryStream(query.getBase()));
    }

    indent = 0;
    writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
    try
    {
      writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

      startTag("query", query.getName(), true);
      writeTagValue("base", query.getBase(), true);

      if (baseQuery == null ||
        !StringUtils.equals(query.getTitle(), baseQuery.getTitle()))
      {
        writeTagValue("title", query.getTitle(), true);
      }
      if (baseQuery == null ||
        !StringUtils.equals(query.getDescription(), baseQuery.getDescription()))
      {
        writeTagValue("description", query.getDescription(), true);
      }
      if (baseQuery == null ||
        !StringUtils.equals(query.getLabel(), baseQuery.getLabel()))
      {
        writeTagValue("label", query.getLabel(), true);
      }
      if (baseQuery == null ||
        !StringUtils.equals(query.getSql(), baseQuery.getSql()))
      {
        writeTagValue("sql", query.getSql(), true);
      }

      startTag("connection", true);
      Query.Connection connection = query.getConnection();
      if (baseQuery == null || !StringUtils.equals(connection.getDsn(), 
        baseQuery.getConnection().getDsn()))
      {
        writeTagValue("dsn", connection.getDsn());
      }
      if (baseQuery == null || !StringUtils.equals(connection.getDriver(), 
        baseQuery.getConnection().getDriver()))
      {
        writeTagValue("driver", connection.getDriver());
      }
      if (baseQuery == null || !StringUtils.equals(connection.getUrl(), 
        baseQuery.getConnection().getUrl()))
      {
        writeTagValue("url", connection.getUrl());
      }
      if (baseQuery == null || !StringUtils.equals(connection.getUsername(), 
        baseQuery.getConnection().getUsername()))
      {
        writeTagValue("username", connection.getUsername());
      }
      if (baseQuery == null || !StringUtils.equals(connection.getPassword(), 
        baseQuery.getConnection().getPassword()))
      {
        writeTagValue("password", connection.getPassword());
      }
      endTag("connection", true);

      StringBuilder sbParametersOrder = new StringBuilder();            
      for (Query.Parameter parameter : query.getParameters())
      {
        Query.Parameter baseParameter = baseQuery == null ? null :
          baseQuery.getParameter(parameter.getName());
        
        if (baseParameter == null ||
          !StringUtils.equals(parameter.getDescription(), baseParameter.getDescription()) ||
          !StringUtils.equals(parameter.getFormat(), baseParameter.getFormat()) ||
          !StringUtils.equals(removeCR(parameter.getSql()), 
            removeCR(baseParameter.getSql())) ||
          !StringUtils.equals(parameter.getDefaultValue(), baseParameter.getDefaultValue()))
        {
          startTag("parameter", parameter.getName(), true);
          writeTagValue("description", parameter.getDescription());
          writeTagValue("format", parameter.getFormat());
          writeTagValue("size", String.valueOf(parameter.getSize()));
          writeTagValue("defaultValue", parameter.getDefaultValue());
          writeTagValue("sql", parameter.getSql(), true);
          endTag("parameter", true);
        }
        sbParametersOrder.append(sbParametersOrder.length() == 0 ? "" : ",").
          append(parameter.getName());
      }
      writeTagValue("parametersOrder", sbParametersOrder.toString());      
      
      StringBuilder sbPredicatesOrder = new StringBuilder();      
      for (Query.Predicate predicate : query.getPredicates())
      {
        Query.Predicate basePredicate = baseQuery == null ? null :
          baseQuery.getPredicate(predicate.getName());

        if (basePredicate == null ||
          !StringUtils.equals(predicate.getLabel(), basePredicate.getLabel()) ||
          !StringUtils.defaultString(predicate.getShortLabel()).equals(
            StringUtils.defaultString(basePredicate.getShortLabel())) ||          
          !StringUtils.equals(removeCR(predicate.getSql()), 
            removeCR(basePredicate.getSql())))
        {
          startTag("predicate", predicate.getName(), true);
          writeTagValue("label", predicate.getLabel());
          writeTagValue("shortLabel", predicate.getShortLabel());
          writeTagValue("sql", predicate.getSql(), true);
          endTag("predicate", true);
        }
        sbPredicatesOrder.append(sbPredicatesOrder.length() == 0 ? "" : ",").
          append(predicate.getName());
      }
      writeTagValue("predicatesOrder", sbPredicatesOrder.toString());

      StringBuilder sbOutputsOrder = new StringBuilder();      
      for (Query.Output output : query.getOutputs())
      {        
        Query.Output baseOutput = baseQuery == null ? null :
          baseQuery.getOutput(output.getName());

        if (baseOutput == null ||
          !StringUtils.equals(output.getLabel(), baseOutput.getLabel()) ||          
          !StringUtils.defaultString(output.getDescription()).equals(
            StringUtils.defaultString(baseOutput.getDescription())) ||
          !StringUtils.equals(removeCR(output.getSql()), 
            removeCR(baseOutput.getSql())))
        {
          startTag("output", output.getName(), true);
          writeTagValue("label", output.getLabel());
          writeTagValue("description", output.getDescription());
          writeTagValue("sql", output.getSql(), true);
          endTag("output", true);
        }
        sbOutputsOrder.append(sbOutputsOrder.length() == 0 ? "" : ",").
          append(output.getName());
      }
      writeTagValue("outputsOrder", sbOutputsOrder.toString());

      for (QueryInstance instance : query.getInstances())
      {
        startTag("instance", instance.getName(), true);
        writeTagValue("description", instance.getDescription());
        if (instance.getMaxResults() != null)
        {
          writeTagValue("maxResults", String.valueOf(instance.getMaxResults()));
        }
        else
        {
          writeTagValue("maxResults", String.valueOf(
            QueryInstance.DEFAULT_MAX_RESULTS));
        }
        Set<Map.Entry<String, String>> entrySet =
          instance.getGlobalParameterValuesMap().entrySet();
        for (Map.Entry<String, String> entry : entrySet)
        {
          String name = entry.getKey();
          if (!name.startsWith("_"))
          {
            writeTagValue("parameter", name,
              String.valueOf(entry.getValue()), false);
          }
        }
        writeExpression(instance.getRootExpression());
        List<QueryInstance.Output> outputs = instance.getOutputs();
        for (QueryInstance.Output output : outputs)
        {
          writeTagValue("output", output.getName(), null, false);
        }
        endTag("instance", true);
      }
      endTag("query", true);
    }
    finally
    {
      writer.close();
      writer = null;
    }
  }

  protected void startTag(String tag)
  {
    startTag(tag, null, false);
  }

  protected void startTag(String tag, boolean hasChildren)
  {
    startTag(tag, null, hasChildren);
  }

  protected void startTag(String tag, String name, boolean hasChildren)
  {
    indent();
    writer.write("<" + tag);
    if (name != null)
    {
      writer.write(" name=\"" + name + "\"");
    }
    writer.write(">");
    if (hasChildren) writer.println();
    indent++;
  }

  protected void endTag(String tag)
  {
    endTag(tag, false);
  }

  protected void endTag(String tag, boolean hasChildren)
  {
    indent--;
    if (hasChildren)
    {
      indent();
    }
    writer.write("</" + tag + ">\n");
  }

  protected void writeTagValue(String tag, String value)
  {
    writeTagValue(tag, value, false);
  }

  protected void writeTagValue(String tag, String value, boolean cdata)
  {
    writeTagValue(tag, null, value, cdata);
  }

  protected void writeTagValue(String tag, String name, String value, boolean cdata)
  {
    startTag(tag, name, false);
    if (!StringUtils.isBlank(value))
    {
      if (cdata)
      {
        writer.print("<![CDATA[" + value + "]]>");
      }
      else
      {
        writer.print(value);
      }
    }
    endTag(tag);
  }

  protected void writeExpression(Expression expression)
  {
    if (expression instanceof Operator)
    {
      Operator operator = (Operator)expression;
      String tag = operator.getType();
      if (Operator.ROOT.equals(tag)) tag = "expression";
      tag = tag.toLowerCase();
      startTag(tag, true);
      for (Expression argument : operator.getArguments())
      {
        writeExpression(argument);
      }
      endTag(tag, true);
    }
    else if (expression instanceof Predicate)
    {
      Predicate predicate = (Predicate)expression;
      startTag("predicate", predicate.getName(), true);
      Set<Map.Entry<String, String>> entrySet =
        predicate.getParameterValuesMap().entrySet();
      for (Map.Entry<String, String> entry : entrySet)
      {
        String name = entry.getKey();
        if (!name.startsWith("_"))
        {
          writeTagValue("parameter", name, entry.getValue(), false);
        }
      }
      endTag("predicate", true);
    }
  }

  protected void indent()
  {
    for (int i = 0; i < indent; i++)
    {
      writer.write('\t');
    }
  }
  
  private String removeCR(String text)
  {
    return (text == null ? null : text.replace("\r", ""));
  }

  public static void main(String[] args)
  {
    try
    {
      QueryReader reader = new QueryReader();
      InputStream is = reader.getClass().getResourceAsStream("query.xml");
      Query query = reader.readQuery(is);
      QueryWriter writer = new QueryWriter();
      writer.writeQuery(query, new FileOutputStream("c:/query.xml"));
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
