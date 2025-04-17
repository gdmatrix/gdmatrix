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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author realor
 */
public class Thread implements Serializable
{
  private String threadId;

  private String docId;

  private String description;

  private String dateTime;

  private String userId;

  private List<ChatMessage> messages = new ArrayList<>();

  public Thread()
  {
    this(UUID.randomUUID().toString());
  }

  public Thread(String threadId)
  {
    this.threadId = threadId;
  }

  public String getThreadId()
  {
    return threadId;
  }

  public void setThreadId(String threadId)
  {
    this.threadId = threadId;
  }

  public String getDateTime()
  {
    return dateTime;
  }

  public void setDateTime(String dateTime)
  {
    this.dateTime = dateTime;
  }

  public String getDocId()
  {
    return docId;
  }

  public void setDocId(String docId)
  {
    this.docId = docId;
  }

  public boolean isPersistent()
  {
    return docId != null;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getUserId()
  {
    return userId;
  }

  public void setUserId(String userId)
  {
    this.userId = userId;
  }

  public List<ChatMessage> getMessages()
  {
    return messages;
  }

  public void setMessages(List<ChatMessage> messages)
  {
    this.messages = messages;
  }

  public void fromJson(String jsonArray)
  {
    Gson gson = new Gson();
    List<Map<String, Object>> list =
      (List<Map<String, Object>>)gson.fromJson(jsonArray, List.class);

    messages.clear();
    for (Map<String, Object> map : list)
    {
      ChatMessage chatMessage = ChatMessageAdapter.fromMap(map);
      messages.add(chatMessage);
    }
  }

  public String toJson()
  {
    List<Map> list = new ArrayList<>();
    for (ChatMessage message : messages)
    {
      list.add(ChatMessageAdapter.toMap(message));
    }
    Gson gson  = new GsonBuilder()
      .disableHtmlEscaping()
      .setPrettyPrinting()
      .create();
    return gson.toJson(list);
  }
}
