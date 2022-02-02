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
package org.santfeliu.web.obj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.faces.model.SelectItem;
import org.matrix.dic.PropertyDefinition;
import org.matrix.web.Describable;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;

import org.santfeliu.faces.component.HtmlCommandMenu;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.web.UserPreferences;
import org.santfeliu.web.UserSessionBean;


public abstract class ObjectBean extends PageBean implements Describable
{
  public static final String OBJECT_TITLE_PROPERTY = "oc.objectTitle";

  protected static final String OBJECT_LABEL_PROPERTY = "_objectLabel";
  
  protected String objectId;
  private String oldObjectId;  
  protected ObjectHistory objectHistory;
  protected transient HtmlCommandMenu commandMenu;
  protected List<ObjectAction> objectActions;

  static final String RENDER_OBJECT_TYPE_ICON_PROPERTY =
    "oc.renderObjectTypeIcon";
  static final String DISABLE_OBJECT_ACTIONS =
    "oc.disableObjectActions";
  static final String OBJECT_TYPE_ICON_URL =
    "_objectTypeIconUrl";
  
  private boolean renderMainHeading = true;  
  
  public ObjectBean()
  {
    objectId = ControllerBean.NEW_OBJECT_ID;
    objectHistory = new ObjectHistory();
    objectActions = null;
  }

  public abstract String getObjectTypeId();

  public String getActualTypeId()
  {
    return getObjectTypeId();
  }
  
  public Type getSelectedType()
  {
    String typeId = getActualTypeId();
    if (typeId != null)
      return TypeCache.getInstance().getType(typeId);
    else
      return null;
  }

  @Override
  public String getTitle(MenuItemCursor cursor)
  {
    return getTitle(cursor, null);
  }

  public String getTitle(MenuItemCursor cursor, String typeId)
  {
    String title = null;
    if (typeId == null)
    {
      title = getTabLabel(OBJECT_LABEL_PROPERTY);
    }
    else
    {
      title = getTabLabel(OBJECT_LABEL_PROPERTY, typeId);
    }
    
    if (title == null)
    {
      title = cursor.getProperty(OBJECT_TITLE_PROPERTY);
      if (title == null)
      {
        return getObjectTypeId();
      }
    }
    return title;
  }

  protected String getTabLabel(String propertyName)
  {
    return getTabLabel(propertyName, getActualTypeId());
  }

  protected String getTabLabel(String propertyName, String typeId)
  {
    org.santfeliu.dic.Type type = null;
    if (typeId != null)
    {
      TypeCache typeCache = TypeCache.getInstance();
      type = typeCache.getType(typeId);
    }

    if (type != null)
    {
      PropertyDefinition pd = type.getPropertyDefinition(propertyName);
      if (pd != null)
        return pd.getValue().get(0);
      else
        return null;
    }
    else
      return null;
  }
  
  @Override
  public String getObjectId()
  {
    return objectId;
  }
  
  public String getOldObjectId()
  {
    return oldObjectId;
  }

  @Override
  public void setObjectId(String objectId)
  {
    if (objectId == null)
    {
      this.objectId = ControllerBean.NEW_OBJECT_ID;
    }
    else
    {
      this.objectId = objectId;
    }
  }

  public String getDescription()
  {
    return getDescription(this.objectId);
  }

  public String getDescription(String objectId)
  {
    return objectId;
  }
  
  public String getObjectTypeIconPath()
  {
    String typeId = getActualTypeId() != null ? getActualTypeId() : getObjectTypeId();
    Type type = TypeCache.getInstance().getType(typeId);
    if (type != null)
    {
      PropertyDefinition pd = type.getPropertyDefinition(OBJECT_TYPE_ICON_URL);

      if (pd != null && pd.getValue() != null)
        return pd.getValue().get(0);
      else
        return getRootObjectTypeIconPath();
    }
    return getRootObjectTypeIconPath();
  }
  
  public String getRootObjectTypeIconPath()
  {
    return (String)UserSessionBean.getCurrentInstance().getObjectIcons().
        get(getObjectTypeId().toLowerCase());    
  }
  
  public boolean isRenderObjectTypeIcon()
  {
    if (getObjectTypeIconPath() == null) return false;
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(RENDER_OBJECT_TYPE_ICON_PROPERTY);
    return (value != null ? value.equals("true") : false);
  }

  public void setCommandMenu(HtmlCommandMenu commandMenu)
  {
    this.commandMenu = commandMenu;
  }

  public HtmlCommandMenu getCommandMenu()
  {
    return commandMenu;
  }

  public String search()
  {
    return getControllerBean().search();
  }

  public String create()
  {
    return getControllerBean().createObject(getObjectTypeId());
  }

