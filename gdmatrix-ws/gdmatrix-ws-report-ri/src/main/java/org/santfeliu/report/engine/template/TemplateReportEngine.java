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
package org.santfeliu.report.engine.template;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import org.apache.commons.collections.LRUMap;
import org.matrix.report.ExportOptions;
import org.matrix.report.Parameter;
import org.matrix.report.ParameterDefinition;
import org.matrix.report.ParameterType;
import org.santfeliu.report.engine.ReportEngine;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.util.MemoryDataSource;
import org.santfeliu.util.script.ScriptableBase;
import org.santfeliu.util.template.JSTemplate;

/**
 *
 * @author realor
 */
public class TemplateReportEngine implements ReportEngine
{
  private final LRUMap cache = new LRUMap(10);
  private static final String PARAMETER_PREFIX = "$";
  private static final String DEFAULT_ENCODING = "UTF-8";
  
  public String getInfo()
  {
    return "TemplateReportEngine";    
  }

  public List<ParameterDefinition> readReportParameters(String contentId, 
    DataSource dataSource)
  {
    List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
    try
    {
      JSTemplate template = getTemplate(contentId, dataSource);
      Set variables = template.getReferencedVariables();
      for (Object v : variables)
      {
        String name = v.toString();
        if (name.startsWith(PARAMETER_PREFIX))
        {
          name = name.substring(PARAMETER_PREFIX.length());
          ParameterDefinition parameter = new ParameterDefinition();
          parameter.setName(name);
          parameter.setDescription(name);
          parameter.setType(ParameterType.STRING);
          parameter.setForPrompting(true);
          parameters.add(parameter);
        }
      }
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
    return parameters;
  }

  public DataHandler executeReport(String contentId, DataSource dataSource, 
    String connectionName, List<Parameter> parameters, 
    ExportOptions exportOptions, Credentials credentials)
  {
    try
    {
      JSTemplate template = getTemplate(contentId, dataSource);
      HashMap variables = new HashMap();
      String charset = null;      
      for (Parameter parameter : parameters)
      {
        String name = parameter.getName();
        String value = parameter.getValue();
        variables.put(PARAMETER_PREFIX + name, value);
        if (name.equals("charset_encoding"))
        {
          charset = value;
        }
      }
      variables.put("exportOptions", exportOptions);
      variables.put("credentials", credentials);
      String result = template.merge(variables);
      byte bytes[] = charset == null ? 
        result.getBytes(DEFAULT_ENCODING) : result.getBytes(charset);
      return new DataHandler(
        new MemoryDataSource(bytes, "result", "text/html"));
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  private JSTemplate getTemplate(String contentId, DataSource dataSource)
    throws Exception
  {
    JSTemplate template = (JSTemplate)cache.get(contentId);
    if (template == null)
    {
      InputStream is = dataSource.getInputStream();
      InputStreamReader reader = new InputStreamReader(is, DEFAULT_ENCODING);
      template = JSTemplate.create(reader, ScriptableBase.class);
      cache.put(contentId, template);
    }
    return template;
  }
}
