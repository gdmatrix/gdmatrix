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

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.server.InstanceResolver;
import com.sun.xml.ws.api.server.WSEndpoint;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.jpa.JPAUtils;
import org.santfeliu.ws.annotations.Disposer;
import org.santfeliu.ws.annotations.Initializer;
import org.santfeliu.ws.annotations.State;

/**
 *
 * @author realor
 */
public class WSController
{
  protected static final Logger LOGGER = Logger.getLogger("WSController");

  private static final HashMap<WSEndpoint, WSController> instances =
    new HashMap<>();

  protected WSEndpoint endpoint;
  protected String endpointName;
  protected Object[] state;
  protected InstanceResolver instanceResolver;

  protected Method initializerMethod;
  protected Method disposerMethod;

  protected Field entityManagerField;
  protected Field[] stateFields;
  protected String unitName;

  public static synchronized WSController getInstance(WSEndpoint endpoint)
  {
    WSController controller = instances.get(endpoint);
    if (controller == null)
    {
      controller = new WSController(endpoint);
      instances.put(endpoint, controller);
    }
    return controller;
  }

  WSController(WSEndpoint endpoint)
  {
    this.endpoint = endpoint;
    setup(endpoint.getImplementationClass());
  }

  private void setup(Class clazz)
  {
    List<Field> stateFieldList = new ArrayList<>();
    Field[] fields = clazz.getDeclaredFields();
    for (Field field : fields)
    {
      if (field.isAnnotationPresent(PersistenceContext.class))
      {
        field.setAccessible(true);
        entityManagerField = field;
        PersistenceContext pc = field.getAnnotation(PersistenceContext.class);
        unitName = pc.unitName();
      }
      else if (field.isAnnotationPresent(State.class))
      {
        field.setAccessible(true);
        stateFieldList.add(field);
      }
    }
    int count = stateFieldList.size();
    stateFields = stateFieldList.toArray(new Field[count]);
    state = new Object[count];

    Method[] methods = clazz.getMethods();
    for (Method method : methods)
    {
      if (method.isAnnotationPresent(Initializer.class))
      {
        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length == 1 && paramTypes[0].equals(String.class))
        {
          initializerMethod = method;
        }
      }
      else if (method.isAnnotationPresent(Disposer.class))
      {
        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length == 1 && paramTypes[0].equals(String.class))
        {
          disposerMethod = method;
        }
      }
      if (initializerMethod != null && disposerMethod != null) break;
    }
  }

  public WSEndpoint getEndpoint()
  {
    return endpoint;
  }

  public String getEndpointName()
  {
    return endpointName;
  }

  public void setEndpointName(String endpointName)
  {
    this.endpointName = endpointName;
  }

  public InstanceResolver getInstanceResolver()
  {
    return instanceResolver;
  }

  public void setInstanceResolver(InstanceResolver instanceResolver)
  {
    this.instanceResolver = instanceResolver;
  }

  public void initialize()
  {
    LOGGER.log(Level.INFO, ">>>>> Initializing endpoint {0}", endpointName);
    try
    {
      if (initializerMethod != null)
      {
        Object instance = instanceResolver.resolve(new Packet());
        try
        {
          if (entityManagerField == null)
          {
            initializerMethod.invoke(instance, endpointName);
          }
          else
          {
            invokeJPA(instance, initializerMethod, endpointName);
          }
          LOGGER.log(Level.INFO, "Endpoint {0} initialized.", endpointName);
        }
        finally
        {
          saveState(instance);
        }
      }
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "Endpoint {0} initialization failed: {1}",
        new Object[]{endpointName, ex.toString()});
    }
  }

  public void dispose()
  {
    LOGGER.log(Level.INFO, ">>>>> Disposing endpoint {0}", endpointName);
    try
    {
      if (disposerMethod != null)
      {
        Object instance = instanceResolver.resolve(new Packet());
        if (entityManagerField == null)
        {
          disposerMethod.invoke(instance, endpointName);
        }
        else
        {
          invokeJPA(instance, disposerMethod, endpointName);
        }
        LOGGER.log(Level.INFO, "Endpoint {0} disposed.", endpointName);
      }
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "Endpoint {0} dispose failed: {1}",
        new Object[]{endpointName, ex.toString()});
    }
    finally
    {
      if (entityManagerField != null)
      {
        String unit = StringUtils.isBlank(unitName) ? endpointName : unitName;

        JPAUtils.closeEntityManagerFactory(unit, endpointName);
      }
    }
  }

  public Object invoke(Object instance, Method method, Object... args)
    throws IllegalArgumentException, IllegalAccessException,
    InvocationTargetException
  {
    Object result;

    LOGGER.log(Level.INFO, ">>>>> Invoke {0}.{1}",
      new Object[]{endpointName, method.getName()});

    try
    {
      injectState(instance);

      if (entityManagerField == null)
      {
        result = method.invoke(instance, args);
      }
      else
      {
        result = invokeJPA(instance, method, args);
      }
    }
    finally
    {
      saveState(instance);
    }
    return result;
  }

  protected EntityManager injectEntityManager(String endpointName,
    Object instance) throws IllegalArgumentException, IllegalAccessException
  {
    String unit = StringUtils.isBlank(unitName) ? endpointName : unitName;

    EntityManager em = JPAUtils.createEntityManager(unit, endpointName);
    entityManagerField.set(instance, em);

    return em;
  }

  protected void injectState(Object instance)
    throws IllegalArgumentException, IllegalAccessException
  {
    if (stateFields != null)
    {
      for (int i = 0; i < stateFields.length; i++)
      {
        Field field = stateFields[i];
        field.set(instance, state[i]);
      }
    }
  }

  protected void saveState(Object instance)
    throws IllegalArgumentException, IllegalAccessException
  {
    if (stateFields != null)
    {
      for (int i = 0; i < stateFields.length; i++)
      {
        Field field = stateFields[i];
        state[i] = field.get(instance);
      }
    }
  }

  protected Object invokeJPA(Object instance,
    Method method, Object...args) throws IllegalArgumentException,
    IllegalAccessException, InvocationTargetException
  {
    Object result = null;
    EntityManager em = injectEntityManager(endpointName, instance);
    try
    {
      EntityTransaction tx = em.getTransaction();
      try
      {
        tx.begin();
        result = method.invoke(instance, args);
        if (tx.isActive()) tx.commit();
      }
      finally
      {
        if (tx.isActive()) tx.rollback();
      }
    }
    finally
    {
      em.close();
    }
    return result;
  }
}
