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
