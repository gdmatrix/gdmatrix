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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.enterprise.context.RequestScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.matrix.workflow.InstanceFilter;
import org.matrix.workflow.InstanceView;
import org.matrix.workflow.VariableFilter;
import org.matrix.workflow.WorkflowConstants;
import org.matrix.workflow.WorkflowManagerPort;
import org.matrix.workflow.WorkflowManagerService;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.bean.CMSProperty;
import static org.matrix.workflow.WorkflowConstants.FAIL_MESSAGE;
import static org.matrix.workflow.WorkflowConstants.TERMINATION_FORM;
import static org.matrix.workflow.WorkflowConstants.TERMINATION_MESSAGE;
import static org.matrix.workflow.WorkflowConstants.FAIL_FORM;
import static org.matrix.workflow.WorkflowConstants.HELP_BUTTON_URL;
import org.santfeliu.web.ApplicationBean;

/**
 *
 * @author realor
 */
@CMSManagedBean
@Named
@RequestScoped
public class WorkflowInstanceListBean extends WebBean implements Serializable
{
  @CMSProperty
  public static final String SHOW_CATALOGUE_MID_PROPERTY = "showCatalogueMid";
  @CMSProperty
  public static final String EXIT_MID_PROPERTY = "exitMid";
  @CMSProperty
  public static final String EXIT_MID_ANONYMOUS_PROPERTY = "exitMidA";
  @CMSProperty
  public static final String FILTER_DAYS_PROPERTY = "filterDays";
  @CMSProperty
  public static final String TERMINATION_MESSAGE_PROPERTY =
    "workflow." + TERMINATION_MESSAGE;
  @CMSProperty
  public static final String FAIL_MESSAGE_PROPERTY =
    "workflow." + FAIL_MESSAGE;
  @CMSProperty
  public static final String TERMINATION_FORM_PROPERTY =
    "workflow." + TERMINATION_FORM;
  @CMSProperty
  public static final String FAIL_FORM_PROPERTY =
    "workflow." + FAIL_FORM;
  @CMSProperty
  public static final String HELP_BUTTON_URL_PROPERTY =
    "workflow." + HELP_BUTTON_URL;

  public static final String WORKFLOW_PARAMETER = "workflow";
  public static final String INSTANCEID_PARAMETER = "instanceid";
  public static final String ACCESS_TOKEN_PARAMETER = "access_token";
  public static final String LOGIN_ACCESS_TOKEN_SEPARATOR = "$";

  static final int VARIABLE_COUNT = 3;
  static final int DAYS = 14;

  private String state;
  private VariableFilter[] variables;
  private String startDate;
  private String endDate;

  private String instanceId;
  private String workflowName;
  private List<InstanceView> instanceList;
  private int firstRow;

  @Inject
  WorkflowInstanceBean instanceBean;

  private String content;

  public WorkflowInstanceListBean()
  {
    // setup inital filter parameters
    state = "S";
    startDate = getDefaultStartDate();
    variables = new VariableFilter[VARIABLE_COUNT];
    for (int i = 0; i < VARIABLE_COUNT; i++)
    {
      variables[i] = new VariableFilter();
    }
  }

  public void setInstanceId(String instanceId)
  {
    this.instanceId = instanceId;
  }

  public String getInstanceId()
  {
    return instanceId;
  }

  public void setWorkflowName(String workflowName)
  {
    this.workflowName = workflowName;
  }

  public String getWorkflowName()
  {
    return workflowName;
  }

  public int getFirstRow()
  {
    return firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    this.firstRow = firstRow;
  }

  public String getState()
  {
    return state;
  }

  public void setState(String state)
  {
    this.state = state;
  }

  public void setVariables(VariableFilter[] variables)
  {
    this.variables = variables;
  }

  public VariableFilter[] getVariables()
  {
    return variables;
  }

  public String getStartDate()
  {
    return startDate;
  }

