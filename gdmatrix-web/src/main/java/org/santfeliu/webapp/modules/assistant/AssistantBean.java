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
import java.util.HashMap;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.webapp.modules.assistant.openai.Assistant;
import org.santfeliu.webapp.modules.assistant.openai.AssistantList;
import org.santfeliu.webapp.modules.assistant.openai.File;
import org.santfeliu.webapp.modules.assistant.openai.MessageList;
import org.santfeliu.webapp.modules.assistant.openai.Model;
import org.santfeliu.webapp.modules.assistant.openai.ModelList;
import org.santfeliu.webapp.modules.assistant.openai.OpenAI;
import org.santfeliu.webapp.modules.assistant.openai.Thread;
import org.santfeliu.webapp.modules.assistant.openai.ThreadStore;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class AssistantBean extends WebBean implements Serializable
{
  String view = "thread"; // thread || assistant
  String assistantId;
  String threadId;
  Assistant assistant;
  Thread thread;
  String text;
  MessageList messageList;
  boolean processing;
  List<SelectItem> assistantSelectItems;
  List<SelectItem> threadSelectItems;
  List<SelectItem> modelSelectItems;
  transient OpenAI openAI = new OpenAI();
  transient HashMap<String, File> fileCache = new HashMap<>();

  @PostConstruct
  public void init()
  {
    String apiKey = MatrixConfig.getProperty("openai.apiKey");
    openAI.setApiKey(apiKey);
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

  private void updateAssistantSelectItems() throws Exception
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

  // Threads

  public String getThreadId()
  {
    return threadId;
  }

  public void setThreadId(String threadId)
  {
    this.threadId = threadId;
  }

  public List<SelectItem> getThreadSelectItems()
  {
    return threadSelectItems;
  }

  public MessageList getMessageList()
  {
    return messageList;
  }

  public String getText()
  {
    return text;
  }

  public void setText(String text)
  {
    this.text = text;
  }

  public void onThreadChange(AjaxBehaviorEvent event)
  {
    try
    {
      if (StringUtils.isBlank(threadId))
      {
        threadId = null;
        thread = null;
        messageList = null;
      }
      else
      {
        thread = openAI.retrieveThread(threadId);
        updateMessages();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void createThread()
  {
    thread = null;
    threadId = null;
    messageList = null;
  }

  public void deleteThread()
  {
    try
    {
      if (threadId != null)
      {
        openAI.deleteThread(threadId);
        getThreadStore().removeThread(threadId);
        updateThreadSelectItems();
        thread = null;
        threadId = null;
        messageList = null;
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private void updateThreadSelectItems() throws Exception
  {
    threadSelectItems = new ArrayList<>();
    threadSelectItems.add(new SelectItem(null, "New thread"));

    List<Thread> threads = getThreadStore().findThreads();
    for (Thread t : threads)
    {
      SelectItem selectItem = new SelectItem();
      String title = (String)t.getMetadata().get("title");
      if (title == null) title = t.getId();
      selectItem.setLabel(title);
      selectItem.setValue(t.getId());
      threadSelectItems.add(selectItem);
    }
  }

  // Messages

  public void createMessage()
  {
    try
    {
      if (threadId == null)
      {
        thread = openAI.createThread();
        threadId = thread.getId();
        String title = text;
        if (title.length() > 100) title = title.substring(0, 100) + "...";
        thread.getMetadata().put("title", title);
        getThreadStore().storeThread(thread);
        updateThreadSelectItems();
      }
      openAI.createMessage(thread, "user", text);
      updateMessages();
      processing = true;
      text = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void assist()
  {
    try
    {
      if (assistantId != null)
      {
        openAI.assist(thread, assistant);
      }
      updateMessages();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    finally
    {
      processing = false;
    }
  }

  public boolean isProcessing()
  {
    return processing;
  }

  // Models

  public List<SelectItem> getModelSelectItems()
  {
    return modelSelectItems;
  }

  private void updateModelSelectItems() throws Exception
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

  // Files

  public File getFile(String fileId)
  {
    File file = null;
    try
    {
      file = fileCache.get(fileId);
      if (file == null)
      {
        file = openAI.retrieveFile(fileId);
        fileCache.put(fileId, file);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return file;
  }

  public void removeFile(String fileId)
  {
    assistant.getFileIds().remove(fileId);
  }

  public String show()
  {
    try
    {
      updateThreadSelectItems();
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

  public ThreadStore getThreadStore()
  {
    ThreadStore threadStore = CDI.current().select(ThreadStore.class).get();
    String userId = UserSessionBean.getCurrentInstance().getUserId();
    threadStore.setUserId(userId);
    return threadStore;
  }

  private void updateMessages() throws Exception
  {
    messageList = openAI.listMessages(threadId);
    Collections.sort(messageList.getData(),
      (a, b) -> (int)(a.getCreatedAt() - b.getCreatedAt()));
  }
}
