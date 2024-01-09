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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.doc.DocumentManagerPort;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.webapp.modules.assistant.openai.Assistant;
import org.santfeliu.webapp.modules.assistant.openai.Model;
import org.santfeliu.webapp.modules.assistant.openai.ModelList;
import org.santfeliu.webapp.modules.assistant.openai.OpenAI;
import org.santfeliu.webapp.modules.doc.DocModuleBean;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class AssistantBean extends WebBean implements Serializable
{
  public static final String ASSISTANTID_PROPERTY = "assistantId";
  public static final String ASSISTANT_ADMIN_ROLEID = "ASSISTANT_ADMIN";

  public static final String CREATION_USERID_METADATA = "creatorUserId";
  public static final String CHANGE_USERID_METADATA = "changeUserId";
  public static final String READ_ROLEID_METADATA = "readRole";
  public static final String WRITE_ROLEID_METADATA = "writeRole";

  String view = "threads"; // threads || assistant
  String assistantId;
  Assistant assistant;
  List<SelectItem> modelSelectItems;
  boolean dialogVisible;
  int activeTabIndex;

  transient OpenAI openAI = new OpenAI();
  transient List<Assistant> assistants;

  @Inject
  ThreadsBean threadsBean;

  @PostConstruct
  public void init()
  {
    String apiKey = MatrixConfig.getProperty("openai.apiKey");
    openAI.setApiKey(apiKey);
  }

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

  public List<Assistant> getAssistants()
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

  public void setAssistantId(String assistantId)
  {
    this.assistantId = assistantId;
  }

  public String getAssistantId()
  {
    return assistantId;
  }

  public String getCreationUserId()
  {
    return (String)assistant.getMetadataValue(CREATION_USERID_METADATA);
  }

  public String getChangeUserId()
  {
    return (String)assistant.getMetadataValue(CHANGE_USERID_METADATA);
  }

  public String getReadRoleId()
  {
    return (String)assistant.getMetadataValue(READ_ROLEID_METADATA);
  }

  public void setReadRoleId(String roleId)
  {
    assistant.setMetadataValue(READ_ROLEID_METADATA, roleId);
  }

  public String getWriteRoleId()
  {
    return (String)assistant.getMetadataValue(WRITE_ROLEID_METADATA);
  }

  public void setWriteRoleId(String roleId)
  {
    assistant.setMetadataValue(WRITE_ROLEID_METADATA, roleId);
  }

  public Assistant getAssistant()
  {
    return assistant;
  }

  public void createAssistant()
  {
    assistant = new Assistant();
    assistant.setName("New assistant");
    assistantId = null;
    activeTabIndex = 0;
  }

  public void reloadAssistant()
  {
    try
    {
      assistant = openAI.retrieveAssistant(assistantId);
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
      String userId = getUserId();
      if (assistant.getMetadataValue(CREATION_USERID_METADATA) == null)
      {
        assistant.setMetadataValue(CREATION_USERID_METADATA, userId);
      }
      assistant.setMetadataValue(CHANGE_USERID_METADATA, userId);

      if (assistantId == null)
      {
        if (!isAdminUser()) throw new Exception("ACCESS_DENIED");
        assistant = openAI.createAssistant(assistant);
        assistantId = assistant.getId();
      }
      else
      {
        assistant = openAI.modifyAssistant(assistant);
      }
      assistants = null;
      growl("STORE_OBJECT");
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
      if (assistantId != null)
      {
        openAI.deleteAssistant(assistantId);
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

  public void changeAssistant(Assistant assistant)
  {
    this.assistant = assistant;
    this.assistantId = assistant.getId();
    activeTabIndex = 0;
  }

  // Models

  public List<SelectItem> getModelSelectItems()
  {
    return modelSelectItems;
  }

  public void updateModelSelectItems() throws Exception
  {
    modelSelectItems = new ArrayList<>();
    ModelList models = openAI.listModels();
    Collections.sort(models.getData(),
      (a, b) -> a.getId().compareTo(b.getId()));

    for (Model m : models.getData())
    {
      SelectItem selectItem = new SelectItem();
      selectItem.setLabel(m.getId());
      selectItem.setValue(m.getId());
      modelSelectItems.add(selectItem);
    }
  }

  public String show()
  {
    try
    {
      threadsBean.updateThreads();
      updateAssistants();
      updateModelSelectItems();

      String preferredAssistantId = getProperty(ASSISTANTID_PROPERTY);
      if (!assistants.isEmpty())
      {
        if (assistants.stream().anyMatch(
          a -> a.getId().equals(preferredAssistantId)))
        {
          assistantId = preferredAssistantId;
        }
        else
        {
          assistantId = (String)assistants.get(0).getId();
        }
      }

      if (StringUtils.isBlank(assistantId))
      {
        createAssistant();
      }
      else
      {
        assistant = openAI.retrieveAssistant(assistantId);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }

    String template = UserSessionBean.getCurrentInstance().getTemplate();
    return "/templates/" + template + "/template.xhtml";
  }

  public String getContent()
  {
    return "/pages/assistant/" + view + ".xhtml";
  }

  public String getView()
  {
    return view;
  }

  public void setView(String view)
  {
    this.view = view;
  }

  public void updateAssistants() throws Exception
  {
    assistants = openAI.listAssistants().getData().stream().filter(
      a -> isVisible(a)).collect(Collectors.toList());
    Collections.sort(assistants, (a, b) -> a.getName().compareTo(b.getName()));
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

  public boolean isVisible(Assistant assistant)
  {
    if (isEditable(assistant)) return true;

    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    String readRoleId = (String)assistant.getMetadataValue(READ_ROLEID_METADATA);
    return userSessionBean.isUserInRole(readRoleId);
  }

  public boolean isEditable(Assistant assistant)
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    if (userSessionBean.isUserInRole(ASSISTANT_ADMIN_ROLEID)) return true;

    String writeRoleId = (String)assistant.getMetadataValue(WRITE_ROLEID_METADATA);
    return userSessionBean.isUserInRole(writeRoleId);
  }
}
