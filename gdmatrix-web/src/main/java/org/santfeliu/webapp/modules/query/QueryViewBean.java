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
package org.santfeliu.webapp.modules.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.sql.QueryParameters;
import org.matrix.sql.QueryRow;
import org.matrix.sql.QueryTable;
import org.matrix.sql.SQLManagerPort;
import org.matrix.sql.SQLManagerService;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.primefaces.event.ReorderEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.ToggleSelectEvent;
import org.primefaces.event.UnselectEvent;
import org.santfeliu.misc.query.Query;
import org.santfeliu.misc.query.Query.Output;
import org.santfeliu.misc.query.QueryInstance;
import org.santfeliu.misc.query.QueryInstance.Expression;
import org.santfeliu.misc.query.QueryInstance.Operator;
import org.santfeliu.misc.query.QueryInstance.Predicate;
import org.santfeliu.misc.query.Query.Parameter;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.webapp.modules.sqlweb.SqlwebBean;

/**
 *
 * @author lopezrj-sf
 */
@Named
@RequestScoped
public class QueryViewBean extends WebBean implements Serializable
{
  private static final String OPERATOR_PREFIX = "operator:";
  
  private QueryInstance selectedInstance;
  private Expression selectedExpression;
  private int id = 0;
  private String selectedInstanceName;
  private String selectedInstanceDescription;  
  private List<SelectItem> instanceSelectItems;    
  private List<Output> allOutputs;
  private List<Output> selectedOutputs;
  private ParameterSelectItemsMap parameterSelectItemsMap;  
  
  @Inject
  QueryMainBean queryMainBean;
  
  @Inject
  QueryListBean queryListBean;  
  
  @Inject
  SqlwebBean sqlWebBean;
  
  public QueryViewBean()
  {
    Query query = getQuery();
    if (query.getInstances().isEmpty())
    {
      addInstance();
    }
    else
    {
      selectedInstance = query.getInstances().get(0);
      selectedInstanceName = selectedInstance.getName();
    }
  }
  
  /* ParameterSelectItemsMap */
  public class ParameterSelectItemsMap
    extends HashMap<String, List<SelectItem>> implements Serializable
  {
    @Override
    public List<SelectItem> get(Object key)
    {
      List<SelectItem> selectItems = super.get(key);
      if (selectItems == null)
      {
        selectItems = new ArrayList<>();
        Query query = getQuery();
        Query.Parameter parameter = query.getParameter((String)key);

        String adminUserId = 
          MatrixConfig.getProperty("adminCredentials.userId");
        String adminPassword = 
          MatrixConfig.getProperty("adminCredentials.password");

        WSDirectory wsDirectory = WSDirectory.getInstance();
        WSEndpoint endpoint = wsDirectory.getEndpoint(SQLManagerService.class);
        SQLManagerPort sqlPort = endpoint.getPort(SQLManagerPort.class,
          adminUserId, adminPassword);

        Query.Connection conn = query.getConnection();
        QueryParameters parameters = new QueryParameters();
        try
        {
          QueryTable result;
          if (!StringUtils.isBlank(conn.getDsn()))
          {
            result = sqlPort.executeAliasQuery(parameter.getSql(),
              parameters, conn.getDsn(), conn.getUsername(), 
              conn.getPassword());
          }
          else
          {
            result = sqlPort.executeDriverQuery(parameter.getSql(),
              parameters, conn.getDriver(), conn.getUrl(),
              conn.getUsername(), conn.getPassword());          
          }
          
          for (QueryRow row : result.getQueryRow())
          {
            List values = row.getValues();
            String itemValue = String.valueOf(values.get(0));
            String itemLabel = values.size() > 1 ?
              String.valueOf(values.get(1)) : itemValue;
            SelectItem selectItem = new SelectItem();
            selectItem.setValue(itemValue);
            selectItem.setLabel(itemLabel);
            selectItems.add(selectItem);
          }
        }
        catch (Exception ex)
        {
          error(ex);
        }
        super.put((String)key, selectItems);
      }
      return selectItems;
    }
  }

  public Query getQuery()
  {
    return (Query)getValue("#{queryMainBean.query}");
  }

  public void setSelectedInstance(QueryInstance selectedInstance)
  {
    this.selectedInstance = selectedInstance;
  }

  public QueryInstance getSelectedInstance()
  {
    return selectedInstance;
  }

  public String getSelectedInstanceName()
  {
    return selectedInstanceName;
  }

