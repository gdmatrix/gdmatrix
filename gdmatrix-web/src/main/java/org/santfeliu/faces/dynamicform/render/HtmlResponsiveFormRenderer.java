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
import java.util.HashSet;
import java.util.Set;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;
import org.santfeliu.faces.Translator;
import org.santfeliu.faces.dynamicform.DynamicForm;
import org.santfeliu.form.Field;
import org.santfeliu.form.View;
import org.santfeliu.form.type.html.HtmlForm;
import org.santfeliu.form.type.html.HtmlView;

/**
 *
 * @author realor
 */
@FacesRenderer(componentFamily="DynamicForm",
	rendererType="HtmlResponsiveFormRenderer")
public class HtmlResponsiveFormRenderer extends HtmlFormRenderer
{
  protected static final String FORM_CLASS_NAME = "responsive_form";
  protected static final String ROW_CLASS_NAME = "row";
  protected static final Set excludedAttributes = new HashSet();

  static
  {
    excludedAttributes.add("style");
  }
  @Override
  protected void encodeHtmlView(HtmlView view, HtmlForm form,
    DynamicForm component, String clientId, ResponseWriter writer)
    throws IOException
  {
    String tag = view.getNativeViewType();
    String viewType = view.getViewType();
    if (tag == null) tag = "span";
    if (tag.equals("body") || tag.equals("form"))
    {
      writer.startElement("div", component);
      writer.writeAttribute("class", FORM_CLASS_NAME, null);
      encodeChildren(view, form, component, clientId, writer);
      writer.endElement("div");
    }
    else if (View.TEXT.equals(viewType))
    {
      encodeText(view, form, component, clientId, writer);
    }
    else if (tag.equals("input"))
    {
      writer.startElement("div", component);
      writer.writeAttribute("class", ROW_CLASS_NAME, null);
      encodeLabel(view, form, component, writer);
      encodeInput(view, form, component, clientId, writer);
      writer.endElement("div");
    }
    else if (tag.equals("textarea"))
    {
      writer.startElement("div", component);
      writer.writeAttribute("class", ROW_CLASS_NAME, null);
      encodeLabel(view, form, component, writer);
      encodeTextarea(view, form, component, clientId, writer);
      writer.endElement("div");
    }
    else if (tag.equals("select"))
    {
      writer.startElement("div", component);
      writer.writeAttribute("class", ROW_CLASS_NAME, null);
      encodeLabel(view, form, component, writer);
      encodeSelect(view, form, component, clientId, writer);
      writer.endElement("div");
    }
    else if (tag.equals("script"))
    {
      encodeScript(view, form, component, clientId, writer);
    }
    else if (View.LABEL.equals(viewType))
    {
      // skip, already rendered
    }
    else
    {
      encodeGenericView(view, form, component, clientId, writer);
    }
  }

  protected void encodeLabel(HtmlView view, HtmlForm form,
    DynamicForm component, ResponseWriter writer) throws IOException
  {
    String reference = view.getReference();
    Field field = form.getField(reference);
    writer.startElement("label", component);
    String id = view.getId();
    if (id != null)
    {
      writer.writeAttribute("for", id, null);
    }
    String labelText = field == null ? reference : field.getLabel();
    Translator translator = component.getTranslator();
    renderHtmlText(labelText, writer, translator, component);
    writer.endElement("label");
  }

  @Override
  protected void renderViewAttributes(View view, ResponseWriter writer,
    String ... excluded) throws IOException
  {
    for (String propertyName : view.getPropertyNames())
    {
      propertyName = propertyName.toLowerCase();
      boolean isExcluded = specialAttributes.contains(propertyName) ||
        excludedAttributes.contains(propertyName);
      int i = 0;
      while (!isExcluded && i < excluded.length)
      {
        isExcluded = propertyName.equalsIgnoreCase(excluded[i]);
        i++;
      }
      if (!isExcluded)
      {
        Object propertyValue = view.getProperty(propertyName);
        writer.writeAttribute(propertyName, propertyValue, null);
      }
    }
    // special case: disabled attribute
    String disabled = (String)view.getProperty("disabled");
    if (disabled != null && !"false".equals(disabled))
    {
      writer.writeAttribute("disabled", "true", null);
    }
  }
}
