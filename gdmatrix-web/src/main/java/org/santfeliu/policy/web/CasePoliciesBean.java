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
package org.santfeliu.policy.web;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.faces.model.SelectItem;
import org.matrix.dic.DictionaryConstants;
import org.matrix.policy.CasePolicy;
import org.matrix.policy.CasePolicyFilter;
import org.matrix.policy.CasePolicyView;
import org.matrix.policy.PolicyManagerPort;
import org.matrix.policy.PolicyState;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.util.TextUtils;
import org.santfeliu.util.log.ListHandler;
import org.santfeliu.util.script.ScriptClient;
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
  public static final String ANALYZER_SCRIPT_PROPERTY = "analyzer";
  public static final Logger LOGGER = Logger.getLogger("case_analyzer");

  private CasePolicy editingCasePolicy;
  private List<CasePolicyView> rows;
  private transient List<LogRecord> messageList = new ArrayList<>();

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

  public List<LogRecord> getMessageList()
  {
    if (messageList == null) messageList = new ArrayList<>();
    return messageList;
  }

  public void setMessageList(List<LogRecord> messageList)
  {
    this.messageList = messageList;
  }

  public String getFormattedMessage(LogRecord record)
  {
    try
    {
      String formattedMessage;
      if (record.getParameters() == null)
      {
        formattedMessage = record.getMessage();
      }
      else
      {
        formattedMessage =
          MessageFormat.format(record.getMessage(), record.getParameters());
      }
      return formattedMessage;
    }
    catch (Exception ex)
    {
      return ex.toString();
    }
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
      ListHandler handler = new ListHandler();
      handler.setLoggingThread(Thread.currentThread());
      LOGGER.addHandler(handler);

      String caseId = getObjectId();

      try
      {
        ScriptClient client = new ScriptClient();
        client.put("caseId", caseId);
        client.put("logger", LOGGER);
        client.refreshCache();

        String scriptName = getProperty(ANALYZER_SCRIPT_PROPERTY);
        if (scriptName == null) scriptName = "analyzer";

        client.executeScript(scriptName);
        getMessageList().addAll(handler.getLogRecords());
      }
      catch (Exception screx)
      {
        LOGGER.log(Level.SEVERE, screx.toString());
      }
      finally
      {
        LOGGER.removeHandler(handler);
      }

      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void reloadPolicies()
  {
    load();
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
