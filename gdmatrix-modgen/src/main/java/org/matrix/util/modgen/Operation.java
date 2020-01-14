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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author realor
 */
public class Operation extends NamedComponent
{
  private List<Parameter> parameters = new ArrayList<Parameter>();
  private Response response;
  private List<Error> errors = new ArrayList<Error>();

  public List<Parameter> getParameters()
  {
    return parameters;
  }

  public Response getResponse()
  {
    return response;
  }

  public void setResponse(Response response)
  {
    this.response = response;
  }

  public List<Error> getErrors()
  {
    return errors;
  }

  public String getSignature()
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append(getName());
    buffer.append("(");
    int i = 0;
    while (i < parameters.size())
    {
      if (i > 0)
      {
        buffer.append(", ");
      }
      Parameter p = parameters.get(i);
      buffer.append(p.getName());
      buffer.append("{").append(p.getType()).append("}");
      if ("unbounded".equals(p.getMaxOccurs()))
      {
        if ("0".equals(p.getMinOccurs())) buffer.append("*");
        else buffer.append("+");
      }
      i++;
    }
    buffer.append(")");

    Response res = getResponse();
    if (res != null)
    {
      buffer.append(" => ");
      buffer.append(res.getName());
      buffer.append("{").append(res.getType()).append("}");
      if ("unbounded".equals(res.getMaxOccurs()))
      {
        if ("0".equals(res.getMinOccurs())) buffer.append("*");
        else buffer.append("+");
      }
    }
    return buffer.toString();
  }

  public String getJavaSignature(ModuleFactory factory)
  {
    StringBuilder buffer = new StringBuilder();
    Response res = getResponse();
    if (res != null)
    {
      buffer.append(getJavaType(res, factory));
    }
    else
    {
      buffer.append("void");
    }
    buffer.append(" ");
    buffer.append(getName());
    buffer.append("(");
    int i = 0;
    while (i < parameters.size())
    {
      if (i > 0)
      {
        buffer.append(", ");
      }
      Parameter p = parameters.get(i);
      buffer.append(getJavaType(p, factory)).append(" ").append(p.getName());
      i++;
    }
    buffer.append(")");
    return buffer.toString();
  }

  @Override
  public String toString()
  {
    return "Operation " + getName() + "\n" +
    getParameters() + "\n" +
    getResponse() + "\n" +
    getErrors();
  }

  private String getJavaType(ComplexTypifiedComponent c, ModuleFactory factory)
  {
    String qname = c.getType();
    String t = qname;
    if ("xs:string".equals(qname)) t = "String";
    else if ("xs:int".equals(qname)) t = "int";
    else if ("xs:long".equals(qname)) t = "long";
    else if ("xs:boolean".equals(qname)) t = "boolean";
    else if ("xs:double".equals(qname)) t = "double";
    else if ("xs:float".equals(qname)) t = "float";
    else if ("xs:dateTime".equals(qname)) t = "javax.xml.datatype.XMLGregorianCalendar";
    else if ("xs:base64Binary".equals(qname)) t = "byte[]";
    else
    {
      Type type = factory.getType(qname);
      if (type != null)
      {
        t = ((Module)type.getParent()).getJavaPackage() + "." + type.getName();
      }
    }
    if ("unbounded".equals(c.getMaxOccurs()))
    {
      t = "List<" + t + ">";
    }
    return t;
  }
}
