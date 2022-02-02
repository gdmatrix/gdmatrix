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
package org.matrix.pf.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import org.matrix.pf.cms.CMSContent;
import org.santfeliu.faces.beansaver.Savable;
import org.santfeliu.web.UserPreferences;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.obj.ObjectDescriptionCache;
import org.matrix.web.Describable;

/**
 *
 * @author blanquepa
 */
public abstract class ObjectBacking extends WebBacking
  implements Savable, Describable
{ 
  public static final String NEW_OBJECT_ID = "";
  public static final String RENDER_OBJECT_TYPE_ICON_PROPERTY =
    "renderObjectTypeIcon";
  
  private String objectId;
  private List<Tab> tabs;  
  private Integer tabIndex;  

  public ObjectBacking()
  {
    tabs = new ArrayList();    
    tabIndex = 0; 
  }
  
  @PostConstruct
  public void init()
  {
    loadTabs();
  }
  
  @Override
  public String getObjectTypeId()
  {
    return super.getObjectTypeId();
  }
  
  public String getRootTypeId()
  {
    CMSContent annotation = getClass().getAnnotation(CMSContent.class);
    if (annotation != null)
      return annotation.typeId();
    else
      return null;
  }
    
  public List<Tab> getTabs()
  {
    return tabs;
  }

  public void setTabs(List<Tab> tabs)
  {
    this.tabs = tabs;
  }

  public Integer getTabIndex()
  {
    return tabIndex;
  }

  public void setTabIndex(Integer tabIndex)
  {
    if (tabIndex != null)
      this.tabIndex = tabIndex;
    else
      this.tabIndex = 0;
  }
  
  public Tab getCurrentTab()
  {
    if (this.tabs != null)
      return this.tabs.get(tabIndex);
    else
      return null;
  }
  
  public Tab getTab(String typeId)
  {
    if (typeId == null)
      return null;
    
    if (this.tabs != null)
    {
      for (Tab tab : tabs)
      {
        if (typeId.equals(tab.getTypeId()))
          return tab;
      }
    }
    return null;
  }
  
  public void doTabAction(ActionEvent event)
  {
    Tab tab = (Tab) event.getComponent().getAttributes().get("tab");
    setTabIndex(tab.getIndex());
  }    
  
  public void clearTabs()
  {
    tabs.clear();
  }
  
  public void addTab(Tab tab)
  {
    tabs.add(tab);
  }  
  
  /* TODO: not abstract, load from dictionary */
  public abstract void loadTabs();  
  
  public String getPageTypeId()
  {
    return getCurrentTab().getTypeId();
  }

  public void setObjectId(String objectId)
  {
    this.objectId = objectId;
  }
 
  @Override
  public String getObjectId()
  {
    return objectId;
  }
  
  public abstract String getObjectId(Object obj);
    

  @Override
  public String getDescription(String objectId)
  {
    if (objectId != null && objectId.startsWith("pf::"))
      objectId = objectId.substring(4);    
    return objectId;
  }
  
  public abstract String getDescription(Object obj);
  
  @Override
  public String getDescription()
  {
    return getDescription(getObjectId());
  }
  
  public String show()
  {
//    BackingNamesCache.getInstance().put(getObjectTypeId(), getBackingName());
    return getSearchBacking().show();
  }
  
  public boolean hasCustomHeader()
  {
    return false;
  }
  
  public List<SelectItem> getFavorites()
  {
    return getFavorites(getObjectTypeId());
  }
  
  public List<SelectItem> getFavorites(String objectTypeId)
  {
    //TODO: when 2 descriptions are identical, put objectId before
    List<SelectItem> items = new LinkedList<SelectItem>();
//    items.add(new SelectItem(ControllerBean.NEW_OBJECT_ID, " ")); // blank row
    ObjectDescriptionCache cache = ObjectDescriptionCache.getInstance();
//    for (String historyObjectId : objectHistory)
//    {
//      SelectItem item = new SelectItem();
//      item.setValue(historyObjectId);
//      String description = cache.getDescription(this, historyObjectId);
//      item.setLabel(("".equals(description)) ? " " : description);
//      item.setDescription(description);
//      items.add(item);
//    }
    try
    {
      UserPreferences userPreferences =
        UserSessionBean.getCurrentInstance().getUserPreferences();
      List<String> favoriteIdList =
        userPreferences.getPreferences(objectTypeId);
      if (!favoriteIdList.isEmpty())
      {
        boolean purge = userPreferences.mustPurgePreferences();
        List<SelectItem> favorites = new ArrayList<SelectItem>();
        for (String favoriteObjectId : favoriteIdList)
        {
          String description = 
            cache.getDescription(this, "pf::" + favoriteObjectId);
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
//          SelectItem separator = new SelectItem(ControllerBean.SEPARATOR_ID,
//            "---------------------------");
//          separator.setDisabled(true);
//          items.add(separator);
          items.addAll(favorites);
        }
      }
    }
    catch (Exception ex)
    {
    }
    return items;
  }  
    
  public abstract SearchBacking getSearchBacking();
    
}
