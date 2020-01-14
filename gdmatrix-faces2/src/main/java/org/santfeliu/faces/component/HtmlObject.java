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
import java.util.regex.Pattern;
import javax.el.ValueExpression;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIOutput;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author blanquepa
 */
@FacesComponent(value = "HtmlObject")
public class HtmlObject extends UIOutput
{
  private String _httpsDisableOnAgent;
  private String _disabledMessage;

  public HtmlObject()
  {
    setRendererType(null);
  }

  public void setHttpsDisableOnAgent(String _httpsDisableOnAgent)
  {
    this._httpsDisableOnAgent = _httpsDisableOnAgent;
  }
  
  public String getHttpsDisableOnAgent()
  {
    if (_httpsDisableOnAgent != null) return _httpsDisableOnAgent;
    ValueExpression ve = getValueExpression("httpsDisableOnAgent");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  @Override
  public boolean getRendersChildren()
  {
    return true;
  }

  public void setDisabledMessage(String _disabledMessage)
  {
    this._disabledMessage = _disabledMessage;
  }

  public String getDisabledMessage()
  {
    if (_disabledMessage != null) return _disabledMessage;
    ValueExpression ve = getValueExpression("disabledMessage");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  @Override
  public String getFamily()
  {
    return "Object";
  }

  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    if (!isRendered()) return;

    ResponseWriter writer = context.getResponseWriter();
    ExternalContext extContext = context.getExternalContext();
    Map headerMap = extContext.getRequestHeaderMap();
    String userAgent = (String)headerMap.get("user-agent");

    HttpServletRequest request =
      (HttpServletRequest)context.getExternalContext().getRequest();

    writer.startElement("div", this);
    if (_httpsDisableOnAgent == null || !request.isSecure() ||
        (request.isSecure() &&
          !Pattern.matches(_httpsDisableOnAgent, userAgent)))
    {
      Object value = getValue();
      if (value != null)
      {
        writer.write(value.toString());
      }
    }
    else
    {
      String message = "WIDGET_DISABLED";
      if (getDisabledMessage() != null)
        message = getDisabledMessage();
      writer.writeText(message, null);
    }
    writer.endElement("div");
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[3];
    values[0] = super.saveState(context);
    values[1] = _httpsDisableOnAgent;
    values[2] = _disabledMessage;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _httpsDisableOnAgent = (String)values[1];
    _disabledMessage = (String)values[2];
  }

  public static void main(String[] args)
  {
    System.out.println(Pattern.matches(
      "Mozilla/(.)+",
      "Mozilla/5.0 (Windows; U; Windows NT 5.1; ca; rv:1.9.2.10) " +
      "Gecko/20100914 Firefox/3.6.10 ( .NET CLR 3.5.30729)")
    );
  }
}
