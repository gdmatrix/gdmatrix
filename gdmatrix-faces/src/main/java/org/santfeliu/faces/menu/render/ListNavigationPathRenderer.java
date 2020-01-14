package org.santfeliu.faces.menu.render;

import java.io.IOException;
import java.util.Map;
import java.util.Vector;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
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
public class ListNavigationPathRenderer extends Renderer
{
  public ListNavigationPathRenderer()
  {
  }
  
  @Override
  public void encodeBegin(FacesContext context, UIComponent component) throws IOException
  {
    if (!component.isRendered()) return;
    
    HtmlNavigationPath navPath = (HtmlNavigationPath) component;    
    ResponseWriter writer = context.getResponseWriter();
    MenuUtils.encodeJavascript(context, writer, component);

    writer.startElement("ul", navPath);
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
        encodeMenuItem(menuItem, writer, context, navPath, i == 0);
      }
      requestMap.remove(navPath.getVar());
    }
  }

  @Override
  public void encodeEnd(FacesContext context, UIComponent component) throws IOException
  {
    if (!component.isRendered()) return;
    ResponseWriter writer = context.getResponseWriter();
    writer.endElement("ul");
  }  
  
  protected void encodeMenuItem(MenuItemCursor menuItem, ResponseWriter writer, 
                             FacesContext context, HtmlNavigationPath navPath,
                             boolean lastItem)
    throws IOException
  {
    UIComponent sepComponent = navPath.getFacet("separator");
    if (sepComponent != null)
    {
      sepComponent.encodeBegin(context);
      if (sepComponent.getRendersChildren()) sepComponent.encodeChildren(context);
      sepComponent.encodeEnd(context);
    }
    
    writer.startElement("li", navPath);

    boolean active = ACTIVE.equalsIgnoreCase(navPath.getMode());
    if (active && !lastItem)
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
    
    if (active && !lastItem) writer.endElement("a");
    
    writer.endElement("li");    
  }   
}
