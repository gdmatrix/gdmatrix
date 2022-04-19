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
import org.santfeliu.util.MatrixConfig;


/**
 * Client class to read and execute script documents. 
 * Scripts executed with adminCredentials defined in MatrixConfig.
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
  protected static final Logger LOGGER = Logger.getLogger("ScriptClient");  
  
  private static final Map cache = 
    Collections.synchronizedMap(new LRUMap(MAX_CACHE_SIZE));
  
  private final Context context;
  
  protected Scriptable scope;
  
  static
  {
    JMXUtils.registerMBean("ScriptClientCache", getCacheMBean());
  }
     
  public ScriptClient()
  {
    context = ContextFactory.getGlobal().enterContext();  
  }
  
  public ScriptClient(Context context)
  {
    this.context = context;
  }
  
  public ScriptClient(Context context, Scriptable scope)
  {
    this.context = context;
    this.scope = scope;
  }

  protected Context getContext()
  {
    return context;
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
  
  public Object execute(Scriptable scope, String method)
  {
    return context.evaluateString(scope, method, "", 1, null);  
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
    LOGGER.log(Level.INFO, "Executing {0} script.", new Object[]{scriptName});
    
    return executeScript(scriptName, null, scope);
  } 
  
  public Object executeScript(String scriptName, String methodExpr)
    throws Exception
  {
    if (scope == null)
      scope = new ScriptableBase(context);
    
    return executeScript(scriptName, methodExpr, scope);
  }
  
  public Object executeScript(String scriptName, String methodExpr, 
    Scriptable scope) throws Exception
  {
    if (methodExpr != null)
    {
      LOGGER.log(Level.INFO, "Executing {0}.{1} script.", 
        new Object[]{scriptName, methodExpr});   
    }
    
    Script script = getScript(scriptName, context);

    Object result;
    try
    {
      result = script.exec(context, scope);
      if (methodExpr != null)
        result = context.evaluateString(scope, methodExpr, scriptName, 1, null);
      result = unwrap(result);
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
  
  public static Object unwrap(Object result)
  {
    if (result instanceof NativeJavaObject)
    {
      NativeJavaObject nat = (NativeJavaObject)result;
      result = nat.unwrap();
    }
    if (result instanceof Undefined)
      result = null;

    return result;
  }
  
  private Script getScript(String scriptName, Context context) throws Exception
  {
    long now = System.currentTimeMillis();
    if (lastCacheRefresh + REFRESH_TIME < now)
      clearCache(now);
    
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
    
  private ScriptData getScriptData(String scriptName) throws Exception
  {
    ScriptData scriptData = null;
    
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
  
  private void clearCache(long now)
  {
    //get scripts modified since last refresh
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
    
      Date lastRefreshDate = new Date(lastCacheRefresh);
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
    lastCacheRefresh = now;     
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

  public static class ScriptClientCacheMBean extends StandardMBean 
    implements CacheMBean
  {
    public ScriptClientCacheMBean() throws NotCompliantMBeanException
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

  private class ScriptData
  {
    String scriptName;
    String contentId;
    File scriptFile;
    String contentType;
  }
}
