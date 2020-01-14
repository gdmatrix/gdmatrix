package org.santfeliu.misc.mapviewer;

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
  public static HashMap<String, ServiceCapabilities> cache =
    new HashMap<String, ServiceCapabilities>();

  public static ServiceCapabilities getServiceCapabilities(String serviceUrl,
    boolean refresh, Credentials credentials) throws Exception
  {
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
