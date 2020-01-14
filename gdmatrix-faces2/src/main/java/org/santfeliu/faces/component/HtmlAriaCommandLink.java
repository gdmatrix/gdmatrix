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
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.component.FacesComponent;
import org.apache.myfaces.component.html.ext.HtmlCommandLink;



/**
 *
 * @author blanquepa
 */
@FacesComponent(value = "HtmlAriaCommandLink")
public class HtmlAriaCommandLink extends HtmlCommandLink
{
  private String _ariaLabel;
  private Boolean _ariaHidden;
  
  public HtmlAriaCommandLink()
  {
    setRendererType("org.apache.myfaces.Link");
  }

  public void setAriaLabel(String _ariaLabel)
  {
    this._ariaLabel = _ariaLabel;
  }
  
  public String getAriaLabel()
  {
    if (_ariaLabel != null)
      return _ariaLabel;
    ValueExpression ve = getValueExpression("ariaLabel");
    return ve != null? (String) ve.getValue(getFacesContext().getELContext()): null;
  }

  public void setAriaHidden(Boolean ariaHidden)
  {
    this._ariaHidden = ariaHidden;
  }

  public Boolean getAriaHidden()
  {
    if (_ariaHidden != null) return _ariaHidden;
    ValueExpression ve = getValueExpression("ariaHidden");
    return (ve != null ? (Boolean)ve.getValue(getFacesContext().getELContext()) : 
      Boolean.FALSE);
  }
  
  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    super.encodeBegin(context);
    ResponseWriter writer = context.getResponseWriter();
    if (getAriaLabel() != null)
    {
      writer.writeAttribute("aria-label", getAriaLabel(), null);
    }
    if (getAriaHidden())
    {
      writer.writeAttribute("aria-hidden", "true", null);
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
    Object values[] = new Object[3];
    values[0] = super.saveState(context);
    values[1] = _ariaLabel;
    values[2] = _ariaHidden;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _ariaLabel = (String)values[1];
    _ariaHidden = (Boolean)values[2];
  }
}