  public void setStartDate(String startDate)
  {
    this.startDate = startDate;
  }

  public String getEndDate()
  {
    return endDate;
  }

  public void setEndDate(String endDate)
  {
    this.endDate = endDate;
  }

  public List<SelectItem> getAllSelectItems()
  {
    List<SelectItem> items = new ArrayList();
    if (isWorkflowAdmin())
    {
      ResourceBundle bundle = ResourceBundle.getBundle(
        "org.santfeliu.workflow.web.resources.WorkflowBundle", getLocale());
      items.add(new SelectItem("A", bundle.getString("allProceduresState")));
    }
    return items;
  }

  public void setInstanceList(List<InstanceView> instanceList)
  {
    this.instanceList = instanceList;
  }

  public List<InstanceView> getInstanceList()
  {
    return instanceList;
  }

  public int getInstanceCount()
  {
    return instanceList == null ? 0 : instanceList.size();
  }

  public String getInstanceStartDate()
  {
    InstanceView instanceView = (InstanceView)getRequestMap().get("instance");
    String startDateTime = instanceView.getStartDateTime();
    if (startDateTime == null) return null;
    Date date = TextUtils.parseInternalDate(startDateTime);
    return TextUtils.formatDate(date, "dd/MM/yyyy");
  }

  public String getInstanceInternalState()
  {
    InstanceView instanceView = (InstanceView)getRequestMap().get("instance");
    String workflowName = instanceView.getName();
    String activeNodes = instanceView.getActiveNodes();
    if (activeNodes == null) activeNodes = "";
    return workflowName + " {" + activeNodes + "}";
  }

  public boolean isWorkflowAdmin()
  {
    return UserSessionBean.getCurrentInstance().
      isUserInRole(WorkflowConstants.WORKFLOW_ADMIN_ROLE);
  }

