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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.matrix.dic.Property;
import org.matrix.security.SecurityManagerPort;
import org.santfeliu.security.web.SecurityConfigBean;
import org.santfeliu.util.RandomUtils;

/**
 *
 * @author lopezrj
 * @author realor
 */
public class UserPreferences implements Serializable
{
  public static String LANGUAGE_PROPERTY = "defaultLanguage";
  public static String THEME_PROPERTY = "defaultTheme";
  public static String PRIMEFACES_THEME_PROPERTY = "primefacesTheme";
  public static String FONT_SIZE_PROPERTY = "fontSize";
  public static String RECENT_PAGES_SIZE_PROPERTY = "recentPagesSize";

  private final static float PURGE_PROBABILITY = 0.05f;

  private Boolean purgePreferences;
  private final String userId;
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
    storePreference(name, value, true);
  }

  public void storePreference(String name, String value, boolean incremental)
  {
    Property property = new Property();
    property.setName(name);
    property.getValue().add(value);
    List<Property> propertyList = new ArrayList();
    propertyList.add(property);
    getSecurityPort().storeUserProperties(userId, propertyList, incremental);
    preferencesMap = null;
  }

  public void removePreference(String name)
  {
    getSecurityPort().removeUserProperties(userId, name, null);
    preferencesMap = null;
  }

  public void removePreference(String name, String value)
  {
    getSecurityPort().removeUserProperties(userId, name, value);
    preferencesMap = null;
  }

  public boolean existsPreference(String name)
  {
    return getPreferencesMap().containsKey(name);
  }

  public boolean existsPreference(String name, String value)
  {
    if (!getPreferencesMap().containsKey(name)) return false;
    else return getPreferencesMap().get(name).contains(value);
  }

  public String getDefaultLanguage() throws Exception
  {
    return getPreference(LANGUAGE_PROPERTY);
  }

  public String getDefaultTheme() throws Exception
  {
    return getPreference(THEME_PROPERTY);
  }

  public String getPrimefacesTheme() throws Exception
  {
    return getPreference(PRIMEFACES_THEME_PROPERTY);
  }

  public String getFontSize() throws Exception
  {
    return getPreference(FONT_SIZE_PROPERTY);
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
      preferencesMap = new HashMap();
      List<Property> propertyList =
        getSecurityPort().findUserProperties(userId, null, null);
      for (Property property : propertyList)
      {
        preferencesMap.put(property.getName(),
          new ArrayList(property.getValue()));
      }
    }
    return preferencesMap;
  }

  private SecurityManagerPort getSecurityPort()
  {
    try
    {
      return SecurityConfigBean.getPort(true);
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

}
