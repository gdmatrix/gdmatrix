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
      Query.Connection baseConnection = query.getConnection();
      if (baseQuery == null ||
        !StringUtils.equals(connection.getDriver(), baseConnection.getDriver()))
      {
        writeTagValue("driver", connection.getDriver());
      }
      if (baseQuery == null ||
        !StringUtils.equals(connection.getUrl(), baseConnection.getUrl()))
      {
        writeTagValue("url", connection.getUrl());
      }
      if (baseQuery == null ||
        !StringUtils.equals(connection.getUsername(), baseConnection.getUsername()))
      {
        writeTagValue("username", connection.getUsername());
      }
      if (baseQuery == null ||
        !StringUtils.equals(connection.getPassword(), baseConnection.getPassword()))
      {
        writeTagValue("password", connection.getPassword());
      }
      endTag("connection", true);

      for (Query.Parameter parameter : query.getParameters())
      {
        Query.Parameter baseParameter = baseQuery == null ? null :
          baseQuery.getParameter(parameter.getName());
        
        if (baseParameter == null ||
          !StringUtils.equals(parameter.getDescription(), baseParameter.getDescription()) ||
          !StringUtils.equals(parameter.getFormat(), baseParameter.getFormat()) ||
          !StringUtils.equals(parameter.getSql(), baseParameter.getSql()) ||
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
      }

      for (Query.Predicate predicate : query.getPredicates())
      {
        Query.Predicate basePredicate = baseQuery == null ? null :
          baseQuery.getPredicate(predicate.getName());

        if (basePredicate == null ||
          !StringUtils.equals(predicate.getLabel(), basePredicate.getLabel()) ||
          !StringUtils.equals(predicate.getSql(), basePredicate.getSql()))
        {
          startTag("predicate", predicate.getName(), true);
          writeTagValue("label", predicate.getLabel());
          writeTagValue("sql", predicate.getSql(), true);
          endTag("predicate", true);
        }
      }

      for (Query.Output output : query.getOutputs())
      {
        Query.Output baseOutput = baseQuery == null ? null :
          baseQuery.getOutput(output.getName());

        if (baseOutput == null ||
          !StringUtils.equals(output.getLabel(), baseOutput.getLabel()) ||
          !StringUtils.equals(output.getSql(), baseOutput.getSql()))
        {
          startTag("output", output.getName(), true);
          writeTagValue("label", output.getLabel());
          writeTagValue("sql", output.getSql(), true);
          endTag("output", true);
        }
      }

      for (QueryInstance instance : query.getInstances())
      {
        startTag("instance", instance.getName(), true);
        writeTagValue("description", instance.getDescription());
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
