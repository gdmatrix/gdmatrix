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
package org.santfeliu.ant.ws;

import java.sql.Connection;
import java.util.ArrayList;
import org.apache.tools.ant.BuildException;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;

/**
 *
 * @author blanquepa
 */
public class WSImportTask extends WSTask
{
  private String dbConnVar;
  private String importerClassName;
  private String serviceClassName;
  private String methodName;
  private String parameterVars;
  private String resultVar;

  public String getDbConnVar()
  {
    return dbConnVar;
  }

  public void setDbConnVar(String dbConnVar)
  {
    this.dbConnVar = dbConnVar;
  }

  public String getImporterClassName()
  {
    return importerClassName;
  }

  public void setImporterClassName(String importerClassName)
  {
    this.importerClassName = importerClassName;
  }

  public String getMethodName()
  {
    return methodName;
  }

  public void setMethodName(String methodName)
  {
    this.methodName = methodName;
  }

  public String getServiceClassName()
  {
    return serviceClassName;
  }

  public void setServiceClassName(String serviceClassName)
  {
    this.serviceClassName = serviceClassName;
  }

  public String getParameterVars()
  {
    return parameterVars;
  }

  public void setParameterVars(String parameterVars)
  {
    this.parameterVars = parameterVars;
  }

  public String getResultVar()
  {
    return resultVar;
  }

  public void setResultVar(String resultVar)
  {
    this.resultVar = resultVar;
  }

  protected Connection getConnection()
  {
    return (Connection)getVariable(dbConnVar);
  }

  protected Object getPort() throws ClassNotFoundException
  {
    Class serviceClass = Class.forName(getServiceClassName());

    String portClassName =
      (getServiceClassName() + ";").replace("Service;", "Port");
    Class portClass = Class.forName(portClassName);

    WSEndpoint endpoint = getEndpoint(serviceClass);
    return endpoint.getPort(portClass, getUsername(), getPassword());
  }

  @Override
  public void execute()
  {
    validateInput();
    try
    {
      log("Processing " + serviceClassName + "." + methodName + "(" + parameterVars + ")");
      logLn();
      long startMs = System.currentTimeMillis();

      WSImporter instance = null;
      if (importerClassName != null)
      {
        Class importerClass = Class.forName(importerClassName);
        instance = (WSImporter)importerClass.getConstructor().newInstance();
        Object object = instance.execute(getConnection(), getPort());
        if (resultVar != null)
          setVariable(resultVar, object);
      }
      else
      {
        ArrayList parameters = new ArrayList();
        if (parameterVars != null)
        {
          String[] paramArray = parameterVars.split(",");
          for (String param : paramArray)
          {
            parameters.add(getVariable(param));
          }
          instance = new DefaultWSImporter(methodName, parameters.toArray());
        }
        else
          instance = new DefaultWSImporter(methodName);


        Object object = instance.execute(getConnection(), getPort());
        if (resultVar != null)
          setVariable(resultVar, object);
      }

      long endMs = System.currentTimeMillis();
      log("Temps de treball: " + (endMs - startMs) + " ms");
    }
    catch (Exception ex)
    {
      log("ERROR: " + ex.toString());
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
    if (serviceClassName == null)
      throw new BuildException("Attribute 'serviceClassName' is required");
    if (importerClassName == null && methodName == null)
      throw new BuildException("Attribute 'importerClassName' or 'methodName' is required");
    if (methodName != null && parameterVars == null)
      throw new BuildException("Attribute 'parameterVars' is required in combination with 'methodName'");
  }

  private WSEndpoint getEndpoint(String endpointName)
  {
    WSDirectory wsDir = (WSDirectory)getVariable(connVar);
    if (wsDir == null) throw new BuildException("connVar undefined");
    return wsDir.getEndpoint(endpointName);
  }

}
