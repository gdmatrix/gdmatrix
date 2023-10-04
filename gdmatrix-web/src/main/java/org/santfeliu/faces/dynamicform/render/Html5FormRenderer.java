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
package org.santfeliu.faces.dynamicform.render;

import java.io.IOException;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;
import org.santfeliu.faces.dynamicform.DynamicForm;
import org.santfeliu.form.type.html.HtmlForm;
import org.santfeliu.form.type.html.HtmlView;

/**
 *
 * @author realor
 */
@FacesRenderer(componentFamily="DynamicForm",
	rendererType="Html5FormRenderer")
public class Html5FormRenderer extends HtmlFormRenderer
{
  @Override
  protected void encodeInput(HtmlView view, HtmlForm form,
    DynamicForm component, String clientId, ResponseWriter writer)
    throws IOException
  {
    String tag = view.getNativeViewType();
    String type = view.getProperty("type");
    String format = view.getProperty("format");

    boolean isDate = format != null &&
      (format.equals(HtmlForm.DATE_FORMAT) ||
       format.equals(HtmlForm.DATE_FORMAT + ":dd/MM/yyyy"));

    if (isDate && "input".equals(tag) && "text".equals(type))
    {
      String name = view.getProperty("name");
      String value = getValueAsString(component, view);

      writer.startElement("input", component);
      writer.writeAttribute("name", getFieldId(clientId, name + "_dp"), null);
      writer.writeAttribute("type", "date", null);
      String isoValue = convertUserToISO(value);
      if (isoValue != null)
      {
        writer.writeAttribute("value", isoValue, null);
      }
      // Convert ISO date to dd/MM/yyyy:
      // idx  = 0123456789
      // iso  = yyyy-MM-dd
      // user = dd/MM/yyyy

      writer.writeAttribute("onchange",
        "var v=this.value;document.getElementById('h-" + clientId + "').value=" +
        "(v.length==10)?v.substring(8,10)+'/'+v.substring(5,7)+'/'+v.substring(0,4):null;", null);

      renderViewAttributes(view, writer, "name", "type", "value", "maxlength");
      writer.endElement("input");

      writer.startElement("input", component);
      writer.writeAttribute("id", "h-" + clientId, null);
      writer.writeAttribute("type", "hidden", null);
      writer.writeAttribute("name", getFieldId(clientId, name), null);
      writer.writeAttribute("value", value, null);
      writer.endElement("input");
    }
    else
    {
      super.encodeInput(view, form, component, clientId, writer);
    }
  }

  protected String convertUserToISO(String sdate)
  {
    if (sdate == null || sdate.length() != 10) return null;

    // Convert dd/MM/yyyy to ISO date:
    // idx  = 0123456789
    // user = dd/MM/yyyy
    // iso  = yyyy-MM-dd

    return sdate.substring(6, 10) + "-" +
      sdate.substring(3, 5) + "-" + sdate.substring(0, 2);
  }

  @Override
  protected void encodeDatePicker(HtmlView view, HtmlForm form,
    DynamicForm component, String clientId, ResponseWriter writer)
    throws IOException
  {
  }
}
