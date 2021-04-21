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
package org.santfeliu.report.web;

import com.sun.xml.ws.developer.JAXWSProperties;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.MTOMFeature;
import org.matrix.doc.Document;
import org.matrix.report.ExportOptions;
import org.matrix.report.Parameter;
import org.matrix.report.ParameterDefinition;
import org.matrix.report.Report;
import org.matrix.report.ReportConstants;
import org.matrix.report.ReportManagerPort;
import org.matrix.report.ReportManagerService;
import org.matrix.security.SecurityConstants;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.security.util.SecurityUtils;
import org.santfeliu.util.IOUtils;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.UserSessionBean;


/**
 *
 * @author realor
 */
public class ReportServlet extends HttpServlet
{
  public static final String SOURCE = "src";
  public static final String CONNECTION_NAME_PARAMETER = "connectionName";
  public static final String FORMAT_PARAMETER = "format";
  private static final String DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";
  
  public ReportServlet()
  {
  }
  
  @Override
  public void init(ServletConfig config)
  {
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    try
    {
      String servletPath = request.getServletPath();
      String servletURI = request.getRequestURI();
      String reportId = servletURI.substring(servletPath.length());
      if (reportId.length() == 0)
      {
        showInfo(response);
      }
      else
      {
        reportId = reportId.substring(1);
        String format = request.getParameter(FORMAT_PARAMETER);
        int index = reportId.lastIndexOf(".");
        if (index != -1)
        {
          format = reportId.substring(index + 1);
          reportId = reportId.substring(0, index);
        }
        if (format == null)
        {
          showForm(reportId, request, response);
        }
        else if (SOURCE.equals(format))
        {
          showReportSource(reportId, request, response);
        }
        else
        {
          executeReport(reportId, format, request, response);
        }
      }
    }
    catch (IOException ex)
    {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
    catch (Exception ex)
    {
      try
      {
        showError(response, ex);
      }
      catch (IOException ioex)
      {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
    }
  }
  
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    doGet(request, response);    
  }
  
  private void showInfo(HttpServletResponse response)
    throws IOException
  {
    response.setContentType("text/html");
    PrintWriter writer = response.getWriter();
    try
    {
      writer.print("<HTML><BODY><H3>ReportServlet</H3></BODY></HTML>");
    }
    finally
    {
      writer.close();
    }
  }
  
  private void showForm(String reportId,
    HttpServletRequest request, HttpServletResponse response) throws Exception
  {
    String adminUserId = MatrixConfig.getProperty("adminCredentials.userId");
    String adminPwd = MatrixConfig.getProperty("adminCredentials.password");
    Credentials adminCredentials = new Credentials(adminUserId, adminPwd);
    ReportManagerPort port = getReportManagerPort(adminCredentials);
    Report report = port.loadReport(reportId, false);
    List<ParameterDefinition> paramDefinitions =
      report.getParameterDefinition();
    HashMap values = new HashMap();
    for (ParameterDefinition pd : paramDefinitions)
    {
      String name = pd.getName();
      String value = request.getParameter(name);
      if (value == null) value = pd.getDefaultValue();
      values.put(name, value);
    }
    Enumeration enu = request.getParameterNames();
    while (enu.hasMoreElements())
    {
      String name = (String)enu.nextElement();
      if (!values.containsKey(name))
      {
        String value = request.getParameter(name);
        ParameterDefinition pd = new ParameterDefinition();
        pd.setName(name);
        pd.setDescription(name);
        pd.setForPrompting(false);
        pd.setDefaultValue(value);
        paramDefinitions.add(pd);        
      }
    }

    request.setAttribute("report", report);
    request.setAttribute("content", report.getContent());
    request.setAttribute("parameters", paramDefinitions);
    request.setAttribute("values", values);
    request.setAttribute("changeDate", getChangeDate(report));
    request.setAttribute("changeUserId", getChangeUserId(report));
    Credentials credentials = getCredentials(request);
    request.setAttribute(SecurityConstants.USERID_PARAMETER,
      credentials.getUserId());
    request.setAttribute(SecurityConstants.PASSWORD_PARAMETER,
      credentials.getPassword());

    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");
    response.setHeader("Cache-Control","no-cache");
    response.setHeader("Pragma","no-cache");
    response.setDateHeader("Expires", -1);

    RequestDispatcher dispatcher = 
      request.getRequestDispatcher("/common/report/form.jsp");
    dispatcher.forward(request, response);
  }
  
  private void showReportSource(String reportId,
    HttpServletRequest request, HttpServletResponse response) throws Exception
  {
    Credentials credentials = getCredentials(request);
    ReportManagerPort port = getReportManagerPort(credentials);
    Report report = port.loadReport(reportId, true);
    DataHandler dh = report.getContent().getData();
    sendData(dh, response, null);
  }
  
  private void executeReport(String reportId, String format,
    HttpServletRequest request, HttpServletResponse response) throws Exception
  {
    Credentials credentials = getCredentials(request);
    ReportManagerPort port = getReportManagerPort(credentials);    
    List<Parameter> parameters = new ArrayList<Parameter>();
    Enumeration enu = request.getParameterNames();
    while (enu.hasMoreElements())
    {
      String paramName = (String)enu.nextElement();
      String paramValue = request.getParameter(paramName);
      if (paramValue.length() > 0)
      {
        Parameter parameter = new Parameter();
        parameter.setName(paramName);
        parameter.setValue(paramValue);
        parameters.add(parameter);
      }
    }
    // set user locale parameter
    if (request.getParameter(ReportConstants.REPORT_LOCALE) == null)
    {
      Locale userLocale = request.getLocale();
      if (userLocale != null)
      {
        Parameter parameter = new Parameter();
        parameter.setName(ReportConstants.REPORT_LOCALE);
        parameter.setValue(userLocale.getLanguage());
        parameters.add(parameter);
      }
    }
    ExportOptions options = new ExportOptions();
    options.setFormat(format);
    String connectionName = request.getParameter(CONNECTION_NAME_PARAMETER);
    if (connectionName != null && connectionName.trim().length() == 0)
      connectionName = null;
    DataHandler dh = port.executeReport(reportId, connectionName,
      parameters, options);

    String filename = null;
    if (format.equalsIgnoreCase("rtf"))
    {
      filename = reportId + ".rtf";
    }
    else if (format.equalsIgnoreCase("csv"))
    {
      filename = reportId + ".csv";
    }
    sendData(dh, response, filename);
  }

  private void showError(HttpServletResponse response, Exception ex)
    throws IOException
  {
    response.setContentType("text/html");
    PrintWriter writer = response.getWriter();
    try
    {
      writer.println("<HTML><BODY><H3>ReportServlet</H3>");
      writer.println("<P style=\"font-family:courier new;font-size:12px;\">");
      String message = ex.getMessage();
      if (message != null)
      {
        message = message.replaceAll("\n", "<BR>");
        message = message.replaceAll("\t", "&nbsp;&nbsp;");
        writer.println(message);
      }
      else
      {
        writer.println("ERROR: " + ex.getClass().getName());
      }
      writer.println("</P></BODY></HTML>");
    }
    finally
    {
      writer.close();
    }
  }

  private void setContentSize(HttpServletResponse response, DataHandler dh)
  {
    DataSource dataSource = dh.getDataSource();
    if (dataSource instanceof FileDataSource)
    {
      File file = ((FileDataSource)dataSource).getFile();
      response.setContentLength((int)file.length());
    }
  }
  
  private ReportManagerPort getReportManagerPort(Credentials credentials)
    throws Exception
  {
    WSDirectory wsDirectory = WSDirectory.getInstance();
    WSEndpoint endpoint = wsDirectory.getEndpoint(ReportManagerService.class);
    ReportManagerPort port = endpoint.getPort(ReportManagerPort.class,
      credentials.getUserId(), credentials.getPassword(), new MTOMFeature());

    Map requestContext = ((BindingProvider)port).getRequestContext();
    requestContext.put("com.sun.xml.ws.connect.timeout", 5 * 60000);
    requestContext.put("com.sun.xml.ws.request.timeout", 30 * 60000);
    requestContext.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);

    return port;
  }
  
