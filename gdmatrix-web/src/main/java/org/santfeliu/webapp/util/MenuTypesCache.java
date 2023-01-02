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
package org.santfeliu.webapp.util;

import java.util.HashMap;
import java.util.Map;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.webapp.NavigatorBean;

/**
 *
 * @author blanquepa
 */
public class MenuTypesCache
{
  private static final String SEPARATOR = "::";
  private static final MenuTypesCache instance = new MenuTypesCache();

  private final Map<String, String> mids;

  public MenuTypesCache()
  {
    mids = new HashMap();
  }

  public static MenuTypesCache getInstance()
  {
    return instance;
  }

  public MenuItemCursor get(MenuItemCursor currentMenuItem, String typeId)
  {
    MenuItemCursor menuItem;    

    String key = currentMenuItem.getMid() + SEPARATOR + typeId;
    String typeMid = mids.get(key);
    if (typeMid == null)
      menuItem = findMenuItem(currentMenuItem, key, typeId);
    else
    {
      menuItem = UserSessionBean.getCurrentInstance().getMenuModel()
        .getMenuItem(typeMid);
      if (menuItem.isNull()) //Auto-purge
      {
        mids.remove(key);
        menuItem = findMenuItem(currentMenuItem, key, typeId);       
      }
    }
    
    return menuItem;
  }
  
  private MenuItemCursor findMenuItem(MenuItemCursor currentMenuItem, 
    String key, String typeId)
  {
    MenuItemCursor menuItem;
    MenuItemCursor topWebMenuItem = 
      WebUtils.getTopWebMenuItem(currentMenuItem);
    menuItem = getMenuItem(topWebMenuItem.getFirstChild(), typeId);
    if (!menuItem.isNull())
      mids.put(key, menuItem.getMid());
    return menuItem;
  }

  private MenuItemCursor getMenuItem(MenuItemCursor menuItem, String typeId)
  {
    if (matchTypeId(menuItem, typeId))
        return menuItem;

    MenuItemCursor auxMenuItem = menuItem.getClone();
    if (auxMenuItem.moveFirstChild())
    {
      auxMenuItem = getMenuItem(auxMenuItem, typeId);
      if (!auxMenuItem.isNull())
        return auxMenuItem;
    }

    auxMenuItem = menuItem.getClone();
    if (auxMenuItem.moveNext())
      return getMenuItem(auxMenuItem, typeId);
    else
      return auxMenuItem;
  }

  private boolean matchTypeId(MenuItemCursor mic, String typeId)
  {
    boolean match = false;

    String nodeTypeId =
      mic.getProperty(NavigatorBean.BASE_TYPEID_PROPERTY);
    Type type = TypeCache.getInstance().getType(typeId);
    if (type != null)
      match = type.isDerivedFrom(nodeTypeId);
    else if (typeId != null)
      match = typeId.equals(nodeTypeId); //Allow types out from dictionary

    return match;
  }
}
