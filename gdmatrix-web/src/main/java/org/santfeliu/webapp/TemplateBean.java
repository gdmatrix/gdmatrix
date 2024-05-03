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
import org.santfeliu.web.ApplicationBean;
import org.santfeliu.web.UserPreferences;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author blanquepa
 * @author realor
*/
@Named
@RequestScoped
public class TemplateBean extends FacesBean implements Serializable
{
  private static final String TOPWEB_PROPERTY = "topweb";
  private static final String HIGHLIGHTED_PROPERTY = "highlighted";
  private static final String TOOLBAR_ENABLED = "toolbarEnabled";
  private static final String TOOLBAR_MODE = "toolbarMode";
  private static final String TOOLBAR_MODE_GLOBAL = "global";
  private static final String TOOLBAR_MODE_CONTEXT = "context";
  private static final String CONTEXT_MID = "contextMid";

  private String componentTree;
  private String username;
  private String password;
  private List<MenuItemCursor> highlightedItems;
  private String workspaceId;

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

  public String getWebTitle()
  {
    ApplicationBean applicationBean = ApplicationBean.getCurrentInstance();
    String toolbarMode = getToolbarMode();
    MenuItemCursor cursor;
    if (TOOLBAR_MODE_GLOBAL.equals(toolbarMode))
    {
      cursor = getTopwebMenuItem();
    }
    else
    {
      cursor = getContextMenuItem();
    }
    return applicationBean.translate(cursor.getProperty("description"));
  }

  public List<MenuItemCursor> getHighlightedItems()
  {
    if (isHighlightedItemsReloadRequired())
    {
      workspaceId = UserSessionBean.getCurrentInstance().getWorkspaceId();
      highlightedItems = new ArrayList<>();
      String toolbarMode = getToolbarMode();

      if (TOOLBAR_MODE_GLOBAL.equals(toolbarMode))
      {
        MenuItemCursor cursor = getTopwebMenuItem();
        addHighlightedMenuItems(cursor);
      }
      else
      {
        MenuItemCursor cursor = getContextMenuItem();
        addContextMenuItems(cursor);
      }
    }
    return highlightedItems;
  }

  private boolean isHighlightedItemsReloadRequired()
  {
    boolean reload;

    if (highlightedItems == null)
    {
      reload = true;
    }
    else
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      reload = !userSessionBean.getWorkspaceId().equals(workspaceId);
    }
    return reload;
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

  public String getUserInitial()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    String displayName = userSessionBean.getDisplayName();
    return displayName.length() > 0 ? displayName.substring(0, 1) : "?";
  }

  public void changeSection(String mid)
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    userSessionBean.setSelectedMid(mid);
    userSessionBean.executeSelectedMenuItem();
    userSessionBean.getAttributes().put(CONTEXT_MID, mid);
    highlightedItems = null;
  }

  public void show(String mid)
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    userSessionBean.setSelectedMid(mid);
    userSessionBean.executeSelectedMenuItem();
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

  public void savePreferences()
  {
    try
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      UserPreferences userPreferences = userSessionBean.getUserPreferences();
      userPreferences.storePreference(UserPreferences.LANGUAGE_PROPERTY,
        userSessionBean.getViewLanguage(), false);
      userPreferences.storePreference(UserPreferences.PRIMEFACES_THEME_PROPERTY,
        userSessionBean.getPrimefacesTheme(), false);
      userPreferences.storePreference(UserPreferences.FONT_SIZE_PROPERTY,
        userSessionBean.getFontSize(), false);

      growl("PREFERENCES_SAVED");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public boolean isSectionMenuItem(MenuItemCursor cursor)
  {
    if (TOOLBAR_MODE_CONTEXT.equals(getToolbarMode()))
    {
      MenuItemCursor parent = cursor.getParent();
      return !parent.isRoot() &&
             "true".equals(parent.getDirectProperty(TOPWEB_PROPERTY));
    }
    return false;
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

  public boolean isToolbarEnabled()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    return !"false".equals(userSessionBean.getSelectedMenuItem()
      .getProperty(TOOLBAR_ENABLED));
  }

  /* private methods */

  private String getToolbarMode()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuItemCursor cursor = userSessionBean.getSelectedMenuItem();
    String toolbarMode = cursor.getProperty(TOOLBAR_MODE);

    if (toolbarMode == null) toolbarMode = TOOLBAR_MODE_GLOBAL;
    return toolbarMode;
  }

  private MenuItemCursor getTopwebMenuItem()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuItemCursor cursor = userSessionBean.getSelectedMenuItem();
    while (!cursor.isRoot() &&
           !"true".equals(cursor.getDirectProperty(TOPWEB_PROPERTY)))
    {
      cursor.moveParent();
    }
    return cursor;
  }

  private MenuItemCursor getContextMenuItem()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    String contextMid = (String)userSessionBean.getAttribute(CONTEXT_MID);
    if (contextMid != null)
      return userSessionBean.getMenuModel().getMenuItem(contextMid);

    MenuItemCursor cursor = userSessionBean.getSelectedMenuItem();
    MenuItemCursor ctxCursor = null;
    while (!cursor.isRoot() &&
           !"true".equals(cursor.getDirectProperty(TOPWEB_PROPERTY)))
    {
      ctxCursor = cursor.getClone();
      cursor.moveParent();
    }
    if (ctxCursor == null) return userSessionBean.getSelectedMenuItem();

    userSessionBean.getAttributes().put(CONTEXT_MID, ctxCursor.getMid());

    return ctxCursor;
  }

  private void addHighlightedMenuItems(MenuItemCursor cursor)
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
        addHighlightedMenuItems(cursor);
        cursor.moveNext();
      }
    }
  }

  private void addContextMenuItems(MenuItemCursor cursor)
  {
    if (cursor.hasChildren())
    {
      cursor.moveFirstChild();
      while (!cursor.isNull())
      {
        highlightedItems.add(cursor.getClone());
        cursor.moveNext();
      }
    }
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
