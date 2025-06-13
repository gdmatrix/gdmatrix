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

import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.util.WebUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.faces.application.FacesMessage;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.PropertyDefinition;
import org.mozilla.javascript.Callable;
import org.primefaces.PrimeFaces;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.util.script.ScriptClient;
import org.santfeliu.web.ApplicationBean;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.webapp.NavigatorBean.BaseTypeInfo;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.setup.ObjectSetup;
import org.santfeliu.webapp.setup.ObjectSetupCache;
import org.santfeliu.webapp.setup.Action;
import org.santfeliu.webapp.setup.ActionObject;
import org.santfeliu.webapp.setup.ActionObject.Message;
import org.santfeliu.webapp.setup.ActionObject.Message.Severity;
import org.santfeliu.webapp.setup.SearchTab;
import org.santfeliu.webapp.util.ComponentUtils;

/**
 *
 * @author realor
 */
public abstract class ObjectBean extends BaseBean
{
  public static final String OBJECT_SETUP_PROPERTY = "objectSetup";

  protected String objectId = NEW_OBJECT_ID;
  private int searchTabSelector;
  private int editTabSelector;
  private String editTabRowId;
  private transient ObjectSetup objectSetup;
  private transient ScriptClient scriptClient;
  private Map<String, Object> state = new HashMap<>();
  private List<Action> actions;

  @Override
  public ObjectBean getObjectBean()
  {
    return this;
  }

  public abstract TypeBean getTypeBean();

  public abstract FinderBean getFinderBean();

  public String getObjectId()
  {
    return objectId;
  }

  public void setObjectId(String objectId)
  {
    this.objectId = objectId;
  }

  public abstract String getRootTypeId();

  public String getDescription()
  {
    return isNew() ? "" : objectId;
  }

  public boolean isNew()
  {
    return NEW_OBJECT_ID.equals(objectId);
  }

  public int getSearchTabSelector()
  {
    return searchTabSelector;
  }

  public void setSearchTabSelector(int selector)
  {
    this.searchTabSelector = selector;
  }

  public int getEditTabSelector()
  {
    return editTabSelector;
  }

  public void setEditTabSelector(int selector)
  {
    this.editTabSelector = selector;
  }

  public String getEditTabRowId()
  {
    return editTabRowId;
  }

  public void setEditTabRowId(String editTabRowId)
  {
    this.editTabRowId = editTabRowId;
  }

  public int getEditModeSelector()
  {
    return 1;
  }

  public List<SearchTab> getSearchTabs()
  {
    try
    {
      return getObjectSetup() == null ?
        Collections.EMPTY_LIST : getObjectSetup().getSearchTabs();
    }
    catch (Exception ex)
    {
      return Collections.EMPTY_LIST;
    }
  }

  public SearchTab getActiveSearchTab()
  {
    int selector = getSearchTabSelector();
    if (selector >= getSearchTabs().size()) return null;

    return getSearchTabs().get(selector);
  }

  public List<EditTab> getEditTabs()
  {
    try
    {
      return getObjectSetup() == null ?
        Collections.EMPTY_LIST : getObjectSetup().getEditTabs();
    }
    catch (Exception ex)
    {
      return Collections.EMPTY_LIST;
    }
  }

  public EditTab getActiveEditTab()
  {
    int selector = getEditTabSelector();
    if (selector >= getEditTabs().size()) return null;

    return getEditTabs().get(selector);
  }

