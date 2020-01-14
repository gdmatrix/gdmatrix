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

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceFeature;

/**
 *
 * @author realor
 */
public class WSEndpoint
{
  private static final int TO_LOCAL = 0;
  private static final int TO_GLOBAL = 1;

  private static final String ID_SUFFIX = "Id";
  
  private static final int CONNECT_TIMEOUT = 2 * 60000; // 2 minutes
  private static final int READ_TIMEOUT = 3 * 60000; // 3 minutes

  private WSDirectory directory;
  private String name;
  private QName serviceName;
  private URL url;
  private URL wsdlLocation;
  private String description;
  private boolean onlyLocal;
  private HashMap<String, InternalEntity> internalEntities =
    new HashMap<String, InternalEntity>();
  private HashMap<String, ExternalEntity> externalEntities =
    new HashMap<String, ExternalEntity>();
  private Service service;

  private static final HashMap<String, String> expansionMap = new HashMap();
  private static final HashSet<String> externalEntityIdSet = new HashSet();

  static
  {
    expansionMap.put("Doc", "Document");
    expansionMap.put("CaseDoc", "CaseDocument");
    expansionMap.put("Prob", "Problem");
    expansionMap.put("Int", "Intervention");
    expansionMap.put("Trans", "Translation");

    externalEntityIdSet.add("RelCaseId");
  }

  WSEndpoint(WSDirectory directory, String name, QName serviceName)
  {
    this.directory = directory;
    this.name = name;
    this.serviceName = serviceName;
  }

  public WSDirectory getDirectory()
  {
    return directory;
  }
  
  public String getName()
  {
    return name;
  }

  public QName getServiceName()
  {
    return serviceName;
  }

  public URL getUrl()
  {
    return url;
  }

  public void setUrl(URL url)
  {
    this.url = url;
  }

  public URL getWsdlLocation()
  {
    return wsdlLocation;
  }

  public void setWsdlLocation(URL wsdlLocation)
  {
    this.wsdlLocation = wsdlLocation;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public boolean isOnlyLocal()
  {
    return onlyLocal;
  }

  public void setOnlyLocal(boolean onlyLocal)
  {
    this.onlyLocal = onlyLocal;
  }

  public Service getService()
  {
    if (service == null)
    {
      service = Service.create(wsdlLocation, serviceName);
    }
    return service;
  }

  public Collection<Entity> getEntities()
  {
    ArrayList<Entity> entities = new ArrayList<Entity>();
    entities.addAll(internalEntities.values());
    entities.addAll(externalEntities.values());
    return entities;
  }

  public Collection<InternalEntity> getInternalEntities()
  {
    return internalEntities.values();
  }

  public Collection<ExternalEntity> getExternalEntities()
  {
    return externalEntities.values();
  }
  
  public Entity getEntity(String name)
  {
    Entity entity = internalEntities.get(name);
    if (entity == null)
    {
      entity = externalEntities.get(name);
    }
    return entity;
  }

  public Entity getEntity(Class entityClass)
  {
    return getEntity(entityClass.getSimpleName());
  }

  public InternalEntity getInternalEntity(String name)
  {
    return internalEntities.get(name);
  }

  public InternalEntity getInternalEntity(Class entityClass)
  {
    return internalEntities.get(entityClass.getSimpleName());
  }

  public ExternalEntity getExternalEntity(String name)
  {
    return externalEntities.get(name);
  }

  public ExternalEntity getExternalEntity(Class entityClass)
  {
    return externalEntities.get(entityClass.getSimpleName());
  }

  public InternalEntity newInternalEntity(String name)
  {
    InternalEntity entity = new InternalEntity(this, name);
    internalEntities.put(name, entity);
    return entity;
  }

  public ExternalEntity newExternalEntity(String name)
  {
    ExternalEntity entity = new ExternalEntity(this, name);
    externalEntities.put(name, entity);
    return entity;
  }

  @Override
  public String toString()
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append("Endpoint(");
    buffer.append(name);
    if (description != null) buffer.append(",").append(description);
    buffer.append(",").append(serviceName);
    buffer.append(",").append(wsdlLocation);
    if (url != null) buffer.append(",").append(url);
    buffer.append(",\n");
    for (Entity entity : internalEntities.values())
    {
      buffer.append(entity).append("\n");
    }
    for (Entity entity : externalEntities.values())
    {
      buffer.append(entity).append("\n");
    }
    buffer.append(")");
    return buffer.toString();
  }

