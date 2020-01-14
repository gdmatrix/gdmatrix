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
package org.santfeliu.web.obj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.santfeliu.faces.beansaver.Savable;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.HttpUtils;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author blanquepa
 */
public abstract class DetailBean extends PageBean implements Savable
{
  public static final String DETAIL_PANELS_MID = "detailPanelsMid";
  public static final String PANEL_CLASS = "panelClass";
  public static final String DETAIL_VIEW_PROPERTY = "detailView";
  public static final String TABBED_VIEW = "tabbed";
  public static final String SHORTCUT_URL_MID = "shortcutURLMid";
  public static final String RENDER_CLOSE_BUTTON = "renderCloseButton";

  //tabbed View
  private List<DetailPanel> panels = new ArrayList();
  private boolean edit = false;

  public String show()
  {
    return show(getObjectId());
  }

  public abstract String show(String objectId);

  public boolean isTabbedView()
  {
    String view = getProperty(DETAIL_VIEW_PROPERTY);
     return TABBED_VIEW.equals(view);
  }

  public boolean isEdit()
  {
    return edit;
  }

  public void setEdit(boolean edit)
  {
    this.edit = edit;
  }

  public List<DetailPanel> getPanels()
  {
    return panels;
  }

  public void setPanels(List<DetailPanel> panels)
  {
    this.panels = panels;
  }
  
  public HashMap<String, DetailPanel> getPanelsMap()
  {
    HashMap<String, DetailPanel> result = new HashMap();
    if (panels != null && !panels.isEmpty())
    {
      for (DetailPanel panel : panels)
      {
        result.put(panel.getMid(), panel);
      }
    }
    return result;
  }

  public boolean isRenderPanelContent()
  {
    DetailPanel row = (DetailPanel)getValue("#{panel}");
    return row.isRenderContent();
  }

  public boolean isRenderShortcutURL()
  {
    return getProperty(SHORTCUT_URL_MID) != null;
  }

  public String getShortcutURL()
  {
    HttpServletRequest request = (HttpServletRequest)getExternalContext().getRequest();
    String contextPath = request.getContextPath();
    String serverName = HttpUtils.getServerName(request);
    String scheme = request.getScheme();
    String port = MatrixConfig.getProperty("org.santfeliu.web.defaultPort");
    port = !"80".equals(port) ? ":" + port : "";

    MenuItemCursor cursor = null;
    if (isRenderShortcutURL())
      cursor = UserSessionBean.getCurrentInstance().getMenuModel()
        .getMenuItem(getProperty(SHORTCUT_URL_MID));
    else
      cursor = getControllerBean().getHeadMenuItem(getSelectedMenuItem());

    String url = contextPath + "/go.faces?xmid=" + cursor.getMid()+ "&" + getShortcutURLObjectIdParameter();
    return scheme + "://" + serverName + port + url;
  }

  public abstract String getShortcutURLObjectIdParameter();

  protected void loadPanels()
  {
    panels.clear();

    MenuItemCursor cursor = null;
    String panelsMid = getProperty(DETAIL_PANELS_MID);
    if (panelsMid != null)
    {
      MenuModel menuModel = UserSessionBean.getCurrentInstance().getMenuModel();
      cursor = menuModel.getMenuItem(panelsMid).getClone();
      cursor.moveFirstChild();
    }
    else //If no panels node defined then it searchs from selected node
      cursor = getSelectedMenuItem().getClone();
    
    boolean next = cursor != null;
    while(next)
    {
      String panelClassName = cursor.getDirectProperty(PANEL_CLASS);
      if (panelClassName != null && cursor.isRendered())
      {
        DetailPanel detailPanel = DetailPanel.getInstance(panelClassName, cursor.getMid());
        if (detailPanel != null)
        {
          detailPanel.load(this);
          panels.add(detailPanel);
        }
      }
      next = cursor.moveNext();
    }
  }

  public String getPanelLabel()
  {
    DetailPanel row = (DetailPanel)getValue("#{panel}");
    return row.getLabel();
  }

  public boolean isRenderCloseButton()
  {
    String render = getProperty(RENDER_CLOSE_BUTTON);
    return (render == null || render.equalsIgnoreCase("true"));
  }
}
