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

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Scriptable;
import org.santfeliu.util.script.ScriptClient;

/**
 *
 * @author realor
 */
public class ToolStore
{
  public static final String ASSISTANT_TOOL_DEF_FUNCTION = "getFunctionDefinition";

  static final Map<String, ToolSpecification> toolSpecs = new HashMap<>();

  public static ToolStore getInstance()
  {
    return new ToolStore();
  }

  public ToolSpecification getToolSpecification(String toolName)
    throws Exception
  {
    return getToolSpecification(toolName, false);
  }

  public ToolSpecification getToolSpecification(String toolName, boolean refresh)
    throws Exception
  {
    ToolSpecification toolSpec = toolSpecs.get(toolName);
    if (toolSpec == null || refresh)
    {
      toolSpec = createToolSpecification(toolName);
      toolSpecs.put(toolName, toolSpec);
    }
    return toolSpec;
  }

  private ToolSpecification createToolSpecification(String toolName)
    throws Exception
  {
    ToolSpecification.Builder toolBuilder = ToolSpecification.builder();
    toolBuilder.name(toolName);

    ScriptClient scriptClient = new ScriptClient();
    scriptClient.refreshCache();
    scriptClient.executeScript(toolName);
    Scriptable scope = scriptClient.getScope();

    Object value = scriptClient.get(ASSISTANT_TOOL_DEF_FUNCTION);
    if (value instanceof Callable)
    {
      Callable fn = (Callable)value;
      Scriptable definition = (Scriptable)scriptClient.execute(fn);
      String description = ((String)definition.get("description", scope));
      toolBuilder.description(description);

      Scriptable parameters = (Scriptable)definition.get("parameters", scope);
      if (parameters != null)
      {
        JsonObjectSchema.Builder paramBuilder = JsonObjectSchema.builder();
        List<String> requiredParams = new ArrayList<>();

        for (Object id : parameters.getIds())
        {
          if (id instanceof String)
          {
            String paramName = (String)id;
            Scriptable scriptParam = (Scriptable)parameters.get(paramName, scope);
            String paramType = (String)scriptParam.get("type", scope);
            String paramDesc = (String)scriptParam.get("description", scope);
            boolean isRequired = Boolean.TRUE.equals(scriptParam.get("required", scope));

            if (paramType.equalsIgnoreCase("boolean"))
            {
              if (paramDesc == null)
              {
                paramBuilder.addBooleanProperty(paramName);
              }
              else
              {
                paramBuilder.addBooleanProperty(paramName, paramDesc);
              }
            }
            else if (paramType.equalsIgnoreCase("number"))
            {
              if (paramDesc == null)
              {
                paramBuilder.addNumberProperty(paramName);
              }
              else
              {
                paramBuilder.addNumberProperty(paramName, paramDesc);
              }
            }
            else if (paramType.equalsIgnoreCase("integer"))
            {
              if (paramDesc == null)
              {
                paramBuilder.addIntegerProperty(paramName);
              }
              else
              {
                paramBuilder.addIntegerProperty(paramName, paramDesc);
              }
            }
            else // string
            {
              if (paramDesc == null)
              {
                paramBuilder.addStringProperty(paramName);
              }
              else
              {
                paramBuilder.addStringProperty(paramName, paramDesc);
              }
            }

            if (isRequired)
            {
              requiredParams.add(paramName);
            }
          }
        }
        toolBuilder.parameters(paramBuilder.required(requiredParams).build());
      }
    }
    return toolBuilder.build();
  }
}
