package org.santfeliu.policy.web;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import javax.faces.model.SelectItem;
import org.matrix.dic.DictionaryConstants;
import org.matrix.policy.CasePolicy;
import org.matrix.policy.CasePolicyFilter;
import org.matrix.policy.CasePolicyView;
import org.matrix.policy.PolicyManagerPort;
import org.matrix.policy.PolicyState;
import org.santfeliu.ant.AntLauncher;
import org.santfeliu.ant.Message;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.bean.CMSProperty;
import org.santfeliu.web.obj.PageBean;

/**
 *
 * @author realor
 */
@CMSManagedBean
public class CasePoliciesBean extends PageBean
{
  @CMSProperty
  public static final String ANALYZER_FILES_PROPERTY = "case_analyzer.files";
  @CMSProperty
  public static final String ANALYZER_TARGET_PROPERTY = "case_analyzer.target";
  @CMSProperty
  public static final String ANALYZER_VERBOSITY_PROPERTY = "case_analyzer.verbosity";
  @CMSProperty
  public static final String ANALYZER_PROPERTIES_PROPERTY = "case_analyzer.properties";

  public static final String ANT_DIR_PROPERTY = "antDir";
  
  private CasePolicy editingCasePolicy;
  private List<CasePolicyView> rows;
  private transient List<String> messageList = new ArrayList<String>();

  public CasePoliciesBean()
  {
    load();
  }

  public CasePolicy getEditingCasePolicy()
  {
    return editingCasePolicy;
  }

  public void setEditingCasePolicy(CasePolicy editingCasePolicy)
  {
    this.editingCasePolicy = editingCasePolicy;
  }

  public List<CasePolicyView> getRows()
  {
    return rows;
  }

  public void setRows(List<CasePolicyView> rows)
  {
    this.rows = rows;
  }

  public List<String> getMessageList()
  {
    if (messageList == null) messageList = new ArrayList<String>();
    return messageList;
  }

  public void setMessageList(List<String> messageList)
  {
    this.messageList = messageList;
  }

  public Date getRowActivationDate()
  {
    String dateString = (String)getValue("#{row.casePolicy.activationDate}");
    return TextUtils.parseInternalDate(dateString);
  }

  public Date getEditingActivationDate()
  {
    Date result = null;
    if (editingCasePolicy != null &&
      editingCasePolicy.getActivationDate() != null)
    {
      result = TextUtils.parseInternalDate(
        editingCasePolicy.getActivationDate());
    }
    return result;
  }

  public Date getEditingCreationDateTime()
  {
    Date result = null;
    if (editingCasePolicy != null &&
      editingCasePolicy.getCreationDateTime() != null)
    {
      result = TextUtils.parseInternalDate(
        editingCasePolicy.getCreationDateTime());
    }
    return result;
  }

  public Date getEditingApprovalDateTime()
  {
    Date result = null;
    if (editingCasePolicy != null &&
      editingCasePolicy.getApprovalDateTime() != null)
    {
      result = TextUtils.parseInternalDate(
        editingCasePolicy.getApprovalDateTime());
    }
    return result;
  }

  public Date getEditingExecutionDateTime()
  {
    Date result = null;
    if (editingCasePolicy != null &&
      editingCasePolicy.getExecutionDateTime() != null)
    {
      result = TextUtils.parseInternalDate(
        editingCasePolicy.getExecutionDateTime());
    }
    return result;
  }

  @Override
  public String show()
  {
    return "case_policies";
  }

  @Override
  public String store()
  {
    if (editingCasePolicy != null)
    {
      storeCasePolicy();
    }
    else
    {
      load();
    }
    return show();
  }

  public String showPolicy()
  {
    return getControllerBean().showObject("Policy",
      (String)getValue("#{row.policy.policyId}"));
  }

  public String searchPolicy()
  {
    return getControllerBean().searchObject("Policy",
      "#{casePoliciesBean.editingCasePolicy.policyId}");
  }

