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

import org.santfeliu.webapp.util.WebUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import org.santfeliu.webapp.util.MenuTypesCache;
import javax.inject.Named;
import org.santfeliu.faces.ManualContext;
import org.santfeliu.faces.ManualScoped;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.web.UserPreferences;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;

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

  public static final String NEW_OBJECT_ID = "";
  public static final int DEFAULT_RECENT_LIST_SIZE = 5;
  public static final int DEFAULT_HISTORY_SIZE = 10;

  private String lastBaseTypeId;
  private final Map<String, BaseTypeInfo> baseTypeInfoMap = new HashMap<>();
  private final History history = new History();
  private int updateCount;
  private int tabIndex;

  public BaseTypeInfo getBaseTypeInfo()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuItemCursor selectedMenuItem = userSessionBean.getSelectedMenuItem();
    String baseTypeId = selectedMenuItem.getProperty(BASE_TYPEID_PROPERTY);
    if (baseTypeId == null) return null;

    BaseTypeInfo baseTypeInfo = baseTypeInfoMap.get(baseTypeId);
    if (baseTypeInfo == null)
    {
      baseTypeInfo = new BaseTypeInfo(selectedMenuItem.getMid());
      baseTypeInfoMap.put(baseTypeId, baseTypeInfo);
    }
    return baseTypeInfo;
  }

  public BaseTypeInfo getBaseTypeInfo(String baseTypeId)
  {
    BaseTypeInfo baseTypeInfo = baseTypeInfoMap.get(baseTypeId);
    if (baseTypeInfo == null)
    {
      MenuItemCursor typeMenuItem = findMenuItem(baseTypeId);
      if (typeMenuItem.isNull()) return null;

      baseTypeInfo = new BaseTypeInfo(typeMenuItem.getMid());
      baseTypeInfoMap.put(baseTypeId, baseTypeInfo);
    }
    return baseTypeInfo;
  }

  public List<String> getBaseTypeIdList()
  {
    return new ArrayList<>(baseTypeInfoMap.keySet());
  }

  public int getUpdateCount()
  {
    return updateCount;
  }

  public int getTabIndex()
  {
    return tabIndex;
  }

  public void setTabIndex(int tabIndex)
  {
    this.tabIndex = tabIndex;
  }

  public String getObjectId()
  {
    BaseTypeInfo baseTypeInfo = getBaseTypeInfo();
    return baseTypeInfo == null? NEW_OBJECT_ID : baseTypeInfo.getObjectId();
  }

  public String show()
  {
    return show(null, null, 0, null);
  }

  public String show(String objectTypeId, String objectId)
  {
    return show(objectTypeId, objectId, 0, null);
  }

  public String show(String objectTypeId, String objectId, int tabIndex)
  {
    return show(objectTypeId, objectId, tabIndex, null);
  }

  public String show(String objectTypeId, String objectId,
    int tabIndex, String returnExpression)
  {
    System.out.println(">>> SHOW " + objectTypeId + "@" + objectId);

    // STEP-1: go to baseTypeId node
    if (objectTypeId != null)
    {
      MenuItemCursor typeMenuItem = findMenuItem(objectTypeId);
      if (typeMenuItem.isNull()) return null;
      typeMenuItem.select();
    }

    // STEP-2: load current baseTypeInfo
    BaseTypeInfo baseTypeInfo = getBaseTypeInfo();
    if (baseTypeInfo == null) return null;

    // STEP-3: save last beans
    if (lastBaseTypeId != null)
    {
      BaseTypeInfo lastBaseTypeInfo = getBaseTypeInfo(lastBaseTypeId);
      if (lastBaseTypeInfo != null)
      {
        ObjectBean lastObjectBean = lastBaseTypeInfo.getObjectBean();
        if (lastObjectBean != null)
        {
          if (!lastObjectBean.isNew())
          {
            history.push(new ReturnInfo(lastBaseTypeInfo));
          }

          FinderBean lastFinderBean = lastObjectBean.getFinderBean();
          if (returnExpression == null)
          {
            lastBaseTypeInfo.saveBeanState(lastFinderBean);
          }
          else
          {
            lastBaseTypeInfo.saveBeanState(lastObjectBean);
            lastBaseTypeInfo.saveBeanState(lastFinderBean);
            lastBaseTypeInfo.saveTabBeansState(lastObjectBean.getTabs());

            baseTypeInfo.setSelectionInfo(
              new SelectionInfo(lastBaseTypeInfo, returnExpression));
          }
        }
      }
    }

    String baseTypeId = baseTypeInfo.getBaseTypeId();
    lastBaseTypeId = baseTypeId;

    // STEP-4: destroy all BaseBean instances
    destroyBeans();

    // STEP-5: restore finder bean state
    ObjectBean objectBean = baseTypeInfo.getObjectBean();
    if (objectBean == null) return null;
    baseTypeInfo.restoreBeanState(objectBean.getFinderBean());

    // STEP-6: load object data
    if (objectId == null) objectId = baseTypeInfo.getObjectId();
    baseTypeInfo.visit(objectId);
    objectBean.setObjectId(objectId);
    objectBean.setTabIndex(tabIndex);
    objectBean.load();

    if (returnExpression == null)
    {
      objectBean.setSearchTabIndex(objectBean.getEditionTabIndex());
    }
    else
    {
      objectBean.setSearchTabIndex(0);
    }
    history.remove(baseTypeId, objectId);

    return objectBean.show();
  }

  public String find(String baseTypeId, String returnExpression)
  {
    return show(baseTypeId, null, 0, returnExpression);
  }

  public String select()
  {
    BaseTypeInfo baseTypeInfo = getBaseTypeInfo();
    if (baseTypeInfo == null) return null;

    SelectionInfo selectionInfo = baseTypeInfo.getSelectionInfo();
    if (selectionInfo == null) return null;

    baseTypeInfo.setSelectionInfo(null);

    String selectedObjectId = baseTypeInfo.getObjectId();

    MenuItemCursor typeMenuItem = findMenuItem(selectionInfo.baseTypeId);
    if (typeMenuItem.isNull()) return null;
    typeMenuItem.select();

    lastBaseTypeId = selectionInfo.baseTypeId;
    baseTypeInfo = getBaseTypeInfo();
    baseTypeInfo.visit(selectionInfo.objectId);

    ObjectBean objectBean = baseTypeInfo.getObjectBean();
    objectBean.setObjectId(selectionInfo.objectId);
    objectBean.setSearchTabIndex(selectionInfo.searchTabIndex);
    objectBean.setTabIndex(selectionInfo.tabIndex);

    baseTypeInfo.restoreBeanState(objectBean);
    baseTypeInfo.restoreBeanState(objectBean.getFinderBean());

    objectBean.loadTabs();

    baseTypeInfo.restoreTabBeansState(objectBean.getTabs());
    baseTypeInfo.clearBeansState();

    String expression = selectionInfo.expression;
    if (!expression.startsWith("#{")) expression = "#{" + expression + "}";
    WebUtils.setValueExpression(expression, String.class, selectedObjectId);

    System.out.println(">>> select : " +
      selectionInfo.expression + " = " + selectedObjectId);

    return objectBean.show();
  }

  public boolean isSelectable()
  {
    BaseTypeInfo baseTypeInfo = getBaseTypeInfo();
    return baseTypeInfo != null
      && baseTypeInfo.getSelectionInfo() != null
      && !NEW_OBJECT_ID.equals(baseTypeInfo.getObjectId());
  }

  public void view(String objectId)
  {
    view(objectId, 0);
  }

  public void view(String objectId, int tabIndex)
  {
    BaseTypeInfo baseTypeInfo = getBaseTypeInfo();
    if (baseTypeInfo == null) return;

    ObjectBean objectBean = baseTypeInfo.getObjectBean();
    if (!objectBean.isNew())
    {
      history.push(new ReturnInfo(baseTypeInfo));
    }
    baseTypeInfo.visit(objectId);

    objectBean.setObjectId(objectId);
    objectBean.setSearchTabIndex(objectBean.getEditionTabIndex());
    objectBean.setTabIndex(tabIndex);
    objectBean.load();

    history.remove(baseTypeInfo.getBaseTypeId(), objectId);
  }

  public String close()
  {
    lastBaseTypeId = null;

    ReturnInfo returnInfo = history.pop();
    if (returnInfo != null)
    {
      return show(returnInfo.baseTypeId, returnInfo.objectId,
        returnInfo.tabIndex);
    }
    return null;
  }

  public History getHistory()
  {
    return history;
  }

  private MenuItemCursor findMenuItem(String objectTypeId)
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuTypesCache menuTypesCache = MenuTypesCache.getInstance();
    MenuItemCursor typeMenuItem = menuTypesCache.get(
      userSessionBean.getSelectedMenuItem(), objectTypeId);

    return typeMenuItem;
  }

  private void destroyBeans()
  {
    BeanManager beanManager = CDI.current().getBeanManager();
    ManualContext context =
      (ManualContext)beanManager.getContext(ManualScoped.class);

    context.destroy(bean ->
    {
      if (bean instanceof BaseBean)
      {
        System.out.println(">>> Destroying bean " + bean.getClass().getName());
        return true;
      }
      return false;
    });
  }

  /**
   * BaseTypeInfo contains information about a base type.
   */
  public class BaseTypeInfo implements Serializable
  {
    String mid;
    List<String> recentObjectIdList = new ArrayList<>();
    Map<String, Serializable> beanStateMap = new HashMap<>();
    SelectionInfo selectionInfo;
    boolean featured = true;

    public BaseTypeInfo(String mid)
    {
      this.mid = mid;
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

    public SelectionInfo getSelectionInfo()
    {
      return selectionInfo;
    }

    public void setSelectionInfo(SelectionInfo selectionInfo)
    {
      this.selectionInfo = selectionInfo;
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

    public void unmarkAsFavorite()
    {
      String objectId = getObjectId();
      if (NEW_OBJECT_ID.equals(objectId)) return;

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
        baseBean.restoreState(state);
      }
    }

    void saveTabBeansState(List<Tab> tabs)
    {
      String baseTypeId = getBaseTypeId();
      BeanManager beanManager = CDI.current().getBeanManager();
      Context context = beanManager.getContext(ManualScoped.class);

      for (Tab tab : tabs)
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

    void restoreTabBeansState(List<Tab> tabs)
    {
      String baseTypeId = getBaseTypeId();
      String objectId = getObjectId();

      for (Tab tab : tabs)
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
    LinkedList<ReturnInfo> stack = new LinkedList<>();

    public Collection<ReturnInfo> getEntries()
    {
      return stack;
    }

    void push(ReturnInfo returnInfo)
    {
      stack.push(returnInfo);
      if (stack.size() > DEFAULT_HISTORY_SIZE) stack.removeLast();
    }

    ReturnInfo pop()
    {
      return stack.isEmpty() ? null : stack.pop();
    }

    ReturnInfo remove(String baseTypeId, String objectId)
    {
      Iterator<ReturnInfo> iter = stack.iterator();
      while (iter.hasNext())
      {
        ReturnInfo next = iter.next();
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

  /**
   * ReturnInfo holds information to return to a previously visited object.
   */
  public static class ReturnInfo implements Serializable
  {
    String baseTypeId;
    String objectId;
    int searchTabIndex;
    int tabIndex;

    ReturnInfo(BaseTypeInfo baseTypeInfo)
    {
      baseTypeId = baseTypeInfo.getBaseTypeId();
      objectId = baseTypeInfo.getObjectId();
      ObjectBean objectBean = baseTypeInfo.getObjectBean();
      if (objectBean != null)
      {
        searchTabIndex = objectBean.getSearchTabIndex();
        tabIndex = objectBean.getTabIndex();
      }
    }

    public String getBaseTypeId()
    {
      return baseTypeId;
    }

    public String getObjectId()
    {
      return objectId;
    }

    public int getSearchTabIndex()
    {
      return searchTabIndex;
    }

    public int getTabIndex()
    {
      return tabIndex;
    }

    public boolean equivalent(ReturnInfo other)
    {
      if (other == null) return false;
      return this.baseTypeId.equals(other.baseTypeId) &&
        this.objectId.equals(other.objectId);
    }
  }

  /**
   * SelectionInfo holds information to return from previous find operation.
   */
  public static class SelectionInfo extends ReturnInfo
  {
    String expression;

    SelectionInfo(BaseTypeInfo baseTypeInfo, String expression)
    {
      super(baseTypeInfo);
      this.expression = expression;
    }

    public String getExpression()
    {
      return expression;
    }
  }

}
