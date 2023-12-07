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
package org.santfeliu.webapp.modules.assistant.openai;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

/**
 *
 * @author realor
 */
public class Run extends OpenAIObject
{
  String id;
  @SerializedName("created_at")
  long createdAt;
  @SerializedName("thread_id")
  String threadId;
  @SerializedName("assistant_id")
  String assistantId;
  String status;
  @SerializedName("required_action")
  RequiredAction requiredAction;
  @SerializedName("last_error")
  Error lastError;
  @SerializedName("expires_at")
  long expiresAt;
  @SerializedName("started_at")
  long startedAt;
  @SerializedName("cancelled_at")
  long cancelledAt;
  @SerializedName("failed_at")
  long failedAt;
  @SerializedName("completed_at")
  long completedAt;
  String model;
  String instructions;
  Object tools;
  @SerializedName("file_ids")
  List<String> fileIds;
  Map<String, Object> metadata;

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public long getCreatedAt()
  {
    return createdAt;
  }

  public void setCreatedAt(long createdAt)
  {
    this.createdAt = createdAt;
  }

  public String getThreadId()
  {
    return threadId;
  }

  public void setThreadId(String threadId)
  {
    this.threadId = threadId;
  }

  public String getAssistantId()
  {
    return assistantId;
  }

  public void setAssistantId(String assistantId)
  {
    this.assistantId = assistantId;
  }

  public String getStatus()
  {
    return status;
  }

  public void setStatus(String status)
  {
    this.status = status;
  }

  public RequiredAction getRequiredAction()
  {
    return requiredAction;
  }

  public void setRequiredAction(RequiredAction requiredAction)
  {
    this.requiredAction = requiredAction;
  }

  public Error getLastError()
  {
    return lastError;
  }

  public void setLastError(Error lastError)
  {
    this.lastError = lastError;
  }

  public long getExpiresAt()
  {
    return expiresAt;
  }

  public void setExpiresAt(long expiresAt)
  {
    this.expiresAt = expiresAt;
  }

  public long getStartedAt()
  {
    return startedAt;
  }

  public void setStartedAt(long startedAt)
  {
    this.startedAt = startedAt;
  }

  public long getCancelledAt()
  {
    return cancelledAt;
  }

  public void setCancelledAt(long cancelledAt)
  {
    this.cancelledAt = cancelledAt;
  }

  public long getFailedAt()
  {
    return failedAt;
  }

  public void setFailedAt(long failedAt)
  {
    this.failedAt = failedAt;
  }

  public long getCompletedAt()
  {
    return completedAt;
  }

  public void setCompletedAt(long completedAt)
  {
    this.completedAt = completedAt;
  }

  public String getModel()
  {
    return model;
  }

  public void setModel(String model)
  {
    this.model = model;
  }

  public String getInstructions()
  {
    return instructions;
  }

  public void setInstructions(String instructions)
  {
    this.instructions = instructions;
  }

  public Object getTools()
  {
    return tools;
  }

  public void setTools(Object tools)
  {
    this.tools = tools;
  }

  public List<String> getFileIds()
  {
    return fileIds;
  }

  public void setFileIds(List<String> fileIds)
  {
    this.fileIds = fileIds;
  }

  public Map<String, Object> getMetadata()
  {
    return metadata;
  }

  public void setMetadata(Map<String, Object> metadata)
  {
    this.metadata = metadata;
  }

  public boolean isPending()
  {
    return "queued".equals(status) || "in_progress".equals(status);
  }

  public boolean isRequiresAction()
  {
    return "requires_action".equals(status);
  }
}
