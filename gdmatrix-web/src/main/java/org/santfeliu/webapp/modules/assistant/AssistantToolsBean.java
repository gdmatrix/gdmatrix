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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.dic.Property;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Scriptable;
import org.primefaces.PrimeFaces;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.script.ScriptClient;
import org.santfeliu.web.WebBean;
import org.santfeliu.webapp.modules.assistant.openai.FunctionParameter;
import org.santfeliu.webapp.modules.assistant.openai.FunctionParameters;
import org.santfeliu.webapp.modules.assistant.openai.FunctionDefinition;
import org.santfeliu.webapp.modules.assistant.openai.OpenAI;
import org.santfeliu.webapp.modules.assistant.openai.Tool;
import static org.santfeliu.webapp.modules.assistant.openai.Tool.FUNCTION_TOOL;

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
  public static final String ASSISTANT_TOOL_DEF_FUNCTION = "getFunctionDefinition";

  Tool editingTool;
  Tool previousTool;

  transient OpenAI openAI = new OpenAI();

  @Inject
  AssistantBean assistantBean;

  @PostConstruct
  public void init()
  {
    String apiKey = MatrixConfig.getProperty("openai.apiKey");
    openAI.setApiKey(apiKey);
  }

  public Tool getEditingTool()
  {
    return editingTool;
  }

  public List<String> completeToolName(String name)
  {
    List<String> results = new ArrayList<>();
    try
    {
      DocumentFilter filter = new DocumentFilter();
      filter.setDocTypeId(ASSISTANT_TOOL_DOCTYPEID);
      Property property = new Property();
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

  public void loadFunction()
  {
    try
    {
      FunctionDefinition function = editingTool.getFunction();
      String functionName = function.getName();

      FunctionParameters functionParameters = new FunctionParameters();
      function.setParameters(functionParameters);
      Map<String, FunctionParameter> properties = new HashMap<>();
      List<String> requiredParameters = new ArrayList<>();
      functionParameters.setProperties(properties);
      functionParameters.setRequired(requiredParameters);

      ScriptClient scriptClient = new ScriptClient();
      scriptClient.refreshCache();
      scriptClient.executeScript(functionName);
      Scriptable scope = scriptClient.getScope();
      Object value = scriptClient.get(ASSISTANT_TOOL_DEF_FUNCTION);
      if (value instanceof Callable)
      {
        Callable fn = (Callable)value;
        Scriptable definition = (Scriptable)scriptClient.execute(fn);
        function.setDescription((String)definition.get("description", scope));
        Scriptable parameters = (Scriptable)definition.get("parameters", scope);
        if (parameters != null)
        {
          for (Object id : parameters.getIds())
          {
            if (id instanceof String)
            {
              String paramName = (String)id;
              Scriptable scriptParam = (Scriptable)parameters.get(paramName, scope);

              FunctionParameter parameter = new FunctionParameter();
              parameter.setType((String)scriptParam.get("type", scope));
              parameter.setDescription((String)scriptParam.get("description", scope));
              boolean required = Boolean.TRUE.equals(scriptParam.get("required", scope));
              properties.put(paramName, parameter);
              if (required)
              {
                requiredParameters.add(paramName);
              }
            }
          }
        }
      }
      else
      {
        function.setDescription(functionName);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void onBlur()
  {
    System.out.println("ON BLUR: " + editingTool.getFunction().getName());
    FunctionDefinition function = editingTool.getFunction();
    if (function.getName() == null)
    {
      function.setDescription(null);
      function.setParameters(null);
      PrimeFaces.current().ajax().update(
        "mainform:assistant_tabs:function_desc",
        "mainform:assistant_tabs:function_params");
    }
  }

  public List<String> getFunctionParameterNames()
  {
    FunctionDefinition function = editingTool.getFunction();
    if (function == null) return Collections.EMPTY_LIST;

    FunctionParameters parameters = function.getParameters();
    if (parameters == null) return Collections.EMPTY_LIST;

    Map<String, FunctionParameter> properties = parameters.getProperties();
    if (properties == null) return Collections.EMPTY_LIST;

    return new ArrayList<>(properties.keySet());
  }

  public FunctionParameter getFunctionParameter(String name)
  {
    return editingTool.getFunction().getParameters().getProperties().get(name);
  }

  public boolean isFunctionParameterRequired(String name)
  {
    return editingTool.getFunction().getParameters().getRequired().contains(name);
  }

  public void addTool()
  {
    previousTool = null;
    editingTool = new Tool();
    editingTool.setType(FUNCTION_TOOL);
    editingTool.setFunction(new FunctionDefinition());
    assistantBean.setDialogVisible(true);
  }

  public void editTool(Tool tool)
  {
    previousTool = tool;
    Gson gson = new Gson();
    String json = gson.toJson(tool);
    editingTool = gson.fromJson(json, Tool.class);
    assistantBean.setDialogVisible(true);
  }

  public void removeTool(Tool tool)
  {
    assistantBean.getAssistant().getTools().remove(tool);
  }

  public void acceptTool()
  {
    if (!FUNCTION_TOOL.equals(editingTool.getType()))
    {
      editingTool.setFunction(null);
    }

    List<Tool> tools = assistantBean.getAssistant().getTools();

    if (previousTool != null && !sameTool(previousTool, editingTool))
    {
      tools.remove(previousTool);
    }

    int i = 0;
    while (i < tools.size())
    {
      if (sameTool(tools.get(i), editingTool))
      {
        break;
      }
      i++;
    }
    if (i == tools.size())
    {
      tools.add(editingTool);
    }
    else
    {
      tools.set(i, editingTool);
    }
    editingTool = null;
    previousTool = null;
    assistantBean.setDialogVisible(false);
  }

  public void cancelTool()
  {
    editingTool = null;
    previousTool = null;
    assistantBean.setDialogVisible(false);
  }

  public boolean sameTool(Tool a, Tool b)
  {
    if (!a.getType().equals(b.getType())) return false;

    if (!FUNCTION_TOOL.equals(a.getType())) return true;

    return a.getFunction().getName().equals(b.getFunction().getName());
  }
}
