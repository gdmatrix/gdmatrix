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
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.faces.FacesBean;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author blanquepa
 */
@Named
@RequestScoped
public class TemplateBean extends FacesBean implements Serializable
{
  private static final String TOPWEB_PROPERTY = "topweb";
  private static final String HIGHLIGHTED_PROPERTY = "highlighted";

  private String componentTree;
  private String username;
  private String password;
  private List<MenuItemCursor> highlightedItems;

  public String getUsername()
  {
    return username;
  }

  public void setUsername(String username)
  {
    this.username = username;
  }

  public String getPassword()
  {
    return password;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }

  public List<MenuItemCursor> getHighlightedItems()
  {
    if (highlightedItems == null)
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      MenuItemCursor cursor = userSessionBean.getSelectedMenuItem();
      cursor = cursor.getClone();
      while (!cursor.isRoot() &&
             !"true".equals(cursor.getDirectProperty(TOPWEB_PROPERTY)))
      {
        cursor.moveParent();
      }
      highlightedItems = new ArrayList<>();
      addHighlightedItems(cursor);
    }
    return highlightedItems;
  }

  private void addHighlightedItems(MenuItemCursor cursor)
  {
    if (cursor.hasChildren())
    {
      cursor = cursor.getClone();
      cursor.moveFirstChild();
      while (!cursor.isNull())
      {
        if ("true".equals(cursor.getDirectProperty(HIGHLIGHTED_PROPERTY))
            && cursor.getDirectProperty("icon") != null)
        {
          highlightedItems.add(cursor.getClone());
        }
        addHighlightedItems(cursor);
        cursor.moveNext();
      }
    }
  }

  public String getContent()
  {
    MenuItemCursor cursor =
      UserSessionBean.getCurrentInstance().getSelectedMenuItem();

    String contentExpression = cursor.getProperty("content");
    if (!StringUtils.isBlank(contentExpression) &&
        !"none".equals(contentExpression))
    {
      return WebUtils.evaluateExpression(contentExpression);
    }
    return "/pages/obj/empty.xhtml";
  }

  public void show(String mid)
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    userSessionBean.setSelectedMid(mid);
    userSessionBean.executeSelectedMenuItem();
  }

  public String getUserInitial()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    String displayName = userSessionBean.getDisplayName();
    return displayName.length() > 0 ? displayName.substring(0, 1) : "?";
  }

  public void login()
  {
    try
    {
      if (!StringUtils.isBlank(username))
      {
        UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
        userSessionBean.login(username, password);
        userSessionBean.redirectSelectedMenuItem();
      }
    }
    catch (Exception ex)
    {
      FacesUtils.addMessage("login_messages", ex);
    }
  }

  public void logout()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    userSessionBean.logout(); // redirect
  }

  public void showComponentTree()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    UIViewRoot viewRoot = context.getViewRoot();
    StringBuilder buffer = new StringBuilder();
    printComponent(viewRoot, 0, buffer);
    componentTree = buffer.toString();
  }

  public String getComponentTree()
  {
    return componentTree;
  }

  private void printComponent(UIComponent component, int indent,
    StringBuilder buffer)
  {
    for (int i = 0; i < indent; i++)
    {
      buffer.append("  ");
    }
    buffer.append(component.getClass().getName()).
      append(": ").append(component.getId()).append("\n");

    List<UIComponent> children = component.getChildren();
    for (UIComponent c : children)
    {
      printComponent(c, indent + 2, buffer);
    }
  }

}
