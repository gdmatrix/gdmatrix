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

import java.util.List;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import org.matrix.report.ExportOptions;
import org.matrix.report.Parameter;
import org.santfeliu.report.engine.ReportEngine;
import org.santfeliu.security.util.Credentials;

/**
 *
 * @author realor
 */
class ReportExecutor extends Thread
{
  public static final int CREATED = 0;
  public static final int RUNNING = 1;
  public static final int FINISHED = 2;
  public static final int FAILED = 3;

  private ReportEngine engine;
  private String reportId;
  private String contentId;
  private DataSource dataSource;
  private String connectionName;
  private List<Parameter> parameters;
  private ExportOptions exportOptions;
  private Credentials credentials;
  
  private int status = CREATED;
  private long executionTime;
  private DataHandler result;
  private Exception exception;
  private long startTime;

  private static int executorCounter;

  ReportExecutor(ReportEngine engine, String reportId, String contentId,
     DataSource dataSource, String connectionName, List<Parameter> parameters,
     ExportOptions exportOptions, Credentials credentials)
  {
    super("ReportExecutor-" + executorCounter + "-" + reportId);
    executorCounter++;
    this.setPriority(Thread.NORM_PRIORITY);
    this.engine = engine;
    this.reportId = reportId;
    this.contentId = contentId;
    this.dataSource = dataSource;
    this.connectionName = connectionName;
    this.parameters = parameters;
    this.exportOptions = exportOptions;
    this.credentials = credentials;
  }

  @Override
  public void run()
  {
    try
    {
      status = RUNNING;
      startTime = System.currentTimeMillis();

      result = engine.executeReport(contentId, dataSource,
              connectionName, parameters, exportOptions, credentials);

      long endTime = System.currentTimeMillis();
      executionTime = endTime - startTime;
      
      status = FINISHED;
    }
    catch (Exception ex)
    {
      status = FAILED;
      this.exception = ex;
    }
    finally
    {
      // notify
      synchronized (this)
      {
        this.notifyAll();
      }
    }
  }

  public String getReportId()
  {
    return reportId;
  }

  public int getStatus()
  {
    return status;
  }

  public DataHandler getResult()
  {
    return result;
  }

  public long getElapsedTime()
  {
    return System.currentTimeMillis() - startTime;
  }

  public long getExecutionTime()
  {
    return executionTime;
  }

  public Exception getException()
  {
    return exception;
  }
}
