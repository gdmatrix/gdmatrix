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
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.doc.DocumentManagerPort;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.webapp.modules.assistant.openai.Assistant;
import org.santfeliu.webapp.modules.assistant.openai.AssistantList;
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
  String view = "threads"; // threads || assistant
  String assistantId;
  Assistant assistant;
  List<SelectItem> assistantSelectItems;
  List<SelectItem> modelSelectItems;
  boolean dialogVisible;

  transient OpenAI openAI = new OpenAI();

  @Inject
  ThreadsBean threadsBean;

  @PostConstruct
  public void init()
  {
    String apiKey = MatrixConfig.getProperty("openai.apiKey");
    openAI.setApiKey(apiKey);
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

  public void setAssistantId(String assistantId)
  {
    this.assistantId = assistantId;
  }

  public String getAssistantId()
  {
    return assistantId;
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
      if (assistantId == null)
      {
        assistant = openAI.createAssistant(assistant);
        assistantId = assistant.getId();
      }
      else
      {
        assistant = openAI.modifyAssistant(assistant);
      }
      updateAssistantSelectItems();
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
        updateAssistantSelectItems();
        growl("REMOVE_OBJECT");
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public List<SelectItem> getAssistantSelectItems()
  {
    return assistantSelectItems;
  }

  public void onAssistantChange(AjaxBehaviorEvent event)
  {
    try
    {
      if (StringUtils.isBlank(assistantId))
      {
        assistantId = null;
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
  }

  public void updateAssistantSelectItems() throws Exception
  {
    assistantSelectItems = new ArrayList<>();
    AssistantList assistants = openAI.listAssistants();
    for (Assistant a : assistants.getData())
    {
      SelectItem selectItem = new SelectItem();
      selectItem.setLabel(a.getName());
      selectItem.setValue(a.getId());
      assistantSelectItems.add(selectItem);
    }
    assistantSelectItems.add(new SelectItem(null, "New assistant"));
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
      updateAssistantSelectItems();
      updateModelSelectItems();
      // TODO: search assistant name specified in cms node property
      assistantId = (String)assistantSelectItems.get(0).getValue();
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

  public DocumentManagerPort getDocPort()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    String userId = userSessionBean.getUserId();
    String password = userSessionBean.getPassword();
    return DocModuleBean.getPort(userId, password);
  }

}
