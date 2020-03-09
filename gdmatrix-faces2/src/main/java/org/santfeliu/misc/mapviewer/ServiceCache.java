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
package org.santfeliu.misc.mapviewer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.HashMap;
import org.santfeliu.misc.mapviewer.io.ServiceCapabilitiesReader;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.security.util.SecurityUtils;
import org.santfeliu.security.util.URLCredentialsCipher;
import org.santfeliu.util.MatrixConfig;

/**
 *
 * @author realor
 */
public class ServiceCache
{
  private static final HashMap<String, ServiceCapabilities> cache =
    new HashMap<String, ServiceCapabilities>();

  private static HashMap<String, String> serviceUrlSubstitution;


  /**
   * Changes the service url according to the rules in the substitution file
   * located in ${configDir}/svc-url-subs.txt
   *
   * The rules in this file follow this syntax:
   * {url} = {substitution_url}
   *
   * @param url, the serviceUrl
   * @return  the actual serviceUrl to use
   */
  public static String getServiceUrl(String url)
  {
    if (serviceUrlSubstitution == null)
    {
      serviceUrlSubstitution = new HashMap<>();
      File configDir = MatrixConfig.getDirectory();
      File substitutionFile = new File(configDir, "svc-url-subs.txt");
      if (substitutionFile.exists())
      {
        try
        {
          BufferedReader reader =
            new BufferedReader(new FileReader(substitutionFile));
          try
          {
            String line = reader.readLine();
            while (line != null)
            {
              String parts[] = line.split("=");
              if (parts.length == 2)
              {
                serviceUrlSubstitution.put(parts[0].trim(), parts[1].trim());
              }
              line = reader.readLine();
            }
          }
          finally
          {
            reader.close();
          }
        }
        catch (IOException ex)
        {
          // ignore
        }
      }
    }
    String subsUrl = serviceUrlSubstitution.get(url);
    return subsUrl == null ? url : subsUrl;
  }

  public static ServiceCapabilities getServiceCapabilities(String serviceUrl,
    boolean refresh, Credentials credentials) throws Exception
  {
    serviceUrl = getServiceUrl(serviceUrl);
    ServiceCapabilities capabilities = cache.get(serviceUrl);
    if (capabilities == null || refresh)
    {
      // connect by proxy
      String port = MatrixConfig.getProperty("org.santfeliu.web.defaultPort");
      String contextPath = MatrixConfig.getProperty("contextPath");
      String requestUrl = "http://localhost:" + port + contextPath  +
      "/proxy?url=" + serviceUrl + "&SERVICE=WMS&REQUEST=GetCapabilities";
      if (credentials != null)
      {
        URLCredentialsCipher urlCredentialsCipher =
        SecurityUtils.getURLCredentialsCipher();
        requestUrl = urlCredentialsCipher.putCredentials(requestUrl, credentials);
      }
      URL url = new URL(requestUrl);
      System.out.println("URL: " + url);
      URLConnection conn = url.openConnection();
      conn.setConnectTimeout(10000);
      conn.setReadTimeout(10000);
      InputStream is = conn.getInputStream();
      ServiceCapabilitiesReader reader = new ServiceCapabilitiesReader();
      capabilities = reader.read(is);
      cache.put(serviceUrl, capabilities);
    }
    return capabilities;
  }

  public static Collection<String> getServiceURLs()
  {
    return cache.keySet();
  }

  public static void clear()
  {
    cache.clear();
  }
}
