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
package org.santfeliu.webapp.modules.assistant;

import com.google.gson.Gson;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.doc.DocumentManagerPort;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.webapp.modules.assistant.langchain4j.Assistant;
import org.santfeliu.webapp.modules.assistant.langchain4j.AssistantStore;
import org.santfeliu.webapp.modules.assistant.langchain4j.AssistantStore.AssistantSummary;
import org.santfeliu.webapp.modules.doc.DocModuleBean;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.santfeliu.web.UserSessionBean.ACTION;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class AssistantBean extends WebBean implements Serializable
{
  public static final String THREADID_PARAMETER = "threadid";
  public static final String ASSISTANT_VIEW_PARAMETER = "assistant_view";
  public static final String ASSISTANTID_PARAMETER = "assistantid";

  public static final String ASSISTANTID_PROPERTY = "assistantId";
  public static final String ASSISTANT_ADMIN_ROLEID = "ASSISTANT_ADMIN";

  public static final String CREATION_USERID_METADATA = "creatorUserId";
  public static final String CHANGE_USERID_METADATA = "changeUserId";
  public static final String READ_ROLEID_METADATA = "readRole";
  public static final String WRITE_ROLEID_METADATA = "writeRole";

  String view = "threads"; // threads || assistant
  Assistant assistant;
  boolean dialogVisible;
  int activeTabIndex;

  List<AssistantSummary> assistants;

  @Inject
  ThreadsBean threadsBean;

  public int getActiveTabIndex()
  {
    return activeTabIndex;
  }

  public void setActiveTabIndex(int activeTabIndex)
  {
    this.activeTabIndex = activeTabIndex;
  }

  public boolean isDialogVisible()
  {
    return dialogVisible;
  }

  public void setDialogVisible(boolean dialogVisible)
  {
    this.dialogVisible = dialogVisible;
  }

  // Assistant

  public List<AssistantSummary> getAssistants()
  {
    if (assistants == null)
    {
      try
      {
        updateAssistants();
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return assistants;
  }

  public String getAssistantId()
  {
    return getAssistant().getAssistantId();
  }

  public Assistant getAssistant()
  {
    if (assistant == null)
    {
      initAssistant(null);
    }
    return assistant;
  }

  public String getJsonPageState()
  {
    MenuItemCursor menuItem = getSelectedMenuItem();
    String mid = menuItem.getMid();
    Map<String, String> jsonState = new HashMap<>();
    StringBuilder urlBuffer = new StringBuilder("/go.faces?xmid=" + mid);
    String threadId = threadsBean.getThreadId();
    String assistantId = getAssistantId();

    String title = menuItem.getLabel();

    if (!isBlank(threadId) && threadsBean.getThread().isPersistent())
    {
      urlBuffer.append("&").append(THREADID_PARAMETER).append("=").append(threadId);
      title += " - " + threadId;
    }

    if (!isBlank(assistantId) && getAssistant().isPersistent())
    {
      urlBuffer.append("&").append(ASSISTANTID_PARAMETER).append("=").append(assistantId);
      if ("assistant".equals(view)) title += " - " + getAssistant().getName();
    }
    urlBuffer.append("&").append(ASSISTANT_VIEW_PARAMETER).append("=").append(view);

    jsonState.put("title", title);
    jsonState.put("url", urlBuffer.toString());

    Gson gson = new Gson();
    return gson.toJson(jsonState);
  }

  public AssistantStore getAssistantStore()
  {
    Credentials credentials =
      UserSessionBean.getCurrentInstance().getCredentials();
    return AssistantStore.getInstance(credentials);
  }

  public void createAssistant()
  {
    assistant = new Assistant();
    assistant.setName("New assistant");
    activeTabIndex = 0;
  }

  public void copyAssistant()
  {
    assistant.setName("New assistant");
    assistant.setAssistantId(UUID.randomUUID().toString());
    assistant.setDocId(null);
    activeTabIndex = 0;
  }

  public void reloadAssistant()
  {
    try
    {
      if (assistant.isPersistent())
      {
        assistant = getAssistantStore().loadAssistant(assistant.getAssistantId());
        growl("RELOAD_OBJECT");
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void saveAssistant()
  {
    try
    {
      getAssistantStore().saveAssistant(assistant);
      growl("STORE_OBJECT");
      assistants = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void deleteAssistant()
  {
    try
    {
      if (assistant.isPersistent())
      {
        getAssistantStore().deleteAssistant(assistant.getAssistantId());
        createAssistant();
        growl("REMOVE_OBJECT");
        assistants = null;
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public boolean isAdminUser()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    return userSessionBean.isUserInRole(ASSISTANT_ADMIN_ROLEID);
  }

  public boolean isAssistantNode()
  {
    String action = getProperty(ACTION);
    return "#{assistantBean.show}".equals(action);
  }

  public void changeAssistant(String assistantId)
  {
    try
    {
      assistant = getAssistantStore().loadAssistant(assistantId);
      activeTabIndex = 0;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public String show()
  {
    Map<String, String> map = getExternalContext().getRequestParameterMap();
    try
    {
      String threadId = map.get(THREADID_PARAMETER);
      if (threadId == null)
      {
        threadsBean.createThread();
      }
      else
      {
        threadsBean.changeThread(threadId);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }

    String reqView = map.get(ASSISTANT_VIEW_PARAMETER);
    if ("assistant".equals(reqView))
    {
      setView("assistant");
    }
    else
    {
      setView("threads");
    }

    initAssistant(map.get(ASSISTANTID_PARAMETER));

    String template = UserSessionBean.getCurrentInstance().getTemplate();
    return "/templates/" + template + "/template.xhtml";
  }

  public String getContent()
  {
    return "/pages/assistant/assistant_panel.xhtml";
  }

  public String getView()
  {
    return view;
  }

  public void setView(String view)
  {
    this.view = view;
    threadsBean.setEditionEnabled(false);
  }

  public void showThreads()
  {
    setView("threads");
    if (threadsBean.getThread().isPersistent())
    {
      threadsBean.changeThread(threadsBean.getThreadId());
    }
  }

  public void updateAssistants() throws Exception
  {
    try
    {
      assistants = getAssistantStore().getAssistants();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public DocumentManagerPort getDocPort()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    String userId = userSessionBean.getUserId();
    String password = userSessionBean.getPassword();
    return DocModuleBean.getPort(userId, password);
  }

  public String getUserId()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    return userSessionBean.getUserId();
  }

  public boolean isAssistantEditable()
  {
    if (assistant == null) return false;

    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    if (userSessionBean.isUserInRole(ASSISTANT_ADMIN_ROLEID)) return true;

    String writeRoleId = (String)assistant.getWriteRoleId();
    return userSessionBean.isUserInRole(writeRoleId);
  }

  void initAssistant(String assistantId)
  {
    if (assistantId == null)
    {
      assistantId = UserSessionBean.getCurrentInstance().getSelectedMenuItem()
        .getProperty(ASSISTANTID_PROPERTY);
    }

    if (assistantId == null)
    {
      createAssistant();
    }
    else
    {
      try
      {
        assistant = getAssistantStore().loadAssistant(assistantId);
      }
      catch (Exception ex)
      {
        createAssistant();
        error(ex);
      }
    }
  }
}
