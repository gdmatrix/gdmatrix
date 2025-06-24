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
import java.util.logging.Level;
import org.apache.commons.lang.StringUtils;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.ConsString;
import org.santfeliu.util.script.ScriptClient;

/**
 *
 * @author realor
 */
public class ToolExecutor extends ScriptClient
{
  public String execute(ToolExecutionRequest request)
  {
    ClassLoader classLoader = getClass().getClassLoader();
    java.lang.Thread thread = java.lang.Thread.currentThread();
    thread.setContextClassLoader(classLoader);

    String functionName = request.name();
    String functionArgs = request.arguments();
    String resultText = null;
    try
    {
      executeScript(functionName);

      Object value = get(functionName);
      if (value instanceof Callable)
      {
        StringBuilder buffer = new StringBuilder();
        buffer.append(functionName);
        buffer.append("(");
        if (!StringUtils.isBlank(functionArgs))
        {
          buffer.append(functionArgs);
        }
        buffer.append(")");
        String cmd = buffer.toString();
        Object result = execute(cmd);
        resultText = String.valueOf(result);
      }
      if (resultText == null) resultText = "Function executed";
    }
    catch (Exception ex)
    {
      // hide error to assistant
      resultText = "Function not available";
    }
    LOGGER.log(Level.INFO, "Call function {0} with args {1} = {2}",
      new Object[]{ functionName, functionArgs, resultText });

    return resultText;
  }

  public String getAction()
  {
    Object value = get("action");
    if (value instanceof String) return (String)value;
    if (value instanceof ConsString) return value.toString();
    return null;
  }
}
