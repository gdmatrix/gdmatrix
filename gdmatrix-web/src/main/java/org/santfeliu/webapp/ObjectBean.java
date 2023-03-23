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
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.faces.view.ViewScoped;
import org.matrix.dic.PropertyDefinition;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.webapp.NavigatorBean.BaseTypeInfo;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.setup.ObjectSetup;
import org.santfeliu.webapp.setup.ObjectSetupCache;
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

  public TabBean getTabBean(EditTab editTab)
  {
    return (TabBean)WebUtils.getBean(editTab.getBeanName());
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

  public void loadObjectSetup() throws Exception
  {
    String setupName = getProperty("objectSetup");
    if (setupName == null)
    {
      // look in dictionary
      if (!isNew())
      {
        String typeId = getTypeBean().getTypeId(getObject());
        Type type = TypeCache.getInstance().getType(typeId);
        PropertyDefinition propdef = type.getPropertyDefinition("objectSetup");
        if (propdef != null && !propdef.getValue().isEmpty())
        {
          setupName = propdef.getValue().get(0);
        }
      }
    }

    ObjectSetup defaultSetup = getTypeBean().getObjectSetup();    
    if (setupName != null)
    {
      objectSetup = ObjectSetupCache.getConfig(setupName);
      objectSetup.merge(defaultSetup);
    }
    else
    {
      objectSetup = defaultSetup;
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
    List<String> writeRoles = getActiveEditTab().getWriteRoles();
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
    for (EditTab tab : getEditTabs())
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
    setEditTabSelector(0);
    load();
  }

  public void selectTabWithErrors()
  {
    ComponentUtils.selectTabWithErrors("mainform:search_tabs:tabs");
  }

  private void clear()
  {
    BeanManager beanManager = CDI.current().getBeanManager();
    Context context = beanManager.getContext(ViewScoped.class);

    for (EditTab tab : getEditTabs())
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
