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
package org.santfeliu.misc.mapviewer.util;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import org.santfeliu.misc.mapviewer.ServiceCapabilities;
import org.santfeliu.misc.mapviewer.ServiceCapabilities.Layer;
import org.santfeliu.misc.mapviewer.io.ServiceCapabilitiesReader;
import org.santfeliu.security.util.BasicAuthorization;
import org.santfeliu.security.util.Credentials;

/**
 *
 * @author realor
 */
public class GeoServerSecurityGenerator
{
  private String readRoles = "*";
  private String writeRoles = "ADMIN";
  private String adminRoles = null;

  public String getAdminRoles()
  {
    return adminRoles;
  }

  public void setAdminRoles(String adminRoles)
  {
    this.adminRoles = adminRoles;
  }

  public String getReadRoles()
  {
    return readRoles;
  }

  public void setReadRoles(String readRoles)
  {
    this.readRoles = readRoles;
  }

  public String getWriteRoles()
  {
    return writeRoles;
  }

  public void setWriteRoles(String writeRoles)
  {
    this.writeRoles = writeRoles;
  }

  public void generate(String wmsUrl, File output, Credentials credentials)
    throws Exception
  {
    URL url = new URL(wmsUrl + "?service=wms&version=1.1.1&request=GetCapabilities");
    URLConnection conn = url.openConnection();
    BasicAuthorization ba = new BasicAuthorization();
    ba.setUserId(credentials.getUserId());
    ba.setPassword(credentials.getPassword());
    conn.setRequestProperty("Authorization", ba.toString());
    PrintWriter writer = new PrintWriter(output);
    try
    {
      // write header
      writer.println("# rule structure is namespace.layer.operation=role1,role2,...");
      writer.println("# namespace: a namespace or * to catch them all (in that case, layer should be *)");
      writer.println("# layer: a layer/featureType/coverage name or * to catch them all");
      writer.println("# operation: r or w (read or write)");
      writer.println("# role list: * to imply all roles, or a list of explicit roles");
      writer.println("# The operation will be allowed if the current user has at least one of the");
      writer.println("# roles in the rule");

      InputStream is = conn.getInputStream();
      try
      {
        ServiceCapabilitiesReader reader = new ServiceCapabilitiesReader();
        ServiceCapabilities capabilities = reader.read(is);
        List<Layer> layers = capabilities.getLayers();
        for (Layer layer : layers)
        {
          String layerName = layer.getName();
          String workspace = "*";
          int index = layerName.indexOf(":");
          if (index != -1)
          {
            workspace = layerName.substring(0, index);
            layerName = layerName.substring(index + 1);
          }
          if (readRoles != null)
          {
            writer.println(workspace + "." + layerName + ".r=" + readRoles);
          }
          if (writeRoles != null)
          {
            writer.println(workspace + "." + layerName + ".w=" + writeRoles);
          }
          if (adminRoles != null)
          {
            writer.println(workspace + "." + layerName + ".a=" + adminRoles);
          }
        }
      }
      finally
      {
        is.close();
      }
    }
    finally
    {
      writer.close();
    }
  }

  public static void main(String args[])
  {
    try
    {
      GeoServerSecurityGenerator generator = new GeoServerSecurityGenerator();
      generator.generate("http://xxx.yyy.zzz.www:pppp/geoserver/wms",
        new File("c:/layers.properties"), new Credentials("admin", "****"));
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
