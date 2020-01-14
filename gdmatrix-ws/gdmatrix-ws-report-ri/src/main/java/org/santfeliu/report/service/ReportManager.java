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
package org.santfeliu.report.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.dic.Property;
import org.matrix.report.ExportOptions;
import org.matrix.report.Parameter;
import org.matrix.report.ParameterDefinition;
import org.matrix.report.Report;
import org.matrix.report.ReportFilter;
import org.matrix.report.ReportManagerPort;
import org.matrix.security.SecurityConstants;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.report.engine.ReportEngine;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.security.util.SecurityUtils;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.ws.WSExceptionFactory;
import org.santfeliu.util.log.CSVLogger;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.handler.MessageContext;

/**
 *
 * @author unknown
 */
@WebService(endpointInterface = "org.matrix.report.ReportManagerPort")
public class ReportManager implements ReportManagerPort
{
  @Resource
  WebServiceContext wsContext;

  private HashMap<String, ReportEngine> engines =
    new HashMap<String, ReportEngine>();

  protected static CSVLogger logger;
  protected static final String LOG_CONFIG = "org.santfeliu.ws.logConfig";
  protected static final String DEFAULT_TECHNOLOGY = "jasper";
  protected static long executionTimeout = 5 * 60 * 1000; // 5 minutes

  static
  {
    String logConfig = MatrixConfig.getPathProperty(LOG_CONFIG);
    if (logConfig != null)
    {
      logger = CSVLogger.getInstance(logConfig);
    }
    String value = MatrixConfig.getClassProperty(
      ReportManager.class, "executionTimeout");
    if (value != null)
    {
      try
      {
        executionTimeout = Long.parseLong(value);
      }
      catch (NumberFormatException ex)
      {
      }
    }
  }

  public ReportManager()
  {
  }

  public Report loadReport(String reportId, boolean includeSourceData)
  {
    try
    {
      Credentials credentials = SecurityUtils.getCredentials(wsContext);
      String userId = credentials.getUserId();

      logOperation("loadReport", "IN", reportId, userId);

      Report report = internalLoadReport(reportId, credentials);
      if (report == null)
        throw new WebServiceException("report:REPORT_NOT_FOUND");
      
      String technology = report.getTechnology();
      ReportEngine engine = getReportEngine(technology);
      
      Content content = report.getContent();
      String contentId = content.getContentId();
      DataSource dataSource = content.getData().getDataSource();
      List<ParameterDefinition> parameterDefinition = 
        engine.readReportParameters(contentId, dataSource);

      report.getParameterDefinition().addAll(parameterDefinition);
      if (!includeSourceData)
      {
        report.getContent().setData(null);
      }
      logOperation("loadReport", "OUT", reportId, userId);
      return report;
    }
    catch (Exception ex)
    {
      logOperation("loadReport", "FAULT", ex.getMessage());
      throw WSExceptionFactory.create(ex);
    }
  }

  public DataHandler executeReport(String reportId,
    String connectionName, List<Parameter> parameters,
    ExportOptions exportOptions)
  {
    try
    {
      DataHandler result = null;
      
      Credentials credentials = SecurityUtils.getCredentials(wsContext);
      String userId = credentials.getUserId();

      String parametersText = toText(parameters);
      logOperation("executeReport", "IN", reportId +
        "(" + parametersText + ")", userId);

      Report report = internalLoadReport(reportId, credentials);
      if (report == null)
        throw new WebServiceException("report:REPORT_NOT_FOUND");
      
      String technology = report.getTechnology();
      ReportEngine engine = getReportEngine(technology);
      if (connectionName == null)
        connectionName = report.getDefaultConnectionName();
      
      Content content = report.getContent();
      String contentId = content.getContentId();
      DataSource dataSource = content.getData().getDataSource();

      ReportExecutor executor = new ReportExecutor(engine, reportId, contentId,
        dataSource, connectionName, parameters, exportOptions, credentials);

      // launch report executor
      executor.start();

      synchronized (executor)
      {
        // wait for report execution
        do
        {
          executor.wait(1000); // wait a second
        }
        while (executor.isAlive() &&
          executor.getElapsedTime() < executionTimeout);
      }
      
      switch (executor.getStatus())
      {
        case ReportExecutor.RUNNING:
          // try to interrupt report execution
          if (executor.isAlive()) executor.interrupt();
          throw new WebServiceException("report:EXECUTION_TIMEOUT");

        case ReportExecutor.FAILED:
          // throw exception
          throw executor.getException();

        case ReportExecutor.FINISHED:
          long executionTime = executor.getExecutionTime();
          result = executor.getResult();
          logOperation("executeReport", "OUT", reportId +
            "(" + parametersText + ") time: " + executionTime + " ms", userId);
      }
      return result;
    }
    catch (Exception ex)
    {
      logOperation("executeReport", "FAULT", ex.getMessage());
      throw WSExceptionFactory.create(ex);
    }
  }

  public Report storeReport(Report report)
  {
    throw new WebServiceException("NOT_IMPLEMENTED");
  }

  public boolean removeReport(String reportId)
  {
    throw new WebServiceException("NOT_IMPLEMENTED");
  }

