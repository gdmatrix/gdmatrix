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
package org.santfeliu.faces.clock;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import javax.el.ValueExpression;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.servlet.http.HttpServletRequest;
import org.santfeliu.web.HttpUtils;

/**
 *
 * @author realor
 */
@FacesComponent(value = "HtmlClock")
public class HtmlClock extends UIOutput
{
  private static final String CLOCK_SCRIPT = "clock_script";
  private String _style;
  private String _styleClass;
  private String _format;

  public HtmlClock()
  {
    setRendererType(null);
  }

  @Override
  public String getFamily()
  {
    return "Clock";
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

  public void setFormat(String format)
  {
    this._format = format;
  }

  public String getFormat()
  {
    if (_format != null) return _format;
    ValueExpression ve = getValueExpression("format");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();

    Map requestMap = context.getExternalContext().getRequestMap();
    if (!requestMap.containsKey(CLOCK_SCRIPT))
    {
      HttpServletRequest request =
        (HttpServletRequest)context.getExternalContext().getRequest();
      String contextPath = request.getContextPath();
      writer.startElement("script", this);
      writer.writeAttribute("type", "text/javascript", null);
      writer.writeAttribute("src", 
        contextPath + "/plugins/clock/clock.js", null);
      writer.endElement("script");

      String clockUrl = HttpUtils.getContextURL(request) + "/clock";
      
      writer.startElement("script", this);
      writer.writeAttribute("type", "text/javascript", null);
      writer.writeText("setClockUrl(\"" + clockUrl + "\")", null);
      writer.endElement("script");
      requestMap.put(CLOCK_SCRIPT, "true");
    }

    String clockId = getId();
    String format = getFormat();
    String sformat;
    if ("date".equals(format)) sformat = "dd/MM/yyyy";
    else if ("time".equals(format)) sformat = "HH:mm:ss";
    else sformat = "dd/MM/yyyy HH:mm:ss";
    
    SimpleDateFormat df = new SimpleDateFormat(sformat);
    String time = df.format(new Date());

    writer.startElement("span", this);
    writer.writeAttribute("id", clockId, null);

    String style = getStyle();
    if (style != null)
    writer.writeAttribute("style", style, null);

    String styleClass = getStyleClass();
    if (styleClass != null)
    writer.writeAttribute("class", styleClass, null);

    writer.writeText(time, null);
    writer.endElement("span");
    
    format = (format == null) ? "null" : "\"" + format + "\"";

    writer.startElement("script", this);
    writer.writeAttribute("type", "text/javascript", null);
    writer.writeText("addClock(\"" + clockId + "\", " + format + ")", null);
    writer.endElement("script");
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[4];
    values[0] = super.saveState(context);
    values[1] = _style;
    values[2] = _styleClass;
    values[3] = _format;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _style = (String)values[1];
    _styleClass = (String)values[2];
    _format = (String)values[3];
  }  
}
