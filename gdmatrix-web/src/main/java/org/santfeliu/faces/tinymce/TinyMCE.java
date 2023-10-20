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
package org.santfeliu.faces.tinymce;

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
@FacesComponent(value="org.gdmatrix.faces.TinyMCE")
@ResourceDependency(library = "gdmatrixfaces", name = "tinymce/tinymce-stub.js")
public class TinyMCE extends UIInput
{
  public static final String COMPONENT_FAMILY = "org.gdmatrix.faces";
  public static final String COMPONENT_TYPE = "org.gdmatrix.faces.TinyMCE";

  private Boolean _readonly;

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

  @Override
  public void decode(FacesContext context)
  {
    String textareaParam = getClientId(context) + "_textarea";
    Map<String, String> params =
      context.getExternalContext().getRequestParameterMap();
    String value = params.get(textareaParam);

    setSubmittedValue(value);
  }

  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    String clientId = getClientId(context);
    String textareaId = clientId + "_textarea";

    ResponseWriter writer = context.getResponseWriter();

    writer.startElement("div", this);
    writer.writeAttribute("id", clientId, null);

    // encode textarea
    writer.startElement("textarea", this);
    writer.writeAttribute("id", textareaId, null);
    writer.writeAttribute("name", textareaId, null);
    String html = (String)getValue();
    if (!StringUtils.isBlank(html))
    {
      writer.write(html);
    }
    writer.endElement("textarea");

    writer.endElement("div");

    // encode script
    Object encoded = context.getExternalContext().getRequestMap().get("tinymce");
    if (encoded == null)
    {
      context.getExternalContext().getRequestMap().put("tinymce", true);

      writer.startElement("script", this);
      writer.writeAttribute("src", "/resources/gdmatrixfaces/tinymce/tinymce.min.js", null);
      writer.write(" ");
      writer.endElement("script");
    }

    String language = context.getViewRoot().getLocale().getLanguage();

    Boolean readonly = isReadonly();
    if (readonly == null) readonly = false;

    writer.startElement("script", this);
    writer.writeText("tinymceInit('" + clientId + "'," + readonly
      + ",'" + language + "');", null);
    writer.endElement("script");
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[2];
    values[0] = super.saveState(context);
    values[1] = _readonly;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _readonly = (Boolean)values[1];
  }
}
