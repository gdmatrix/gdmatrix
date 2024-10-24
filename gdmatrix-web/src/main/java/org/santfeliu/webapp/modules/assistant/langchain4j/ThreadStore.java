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
package org.santfeliu.webapp.modules.assistant.langchain4j;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.activation.DataHandler;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.Property;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import static org.matrix.doc.DocumentConstants.UNIVERSAL_LANGUAGE;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.DocumentManagerPort;
import org.matrix.doc.OrderByProperty;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.MemoryDataSource;
import org.santfeliu.webapp.modules.doc.DocModuleBean;

/**
 *
 * @author realor
 */
public class ThreadStore
{
  public static final String THREAD_TYPEID = "Thread";
  public static final int THREAD_DESCRIPTION_LENGTH = 80;
  private String userId;

  private DocumentManagerPort documentManagerPort;

  public static ThreadStore getInstance(String userId)
  {
    String adminUserId = MatrixConfig.getProperty("adminCredentials.userId");
    String adminPassword = MatrixConfig.getProperty("adminCredentials.password");

    ThreadStore threadStore = new ThreadStore();
    threadStore.documentManagerPort = DocModuleBean.getPort(
      adminUserId, adminPassword);
    threadStore.userId = userId;
    return threadStore;
  }

  public String getUserId()
  {
    return userId;
  }

  public List<ThreadSummary> getThreads()
  {
    DocumentFilter filter = new DocumentFilter();
    filter.setDocTypeId(THREAD_TYPEID);
    Property property = new Property();
    property.setName("userId");
    property.getValue().add(userId);
    filter.getProperty().add(property);
    filter.getOutputProperty().add("threadId");
    filter.getOutputProperty().add("description");
    OrderByProperty orderBy = new OrderByProperty();
    orderBy.setName("captureDateTime");
    orderBy.setDescending(true);
    filter.getOrderByProperty().add(orderBy);

    List<ThreadSummary> threads = new ArrayList<>();
    List<Document> documents = getPort().findDocuments(filter);
    for (Document document : documents)
    {
      String threadId =
        DictionaryUtils.getPropertyValue(document.getProperty(), "threadId");
      String description =
        DictionaryUtils.getPropertyValue(document.getProperty(), "description");

      threads.add(new ThreadSummary(threadId, document.getCaptureDateTime(), description));
    }
    return threads;
  }

  public Thread loadThread(String threadId) throws Exception
  {
    Document document = findDocumentByThreadId(threadId);
    if (document == null) throw new Exception("Thread not found: " + threadId);

    String contentId = document.getContent().getContentId();
    Content content = getPort().loadContent(contentId);
    InputStream is = content.getData().getInputStream();
    String json = IOUtils.toString(is, "UTF-8");
    Gson gson = new Gson();
    Map map = gson.fromJson(json, Map.class);
    List<Map<String, Object>> messages = (List<Map<String, Object>>)map.get("messages");
    Thread thread = new Thread(threadId);
    thread.setDocId(document.getDocId());
    thread.setDateTime(document.getCaptureDateTime());
    String description =
      DictionaryUtils.getPropertyValue(document.getProperty(), "description");
    thread.setDescription(description);
    for (Map<String, Object> message : messages)
    {
      ChatMessage chatMessage = ChatMessageAdapter.fromMap(message);
      thread.getMessages().add(chatMessage);
    }
    return thread;
  }

  public Thread saveThread(Thread thread) throws Exception
  {
    boolean descriptionUpdated = updateDescription(thread);

    List<Map<String, Object>> messages = new ArrayList<>();
    Map<String, Object> map = new HashMap<>();
    map.put("threadId", thread.getThreadId());
    map.put("dateTime", thread.getDateTime());
    map.put("description", thread.getDescription());
    map.put("messages", messages);
    for (ChatMessage message : thread.getMessages())
    {
      messages.add(ChatMessageAdapter.toMap(message));
    }
    Gson gson  = new GsonBuilder().disableHtmlEscaping().create();
    String json = gson.toJson(map);

    Document document = new Document();
    document.setDocId(thread.getDocId());
    document.setDocTypeId(THREAD_TYPEID);
    document.setTitle("Assistant thread: " + thread.getThreadId());

    if (thread.getDocId() == null)
    {
      Property property = new Property();
      property.setName("threadId");
      property.getValue().add(thread.getThreadId());
      document.getProperty().add(property);
    }

    if (userId != null)
    {
      Property property = new Property();
      property.setName("userId");
      property.getValue().add(userId);
      document.getProperty().add(property);
    }

    if (descriptionUpdated)
    {
      Property property = new Property();
      property.setName("description");
      property.getValue().add(thread.getDescription());
      document.getProperty().add(property);
    }

    document.setIncremental(true);
    MemoryDataSource ds =
      new MemoryDataSource(json.getBytes("UTF-8"), "thread", "application/json");
    DataHandler dh = new DataHandler(ds);
    Content content = new Content();
    content.setData(dh);
    content.setContentType("application/json");
    content.setLanguage(UNIVERSAL_LANGUAGE);
    document.setContent(content);
    document = getPort().storeDocument(document);
    thread.setDocId(document.getDocId());

    return thread;
  }

  public void deleteThread(String threadId)
  {
    Document document = findDocumentByThreadId(threadId);
    if (document != null)
    {
      getPort().removeDocument(document.getDocId(), -4);
    }
  }

  public DocumentManagerPort getPort()
  {
    if (documentManagerPort == null)
    {
      documentManagerPort = DocModuleBean.getPort("anonymous", null);
    }
    return documentManagerPort;
  }

  private Document findDocumentByThreadId(String threadId)
  {
    DocumentFilter filter = new DocumentFilter();
    filter.setDocTypeId(THREAD_TYPEID);
    Property property = new Property();
    property.setName("threadId");
    property.getValue().add(threadId);
    filter.getProperty().add(property);

    DocumentManagerPort port = getPort();
    List<Document> documents = port.findDocuments(filter);
    if (documents.isEmpty()) return null;

    return documents.get(0);
  }

  private boolean updateDescription(Thread thread)
  {
    String description = thread.getDescription();
    if (description != null && description.length() > THREAD_DESCRIPTION_LENGTH)
      return false;

    StringBuilder buffer = new StringBuilder();
    List<ChatMessage> messages = thread.getMessages();
    for (ChatMessage message : messages)
    {
      if (message instanceof UserMessage)
      {
        UserMessage userMessage = (UserMessage)message;
        if (buffer.length() > 0) buffer.append(", ");
        buffer.append(userMessage.singleText());
        if (buffer.length() > THREAD_DESCRIPTION_LENGTH)
        {
          buffer.setLength(THREAD_DESCRIPTION_LENGTH);
          buffer.append("...");
          break;
        }
      }
    }

    thread.setDescription(buffer.toString());

    return true;
  }
}
