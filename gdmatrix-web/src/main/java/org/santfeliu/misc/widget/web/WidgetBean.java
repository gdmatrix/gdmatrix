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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.santfeliu.misc.widget.web.builder.WidgetBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import org.apache.myfaces.custom.div.Div;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.Translator;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.faces.widget.HtmlWidget;
import org.santfeliu.faces.widget.HtmlWidgetContainer;
import org.santfeliu.faces.widget.WidgetLayout;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.ApplicationBean;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.bean.CMSProperty;

/**
 *
 * @author realor
 */
@CMSManagedBean
public class WidgetBean extends WebBean
{
  @CMSProperty
  public static final String HEADER_COLUMNS_PROPERTY = "headerColumns";
  @CMSProperty
  public static final String HEADER_COLUMN_CLASSES_PROPERTY =
    "headerColumnClasses";
  @CMSProperty
  public static final String HEADER_COLUMN_TITLES_PROPERTY =
    "headerColumnTitles";
  @CMSProperty
  public static final String HEADER_COLUMN_RENDER_AS_LIST_PROPERTY =
    "headerColumnRenderAsList";
  @CMSProperty
  public static final String FOOTER_COLUMNS_PROPERTY = "footerColumns";
  @CMSProperty
  public static final String FOOTER_COLUMN_CLASSES_PROPERTY =
    "footerColumnClasses";
  @CMSProperty
  public static final String FOOTER_COLUMN_TITLES_PROPERTY =
    "footerColumnTitles";
  @CMSProperty
  public static final String FOOTER_COLUMN_RENDER_AS_LIST_PROPERTY =
    "footerColumnRenderAsList";
  @CMSProperty
  public static final String MAIN_COLUMNS_PROPERTY = "mainColumns";
  @CMSProperty
  public static final String MAIN_COLUMN_CLASSES_PROPERTY = "mainColumnClasses";
  @CMSProperty
  public static final String MAIN_COLUMN_TITLES_PROPERTY = "mainColumnTitles";
  @CMSProperty
  public static final String MAIN_COLUMN_RENDER_AS_LIST_PROPERTY =
    "mainColumnRenderAsList";  
  @CMSProperty
  public static final String LEFT_COLUMNS_PROPERTY = "leftColumns";
  @CMSProperty
  public static final String LEFT_COLUMN_CLASSES_PROPERTY = "leftColumnClasses";
  @CMSProperty
  public static final String LEFT_COLUMN_TITLES_PROPERTY = "leftColumnTitles";
  @CMSProperty
  public static final String LEFT_COLUMN_RENDER_AS_LIST_PROPERTY =
    "leftColumnRenderAsList";
  @CMSProperty
  public static final String CUSTOM_COLUMNS_PROPERTY = "customColumns";
  @CMSProperty
  public static final String CUSTOM_COLUMN_CLASSES_PROPERTY =
    "customColumnClasses";
  @CMSProperty
  public static final String CUSTOM_COLUMN_TITLES_PROPERTY =
    "customColumnTitles";
  @CMSProperty
  public static final String CUSTOM_COLUMN_RENDER_AS_LIST_PROPERTY =
    "customColumnRenderAsList";  
  @CMSProperty
  public static final String CUSTOM_DRAGGABLE_WIDGETS =
    "customDraggableWidgets";  
  @CMSProperty
  public static final String CUSTOM_COLUMN_WIDGET_TYPE_PROPERTY =
    "customColumnWidgetType";  
  @CMSProperty
  public static final String INTERIOR_COLUMNS_PROPERTY = "interiorWidgetColumns";
  @CMSProperty
  public static final String INTERIOR_TOP_COLUMNS_PROPERTY = "interiorTopWidgetColumns";
  @CMSProperty
  public static final String INTERIOR_BOTTOM_COLUMNS_PROPERTY = "interiorBottomWidgetColumns";
  @CMSProperty
  public static final String WIDGET_SELECTOR_WINDOW_PROPERTY =
    "widgetSelectorWindow";
  @CMSProperty
  public static final String HEADER_LAYOUT_PROPERTY = "headerLayout";
  @CMSProperty
  public static final String FOOTER_LAYOUT_PROPERTY = "footerLayout";
  @CMSProperty
  public static final String MAIN_LAYOUT_PROPERTY = "mainLayout";
  @CMSProperty
  public static final String LEFT_LAYOUT_PROPERTY = "leftLayout";
  @CMSProperty
  public static final String CUSTOM_LAYOUT_PROPERTY = "customLayout";
  @CMSProperty
  public static final String INFO_PANEL_DOCID_PROPERTY = "infoPanelDocId";
  @CMSProperty
  public static final String WIDGET_MID_PROPERTY = "widgetMid";
  @CMSProperty
  public static final String INTERIOR_WIDGET_MID_PROPERTY = "interiorWidgetMid";
  @CMSProperty
  public static final String INTERIOR_TOP_WIDGET_MID_PROPERTY = "interiorTopWidgetMid";
  @CMSProperty
  public static final String INTERIOR_BOTTOM_WIDGET_MID_PROPERTY = "interiorBottomWidgetMid";
  @CMSProperty
  public static final String COOKIE_NAME_PROPERTY = "cookieName";
  @CMSProperty
  public static final String INTERIOR_LAYOUT_PROPERTY = "interiorWidgetLayout";
  @CMSProperty
  public static final String INTERIOR_TOP_LAYOUT_PROPERTY = "interiorTopWidgetLayout";
  @CMSProperty
  public static final String INTERIOR_BOTTOM_LAYOUT_PROPERTY = "interiorBottomWidgetLayout";
  @CMSProperty
  public static final String INTERIOR_WIDGET_RENDER_PROPERTY = "interiorWidgetRender";
  @CMSProperty
  public static final String INTERIOR_TOP_WIDGET_RENDER_PROPERTY = "interiorTopWidgetRender";
  @CMSProperty
  public static final String INTERIOR_BOTTOM_WIDGET_RENDER_PROPERTY = "interiorBottomWidgetRender";

  @CMSProperty
  public static final String INTERIOR_COLUMN_CLASSES_PROPERTY = "interiorWidgetColumnClasses";
  @CMSProperty
  public static final String INTERIOR_COLUMN_TITLES_PROPERTY = "interiorWidgetColumnTitles";
  @CMSProperty
  public static final String INTERIOR_COLUMN_RENDER_AS_LIST_PROPERTY =
    "interiorWidgetColumnRenderAsList";    
  @CMSProperty
  public static final String INTERIOR_TITLE_PROPERTY = "interiorWidgetTitle";
  @CMSProperty
  public static final String INTERIOR_HEADER_DOC_ID_PROPERTY = "interiorWidgetHeaderDocId";
  @CMSProperty
  public static final String INTERIOR_FOOTER_DOC_ID_PROPERTY = "interiorWidgetFooterDocId";
  
