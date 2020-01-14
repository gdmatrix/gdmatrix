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
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.collections.LRUMap;
import org.matrix.dic.Property;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.santfeliu.doc.client.CachedDocumentManagerClient;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.jmx.CacheMBean;
import org.santfeliu.jmx.JMXUtils;
import org.santfeliu.util.IOUtils;


/**
 * Client class to read and execute script documents.
 * 
 * @author blanquepa
 */
public class ScriptClient
{
  private static final int MAX_CACHE_SIZE = 25;
  private static long lastCacheRefresh = System.currentTimeMillis();
  private static final long REFRESH_TIME = 60 * 1000; //60 seconds
  
  public static final String SCRIPT_DOCUMENT_TYPE  = "CODE";
  public static final String SCRIPT_PROPERTY_NAME  = "workflow.js";  
  
  private static final Map cache = 
    Collections.synchronizedMap(new LRUMap(MAX_CACHE_SIZE));
  
  protected String userId;
  protected String password;
  protected Scriptable scope;
  
  protected final Context context;
  
  static
  {
    JMXUtils.registerMBean("ScriptClientCache", getCacheMBean());
  }
  
  public ScriptClient()
  {
    context = ContextFactory.getGlobal().enterContext();  
  }
  
  public ScriptClient(String userId, String password)
  {
    context = ContextFactory.getGlobal().enterContext();      
    this.userId = userId;
    this.password = password;
  }

  /* Put objects into scope */
  public void put(String key, Object object)
  {
    if (scope == null)
      scope = new ScriptableBase(context);

    scope.put(key, scope, object);
  }
  
  public Object get(String key)
  {
    if (scope != null)
      return scope.get(key, scope);
    else
      return null;
  }
  
  private void clearCache(long now)
  {
    //get scripts modified since last refresh
    List<Document> documents = getModifiedScripts();
    if (documents != null && !documents.isEmpty())
    {
      for (Document document : documents)
      {
        String docScriptName = DocumentUtils.getPropertyValue(document, SCRIPT_PROPERTY_NAME);
        cache.remove(docScriptName);
      }
    }
    lastCacheRefresh = now;     
  }
  
  private Script getScript(String scriptName) throws IOException
  {
    Script script = (Script) cache.get(scriptName);
    if (script == null)
    {
      ScriptData scriptData = getScriptData(scriptName);

      File scriptFile = scriptData.scriptFile;
      InputStreamReader reader =
        new InputStreamReader(new FileInputStream(scriptFile), "UTF-8");

      script = context.compileReader(reader, scriptName, 0, null);
      cache.put(scriptName, script);
    }
    return script;
  }
  
  public Object executeScript(String scriptName) throws Exception
  {
    if (scope == null)
      scope = new ScriptableBase(context);

    return executeScript(scriptName, scope);
  }

  public Object executeScript(String scriptName, Scriptable scope)
    throws Exception
  {
    Script script;
    
    long now = System.currentTimeMillis();
    if (lastCacheRefresh + REFRESH_TIME < now)
      clearCache(now);

    script = getScript(scriptName);
    
    Object result = null;    
    try
    {
      result = script.exec(context, scope);
      if (result instanceof NativeJavaObject)
      {
        NativeJavaObject nat = (NativeJavaObject)result;
        result = nat.unwrap();
      }
      if (result instanceof Undefined)
        result = null;
    }
    catch (JavaScriptException ex)
    {
      throw new Exception(ex.getMessage());
    }
    finally
    {
      Context.exit();
    }

    return result;
  }
  
  public void writeScript(String scriptName, HttpServletResponse response) 
    throws IOException
  {
    ScriptData scriptData = getScriptData(scriptName);

    response.setContentType(scriptData.contentType);
    response.setCharacterEncoding("UTF-8");
    File scriptFile = scriptData.scriptFile;
    IOUtils.writeToStream(new FileInputStream(scriptFile),
      response.getOutputStream());    
  }
  
  private ScriptData getScriptData(String scriptName) throws IOException
  {
    ScriptData scriptData = null;
    DocumentManagerClient client = null;
    if (userId != null && password != null)
      client = new CachedDocumentManagerClient(userId, password);
    else // to download (only public scripts)
      client = new DocumentManagerClient();
    
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
        scriptData = new ScriptData();
        scriptData.scriptName = scriptName;
        scriptData.contentId = content.getContentId();        
        scriptData.scriptFile = client.getContentFile(contentId);
        scriptData.contentType = content.getContentType();
      }
    }
    else throw new IOException("Script not found: " + scriptName);

    return scriptData;
  }  
  
  private List<Document> getModifiedScripts()
  {
    DocumentManagerClient client = null;
    if (userId != null && password != null)
      client = new CachedDocumentManagerClient(userId, password);
    else // to download (only public scripts)
      client = new DocumentManagerClient();
    
    DocumentFilter filter = new DocumentFilter();
    filter.setDocTypeId(SCRIPT_DOCUMENT_TYPE);
    filter.setIncludeContentMetadata(true);
    Property property = new Property();
    property.setName(SCRIPT_PROPERTY_NAME);
    property.getValue().add("%");
    filter.getProperty().add(property);
    filter.getOutputProperty().add(SCRIPT_PROPERTY_NAME);
    
    Date lastRefreshDate = new Date(lastCacheRefresh);
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    String dateTime = df.format(lastRefreshDate);
    
    filter.setMetadataSearchExpression("d.modifydate > '" + dateTime + "'");
    return client.findDocuments(filter);
  }
  
  private static ScriptClientCacheMBean getCacheMBean()
  {
    try
    {
      return new ScriptClientCacheMBean();
    }
    catch (NotCompliantMBeanException ex)
    {
      return null;
    }
  }  
  
  public static class ScriptClientCacheMBean extends StandardMBean implements CacheMBean
  {
    public ScriptClientCacheMBean() throws NotCompliantMBeanException
    {
      super(CacheMBean.class);
    }

    public String getName()
    {
      return "ScriptClientCache";
    }

    public long getMaxSize()
    {
      return MAX_CACHE_SIZE;
    }

    public long getSize()
    {
      return cache.size();
    }

    public String getDetails()
    {
      return "scriptCacheSize=" + getSize() + "/" + getMaxSize();
    }

    public void clear()
    {
      cache.clear();
    }

    public void update()
    {
    }
  }  

  
  private class ScriptData
  {
    String scriptName;
    String contentId;
    File scriptFile;
    String contentType;
  }
  
  
}
