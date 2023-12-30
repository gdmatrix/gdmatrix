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
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.mozilla.javascript.Callable;
import org.primefaces.PrimeFaces;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.script.ScriptClient;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.webapp.modules.assistant.openai.FunctionCall;
import org.santfeliu.webapp.modules.assistant.openai.FunctionExecutor;
import org.santfeliu.webapp.modules.assistant.openai.MessageList;
import org.santfeliu.webapp.modules.assistant.openai.OpenAI;
import org.santfeliu.webapp.modules.assistant.openai.Thread;
import org.santfeliu.webapp.modules.assistant.openai.ThreadStore;
import static org.apache.commons.lang.StringUtils.isBlank;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class ThreadsBean extends WebBean
  implements Serializable, FunctionExecutor
{
  String threadId;
  Thread thread;
  String text;
  List<Thread> threads;
  MessageList messageList;
  boolean processing;

  transient OpenAI openAI = new OpenAI();

  @Inject
  AssistantBean assistantBean;

  @PostConstruct
  public void init()
  {
    String apiKey = MatrixConfig.getProperty("openai.apiKey");
    openAI.setApiKey(apiKey);
    openAI.setFunctionExecutor(this);
  }

  public String getThreadId()
  {
    return threadId;
  }

  public void setThreadId(String threadId)
  {
    this.threadId = threadId;
  }

  public List<Thread> getThreads()
  {
    return threads;
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

  public void changeThread(String threadId)
  {
    try
    {
      if (StringUtils.isBlank(threadId))
      {
        this.threadId = null;
        thread = null;
        messageList = null;
      }
      else
      {
        this.threadId = threadId;
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
        updateThreads();
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

  public String getThreadTitle(String threadId)
  {
    if (isBlank(threadId)) return "";

    if (threads == null) return threadId;

    Thread selectedThread = threads.stream().filter(
      t -> StringUtils.equals(threadId, t.getId())).findFirst().orElse(null);

    return selectedThread == null ?
      threadId : (String)selectedThread.getMetadata().get("title");
  }

  public void updateThreads() throws Exception
  {
    threads = getThreadStore().findThreads();
  }

  // Messages

  public void createMessage()
  {
    try
    {
      if (isBlank(threadId))
      {
        thread = openAI.createThread();
        threadId = thread.getId();
        String title = text;
        if (title.length() > 100) title = title.substring(0, 100) + "...";
        thread.getMetadata().put("title", title);
        getThreadStore().storeThread(thread);
        updateThreads();
      }

      openAI.createMessage(threadId, "user", text);
      updateMessages();

      if (text.startsWith("#"))
      {
        if (text.equals("#error"))
        {
          error(text);
        }
      }
      else
      {
        PrimeFaces.current().executeScript("assist()");
        processing = true;
      }
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
      String assistantId = assistantBean.getAssistantId();
      if (assistantId != null)
      {
        openAI.assist(threadId, assistantId);
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

  @Override
  public String execute(FunctionCall function)
  {
    String functionName = function.getName();
    String functionArgs = function.getArguments();
    String resultText;

    try
    {
      ScriptClient scriptClient = new ScriptClient();
      scriptClient.executeScript(functionName);

      Object value = scriptClient.get(functionName);
      if (value instanceof Callable)
      {
        StringBuilder buffer = new StringBuilder();
        buffer.append(functionName);
        buffer.append("(");
        if (!StringUtils.isBlank(functionArgs))
        {
          buffer.append(functionArgs);
        }
        buffer.append(")");
        String cmd = buffer.toString();
        System.out.println("CALL " + cmd);
        Object result = scriptClient.execute(cmd);
        resultText = String.valueOf(result);
      }
      else
      {
        resultText = "Function executed.";
      }
    }
    catch (Exception ex)
    {
      resultText = "Function not available";
    }
    System.out.println("Result: " + resultText);
    return resultText;
  }

  public boolean isProcessing()
  {
    return processing;
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
