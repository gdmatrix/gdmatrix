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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;
import org.matrix.dic.Type;
import org.matrix.util.ExternalEntity;
import org.matrix.util.InternalEntity;
import org.matrix.util.InvalidIdentifierException;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.security.util.SecurityUtils;

/**
 *
 * @author realor
 */
public class WSProxy
{
  @Resource
  protected WebServiceContext wsContext;
  protected WSEndpoint proxyEndpoint;
  private Class serviceClass;
  private Class portClass;

  protected WSProxy(Class serviceClass, Class portClass)
  {
    this.serviceClass = serviceClass;
    this.portClass = portClass;
  }

  protected int count(String countMethodName, Object filter)
  {
    int count = 0;
    Exception exception = null;
    boolean throwException = true;

    Credentials credentials = SecurityUtils.getCredentials(wsContext);
    List<WSEndpoint> endpoints =
      getWSDirectory().getEndpoints(serviceClass);

    for (WSEndpoint endpoint : endpoints)
    {
      if (endpoint != getProxyEndpoint()) // skip proxy
      {
        try
        {
          Object port = endpoint.getPort(portClass, 
            credentials.getUserId(), credentials.getPassword());

          // force filter conversion to check errors
          Object endpointFilter =
            endpoint.toLocal((Class)filter.getClass(), filter);
          if (!endpoint.isOnlyLocal()) endpointFilter = filter;

          count += countObjects(port, countMethodName, endpointFilter);
          // at least one endpoint was invoked successfully,
          // do not throw exception
          throwException = false;
        }
        catch (InvalidIdentifierException ex)
        {
          // ignore, endpoint do not match filter
        }
        catch (Exception ex)
        {
          // save exception to throw it later if necessary
          exception = ex;
        }
      }
    }
    if (exception != null && throwException)
    {
      // Exception is thrown when all endpoints throw an exception
      throw WSExceptionFactory.create(exception);
    }
    return count;
  }

  protected <T> List<T> find(String countMethodName, String findMethodName,
    Object filter, Class<T> objectClass)
  {
    List<T> objects = new ArrayList<T>();
    int firstResult = getFirstResult(filter);
    int maxResults = getMaxResults(filter);

    int count = 0;
    Exception exception = null;
    boolean throwException = true;

    Credentials credentials = SecurityUtils.getCredentials(wsContext);
    List<WSEndpoint> endpoints = getWSDirectory().getEndpoints(serviceClass);
    Iterator<WSEndpoint> iter = endpoints.iterator();

    while (iter.hasNext() && (objects.size() < maxResults || maxResults == 0))
    {
      WSEndpoint endpoint = iter.next();
      if (endpoint != getProxyEndpoint()) // skip proxy
      {
        try
        {
          Object port = endpoint.getPort(portClass,
            credentials.getUserId(), credentials.getPassword());

          // force filter conversion to check errors
          Object endpointFilter =
            endpoint.toLocal((Class)filter.getClass(), filter);
          if (!endpoint.isOnlyLocal()) endpointFilter = filter;

          if (firstResult == 0 && maxResults == 0) // find all
          {
            // find types
            List endpointObjects =
              findObjects(port, findMethodName, endpointFilter);
            for (Object object : endpointObjects)
            {
              // force conversion to check errors
              T t = (T)endpoint.toGlobal((Class)object.getClass(), object);
              if (endpoint.isOnlyLocal()) objects.add(t);
              else objects.add((T)object);
            }
          }
          else
          {
            // count types
            int endpointCount = countObjects(port,
              countMethodName, endpointFilter);
            if (firstResult < count + endpointCount)
            {
              int endpointFirstResult = (firstResult > count) ?
                firstResult - count : 0;
              int endpointMaxResults = (maxResults == 0) ?
                 0 : maxResults - objects.size();
              setFirstResult(endpointFilter, endpointFirstResult);
              setMaxResults(endpointFilter, endpointMaxResults);

              // find types
              List endpointObjects =
                findObjects(port, findMethodName, endpointFilter);
              for (Object object : endpointObjects)
              {
                T t = endpoint.isOnlyLocal() ?
                  (T)endpoint.toGlobal((Class)object.getClass(), object) :
                  (T)object;
                objects.add(t);
              }
            }
            count += endpointCount;
          }
          // at least one endpoint was invoked successfully,
          // do not throw exception
          throwException = false;
        }
        catch (InvalidIdentifierException ex)
        {
          // ignore this exception, endpoint do not match filter
        }
        catch (Exception ex)
        {
          // save exception to throw it later if necessary
          exception = ex;
        }
      }
    }
    if (exception != null && throwException)
    {
      // Exception is thrown when all endpoints throw an exception
      throw WSExceptionFactory.create(exception);
    }
    return objects;
  }

