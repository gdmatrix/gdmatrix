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
package org.santfeliu.faces.print;

import java.io.IOException;
import javax.el.ValueExpression;
import javax.faces.component.FacesComponent;

import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.servlet.ServletContext;

/**
 *
 * @author unknown
 */
@FacesComponent(value = "HtmlPrintableGroup")
public class HtmlPrintableGroup extends UIComponentBase
{
  private String _name;
 
  public HtmlPrintableGroup()
  {
  }
  
  public String getFamily()
  {
    return "PrintableGroup";
  }

  public void setName(String name)
  {
    this._name = name;
  }

  public String getName()
  {
    if (_name != null) return _name;
    ValueExpression ve = getValueExpression("name");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    if (!isRendered()) return;
    ResponseWriter writer = context.getResponseWriter();
    writer.startElement("script", this);
    ServletContext servletContext = 
      (ServletContext)context.getExternalContext().getContext();
    String contextPath = servletContext.getContextPath();
    writer.writeAttribute("src", contextPath + "/plugins/print/print.js", null);
    writer.writeAttribute("type", "text/javascript", null);
    writer.writeText("", null);
    writer.endElement("script");
    
    writer.startElement("div", this);
    writer.writeAttribute("id", getName(), null);
  }

  @Override
  public void encodeEnd(FacesContext context) throws IOException
  {
    if (!isRendered()) return;
    ResponseWriter writer = context.getResponseWriter();
    writer.endElement("div");
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[2];
    values[0] = super.saveState(context);
    values[1] = _name;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _name =(String)values[1];
  }
}
