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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author realor
 */
public class Assistant extends OpenAIObject
{
  String id;
  String name;
  String description;
  String model;
  String instructions;
  List<Tool> tools;
  @SerializedName("created_at")
  long createdAt;
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

  public List<Tool> getTools()
  {
    return tools;
  }

  public void setTools(List<Tool> tools)
  {
    this.tools = tools;
  }

  public long getCreatedAt()
  {
    return createdAt;
  }

  public void setCreatedAt(long createdAt)
  {
    this.createdAt = createdAt;
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

  public Object getMetadataValue(String name)
  {
    if (metadata == null) return null;
    return metadata.get(name);
  }

  public void setMetadataValue(String name, Object value)
  {
    if (metadata == null) metadata = new HashMap<>();
    metadata.put(name, value);
  }
}
