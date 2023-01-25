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
package org.santfeliu.util.script;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import org.apache.commons.collections.map.LRUMap;
import org.matrix.dic.Property;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Script;
import org.santfeliu.doc.client.CachedDocumentManagerClient;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.jmx.CacheMBean;
import org.santfeliu.jmx.JMXUtils;
import org.santfeliu.util.MatrixConfig;

/**
 *
 * @author blanquepa
 * @author realor
 */
public class ScriptCache
{
  private static final int MAX_CACHE_SIZE = 50;
  private static long lastRefresh = System.currentTimeMillis();

  public static final String SCRIPT_DOCUMENT_TYPE  = "CODE";
  public static final String SCRIPT_PROPERTY_NAME  = "workflow.js";
  protected static final Logger LOGGER = Logger.getLogger("ScriptCache");

  private static final Map cache =
    Collections.synchronizedMap(new LRUMap(MAX_CACHE_SIZE));

  static
  {
    JMXUtils.registerMBean("ScriptCache", getCacheMBean());
  }

  public static synchronized Script getScript(String scriptName)
    throws Exception
  {
    Script script = (Script) cache.get(scriptName);
    if (script == null)
    {
      File scriptFile = getScriptFile(scriptName);

      try  (InputStreamReader reader =
            new InputStreamReader(new FileInputStream(scriptFile), "UTF-8"))
      {
        Context context = ContextFactory.getGlobal().enterContext();
        try
        {
          LOGGER.log(Level.INFO, "Compiling script {0}", scriptName);
          script = context.compileReader(reader, scriptName, 0, null);
          cache.put(scriptName, script);
        }
        finally
        {
          Context.exit();
        }
      }
    }
    return script;
  }

  public static synchronized void clear()
  {
    LOGGER.log(Level.FINE, "Clearing cache.");

    cache.clear();
  }

  public static synchronized void refresh()
  {
    //get scripts modified since last refresh

    LOGGER.log(Level.FINE, "Refreshing cache.");

    long now = System.currentTimeMillis();

    String userId = MatrixConfig.getProperty("adminCredentials.userId");
    String password = MatrixConfig.getProperty("adminCredentials.password");

    if (userId != null)
    {
      DocumentManagerClient client =
        new CachedDocumentManagerClient(userId, password);

      DocumentFilter filter = new DocumentFilter();
      filter.setDocTypeId(SCRIPT_DOCUMENT_TYPE);
      filter.setIncludeContentMetadata(true);
      Property property = new Property();
      property.setName(SCRIPT_PROPERTY_NAME);
      property.getValue().add("%");
      filter.getProperty().add(property);
      filter.getOutputProperty().add(SCRIPT_PROPERTY_NAME);

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
          String docScriptName =
            DocumentUtils.getPropertyValue(document, SCRIPT_PROPERTY_NAME);
          cache.remove(docScriptName);
        }
      }
    }
    else
    {
      //If it's not executed by user with admin rights it is not guaranted that
      //script document is found to be refreshed then keep cache clear.
      cache.clear();
    }
    lastRefresh = now;
  }

  public static long getLastRefresh()
  {
    return lastRefresh;
  }

  private static File getScriptFile(String scriptName) throws Exception
  {
    String userId = MatrixConfig.getProperty("adminCredentials.userId");
    String password = MatrixConfig.getProperty("adminCredentials.password");
    if (userId == null)
      LOGGER.warning("Trying to execute without administrator credentials");

    DocumentManagerClient client =
      new CachedDocumentManagerClient(userId, password);

    DocumentFilter filter = new DocumentFilter();
    filter.setDocTypeId(SCRIPT_DOCUMENT_TYPE);
    filter.setIncludeContentMetadata(true);
    Property property = new Property();
    property.setName(SCRIPT_PROPERTY_NAME);
    property.getValue().add(scriptName);
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
    throw new IOException("Script not found: " + scriptName);
  }

  private static ScriptCacheMBean getCacheMBean()
  {
    try
    {
      return new ScriptCacheMBean();
    }
    catch (NotCompliantMBeanException ex)
    {
      return null;
    }
  }

  public static class ScriptCacheMBean extends StandardMBean
    implements CacheMBean
  {
    public ScriptCacheMBean() throws NotCompliantMBeanException
    {
      super(CacheMBean.class);
    }

    @Override
    public String getName()
    {
      return "ScriptClientCache";
    }

    @Override
    public long getMaxSize()
    {
      return MAX_CACHE_SIZE;
    }

    @Override
    public long getSize()
    {
      return cache.size();
    }

    @Override
    public String getDetails()
    {
      return "scriptCacheSize=" + getSize() + "/" + getMaxSize();
    }

    @Override
    public void clear()
    {
      cache.clear();
    }

    @Override
    public void update()
    {
    }
  }
}
