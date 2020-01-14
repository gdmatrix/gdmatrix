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
package org.santfeliu.ant.cases;

import java.sql.Connection;
import org.apache.tools.ant.BuildException;
import org.matrix.cases.CaseManagerPort;
import org.matrix.cases.CaseManagerService;
import org.matrix.util.WSEndpoint;
import org.santfeliu.ant.ws.WSTask;

/**
 *
 * @author unknown
 */
public class ImportCasesTask extends WSTask
{
  private String dbConnVar;
  private String fullLogProperty;
  private String summaryLogProperty;
  private String importerClassName;

  //logging
  private StringBuilder fullLogBuilder = new StringBuilder();
  private StringBuilder summaryLogBuilder = new StringBuilder();

  public String getDbConnVar()
  {
    return dbConnVar;
  }

  public void setDbConnVar(String dbConnVar)
  {
    this.dbConnVar = dbConnVar;
  }

  public String getFullLogProperty()
  {
    return fullLogProperty;
  }

  public void setFullLogProperty(String fullLogProperty)
  {
    this.fullLogProperty = fullLogProperty;
  }

  public String getSummaryLogProperty()
  {
    return summaryLogProperty;
  }

  public void setSummaryLogProperty(String summaryLogProperty)
  {
    this.summaryLogProperty = summaryLogProperty;
  }

  public String getImporterClassName()
  {
    return importerClassName;
  }

  public void setImporterClassName(String importerClassName)
  {
    this.importerClassName = importerClassName;
  }

  @Override
  public void execute()
  {
    validateInput();
    try
    {
      log("Importació d'expedients");
      logLn();
      long startMs = System.currentTimeMillis();
      
      Class importerClass = Class.forName(importerClassName);
      Importer instance = (Importer)importerClass.newInstance();
      instance.execute(getConnection(), getPort());

      if (fullLogProperty != null)
      {
        for (String message : instance.getFullLog())
        {
          logFull(message);
        }
      }

      if (summaryLogProperty != null)
      {
        for (String message : instance.getSummaryLog())
        {
          logSummary(message);
        }
      }

      long endMs = System.currentTimeMillis();
      log("Temps de treball: " + (endMs - startMs) + " ms");
    }
    catch (Exception ex)
    {
      log("ERROR: " + ex.toString());
    }

    // write log properties
    if (fullLogProperty != null)
    {
      String currentLog = getProject().getProperty(fullLogProperty);
      String log = (currentLog == null ? "" : currentLog) +
        fullLogBuilder.toString();
      getProject().setProperty(fullLogProperty, log);
    }
    if (summaryLogProperty != null)
    {
      String currentLog = getProject().getProperty(summaryLogProperty);
      String log = (currentLog == null ? "" : currentLog) +
        summaryLogBuilder.toString();
      getProject().setProperty(summaryLogProperty, log);
    }
  }

  @Override
  public void log(String s)
  {
    if (fullLogProperty != null) logFull(s);
    if (summaryLogProperty != null) logSummary(s);
  }

  private void logFull(String s)
  {
    super.log(s);
    fullLogBuilder.append(s);
    fullLogBuilder.append("<br/>");
  }

  private void logSummary(String s)
  {
    summaryLogBuilder.append(s);
    summaryLogBuilder.append("<br/>");
  }

  private void logLn()
  {
    log("");
  }

  private void validateInput()
  {
    if (connVar == null)
      throw new BuildException("Attribute 'connVar' is required");
    if (dbConnVar == null)
      throw new BuildException("Attribute 'dbConnVar' is required");
    if (importerClassName == null)
      throw new BuildException("Attribute 'importerClassName' is required");
  }

  private Connection getConnection()
  {
    return (Connection)getVariable(dbConnVar);
  }

  private CaseManagerPort getPort()
  {
    WSEndpoint casesEndpoint = getEndpoint(CaseManagerService.class);
    CaseManagerPort port = casesEndpoint.getPort(CaseManagerPort.class,
      getUsername(), getPassword());
    return port;
  }

}
