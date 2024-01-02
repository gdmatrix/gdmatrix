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
package org.santfeliu.webapp.modules.policy;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.policy.DocumentPolicy;
import org.matrix.policy.DocumentPolicyFilter;
import org.matrix.policy.DocumentPolicyView;
import org.matrix.policy.PolicyState;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.util.log.ListHandler;
import org.santfeliu.util.script.ScriptClient;
import org.santfeliu.web.bean.CMSProperty;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.modules.doc.DocumentObjectBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import static org.santfeliu.webapp.modules.policy.PolicyModuleBean.getPort;

/**
 *
 * @author blanquepa
 */
@Named
@ViewScoped
public class DocumentPoliciesTabBean extends TabBean
{
  private List<DocumentPolicyView> rows;
  private int firstRow;
  private DocumentPolicy editing; 
  
  @CMSProperty
  public static final String ANALYZER_SCRIPT_PROPERTY = "analyzer";
  public static final Logger LOGGER = Logger.getLogger("document_analyzer");  
  private List<LogRecord> messageList = new ArrayList<>();
  private ListHandler logHandler;    
  
  @Inject
  private DocumentObjectBean documentObjectBean;

  @Override
  public ObjectBean getObjectBean()
  {
    return documentObjectBean;
  }

  public List<DocumentPolicyView> getRows()
  {
    return rows;
  }

  public void setRows(List<DocumentPolicyView> rows)
  {
    this.rows = rows;
  }

  public int getFirstRow()
  {
    return firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    this.firstRow = firstRow;
  }

  public DocumentPolicy getEditing()
  {
    return editing;
  }

  public void setEditing(DocumentPolicy editing)
  {
    this.editing = editing;
  }
  
  public List<LogRecord> getMessageList()
  {
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
  
  public void refreshAnalyzeStatus()
  {
    if (logHandler != null)
    {
      List<LogRecord> logRecords = logHandler.getLogRecords();
      if (!logRecords.isEmpty())
      {
        getMessageList().clear();    
        getMessageList().addAll(logRecords);        
      }
    }
  }
    
  
  public SelectItem[] getPolicyStateSelectItems()
  {
    ResourceBundle bundle = ResourceBundle.getBundle(
      "org.santfeliu.policy.web.resources.PolicyBundle", getLocale());
    return FacesUtils.getEnumSelectItems(PolicyState.class, bundle);
  }

  public String getPolicyState(DocumentPolicyView row)
  {
    if (row != null && row.getDocPolicy() != null)
    {
      PolicyState state = row.getDocPolicy().getState();
      ResourceBundle bundle = ResourceBundle.getBundle(
        "org.santfeliu.policy.web.resources.PolicyBundle", getLocale());
      String key = state.getClass().getName() + "." + state.toString();
      return bundle.getString(key);
    }
    return "";
  }  
  
  @Override
  public void load()
  {
    if (!NEW_OBJECT_ID.equals(getObjectId()))
    {
      try
      {
        loadTableRows();
        messageList.clear();
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    else rows = Collections.EMPTY_LIST;
  }

  @Override
  public void store()
  {
    try
    {
      editing.setDocId(getObjectId());
      try
      {
        getPort(false).storeDocumentPolicy(editing);
      }
      catch (Exception ex)
      {
        throw new Exception("INVALID_OPERATION");
      }
      load();
      editing = null;
      growl("STORE_OBJECT");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void cancel()
  {
    editing = null;
  }

  @Override
  public boolean isDialogVisible()
  {
    return (editing != null);
  }  
  
  public void create()
  {
    editing = new DocumentPolicy();
  }

  public void edit(DocumentPolicyView documentPolicyView)
  {
    if (documentPolicyView != null)
    {
      try
      {
        editing = getPort(false)
          .loadDocumentPolicy(documentPolicyView.getDocPolicy().getDocPolicyId());
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    else
    {
      create();
    }
  }

  public void remove(DocumentPolicyView docPolicyView)
  {
    if (docPolicyView != null)
    {
      try
      {
        String docPolicyId = docPolicyView.getDocPolicy().getDocPolicyId();
        getPort(false).removeDocumentPolicy(docPolicyId);
        load();
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
  }
  
  public void analyzeCase()
  {
    try
    {
      getMessageList().clear();

      logHandler = new ListHandler();
      logHandler.setFilter(record ->
        record.getThreadID() == Thread.currentThread().getId());
      logHandler.setLevel(Level.ALL);
      LOGGER.addHandler(logHandler);
      LOGGER.setLevel(Level.ALL);

      String docId = getObjectId();

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
        getMessageList().clear();
        getMessageList().addAll(logHandler.getLogRecords());
        LOGGER.removeHandler(logHandler);
        loadTableRows();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }  

  @Override
  public Serializable saveState()
  {
    return new Object[]{ editing };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      editing = (DocumentPolicy)stateArray[0];

      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  } 
  
  private void loadTableRows() throws Exception
  {  
    DocumentPolicyFilter filter = new DocumentPolicyFilter();
    filter.setDocId(getObjectId());
    rows = getPort(false).findDocumentPolicyViews(filter); 
  }
}
