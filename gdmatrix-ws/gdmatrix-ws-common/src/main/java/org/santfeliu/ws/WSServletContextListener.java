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

import com.sun.xml.ws.transport.http.servlet.*;
import com.sun.istack.NotNull;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.resources.WsservletMessages;
import com.sun.xml.ws.transport.http.DeploymentDescriptorParser;

import java.io.File;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.xml.ws.WebServiceException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.santfeliu.util.MatrixConfig;

/**
 *
 * @author realor
 */
public final class WSServletContextListener
  implements ServletContextAttributeListener, ServletContextListener
{
  private WSServletDelegate delegate;
  private static final String JAXWS_RI_RUNTIME = "/WEB-INF/sun-jaxws.xml";
  private static final Logger logger = Logger.getLogger(
    com.sun.xml.ws.util.Constants.LoggingDomain + ".server.http");

  public void contextInitialized(ServletContextEvent event)
  {
    if (logger.isLoggable(Level.INFO))
    {
      logger.info(WsservletMessages.LISTENER_INFO_INITIALIZE());
    }
    ServletContext context = event.getServletContext();
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    if (classLoader == null)
    {
      classLoader = getClass().getClassLoader();
    }
    try
    {
      // Parse the descriptor file and build endpoint infos
      DeploymentDescriptorParser<ServletAdapter> parser =
        new DeploymentDescriptorParser<ServletAdapter>(
        classLoader, new ServletResourceLoader(context),
        createContainer(context), new ServletAdapterList());
      
      URL sunJaxWsXml;

      File matrixDir = MatrixConfig.getDirectory();
      File jaxwsFile = new File(matrixDir, "sun-jaxws.xml");
      if (jaxwsFile.exists() && jaxwsFile.isFile())
      {
        sunJaxWsXml = jaxwsFile.toURI().toURL();
      }
      else
      {
        sunJaxWsXml = context.getResource(JAXWS_RI_RUNTIME);
        if (sunJaxWsXml == null)
        {
          throw new WebServiceException(
            WsservletMessages.NO_SUNJAXWS_XML(JAXWS_RI_RUNTIME));
        }
      }
      List<ServletAdapter> adapters = 
        parser.parse(sunJaxWsXml.toExternalForm(), sunJaxWsXml.openStream());

      delegate = createDelegate(adapters, context);

      context.setAttribute(WSServlet.JAXWS_RI_RUNTIME_INFO, delegate);
    }
    catch (Throwable e)
    {
      logger.log(Level.SEVERE,
              WsservletMessages.LISTENER_PARSING_FAILED(e), e);
      context.removeAttribute(WSServlet.JAXWS_RI_RUNTIME_INFO);
      throw new WebServiceException("listener.parsingFailed", e);
    }
  }

  public void contextDestroyed(ServletContextEvent event)
  {
    if (delegate != null)
    { // the deployment might have failed.
      delegate.destroy();
    }

    if (logger.isLoggable(Level.INFO))
    {
      logger.info(WsservletMessages.LISTENER_INFO_DESTROY());
    }
  }

  public void attributeAdded(ServletContextAttributeEvent event)
  {
  }

  public void attributeRemoved(ServletContextAttributeEvent event)
  {
  }

  public void attributeReplaced(ServletContextAttributeEvent event)
  {
  }

  /**
   * Creates {@link Container} implementation that hosts the JAX-WS endpoint.
   */
  protected
  @NotNull
  Container createContainer(ServletContext context)
  {
    return new ServletContainer(context);
  }

  /**
   * Creates {@link WSServletDelegate} that does the real work.
   */
  protected
  @NotNull
  WSServletDelegate createDelegate(List<ServletAdapter> adapters, ServletContext context)
  {
    return new WSServletDelegate(adapters, context);
  }
}
