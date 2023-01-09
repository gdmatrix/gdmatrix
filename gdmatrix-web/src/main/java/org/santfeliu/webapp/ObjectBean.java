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
import javax.enterprise.context.spi.AlterableContext;
import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
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
  private transient TabView tabView;
  private transient TabView searchTabView;

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

  public TabView getTabView()
  {
    return tabView;
  }

  public void setTabView(TabView tabView)
  {
    this.tabView = tabView;
  }

  public int getTabIndex()
  {
    if (tabView == null)
    {
      tabView = new TabView();
    }
    return tabView.getActiveIndex();
  }

  public void setTabIndex(int tabIndex)
  {
    if (tabView == null)
    {
      tabView = new TabView();
    }
    tabView.setActiveIndex(tabIndex);
  }

  public TabView getSearchTabView()
  {
    return searchTabView;
  }

  public void setSearchTabView(TabView searchTabView)
  {
    this.searchTabView = searchTabView;
  }

  public int getSearchTabIndex()
  {
    if (searchTabView == null)
    {
      searchTabView = new TabView();
    }
    return searchTabView.getActiveIndex();
  }

  public void setSearchTabIndex(int searchTabIndex)
  {
    if (searchTabView == null)
    {
      searchTabView = new TabView();
    }
    searchTabView.setActiveIndex(searchTabIndex);
  }

  public int getEditionTabIndex()
  {
    return 1;
  }

  public List<Tab> getTabs()
  {
    return tabs;
  }

  public Tab getCurrentTab()
  {
    if (tabs.isEmpty()) return null;

    return tabs.get(getTabIndex());
  }

  public abstract String show();

  public Object getObject()
  {
    return null;
  }

  public BaseTypeInfo getBaseTypeInfo()
  {
    NavigatorBean navigatorBean = WebUtils.getBean("navigatorBean");
    return navigatorBean.getBaseTypeInfo();
  }

  public void load()
  {
    try
    {
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
    if (getTabIndex() >= tabs.size()) return;

    Tab tab = tabs.get(getTabIndex());
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
    clear();
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
          if (context instanceof AlterableContext)
          {
            System.out.println(">>> cancel: destroy tabBean " +
              bean.getBeanClass());
            ((AlterableContext)context).destroy(bean);
          }
        }
      }
    }
  }
}
