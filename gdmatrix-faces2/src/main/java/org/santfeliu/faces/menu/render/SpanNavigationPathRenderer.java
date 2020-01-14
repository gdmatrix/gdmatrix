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
package org.santfeliu.faces.menu.render;

import java.io.IOException;
import java.util.Map;
import java.util.Vector;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;
import javax.faces.render.Renderer;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.faces.menu.util.MenuUtils;
import org.santfeliu.faces.menu.view.HtmlNavigationPath;
import static org.santfeliu.faces.menu.view.HtmlNavigationPath.ACTIVE;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author blanquepa
 */
@FacesRenderer(componentFamily="NavigationPath",
	rendererType="SpanNavigationPath")
public class SpanNavigationPathRenderer extends Renderer
{
  public SpanNavigationPathRenderer()
  {
  }
  
  @Override
  public void encodeBegin(FacesContext context, UIComponent component) throws IOException
  {
    if (!component.isRendered()) return;
    
    HtmlNavigationPath navPath = (HtmlNavigationPath) component;    
    ResponseWriter writer = context.getResponseWriter();
    MenuUtils.encodeJavascript(context, writer, component);

    writer.startElement("span", navPath);
    String style = navPath.getStyle();
    if (style != null)
    {
      writer.writeAttribute("style", style, null);
    }
    String styleClass = navPath.getStyleClass();
    if (styleClass != null)
    {
      writer.writeAttribute("class", styleClass, null);
    }
  } 
  
  @Override
  public void encodeChildren(FacesContext context, UIComponent component) throws IOException
  {
    if (!component.isRendered()) return;
    HtmlNavigationPath navPath = (HtmlNavigationPath) component;        
    ExternalContext extContext = context.getExternalContext();
  
    String menuName = (String)navPath.getValue();
    if (menuName == null) return;
    
    MenuModel menuModel = 
      UserSessionBean.getCurrentInstance().getMenuModel();

    if (menuModel == null) return;

    MenuItemCursor selMenuItem = menuModel.getSelectedMenuItem();
    if (!selMenuItem.isNull())
    {
      Integer maxDepth = navPath.getMaxDepth();      
      Vector itemsVector = new Vector();
      
      MenuItemCursor menuItem = selMenuItem;
      boolean stop = false;
      do
      {
        if (menuItem.getMid().equals(navPath.getBaseMid())) stop = true;
        if (maxDepth == null || menuItem.getDepth() <= maxDepth)
        {
          itemsVector.addElement(menuItem);
        }        
        menuItem = menuItem.getParent();
        if (menuItem.isNull()) stop = true;
      } while (!stop);

      Map requestMap = extContext.getRequestMap();
      ResponseWriter writer = context.getResponseWriter();
      for (int i = itemsVector.size() - 1; i >= 0; i--)
      {
        menuItem = (MenuItemCursor)itemsVector.elementAt(i);
        requestMap.put(navPath.getVar(), menuItem);
        encodeMenuItem(menuItem, writer, context, navPath);
      }
      requestMap.remove(navPath.getVar());
    }
  }

  @Override
  public void encodeEnd(FacesContext context, UIComponent component) throws IOException
  {
    if (!component.isRendered()) return;
    ResponseWriter writer = context.getResponseWriter();
    writer.endElement("span");
  }
  
  protected void encodeMenuItem(MenuItemCursor menuItem, ResponseWriter writer, 
                             FacesContext context, HtmlNavigationPath navPath)
    throws IOException
  {
    UIComponent sepComponent = navPath.getFacet("separator");
    if (sepComponent != null)
    {
      sepComponent.encodeBegin(context);
      if (sepComponent.getRendersChildren()) sepComponent.encodeChildren(context);
      sepComponent.encodeEnd(context);
    }

    boolean active = ACTIVE.equalsIgnoreCase(navPath.getMode());
    if (active)
    {
      writer.startElement("a", navPath);
      MenuUtils.encodeMenuItemLinkAttributes(menuItem, writer);
    }

    UIComponent menuItemComponent = navPath.getFacet("menuitem");
    menuItemComponent.encodeBegin(context);
    if (menuItemComponent.getRendersChildren()) 
    {
      menuItemComponent.encodeChildren(context);
    }
    menuItemComponent.encodeEnd(context);
    
    if (active) writer.endElement("a");
  }  
  
}
