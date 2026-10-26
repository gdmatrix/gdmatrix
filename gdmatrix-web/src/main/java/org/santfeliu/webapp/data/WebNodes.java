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
 * distributed under the licenses is distributed on an "AS IS" BASIS, WITHOUT 
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
package org.santfeliu.webapp.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;

/**
 *
 * @author blanquepa
 */
public class WebNodes
{
  private final MenuModel menuModel;

  public WebNodes(MenuModel menuModel)
  {
    this.menuModel = menuModel;
  }

  public Map<String, Object> getNode(String mid)
  {
    return menuModel.getMenuItem(mid).getProperties();
  }
  
  public Map<String, Object> getParent(String mid)
  {
    return menuModel.getMenuItem(mid).getParent().getProperties();
  }

  public List<Map<String, Object>> getChildren(String mid) throws Exception
  {
    List<Map<String, Object>> children = new ArrayList<>();
    MenuItemCursor cursor = menuModel.getMenuItem(mid);
    if (cursor.getMid() == null)
    {
      return children;
    }

    MenuItemCursor child = cursor.getFirstChild();
    while (!child.isNull())
    {
      Map<String, Object> node = new HashMap<>();
      node.put("mid", child.getMid());
      node.putAll(child.getProperties());       
      children.add(node);
      child.moveNext();
    }

    return children;
  }
}