  @CMSProperty
  public static final String INTERIOR_TOP_COLUMN_CLASSES_PROPERTY = "interiorTopWidgetColumnClasses";
  @CMSProperty
  public static final String INTERIOR_TOP_COLUMN_TITLES_PROPERTY = "interiorTopWidgetColumnTitles";
  @CMSProperty
  public static final String INTERIOR_TOP_COLUMN_RENDER_AS_LIST_PROPERTY =
    "interiorTopWidgetColumnRenderAsList";
  @CMSProperty
  public static final String INTERIOR_TOP_TITLE_PROPERTY = "interiorTopWidgetTitle";
  @CMSProperty
  public static final String INTERIOR_TOP_HEADER_DOC_ID_PROPERTY = "interiorTopWidgetHeaderDocId";
  @CMSProperty
  public static final String INTERIOR_TOP_FOOTER_DOC_ID_PROPERTY = "interiorTopWidgetFooterDocId";
  
  @CMSProperty
  public static final String INTERIOR_BOTTOM_COLUMN_CLASSES_PROPERTY = "interiorBottomWidgetColumnClasses";
  @CMSProperty
  public static final String INTERIOR_BOTTOM_COLUMN_TITLES_PROPERTY = "interiorBottomWidgetColumnTitles";
  @CMSProperty
  public static final String INTERIOR_BOTTOM_COLUMN_RENDER_AS_LIST_PROPERTY =
    "interiorBottomWidgetColumnRenderAsList";
  @CMSProperty
  public static final String INTERIOR_BOTTOM_TITLE_PROPERTY = "interiorBottomWidgetTitle";
  @CMSProperty
  public static final String INTERIOR_BOTTOM_HEADER_DOC_ID_PROPERTY = "interiorBottomWidgetHeaderDocId";
  @CMSProperty
  public static final String INTERIOR_BOTTOM_FOOTER_DOC_ID_PROPERTY = "interiorBottomWidgetFooterDocId";
  
  private static final String DOC_SERVLET_URL = "/documents/";
  private static final String DATE_FORMAT = "dd/MM/yyyy-HH:mm:ss";
  private static final int EXPIRE_DAYS = 500;
  private static final String DEFAULT_WIDGET_SELECTOR_WINDOW = "7";
  
  private HtmlWidgetContainer headerContainer;
  private HtmlWidgetContainer mainContainer;
  private HtmlWidgetContainer leftContainer;
  private HtmlWidgetContainer customContainer;
  private HtmlWidgetContainer footerContainer;
  private HtmlWidgetContainer interiorContainer; //internal pages 
  private HtmlWidgetContainer interiorTopContainer; //internal pages - top
  private HtmlWidgetContainer interiorBottomContainer; //internal pages - bottom  

  // custom widget definitions
  private Map<String, WidgetDefinition> widgetDefinitionMap;  
  private List<WidgetDefinition> widgetCatalogue;
  private transient Map cookieMap;
  
  private Integer scroll;
  
  public WidgetBean()
  {
  }

  // containers
  public HtmlWidgetContainer getHeaderContainer()
  {
    return headerContainer;
  }

  public void setHeaderContainer(HtmlWidgetContainer headerContainer)
  {
    this.headerContainer = headerContainer;
  }
  
  public HtmlWidgetContainer getFooterContainer()
  {
    return footerContainer;
  }

  public void setFooterContainer(HtmlWidgetContainer footerContainer)
  {
    this.footerContainer = footerContainer;
  }

  public HtmlWidgetContainer getMainContainer()
  {
    return mainContainer;
  }

  public void setMainContainer(HtmlWidgetContainer upperContainer)
  {
    this.mainContainer = upperContainer;
  }

  public HtmlWidgetContainer getLeftContainer()
  {
    return leftContainer;    
  }

  public void setLeftContainer(HtmlWidgetContainer leftContainer)
  {
    this.leftContainer = leftContainer;
  }
 
  public HtmlWidgetContainer getInteriorContainer()
  {
    interiorContainer = new HtmlWidgetContainer();
    String widgetMid = getCursor().getProperty(INTERIOR_WIDGET_MID_PROPERTY);
    if (widgetMid == null)
    {
      widgetMid = getCursor().getProperty(WIDGET_MID_PROPERTY);
    }
    if (widgetMid != null)
    {
      List<String> interiorLayout = getInteriorLayout();
      if (interiorLayout != null && !interiorLayout.isEmpty() && isInteriorWidgetRender())
      {
        loadInteriorContainer(interiorContainer, interiorLayout, widgetMid);
      }
    }
    return interiorContainer;
  }

  public void setInteriorContainer(HtmlWidgetContainer interiorContainer)
  {
    this.interiorContainer = interiorContainer;
  }

  public HtmlWidgetContainer getInteriorTopContainer()
  {
    interiorTopContainer = new HtmlWidgetContainer();
    String widgetMid = getCursor().getProperty(INTERIOR_TOP_WIDGET_MID_PROPERTY);
    if (widgetMid == null)
    {
      widgetMid = getCursor().getProperty(WIDGET_MID_PROPERTY);
    }
    if (widgetMid != null)
    {
      List<String> interiorTopLayout = getInteriorTopLayout();
      if (interiorTopLayout != null && !interiorTopLayout.isEmpty() && isInteriorTopWidgetRender())
      {
        loadInteriorContainer(interiorTopContainer, interiorTopLayout, widgetMid);
      }
    }
    return interiorTopContainer;
  }

  public void setInteriorTopContainer(HtmlWidgetContainer interiorTopContainer)
  {
    this.interiorTopContainer = interiorTopContainer;
  }
  
  public HtmlWidgetContainer getInteriorBottomContainer()
  {
    interiorBottomContainer = new HtmlWidgetContainer();
    String widgetMid = getCursor().getProperty(INTERIOR_BOTTOM_WIDGET_MID_PROPERTY);
    if (widgetMid == null)
    {
      widgetMid = getCursor().getProperty(WIDGET_MID_PROPERTY);
    }
    if (widgetMid != null)
    {
      List<String> interiorBottomLayout = getInteriorBottomLayout();
      if (interiorBottomLayout != null && !interiorBottomLayout.isEmpty() && isInteriorBottomWidgetRender())
      {
        loadInteriorContainer(interiorBottomContainer, interiorBottomLayout, widgetMid);
      }
    }
    return interiorBottomContainer;
  }

  public void setInteriorBottomContainer(HtmlWidgetContainer interiorBottomContainer)
  {
    this.interiorBottomContainer = interiorBottomContainer;
  }
  
