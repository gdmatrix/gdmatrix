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
package org.matrix.util;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

/**
 *
 * @author realor
 */
public class WSDirectory
{
  static final String PREFIX_SEPARATOR = ":";
  static final String NAMESPACE_SUFFIX = ".matrix.org/";
  static final Map<URL, WSDirectory> instances =
    Collections.synchronizedMap(new HashMap<URL, WSDirectory>());  
  static URL defaultInstanceURL;

  final HashMap<String, WSEndpoint> endpointsByName =
    new HashMap<String, WSEndpoint>();
  final HashMap<QName, ArrayList<WSEndpoint>> endpointsByQName =
    new HashMap<QName, ArrayList<WSEndpoint>>();

  private URL url;

  public static WSDirectory getInstance()
  {
    return getInstance(defaultInstanceURL);
  }

  public static WSDirectory getInstance(URL url)
  {
    WSDirectory directory = instances.get(url);
    if (directory == null)
    {
      directory = createInstance(url);
      instances.put(url, directory);
    }
    return directory;
  }

  public static void setDefaultInstanceURL(URL url)
  {
    defaultInstanceURL = url;
  }

  public static URL getDefaultInstanceURL()
  {
    return defaultInstanceURL;
  }

  public static WSDirectory createInstance(URL url)
  {
    try
    {
      if (url == null) url = new URL("http://localhost/wsdirectory");
      WSDirectory directory = new WSDirectory();
      directory.url = url;
      InputStream is = url.openStream();
      WSDirectoryLoader loader = new WSDirectoryLoader();      
      loader.load(is, directory);
      return directory;
    }
    catch (Exception ex)
    {
      throw new RuntimeException("Error reading wsdirectory: " + ex);
    }
  }

  public URL getUrl()
  {
    return url;
  }

  /**
   * returns the qualified name of service serviceClass
   * @param serviceClass
   * @return
   */
  public QName getServiceName(Class<? extends Service> serviceClass)
  {
    String serviceSimpleName = serviceClass.getSimpleName();
    String serviceClassName = serviceClass.getName();
    int eindex = serviceClassName.lastIndexOf(".");
    int bindex = serviceClassName.lastIndexOf(".", eindex - 1);
    String modulePackage = serviceClassName.substring(bindex + 1, eindex);
    String namespaceURI = "http://" + modulePackage + NAMESPACE_SUFFIX;
    QName serviceName = new QName(namespaceURI, serviceSimpleName);
    return serviceName;
  }

  /**
   * returns the endpoint named name
   * @param endpointName
   * @return
   */
  public WSEndpoint getEndpoint(String endpointName)
  {
    WSEndpoint endpoint = endpointsByName.get(endpointName);
    if (endpoint == null) throw new InvalidEndpointException();
    return endpoint;
  }

  /**
   * returns the default endpoint where service qualified name is serviceName
   * @param serviceName
   * @return
   */
  public WSEndpoint getEndpoint(QName serviceName)
  {
    WSEndpoint endpoint;
    List<WSEndpoint> endpoints =
      endpointsByQName.get(serviceName);
    if (endpoints != null && endpoints.size() > 0)
    {
      // take first endpoint as default
      endpoint = endpoints.get(0);
    }
    else endpoint = null;
    return endpoint;
  }

  /**
   * returns the default endpoint where service class is serviceClass
   * @param serviceName
   * @return
   */
  public WSEndpoint getEndpoint(Class<? extends Service> serviceClass)
  {
    QName serviceName = getServiceName(serviceClass);
    return getEndpoint(serviceName);
  }

  /**
   * returns all endpoints where service qualified name is serviceName
   * @param serviceName
   * @return
   */
  public List<WSEndpoint> getEndpoints(QName serviceName)
  {
    return endpointsByQName.get(serviceName);
  }

  /**
   * returns all endpoints where service class is serviceName
   * @param serviceName
   * @return
   */
  public List<WSEndpoint> getEndpoints(
    Class<? extends Service> serviceClass)
  {
    QName serviceName = getServiceName(serviceClass);
    return getEndpoints(serviceName);
  }

  public WSEndpoint newEndpoint(String name, QName serviceName)
  {
    WSEndpoint endpoint = new WSEndpoint(this, name, serviceName);

    endpointsByName.put(name, endpoint);
    ArrayList<WSEndpoint> endpoints = endpointsByQName.get(serviceName);
    if (endpoints == null)
    {
      endpoints = new ArrayList<WSEndpoint>();
      endpointsByQName.put(serviceName, endpoints);
    }
    endpoints.add(endpoint);
    return endpoint;
  }

  /**
   *
   * @param entityClass
   * @param globalId
   * @return
   */
  public InternalEntity getInternalEntity(Class entityClass, String globalId)
  {
    return getInternalEntity(entityClass.getSimpleName(), globalId);
  }

  /**
   *
   * @param entityName
   * @param globalId
   * @return the entity object associated to globalId
   */
  public InternalEntity getInternalEntity(String entityName, String globalId)
  {
    InternalEntity internalEntity = null;
    int index = globalId.indexOf(WSDirectory.PREFIX_SEPARATOR);
    String prefix = (index == -1) ? null : globalId.substring(0, index);

    Iterator<WSEndpoint> iter = endpointsByName.values().iterator();
    while (iter.hasNext() && internalEntity == null)
    {
      WSEndpoint ep = iter.next();
      InternalEntity entity = ep.getInternalEntity(entityName);
      if (entity != null)
      {
        String prf = entity.getPrefix();
        if (prf != null && prf.equals(prefix) ||
            (prf == null && prefix == null))
          internalEntity = entity;
      }
    }
    return internalEntity;
  }

  /**
   *
   * @param entityClass
   * @param globalId
   * @param serviceName
   * @return
   */
  public List<ExternalEntity> getExternalEntities(Class entityClass,
    String globalId, QName serviceName)
  {
    return getExternalEntities(entityClass.getSimpleName(), globalId, serviceName);
  }
  
  /**
   * 
   * @param entityName
   * @param globalId
   * @return the list of external entities that has a reference to
   *   entityName/globalId
   */
  public List<ExternalEntity> getExternalEntities(String entityName,
    String globalId, QName serviceName)
  {
    List<ExternalEntity> externalEntities = new ArrayList<ExternalEntity>();
    int index = globalId.indexOf(WSDirectory.PREFIX_SEPARATOR);
    String prefix = (index == -1) ? null : globalId.substring(0, index);

    Iterator<WSEndpoint> iter = endpointsByName.values().iterator();
    while (iter.hasNext())
    {
      WSEndpoint ep = iter.next();
      if (serviceName == null || ep.getServiceName().equals(serviceName))
      {
        ExternalEntity entity = ep.getExternalEntity(entityName);
        if (entity != null)
        {
          String from = entity.getFrom();
          if (from != null)
          {
            WSEndpoint fromEndpoint = endpointsByName.get(from);
            String prf = fromEndpoint.getInternalEntity(entityName).getPrefix();

            if (prf != null && prf.equals(prefix) ||
              (prf == null && prefix == null))
            externalEntities.add(entity);
          }
          else if (prefix == null)
          {
            externalEntities.add(entity);
          }
        }
      }
    }
    return externalEntities;
  }

  public boolean isLocalId(String entityName, String id)
  {
    if (id == null) return true;
    return !id.contains(PREFIX_SEPARATOR);
  }

  public boolean isGlobalId(String entityName, String id)
  {
    if (id == null) return true;
    if (id.contains(PREFIX_SEPARATOR)) return true;
    InternalEntity entity = getInternalEntity(entityName, id);
    return entity != null;
  }
}
