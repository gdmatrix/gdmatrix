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
package org.santfeliu.webapp.setup;

import com.sun.istack.logging.Logger;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.Property;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.santfeliu.doc.client.CachedDocumentManagerClient;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.webapp.TypeBean;

/**
 *
 * @author realor
 */
public class ObjectSetupCache
{
  public static final String OBJECT_SETUP_DOCUMENT_TYPE  = "ObjectSetup";
  public static final String OBJECT_SETUP_PROPERTY_NAME  = "setupName";
  public static final Logger LOGGER = Logger.getLogger(ObjectSetupCache.class);

  private static final Map<String, ObjectSetup> cache = new HashMap<>();
  private static long lastRefresh = System.currentTimeMillis();

  public static synchronized ObjectSetup getConfig(String setupName)
    throws Exception
  {
    if (System.currentTimeMillis() - lastRefresh > 10000)
    {
      refresh();
    }

    ObjectSetup objectSetup = cache.get(setupName);
    if (objectSetup == null)
    {
      File file = getConfigFile(setupName);
            
      try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(new FileInputStream(file), 
          StandardCharsets.UTF_8)))
      {
        objectSetup = ObjectSetup.read(reader);
        String typeId = objectSetup.getTypeId();
        if (!StringUtils.isBlank(typeId))
        {
          TypeBean typeBean = TypeBean.getInstance(typeId);
          ObjectSetup defaultSetup = typeBean.getObjectSetup();
          objectSetup.merge(defaultSetup);
        }
        cache.put(setupName, objectSetup);
      }
    }
    return objectSetup;
  }

  public static synchronized void refresh()
  {
    //get configs modified since last refresh

    LOGGER.log(Level.FINE, "Refreshing cache.");

    long now = System.currentTimeMillis();

    String userId = MatrixConfig.getProperty("adminCredentials.userId");
    String password = MatrixConfig.getProperty("adminCredentials.password");

    if (userId != null)
    {
      DocumentManagerClient client =
        new CachedDocumentManagerClient(userId, password);

      DocumentFilter filter = new DocumentFilter();
      filter.setDocTypeId(OBJECT_SETUP_DOCUMENT_TYPE);
      filter.setIncludeContentMetadata(true);
      Property property = new Property();
      property.setName(OBJECT_SETUP_PROPERTY_NAME);
      property.getValue().add("%");
      filter.getProperty().add(property);
      filter.getOutputProperty().add(OBJECT_SETUP_PROPERTY_NAME);

      Date lastRefreshDate = new Date(lastRefresh);
      SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
      String dateTime = df.format(lastRefreshDate);

      filter.setDateComparator("1"); //changeDateTime
      filter.setStartDate(dateTime);

      List<Document> documents = client.findDocuments(filter);
      if (documents != null && !documents.isEmpty())
      {
        for (Document document : documents)
        {
          String configName =
            DocumentUtils.getPropertyValue(document, OBJECT_SETUP_PROPERTY_NAME);
          cache.remove(configName);
        }
      }
    }
    else
    {
      cache.clear();
    }
    lastRefresh = now;
  }

  public static synchronized void clear()
  {
    cache.clear();
  }

  private static File getConfigFile(String configName) throws Exception
  {
    String userId = MatrixConfig.getProperty("adminCredentials.userId");
    String password = MatrixConfig.getProperty("adminCredentials.password");
    if (userId == null)
      LOGGER.warning("Trying to execute without administrator credentials");

    DocumentManagerClient client =
      new CachedDocumentManagerClient(userId, password);

    DocumentFilter filter = new DocumentFilter();
    filter.setDocTypeId(OBJECT_SETUP_DOCUMENT_TYPE);
    filter.setIncludeContentMetadata(true);
    Property property = new Property();
    property.setName(OBJECT_SETUP_PROPERTY_NAME);
    property.getValue().add(configName);
    filter.getProperty().add(property);
    List<Document> documents = client.findDocuments(filter);

    if (!documents.isEmpty())
    {
      Document document = documents.get(0);
      Content content = document.getContent();
      if (content != null)
      {
        String contentId = content.getContentId();
        return client.getContentFile(contentId);
      }
    }
    throw new IOException("Object config not found: " + configName);
  }

}
