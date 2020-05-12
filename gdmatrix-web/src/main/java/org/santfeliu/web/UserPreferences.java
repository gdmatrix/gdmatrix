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
package org.santfeliu.web;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.matrix.sql.QueryParameter;
import org.matrix.sql.QueryParameters;
import org.matrix.sql.QueryRow;
import org.matrix.sql.QueryTable;
import org.matrix.sql.SQLManagerPort;
import org.matrix.sql.SQLManagerService;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.RandomUtils;

/**
 *
 * @author unknown
 */
public class UserPreferences implements Serializable
{
  public static String DEFAULT_LANGUAGE_PROPERTY = "defaultLanguage";
  public static String DEFAULT_THEME_PROPERTY = "defaultTheme";
  public static String RECENT_PAGES_SIZE_PROPERTY = "recentPagesSize";

  private static String DB_ALIAS = MatrixConfig.getClassProperty(
    UserPreferences.class, "dbAlias");
  private static String DB_USERNAME = MatrixConfig.getClassProperty(
    UserPreferences.class, "dbUsername");
  private static String DB_PASSWORD = MatrixConfig.getClassProperty(
    UserPreferences.class, "dbPassword");
  private static float PURGE_PROBABILITY = 0.05f;

  private Boolean purgePreferences;
  private String userId;
  private Map<String, List<String>> preferencesMap;

  public UserPreferences(String userId)
  {
    this.purgePreferences = null;
    this.userId = userId;
    this.preferencesMap = null;    
  }

  public String getUserId()
  {
    return userId;
  }

  public String getPreference(String name) throws Exception
  {
    return getPreferences(name).get(0);
  }

  public List<String> getPreferences(String name) throws Exception
  {
    if (getPreferencesMap().containsKey(name))
    {
      return getPreferencesMap().get(name);
    }
    else
    {
      throw new Exception("INVALID_PREFERENCE");
    }
  }

  public void storePreference(String name, String value)
  {
    SQLManagerPort port = getSQLPort();
    String sql = "select max(idx) from web_preferences where " +
      "trim(usrcod) = trim({usrcod}) and name = {name}";
    QueryParameters parameters = new QueryParameters();
    QueryParameter param = new QueryParameter();
    param.setName("usrcod");
    param.setValue(userId);
    parameters.getParameters().add(param);
    param = new QueryParameter();
    param.setName("name");
    param.setValue(name);
    parameters.getParameters().add(param);
    QueryTable table = port.executeAliasQuery(sql, parameters, DB_ALIAS,
      DB_USERNAME, DB_PASSWORD);
    List<QueryRow> rows = table.getQueryRow();
    String maxIndex = "1";
    if (rows != null && rows.size() > 0)
    {
      Object maxValue = rows.get(0).getValues().get(0);
      if (maxValue != null)
      {
        int iMaxIndex = ((BigDecimal)maxValue).intValue();
        maxIndex = String.valueOf(iMaxIndex + 1);
      }
    }
    sql = "insert into web_preferences(usrcod,name,idx,value) "
      + "values({usrcod},{name},{idx},{value})";
    parameters = new QueryParameters();
    param = new QueryParameter();
    param.setName("usrcod");
    param.setValue(userId);
    parameters.getParameters().add(param);
    param = new QueryParameter();
    param.setName("name");
    param.setValue(name);
    parameters.getParameters().add(param);
    param = new QueryParameter();
    param.setName("idx");
    param.setValue(maxIndex);
    parameters.getParameters().add(param);
    param = new QueryParameter();
    param.setName("value");
    param.setValue(value);
    parameters.getParameters().add(param);
    port.executeAliasUpdate(sql, parameters, DB_ALIAS, DB_USERNAME, DB_PASSWORD);
    preferencesMap = null;
  }

  public void removePreference(String name)
  {    
    QueryParameters parameters = new QueryParameters();
    QueryParameter param = new QueryParameter();
    String sql = "delete from web_preferences where "
      + "trim(usrcod) = trim({usrcod}) and name={name}";
    parameters = new QueryParameters();
    param = new QueryParameter();
    param.setName("usrcod");
    param.setValue(userId);
    parameters.getParameters().add(param);
    param = new QueryParameter();
    param.setName("name");
    param.setValue(name);
    parameters.getParameters().add(param);
    getSQLPort().executeAliasUpdate(sql, parameters, DB_ALIAS, DB_USERNAME,
      DB_PASSWORD);
    preferencesMap = null;
  }  

  public void removePreference(String name, String value)
  {
    QueryParameters parameters = new QueryParameters();
    QueryParameter param = new QueryParameter();
    String sql = "delete from web_preferences where "
      + "trim(usrcod) = trim({usrcod}) and name={name} and value={value}";
    parameters = new QueryParameters();
    param = new QueryParameter();
    param.setName("usrcod");
    param.setValue(userId);
    parameters.getParameters().add(param);
    param = new QueryParameter();
    param.setName("name");
    param.setValue(name);
    parameters.getParameters().add(param);
    param = new QueryParameter();
    param.setName("value");
    param.setValue(value);
    parameters.getParameters().add(param);
    getSQLPort().executeAliasUpdate(sql, parameters, DB_ALIAS, DB_USERNAME,
      DB_PASSWORD);
    preferencesMap = null;
  }

  public boolean existsPreference(String name, String value)
  {
    if (!getPreferencesMap().containsKey(name)) return false;
    else return getPreferencesMap().get(name).contains(value);
  }

  public String getDefaultLanguage() throws Exception
  {
    return getPreference(DEFAULT_LANGUAGE_PROPERTY);
  }

  public String getDefaultTheme() throws Exception
  {
    return getPreference(DEFAULT_THEME_PROPERTY);
  }

  public String getRecentPagesSize() throws Exception
  {
    return getPreference(RECENT_PAGES_SIZE_PROPERTY);
  }

  public boolean mustPurgePreferences()
  {
    if (purgePreferences == null)
    {
      purgePreferences = RandomUtils.test(PURGE_PROBABILITY);
    }
    return purgePreferences;
  }

  private Map<String, List<String>> getPreferencesMap()
  {
    if (preferencesMap == null)
    {
      preferencesMap = new HashMap<String, List<String>>();
      String sql =
        "select name,value from web_preferences where "
        + "trim(usrcod) = trim({usrcod}) order by name,idx";
      QueryParameters parameters = new QueryParameters();
      QueryParameter param = new QueryParameter();
      param.setName("usrcod");
      param.setValue(userId);
      parameters.getParameters().add(param);
      QueryTable table = getSQLPort().executeAliasQuery(sql, parameters,
        DB_ALIAS, DB_USERNAME, DB_PASSWORD);
      List<QueryRow> rows = table.getQueryRow();
      for (QueryRow row : rows)
      {
        String name = (String)row.getValues().get(0);
        String value = (String)row.getValues().get(1);
        if (!preferencesMap.containsKey(name))
        {
          preferencesMap.put(name, new ArrayList<String>());
        }
        preferencesMap.get(name).add(value);
      }
    }
    return preferencesMap;
  }

  private SQLManagerPort getSQLPort()
  {
    WSDirectory dir = WSDirectory.getInstance();
    WSEndpoint endpoint = dir.getEndpoint(SQLManagerService.class);
    return endpoint.getPort(SQLManagerPort.class);
  }

}
