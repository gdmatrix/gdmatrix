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

import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.webapp.NavigatorBean;

/**
 *
 * @author blanquepa
 */
public class MenuTypesFinder
{  
  public MenuItemCursor find(MenuItemCursor currentMenuItem, String typeId)
  {
    MenuItemCursor menuItemCursor = null;
    MenuItemCursor topWebMenuItem = 
      WebUtils.getTopWebMenuItem(currentMenuItem);
    MatchItem foundMenuItem = 
      getMenuItem(topWebMenuItem.getFirstChild(), typeId, null);

    if (foundMenuItem != null)
      menuItemCursor = foundMenuItem.getCursor();

    return menuItemCursor;
  }

  private MatchItem getMenuItem(MenuItemCursor menuItem, String typeId, 
    MatchItem candidate)
  {          
    MatchItem matchItem = candidate;
    if (menuItem.isRendered())
    {
      matchItem = matchTypeId(menuItem, typeId);
      if (matchItem.hasExactMatch()) 
        return matchItem;
      else if (!matchItem.hasMatch()) //Spread the candidate
        matchItem = candidate;
      else if (matchItem.isCandidate() && candidate != null &&
        matchItem.getCandidate() < candidate.getCandidate())
      {
        matchItem = candidate; 
      }
    }

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
    else if (matchItem != null && matchItem.hasExactMatch())
      return matchItem;
    else if (matchItem != null && matchItem.isCandidate())
    {
      if (candidate == null)
        return matchItem;

      return matchItem.getCandidate() > candidate.getCandidate() ? 
        matchItem : candidate;
    }
    else if (candidate != null && candidate.hasCandidateMatch())
      return candidate;
    else 
      return new MatchItem(auxMenuItem);
  }

  private MatchItem matchTypeId(MenuItemCursor mic, String typeId)
  {    
    String nodeTypeId = mic.getProperty(NavigatorBean.BASE_TYPEID_PROPERTY);

    if (typeId.equals(nodeTypeId))
      return new MatchItem(mic);

    Type type = TypeCache.getInstance().getType(typeId);      
    if (type != null && type.isDerivedFrom(nodeTypeId))
    { 
      return new MatchItem(mic.getClone(), 
        type.getTypePath().indexOf("/" + nodeTypeId + "/"));
    }

    return new MatchItem();
  } 

  /**
   * Represents an item that match with the typeId criteria. If candidate is 
   * NO_CANDIDATE is an equals match, else if candidate is positive number 
   * then is a derived type. When higher value, nearer is the parent.
   */
  private class MatchItem
  {
    static final int NO_CANDIDATE = -1;
    MenuItemCursor cursor;
    int candidate = NO_CANDIDATE; //higher value --> nearer parent

    public MatchItem()
    {
      this(null);
    }

    public MatchItem(MenuItemCursor cursor)
    {
      this(cursor, NO_CANDIDATE);
    }

    public MatchItem(MenuItemCursor cursor, int candidate)
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
      return candidate > NO_CANDIDATE;
    }

    public int getCandidate()
    {
      return candidate;
    }

    public void setCandidate(int candidate)
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
