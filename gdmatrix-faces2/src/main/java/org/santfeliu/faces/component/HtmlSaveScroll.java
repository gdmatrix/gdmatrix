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
import java.util.Iterator;
import java.util.Map;
import javax.el.ValueExpression;
import javax.faces.application.FacesMessage;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

/**
 *
 * @author realor
 */
@FacesComponent(value = "HtmlSaveScroll")
public class HtmlSaveScroll extends UIComponentBase
{
  private Integer _value = null; // scroll value
  private Boolean _resetIfError;

  public void setValue(Integer value)
  {
    this._value = value;
  }

  public Integer getValue()
  {
    if (_value != null) return _value;
    ValueExpression ve = getValueExpression("value");
    if (ve == null) return 0;
    Integer value = (Integer)ve.getValue(getFacesContext().getELContext());
    return value == null ? 0 : value;
  }

  public void setResetIfError(Boolean resetIfError)
  {
    this._resetIfError = resetIfError;
  }

  public Boolean getResetIfError()
  {
    if (_resetIfError != null) return _resetIfError;
    ValueExpression ve = getValueExpression("resetIfError");
    if (ve == null) return false;
    Boolean value = (Boolean)ve.getValue(getFacesContext().getELContext());
    return value == null ? false : value;
  }  
  
  @Override
  public String getFamily()
  {
    return "SaveScroll";
  }

  @Override
  public void decode(FacesContext context)
  {
    Map parameters = context.getExternalContext().getRequestParameterMap();
    String textValue = (String)parameters.get("__SAVESCROLL");
    if (textValue != null)
    {
      _value = Integer.parseInt(textValue);
      ValueExpression ve = getValueExpression("value");
      if (ve != null)
      {
        // save _value in backed bean and reset _value
        ve.setValue(context.getELContext(), _value);
        _value = null;
      }
    }
  }

  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    if (!isRendered()) return;
    ResponseWriter writer = context.getResponseWriter();
    
    Integer scrollValue = getValue();
    if (getResetIfError() && isFacesError(context))
    {
      scrollValue = 0;
    }
    
    writer.startElement("input", this);
    writer.writeAttribute("id", "__SAVESCROLL", null);
    writer.writeAttribute("type", "hidden", null);
    writer.writeAttribute("name", "__SAVESCROLL", null);
    writer.writeAttribute("value", scrollValue, null);
    writer.endElement("input");

    String contextPath = context.getExternalContext().getRequestContextPath();
    writer.startElement("script", this);
    writer.writeAttribute("type", "text/javascript", null);
    writer.writeAttribute("src", 
      contextPath + "/plugins/savescroll/savescroll.js", null);
    writer.endElement("script");
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[5];
    values[0] = super.saveState(context);
    values[1] = _value;
    values[2] = _resetIfError;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _value = (Integer)values[1];
    _resetIfError = (Boolean)values[2];
  }
  
  private boolean isFacesError(FacesContext context)
  {
    Iterator itMessages = context.getMessages();
    while (itMessages.hasNext())
    {
      FacesMessage facesMessage = (FacesMessage)itMessages.next();
      if (facesMessage.getSeverity() == FacesMessage.SEVERITY_ERROR || 
        facesMessage.getSeverity() == FacesMessage.SEVERITY_FATAL)
      {
        return true;
      }
    }
    return false;
  }
}
