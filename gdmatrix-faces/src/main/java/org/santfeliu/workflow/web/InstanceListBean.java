package org.santfeliu.workflow.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.faces.model.SelectItem;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.matrix.workflow.InstanceFilter;
import org.matrix.workflow.InstanceView;
import org.matrix.workflow.VariableFilter;
import org.matrix.workflow.WorkflowConstants;
import org.matrix.workflow.WorkflowManagerPort;
import org.matrix.workflow.WorkflowManagerService;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.bean.CMSProperty;

@CMSManagedBean
public class InstanceListBean extends WebBean implements Serializable
{
  @CMSProperty
  public static final String SHOW_CATALOGUE_MID_PROPERTY = "showCatalogueMid";
  @CMSProperty
  public static final String EXIT_MID_PROPERTY = "exitMid";
  @CMSProperty
  public static final String EXIT_MID_ANONYMOUS_PROPERTY = "exitMidA";

  public static final String INSTANCEID_PARAM = "instanceid";
  public static final String ACCESS_TOKEN_PARAM = "access_token";
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

  public InstanceListBean()
  {
    // setup inital filter parameters
    state = "P";
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
      String workflowName = (String)parameters.get(
        ProcedureCatalogueBean.WORKFLOW);
      String instanceId = (String)parameters.get(INSTANCEID_PARAM);
      if (workflowName != null)
      {
        InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
        return instanceBean.createInstance(workflowName, null, false, parameters);
      }
      else if (instanceId != null)
      {
        String outcome;
        InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
        instanceBean.setInstanceId(instanceId);
        String accessToken = instanceBean.getAccessToken();
        if (accessToken != null)
        {
          if (!accessToken.equals(parameters.get(ACCESS_TOKEN_PARAM)))
            throw new Exception("INVALID_INSTANCE_TOKEN");

          int index = accessToken.indexOf(LOGIN_ACCESS_TOKEN_SEPARATOR);
          if (index != -1)
          {
            String formVariable = accessToken.substring(index + 1);
            instanceBean.login(formVariable);
          }
          outcome = instanceBean.forward();
        }
        else if (!UserSessionBean.getCurrentInstance().isAnonymousUser())
        {
          // allow access if user is logged in, for backward compatibility
          outcome = instanceBean.forward();          
        }
        else // anonymous users can not access an instance without access token
        {
          // show empty instance list instead
          outcome = "instance_list";
        }
        return outcome;
      }
      else return findInstances();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return "instance_list";
  }

  @CMSAction
  public String findInstances()
  {
    try
    {
      loadInstanceList();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return "instance_list";
  }

  public String showCatalogue()
  {
    MenuModel menuModel = UserSessionBean.getCurrentInstance().getMenuModel();
    String showCatalogueMid = (String)menuModel.
      getSelectedMenuItem().getProperties().get(SHOW_CATALOGUE_MID_PROPERTY);

    if (showCatalogueMid != null)
    {
      menuModel.setSelectedMid(showCatalogueMid);
      ProcedureCatalogueBean procedureCatalogueBean = 
        (ProcedureCatalogueBean)getBean("procedureCatalogueBean");
      return procedureCatalogueBean.showCatalogue();
    }
    return null;
  }

  public String simulate()
  {
    try
    {
      InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
      return instanceBean.createInstance(workflowName, null, true);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String transact()
  {
    try
    {
      InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
      return instanceBean.createInstance(workflowName, null, false);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String forward()
  {
    InstanceView instanceView = (InstanceView)getRequestMap().get("instance");
    String instanceId = instanceView.getInstanceId();
    InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
    instanceBean.setInstanceId(instanceId);
    return instanceBean.forward();
  }
  
  public String destroyInstance()
  {
    InstanceView instanceView = (InstanceView)getRequestMap().get("instance");
    String instanceId = instanceView.getInstanceId();    
    InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
    instanceBean.setInstanceId(instanceId);
    return instanceBean.destroyInstance();
  }

  public String showInstanceById()
  {
    InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
    instanceBean.setInstanceId(instanceId);
    return instanceBean.forward();
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
    long millis = System.currentTimeMillis() - (long)DAYS * 24L * 3600L * 1000L;
    Date date = new Date(millis);
    return TextUtils.formatDate(date, "yyyyMMdd");
  }
}
