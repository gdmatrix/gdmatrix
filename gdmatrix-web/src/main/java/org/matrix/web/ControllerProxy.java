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
package org.matrix.web;

import org.matrix.pf.web.ControllerBacking;
import org.matrix.pf.web.ObjectBacking;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.obj.ControllerBean;

/**
 *
 * @author blanquepa
 */
public class ControllerProxy
{
  private final MenuItemCursor menuItem;
  
  public ControllerProxy(MenuItemCursor menuItem)
  {
    this.menuItem = menuItem;
  }
  
  public void visit()
  {
    Object controller = getControllerInstance(menuItem);
    if (controller instanceof ControllerBean)
    {
      ControllerBean controllerBean = (ControllerBean) controller;
      if (controllerBean.getSearchBean(menuItem) != null)
      {
        MenuItemCursor selectedMenuItem = 
          UserSessionBean.getCurrentInstance().getMenuModel()
            .getSelectedMenuItem();
        String oldTypeId = controllerBean.getActualTypeId(selectedMenuItem);
        controllerBean.getPageHistory()
          .visit(menuItem.getMid(), null, oldTypeId);
      }          
    }
    else if (controller instanceof ControllerBacking)
    {
      ControllerBacking controllerBacking = (ControllerBacking) controller;
      ObjectBacking objectBacking = 
        controllerBacking.getObjectBacking(menuItem);

      if (objectBacking != null)
      {
        String currentTypeId = objectBacking.getTypeId();     
        if (currentTypeId != null)
        {
          controllerBacking.getPageHistory()
            .visit(menuItem.getMid(), null, currentTypeId);
        }
      }
    }
  }
  
  private Object getControllerInstance(MenuItemCursor menuItem)
  {
    String action = menuItem.getAction(); 
    if (action != null && action.contains("Backing"))
      return ControllerBacking.getCurrentInstance();
    else
      return ControllerBean.getCurrentInstance();    
  }
}
