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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
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

  private static final HashMap<String, WSController> instances =
    new HashMap<>();

  protected Class clazz;
  protected Method initializerMethod;
  protected Method disposerMethod;

  protected Field entityManagerField;
  protected Field stateField;
  protected String unitName;

  public static synchronized WSController getInstance(Class clazz)
  {
    String className = clazz.getName();
    WSController controller = instances.get(className);
    if (controller == null)
    {
      controller = new WSController(clazz);
      instances.put(className, controller);
    }
    return controller;
  }

  WSController(Class clazz)
  {
    this.clazz = clazz;
    setup(clazz);
  }

  private void setup(Class clazz)
  {
    Field[] fields = clazz.getFields();
    for (Field field : fields)
    {
      if (field.isAnnotationPresent(PersistenceContext.class))
      {
        PersistenceContext pc = field.getAnnotation(PersistenceContext.class);
        entityManagerField = field;
        unitName = pc.unitName();
      }
      else if (field.isAnnotationPresent(State.class))
      {
        stateField = field;
      }
      if (stateField != null && entityManagerField != null) break;
    }

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

  public void initialize(String endpointName)
    throws InstantiationException, IllegalAccessException,
    IllegalArgumentException, InvocationTargetException
  {
    if (initializerMethod != null)
    {
      Object instance = clazz.newInstance();
      try
      {
        if (entityManagerField == null)
        {
          initializerMethod.invoke(instance, endpointName);
        }
        else
        {
          invokeJPA(endpointName, instance, initializerMethod, endpointName);
        }
      }
      finally
      {
        saveState(endpointName, instance);
      }
    }
  }

  public void dispose(String endpointName) throws InstantiationException,
    IllegalAccessException, IllegalArgumentException, InvocationTargetException
  {
    try
    {
      if (disposerMethod != null)
      {
        Object instance = clazz.newInstance();
        if (entityManagerField == null)
        {
          disposerMethod.invoke(instance, endpointName);
        }
        else
        {
          invokeJPA(endpointName, instance, disposerMethod, endpointName);
        }
      }
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

  public Object invoke(String endpointName, Object instance, Method method,
    Object... args) throws IllegalArgumentException, IllegalAccessException,
    InvocationTargetException
  {
    Object result;

    LOGGER.log(Level.INFO, ">>>>>>>>>>> Invoke {0}.{1}",
      new Object[]{endpointName, method.getName()});

    try
    {
      injectState(endpointName, instance);

      if (entityManagerField == null)
      {
        result = method.invoke(instance, args);
      }
      else
      {
        result = invokeJPA(endpointName, instance, method, args);
      }
    }
    finally
    {
      saveState(endpointName, instance);
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

  protected void injectState(String endpointName, Object instance)
    throws IllegalArgumentException, IllegalAccessException
  {
    if (stateField != null)
    {
      Object state = StateStore.getState(endpointName);
      stateField.set(instance, state);
    }
  }

  protected void saveState(String endpointName, Object instance)
    throws IllegalArgumentException, IllegalAccessException
  {
    if (stateField != null)
    {
      Object state = stateField.get(instance);
      StateStore.putState(endpointName, state);
    }
  }

  protected Object invokeJPA(String endpointName, Object instance,
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