  public HtmlWidgetContainer getCustomContainer()
  {
    return customContainer;
  }

  public void setCustomContainer(HtmlWidgetContainer container)
  {
    this.customContainer = container;
  }

  public Integer getScroll()
  {
    return scroll;
  }

  public void setScroll(Integer scroll)
  {
    this.scroll = scroll;
  }
  
  // columns
  
  public List getHeaderColumns()
  {    
    List list = getCursor().getMultiValuedProperty(HEADER_COLUMNS_PROPERTY);
    return (list.isEmpty() ? Arrays.asList(1) : list);
  }

  public List getHeaderColumnClasses()
  {   
    return getCursor().getMultiValuedProperty(HEADER_COLUMN_CLASSES_PROPERTY);
  }

  public List getHeaderColumnTitles()
  {   
    return getCursor().getMultiValuedProperty(HEADER_COLUMN_TITLES_PROPERTY);
  }

  public List getHeaderColumnRenderAsList()
  {   
    return getCursor().getMultiValuedProperty(HEADER_COLUMN_RENDER_AS_LIST_PROPERTY);
  }

  public List getFooterColumns()
  {
    List list = getCursor().getMultiValuedProperty(FOOTER_COLUMNS_PROPERTY);
    return (list.isEmpty() ? Arrays.asList(1) : list);
  }

  public List getFooterColumnClasses()
  {
    return getCursor().getMultiValuedProperty(FOOTER_COLUMN_CLASSES_PROPERTY);
  }

  public List getFooterColumnTitles()
  {
    return getCursor().getMultiValuedProperty(FOOTER_COLUMN_TITLES_PROPERTY);
  }
  
  public List getFooterColumnRenderAsList()
  {
    return getCursor().getMultiValuedProperty(FOOTER_COLUMN_RENDER_AS_LIST_PROPERTY);
  }

  public List getMainColumns()
  {
    List list = getCursor().getMultiValuedProperty(MAIN_COLUMNS_PROPERTY);
    return (list.isEmpty() ? Arrays.asList(3) : list);
  }

  public List getMainColumnClasses()
  {   
    return getCursor().getMultiValuedProperty(MAIN_COLUMN_CLASSES_PROPERTY);
  }

  public List getMainColumnTitles()
  {   
    return getCursor().getMultiValuedProperty(MAIN_COLUMN_TITLES_PROPERTY);
  }

  public List getMainColumnRenderAsList()
  {   
    return getCursor().getMultiValuedProperty(MAIN_COLUMN_RENDER_AS_LIST_PROPERTY);
  }

  public List getLeftColumns()
  {   
    List list = getCursor().getMultiValuedProperty(LEFT_COLUMNS_PROPERTY);
    return (list.isEmpty() ? Arrays.asList(1) : list);
  }

  public List getLeftColumnClasses()
  {
    return getCursor().getMultiValuedProperty(LEFT_COLUMN_CLASSES_PROPERTY);
  }

  public List getLeftColumnTitles()
  {
    return getCursor().getMultiValuedProperty(LEFT_COLUMN_TITLES_PROPERTY);
  }
  
  public List getLeftColumnRenderAsList()
  {
    return getCursor().getMultiValuedProperty(LEFT_COLUMN_RENDER_AS_LIST_PROPERTY);
  }

  public List getCustomColumns()
  {
    MenuItemCursor cursor = getCursor();
    String sValue = cursor.getProperty(CUSTOM_COLUMNS_PROPERTY);
    Integer value = getInteger(sValue, 3);
    return Arrays.asList(value);
  }

  public List getCustomColumnClasses()
  {
    MenuItemCursor cursor = getCursor();
    String value = cursor.getProperty(CUSTOM_COLUMN_CLASSES_PROPERTY);
    return Arrays.asList(value);
  }

  public List getCustomColumnTitles()
  {
    MenuItemCursor cursor = getCursor();
    String value = cursor.getProperty(CUSTOM_COLUMN_TITLES_PROPERTY);
    return Arrays.asList(value);
  }
  
  public List getCustomColumnRenderAsList()
  {
    MenuItemCursor cursor = getCursor();
    String value = cursor.getProperty(CUSTOM_COLUMN_RENDER_AS_LIST_PROPERTY);
    return Arrays.asList(value);
  }

  public String getCustomDraggableWidgets()
  {
    MenuItemCursor cursor = getCursor();
    String value = cursor.getProperty(CUSTOM_DRAGGABLE_WIDGETS);
    return (value == null ? "true" : value);
  }

  public String getCustomColumnWidgetType()
  {
    //Only if not draggable
    if (Boolean.parseBoolean(getCustomDraggableWidgets())) return null;
    
    MenuItemCursor cursor = getCursor();
    return cursor.getProperty(CUSTOM_COLUMN_WIDGET_TYPE_PROPERTY);
  }

  // layouts
  public List getHeaderLayout()
  {
    return getCursor().getMultiValuedProperty(HEADER_LAYOUT_PROPERTY);    
  }

  public List getFooterLayout()
  {
    return getCursor().getMultiValuedProperty(FOOTER_LAYOUT_PROPERTY);    
  }

  public List getMainLayout()
  {
    return getCursor().getMultiValuedProperty(MAIN_LAYOUT_PROPERTY);    
  }

  public List getLeftLayout()
  {
    return getCursor().getMultiValuedProperty(LEFT_LAYOUT_PROPERTY);    
  }
  
  public List getCustomLayout() 
  {
    String layout = getCustomLayoutCookieValue(); // take layout from cookie
    if (layout == null)
    {
      layout = getDefaultCustomLayout();
    }
    return Arrays.asList(layout);
  }

  public String getDefaultCustomLayout()
  {
    MenuItemCursor cursor = getCursor();
    String defaultLayout = cursor.getProperty(CUSTOM_LAYOUT_PROPERTY);
    return (defaultLayout == null) ? "" : defaultLayout;
  }
  
  public String getInfoPanelUrl()
  {
    MenuItemCursor cursor = getCursor();
    String infoPanelDocId = cursor.getProperty(INFO_PANEL_DOCID_PROPERTY);
    if (infoPanelDocId != null)
      return DOC_SERVLET_URL + infoPanelDocId;
    else
      return null;
  }

  public Map<String, WidgetDefinition> getWidgetDefinitionMap()
  {
    if (widgetDefinitionMap == null)
    {
      widgetDefinitionMap = new HashMap();
      String widgetMid = getCursor().getProperty(WIDGET_MID_PROPERTY);
      if (widgetMid != null)
      {
        MenuModel menuModel =
          UserSessionBean.getCurrentInstance().getMenuModel();
        MenuItemCursor cursor = menuModel.getMenuItem(widgetMid);
        cursor.moveFirstChild();
        while (!cursor.isNull())
        {
          WidgetDefinition widgetDef = new WidgetDefinition(cursor.getMid());
          widgetDefinitionMap.put(widgetDef.getWidgetId(), widgetDef);
          cursor.moveNext();
        }
      }
    }
    return widgetDefinitionMap;
  }
  
