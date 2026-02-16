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
package org.santfeliu.webapp.util;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import org.santfeliu.matrix.MatrixInfo;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author realor
 */
public class ServiceLogger
{
  static final Logger LOGGER = Logger.getLogger("ServiceLogger");

  public static void start()
  {    
    register("start");
  }
  
  public static void stop()
  {    
    register("stop");
  }
  
  public static void register(String action)
  {
    String value =
      MatrixConfig.getProperty("org.santfeliu.ServiceLogger.enabled");

    if (!"true".equals(value)) return;

    try
    {
      Context initContext = new InitialContext();
      Context envContext  = (Context)initContext.lookup("java:/comp/env");
      DataSource ds = (DataSource)envContext.lookup("jdbc/matrix");
      try (Connection conn = ds.getConnection())
      {
        String dateTime = TextUtils.formatDate(new Date(), "yyyyMMddHHmmss");
        try (PreparedStatement stmt = conn.prepareStatement(
          "insert into gdmatrix_service(datetime, host, action, version, revision) " +
            "values (?,?,?,?,?)"))
        {
          stmt.setString(1, dateTime);
          stmt.setString(2, getHostname());
          stmt.setString(3, action);
          stmt.setString(4, MatrixInfo.getFullVersion());
          stmt.setString(5, MatrixInfo.getRevision());
          stmt.executeUpdate();
          conn.commit();
        }
      }
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, ex.toString());
    }
  }
  
  static String getHostname()
  {
    String host = System.getProperty("host");
    if (host == null)
    {
      try
      {
        InetAddress ip = InetAddress.getLocalHost();
        host = ip.getHostName();
      }
      catch (Exception ex)
      {        
        host = "localhost";
      }
    }
    return host;
  }
}
