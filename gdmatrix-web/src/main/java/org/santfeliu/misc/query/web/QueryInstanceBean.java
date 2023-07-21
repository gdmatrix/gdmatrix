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
package org.santfeliu.misc.query.web;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UISelectItems;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.component.html.HtmlCommandLink;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;
import org.apache.commons.lang.StringUtils;
import org.apache.myfaces.component.html.ext.HtmlPanelGroup;
import org.apache.myfaces.custom.datalist.HtmlDataList;
import org.matrix.sql.QueryParameters;
import org.matrix.sql.QueryRow;
import org.matrix.sql.QueryTable;
import org.matrix.sql.SQLManagerPort;
import org.matrix.sql.SQLManagerService;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.faces.FacesBean;
import org.santfeliu.faces.beansaver.Savable;
import org.santfeliu.faces.component.jqueryui.HtmlCalendar;
import org.santfeliu.misc.query.Query;
import org.santfeliu.misc.query.QueryInstance;
import org.santfeliu.misc.query.QueryInstance.Expression;
import org.santfeliu.misc.query.QueryInstance.Operator;
import org.santfeliu.misc.query.QueryInstance.Predicate;
import org.santfeliu.misc.query.Query.Output;
import org.santfeliu.misc.sqlweb.web.SqlWebBean;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author realor
 */
public class QueryInstanceBean extends FacesBean implements Savable
{
  private static final String OPERATOR_PREFIX = "operator:";
  private QueryInstance selectedInstance;
  private transient HtmlPanelGroup panelGroup;
  private final ExpressionWrapperMap expressionWrappers =
    new ExpressionWrapperMap();
  private Expression selectedExpression;
  private int id = 0;
  private Integer scroll;
  private String selectedInstanceName;
  private String selectedInstanceDescription;  
  private transient List<SelectItem> instanceSelectItems;
  private transient List<SelectItem> availableOutputSelectItems;
  private transient List<SelectItem> selectedOutputSelectItems;
  private transient ParameterSelectItemsMap parameterSelectItemsMap;
  private String[] outputsToAdd;
  private String[] outputsToRemove;

  public QueryInstanceBean()
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

  /* ExpressionWrapper */
  public class ExpressionWrapper implements Serializable
  {
    private final Expression expression;

    public ExpressionWrapper(Expression expression)
    {
      this.expression = expression;
    }

    public Expression getExpression()
    {
      return expression;
    }

    public String getLinkLabel()
    {
      return expression instanceof Operator ?
        getQueryLabel(((Operator)expression).getType() + "_link") : null;
    }

    public String removeExpression()
    {
      expression.getParent().removeExpression(expression);
      return "query_instance";
    }

    public String selectExpression()
    {
      selectedExpression = expression;
      return "query_instance";
    }

    public boolean isAddButtonRendered()
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
  }

