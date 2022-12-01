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

import org.santfeliu.webapp.util.WebUtils;
import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import org.santfeliu.webapp.util.MenuTypesCache;
import javax.inject.Named;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.webapp.navigator.*;

/**
 *
 * @author realor
 */
@Named("navigatorBean")
@SessionScoped
public class NavigatorBean extends WebBean implements Serializable
{
  public static final String NEW_OBJECT_ID = "";

  private final DescriptionCache descriptionCache = new DescriptionCache();
  private final RecentList recentList = new RecentList();
  private final ReturnStack returnStack = new ReturnStack();

  public String show(String typeId, String objectId)
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuItemCursor selectedMenuItem = userSessionBean.getSelectedMenuItem();

    MenuTypesCache menuTypesCache = MenuTypesCache.getInstance();
    MenuItemCursor typeMenuItem = menuTypesCache.get(selectedMenuItem, typeId);
    if (typeMenuItem.isNull()) return "blank";

    userSessionBean.getMenuModel().setSelectedMid(typeMenuItem.getMid());
    String backingName = typeMenuItem.getProperty("backing");
    if (backingName == null) return "blank";

    Object bean = WebUtils.getBacking(backingName);
    if (!(bean instanceof ObjectBean)) return "blank";

    ObjectBean objectBean = (ObjectBean)bean;
    objectBean.clear();
    objectBean.setObjectId(objectId);
    return objectBean.show();
  }

  public String search(String typeId)
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuItemCursor selectedMenuItem = userSessionBean.getSelectedMenuItem();

    MenuTypesCache menuTypesCache = MenuTypesCache.getInstance();
    MenuItemCursor typeMenuItem = menuTypesCache.get(selectedMenuItem, typeId);
    if (typeMenuItem.isNull()) return "blank";

    String backingName = typeMenuItem.getProperty("backing");
    if (backingName == null) return "blank";

    Object bean = WebUtils.getBacking(backingName);
    if (!(bean instanceof ObjectBean)) return "blank";

    ObjectBean searchBacking = (ObjectBean)bean;
    return searchBacking.show();
  }

  public String search(String typeId, String returnExpression)
  {
    return null;
  }

}
