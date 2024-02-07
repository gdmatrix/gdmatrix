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
package org.santfeliu.geo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.postgis.jdbc.PGgeometry;

/**
 *
 * @author realor
 */
public class DataStore
{
  public static List<Feature> readFeatures(Map<String, Object> params,
    String query) throws Exception
  {
    String driver = (String)params.get("driver");
    String url = (String)params.get("url");
    String user = (String)params.get("user");
    String password = (String)params.get("password");

    List<Feature> features = new ArrayList<>();

    Class.forName(driver);
    try (Connection conn = DriverManager.getConnection(url, user, password))
    {
      try (Statement stmt = conn.createStatement())
      {
        try (ResultSet rs = stmt.executeQuery(query))
        {
          ResultSetMetaData metaData = rs.getMetaData();
          while (rs.next())
          {
            Feature feature = new Feature();
            int count = metaData.getColumnCount();
            for (int i = 0; i < count; i++)
            {
              String columnName = metaData.getColumnName(i + 1);
              Object value = rs.getObject(i + 1);
              if (value instanceof PGgeometry) // for postgis only
              {
                String wkt = value.toString();
                int index = wkt.indexOf(";");
                if (index > 0) wkt = wkt.substring(index + 1);
                feature.setGeometryColumnName(columnName);
                feature.setGeometry(wkt);
              }
              else
              {
                feature.put(columnName, value);
              }
            }
            features.add(feature);
          }
        }
      }
    }
    return features;
  }

  public static void writeFeatures(Map<String, Object> params, String layerName,
    List<Feature> features, int srid, boolean truncate) throws Exception
  {
    if (features.isEmpty()) return;

    Feature firstFeature = features.get(0);
    String geometryColumnName = firstFeature.getGeometryColumnName();
    Set<String> keySet = firstFeature.keySet();
    StringBuilder buffer = new StringBuilder();
    buffer.append("INSERT INTO ").append('"').append(layerName).append("\" (");
    StringBuilder paramsBuffer = new StringBuilder("(");
    List<String> columnNames = new ArrayList<>(keySet);
    for (int i = 0; i < columnNames.size(); i++)
    {
      String columnName = columnNames.get(i);
      if (i > 0)
      {
        buffer.append(", ");
        paramsBuffer.append(", ");
      }
      buffer.append('"');
      buffer.append(columnName);
      buffer.append('"');
      if (columnName.equals(geometryColumnName))
      {
        paramsBuffer.append("st_geomfromtext(?, ").append(srid).append(")");
      }
      else
      {
        paramsBuffer.append("?");
      }
    }
    paramsBuffer.append(")");
    buffer.append(") values ").append(paramsBuffer);
    String insert = buffer.toString();

    String driver = (String)params.get("driver");
    String url = (String)params.get("url");
    String user = (String)params.get("user");
    String password = (String)params.get("password");

    Class.forName(driver);
    try (Connection conn = DriverManager.getConnection(url, user, password))
    {
      if (truncate)
      {
        try (Statement stmt = conn.createStatement())
        {
          stmt.executeUpdate("TRUNCATE TABLE \"" + layerName + "\"");
        }
      }

      try (PreparedStatement prepStmt = conn.prepareStatement(insert))
      {
        for (Feature feature : features)
        {
          for (int i = 0; i < columnNames.size(); i++)
          {
            String columnName = columnNames.get(i);
            Object value = feature.get(columnName);
            prepStmt.setObject(i + 1, value);
          }
          prepStmt.executeUpdate();
        }
      }
    }
  }

  public static void main(String[] args)
  {
    try
    {
      Map conn = new java.util.HashMap();
      conn.put("driver", "org.postgresql.Driver");
      conn.put("url", "jdbc:postgresql://hathor:5432/ajfeliu");
      conn.put("user", "gis");
      conn.put("password", "obione");

      List<Feature> features = new ArrayList<>();
      Feature feature = new Feature("GEOMETRY");
      feature.put("NOM", "Ricard");
      feature.put("COGNOM", "Ricard");
      feature.put("AGE", 50);
      feature.put("COLOR", "white");
      feature.put("GEOMETRY", "POINT(0, 0)");
      features.add(feature);

      DataStore.writeFeatures(conn, "TEST", features, 25831, true);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
