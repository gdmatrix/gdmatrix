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

import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.http.servlet.ServletAdapter;
import com.sun.xml.ws.transport.http.servlet.WSServlet;
import com.sun.xml.ws.transport.http.servlet.WSServletDelegate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletContext;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

/**
 *
 * @author realor
 */
public class WSUtils
{
  public static List<ServletAdapter> getServletAdapters(ServletContext context)
  {
    WSServletDelegate delegate =
      (WSServletDelegate)context.getAttribute(WSServlet.JAXWS_RI_RUNTIME_INFO);

    return delegate.adapters;
  }

  public static List<ServletAdapter> getServletAdapters(WebServiceContext wsContext)
  {
    ServletContext context = (ServletContext)wsContext.getMessageContext().
      get(MessageContext.SERVLET_CONTEXT);

    WSServletDelegate delegate =
      (WSServletDelegate)context.getAttribute(WSServlet.JAXWS_RI_RUNTIME_INFO);

    return delegate.adapters;
  }

  public static List<ServletAdapter> getServletAdapters(
    WebServiceContext wsContext, QName serviceName)
  {
    ArrayList<ServletAdapter> servletAdapters = new ArrayList<>();
    List<ServletAdapter> adapters = getServletAdapters(wsContext);

    Iterator<ServletAdapter> iter = adapters.iterator();
    while (iter.hasNext())
    {
      ServletAdapter sa = iter.next();
      if (sa.getEndpoint().getServiceName().equals(serviceName))
      {
        servletAdapters.add(sa);
      }
    }
    return servletAdapters;
  }

  public static ServletAdapter getServletAdapter(
    WebServiceContext wsContext)
  {
    String pathInfo =
      (String)wsContext.getMessageContext().get(MessageContext.PATH_INFO);

    ServletAdapter servletAdapter = null;
    List<ServletAdapter> adapters = getServletAdapters(wsContext);

    Iterator<ServletAdapter> iter = adapters.iterator();
    while (iter.hasNext() && servletAdapter == null)
    {
      ServletAdapter sa = iter.next();
      if (sa.getValidPath().endsWith(pathInfo))
      {
        servletAdapter = sa;
      }
    }
    return servletAdapter;
  }

  public static ServletAdapter getServletAdapter(
    WebServiceContext wsContext, String name)
  {
    ServletAdapter servletAdapter = null;
    List<ServletAdapter> adapters = getServletAdapters(wsContext);

    Iterator<ServletAdapter> iter = adapters.iterator();
    while (iter.hasNext() && servletAdapter == null)
    {
      ServletAdapter sa = iter.next();
      if (sa.getName().equals(name))
      {
        servletAdapter = sa;
      }
    }
    return servletAdapter;
  }

  public static ServletAdapter getServletAdapter(
    WebServiceContext wsContext, Class implementationClass)
  {
    ServletAdapter servletAdapter = null;
    List<ServletAdapter> adapters = getServletAdapters(wsContext);

    Iterator<ServletAdapter> iter = adapters.iterator();
    while (iter.hasNext() && servletAdapter == null)
    {
      ServletAdapter sa = iter.next();
      if (sa.getEndpoint().getImplementationClass().equals(implementationClass))
      {
        servletAdapter = sa;
      }
    }
    return servletAdapter;
  }

  public static ServletAdapter getServletAdapter(WSEndpoint endpoint)
  {
    ServletAdapter servletAdapter = null;
    ServletContext servletContext =
      endpoint.getContainer().getSPI(ServletContext.class);
    List<ServletAdapter> adapters = getServletAdapters(servletContext);
    Iterator<ServletAdapter> iter = adapters.iterator();
    while (iter.hasNext() && servletAdapter == null)
    {
      ServletAdapter sa = iter.next();
      if (sa.getEndpoint() == endpoint)
      {
        servletAdapter = sa;
      }
    }
    return servletAdapter;
  }
}
