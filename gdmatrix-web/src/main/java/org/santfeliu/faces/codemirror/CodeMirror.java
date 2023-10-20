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
package org.santfeliu.faces.codemirror;

import java.io.IOException;
import java.util.Map;
import javax.el.ValueExpression;
import javax.faces.application.ResourceDependency;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author realor
 */
@FacesComponent(value="org.gdmatrix.faces.CodeMirror")
@ResourceDependency(library = "gdmatrixfaces", name = "codemirror/codemirror.css")
public class CodeMirror extends UIInput
{
  public static final String COMPONENT_FAMILY = "org.gdmatrix.faces";
  public static final String COMPONENT_TYPE = "org.gdmatrix.faces.CodeMirror";

  private Boolean _readonly;
  private String _style;
  private String _styleClass;

  @Override
  public String getFamily()
  {
    return COMPONENT_FAMILY;
  }

  public Boolean isReadonly()
  {
    if (_readonly != null) return _readonly;
    ValueExpression ve = getValueExpression("readonly");
    return ve != null ?
      (Boolean)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setReadonly(Boolean readonly)
  {
    this._readonly = readonly;
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
  public void decode(FacesContext context)
  {
    String inputParam = getClientId(context) + "_input";
    Map<String, String> params =
      context.getExternalContext().getRequestParameterMap();
    String value = params.get(inputParam);

    setSubmittedValue(value);
  }

  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    String clientId = getClientId(context);
    String inputId = clientId + "_input";
    String editorId = clientId + "_editor";

    ResponseWriter writer = context.getResponseWriter();

    writer.startElement("div", this);
    writer.writeAttribute("id", clientId, null);

    String styleClass = getStyleClass();
    if (StringUtils.isBlank(styleClass)) styleClass = "codemirror-container";
    else styleClass = "codemirror-container " + styleClass;
    writer.writeAttribute("class", styleClass, null);

    String style = getStyle();
    if (!StringUtils.isBlank(style))
    {
      writer.writeAttribute("style", style, null);
    }

    // encode editor
    writer.startElement("div", this);
    writer.writeAttribute("id", editorId, null);
    writer.endElement("div");

    // encode hidden
    writer.startElement("input", this);
    writer.writeAttribute("type", "hidden", null);
    writer.writeAttribute("id", inputId, null);
    writer.writeAttribute("name", inputId, null);
    writer.writeAttribute("value", getValue(), null);
    writer.writeAttribute("autocomplete", "off", null);
    writer.endElement("input");

    writer.endElement("div");

    Boolean readonly = isReadonly();
    if (readonly == null) readonly = false;

    // encode script
    writer.startElement("script", this);
    writer.writeAttribute("type", "module", null);
    writer.writeText("import '/resources/gdmatrixfaces/codemirror/codemirror-stub.js';\n", null);
    writer.writeText("codemirrorInit('" + clientId + "', " + readonly + ");", null);
    writer.endElement("script");
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[4];
    values[0] = super.saveState(context);
    values[1] = _readonly;
    values[2] = _style;
    values[3] = _styleClass;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _readonly = (Boolean)values[1];
    _style = (String)values[2];
    _styleClass = (String)values[3];
  }
}
