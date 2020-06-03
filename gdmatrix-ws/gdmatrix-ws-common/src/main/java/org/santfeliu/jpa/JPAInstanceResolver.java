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
package org.santfeliu.jpa;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.server.Invoker;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.server.WSWebServiceContext;
import com.sun.xml.ws.server.AbstractMultiInstanceResolver;

import com.sun.xml.ws.transport.http.servlet.ServletAdapter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;

import javax.xml.ws.Provider;
import org.santfeliu.ws.WSUtils;

/**
 *
 * @author realor
 */
public class JPAInstanceResolver<T> 
  extends AbstractMultiInstanceResolver<T>
{
  protected static final Logger log = Logger.getLogger("JPAInstanceResolver");

  private String serviceName;
  private Field field;

  public JPAInstanceResolver(@NotNull Class<T> clazz)
  {
    super(clazz);
    setup(clazz);
  }

  public T resolve(Packet request)
  {
    return create();
  }
  
  @Override
  public void postInvoke(Packet packet, T instance)
  {
  }

  @Override
  public Invoker createInvoker()
  {
    return new Invoker()
    {
      ServletAdapter servletAdapter;

      @Override
      public void start(@NotNull WSWebServiceContext wsc, 
        @NotNull WSEndpoint endpoint)
      {
        log.log(Level.INFO, ">>>>> Start {0}", serviceName);
        JPAInstanceResolver.this.start(wsc, endpoint);
      }

      @Override
      public void dispose()
      {
        log.log(Level.INFO, ">>>>> Dispose {0}", serviceName);
        if (servletAdapter != null)
        {
          JPAUtils.closeEntityManagerFactory(servletAdapter.getName());
          JPAInstanceResolver.this.dispose();
        }
      }

      @Override
      public Object invoke(Packet p, Method method, Object... args)
        throws InvocationTargetException, IllegalAccessException
      {
        Object result = null;
        T instance = resolve(p);

        if (servletAdapter == null)
          servletAdapter = WSUtils.getServletAdapter(p.endpoint);

        String endpointName = servletAdapter.getName();
        log.log(Level.INFO, ">>>>> Invoke {0}.{1}",
          new Object[]{endpointName, method.getName()});

        EntityManager em = JPAUtils.createEntityManager(servletAdapter.getName());

        field.set(instance, em); // EntityManager injection
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
          postInvoke(p, instance);
        }
        return result;
      }

      @Override
      public <U> U invokeProvider(@NotNull Packet p, U arg)
      {
        T t = resolve(p);
        try
        {
          return ((Provider<U>) t).invoke(arg);
        }
        finally
        {
          postInvoke(p,t);
        }
      }

      @Override
      public String toString()
      {
        return "Persistent Invoker over " + 
          JPAInstanceResolver.this.toString();
      }
    };
  }

  private void setup(Class clazz)
  {
    PersistenceContext pc = null;
    Field[] fields = clazz.getFields();
    field = null;
    int i = 0;
    while (i < fields.length && pc == null)
    {
      field = fields[i++];
      if (field.isAnnotationPresent(PersistenceContext.class))
      {
        pc = field.getAnnotation(PersistenceContext.class);
      }
    }
    serviceName = clazz.getName();
  }
}
