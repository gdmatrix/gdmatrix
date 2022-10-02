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
package org.santfeliu.security.util;

import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Collection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import org.matrix.security.SecurityConstants;
import org.santfeliu.security.SecurityProvider;
import org.santfeliu.util.MatrixConfig;


/**
 *
 * @author realor
 */
public class SecurityUtils
{
  private static SecurityProvider defaultSecurityProvider;
  private static URLCredentialsCipher urlCredentialsCipher;
  public static final String REPRESENTANT_PATTERN = "\\(R: (\\w+)\\)";

  static
  {
    try
    {
      String providerClassName = MatrixConfig.getProperty(
        "org.santfeliu.security.provider.className");
      Class cls = Class.forName(providerClassName);
      defaultSecurityProvider = (SecurityProvider)cls.getConstructor().newInstance();
      String secret = MatrixConfig.getProperty(
        "org.santfeliu.security.urlCredentialsCipher.secret");
      urlCredentialsCipher = new URLCredentialsCipher(secret);
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  public static SecurityProvider getSecurityProvider()
  {
    return defaultSecurityProvider;
  }

  public static URLCredentialsCipher getURLCredentialsCipher()
  {
    return urlCredentialsCipher;
  }

  public static Credentials getCredentials(HttpServletRequest request)
  {
    return getCredentials(request, true);
  }

  public static Credentials getCredentials(HttpServletRequest request,
    boolean neverNull)
  {
    Credentials credentials = null;
    // look for userid/password parameters
    String userId = request.getParameter(SecurityConstants.USERID_PARAMETER);
    if (userId == null)
    {
      userId = request.getParameter("userId"); // backward compatibility
    }
    String password = request.getParameter(SecurityConstants.PASSWORD_PARAMETER);
    if (userId != null)
    {
      credentials = new Credentials(userId, password);
    }
    else
    {
      // look for credentials string parameter
      if (request.getParameter(
        URLCredentialsCipher.CREDENTIALS_PARAMETER) != null)
      {
        String queryString = request.getQueryString();
        StringBuffer urlBuffer = request.getRequestURL();
        urlBuffer.append("?");
        urlBuffer.append(queryString);
        String url = urlBuffer.toString();
        credentials = urlCredentialsCipher.getCredentials(url);
      }
      if (credentials == null)
      {
        // look for Authorization header
        String autho = request.getHeader("Authorization");
        if (autho != null)
        {
          BasicAuthorization ba = new BasicAuthorization();
          ba.fromString(autho);
          userId = ba.getUserId();
          password = ba.getPassword();
          credentials = new Credentials(userId, password);
        }
      }
    }
    if (credentials == null && neverNull)
    {
      credentials = new Credentials();
    }
    return credentials;
  }

  public static Credentials getCredentials(WebServiceContext wsContext)
  {
    Credentials credentials = null;
    MessageContext context = wsContext.getMessageContext();
    ServletRequest request =
      (ServletRequest)context.get(MessageContext.SERVLET_REQUEST);
    if (request instanceof HttpServletRequest)
    {
      credentials = getCredentials((HttpServletRequest)request, true);
    }
    else
    {
      credentials = new Credentials();
    }
    return credentials;
  }

  public static String calculateDigestBase64(String text)
  {
    try
    {
      MessageDigest md5 = MessageDigest.getInstance("MD5");
      md5.update(text.getBytes());
      return Base64.getMimeEncoder().encodeToString(md5.digest());
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  public static String marshallUsername(String username, int length)
  {
    StringBuilder buffer = new StringBuilder(username.toLowerCase());
    while (buffer.length() < length)
    {
      buffer.append(' ');
    }
    return buffer.toString();
  }

  public static Map getCertificateAttributes(X509Certificate certificate)
    throws Exception
  {
    Map attributes = new HashMap();
    String subjectDN = certificate.getSubjectDN().getName();
    parseAttributes(subjectDN, attributes);
    // parse subject alternative names
    Collection<List<?>> names = certificate.getSubjectAlternativeNames();
    if (names != null)
    {
      for (List list : names)
      {
        Integer key = (Integer)list.get(0);
        String value = String.valueOf(list.get(1));

        attributes.put("SAN-" + key, value);
      }
    }
    return attributes;
  }

  public static String getRepresentantCIF(String commonName)
  {
    String CIF = null;
    if (commonName != null)
    {
      Pattern pattern = Pattern.compile(REPRESENTANT_PATTERN);
      Matcher matcher = pattern.matcher(commonName);
      if (matcher.find())
      {
        CIF = matcher.group(1);
      }
    }
    return CIF;
  }

  private static void parseAttributes(String field, Map attributes)
  {
    String[] pairs = field.split(",");
    for (int i = 0; i < pairs.length; i++)
    {
      int index = pairs[i].indexOf('=');
      if (index != -1)
      {
        String pair = pairs[i];
        String key = pair.substring(0, index).trim();
        String value = pair.substring(index + 1).trim();
        attributes.put(key, value);
      }
    }
  }
}
