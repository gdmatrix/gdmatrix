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
package org.santfeliu.webapp.modules.geo;

import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.HashMap;
import javax.enterprise.context.ApplicationScoped;
import org.santfeliu.webapp.modules.geo.io.ServiceCapabilitiesReader;
import org.santfeliu.webapp.modules.geo.ogc.ServiceCapabilities;

/**
 *
 * @author realor
 */
@ApplicationScoped
public class GeoServiceBean implements Serializable
{
  private final HashMap<String, ServiceCapabilities> cache = new HashMap<>();

  public ServiceCapabilities getServiceCapabilities(String serviceUrl,
    boolean refresh) throws Exception
  {
    ServiceCapabilities capabilities = cache.get(serviceUrl);
    if (capabilities == null || refresh)
    {
      String requestUrl = serviceUrl + "?SERVICE=WMS&REQUEST=GetCapabilities";

      URL url = new URL(requestUrl);
      URLConnection conn = url.openConnection();
      conn.setConnectTimeout(10000);
      conn.setReadTimeout(10000);
      try (InputStream is = conn.getInputStream())
      {
        ServiceCapabilitiesReader reader = new ServiceCapabilitiesReader();
        capabilities = reader.read(is);
      }
      cache.put(serviceUrl, capabilities);
    }
    System.out.println(capabilities);
    return capabilities;
  }

  public Collection<String> getServiceURLs()
  {
    return cache.keySet();
  }

  public void clear()
  {
    cache.clear();
  }
}
