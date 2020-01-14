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
package org.santfeliu.matrix.client.ui.scanner;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Used only for compatibility reasons use MultipartServletConnection instead.
 * 
 * @author blanquepa
 */
public class ApplicationServletConnection extends AbstractServletConnection
{
  public ApplicationServletConnection(String servletUrl, Map properties) throws IOException
  {
    URL url = new URL(servletUrl);
    this.httpConn = (HttpURLConnection) url.openConnection(); 
    httpConn.setRequestProperty("Accept-Charset", "UTF-8"); 
    httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8"); 
    Set keys = properties.keySet();
    for (Object key : keys)
    {
      if (((String)key).startsWith("scan_"))
      {
        httpConn.setRequestProperty((String) key, (String) properties.get(key));
      }
    }
    httpConn.setUseCaches(false);
    httpConn.setDoOutput(true);
    httpConn.setRequestMethod("POST");      
  }

  @Override
  public OutputStream getOutputStream()
  {
    try
    {
      return httpConn.getOutputStream();
    }
    catch (IOException ex)
    {
      Logger.getLogger(ApplicationServletConnection.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  @Override
  public void flush()
  {
  }
  
}