  protected WSDirectory getWSDirectory()
  {
    return WSDirectory.getInstance();
  }

  protected WSEndpoint getProxyEndpoint()
  {
    if (proxyEndpoint == null)
    {
      String endpointName = WSUtils.getServletAdapter(wsContext).getName();
      proxyEndpoint = WSDirectory.getInstance().getEndpoint(endpointName);
    }
    return proxyEndpoint;
  }

  protected WSEndpoint getDestinationEndpoint(Class entityClass, String typeId)
  {
    InternalEntity internalEntity =
      getWSDirectory().getInternalEntity(entityClass.getSimpleName(), typeId);
    WSEndpoint endpoint = (internalEntity == null) ?
      getDefaultEndpoint() : internalEntity.getEndpoint();
    return endpoint;
  }

  protected WSEndpoint getDestinationEnpointByTypeId(String typeId)
  {
    WSEndpoint endpoint;
    List<ExternalEntity> entities = getWSDirectory().getExternalEntities(
      Type.class, typeId, getProxyEndpoint().getServiceName());
    if (entities.size() > 0)
    {
      endpoint = entities.get(0).getEndpoint();
    }
    else
    {
      endpoint = getDefaultEndpoint();
    }
    return endpoint;
  }

  protected WSEndpoint getDefaultEndpoint()
  {
    WSEndpoint endpoint = null;
    List<WSEndpoint> endpoints =
      getWSDirectory().getEndpoints(serviceClass);
    Iterator<WSEndpoint> iter = endpoints.iterator();
    while (iter.hasNext() && endpoint == null)
    {
      WSEndpoint ep = iter.next();
      if (ep != getProxyEndpoint()) endpoint = ep;
    }
    return endpoint;
  }

  private int getFirstResult(Object filter)
  {
    try
    {
      Class filterClass = filter.getClass();
      Method method = filterClass.getMethod("getFirstResult");
      return (Integer)method.invoke(filter);
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  private int getMaxResults(Object filter)
  {
    try
    {
      Class filterClass = filter.getClass();
      Method method = filterClass.getMethod("getMaxResults");
      return (Integer)method.invoke(filter);
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  private void setFirstResult(Object filter, int firstResult)
  {
    try
    {
      Class filterClass = filter.getClass();
      Method method = filterClass.getMethod("setFirstResult",
       new Class[]{int.class});
      method.invoke(filter, new Object[]{firstResult});
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  private void setMaxResults(Object filter, int maxResults)
  {
    try
    {
      Class filterClass = filter.getClass();
      Method method = filterClass.getMethod("setMaxResults",
       new Class[]{int.class});
      method.invoke(filter, new Object[]{maxResults});
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  private int countObjects(Object port,
    String methodName, Object filter)
  {
    try
    {
      Method method = portClass.getMethod(methodName, filter.getClass());
      return (Integer)method.invoke(port, filter);
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  private List findObjects(Object port,
    String methodName, Object filter)
  {
    try
    {
      Method method = portClass.getMethod(methodName, filter.getClass());
      return (List)method.invoke(port, filter);
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }
}
