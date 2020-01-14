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
package org.santfeliu.ws;

import java.net.URL;

import java.util.HashMap;

import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceFeature;



/**
 *
 * @author unknown
 */
public class WSPortFactory
{
  static final String PORT_SUFIX = "Port";
  static final String SERVICE_SUFIX = "Service";
  static final String NAMESPACE_SUFIX = ".matrix.org/";

  static HashMap<String, Service> services = new HashMap<String, Service>();

  public static <T> T getPort(
    Class<T> portClass, String wsdlLocation,
    WebServiceFeature... features)
    throws Exception
  {
    return getPort(portClass, wsdlLocation, null, null, features);    
  }

  public static <T> T getPort(
    Class<T> portClass, String wsdlLocation, 
    String userId, String password,
    WebServiceFeature... features)
    throws Exception
  {
    String portName = portClass.getSimpleName();
    if (!portName.endsWith(PORT_SUFIX))
      throw new Exception("Invalid port name");
    
    String managerName = 
      portName.substring(0, portName.length() - PORT_SUFIX.length());
    String serviceName = managerName + SERVICE_SUFIX;
    
    String portClassName = portClass.getName();
    int eindex = portClassName.lastIndexOf(".");
    int bindex = portClassName.lastIndexOf(".", eindex - 1);
    String modulePackage = portClassName.substring(bindex + 1, eindex);
    String namespaceURI = "http://" + modulePackage + NAMESPACE_SUFIX;
    
    Service service = services.get(wsdlLocation);
    if (service == null)
    {    
      QName serviceQName = new QName(namespaceURI, serviceName);
      service = Service.create(new URL(wsdlLocation), serviceQName);
      services.put(wsdlLocation, service);
    }
    QName portQName = new QName(namespaceURI, portName);
    T port = service.getPort(portQName, portClass, features);
    
    if (userId != null && userId.trim().length() > 0)
    {
      Map requestContext = ((BindingProvider)port).getRequestContext();
      requestContext.put(BindingProvider.USERNAME_PROPERTY, userId);
      requestContext.put(BindingProvider.PASSWORD_PROPERTY, 
        password == null ? "" : password);
    }
    return port;
  }

  public static void clear()
  {
    services.clear();
  }
}
