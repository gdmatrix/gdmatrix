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

import java.io.IOException;
import java.util.Map;
import javax.el.ValueExpression;
import javax.faces.component.FacesComponent;
import javax.faces.component.UICommand;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.ActionEvent;

/**
 *
 * @author realor
 */
@FacesComponent(value = "HtmlCommandTimer")
public class HtmlCommandTimer extends UICommand
{
  private String _time;
  private String _enabled;

  public HtmlCommandTimer()
  {
    setRendererType(null);
  }

  public String getTime()
  {
    if (_time != null) return _time;
    ValueExpression ve = getValueExpression("time");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setTime(String time)
  {
    this._time = time;
  }

  public String getEnabled()
  {
    if (_enabled != null) return _enabled;
    ValueExpression ve = getValueExpression("enabled");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setEnabled(String enabled)
  {
    this._enabled = enabled;
  }

  @Override
  public boolean getRendersChildren()
  {
    return true;
  }

  @Override
  public void decode(FacesContext context)
  {
    Map parameters = context.getExternalContext().getRequestParameterMap();
    String value = (String)parameters.get("refresh");
    if ("yes".equals(value))
    {
      queueEvent(new ActionEvent(this));
    }
  }

  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    if (!isRendered()) return;
    ResponseWriter writer = context.getResponseWriter();
    writer.startElement("input", this);
    writer.writeAttribute("type", "hidden", null);
    writer.writeAttribute("name", "refresh", null);
    writer.writeAttribute("value", "", null);
    writer.endElement("input");

    writer.startElement("script", this);
    writer.writeAttribute("type", "text/javascript", null);
    writer.write("function refreshPage(){");
    String enabled = getEnabled();
    if (enabled != null)
      writer.write("if (" + enabled + "){");
    else writer.write("if (true){");
    writer.write("document.forms[0]['refresh'].value='yes';"
      + "document.forms[0].submit();} else {");
    writer.write("setTimeout('refreshPage()', " + getTime() + ");}}");
    writer.write("setTimeout('refreshPage()', " + getTime() + ");");
    writer.endElement("script");
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[3];
    values[0] = super.saveState(context);
    values[1] = _time;
    values[2] = _enabled;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[]) state;
    super.restoreState(context, values[0]);
    _time = (String)values[1];
    _enabled = (String)values[2];
  }
}
