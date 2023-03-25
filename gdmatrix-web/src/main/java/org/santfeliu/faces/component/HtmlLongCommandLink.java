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
package org.santfeliu.faces.component;

import org.santfeliu.faces.HtmlRenderUtils;
import java.io.IOException;
import java.util.Map;
import javax.el.ValueExpression;
import javax.faces.component.FacesComponent;
import javax.faces.component.UICommand;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.ActionEvent;
import org.santfeliu.faces.FacesUtils;

/**
 *
 * @author unknown
 */
@FacesComponent(value = "HtmlLongCommandLink")
public class HtmlLongCommandLink extends UICommand
{
  private String _value;
  private String _style;
  private String _styleClass;

  public HtmlLongCommandLink()
  {
  }

  @Override
  public String getFamily()
  {
    return "LongCommandLink";
  }

  public void setValue(String value)
  {
    this._value = value;
  }

  @Override
  public String getValue()
  {
    if (_value != null) return _value;
    ValueExpression ve = getValueExpression("value");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
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

  @Override
  public void decode(FacesContext context)
  {
    try
    {
      if (!isRendered()) return;
      String clientId = getClientId(context);
      Map parameterMap = context.getExternalContext().getRequestParameterMap();
      String formId = FacesUtils.getParentFormId(this, context);

      Object value = parameterMap.get(
        formId + ":" + HtmlRenderUtils.HIDDEN_LINK_ID);
      if (clientId.equals(value))
      {
        queueEvent(new ActionEvent(this));
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    if (!isRendered()) return;

    HtmlRenderUtils.renderHiddenLink(this, context);

    String clientId = getClientId(context);
    ResponseWriter writer = context.getResponseWriter();
    String formId = FacesUtils.getParentFormId(this, context);
    
    writer.startElement("a", this);
    writer.writeAttribute("href", "#", null);
    if (formId != null)
    {
      writer.writeAttribute("onclick",
        "showOverlay(); document.forms['" + formId +
        "']['" + formId + ":" + HtmlRenderUtils.HIDDEN_LINK_ID + "'].value='" +
        clientId + "'; document.forms['" + formId +
        "'].submit(); return false;", null);
    }
    if (getStyle() != null)
    {
      writer.writeAttribute("style", getStyle(), null);
    }
    if (getStyleClass() != null)
    {
      writer.writeAttribute("class", getStyleClass(), null);
    }
    String value = getValue();
    if (value != null)
    {
      writer.writeText(value, null);
    }
  }

  @Override
  public void encodeEnd(FacesContext context) throws IOException
  {
    if (!isRendered()) return;
    ResponseWriter writer = context.getResponseWriter();
    writer.endElement("a");
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[4];
    values[0] = super.saveState(context);
    values[1] = _value;
    values[2] = _style;
    values[3] = _styleClass;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _value = (String)values[1];
    _style = (String)values[2];
    _styleClass = (String)values[3];
  }
}
