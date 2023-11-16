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
package org.santfeliu.faces.maplibre;

import java.io.IOException;
import javax.el.ValueExpression;
import javax.faces.application.ResourceDependency;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.faces.maplibre.model.Map;

/**
 *
 * @author realor
 */
@FacesComponent(value="org.gdmatrix.faces.MapLibre")
@ResourceDependency(library = "gdmatrixfaces", name = "maplibre/maplibre-stub.js")
@ResourceDependency(library = "gdmatrixfaces", name = "maplibre/maplibre-gl.js")
@ResourceDependency(library = "gdmatrixfaces", name = "maplibre/maplibre-default.css")
@ResourceDependency(library = "gdmatrixfaces", name = "maplibre/maplibre-gl.css")
public class MapLibre extends UIOutput
{
  public static final String COMPONENT_FAMILY = "org.gdmatrix.faces";
  public static final String COMPONENT_TYPE = "org.gdmatrix.faces.MapLibre";

  private String _style;
  private String _styleClass;

  @Override
  public String getFamily()
  {
    return COMPONENT_FAMILY;
  }

  public void setStyle(String style)
  {
    this._style = style;
  }

  public String getStyle()
  {
    if (_style != null) return _style;
    ValueExpression ve = getValueExpression("style");
    return ve != null ?
      (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setStyleClass(String styleClass)
  {
    this._styleClass = styleClass;
  }

  public String getStyleClass()
  {
    if (_styleClass != null) return _styleClass;
    ValueExpression ve = getValueExpression("styleClass");
    return ve != null ?
      (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    String clientId = getClientId(context);

    ResponseWriter writer = context.getResponseWriter();

    writer.startElement("div", this);
    writer.writeAttribute("id", clientId, null);

    String styleClass = getStyleClass();
    if (StringUtils.isBlank(styleClass)) styleClass = "maplibre";
    else styleClass = "maplibre " + styleClass;
    writer.writeAttribute("class", styleClass, null);

    String style = getStyle();
    if (!StringUtils.isBlank(style))
    {
      writer.writeAttribute("style", style, null);
    }

    writer.endElement("div");

    // encode script
    Map map = (Map)getValue();

    writer.startElement("script", this);
    writer.writeText("maplibreInit('" + clientId + "', " + map + ");", null);
    writer.endElement("script");
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[6];
    values[0] = super.saveState(context);
    values[1] = _style;
    values[2] = _styleClass;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _style = (String)values[1];
    _styleClass = (String)values[2];
  }
}