  public void setSelectedInstanceName(String selectedInstanceName)
  {
    this.selectedInstanceName = selectedInstanceName;
  }

  public Map<String, Expression> getExpressions()
  {
    return selectedInstance.getExpressions();
  }

  public void setSelectedExpression(Expression selectedExpression)
  {
    this.selectedExpression = selectedExpression;
  }

  public Expression getSelectedExpression()
  {
    return selectedExpression;
  }

  public void setInstanceSelectItems(List<SelectItem> instanceSelectItems)
  {
    this.instanceSelectItems = instanceSelectItems;
  }

  public List<SelectItem> getInstanceSelectItems()
  {
    if (instanceSelectItems == null)
    {
      instanceSelectItems = new ArrayList();
      List<QueryInstance> instances = getQuery().getInstances();
      for (QueryInstance instance : instances)
      {
        SelectItem selectItem = new SelectItem();
        selectItem.setValue(instance.getName());
        selectItem.setLabel(instance.getDescription());
        selectItem.setDescription(instance.getDescription());
        instanceSelectItems.add(selectItem);
      }
    }
    return instanceSelectItems;
  }

  public void setParameterSelectItemsMap(
    ParameterSelectItemsMap parameterSelectItemsMap)
  {
    this.parameterSelectItemsMap = parameterSelectItemsMap;
  }

  public Map<String, List<SelectItem>> getParameterSelectItemsMap()
  {
    if (parameterSelectItemsMap == null)
    {
      parameterSelectItemsMap = new ParameterSelectItemsMap();
    }
    return parameterSelectItemsMap;
  }

  public String getSelectedInstanceDescription()
  {
    return selectedInstanceDescription;
  }

  public void setSelectedInstanceDescription(String selectedInstanceDescription)
  {
    this.selectedInstanceDescription = selectedInstanceDescription;
  }

  public void removeExpression(Expression expression)
  {
    expression.getParent().removeExpression(expression);
  }

  public void selectExpression(Expression expression)
  {
    selectedExpression = expression;    
  }  
  
  public boolean isAddButtonRendered(Expression expression)
  {
    if (expression instanceof Operator)
    {
      Operator operator = (Operator)expression;
      if (operator.getType().equals(Operator.ROOT))
      {
        return operator.getArguments().isEmpty() && 
          getQuery().getPredicateCount() > 0;
      }
      return true;
    }
    return false;
  }

  public String getLinkLabel(Expression expression)
  {
    return expression instanceof Operator ?
      getQueryLabel(((Operator)expression).getType() + "_link") : null;
  }  
  
  public boolean isOperator(Expression expression)
  {    
    return (expression instanceof Operator);
  }
  
  public String getOperatorType(Operator operator)
  {
    return operator.getType();
  }
  
  public void selectInstance()
  {
    selectedInstance = getQuery().getInstance(selectedInstanceName);
    allOutputs = null;
    selectedOutputs = null;
  }

  public List<Output> getAllOutputs()
  {
    if (allOutputs == null)
    {
      allOutputs = new ArrayList();      
      for (QueryInstance.Output instanceOutput : selectedInstance.getOutputs())
      {
        allOutputs.add(instanceOutput.getOutput());
      }
      for (Output queryOutput : getQuery().getOutputs())
      {
        if (selectedInstance.indexOfOutput(queryOutput.getName()) == -1) 
        {
          //not selected          
          allOutputs.add(queryOutput);
        }        
      }
    }
    return allOutputs;
  }

  public void setAllOutputs(List<Output> allOutputs)
  {
    this.allOutputs = allOutputs;
  }

  public List<Output> getSelectedOutputs()
  {
    if (selectedOutputs == null)
    {
      selectedOutputs = new ArrayList();
      for (QueryInstance.Output instanceOutput : selectedInstance.getOutputs())
      {
        selectedOutputs.add(instanceOutput.getOutput());
      }
    }
    return selectedOutputs;
  }

  public void setSelectedOutputs(List<Output> selectedOutputs)
  {
    this.selectedOutputs = selectedOutputs;
  }
  
  public void onOutputSelect(SelectEvent<Output> event) 
  {
    syncOutputs();
  }

  public void onOutputUnselect(UnselectEvent<Output> event) 
  {
    syncOutputs();    
  }
  
  public void onOutputReorder(ReorderEvent event)
  {
    syncOutputs();
  }
  
