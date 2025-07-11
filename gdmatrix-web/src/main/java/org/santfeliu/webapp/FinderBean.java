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
package org.santfeliu.webapp;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.activation.DataHandler;
import org.matrix.dic.Property;
import org.matrix.dic.PropertyDefinition;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.mozilla.javascript.Callable;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.util.BigList;
import org.santfeliu.util.script.ScriptClient;
import org.santfeliu.web.ApplicationBean;
import org.santfeliu.web.UserSessionBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.modules.doc.DocModuleBean;
import org.santfeliu.webapp.setup.Action;
import static org.santfeliu.webapp.setup.Action.PUT_DEFAULT_FILTER;
import org.santfeliu.webapp.setup.ActionObject;
import org.santfeliu.webapp.setup.ObjectSetup;
import org.santfeliu.webapp.setup.ObjectSetupCache;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author realor
 */
public abstract class FinderBean extends BaseBean
{
  private static final String SMART_SEARCH_TIP_DOCID_PROPERTY =
    "smartSearchTipDocId";
  private static final String DEFAULT_SEARCH_PAGE_SIZE_PROPERTY =
    "defaultSearchPageSize";
  private static final String SESSION_PROPERTIES = 
    "sessionProperties";
  private static final String FILTER_PREFIX = 
    "finder:"; 
  
  private static final Map<String, String> smartSearchTipContentMap =
    new HashMap();
  private static long lastSmartSearchTipRefresh = System.currentTimeMillis();

  private int filterTabSelector;
  private int objectPosition = -1;
  private boolean finding;
  private transient ObjectSetup objectSetup;
  private transient ScriptClient scriptClient;
  private int pageSize = -1;

  public int getFilterTabSelector()
  {
    return filterTabSelector;
  }

  public void setFilterTabSelector(int selector)
  {
    this.filterTabSelector = selector;
  }

  public abstract void find();

  public abstract void smartFind();

  public abstract Object getFilter();
  
  public abstract List getRows();

  protected void setActionResult(ActionObject scriptObject)
  {
  }

  public int getObjectCount()
  {
    return 0;
  }

  public String getObjectId(int position)
  {
    return NEW_OBJECT_ID;
  }

  public int getObjectPosition()
  {
    return objectPosition;
  }

  public void setObjectPosition(int objectPosition)
  {
    this.objectPosition = objectPosition;
  }

  public boolean isFinding()
  {
    return finding;
  }

  public void setFinding(boolean finding)
  {
    this.finding = finding;
  }

  public int getPageSize() 
  {
    if (pageSize == -1)
    {
      pageSize = getDefaultPageSize();
    }
    return pageSize;
  }

  public void setPageSize(int pageSize) 
  {
    if (this.pageSize != pageSize)
    {
      List rows = getRows();
      if (rows instanceof BigList)
      {
        ((BigList)rows).setCacheSize(2 * pageSize + 1);
        ((BigList)rows).setBlockSize(pageSize);
      }
    }
    this.pageSize = pageSize;    
  }
  
  public ObjectSetup getObjectSetup() throws Exception
  {
    if (objectSetup == null)
    {
      loadObjectSetup();
    }
    return objectSetup;
  }
  
  public ScriptClient getScriptClient(String scriptName) throws Exception
  {
    if (scriptClient == null)
      scriptClient = new ScriptClient();      
    
    if (scriptClient.get("userSessionBean") == null)
    {
      scriptClient.put("userSessionBean", UserSessionBean.getCurrentInstance());
      scriptClient.put("applicationBean", ApplicationBean.getCurrentInstance());
      scriptClient.put("WebUtils",
        WebUtils.class.getConstructor().newInstance());
      scriptClient.put("DictionaryUtils",
        DictionaryUtils.class.getConstructor().newInstance());         
      scriptClient.executeScript(scriptName);
    } 
    
    return scriptClient;
  }
  
  @Override
  public void clear()
  {
    objectSetup = null;
    pageSize = -1;
  }

  public void putDefaultFilter()
  {
    executeAction(PUT_DEFAULT_FILTER, getFilter());
  }

