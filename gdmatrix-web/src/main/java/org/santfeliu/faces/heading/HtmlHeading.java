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
package org.santfeliu.faces.heading;

import java.io.IOException;
import javax.el.ValueExpression;
import javax.faces.component.FacesComponent;

import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

/**
 *
 * @author unknown
 */
@FacesComponent(value = "HtmlHeading")
public class HtmlHeading extends UIComponentBase
{
  private Integer _level;
  private String _style;
  private String _styleClass;  

  public HtmlHeading()
  {
  }
  
  public String getFamily()
  {
    return "Heading";
  }

  public void setLevel(Integer level)
  {
    this._level = level;
  }

  public Integer getLevel()
  {
    if (_level != null) return _level;
    ValueExpression ve = getValueExpression("level");
    if (ve == null) return 1;
    
    Integer level = null;
    Object value = ve.getValue(getFacesContext().getELContext());
    if (value instanceof String)
      level = Integer.valueOf((String)value);
    else  
      level = (Integer)value;
    return level == null ? 1 : level;
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
  public void encodeBegin(FacesContext context) throws IOException
  {
    try
    {
      if (!isRendered()) return;
      ResponseWriter writer = context.getResponseWriter();
      int level = getLevel();

      writer.startElement("h" + level, this);
      String style = getStyle();
      if (style != null)
      {
        writer.writeAttribute("style", style, null);
      }
      String styleClass = getStyleClass();
      if (styleClass != null)
      {
        writer.writeAttribute("class", styleClass, null);
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  @Override
  public void encodeEnd(FacesContext context) throws IOException
  {
    if (!isRendered()) return;
    ResponseWriter writer = context.getResponseWriter();
    int level = getLevel();
    writer.endElement("h" + level);
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[4];
    values[0] = super.saveState(context);
    values[1] = _level;
    values[2] = _style;
    values[3] = _styleClass;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _level = (Integer)values[1];
    _style = (String)values[2];
    _styleClass = (String)values[3];
  }
}