  public List<WidgetDefinition> getWidgetCatalogue()
  {
    if (widgetCatalogue == null)
    {
      widgetCatalogue = new ArrayList();
      String widgetMid = getCursor().getProperty(WIDGET_MID_PROPERTY);
      if (widgetMid != null)
      {
        WidgetLayout layout = 
          new WidgetLayout((Integer)getCustomColumns().get(0), 
            (String)getCustomLayout().get(0));

        MenuModel menuModel =
          UserSessionBean.getCurrentInstance().getMenuModel();
        MenuItemCursor cursor = menuModel.getMenuItem(widgetMid);
        cursor.moveFirstChild();
        while (!cursor.isNull())
        {
          WidgetDefinition widgetDef = new WidgetDefinition(cursor.getMid());
          int column = layout.getColumn(widgetDef.getWidgetId());
          widgetDef.setVisible(column != -1);
          widgetDef.setLayoutColumn(column == -1 ? 0 : column);
          if (widgetDef.isInCatalogue())
          {
            widgetCatalogue.add(widgetDef);
          }
          cursor.moveNext();
        }        
      }
    }
    return widgetCatalogue;
  }

  public void setWidgetCatalogue(List<WidgetDefinition> widgetCatalogue)
  {
    this.widgetCatalogue = widgetCatalogue;
  }

  public String getCustomLayoutCookieName()
  {
    MenuItemCursor cursor = getCursor();
    String cookieName = cursor.getProperty(COOKIE_NAME_PROPERTY);
    return (cookieName == null) ? "custom_layout" : cookieName;
  }

  public String getLastVisitCookieName()
  {
    return getCustomLayoutCookieName() + "_last_visit";
  }
  
  public String getScripts()
  {
    return
    "<script type=\"text/javascript\" src=\"/plugins/widget/widgetfold.js?v=" + 
      ApplicationBean.getCurrentInstance().getResourcesVersion() + "\">" +
    "</script>" +      
    "<script type=\"text/javascript\">" +
    "function onUpdate(portal) {" +
    "var serialization = portal.serialize();" +
    "var expireDate = new Date();" +
    "expireDate.setDate(expireDate.getDate() + " + EXPIRE_DAYS + ");" +
    "document.cookie=\"" + getCustomLayoutCookieName() +
    "=\" + escape(serialization) + \";expires" +
    "=\" + expireDate.toUTCString() + \";path=/\";}\n" +
    "function removeWidget(widgetId) {" +
    "var widget = $(widgetId).widget;" +
    "if (widget) {" +
    "portal_custom_container.remove(widget); " +
    "if (typeof(unselectWidget) === 'function') {" +   
    "unselectWidget(widgetId); " +
    "}}}\n" +
    "function foldWidget(widgetId) {" +
    "doFoldWidget(widgetId, '" + getWidgetStateCookieName() + "');" +
    "var widget = $(widgetId).widget;" +
    "if (widget) widget.updateHeight();" +
    "portal_custom_container._updateColumnsHeight();" +
    "}\n" +
    "function foldStandaloneWidget(widgetId) {" +
    "doFoldWidget(widgetId, '" + getWidgetStateCookieName() + "');" +
    "}\n" +    
    "makeFoldWidgetIconsVisible(" + TextUtils.listToJSArray(getLayoutWidgetIds(false)) + ");" +  
    "</script>";    
  }
  
  //INTERIOR

  public boolean isInteriorWidgetRender()
  {
    String interiorWidgetRender = 
      getCursor().getBrowserSensitiveProperty(INTERIOR_WIDGET_RENDER_PROPERTY);
    return ("true".equals(interiorWidgetRender));
  }
  
  public List getInteriorColumns()
  {
    List list = getCursor().getBrowserSensitiveMultiValuedProperty(INTERIOR_COLUMNS_PROPERTY);
    return (list.isEmpty() ? Arrays.asList(3) : list);
  }
  
  public List getInteriorColumnClasses()
  {
    return getCursor().getBrowserSensitiveMultiValuedProperty(INTERIOR_COLUMN_CLASSES_PROPERTY);
  }
  
  public List getInteriorColumnTitles()
  {
    return getCursor().getBrowserSensitiveMultiValuedProperty(INTERIOR_COLUMN_TITLES_PROPERTY);
  }

  public List getInteriorColumnRenderAsList()
  {
    return getCursor().getBrowserSensitiveMultiValuedProperty(INTERIOR_COLUMN_RENDER_AS_LIST_PROPERTY);
  }

  public List getInteriorLayout()
  {
    return getCursor().getBrowserSensitiveMultiValuedProperty(INTERIOR_LAYOUT_PROPERTY);
  }

  public List getInteriorTitle()
  {
    return getCursor().getBrowserSensitiveMultiValuedProperty(INTERIOR_TITLE_PROPERTY, false);    
  }
  
  public List getInteriorHeaderDocId()
  {
    return getCursor().getBrowserSensitiveMultiValuedProperty(INTERIOR_HEADER_DOC_ID_PROPERTY, false);    
  }
  
  public List getInteriorFooterDocId()
  {
    return getCursor().getBrowserSensitiveMultiValuedProperty(INTERIOR_FOOTER_DOC_ID_PROPERTY, false);    
  }
  
  //INTERIOR_TOP
  
  public boolean isInteriorTopWidgetRender()
  {
    String interiorTopWidgetRender = 
      getCursor().getBrowserSensitiveProperty(INTERIOR_TOP_WIDGET_RENDER_PROPERTY);
    return ("true".equals(interiorTopWidgetRender));
  }
  
  public List getInteriorTopColumns()
  {
    List list = getCursor().getBrowserSensitiveMultiValuedProperty(INTERIOR_TOP_COLUMNS_PROPERTY);
    return (list.isEmpty() ? Arrays.asList(2) : list);    
  }
  
  public List getInteriorTopColumnClasses()
  {
    return getCursor().getBrowserSensitiveMultiValuedProperty(INTERIOR_TOP_COLUMN_CLASSES_PROPERTY);
  }
  
  public List getInteriorTopColumnTitles()
  {
    return getCursor().getBrowserSensitiveMultiValuedProperty(INTERIOR_TOP_COLUMN_TITLES_PROPERTY);
  }

  public List getInteriorTopColumnRenderAsList()
  {
    return getCursor().getBrowserSensitiveMultiValuedProperty(INTERIOR_TOP_COLUMN_RENDER_AS_LIST_PROPERTY);
  }

