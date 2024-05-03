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
package org.santfeliu.webapp.modules.geo.io;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.ExternalContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author realor
 */
public class MapAccessLogger
{
  static HashMap<String, List<Access>> statisticsMap = new HashMap();
  static final Logger LOGGER = Logger.getLogger("MapAccessLogger");

  public static synchronized List<Access> getStatistics(String mapName)
  {
    String value =
      MatrixConfig.getProperty("org.santfeliu.geo.logAccess");

    if (!"true".equals(value)) return null;

    List<Access> statistics = statisticsMap.get(mapName);
    if (statistics == null)
    {
      try
      {
        statistics = new ArrayList<>();
        Context initContext = new InitialContext();
        Context envContext  = (Context)initContext.lookup("java:/comp/env");
        DataSource ds = (DataSource)envContext.lookup("jdbc/matrix");
        try (Connection conn = ds.getConnection())
        {
          try (PreparedStatement stmt = conn.prepareStatement(
            "select substr(datetime, 1, 6), " +
            "count(*), count(distinct(ip)), count(distinct(userid)) " +
            "from log_geomap " +
            "where datetime > ? and mapname = ? " +
            "group by substr(datetime, 1, 6) order by 1 desc"))
          {
            long yearMillis = 365 * 24 * 3600 * 1000;
            Date date = new Date(System.currentTimeMillis() - yearMillis);
            String startDate = TextUtils.formatDate(date, "yyyyMM");
            System.out.println(">>> Date: " + startDate);
            stmt.setString(1, startDate);
            stmt.setString(2, mapName);
            try (ResultSet rs = stmt.executeQuery())
            {
              while (rs.next())
              {
                String period = rs.getString(1);
                period = period.substring(0, 4) + "/" + period.substring(4, 6);
                int visualizations = rs.getInt(2);
                int addresses = rs.getInt(3);
                int users = rs.getInt(4);
                statistics.add(
                  new Access(period, visualizations, addresses, users));
              }
            }
            statisticsMap.put(mapName, statistics);
          }
        }
      }
      catch (Exception ex)
      {
        LOGGER.log(Level.SEVERE, ex.toString());
      }
    }
    return statistics;
  }

  public static synchronized void clearStatistics()
  {
    statisticsMap.clear();
  }

  public static synchronized void clearStatistics(String mapName)
  {
    statisticsMap.remove(mapName);
  }

  public static void registerAccess(String mapName, ExternalContext context)
  {
    String value =
      MatrixConfig.getProperty("org.santfeliu.geo.logAccess");

    if (!"true".equals(value)) return;

    try
    {
      Context initContext = new InitialContext();
      Context envContext  = (Context)initContext.lookup("java:/comp/env");
      DataSource ds = (DataSource)envContext.lookup("jdbc/matrix");
      try (Connection conn = ds.getConnection())
      {
        String id = UUID.randomUUID().toString();
        String userId = UserSessionBean.getCurrentInstance().getUserId();
        String dateTime = TextUtils.formatDate(new Date(), "yyyyMMddHHmmss");
        String ip = ((HttpServletRequest)context.getRequest()).getRemoteAddr();
        try (PreparedStatement stmt = conn.prepareStatement(
          "insert into log_geomap(id, datetime, userid, ip, mapName) " +
            "values (?,?,?,?,?)"))
        {
          stmt.setString(1, id);
          stmt.setString(2, dateTime);
          stmt.setString(3, userId);
          stmt.setString(4, ip);
          stmt.setString(5, mapName);
          stmt.executeUpdate();
        }
      }
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, ex.toString());
    }
  }

  static public class Access implements Serializable
  {
    String period;
    int visualizations;
    int addresses;
    int users;

    public Access(String period, int visualizations, int addresses, int users)
    {
      this.period = period;
      this.visualizations = visualizations;
      this.addresses = addresses;
      this.users = users;
    }

    public String getPeriod()
    {
      return period;
    }

    public int getVisualizations()
    {
      return visualizations;
    }

    public int getAddresses()
    {
      return addresses;
    }

    public int getUsers()
    {
      return users;
    }
  }
}