  public String removeCasePolicy()
  {
    try
    {
      CasePolicyView row = (CasePolicyView)getRequestMap().get("row");
      PolicyManagerPort port = PolicyConfigBean.getPort();
      port.removeCasePolicy(row.getCasePolicy().getCasePolicyId());
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String storeCasePolicy()
  {
    try
    {
      String caseId = getObjectId();
      editingCasePolicy.setCaseId(caseId);

      PolicyManagerPort port = PolicyConfigBean.getPort();
      port.storeCasePolicy(editingCasePolicy);
      editingCasePolicy = null;
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String cancelCasePolicy()
  {
    editingCasePolicy = null;
    return null;
  }

  public String createCasePolicy()
  {
    editingCasePolicy = new CasePolicy();
    return null;
  }

  public String editCasePolicy()
  {
    try
    {
      CasePolicyView row = (CasePolicyView)getExternalContext().
        getRequestMap().get("row");

      CasePolicy casePolicy = row.getCasePolicy();

      if (casePolicy != null)
        editingCasePolicy = casePolicy;
    }
    catch(Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public void analyzeCase()
  {
    try
    {
      int maxVerbosityLevel = getMaxVerbosityLevel();
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      MenuModel menuModel = userSessionBean.getMenuModel();

      List<String> antFiles = menuModel.getSelectedMenuItem().
        getMultiValuedProperty(ANALYZER_FILES_PROPERTY);
      if (!antFiles.isEmpty())
      {
        String[] fileArray = (String[])antFiles.toArray(
          new String[antFiles.size()]);

        String antTarget = menuModel.getSelectedMenuItem().
          getProperty(ANALYZER_TARGET_PROPERTY);

        // preparing ant properties
        HashMap properties = new HashMap();
        List<String> nameValuePairs = menuModel.getSelectedMenuItem().
          getMultiValuedProperty(ANALYZER_PROPERTIES_PROPERTY);
        for (String nameValuePair : nameValuePairs)
        {
          int index = nameValuePair.indexOf("=");
          if (index > 0)
          {
            String name = nameValuePair.substring(0, index);
            String value = nameValuePair.substring(index + 1);
            properties.put(name, value);
          }
        }
        properties.put("caseId", getObjectId());

        File antDir = null;
        String dir = System.getProperty(ANT_DIR_PROPERTY);
        if (dir != null)
        {
          antDir = new File(dir);
        }
        String contextPath = MatrixConfig.getProperty("contextPath");
        URL wsDirectory =
          new URL("http://localhost" + contextPath + "/wsdirectory");

        String userId =
          UserSessionBean.getCurrentInstance().getCredentials().getUserId();
        String password =
          UserSessionBean.getCurrentInstance().getCredentials().getPassword();
        
        List<Message> messages = AntLauncher.execute(fileArray, antTarget,
          properties, wsDirectory, userId, password, antDir);
        
        for (Message message : messages)
        {
          if (message.getLevel() <= maxVerbosityLevel)
          {
            getMessageList().add(message.getMessage());
          }
        }
        load();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public int getMaxVerbosityLevel()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuModel menuModel = userSessionBean.getMenuModel();
    String value = menuModel.getSelectedMenuItem().
      getProperty(ANALYZER_VERBOSITY_PROPERTY);
    if (value != null)
    {
      try
      {
        return Integer.parseInt(value);
      }
      catch (NumberFormatException ex)
      {
      }
    }
    return Integer.MAX_VALUE;
  }

  public List<SelectItem> getPolicySelectItems()
  {
    PolicyBean policyBean = (PolicyBean)getBean("policyBean");
    return policyBean.getSelectItems(editingCasePolicy.getPolicyId());
  }

  public SelectItem[] getPolicyStateSelectItems()
  {
    ResourceBundle bundle = ResourceBundle.getBundle(
      "org.santfeliu.policy.web.resources.PolicyBundle", getLocale());
    return FacesUtils.getEnumSelectItems(PolicyState.class, bundle);
  }

  public String getPolicyState()
  {
    CasePolicyView casePolicyView = (CasePolicyView) getValue("#{row}");
    if (casePolicyView != null && casePolicyView.getCasePolicy() != null)
    {
      PolicyState state = casePolicyView.getCasePolicy().getState();
      ResourceBundle bundle = ResourceBundle.getBundle(
        "org.santfeliu.policy.web.resources.PolicyBundle", getLocale());
      return bundle.getString(state.getClass().getName() + "." + state.toString());
    }
    return "";
  }

  @Override
  public Type getSelectedType()
  {
    return TypeCache.getInstance().getType(DictionaryConstants.CASE_POLICY_TYPE);
  }

  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }

  private void load()
  {
    try
    {
      if (!isNew())
      {
        CasePolicyFilter filter = new CasePolicyFilter();
        filter.setCaseId(getObjectId());
        rows = PolicyConfigBean.getPort().findCasePolicyViews(filter);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
}
