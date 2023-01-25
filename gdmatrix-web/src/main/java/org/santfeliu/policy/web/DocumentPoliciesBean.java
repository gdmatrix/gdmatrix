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
import org.matrix.policy.DocumentPolicyFilter;
import org.matrix.policy.DocumentPolicy;
import org.matrix.policy.DocumentPolicyView;
import org.matrix.policy.PolicyManagerPort;
import org.matrix.policy.PolicyState;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.doc.web.DocumentConfigBean;
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
public class DocumentPoliciesBean extends PageBean
{
  @CMSProperty
  public static final String ANALYZER_SCRIPT_PROPERTY = "analyzer";
  public static final Logger LOGGER = Logger.getLogger("document_analyzer");

  private DocumentPolicy editingDocumentPolicy;
  private List<DocumentPolicyView> rows;
  private transient List<LogRecord> messageList = new ArrayList<>();

  public DocumentPoliciesBean()
  {
    load();
  }

  public DocumentPolicy getEditingDocumentPolicy()
  {
    return editingDocumentPolicy;
  }

  public void setEditingDocumentPolicy(DocumentPolicy editingDocumentPolicy)
  {
    this.editingDocumentPolicy = editingDocumentPolicy;
  }

  public List<DocumentPolicyView> getRows()
  {
    return rows;
  }

  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }

  public void setRows(List<DocumentPolicyView> rows)
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
    String dateString = (String)getValue("#{row.docPolicy.activationDate}");
    return TextUtils.parseInternalDate(dateString);
  }

  public Date getEditingActivationDate()
  {
    Date result = null;
    if (editingDocumentPolicy != null &&
      editingDocumentPolicy.getActivationDate() != null)
    {
      result = TextUtils.parseInternalDate(
        editingDocumentPolicy.getActivationDate());
    }
    return result;
  }

  public Date getEditingCreationDateTime()
  {
    Date result = null;
    if (editingDocumentPolicy != null &&
      editingDocumentPolicy.getCreationDateTime() != null)
    {
      result = TextUtils.parseInternalDate(
        editingDocumentPolicy.getCreationDateTime());
    }
    return result;
  }

  public Date getEditingApprovalDateTime()
  {
    Date result = null;
    if (editingDocumentPolicy != null &&
      editingDocumentPolicy.getApprovalDateTime() != null)
    {
      result = TextUtils.parseInternalDate(
        editingDocumentPolicy.getApprovalDateTime());
    }
    return result;
  }

  public Date getEditingExecutionDateTime()
  {
    Date result = null;
    if (editingDocumentPolicy != null &&
      editingDocumentPolicy.getExecutionDateTime() != null)
    {
      result = TextUtils.parseInternalDate(
        editingDocumentPolicy.getExecutionDateTime());
    }
    return result;
  }

  @Override
  public String show()
  {
    return "document_policies";
  }

  @Override
  public String store()
  {
    if (editingDocumentPolicy != null)
    {
      storeDocumentPolicy();
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
      "#{documentPoliciesBean.editingDocumentPolicy.policyId}");
  }

  public String removeDocumentPolicy()
  {
    try
    {
      DocumentPolicyView row = (DocumentPolicyView)getRequestMap().get("row");
      PolicyManagerPort port = PolicyConfigBean.getPort();
      port.removeDocumentPolicy(row.getDocPolicy().getDocPolicyId());
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String storeDocumentPolicy()
  {
    try
    {
      String[] objectId = DocumentConfigBean.fromObjectId(getObjectId());
      String docId = objectId[0];
      editingDocumentPolicy.setDocId(docId);

      PolicyManagerPort port = PolicyConfigBean.getPort();
      port.storeDocumentPolicy(editingDocumentPolicy);
      editingDocumentPolicy = null;
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String cancelDocumentPolicy()
  {
    editingDocumentPolicy = null;
    return null;
  }

  public String createDocumentPolicy()
  {
    editingDocumentPolicy = new DocumentPolicy();
    return null;
  }

  public String editDocumentPolicy()
  {
    try
    {
      DocumentPolicyView row = (DocumentPolicyView)getExternalContext().
        getRequestMap().get("row");

      DocumentPolicy docPolicy = row.getDocPolicy();

      if (docPolicy != null)
        editingDocumentPolicy = docPolicy;
    }
    catch(Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public void analyzeDocument()
  {
    try
    {
      getMessageList().clear();

      ListHandler handler = new ListHandler();
      handler.setFilter(record ->
        record.getThreadID() == Thread.currentThread().getId());
      handler.setLevel(Level.ALL);
      LOGGER.addHandler(handler);
      LOGGER.setLevel(Level.ALL);

      String[] id = DocumentConfigBean.fromObjectId(getObjectId());
      String docId = id[0];

      try
      {
        ScriptClient client = new ScriptClient();
        client.put("docId", docId);
        client.put("logger", LOGGER);
        client.refreshCache();

        String scriptName = getProperty(ANALYZER_SCRIPT_PROPERTY);
        if (scriptName == null) scriptName = "analyzer";

        client.executeScript(scriptName);
      }
      catch (Exception screx)
      {
        LOGGER.log(Level.SEVERE, screx.toString());
      }
      finally
      {
        getMessageList().addAll(handler.getLogRecords());
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
    return policyBean.getSelectItems(editingDocumentPolicy.getPolicyId());
  }

  public SelectItem[] getPolicyStateSelectItems()
  {
    ResourceBundle bundle = ResourceBundle.getBundle(
      "org.santfeliu.policy.web.resources.PolicyBundle", getLocale());
    return FacesUtils.getEnumSelectItems(PolicyState.class, bundle);
  }

  public String getPolicyState()
  {
    DocumentPolicyView documentPolicyView = (DocumentPolicyView) getValue("#{row}");
    if (documentPolicyView != null && documentPolicyView.getDocPolicy() != null)
    {
      PolicyState state = documentPolicyView.getDocPolicy().getState();
      ResourceBundle bundle = ResourceBundle.getBundle(
        "org.santfeliu.policy.web.resources.PolicyBundle", getLocale());
      return bundle.getString(state.getClass().getName() + "." + state.toString());
    }
    return "";
  }

  @Override
  public Type getSelectedType()
  {
    return TypeCache.getInstance().getType(DictionaryConstants.DOCUMENT_POLICY_TYPE);
  }

  private void load()
  {
    try
    {
      if (!isNew())
      {
        DocumentPolicyFilter filter = new DocumentPolicyFilter();
        String[] objectId = DocumentConfigBean.fromObjectId(getObjectId());
        filter.setDocId(objectId[0]);
        rows =
          PolicyConfigBean.getPort().findDocumentPolicyViews(filter);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
}
