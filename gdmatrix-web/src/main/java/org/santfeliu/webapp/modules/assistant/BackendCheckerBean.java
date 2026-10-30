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
package org.santfeliu.webapp.modules.assistant;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

/**
 *
 * @author realor
 */
@Named
@ApplicationScoped
public class BackendCheckerBean
{
  private static final long UPDATE_INTERVAL = 10; // seconds
  private static final long CONNECT_TIMEOUT = 3; // seconds

  private final Map<String, Backend> backendMap = 
    Collections.synchronizedMap(new HashMap<>());
    
  public boolean isActive(String url)
  {
    return getBackend(url).isActive();
  }
  
  public void clear()
  {
    backendMap.clear();
  }
 
  Backend getBackend(String url)
  {
    Backend backend = backendMap.get(url);
    if (backend == null) 
    {
      backend = new Backend(url);
      backend.readStatus();
      backendMap.put(url, backend);
    }
    return backend;    
  }
  
  class Backend
  {
    String url;
    long lastCheck;
    int status;

    Backend(String url)
    {
      this.url = url;
    }
    
    int readStatus()
    {
      lastCheck = System.currentTimeMillis();

      try
      {
        HttpClient client = HttpClient.newBuilder()
          .connectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT))
          .followRedirects(HttpClient.Redirect.NORMAL)
          .build();

        HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .method("HEAD", HttpRequest.BodyPublishers.noBody())
          .build();

        HttpResponse<Void> response = 
          client.send(request, HttpResponse.BodyHandlers.discarding());

        status = response.statusCode();
      }
      catch (Exception e)
      {
        status = -1;
      }
      System.out.println("Backend status " + url + ": " + status);
      return status;
    }

    boolean isActive()
    {
      long updateTime = 1000L * UPDATE_INTERVAL;
      if (status != 200)
      {
        updateTime *= 10;
      }
      
      if (System.currentTimeMillis() - lastCheck > updateTime)
      {
        readStatus();
      }
      return status == 200;
    }
  }
}
