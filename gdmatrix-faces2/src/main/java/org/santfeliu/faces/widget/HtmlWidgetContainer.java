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
package org.santfeliu.faces.widget;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.el.ValueExpression;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.servlet.http.HttpServletRequest;
import org.apache.myfaces.shared_tomahawk.renderkit.JSFAttr;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.Translator;
import org.santfeliu.faces.browser.HtmlBrowser;
import org.santfeliu.web.ApplicationBean;
import org.santfeliu.web.HttpUtils;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author realor
 */

@FacesComponent(value = "HtmlWidgetContainer")
public class HtmlWidgetContainer extends UIPanel
{
  private static final String JAVASCRIPT_WIDGET_ENCODED =
    "JAVASCRIPT_WIDGET_ECONDED";

  private static final String DOC_SERVLET_PATH = "/documents/";
  private static final String NONE_VALUE = "none";
  
  private List _columns;
  private List _columnClasses;
  private List _columnTitles;
  private List _columnRenderAsList;
  private List _layout;
  private String _style;
  private String _styleClass;
  private Boolean _dynamic;
  private Boolean _draggableWidgets;
  private String _updateCallback;
  private List _title;
  private List _headerDocId;
  private List _footerDocId;
  private Translator _translator;
  private String _translationGroup;
  
  private Map<String, HtmlWidget> widgetMap; // <widgetId, HtmlWidget>

  public HtmlWidgetContainer()
  {
    setRendererType(null);
  }

  @Override
  public String getFamily()
  {
    return "WidgetContainer";
  }

  @Override
  public boolean getRendersChildren()
  {
    return true;
  }
  
  public void setColumns(List columns)
  {
    this._columns = columns;
  }

  public List getColumns()
  {
    if (_columns != null) return _columns;
    ValueExpression ve = getValueExpression("columns");
    if (ve != null)
    {
      Object value = ve.getValue(getFacesContext().getELContext());
      return (value instanceof List ? (List)value : Arrays.asList(value));
    }
    else return null;
  }
  
  public void setColumnClasses(List columnClasses)
  {
    this._columnClasses = columnClasses;
  }

  public List getColumnClasses()
  {
    if (_columnClasses != null) return _columnClasses;
    ValueExpression ve = getValueExpression("columnClasses");
    if (ve != null)
    {
      Object value = ve.getValue(getFacesContext().getELContext());
      return (value instanceof List ? (List)value : Arrays.asList(value));
    }
    else return null;
  }
  
  public void setColumnTitles(List columnTitles)
  {
    this._columnTitles = columnTitles;
  }

  public List getColumnTitles()
  {
    if (_columnTitles != null) return _columnTitles;
    ValueExpression ve = getValueExpression("columnTitles");
    if (ve != null)
    {
      Object value = ve.getValue(getFacesContext().getELContext());
      return (value instanceof List ? (List)value : Arrays.asList(value));
    }
    else return null;
  }  

  public void setColumnRenderAsList(List _columnRenderAsList)
  {
    this._columnRenderAsList = _columnRenderAsList;
  }

  public List getColumnRenderAsList()
  {
    if (_columnRenderAsList != null) return _columnRenderAsList;
    ValueExpression ve = getValueExpression("columnRenderAsList");
    if (ve != null)
    {
      Object value = ve.getValue(getFacesContext().getELContext());
      return (value instanceof List ? (List)value : Arrays.asList(value));
    }
    else return null;
  }
  
  public void setLayout(List layout)
  {
    this._layout = layout;
  }

  public List getLayout()
  {
    if (_layout != null) return _layout;
    ValueExpression ve = getValueExpression("layout");
    if (ve != null)
    {
      Object value = ve.getValue(getFacesContext().getELContext());
      return (value instanceof List ? (List)value : Arrays.asList(value));
    }
    else return null;
  }
  
  public void setStyle(String style)
  {
    this._style = style;
  }

  public String getStyle()
  {
    if (_style != null) return _style;
    ValueExpression ve = getValueExpression("style");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
    }

  public void setStyleClass(String styleClass)
  {
    this._styleClass = styleClass;
  }

  public String getStyleClass()
  {
    if (_styleClass != null) return _styleClass;
    ValueExpression ve = getValueExpression("styleClass");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }  
  
  public void setDynamic(Boolean _dynamic)
  {
    this._dynamic = _dynamic;
  }