  // **** actions ****
  @CMSAction
  public String main()
  {
    try
    {
      Map parameters = new HashMap();
      parameters.putAll(getExternalContext().getRequestParameterMap());
      String extWorkflowName = (String)parameters.remove(WORKFLOW_PARAMETER);
      String extInstanceId = (String)parameters.remove(INSTANCEID_PARAMETER);
      if (extWorkflowName != null)
      {
        // create new instance passing parameters as variables
        instanceBean.createInstance(extWorkflowName,
          null, false, parameters);
      }
      else if (extInstanceId != null)
      {
        // enter existing instance
        String outcome;
        instanceBean.setInstanceId(extInstanceId);
        String accessToken = instanceBean.getAccessToken();
        if (accessToken != null)
        {
          // check workflow accessToken
          if (!accessToken.equals(parameters.remove(ACCESS_TOKEN_PARAMETER)))
            throw new Exception("INVALID_INSTANCE_TOKEN");

          // special login case <token>$<form_variable>
          int index = accessToken.indexOf(LOGIN_ACCESS_TOKEN_SEPARATOR);
          if (index != -1)
          {
            String formVariable = accessToken.substring(index + 1);
            instanceBean.login(formVariable);
          }
          instanceBean.forward();
        }
        else if (!UserSessionBean.getCurrentInstance().isAnonymousUser())
        {
          // allow access if user is logged in, for backward compatibility
          instanceBean.forward();
        }
        else // anonymous users can not access an instance without access token
        {
          // show empty instance list instead
          setContent("/pages/workflow/instance_list.xhtml");
        }
      }
      else
      {
        findInstances();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }

    String template = UserSessionBean.getCurrentInstance().getTemplate();
    return "/templates/" + template + "/template.xhtml";
  }

  @CMSAction
  public void findInstances()
  {
    try
    {
      loadInstanceList();
      setContent("/pages/workflow/instance_list.xhtml");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public String getContent()
  {
    return content;
  }

  public void setContent(String content)
  {
    this.content = content;
  }

  public String translate(String text)
  {
    ApplicationBean applicationBean = ApplicationBean.getCurrentInstance();
    return applicationBean.translate(text, "wf:instanceList");
  }

  public void simulate()
  {
    try
    {
      instanceBean.createInstance(workflowName, null, true);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void transact()
  {
    try
    {
      instanceBean.createInstance(workflowName, null, false);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void forward()
  {
    InstanceView instanceView = (InstanceView)getRequestMap().get("instance");
    instanceBean.setInstanceId(instanceView.getInstanceId());
    instanceBean.clearForm();
    instanceBean.forward();
  }

  public void destroyInstance()
  {
    InstanceView instanceView = (InstanceView)getRequestMap().get("instance");
    instanceBean.setInstanceId(instanceView.getInstanceId());
    instanceBean.destroyInstance();
  }

  public void showInstanceById()
  {
    instanceBean.setInstanceId(instanceId);
    instanceBean.updateInstance();
  }

  public void debugInstanceById()
  {
    instanceBean.setInstanceId(instanceId);
    instanceBean.setDebugModeEnabled(true);
    instanceBean.updateInstance(true);
  }

  // private methods

  private WorkflowManagerPort getWorkflowManagerPort()
    throws Exception
  {
    WSDirectory dir = WSDirectory.getInstance();
    WSEndpoint endpoint = dir.getEndpoint(WorkflowManagerService.class);
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    String userId = userSessionBean.getUsername();
    String password = userSessionBean.getPassword();
    return endpoint.getPort(WorkflowManagerPort.class, userId, password);
  }

  private void loadInstanceList() throws Exception
  {
    instanceList = null;
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    if (!userSessionBean.isAnonymousUser())
    {
      WorkflowManagerPort port = getWorkflowManagerPort();
      InstanceFilter filter = new InstanceFilter();
      filter.setStartDate(startDate);
      filter.setEndDate(endDate);
      filter.setFirstResult(0);
      filter.setMaxResults(100);

      if ("P".equals(state))
      {
        VariableFilter variable = new VariableFilter();
        variable.setName(WorkflowConstants.FORM_PREFIX + "%");
        variable.setValue("%");
        variable.setExtendedVisibility(Boolean.FALSE);
        filter.getVariable().add(variable);

        variable = new VariableFilter();
        variable.setName(WorkflowConstants.ACTIVE_NODES);
        variable.setValue("%");
        variable.setExtendedVisibility(Boolean.TRUE);
        filter.getVariable().add(variable);
      }
      else if ("S".equals(state))
      {
        VariableFilter variable = new VariableFilter();
        variable.setName("creator_userid");
        variable.setValue(userSessionBean.getUserId());
        variable.setExtendedVisibility(Boolean.TRUE);
        filter.getVariable().add(variable);
      }
      for (int i = 0; i < VARIABLE_COUNT; i++)
      {
        VariableFilter variable = variables[i];
        String name = variable.getName();
        String value = variable.getValue();
        if (name != null && name.length() > 0 &&
            value != null && value.length() > 0)
        {
          variable.setExtendedVisibility(Boolean.TRUE);
          filter.getVariable().add(variable);
        }
      }
      instanceList = port.findInstances(filter);
    }
  }

  private String getDefaultStartDate()
  {
    long millisPerDay = 24L * 3600L * 1000L;
    long millis = (long)getFilterDays() * millisPerDay;
    Date date = new Date(System.currentTimeMillis() - millis);
    return TextUtils.formatDate(date, "yyyyMMdd");
  }

  private int getFilterDays()
  {
    MenuModel menuModel = UserSessionBean.getCurrentInstance().getMenuModel();
    MenuItemCursor selectedMenuItem = menuModel.getSelectedMenuItem();

    String days = selectedMenuItem.getProperty(FILTER_DAYS_PROPERTY);
    if (days != null)
    {
      try
      {
        return Integer.parseInt(days);
      }
      catch (NumberFormatException ex)
      {
        // ignore
      }
    }
    return DAYS;
  }
}
