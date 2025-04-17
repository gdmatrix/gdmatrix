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

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static dev.langchain4j.data.message.ChatMessageType.AI;
import static dev.langchain4j.data.message.ChatMessageType.USER;
import static dev.langchain4j.data.message.ChatMessageType.TOOL_EXECUTION_RESULT;

/**
 *
 * @author realor
 */
public class ChatMessageAdapter
{
  public static Map<String, Object> toMap(ChatMessage message)
  {
    Map<String, Object> map = new HashMap<>();
    map.put("type", message.type().name());

    if (message instanceof AiMessage)
    {
      AiMessage aiMessage = (AiMessage)message;
      map.put("text", aiMessage.text());
      map.put("toolExecutionRequests", aiMessage.toolExecutionRequests());
    }
    else if (message instanceof UserMessage)
    {
      UserMessage userMessage = (UserMessage)message;
      map.put("text", userMessage.singleText());
    }
    else if (message instanceof ToolExecutionResultMessage)
    {
      ToolExecutionResultMessage resultMessage = (ToolExecutionResultMessage)message;
      map.put("id", resultMessage.id());
      map.put("toolName", resultMessage.toolName());
      map.put("text", resultMessage.text());
    }
    else if (message instanceof SystemMessage)
    {
      SystemMessage systemMessage = (SystemMessage)message;
      map.put("text", systemMessage.text());
    }
    return map;
  }

  public static ChatMessage fromMap(Map<String, Object> map)
  {
    String text = (String)map.get("text");
    String type = (String)map.get("type");
    if (USER.name().equals(type))
    {
      return UserMessage.from(text);
    }
    else if (AI.name().equals(type))
    {
      List<Map<String, String>> list =
        (List<Map<String, String>>)map.get("toolExecutionRequests");
      if (list != null)
      {
        List<ToolExecutionRequest> toolRequests = new ArrayList<>();
        for (Map<String, String> item : list)
        {
          ToolExecutionRequest toolRequest = ToolExecutionRequest.builder()
            .id(item.get("id"))
            .name(item.get("name"))
            .arguments(item.get("arguments"))
            .build();
          toolRequests.add(toolRequest);
        }
        return text == null ?
          AiMessage.from(toolRequests) :
          AiMessage.from(text, toolRequests);
      }
      else return AiMessage.from(text);
    }
    else if (TOOL_EXECUTION_RESULT.name().equals(type))
    {
      String id = (String)map.get("id");
      String toolName = (String)map.get("toolName");
      return ToolExecutionResultMessage.from(id, toolName, text);
    }
    else
    {
      return SystemMessage.from(text);
    }
  }
}
