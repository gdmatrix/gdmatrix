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

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.util.MatrixConfig;
import static org.santfeliu.webapp.modules.assistant.langchain4j.Assistant.OLLAMA_PROVIDER;
import static org.santfeliu.webapp.modules.assistant.langchain4j.Assistant.OPENAI_PROVIDER;

/**
 *
 * @author realor
 */
public class AssistantData
{
  private static final String CONFIG_PREFIX = "config:";
  private static final long PURGE_TIME = 10 * 60 * 1000; // 10 minutes
  private static final Map<String, AssistantData> dataCache = new HashMap<>();
  private static long lastPurge;

  private ChatLanguageModel model;
  private StreamingChatLanguageModel streamingModel;
  private final List<String> toolNames;
  private final String instructions;
  private long lastAccess;

  public static synchronized AssistantData getInstance(Assistant assistant)
  {
    String hash = assistant.getHash();
    System.out.println("GET " + hash);
    AssistantData assistantData = dataCache.get(hash);
    if (assistantData == null)
    {
      assistantData = new AssistantData(assistant);
      dataCache.put(hash, assistantData);
    }
    assistantData.lastAccess = System.currentTimeMillis();

    purge();

    return assistantData;
  }

  public static synchronized void purge()
  {
    long now = System.currentTimeMillis();
    if (now - lastPurge > PURGE_TIME)
    {
      lastPurge = now;
      dataCache.values().removeIf(data -> now - data.lastAccess > PURGE_TIME);
    }
  }

  public static synchronized void clear()
  {
    dataCache.clear();
  }

  public AssistantData(Assistant assistant)
  {
    if (OPENAI_PROVIDER.equals(assistant.getProvider()))
    {
      if (assistant.isStreaming())
      {
        streamingModel = OpenAiStreamingChatModel.builder()
          .apiKey(getApiKey(assistant.getApiKey()))
          .modelName(assistant.getModelName())
          .temperature(assistant.getTemperature())
          .seed(assistant.getSeed())
          .maxTokens(assistant.getMaxTokens())
          .build();
      }
      else
      {
        model = OpenAiChatModel.builder()
          .apiKey(getApiKey(assistant.getApiKey()))
          .modelName(assistant.getModelName())
          .temperature(assistant.getTemperature())
          .seed(assistant.getSeed())
          .maxTokens(assistant.getMaxTokens())
          .build();
      }
    }
    else if (OLLAMA_PROVIDER.equals(assistant.getProvider()))
    {
      if (assistant.isStreaming())
      {
        streamingModel = OllamaStreamingChatModel.builder()
          .baseUrl(assistant.getBaseUrl())
          .modelName(assistant.getModelName())
          .temperature(assistant.getTemperature())
          .seed(assistant.getSeed())
          .build();
      }
      else
      {
        model = OllamaChatModel.builder()
          .baseUrl(assistant.getBaseUrl())
          .modelName(assistant.getModelName())
          .temperature(assistant.getTemperature())
          .seed(assistant.getSeed())
          .build();
      }
    }
    else throw new RuntimeException("Invalid provider");

    toolNames = new ArrayList<>(assistant.getToolNames());
    instructions = assistant.getInstructions();
  }

  public void generate(List<ChatMessage> messages, ChatMessageListener listener)
  {
    Generator generator = new Generator(messages, listener);
    generator.start();
  }

  private String getApiKey(String apiKey)
  {
    if (apiKey != null)
    {
      if (apiKey.startsWith(CONFIG_PREFIX))
      {
        String property = apiKey.substring(CONFIG_PREFIX.length());
        apiKey = MatrixConfig.getProperty(property);
      }
    }
    return apiKey;
  }

  class Generator implements StreamingChatResponseHandler
  {
    final List<ChatMessage> memory = new ArrayList<>();
    final List<ToolSpecification> tools = new ArrayList<>();
    final ChatMessageListener listener;

    public Generator(List<ChatMessage> messages, ChatMessageListener listener)
    {
      // setup memory
      if (!StringUtils.isBlank(instructions))
      {
        memory.add(SystemMessage.from(instructions));
      }
      memory.addAll(messages);
      this.listener = listener;
      loadTools();
    }

    public void start()
    {
      generate();
    }

    @Override
    public void onPartialResponse(String token)
    {
      listener.onNext(token);
    }

    @Override
    public void onCompleteResponse(ChatResponse chatResponse)
    {
      processChatResponse(chatResponse);
    }

    @Override
    public void onError(Throwable t)
    {
      listener.onError(t);
    }

    private void loadTools()
    {
      ToolStore toolStore = ToolStore.getInstance();
      for (String toolName : toolNames)
      {
        try
        {
          ToolSpecification tool = toolStore.getToolSpecification(toolName);
          tools.add(tool);
        }
        catch (Exception ex)
        {
          // ignore, missing tool
        }
      }
    }

    private void generate()
    {
      if (model != null)
      {
        if (tools.isEmpty())
        {
          ChatResponse chatResponse = model.chat(memory);
          processChatResponse(chatResponse);
        }
        else
        {
          ChatRequestParameters parameters = ChatRequestParameters.builder()
            .toolSpecifications(tools)
            .build();

          ChatRequest chatRequest = new ChatRequest.Builder()
            .messages(memory)
            .parameters(parameters)
            .build();

          ChatResponse chatResponse = model.chat(chatRequest);
          processChatResponse(chatResponse);
        }
      }
      else // streaming
      {
        if (tools.isEmpty())
        {
          streamingModel.chat(memory, this);
        }
        else
        {
          ChatRequestParameters parameters = ChatRequestParameters.builder()
            .toolSpecifications(tools)
            .build();

          ChatRequest chatRequest = new ChatRequest.Builder()
            .messages(memory)
            .parameters(parameters)
            .build();

          streamingModel.chat(chatRequest, this);
        }
      }
    }

    private void processChatResponse(ChatResponse chatResponse)
    {
      AiMessage aiMessage = chatResponse.aiMessage();
      memory.add(aiMessage);
      listener.onMessage(aiMessage);

      if (aiMessage.hasToolExecutionRequests())
      {
        for (ToolExecutionRequest toolRequest : aiMessage.toolExecutionRequests())
        {
          String result = listener.onExecute(toolRequest);
          ToolExecutionResultMessage resultMessage =
            ToolExecutionResultMessage.from(toolRequest, result);
          memory.add(resultMessage);
          listener.onMessage(resultMessage);
        }
        generate();
      }
      else
      {
        if (model != null)
        {
          listener.onNext(aiMessage.text());
        }
        listener.onComplete(chatResponse.finishReason());
      }
    }
  }
}
