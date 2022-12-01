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
import java.util.Collections;
import java.util.List;
import org.primefaces.event.TabChangeEvent;
import org.santfeliu.faces.beansaver.Savable;
import org.santfeliu.web.WebBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;

/**
 *
 * @author realor
 */
public abstract class ObjectBean extends WebBean implements Serializable
{
  private String objectId = NEW_OBJECT_ID;
  private int tabIndex;
  private String typeId;

  public String getObjectId()
  {
    return objectId;
  }

  public void setObjectId(String objectId)
  {
    this.objectId = objectId;
  }

  public String getTypeId()
  {
    return typeId;
  }

  public void setTypeId(String typeId)
  {
    this.typeId = typeId;
  }

  public int getTabIndex()
  {
    return tabIndex;
  }

  public void setTabIndex(int tabIndex)
  {
    this.tabIndex = tabIndex;
  }

  public void onTabChange(TabChangeEvent event)
  {
    System.out.println(">>> tabIndex: " + tabIndex);
    Tab tab = getTabs().get(tabIndex);
    String beanName = tab.getBackingName();
    if (beanName != null)
    {
      TabBean tabBean = WebUtils.getBacking(beanName);
      tabBean.load();
    }
  }

  public String getDescription()
  {
    return objectId;
  }

  public List<Tab> getTabs()
  {
    // TODO: take tabs from CMSnode
    return Collections.EMPTY_LIST;
  }

  public void show(String objectId)
  {
    System.out.println("ObjectId: " + objectId);
    System.out.println("tabIndex: " + tabIndex);

    setObjectId(objectId);
    Tab tab = getTabs().get(tabIndex);
    String beanName = tab.getBackingName();
    if (beanName != null)
    {
      TabBean tabBean = WebUtils.getBacking(beanName);
      tabBean.load();
    }
  }

  public abstract String show();

  public void store()
  {
    try
    {
      List<Tab> tabs = getTabs();
      for (Tab tab : tabs)
      {
        String backingName = tab.getBackingName();
        Object backing = WebUtils.getBackingIfExists(backingName);
        if (backing instanceof TabBean)
        {
          TabBean tabBean = (TabBean)backing;
          if (tabBean.isModified())
          {
            tabBean.store();
          }
        }
      }
      info("Saved");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void remove()
  {
    error("NOT_IMPLEMENTED");
  }

  public void clear()
  {
    List<Tab> tabs = getTabs();
    for (Tab tab : tabs)
    {
      String backingName = tab.getBackingName();
      System.out.println("Clearing " + backingName + ":" +
        WebUtils.clearBacking(backingName));
    }
  }
}