  public List getInteriorTopLayout()
  {
    return getCursor().getBrowserSensitiveMultiValuedProperty(INTERIOR_TOP_LAYOUT_PROPERTY);
  }

  public List getInteriorTopTitle()
  {
    return getCursor().getBrowserSensitiveMultiValuedProperty(INTERIOR_TOP_TITLE_PROPERTY, false);    
  }
  
  public List getInteriorTopHeaderDocId()
  {
    return getCursor().getBrowserSensitiveMultiValuedProperty(INTERIOR_TOP_HEADER_DOC_ID_PROPERTY, false);    
  }
  
  public List getInteriorTopFooterDocId()
  {
    return getCursor().getBrowserSensitiveMultiValuedProperty(INTERIOR_TOP_FOOTER_DOC_ID_PROPERTY, false);    
  }

  //INTERIOR_BOTTOM
  
  public boolean isInteriorBottomWidgetRender()
  {
    String interiorBottomWidgetRender = 
      getCursor().getBrowserSensitiveProperty(INTERIOR_BOTTOM_WIDGET_RENDER_PROPERTY);
    return ("true".equals(interiorBottomWidgetRender));
  }
  
  public List getInteriorBottomColumns()
  {
    List list = getCursor().getBrowserSensitiveMultiValuedProperty(INTERIOR_BOTTOM_COLUMNS_PROPERTY);
    return (list.isEmpty() ? Arrays.asList(2) : list);    
  }
  
  public List getInteriorBottomColumnClasses()
  {
    return getCursor().getBrowserSensitiveMultiValuedProperty(INTERIOR_BOTTOM_COLUMN_CLASSES_PROPERTY);
  }

  public List getInteriorBottomColumnTitles()
  {
    return getCursor().getBrowserSensitiveMultiValuedProperty(INTERIOR_BOTTOM_COLUMN_TITLES_PROPERTY);
  }
  
  public List getInteriorBottomColumnRenderAsList()
  {
    return getCursor().getBrowserSensitiveMultiValuedProperty(INTERIOR_BOTTOM_COLUMN_RENDER_AS_LIST_PROPERTY);
  }
  
  public List getInteriorBottomLayout()
  {
    return getCursor().getBrowserSensitiveMultiValuedProperty(INTERIOR_BOTTOM_LAYOUT_PROPERTY);
  }

  public List getInteriorBottomTitle()
  {
    return getCursor().getBrowserSensitiveMultiValuedProperty(INTERIOR_BOTTOM_TITLE_PROPERTY, false);    
  }
  
  public List getInteriorBottomHeaderDocId()
  {
    return getCursor().getBrowserSensitiveMultiValuedProperty(INTERIOR_BOTTOM_HEADER_DOC_ID_PROPERTY, false);    
  }
  
  public List getInteriorBottomFooterDocId()
  {
    return getCursor().getBrowserSensitiveMultiValuedProperty(INTERIOR_BOTTOM_FOOTER_DOC_ID_PROPERTY, false);    
  }
  
  // ****************** actions ********************

  @CMSAction
  public String show()
  {
    // add new widgets from date
    Date lastVisit = getLastVisitCookieValue();
    Date now = new Date();
    addNewWidgets(lastVisit, now);
    setLastVisitCookieValue(now);
    
    // Map<widgetId, container>
    Map<String, HtmlWidgetContainer> containers = prepareContainers();
    
    String widgetMid = getCursor().getProperty(WIDGET_MID_PROPERTY);
    if (widgetMid != null)
    {
      MenuModel menuModel = UserSessionBean.getCurrentInstance().getMenuModel();
      MenuItemCursor cursor = menuModel.getMenuItem(widgetMid);
      cursor.moveFirstChild();
      while (!cursor.isNull())
      {
        WidgetDefinition widgetDef = new WidgetDefinition(cursor.getMid());
        String widgetId = widgetDef.getWidgetId();
        HtmlWidgetContainer container = containers.get(widgetId);
        if (container != null)
        {
          HtmlWidget widget = buildWidget(widgetDef);
          container.getChildren().add(widget);
        }
        cursor.moveNext();
      }
    }
    return "widget_container";
  }

  public String setup()
  {
    return "widget_setup";
  }
  
  public String sort()
  {
    if (getCustomColumnWidgetType() == null)
    {
      String customLayout = (String)getCustomLayout().get(0);
      WidgetLayout layout = new WidgetLayout(customLayout);
      List<String> widgetIds = layout.getWidgetIds();
      int columnCount = (Integer)getCustomColumns().get(0);
      layout = new WidgetLayout(columnCount);
      int column = 0;
      for (String widgetId : widgetIds)
      {
        layout.getWidgetIds(column).add(widgetId);
        column = (column + 1) % columnCount;
      }
      setCustomLayoutCookieValue(layout.toString());    
    }
    return null;
  }

  public String reset()
  {
    setCustomLayoutCookieValue(getDefaultCustomLayout());
    widgetCatalogue = null;
    return show();
  }
  
  public String apply()
  {
    String[] customColumnWidgetTypes = new String[0];
    if (getCustomColumnWidgetType() != null)
    {
      customColumnWidgetTypes = getCustomColumnWidgetType().split(",");
    }    
    // update layout
    int customColumns = (Integer)getCustomColumns().get(0);
    WidgetLayout layout = new WidgetLayout(customColumns, (String)getCustomLayout().get(0));
    for (WidgetDefinition widget : widgetCatalogue)
    {
      String widgetId = widget.getWidgetId();
      int oldColumn = layout.getColumn(widgetId);

      if (widget.isVisible())
      {
        Integer newColumn;        
        if (getCustomColumnWidgetType() == null)
        {
          newColumn = widget.getLayoutColumn();
        }
        else
        {
          if (oldColumn < 0) //new widget
          {
            newColumn = getEmptiestLayoutColumn(layout, widget, customColumnWidgetTypes);            
          }
          else //existing widget
          {
            newColumn = oldColumn;
          }
        }
        if (newColumn < 0) newColumn = 0;
        else if (newColumn >= customColumns) newColumn = customColumns - 1;
        if (oldColumn != newColumn)
        {
          List<String> widgetIds = layout.getWidgetIds(newColumn);
          widgetIds.add(0, widgetId);
          if (oldColumn != -1)
          {
            layout.getWidgetIds(oldColumn).remove(widgetId);
          }
        }
      }
      else
      {
        if (oldColumn > -1)
          layout.getWidgetIds(oldColumn).remove(widgetId);
      }
    }

    setCustomLayoutCookieValue(layout.toString());    

    // create components inside HtmlWidgetContainer
    return show();
  }
  
