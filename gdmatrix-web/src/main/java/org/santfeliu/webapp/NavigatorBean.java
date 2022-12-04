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
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.AlterableContext;
import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.faces.event.PostRenderViewEvent;
import org.santfeliu.webapp.util.MenuTypesCache;
import javax.inject.Named;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
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

  private final Map<String, BaseTypeInfo> baseTypeInfoMap = new HashMap<>();

  public String getBaseTypeId()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuItemCursor selectedMenuItem = userSessionBean.getSelectedMenuItem();
    String baseTypeId = selectedMenuItem.getProperty(BASE_TYPEID_PROPERTY);
    return baseTypeId;
  }

  public BaseTypeInfo getBaseTypeInfo()
  {
    String baseTypeId = getBaseTypeId();
    if (baseTypeId == null) return null;

    return getBaseTypeInfo(baseTypeId);
  }

  public String getObjectId()
  {
    String baseTypeId = getBaseTypeId();
    if (baseTypeId == null) return NEW_OBJECT_ID;

    return getBaseTypeInfo(baseTypeId).getObjectId();
  }

  public String show()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuItemCursor selectedMenuItem = userSessionBean.getSelectedMenuItem();

    String beanName = selectedMenuItem.getProperty(OBJECT_BEAN_PROPERTY);
    if (beanName == null) return "blank";

    Object bean = WebUtils.getBean(beanName);
    if (!(bean instanceof ObjectBean)) return "blank";

    BaseTypeInfo baseTypeInfo = getBaseTypeInfo();

    ObjectBean objectBean = (ObjectBean)bean;
    objectBean.setObjectId(baseTypeInfo.getObjectId());
    objectBean.setSearchTabIndex(1);
    objectBean.setTabIndex(0);
    objectBean.load();

    return objectBean.show();
  }

  public String show(String typeId, String objectId)
  {
    return show(typeId, objectId, 0);
  }

  public String show(String baseTypeId, String objectId, int tabIndex)
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuItemCursor selectedMenuItem = userSessionBean.getSelectedMenuItem();

    MenuTypesCache menuTypesCache = MenuTypesCache.getInstance();
    MenuItemCursor typeMenuItem = menuTypesCache.get(selectedMenuItem, baseTypeId);
    if (typeMenuItem.isNull()) return "blank";

    userSessionBean.setSelectedMid(typeMenuItem.getMid());

    String beanName = typeMenuItem.getProperty(OBJECT_BEAN_PROPERTY);
    if (beanName == null) return "blank";

    Object bean = WebUtils.getBean(beanName);
    if (!(bean instanceof ObjectBean)) return "blank";

    BaseTypeInfo baseTypeInfo = getBaseTypeInfo(baseTypeId);
    baseTypeInfo.visit(objectId);
    baseTypeInfo.setTabIndex(tabIndex);

    ObjectBean objectBean = (ObjectBean)bean;
    objectBean.setObjectId(objectId);
    objectBean.setSearchTabIndex(1);
    objectBean.setTabIndex(tabIndex);
    objectBean.load();
    return objectBean.show();
  }

  public void show(String objectId)
  {
    BaseTypeInfo baseTypeInfo = getBaseTypeInfo();
    if (baseTypeInfo != null)
    {
      baseTypeInfo.visit(objectId);
    }

    ObjectBean objectBean = getObjectBean();
    objectBean.setObjectId(objectId);
    objectBean.load();
  }

  public String search(String baseTypeId)
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuItemCursor selectedMenuItem = userSessionBean.getSelectedMenuItem();

    MenuTypesCache menuTypesCache = MenuTypesCache.getInstance();
    MenuItemCursor typeMenuItem = menuTypesCache.get(selectedMenuItem, baseTypeId);
    if (typeMenuItem.isNull()) return "blank";

    userSessionBean.setSelectedMid(typeMenuItem.getMid());

    String beanName = typeMenuItem.getProperty(OBJECT_BEAN_PROPERTY);
    if (beanName == null) return "blank";

    Object bean = WebUtils.getBean(beanName);
    if (!(bean instanceof ObjectBean)) return "blank";

    ObjectBean objectBean = (ObjectBean)bean;
    objectBean.setSearchTabIndex(0);
    return objectBean.show();
  }

  public String search(String typeId, String returnExpression)
  {
    return null;
  }

  public ObjectBean getObjectBean()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuItemCursor selectedMenuItem = userSessionBean.getSelectedMenuItem();

    String beanName = selectedMenuItem.getProperty(OBJECT_BEAN_PROPERTY);
    if (beanName == null) return null;

    Object bean = WebUtils.getBean(beanName);
    if (bean instanceof ObjectBean) return (ObjectBean) bean;

    return null;
  }

  public class BaseTypeInfo implements Serializable
  {
    String rootTypeId;
    String baseTypeId;
    List<String> recentObjectIds = new ArrayList<>();
    int tabIndex;

    public BaseTypeInfo(String baseTypeId)
    {
      this.baseTypeId = baseTypeId;
      Type type = TypeCache.getInstance().getType(baseTypeId);
      this.rootTypeId = type.getRootType().getTypeId();
    }

    public String getRootTypeId()
    {
      return rootTypeId;
    }

    public String getBaseTypeId()
    {
      return baseTypeId;
    }

    public String getObjectId()
    {
      if (recentObjectIds.isEmpty()) return NEW_OBJECT_ID;
      return recentObjectIds.get(0);
    }

    public int getTabIndex()
    {
      return tabIndex;
    }

    public void setTabIndex(int tabIndex)
    {
      this.tabIndex = tabIndex;
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

    @Override
    public String toString()
    {
      return recentObjectIds.toString();
    }
  }

  @Override
  public String toString()
  {
    return baseTypeInfoMap.toString();
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

  public void postRenderView(PostRenderViewEvent event)
  {
    System.out.println(">>> START POST RENDER VIEW");

    BeanManager beanManager = CDI.current().getBeanManager();
    AlterableContext context =
      (AlterableContext)beanManager.getContext(SessionScoped.class);

    Set<Bean<?>> beans = beanManager.getBeans(Object.class);

    BaseTypeInfo baseTypeInfo = this.getBaseTypeInfo();
    String rootTypeId = baseTypeInfo.getRootTypeId();
    System.out.println("CURRENT rootTypeId: " + rootTypeId);

    for (Bean bean : beans)
    {
      Object beanInstance = context.get(bean);
      if (beanInstance != null)
      {
        System.out.println(">> BEAN " + beanInstance.getClass());
        if (beanInstance instanceof BaseBean)
        {
          BaseBean baseBean = (BaseBean) beanInstance;
          ObjectBean objectBean = baseBean.getObjectBean();
          if (!objectBean.getRootTypeId().equals(rootTypeId))
          {
            System.out.println("  -> Destroying bean of type " +
              objectBean.getRootTypeId());
            context.destroy(bean);
          }
        }
      }
    }
    System.out.println(">>> END POST RENDER VIEW");
  }

  public String getSystemTime()
  {
    return String.valueOf(System.currentTimeMillis());
  }
}
