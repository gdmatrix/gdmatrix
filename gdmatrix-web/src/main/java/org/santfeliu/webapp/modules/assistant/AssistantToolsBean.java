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

import com.google.gson.Gson;
import dev.langchain4j.agent.tool.ToolParameters;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonBooleanSchema;
import dev.langchain4j.model.chat.request.json.JsonIntegerSchema;
import dev.langchain4j.model.chat.request.json.JsonNumberSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;
import dev.langchain4j.model.chat.request.json.JsonStringSchema;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.Property;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.web.WebBean;
import org.santfeliu.webapp.modules.assistant.langchain4j.ToolStore;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class AssistantToolsBean extends WebBean implements Serializable
{
  public static final String ASSISTANT_TOOL_DOCTYPEID = "CODE";
  public static final String ASSISTANT_TOOL_NAME_PROPERTY = "workflow.js";
  public static final String ASSISTANT_TOOL_PURPOSE_PROPERTY = "purpose";
  public static final String ASSISTANT_TOOL_PURPOSE_VALUE = "assistant";
  public static final String ASSISTANT_TOOL_DEF_FUNCTION = "getFunctionDefinition";

  private String editingToolName;
  ToolSpecification toolSpecification;

  @Inject
  AssistantBean assistantBean;

  public String getEditingToolName()
  {
    return editingToolName;
  }

  public void setEditingToolName(String toolName)
  {
    editingToolName = toolName;
  }

  public ToolSpecification getToolSpecification()
  {
    return toolSpecification;
  }

  public String getToolDescription(String toolName)
  {
    try
    {
      return ToolStore.getInstance().getToolSpecification(toolName).description();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public List<String> completeToolName(String name)
  {
    List<String> results = new ArrayList<>();
    try
    {
      DocumentFilter filter = new DocumentFilter();
      filter.setDocTypeId(ASSISTANT_TOOL_DOCTYPEID);

      Property property = new Property();
      property.setName(ASSISTANT_TOOL_PURPOSE_PROPERTY);
      property.getValue().add(ASSISTANT_TOOL_PURPOSE_VALUE);
      filter.getProperty().add(property);

      property = new Property();
      property.setName(ASSISTANT_TOOL_NAME_PROPERTY);
      property.getValue().add("%" + name + "%");
      filter.getProperty().add(property);

      filter.getOutputProperty().add(ASSISTANT_TOOL_NAME_PROPERTY);
      List<Document> documents =
        assistantBean.getDocPort().findDocuments(filter);
      for (Document document : documents)
      {
        String value = DictionaryUtils.getPropertyValue(document.getProperty(),
          ASSISTANT_TOOL_NAME_PROPERTY);
        results.add(value);
      }
    }
    catch (Exception ex)
    {
      // ignore
    }
    return results;
  }

  public void loadToolSpecification()
  {
    try
    {
      toolSpecification =
        ToolStore.getInstance().getToolSpecification(editingToolName, true);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public boolean isParameterRequired(String name)
  {
    if (toolSpecification == null) return false;
    return toolSpecification.parameters().required().contains(name);
  }

  public String getParameterType(String name)
  {
    if (toolSpecification == null) return null;
    JsonSchemaElement schema = toolSpecification.parameters().properties().get(name);
    if (schema instanceof JsonStringSchema) return "string";
    else if (schema instanceof JsonNumberSchema) return "number";
    else if (schema instanceof JsonIntegerSchema) return "integer";
    else if (schema instanceof JsonBooleanSchema) return "boolean";
    return String.valueOf(schema);
  }

  public String getParameterDescription(String name)
  {
    if (toolSpecification == null) return null;
    JsonSchemaElement schema = toolSpecification.parameters().properties().get(name);
    try
    {
      return (String)schema.getClass().getMethod("description").invoke(schema);
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  public void addTool()
  {
    this.editingToolName = "";
    toolSpecification = null;
    assistantBean.setDialogVisible(true);
  }

  public void editTool(String toolName)
  {
    try
    {
      toolSpecification =
        ToolStore.getInstance().getToolSpecification(toolName, true);
      this.editingToolName = toolName;
      assistantBean.setDialogVisible(true);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void removeTool(String toolName)
  {
    assistantBean.getAssistant().getToolNames().remove(toolName);
  }

  public void acceptTool()
  {
    if (!StringUtils.isBlank(editingToolName))
    {
      editingToolName = editingToolName.trim();
      List<String> toolNames = assistantBean.getAssistant().getToolNames();
      if (!toolNames.contains(editingToolName))
      {
        toolNames.add(editingToolName);
      }
    }
    editingToolName = null;
    assistantBean.setDialogVisible(false);
  }

  public void cancelTool()
  {
    editingToolName = null;
    assistantBean.setDialogVisible(false);
  }
}
