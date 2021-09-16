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
package org.santfeliu.web.filter;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.santfeliu.util.script.WebScriptableBase;
import org.santfeliu.util.template.JSTemplate;
import org.santfeliu.web.HttpUtils;

/**
 *
 * @author realor
 */
public class ShortcutFilter implements Filter
{
  public static final String WILDCARD = "*";
  static final Logger LOGGER = Logger.getLogger("ShortcutFilter");
  public static final String URL_PARAM =
    ShortcutFilter.class.getName() + ".url";
  public static final String UPDATE_INTERVAL_PARAM =
    ShortcutFilter.class.getName() + ".updateInterval";

  private Properties shortcuts = new Properties();
  private int updateInterval = 300000; // 5 minutes
  private long lastUpdate;
  private URL shortcutsUrl;
  private static final HashSet<String> reservedUris = new HashSet<>();

  static
  {
    reservedUris.add("/documents");
    reservedUris.add("/services");
    reservedUris.add("/wsdirectory");
    reservedUris.add("/scanner");
    reservedUris.add("/classif");
    reservedUris.add("/rss");
    reservedUris.add("/feed");
    reservedUris.add("/events");
    reservedUris.add("/signatures");
    reservedUris.add("/qrcode");
    reservedUris.add("/proxy");
    reservedUris.add("/form");
    reservedUris.add("/scripts");
    reservedUris.add("/rest");
    reservedUris.add("/valid");
    reservedUris.add("/pagament");
    reservedUris.add("/clock");
    reservedUris.add("/fckfaces");
    reservedUris.add("/reports");
    reservedUris.add("/imgscale");
    reservedUris.add("/work");
    reservedUris.add("/templates");
    reservedUris.add("/themes");
    reservedUris.add("/common");
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException
  {
    ServletContext servletContext = filterConfig.getServletContext();
    String url = servletContext.getInitParameter(URL_PARAM);
    if (url != null && url.length() > 0)
    {
      try
      {
        shortcutsUrl = new URL(url);
        String num = servletContext.getInitParameter(UPDATE_INTERVAL_PARAM);
        if (num != null)
        {
          updateInterval = Integer.parseInt(num);
        }
      }
      catch (MalformedURLException ex)
      {
        LOGGER.log(Level.SEVERE, "Init failed", ex);
      }
    }
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
    FilterChain chain) throws IOException, ServletException
  {
    if (shortcutsUrl == null)
    {
      chain.doFilter(request, response);
    }
    else
    {
      updateShortcuts();

      HttpServletRequest httpRequest = (HttpServletRequest)request;
      HttpServletResponse httpResponse = (HttpServletResponse)response;

      String location = getLocation(httpRequest);
      if (location == null)
      {
        chain.doFilter(request, response);
      }
      else
      {
        if (location.startsWith("/"))
        {
          location = HttpUtils.getContextURL(httpRequest) + location;
        }
        httpResponse.sendRedirect(location);
      }
    }
  }

  @Override
  public void destroy()
  {
    shortcuts.clear();
  }

  protected void updateShortcuts()
  {
    boolean update = false;

    synchronized (this)
    {
      long now = System.currentTimeMillis();
      if (now - lastUpdate > updateInterval)
      {
        update = true;
        lastUpdate = now;
      }
    }

    if (update)
    {
      try
      {
        URLConnection conn = shortcutsUrl.openConnection();
        InputStream is = conn.getInputStream();
        try
        {
          Properties newShortcuts = new Properties();
          newShortcuts.load(is);
          shortcuts = newShortcuts;
          LOGGER.log(Level.INFO, "Update completed: {0} shortcuts",
            new Object[]{ shortcuts.size() });
        }
        finally
        {
          is.close();
        }
      }
      catch (IOException ex)
      {
        LOGGER.log(Level.SEVERE, "Update failed", ex);
      }
    }
  }

  private String getLocation(HttpServletRequest request)
  {
    String host = request.getHeader("Host");
    String uri = request.getServletPath();

    if (uri.endsWith(".faces") || reservedUris.contains(uri))
      return null;

    if (uri.endsWith("/")) uri = uri.substring(0, uri.length() - 1);

    String location = shortcuts.getProperty(host + uri);
    if (location == null)
    {
      location = shortcuts.getProperty(uri);
    }

    int index = uri.lastIndexOf("/");
    while (location == null && index > 0)
    {
      String wildcardUri = uri.substring(0, index + 1) + WILDCARD;
      location = shortcuts.getProperty(host + wildcardUri);
      if (location == null)
      {
        location = shortcuts.getProperty(wildcardUri);
      }
      index = uri.lastIndexOf("/", index - 1);
    }
    if (location != null)
    {
      location = mergeLocation(request, location);
      LOGGER.log(Level.INFO, "Redirect [{0}] to [{1}]",
        new String[]{host + uri, location});
    }
    return location;
  }

  private String mergeLocation(HttpServletRequest request, String location)
  {
    if (location.contains("${"))
    {
      String uri = request.getServletPath();
      HashMap variables = new HashMap();
      variables.put("request", request);
      variables.put("servletPath", uri);
      variables.put("queryString", request.getQueryString());
      variables.put("path", uri.substring(1).split("/"));
      JSTemplate template = new JSTemplate(location, WebScriptableBase.class);
      return template.merge(variables);
    }
    return location;
  }
}
