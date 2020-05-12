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
package org.santfeliu.faces.menu.view;

import javax.el.ValueExpression;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author unknown
 */
@FacesComponent(value = "HtmlNavigationPath")
public class HtmlNavigationPath extends UIComponentBase
{
  public static String ACTIVE = "ACTIVE";
  public static String PASSIVE = "PASSIVE";

  private Object _value;
  private String _var;
  private String _baseMid;  
  private String _mode = ACTIVE;
  private String _style;
  private String _styleClass;
  private Integer _maxDepth; 
  private String _renderMode;

  public String getFamily()
  {
    return "NavigationPath";
  }

  public HtmlNavigationPath()
  {
  }

  public void setValue(Object value)
  {
    if (value instanceof String)
    {
      _value = value;
    }
  }

  public Object getValue()
  {
    if (_value != null) return _value;
    ValueExpression ve = getValueExpression("value");
    return ve != null ? (Object)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setVar(String var)
  {
    _var = var;
  }

  public String getVar()
  {
    return _var;
  }

  public void setBaseMid(String baseMid)
  {
    _baseMid = baseMid;
  }

  public String getBaseMid()
  {
    if (_baseMid != null) return _baseMid;
    ValueExpression ve = getValueExpression("baseMid");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setMode(String mode)
  {
    _mode = mode;
  }

  public String getMode()
  {
    if (_mode != null) return _mode;
    ValueExpression ve = getValueExpression("mode");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setStyle(String style)
  {
    _style = style;
  }

  public String getStyle()
  {
    if (_style != null) return _style;
    ValueExpression ve = getValueExpression("style");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setStyleClass(String styleClass)
  {
    _styleClass = styleClass;
  }

  public String getStyleClass()
  {
    if (_styleClass != null) return _styleClass;
    ValueExpression ve = getValueExpression("styleClass");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setMaxDepth(Integer maxDepth)
  {
    this._maxDepth = maxDepth;
  }
  
  public Integer getMaxDepth()
  {
    if (_maxDepth != null) return _maxDepth.intValue();
    ValueExpression ve = getValueExpression("maxDepth");
    return ve != null ? (Integer)ve.getValue(getFacesContext().getELContext()) : null;    
  }
  
  public void setRenderMode(String renderMode)
  {
    _renderMode = renderMode;
  }

  public String getRenderMode()
  {
    if (_renderMode != null) return _renderMode;
    ValueExpression ve = getValueExpression("renderMode");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }  
  
  @Override
  public boolean getRendersChildren()
  {
    return true;
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[9];
    values[0] = super.saveState(context);
    values[1] = _value; 
    values[2] = _var; 
    values[3] = _baseMid; 
    values[4] = _mode;
    values[5] = _style;
    values[6] = _styleClass;
    values[7] = _maxDepth;  
    values[8] = _renderMode;
    return values;
  }
  
  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _value = values[1];
    _var = (String)values[2];
    _baseMid = (String)values[3];
    _mode = (String)values[4];
    _style = (String)values[5];
    _styleClass = (String)values[6];
    _maxDepth = (Integer)values[7];
    _renderMode = (String)values[8];
  }
  
  @Override
  public String getRendererType()
  {
    String renderMode = getRenderMode();
    if (renderMode == null)
      return "SpanNavigationPath";
    else
      return StringUtils.capitalize(renderMode) + "NavigationPath";
  }
}
