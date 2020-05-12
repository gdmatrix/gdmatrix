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
import javax.faces.component.FacesComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import org.apache.myfaces.custom.div.Div;

/**
 *
 * @author blanquepa
 */
@FacesComponent(value = "HtmlDiv")
public class HtmlDiv extends Div
{
  private Boolean _ariaHidden;
  private String _role;  
  
  public static final String COMPONENT_TYPE = "Div";  

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

  public String getRole()
  {
    if (_role != null)
      return _role;
    ValueExpression ve = getValueExpression("role");
    return ve != null? (String) ve.getValue(getFacesContext().getELContext()): null;
  }

  public void setRole(String role)
  {
    this._role = role;
  }

  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    if (context == null)
      throw new NullPointerException();
   
    if (!isRendered()) 
      return;
    
    super.encodeBegin(context);

    ResponseWriter writer = context.getResponseWriter();    

    if (getAriaHidden())
    {
      writer.writeAttribute("aria-hidden", "true", null);
    }
    String role = getRole();
    if (role != null)
    {
      writer.writeAttribute("role", role, null);
    }    
  } 
 
  @Override
  public void encodeEnd(FacesContext context) throws IOException
  {
    super.encodeEnd(context);
  }
  
  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[3];
    values[0] = super.saveState(context);
    values[1] = _ariaHidden;   
    values[2] = _role;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[]) state;
    super.restoreState(context, values[0]);
    _ariaHidden = (Boolean)values[1];
    _role = (String)values[2];
  }   

}
