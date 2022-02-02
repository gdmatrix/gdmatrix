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
package org.santfeliu.web.obj;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author realor
 */
public class PageHistory extends ArrayList implements Serializable
{
  protected String currentMid;
  protected String currentObjectId;

  public void visit(String mid, String objectId)
  {
    visit(mid, objectId, null);
  }
  
  public void visit(String mid, String objectId, String currentTypeId)
  {    
    if (currentMid != null)
    {
      // objectId is always null for searchBeans and only null for searchBeans
      // if mid is a tab, objectId can not be null!!!
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      MenuItemCursor cursor1 = userSessionBean.getMenuModel().getMenuItem(mid);
      MenuItemCursor cursor2 = userSessionBean.getMenuModel().getMenuItem(currentMid);
      if ((!isSamePage(cursor1, cursor2) ||
          !isSameObject(objectId, currentObjectId)))
      {
        // page changed, add current page

        // remove it because is going to be current
        removePage(cursor1, objectId);
        // add current page/object to list unless it is new object
        if (!ControllerBean.NEW_OBJECT_ID.equals(currentObjectId))
        {
          // do not add to list new objects
          add(new Entry(currentMid, currentObjectId, currentTypeId));
        }
      }
    }
    currentMid = mid;
    currentObjectId = objectId;
  }

  public void reset()
  {
    currentObjectId = ControllerBean.NEW_OBJECT_ID;
  }

  public String close()
  {
    if (isEmpty())
    {
      currentMid = null;
      currentObjectId = null;
      return "blank";
    }
    else
    {
      Entry entry = (Entry)remove(size() - 1);
      currentMid = entry.getMid();
      currentObjectId = entry.getObjectId();

      ControllerBean controllerBean = ControllerBean.getCurrentInstance();
      if (currentObjectId == null ||
        currentObjectId.equals(ControllerBean.NEW_OBJECT_ID))
      {
        UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
        MenuItemCursor menuItem =
          userSessionBean.getMenuModel().getMenuItem(currentMid);
        PageBean searchBean = controllerBean.getSearchBean(menuItem);
        if (searchBean == null)
        {
          // go last object screen
          return controllerBean.show(currentMid);
        }
        else
        {
          // go search screen
          return controllerBean.search(currentMid);
        }
      }
      else
      {
        // go object screen for currentObjectId
        return controllerBean.show(currentMid, currentObjectId);
      }
    }
  }

  protected void removePage(MenuItemCursor cursor, String objectId)
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    int i = 0;
    boolean found = false;
    while (i < size() && !found)
    {
      Entry entry = (Entry)get(i);
      String entryMid = entry.getMid();
      String entryObjectId = entry.getObjectId();
      MenuItemCursor entryCursor =
        userSessionBean.getMenuModel().getMenuItem(entryMid);
      if (isSamePage(cursor, entryCursor) && 
          isSameObject(objectId, entryObjectId))
      {
        found = true;
      }
      else
      {
        i++;
      }
    }
    if (found) remove(i);
  }

  protected boolean isSamePage(MenuItemCursor cursor1, MenuItemCursor cursor2)
  {
    if (cursor1.equals(cursor2)) return true;
    
    Map properties = (Map)cursor1.getDirectProperties();
    if (properties.containsKey(ControllerBean.OBJECT_BEAN_PROPERTY))
      return false;

    properties = (Map)cursor2.getDirectProperties();
    if (properties.containsKey(ControllerBean.OBJECT_BEAN_PROPERTY))
      return false;

    // both cursors are tabs
    if (cursor1.getParent().equals(cursor2.getParent()))
      return true;

    return false;
  }

  protected boolean isSameObject(String objectId1, String objectId2)
  {
    if (objectId1 == null) return objectId2 == null;
    return objectId1.equals(objectId2);
  }

  /* Inner class Entry */
  public class Entry implements Serializable
  {
    protected String mid;
    protected String objectId;
    protected String typeId;

    public Entry(String mid, String objectId, String typeId)
    {
      this.mid = mid;
      this.objectId = objectId;
      this.typeId = typeId;
    }

    public String getMid()
    {
      return mid;
    }

    public String getObjectId()
    {
      return objectId;
    }

    public String getTypeId()
    {
      return typeId;
    }

    public String getTitle()
    {
      String title = null;
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      MenuItemCursor cursor = userSessionBean.getMenuModel().getMenuItem(mid);
      ControllerBean controllerBean = ControllerBean.getCurrentInstance();
      if (objectId == null) // search
      {
        PageBean searchBean = controllerBean.getSearchBean(cursor);
        if (searchBean != null)
        {
          title = searchBean.getTitle(cursor);
        }
      }

      if (title == null)
      {
        ObjectBean objectBean = controllerBean.getObjectBean(cursor);
        if (objectBean != null)
        {
          title = objectBean.getTitle(cursor, typeId);
        }
      }
      return title;
    }

    public String getDescription()
    {
      String description = null;

      ControllerBean controllerBean = ControllerBean.getCurrentInstance();
      ObjectBean objectBean = controllerBean.getObjectBean(mid);
      if (objectBean != null && objectId != null)
      {
        ObjectDescriptionCache cache = ObjectDescriptionCache.getInstance();
        description = cache.getDescription(objectBean, objectId);
      }
      return description;
    }

    public String show()
    {
      ControllerBean controllerBean = ControllerBean.getCurrentInstance();
      if (objectId == null)
      {
        PageBean searchBean = controllerBean.getSearchBean(mid);
        if (searchBean != null)
          return controllerBean.search(mid);
        else
          return controllerBean.show(mid);
      }
      else
      {
        return controllerBean.show(mid, objectId);
      }
    }

    public String close()
    {
      remove(this);
      return null;
    }

    public String getObjectTypeIconPath()
    {
      ControllerBean controllerBean = ControllerBean.getCurrentInstance();
      ObjectBean objectBean = controllerBean.getObjectBean(mid);
      if (objectBean == null) return null;
      String key = objectBean.getObjectTypeId().toLowerCase();
      return (String)UserSessionBean.getCurrentInstance().getObjectIcons().
        get(key);
    }

    public boolean isRenderObjectTypeIcon()
    {
      if (getObjectTypeIconPath() == null) return false;
      try
      {
        MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
          getMenuModel().getMenuItemByMid(mid);
        String value =
          menuItem.getProperty(ObjectBean.RENDER_OBJECT_TYPE_ICON_PROPERTY);
        return (value != null ? value.equals("true") : false);
      }
      catch (Exception ex)
      {
        return false;
      }
    }
  }
}