  public Boolean getDynamic()
  {
    if (_dynamic != null) return _dynamic;
    ValueExpression ve = getValueExpression("dynamic");
    return ve != null ? (Boolean)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setDraggableWidgets(Boolean _draggableWidgets)
  {
    this._draggableWidgets = _draggableWidgets;
  }

  public Boolean getDraggableWidgets()
  {
    if (_draggableWidgets != null) return _draggableWidgets;
    ValueExpression ve = getValueExpression("draggableWidgets");
    //return vb != null ? Boolean.parseBoolean((String)vb.getValue(getFacesContext())) : Boolean.FALSE;
    return ve != null ? (Boolean)ve.getValue(getFacesContext().getELContext()) : null;    
  }

  public void setUpdateCallback(String updateCallback)
  {
    this._updateCallback = updateCallback;
  }

  public String getUpdateCallback()
  {
    if (_updateCallback != null) return _updateCallback;
    ValueExpression ve = getValueExpression("updateCallback");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public List getTitle()
  {
    if (_title != null) return _title;
    ValueExpression ve = getValueExpression("title");
    if (ve != null)
    {
      Object value = ve.getValue(getFacesContext().getELContext());
      return (value instanceof List ? (List)value : Arrays.asList(value));
    }
    else return null;
  }

  public void setTitle(List title)
  {
    this._title = title;
  }
  
  public List getHeaderDocId()
  {
    if (_headerDocId != null) return _headerDocId;
    ValueExpression ve = getValueExpression("headerDocId");
    if (ve != null)
    {
      Object value = ve.getValue(getFacesContext().getELContext());
      return (value instanceof List ? (List)value : Arrays.asList(value));
    }
    else return null;
  }

  public void setHeaderDocId(List headerDocId)
  {
    this._headerDocId = headerDocId;
  }
  
  public List getFooterDocId()
  {
    if (_footerDocId != null) return _footerDocId;
    ValueExpression ve = getValueExpression("footerDocId");
    if (ve != null)
    {
      Object value = ve.getValue(getFacesContext().getELContext());
      return (value instanceof List ? (List)value : Arrays.asList(value));
    }
    else return null;
  }

  public void setFooterDocId(List footerDocId)
  {
    this._footerDocId = footerDocId;
  }
  
  public Translator getTranslator()
  {
    if (_translator != null)
      return _translator;
    ValueExpression ve = getValueExpression("translator");
    return ve != null? (Translator) ve.getValue(getFacesContext().getELContext()): null;
  }

  public void setTranslator(Translator _translator)
  {
    this._translator = _translator;
  }

  public void setTranslationGroup(String translationGroup)
  {
    this._translationGroup = translationGroup;
  }

  public String getTranslationGroup()
  {
    if (_translationGroup != null)
      return _translationGroup;
    ValueExpression ve = getValueExpression("translationGroup");
    return ve != null? (String) ve.getValue(getFacesContext().getELContext()): "jsp";
  }  
  
  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    createWidgetMap();
    ResponseWriter writer = context.getResponseWriter();
    if (getDynamic())
    {
      encodeLibraries(context, writer);
    }    
    writer.startElement("div", this);
    String id = getId();
    if (id != null)
    {
      writer.writeAttribute("id", id, null);
    }
    String style = getStyle();
    if (style != null)
    {
      writer.writeAttribute("style", style, null);
    }
    String styleClass = getStyleClass();
    if (styleClass != null)
    {
      writer.writeAttribute("class", styleClass, null);
    }
  }

  @Override
  public void encodeChildren(FacesContext context) throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();    
    if (isRenderPaginator()) 
    {
      encodePaginator(writer, getTranslator());
      writer.startElement("div", this);
      writer.writeAttribute("id", getJSVarName() + "_window", null); 
      writer.writeAttribute("class", "widgetContainerWindow", null);
      writer.startElement("div", this);
      writer.writeAttribute("id", getJSVarName() + "_pages", null); 
      writer.writeAttribute("class", "widgetContainerPages", null);
    }
      
    for (int i = 0; i < getPageCount(); i++)
    {
      if (isRenderPaginator())
      {
        writer.startElement("div", this);
        writer.writeAttribute("id", getJSVarName() + "_page_" + i, null); 
        writer.writeAttribute("class", "widgetContainerPage", null);
      }
      String title = getTitle(i);
      if (title != null)
      {
        writer.startElement("div", this);        
        writer.writeAttribute("class", "title", null);
        renderHtmlText(title, writer, getTranslator(),
          UserSessionBean.getCurrentInstance().getSelectedMid());
        writer.endElement("div");
      }
      
      String headerDocId = getHeaderDocId(i);
      if (headerDocId != null)
      {
        writer.startElement("div", this);        
        writer.writeAttribute("class", "headerBrowser", null);      
        HtmlBrowser htmlBrowser = new HtmlBrowser();      
        htmlBrowser.setUrl(getDocUrl(headerDocId));
        htmlBrowser.setPort(ApplicationBean.getCurrentInstance().getDefaultPort());
        htmlBrowser.setTranslator(UserSessionBean.getCurrentInstance().getTranslator());
        htmlBrowser.setTranslationGroup(UserSessionBean.getCurrentInstance().getTranslationGroup());
        RendererUtils.renderChild(context, htmlBrowser);      
        writer.endElement("div");
      }
      
      String sLayout = getLayout(i);    
      boolean renderGroupDiv = sLayout.contains("#") && !getDynamic();
      boolean firstGroup = true;
      for (String group : sLayout.split("#"))
      {
        String groupLayout = group;
        if (renderGroupDiv)
        {
          writer.startElement("div", this);
          writer.writeAttribute("class", "widgetGroup", null);        

          if (group.contains(":"))
          {
            String groupDescription = group.split(":")[0];
            writer.startElement("div", this);
            writer.writeAttribute("class", "title", null);
            renderHtmlText(groupDescription, writer, getTranslator(), 
              UserSessionBean.getCurrentInstance().getSelectedMid());          
            writer.endElement("div");
            groupLayout = group.split(":")[1];
          }
        }
        WidgetLayout layout = new WidgetLayout(getColumns(i), groupLayout);
        List<List<HtmlWidget>> widgets = readWidgets(layout);

        for (int column = 0; column < layout.getColumns(); column++)
        {
          writer.write("\n");
          writer.writeComment("********* Column " + column + " *********");
          writer.write("\n");
          writer.startElement("div", this);

          String columnClass = getColumnClass(column, i);
          if (columnClass != null)
          {
            writer.writeAttribute("class", columnClass, getStyle());
          }
          writer.writeText("\n", null);
          
          String columnTitle = getColumnTitle(column, i);
          if (columnTitle != null && firstGroup)
          {
            writer.startElement("div", this);
            writer.writeAttribute("class", "columnTitle", null);
            renderHtmlText(columnTitle, writer, getTranslator(), 
              UserSessionBean.getCurrentInstance().getSelectedMid());
            writer.endElement("div");
          }
          
          if (column < widgets.size())
          {
            boolean columnRenderAsList = 
              Boolean.valueOf(getColumnRenderAsList(column, i));
            if (columnRenderAsList)
            {
              writer.startElement("ul", this);
              writer.writeAttribute("class", "widgetList", null);              
            }
            for (HtmlWidget widget : widgets.get(column))
            {
              if (columnRenderAsList)
              {
                writer.startElement("li", this);
              }
              RendererUtils.renderChild(context, widget);
              if (columnRenderAsList)
              {
                writer.endElement("li");
              }
              writer.write("\n");
            }
            if (columnRenderAsList)
            {
              writer.endElement("ul");
            }
          }
          writer.endElement("div");
        }

        if (renderGroupDiv)
        {
          writer.endElement("div");
        }
        firstGroup = false;
      }
      
      String footerDocId = getFooterDocId(i);
      if (footerDocId != null)
      {
        writer.startElement("div", this);
        writer.writeAttribute("class", "footerBrowser", null);      
        HtmlBrowser htmlBrowser = new HtmlBrowser();      
        htmlBrowser.setUrl(getDocUrl(footerDocId));
        htmlBrowser.setPort(ApplicationBean.getCurrentInstance().getDefaultPort());
        htmlBrowser.setTranslator(UserSessionBean.getCurrentInstance().getTranslator());
        htmlBrowser.setTranslationGroup(UserSessionBean.getCurrentInstance().getTranslationGroup());
        RendererUtils.renderChild(context, htmlBrowser);
        writer.endElement("div");
      }
      
      if (isRenderPaginator())
      {
        writer.endElement("div"); //page_x
      }
    }
    
    if (isRenderPaginator())
    {
      writer.endElement("div"); // pages div
      writer.endElement("div"); // window div
    }
    
    writer.write("\n");
  }

