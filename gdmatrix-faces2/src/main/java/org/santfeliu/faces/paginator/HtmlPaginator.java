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
package org.santfeliu.faces.paginator;

import java.io.IOException;

import java.util.Map;
import javax.el.ValueExpression;
import javax.faces.component.FacesComponent;

import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import org.santfeliu.faces.FacesUtils;

/**
 *
 * @author unknown
 */
@FacesComponent(value = "HtmlPaginator")
public class HtmlPaginator extends UIInput
{
  public static final String DOTS = "···";

  // value is the current page
  private int _pageCount = -1;
  private int _visiblePages = -1;
  private String _style;
  private String _styleClass;

  public HtmlPaginator()
  {
  }

  public String getFamily()
  {
    return "Paginator";
  }

  public void setPageCount(int pageCount)
  {
    this._pageCount = pageCount;
  }

  public int getPageCount()
  {
    if (_pageCount > -1) return _pageCount;
    ValueExpression ve = getValueExpression("pageCount");
    if (ve == null) return 0;
    else
    {
      Object number = ve.getValue(getFacesContext().getELContext());
      if (number instanceof Number)
      {
        return ((Number)number).intValue();
      }
      else return 0;
    }
  }
  
  public void setVisiblePages(int visiblePages)
  {
    this._visiblePages = visiblePages;
  }

  public int getVisiblePages()
  {
    if (_visiblePages > -1) return _visiblePages;
    ValueExpression ve = getValueExpression("visiblePages");
    if (ve == null) return 0;
    else
    {
      Object number = ve.getValue(getFacesContext().getELContext());
      if (number instanceof Number)
      {
        return ((Number)number).intValue();
      }
      else return 0;
    }
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
  
  public int getCurrentPage() // 1 based
  {
    int currentPage = -1;
    Object number = getValue();
    if (number instanceof Number)
    {
      currentPage = ((Number)number).intValue();
    }
    return currentPage;
  }
  
  public void decode(FacesContext context)
  {
    if (!isRendered()) return;
    String clientId = getClientId(context);
    Map parameterMap = context.getExternalContext().getRequestParameterMap();

    String page = (String)parameterMap.get(clientId); // 1 based
    if (!"*".equals(page))
    {
      this.setSubmittedValue(page);
    }
  }
  
  public void encodeBegin(FacesContext context) throws IOException
  {
    String clientId = getClientId(context);
    String formId = FacesUtils.getParentFormId(this, context);
    ResponseWriter writer = context.getResponseWriter();
    writer.startElement("input", this);
    writer.writeAttribute("type", "hidden", null);
    writer.writeAttribute("name", clientId, null);
    writer.writeAttribute("value", "*", null);
    writer.endElement("input");
    
    writer.startElement("span", this);
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
    int currentPage = getCurrentPage() - 1;
    int visiblePages = getVisiblePages();
    int pageCount = getPageCount();
    int first;
    if (currentPage < visiblePages / 2)
    {
      first = 0;
    }
    else if (currentPage > pageCount - visiblePages / 2)
    {
      first = pageCount - visiblePages;
    }
    else // in the middle
    {
      first = currentPage - visiblePages / 2;
    }
    if (first < 0) first = 0;
    int last = Math.min(pageCount, first + visiblePages);
    if (first > 0)
    {
      encodePageLink(writer, clientId, formId, 0, false);
      if (first > 1) writer.writeText(DOTS, null);
    }
    for (int i = first; i < last; i++)
    {
      encodePageLink(writer, clientId, formId, i, i == currentPage);
    }
    if (last < pageCount)
    {
      if (last < pageCount - 1) writer.writeText(DOTS, null);
      encodePageLink(writer, clientId, formId, pageCount - 1, false);
    }
    writer.endElement("span");
  }

  public void encodeEnd(FacesContext context) throws IOException
  {
  }

  private void encodePageLink(ResponseWriter writer, 
    String clientId, String formId, int page, boolean isCurrent)
    throws IOException
  {
    writer.startElement("a", this);
    writer.writeAttribute("href", "#", null);
    if (isCurrent)
    {
      writer.writeAttribute("class", "current", null);
    }
    writer.writeAttribute("onclick", 
      "document.forms['" + formId + "']['" + clientId + "'].value='" + 
      (page + 1) + "'; document.forms['" + formId + "'].submit(); return false;", null);    
    writer.writeText(String.valueOf(page + 1), null);
    writer.endElement("a");
  }

  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[5];
    values[0] = super.saveState(context);
    values[1] = new Integer(_pageCount);
    values[2] = new Integer(_visiblePages);
    values[3] = _style;
    values[4] = _styleClass;
    return values;
  }
  
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _pageCount = ((Integer)values[1]).intValue();
    _visiblePages = ((Integer)values[2]).intValue();
    _style = (String)values[3];
    _styleClass = (String)values[4];
  }
}
