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
package org.santfeliu.faces.menu.view;

import java.io.IOException;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.el.ValueExpression;
import javax.faces.component.FacesComponent;

import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import org.apache.commons.lang.StringUtils;

import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.faces.menu.util.MenuUtils;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author realor
 */
@FacesComponent(value = "HtmlNavigationMenu")
public class HtmlNavigationMenu extends UIComponentBase
{
  public static String TABLE = "TABLE";
  public static String LIST = "LIST";

  public static String VERTICAL = "VERTICAL";
  public static String HORIZONTAL = "HORIZONTAL";

  public static String ACTIVE = "ACTIVE";
  public static String PASSIVE = "PASSIVE";
  
  private Object _value; // menuName
  private String _var;
  private String _baseMid;

  private String _layout = TABLE;
  private String _orientation = VERTICAL;
  private String _mode = ACTIVE;
  private Integer _maxVisibleMenuItems;

  private String _style;
  private String _styleClass;
  private String _selectedStyleClass;
  private String _unselectedStyleClass;

  private String JS_MENU_ENCODED = "jsMenuEncoded";

  public String getFamily()
  {
    return "NavigationMenu";
  }

  public HtmlNavigationMenu()
  {
  }

  @Override
  public boolean getRendersChildren()
  {
    return true;
  }

  public void setValue(Object value)
  {
    if (value instanceof String)
    {
      _value = value;
    }
  }