  public void onOutputToggleAll(ToggleSelectEvent event)
  {
    syncOutputs();
  }
  
  private void syncOutputs()
  {
    selectedInstance.getOutputs().clear();
    for (Output output : allOutputs)
    {
      if (selectedOutputs.contains(output)) //selected
      {
        selectedInstance.addOutput(output.getName());
      }
    }
  }
  
  private List<SelectItem> getAllOutputSelectItems()
  {
    List outputSelectItems = new ArrayList();
    List<Query.Output> auxOutputs = getQuery().getOutputs();
    for (Query.Output output : auxOutputs)
    {
      SelectItem selectItem = new SelectItem();
      selectItem.setValue(output.getName());
      selectItem.setLabel(output.getLabel());
      selectItem.setDescription(output.getLabel());
      outputSelectItems.add(selectItem);
    }
    return outputSelectItems;
  }  
  
  public Operator getRootExpression()
  {
    return selectedInstance.getRootExpression();
  }

  public Converter<SelectItem> getOutputConverter()
  {
    return new Converter<SelectItem>() 
    {
      @Override
      public SelectItem getAsObject(FacesContext fc, UIComponent uic, String s)
      {
        List<SelectItem> list = getAllOutputSelectItems();
        for (SelectItem item : list)
        {
          if (s.equals((String)item.getValue())) return item;
        }
        return null;
      }

      @Override
      public String getAsString(FacesContext fc, UIComponent uic, SelectItem t)
      {
        return (String)t.getValue();        
      }      
    };
  }
  
  public void cancel()
  {
    selectedExpression = null;
  }

  public void validateNumber(FacesContext context, UIComponent component,
    Object value) throws ValidatorException
  {
    try
    {
      Double.parseDouble(String.valueOf(value));
    }
    catch (NumberFormatException ex)
    {
      FacesMessage message = new FacesMessage("INVALID_NUMBER");
      throw new ValidatorException(message);
    }
  }

  public void addExpression(String expressionName)
  {
    Operator op = (Operator)selectedExpression;
    if (expressionName.startsWith(OPERATOR_PREFIX))
    {
      String operator = expressionName.substring(OPERATOR_PREFIX.length());
      op.addOperator(operator);
    }
    else
    {
      Predicate predicate = op.addPredicate(expressionName);
      putPredicateDefaultValues(predicate);      
    }
    selectedExpression = null;
  }
  
  public String getExpressionLabel(String expressionName)
  {
    if (expressionName.startsWith(OPERATOR_PREFIX))
    {
      String operator = expressionName.substring(OPERATOR_PREFIX.length());
      return getQueryLabel(operator);
    }
    else
    {
      Query.Predicate predicate = getQuery().getPredicate(expressionName);      
      return (StringUtils.defaultString(predicate.getShortLabel()).isEmpty() ? 
        predicate.getLabel() : 
        predicate.getShortLabel());
    }
  }
  
  public List<String> getLabelTokens(String label)
  {
    List<String> items = new ArrayList<>();
    Pattern pattern = Pattern.compile("\\$\\{.*?\\}");
    Matcher matcher = pattern.matcher(label);
    int start = 0;
    while (matcher.find())
    {
      if (start != matcher.start())
      {
        items.add("t:" + label.substring(start, matcher.start()));
      }
      items.add("p:" + label.substring(matcher.start() + 2, matcher.end() - 1));
      start = matcher.end();
    }
    if (start != label.length())
    {
      items.add("t:" + label.substring(start));
    }
    return items;
  }

  public Parameter getParameter(List<Parameter> parameters, String paramName)  
  {
    for (Parameter param : parameters)
    {
      if (paramName.equals(param.getName())) return param;
    }
    return null;
  }

  public List getExpressionListItems()
  {
    List list = new ArrayList();
    list.add(OPERATOR_PREFIX + Operator.AND);
    list.add(OPERATOR_PREFIX + Operator.OR);
    list.add(OPERATOR_PREFIX + Operator.NOR);
    List<Query.Predicate> predicates = getQuery().getPredicates();
    for (Query.Predicate predicate : predicates)
    {
      list.add(predicate.getName());
    }
    return list;    
  }

  public void editQuery()
  {
    queryMainBean.setView("query_edit");
  }
  