  @Override
  public void encodeEnd(FacesContext context) throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();
    writer.endElement("div");
    if (getDynamic())
    {
      encodeJavascript(context, writer);
    }
    if (isRenderPaginator()) encodePaginatorInitJS(writer);    
    destroyWidgetMap();
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[16];
    values[0] = super.saveState(context);
    values[1] = _columns;
    values[2] = _columnClasses;
    values[3] = _layout;
    values[4] = _style;
    values[5] = _styleClass;
    values[6] = _dynamic;
    values[7] = _updateCallback;
    values[8] = _title;
    values[9] = _headerDocId;
    values[10] = _footerDocId;     
    values[11] = _translator;     
    values[12] = _translationGroup;    
    values[13] = _columnTitles;
    values[14] = _columnRenderAsList;
    values[15] = _draggableWidgets;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _columns = (List)values[1];
    _columnClasses = (List)values[2];
    _layout = (List)values[3];
    _style = (String)values[4];
    _styleClass = (String)values[5];
    _dynamic = (Boolean)values[6];
    _updateCallback = (String)values[7];
    _title = (List)values[8];
    _headerDocId = (List)values[9];
    _footerDocId = (List)values[10];
    _translator = (Translator)values[11];
    _translationGroup = (String)values[12];
    _columnTitles = (List)values[13];
    _columnRenderAsList = (List)values[14];
    _draggableWidgets = (Boolean)values[15];    
  }

  private void encodeLibraries(FacesContext context, ResponseWriter writer)
    throws IOException
  {
    Map requestMap = context.getExternalContext().getRequestMap();
    if (!requestMap.containsKey(JAVASCRIPT_WIDGET_ENCODED))
    {
      writer.write("\n");
      includeScript("/plugins/widget/prototype.js", writer);
      includeScript("/plugins/widget/builder.js", writer);
      includeScript("/plugins/widget/effects.js", writer);
      includeScript("/plugins/widget/dragdrop.js", writer);
      includeScript("/plugins/widget/widget.js", writer);
      requestMap.put(JAVASCRIPT_WIDGET_ENCODED, "true");
    }
  }

  private void encodeJavascript(FacesContext context, ResponseWriter writer)
    throws IOException
  {
    writer.write("\n");
    writer.startElement("script", this);
    writer.writeAttribute("type", "text/javascript", null);
    
    StringBuilder builder = new StringBuilder();
    String functionName = "initWidgets_" + getId();
    String varName = "portal_" + getId();
    builder.append("function " + functionName + "() {\n");
    builder.append(varName + " = new Xilinus.Portal(\"" + getId() +
       "\", {onUpdate:" + getUpdateCallback() + "});\n");
    WidgetLayout layout = new WidgetLayout(getColumns(0), getLayout(0)); //TODO
    for (String widgetId : layout.getWidgetIds())
    {
      if (widgetMap.containsKey(widgetId))
      {
        builder.append(varName + ".add(new Xilinus.Widget(\"" +
          widgetId + "\")" + (!getDraggableWidgets() ? ", false" : "") + ");\n");
      }
    }
    builder.append("}\n");
    builder.append("Event.observe(window, 'load', " + functionName + ");\n");
    
    writer.writeText(builder.toString(), null);
    writer.endElement("script");
    writer.write("\n");
  }

  private void includeScript(String source, ResponseWriter writer)
    throws IOException
  {
    writer.startElement("script", this);
    writer.writeAttribute("type", "text/javascript", null);
    writer.writeAttribute("src", source, null);
    writer.endElement("script");
    writer.write("\n");
  }

  private void createWidgetMap()
  {
    widgetMap = new HashMap();
    List<UIComponent> children = getChildren();
    for (UIComponent child : children)
    {
      if (child instanceof HtmlWidget)
      {
        widgetMap.put(child.getId(), (HtmlWidget)child);
      }
    }
  }

  private void destroyWidgetMap()
  {
    widgetMap.clear();
    widgetMap = null;
  }

  private List<List<HtmlWidget>> readWidgets(WidgetLayout layout)
  {
    List<List<HtmlWidget>> widgets = new ArrayList();
    for (int column = 0; column < layout.getColumns(); column++)
    {
      List columnWidgets = new ArrayList();
      widgets.add(columnWidgets);
      for (String widgetId : layout.getWidgetIds(column))
      {
        HtmlWidget widget = widgetMap.get(widgetId);
        if (widget != null)
        {
          columnWidgets.add(widget);
        }
      }
    }
    return widgets;
  }
  
  private String getColumnClass(int column, int page)
  {
    String columnClass = null;
    String columnClasses = getColumnClasses(page);
    if (columnClasses != null)
    {
      String columnClassesArray[] = columnClasses.split(",");
      if (column < columnClassesArray.length)
      {
        columnClass = columnClassesArray[column].trim();
      }
    }
    return columnClass;
  }

  private String getColumnTitle(int column, int page)
  {
    String columnTitle = null;
    String columnTitles = getColumnTitles(page);
    if (columnTitles != null)
    {
      String columnTitlesArray[] = columnTitles.split(",");
      if (column < columnTitlesArray.length)
      {
        columnTitle = columnTitlesArray[column].trim();
      }
    }
    return columnTitle;
  }

  private String getColumnRenderAsList(int column, int page)
  {
    String renderAsList = null;
    String renderAsListProp = getColumnRenderAsList(page);
    if (renderAsListProp != null)
    {
      String renderAsListArray[] = renderAsListProp.split(",");
      if (column < renderAsListArray.length)
      {
        renderAsList = renderAsListArray[column].trim();
      }
    }
    return renderAsList;
  }
  
  private String getDocUrl(String docId)
  {
    return getDocumentServletURL() + docId;
  }

  private String getDocumentServletURL()
  {
    HttpServletRequest request = (HttpServletRequest)FacesContext.
      getCurrentInstance().getExternalContext().getRequest();
    return HttpUtils.getContextURL(request) + DOC_SERVLET_PATH;
  }
  
  private void renderPlainText(String text, ResponseWriter writer, 
    Translator translator, String trGroupSuffix) throws IOException
  {
    String textToRender = null;
    if (translator != null)
    {
      String userLanguage = FacesUtils.getViewLanguage();
      String translationGroup = getTranslationGroup();
      if (!translationGroup.contains(":") && trGroupSuffix != null) 
        translationGroup = translationGroup + ":" + trGroupSuffix;      
      StringWriter sw = new StringWriter();
      translator.translate(new StringReader(text), sw, "text/plain",
        userLanguage, translationGroup);
      textToRender = sw.toString();
    }
    else textToRender = text;

    String lines[] = textToRender.split("\n");
    writer.writeText(lines[0], JSFAttr.VALUE_ATTR);
    for (int i = 1; i < lines.length; i++)
    {
      writer.startElement("br", this);
      writer.endElement("br");
      writer.writeText(lines[i], JSFAttr.VALUE_ATTR);
    }
  }

  private void renderHtmlText(String text, ResponseWriter writer, 
    Translator translator, String trGroupSuffix) throws IOException
  {
    if (translator != null)
    {
      String userLanguage = FacesUtils.getViewLanguage();
      String translationGroup = getTranslationGroup();
      if (!translationGroup.contains(":") && trGroupSuffix != null) 
        translationGroup = translationGroup + ":" + trGroupSuffix;
      translator.translate(new StringReader(text),
        writer, "text/html", userLanguage, translationGroup);
    }
    else writer.write(text);
  }
  
  //PAGINATION
  
  private int getPageCount()
  {
    return getLayout().size();
  }
  
  private boolean isRenderPaginator()
  {
    return (getPageCount() > 1);
  }
  
  private void encodePaginatorInitJS(ResponseWriter writer) throws IOException
  {
    Boolean isMobileBrowser = 
      UserSessionBean.getCurrentInstance().getBrowserType().equals("mobile");
    writer.startElement("script", this);
    writer.writeAttribute("type", "text/javascript", null);    
    writer.writeText("var " + getJSVarName() + " = new WidgetContainerPaginator('" + getId() + "');", null);    
    writer.writeText(getJSVarName() + ".pageCount = " + getPageCount() + ";", null);
    writer.writeText(getJSVarName() + ".enableSwipe = " + isMobileBrowser.toString() + ";", null);    
    writer.writeText(getJSVarName() + ".init();", null);
    writer.endElement("script");
  }
  
  private void encodePaginator(ResponseWriter writer, Translator translator) 
    throws IOException
  {
    writer.startElement("script", this);
    writer.writeAttribute("type", "text/javascript", null);
    writer.writeAttribute("src", "/plugins/widget/widgetcontainerpaginator.js", null);
    writer.endElement("script");
    
    writer.startElement("div", this);
    writer.writeAttribute("class", "widgetContainerPaginator", null);
    
    writer.startElement("a", this);
    writer.writeAttribute("id", getJSVarName() + "_prev", null);
    writer.writeAttribute("title", 
      getTranslation(getPagePrevLabel(), translator), null);    
    writer.writeAttribute("class", "pagePrev", null);
    writer.writeAttribute("onclick", getJSVarName() + ".prevPageSelect();", null);    
    writer.endElement("a");
    
    for (int i = 0; i < getPageCount(); i++)
    {
      writer.startElement("a", this);
      writer.writeAttribute("id", getJSVarName() + "_point_" + i, null);
      writer.writeAttribute("title", 
        getTranslation(getPageLabel(i + 1), translator), null);
      writer.writeAttribute("class", "point", null);
      writer.writeAttribute("onclick", getJSVarName() + ".selectPage(" + i + ");", null);
      writer.endElement("a");
    }
    
    writer.startElement("a", this);
    writer.writeAttribute("id", getJSVarName() + "_next", null);
    writer.writeAttribute("title", 
      getTranslation(getPageNextLabel(), translator), null);
    writer.writeAttribute("class", "pageNext", null);
    writer.writeAttribute("onclick", getJSVarName() + ".nextPageSelect();", null);        
    writer.endElement("a");
    
    writer.endElement("div");
  }
  
  private String getJSVarName()
  {
    return getId();
  }
  
  private String getTranslation(String text, Translator translator) 
    throws IOException
  {
    if (text != null)
    {      
      if (translator != null)
      {
        String userLanguage = FacesUtils.getViewLanguage();
        String translationGroup = getTranslationGroup();
        StringWriter sw = new StringWriter();
        translator.translate(new StringReader(text), sw, "text/plain",
          userLanguage, translationGroup);
        return sw.toString();
      }
      else
      {
        return text;
      }
    }
    else
    {
      return "";
    }    
  }  
  
  private String getPagePrevLabel() 
  {
    return "Mostrar anterior pàgina de widgets";
  }

  private String getPageLabel(int pageNum) 
  {
    return "Mostrar pàgina de widgets " + pageNum;
  }

  private String getPageNextLabel() 
  {
    return "Mostrar següent pàgina de widgets";
  }
  
  // INDEXED PROPERTIES METHODS
  
  private Integer getColumns(int page)
  {
    if (getColumns() == null || page < 0 || page >= getColumns().size()) 
      return 0;
    else
    {
      Object value = getColumns().get(page);
      if (value instanceof String && NONE_VALUE.equals((String)value))
      {
        return null;
      }
      else
      {
        return (value instanceof Integer ? (Integer)value : Integer.valueOf((String)value));
      }
    }    
  }

  private String getColumnClasses(int page)
  {
    if (getColumnClasses() == null || page < 0 || page >= getColumnClasses().size()) 
      return null;
    else
    {
      String value = (String)getColumnClasses().get(page);
      return (NONE_VALUE.equals(value) ? null : value);
    }
  }

  private String getColumnTitles(int page)
  {
    if (getColumnTitles() == null || page < 0 || page >= getColumnTitles().size()) 
      return null;
    else
    {
      String value = (String)getColumnTitles().get(page);
      return (NONE_VALUE.equals(value) ? null : value);
    }
  }
  
  private String getColumnRenderAsList(int page)
  {
    if (getColumnRenderAsList() == null || page < 0 || page >= getColumnRenderAsList().size()) 
      return null;
    else
    {
      String value = (String)getColumnRenderAsList().get(page);
      return (NONE_VALUE.equals(value) ? null : value);
    }
  }
  
  private String getLayout(int page)
  {
    if (getLayout() == null || page < 0 || page >= getLayout().size()) 
      return null;
    else 
      return (String)getLayout().get(page);        
  }  

  private String getTitle(int page)
  {
    if (getTitle() == null || page < 0 || page >= getTitle().size()) 
      return null;
    else
    {
      String value = (String)getTitle().get(page);
      return (NONE_VALUE.equals(value) ? null : value);      
    }      
  }
  
  private String getHeaderDocId(int page)
  {
    if (getHeaderDocId() == null || page < 0 || page >= getHeaderDocId().size()) 
      return null;
    else
    {
      String value = (String)getHeaderDocId().get(page);
      return (NONE_VALUE.equals(value) ? null : value);      
    }      
  }
  
  private String getFooterDocId(int page)
  {
    if (getFooterDocId() == null || page < 0 || page >= getFooterDocId().size()) 
      return null;
    else
    {
      String value = (String)getFooterDocId().get(page);
      return (NONE_VALUE.equals(value) ? null : value);      
    }      
  }  
  
}
