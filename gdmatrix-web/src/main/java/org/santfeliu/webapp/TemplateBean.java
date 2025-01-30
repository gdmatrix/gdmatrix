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

import com.google.gson.Gson;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.faces.FacesBean;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.web.ApplicationBean;
import org.santfeliu.web.UserPreferences;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.webapp.util.ContextMenuTypesFinder;
import org.santfeliu.webapp.util.WebUtils;
import static org.santfeliu.webapp.util.WebUtils.TOPWEB_PROPERTY;

/**
 *
 * @author blanquepa
 * @author realor
*/
@Named
@RequestScoped
public class TemplateBean extends FacesBean implements Serializable
{
  public static final String TOOLBAR_ENABLED_PROPERTY = "toolbarEnabled";
  public static final String HIGHLIGHTED_PROPERTY = "highlighted";
  public static final String CONTEXT_PROPERTY = "context";
  public static final String CONTEXT_AUTO = "auto";

  private String componentTree;
  private String username;
  private String password;
  private List<MenuItemCursor> highlightedItems;
  private String workspaceId;
  private String contextMid;

  @Inject
  NavigatorBean navigatorBean;

  @PostConstruct
  public void init()
  {
    navigatorBean.setMenuTypesFinder(new ContextMenuTypesFinder());
  }

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
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuItemCursor cursor = userSessionBean.getSelectedMenuItem();
    cursor = WebUtils.getTopWebMenuItem(cursor);

    ApplicationBean applicationBean = ApplicationBean.getCurrentInstance();
    return applicationBean.translate(cursor.getProperty("description"));
  }

  public String getContextTitle()
  {
    ApplicationBean applicationBean = ApplicationBean.getCurrentInstance();
    MenuItemCursor cursor = getContextMenuItem();

    return applicationBean.translate(cursor.getProperty("description"));
  }

  public List<MenuItemCursor> getHighlightedItems()
  {
    if (isHighlightedItemsReloadRequired())
    {
      workspaceId = UserSessionBean.getCurrentInstance().getWorkspaceId();
      highlightedItems = new ArrayList<>();

      MenuItemCursor cursor = getContextMenuItem();

      addHighlightedMenuItems(cursor);
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

  // execute mid with context change (if mid is null, mid = topWebMid)
  public void changeContext(String mid)
  {
    String ctxMid = contextMid;

    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    if (StringUtils.isBlank(mid))
    {
      MenuItemCursor topCursor =
        WebUtils.getTopWebMenuItem(userSessionBean.getSelectedMenuItem());
      topCursor.select();
      contextMid = topCursor.getMid();
    }
    else
    {
      userSessionBean.setSelectedMid(mid);
      contextMid = getContextMid(true);
    }

    if (!contextMid.equals(ctxMid)) // context has changed
    {
      // force toolbar & menu update
      highlightedItems = null;
    }
    userSessionBean.executeSelectedMenuItem();
  }

  // execute mid without context change
  public void show(String mid)
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    userSessionBean.setSelectedMid(mid);
    userSessionBean.executeSelectedMenuItem();
  }

  public String showObject()
  {
    Map<String, String> map = getExternalContext().getRequestParameterMap();
    String typeId = map.get("typeId");
    String objectId = map.get("objectId");
    String jsonParams = map.get("parameters");
    Map<String, Object> parameters = null;
    if (jsonParams != null)
    {
      Gson gson = new Gson();
      parameters = gson.fromJson(jsonParams, Map.class);
    }
    return navigatorBean.show(typeId, objectId, parameters);
  }

  public void viewObject()
  {
    Map<String, String> map = getExternalContext().getRequestParameterMap();
    String objectId = map.get("objectId");
    String jsonParams = map.get("parameters");
    Map<String, Object> parameters = null;
    if (jsonParams != null)
    {
      Gson gson = new Gson();
      parameters = gson.fromJson(jsonParams, Map.class);
    }
    navigatorBean.view(objectId, parameters);
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

  public boolean isContextChangeMenuItem(MenuItemCursor cursor)
  {
    if (cursor.getDirectProperty(CONTEXT_PROPERTY) != null) return true;

    return CONTEXT_AUTO.equals(cursor.getProperty(CONTEXT_PROPERTY)) &&
      !getContextMid().equals(getContextMid(cursor));
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
      .getProperty(TOOLBAR_ENABLED_PROPERTY));
  }

  public String getContextMid()
  {
    return getContextMid(false);
  }

  public String getContextMid(boolean update)
  {
    if (contextMid == null || update)
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      MenuItemCursor cursor = userSessionBean.getSelectedMenuItem();
      contextMid = getContextMid(cursor);
    }
    return contextMid;
  }

  public String getContextMid(MenuItemCursor cursor)
  {
    MenuItemCursor ctxCursor = cursor.getClone();
    while (!ctxCursor.isRoot() &&
           !"true".equals(ctxCursor.getDirectProperty(TOPWEB_PROPERTY)) &&
           ctxCursor.getDirectProperty(CONTEXT_PROPERTY) == null)
    {
      ctxCursor.moveParent();
    }
    return ctxCursor.getMid();
  }

  public void setContextMid(String mid)
  {
    contextMid = mid;
  }

  /* private methods */

  private MenuItemCursor getContextMenuItem()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    return userSessionBean.getMenuModel().getMenuItem(getContextMid());
  }

  private void addHighlightedMenuItems(MenuItemCursor cursor)
  {
    if (cursor.hasChildren())
    {
      cursor = cursor.getClone();
      cursor.moveFirstChild();
      while (!cursor.isNull())
      {
        if (cursor.getDirectProperty(CONTEXT_PROPERTY) == null)
        {
          if ("true".equals(cursor.getDirectProperty(HIGHLIGHTED_PROPERTY))
              && cursor.getDirectProperty("icon") != null)
          {
            highlightedItems.add(cursor.getClone());
          }
          else
          {
            addHighlightedMenuItems(cursor);
          }
        }
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