  public String getWidgetSelectorWindow()
  {
    MenuItemCursor cursor = getCursor();
    String value = cursor.getProperty(WIDGET_SELECTOR_WINDOW_PROPERTY);
    return (value == null) ? DEFAULT_WIDGET_SELECTOR_WINDOW : value;
  }
    
  public String getWidgetSelectorItemsHtml()
  {
    StringBuilder sb = new StringBuilder();
    int index = 1;
    for (WidgetDefinition widgetDef : getWidgetCatalogue())
    {
      sb.append(getWidgetSelectorItemHtml(widgetDef, index++));
    }
    return sb.toString();
  }

  private String getWidgetSelectorItemHtml(WidgetDefinition widgetDef, 
    int index)
  {
    StringBuilder sb = new StringBuilder();
    sb.append("<div ");
    sb.append("style=\"visibility: hidden;\" ");
    if (widgetDef.isVisible())
    {
      sb.append("class=\"item selected\" ");
    }
    else
    {
      sb.append("class=\"item\" ");
    }

    String trWidgetLabel = translatePlainText(widgetDef.getLabel());
    String trAriaLabel = translatePlainText("Activar/Desactivar widget: ") + 
      trWidgetLabel;

    sb.append("id=\"widgetSelectorItem" + index + "\">");
    sb.append("<a ");    
    sb.append("id=\"" + "widget_selector_" + widgetDef.getWidgetId() + "\" ");
    sb.append("class=\"widgetLink\" ");
    sb.append("title=\"" + trAriaLabel + "\" ");
    sb.append("onclick=\"return(switchWidget('" + widgetDef.getWidgetId() + 
      "', '" + getCustomLayoutCookieName() + "'));\" ");
    sb.append("href=\"#\">");

    sb.append("<img ");
    sb.append("alt=\"" + trAriaLabel + "\" ");
    sb.append("class=\"widgetIcon\" ");
    sb.append("src=\"" + widgetDef.getIconUrl() + "\" />");

    sb.append("<div ");    
    sb.append("class=\"widgetLabel\">");
    sb.append(trWidgetLabel);
    sb.append("</div>");

    sb.append("</a>");    

    sb.append("</div>");
    
    return sb.toString();
  }

  public String getWidgetSelectorScripts()
  {
    return "<script type=\"text/javascript\" " +
    "src=\"/plugins/widget/widgetselector.js\"></script>" +
    "<script type=\"text/javascript\">" +
    "var widgetSelectorWindow = " + getWidgetSelectorWindow() + ";" +
    "var widgetSelectorSize = " + getWidgetCatalogue().size() + ";" +
    "var widgetSelectorIndexCookieName = '" +
    getWidgetSelectorIndexCookieName() + "';" +
    "widgetSelectorMove(" + getWidgetSelectorIndexCookieValue() + ");" +
    "widgetSelectorMakeElementsVisible();" +  
    "</script>";
  }
  
  private boolean isWidgetFolded(String widgetId)
  {    
    String cookieValue = getWidgetStateCookieValue();
    if (cookieValue != null)
    {
      for (String item : cookieValue.split(","))
      {
        if (item.contains("="))
        {
          String itemWidgetId = item.split("=")[0];
          if (itemWidgetId.equals(widgetId))
          {
            String state = item.split("=")[1];
            return "f".equals(state);
          }
        }
      }
    }
    return false;
  }
  
  private HtmlWidget buildWidget(WidgetDefinition widgetDef)
  {
    return buildWidget(widgetDef, false);
  }  
  
  private HtmlWidget buildWidget(WidgetDefinition widgetDef, boolean standalone)
  {
    // build widget
    HtmlWidget htmlWidget = new HtmlWidget();
    htmlWidget.setId(widgetDef.getWidgetId());
    htmlWidget.setStyle(widgetDef.getStyle());
    htmlWidget.setStyleClass(widgetDef.getStyleClass());
    htmlWidget.setExternalTitle(widgetDef.getExternalTitle());
    htmlWidget.setAriaHidden(widgetDef.isAriaHidden());
    htmlWidget.setContentType(widgetDef.getContentType());
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    if (userSessionBean.isAdministrator())
    {
      try
      {
        String mid = widgetDef.getMid();
        userSessionBean.getMenuModel().getMenuItemByMid(mid); //check if visible
        htmlWidget.getAttributes().put("nodeId", mid);            
      }
      catch (Exception ex) //non visible widget
      {

      }
    }
    boolean folded = isWidgetFolded(widgetDef.getWidgetId());
    htmlWidget.getAttributes().put("folded", folded);    

    // build widget header
    String headerBuilderName = widgetDef.getHeaderBuilder();
    if (headerBuilderName != null)
    {
      WidgetBuilder widgetBuilder =
        WidgetBuilder.getInstance(headerBuilderName);
      if (widgetBuilder != null)
      {
        widgetBuilder.setFolded(folded);
        widgetBuilder.setStandalone(standalone);
        UIComponent component = widgetBuilder.getComponent(
          widgetDef, getFacesContext());
        if (component.getId() == null)
          component.setId(widgetDef.getWidgetId() + "_header");
        htmlWidget.getFacets().put("header", component);
      }
    }

    // build widget content
    String contentBuilderName = widgetDef.getContentBuilder();
    if (contentBuilderName != null)
    {
      WidgetBuilder widgetBuilder =
        WidgetBuilder.getInstance(contentBuilderName);        
      if (widgetBuilder != null)
      {
        widgetBuilder.setFolded(folded);
        widgetBuilder.setStandalone(standalone);        
        UIComponent component = widgetBuilder.getComponent(
          widgetDef, getFacesContext());        
        if (component.getId() == null)
          component.setId(widgetDef.getWidgetId() + "_content");
        String scrollerStyle = widgetDef.getScrollerStyle();
        if (scrollerStyle != null)
        {
          Div div = new Div();
          div.setId(widgetDef.getWidgetId() + "_scroller");
          div.setStyle(scrollerStyle);
          div.getChildren().add(component);
          htmlWidget.getChildren().add(div);
        }
        else htmlWidget.getChildren().add(component);
      }
    }

    // build widget footer
    String footerBuilderName = widgetDef.getFooterBuilder();
    if (footerBuilderName != null)
    {
      WidgetBuilder widgetBuilder =
        WidgetBuilder.getInstance(footerBuilderName);
      if (widgetBuilder != null)
      {
        widgetBuilder.setFolded(folded);
        widgetBuilder.setStandalone(standalone);        
        UIComponent component = widgetBuilder.getComponent(
          widgetDef, getFacesContext());
        if (component.getId() == null)
          component.setId(widgetDef.getWidgetId() + "_footer");
        htmlWidget.getFacets().put("footer", component);
      }
    }
    return htmlWidget;
  }