  public Object getValue()
  {
    if (_value != null) return _value;
    ValueExpression ve = getValueExpression("value");
    return ve != null ? (Object)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setVar(String var)
  {
    _var = var;
  }

  public String getVar()
  {
    return _var;
  }

  public void setBaseMid(String baseMid)
  {
    _baseMid = baseMid;
  }

  public String getBaseMid()
  {
    if (_baseMid != null) return _baseMid;
    ValueExpression ve = getValueExpression("baseMid");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setLayout(String layout)
  {
    _layout = layout;
  }

  public String getLayout()
  {
    if (_layout != null) return _layout;
    ValueExpression ve = getValueExpression("layout");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setOrientation(String orientation)
  {
    _orientation = orientation;
  }

  public String getOrientation()
  {
    if (_orientation != null) return _orientation;
    ValueExpression ve = getValueExpression("orientation");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setMode(String mode)
  {
    _mode = mode;
  }

  public String getMode()
  {
    if (_mode != null) return _mode;
    ValueExpression ve = getValueExpression("mode");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setStyle(String style)
  {
    _style = style;
  }

  public String getStyle()
  {
    if (_style != null) return _style;
    ValueExpression ve = getValueExpression("style");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setStyleClass(String styleClass)
  {
    _styleClass = styleClass;
  }

  public String getStyleClass()
  {
    if (_styleClass != null) return _styleClass;
    ValueExpression ve = getValueExpression("styleClass");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setSelectedStyleClass(String selectedStyleClass)
  {
    _selectedStyleClass = selectedStyleClass;
  }

  public String getSelectedStyleClass()
  {
    if (_selectedStyleClass != null) return _selectedStyleClass;
    ValueExpression ve = getValueExpression("selectedStyleClass");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setUnselectedStyleClass(String unselectedStyleClass)
  {
    _unselectedStyleClass = unselectedStyleClass;
  }

  public String getUnselectedStyleClass()
  {
    if (_unselectedStyleClass != null) return _unselectedStyleClass;
    ValueExpression ve = getValueExpression("unselectedStyleClass");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setMaxVisibleMenuItems(Integer maxVisibleMenuItems)
  {
    this._maxVisibleMenuItems = maxVisibleMenuItems;
  }

  public Integer getMaxVisibleMenuItems()
  {
    if (_maxVisibleMenuItems != null) return _maxVisibleMenuItems;
    ValueExpression ve = getValueExpression("maxVisibleMenuItems");
    return ve != null ? (Integer)ve.getValue(getFacesContext().getELContext()) : null;
  }

  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    if (!isRendered()) return;
    ResponseWriter writer = context.getResponseWriter();
    MenuUtils.encodeJavascript(context, writer, this);    
    if (getLayout().equalsIgnoreCase(TABLE))
    {
      writer.startElement("table", this);
      writer.writeAttribute("cellspacing", "0", null);
      writer.writeAttribute("cellpadding", "0", null);
    }
    else if (getLayout().equalsIgnoreCase(LIST))
    {
      encodeJavascript(context, writer);
      writer.startElement("ul", this);
      writer.writeAttribute("id", getClientId(context), null);
    }
    if (getStyle() != null)
    {
      writer.writeAttribute("style", getStyle(), null);
    }
    if (getStyleClass() != null)
    {
      writer.writeAttribute("class", getStyleClass(), null);     
    }
  }

  @Override
  public void encodeChildren(FacesContext context) throws IOException
  {
    if (!isRendered()) return;
    String menuName = (String)getValue();
    if (menuName == null) return;
    
    MenuItemCursor baseMenuItem = getBaseMenuItem();

    if (baseMenuItem != null && !baseMenuItem.isNull())
    {
      String layout = getLayout();
      if (layout.equalsIgnoreCase(TABLE))
      {
        if (getOrientation().equalsIgnoreCase(HORIZONTAL))
        {
          encodeHorizontalTableMenu(baseMenuItem, context);
        }
        else // VERTICAL
        {
          encodeVerticalTableMenu(baseMenuItem, context);
        }
      }
      else if (layout.equalsIgnoreCase(LIST)) // LIST
      {
        encodeListMenu(baseMenuItem, context);
      }
    }
  }

  @Override
  public void encodeEnd(FacesContext context) throws IOException
  {
    if (!isRendered()) return;
    ResponseWriter writer = context.getResponseWriter();
    if (getLayout().equalsIgnoreCase(TABLE))
    {
      writer.endElement("table");
    }
    else if (getLayout().equalsIgnoreCase(LIST))
    {
      writer.endElement("ul");
      Integer maxVisibleMenuItems = getMaxVisibleMenuItems();
      if (maxVisibleMenuItems != null)
      {
        int pos = getSelectedTabIndex(getBaseMenuItem());
        pos = (pos / maxVisibleMenuItems) * maxVisibleMenuItems;
        writer.startElement("script", this);
        writer.writeAttribute("type", "text/javascript", null);
        writer.writeText("initMenu('" + getClientId(context) +
          "');", null);
        writer.writeText("showMenu('" + getClientId(context) +
          "', " + pos + ", " + maxVisibleMenuItems + ")", null);
        writer.endElement("script");
      }
    }
  }

  private void encodeVerticalTableMenu(MenuItemCursor baseMenuItem, 
                                       FacesContext context)
    throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();
    MenuItemCursor childMenuItem = baseMenuItem.getFirstChild();
    Map requestMap = context.getExternalContext().getRequestMap();

    while (!childMenuItem.isNull())
    {
      requestMap.put(getVar(), childMenuItem);
      if (isAnyChildComponentRendered())
      {
        writer.startElement("tr", this);
        encodeMenuItem("td", childMenuItem, writer, context);
        writer.endElement("tr");
      }
      childMenuItem.moveNext();
    }
    requestMap.remove(getVar());
  }

  private void encodeHorizontalTableMenu(MenuItemCursor baseMenuItem,
                                         FacesContext context)
    throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();
    MenuItemCursor childMenuItem = baseMenuItem.getFirstChild();
    Map requestMap = context.getExternalContext().getRequestMap();   
    
    writer.startElement("tr", this);
    while (!childMenuItem.isNull())
    {
      requestMap.put(getVar(), childMenuItem);
      if (isAnyChildComponentRendered())
      {
        encodeMenuItem("td", childMenuItem, writer, context);
      }
      childMenuItem.moveNext();
    }
    writer.endElement("tr");
    requestMap.remove(getVar());
  }

  private int getSelectedTabIndex(MenuItemCursor baseMenuItem)
  {
    int index = 0;
    boolean found = false;
    MenuItemCursor childMenuItem = baseMenuItem.getFirstChild();
    while (!childMenuItem.isNull() && !found)
    {
      if (childMenuItem.isRendered())
      {
        if (childMenuItem.containsSelection()) found = true;
        else index++;
      }
      childMenuItem.moveNext();
    }
    return index;
  }

  private void encodeListMenu(MenuItemCursor baseMenuItem, 
                              FacesContext context)
    throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();
    MenuItemCursor childMenuItem = baseMenuItem.getFirstChild();
    Map requestMap = context.getExternalContext().getRequestMap();

    while (!childMenuItem.isNull())
    {
      requestMap.put(getVar(), childMenuItem);
      if (isAnyChildComponentRendered())
      {
        encodeMenuItem("li", childMenuItem, writer, context);
      }
      childMenuItem.moveNext();
    }
    requestMap.remove(getVar());
  }

  private MenuItemCursor getBaseMenuItem()
  {
    MenuItemCursor baseMenuItem;

    MenuModel menuModel =
      UserSessionBean.getCurrentInstance().getMenuModel();

    if (menuModel == null) return null;
    
    String baseMid = getBaseMid();

    if (StringUtils.isBlank(baseMid))
    {
      baseMenuItem = menuModel.getSelectedMenuItem();
      if (!baseMenuItem.isNull())
      {
        if (!baseMenuItem.hasChildren())
        {
          MenuItemCursor parentMenuItem = baseMenuItem.getParent();
          if (!parentMenuItem.isNull())
          {
            baseMenuItem = parentMenuItem;
          }
        }
      }
    }
    else
    {
      baseMenuItem = menuModel.getMenuItem(baseMid);
    }
    return baseMenuItem;
  }

  private void encodeMenuItem(String tag, MenuItemCursor menuItem,
    ResponseWriter writer, FacesContext context) throws IOException
  {
    writer.startElement(tag, this);
    String styleClass;
    if (menuItem.containsSelection())
    {
      styleClass = getSelectedStyleClass();
    }
    else
    {
      styleClass = getUnselectedStyleClass();
    }
    if (styleClass != null)
      writer.writeAttribute("class", styleClass, null);

    boolean active = ACTIVE.equalsIgnoreCase(getMode());
    if (active)
    {
      writer.startElement("a", this);
      MenuUtils.encodeMenuItemLinkAttributes(menuItem, writer);
    }
    List list = getChildren();
    Iterator iter = list.iterator();
    while (iter.hasNext())
    {
      UIComponent component = (UIComponent)iter.next();
      component.encodeBegin(context);
      if (component.getRendersChildren()) component.encodeChildren(context);
      component.encodeEnd(context);
    }
    if (active) writer.endElement("a");
    writer.endElement(tag);
  }

  private void encodeJavascript(FacesContext context, ResponseWriter writer)
    throws IOException
  {
    if (getMaxVisibleMenuItems() != null)
    {
      Map attributes = (Map)context.getExternalContext().getRequestMap();
      String encoded = (String)attributes.get(JS_MENU_ENCODED);
      if (encoded == null)
      {
        attributes.put(JS_MENU_ENCODED, "encoded");
        writer.startElement("script", this);
        writer.writeAttribute("type", "text/javascript", null);
        writer.writeAttribute("src", "/plugins/menu/menu.js", null);
        writer.endElement("script");
      }
    }
  }

  private boolean isAnyChildComponentRendered()
  {
    List list = getChildren();
    boolean render = false;
    Iterator iter = list.iterator();
    while (iter.hasNext() && !render)
    {
      UIComponent component = (UIComponent)iter.next();
      render = component.isRendered();
    }
    return render;
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[12];
    values[0] = super.saveState(context);
    values[1] = _value;
    values[2] = _var;
    values[3] = _baseMid;
    values[4] = _layout;
    values[5] = _orientation;
    values[6] = _mode;    
    values[7] = _style;
    values[8] = _styleClass;
    values[9] = _selectedStyleClass;
    values[10] = _unselectedStyleClass;
    values[11] = _maxVisibleMenuItems;
    return values;
  }
  
  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _value = values[1];
    _var = (String)values[2];
    _baseMid = (String)values[3];
    _layout = (String)values[4];
    _orientation = (String)values[5];
    _mode = (String)values[6];
    _style = (String)values[7];
    _styleClass = (String)values[8];
    _selectedStyleClass = (String)values[9];
    _unselectedStyleClass = (String)values[10];
    _maxVisibleMenuItems = (Integer)values[11];
  }
}
