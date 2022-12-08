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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
  public static final String NEW_OBJECT_ID = "";

  private String lastBaseTypeId;
  private final Map<String, BaseTypeInfo> baseTypeInfoMap = new HashMap<>();
//  private final List<VisitedObject> history;

  public BaseTypeInfo getBaseTypeInfo()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuItemCursor selectedMenuItem = userSessionBean.getSelectedMenuItem();
    String baseTypeId = selectedMenuItem.getProperty(BASE_TYPEID_PROPERTY);
    if (baseTypeId == null) return null;

    BaseTypeInfo baseTypeInfo = getBaseTypeInfo(baseTypeId);
    if (baseTypeInfo.objectBeanName == null)
    {
      baseTypeInfo.objectBeanName =
        selectedMenuItem.getProperty(OBJECT_BEAN_PROPERTY);
    }
    return baseTypeInfo;
  }

  public BaseTypeInfo getBaseTypeInfo(String baseTypeId)
  {
    BaseTypeInfo baseTypeInfo = baseTypeInfoMap.get(baseTypeId);
    if (baseTypeInfo == null)
    {
      baseTypeInfo = new BaseTypeInfo(baseTypeId);
      baseTypeInfoMap.put(baseTypeId, baseTypeInfo);
    }
    return baseTypeInfo;
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
    // STEP-1: go to baseTypeId node
    if (objectTypeId != null)
    {
      MenuItemCursor typeMenuItem = selectMenuItem(objectTypeId);
      if (typeMenuItem.isNull()) return null;
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

            ReturnInfo returnInfo = new ReturnInfo();
            returnInfo.baseTypeId = lastBaseTypeId;
            returnInfo.objectId = lastObjectBean.getObjectId();
            returnInfo.searchTabIndex = lastObjectBean.getSearchTabIndex();
            returnInfo.tabIndex = lastObjectBean.getTabIndex();
            returnInfo.expression = returnExpression;
            baseTypeInfo.setReturnInfo(returnInfo);
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
      objectBean.setSearchTabIndex(1);
    }
    else
    {
      objectBean.setSearchTabIndex(0);
    }
    return objectBean.show();
  }

  public String find(String baseTypeId, String returnExpression)
  {
    return show(baseTypeId, null, 0, returnExpression);
  }

  public boolean isSelectable()
  {
    BaseTypeInfo baseTypeInfo = getBaseTypeInfo();
    return baseTypeInfo != null
      && baseTypeInfo.getReturnInfo() != null
      && !NEW_OBJECT_ID.equals(baseTypeInfo.getObjectId());
  }

  public String select()
  {
    BaseTypeInfo baseTypeInfo = getBaseTypeInfo();
    if (baseTypeInfo == null) return null;

    ReturnInfo returnInfo = baseTypeInfo.getReturnInfo();
    baseTypeInfo.setReturnInfo(null);

    if (returnInfo == null) return null;

    String selectedObjectId = baseTypeInfo.getObjectId();

    MenuItemCursor typeMenuItem = selectMenuItem(returnInfo.baseTypeId);
    if (typeMenuItem.isNull()) return null;

    lastBaseTypeId = returnInfo.baseTypeId;
    baseTypeInfo = getBaseTypeInfo();
    baseTypeInfo.visit(returnInfo.objectId);

    ObjectBean objectBean = baseTypeInfo.getObjectBean();
    objectBean.setObjectId(returnInfo.objectId);
    objectBean.setSearchTabIndex(returnInfo.searchTabIndex);
    objectBean.setTabIndex(returnInfo.tabIndex);

    baseTypeInfo.restoreBeanState(objectBean);
    baseTypeInfo.restoreBeanState(objectBean.getFinderBean());

    objectBean.loadTabs();

    baseTypeInfo.restoreTabBeansState(objectBean.getTabs());
    baseTypeInfo.clearBeansState();

    String expression = returnInfo.expression;
    if (!expression.startsWith("#{")) expression = "#{" + expression + "}";
    WebUtils.setValueExpression(expression, String.class, selectedObjectId);

    System.out.println(">>> select : " +
      returnInfo.expression + " = " + selectedObjectId);

    return objectBean.show();
  }

  public void view(String objectId)
  {
    view(objectId, 0);
  }

  public void view(String objectId, int tabIndex)
  {
    BaseTypeInfo baseTypeInfo = getBaseTypeInfo();
    if (baseTypeInfo == null) return;

    baseTypeInfo.visit(objectId);

    ObjectBean objectBean = baseTypeInfo.getObjectBean();
    objectBean.setObjectId(objectId);
    objectBean.setTabIndex(tabIndex);
    objectBean.load();
  }

  @Override
  public String toString()
  {
    return baseTypeInfoMap.toString();
  }

  private MenuItemCursor selectMenuItem(String objectTypeId)
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuTypesCache menuTypesCache = MenuTypesCache.getInstance();
    MenuItemCursor typeMenuItem = menuTypesCache.get(
      userSessionBean.getSelectedMenuItem(), objectTypeId);

    if (!typeMenuItem.isNull())
    {
      userSessionBean.setSelectedMid(typeMenuItem.getMid());
    }
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

  public class BaseTypeInfo implements Serializable
  {
    String baseTypeId;
    String objectBeanName;
    List<String> recentObjectIds = new ArrayList<>();
    Map<String, Serializable> beanStateMap = new HashMap<>();
    ReturnInfo returnInfo;

    public BaseTypeInfo(String baseTypeId)
    {
      this.baseTypeId = baseTypeId;
    }

    public String getBaseTypeId()
    {
      return baseTypeId;
    }

    public String getObjectBeanName()
    {
      return objectBeanName;
    }

    public ObjectBean getObjectBean()
    {
      return objectBeanName == null ? null : WebUtils.getBean(objectBeanName);
    }

    public String getObjectId()
    {
      if (recentObjectIds.isEmpty()) return NEW_OBJECT_ID;
      return recentObjectIds.get(0);
    }

    public ReturnInfo getReturnInfo()
    {
      return returnInfo;
    }

    public void setReturnInfo(ReturnInfo returnInfo)
    {
      this.returnInfo = returnInfo;
    }

    public void visit(String objectId)
    {
      int index = recentObjectIds.indexOf(objectId);
      if (index != -1)
      {
        recentObjectIds.remove(index);
      }
      recentObjectIds.add(0, objectId);
    }

    public List<String> getRecentObjectIds()
    {
      return recentObjectIds;
    }

    void saveBeanState(BaseBean baseBean)
    {
      Serializable state = baseBean.saveState();
      if (state != null)
      {
        String beanName = WebUtils.getBeanName(baseBean);
        System.out.println(">>> saveBean: " + beanName + "@" + baseTypeId);
        beanStateMap.put(beanName, state);
      }
    }

    void restoreBeanState(BaseBean baseBean)
    {
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
            System.out.println(">>> Restoring bean " + tabBean + "@" + baseTypeId);
            tabBean.restoreState(state);
          }
        }
      }
    }

    void clearBeansState()
    {
      beanStateMap.clear();
    }

    @Override
    public String toString()
    {
      return "RecentList: " + recentObjectIds.toString() +
        " state: " + beanStateMap;
    }
  }

  public class ReturnInfo
  {
    String baseTypeId;
    String objectId;
    int searchTabIndex;
    int tabIndex;
    String expression;
  }

}