  private Map<String, HtmlWidgetContainer> prepareContainers()
  {
    if (headerContainer == null) headerContainer = new HtmlWidgetContainer();
    else headerContainer.getChildren().clear();

    if (mainContainer == null) mainContainer = new HtmlWidgetContainer();
    else mainContainer.getChildren().clear();

    if (leftContainer == null) leftContainer = new HtmlWidgetContainer();
    else leftContainer.getChildren().clear();

    if (customContainer == null) customContainer = new HtmlWidgetContainer();
    else customContainer.getChildren().clear();

    if (footerContainer == null) footerContainer = new HtmlWidgetContainer();
    else footerContainer.getChildren().clear();

    Map<String, HtmlWidgetContainer> containers = new HashMap();

    addWidgetIds(containers, getCustomLayout(), customContainer);
    addWidgetIds(containers, getFooterLayout(), footerContainer);
    addWidgetIds(containers, getHeaderLayout(), headerContainer);
    addWidgetIds(containers, getMainLayout(), mainContainer);
    addWidgetIds(containers, getLeftLayout(), leftContainer);    

    return containers;  
  }
 
  private void addWidgetIds(Map containers,
    List<String> layoutList, HtmlWidgetContainer container)
  {    
    if (layoutList != null)
    {
      for (String layout : layoutList)
      {
        WidgetLayout widgetLayout = new WidgetLayout(layout);
        for (String widgetId : widgetLayout.getWidgetIds())
        {
          containers.put(widgetId, container);
        }
      }
    }
  }
  
  private String getCustomLayoutCookieValue()
  {
    String layoutValue = null;
    Cookie cookie = getCookie(getCustomLayoutCookieName());
    if (cookie != null)
    {
      layoutValue = cookie.getValue();
      layoutValue = layoutValue.replaceAll("%2C", ",");
      layoutValue = layoutValue.replaceAll("%7C", "|");
    }
    return layoutValue;
  }

  private void setCustomLayoutCookieValue(String layoutValue)
  {
    layoutValue = layoutValue.replaceAll("\\,", "%2C");
    layoutValue = layoutValue.replaceAll("\\|", "%7C");

    String cookieName = getCustomLayoutCookieName();
    Cookie cookie = getCookie(cookieName);
    if (cookie == null)
    {
      cookie = new Cookie(cookieName, layoutValue);
    }
    else
    {
      cookie.setValue(layoutValue);
    }
    cookie.setPath("/");
    cookie.setMaxAge(EXPIRE_DAYS * 24 * 3600);
    setCookie(cookie);
  }

  private void setLastVisitCookieValue(Date date)
  {
    String cookieName = getLastVisitCookieName();
    SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
    String dateString = df.format(date);
    Cookie cookie = getCookie(cookieName);
    if (cookie == null)
    {
      cookie = new Cookie(cookieName, dateString);
    }
    else
    {
      cookie.setValue(dateString);
    }
    cookie.setPath("/");
    cookie.setMaxAge(EXPIRE_DAYS * 24 * 3600);
    setCookie(cookie);
  }

  private Date getLastVisitCookieValue()
  {
    String dateString = null;
    Cookie cookie = getCookie(getLastVisitCookieName());
    if (cookie != null)
    {
      dateString = cookie.getValue();
    }
    Date date;
    SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
    if (dateString == null)
    {
      date = new Date(0);
    }
    else
    {
      try
      {
        date = df.parse(dateString);
      }
      catch (ParseException ex)
      {
        date = new Date(0);
      }
    }
    return date;
  }
  
  private String getWidgetStateCookieName()
  {
    return getCustomLayoutCookieName() + "_widget_state";
  }
  
  private String getWidgetStateCookieValue()
  {
    String cookieName = getWidgetStateCookieName();
    Cookie cookie = getCookie(cookieName);
    if (cookie != null)
    {
      String value = cookie.getValue();
      value = value.replaceAll("%2C", ",");
      value = value.replaceAll("%3D", "=");
      return value;
    }
    return null;
  }

  private String getWidgetSelectorIndexCookieName()
  {
    return getCustomLayoutCookieName() + "_widget_selector_index";
  }
  
  private String getWidgetSelectorIndexCookieValue()
  {
    String cookieName = getWidgetSelectorIndexCookieName();
    Cookie cookie = getCookie(cookieName);
    if (cookie == null)
    {
      cookie = new Cookie(cookieName, "1");
      cookie.setPath("/");
      cookie.setMaxAge(1800); //30 minutes
      setCookie(cookie);      
    }
    return cookie.getValue();
  }
    
  private Cookie getCookie(String name)
  {
    initCookieMap();
    return (Cookie)cookieMap.get(name);
  }

  private void setCookie(Cookie cookie)
  {
    initCookieMap();
    cookieMap.put(cookie.getName(), cookie);
    HttpServletResponse response =
     (HttpServletResponse)getExternalContext().getResponse();
    response.addCookie(cookie);
  }

  private void initCookieMap()
  {
    if (cookieMap == null)
    {
      cookieMap = new HashMap();
      FacesContext context = FacesContext.getCurrentInstance();
      cookieMap.putAll(context.getExternalContext().getRequestCookieMap());
    }
  }

  private MenuItemCursor getCursor()
  {
    MenuItemCursor cursor =
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
    return cursor;
  }

  private Integer getInteger(String value, Integer defaultValue)
  {
    Integer intValue;
    if (value == null)
    {
      intValue = defaultValue;
    }
    else
    {
      try
      {
        intValue = new Integer(value);
      }
      catch (NumberFormatException ex)
      {
        intValue = defaultValue;
      }
    }
    return intValue;
  }

  private void addNewWidgets(Date lastVisit, Date now)
  {
    String widgetMid = getCursor().getProperty(WIDGET_MID_PROPERTY);
    if (widgetMid != null)
    {
      WidgetLayout layout =
        new WidgetLayout((Integer)getCustomColumns().get(0), 
          (String)getCustomLayout().get(0));

      String[] customColumnWidgetTypes = new String[0];
      if (getCustomColumnWidgetType() != null)
      {
        customColumnWidgetTypes = getCustomColumnWidgetType().split(",");
        formatCustomLayout(layout, customColumnWidgetTypes);
      }

      MenuModel menuModel = UserSessionBean.getCurrentInstance().getMenuModel();
      MenuItemCursor cursor = menuModel.getMenuItem(widgetMid);
      cursor.moveFirstChild();
      while (!cursor.isNull())
      {
        WidgetDefinition widgetDef = new WidgetDefinition(cursor.getMid());
        Date addSince = widgetDef.getAddSince();
        if (addSince != null)
        {
          if (lastVisit.before(addSince) && addSince.before(now))
          {
            String widgetId = widgetDef.getWidgetId();
            if (layout.getColumn(widgetId) == -1)
            {
              int columnIndex = getEmptiestLayoutColumn(layout, widgetDef, 
                customColumnWidgetTypes);
              layout.getWidgetIds(columnIndex).add(0, widgetId);              
            }
          }
        }
        cursor.moveNext();
      }
      setCustomLayoutCookieValue(layout.toString());
    }
  }