  public List<String> getDialogViewIds()
  {
    List<String> dialogViewIds = new ArrayList<>();
    for (EditTab tab : getEditTabs())
    {
      String dialogViewId = tab.getDialogViewId();
      if (!dialogViewIds.contains(dialogViewId))
      {
        dialogViewIds.add(dialogViewId);
      }
    }
    return dialogViewIds;
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

  public List<Action> getActions()
  {
    return actions;
  }

  public TabBean getTabBean(EditTab editTab)
  {
    return (TabBean)WebUtils.getBean(editTab.getBeanName());
  }

  public TabBean getActiveTabBean()
  {
    EditTab tab = getActiveEditTab();
    if (tab == null)
      return null;
    else
      return getTabBean(tab);
  }

  public abstract Object getObject();
  
  public String getObjectTypeId()
  {
    return (isNew() ? null : getTypeBean().getTypeId(getObject()));
  }

  public BaseTypeInfo getBaseTypeInfo()
  {
    NavigatorBean navigatorBean = WebUtils.getBean("navigatorBean");
    return navigatorBean.getBaseTypeInfo();
  }

  public void load()
  {
    load(null);
  }

  public void load(Map<String, Object> parameters)
  {
    try
    {
      clear();
      loadObject();
      loadObjectSetup();
      processParameters(parameters);
      if (!NEW_OBJECT_ID.equals(objectId))
        executeAction(Action.POST_LOAD_ACTION);
      loadActiveEditTab();

      Object object = getObject();
      TypeBean typeBean = getTypeBean();
      if (typeBean != null)
      {
        typeBean.updateDescription(objectId, object);
      }
    }
    catch (Exception ex)
    {
      if (!NEW_OBJECT_ID.equals(objectId))
      {
        // show error
        error(ex);

        // show empty object
        objectId = NEW_OBJECT_ID;
        load();
      }
    }
  }

  public void loadObject() throws Exception
  {
  }

  public void loadObjectSetup() throws Exception
  {
    String setupName = getProperty(OBJECT_SETUP_PROPERTY);
    if (setupName == null)
    {
      // look in dictionary      
      String typeId = getObjectTypeId(); 
      if (typeId == null) typeId = getBaseTypeInfo().getBaseTypeId(); 
      
      if (typeId != null)
      {
        Type type = TypeCache.getInstance().getType(typeId);
        PropertyDefinition propdef = type.getPropertyDefinition("objectSetup");
        if (propdef != null && !propdef.getValue().isEmpty())
        {
          setupName = propdef.getValue().get(0);
        }
      }
    }

    if (setupName == null)
    {
      objectSetup = getTypeBean().getObjectSetup();
    }
    else
    {
      objectSetup = ObjectSetupCache.getConfig(setupName);
    }
  }
  
  public void loadActiveEditTab() throws Exception
  {
    EditTab tab = getActiveEditTab();
    if (tab == null) return;

    String beanName = tab.getBeanName();
    if (beanName != null)
    {
      Object bean = WebUtils.getBean(beanName);
      if (bean instanceof TabBean)
      {
        TabBean tabBean = (TabBean)bean;
        if (!objectId.equals(tabBean.getObjectId()))
        {
          tabBean.setObjectId(objectId);
          tabBean.load();
          if (editTabRowId != null)
          {
            tabBean.edit(editTabRowId);
            editTabRowId = null;
          }
        }
      }
    }
  }

  public void loadActions()
  {
    try
    {
      if (actions != null) return;

      System.out.println(">>> loadActions");
      actions = new ArrayList();
      for (Action action : getObjectSetup().getActions())
      {
        if (!Action.predefinedActionNames.contains(action.getName()))
          actions.add(action);
      }

      ObjectSetup setup = getObjectSetup();

      //If not script defined then return
      if (setup.getScriptName() == null) return;
      //If predefined actions are set but not GET_ACTIONS_ACTION then return
      if (setup.containsPredefindedActions() &&
        !setup.containsAction(Action.GET_ACTIONS_ACTION)) return;

      ScriptClient client = getScriptClient(setup.getScriptName());
      Object callable = client.get(Action.GET_ACTIONS_ACTION);
      if (callable instanceof Callable)
      {
        client.put("actionObject", new ActionObject(getObject()));
        List<Action> actionList =
          (List<Action>) client.execute((Callable)callable);

        if (actionList != null)
          actions.addAll(actionList);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void callAction(String actionName)
  {
    Action action = getAction(actionName);
    if (action != null)
      executeAction(action.getName(), action.getParameters());
  }

  protected Action getAction(String name)
  {
    Action action = null;
    if (actions != null && name != null)
    {
      action = actions.stream()
        .filter(a -> name.equals(a.getName()))
        .findFirst()
        .orElse(null);
    }
    return action;
  }

  protected ActionObject executeAction(String actionName)
  {
    return executeAction(actionName, null);
  }

  protected ActionObject executeAction(String actionName, Object[] parameters)
  {
    return executeAction(actionName, parameters, getObject());
  }

  protected ActionObject executeAction(String actionName, Object[] parameters,
    Object object)
  {
    ActionObject actionObject = new ActionObject(object);
    actionObject.setMainObject(getObject());

    ScriptClient client;
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
      client = getScriptClient(setup.getScriptName());
    }
    catch (Exception ex)
    {
      error(ex);
      return actionObject;
    }
    
    if (client != null)
    {
      Object callable = client.get(actionName);
      if (callable instanceof Callable)
      {
        client.put("actionObject", actionObject);
        client.put("state", state);
        client.execute((Callable)callable, parameters);
        state = (Map<String, Object>) client.get("state");
        actionObject = (ActionObject) client.get("actionObject");
        if (actionObject != null)
        {
          setActionResult(actionObject);
          if (actionObject.isUpdateForm())
          {
            getRequestMap().put("updateForm", "true");
          }
          if (actionObject.isRefresh() && !actionName.equals(Action.POST_LOAD_ACTION))
            load();
          addFacesMessages(actionObject.getMessages());
        }
      }
    }

    return actionObject;
  }

  public ActionObject executeTabAction(String actionName, Object object)
  {
    ActionObject actionObject =
      new ActionObject(object, getActiveEditTab().getSubviewId());
    actionObject.setMainObject(getObject());
    
    ScriptClient client = null;
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
      client = getScriptClient(setup.getScriptName());
    }
    catch (Exception ex)
    {
      error(ex);
    }
    
    if (client != null)
    {
      Object callable = client.get(actionName);
      if (callable instanceof Callable)
      {
        client.put("actionObject", actionObject);
        client.put("state", state);
        client.execute((Callable)callable);
        actionObject = (ActionObject) client.get("actionObject");
        if (actionObject.isUpdateForm())
        {
          getRequestMap().put("updateForm", "true");
        }
        if (actionObject.isRefresh())
          load();
        if (actionObject.isFullRefresh())
        {
          getBaseTypeInfo().visit(objectId);
          TypeBean typeBean = getTypeBean();
          if (typeBean != null)
          {
            typeBean.updateDescription(objectId, getObject());
          }
          PrimeFaces.current().ajax().update("mainform:cnt");
        }
        addFacesMessages(actionObject.getMessages());
      }
    }

    return actionObject;
  }

  protected void processParameters(Map<String, Object> parameters)
  {
    if (parameters != null)
    {
      int newEditTabSelector = 0;
      try
      {
        String tabIndex = (String)parameters.get("tabIndex");
        if (tabIndex != null)
        {
          newEditTabSelector = Integer.parseInt(tabIndex);
        }
        else
        {
          String viewId = (String)parameters.get("viewId");
          String subviewId = (String)parameters.get("subviewId");
          newEditTabSelector = getObjectSetup().findEditTabSelector(viewId, 
            subviewId);
        }
      }
      catch (Exception ex) { }
      editTabSelector = newEditTabSelector;
      editTabRowId = (String)parameters.get("rowId");
    }    
  }  
  
  protected void addFacesMessages(List<Message> messages)
  {
    for (Message message : messages)
    {
      String text = message.getText();
      String[] params = message.getParams();
      Severity severity = message.getSeverity();
      switch(severity)
      {
        case ERROR:
          FacesUtils.addMessage(text, params, FacesMessage.SEVERITY_ERROR);
          break;
        case WARN:
          FacesUtils.addMessage(text, params, FacesMessage.SEVERITY_WARN);
          break;
        default:
          FacesUtils.addMessage(text, params, FacesMessage.SEVERITY_INFO);
      }
    }
    messages.clear();
  }

  protected void setActionResult(ActionObject scriptObject)
  {
  }

  public boolean isDisabledEditTab(EditTab tab)
  {
    return tab.getBeanName() != null && NEW_OBJECT_ID.equals(objectId);
  }

  public boolean isRenderedEditTab(EditTab tab)
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    List<String> readRoles = tab.getReadRoles();
    return readRoles == null || readRoles.isEmpty()
      || userSessionBean.isUserInRole(readRoles);
  }

  public boolean isEditable()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    EditTab editTab = getActiveEditTab();
    List<String> writeRoles = editTab != null ? editTab.getWriteRoles() : null;
    return writeRoles == null || writeRoles.isEmpty()
      || userSessionBean.isUserInRole(writeRoles);
  }

