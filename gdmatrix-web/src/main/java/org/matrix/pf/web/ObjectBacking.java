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
import java.util.LinkedList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import org.matrix.pf.cms.CMSContent;
import org.matrix.pf.web.helper.TabPage;
import org.matrix.pf.web.helper.Typed;
import org.matrix.pf.web.helper.TypedHelper;
import org.matrix.web.Describable;
import org.matrix.web.WebUtils;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.faces.beansaver.Savable;
import org.santfeliu.web.UserPreferences;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author blanquepa
 * @param <T>
 */
public abstract class ObjectBacking<T> extends WebBacking
  implements Savable, Describable, Typed
{ 
  public static final String NEW_OBJECT_ID = "";
  public static final String RENDER_OBJECT_TYPE_ICON_PROPERTY =
    "renderObjectTypeIcon";
  public static final String TABS_PROPERTY = "tabs";
  
  protected String objectId;
  protected List<Tab> tabs;  
  protected Integer tabIndex;  
  
  protected TypedHelper typedHelper;
  
  protected ObjectBacking()
  {
    tabs = new ArrayList<>();    
    tabIndex = 0; 
  }
  
  @PostConstruct
  public void init()
  {
    typedHelper = new TypedHelper(this); 
  }

  @Override
  public TypedHelper getTypedHelper()
  {
    return typedHelper;
  }

  @Override
  public String getTypeId()
  {
    return getMenuItemTypeId();
  }
      
  /**
   * Legacy function only used by Describable for compatibility with classes 
   * derived from ObjectBean. It's equivalent to getRootTypeId function.
   * @return 
   */
  @Override
  public String getObjectTypeId()
  {
    return getRootTypeId();
  }
  
  @Override
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
  
  public void loadTabs()
  {
    clearTabs();
    List<String> tabsDef = getMultivaluedProperty(TABS_PROPERTY);
    if (tabsDef != null)
    {    
      for (int i = 0; i < tabsDef.size(); i++)
      {
        String tabDef = tabsDef.get(i);
        String[] parts = tabDef.split("::");
        String label = parts[0];
        String typeId = parts[1];
        String action = parts[2];
        Tab tab = new Tab(i, label, typeId, action);  
        tabs.add(tab);
      }
    }
  }
  
  
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
  
  public abstract String getObjectId(T obj);
      
  public abstract String getDescription(T obj);
  
  @Override
  public String getDescription()
  {
    return getDescription(getObjectId());
  }
  
  @Override
  public abstract String getAdminRole();
  
  public boolean isNew()
  {
    return NEW_OBJECT_ID.equals(getObjectId());
  }  
  
  public String show()
  {
    loadTabs();
    return getSearchBacking().show();
  }
  
  public boolean isEditable()
  {
    return true;
  }  
  
  public boolean hasCustomHeader()
  {
    return false;
  }
  
  public List<SelectItem> getFavorites()
  {
    return getFavorites(getTypeId());
  }
  
  public List<SelectItem> getFavorites(String objectTypeId)
  {
    List<SelectItem> items = new LinkedList<>();

    try
    {
      UserPreferences userPreferences =
        UserSessionBean.getCurrentInstance().getUserPreferences();
      List<String> favoriteIdList =
        userPreferences.getPreferences(objectTypeId);
      if (!favoriteIdList.isEmpty())
      {
        boolean purge = userPreferences.mustPurgePreferences();
        List<SelectItem> favorites = new ArrayList<>();
        for (String favoriteObjectId : favoriteIdList)
        {        
          String description = 
            ObjectDescriptions.getDescription(this, favoriteObjectId);
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
              userPreferences.removePreference(getTypeId(),
                favoriteObjectId);
            }
          }
        }
        if (!favorites.isEmpty())
        {
          Collections.sort(favorites, (Object o1, Object o2) ->
          {
            SelectItem item1 = (SelectItem)o1;
            SelectItem item2 = (SelectItem)o2;
            return item1.getLabel().compareToIgnoreCase(item2.getLabel());
          });
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
  
  public String create()
  {
    setObjectId(NEW_OBJECT_ID);
    for (Tab tab :  getTabs())
    {
      PageBacking pageBacking = 
        WebUtils.getBackingFromAction(tab.getAction());
      if (pageBacking instanceof TabPage)
        ((TabPage)pageBacking).reset();
    }    
    return getSearchBacking().show(NEW_OBJECT_ID);    
  }
  
  public void preRemove()
  {
    //TODO executeTypeAction
  }
  
  public void postRemove()
  {
    //TODO executeTypeAction    
  }
  
  public abstract boolean remove(String objectId);
  
  public String remove()
  {
    String result = null;
    try
    {
      if (!isNew())
      {
        preRemove();
        if (remove(getObjectId()))
          result = getSearchBacking().show();
        postRemove();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    
    return result;
  }  
  
  public String store() 
  {
    String outcome = null;
    try
    {
      //Executes store() of current tab.
      //TODO: Maybe detect if other tabs has been modified
      Tab tab = getCurrentTab();
      PageBacking pageBacking = WebUtils.getBackingFromAction(tab.getAction());
      
      if (pageBacking instanceof TabPage)
        outcome = ((TabPage)pageBacking).store();
      
      if (pageBacking instanceof MainPage)
      {
        String pageObjectId = pageBacking.getPageObjectId();
        ObjectDescriptions.clearDescription(this, pageObjectId);
        getSearchBacking().refresh();       
        return outcome;        
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    
    return outcome;
  }  
  
  public String cancel() 
  {
    try
    {
      //TODO: Maybe detect if other tabs has been modified
      Tab tab = getCurrentTab();
      PageBacking pageBacking = 
        WebUtils.getBackingFromAction(tab.getAction());
      if (pageBacking instanceof TabPage)
        return ((TabPage)pageBacking).cancel();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    
    return null;
  }   

  public List<String> getDerivedTypeIds()
  {
    Type type = getSelectedType();
    return type.getDerivedTypeIds();
  }
  
  public Type getSelectedType()
  {
    return TypeCache.getInstance().getType(getTypeId());
  }  
        
}