  private void formatCustomLayout(WidgetLayout layout, String[] customColumnWidgetTypes)
  {
    if (customColumnWidgetTypes == null || customColumnWidgetTypes.length == 0)  
      return;
  
    for (int iCol = 0; iCol < layout.getColumns(); iCol++)
    {
      List<String> finalColWidgetIds = new ArrayList();
      for (String widgetId : layout.getWidgetIds(iCol))
      {
        WidgetDefinition widgetDef = getWidgetDefinition(widgetId);
        if (widgetDef != null)
        {
          if (isWidgetAllowedInColumn(widgetDef, iCol, customColumnWidgetTypes))
          {
            finalColWidgetIds.add(widgetId);
          }
          else
          {
            //Search for a valid column, if any            
            Integer newCol = getEmptiestLayoutColumn(layout, widgetDef, customColumnWidgetTypes);
            if (newCol != null)
            {
              layout.getWidgetIds(newCol).add(0, widgetId);
            }
          }
        }
      }
      layout.getWidgetIds(iCol).clear();
      layout.getWidgetIds(iCol).addAll(finalColWidgetIds);
    }
  }  
  
  private WidgetDefinition getWidgetDefinition(String widgetId)
  {    
    return getWidgetDefinitionMap().get(widgetId);
  }
  
  private Integer getEmptiestLayoutColumn(WidgetLayout layout, 
    WidgetDefinition widgetDef, String[] columnWidgetTypes)
  {
    int minCount = Integer.MAX_VALUE;
    Integer minColumn = null;
    for (int i = 0; i < layout.getColumns(); i++)
    {
      boolean allow = isWidgetAllowedInColumn(widgetDef, i, columnWidgetTypes);
      if (allow && layout.getWidgetIds(i).size() < minCount)
      {
        minCount = layout.getWidgetIds(i).size();
        minColumn = i;
      }
    }
    return minColumn;
  }
  
  private boolean isWidgetAllowedInColumn(WidgetDefinition widgetDef, 
    int colIdx, String[] columnWidgetTypes)
  {
    return (
      (colIdx >= columnWidgetTypes.length) ||
      ("all".equals(columnWidgetTypes[colIdx])) || 
      (widgetDef.isBannerWidget() && "banner".equals(columnWidgetTypes[colIdx])) ||
      (!widgetDef.isBannerWidget() && "nonbanner".equals(columnWidgetTypes[colIdx]))
    );    
  }
  
  private String translatePlainText(String text)
  {
    if (text == null || text.trim().isEmpty()) return "";
    
    Translator translator =
      ApplicationBean.getCurrentInstance().getTranslator();

    if (translator != null)
    {
      try
      {
        String userLanguage = FacesUtils.getViewLanguage();
        StringWriter sw = new StringWriter();
        translator.translate(new StringReader(text), sw, "text/plain", 
          userLanguage, "widgetLabel");
        return sw.toString();
      }
      catch (Exception ex)
      {
      }
    }
    return text;
  }
    
  private List<String> getLayoutWidgetIds(boolean includeDynamic)
  {
    List<String> result = new ArrayList();
    result.addAll(getWidgetIdListFromLayout(getHeaderLayout()));
    result.addAll(getWidgetIdListFromLayout(getLeftLayout()));
    result.addAll(getWidgetIdListFromLayout(getMainLayout()));
    result.addAll(getWidgetIdListFromLayout(getFooterLayout()));
    result.addAll(getWidgetIdListFromLayout(getInteriorLayout()));
    result.addAll(getWidgetIdListFromLayout(getInteriorTopLayout()));
    result.addAll(getWidgetIdListFromLayout(getInteriorBottomLayout()));    
    if (includeDynamic) 
    {
      result.addAll(getWidgetIdListFromLayout(getCustomLayout()));
    }      
    return result;
  }

  private List<String> getWidgetIdListFromLayout(List<String> layoutList)
  {
    List<String> result = new ArrayList();
    for (String layout : layoutList)
    {
      for (String group : layout.split("#"))
      {
        String groupLayout = group;
        if (group.contains(":"))
        {
          groupLayout = group.split(":")[1];
        }
        if (groupLayout != null && !groupLayout.isEmpty())
        {
          for (String widgetColumn : groupLayout.split("\\|"))
          {
            for (String widgetId : widgetColumn.split(","))
            {
              result.add(widgetId);
            }          
          }
        }
      }      
    }
    return result;
  }
  
  private void loadInteriorContainer(HtmlWidgetContainer container, 
    List<String> layout, String widgetMid)
  {
    List<String> widgetIdList = getWidgetIdListFromLayout(layout);
    MenuModel menuModel = 
      UserSessionBean.getCurrentInstance().getMenuModel();
    MenuItemCursor rootWidgetCursor = menuModel.getMenuItem(widgetMid);
    searchAndBuildWidgets(container, widgetIdList, rootWidgetCursor);
  }
  
  private void searchAndBuildWidgets(HtmlWidgetContainer widgetContainer, 
    List widgetIdList, MenuItemCursor rootCursor)
  {    
    MenuItemCursor auxCursor = rootCursor.getFirstChild();   
    while (!auxCursor.isNull())
    {
      WidgetDefinition widgetDef = new WidgetDefinition(auxCursor.getMid());
      if (widgetIdList.contains(widgetDef.getWidgetId()))
      {
        HtmlWidget widget = buildWidget(widgetDef, true);
        widgetContainer.getChildren().add(widget);
        widgetIdList.remove(widgetDef.getWidgetId());
        if (widgetIdList.isEmpty()) return;
      }
      auxCursor = auxCursor.getNext();      
    }
    //Not all widgets have been built yet
    auxCursor = rootCursor.getFirstChild();    
    while (!auxCursor.isNull())
    {
      searchAndBuildWidgets(widgetContainer, widgetIdList, auxCursor);
      if (widgetIdList.isEmpty()) return;
      auxCursor = auxCursor.getNext();
    }      
  }
  
  private void logToFile(String s)
  {
    try 
    {
      FileWriter fw = new FileWriter("c://widgets_log.txt", true);
      BufferedWriter bw = new BufferedWriter(fw);
      PrintWriter out = new PrintWriter(bw);      
      out.println(s);
      out.close();
    }
    catch (IOException ex) 
    {
      // Report
    } 
    finally 
    {
    }
  }
}

