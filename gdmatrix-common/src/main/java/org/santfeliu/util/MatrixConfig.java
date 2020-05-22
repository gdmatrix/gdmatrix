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
package org.santfeliu.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author realor
 */
public class MatrixConfig
{
  public static final String MATRIX_CONFIG_DIR_PROPERTY = "matrix.config.dir";
  public static final String MATRIX_CONFIG_DEFAULT_DIR = "/etc/matrix";

  public static Properties properties = new Properties();

  static final Logger LOGGER = Logger.getLogger("MatrixConfig");

  public static String getProperty(String name)
  {
    return properties.getProperty(name);
  }

  public static void setProperty(String name, String value)
  {
    LOGGER.log(Level.FINEST, "Setting property: {0} = {1}",
      new Object[]{name, value});
    properties.setProperty(name, value);
  }

  public static String getPathProperty(String name)
  {
    String path = properties.getProperty(name);
    if (path != null)
    {
      File dir = new File(getDirectory(), path);
      path = dir.getAbsolutePath();
    }
    return path;
  }

  public static String getClassProperty(Class cls, String name)
  {
    name = cls.getName() + "." + name;
    return properties.getProperty(name);
  }

  public static Properties getProperties()
  {
    return properties;
  }

  public static Properties getBaseProperties(String base)
  {
    Properties selection = new Properties();
    for (Map.Entry entry : properties.entrySet())
    {
      String name = (String)entry.getKey();
      if (name.startsWith(base))
      {
        String value = (String)entry.getValue();
        selection.setProperty(name, value);
      }
    }
    return selection;
  }

  public static Properties getClassProperties(Class cls)
  {
    return getBaseProperties(cls.getName() + ".");
  }

  public static List<String> getPropertyNames()
  {
    ArrayList names = new ArrayList();
    names.addAll(properties.keySet());
    return names;
  }

  public static List<String> getBasePropertyNames(String base)
  {
    ArrayList names = new ArrayList();
    for (Object key : properties.keySet())
    {
      String name = (String)key;
      if (name.startsWith(base))
      {
        names.add(name);
      }
    }
    return names;
  }

  public static File getDirectory()
  {
    String matrixConfigDir = properties.getProperty(MATRIX_CONFIG_DIR_PROPERTY);
    if (matrixConfigDir == null)
    {
      matrixConfigDir = MATRIX_CONFIG_DEFAULT_DIR; // default dir
    }
    return new File(matrixConfigDir);
  }

  /****** deprecated methods ******/

  @Deprecated
  public static String getProperty(String ns, String name)
  {
    return getProperty(name);
  }

  @Deprecated
  public static String getProperty(String ns, String name, String defaultValue)
  {
    return properties.getProperty(name, defaultValue);
  }

  @Deprecated
  public static String getPathProperty(String ns, String name)
  {
    return getPathProperty(name);
  }

  @Deprecated
  public static String getClassProperty(String ns, Object obj, String name)
  {
    Class cls;
    if (obj instanceof Class) cls = (Class)obj;
    else cls = obj.getClass();
    return getClassProperty(cls, name);
  }

  @Deprecated
  public static Properties getProperties(String ns)
  {
    return getProperties();
  }

  @Deprecated
  public static Properties getProperties(String ns, String base)
  {
    return getBaseProperties(base);
  }

  @Deprecated
  public static Properties getClassProperties(String ns, Object obj)
  {
    Class cls;
    if (obj instanceof Class) cls = (Class)obj;
    else cls = obj.getClass();
    return getBaseProperties(cls.getName() + ".");
  }

  @Deprecated
  public static List<String> getPropertyNames(String ns)
  {
    return getPropertyNames();
  }

  @Deprecated
  public static List<String> getPropertyNames(String ns, String base)
  {
    return getBasePropertyNames(base);
  }
}
