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
package org.santfeliu.ant.sentilo;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 *
 * @author realor
 */
public class SentiloPublishTask extends Task
{
  private String url;
  private String provider;
  private String sensor;
  private String value;
  private String token;

  public String getUrl()
  {
    return url;
  }

  public void setUrl(String url)
  {
    this.url = url;
  }

  public String getProvider()
  {
    return provider;
  }

  public void setProvider(String provider)
  {
    this.provider = provider;
  }

  public String getSensor()
  {
    return sensor;
  }

  public void setSensor(String sensor)
  {
    this.sensor = sensor;
  }

  public String getValue()
  {
    return value;
  }

  public void setValue(String value)
  {
    this.value = value;
  }

  public String getToken()
  {
    return token;
  }

  public void setToken(String token)
  {
    this.token = token;
  }
  
  @Override
  public void execute() throws BuildException
  {
    try
    {
      URL targetUrl = new URL(url + "/data/" + provider + "/" + sensor + "/" + value);
      HttpURLConnection conn = (HttpURLConnection)targetUrl.openConnection();
      conn.setRequestMethod("PUT");
      conn.setRequestProperty("IDENTITY_KEY", token);
      InputStream is = conn.getInputStream();
      try
      {
        while (is.read() != -1)
        {
        }
      }
      finally
      {
        is.close();
      }
    }
    catch (Exception ex)
    {
      throw new BuildException(ex);
    }    
  }
}
