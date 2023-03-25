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
import java.io.StringReader;
import java.io.StringWriter;
import javax.el.ValueExpression;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.Translator;

/**
 *
 * @author lopezrj
 */
@FacesComponent(value = "HtmlGraphicImage")
public class HtmlGraphicImage
  extends javax.faces.component.html.HtmlGraphicImage
{
  private Translator _translator;
  private String _translationGroup;

  public HtmlGraphicImage()
  {
    setRendererType(null);
  }

  public void setTranslator(Translator translator)
  {
    this._translator = translator;
  }

  public Translator getTranslator()
  {
    if (_translator != null)
      return _translator;
    ValueExpression ve = getValueExpression("translator");
    return ve != null? (Translator) ve.getValue(getFacesContext().getELContext()): null;
  }

  public void setTranslationGroup(String translationGroup)
  {
    this._translationGroup = translationGroup;
  }

  public String getTranslationGroup()
  {
    if (_translationGroup != null)
      return _translationGroup;
    ValueExpression ve = getValueExpression("translationGroup");
    return ve != null? (String) ve.getValue(getFacesContext().getELContext()): null;
  }

  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    if (context == null)
      throw new NullPointerException();

    if (!isRendered())
      return;

    ResponseWriter writer = context.getResponseWriter();

    writer.startElement("img", this);

    if (getId() != null && !getId().startsWith(UIViewRoot.UNIQUE_ID_PREFIX))
    {
      writer.writeAttribute("id", getId(), null);
    }
    Object src = (getValue() != null ? getValue() : getUrl());
    writer.writeAttribute("src", (src != null ? src : ""), null);
    if (getStyle() != null)
    {
      writer.writeAttribute("style", getStyle(), null);
    }
    if (getStyleClass() != null)
    {
      writer.writeAttribute("class", getStyleClass(), null);
    }
    writer.writeAttribute("alt", (getAlt() != null ? translate(getAlt()) : ""),
      null);
    if (getTitle() != null)
    {
      writer.writeAttribute("title", translate(getTitle()), null);
    }
    if (getHeight() != null)
    {
      writer.writeAttribute("height", getHeight(), null);
    }
    if (getWidth() != null)
    {
      writer.writeAttribute("width", getWidth(), null);
    }
    if (getOnclick() != null)
    {
      writer.writeAttribute("onclick", getOnclick(), null);
    }
    if (this.getOnmouseover() != null)
    {
      writer.writeAttribute("onmouseover", getOnmouseover(), null);
    }
    writer.endElement("img");
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[2];
    values[0] = super.saveState(context);
    values[1] = _translationGroup;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[]) state;
    super.restoreState(context, values[0]);
    _translationGroup = (String) values[1];
  }

  private String translate(String text) throws IOException
  {
    Translator translator = getTranslator();
    if (translator != null)
    {
      String userLanguage = FacesUtils.getViewLanguage();
      StringWriter sw = new StringWriter();
      translator.translate(new StringReader(text), sw, "text/plain",
        userLanguage, getTranslationGroup());
      text = sw.toString();
    }
    return text;
  }

}
