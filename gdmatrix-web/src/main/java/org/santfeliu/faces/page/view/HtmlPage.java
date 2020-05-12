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
package org.santfeliu.faces.page.view;

import javax.el.ValueExpression;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;

import javax.faces.el.ValueBinding;

/**
 *
 * @author unknown
 */
@FacesComponent(value = "HtmlPage")
public class HtmlPage extends UIComponentBase
{
  private String _template; // templateName
  private Boolean _match = Boolean.TRUE; // match user template

  public HtmlPage()
  {
  }
  
  public String getFamily()
  {
    return "Page";
  }
  
  public void setTemplate(String template)
  {
    _template = template;
  }
  
  public String getTemplate()
  {
    if (_template != null) return _template;
    ValueExpression ve = getValueExpression("template");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;  
  }

  public void setMatch(boolean match)
  {
    _match = Boolean.valueOf(match);  
  }
  
  public boolean isMatch()
  {
    return _match.booleanValue();
  }
  
  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[3];
    values[0] = super.saveState(context);
    values[1] = _template;
    values[2] = _match;
    return values;
  }
  
  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _template = (String)values[1];
    _match = (Boolean)values[2];
  }
  
  @Override
  public String toString()
  {
    return super.toString() + ": " + _template + " " +
      (_match.booleanValue() ? "[match]" : "[no match]");
  }
}
