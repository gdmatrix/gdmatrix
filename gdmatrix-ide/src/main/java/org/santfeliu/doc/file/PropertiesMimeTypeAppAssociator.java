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
package org.santfeliu.doc.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;


/**
 *
 * @author realor
 */
public class PropertiesMimeTypeAppAssociator implements MimeTypeAppAssociator
{
  public static final String OP_SEPARATOR = "*";
  static Properties properties;
  String filename = System.getProperty("user.home") + "/filetypes.properties";

  public PropertiesMimeTypeAppAssociator()
  {
  }

  public void setFilename(String filename)
  {
    this.filename = filename;
  }

  public String getApplicationPath(String mimeType, String operation)
    throws Exception
  {
    if (properties == null)
    {
      properties = new Properties();
      load();
    }

    String path = null;
    if (mimeType != null && operation != null)
      path = properties.getProperty(mimeType + OP_SEPARATOR + operation);

    return path;
  }

  @Override
  public void setApplicationPath(String mimeType, String operation,
    String path) throws Exception
  {
    String key;
    if (mimeType != null && operation != null)
    {
      key = mimeType + OP_SEPARATOR + operation;
      putApplicationPath(key, path);
    }
    store();
  }

  private void putApplicationPath(String key, String path)
  {
    properties.put(key, path);
  }

  private void load()
    throws Exception
  {
    File file = new File(filename);
    if (file.exists())
    {
      FileInputStream fis = new FileInputStream(file);
      properties.load(fis);
      fis.close();
    }
  }

  private void store()
    throws Exception
  {
    File file = new File(filename);
    FileOutputStream fos = new FileOutputStream(file);
    try
    {
      properties.store(fos, "PropertiesMimeTypeAppBinder");
    }
    finally
    {
      fos.close();
    }
  }

  public Map getProperties()
    throws Exception
  {
    if (properties == null)
      properties = new Properties();
    load();
    return properties;
  }

  public void setProperties(Map props)
    throws Exception
  {
    if (properties == null)
      properties = new Properties();
    properties.clear();
    Set<Map.Entry<String, String>> propertySet = props.entrySet();
    for (Map.Entry<String, String> property : propertySet)
    {
      String key = property.getKey();
      String path = property.getValue();
      putApplicationPath(key, path);
    }
    store();
  }
}
