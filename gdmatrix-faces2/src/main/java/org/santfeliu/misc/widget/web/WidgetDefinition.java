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
package org.santfeliu.misc.widget.web;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.web.UserSessionBean;
import java.util.List;
import org.santfeliu.util.HTMLNormalizer;

/**
 *
 * @author unknown
 */
public class WidgetDefinition implements Serializable
{
  private static final String DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";    
  private String mid;
  private boolean visible;
  private int column;

  public WidgetDefinition(String mid)
  {
    this.mid = mid;
  }

  public String getWidgetId()
  {
    String widgetId = 
      (String)getCursor().getDirectProperties().get("widgetId");
    return widgetId == null ? "w" + mid : widgetId;
  }

  public String getMid()
  {
    return mid;
  }

  public String getHeaderBuilder()
  {
    return (String)getCursor().getDirectProperties().get("headerBuilder");
  }

  public String getContentBuilder()
  {
    return (String)getCursor().getDirectProperties().get("contentBuilder");
  }

  public String getFooterBuilder()
  {
    return (String)getCursor().getDirectProperties().get("footerBuilder");
  }
    
  public String getInfoUrl()
  {
    Map properties = getCursor().getDirectProperties();    
    String infoURL = (String)properties.get("showMoreURL");
    if (infoURL == null) infoURL = (String)properties.get("url");
    if (infoURL == null) infoURL = (String)properties.get("rssURL");
    return infoURL;
  }
  
  public boolean isSelectorLinkRender()
  {
    List<String> roles = getCursor().getMultiValuedProperty("selectorLinkRole");
    return UserSessionBean.getCurrentInstance().isUserInRole(roles);
  }
  
  public String getReadRolesDescription()
  {
    StringBuilder sb = new StringBuilder();
    List<String> values = getCursor().getMultiValuedProperty("roles.select");
    if (values != null && !values.isEmpty())
    {
      for (String value : values)
      {
        if (sb.length() > 0) sb.append(",");
        sb.append(value);
      }
    }
    return sb.toString();
  }

  public boolean isInCatalogue()
  {
    return "true".equals(getCursor().getProperty("inCatalogue"));
  }

  public boolean isBannerWidget()
  {
    return "BannerWidgetBuilder".equals(getContentBuilder());
  }

  public String getContentType()
  {
    String contentBuilder = getContentBuilder();
    if (contentBuilder != null && contentBuilder.contains("WidgetBuilder"))
    {
      return contentBuilder.substring(0, 
        contentBuilder.lastIndexOf("WidgetBuilder")).toLowerCase();
    }
    return null;    
  }
  
  public String getDragAreaPosition()
  {
    String dragAreaPosition = 
      (String)getCursor().getProperties().get("dragAreaPosition");
    if (dragAreaPosition == null)
    {
      dragAreaPosition = (isBannerWidget() ? "content" : "header");
    }
    return dragAreaPosition;
  }

  public String getCloseLinkPosition()
  {
    String closeLinkPosition = 
      (String)getCursor().getProperties().get("closeLinkPosition");
    if (closeLinkPosition == null)
    {
      closeLinkPosition = (isBannerWidget() ? "content" : "header");
    }
    return closeLinkPosition;
  }
  
  public Date getAddSince()
  {
    Date date;
    String addSince = (String)getCursor().getProperties().get("addSince");
    if (addSince == null)
    {
      date = null;
    }
    else
    {
      SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
      try
      {
        date = df.parse(addSince);
      }
      catch (ParseException ex)
      {
        date = null;
      }
    }
    return date;
  }

  public boolean isVisible()
  {
    return visible;
  }

  public void setVisible(boolean visible)
  {
    this.visible = visible;
  }

  public int getLayoutColumn()
  {
    return column - 1;
  }

  public void setLayoutColumn(int column)
  {
    this.column = column + 1;
  }

  public int getColumn()
  {
    return this.column;
  }

  public void setColumn(int column)
  {
    this.column = column;
  }

  public String getLabel()
  {
    return getCursor().getLabel();
  }

  public String getTitle()
  {
    return getCursor().getProperty("title");
  }

  public String getStatus()
  {
    return getCursor().getProperty("status");
  }

  public String getHeaderImage()
  {
    return getCursor().getProperty("headerImage");
  }

  public String getInfo()
  {
    return getCursor().getProperty("info");
  }

  public String getIconUrl()
  {
    String iconURL = getCursor().getProperty("icon");
    if (iconURL == null)
      iconURL = getCursor().getProperty("iconURL");
    return iconURL;
  }

  public String getStyle()
  {
    return getCursor().getProperty("style");
  }
  
  public String getStyleClass()
  {
    String styleClass = getCursor().getProperty("styleClass");
    return styleClass == null ? "widget" : styleClass;
  }

  public String getExternalTitle()
  {
    return getCursor().getProperty("externalTitle");
  }
  
  public Boolean isAriaHidden()
  {
    String ariaHidden = getCursor().getProperty("ariaHidden");
    return (ariaHidden != null ? Boolean.valueOf(ariaHidden) : null);
  }
  
  public String getScrollerStyle()
  {
    return getCursor().getProperty("scrollerStyle");
  }

  public Map getProperties()
  {
    return getCursor().getProperties();
  }

  public List<String> getMultivaluedProperty(String propertyName)
  {
    return getCursor().getMultiValuedProperty(propertyName);
  }
  
  public String getAriaDescription()
  {
    StringBuilder sbResult = new StringBuilder();    
    Map properties = getProperties();
    String title = (String)properties.get("title");
    if (title != null)
    {
      if (title.contains("<a"))
      {
        sbResult.append(HTMLNormalizer.cleanHTML(title.trim(), true));
      }
      else
      {
        sbResult.append(title.trim());
      }      
      String subTitle = (String)properties.get("subTitle");
      if (subTitle != null)
      {
        sbResult.append(" ");
        if (subTitle.contains("<a"))
        {
          sbResult.append(HTMLNormalizer.cleanHTML(subTitle.trim(), true));
        }
        else
        {
          sbResult.append(subTitle.trim());
        }
      }
    }
    else
    {
      String label = getLabel();
      if (label != null)
      {
        sbResult.append(label.trim());
      }
      else
      {
        String widgetId = getWidgetId();
        sbResult.append("(*").append(widgetId).append("*)");
      }
    }
    return sbResult.toString();
  }

  protected MenuItemCursor getCursor()
  {
    return UserSessionBean.getCurrentInstance().getMenuModel().getMenuItem(mid).
      getClone();
  }
}