  public void reloadQuery()
  {
    try
    {
      queryMainBean.reloadQuery();
      instanceSelectItems = null;
      allOutputs = null;
      selectedOutputs = null;
      selectedInstance = getQuery().getInstances().get(0);
      selectedInstanceName = selectedInstance.getName();
      selectedInstanceDescription = null;
      info("QUERY_RELOAD");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public void saveQuery()
  {
    try
    {
      queryMainBean.setCreateNewVersion(false);
      queryMainBean.saveQuery(false);
      info("QUERY_SAVED");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public void saveQueryNewVersion()
  {
    try
    {
      queryMainBean.setCreateNewVersion(true);
      queryMainBean.saveQuery(false);
      info("QUERY_SAVED");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }  

  public void addInstance()
  {
    Query query = getQuery();
    int num = query.getInstances().size() + 1;
    selectedInstanceName = "inst_" + num;
    while (query.getInstance(selectedInstanceName) != null)
    {
      num++;
      selectedInstanceName = "inst_" + num;
    }
    selectedInstance = query.addInstance();
    selectedInstance.setName(selectedInstanceName);
    selectedInstance.setDescription("Instància " + num);
    selectedInstance.setMaxResults(QueryInstance.DEFAULT_MAX_RESULTS);
    putInstanceDefaultValues(selectedInstance);
    allOutputs = null;
    selectedOutputs = null;
    instanceSelectItems = null;
  }

  public void removeInstance()
  {
    Query query = getQuery();
    query.removeInstance(selectedInstance);
    if (query.getInstances().isEmpty())
    {
      addInstance();
    }
    else
    {
      selectedInstance = query.getInstances().get(0);
      selectedInstanceName = selectedInstance.getName();
      allOutputs = null;
      selectedOutputs = null;
    }
    instanceSelectItems = null;    
  }

  public String renameInstance()
  {
    selectedInstanceDescription = selectedInstance.getDescription();
    return null;
  }

  public String acceptRename()
  {
    selectedInstance.setDescription(selectedInstanceDescription);
    selectedInstanceDescription = null;
    instanceSelectItems = null;
    return null;
  }

  public String cancelRename()
  {
    selectedInstanceDescription = null;
    return null;
  }

  public void execute()
  {
    putAutomaticParameters();
    Query query = getQuery();
    Query.Connection connection = query.getConnection();
    if (!StringUtils.isBlank(connection.getDsn()))
    {
      sqlWebBean.setDsn(connection.getDsn());
    }
    else
    {
      sqlWebBean.setDsn(null);
    }
    sqlWebBean.setDriver(connection.getDriver());
    sqlWebBean.setUrl(connection.getUrl());
    sqlWebBean.setUsername(connection.getUsername());
    sqlWebBean.setPassword(connection.getPassword());
    sqlWebBean.setTitle(query.getTitle());
    sqlWebBean.setSql(selectedInstance.generateSql());
    sqlWebBean.setEditMode(queryMainBean.isEditionEnabled());
    sqlWebBean.setMaxRows(selectedInstance.getMaxResults());
    for (QueryInstance.Output output : selectedInstance.getOutputs())
    {
      String label = output.getOutput().getLabel();
      String description = output.getOutput().getDescription();
      if (description != null && !description.isEmpty())
      {
        sqlWebBean.getColumnDescriptionMap().put(label, description);
      }
    }
    sqlWebBean.setDeferredExecution(true);
    if (!sqlWebBean.isEditMode())
    {
      sqlWebBean.setHtmlDescription(getExecutionHtmlDescription());
    }
    queryMainBean.setView("query_results");
  }

  protected void putAutomaticParameters()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    Map<String, String> map = selectedInstance.getGlobalParameterValuesMap();
    Date now = new Date();
    map.put("_USERID", userSessionBean.getUserId());
    map.put("_DATE", TextUtils.formatDate(now, "yyyyMMdd"));
    map.put("_DATETIME", TextUtils.formatDate(now, "yyyyMMddHHmmss"));
  }
  
  protected String getQueryLabel(String key)
  {
    return (String)getValue("#{queryBundle." + key + "}");
  }

  /* NEW METHODS */
  
  public String getLanguage()
  {
    return getLocale().getLanguage();
  }

  private void putInstanceDefaultValues(QueryInstance instance)
  {    
    for (Query.Parameter parameter : instance.getQuery().getGlobalParameters())
    {
      instance.getGlobalParameterValuesMap().put(parameter.getName(), 
        parameter.getDefaultValue());
    }
  }
  
  private void putPredicateDefaultValues(Predicate predicate)
  {
    for (Query.Parameter parameter : predicate.getParameters())
    {
      predicate.getParameterValuesMap().put(parameter.getName(), 
        parameter.getDefaultValue());
    }
  }
  
  // QUERY HTML DESCRIPTION
  
  private String getExecutionHtmlDescription()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("<div class='mb-4'>");
    String queryLabel = selectedInstance.getQuery().getLabel();
    List<String> mainParamNames = extractMainParameters(queryLabel);
    for (String mainParamName : mainParamNames)
    {
      String mainParamValue = 
        selectedInstance.getGlobalParameterValuesMap().get(mainParamName);
      String labeledValue = getLabeledValue(mainParamName, mainParamValue);
      queryLabel = queryLabel.replace("${" + mainParamName + "}", 
        "<b>" + labeledValue + "</b>");
    }
    sb.append(getItemHtml(queryLabel, 0));
    sb.append(getExpressionHtmlDescription(getRootExpression(), 0, true, null));    
    sb.append("</div>");
    return sb.toString();
  }  
  
  private String getItemHtml(String text, int depth)
  {
    StringBuilder sb = new StringBuilder();
    sb.append("<div style='margin-left: ").append(depth * 20).
      append("px;' class='mt-1 mb-1'>");
    sb.append(text);
    sb.append("</div>");
    return sb.toString();
  }

  private String getExpressionHtmlDescription(
    Expression expression, int depth, boolean first, Operator currentOperator)
  {
    StringBuilder sb = new StringBuilder();
    if (!first)
    {
      String argumentSeparator = 
        translate(currentOperator.getType() + "_link");
      sb.append(getItemHtml(argumentSeparator, depth));          
    }      
    if (expression instanceof Operator)
    {
      Operator operator = (Operator)expression;
      if (!operator.getType().equals(Operator.ROOT))
      {
        String operatorLabel = translate(operator.getType());        
        sb.append(getItemHtml(operatorLabel, depth));
      }
      int i = 0;
      for (Expression expr : operator.getArguments())
      {
        sb.append(
          getExpressionHtmlDescription(expr, depth + 1, (i == 0), operator));
        i++;
      }
    }
    else // Predicate
    {
      Predicate predicate = (Predicate)expression;
      String predicateLabel = predicate.getLabel();      
      for (Object paramName : predicate.getParameterValuesMap().keySet())
      {
        String paramValue = 
          (String)predicate.getParameterValuesMap().get(paramName);
        String sParamName = (String)paramName;
        String labeledValue = getLabeledValue(sParamName, paramValue);
        predicateLabel = predicateLabel.replace("${" + sParamName + "}", 
          "<b>" + labeledValue + "</b>");
      }
      sb.append(getItemHtml(predicateLabel, depth));        
    }
    return sb.toString();
  }
  
  private String translate(String key)
  {
    try
    {
      String queryBundlePath = 
        "org.santfeliu.misc.query.web.resources.QueryBundle";      
      ResourceBundle bundle = ResourceBundle.getBundle(queryBundlePath, 
        getFacesContext().getViewRoot().getLocale());
      return bundle.getString(key);
    }
    catch (Exception ex)
    {
      return key;
    }
  }
  
  private List<String> extractMainParameters(String text) 
  {
    List<String> result = new ArrayList<>();
    Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
    Matcher matcher = pattern.matcher(text);
    while (matcher.find()) 
    {
      result.add(matcher.group(1));
    }
    return result;
  }
  
  private String getLabeledValue(String paramName, String value)
  {       
    Parameter parameter = getQuery().getParameter(paramName);
    if (parameter != null)
    {
      if (!StringUtils.isBlank(StringUtils.defaultString(parameter.getSql())))
      {
        List<SelectItem> items = getParameterSelectItemsMap().get(paramName);
        if (items != null)
        {
          for (SelectItem item : items)
          {
            String itemValue = (String)item.getValue();
            if (itemValue != null && 
              itemValue.equals(value))
            {
              return StringUtils.defaultIfBlank(item.getLabel(), "N/A");
            }
          }
        }
      }
      else if (parameter.getFormat().equals(Parameter.DATE))
      {
        return StringUtils.defaultIfBlank(
          TextUtils.formatInternalDate(value, "dd/MM/yyyy"), "N/A");
      }
    }    
    return StringUtils.defaultIfBlank(value, "N/A");
  }

}
