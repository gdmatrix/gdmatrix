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

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.output.FinishReason;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import javax.activation.DataHandler;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.matrix.dic.Property;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.util.FileDataSource;
import org.santfeliu.util.IOUtils;
import org.santfeliu.util.MimeTypeMap;
import org.santfeliu.web.servlet.stream.StreamQueue;
import org.santfeliu.webapp.modules.assistant.langchain4j.Assistant;
import org.santfeliu.webapp.modules.assistant.langchain4j.ChatMessageListener;
import org.santfeliu.webapp.modules.assistant.langchain4j.AssistantStore;
import org.santfeliu.webapp.modules.assistant.langchain4j.ChatMessageAdapter;
import org.santfeliu.webapp.modules.assistant.langchain4j.Thread;
import org.santfeliu.webapp.modules.assistant.langchain4j.ThreadStore;
import org.santfeliu.webapp.modules.assistant.langchain4j.ThreadSummary;
import org.santfeliu.webapp.modules.assistant.langchain4j.ToolExecutor;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class ThreadsBean extends WebBean implements Serializable
{
  public static final String TEXT_PLACEHOLDER_PROPERTY = "textPlaceholder";
  public static final String ATTACH_PLACEHOLDER_PROPERTY = "attachPlaceholder";

  public static final String ATTACHMENT_DOCTYPEID_PROPERTY = "Document";
  public static final String ATTACHMENT_THREADID_PROPERTY = "threadId";

  Thread thread;
  List<ThreadSummary> threads;
  String text;
  boolean debugEnabled = false;
  String attachedFilename;
  String attachedDocId;

  @Inject
  AssistantBean assistantBean;

  public Thread getThread()
  {
    return thread;
  }

  public String getThreadId()
  {
    return thread.getThreadId();
  }

  public List<ChatMessage> getMessages()
  {
    return thread.getMessages();
  }

  public List<ThreadSummary> getThreads()
  {
    if (threads == null)
    {
      updateThreads(true);
    }
    return threads;
  }

  public boolean isDebugEnabled()
  {
    return debugEnabled;
  }

  public void setDebugEnabled(boolean debugEnabled)
  {
    this.debugEnabled = debugEnabled;
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
      thread = getThreadStore().loadThread(threadId);
      repaintThread();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void repaintThread()
  {
    String threadId = getThreadId();
    StreamQueue queue = StreamQueue.getInstance(threadId, true);
    for (ChatMessage message : thread.getMessages())
    {
      pushMessage(queue, message, true);
    }
    queue.push(0);
    PrimeFaces.current().executeScript("showResponse('" + threadId + "')");
  }

  public void createThread()
  {
    thread = new Thread();
  }

  public void deleteThread()
  {
    try
    {
      getThreadStore().deleteThread(getThreadId());
      updateThreads(true);
      createThread();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void updateThreads(boolean reload)
  {
    if (reload)
    {
      threads = getThreadStore().getThreads();
    }
    else
    {
      for (ThreadSummary t : threads)
      {
        if (t.getThreadId().equals(getThreadId()))
        {
          t.setDescription(getThread().getDescription());
          break;
        }
      }
    }
  }

  // Messages

  public String getInputPlaceholder()
  {
    if (attachedFilename == null)
    {
      return getProperty(TEXT_PLACEHOLDER_PROPERTY);
    }
    else
    {
      return getProperty(ATTACH_PLACEHOLDER_PROPERTY);
    }
  }

  public void sendMessage()
  {
    final StreamQueue queue = StreamQueue.getInstance(getThreadId(), true);
    String userId = UserSessionBean.getCurrentInstance().getUserId();
    String threadId = getThreadId();
    attachedFilename = null;
    attachedDocId = null;

    try
    {
      queue.clear();
      if (!StringUtils.isBlank(text))
      {
        getMessages().add(UserMessage.from(text));
      }
      text = null;

      // save thread with the user message
      ThreadStore threadStore = getThreadStore();
      boolean reload = !thread.isPersistent();
      threadStore.saveThread(thread);
      updateThreads(reload);

      Assistant assistant = assistantBean.getAssistant();

      assistant.generate(getMessages(), new ChatMessageListener()
      {
        boolean started = false;

        @Override
        public void onNext(String tokens)
        {
          if (!started)
          {
            queue.push(1);
            started = true;
          }
          if (!StringUtils.isEmpty(tokens))
          {
            queue.push(tokens);
          }
        }

        @Override
        public void onMessage(ChatMessage message)
        {
          getMessages().add(message);
          pushMessage(queue, message, false);
        }

        @Override
        public String onExecute(ToolExecutionRequest toolRequest)
        {
          ToolExecutor executor = new ToolExecutor();
          executor.put("userId", userId);
          executor.put("threadId", threadId);
          return executor.execute(toolRequest);
        }

        @Override
        public void onComplete(FinishReason reason)
        {
          queue.push(0);
          try
          {
            threadStore.saveThread(thread);
          }
          catch (Exception ex)
          {
          }
        }

        @Override
        public void onError(Throwable t)
        {
          queue.push(t.toString());
          queue.push(0);
        }
      });
    }
    catch (Exception ex)
    {
      queue.push(0);
      error(ex);
    }
  }

  public String getAttachedFilename()
  {
    return attachedFilename;
  }

  public String getAttachedDocId()
  {
    return attachedDocId;
  }

  public void deleteAttachedFile()
  {
    try
    {
      if (attachedDocId != null)
      {
        assistantBean.getDocPort().removeDocument(attachedDocId, -4);
      }
      attachedFilename = null;
      attachedDocId = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void uploadFile(FileUploadEvent event)
  {
    UploadedFile fileToUpload = event.getFile();
    try
    {
      attachedFilename = fileToUpload.getFileName();
      int index = attachedFilename.lastIndexOf(".");
      String extension = index == -1 ? "bin" :
        attachedFilename.substring(index + 1);

      File attachedFile = java.io.File.createTempFile("attach", "." + extension);

      try (InputStream is = fileToUpload.getInputStream())
      {
        IOUtils.writeToFile(is, attachedFile);
      }

      Document document = new Document();
      document.setTitle(attachedFilename);
      document.setDocTypeId(ATTACHMENT_DOCTYPEID_PROPERTY);

      Property property = new Property();
      property.setName(ATTACHMENT_THREADID_PROPERTY);
      property.getValue().add("0");
      document.getProperty().add(property);

      String contentType = MimeTypeMap.getMimeTypeMap().getContentType(attachedFile);

      Content content = new Content();
      content.setData(new DataHandler(new FileDataSource(attachedFile)));
      content.setContentType(contentType);
      document.setContent(content);
      document = assistantBean.getDocPort().storeDocument(document);
      attachedDocId = document.getDocId();

      attachedFile.delete();
    }
    catch (IOException ex)
    {
      error(ex);
    }
  }

  public AssistantStore getAssistantStore()
  {
    Credentials credentials =
      UserSessionBean.getCurrentInstance().getCredentials();
    return AssistantStore.getInstance(credentials);
  }

  public ThreadStore getThreadStore()
  {
    String userId;
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    if (userSessionBean.isAnonymousUser() || userSessionBean.isAutoLoginUser())
    {
      userId = getExternalContext().getSessionId(true);
    }
    else
    {
      userId = userSessionBean.getUserId();
    }
    ThreadStore threadStore = ThreadStore.getInstance(userId);
    return threadStore;
  }

  private void pushMessage(StreamQueue queue, ChatMessage message,
    boolean listing)
  {
    if (message instanceof ToolExecutionResultMessage)
    {
      if (debugEnabled)
      {
        queue.push(ChatMessageAdapter.toMap(message));
      }
    }
    else if (message instanceof AiMessage)
    {
      AiMessage aiMessage = (AiMessage)message;
      if (aiMessage.hasToolExecutionRequests())
      {
        if (debugEnabled)
        {
          queue.push(ChatMessageAdapter.toMap(message));
        }
      }
      else if (listing)
      {
        queue.push(ChatMessageAdapter.toMap(message));
      }
    }
    else if (message instanceof UserMessage)
    {
      if (listing)
      {
        queue.push(ChatMessageAdapter.toMap(message));
      }
    }
  }
}
