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
package org.santfeliu.misc.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.santfeliu.util.TextUtils;
import org.santfeliu.util.template.Template;
import org.santfeliu.util.template.WebTemplate;

/**
 *
 * @author realor
 */
public class QueryInstance implements Serializable
{
  public static final Integer DEFAULT_MAX_RESULTS = 1000;
  
  private final Query query;
  private String name;
  private String description;
  private final Operator rootExpression = new Operator(Operator.ROOT);
  private final List<Output> outputs = new ArrayList();
  private final HashMap<String, String> globalParameterValuesMap = 
    new HashMap();
  private final Map<String, Expression> expressions = new HashMap();
  private Integer maxResults;
  
  public QueryInstance(Query query)
  {
    this.query = query;
    expressions.put(rootExpression.getId(), rootExpression);
  }
  
  public Query getQuery()
  {
    return query;
  }
  
  public Operator getRootExpression()
  {
    return rootExpression;
  }

  public Map<String, Expression> getExpressions()
  {
    return expressions;
  }

  public Map<String, String> getGlobalParameterValuesMap()
  {
    return globalParameterValuesMap;
  }

  public List<Output> getOutputs()
  {
    return outputs;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = TextUtils.normalizeName(name);
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public Integer getMaxResults() 
  {
    return maxResults;
  }

  public void setMaxResults(Integer maxResults) 
  {
    this.maxResults = maxResults;
  }

  public Output addOutput(String name)
  {
    return addOutput(name, outputs.size());
  }
  
  public Output addOutput(String name, int position)
  {
    Query.Output output = query.getOutput(name);
    if (output == null) return null;
    Output outputInstance = new Output(output);
    outputs.add(position, outputInstance);
    return outputInstance;
  }

  public int indexOfOutput(String name)
  {
    int index = 0;
    boolean found = false;
    while (index < outputs.size() && !found)
    {
      Output output = outputs.get(index);
      if (output.getName().equals(name))
      {
        found = true;
      }
      else index++;
    }
    return found ? index : -1;
  }

  /*** class QueryInstance.Expression ***/
  public abstract class Expression implements Serializable
  {
    private final String id;
    private Operator parent;

    Expression()
    {
      id = UUID.randomUUID().toString();
    }

    public String getId()
    {
      return id;
    }

    public Operator getParent()
    {
      return parent;
    }
  }

  /*** class QueryInstance.Operator ***/
  public class Operator extends Expression
  {
    public static final String ROOT = "ROOT";
    public static final String AND = "AND";
    public static final String OR = "OR";
    public static final String NOR = "NOR";

    private String type;
    final List<Expression> arguments = new ArrayList();

    Operator(String type)
    {
      this.type = type;
    }

    public String getType()
    {
      return type;
    }

    public void setType(String type)
    {
      this.type = type;
    }

    public List<Expression> getArguments()
    {
      return arguments;
    }

    public Predicate addPredicate(String name)
    {
      Query.Predicate predicate = query.getPredicate(name);
      if (predicate == null) return null;
      Predicate predicateInstance = new Predicate(predicate);
      addExpression(predicateInstance);
      return predicateInstance;
    }

    public Operator addOperator(String type)
    {
      type = type.toUpperCase();
      if (type.equals(Operator.AND)
        || type.equals(Operator.OR)
        || type.equals(Operator.NOR))
      {
        Operator operator = new Operator(type);
        addExpression(operator);
        return operator;
      }
      return null;
    }

    public void removeExpression(Expression expression)
    {
      arguments.remove(expression);
    }

    protected void addExpression(Expression expression)
    {
      if (expression.parent == null)
      {
        expression.parent = this;
        arguments.add(expression);
        expressions.put(expression.getId(), expression);
      }
    }
  }

  /*** class QueryInstance.Predicate ***/
  public class Predicate extends Expression
  {
    private final Query.Predicate predicate;
    final HashMap<String, String> parameterValuesMap = new HashMap();

    Predicate(Query.Predicate predicate)
    {
      this.predicate = predicate;
    }

    public Query.Predicate getPredicate()
    {
      return predicate;
    }

    public String getName()
    {
      return predicate.getName();
    }

    public String getLabel()
    {
      return predicate.getLabel();
    }

    public List<Query.Parameter> getParameters()
    {
      return predicate.getParameters();
    }

    public Map getParameterValuesMap()
    {
      return parameterValuesMap;
    }
  }

  /*** class QueryInstance.Output ***/
  public class Output implements Serializable
  {
    private final Query.Output output;

    Output(Query.Output output)
    {
      this.output = output;
    }

    public Query.Output getOutput()
    {
      return output;
    }

    public String getName()
    {
      return output.getName();
    }
  }

  public String generateSql()
  {
    StringBuilder outputBuffer = new StringBuilder();
    StringBuilder outputNamesBuffer = new StringBuilder();
    StringBuilder outputLabelsBuffer = new StringBuilder();

    HashMap<String, String> variables = new HashMap();
    variables.putAll(globalParameterValuesMap);
    
    if (outputs.isEmpty())
    {
      outputBuffer.append("1");
      outputNamesBuffer.append("1");
      outputLabelsBuffer.append("1");
    }
    else
    {
      for (int i = 0; i < outputs.size(); i++)
      {
        if (i > 0)
        {
          outputBuffer.append(", ");
          outputNamesBuffer.append(", ");
          outputLabelsBuffer.append(", ");
        }
        Output output = outputs.get(i);
        String outputName = output.getName();
        String outputLabel = output.getOutput().getLabel();
        String sql = output.getOutput().getSql();
        sql = WebTemplate.create(sql).merge(variables);
        outputBuffer.append(sql);

        outputNamesBuffer.append(sql);
        outputNamesBuffer.append(" \"");
        outputNamesBuffer.append(outputName);
        outputNamesBuffer.append('"');

        outputLabelsBuffer.append(sql);
        outputLabelsBuffer.append(" \"");
        outputLabelsBuffer.append(outputLabel);
        outputLabelsBuffer.append('"');
      }
    }
    String outputString = outputBuffer.toString();
    String outputNamesString = outputNamesBuffer.toString();
    String outputLabelsString = outputLabelsBuffer.toString();
    
    String filterString = getSqlExpression(getRootExpression());
    if (filterString == null) filterString = "1=1";

    variables.put(Query.OUTPUT, outputString);
    variables.put(Query.OUTPUT_NAMES, outputNamesString);
    variables.put(Query.OUTPUT_LABELS, outputLabelsString);
    variables.put(Query.FILTER, filterString);
    // Deprecated variables:
    variables.put("OUTPUT", outputNamesString);
    variables.put("EXPRESSION", filterString);
    
    return WebTemplate.create(getQuery().getSql()).merge(variables);
  }

  private String getSqlExpression(Expression expression)
  {
    if (expression instanceof Operator)
    {
      Operator operator = (Operator)expression;
      StringBuilder buffer = new StringBuilder();
      int added = 0;
      String sqlOperator = operator.type.equals(Operator.AND) ? "AND" : "OR";
      for (Expression expr : operator.arguments)
      {
        String sqlExpr = getSqlExpression(expr);
        if (sqlExpr != null)
        {
          if (added > 0) buffer.append(" ").append(sqlOperator).append(" ");
          buffer.append(sqlExpr);
          added++;
        }
      }
      if (added == 0) return null;

      if (operator.type.equals(Operator.NOR))
      {
        return " NOT (" + buffer + ")";
      }
      else if (added == 1)
      {
        return buffer.toString();
      }
      return "(" + buffer + ")";
    }
    else // Predicate
    {
      Predicate predicate = (Predicate)expression;
      if (predicate.getName() != null)
      {
        String sql = getQuery().getPredicate(predicate.getName()).getSql();
        HashMap<String, String> variables = new HashMap();
        variables.putAll(globalParameterValuesMap);
        variables.putAll(predicate.getParameterValuesMap());
        return Template.create(sql).merge(variables);
      }
      else return null;
    }
  }
}
