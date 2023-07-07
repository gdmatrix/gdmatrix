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
import java.util.List;
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
  
  public static synchronized void clear(String mid)
  {
    List<String> keys =
    instance.mids.entrySet().stream()
      .filter(e -> e.getValue().getMid().equals(mid))
      .map(Map.Entry::getKey)
      .collect(Collectors.toList());
    keys.stream().forEach(k -> instance.mids.remove(k));
  }
    
  public MenuItemCursor get(MenuItemCursor currentMenuItem, String typeId)
  {
    MenuItemCursor menuItem;
    long now = System.currentTimeMillis();

    String key = currentMenuItem.getMid() + SEPARATOR + typeId;
    MidItem typeMid = mids.get(key);
    MenuItemFinder miFinder = new MenuItemFinder();
    if (typeMid == null || typeMid.timeMillis + PURGE_MILLIS < now)
      menuItem = miFinder.find(currentMenuItem, key, typeId);
    else
    {
      menuItem = UserSessionBean.getCurrentInstance().getMenuModel()
        .getMenuItem(typeMid.mid);
      if (menuItem.isNull()) //Auto-purge
      {
        mids.remove(key);
        menuItem = miFinder.find(currentMenuItem, key, typeId);       
      }
    }
    
    return menuItem;
  }
  
  public void clear()
  {
    this.mids.clear();
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
  
  private class MenuItemFinder
  {
    public MenuItemCursor find(MenuItemCursor currentMenuItem, 
      String key, String typeId)
    {
      MenuItemCursor menuItemCursor = null;
      MenuItemCursor topWebMenuItem = 
        WebUtils.getTopWebMenuItem(currentMenuItem);
      MatchItem foundMenuItem = 
        getMenuItem(topWebMenuItem.getFirstChild(), typeId, null);
      
      if (foundMenuItem != null)
        menuItemCursor = foundMenuItem.getCursor();
      
      if (menuItemCursor != null && !menuItemCursor.isNull())
        mids.put(key, new MidItem(menuItemCursor.getMid()));
      
      return menuItemCursor;
    }

    private MatchItem getMenuItem(MenuItemCursor menuItem, String typeId, 
      MatchItem candidate)
    {      
      if (!menuItem.isRendered())
        return candidate;
      
      MatchItem matchItem = matchTypeId(menuItem, typeId);
      if (matchItem.hasExactMatch()) 
        return matchItem;
      else if (!matchItem.hasMatch()) //Spread the candidate
        matchItem = candidate;

      //First child
      MenuItemCursor auxMenuItem = menuItem.getClone();
      if (auxMenuItem.moveFirstChild())
      {
        matchItem = getMenuItem(auxMenuItem, typeId, matchItem);
        if (matchItem.hasExactMatch())
          return matchItem;
      }

      //Next sibling
      auxMenuItem = menuItem.getClone();
      if (auxMenuItem.moveNext())
        return getMenuItem(auxMenuItem, typeId, matchItem);
      else if (matchItem != null && matchItem.hasMatch())
        return matchItem;
      else if (candidate != null && candidate.hasCandidateMatch())
        return candidate;
      else 
        return new MatchItem(auxMenuItem);
    }

    /**
     * @return 1: match equals, 0: match derived from, -1 not match
     */
    private MatchItem matchTypeId(MenuItemCursor mic, String typeId)
    {    
      String nodeTypeId = mic.getProperty(NavigatorBean.BASE_TYPEID_PROPERTY);

      if (typeId.equals(nodeTypeId))
        return new MatchItem(mic);

      Type type = TypeCache.getInstance().getType(typeId);      
      if (type != null && type.isDerivedFrom(nodeTypeId))
      { 
        return new MatchItem(mic.getClone(), true);
      }

      return new MatchItem();
    } 
    
    /**
     * Represents an item that match with the typeId criteria. If candidate is 
     * false is an equals match, else if candidate is true then is a derived 
     * type.
     */
    private class MatchItem
    {
      MenuItemCursor cursor;
      boolean candidate = false;
      
      public MatchItem()
      {
        this(null);
      }

      public MatchItem(MenuItemCursor cursor)
      {
        this(cursor, false);
      }
      
      public MatchItem(MenuItemCursor cursor, boolean candidate)
      {
        this.cursor = cursor;
        this.candidate = candidate;
      }

      public MenuItemCursor getCursor()
      {
        return cursor;
      }

      public void setCursor(MenuItemCursor cursor)
      {
        this.cursor = cursor;
      }

      public boolean isCandidate()
      {
        return candidate;
      }

      public void setCandidate(boolean candidate)
      {
        this.candidate = candidate;
      }
      
      public boolean hasMatch()
      {
        return cursor != null && !cursor.isNull();
      }

      public boolean hasExactMatch()
      {
        return hasMatch() && !isCandidate();
      }
      
      public boolean hasCandidateMatch()
      {
        return hasMatch() && isCandidate();
      }      
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
  
  public interface IMenuTypesCacheMBean extends CacheMBean
  {
    public void clear(String mid);
  }
  
  public class MenuTypesCacheMBean extends StandardMBean 
    implements IMenuTypesCacheMBean 
  {
    public MenuTypesCacheMBean() throws NotCompliantMBeanException
    {
      super(IMenuTypesCacheMBean.class);
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
    public void clear(String mid)
    {
      MenuTypesCache.clear(mid);
    }    
    
    @Override
    public void update()
    {
    }
        
  }
}