  // Port obtention methods

  public <T> T getPort(Class<T> portClass)
  {
    return getPort(portClass, null, null);
  }

  public <T> T getPort(Class<T> portClass, String userId, String password,
    WebServiceFeature ... features)
  {
    T port = getService().getPort(portClass, features);

    Map requestContext = ((BindingProvider)port).getRequestContext();
    // endpoint url
    if (url != null)
    {
      requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, 
        url.toString());
    }
    // authentication
    if (userId != null && userId.trim().length() > 0)
    {
      requestContext.put(BindingProvider.USERNAME_PROPERTY, userId);
      requestContext.put(BindingProvider.PASSWORD_PROPERTY,
        password == null ? "" : password);
    }
    // set connection and read timeouts
    requestContext.put("com.sun.xml.ws.connect.timeout", CONNECT_TIMEOUT);
    requestContext.put("com.sun.xml.ws.request.timeout", READ_TIMEOUT);

    return port;
  }

  // id conversion methods

  public String toLocalId(String entityName, String globalId)
  {
    return convertId(entityName, globalId, TO_LOCAL);
  }

  public String toLocalId(Class entityClass, String globalId)
  {
    return convertId(entityClass, globalId, TO_LOCAL);
  }

  public String toGlobalId(String entityName, String localId)
  {
    return convertId(entityName, localId, TO_GLOBAL);
  }

  public String toGlobalId(Class entityClass, String localId)
  {
    return convertId(entityClass, localId, TO_GLOBAL);
  }

  public List<String> toLocalIds(String entityName, List<String> globalIds)
  {
    return convertIds(entityName, globalIds, TO_LOCAL);
  }

  public List<String> toLocalIds(Class entityClass, List<String> globalIds)
  {
    return convertIds(entityClass, globalIds, TO_LOCAL);
  }

  public List<String> toGlobalIds(String entityName, List<String> localIds)
  {
    return convertIds(entityName, localIds, TO_GLOBAL);
  }

  public List<String> toGlobalIds(Class entityClass, List<String> localIds)
  {
    return convertIds(entityClass, localIds, TO_GLOBAL);
  }

  public <T> T toLocal(Class<T> entityClass, T global)
  {
    return convert(entityClass, global, TO_LOCAL);
  }

  public <T> T toGlobal(Class<T> entityClass, T local)
  {
    return convert(entityClass, local, TO_GLOBAL);
  }

  // *** private methods ****

  private String convertId(String entityName, String id, int mode)
  {
    String convId;
    Entity entity = getEntity(entityName);
    if (entity != null)
    {
      if (id != null)
      {
        convId = (mode == TO_LOCAL) ?
          entity.toLocalId(id) : entity.toGlobalId(id);
      }
      else convId = id;
    }
    else convId = id;
    return convId;
  }

  private String convertId(Class entityClass, String id, int mode)
  {
    return convertId(entityClass.getSimpleName(), id, mode);
  }

  private List<String> convertIds(String entityName, List<String> ids, int mode)
  {
    List<String> convIds;
    Entity entity = getEntity(entityName);
    if (entity != null)
    {
      if (ids != null && ids.size() > 0)
      {
        convIds = new ArrayList<String>();
        for (String id : ids)
        {
          String convId = (mode == TO_LOCAL) ?
            entity.toLocalId(id) : entity.toGlobalId(id);
          convIds.add(convId);
        }
      }
      else convIds = ids;
    }
    else convIds = ids;
    return convIds;
  }

  private List<String> convertIds(Class entityClass, List<String> ids, int mode)
  {
    return convertIds(entityClass.getSimpleName(), ids, mode);
  }

  private <T> T convert(Class<T> entityClass, T source, int mode)
  {
    try
    {
      T dest;
      if (source == null) dest = null;
      else if (isBasicType(entityClass) || entityClass.isEnum())
      {
        // basic type do not require conversion
        dest = source;
      }
      else if (source instanceof List) // source is a List
      {
        List sourceList = (List)source;
        if (sourceList.size() > 0) // is not empty?
        {
          dest = entityClass.newInstance();
          for (Object elem : sourceList)
          {
            if (elem == null)
            {
              ((List)dest).add(null);
            }
            else
            {
              ((List)dest).add(convert((Class)elem.getClass(), elem, mode));
            }
          }
        }
        else dest = source; // return empty list
      }
      else // POJO
      {
        // conversion needed
        dest = entityClass.newInstance();
        Method[] methods = entityClass.getMethods();
        for (Method method : methods)
        {
          convertProperty(entityClass, method, source, dest, mode);
        }
      }
      return dest;
    }
    catch (RuntimeException ex)
    {
      throw ex;
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  private void convertProperty(Class entityClass, Method method,
    Object source, Object dest, int mode) throws Exception
  {
    String methodName = method.getName();
    Class[] methodParams = method.getParameterTypes();
    if (methodParams.length == 0 && 
      (methodName.startsWith("get") || methodName.startsWith("is")))
    {
      Method getter = method;
      Method setter = getSetter(entityClass, getter);

      Object value = getter.invoke(source);

      if (methodName.endsWith(ID_SUFFIX)) // Id property
      {
        String propertyName = methodName.startsWith("get") ?
          methodName.substring(3) : methodName.substring(2);

        Entity entity = findEntity(propertyName);

        if (value instanceof String && setter != null)
        {
          String convId;
          if (entity != null)
          {
            String id = (String)value;
            convId = (mode == TO_LOCAL) ?
              entity.toLocalId(id) : entity.toGlobalId(id);
          }
          else convId = (String)value; // entity not found
          setter.invoke(dest, convId);
        }
        else if (value instanceof List) // value instanceof List<String>
        {
          // convertProperty idList
          List<String> sourceList = (List<String>)value;
          List<String> destList = (List<String>)getter.invoke(dest);
          for (String id : sourceList)
          {
            String convId;
            if (entity != null)
            {
              convId = (mode == TO_LOCAL) ?
                entity.toLocalId(id) : entity.toGlobalId(id);
            }
            else convId = id;
            destList.add(convId);
          }
        }
      }
      else if (value != null) // non Id property
      {
        if (setter != null) // single property
        {
          Object convValue = convert((Class)value.getClass(), value, mode);
          setter.invoke(dest, convValue);
        }
        else if (value instanceof List) // list property
        {
          List convList = 
            (List)convert((Class)value.getClass(), value, mode);
          List destList = (List)getter.invoke(dest);
          destList.addAll(convList);
        }
      }
    }
  }

  private boolean isBasicType(Class cls)
  {
    if (cls == String.class) return true;
    if (cls == Boolean.class) return true;
    if (cls == boolean.class) return true;
    if (Number.class.isAssignableFrom(cls)) return true;
    return false;
  }

  private Method getSetter(Class entityClass, Method getter)
  {
    Method setter;
    try
    {
      String setterMethodName;
      if (getter.getName().startsWith("get"))
      {
        setterMethodName = "set" + getter.getName().substring(3);
      }
      else // starts with "is"
      {
        setterMethodName = "set" + getter.getName().substring(2);
      }
      setter = entityClass.getMethod(setterMethodName, getter.getReturnType());
    }
    catch (NoSuchMethodException ex)
    {
      setter = null;
    }
    return setter;
  }

  private Entity findEntity(String propertyName)
  {
    Entity entity = null;
    String str = propertyName.substring(0,
      propertyName.length() - ID_SUFFIX.length());
    do
    {
      String n = expandEntityName(str);
      str = nextEntityName(str);
      entity = isExternalEntityId(propertyName) ?
        getExternalEntity(n) : getEntity(n);
    }
    while (entity == null && str.length() > 0);    
    return entity;
  }

  private String expandEntityName(String shortEntityName)
  {
    String entityName = expansionMap.get(shortEntityName);
    return (entityName == null) ? shortEntityName : entityName;
  }

  private boolean isExternalEntityId(String propertyName)
  {
    return externalEntityIdSet.contains(propertyName);
  }

  private String nextEntityName(String str)
  {
    int i = 1;
    boolean found = false;
    while (i < str.length() && !found)
    {
      if (Character.isUpperCase(str.charAt(i))) found = true;
      else i++;
    }
    return str.substring(i);
  }
}
