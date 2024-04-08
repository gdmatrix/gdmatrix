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
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author realor
 */
public class RemoteIpFilter implements Filter
{
  public static final String X_FORWARDED_FOR_HEADER  = "X-Forwarded-For";
  public static final String X_FORWARDED_PROTO_HEADER  = "X-Forwarded-Proto";
  public static final String X_FORWARDED_PORT_HEADER  = "X-Forwarded-Port";

  @Override
  public void init(FilterConfig fc) throws ServletException
  {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
    FilterChain chain) throws IOException, ServletException
  {
    HttpServletRequest httpRequest = (HttpServletRequest)request;
    HttpServletResponse httpResponse = (HttpServletResponse)response;

    if (httpRequest.getHeader(X_FORWARDED_FOR_HEADER) == null)
    {
      chain.doFilter(httpRequest, httpResponse);
    }
    else
    {
      ForwardedHttpServletRequest forwardedRequest =
        new ForwardedHttpServletRequest(httpRequest);
      chain.doFilter(forwardedRequest, httpResponse);
    }
  }

  @Override
  public void destroy()
  {
  }

  public class ForwardedHttpServletRequest extends HttpServletRequestWrapper
  {
    private final String scheme;
    private final int port;
    private final boolean secure;
    private final String remoteAddr;

    public ForwardedHttpServletRequest(HttpServletRequest httpRequest)
    {
      super(httpRequest);

      String xForwardedFor = httpRequest.getHeader(X_FORWARDED_FOR_HEADER);
      if (xForwardedFor == null)
      {
        remoteAddr = httpRequest.getRemoteAddr();
      }
      else
      {
        remoteAddr = xForwardedFor;
      }

      String xForwardedProto = httpRequest.getHeader(X_FORWARDED_PROTO_HEADER);
      if (xForwardedProto == null)
      {
        scheme = httpRequest.getScheme();
      }
      else
      {
        scheme = xForwardedProto;
      }
      secure = "https".equals(scheme);

      String xForwardedPort = httpRequest.getHeader(X_FORWARDED_PORT_HEADER);
      if (xForwardedPort == null)
      {
        port = httpRequest.getServerPort();
      }
      else
      {
        int portNum;
        try
        {
          portNum = Integer.parseInt(xForwardedPort);
        }
        catch (NumberFormatException ex)
        {
          portNum = httpRequest.getServerPort();
        }
        port = portNum;
      }
    }

    @Override
    public int getServerPort()
    {
      return port;
    }

    @Override
    public String getScheme()
    {
      return scheme;
    }

    @Override
    public boolean isSecure()
    {
      return secure;
    }

    @Override
    public String getRemoteAddr()
    {
      return remoteAddr;
    }

    @Override
    public StringBuffer getRequestURL()
    {
      String scheme = getScheme();             // http
      String serverName = getServerName();     // hostname.com
      int serverPort = getServerPort();        // 80
      String contextPath = getContextPath();   // /mywebapp
      String servletPath = getServletPath();   // /servlet/MyServlet
      String pathInfo = getPathInfo();         // /a/b;c=123

      StringBuffer url =  new StringBuffer();
      url.append(scheme).append("://").append(serverName);

      if (serverPort != -1 && serverPort != 80 && serverPort != 443)
      {
        url.append(":").append(serverPort);
      }
      url.append(contextPath).append(servletPath);

      if (pathInfo != null)
      {
        url.append(pathInfo);
      }
      return url;
    }
  }
}