  public String getSmartSearchTip()
  {
    if (System.currentTimeMillis() - lastSmartSearchTipRefresh >
      (60 * 60 * 1000))
    {
      smartSearchTipContentMap.clear();
      lastSmartSearchTipRefresh = System.currentTimeMillis();
    }

    String docId = getSmartSearchTipDocId();
    if (docId != null)
    {
      if (!smartSearchTipContentMap.containsKey(docId))
      {
        String content = getDocContent(docId);
        smartSearchTipContentMap.put(docId, content);
      }
      return smartSearchTipContentMap.get(docId);
    }
    else
    {
      return null;
    }
  }

  public boolean isScrollEnabled()
  {
    if (objectPosition < 0 || objectPosition >= getObjectCount()) return false;

    ObjectBean objectBean = getObjectBean();

    return getObjectCount() > 1 && !objectBean.isNew() &&
      objectBean.getObjectId().equals(getObjectId(objectPosition));
  }

  public boolean hasNext()
  {
    return objectPosition >= 0 && objectPosition < getObjectCount() - 1;
  }

  public boolean hasPrevious()
  {
    return objectPosition >= 1;
  }

  public void view(int objectPosition)
  {
    if (objectPosition < 0 || objectPosition >= getObjectCount()) return;

    this.objectPosition = objectPosition;
    String objectId = getObjectId(objectPosition);
    if (!NEW_OBJECT_ID.equals(objectId))
    {
      NavigatorBean navigatorBean = WebUtils.getBean("navigatorBean");
      navigatorBean.view(objectId);
    }
  }

  public void viewNext()
  {
    if (hasNext())
    {
      view(objectPosition + 1);
    }
  }

  public void viewPrevious()
  {
    if (hasPrevious())
    {
      view(objectPosition - 1);
    }
  }

  public boolean isRender(String propertyName)
  {
    try
    {
      List hideProperties = (List)getObjectSetup().getProperties().
        get("hideFinderProperties");
      return (hideProperties == null || !hideProperties.contains(propertyName));
    }
    catch (Exception ex)
    {
      return true;
    }    
  }  
  
  protected ActionObject executeAction(String actionName, Object object)
  {
    ActionObject actionObject = new ActionObject(object);
    try
    {
      ObjectSetup setup = getObjectSetup();
      
      if (setup.getScriptName() == null)
        return actionObject;
      
      if (setup.containsPredefindedActions() &&
        Action.predefinedActionNames.contains(actionName) &&
        !setup.containsAction(actionName))
      {
        return actionObject;
      }
      
      ScriptClient client = getScriptClient(setup.getScriptName());
      Object callable = client.get(actionName);
      if (callable instanceof Callable)
      {
        client.put("actionObject", actionObject);
        client.execute((Callable)callable);
        actionObject = (ActionObject)client.get("actionObject");
        getObjectBean().addFacesMessages(actionObject.getMessages());
      }      
    }
    catch (Exception ex)
    {
      error(ex);
    }

    return actionObject;
  }
    
  protected Object getSessionProperties(Object filter)
  {    
    if (WebUtils.isRenderResponsePhase())
    {
      List<String> propNames = getSessionPropertyNames();
      if (propNames != null && !propNames.isEmpty())
      {    
        UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();        
        for (String propName: propNames)
        {
          setSessionValuesToFilter(userSessionBean, filter, propName);
        } 
      }
    }
    return filter;
  } 
  
