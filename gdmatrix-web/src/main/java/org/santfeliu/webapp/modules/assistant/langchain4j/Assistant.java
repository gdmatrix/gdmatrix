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

import dev.langchain4j.data.message.ChatMessage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author realor
 */
public class Assistant implements Serializable
{
  public static final String OPENAI_PROVIDER = "openai";
  public static final String OLLAMA_PROVIDER = "ollama";

  private String assistantId;
  private String name;
  private String description;
  private String provider;
  private String modelName;
  private String baseUrl;
  private String apiKey;
  private boolean streaming;
  private Integer seed;
  private Double temperature;
  private Integer maxTokens;
  private String instructions;
  private String docId;
  List<String> toolNames = new ArrayList<>();
  List<String> docIds = new ArrayList<>();
  private String readRoleId;
  private String writeRoleId;
  private String creationUserId;
  private String changeUserId;

  public Assistant()
  {
    this(UUID.randomUUID().toString());
  }

  public Assistant(String assistantId)
  {
    this.assistantId = assistantId;
  }

  public String getAssistantId()
  {
    return assistantId;
  }

  public void setAssistantId(String assistantId)
  {
    this.assistantId = assistantId;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getProvider()
  {
    return provider;
  }

  public void setProvider(String provider)
  {
    this.provider = provider;
  }

  public String getModelName()
  {
    return modelName;
  }

  public void setModelName(String modelName)
  {
    this.modelName = modelName;
  }

  public String getDocId()
  {
    return docId;
  }

  public void setDocId(String docId)
  {
    this.docId = docId;
  }

  public String getBaseUrl()
  {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl)
  {
    this.baseUrl = baseUrl;
  }

  public String getApiKey()
  {
    return apiKey;
  }

  public void setApiKey(String apiKey)
  {
    this.apiKey = apiKey;
  }

  public String getInstructions()
  {
    return instructions;
  }

  public void setInstructions(String instructions)
  {
    this.instructions = instructions;
  }

  public boolean isStreaming()
  {
    return streaming;
  }

  public void setStreaming(boolean streaming)
  {
    this.streaming = streaming;
  }

  public Integer getSeed()
  {
    return seed;
  }

  public void setSeed(Integer seed)
  {
    this.seed = seed;
  }

  public Double getTemperature()
  {
    return temperature;
  }

  public void setTemperature(Double temperature)
  {
    this.temperature = temperature;
  }

  public Integer getMaxTokens()
  {
    return maxTokens;
  }

  public void setMaxTokens(Integer maxTokens)
  {
    this.maxTokens = maxTokens;
  }

  public List<String> getToolNames()
  {
    return toolNames;
  }

  public void setToolNames(List<String> toolNames)
  {
    this.toolNames = toolNames;
  }

  public List<String> getDocIds()
  {
    return docIds;
  }

  public void setDocIds(List<String> docIds)
  {
    this.docIds = docIds;
  }

  public boolean isPersistent()
  {
    return docId != null;
  }

  public String getReadRoleId()
  {
    return readRoleId;
  }

  public void setReadRoleId(String readRoleId)
  {
    this.readRoleId = readRoleId;
  }

  public String getWriteRoleId()
  {
    return writeRoleId;
  }

  public void setWriteRoleId(String writeRoleId)
  {
    this.writeRoleId = writeRoleId;
  }

  public String getCreationUserId()
  {
    return creationUserId;
  }

  public void setCreationUserId(String creationUserId)
  {
    this.creationUserId = creationUserId;
  }

  public String getChangeUserId()
  {
    return changeUserId;
  }

  public void setChangeUserId(String changeUserId)
  {
    this.changeUserId = changeUserId;
  }

  @Override
  public String toString()
  {
    StringBuilder buffer = new StringBuilder(getClass().getName());
    if (provider != null)
    {
      buffer.append("/").append(provider);
    }
    if (modelName != null)
    {
      buffer.append("/").append(modelName);
    }
    return buffer.toString();
  }

  public String getHash()
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append(provider)
      .append("|")
      .append(modelName)
      .append("|")
      .append(StringUtils.defaultIfBlank(baseUrl, "-"))
      .append("|")
      .append(StringUtils.defaultIfBlank(instructions, "-"))
      .append("|")
      .append(seed == null ? "-" : seed)
      .append("|")
      .append(maxTokens == null ? "-" : maxTokens)
      .append("|")
      .append(temperature == null ? "-" : temperature)
      .append("|")
      .append(streaming)
      .append("|")
      .append(toolNames)
      .append("|")
      .append(docIds);
    return buffer.toString();
  }

  public void generate(List<ChatMessage> messages, ChatMessageListener listener)
  {
    AssistantData.getInstance(this).generate(messages, listener);
  }
}
