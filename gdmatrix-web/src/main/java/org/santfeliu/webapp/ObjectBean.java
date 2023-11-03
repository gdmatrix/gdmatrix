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
import java.util.HashSet;
import java.util.List;
import javax.faces.application.FacesMessage;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.PropertyDefinition;
import org.mozilla.javascript.Callable;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.util.script.ScriptClient;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.webapp.NavigatorBean.BaseTypeInfo;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.setup.ObjectSetup;
import org.santfeliu.webapp.setup.ObjectSetupCache;
import org.santfeliu.webapp.setup.Action;
import org.santfeliu.webapp.setup.ActionObject;
import org.santfeliu.webapp.setup.ActionObject.Message;
import org.santfeliu.webapp.setup.ActionObject.Message.Severity;
import org.santfeliu.webapp.setup.ScriptActions;
import org.santfeliu.webapp.setup.SearchTab;
import org.santfeliu.webapp.util.ComponentUtils;

/**
 *
 * @author realor
 */
public abstract class ObjectBean extends BaseBean
{
  protected String objectId = NEW_OBJECT_ID;
  private int searchTabSelector;
  private int editTabSelector;
  private transient ObjectSetup objectSetup;
  private ScriptClient actionsClient;
  private ScriptActions scriptActions;

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

  public int getEditModeSelector()
  {
    return 1;
  }

  public List<SearchTab> getSearchTabs()
  {
    return objectSetup == null ?
      Collections.EMPTY_LIST : objectSetup.getSearchTabs();
  }

  public SearchTab getActiveSearchTab()
  {
    int selector = getSearchTabSelector();
    if (selector >= getSearchTabs().size()) return null;

    return getSearchTabs().get(selector);
  }

  public List<EditTab> getEditTabs()
  {
    return objectSetup == null ?
      Collections.EMPTY_LIST : objectSetup.getEditTabs();
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

  public ObjectSetup getObjectSetup()
  {
    return objectSetup;
  }
  
  public ScriptActions getScriptActions()
  {
    return scriptActions;
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

  public BaseTypeInfo getBaseTypeInfo()
  {
    NavigatorBean navigatorBean = WebUtils.getBean("navigatorBean");
    return navigatorBean.getBaseTypeInfo();
  }

  public void load()
  {
    try
    {
      clear();
      loadObject();
      loadObjectSetup();
      loadActiveEditTab();
      loadActions();

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
    String setupName = getProperty("objectSetup");
    if (setupName == null)
    {
      // look in dictionary
      String typeId = isNew() ? getBaseTypeInfo().getBaseTypeId() :
        getTypeBean().getTypeId(getObject());

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
        }
      }
    }
  }
  
  public void loadActions()
  {
    String actionsScriptName =
      (String) getObjectSetup().getScriptActions().getScriptName();

    if (actionsScriptName != null)
    {
      scriptActions = new ScriptActions(actionsScriptName);
      scriptActions.getActions().addAll(
        getObjectSetup().getScriptActions().getActions());
      try
      {
        actionsClient = new ScriptClient();
        actionsClient.executeScript(actionsScriptName);
        Object callable = actionsClient.get("getActions");
        if (callable instanceof Callable)
        {
          List<Action> actionList = 
            (List<Action>) actionsClient.execute((Callable)callable);
          
          if (actionList != null)
            scriptActions.getActions().addAll(actionList);
        }        
      }                
      catch (Exception ex)
      {
        error(ex);
        actionsClient = null;
      }
    }
    else
      scriptActions = null;
  }

  public void callAction(String actionName)
  {
    Action action = scriptActions.getAction(actionName);
    if (action != null)
      executeAction(action.getName());
  }

  protected ActionObject executeAction(String actionName)
  {
    ActionObject actionObject = new ActionObject(getObject());
    if (actionsClient != null)
    {
      Object callable = actionsClient.get(actionName);
      if (callable instanceof Callable)
      {
        actionsClient.put("actionObject", actionObject);
        actionsClient.execute((Callable)callable);
        actionObject = (ActionObject) actionsClient.get("actionObject");
        if (actionObject != null)
        {
          setActionResult(actionObject);
          if (actionObject.isRefresh())
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
    if (actionsClient != null)
    {
      Object callable = actionsClient.get(actionName);
      if (callable instanceof Callable)
      {
        actionsClient.put("actionObject", actionObject);
        actionsClient.execute((Callable)callable);
        actionObject = (ActionObject) actionsClient.get("actionObject");
        addFacesMessages(actionObject.getMessages());
      }
    }
    return actionObject;
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
    growl("CANCEL_OBJECT");
  }

  public void selectTabWithErrors()
  {
    ComponentUtils.selectTabWithErrors("mainform:search_tabs:tabs");
  }

  public boolean isRenderProperty(String propName)
  {
    propName = "render" + StringUtils.capitalize(propName);
    Object value = objectSetup.getProperties().get(propName);
    if (value == null)
      return true;
    else
      return Boolean.parseBoolean(value.toString());
  }

  private void clear()
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
  }
}