  /* ExpressionWrapperMap */
  public class ExpressionWrapperMap
    extends AbstractMap<String, ExpressionWrapper> implements Serializable
  {
    @Override
    public ExpressionWrapper get(Object expressionId)
    {
      Expression expression = getExpressions().get(expressionId);
      return new ExpressionWrapper(expression);
    }

    @Override
    public Set entrySet()
    {
      return Collections.EMPTY_SET;
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
          QueryTable result = sqlPort.executeDriverQuery(parameter.getSql(),
            parameters, conn.getDriver(), conn.getUrl(),
            conn.getUsername(), conn.getPassword());

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

  public final Query getQuery()
  {
    return (Query)getValue("#{queryBean.query}");
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

  public ExpressionWrapperMap getExpressionWrappers()
  {
    return expressionWrappers;
  }

  public Map<String, Expression> getExpressions()
  {
    return selectedInstance.getExpressions();
  }

  public Expression getSelectedExpression()
  {
    return selectedExpression;
  }

  public Integer getScroll()
  {
    return scroll;
  }

  public void setScroll(Integer scroll)
  {
    this.scroll = scroll;
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

  public String selectInstance()
  {
    selectedInstance = getQuery().getInstance(selectedInstanceName);
    clearSelectItems();
    return "query_instance";
  }

  public List<SelectItem> getAvailableOutputSelectItems()
  {
    if (availableOutputSelectItems == null)
    {
      availableOutputSelectItems = new ArrayList();
      List<Query.Output> outputs = getQuery().getOutputs();
      for (Query.Output output : outputs)
      {
        if (selectedInstance.indexOfOutput(output.getName()) == -1)
        {
          SelectItem selectItem = new SelectItem();
          selectItem.setValue(output.getName());
          selectItem.setLabel(output.getLabel());
          selectItem.setDescription(output.getLabel());
          availableOutputSelectItems.add(selectItem);
        }
      }      
    }
    return availableOutputSelectItems;
  }

  public List<SelectItem> getSelectedOutputSelectItems()
  {
    if (selectedOutputSelectItems == null)
    {
      selectedOutputSelectItems = new ArrayList();
      List<QueryInstance.Output> outputInstances =
        selectedInstance.getOutputs();
      for (QueryInstance.Output outputInstance : outputInstances)
      {
        Output output = getQuery().getOutput(outputInstance.getName());
        SelectItem selectItem = new SelectItem();
        selectItem.setValue(output.getName());
        selectItem.setLabel(output.getLabel());
        selectItem.setDescription(output.getLabel());
        selectedOutputSelectItems.add(selectItem);
      }
    }
    return selectedOutputSelectItems;
  }

  public String[] getOutputsToAdd()
  {
    return outputsToAdd;
  }

  public void setOutputsToAdd(String[] outputsToAdd)
  {
    this.outputsToAdd = outputsToAdd;
  }

  public String[] getOutputsToRemove()
  {
    return outputsToRemove;
  }

  public void setOutputsToRemove(String[] outputsToRemove)
  {
    this.outputsToRemove = outputsToRemove;
  }

  public Operator getRootExpression()
  {
    return selectedInstance.getRootExpression();
  }

  public void addOutputs()
  {
    int position = selectedInstance.getOutputs().size();
    if (outputsToRemove.length > 0)
    {
      String firstOutputToRemove = outputsToRemove[0];
      position = selectedInstance.indexOfOutput(firstOutputToRemove);
    }
    for (String outputName : outputsToAdd)
    {
      selectedInstance.addOutput(outputName, position++);
    }
    clearSelectItems();
  }

  public void removeOutputs()
  {
    for (String outputName : outputsToRemove)
    {
      int index = selectedInstance.indexOfOutput(outputName);
      if (index != -1)
      {
        selectedInstance.getOutputs().remove(index);
      }
    }
    clearSelectItems();
  }

  public String cancel()
  {
    selectedExpression = null;
    return "query_instance";
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

  public String addExpression()
  {
    Operator op = (Operator)selectedExpression;
    String expressionName = (String)getValue("#{expression}");
    if (expressionName.startsWith(OPERATOR_PREFIX))
    {
      String operator = expressionName.substring(OPERATOR_PREFIX.length());
      op.addOperator(operator);
    }
    else
    {
      op.addPredicate(expressionName);
    }
    selectedExpression = null;
    return "query_instance";
  }

  public void setExpressionComponents(HtmlPanelGroup panelGroup)
  {
    this.panelGroup = panelGroup;
  }

  public HtmlPanelGroup getExpressionComponents()
  {
    panelGroup = new HtmlPanelGroup();
    panelGroup.setId("expression");
    panelGroup.setStyleClass("operator");
    addExpressionComponents(selectedInstance.getRootExpression(), panelGroup);
    return panelGroup;
  }

  public String getExpressionLabel()
  {
    String expressionName = (String)getValue("#{expression}");
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

  public void addExpressionComponents(Expression expression,
    HtmlPanelGroup parentGroup)
  {
    id++;
    HtmlPanelGroup exprPanelGroup = new HtmlPanelGroup();
    exprPanelGroup.setId("expr_" + id);
    parentGroup.getChildren().add(exprPanelGroup);

    if (expression instanceof Operator)
    {
      Operator operator = (Operator)expression;
      addOperatorComponents(operator, exprPanelGroup);
    }
    else // expression is not operator
    {
      Predicate predicate = (Predicate)expression;
      addPredicateComponents(predicate, exprPanelGroup);
    }
  }

  private void addOperatorComponents(Operator operator,
    HtmlPanelGroup exprPanelGroup)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Application application = context.getApplication();

    ValueBinding valueBinding = context.getApplication().createValueBinding(
      "expression #{queryInstanceBean.expressions['" + operator.getId() +
      "'].type}");
    exprPanelGroup.setValueBinding("styleClass", valueBinding);

    if (operator.getType().equals(Operator.ROOT))
    {
      addLabelComponents(selectedInstance.getQuery().getLabel(),
        "selectedInstance.globalParameterValuesMap",
        selectedInstance.getQuery().getGlobalParameters(), exprPanelGroup);
    }
    else // other operators: and, or, nor
    {
      // add operator select box
      id++;
      HtmlSelectOneMenu selectMenu = new HtmlSelectOneMenu();
      selectMenu.setId(operator.getType() + "_" + id);
      selectMenu.setStyleClass("operator");
      selectMenu.setOnchange("submit();");
      String valueExpr = "#{queryInstanceBean.expressions['" +
        operator.getId() + "'].type}";
      valueBinding = context.getApplication().createValueBinding(valueExpr);
      selectMenu.setValueBinding("value", valueBinding);
      ArrayList list = new ArrayList();

      SelectItem item1 = new SelectItem();
      item1.setDescription(Operator.AND);
      item1.setValue(Operator.AND);
      item1.setLabel(getQueryLabel(Operator.AND));
      list.add(item1);

      SelectItem item2 = new SelectItem();
      item2.setDescription(Operator.OR);
      item2.setValue(Operator.OR);
      item2.setLabel(getQueryLabel(Operator.OR));
      list.add(item2);

      SelectItem item3 = new SelectItem();
      item3.setDescription(Operator.NOR);
      item3.setValue(Operator.NOR);
      item3.setLabel(getQueryLabel(Operator.NOR));
      list.add(item3);

      id++;
      UISelectItems selectItems = new UISelectItems();
      selectItems.setId("si_" + id);
      selectItems.setValue(list);
      selectMenu.getChildren().add(selectItems);
      exprPanelGroup.getChildren().add(selectMenu);

      id++;
      HtmlCommandButton button = new HtmlCommandButton();
      button.setId("button_" + id);
      MethodBinding method = application.createMethodBinding(
        "#{queryInstanceBean.expressionWrappers['" + operator.getId() +
        "'].removeExpression}", new Class[0]);
      button.setAction(method);
      button.setImage("/common/misc/images/remove_expr.png");
      button.setStyleClass("expr_button remove");
      exprPanelGroup.getChildren().add(button);
    }
    id++;
    HtmlPanelGroup subPanelGroup = new HtmlPanelGroup();
    subPanelGroup.setId("comp_" + id);
    subPanelGroup.setStyleClass("op_children");
    exprPanelGroup.getChildren().add(subPanelGroup);

    for (Expression expr : operator.getArguments())
    {
      addExpressionComponents(expr, subPanelGroup);
      if (!operator.getType().equals(Operator.ROOT))
      {
        id++;
        HtmlOutputText linkText = new HtmlOutputText();
        linkText.setId("link_" + id);
        valueBinding = application.createValueBinding(
          "#{queryInstanceBean.expressionWrappers['" + operator.getId() +
          "'].linkLabel}");
        linkText.setValueBinding("value", valueBinding);
        linkText.setStyleClass("operator_link");
        subPanelGroup.getChildren().add(linkText);
      }
    }
    if (operator.equals(selectedExpression))
    {
      addExpressionList(context, subPanelGroup);
    }
    else
    {
      id++;
      HtmlCommandButton button = new HtmlCommandButton();
      button.setId("button_" + id);
      MethodBinding method = application.createMethodBinding(
        "#{queryInstanceBean.expressionWrappers['" + operator.getId() +
        "'].selectExpression}", new Class[0]);
      button.setAction(method);
      button.setImage("/common/misc/images/add_expr.png");
      button.setStyleClass("expr_button add");
      ValueBinding value = application.createValueBinding(
        "#{queryInstanceBean.expressionWrappers['" + operator.getId() +
        "'].addButtonRendered}");
      button.setValueBinding("rendered", value);
      subPanelGroup.getChildren().add(button);
    }
  }

  private void addPredicateComponents(Predicate predicate,
    HtmlPanelGroup exprPanelGroup)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Application application = context.getApplication();

    exprPanelGroup.setStyleClass("expression predicate");

    addLabelComponents(predicate.getLabel(),
      "expressions['" + predicate.getId() + "'].parameterValuesMap",
      predicate.getParameters(), exprPanelGroup);

    id++;
    HtmlCommandButton button = new HtmlCommandButton();
    button.setId("button_" + id);
    MethodBinding method = application.createMethodBinding(
      "#{queryInstanceBean.expressionWrappers['" + predicate.getId() +
      "'].removeExpression}", new Class[0]);
    button.setAction(method);
    button.setImage("/common/misc/images/remove_expr.png");
    button.setStyleClass("expr_button remove");
    exprPanelGroup.getChildren().add(button);
  }

  private void addLabelComponents(String label, String expression,
    List<Query.Parameter> parameters, HtmlPanelGroup exprPanelGroup)
  {
    int index1 = 0;
    int index2;
    for (Query.Parameter parameter : parameters)
    {
      String paramName = parameter.getName();
      String tag = "${" + paramName + "}";
      index2 = label.indexOf(tag, index1);
      if (index2 != -1)
      {
        String text = label.substring(index1, index2);
        id++;
        HtmlOutputText outputText = new HtmlOutputText();
        outputText.setId("output_" + id);
        outputText.setValue(text);
        outputText.setStyleClass("part");
        exprPanelGroup.getChildren().add(outputText);
        index1 = index2 + tag.length();
        addParameterComponent(parameter, expression, exprPanelGroup);
      }
    }
    if (index1 < label.length())
    {
      id++;
      String text = label.substring(index1);
      HtmlOutputText outputText = new HtmlOutputText();
      outputText.setId("output_" + id);
      outputText.setValue(text);
      outputText.setStyleClass("part");
      exprPanelGroup.getChildren().add(outputText);
    }
  }

  private void addParameterComponent(Query.Parameter parameter,
    String expression, HtmlPanelGroup exprPanelGroup)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    String sql = parameter.getSql();
    if (StringUtils.isBlank(sql))
    {
      String format = parameter.getFormat();
      ValueBinding valueBinding;
      if (Query.Parameter.DATE.equals(format))
      {
        HtmlCalendar calendar = new HtmlCalendar();
        calendar.setId("input_" + id);
        String expr = "#{queryInstanceBean." + expression +
          "['" + parameter.getName() + "']}";
        valueBinding = context.getApplication().createValueBinding(expr);
        calendar.setValueBinding("value", valueBinding);
        calendar.setStyleClass("input_value date");
        exprPanelGroup.getChildren().add(calendar);
      }
      else
      {
        HtmlInputText inputText = new HtmlInputText();
        inputText.setId("input_" + id);
        String expr = "#{queryInstanceBean." + expression +
          "['" + parameter.getName() + "']}";
        valueBinding = context.getApplication().createValueBinding(expr);
        inputText.setValueBinding("value", valueBinding);
        inputText.setStyleClass("input_value");
        String title = parameter.getDescription() == null ?
          parameter.getName() : parameter.getDescription();
        inputText.setTitle(title);
        inputText.setAlt(title);
        inputText.setSize(parameter.getSize());
        if (Query.Parameter.NUMBER.equals(format))
        {
          Application application = context.getApplication();
          MethodBinding method = application.
            createMethodBinding("#{queryInstanceBean.validateNumber}",
            new Class[]{FacesContext.class, UIComponent.class, Object.class});
          inputText.setValidator(method);
        }
        exprPanelGroup.getChildren().add(inputText);
      }
      Object value = valueBinding.getValue(context);
      if (value == null)
      {
        String defaultValue = parameter.getDefaultValue();
        if (!StringUtils.isBlank(defaultValue))
        {
          valueBinding.setValue(context, String.valueOf(defaultValue));
        }
      }
    }
    else // selectOneMenu
    {
      HtmlSelectOneMenu selectOneMenu = new HtmlSelectOneMenu();
      selectOneMenu.setId("input_" + id);
      String expr = "#{queryInstanceBean." + expression +
        "['" + parameter.getName() + "']}";
      ValueBinding valueBinding =
        context.getApplication().createValueBinding(expr);
      selectOneMenu.setValueBinding("value", valueBinding);
      selectOneMenu.setStyleClass("input_value select");
      id++;
      UISelectItems uiSelectItems = new UISelectItems();
      uiSelectItems.setId("items_" + id);
      expr = "#{queryInstanceBean.parameterSelectItemsMap['" +
        parameter.getName() + "']}";
      ValueBinding itemsValue =
        context.getApplication().createValueBinding(expr);
      uiSelectItems.setValueBinding("value", itemsValue);
      selectOneMenu.getChildren().add(uiSelectItems);
      exprPanelGroup.getChildren().add(selectOneMenu);
    }
  }

  private void addExpressionList(FacesContext context,
    HtmlPanelGroup subPanelGroup)
  {
    id++;
    HtmlDataList dataList = new HtmlDataList();
    dataList.setId("data_list_" + id);
    dataList.setVar("expression");
    dataList.setLayout("unorderedList");
    dataList.setStyleClass("data_list");
    ArrayList list = new ArrayList();
    list.add(OPERATOR_PREFIX + Operator.AND);
    list.add(OPERATOR_PREFIX + Operator.OR);
    list.add(OPERATOR_PREFIX + Operator.NOR);
    List<Query.Predicate> predicates = getQuery().getPredicates();
    for (Query.Predicate predicate : predicates)
    {
      list.add(predicate.getName());
    }
    dataList.setValue(list);
    subPanelGroup.getChildren().add(dataList);

    id++;
    HtmlCommandLink link = new HtmlCommandLink();
    link.setId("expr_link_" + id);
    Application application = context.getApplication();
    MethodBinding method = application.
      createMethodBinding("#{queryInstanceBean.addExpression}", new Class[0]);
    link.setAction(method);
    ValueBinding valueBinding = application.createValueBinding(
      "#{queryInstanceBean.expressionLabel}");
    link.setValueBinding("value", valueBinding);
    link.setStyleClass("expr_option");
    dataList.getChildren().add(link);

    id++;
    HtmlCommandButton cancelButton = new HtmlCommandButton();
    cancelButton.setId("cancel_link_" + id);
    method = context.getApplication().
      createMethodBinding("#{queryInstanceBean.cancel}", new Class[0]);
    cancelButton.setAction(method);
    valueBinding = application.createValueBinding(
      "#{objectBundle.cancel}");
    cancelButton.setValueBinding("value", valueBinding);
    cancelButton.setStyleClass("big_button");
    subPanelGroup.getChildren().add(cancelButton);
  }

  public String getSql()
  {
    return selectedInstance.generateSql();
  }

  public String show()
  {
    return "query_instance";
  }

  public String reloadQuery()
  {
    try
    {
      QueryBean queryBean = (QueryBean)getBean("queryBean");
      queryBean.reloadQuery();
      info("QUERY_RELOAD");
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return "query_instance";
  }
  
  public String saveQuery()
  {
    try
    {
      QueryBean queryBean = (QueryBean)getBean("queryBean");
      queryBean.saveQuery();
      info("QUERY_SAVED");
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return "query_instance";
  }

  public String addInstance()
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
    clearSelectItems();
    scroll = 0;
    instanceSelectItems = null;
    return "query_instance";
  }

  public String removeInstance()
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
      clearSelectItems();
    }
    scroll = 0;
    instanceSelectItems = null;    
    return "query_instance";
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

  public String execute()
  {
    putAutomaticParameters();
    QueryBean queryBean = (QueryBean)getBean("queryBean");
    SqlWebBean sqlWebBean = (SqlWebBean)getBean("sqlWebBean");
    Query query = getQuery();
    Query.Connection connection = query.getConnection();
    sqlWebBean.setDriver(connection.getDriver());
    sqlWebBean.setUrl(connection.getUrl());
    sqlWebBean.setUsername(connection.getUsername());
    sqlWebBean.setPassword(connection.getPassword());
    sqlWebBean.setTitle(query.getTitle());
    sqlWebBean.setSql(selectedInstance.generateSql());
    sqlWebBean.setEditMode(queryBean.isEditionEnabled());
    sqlWebBean.setMaxRows(selectedInstance.getMaxResults());
    sqlWebBean.setAutoExecute(true);
    for (QueryInstance.Output output : selectedInstance.getOutputs())
    {
      String label = output.getOutput().getLabel();
      String description = output.getOutput().getDescription();
      if (description != null && !description.isEmpty())
      {
        sqlWebBean.getColumnDescriptionMap().put(label, description);
      }
    }
    return sqlWebBean.showFullscreen();
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

  private void clearSelectItems()
  {
    availableOutputSelectItems = null;
    selectedOutputSelectItems = null;
    outputsToAdd = null;
    outputsToRemove = null;
  }
}