  public String show()
  {
    try
    {
      preShow();
      String outcome = getControllerBean().showObject(getObjectTypeId());
      postShow();
      return outcome;
    }
    catch (Exception ex)
    {
      error(ex);
      return null;
    }
  }

  public String markFavorite()
  {
    return getControllerBean().markObjectAsFavorite(getObjectTypeId(),
      objectId);
  }

  public String unmarkFavorite()
  {
    return getControllerBean().unmarkObjectAsFavorite(getObjectTypeId(),
      objectId);
  }

  public String select()
  {
    return getControllerBean().select();
  }

  public boolean isFavorite()
  {
    return getControllerBean().isObjectFavorite(getObjectTypeId(), objectId);
  }

  @Override
  public boolean isNew()
  {
    return ControllerBean.NEW_OBJECT_ID.equals(getObjectId());
  }
  
  @Override
  public final String store()
  {
    try
    {
      preStore();
      
      ControllerBean controllerBean = getControllerBean();
      FacesContext context = FacesContext.getCurrentInstance();
      
      oldObjectId = objectId;
      
      MenuItemCursor menuItem = getSelectedMenuItem();
      MenuItemCursor headMenuItem = controllerBean.getHeadMenuItem(menuItem);
      menuItem = headMenuItem.getFirstChild();
      
      // store tabs
      PageBean tabBean = null;
      while (!menuItem.isNull())
      {
        String tabBeanName = controllerBean.getBeanName(menuItem);
        if (controllerBean.existsBean(tabBeanName))
        {
          tabBean = (PageBean)getBean(tabBeanName);
          if (tabBean.isModified())
          {
            String outcome = tabBean.store();
            FacesMessage.Severity severity = context.getMaximumSeverity();
            if (severity != null)
            {
              if (FacesMessage.SEVERITY_ERROR.compareTo(severity) <= 0)
              {
                // show error
                getControllerBean().setCurrentMid(menuItem.getMid());
                return outcome;
              }
            }
          }
        }
        menuItem.moveNext();
      }
      postStore();
      
      // update history and description
      objectHistory.removeObject(oldObjectId);
      objectHistory.setObject(objectId);
      // refresh description
      ObjectDescriptionCache.getInstance().clearDescription(this, objectId);
      
      // refresh search bean after store
      PageBean bean = controllerBean.getSearchBean(headMenuItem);
      if (bean instanceof BasicSearchBean)
      {
        if (((BasicSearchBean)bean).getRows() != null)
        {
          ((BasicSearchBean)bean).refresh();
        }
      }
      info("STORE_OBJECT");
      
      return null;
    }
    catch (Exception ex)
    {
      error(ex);
      return null;
    }
  }

  public String cancel()
  {
    try
    {
      preCancel();
      
      MenuItemCursor menuItem = getSelectedMenuItem();
      
      ControllerBean controllerBean = getControllerBean();
      controllerBean.clearBeans(menuItem);
      
      postCancel();
      
      if (!isNew())
      {
        // refresh description
        ObjectDescriptionCache.getInstance().clearDescription(this, objectId);
      }
      info("CANCEL_OBJECT");

      return controllerBean.show();          
    }
    catch (Exception ex)
    {
      error(ex);
      return null;
    }
  }

  public List<ObjectAction> getObjectActions()
  {
    if (isNew()) return Collections.EMPTY_LIST;
    
    if (objectActions != null)
      return objectActions;
    
    objectActions = new ArrayList<ObjectAction>();
    String typeId = getActualTypeId();
    Type type = TypeCache.getInstance().getType(typeId);
    if (type != null)
    {
      List<String> actionNames = type.getActions();
      if (actionNames != null)
      {
        if (actionNames.contains("Print") && !isNew())
        {
          ObjectAction printAction = getPrintAction();
          if (printAction != null)
          {
            objectActions.add(printAction);
          }
        }
        
        if (isActionsScriptEnabled(type))  
        {
          try
          {
            if (typeId != null)
            {
              String action = UserSessionBean.ACTION_SCRIPT_PREFIX + ":" +
                getActionsScriptName(type) + ".getActions";
              List<ObjectAction> actions = 
                (List<ObjectAction>)UserSessionBean.getCurrentInstance().executeScriptAction(action);
              if (actions != null)
                objectActions.addAll(actions);
            }              
          }
          catch (Exception ex)
          {
            error(ex);
          }
        }
      }
    }
    return objectActions;
  }

  // pre, post actions

  public void preStore() throws Exception
  {
    executeTypeAction("preObjectStore");
  };
  
  public void postStore() throws Exception
  {
    executeTypeAction("postObjectStore");
  }; 

