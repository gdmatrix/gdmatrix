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
package org.santfeliu.webapp.modules.workflow;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.matrix.workflow.InstanceEvent;
import org.matrix.workflow.VariableChange;
import org.matrix.workflow.WorkflowConstants;
import org.matrix.workflow.WorkflowManagerPort;
import org.matrix.workflow.WorkflowManagerService;
import org.santfeliu.faces.FacesBean;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.Properties;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.workflow.VariableListConverter;
import org.santfeliu.workflow.form.Form;
import org.santfeliu.workflow.form.FormFactory;
import static org.matrix.workflow.WorkflowConstants.*;
import org.primefaces.PF;
import org.primefaces.event.ToggleEvent;
import org.santfeliu.webapp.util.WebUtils;
import static org.santfeliu.workflow.web.InstanceListBean.FAIL_FORM_PROPERTY;
import static org.santfeliu.workflow.web.InstanceListBean.FAIL_MESSAGE_PROPERTY;
import static org.santfeliu.workflow.web.InstanceListBean.TERMINATION_FORM_PROPERTY;
import static org.santfeliu.workflow.web.InstanceListBean.TERMINATION_MESSAGE_PROPERTY;
import static org.santfeliu.workflow.web.InstanceListBean.HELP_BUTTON_URL_PROPERTY;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class WorkflowInstanceBean extends FacesBean implements Serializable
{
  private static final String USERNAME = "username";
  private static final String CREATOR_USERID = "creator_userid";
  private static final String CREATOR_NAME = "creator_name";
  private static final String CREATOR_SURNAME1 = "creator_surname1";
  private static final String CREATOR_SURNAME2 = "creator_surname2";
  private static final String CREATOR_NIF = "creator_nif";
  private static final String CREATOR_CIF = "creator_cif";
  private static final String CREATOR_REPRESENTANT = "creator_representant";
  private static final String CREATOR_EMAIL = "creator_email";
  private static final String CREATOR_ORGANIZATION = "creator_organization";
  private static final String CREATOR_LANGUAGE = "creator_language";
  private static final String ACCESS_TOKEN = "access_token";

  private String instanceId;
  private Map variables;

  private boolean debugModeEnabled;
  private boolean formEnabled;
  private boolean forwardEnabled;
  private boolean backwardEnabled;

  private Form selectedForm;
  private List<Form> pendentForms = new ArrayList<>();
  private int activeDebugTab = 0;
  private String variableAssignment;

  @Inject
  WorkflowInstanceListBean instanceListBean;

  public String getInstanceId()
  {
    return instanceId;
  }

  public void setInstanceId(String instanceId)
  {
    this.instanceId = instanceId;
  }

  public Map getVariables()
  {
    return variables;
  }

  public void setVariables(Map variables)
  {
    this.variables = variables;
  }

  public Map getVariablesUnmodificable()
  {
    return variables;
  }

  public void setVariablesUnmodificable(Map variables)
  {
  }

  public void setSelectedForm(Form selectedForm)
  {
    this.selectedForm = selectedForm;
  }

  public Form getSelectedForm()
  {
    return selectedForm;
  }

  public boolean isFormSelected()
  {
    if (selectedForm == null) return false;
    Form form = (Form)getRequestMap().get("form");
    return selectedForm.equals(form);
  }

  public void setPendentForms(List<Form> pendentForms)
  {
    this.pendentForms = pendentForms;
  }

  public List<Form> getPendentForms()
  {
    return pendentForms;
  }

  public boolean isShowPendentForms()
  {
    if (pendentForms == null) return false;
    return pendentForms.size() > 1;
  }

  public String getVariableStyleClass()
  {
    String type = "T";
    Map.Entry entry = (Map.Entry)getRequestMap().get("instanceVar");
    if (entry != null)
    {
      String variableName = String.valueOf(entry.getKey());
      Object value = entry.getValue();
      if (value instanceof String) type = "T";
      if (value instanceof Number) type = "N";
      if (value instanceof Boolean) type = "B";
      return getVariableStyleClass(variableName, type);
    }
    return null;
  }

  public String getEventVariableStyleClass()
  {
    VariableChange variableChange =
      (VariableChange)getRequestMap().get("variableChange");
    if (variableChange != null)
    {
      String variableName = variableChange.getName();
      String type = variableChange.getType();
      return getVariableStyleClass(variableName, type);
    }
    return null;
  }

  public String getVariableStyleClass(String variableName, String type)
  {
    String nameStyleClass = "normalVar";
    if (variableName.startsWith(WorkflowConstants.FORM_PREFIX))
    {
      nameStyleClass = "formVar";
    }
    else if (variableName.startsWith(WorkflowConstants.ERROR_PREFIX) ||
      variableName.equals(WorkflowConstants.ERRORS))
    {
      nameStyleClass = "errorVar";
    }
    else if (variableName.startsWith(WorkflowConstants.READ_ACCESS_PREFIX) ||
      variableName.startsWith(WorkflowConstants.WRITE_ACCESS_PREFIX) ||
      variableName.startsWith(WorkflowConstants.UPDATE_ONLY_PREFIX))
    {
      nameStyleClass = "accessVar";
    }

    String typeStyleClass = "text_type";
    if ("N".equals(type)) typeStyleClass = "number_type";
    if ("B".equals(type)) typeStyleClass = "boolean_type";

    return nameStyleClass + " " + typeStyleClass;
  }

  public int getActiveDebugTab()
  {
    return activeDebugTab;
  }

  public void setActiveDebugTab(int activeDebugTab)
  {
    this.activeDebugTab = activeDebugTab;
  }

  public String getVariableAssignment()
  {
    return variableAssignment;
  }

  public void setVariableAssignment(String variableAssignment)
  {
    this.variableAssignment = variableAssignment;
  }

  public String getWorkflowName()
  {
    return (String)variables.get(WorkflowConstants.WORKFLOW_NAME);
  }

  public String getInstanceDescription()
  {
    return (String)variables.get(WorkflowConstants.DESCRIPTION);
  }

  public String getInstanceUserState()
  {
    return (String)variables.get(WorkflowConstants.STATE);
  }

  public String getInstanceStartDate()
  {
    String startDateTime =
      (String)variables.get(WorkflowConstants.START_DATE_TIME);
    if (startDateTime == null) return null;
    Date date = TextUtils.parseInternalDate(startDateTime);
    return TextUtils.formatDate(date, "dd/MM/yyyy");
  }

  public double getRandom()
  {
    return Math.random();
  }

  public void setForwardEnabled(boolean forwardEnabled)
  {
    this.forwardEnabled = forwardEnabled;
  }

  public boolean isForwardEnabled()
  {
    return forwardEnabled;
  }

  public void setBackwardEnabled(boolean backwardEnabled)
  {
    this.backwardEnabled = backwardEnabled;
  }

  public boolean isBackwardEnabled()
  {
    return backwardEnabled;
  }

  public boolean isFormEnabled()
  {
    return formEnabled;
  }

  public void setFormEnabled(boolean formEnabled)
  {
    this.formEnabled = formEnabled;
  }

  public boolean isExitEnabled()
  {
    boolean exitEnabled = true; // true by default
    if (variables != null)
    {
      Object value = variables.get(WorkflowConstants.EXIT_BUTTON_ENABLED);
      if (value instanceof Boolean)
      {
        exitEnabled = ((Boolean)value);
      }
    }
    return exitEnabled;
  }

  public boolean isDestroyEnabled()
  {
    boolean destroyEnabled = false; // false by default
    if (variables != null)
    {
      Object value = variables.get(WorkflowConstants.DESTROY_BUTTON_ENABLED);
      if (value instanceof Boolean)
      {
        destroyEnabled = ((Boolean)value);
      }
    }
    return destroyEnabled;
  }

  public boolean isRecoverEnabled()
  {
    return this.isErrorDetected();
  }

  public String getFormRenderers()
  {
    if (variables != null)
    {
      Object value = variables.get(WorkflowConstants.FORM_RENDERERS);
      if (value instanceof String)
      {
        return (String)value;
      }
    }
    return "HtmlFormRenderer,GenericFormRenderer";
  }

  public boolean isHelpEnabled()
  {
    return getHelpUrl() != null;
  }

  public String getHelpUrl()
  {
    String url = null;
    if (variables != null)
    {
      Object value = variables.get(HELP_BUTTON_URL);
      if (value instanceof String)
      {
        url = (String)value;
      }
    }
    if (url == null)
    {
      url = getNodeProperty(HELP_BUTTON_URL_PROPERTY);
    }
    return url;
  }

  public boolean isAnyFooterButtonEnabled()
  {
    return isWorkflowAdmin() || isHelpEnabled() || isDestroyEnabled() ||
       (formEnabled && (forwardEnabled || backwardEnabled));
  }

  public boolean isHeaderFormEnabled()
  {
    return variables.containsKey(WorkflowConstants.HEADER_FORM);
  }

  public org.santfeliu.form.Form getHeaderForm()
  {
    Object value = variables.get(WorkflowConstants.HEADER_FORM);
    if (value instanceof String)
    {
      String selector = (String)value;
      try
      {
        org.santfeliu.form.FormFactory formFactory =
          org.santfeliu.form.FormFactory.getInstance();
        if (getFacesContext().getRenderResponse())
        {
          formFactory.clearForm(selector);
        }
        return formFactory.getForm(selector, variables, true);
      }
      catch (Exception ex)
      {
      }
    }
    return null;
  }

  public boolean isTranslationEnabled()
  {
    boolean translationEnabled = false; // false by default
    if (variables != null)
    {
      Object value = variables.get(TRANSLATION_ENABLED);
      if (value instanceof Boolean)
      {
        translationEnabled = ((Boolean)value);
      }
    }
    return translationEnabled;
  }

  public boolean isDebugModeEnabled()
  {
    return debugModeEnabled;
  }

  public void setDebugModeEnabled(boolean enabled)
  {
    this.debugModeEnabled = enabled;
  }

  public void onToogleDebug(ToggleEvent event)
  {
    this.debugModeEnabled = !debugModeEnabled;
  }

  public boolean isTerminated()
  {
    if (variables == null) return false;
    return variables.get(ACTIVE_NODES) == null;
  }

  public boolean isErrorDetected()
  {
    if (variables == null) return false;
    return variables.get(ERRORS) != null;
  }

  public String getFinalFormSelector()
  {
    String selector = null;

    if (variables != null)
    {
      if (variables.get(ERRORS) == null) // OK
      {
        Object value = variables.get(TERMINATION_FORM);
        if (value instanceof String)
        {
          selector = (String)value;
        }
        else if (!variables.containsKey(TERMINATION_MESSAGE))
        {
          selector = getNodeProperty(TERMINATION_FORM_PROPERTY);
        }
      }
      else // end with errors
      {
        Object value = variables.get(FAIL_FORM);
        if (value instanceof String)
        {
          selector = (String)value;
        }
        else if (!variables.containsKey(FAIL_MESSAGE))
        {
          selector = getNodeProperty(FAIL_FORM_PROPERTY);
        }
      }
    }
    return selector;
  }

  public org.santfeliu.form.Form getFinalForm()
  {
    String selector = getFinalFormSelector();
    if (selector == null) return null;

    try
    {
      // in post back always use cache
      org.santfeliu.form.FormFactory formFactory =
        org.santfeliu.form.FormFactory.getInstance();

      if (getFacesContext().getRenderResponse())
      {
        formFactory.clearForm(selector);
      }
      return formFactory.getForm(selector, variables);
    }
    catch (Exception ex)
    {
      error(ex);
      return null;
    }
  }

  public String getFinalMessage()
  {
    String message = null;

    if (variables != null)
    {
      if (variables.get(ERRORS) == null) // OK
      {
        Object value = variables.get(TERMINATION_MESSAGE);
        if (value instanceof String)
        {
          message = (String)value;
        }
        if (message == null) // get default message
        {
          message = getNodeProperty(TERMINATION_MESSAGE_PROPERTY);
          if (message == null)
          {
            Locale locale = getLocale();
            ResourceBundle bundle = ResourceBundle.getBundle(
              "org.santfeliu.workflow.web.resources.WorkflowBundle", locale);
            message = bundle.getString("instanceTerminated");
          }
        }
      }
      else // ERROR
      {
        Object value = variables.get(FAIL_MESSAGE);
        if (value instanceof String)
        {
          message = (String)value;
        }
        if (message == null) // get default message
        {
          message = getNodeProperty(FAIL_MESSAGE_PROPERTY);
          if (message == null)
          {
            Locale locale = getLocale();
            ResourceBundle bundle = ResourceBundle.getBundle(
              "org.santfeliu.workflow.web.resources.WorkflowBundle", locale);
            message = bundle.getString("instanceTerminatedWithErrors");
          }
        }
      }
    }
    return message;
  }

  public String getFinalIcon()
  {
    String url = null;

    if (variables != null)
    {
      if (variables.get(ERRORS) == null) // OK
      {
        Object value = variables.get(TERMINATION_ICON);
        if (value instanceof String)
        {
          url = (String)value;
        }
        else
        {
          url = "/common/workflow/images/ok.gif";
        }
      }
      else // FAIL
      {
        Object value = variables.get(FAIL_ICON);
        if (value instanceof String)
        {
          url = (String)value;
        }
        else
        {
          url = "/common/workflow/images/warning.gif";
        }
      }
    }
    return url;
  }

  public boolean isWorkflowAdmin()
  {
    return UserSessionBean.getCurrentInstance()
      .isUserInRole(WORKFLOW_ADMIN_ROLE);
  }

  public Object[] getVariablesArray()
  {
    if (variables == null) return null;

    TreeMap map = new TreeMap();
    Set set = variables.entrySet();
    Iterator iter = set.iterator();
    while (iter.hasNext())
    {
      Map.Entry entry = (Map.Entry)iter.next();
      String variable = String.valueOf(entry.getKey());
      Object value = entry.getValue();
      map.put(variable, value);
    }
    return map.entrySet().toArray();
  }

  //************** action methods ****************

  public void exitInstance()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuModel menuModel = userSessionBean.getMenuModel();
    MenuItemCursor selectedMenuItem = menuModel.getSelectedMenuItem();
    String mid;
    if (userSessionBean.isAnonymousUser())
    {
      mid = selectedMenuItem.getDirectProperty(
        WorkflowInstanceListBean.EXIT_MID_ANONYMOUS_PROPERTY);
      if (mid == null)
      {
        mid = selectedMenuItem.getParent().getMid();
      }
    }
    else
    {
      mid = selectedMenuItem.getDirectProperty(
        WorkflowInstanceListBean.EXIT_MID_PROPERTY);
      if (mid == null)
      {
        mid = menuModel.getSelectedMenuItem().getMid();
      }
    }
    menuModel.setSelectedMid(mid);
    userSessionBean.executeSelectedMenuItem();
  }

  public void destroyInstance()
  {
    try
    {
      WorkflowManagerPort port = getWorkflowManagerPort();
      port.destroyInstance(instanceId);
      instanceListBean.findInstances();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void clearForm()
  {
    selectedForm = null;
  }

  public void forward()
  {
    try
    {
      Map formVariables = null;
      if (selectedForm != null)
      {
        WorkflowBean workflowBean = this.getSelectedWorkflowBean();
        formVariables = workflowBean.submit();
        if (formVariables != null)
        {
          formVariables.put(selectedForm.getVariable(), FORWARD_STATE);
        }
      }
      WorkflowManagerPort port = getWorkflowManagerPort();
      this.variables = VariableListConverter.toMap(
        port.processInstance(instanceId,
        VariableListConverter.toList(formVariables), true));
      loadPendentForms();
      updateForm();
    }
    catch (Exception ex)
    {
      processException(ex);
    }
  }

  public void backward()
  {
    try
    {
      Map formVariables = null;
      if (selectedForm != null)
      {
        formVariables = new HashMap();
        formVariables.put(selectedForm.getVariable(), BACKWARD_STATE);
      }
      WorkflowManagerPort port = getWorkflowManagerPort();
      this.variables = VariableListConverter.toMap(
        port.processInstance(instanceId,
        VariableListConverter.toList(formVariables), true));
      loadPendentForms();
      updateForm();
    }
    catch (Exception ex)
    {
      processException(ex);
    }
  }

  public void selectForm()
  {
    selectedForm = (Form)getRequestMap().get("form");
    updateForm();
  }

  public void updateForm()
  {
    updateForm(false);
  }

  public void updateForm(boolean stayInInstance)
  {
    formEnabled = true;
    if (variables.get(ACTIVE_NODES) == null)
    {
      // look for return
      if (!stayInInstance)
      {
        String invokerInstanceId = (String)variables.get(INVOKER_INSTANCE_ID);
        if (invokerInstanceId != null && !isErrorDetected())
        {
          setInstanceId(invokerInstanceId);
          forward();
          return;
        }
      }
      // instance terminated
      forwardEnabled = false;
      backwardEnabled = false;
      instanceListBean.setContent("/pages/workflow/instance_terminated.xhtml");
      return;
    }
    else
    {
      if (selectedForm != null)
      {
        formEnabled = isFormEnabled(selectedForm);
        forwardEnabled = selectedForm.isForwardEnabled();
        backwardEnabled = selectedForm.isBackwardEnabled();

        WorkflowBean workflowBean = getSelectedWorkflowBean();
        if (workflowBean != null)
        {
          String error = selectedForm.getError();
          if (error.length() > 0) // show error
          {
            String trnError =
              UserSessionBean.getCurrentInstance().translate(error);
            error("INVALID_INPUT_DATA", new Object[]{trnError});
          }
          instanceListBean.setContent(workflowBean.show(selectedForm));
          return;
        }
        else
        {
          warn("form '" + selectedForm.getType() + "' not found.");
        }
      }
      else // there is no Form, look for invocation
      {
        if (!stayInInstance)
        {
          String invokedInstanceId = findInvokedInstance();
          if (invokedInstanceId != null)
          {
            setInstanceId(invokedInstanceId);
            forward();
            return;
          }
        }
      }
    }
    // nothing to do
    forwardEnabled = false;
    backwardEnabled = false;

    instanceListBean.setContent("/pages/workflow/nothing_to_do.xhtml");
  }

  public void recover()
  {
    try
    {
      Object errorsValue = variables.get(ERRORS);
      if (errorsValue instanceof String)
      {
        Map changes = new HashMap();

        Set<String> activeNodesSet = new HashSet<>();

        // load active nodes in a Set
        Object activeNodesValue = variables.get(ACTIVE_NODES);
        if (activeNodesValue != null)
        {
          String activeNodes = String.valueOf(activeNodesValue);
          for (String activeNode : activeNodes.split(" "))
          {
            activeNodesSet.add(activeNode);
          }
        }

        // add the error nodes to the Set
        String errors = String.valueOf(errorsValue);
        for (String errorNode : errors.split(" "))
        {
          changes.put(ERROR_PREFIX + errorNode, null);
          activeNodesSet.add(errorNode);
        }

        String activeNodes = " " + String.join(" ", activeNodesSet) + " ";
        changes.put(ACTIVE_NODES, activeNodes);
        changes.put(ERRORS, null);

        WorkflowManagerPort port = getWorkflowManagerPort();
        this.variables = VariableListConverter.toMap(
          port.processInstance(instanceId,
          VariableListConverter.toList(changes), true));
        loadPendentForms();
        updateForm();
      }
    }
    catch (Exception ex)
    {
      processException(ex);
    }
  }

  public void doStep()
  {
    try
    {
      WorkflowManagerPort port = getWorkflowManagerPort();
      port.doStep(instanceId);
      this.variables =
        VariableListConverter.toMap(port.getVariables(instanceId));
      loadPendentForms();
      updateForm(true);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void undoStep()
  {
    try
    {
      WorkflowManagerPort port = getWorkflowManagerPort();
      port.undoStep(instanceId);
      this.variables =
        VariableListConverter.toMap(port.getVariables(instanceId));
      loadPendentForms();
      updateForm(true);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void updateInstance()
  {
    updateInstance(false);
  }

  public void updateInstance(boolean debug)
  {
    try
    {
      WorkflowManagerPort port = getWorkflowManagerPort();
      this.variables =
        VariableListConverter.toMap(port.getVariables(instanceId));
      activeDebugTab = 0;
      loadPendentForms();
      updateForm(debug);
    }
    catch (Exception ex)
    {
      if (debug)
      {
        error(ex);
      }
      else
      {
        processException(ex);
      }
    }
  }

  public void setVariables()
  {
    try
    {
      Properties newVariables = new Properties();
      newVariables.loadFromString(variableAssignment);

      WorkflowManagerPort port = getWorkflowManagerPort();
      port.setVariables(instanceId,
        VariableListConverter.toList(newVariables));
      this.variables =
        VariableListConverter.toMap(port.getVariables(instanceId));
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void showInstance(String instanceId)
  {
    if (this.instanceId.equals(instanceId)) return;

    this.instanceId = instanceId;

    updateInstance(true);
  }

  public boolean isInstanceIdVariable(String name)
  {
    return name.equals(INSTANCE_ID)
      || name.equals(INVOKER_INSTANCE_ID)
      || name.startsWith(INVOCATION_PREFIX);
  }

  public List<StackItem> getStackItems()
  {
    List<StackItem> stackItems = new ArrayList<>();

    try
    {
      Map currentVariables = this.variables;
      WorkflowManagerPort port = getWorkflowManagerPort();

      while (currentVariables != null)
      {
        StackItem stackItem = new StackItem();
        stackItem.setWorkflowName((String)currentVariables.get(WORKFLOW_NAME));
        stackItem.setInstanceId((String)currentVariables.get(INSTANCE_ID));
        stackItem.setDescription((String)currentVariables.get(DESCRIPTION));
        stackItem.setActiveNodes((String)currentVariables.get(ACTIVE_NODES));
        stackItem.setInvokerNodeId((String)currentVariables.get(INVOKER_NODE_ID));
        stackItem.setErrors((String)currentVariables.get(ERRORS));
        stackItems.add(stackItem);

        String invokerInstanceId = (String)currentVariables.get(INVOKER_INSTANCE_ID);
        if (invokerInstanceId != null)
        {
          currentVariables =
            VariableListConverter.toMap(port.getVariables(invokerInstanceId));
        }
        else
        {
          currentVariables = null;
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return stackItems;
  }

  public List<InstanceEvent> getInstanceEvents()
  {
    try
    {
      WorkflowManagerPort port = getWorkflowManagerPort();
      List<InstanceEvent> instanceEvents = port.getInstanceEvents(instanceId);
      Collections.reverse(instanceEvents);
      return instanceEvents;
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  // ************** special actions ******************

  public void createInstance(String workflowName, String description)
    throws Exception
  {
    createInstance(workflowName, description, false, null);
  }

  public void createInstance(String workflowName,
    String description, boolean simulation) throws Exception
  {
    createInstance(workflowName, description, simulation, null);
  }

  public void createInstance(String workflowName,
    String description, boolean simulation, Map parameters) throws Exception
  {
    this.selectedForm = null;

    WorkflowManagerPort port = getWorkflowManagerPort();

    if (parameters == null) parameters = new HashMap();

    // add creator variables
    addCreatorVariables(parameters);

    // set description
    if (description != null)
      parameters.put(DESCRIPTION, description);

    // set simulation variable
    parameters.put(SIMULATION, simulation);

    // assign any agent
    parameters.put(AGENT_NAME, ANY_AGENT);

    // create instance
    instanceId = port.createInstance(workflowName,
      VariableListConverter.toList(parameters));
    forward();
  }

  public String getAccessToken()
  {
    try
    {
      WorkflowManagerPort port = getWorkflowManagerPort(true);
      Map vars = VariableListConverter.toMap(port.getVariables(instanceId));
      return (String)vars.get(ACCESS_TOKEN);
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  public void setAccessToken(String accessToken)
  {
    try
    {
      WorkflowManagerPort port = getWorkflowManagerPort(true);
      Map vars = new HashMap();
      vars.put(ACCESS_TOKEN, accessToken);
      port.processInstance(instanceId,
        VariableListConverter.toList(vars), false);
    }
    catch (Exception ex)
    {
    }
  }

  /*
     Logs the current user into a started instance and forwards the given form.
     That instance is showing the form specified by formName.
  */
  public void login(String formName) throws Exception
  {
    String adminUserId =
      MatrixConfig.getProperty("adminCredentials.userId");
    String adminPassword =
      MatrixConfig.getProperty("adminCredentials.password");

    WSDirectory wsDirectory = WSDirectory.getInstance();
    WSEndpoint endpoint =
      wsDirectory.getEndpoint(WorkflowManagerService.class);
    WorkflowManagerPort port =
      endpoint.getPort(WorkflowManagerPort.class, adminUserId, adminPassword);

    HashMap newVariables = new HashMap();
    addCreatorVariables(newVariables);

    newVariables.put(formName, FORWARD_STATE);
    port.processInstance(instanceId,
      VariableListConverter.toList(newVariables), false);
  }

  //********* private methods **********

  private WorkflowBean getSelectedWorkflowBean()
  {
    Object bean = WebUtils.getBean(selectedForm.getType() + "WorkflowBean");

    return (bean instanceof WorkflowBean) ? (WorkflowBean)bean : null;
  }

  private void processException(Exception ex)
  {
    if (ex.getMessage().contains("Can't set variable"))
    {
      error("SET_VARIABLES_FAILED", new Object[]{ex.toString()});
      instanceListBean.setContent("/pages/worlflow/error_detected.xhtml");
    }
    else
    {
      error(ex);
    }
  }

  private void loadPendentForms()
  {
    String group = null;
    if (selectedForm != null)
    {
      group = selectedForm.getGroup();
    }
    selectedForm = null;

    pendentForms.clear();
    Set set = variables.entrySet();
    Iterator iter = set.iterator();
    while (iter.hasNext())
    {
      Map.Entry entry = (Map.Entry)iter.next();
      String variable = (String)entry.getKey();
      if (variable.startsWith(FORM_PREFIX))
      {
        Object value = entry.getValue();
        if (value instanceof String)
        {
          String formValue = (String)value;

          Form form = FormFactory.parse(formValue);
          form.setVariable(variable);
          if (SHOW_STATE.equals(form.getState()))
          {
            pendentForms.add(form);
            if (form.getGroup().equals(group))
            {
              selectedForm = form;
            }
          }
        }
      }
    }
    Collections.sort(pendentForms, Form.getComparator());
    if (selectedForm == null && pendentForms.size() > 0)
    {
      selectedForm = pendentForms.get(0);
    }
  }

  private boolean isFormEnabled(Form selectedForm)
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    if (userSessionBean.isUserInRole(WORKFLOW_ADMIN_ROLE))
      return true;

    Set roles = userSessionBean.getRoles();

    String formVariable = selectedForm.getVariable();
    boolean editable = false;
    Iterator iter = variables.keySet().iterator();
    while (iter.hasNext() && !editable)
    {
      String variable = (String)iter.next();
      if (variable.startsWith(WRITE_ACCESS_PREFIX))
      {
        String value = (String)variables.get(variable);
        String formVariablePattern = value.substring(20).trim();
        if (formVariable.matches(formVariablePattern.replace("%", ".*")))
        {
          String role = value.substring(0, 20).trim();
          editable = roles.contains(role);
        }
      }
    }
    return editable;
  }

  private String findInvokedInstance()
  {
    String invokedInstanceId = null;
    Set set = variables.entrySet();
    Iterator iter = set.iterator();
    while (iter.hasNext() && invokedInstanceId == null)
    {
      Map.Entry entry = (Map.Entry)iter.next();
      String variable = (String)entry.getKey();
      if (variable.startsWith(INVOCATION_PREFIX))
      {
        Object value = entry.getValue();
        invokedInstanceId = value.toString();
      }
    }
    return invokedInstanceId;
  }

  private void addCreatorVariables(Map map)
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    String userId = userSessionBean.getUsername();

    map.put(USERNAME, userId);
    map.put(CREATOR_USERID, userId);
    map.put(CREATOR_NAME, userSessionBean.getGivenName());

    String surname = userSessionBean.getSurname();
    if (surname != null)
    {
      int idx = surname.trim().indexOf(" ");
      if (idx != -1)
      {
        map.put(CREATOR_SURNAME1, surname.substring(0, idx));
        map.put(CREATOR_SURNAME2,
          surname.substring(idx + 1, surname.length()));
      }
      else
      {
        map.put(CREATOR_SURNAME1, surname);
      }
    }
    map.put(CREATOR_NIF, userSessionBean.getNIF());
    map.put(CREATOR_CIF, userSessionBean.getCIF());
    map.put(CREATOR_REPRESENTANT, userSessionBean.isRepresentant());
    map.put(CREATOR_ORGANIZATION, userSessionBean.getOrganizationName());
    map.put(CREATOR_EMAIL, userSessionBean.getEmail());
    map.put(CREATOR_LANGUAGE, FacesUtils.getViewLanguage());
  }

  private WorkflowManagerPort getWorkflowManagerPort()
    throws Exception
  {
    return getWorkflowManagerPort(false);
  }

  private WorkflowManagerPort getWorkflowManagerPort(boolean asAdmin)
    throws Exception
  {
    WSDirectory dir = WSDirectory.getInstance();
    WSEndpoint endpoint = dir.getEndpoint(WorkflowManagerService.class);
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    String userId;
    String password;
    if (asAdmin)
    {
      userId = MatrixConfig.getProperty("adminCredentials.userId");
      password = MatrixConfig.getProperty("adminCredentials.password");
    }
    else
    {
      userId = userSessionBean.getUsername();
      password = userSessionBean.getPassword();
    }
    return endpoint.getPort(WorkflowManagerPort.class, userId, password);
  }

  private String getNodeProperty(String propertyName)
  {
    MenuModel menuModel = UserSessionBean.getCurrentInstance().getMenuModel();
    MenuItemCursor selectedMenuItem = menuModel.getSelectedMenuItem();

    return selectedMenuItem.getProperty(propertyName);
  }
}
