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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.log.CSVLogger;

/**
 *
 * @author realor
 */
public class WebAuditor
{
  protected static CSVLogger logger;
  public static final String FORCED_LANGUAGE =
    "org.santfeliu.web.forcedLanguage";
  public static final String LOG_CONFIG =
    "org.santfeliu.web.logConfig";

  public WebAuditor()
  {
    String logConfig = MatrixConfig.getPathProperty(LOG_CONFIG);
    if (logConfig != null) logger = CSVLogger.getInstance(logConfig);
  }

  public void logRequest(UserSessionBean userSessionBean, FacesContext context)
  {
    if (logger != null)
    {
      MenuItemCursor cursor =
        userSessionBean.getMenuModel().getSelectedMenuItem();
      ExternalContext extContext = context.getExternalContext();
      Date date = new Date();
      String userId = userSessionBean.getUserId().trim();
      SimpleDateFormat hdf = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss");
      String dateTime = hdf.format(date);
      long freeMemory = Runtime.getRuntime().freeMemory();
      String language = userSessionBean.getLastPageLanguage();
      if (language == null)
      {
        language = MatrixConfig.getProperty(FORCED_LANGUAGE);
      }
      String ip = "?";
      String userAgent = "?";
      String sessionId = "?";
      String acceptLanguage = "?";
      String requestMethod = "?";
      String parameters = "?";
      String browserType = UserSessionBean.getCurrentInstance().getBrowserType(); 

      Object req = extContext.getRequest();
      if (req instanceof HttpServletRequest)
      {
        HttpServletRequest servletRequest = (HttpServletRequest)req;
        ip = servletRequest.getRemoteAddr();
        userAgent = servletRequest.getHeader("User-Agent");
        acceptLanguage = servletRequest.getHeader("Accept-Language");
        sessionId = servletRequest.getSession().getId();
        requestMethod = servletRequest.getMethod();
        StringBuilder buffer = new StringBuilder();
        Enumeration enu = servletRequest.getParameterNames();
        while (enu.hasMoreElements())
        {
          String parameter = (String)enu.nextElement();
          if (isOutputParameter(parameter))
          {
            String value = servletRequest.getParameter(parameter);
            if (value != null && value.length() > 0)
            {
              if (buffer.length() > 0) buffer.append("&");
              buffer.append(parameter).append("=").append(value);
            }
          }
        }
        parameters = buffer.toString();
        if (parameters.length() == 0) parameters = "-";
      }
      // log to file
      logger.log(dateTime, userId, cursor.getMid(), getPathLabels(cursor),
          ip, String.valueOf(freeMemory), userAgent, acceptLanguage, language,
          sessionId, requestMethod, parameters, browserType);
    }
  }

  private String getPathLabels(MenuItemCursor cursor)
  {
    StringBuilder buffer = new StringBuilder();
    MenuItemCursor cursorPath[] = cursor.getCursorPath();
    if (cursorPath != null)
    {
      for (int i = 0; i < cursorPath.length; i++)
      {
        buffer.append("/[");
        buffer.append(cursorPath[i].getMid());
        buffer.append("]:");
        buffer.append(cursorPath[i].getLabel());
      }
    }
    else
    {
      buffer.append("?");
    }
    return buffer.toString();
  }

  private boolean isOutputParameter(String parameter)
  {
    if ("com.sun.faces.VIEW".equals(parameter) ||
      "javax.faces.ViewState".equals(parameter) ||
      parameter.startsWith("mainform")) return false;
    return true;
  }
}
