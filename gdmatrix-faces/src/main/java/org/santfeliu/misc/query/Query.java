package org.santfeliu.misc.query;

import java.util.Collections;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.misc.query.QueryInstance.Expression;
import org.santfeliu.misc.query.QueryInstance.Operator;
import org.santfeliu.util.TextUtils;
import org.santfeliu.util.template.Template;

/**
 *
 * @author realor
 */
public class Query implements Serializable
{
  public static final String FILTER = "_FILTER";
  public static final String OUTPUT = "_OUTPUT";
  public static final String OUTPUT_NAMES = "_OUTPUT_NAMES";
  public static final String OUTPUT_LABELS = "_OUTPUT_LABELS";

  private String name;
  private String base;
  private String title;
  private String description;
  private String label;
  private String sql;
  private final Connection connection = new Connection();
  private final List<Parameter> parameters = new ArrayList<Parameter>();
  private final List<Predicate> predicates = new ArrayList<Predicate>();
  private final List<Output> outputs = new ArrayList<Output>();
  private final List<QueryInstance> instances = new ArrayList<QueryInstance>();

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    if (name == null) this.name = null;
    else this.name = TextUtils.normalizeName(name);
  }

  public String getBase()
  {
    return base;
  }

  public void setBase(String base)
  {
    this.base = base;
  }

  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }

  public Connection getConnection()
  {
    return connection;
  }

  public String getSql()
  {
    return sql;
  }

  public void setSql(String sql)
  {
    this.sql = sql;
  }

  public boolean isUpdateSql()
  {
    if (StringUtils.isBlank(sql)) return false;
    String upperSql = sql.toUpperCase();
    return upperSql.contains("INSERT") ||
      upperSql.contains("UPDATE") ||
      upperSql.contains("DELETE");
  }

  public List<Parameter> getParameters()
  {
    return Collections.unmodifiableList(parameters);
  }

  public List<Parameter> getGlobalParameters()
  {
    List<Parameter> globalParameters = new ArrayList<Parameter>();
    List variables = new ArrayList();
    Template.create(label).loadReferencedVariables(variables);
    for (Object v : variables)
    {
      String variable = v.toString();
      if (!OUTPUT.equals(variable) && !FILTER.equals(variable))
      {
        Parameter parameter = getParameter(variable);
        if (parameter == null)
        {
          parameter = new Parameter();
          parameter.setName(variable);
          parameter.setDescription(variable);
        }
        globalParameters.add(parameter);
      }
    }
    return globalParameters;
  }

  public int getPredicateCount()
  {
    return predicates.size();
  }
  
  public int getOutputCount()
  {
    return outputs.size();
  }
  
  public List<Predicate> getPredicates()
  {
    return Collections.unmodifiableList(predicates);
  }

  public List<Output> getOutputs()
  {
    return Collections.unmodifiableList(outputs);
  }

  public List<QueryInstance> getInstances()
  {
    return Collections.unmodifiableList(instances);
  }

  public Parameter addParameter()
  {
    Parameter parameter = new Parameter();
    parameters.add(parameter);
    return parameter;
  }

  public void removeParameter(Parameter parameter)
  {
    parameters.remove(parameter);
  }
  
  public void sortParameters()
  {
    Collections.sort(parameters, new Comparator()
    {
      public int compare(Object o1, Object o2)
      {
        Query.Parameter param1 = (Query.Parameter)o1;
        Query.Parameter param2 = (Query.Parameter)o2;
        return param1.getName().compareTo(param2.getName());
      }
    });
  }

  public Predicate addPredicate()
  {
    Predicate predicate = new Predicate();
    predicates.add(predicate);
    return predicate;
  }

  public void removePredicate(Predicate predicate)
  {
    if (predicates.remove(predicate))
    {
      for (QueryInstance instance : instances)
      {
        removePredicate(instance.getRootExpression(), predicate);
      }
    }
  }

  public void movePredicate(Predicate predicate, int offset)
  {
    int index = predicates.indexOf(predicate);
    if (index != -1)
    {
      predicates.remove(index);
      index += offset;
      if (index < 0) index = 0;
      else if (index > predicates.size()) index = predicates.size();
      predicates.add(index, predicate);
    }
  }
  
  protected void removePredicate(Expression expression, Predicate predicate)
  {
    if (expression instanceof QueryInstance.Predicate)
    {
      QueryInstance.Predicate predicateInstance =
        (QueryInstance.Predicate) expression;
      if (predicateInstance.getPredicate() == predicate)
      {
        predicateInstance.getParent().removeExpression(predicateInstance);
      }
    }
    else
    {
      Operator operator = (Operator)expression;
      List<QueryInstance.Expression> arguments = operator.getArguments();
      for (int i = arguments.size() - 1; i >= 0; i--)
      {
        QueryInstance.Expression argument = arguments.get(i);
        removePredicate(argument, predicate);
      }
    }
  }

  public void sortPredicates()
  {
    Collections.sort(predicates, new Comparator()
    {
      public int compare(Object o1, Object o2)
      {
        Query.Predicate predicate1 = (Query.Predicate)o1;
        Query.Predicate predicate2 = (Query.Predicate)o2;
        return predicate1.getName().compareTo(predicate2.getName());
      }
    });
  }

  public Output addOutput()
  {
    Output output = new Output();
    outputs.add(output);
    return output;
  }

  public void removeOutput(Output output)
  {
    if (outputs.remove(output))
    {
      for (QueryInstance instance : instances)
      {
        removeOutput(instance, output);
      }
    }
  }

  public void moveOutput(Output output, int offset)
  {
    int index = outputs.indexOf(output);
    if (index != -1)
    {
      outputs.remove(index);
      index += offset;
      if (index < 0) index = 0;
      else if (index > outputs.size()) index = outputs.size();
      outputs.add(index, output);
    }
  }
  
  protected void removeOutput(QueryInstance instance, Output output)
  {
    Iterator<QueryInstance.Output> iter = instance.getOutputs().iterator();
    while (iter.hasNext())
    {
      QueryInstance.Output outputInstance = iter.next();
      if (outputInstance.getOutput() == output)
      {
        iter.remove();
      }
    }
  }

  public void sortOutputs()
  {
    Collections.sort(outputs, new Comparator()
    {
      public int compare(Object o1, Object o2)
      {
        Query.Output output1 = (Query.Output)o1;
        Query.Output output2 = (Query.Output)o2;
        return output1.getName().compareTo(output2.getName());
      }
    });
  }

  public QueryInstance addInstance()
  {
    QueryInstance instance = new QueryInstance(this);
    instances.add(instance);
    return instance;
  }

  public void removeInstance(QueryInstance instance)
  {
    instances.remove(instance);
  }

  public Parameter getParameter(String name)
  {
    Parameter parameter = null;
    int i = 0;
    while (parameter == null && i < parameters.size())
    {
      Parameter p = parameters.get(i);
      if (name.equals(p.name)) parameter = p;
      else i++;
    }
    return parameter;
  }

  public Predicate getPredicate(String name)
  {
    Predicate predicate = null;
    int i = 0;
    while (predicate == null && i < predicates.size())
    {
      Predicate p = predicates.get(i);
      if (name.equals(p.name)) predicate = p;
      else i++;
    }
    return predicate;
  }

  public Output getOutput(String name)
  {
    Output output = null;
    int i = 0;
    while (output == null && i < outputs.size())
    {
      Output o = outputs.get(i);
      if (name.equals(o.name)) output = o;
      else i++;
    }
    return output;
  }

  public QueryInstance getInstance(String name)
  {
    QueryInstance instance = null;
    int i = 0;
    while (instance == null && i < instances.size())
    {
      QueryInstance inst = instances.get(i);
      if (name.equals(inst.getName())) instance = inst;
      else i++;
    }
    return instance;
  }

  /*** class Query.Connection ***/
  public class Connection implements Serializable
  {
    private String driver;
    private String url;
    private String username;
    private String password;

    public String getDriver()
    {
      return driver;
    }

    public void setDriver(String driver)
    {
      this.driver = driver;
    }

    public String getUrl()
    {
      return url;
    }

    public void setUrl(String url)
    {
      this.url = url;
    }

    public String getUsername()
    {
      return username;
    }

    public void setUsername(String username)
    {
      this.username = username;
    }

    public String getPassword()
    {
      return password;
    }

    public void setPassword(String password)
    {
      this.password = password;
    }
  }

  /*** class Query.Parameter ***/
  public class Parameter implements Serializable
  {
    public static final String TEXT = "text";
    public static final String NUMBER = "number";
    public static final String DATE = "date";

    private String name;
    private String description;
    private String format = TEXT;
    private int size = 10;
    private String defaultValue;
    private String sql;

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

    public String getSql()
    {
      return sql;
    }

    public void setSql(String sql)
    {
      this.sql = sql;
    }

    public String getFormat()
    {
      return format;
    }

    public void setFormat(String format)
    {
      this.format = format;
    }

    public int getSize()
    {
      return size;
    }

    public void setSize(int size)
    {
      this.size = size;
    }

    public String getDefaultValue()
    {
      return defaultValue;
    }

    public void setDefaultValue(String defaultValue)
    {
      this.defaultValue = defaultValue;
    }

    @Override
    public String toString()
    {
      return "Parameter " + name;
    }
  }

  /*** class Query.Fragment (Predicate|Output) ***/
  public abstract class Fragment implements Serializable
  {
    String name;
    String label;
    String sql;

    public String getName()
    {
      return name;
    }

    public void setName(String name)
    {
      this.name = TextUtils.normalizeName(name);
    }

    public String getLabel()
    {
      return label;
    }

    public void setLabel(String label)
    {
      this.label = label;
    }

    public String getSql()
    {
      return sql;
    }

    public void setSql(String sql)
    {
      this.sql = sql;
    }
  }

  /*** class Query.Predicate ***/
  public class Predicate extends Fragment
  {
    public List<Parameter> getParameters()
    {
      List<Parameter> parameters = new ArrayList<Parameter>();
      List variables = new ArrayList();
      Template.create(label).loadReferencedVariables(variables);
      for (Object v : variables)
      {
        String variable = v.toString();
        Parameter parameter = getParameter(variable);
        if (parameter == null)
        {
          parameter = new Parameter();
          parameter.setName(variable);
          parameter.setDescription(variable);
        }
        parameters.add(parameter);
      }
      return parameters;
    }

    @Override
    public String toString()
    {
      return "Predicate " + name;
    }
  }

  /*** class Query.Output ***/
  public class Output extends Fragment
  {
    @Override
    public String toString()
    {
      return "Output " + name;
    }
  }
}