  public void preRemove() throws Exception
  {
    executeTypeAction("preObjectRemove");
  };
  
  public void postRemove() throws Exception
  {
    executeTypeAction("postObjectRemove");
  }; 
  
  public void preCancel() throws Exception 
  {
    executeTypeAction("preObjectCancel");
  };
  
  public void postCancel() throws Exception 
  {
    executeTypeAction("postObjectCancel");
  };
  
  public void preShow() throws Exception
  {
    executeTypeAction("preObjectShow");
  }
  
  public void postShow() throws Exception
  {
    executeTypeAction("postObjectShow");
  }  
  
  public boolean isRenderMainHeading() 
  {
    return renderMainHeading;
  }

  public void setRenderMainHeading(boolean renderMainHeading) 
  {
    this.renderMainHeading = renderMainHeading;
  }

  // other actions that may be implemented in derived classes

  public String remove()
  {
    warn("NOT_IMPLEMENTED");
    return null;
  }

  public boolean isEditable()
  {
    return true;
  }

  public boolean isObjectActionsDisabled()
  {
    MenuItemCursor cursor = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String disable = cursor.getProperty(DISABLE_OBJECT_ACTIONS);
    return (disable != null && disable.equalsIgnoreCase("true"));
  }

  @Override
  public boolean isModified()
  {
    // TODO: explore page beans
    return true;
  }
  
  public boolean isTabRendered(MenuItemCursor item)
  {
    if (item != null)
      return item.isRendered();
    else
    {
      PhaseId phaseId = FacesContext.getCurrentInstance().getCurrentPhaseId();
      if (PhaseId.APPLY_REQUEST_VALUES.equals(phaseId))
        return true;
      else
        return false;
    }
  }

  public String showTab() // is provisional
  {
    String tabMid =
      (String)getExternalContext().getRequestParameterMap().get("tabmid");
    
    MenuItemCursor menuItem =
      UserSessionBean.getCurrentInstance().getMenuModel().getMenuItem(tabMid);
    return getControllerBean().show(menuItem.getMid(), objectId);
  }

  public ObjectHistory getObjectHistory()
  {
    return objectHistory;
  }

  public String changeObject()
  {
    MenuItemCursor menuItem = getSelectedMenuItem();
    String nextObjectId = objectId;

    if (ControllerBean.SEPARATOR_ID.equals(nextObjectId))
    {
      nextObjectId = ControllerBean.NEW_OBJECT_ID;
    }
    if (ControllerBean.NEW_OBJECT_ID.equals(nextObjectId))
    {
      return getControllerBean().create();
    }
    getControllerBean().clearBeans(menuItem);
    return getControllerBean().show(menuItem.getMid(), nextObjectId);
  }

  public List<SelectItem> getSelectItems()
  {
    //TODO: when 2 descriptions are identical, put objectId before
    List<SelectItem> items = new LinkedList<SelectItem>();
    items.add(new SelectItem(ControllerBean.NEW_OBJECT_ID, " ")); // blank row
    ObjectDescriptionCache cache = ObjectDescriptionCache.getInstance();
    for (String historyObjectId : objectHistory)
    {
      SelectItem item = new SelectItem();
      item.setValue(historyObjectId);
      String description = cache.getDescription(this, historyObjectId);
      item.setLabel(("".equals(description)) ? " " : description);
      item.setDescription(description);
      items.add(item);
    }
    try
    {
      UserPreferences userPreferences =
        UserSessionBean.getCurrentInstance().getUserPreferences();
      List<String> favoriteIdList =
        userPreferences.getPreferences(getObjectTypeId());
      if (!favoriteIdList.isEmpty())
      {
        boolean purge = userPreferences.mustPurgePreferences();
        List<SelectItem> favorites = new ArrayList<SelectItem>();
        for (String favoriteObjectId : favoriteIdList)
        {
          String description = cache.getDescription(this, favoriteObjectId);
          if (description != null && !description.isEmpty())
          {
            SelectItem item = new SelectItem();
            item.setValue(favoriteObjectId);
            item.setLabel(description);
            item.setDescription(description);
            favorites.add(item);
          }
          else //Non existing favorite
          {
            if (purge)
            {
              userPreferences.removePreference(getObjectTypeId(),
                favoriteObjectId);
            }
          }
        }
        if (!favorites.isEmpty())
        {
          Collections.sort(favorites, new Comparator()
            {
              public int compare(Object o1, Object o2)
              {
                SelectItem item1 = (SelectItem)o1;
                SelectItem item2 = (SelectItem)o2;
                return item1.getLabel().compareToIgnoreCase(item2.getLabel());
              }
            }
          );
          // separator row
          SelectItem separator = new SelectItem(ControllerBean.SEPARATOR_ID,
            "---------------------------");
          separator.setDisabled(true);
          items.add(separator);
          items.addAll(favorites);
        }
      }
    }
    catch (Exception ex)
    {
    }
    return items;
  }