  protected void setSessionProperties(Object filter)
  {
    List<String> propNames = getSessionPropertyNames();
    if (propNames != null && !propNames.isEmpty())
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      for (String propName: propNames)
      {
        setFilterValuesToSession(userSessionBean, filter, propName);
      } 
    }    
  }  

  protected void clearSessionProperties()
  {
    List<String> propNames = getSessionPropertyNames();
    if (propNames != null && !propNames.isEmpty())
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      for (String propName: propNames)
      {
        userSessionBean.getAttributes().remove(FILTER_PREFIX + propName);
      } 
    }      
  }
  
  private List<String> getSessionPropertyNames()
  {
    try
    {
      return getObjectSetup().getProperties().getList(SESSION_PROPERTIES);
    }
    catch (Exception ex)
    {
      return Collections.EMPTY_LIST;
    }
  }    
  
  private void setSessionValuesToFilter(UserSessionBean userSessionBean, 
    Object filter, String propName)
  {
    String value = 
      (String) userSessionBean.getAttribute(FILTER_PREFIX + propName);
    
    Property pValue = DictionaryUtils.getProperty(filter, propName);
    if (value != null && (pValue == null || !pValue.getValue().isEmpty()) ||
      (value == null && pValue != null && !pValue.getValue().isEmpty()))
    {          
      DictionaryUtils.setProperty(filter, propName, value);
    }    
  }
  
  private void setFilterValuesToSession(UserSessionBean userSessionBean, 
    Object filter, String propName)
  {
    String sessionValue = 
      (String) userSessionBean.getAttribute(FILTER_PREFIX + propName);
      
    Property property = DictionaryUtils.getProperty(filter, propName);
    if (property != null)
    {
      String value = (property.getValue().isEmpty() ? null :
          property.getValue().get(0));    

      if ((value != null && !value.equals(sessionValue)) 
        || (value == null && sessionValue != null))
      {
        userSessionBean.getAttributes().put(FILTER_PREFIX + propName, value);
      }  
    }
    else if (sessionValue != null)
      userSessionBean.getAttributes().put(FILTER_PREFIX + propName, null);  
  }   
  
  private void loadObjectSetup() throws Exception
  {
    String setupName = getProperty("objectSetup");
    if (setupName == null)
    {
      NavigatorBean navigatorBean = WebUtils.getBean("navigatorBean");
      String typeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
      Type type = TypeCache.getInstance().getType(typeId);
      PropertyDefinition propdef = type.getPropertyDefinition("objectSetup");
      if (propdef != null && !propdef.getValue().isEmpty())
      {
        setupName = propdef.getValue().get(0);
      }
    }

    ObjectSetup defaultSetup = getObjectBean().getTypeBean().getObjectSetup();
    if (setupName != null)
    {
      objectSetup = ObjectSetupCache.getConfig(setupName);
    }
    else
    {
      objectSetup = defaultSetup;
    }
  }

  private String getSmartSearchTipDocId()
  {
    try
    {
      String docId = getObjectSetup().getSmartSearchTipDocId();
      if (docId == null)
      {
        docId = getProperty(SMART_SEARCH_TIP_DOCID_PROPERTY);
        if (docId == null)
        {
          NavigatorBean navigatorBean = WebUtils.getBean("navigatorBean");
          String typeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
          Type type = TypeCache.getInstance().getType(typeId);
          PropertyDefinition propdef =
            type.getPropertyDefinition("_" + SMART_SEARCH_TIP_DOCID_PROPERTY);
          if (propdef != null && !propdef.getValue().isEmpty())
          {
            docId = propdef.getValue().get(0);
          }
        }
      }
      return docId;
    }
    catch (Exception ex)
    {
      return null;
    }
  }
  
  private int getDefaultPageSize()
  {
    try
    {
      String val = getObjectSetup().getDefaultSearchPageSize();
      if (val == null)
      {
        val = getProperty(DEFAULT_SEARCH_PAGE_SIZE_PROPERTY);
        if (val == null)
        {
          NavigatorBean navigatorBean = WebUtils.getBean("navigatorBean");
          String typeId = navigatorBean.getBaseTypeInfo().getBaseTypeId();
          Type type = TypeCache.getInstance().getType(typeId);
          PropertyDefinition propdef =
            type.getPropertyDefinition("_" + DEFAULT_SEARCH_PAGE_SIZE_PROPERTY);
          if (propdef != null && !propdef.getValue().isEmpty())
          {
            val = propdef.getValue().get(0);
          }
        }
      }
      return Integer.parseInt(val);
    }
    catch (Exception ex)
    {
      return 10;
    }
  }  

  private String getDocContent(String docId)
  {
    try
    {
      Document document = DocModuleBean.getPort(true).loadDocument(docId, 0,
        ContentInfo.ALL);
      DataHandler dh = DocumentUtils.getContentData(document);
      long size = document.getContent().getSize();
      int iSize = (int)size;
      InputStream is = dh.getInputStream();
      byte[] byteArray = new byte[iSize];
      is.read(byteArray);
      return new String(byteArray);
    }
    catch (Exception ex)
    {
      return null;
    }
  }

}
