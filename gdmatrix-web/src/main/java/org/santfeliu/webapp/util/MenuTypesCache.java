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

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import org.apache.commons.collections.map.LRUMap;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.jmx.CacheMBean;
import org.santfeliu.jmx.JMXUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.webapp.NavigatorBean;

/**
 *
 * @author blanquepa
 */
public class MenuTypesCache
{
  private static final String SEPARATOR = "::";
  private static final int MAX_SIZE = 3000;
  private static final long PURGE_MILLIS = 5 * 60 * 1000; // 5 minutes 
  
  private static final MenuTypesCache instance = new MenuTypesCache();

  private final Map<String, MidItem> mids = 
    Collections.synchronizedMap(new LRUMap(MAX_SIZE));

  public static MenuTypesCache getInstance()
  {
    JMXUtils.registerMBean("MenuTypesCache", instance.getCacheMBean());    
    return instance;
  }

  public static void reset()
  {
    JMXUtils.unregisterMBean("MenuTypesCache");
    instance.mids.clear();
  }

  public MenuItemCursor get(MenuItemCursor currentMenuItem, String typeId)
  {
    MenuItemCursor menuItem;
    long now = System.currentTimeMillis();

    String key = currentMenuItem.getMid() + SEPARATOR + typeId;
    MidItem typeMid = mids.get(key);
    if (typeMid == null || typeMid.timeMillis + PURGE_MILLIS < now)
      menuItem = findMenuItem(currentMenuItem, key, typeId);
    else
    {
      menuItem = UserSessionBean.getCurrentInstance().getMenuModel()
        .getMenuItem(typeMid.mid);
      if (menuItem.isNull()) //Auto-purge
      {
        mids.remove(key);
        menuItem = findMenuItem(currentMenuItem, key, typeId);       
      }
    }
    
    return menuItem;
  }
  
  public void clear()
  {
    this.mids.clear();
  }
  
  private MenuItemCursor findMenuItem(MenuItemCursor currentMenuItem, 
    String key, String typeId)
  {
    MenuItemCursor menuItem;
    MenuItemCursor topWebMenuItem = 
      WebUtils.getTopWebMenuItem(currentMenuItem);
    menuItem = getMenuItem(topWebMenuItem.getFirstChild(), typeId);
    if (!menuItem.isNull())
      mids.put(key, new MidItem(menuItem.getMid()));
    return menuItem;
  }
  
  private MenuItemCursor getMenuItem(MenuItemCursor menuItem, String typeId)
  {
    int matchResult = matchTypeId(menuItem, typeId);
    if (matchResult == 1)
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
      return matchResult >= 0 ? menuItem : auxMenuItem;
  }

  /**
   * @return 1: match equals, 0: match derived from, -1 not match
   */
  private int matchTypeId(MenuItemCursor mic, String typeId)
  {    
    String nodeTypeId = mic.getProperty(NavigatorBean.BASE_TYPEID_PROPERTY);

    if (typeId.equals(nodeTypeId))
      return 1;
      
    Type type = TypeCache.getInstance().getType(typeId);      
    if (type.isDerivedFrom(nodeTypeId))
      return 0;
    
    return -1;
  }
    
  private MenuTypesCacheMBean getCacheMBean()
  {
    try
    {
      return new MenuTypesCacheMBean();
    }
    catch (NotCompliantMBeanException ex)
    {
      return null;
    }
  }  
  
  private class MidItem 
  {
    private String mid;
    private long timeMillis;

    private MidItem(String mid)
    {
      this.mid = mid;
      this.timeMillis = System.currentTimeMillis();
    }

    public String getMid()
    {
      return mid;
    }

    public long getTimeMillis()
    {
      return timeMillis;
    }
  }
  
  public class MenuTypesCacheMBean extends StandardMBean implements CacheMBean 
  {
    public MenuTypesCacheMBean() throws NotCompliantMBeanException
    {
      super(CacheMBean.class);
    }    
    
    @Override
    public String getName()
    {
      return "MenuTypesCache";
    }

    @Override
    public long getMaxSize()
    {
      return -1;
    }

    @Override
    public long getSize()
    {
      return mids.size();
    }

    @Override
    public String getDetails()
    {
      return mids.entrySet().stream()
        .map(e -> e.getKey() + ":" + e.getValue().getMid())
        .collect(Collectors.toList())
        .toString();
    }

    @Override
    public void clear()
    {
      MenuTypesCache.this.clear();
    }

    @Override
    public void update()
    {
    }
    
  }
}