  private void sendData(DataHandler dh, HttpServletResponse response,
    String filename) throws IOException
  {
    response.setContentType(dh.getContentType());
    setContentSize(response, dh);
    response.setDateHeader("Expires", System.currentTimeMillis() + 3000);
    if (filename != null)
    {
      response.setHeader("Content-disposition",
        "attachment; filename=" + filename);
    }    
    OutputStream os = response.getOutputStream();
    IOUtils.writeToStream(dh, os);
  }
  
  private String getChangeUserId(Document document)
  {
    return document.getChangeUserId() == null ?
      document.getCaptureUserId() : document.getChangeUserId();
  }
  
  private String getChangeDate(Document document) throws Exception
  {
    String sdate = document.getChangeDateTime();
    if (sdate == null)
    {
      sdate = document.getCaptureDateTime();
    }
    if (sdate != null)
    {
      SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyyMMddHHmmss");
      SimpleDateFormat dateFormat2 = new SimpleDateFormat(DATE_FORMAT);
      Date date = dateFormat1.parse(sdate);
      sdate = dateFormat2.format(date);
    }
    return sdate;
  }

  private Credentials getCredentials(HttpServletRequest request)
  {
    // credentials specified by parameter or header properties
    // have preference over UserSessionBean credentials
    Credentials credentials = SecurityUtils.getCredentials(request, false);
    if (credentials == null)
    {
      credentials = UserSessionBean.getCredentials(request);
    }
    return credentials;
  }
}
