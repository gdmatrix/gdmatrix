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
package org.santfeliu.matrix;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author realor
 */
public class MatrixInfo
{
  static
  {
    loadInfo();
    System.out.println("DONE");
  }

  private static Properties properties;
  private static final String VERSION = "git.build.version";
  private static final String REVISION = "git.total.commit.count";
  private static final String TAGS = "git.tags";

  private static final String[][] TEAM =
  {
    {"Abel Blanque Parcerisa", "blanquepa@ext.santfeliu.cat"},
    {"Jordi López Rodríguez", "lopezrj@ext.santfeliu.cat"},
    {"Ricard Real Osuna", "realor@santfeliu.cat"}
  };
  private static String license;

  public static Properties getProperties()
  {
    return properties;
  }

  public static String getFullVersion()
  {
    String tags = getTags();
    String version = StringUtils.isBlank(tags)? getVersion() : tags;
    return version + "-r" + getRevision();
  }

  public static String getVersion()
  {
    return properties.getProperty(VERSION, "0");
  }

  public static String getRevision()
  {
    return properties.getProperty(REVISION, "0");
  }

  public static String getTags()
  {
    return properties.getProperty(TAGS, null);
  }

  public static String getLicense()
  {
    return license;
  }

  public static String[][] getTeam()
  {
    return TEAM;
  }

  private static void loadInfo()
  {
    try
    {
      properties = new Properties();
      InputStream is =
        MatrixInfo.class.getResourceAsStream("MatrixInfo.properties");
      if (is != null)
      {
        properties.load(is);
      }
    }
    catch (IOException ex)
    {
      // ignore
    }

    try
    {
      InputStream is =
        MatrixInfo.class.getResourceAsStream("LICENSE.txt");
      if (is == null)
      {
        license = "Not defined yet.";
      }
      else
      {
        license = IOUtils.toString(is, "UTF-8");
      }
    }
    catch (IOException ex)
    {
      // ignore
    }
  }

  public static void main(String[] args)
  {
    System.out.println(MatrixInfo.getFullVersion());
    System.out.println(MatrixInfo.getLicense());
  }
}
