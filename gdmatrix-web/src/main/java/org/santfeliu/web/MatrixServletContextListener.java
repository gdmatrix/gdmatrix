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
package org.santfeliu.web;

import java.net.URL;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.matrix.util.WSDirectory;
import org.santfeliu.util.MatrixConfig;

/**
 *
 * @author realor
 */
public class MatrixServletContextListener implements ServletContextListener
{
  public static final String CONTEXT_NAME = "contextName";
  public static final String CONTEXT_PATH = "contextPath";

  static final Logger logger = Logger.getLogger("Matrix");

  public void contextInitialized(ServletContextEvent sce)
  {
    // setting properties into MatrixConfig
    ServletContext servletContext = sce.getServletContext();

    log(servletContext, Level.INFO, "Initializing MATRIX on context \"{0}\"",
      servletContext.getContextPath());

    Enumeration<String> enu = servletContext.getInitParameterNames();
    while (enu.hasMoreElements())
    {
      String name = enu.nextElement();
      String value = servletContext.getInitParameter(name);
      MatrixConfig.setProperty(name, value);
    }
    // set automatic properties
    MatrixConfig.setProperty(CONTEXT_NAME, servletContext.getServletContextName());
    MatrixConfig.setProperty(CONTEXT_PATH, servletContext.getContextPath());
    
    // set default WSDirectory instance
    try
    {
      URL url = new URL(MatrixConfig.getProperty("wsdirectory.url"));
      WSDirectory.setDefaultInstanceURL(url);
    }
    catch (Exception ex)
    {
      log(servletContext, Level.SEVERE, ex.toString(),
        servletContext.getContextPath());
    }
  }

  public void contextDestroyed(ServletContextEvent sce)
  {
    ServletContext servletContext = sce.getServletContext();
    log(servletContext, Level.INFO, "Destroying MATRIX on context \"{0}\"",
      servletContext.getContextPath());
  }

  protected void log(ServletContext servletContext, Level level,
    String message, Object ... params)
  {
    logger.log(level, message, params);
    servletContext.log(MessageFormat.format(message, params));
  }
}
