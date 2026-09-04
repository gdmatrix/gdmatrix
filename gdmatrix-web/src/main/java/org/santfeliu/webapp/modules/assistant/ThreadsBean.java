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
import dev.langchain4j.model.chat.response.StreamingHandle;
import dev.langchain4j.model.output.FinishReason;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import javax.activation.DataHandler;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.matrix.dic.Property;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.santfeliu.cms.CMSCache;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.util.FileDataSource;
import org.santfeliu.util.IOUtils;
import org.santfeliu.util.MimeTypeMap;
import org.santfeliu.web.ApplicationBean;
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
  String threadFilter;
  int totalThreads = -1;
  boolean debugEnabled = false;
  boolean simulationEnabled = false;
  boolean editionEnabled = false;
  boolean infoEnabled = false;
  String attachedFilename;
  String attachedDocId;
  String attachedContentId;
  String json;

  @Inject
  AssistantBean assistantBean;

  public Thread getThread()
  {
    if (thread == null)
    {
      createThread();
    }
    return thread;
  }

  public String getThreadId()
  {
    return getThread().getThreadId();
  }

  public String getThreadLink()
  {
    String host = System.getProperty("host");
    if (host == null) host = "localhost";
    String contextPath = getContextPath();
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    return "https://" + host + contextPath + "/web/" +
      userSessionBean.getSelectedMid() + "?threadid=" + getThreadId();
  }

  public String getThreadFilter()
  {
    return threadFilter;
  }

  public void setThreadFilter(String threadFilter)
  {
    this.threadFilter = threadFilter;
  }

  public List<ChatMessage> getMessages()
  {
    return getThread().getMessages();
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

  public boolean isSimulationEnabled()
  {
    return simulationEnabled;
  }

  public void setSimulationEnabled(boolean simulationEnabled)
  {
    this.simulationEnabled = simulationEnabled;
  }

  public boolean isEditionEnabled()
  {
    return editionEnabled;
  }

  public void setEditionEnabled(boolean editionEnabled)
  {
    this.editionEnabled = editionEnabled;
  }

  public boolean isInfoEnabled()
  {
    return infoEnabled;
  }

  public void setInfoEnabled(boolean infoEnabled)
  {
    this.infoEnabled = infoEnabled;
  }

  public String getText()
  {
    return text;
  }

  public void setText(String text)
  {
    this.text = text;
  }

  public String getJson()
  {
    if (json == null)
    {
      json = thread.toJson();
    }
    return json;
  }

  public void setJson(String json)
  {
    this.json = json;
  }

  public void endEdition()
  {
    try
    {
      thread.fromJson(json);
      getThreadStore().saveThread(thread);
      editionEnabled = false;
      json = null;
      repaintThread();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void cancelEdition()
  {
    editionEnabled = false;
    json = null;
    repaintThread();
  }

  public void changeThread(String threadId)
  {
    try
    {
      interruptStreaming();      
      thread = getThreadStore().loadThread(threadId);
      repaintThread();
      editionEnabled = false;
      json = null;
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
    queue.push(1); // clear message list
    for (ChatMessage message : thread.getMessages())
    {
      pushMessage(queue, message, true);
    }
    queue.push(0);
    PrimeFaces.current().executeScript("showResponse('" + threadId + "')");
  }
  
  public int getTotalThreads()
  {
    if (totalThreads == -1)
    {
      updateThreads(true);
    }    
    return totalThreads;
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
  
  public void resetThreadFilter()
  {
    this.threadFilter = null;
    updateThreads(true);
  }
  
  public void saveThreadInfo()
  {
    try
    {
      infoEnabled = false;
      thread.setEdited(true);
      getThreadStore().saveThread(thread, false);
      updateThreads(true);
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
      ThreadStore store = getThreadStore();
      threads = store.findThreads(threadFilter);
      if (StringUtils.isBlank(threadFilter))
      {
        totalThreads = threads.size();
      }
      else
      {
        totalThreads = store.countThreads();
      }
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
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    CMSCache cmsCache = ApplicationBean.getCurrentInstance().getCmsCache();
    String userId = userSessionBean.getUserId();
    String mid = userSessionBean.getSelectedMid();
    String threadId = getThreadId();
    attachedFilename = null;
    attachedDocId = null;
    attachedContentId = null;

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
        @Override
        public void onNext(String tokens, StreamingHandle handle)
        {
          if (queue.isInterrupted())
          {
            System.out.println(queue + " interrupted.");
            if (handle != null) handle.cancel();
          }
          else if (!StringUtils.isEmpty(tokens))
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
          executor.put("userSessionBean", userSessionBean);
          executor.put("cmsCache", cmsCache);
          executor.put("userId", userId);
          executor.put("mid", mid);
          executor.put("threadId", threadId);
          executor.put("simulation", simulationEnabled);
          String result = executor.execute(toolRequest);
          String action = executor.getAction();
          if (action != null)
          {
            pushAction(queue, action);
          }
          return result;
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
          queue.clear();
          pushError(queue, t);
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

  public void interruptStreaming()
  {
    StreamQueue queue = StreamQueue.getInstance(getThreadId(), false);
    if (queue != null)
    {
      queue.push(0);      
      queue.interrupt();
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

  public String getAttachedContentId()
  {
    return attachedContentId;
  }

  public boolean isAttachedImage()
  {
    if (attachedFilename == null) return false;
    return attachedFilename.endsWith(".png") ||
           attachedFilename.endsWith(".jpg");
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
      attachedContentId = null;
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
      createDocument(attachedFile);
    }
    catch (IOException ex)
    {
      error(ex);
    }
  }

  public void uploadImage()
  {
    String base64 = FacesContext.getCurrentInstance().getExternalContext()
      .getRequestParameterMap().get("image");
    if (base64 != null)
    {
      try
      {
        int index = base64.indexOf(",");
        if (index != -1)
        {
          String prefix = base64.substring(0, index).toLowerCase();
          String extension = ".png";
          if (prefix.contains("jpg") || prefix.contains("jpeg"))
          {
            extension = ".jpg";
          }
          attachedFilename = "image" + extension;

          byte[] data = Base64.getDecoder().decode(base64.substring(index + 1));
          File attachedFile = java.io.File.createTempFile("attach", extension);
          IOUtils.writeToFile(new ByteArrayInputStream(data), attachedFile);
          createDocument(attachedFile);
        }
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
  }

  public void createDocument(File attachedFile)
  {
    Document document = new Document();
    document.setTitle(attachedFilename);
    document.setDocTypeId(ATTACHMENT_DOCTYPEID_PROPERTY);

    Property property = new Property();
    property.setName(ATTACHMENT_THREADID_PROPERTY);
    property.getValue().add(getThreadId());
    document.getProperty().add(property);

    String contentType = MimeTypeMap.getMimeTypeMap().getContentType(attachedFile);

    Content content = new Content();
    content.setData(new DataHandler(new FileDataSource(attachedFile)));
    content.setContentType(contentType);
    document.setContent(content);
    document = assistantBean.getDocPort().storeDocument(document);
    attachedDocId = document.getDocId();
    attachedContentId = document.getContent().getContentId();
    attachedFile.delete();
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
        queue.push(ChatMessageAdapter.toMap(message, debugEnabled));
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

  private void pushAction(StreamQueue queue, String action)
  {
    queue.push(Map.of("type", "ACTION", "text", action));
  }

  private void pushError(StreamQueue queue, Throwable t)
  {
    Throwable cause = t.getCause();
    if (cause != null)
    {
      t = cause;
    }
    queue.push(Map.of("type", "ERROR", "text", t.toString()));
  }
}
