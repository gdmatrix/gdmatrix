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
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import org.primefaces.component.tabview.TabView;
import org.santfeliu.webapp.NavigatorBean.BaseTypeInfo;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;

/**
 *
 * @author realor
 */
public abstract class ObjectBean extends BaseBean
{
  protected String objectId = NEW_OBJECT_ID;
  protected List<Tab> tabs = Collections.EMPTY_LIST;
  private transient TabView searchTabView;
  private transient TabView detailTabView;

  @PostConstruct
  void create()
  {
    System.out.println("CREATING " + getClass().getSimpleName());

    NavigatorBean navigatorBean = WebUtils.getBean("navigatorBean");
    navigatorBean.construct(this);
  }

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

  public TabView getSearchTabView()
  {
    return searchTabView;
  }

  public void setSearchTabView(TabView tabView)
  {
    this.searchTabView = tabView;
  }

  public int getSearchSelector()
  {
    if (searchTabView == null)
    {
      try
      {
        searchTabView =
          WebUtils.createComponent("org.primefaces.component.TabView");
      }
      catch (Exception ex)
      {
        return 0;
      }
    }
    return searchTabView.getActiveIndex();
  }

  public void setSearchSelector(int selector)
  {
    if (searchTabView == null)
    {
      try
      {
        searchTabView =
          WebUtils.createComponent("org.primefaces.component.TabView");
      }
      catch (Exception ex)
      {
        return;
      }
    }
    searchTabView.setActiveIndex(selector);
  }

  public TabView getDetailTabView()
  {
    return detailTabView;
  }

  public void setDetailTabView(TabView tabView)
  {
    this.detailTabView = tabView;
  }

  public int getDetailSelector()
  {
    if (detailTabView == null)
    {
      try
      {
        detailTabView =
          WebUtils.createComponent("org.primefaces.component.TabView");
      }
      catch (Exception ex)
      {
        return 0;
      }
    }
    return detailTabView.getActiveIndex();
  }

  public void setDetailSelector(int selector)
  {
    if (detailTabView == null)
    {
      try
      {
        detailTabView =
          WebUtils.createComponent("org.primefaces.component.TabView");
      }
      catch (Exception ex)
      {
        return;
      }
    }
    detailTabView.setActiveIndex(selector);
  }

  public int getEditionSelector()
  {
    return 1;
  }

  @Deprecated
  public TabView getTabView()
  {
    return getDetailTabView();
  }

  @Deprecated
  public void setTabView(TabView tabView)
  {
    this.setDetailTabView(tabView);
  }

  @Deprecated
  public int getTabIndex()
  {
    return getDetailSelector();
  }

  @Deprecated
  public void setTabIndex(int selector)
  {
    setDetailSelector(selector);
  }

  @Deprecated
  public int getSearchTabIndex()
  {
    return getSearchSelector();
  }

  @Deprecated
  public void setSearchTabIndex(int selector)
  {
    setSearchSelector(selector);
  }

  @Deprecated
  public int getEditionTabIndex()
  {
    return getEditionSelector();
  }

  public List<Tab> getTabs()
  {
    return tabs;
  }

  public Tab getCurrentTab()
  {
    int selector = getDetailSelector();
    if (selector >= tabs.size()) return null;

    return tabs.get(selector);
  }

  @Deprecated
  public String show()
  {
    return null;
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
      loadTabs();
      loadActiveTab();

      Object object = getObject();
      TypeBean typeBean = getTypeBean();
      if (typeBean != null)
      {
        typeBean.updateDescription(objectId, object);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void loadObject() throws Exception
  {
  }

  public void loadTabs()
  {
  }

  public void loadActiveTab() throws Exception
  {
    Tab tab = getCurrentTab();
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

  public boolean isDisabledTab(Tab tab)
  {
    return tab.getBeanName() != null && NEW_OBJECT_ID.equals(objectId);
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

      info("STORE_OBJECT");
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
    BeanManager beanManager = CDI.current().getBeanManager();
    for (Tab tab : tabs)
    {
      String beanName = tab.getBeanName();
      if (beanName != null)
      {
        Iterator<Bean<?>> iter = beanManager.getBeans(beanName).iterator();
        if (iter.hasNext())
        {
          Bean<?> bean = iter.next();
          Class<? extends Annotation> scope = bean.getScope();
          Context context = beanManager.getContext(scope);
          Object beanInstance = context.get(bean);

          if (beanInstance instanceof TabBean)
          {
            TabBean tabBean = (TabBean)beanInstance;
            if (tabBean.isModified())
            {
              System.out.println(">>> storeTabs: store tabBean: " +
                tabBean.getClass().getName());
              tabBean.store();
            }
          }
        }
      }
    }
  }

  public void remove()
  {
    error("NOT_IMPLEMENTED");
  }

  public void cancel()
  {
    setDetailSelector(0);
    load();
  }

  public void selectTabWithErrors()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    if (context.isValidationFailed())
    {
      UIViewRoot viewRoot = context.getViewRoot();
      Iterator<String> iter = context.getClientIdsWithMessages();
      if (iter.hasNext())
      {
        String id = iter.next();
        UIComponent component = viewRoot.findComponent(id);

        while (component != null && component != viewRoot)
        {
          if (component instanceof org.primefaces.component.tabview.Tab)
          {
            TabView currentTabView = (TabView)component.getParent();
            int index = currentTabView.getChildren().indexOf(component);
            if (index >= 0)
            {
              currentTabView.setActiveIndex(index);
            }
          }
          component = component.getParent();
        }
      }
    }
  }

  private void clear()
  {
    BeanManager beanManager = CDI.current().getBeanManager();
    Context context = beanManager.getContext(ViewScoped.class);

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
          if (!tabBean.isNew())
          {
            System.out.println(">>> Clearing " + beanName);
            tabBean.setObjectId(NEW_OBJECT_ID);
            try
            {
              tabBean.load();
            }
            catch (Exception ex)
            {
              // ignore
            }
          }
        }
      }
    }
  }
}
