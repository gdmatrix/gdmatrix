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
package org.matrix.util.modgen;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author realor
 */
public class Module extends NamedComponent
{

  private String title;
  private String version;
  private String namespace;
  private String service;
  private String port;
  private String wsdlLocation;
  private String authors;
  private String status = "FINAL";
  private List<Import> imports = new ArrayList<Import>();
  private List<Type> types = new ArrayList<Type>();  
  private List<Operation> operations = new ArrayList<Operation>();
  private URL location;

  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public String getAuthors()
  {
    return authors;
  }

  public void setAuthors(String authors)
  {
    this.authors = authors;
  }

  public String getStatus()
  {
    return status;
  }

  public void setStatus(String status)
  {
    this.status = status;
  }

  public String getWsdlLocation()
  {
    return wsdlLocation;
  }

  public void setWsdlLocation(String wsdlLocation)
  {
    this.wsdlLocation = wsdlLocation;
  }

  public URL getLocation()
  {
    return location;
  }

  public void setLocation(URL location)
  {
    this.location = location;
  }

  public String getNamespace()
  {
    return namespace;
  }

  public void setNamespace(String namespace)
  {
    this.namespace = namespace;
  }

  public String getService()
  {
    return service;
  }

  public void setService(String service)
  {
    this.service = service;
  }

  public String getPort()
  {
    return port;
  }

  public void setPort(String port)
  {
    this.port = port;
  }

  public List<Import> getImports()
  {
    return imports;
  }

  public List<Type> getTypes()
  {
    return types;
  }

  public List<Operation> getOperations()
  {
    return operations;
  }

  public String getVersion()
  {
    return version;
  }

  public void setVersion(String version)
  {
    this.version = version;
  }

  public String getJavaPackage()
  {
    StringBuilder builder = new StringBuilder();
    String s = namespace.replaceAll("/", "");
    if (s.startsWith("https:")) s = s.substring(6);
    if (s.startsWith("http:")) s = s.substring(5);

    String[] parts = s.split("\\.");
    for (int i = parts.length - 1; i >= 0; i--)
    {
      if (builder.length() > 0) builder.append(".");
      if (parts[i].length() > 0)
      {
        builder.append(parts[i]);        
      }
    }
    return builder.toString();
  }

  public Type getType(String name)
  {
    Type type = null;
    Iterator<Type> iter = types.iterator();
    while (type == null && iter.hasNext())
    {
      Type currType = iter.next();
      if (currType.getName().equals(name))
      {
        type = currType;
      }
    }
    return type;
  }

  public Operation getOperation(String name)
  {
    Operation operation = null;
    Iterator<Operation> iter = operations.iterator();
    while (operation == null && iter.hasNext())
    {
      Operation currOperation = iter.next();
      if (currOperation.getName().equals(name))
      {
        operation = currOperation;
      }
    }
    return operation;
  }

  @Override
  public String toString()
  {
    return "Module " + getName() + "\n" +
      getImports() + "\n" +
      getTypes() + "\n" +
      getOperations();
  }
}
