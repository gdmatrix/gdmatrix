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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;
import org.matrix.dic.PropertyDefinition;
import org.primefaces.PrimeFaces;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.web.UserPreferences;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.webapp.setup.ObjectSetup;
import org.santfeliu.webapp.setup.ObjectSetupCache;
import org.santfeliu.webapp.util.GlobalMenuTypesFinder;
import org.santfeliu.webapp.util.MenuTypesFinder;

/**
 *
 * @author realor
 */
@Named("navigatorBean")
@SessionScoped
public class NavigatorBean extends WebBean implements Serializable
{
  public static final String OBJECT_BEAN_PROPERTY = "objectBean";
  public static final String BASE_TYPEID_PROPERTY = "objectTypeId";
  public static final String RECENT_LIST_SIZE_PROPERTY = "recentListSize";
  public static final String ICON_PROPERTY = "icon";
  public static final String DEFAULT_SEARCH_TAB_SELECTOR_PROPERTY =
    "defaultSearchTabSelector";
  public static final String FIND_ON_FIRST_LOAD_PROPERTY = "findOnFirstLoad";
  public static final String OBJECTID_PARAMETER = "oid";

  public static final String NEW_OBJECT_ID = "";
  public static final int DEFAULT_RECENT_LIST_SIZE = 5;
  public static final int DEFAULT_HISTORY_SIZE = 10;

  private final BaseTypeInfoMap baseTypeInfoMap = new BaseTypeInfoMap();
  private final History history = new History();
  private String lastBaseTypeId;
  private String currentContextPanel;
  private int updateCount;
  //private Leap inProgressLeap;
  private MenuTypesFinder menuTypesFinder = new GlobalMenuTypesFinder();
  private boolean finderPanelVisible = true;
  private boolean contextPanelVisible = true;

  private static final List<String> DEFAULT_CONTEXT_PANELS =
    Arrays.asList("recents", "history", "favorites");

  public BaseTypeInfo getBaseTypeInfo()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuItemCursor selectedMenuItem = userSessionBean.getSelectedMenuItem();

    String baseTypeId = selectedMenuItem.getProperty(BASE_TYPEID_PROPERTY);
    if (baseTypeId == null) return null;

