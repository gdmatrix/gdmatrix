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
package org.santfeliu.web;

import java.util.Map;
import org.santfeliu.faces.FacesBean;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.web.bean.CMSProperty;

public abstract class WebBean extends FacesBean
{
  @CMSProperty
  public static String FRAME_PROPERTY = "frame";
  @CMSProperty
  public static String TEMPLATE_PROPERTY = "template";
  @CMSProperty
  public static String LANGUAGE_PROPERTY = "language";
  @CMSProperty
  public static String RENDERED_PROPERTY = "rendered";
  @CMSProperty
  public static String ROLES_SELECT_PROPERTY = "roles.select";
  @CMSProperty
  public static String ROLES_UPDATE_PROPERTY = "roles.update";
  @CMSProperty
  public static String ROLES_ACCESS_PROPERTY = "roles.access";
  @CMSProperty
  public static String LABEL_PROPERTY = "label";
  @CMSProperty
  public static String ACTION_PROPERTY = "action";
  @CMSProperty
  public static String THEME_PROPERTY = "theme";
  @CMSProperty
  public static String NODECSS_PROPERTY = "nodeCSS";
  @CMSProperty
  public static String TRANSLATION_ENABLED_PROPERTY = "translationEnabled";
  @CMSProperty
  public static String INHERIT_NODE_CSS_PATH_PROPERTY = "inheritNodeCSSPath";
  @CMSProperty
  public static String TARGET_PROPERTY = "target";
  @CMSProperty
  public static String ENABLED_PROPERTY = "enabled";
  @CMSProperty
  public static String CERTIFICATE_REQUIRED_PROPERTY = "certificateRequired";

  protected String getProperty(String name)
  {
    MenuItemCursor menuItem =
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
    return menuItem.getProperty(name);
  }

  protected Map getProperties()
  {
    MenuItemCursor menuItem =
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
    return menuItem.getProperties();
  }

  protected MenuItemCursor getSelectedMenuItem()
  {
    return UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
  }
}
