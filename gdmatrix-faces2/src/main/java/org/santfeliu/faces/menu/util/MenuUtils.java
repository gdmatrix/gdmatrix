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
package org.santfeliu.faces.menu.util;

import java.io.IOException;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.faces.beansaver.BeanSaverUtils;

/**
 *
 * @author unknown
 */
public class MenuUtils
{
  public static final String URL_ACTION = "url";  
  private static final String MENU_CODE_GENERATED = "menuCodeGenerated";

  public static void encodeJavascript(
    FacesContext context, ResponseWriter writer, UIComponent component)
    throws IOException
  {
    Map requestMap = context.getExternalContext().getRequestMap();
    if (!requestMap.containsKey(MENU_CODE_GENERATED))
    // test if already added to tree
    {
      MenuModel menuModel = 
        UserSessionBean.getCurrentInstance().getMenuModel();
      String selectedMid = menuModel.getSelectedMid();

      writer.startElement("script", component);
      writer.writeAttribute("type", "text/javascript", null);
      writer.writeText("setMid('" + selectedMid + "');", null);
      writer.endElement("script");
      requestMap.put(MENU_CODE_GENERATED, "yes");
    }
  }
  
  public static String getActionURL(MenuItemCursor menuItem)
  {
    String url = null;
    String action = menuItem.getAction();
    if (URL_ACTION.equals(action))
    {
      url = menuItem.getURL();
      if (url == null) url = "#";
    }
    else
    {
      StringBuilder sb = new StringBuilder();
      sb.append(MatrixConfig.getProperty("contextPath"));
      sb.append("/go.faces");
      if (BeanSaverUtils.isSessionSavingMethod())
      {
        ExternalContext extContext = 
          FacesContext.getCurrentInstance().getExternalContext();
        Map cookieMap = extContext.getRequestCookieMap();
        if (cookieMap == null || cookieMap.isEmpty())
        {
          String sessionId = extContext.getSessionId(false);
          sb.append(";jsessionid=");
          sb.append(sessionId);          
        }
      }
      sb.append("?xmid=");
      sb.append(menuItem.getMid());
      url = sb.toString();
    }
    return url;
  }
  
  public static String getOnclick(MenuItemCursor menuItem)
  {
    String onclick = null;
    String action = menuItem.getAction();
    if (!URL_ACTION.equals(action)) // is not an URL
    {
      onclick = "";
      String target = menuItem.getTarget();
      if (target != null)
      {
        onclick += "changeTarget('" + target + "');";
      }
      if (!BeanSaverUtils.isSessionSavingMethod())
      {
        onclick += "return goMid('" + menuItem.getMid() + "');";
      }
    }
    return onclick;
  }
  
  public static void encodeMenuItemLinkAttributes(
    MenuItemCursor menuItem, ResponseWriter writer)
    throws IOException
  {
    String url = getActionURL(menuItem);
    writer.writeAttribute("href", url, null);
    
    String onclick = menuItem.getOnclick();
    if (onclick != null)
    {
      writer.writeAttribute("onclick", onclick, null);
    }
    String target = menuItem.getTarget();
    if (target != null)
    {
      writer.writeAttribute("target", target, null);
    }
  }


}
