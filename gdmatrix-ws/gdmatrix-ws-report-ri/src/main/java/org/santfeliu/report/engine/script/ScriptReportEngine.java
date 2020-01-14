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
package org.santfeliu.report.engine.script;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import org.apache.commons.collections.LRUMap;
import org.matrix.report.ExportOptions;
import org.matrix.report.Parameter;
import org.matrix.report.ParameterDefinition;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptableObject;
import org.santfeliu.report.engine.ReportEngine;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.util.script.FunctionFactory;

/**
 *
 * @author realor
 */
public class ScriptReportEngine implements ReportEngine
{
  private final LRUMap cache = new LRUMap(10);

  public String getInfo()
  {
    return "ScriptReportEngine";
  }

  public List<ParameterDefinition> readReportParameters(String contentId,
    DataSource dataSource)
  {
    try
    {
      Script script = getScript(contentId, dataSource);
      Context cx = Context.enter();
      try
      {
        ScriptableObject scriptable = cx.initStandardObjects();
        FunctionFactory.initFunctions(scriptable);
        script.exec(cx, scriptable);
        if (scriptable.has("fillParameters", scriptable))
        {
          ScriptReportParameters parameters = new ScriptReportParameters();
          scriptable.put("parameters", scriptable, parameters);
          cx.evaluateString(scriptable, "fillParameters()", contentId, 1, null);
          return parameters.getParameterDefinitions();
        }
        else throw new RuntimeException("Missing method fillParameters");
      }
      finally
      {
        Context.exit();
      }
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  public DataHandler executeReport(String contentId, DataSource dataSource,
    String connectionName, List<Parameter> parameters,
    ExportOptions exportOptions, Credentials credentials)
  {
    try
    {
      Script script = getScript(contentId, dataSource);
      Context cx = Context.enter();
      try
      {
        ScriptableObject scriptable = cx.initStandardObjects();
        FunctionFactory.initFunctions(scriptable);
        script.exec(cx, scriptable);
        if (scriptable.has("fillReport", scriptable))
        {
          ScriptReport report = createScriptReport(exportOptions);
          for (Parameter parameter : parameters)
          {
            report.setParameter(parameter.getName(), parameter.getValue());
          }
          scriptable.put("report", scriptable, report);
          scriptable.put("credentials", scriptable, credentials);
          scriptable.put("connectionName", scriptable, connectionName);
          cx.evaluateString(scriptable, "fillReport()", contentId, 1, null);
          return new DataHandler(report.getData());
        }
        else throw new RuntimeException("Missing method fillReport");
      }
      finally
      {
        Context.exit();
      }
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  private Script getScript(String contentId, DataSource dataSource)
    throws Exception
  {
    Script script = (Script)cache.get(contentId);
    if (script == null)
    {
      InputStream is = dataSource.getInputStream();
      InputStreamReader reader = new InputStreamReader(is, "UTF-8");
      try
      {
        Context cx = Context.enter();
        try
        {
          script = cx.compileReader(reader, contentId, 0, null);
          cache.put(contentId, script);
        }
        finally
        {
          Context.exit();
        }
      }
      finally
      {
        reader.close();
      }
    }
    return script;
  }

  private ScriptReport createScriptReport(ExportOptions exportOptions)
  {
    ScriptReport report;
    if (exportOptions.getFormat().equalsIgnoreCase("pdf"))
    {
      report = new PdfScriptReport();
    }
    else
    {
      report = new HtmlScriptReport();
    }
    return report;
  }
}
