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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
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
import org.santfeliu.webapp.modules.assistant.openai.Message;
import org.santfeliu.webapp.modules.assistant.openai.Run;
import static org.santfeliu.webapp.modules.assistant.openai.Message.ASSISTANT_ROLE;
import static org.santfeliu.webapp.modules.assistant.openai.Message.USER_ROLE;
import static org.apache.commons.lang.StringUtils.isBlank;
import org.matrix.dic.Property;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.santfeliu.util.FileDataSource;
import org.santfeliu.util.IOUtils;
import org.santfeliu.util.MimeTypeMap;
import org.santfeliu.util.markdown.MarkdownToHtml;
import org.santfeliu.webapp.modules.assistant.openai.ContentItem;
import org.santfeliu.webapp.modules.assistant.openai.Text;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class ThreadsBean extends WebBean
  implements Serializable, FunctionExecutor
{
  public static final String TEXT_PLACEHOLDER_PROPERTY = "textPlaceholder";
  public static final String ATTACH_PLACEHOLDER_PROPERTY = "attachPlaceholder";

  public static final String ATTACHMENT_DOCTYPEID_PROPERTY = "Document";
  public static final String ATTACHMENT_THREADID_PROPERTY = "threadId";

  String threadId;
  Thread thread;
  String text;
  List<Thread> threads;
  MessageList messageList;
  String runId;
  boolean debugEnabled = false;
  boolean assistEnabled = true;
  String attachedFilename;
  File attachedFile;

  transient OpenAI openAI = new OpenAI();
  transient MarkdownToHtml md2html = new MarkdownToHtml();
  transient SimpleDateFormat dateFormat;
  transient SimpleDateFormat timeFormat;

  @Inject
  AssistantBean assistantBean;

  @PostConstruct
  public void init()
  {
    String apiKey = MatrixConfig.getProperty("openai.apiKey");
    openAI.setApiKey(apiKey);
    openAI.setFunctionExecutor(this);

    Locale locale = getFacesContext().getViewRoot().getLocale();
    dateFormat = new SimpleDateFormat("EEE, dd/MM/yyyy", locale);
    timeFormat = new SimpleDateFormat("HH:mm:ss", locale);
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
      messageList = null;
      if (StringUtils.isBlank(threadId))
      {
        this.threadId = null;
        thread = null;
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

  public String formatMessageDate(Message message, int index)
  {
    Date date = message.getCreationDate();
    String formattedTime = timeFormat.format(date);

    if (index == 0)
    {
      String formattedDate = dateFormat.format(date);
      return formattedDate + " " + formattedTime;
    }
    else
    {
      Message prevMessage = messageList.getData().get(index - 1);
      if (message.getCreatedAt() - prevMessage.getCreatedAt() > 300) // 5 min
      {
        String formattedDate = dateFormat.format(date);
        return formattedDate + " " + formattedTime;
      }
    }
    return formattedTime;
  }

  public String formatMessageContent(Message message)
  {
    List<ContentItem> content = message.getContent();
    if (content != null && !content.isEmpty())
    {
      Text msgText = content.get(0).getText();
      if (msgText != null)
      {
        String md = msgText.getValue();
        return md2html.transform(md);
      }
    }
    return null;
  }

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

      if (text.startsWith("#"))
      {
        String cmd = text;
        if (messageList == null) messageList = new MessageList();
        Message message = processCommand(cmd);
        messageList.getData().add(message);
      }
      else
      {
        if (attachedFile == null || !attachedFile.exists())
        {
          openAI.createMessage(threadId, "user", text);
          deleteAttachedFile();
        }
        else
        {
          String docId = storeAttachedFile();
          String attachText = text +
            "\n(docId: [" + docId + "](/documents/" + docId + "))";
          openAI.createMessage(threadId, "user", attachText);
        }
        updateMessages();
        if (assistEnabled)
        {
          PrimeFaces.current().executeScript("assistImmediately()");
        }
      }
      text = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public String getAttachedFilename()
  {
    return attachedFilename;
  }

  public void deleteAttachedFile()
  {
    attachedFilename = null;
    if (attachedFile != null)
    {
      attachedFile.delete();
    }
  }

  public void uploadFile(FileUploadEvent event)
  {
    UploadedFile fileToUpload = event.getFile();
    try
    {
      if (attachedFile != null)
      {
        attachedFile.delete();
        attachedFile = null;
      }

      attachedFilename = fileToUpload.getFileName();
      int index = attachedFilename.lastIndexOf(".");
      String extension = index == -1 ? "bin" :
        attachedFilename.substring(index + 1);

      attachedFile = java.io.File.createTempFile("attach", "." + extension);

      try (InputStream is = fileToUpload.getInputStream())
      {
        IOUtils.writeToFile(is, attachedFile);
      }
    }
    catch (IOException ex)
    {
      error(ex);
    }
  }

  public void assist()
  {
    try
    {
      Run run;

      if (runId == null)
      {
        String assistantId = assistantBean.getAssistantId();
        if (assistantId == null) return;

        run = openAI.createRun(threadId, assistantId);
        runId = run.getId();
      }
      else
      {
        run = openAI.retrieveRun(threadId, runId);
      }

      if (run.isPending())
      {
        PrimeFaces.current().executeScript("assistDelayed()");
      }
      else if (run.isRequiresAction())
      {
        openAI.executeRequiredAction(run);
        PrimeFaces.current().executeScript("assistImmediately()");
      }
      else // run completed or cancelled
      {
        runId = null;
      }
      updateMessages();
    }
    catch (Exception ex)
    {
      error(ex);
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
      resultText = executeFunction(functionName, functionArgs);
      if (resultText == null) resultText = "Function executed";
    }
    catch (Exception ex)
    {
      // hide error to assistant
      resultText = "Function not available";
    }

    if (debugEnabled)
    {
      if (functionArgs == null) functionArgs = "";
      Message message = Message.create(ASSISTANT_ROLE,
        "@" + functionName + " " + functionArgs + "\n" + resultText);
      messageList.getData().add(message);
    }
    return resultText;
  }

  public ThreadStore getThreadStore()
  {
    ThreadStore threadStore = CDI.current().select(ThreadStore.class).get();
    String userId = UserSessionBean.getCurrentInstance().getUserId();
    threadStore.setUserId(userId);
    return threadStore;
  }

  private String storeAttachedFile() throws Exception
  {
    Document document = new Document();
    document.setTitle(attachedFilename);
    document.setDocTypeId(ATTACHMENT_DOCTYPEID_PROPERTY);

    Property property = new Property();
    property.setName(ATTACHMENT_THREADID_PROPERTY);
    property.getValue().add(threadId);
    document.getProperty().add(property);

    String contentType = MimeTypeMap.getMimeTypeMap().getContentType(attachedFile);

    Content content = new Content();
    content.setData(new DataHandler(new FileDataSource(attachedFile)));
    content.setContentType(contentType);
    document.setContent(content);
    document = assistantBean.getDocPort().storeDocument(document);

    deleteAttachedFile();

    return document.getDocId();
  }

  private Message processCommand(String cmd)
  {
    String cmdName;
    String cmdArgs;
    int index = cmd.indexOf(" ");
    if (index == -1)
    {
      cmdName = cmd.trim();
      cmdArgs = null;
    }
    else
    {
      cmdName = cmd.substring(0, index).trim();
      cmdArgs = cmd.substring(index).trim();
    }

    switch (cmdName)
    {
      case "#debug":
      {
        if (cmdArgs != null)
        {
          if ("on".equals(cmdArgs)) debugEnabled = true;
          else if ("off".equals(cmdArgs)) debugEnabled = false;
        }
        String stateMessage = debugEnabled ?
          "Debug mode is enabled." : "Debug mode is disabled.";

        return Message.create(USER_ROLE, cmd + "\n" + stateMessage);
      }
      case "#assist":
      {
        if (cmdArgs != null)
        {
          if ("on".equals(cmdArgs)) assistEnabled = true;
          else if ("off".equals(cmdArgs)) assistEnabled = false;
        }
        String stateMessage = assistEnabled ?
          "Assist mode is enabled." : "Assist mode is disabled.";

        return Message.create(USER_ROLE, cmd + "\n" + stateMessage);
      }
      case "#call":
      {
        String result;
        if (cmdArgs != null)
        {
          String functionName;
          String functionArgs;
          index = cmdArgs.indexOf(" ");
          if (index == -1)
          {
            functionName = cmdArgs.trim();
            functionArgs = null;
          }
          else
          {
            functionName = cmdArgs.substring(0, index).trim();
            functionArgs = cmdArgs.substring(index).trim();
          }
          try
          {
            result = executeFunction(functionName, functionArgs);
          }
          catch (Exception ex)
          {
            result = ex.toString();
          }
        }
        else
        {
          result = "Function name not specified.";
        }
        return Message.create(USER_ROLE, cmd + "\n" + result);
      }
      case "#log":
      {
        Logger logger = Logger.getLogger("OpenAI");
        if (cmdArgs != null)
        {
          String level = cmdArgs;
          if ("info".equalsIgnoreCase(level)) logger.setLevel(Level.INFO);
          else if ("fine".equalsIgnoreCase(level)) logger.setLevel(Level.FINE);
          else if ("finer".equalsIgnoreCase(level)) logger.setLevel(Level.FINER);
        }
        String level = logger.getLevel() == null ?
          "default" : logger.getLevel().toString();
        String stateMessage = "Logging level is " + level;

        return Message.create(Message.USER_ROLE, cmd + "\n" + stateMessage);
      }
      case "#help":
      {
        return Message.create(Message.USER_ROLE, cmd +
          "\nSupported commands:\n " +
          "#debug [on|off]\n" +
          "#assist [on|off]\n" +
          "#call functionName [argumentsMap]\n" +
          "#log [info|fine|finer]\n" +
          "#help");
      }
      default:
        return Message.create(Message.USER_ROLE, cmd +
          "\nUnknown command. Type #help to know what commands are supported.");
    }
  }

  private String executeFunction(String functionName, String functionArgs)
    throws Exception
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
      Object result = scriptClient.execute(cmd);
      return String.valueOf(result);
    }
    return null;
  }

  private void updateMessages() throws Exception
  {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("limit", 100);
    parameters.put("order", "desc");

    if (messageList == null || messageList.getData().isEmpty())
    {
      messageList = openAI.listMessages(threadId, parameters);
      messageList.reverse();
    }
    else
    {
      if (messageList.getLastId() != null)
      {
        parameters.put("before", messageList.getLastId());
      }
      MessageList newMessages = openAI.listMessages(threadId, parameters);
      if (!newMessages.getData().isEmpty())
      {
        newMessages.reverse();
        for (Message newMessage : newMessages.getData())
        {
          if (newMessage.isCompleted())
          {
            messageList.getData().add(newMessage);
            messageList.setLastId(newMessage.getId());
          }
        }
      }
    }
  }
}
