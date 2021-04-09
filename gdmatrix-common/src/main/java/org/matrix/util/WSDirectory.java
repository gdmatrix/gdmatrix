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

  final HashMap<String, WSEndpoint> endpointsByName = new HashMap<>();
  final HashMap<QName, ArrayList<WSEndpoint>> endpointsByQName = 
    new HashMap<>();

  private URL url;

  /**
   * Gets the default WSDirectory instance
   * @return the default instance
   */
  public static WSDirectory getInstance()
  {
    return getInstance(defaultInstanceURL);
  }

  /**
   * Gets the WSDirectory instance for the given url
   * @param url the wsdirectory url
   * @return the WSDirectory instance
   */
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

  static WSDirectory createInstance(URL url)
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
   * Gets the qualified name of service serviceClass
   * @param serviceClass the service class
   * @return the qualified name of service serviceClass
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
   * Gets the endpoint named name
   * @param endpointName the endpointName
   * @return the endpoint named name
   */
  public WSEndpoint getEndpoint(String endpointName)
  {
    WSEndpoint endpoint = endpointsByName.get(endpointName);
    if (endpoint == null) throw new InvalidEndpointException();
    return endpoint;
  }

  /**
   * Gets the default endpoint where service qualified name is serviceName
   * @param serviceName the service qname
   * @return the default endpoint where service qualified name is serviceName
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
   * Gets the default endpoint whose service class is serviceClass
   * @param serviceClass the service class
   * @return the default endpoint whose service class is serviceClass
   */
  public WSEndpoint getEndpoint(Class<? extends Service> serviceClass)
  {
    QName serviceName = getServiceName(serviceClass);
    return getEndpoint(serviceName);
  }

  /**
   * Gets all the endpoints whose service qualified name is serviceName
   * @param serviceName the service qname
   * @return the endpoints whose service qualified name is serviceName
   */
  public List<WSEndpoint> getEndpoints(QName serviceName)
  {
    return endpointsByQName.get(serviceName);
  }

  /**
   * Gets all the endpoints whose service class is serviceClass
   * @param serviceClass the service class
   * @return the endpoints whose service class is serviceClass
   */
  public List<WSEndpoint> getEndpoints(Class<? extends Service> serviceClass)
  {
    QName serviceName = getServiceName(serviceClass);
    return getEndpoints(serviceName);
  }

  /**
   * Creates an endpoint with the given name and service name
   * @param name the endpoint name
   * @param serviceName the service qname
   * @return the new endpoint
   */
  public WSEndpoint newEndpoint(String name, QName serviceName)
  {
    WSEndpoint endpoint = new WSEndpoint(this, name, serviceName);

    endpointsByName.put(name, endpoint);
    ArrayList<WSEndpoint> endpoints = endpointsByQName.get(serviceName);
    if (endpoints == null)
    {
      endpoints = new ArrayList<>();
      endpointsByQName.put(serviceName, endpoints);
    }
    endpoints.add(endpoint);
    return endpoint;
  }

  /**
   * Gets the internal entity of the given entity class and globalId
   * @param entityClass the entity class
   * @param globalId the globalId
   * @return the InternalEntity associated to the given class and globalId
   */
  public InternalEntity getInternalEntity(Class entityClass, String globalId)
  {
    return getInternalEntity(entityClass.getSimpleName(), globalId);
  }

  /**
   * Gets the internal entity of the given name and globalId
   * @param entityName the entity name
   * @param globalId the globalId
   * @return the InternalEntity associated to the given name and globalId
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
   * Gets a list of the external entities that have a reference to the given 
   *   entityClass and globalId
   * @param entityClass the entity class
   * @param globalId the globalId
   * @param serviceName the serviceName to filter (may be null)
   * @return the list of ExternalEntities that satisfy the search criteria
   */
  public List<ExternalEntity> getExternalEntities(Class entityClass,
    String globalId, QName serviceName)
  {
    return getExternalEntities(entityClass.getSimpleName(), globalId, 
      serviceName);
  }
  
  /**
   * Gets a list of the external entities that have a reference to the given 
   *   entity name and globalId
   * @param entityName the entity name
   * @param globalId the globalId
   * @param serviceName the serviceName to filter (may be null)
   * @return the list of ExternalEntities that satisfy the search criteria
   */
  public List<ExternalEntity> getExternalEntities(String entityName,
    String globalId, QName serviceName)
  {
    List<ExternalEntity> externalEntities = new ArrayList<>();
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

  /**
   * Indicates if an entity id is local 
   * @param entityName the entity name
   * @param id the id
   * @return true if local, false otherwise
   */
  public boolean isLocalId(String entityName, String id)
  {
    if (id == null) return true;
    return !id.contains(PREFIX_SEPARATOR);
  }

  /**
   * Indicates if an entity id is global 
   * @param entityName the entity name
   * @param id the id
   * @return true if global, false otherwise
   */
  public boolean isGlobalId(String entityName, String id)
  {
    if (id == null) return true;
    if (id.contains(PREFIX_SEPARATOR)) return true;
    InternalEntity entity = getInternalEntity(entityName, id);
    return entity != null;
  }
}