  public List<Report> findReports(ReportFilter reportFilter)
  {
    throw new WebServiceException("NOT_IMPLEMENTED");
  }

  private Report internalLoadReport(String reportId, Credentials credentials)
    throws Exception
  {
    String userId = credentials.getUserId();
    String password = credentials.getPassword();
    DocumentManagerClient client = 
      new DocumentManagerClient(userId, password);

    DocumentFilter filter = new DocumentFilter();
    Property property = new Property();
    property.setName("report");
    property.getValue().add(reportId);
    filter.getProperty().add(property);
    filter.setMaxResults(1);
    List<Document> documents = client.findDocuments(filter);
    if (documents.size() == 1)
    {
      Report report = new Report();
      Document document = documents.get(0);
      String docId = document.getDocId();
      document = client.loadDocument(docId, 0);
      copyReport(report, document);
      return report;
    }
    return null;
  }

  private ReportEngine getReportEngine(String technology)
  {
    ReportEngine engine = engines.get(technology);
    if (engine == null)
    {
      String engineClassName = MatrixConfig.getProperty(
       "org.santfeliu.report.engine." + technology + ".className");
      if (engineClassName != null)
      {
        try
        {
          Class engineClass = Class.forName(engineClassName);
          engine = (ReportEngine)engineClass.newInstance();
          engines.put(technology, engine);
        }
        catch (Exception ex)
        {
          throw new WebServiceException(
            "report:REPORT_ENGINE_INITIALIZATION_FAILED");
        }
      }
      else
      {
        throw new WebServiceException("report:UNSUPPORTED_REPORT_TECHNOLOGY");
      }
    }
    return engine;
  }

  private void logOperation(String operation, String messageType,
    String message)
  {
    logOperation(operation, messageType, message, SecurityConstants.ANONYMOUS);
  }

  private void logOperation(String operation, String messageType,
    String message, String userId)
  {
    if (logger != null)
    {
      // TODO: CHECK URL
      HttpServletRequest request =
        (HttpServletRequest)wsContext.getMessageContext().
          get(MessageContext.SERVLET_REQUEST);
      String url = request.getRequestURL().toString();
      String ip = request.getRemoteAddr();

      logger.log(getCurrentDateTime("dd/MM/yyyy-HH:mm:ss"),
        userId, ip, url, operation, messageType, message);
    }
  }

  private String getCurrentDateTime(String dateFormat)
  {
    Date now = new Date();
    SimpleDateFormat df = new SimpleDateFormat(dateFormat);
    return df.format(now);
  }

  private String toText(List<Parameter> parameters)
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append("{");
    if (parameters.size() > 0)
    {
      Parameter parameter = parameters.get(0);
      buffer.append(toText(parameter));
      for (int i = 1; i < parameters.size(); i++)
      {
        buffer.append(", ");
        parameter = parameters.get(i);
        buffer.append(toText(parameter));
      }
    }
    buffer.append("}");
    return buffer.toString();
  }

  private String toText(Parameter parameter)
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append(parameter.getName());
    buffer.append("=");
    if (!parameter.getName().equals(SecurityConstants.PASSWORD_PARAMETER))
    {
      buffer.append(parameter.getValue());
    }
    else
    {
      buffer.append("********");
    }
    return buffer.toString();
  }

  private void copyReport(Report report, Document document)
  {
    report.setDocId(document.getDocId());
    report.setVersion(document.getVersion());
    report.setTitle(document.getTitle());
    report.setDocTypeId(document.getDocTypeId());
    report.setLanguage(document.getLanguage());
    report.setLockUserId(document.getLockUserId());
    report.setContent(document.getContent());
    report.setState(document.getState());
    report.getClassId().addAll(document.getClassId());
    report.getProperty().addAll(document.getProperty());
    report.setCaptureDateTime(document.getCaptureDateTime());
    report.setCaptureUserId(document.getCaptureUserId());
    report.setChangeDateTime(document.getChangeDateTime());
    report.setChangeUserId(document.getChangeUserId());
    report.setCreationDate(document.getCreationDate());
    report.getAccessControl().addAll(document.getAccessControl());    

    // reportId
    Property reportIdProp = DocumentUtils.getProperty(document, "reportId");
    if (reportIdProp != null)
    {
      report.setReportId(reportIdProp.getValue().get(0));
    }
    else
    {
      reportIdProp = DocumentUtils.getProperty(document, "report");
      if (reportIdProp != null)
      {
        report.setReportId(reportIdProp.getValue().get(0));
      }
    }
    // defaultConnectionName
    Property defaultConnectionNameProp =
      DocumentUtils.getProperty(document, "defaultConnectionName");
    if (defaultConnectionNameProp != null)
    {
      report.setDefaultConnectionName(
        defaultConnectionNameProp.getValue().get(0));
    }
    // technology
    Property technologyProp =
      DocumentUtils.getProperty(document, "technology");
    if (technologyProp != null)
    {
      report.setTechnology(technologyProp.getValue().get(0));
    }
    else report.setTechnology(DEFAULT_TECHNOLOGY);
  }
}

