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

import org.matrix.web.WebUtils;
import java.io.Serializable;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.obj.ObjectDescriptionCache;

/**
 *
 * @author blanquepa
 */
public class PageHistory extends org.santfeliu.web.obj.PageHistory 
  implements Serializable
{   
  @Override
  protected boolean isSamePage(MenuItemCursor cursor1, MenuItemCursor cursor2)
  {
    return cursor1.equals(cursor2);
  }

  @Override
  protected boolean isSameObject(String objectId1, String objectId2)
  {
    if (objectId1 == null) return objectId2 == null;
    return objectId1.equals(objectId2);
  }  

  /* Inner class Entry */
  public class Entry extends org.santfeliu.web.obj.PageHistory.Entry 
    implements Serializable
  {

    public Entry(String mid, String objectId, String typeId)
    {
      super(mid, objectId, typeId);
    }

    @Override
    public String getTitle()
    {
      String title = null;
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      MenuItemCursor cursor = userSessionBean.getMenuModel().getMenuItem(mid);
      ObjectBacking objectBacking = WebUtils.getBacking(cursor);      
      if (objectId == null) // search
      {        
        SearchBacking searchBacking = objectBacking.getSearchBacking();
        if (searchBacking != null)
        {
          title = cursor.getProperty("title"); //TODO
        }
      }

      if (title == null)
      {
        if (objectBacking != null)
        {
          title = objectBacking.getDescription(objectId);
        }
      }
      return title;
    }
    
    @Override
    public String getDescription()
    {
      String description = null;

      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();      
      MenuItemCursor cursor = userSessionBean.getMenuModel().getMenuItem(mid);      
      ObjectBacking objectBacking = WebUtils.getBacking(cursor);  
      
      if (objectBacking != null && objectId != null)
      {
        ObjectDescriptionCache cache = ObjectDescriptionCache.getInstance();
        description = cache.getDescription(objectBacking, objectId);
      }
      return description;
    }

    @Override
    public String show()
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();      
      MenuItemCursor cursor = userSessionBean.getMenuModel().getMenuItem(mid); 
      ObjectBacking objectBacking = WebUtils.getBacking(cursor);      
      if (objectId == null)
      {
        return objectBacking.show();
      }
      else
      {
        String objectTypeId = 
          cursor.getProperty(WebUtils.OBJECT_TYPEID_PROPERTY);
        return ControllerBacking.getCurrentInstance()
          .show(objectTypeId, objectId);
      }
    }

    @Override
    public String getObjectTypeIconPath()
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();      
      MenuItemCursor cursor = userSessionBean.getMenuModel().getMenuItem(mid);
      ObjectBacking objectBacking = WebUtils.getBacking(cursor);  
      if (objectBacking == null) return null;
      String key = objectBacking.getObjectTypeId().toLowerCase();
      return (String)UserSessionBean.getCurrentInstance().getObjectIcons().
        get(key);
    }

    @Override
    public boolean isRenderObjectTypeIcon()
    {
      if (getObjectTypeIconPath() == null) return false;
      try
      {
        MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
          getMenuModel().getMenuItemByMid(mid);
        String value =
          menuItem.getProperty(ObjectBacking.RENDER_OBJECT_TYPE_ICON_PROPERTY);
        return (value != null ? value.equals("true") : false);
      }
      catch (Exception ex)
      {
        return false;
      }
    }
  }  
}
