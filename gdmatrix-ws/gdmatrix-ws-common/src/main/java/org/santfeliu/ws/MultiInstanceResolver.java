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

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.server.Invoker;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.server.WSWebServiceContext;
import com.sun.xml.ws.server.AbstractMultiInstanceResolver;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.xml.ws.Provider;

/**
 *
 * @author realor
 */
public class MultiInstanceResolver<T> extends AbstractMultiInstanceResolver<T>
{
  protected final WSController controller;
  protected String endpointName;

  public MultiInstanceResolver(@NotNull Class<T> clazz)
  {
    // An instance of this resolver is created for each endpoint
    super(clazz);
    controller = WSController.getInstance(clazz);
  }

  @Override
  public T resolve(Packet request)
  {
    return create(); // create service instance and inject WevServiceContext
  }

  @Override
  public Invoker createInvoker()
  {
    return new Invoker()
    {
      @Override
      public void start(@NotNull WSWebServiceContext wsc,
        @NotNull WSEndpoint endpoint)
      {
        MultiInstanceResolver.this.start(wsc, endpoint);
      }

      @Override
      public void dispose()
      {
        MultiInstanceResolver.this.dispose();
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
          postInvoke(p, t);
        }
      }

      @Override
      public Object invoke(Packet packet, Method method, Object... args)
        throws InvocationTargetException, IllegalAccessException
      {
        T instance = resolve(packet); // create service instance

        if (endpointName == null)
        {
          // get endpointName on first invoke, not possible to get it on start
          endpointName = WSUtils.getServletAdapter(packet.endpoint).getName();
        }

        return controller.invoke(endpointName, instance, method, args);
      }
    };
  }
}