  public List<SelectItem> getSelectItems(String selectedObjectId)
  {
    List<SelectItem> items = getSelectItems();
    if (selectedObjectId != null &&
      !ControllerBean.NEW_OBJECT_ID.equals(selectedObjectId) &&
      !objectHistory.containsObject(selectedObjectId))
    {
      ObjectDescriptionCache cache = ObjectDescriptionCache.getInstance();
      SelectItem selectItem = new SelectItem();
      selectItem.setValue(selectedObjectId);
      String description = cache.getDescription(this, selectedObjectId);
      selectItem.setLabel(("".equals(description)) ? " " : description);
      selectItem.setDescription(description);
      items.add(1, selectItem); // position 0 is blank
    }
    return items;
  }

  public List<SelectItem> getSelectItems(List<String> objectIdList,
    String selectedObjectId)
  {
    List<SelectItem> items = getSelectItems(objectIdList);
    if (selectedObjectId != null &&
      !ControllerBean.NEW_OBJECT_ID.equals(selectedObjectId) &&
      !objectIdList.contains(selectedObjectId))
    {
      ObjectDescriptionCache cache = ObjectDescriptionCache.getInstance();
      SelectItem selectItem = new SelectItem();
      selectItem.setValue(selectedObjectId);
      String description = cache.getDescription(this, selectedObjectId);
      selectItem.setLabel(("".equals(description)) ? " " : description);      
      selectItem.setDescription(description);
      items.add(1, selectItem); // position 0 is blank
    }
    return items;
  }

  public List<SelectItem> getSelectItems(List<String> objectIdList)
  {
    List<SelectItem> items = new LinkedList<SelectItem>();
    items.add(new SelectItem(ControllerBean.NEW_OBJECT_ID, " ")); // blank row
    ObjectDescriptionCache cache = ObjectDescriptionCache.getInstance();
    for (String objId : objectIdList)
    {
      SelectItem item = new SelectItem();
      item.setValue(objId);
      String description = cache.getDescription(this, objId);
      item.setLabel(("".equals(description)) ? " " : description);
      item.setDescription(description);
      items.add(item);
    }

    return items;
  }

  // perform history cleanup, refresh search bean and shows a message
  public void removed()
  {
    clearObject();

    ControllerBean controllerBean = getControllerBean();
    MenuItemCursor menuItem = getSelectedMenuItem();
    MenuItemCursor headMenuItem = controllerBean.getHeadMenuItem(menuItem);

    // refresh search bean after store
    PageBean bean = controllerBean.getSearchBean(headMenuItem);
    if (bean instanceof BasicSearchBean)
    {
      ((BasicSearchBean)bean).refresh();
    }
    info("REMOVE_OBJECT");
  }

  public void clearObject()
  {
    objectHistory.removeObject(objectId);
    ControllerBean controllerBean = getControllerBean();
    objectId = ControllerBean.NEW_OBJECT_ID;
    controllerBean.getPageHistory().reset();
    controllerBean.clearBeans(getSelectedMenuItem());
  }
  
  public void clearObjectActions()
  {
    objectActions = null;
  }

  private ObjectAction getPrintAction()
  {
    String objectBeanName = getObjectBeanName();
    String printReportName = getPrintReportName();
    if (objectBeanName != null && printReportName != null)
    {
      ResourceBundle bundle = ResourceBundle.getBundle(
        "org.santfeliu.web.obj.resources.ObjectBundle", getLocale());
      ObjectAction action = new ObjectAction();
      action.setDescription(bundle.getString("print"));
      action.setUrl("/reports/" + printReportName + ".pdf?objectId=" + 
        getObjectId());
      action.setTarget("_blank");
      return action;
    }
    return null;
  }

  private String getObjectBeanName()
  {
    MenuItemCursor headMenuItem =
      getControllerBean().getHeadMenuItem(getSelectedMenuItem());
    if (!headMenuItem.isNull())
    {
      return (String)headMenuItem.getProperties().get(
        ControllerBean.OBJECT_BEAN_PROPERTY);
    }
    return null;
  }

  private String getPrintReportName()
  {
    Type type = TypeCache.getInstance().getType(getActualTypeId());
    PropertyDefinition propDef = type.getPropertyDefinition("_printReportName");
    if (propDef != null && propDef.getValue().size() > 0)
    {
      return propDef.getValue().get(0);
    }
    return null;
  }
  
}

