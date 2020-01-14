package org.santfeliu.faces.menu.util;

import java.io.IOException;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.UserSessionBean;

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
      url = MatrixConfig.getProperty("contextPath") +
        "/go.faces?xmid=" + menuItem.getMid();
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
      onclick += "return goMid('" + menuItem.getMid() + "');";
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