    if (FacesContext.getCurrentInstance().isPostback()) // POST
    {
      return baseTypeInfoMap.getBaseTypeInfo(baseTypeId);
    }
    else // GET
    {
      return baseTypeInfoMap.getBaseTypeInfo(baseTypeId,
        selectedMenuItem.getMid());
    }
  }

  public BaseTypeInfo getBaseTypeInfo(String baseTypeId)
  {
    return baseTypeInfoMap.getBaseTypeInfo(baseTypeId);
  }

  public List<String> getBaseTypeIdList()
  {
    return baseTypeInfoMap.getBaseTypeIdList();
  }

  public void clearBaseTypeInfos()
  {
    baseTypeInfoMap.clear();
  }

  public MenuTypesFinder getMenuTypesFinder()
  {
    return menuTypesFinder;
  }

  public void setMenuTypesFinder(MenuTypesFinder menuTypesFinder)
  {
    this.menuTypesFinder = menuTypesFinder;
  }

  public List<String> getContextPanels()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    List<String> contextPanels = userSessionBean.getSelectedMenuItem()
      .getMultiValuedProperty("contextPanels");
    if (contextPanels.isEmpty())
    {
      return DEFAULT_CONTEXT_PANELS;
    }
    return contextPanels;
  }

  public int getUpdateCount()
  {
    return updateCount;
  }

  public String getObjectId()
  {
    BaseTypeInfo baseTypeInfo = getBaseTypeInfo();
    return baseTypeInfo == null? NEW_OBJECT_ID : baseTypeInfo.getObjectId();
  }

  public String getJsonPageState()
  {
    String baseTypeId = "";
    String objectId = "";
    String pageUrl = "";
    String pageTitle = getProperty("pageTitle");

    if (pageTitle == null)
    {
      pageTitle = getSelectedMenuItem().getLabel();
    }

    String prefix = getProperty("pageTitlePrefix");
    if (prefix != null)
    {
      pageTitle = prefix + " " + pageTitle;
    }

    BaseTypeInfo baseTypeInfo = getBaseTypeInfo();
    if (baseTypeInfo != null)
    {
      baseTypeId = baseTypeInfo.getBaseTypeId();
      ObjectBean objectBean = baseTypeInfo.getObjectBean();
      if (objectBean != null)
      {
        objectId = objectBean.getObjectId();
        String viewId = objectBean.getTypeBean().getViewId();
        pageUrl = viewId + "?xmid=" + baseTypeInfo.getMid();

        if (!objectBean.isNew())
        {
          pageUrl += "&" + OBJECTID_PARAMETER + "=" + objectBean.getObjectId();
          pageTitle += " > " + objectBean.getDescription();
        }
      }
    }
    JSONObject json = new JSONObject();

    json.put("baseTypeId", baseTypeId);
    json.put("objectId", objectId);
    json.put("title", pageTitle);
    json.put("url", pageUrl);

    return json.toJSONString();
  }

  public String getContent()
  {
    try
    {
      return getBaseTypeInfo().getObjectSetup().getViewId();
    }
    catch (Exception ex)
    {
      return "/pages/obj/empty.xhtml";
    }
  }

  public boolean isFinderPanelVisible()
  {
    return finderPanelVisible;
  }

  public void setFinderPanelVisible(boolean finderPanelVisible)
  {
    this.finderPanelVisible = finderPanelVisible;
  }

  public boolean isContextPanelVisible()
  {
    return contextPanelVisible;
  }

  public void setContextPanelVisible(boolean contextPanelVisible)
  {
    this.contextPanelVisible = contextPanelVisible;
  }

  public String show()
  {
    return show(null, null, 0, null, null);
  }

  public String show(String objectTypeId, String objectId)
  {
    return show(objectTypeId, objectId, 0, null, null);
  }

  public String show(String objectTypeId, String objectId, int editTabSelector)
  {
    return show(objectTypeId, objectId, editTabSelector, null, null);
  }

  public String show(String objectTypeId, String objectId,
    Map<String, Object> parameters)
  {
    BaseTypeInfo baseTypeInfo = objectTypeId == null ?
      getBaseTypeInfo() : getBaseTypeInfo(objectTypeId);

    if (baseTypeInfo == null) return null;

    DirectLeap leap = new DirectLeap(baseTypeInfo.getBaseTypeId(),
      objectId, parameters);

    leap.setSearchTabSelector(objectId == null ?
      baseTypeInfo.getDefaultSearchTabSelector() : -1);

    return execute(leap, true);    
  }

  public String show(String objectTypeId, String objectId,
    int editTabSelector, String selectExpression, String jsAction)
  {
    BaseTypeInfo baseTypeInfo = objectTypeId == null ?
      getBaseTypeInfo() : getBaseTypeInfo(objectTypeId);

    if (baseTypeInfo == null) return null;

    String requestedObjectId = objectId;
    DirectLeap leap = new DirectLeap(baseTypeInfo.getBaseTypeId());
    if (objectId == null)
    {
      HttpServletRequest request = (HttpServletRequest)FacesContext.
        getCurrentInstance().getExternalContext().getRequest();
      if ("GET".equals(request.getMethod()))
      {
        objectId = request.getParameter(OBJECTID_PARAMETER);
        if (objectId == null)
        {
          objectId = NavigatorBean.NEW_OBJECT_ID;
        }
        else
        {
          requestedObjectId = objectId;
        }
        leap.setParameters(new HashMap());
        for (String paramName : request.getParameterMap().keySet())
        {
          leap.getParameters().put(paramName, request.getParameter(paramName));
        }   
      }
      else
      {
        objectId = baseTypeInfo.getObjectId();
      }
    }

    leap.setObjectId(objectId);
    if (selectExpression != null)
    {
      int searchTabSelector = baseTypeInfo.getDefaultSearchTabSelector();
      if (searchTabSelector == -1) searchTabSelector = 0;
      leap.setSearchTabSelector(searchTabSelector);
    }
    else
    {
      leap.setSearchTabSelector(requestedObjectId == null ?
        baseTypeInfo.getDefaultSearchTabSelector() : -1);
    }
    leap.setEditTabSelector(editTabSelector);

    return execute(leap, true, selectExpression, jsAction);
  }

  public String find(String baseTypeId, String selectExpression)
  {
    return show(baseTypeId, null, 0, selectExpression, null);
  }

  public String find(String baseTypeId, String selectExpression, String jsAction)
  {
    return show(baseTypeId, null, 0, selectExpression, jsAction);
  }

  public String select()
  {
    return select(null);
  }

  public String select(String objectId)
  {
    BaseTypeInfo baseTypeInfo = getBaseTypeInfo();
    if (baseTypeInfo == null) return null;

    SelectLeap selectLeap = baseTypeInfo.selectLeap;

    if (selectLeap == null) return null;

    baseTypeInfo.selectLeap = null;

    if (objectId == null) objectId = baseTypeInfo.getObjectId();

    selectLeap.setSelectedObjectId(objectId);
    selectLeap.setSelectedTypeId(baseTypeInfo.getBaseTypeId());

    return execute(selectLeap);
  }

  public boolean isSelectionPending()
  {
    BaseTypeInfo baseTypeInfo = getBaseTypeInfo();
    return baseTypeInfo != null && baseTypeInfo.selectLeap != null;
  }

  public boolean isSelectable()
  {
    BaseTypeInfo baseTypeInfo = getBaseTypeInfo();
    return baseTypeInfo != null && baseTypeInfo.selectLeap != null
      && !NEW_OBJECT_ID.equals(baseTypeInfo.getObjectId());
  }

  public void view(String objectId)
  {
    view(objectId, 0, false);
  }

  public void view(String objectId, Map<String, Object> parameters)
  {
    view(objectId, 0, false, parameters);
  }
  
  public void view(String objectId, int editTabSelector, boolean resetPosition)
  {
    view(objectId, editTabSelector, resetPosition, null);
  }
  
  public void view(String objectId, int editTabSelector, boolean resetPosition, 
    Map<String, Object> parameters)
  {
    BaseTypeInfo baseTypeInfo = getBaseTypeInfo();
    if (baseTypeInfo == null) return;

    String baseTypeId = baseTypeInfo.getBaseTypeId();

    ObjectBean objectBean = baseTypeInfo.getObjectBean();

    // save previous object in history
    DirectLeap historyLeap = new DirectLeap(baseTypeId);
    historyLeap.setup(objectBean);
    history.push(historyLeap);

    objectBean.setObjectId(objectId);
    objectBean.setSearchTabSelector(objectBean.getEditModeSelector());
    objectBean.setEditTabSelector(editTabSelector);
    objectBean.load(parameters);

    if (resetPosition)
    {
      objectBean.getFinderBean().setObjectPosition(-1);
    }

    baseTypeInfo.visit(objectId);
    history.remove(baseTypeId, objectId);
  }

  public void remove()
  {
    BaseTypeInfo baseTypeInfo = getBaseTypeInfo();
    if (baseTypeInfo == null) return;

    baseTypeInfo.remove();
    updateCount++;

    ObjectBean objectBean = baseTypeInfo.getObjectBean();
    objectBean.setObjectId(NEW_OBJECT_ID);
    objectBean.load();
  }

  public void remove(String objectTypeId, String objectId)
  {
    if (objectTypeId != null && objectId != null)
    {
      BaseTypeInfo baseTypeInfo = getBaseTypeInfo(objectTypeId);
      if (baseTypeInfo == null) return;

      baseTypeInfo.remove(objectId);
      updateCount++;

      ObjectBean objectBean = baseTypeInfo.getObjectBean();
      if (objectBean.getObjectId().equals(objectId))
      {
        objectBean.setObjectId(NEW_OBJECT_ID);
        objectBean.load();
      }
    }
  }

  public String close()
  {
    if (history.isEmpty())
    {
      BaseTypeInfo baseTypeInfo = getBaseTypeInfo();
      if (baseTypeInfo == null) return null;
      DirectLeap leap = new DirectLeap(baseTypeInfo.getBaseTypeId());
      leap.setSearchTabSelector(-1); // edit tab
      return execute(leap, false);
    }
    else
    {
      DirectLeap leap = history.pop();
      return execute(leap, false);
    }
  }

  public String execute(Leap leap)
  {
    return execute(leap, true, null, null);
  }

  public String execute(Leap leap, boolean saveHistory)
  {
    return execute(leap, saveHistory, null, null);
  }

  public String execute(Leap leap, boolean saveHistory,
    String selectExpression)
  {
    return execute(leap, saveHistory, selectExpression, null);
  }

  /**
   * Executes the given leap.
   *
   * @param leap the leap to execute.
   * @param saveHistory saves previous object in history.
   * @param selectExpression the expression to select a leap destination
   *   objectId.
   * @param jsAction the javascript action to execute when selecting an object.
   * @return the target outcome.
  */
  public String execute(Leap leap, boolean saveHistory,
    String selectExpression, String jsAction)
  {
    SelectLeap selectLeap = null;

    // save previous beans state
    if (lastBaseTypeId != null && WebUtils.isPostback())
    {
      BaseTypeInfo lastBaseTypeInfo = getBaseTypeInfo(lastBaseTypeId);
      if (lastBaseTypeInfo != null)
      {
        ObjectBean lastObjectBean = lastBaseTypeInfo.getObjectBean();
        if (lastObjectBean == null) return null;

        if (saveHistory)
        {
          // save previous object in history
          DirectLeap historyLeap = new DirectLeap(lastBaseTypeId);
          historyLeap.setup(lastObjectBean);
          history.push(historyLeap);
        }

        FinderBean finderBean = lastObjectBean.getFinderBean();
        lastBaseTypeInfo.saveBeanState(finderBean);

        if (selectExpression != null)
        {
          lastBaseTypeInfo.saveBeanState(lastObjectBean);
          lastBaseTypeInfo.saveTabBeansState(lastObjectBean.getEditTabs());

          selectLeap =
            new SelectLeap(lastBaseTypeInfo.getBaseTypeId(),
              selectExpression, jsAction);
          selectLeap.setup(lastObjectBean);
        }
      }
    }

    // change to destination node
    String baseTypeId = leap.getBaseTypeId();

    BaseTypeInfo baseTypeInfo = getBaseTypeInfo(baseTypeId);
    if (baseTypeInfo == null) return null;

    baseTypeInfo.selectLeap = selectLeap;

    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    userSessionBean.getMenuModel().setSelectedMid(baseTypeInfo.getMid());

    ObjectBean objectBean = baseTypeInfo.getObjectBean();
    if (objectBean != null)
    {   
      leap.construct(objectBean);
      
      baseTypeInfo.visit(objectBean.getObjectId());
      lastBaseTypeId = baseTypeInfo.getBaseTypeId();
      history.remove(baseTypeInfo.getBaseTypeId(), objectBean.getObjectId());
    }
    String template = userSessionBean.getTemplate();
    return "/templates/" + template + "/template.xhtml";
  }

  public History getHistory()
  {
    return history;
  }

  public int getContextTabSelector()
  {
    List<String> contextPanels = getContextPanels();
    if (contextPanels.isEmpty()) return 0;

    if (currentContextPanel == null)
    {
      currentContextPanel = contextPanels.get(0);
    }

    return contextPanels.indexOf(currentContextPanel);
  }

  public void setContextTabSelector(int selector)
  {
    List<String> contextPanels = getContextPanels();
    if (selector < 0 || selector >= contextPanels.size()) return;

    currentContextPanel = contextPanels.get(selector);
  }


  /**
   * BaseTypeInfo contains information about a base type.
   */
  public class BaseTypeInfo implements Serializable
  {
    String mid;
    List<String> recentObjectIdList = new ArrayList<>();
    Map<String, Serializable> beanStateMap = new HashMap<>();
    SelectLeap selectLeap;
    boolean featured = true;

    public BaseTypeInfo(String mid)
    {
      this.mid = mid;
    }

    public String getMid()
    {
      return mid;
    }

    public String getLabel()
    {
      return getProperty(MenuModel.LABEL);
    }

    public String getBaseTypeId()
    {
      return getProperty(BASE_TYPEID_PROPERTY);
    }

    public String getObjectBeanName()
    {
      return getProperty(OBJECT_BEAN_PROPERTY);
    }
    
    public int getDefaultSearchTabSelector()
    {
      try
      {
        String val = getObjectSetup().getDefaultSearchTabSelector();
        if (val == null)
        {
          val = getProperty(DEFAULT_SEARCH_TAB_SELECTOR_PROPERTY);
          if (val == null)
          {
            Type type = TypeCache.getInstance().getType(getBaseTypeId());
            PropertyDefinition propdef = type.getPropertyDefinition(
              "_" + DEFAULT_SEARCH_TAB_SELECTOR_PROPERTY);
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
        return -1;
      }
    }

    public String getFindOnFirstLoad() //'true', 'false' or 'custom'
    {
      try
      {
        String val = getObjectSetup().getFindOnFirstLoad();
        if (val == null)
        {
          val = getProperty(FIND_ON_FIRST_LOAD_PROPERTY);
          if (val == null)
          {
            Type type = TypeCache.getInstance().getType(getBaseTypeId());
            PropertyDefinition propdef = type.getPropertyDefinition(
              "_" + FIND_ON_FIRST_LOAD_PROPERTY);
            if (propdef != null && !propdef.getValue().isEmpty())
            {
              val = propdef.getValue().get(0);
            }
          }
        }
        return (val != null ? val : "false");
      }
      catch (Exception ex)
      {
        return "false";
      }
    }

    public ObjectBean getObjectBean()
    {
      String objectBeanName = getObjectBeanName();
      return objectBeanName == null ? null : WebUtils.getBean(objectBeanName);
    }

    public int getRecentListSize()
    {
      String value = getProperty(RECENT_LIST_SIZE_PROPERTY);
      try
      {
        if (value != null) return Integer.parseInt(value);
      }
      catch (NumberFormatException ex)
      {
      }
      return DEFAULT_RECENT_LIST_SIZE;
    }

    public String getIcon()
    {
      return getProperty(ICON_PROPERTY);
    }

    public String getObjectId()
    {
      if (recentObjectIdList.isEmpty()) return NEW_OBJECT_ID;
      return recentObjectIdList.get(0);
    }

    public void visit(String objectId)
    {
      int index = recentObjectIdList.indexOf(objectId);
      if (index != -1)
      {
        recentObjectIdList.remove(index);
      }
      recentObjectIdList.add(0, objectId);
      if (recentObjectIdList.size() > getRecentListSize())
      {
        recentObjectIdList.remove(recentObjectIdList.size() - 1);
      }
      updateCount++;
    }

    public boolean isFeatured()
    {
      return featured;
    }

    public void setFeatured(boolean featured)
    {
      this.featured = featured;
    }

    public List<String> getRecentObjectIdList()
    {
      return recentObjectIdList;
    }

    public List<String> getFavoriteObjectIdList()
    {
      try
      {
        String baseTypeId = getBaseTypeId();
        UserPreferences userPreferences =
          UserSessionBean.getCurrentInstance().getUserPreferences();

        if (userPreferences.existsPreference(baseTypeId))
        {
          return userPreferences.getPreferences(baseTypeId);
        }
      }
      catch (Exception ex)
      {
        error(ex);
      }
      return Collections.EMPTY_LIST;
    }

    public boolean isMarkedAsFavorite()
    {
      String objectId = getObjectId();
      if (NEW_OBJECT_ID.equals(objectId)) return false;

      return getFavoriteObjectIdList().contains(objectId);
    }

    public void markAsFavorite()
    {
      String objectId = getObjectId();
      if (NEW_OBJECT_ID.equals(objectId)) return;

      markAsFavorite(objectId);
    }

    public void markAsFavorite(String objectId)
    {
      String baseTypeId = getBaseTypeId();
      UserPreferences userPreferences =
        UserSessionBean.getCurrentInstance().getUserPreferences();
      try
      {
        userPreferences.storePreference(baseTypeId, objectId);
        updateCount++;
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }

    public void remove()
    {
      String objectId = getObjectId();
      if (NEW_OBJECT_ID.equals(objectId)) return;

      remove(objectId);
    }

    public void remove(String objectId)
    {
      unmarkAsFavorite(objectId);
      getRecentObjectIdList().remove(objectId);
      history.remove(getBaseTypeId(), objectId);
    }

    public void unmarkAsFavorite()
    {
      String objectId = getObjectId();
      if (NEW_OBJECT_ID.equals(objectId)) return;

      unmarkAsFavorite(objectId);
    }

    public void unmarkAsFavorite(String objectId)
    {
      String baseTypeId = getBaseTypeId();
      UserPreferences userPreferences =
        UserSessionBean.getCurrentInstance().getUserPreferences();
      try
      {
        userPreferences.removePreference(baseTypeId, objectId);
        updateCount++;
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }

    public String getProperty(String propertyName)
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      MenuItemCursor menuItem = userSessionBean.getMenuModel().getMenuItem(mid);
      return menuItem.getProperty(propertyName);
    }

    public ObjectSetup getObjectSetup() throws Exception
    {
      String setupName = getProperty("objectSetup");
      if (setupName == null)
      {
        Type type = TypeCache.getInstance().getType(getBaseTypeId());
        PropertyDefinition propdef = type.getPropertyDefinition("objectSetup");
        if (propdef != null && !propdef.getValue().isEmpty())
        {
          setupName = propdef.getValue().get(0);
        }
      }
      if (setupName != null)
      {
        return ObjectSetupCache.getConfig(setupName);
      }
      else
      {
        ObjectSetup defaultSetup =
          getObjectBean().getTypeBean().getObjectSetup();
        return defaultSetup;
      }
    }

    void saveBeanState(BaseBean baseBean)
    {
      Serializable state = baseBean.saveState();
      if (state != null)
      {
        String baseTypeId = getBaseTypeId();
        String beanName = WebUtils.getBeanName(baseBean);
        System.out.println(">>> saveBean: " + beanName + "@" + baseTypeId);
        beanStateMap.put(beanName, state);
      }
    }

    void restoreBeanState(BaseBean baseBean)
    {
      String baseTypeId = getBaseTypeId();
      String beanName = WebUtils.getBeanName(baseBean);
      System.out.println(">>> restoreBean: " + beanName + "@" + baseTypeId);
      Serializable state = beanStateMap.get(beanName);
      if (state != null)
      {
        try
        {
          baseBean.restoreState(state);
        }
        catch (Exception ex)
        {
          error(ex);
        }
      }
    }

    void saveTabBeansState(List<EditTab> tabs)
    {
      String baseTypeId = getBaseTypeId();
      BeanManager beanManager = CDI.current().getBeanManager();
      Context context = beanManager.getContext(RequestScoped.class);

      for (EditTab tab : tabs)
      {
        String beanName = tab.getBeanName();
        if (beanName == null) continue;

        Iterator<Bean<?>> iter = beanManager.getBeans(beanName).iterator();
        if (iter.hasNext())
        {
          Bean<?> bean = iter.next();
          Object beanInstance = context.get(bean);

          if (beanInstance instanceof TabBean)
          {
            TabBean tabBean = (TabBean)beanInstance;
            if (!tabBean.isNew())
            {
              Serializable state = tabBean.saveState();
              if (state != null)
              {
                System.out.println(">>> saveBean: " + beanName + "@" + baseTypeId);
                beanStateMap.put(beanName, state);
              }
            }
          }
        }
      }
    }

    void restoreTabBeansState(List<EditTab> tabs, String objectId)
    {
      String baseTypeId = getBaseTypeId();
      if (objectId == null) objectId = getObjectId();

      for (EditTab tab : tabs)
      {
        String beanName = tab.getBeanName();
        if (beanName == null) continue;

        Serializable state = beanStateMap.get(beanName);
        if (state != null)
        {
          Object beanInstance = WebUtils.getBean(beanName);
          if (beanInstance instanceof TabBean)
          {
            TabBean tabBean = (TabBean)beanInstance;
            tabBean.setObjectId(objectId);
            System.out.println(">>> Restoring bean " + beanName + "@" + baseTypeId);
            tabBean.restoreState(state);
          }
        }
      }
    }

    void clearBeansState()
    {
      beanStateMap.clear();
    }
  }

  public class History implements Serializable
  {
    LinkedList<DirectLeap> stack = new LinkedList<>();

    public Collection<DirectLeap> getEntries()
    {
      return stack;
    }

    boolean isEmpty()
    {
      return stack.isEmpty();
    }

    DirectLeap peek()
    {
      return stack.peek();
    }

    void push(DirectLeap leap)
    {
      remove(leap.getBaseTypeId(), leap.getObjectId());
      stack.push(leap);
      if (stack.size() > DEFAULT_HISTORY_SIZE) stack.removeLast();
    }

    DirectLeap pop()
    {
      return stack.isEmpty() ? null : stack.pop();
    }

    DirectLeap remove(String baseTypeId, String objectId)
    {
      Iterator<DirectLeap> iter = stack.iterator();
      while (iter.hasNext())
      {
        DirectLeap next = iter.next();
        if (next.getBaseTypeId().equals(baseTypeId) &&
          next.objectId.equals(objectId))
        {
          iter.remove();
          return next;
        }
      }
      return null;
    }
  }

  public static abstract class Leap implements Serializable
  {
    final String baseTypeId;

    public Leap(String baseTypeId)
    {
      this.baseTypeId = baseTypeId;
    }

    public String getBaseTypeId()
    {
      return baseTypeId;
    }

    public abstract void construct(ObjectBean objectBean);
  }

  public static class DirectLeap extends Leap
  {
    String objectId = NEW_OBJECT_ID;
    int searchTabSelector = 0;
    int editTabSelector = 0;
    Map<String, Object> parameters;

    public DirectLeap(String baseTypeId)
    {
      super(baseTypeId);
    }

    public DirectLeap(String baseTypeId, String objectId)
    {
      super(baseTypeId);
      this.objectId = objectId;
    }

    public DirectLeap(String baseTypeId, String objectId,
      Map<String, Object> parameters)
    {
      super(baseTypeId);
      this.objectId = objectId;
      this.parameters = parameters;
    }

    public void setup(ObjectBean objectBean)
    {
      objectId = objectBean.getObjectId();
      searchTabSelector = objectBean.getSearchTabSelector();
      editTabSelector = objectBean.getEditTabSelector();
    }

    public String getObjectId()
    {
      return objectId;
    }

    public void setObjectId(String objectId)
    {
      this.objectId = objectId;
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

    public Map<String, Object> getParameters()
    {
      return parameters;
    }

    public void setParameters(Map<String, Object> parameters)
    {
      this.parameters = parameters;
    }

    @Override
    public void construct(ObjectBean objectBean)
    {
      objectBean.setObjectId(objectId);
      boolean showObject = (searchTabSelector == -1);
      objectBean.setEditTabSelector(editTabSelector);

      NavigatorBean navigatorBean = WebUtils.getBean("navigatorBean");
      BaseTypeInfo baseTypeInfo = navigatorBean.getBaseTypeInfo(baseTypeId);
      FinderBean finderBean = objectBean.getFinderBean();
      finderBean.clear();
      baseTypeInfo.restoreBeanState(finderBean);

      if (!finderBean.isFinding() && !showObject)
      {
        String findOnFirstLoadValue = baseTypeInfo.getFindOnFirstLoad();
        if ("custom".equals(findOnFirstLoadValue))
        {
          finderBean.putDefaultFilter();
          finderBean.find();
        }
        else if ("true".equals(findOnFirstLoadValue))
        {
          finderBean.smartFind();
        }
      }

      finderBean.setObjectPosition(-1);
      objectBean.load(parameters);

      objectBean.setSearchTabSelector(showObject ?
        objectBean.getEditModeSelector() : searchTabSelector);
    }

    @Override
    public String toString()
    {
      return this.getClass().getSimpleName() +
        ": " + baseTypeId + "@" + objectId;
    }
  }

  public static class SelectLeap extends DirectLeap
  {
    final String selectExpression;
    final String jsAction;
    String selectedObjectId;
    String selectedTypeId;

    public SelectLeap(String baseTypeId, String selectExpression, String jsAction)
    {
      super(baseTypeId);
      this.selectExpression = selectExpression;
      this.jsAction = jsAction;
    }

    public String getSelectedObjectId()
    {
      return selectedObjectId;
    }

    public void setSelectedObjectId(String selectedObjectId)
    {
      this.selectedObjectId = selectedObjectId;
    }

    public void setSelectedTypeId(String selectedTypeId)
    {
      this.selectedTypeId = selectedTypeId;
    }
    
    @Override
    public void construct(ObjectBean objectBean)
    {
      System.out.println(">> construct " + objectBean);

      objectBean.setObjectId(objectId);
      objectBean.setSearchTabSelector(searchTabSelector == -1 ?
        objectBean.getEditModeSelector() : searchTabSelector);
      objectBean.setEditTabSelector(editTabSelector);

      NavigatorBean navigatorBean = WebUtils.getBean("navigatorBean");
      BaseTypeInfo baseTypeInfo = navigatorBean.getBaseTypeInfo(baseTypeId);
      baseTypeInfo.restoreBeanState(objectBean);
      objectBean.getRootTypeId();
      Type selectedType = TypeCache.getInstance().getType(selectedTypeId);
      if (selectedType != null && 
          selectedType.isDerivedFrom(objectBean.getRootTypeId()))
      {
        try
        {
          objectBean.loadObjectSetup();
        }
        catch (Exception ex)
        {
        }
      }
      baseTypeInfo.restoreTabBeansState(objectBean.getEditTabs(), objectId);      
      FinderBean finderBean = objectBean.getFinderBean();
      finderBean.clear();
      baseTypeInfo.restoreBeanState(finderBean);
      finderBean.setObjectPosition(-1);
      baseTypeInfo.clearBeansState();

      System.out.println("selectExpression: " + selectExpression);
      System.out.println("selectedObjectId: " + selectedObjectId);

      WebUtils.setValue(selectExpression, String.class, selectedObjectId);

      if (!StringUtils.isBlank(jsAction))
      {
        PrimeFaces current = PrimeFaces.current();
        current.executeScript(jsAction);
      }
    }
  }

  public class BaseTypeInfoMap extends
    HashMap<String, Map<String, BaseTypeInfo>>
  {

    public BaseTypeInfo getBaseTypeInfo(String baseTypeId)
    {
      return getBaseTypeInfo(baseTypeId, null);
    }

    public BaseTypeInfo getBaseTypeInfo(String baseTypeId, String baseTypeMid)
    {
      String contextKey = getContextKey();

      Map<String, BaseTypeInfo> contextMap = this.get(contextKey);
      if (contextMap == null)
      {
        contextMap = new HashMap<>();
        this.put(contextKey, contextMap);
      }

      BaseTypeInfo baseTypeInfo = contextMap.get(baseTypeId);
      if (baseTypeInfo == null)
      {
        if (baseTypeMid == null)
        {
          baseTypeMid = menuTypesFinder.findTypeMid(baseTypeId);
          if (baseTypeMid == null) return null;
        }

        baseTypeInfo = new BaseTypeInfo(baseTypeMid);
        contextMap.put(baseTypeId, baseTypeInfo);
      }

      return baseTypeInfo;
    }

    public List<String> getBaseTypeIdList()
    {
      String contextKey = getContextKey();

      Map<String, BaseTypeInfo> contextMap = this.get(contextKey);
      if (contextMap != null)
        return new ArrayList<>(contextMap.keySet());
      else
        return Collections.EMPTY_LIST;
    }

    private String getContextKey()
    {
      String topMid = menuTypesFinder.findTopMid();
      String contextKey = topMid + "/" +
        UserSessionBean.getCurrentInstance().getWorkspaceId();
      return contextKey;
    }
  }
}
