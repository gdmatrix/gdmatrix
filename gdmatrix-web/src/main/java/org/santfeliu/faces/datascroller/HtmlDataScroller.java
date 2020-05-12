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
package org.santfeliu.faces.datascroller;

import java.io.IOException;
import java.util.Map;
import javax.el.ValueExpression;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.component.UIPanel;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.ActionEvent;
import javax.faces.event.FacesEvent;
import javax.faces.event.PhaseId;

/**
 *
 * @author real
 */
@FacesComponent(value = "HtmlDataScroller")
public class HtmlDataScroller extends UIPanel
{
  private static final int FIRST = 0;
  private static final int LAST = 1;
  private static final int NEXT = 2;
  private static final int PREVIOUS = 3;
  private static final int FAST_FORWARD = 4;
  private static final int FAST_REWIND = 5;
  private static final int PAGE = 6;

  private transient UIData _UIData;
  private String _for;
  private String _pageIndexVar;
  private String _pageCountVar;
  private String _rowsCountVar;
  private String _firstRowIndexVar;
  private String _lastRowIndexVar;
  private String _style;
  private String _styleClass;

  @Override
  public String getFamily()
  {
    return "DataScroller";
  }

  @Override
  public boolean getRendersChildren()
  {
    return true;
  }

  public void setFor(String _for)
  {
    this._for = _for;
  }

