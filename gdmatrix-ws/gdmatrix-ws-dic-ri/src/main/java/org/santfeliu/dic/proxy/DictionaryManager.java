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
package org.santfeliu.dic.proxy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.jws.WebService;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.DictionaryManagerPort;
import org.matrix.dic.DictionaryManagerService;
import org.matrix.dic.EnumType;
import org.matrix.dic.EnumTypeFilter;
import org.matrix.dic.EnumTypeItem;
import org.matrix.dic.EnumTypeItemFilter;
import org.matrix.dic.Property;
import org.matrix.dic.Type;
import org.matrix.dic.TypeFilter;
import org.matrix.util.InternalEntity;
import org.matrix.util.InvalidIdentifierException;
import org.matrix.util.WSEndpoint;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.security.util.SecurityUtils;
import org.santfeliu.ws.WSProxy;

/**
 *
 * @author realor
 */
@WebService(endpointInterface = "org.matrix.dic.DictionaryManagerPort")
public class DictionaryManager extends WSProxy implements DictionaryManagerPort
{
  public DictionaryManager()
  {
    super(DictionaryManagerService.class, DictionaryManagerPort.class);
  }

  public Type loadType(String typeId)
  {
    if (typeId == null) return null;
    // find endpoint
    WSEndpoint endpoint = getDestinationEndpoint(typeId);

    // get port
    DictionaryManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      String id = endpoint.toLocalId(Type.class, typeId);
      Type type = port.loadType(id);
      return TypeConverter.toGlobal(type, endpoint);
    }
    else return port.loadType(typeId);
  }

  public Type storeType(Type type)
  {
    // find endpoint
    String typeId = type.getTypeId();
    WSEndpoint endpoint = getDestinationEndpoint(typeId);

    // get port
    DictionaryManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      type = TypeConverter.toLocal(type, endpoint);
      type = port.storeType(type);
      return TypeConverter.toGlobal(type, endpoint);
    }
    else return port.storeType(type);
  }

  public boolean removeType(String typeId)
  {
    // find endpoint
    WSEndpoint endpoint = getDestinationEndpoint(typeId);

    // get port
    DictionaryManagerPort port = getPort(endpoint);

    if (endpoint.isOnlyLocal())
    {
      String id = endpoint.toLocalId(Type.class, typeId);
      return port.removeType(id);
    }
    else return port.removeType(typeId);
  }

  public int countTypes(TypeFilter filter)
  {
    int count = 0;
    Credentials credentials = SecurityUtils.getCredentials(wsContext);
    List<WSEndpoint> endpoints =
      getWSDirectory().getEndpoints(DictionaryManagerService.class);
    for (WSEndpoint endpoint : endpoints)
    {
      if (endpoint != getProxyEndpoint()) // skip proxy
      {
        try
        {
          // force filter conversion to check if endpoint accepts call
          TypeFilter endpointFilter =
            TypeConverter.toLocal(filter, endpoint);

          if (!endpoint.isOnlyLocal()) endpointFilter = filter;

          DictionaryManagerPort port = getPort(endpoint, credentials);
          count += port.countTypes(endpointFilter);
        }
        catch (InvalidIdentifierException ex)
        {
          // ignore exception
        }
      }
    }
    return count;
  }

  public List<Type> findTypes(TypeFilter filter)
  {
    List<Type> types = new ArrayList<Type>();
    Credentials credentials = SecurityUtils.getCredentials(wsContext);
    List<WSEndpoint> endpoints =
      getWSDirectory().getEndpoints(DictionaryManagerService.class);
    Iterator<WSEndpoint> iter = endpoints.iterator();
    int count = 0;
    while (iter.hasNext() && 
      (types.size() < filter.getMaxResults() || filter.getMaxResults() == 0))
    {
      WSEndpoint endpoint = iter.next();
      if (endpoint != getProxyEndpoint()) // skip proxy
      {
        try
        {
          // force filter conversion to check if endpoint accepts call
          TypeFilter endpointFilter = 
            TypeConverter.toLocal(filter, endpoint);

          if (!endpoint.isOnlyLocal()) endpointFilter = filter;

          DictionaryManagerPort port = getPort(endpoint, credentials);          
          // count types
          int endpointCount = port.countTypes(endpointFilter);
          if (filter.getFirstResult() < count + endpointCount)
          {
            int firstResult = (filter.getFirstResult() > count) ?
              filter.getFirstResult() - count : 0;
            int maxResults = (filter.getMaxResults() == 0) ?
               0 : filter.getMaxResults() - types.size();
            endpointFilter.setFirstResult(firstResult);
            endpointFilter.setMaxResults(maxResults);
            
            // find types
            List<Type> endpointTypes = port.findTypes(endpointFilter);
            for (Type type : endpointTypes)
            {
              if (endpoint.isOnlyLocal())
              {
                types.add(TypeConverter.toGlobal(type, endpoint));
              }
              else types.add(type);
            }
          }
          count += endpointCount;
        }
        catch (InvalidIdentifierException ex)
        {
          // ignore error
        }
      }
    }
    return types;
  }

  public List<Property> initProperties(String typeId, List<Property> property)
  {
    if (typeId == null) return null;
    // find endpoint
    WSEndpoint endpoint = getDestinationEndpoint(typeId);

    // get port
    DictionaryManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      String id = endpoint.toLocalId(Type.class, typeId);
      return port.initProperties(id, property);
    }
    else return port.initProperties(typeId, property);
  }

  public List<Property> completeProperties(String typeId,
    List<Property> property)
  {
    if (typeId == null) return null;
    // find endpoint
    WSEndpoint endpoint = getDestinationEndpoint(typeId);

    // get port
    DictionaryManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      String id = endpoint.toLocalId(Type.class, typeId);
      return port.completeProperties(id, property);
    }
    else return port.completeProperties(typeId, property);
  }

  public List<String> getTypeActions(String typeId)
  {
    ArrayList<String> actions = new ArrayList();
    if (typeId != null)
    {
      // find endpoint
      WSEndpoint endpoint = getDestinationEndpoint(typeId);

      // get port
      DictionaryManagerPort port = getPort(endpoint);

      // do call
      if (endpoint.isOnlyLocal())
      {
        String id = endpoint.toLocalId(Type.class, typeId);
        actions.addAll(port.getTypeActions(id));
      }
      else actions.addAll(port.getTypeActions(typeId));
    }
    return actions;
  }

  public List<String> listModifiedTypes(String dateTime1, String dateTime2)
  {
    ArrayList<String> list = new ArrayList<String>();
    Credentials credentials = SecurityUtils.getCredentials(wsContext);
    List<WSEndpoint> endpoints =
      getWSDirectory().getEndpoints(DictionaryManagerService.class);
    for (WSEndpoint endpoint : endpoints)
    {
      if (endpoint != getProxyEndpoint()) // skip proxy
      {
        try
        {
          DictionaryManagerPort port = getPort(endpoint, credentials);
          List<String> endpointList =
            port.listModifiedTypes(dateTime1, dateTime2);
          for (String typeId : endpointList)
          {
            if (endpoint.isOnlyLocal())
            {
              list.add(endpoint.toGlobalId(Type.class, typeId));
            }
            else list.add(typeId);
          }
        }
        catch (InvalidIdentifierException ex)
        {
          // ignore
        }
      }
    }
    return list;
  }

  /* private methods */

  private WSEndpoint getDestinationEndpoint(String typeId)
  {
    InternalEntity internalEntity =
      getWSDirectory().getInternalEntity(DictionaryConstants.TYPE_TYPE, typeId);
    WSEndpoint endpoint = (internalEntity == null) ?
      getDefaultEndpoint() : internalEntity.getEndpoint();
    return endpoint;
  }

  private DictionaryManagerPort getPort(WSEndpoint endpoint)
  {
    Credentials credentials = SecurityUtils.getCredentials(wsContext);
    return getPort(endpoint, credentials);
  }

  private DictionaryManagerPort getPort(WSEndpoint endpoint,
    Credentials credentials)
  {
    DictionaryManagerPort port =
      endpoint.getPort(DictionaryManagerPort.class,
      credentials.getUserId(), credentials.getPassword());
    return port;
  }

  public EnumType loadEnumType(String enumTypeId)
  {
    if (enumTypeId == null) return null;
    // find endpoint
    WSEndpoint endpoint = getDestinationEndpoint(EnumType.class, enumTypeId);

    // get port
    DictionaryManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      String id = endpoint.toLocalId(EnumType.class, enumTypeId);
      EnumType enumType = port.loadEnumType(id);
      return endpoint.toGlobal(EnumType.class, enumType);
    }
    else return port.loadEnumType(enumTypeId);
  }

  public EnumType storeEnumType(EnumType enumType)
  {
    // find endpoint
    String enumTypeId = enumType.getEnumTypeId();
    WSEndpoint endpoint = getDestinationEndpoint(EnumType.class, enumTypeId);

    // get port
    DictionaryManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      enumType = endpoint.toLocal(EnumType.class, enumType);
      enumType = port.storeEnumType(enumType);
      return endpoint.toGlobal(EnumType.class, enumType);
    }
    else return port.storeEnumType(enumType);
  }

  public boolean removeEnumType(String enumTypeId)
  {
    // find endpoint
    WSEndpoint endpoint = getDestinationEndpoint(EnumType.class, enumTypeId);

    // get port
    DictionaryManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      String id = endpoint.toLocalId(EnumType.class, enumTypeId);
      return port.removeEnumType(id);
    }
    else return port.removeEnumType(enumTypeId);
  }

  public int countEnumTypes(EnumTypeFilter filter)
  {
    return count("countEnumTypes", filter);
  }

  public List<EnumType> findEnumTypes(EnumTypeFilter filter)
  {
    return find("countEnumTypes", "findEnumTypes", filter, EnumType.class);
  }

  public EnumTypeItem loadEnumTypeItem(String enumTypeItemId)
  {
    if (enumTypeItemId == null) return null;
    // find endpoint
    WSEndpoint endpoint = getDestinationEndpoint(EnumTypeItem.class,
      enumTypeItemId);

    // get port
    DictionaryManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      String id = endpoint.toLocalId(EnumTypeItem.class, enumTypeItemId);
      EnumTypeItem enumTypeItem = port.loadEnumTypeItem(id);
      return endpoint.toGlobal(EnumTypeItem.class, enumTypeItem);
    }
    else return port.loadEnumTypeItem(enumTypeItemId);
  }

  public EnumTypeItem storeEnumTypeItem(EnumTypeItem enumTypeItem)
  {
    // find endpoint
    String enumTypeItemId = enumTypeItem.getEnumTypeItemId();
    WSEndpoint endpoint;
    if (enumTypeItemId != null)
    {
      endpoint = getDestinationEndpoint(EnumTypeItem.class, enumTypeItemId);
    }
    else
    {
      // new enum type item
      String enumTypeId = enumTypeItem.getEnumTypeId();
      endpoint = getDestinationEndpoint(EnumType.class, enumTypeId);
    }
    // get port
    DictionaryManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      enumTypeItem = endpoint.toLocal(EnumTypeItem.class, enumTypeItem);
      enumTypeItem = port.storeEnumTypeItem(enumTypeItem);
      return endpoint.toGlobal(EnumTypeItem.class, enumTypeItem);
    }
    else return port.storeEnumTypeItem(enumTypeItem);
  }

  public boolean removeEnumTypeItem(String enumTypeItemId)
  {
    // find endpoint
    WSEndpoint endpoint = getDestinationEndpoint(EnumTypeItem.class,
      enumTypeItemId);

    // get port
    DictionaryManagerPort port = getPort(endpoint);

    // do call
    if (endpoint.isOnlyLocal())
    {
      String id = endpoint.toLocalId(EnumTypeItem.class, enumTypeItemId);
      return port.removeEnumTypeItem(id);
    }
    else return port.removeEnumTypeItem(enumTypeItemId);
  }

  public int countEnumTypeItems(EnumTypeItemFilter filter)
  {
    return count("countEnumTypeItems", filter);
  }

  public List<EnumTypeItem> findEnumTypeItems(EnumTypeItemFilter filter)
  {
    return find("countEnumTypeItems", "findEnumTypeItems", filter,
      EnumTypeItem.class);
  }
}
