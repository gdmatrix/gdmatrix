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
package org.santfeliu.webapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuElement;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.webapp.util.MatrixMenuItem;
import org.santfeliu.webapp.util.MatrixMenuModel;

/**
 *
 * @author blanquepa
 */
@Named
@RequestScoped
public class TemplateBean implements Serializable
{
  private static final String HIGHLIGHTED_PROPERTY = "highlighted";

  private MatrixMenuModel matrixMenuModel;
  private List<MatrixMenuItem> highlightedItems;

  public MatrixMenuModel getPFMenuModel()
  {
    if (matrixMenuModel == null)
    {
      System.out.println(">>> getPFMenuModel");
      MenuModel menuModel = UserSessionBean.getCurrentInstance().getMenuModel();
      matrixMenuModel = new MatrixMenuModel(menuModel);
    }
    return matrixMenuModel;
  }

  public List<MatrixMenuItem> getHighlightedItems()
  {
    if (highlightedItems == null)
    {
      System.out.println(">>> getHighlightedItems");
      MatrixMenuModel mMenuModel = getPFMenuModel();
      highlightedItems = getHighlightedItems(mMenuModel.getElements());
    }
    return highlightedItems;
  }

  private List<MatrixMenuItem> getHighlightedItems(List<MenuElement> elements)
  {
    List<MatrixMenuItem> menuItems = new ArrayList<>();
    for (MenuElement element : elements)
    {
      if (element instanceof MatrixMenuItem)
      {
        MatrixMenuItem menuItem = (MatrixMenuItem) element;
        if (menuItem.getIcon() != null)
        {
          String highlighted = menuItem.getProperty(HIGHLIGHTED_PROPERTY);
          if (highlighted != null && highlighted.equalsIgnoreCase("true"))
            menuItems.add(menuItem);
        }
      }
      else if (element instanceof DefaultSubMenu)
      {
        List<MatrixMenuItem> highlihted =
          getHighlightedItems(((DefaultSubMenu) element).getElements());
        menuItems.addAll(highlihted);
      }
    }
    return menuItems;
  }

}