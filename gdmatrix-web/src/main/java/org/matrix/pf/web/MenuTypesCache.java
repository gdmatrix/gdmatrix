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

import java.util.HashMap;
import java.util.Map;
import org.matrix.web.WebUtils;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.faces.menu.model.MenuItemCursor;

/**
 * TODO: Refresh system
 * 
 * @author blanquepa
 */
public class MenuTypesCache
{
  private static final String SEPARATOR = "::";
  private static final MenuTypesCache instance = new MenuTypesCache();
  
  private final Map<String, MenuItemCursor> menuItems;
  
  public MenuTypesCache()
  {
    menuItems = new HashMap();
  }
  
  public static MenuTypesCache getInstance()
  {
    return instance;
  }
  
  public MenuItemCursor get(MenuItemCursor currentMenuItem, String typeId)
  {
    MenuItemCursor topWebMenuItem = WebUtils.getTopWebMenuItem(currentMenuItem);
    String key = topWebMenuItem.getMid() + SEPARATOR + typeId;
    MenuItemCursor menuItem = menuItems.get(key);
    if (menuItem == null)
    {
      menuItem = getMenuItem(topWebMenuItem.getFirstChild(), typeId);
      if (!menuItem.isNull())
        put(key, menuItem);
    }    
    return menuItem;
  }
  
  private void put(String typeId, MenuItemCursor menuItem)
  {
    menuItems.put(typeId, menuItem);
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
      mic.getProperty(WebUtils.OBJECT_TYPEID_PROPERTY);
    Type type = TypeCache.getInstance().getType(typeId);
    if (type != null)
      match = type.isDerivedFrom(nodeTypeId);
    else if (typeId != null)
      match = typeId.equals(nodeTypeId); //Allow types out from dictionary
    
    return match;
  }   
}
