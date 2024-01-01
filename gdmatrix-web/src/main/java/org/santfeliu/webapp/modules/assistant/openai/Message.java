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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author realor
 */
public class Message extends OpenAIObject
{
  public static final String USER_ROLE = "user";
  public static final String ASSISTANT_ROLE = "assistant";
  
  String id;
  @SerializedName("created_at")
  long createdAt;
  @SerializedName("thread_id")
  String threadId;
  String role;
  List<ContentItem> content;
  @SerializedName("file_ids")
  List<String> fileIds;
  @SerializedName("assistant_id")
  String assistantId;
  @SerializedName("run_id")
  String runId;
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

  public String getRole()
  {
    return role;
  }

  public void setRole(String role)
  {
    this.role = role;
  }

  public List<ContentItem> getContent()
  {
    return content;
  }

  public void setContent(List<ContentItem> content)
  {
    this.content = content;
  }

  public List<String> getFileIds()
  {
    return fileIds;
  }

  public void setFileIds(List<String> fileIds)
  {
    this.fileIds = fileIds;
  }

  public String getAssistantId()
  {
    return assistantId;
  }

  public void setAssistantId(String assistantId)
  {
    this.assistantId = assistantId;
  }

  public String getRunId()
  {
    return runId;
  }

  public void setRunId(String runId)
  {
    this.runId = runId;
  }

  public Map<String, Object> getMetadata()
  {
    return metadata;
  }

  public void setMetadata(Map<String, Object> metadata)
  {
    this.metadata = metadata;
  }

  public static Message create(String role, String text)
  {
    Message message = new Message();
    message.setRole(role);
    List<ContentItem> content = new ArrayList<>();
    message.setContent(content);
    ContentItem item = new ContentItem();
    item.setType(ContentItem.TEXT_TYPE);
    Text itemText = new Text();
    itemText.setValue(text);
    item.setText(itemText);
    content.add(item);
    return message;
  }

  public boolean isCompleted()
  {
    if (content.isEmpty()) return false;

    for (ContentItem item : content)
    {
      if (item.isTextType())
      {
        if (StringUtils.isBlank(item.getText().getValue())) return false;
      }
      else if (item.isImageFileType())
      {
        if (StringUtils.isBlank(item.getImageFile().getFileId())) return false;
      }
    }
    return true;
  }
}
