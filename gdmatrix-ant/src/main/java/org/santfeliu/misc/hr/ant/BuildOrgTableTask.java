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
package org.santfeliu.misc.hr.ant;

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
public class BuildOrgTableTask extends WSTask
{
  //task properties
  private String dbConnVar;
  private String rootCaseId;
  private String logProperty;

  //logging
  private StringBuilder logBuilder = new StringBuilder();

  public String getDbConnVar()
  {
    return dbConnVar;
  }

  public void setDbConnVar(String dbConnVar)
  {
    this.dbConnVar = dbConnVar;
  }

  public String getRootCaseId()
  {
    return rootCaseId;
  }

  public void setRootCaseId(String rootCaseId)
  {
    this.rootCaseId = rootCaseId;
  }

  public String getLogProperty()
  {
    return logProperty;
  }

  public void setLogProperty(String logProperty)
  {
    this.logProperty = logProperty;
  }

  @Override
  public void execute()
  {
    validateInput();
    try
    {
      log("Construcció de la taula de departaments");
      logLn();
      long startMs = System.currentTimeMillis();
      
      OrgTableBuilder orgTableBuilder = 
        new OrgTableBuilder(rootCaseId, getConnection(), getPort());
      orgTableBuilder.execute();

      for (String message : orgTableBuilder.getLog())
      {
        log(message);
      }

      long endMs = System.currentTimeMillis();
      log("Temps de treball: " + (endMs - startMs) + " ms");
    }
    catch (Exception ex)
    {
      log("ERROR: " + ex.toString());
    }

    if (logProperty != null)
    {
      String currentLog = getProject().getProperty(logProperty);
      String log = (currentLog == null ? "" : currentLog) +
        logBuilder.toString();
      getProject().setProperty(logProperty, log);
    }

  }

  @Override
  public void log(String s)
  {
    super.log(s);
    if (logProperty != null)
    {
      logBuilder.append(s);
      logBuilder.append("<br/>");
    }
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
    if (rootCaseId == null)
      throw new BuildException("Attribute 'rootCaseId' is required");
  }

  private Connection getConnection()
  {
    return (Connection)getVariable(dbConnVar);
  }

  private CaseManagerPort getPort()
  {
    WSEndpoint casesEndpoint = getEndpoint(CaseManagerService.class);
    return casesEndpoint.getPort(CaseManagerPort.class, getUsername(),
      getPassword());
  }
}
