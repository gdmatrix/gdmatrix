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


import java.security.cert.X509Certificate;
import java.util.Base64;
import javax.servlet.http.HttpServletRequest;
import org.matrix.security.SecurityConstants;
import org.santfeliu.util.MatrixConfig;

/**
 *
 * @author realor
 */
public class HttpUtils
{
  public static final String X_FORWARDED_CERTIFICATE_HEADER  = "X-Forwarded-Certificate";
  public static final String X_USER_HEADER  = "X-User";
  
  public static String getScheme(HttpServletRequest request)
  {
    return request.getScheme();
  }
  
  public static boolean isSecure(HttpServletRequest request)
  {
    return request.isSecure();
  }
  
  public static String getServerName(HttpServletRequest request)
  {
    return request.getServerName();
  }

  public static int getServerPort(HttpServletRequest request)
  {
    return request.getServerPort();
  }

  public static String getRemoteAddress(HttpServletRequest request)
  {
    return request.getRemoteAddr();
  }
  
  public static byte[] getUserCertificate(HttpServletRequest request)
    throws Exception
  {
    byte[] cert = null;
    
    X509Certificate[] certs = (X509Certificate[])request.getAttribute(
      "javax.servlet.request.X509Certificate");

    if (certs != null)
    {
      cert = certs[0].getEncoded();
    }
    else // look for X-User-Cert in http header
    {
      String cert64 = request.getHeader(X_FORWARDED_CERTIFICATE_HEADER);
      if (cert64 != null)
      {
        cert = Base64.getMimeDecoder().decode(cert64);
      }
      else throw new Exception("INVALID_CERTIFICATE");     
    }
    return cert;
  }
  
  public static String getContextURL(HttpServletRequest request)
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append(HttpUtils.getScheme(request));
    buffer.append("://");
    buffer.append(HttpUtils.getServerName(request));
    buffer.append(":");
    buffer.append(HttpUtils.getServerPort(request));
    buffer.append(request.getContextPath());
    return buffer.toString();
  }
  
  public static String getURL(HttpServletRequest request, 
    String scheme, String port, String targetURI, String queryString,
    boolean removeSensitiveParameters)
  {
    String host = HttpUtils.getServerName(request);
    String contextPath = request.getContextPath();
    String url = scheme + "://" + host + ":" + port + contextPath + targetURI;

    if (queryString != null)
    {
      // remove password & sessionid parameter
      if (removeSensitiveParameters)
      {
        queryString = removeQueryParameter(queryString,
          SecurityConstants.PASSWORD_PARAMETER);
      }

      if (queryString.endsWith("&"))
        queryString = queryString.substring(0, queryString.length() - 1);

      if (queryString.length() > 0) url += "?" + queryString;
    }
    return url;
  }

  public static String getDefaultURL(HttpServletRequest request,
     String targetURI, String queryString)
  {
    String port =
      MatrixConfig.getProperty("org.santfeliu.web.defaultPort");
    return getURL(request, "http", port, targetURI, queryString, true);
  }

  public static String getServerSecureURL(HttpServletRequest request,
     String targetURI, String queryString)
  {
    String port = 
      MatrixConfig.getProperty("org.santfeliu.web.serverSecurePort");
    return getURL(request, "https", port, targetURI, queryString, true);
  }

  public static String getClientSecureURL(HttpServletRequest request,
     String targetURI, String queryString)
  {
    String port = 
      MatrixConfig.getProperty("org.santfeliu.web.clientSecurePort");
    return getURL(request, "https", port, targetURI, queryString, true);
  }

  private static String removeQueryParameter(String queryString, String name)
  {
    int index = queryString.indexOf(name + "=");
    if (index != -1)
    {
      int index2 = queryString.indexOf("&", index);
      if (index2 == -1)
      {
        queryString = queryString.substring(0, index);
      }
      else
      {
        queryString = queryString.substring(0, index) +
          queryString.substring(index2 + 1);
      }
    }
    return queryString;
  }
}