  public String getFor()
  {
    if (_for != null) return _for;
    ValueExpression ve = getValueExpression("for");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setFirstRowIndexVar(String _firstRowIndexVar)
  {
    this._firstRowIndexVar = _firstRowIndexVar;
  }

  public String getFirstRowIndexVar()
  {
    if (_firstRowIndexVar != null) return _firstRowIndexVar;
    ValueExpression ve = getValueExpression("firstRowIndexVar");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setLastRowIndexVar(String _lastRowIndexVar)
  {
    this._lastRowIndexVar = _lastRowIndexVar;
  }

  public String getLastRowIndexVar()
  {
    if (_lastRowIndexVar != null) return _lastRowIndexVar;
    ValueExpression ve = getValueExpression("lastRowIndexVar");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setPageCountVar(String _pageCountVar)
  {
    this._pageCountVar = _pageCountVar;
  }

  public String getPageCountVar()
  {
    if (_pageCountVar != null) return _pageCountVar;
    ValueExpression ve = getValueExpression("pageCountVar");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setPageIndexVar(String _pageIndexVar)
  {
    this._pageIndexVar = _pageIndexVar;
  }

  public String getPageIndexVar()
  {
    if (_pageIndexVar != null) return _pageIndexVar;
    ValueExpression ve = getValueExpression("pageIndexVar");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setRowsCountVar(String _rowsCountVar)
  {
    this._rowsCountVar = _rowsCountVar;
  }

  public String getRowsCountVar()
  {
    if (_rowsCountVar != null) return _rowsCountVar;
    ValueExpression ve = getValueExpression("rowsCountVar");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setStyle(String _style)
  {
    this._style = _style;
  }

  public String getStyle()
  {
    if (_style != null) return _style;
    ValueExpression ve = getValueExpression("style");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setStyleClass(String _styleClass)
  {
    this._styleClass = _styleClass;
  }

  public String getStyleClass()
  {
    if (_styleClass != null) return _styleClass;
    ValueExpression ve = getValueExpression("styleClass");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  @Override
  public void decode(FacesContext context)
  {
    ScrollerActionEvent event;

    Map parameters = context.getExternalContext().getRequestParameterMap();
    if (parameters.containsKey(getId() + "_previous"))
    {
      event = new ScrollerActionEvent(this, PREVIOUS, 0);
    }
    else if (parameters.containsKey(getId() + "_next"))
    {
      event = new ScrollerActionEvent(this, NEXT, 0);
    }
    else if (parameters.containsKey(getId() + "_first"))
    {
      event = new ScrollerActionEvent(this, FIRST, 0);
    }
    else if (parameters.containsKey(getId() + "_last"))
    {
      event = new ScrollerActionEvent(this, LAST, 0);
    }
    else if (parameters.containsKey(getId() + "_page"))
    {
      int page = Integer.parseInt((String)parameters.get(getId() + "_page"));
      event = new ScrollerActionEvent(this, PAGE, page);
    }
    else event = null;
    
    if (event != null)
    {
      event.setPhaseId(PhaseId.INVOKE_APPLICATION);
      queueEvent(event);
    }
  }

  @Override
  public void broadcast(FacesEvent event)
  {
    super.broadcast(event);
    if (event instanceof ScrollerActionEvent)
    {
      ScrollerActionEvent scrollerEvent = (ScrollerActionEvent)event;
      UIData uiData = getUIData();
      if (uiData == null) return;

      switch (scrollerEvent.action)
      {
        case NEXT:
          int next = uiData.getFirst() + uiData.getRows();
          if (next < uiData.getRowCount())
          {
            uiData.setFirst(next);
          }
          break;

        case PREVIOUS:
          int previous = uiData.getFirst() - uiData.getRows();
          if (previous >= 0)
          {
            uiData.setFirst(previous);
          }
          break;

        case FIRST:
          uiData.setFirst(0);
          break;

        case LAST:
          int rowCount = uiData.getRowCount();
          int rows = uiData.getRows();
          int delta = rowCount % rows;
          int first = delta > 0 && delta < rows ? 
             rowCount - delta : rowCount - rows;
          if (first >= 0)
          {
            uiData.setFirst(first);
          }
          else
          {
            uiData.setFirst(0);
          }
          break;
      }
    }
  }

  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    setVariables(context);
  }

  @Override
  public void encodeChildren(FacesContext context) throws IOException
  {
  }

  @Override
  public void encodeEnd(FacesContext context) throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();
    writer.startElement("table", this);
    writer.writeAttribute("title", "scroller", null);
    writer.startElement("tr", this);

    // first button
    writer.startElement("td", this);
    writer.startElement("input", this);
    writer.writeAttribute("type", "submit", null);
    writer.writeAttribute("name", getId() + "_first", null);
    writer.writeAttribute("value", "first", null);
    writer.endElement("td");

    // previous button    
    writer.startElement("td", this);
    writer.startElement("input", this);
    writer.writeAttribute("type", "submit", null);
    writer.writeAttribute("name", getId() + "_previous", null);
    writer.writeAttribute("value", "previous", null);
    writer.endElement("td");

    // next button
    writer.startElement("td", this);
    writer.startElement("input", this);
    writer.writeAttribute("type", "submit", null);
    writer.writeAttribute("name", getId() + "_next", null);
    writer.writeAttribute("value", "next", null);
    writer.endElement("td");

    // last button
    writer.startElement("td", this);
    writer.startElement("input", this);
    writer.writeAttribute("type", "submit", null);
    writer.writeAttribute("name", getId() + "_last", null);
    writer.writeAttribute("value", "last", null);
    writer.endElement("td");

    writer.endElement("tr");
    writer.endElement("table");

    removeVariables(context);
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[9];
    values[0] = super.saveState(context);
    values[1] = _for;
    values[2] = _pageIndexVar;
    values[3] = _pageCountVar;
    values[4] = _rowsCountVar;
    values[5] = _firstRowIndexVar;
    values[6] = _lastRowIndexVar;
    values[7] = _style;
    values[8] = _styleClass;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _for = (String)values[1];
    _pageIndexVar = (String)values[2];
    _pageCountVar = (String)values[3];
    _rowsCountVar = (String)values[4];
    _firstRowIndexVar = (String)values[5];
    _lastRowIndexVar = (String)values[6];
    _style = (String)values[7];
    _styleClass = (String)values[8];
  }

  protected void setVariables(FacesContext context)
  {
  }

  protected void removeVariables(FacesContext context)
  {
  }

  protected UIData getUIData()
  {
    if (_UIData == null)
    {
      _UIData = findUIData();
    }
    return _UIData;
  }

  protected UIData findUIData()
  {
    String forStr = getFor();
    UIComponent forComp;
    if (forStr == null)
    {
      // DataScroller may be a child of uiData
       forComp = getParent();
    }
    else
    {
      forComp = findComponent(forStr);
    }
    if (forComp == null)
    {
      throw new IllegalArgumentException(
        "could not find UIData referenced by attribute dataScroller@for = '" +
        forStr + "'");
    }
    else if (!(forComp instanceof UIData))
    {
      throw new IllegalArgumentException(
        "uiComponent referenced by attribute dataScroller@for = '" +
        forStr + "' must be of type " + UIData.class.getName() +
        ", not type " + forComp.getClass().getName());
    }
    return (UIData) forComp;
  }

  class ScrollerActionEvent extends ActionEvent
  {
    private int action;
    private int page;

    public ScrollerActionEvent(UIComponent component, int action, int page)
    {
      super(component);
      this.action = action;
      this.page = page;
    }
  }
}