  public void store()
  {
    try
    {
      storeObject();
      storeTabs();

      Object object = getObject();

      getBaseTypeInfo().visit(objectId);

      TypeBean typeBean = getTypeBean();
      if (typeBean != null)
      {
        typeBean.updateDescription(objectId, object);
      }

      loadObjectSetup();

      growl("STORE_OBJECT");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void storeObject() throws Exception
  {
  }

  public void storeTabs() throws Exception
  {
    for (EditTab tab : getEditTabs())
    {
      String beanName = tab.getBeanName();
      if (beanName == null) continue;

      TabBean tabBean = WebUtils.getBean(beanName);
      if (tabBean.isModified())
      {
        System.out.println(">>> storeTabs: store tabBean: " + beanName);
        tabBean.store();
      }
    }
  }
  
  public void copy()
  {
    try
    {
      copyObject();
      growl("COPY_OBJECT");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }  
  
  public void copyObject() throws Exception
  {    
  }
  
  public boolean isRenderCopyButton()
  {
    if (!isNew())
    {
      String typeId = getTypeBean().getTypeId(getObject());
      if (typeId != null)
      {
        Type type = TypeCache.getInstance().getType(typeId);
        PropertyDefinition propdef = type.getPropertyDefinition("copyEnabled");
        if (propdef != null && !propdef.getValue().isEmpty())
        {
          return "true".equals(propdef.getValue().get(0));
        }
      }
    }
    return false;    
  }

  public void remove()
  {
    try
    {
      removeObject();

      NavigatorBean navigatorBean = WebUtils.getBean("navigatorBean");
      navigatorBean.remove();

      growl("REMOVE_OBJECT");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void removeObject() throws Exception
  {
    throw new Exception("NOT_IMPLEMENTED");
  }

  public void cancel()
  {
    setEditTabSelector(0);
    load();
    growl("RELOAD_OBJECT");
  }

  public void selectTabWithErrors()
  {
    ComponentUtils.selectTabWithErrors("mainform:search_tabs:tabs");
  }

  public boolean isMultipleObjectSetup()
  {
    String setupName =
      getSelectedMenuItem().getDirectProperty(OBJECT_SETUP_PROPERTY);
    return setupName == null;
  }
  
  public boolean isRenderProperty(String propName)
  {
    boolean renderProperty = true;
    EditTab tab = getActiveEditTab();
    if (tab != null)
      renderProperty = tab.isRenderProperty(propName);
    
    if (renderProperty)
      renderProperty = isRenderPropertyLegacy(propName); //fallback

    return renderProperty;
  }

  @Override
  public void clear()
  {
    HashSet<TabBean> tabBeans = new HashSet<>();
    for (EditTab tab : getEditTabs())
    {
      String beanName = tab.getBeanName();
      if (beanName == null) continue;

      TabBean tabBean = WebUtils.getBean(beanName);
      tabBeans.add(tabBean);
    }

    for (TabBean tabBean : tabBeans)
    {
      tabBean.clear();
    }
    state.clear();
    actions = null;
  }
  
  @Deprecated
  private boolean isRenderPropertyLegacy(String propName)
  {
    try
    {
      propName = "render" + StringUtils.capitalize(propName);
      Object value = getObjectSetup().getProperties().get(propName);
      if (value == null)
        return true;
      else
        return Boolean.parseBoolean(value.toString());
    }
    catch (Exception ex)
    {
      return false;
    }    
  }  
}
