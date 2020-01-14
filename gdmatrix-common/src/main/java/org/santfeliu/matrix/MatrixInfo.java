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

import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author unknown
 */
public class MatrixInfo
{
  static
  {
    loadInfo();
  }

  private static Properties properties;
  private static final String VERSION = "version";
  private static final String REVISION = "revision";
  private static final String DATE = "date";

  private static final String LICENSE = "Not defined yet.";
  private static final String[][] TEAM =
  {
    {"Abel Blanque Parcerisa", "blanquepa@ext.santfeliu.cat"},    
    {"Jordi López Rodríguez", "lopezrj@ext.santfeliu.cat"},
    {"Ricard Real Osuna", "realor@santfeliu.cat"}
  };

  public static Properties getProperties()
  {
    return properties;
  }

  public static String getFullVersion()
  {
    return getVersion() + " (revision " + getRevision() + ")";
  }

  public static String getVersion()
  {
    return properties.getProperty(VERSION, "???");
  }

  public static String getRevision()
  {
    return properties.getProperty(REVISION, "???");
  }

  public static String getLicense()
  {
    return LICENSE;
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
      if (is != null) properties.load(is);
    }
    catch (Exception ex)
    {
    }
  }

  public static void main(String[] args)
  {
    System.out.println(MatrixInfo.getFullVersion());
  }
}
