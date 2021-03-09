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
package org.santfeliu.web.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.matrix.dic.Property;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.util.IOUtils;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.script.WebScriptableBase;

/**
 *
 * @author realor
 */
public class ScriptServlet extends HttpServlet
{
  public static final String EXECUTE_PARAMETER  = "execute";
  public static final String REST_SERVLET_PATH  = "/rest";

  public static final String SCRIPT_DOCUMENT_TYPE  = "CODE";
  public static final String SCRIPT_PROPERTY_NAME  = "workflow.js";

  public void process(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    boolean toExecute = mustExecute(request);
    Script script = getScript(request, toExecute);
    if (script == null)
    {
      printInfo(response);
    }
    else
    {
      if (toExecute)// execute script
      {
        Object output = null;
        if ("text/javascript".equals(script.contentType) ||
            "application/x-javascript".equals(script.contentType) ||
            "application/javascript".equals(script.contentType))
        {
          output = executeJavaScript(script, request, response);
          if (output != null) writeOutput(output, response);
        }
        else
        {
          response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
        }
      }
      else 
      {
        // send script
        writeScript(script, response);
      }
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    process(request, response);
  }

  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    process(request, response);
  }
  
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    process(request, response);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    process(request, response);
  }
  
  public boolean mustExecute(HttpServletRequest request)
  {
    return (request.getServletPath().equals(REST_SERVLET_PATH)) ||
      request.getParameter(EXECUTE_PARAMETER) != null;
  }

  private void printInfo(HttpServletResponse response) throws IOException
  {
    response.setContentType("text/html");
    response.getWriter().println("<html><body>ScriptServlet v1.0</body></html>");
  }

  private Script getScript(HttpServletRequest request, boolean toExecute)
  {
    String uri = request.getRequestURI().substring(1);
    int index = uri.indexOf("/"); // skip servlet path
    String scriptName = uri.substring(index + 1);
    index = scriptName.indexOf("/");
    if (index != -1)
    {
      scriptName = scriptName.substring(0, index);      
    }
    index = scriptName.indexOf(";");
    if (index != -1)
    {
      scriptName = scriptName.substring(0, index);
    }
    index = scriptName.indexOf(".");
    if (index != -1)
    {
      scriptName = scriptName.substring(0, index);
    }
    Script script = null;
    DocumentManagerClient client = null;
    if (toExecute)
    {
      String userId = MatrixConfig.getProperty("adminCredentials.userId");
      String password = MatrixConfig.getProperty("adminCredentials.password");
      client = new DocumentManagerClient(userId, password);
    }
    else // to download (only public scripts)
    {
      client = new DocumentManagerClient();
    }
    DocumentFilter filter = new DocumentFilter();
    filter.setDocTypeId(SCRIPT_DOCUMENT_TYPE);
    filter.setIncludeContentMetadata(true);
    Property property = new Property();
    property.setName(SCRIPT_PROPERTY_NAME);
    property.getValue().add(scriptName);
    filter.getProperty().add(property);
    List<Document> documents = client.findDocuments(filter);
    if (!documents.isEmpty())
    {
      Document document = documents.get(0);
      Content content = document.getContent();
      if (content != null)
      {
        String contentId = content.getContentId();
        script = new Script();
        script.scriptName = scriptName;
        script.contentId = content.getContentId();        
        script.scriptFile = client.getContentFile(contentId);
        script.contentType = content.getContentType();
      }
    }
    return script;
  }

  private void writeScript(Script script, HttpServletResponse response)
    throws IOException
  {
    response.setContentType(script.contentType);
    response.setCharacterEncoding("UTF-8");
    File scriptFile = script.scriptFile;
    IOUtils.writeToStream(new FileInputStream(scriptFile),
      response.getOutputStream());
  }

  private void writeOutput(Object output, HttpServletResponse response)
    throws IOException
  {
    if (output instanceof String)
    {
      if (response.getContentType() == null)
      {
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
      }
      String sout = output.toString();
      response.getWriter().write(sout);
    }
  }

  private Object executeJavaScript(Script script, 
    HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    File scriptFile = script.scriptFile;
    InputStreamReader reader =
      new InputStreamReader(new FileInputStream(scriptFile), "UTF-8");
    Context context = ContextFactory.getGlobal().enterContext();
    Object result = null;
    try
    {
      Scriptable scope = new WebScriptableBase(context);
      scope.put("request", scope, request);
      scope.put("response", scope, response);
      scope.put("servletConfig", scope, getServletConfig());
      scope.put("servletContext", scope, getServletContext());
      result = context.evaluateReader(scope, reader, "<code>", 1, null);
      if (result instanceof NativeJavaObject)
      {
        NativeJavaObject nat = (NativeJavaObject)result;
        result = nat.unwrap();
      }
    }
    finally
    {
      Context.exit();
    }
    return result;
  }

  class Script
  {
    String scriptName;
    String contentId;
    File scriptFile;
    String contentType;
  }
}
