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

import org.apache.myfaces.shared_tomahawk.renderkit.JSFAttr;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;

import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.Translator;

/**
 *
 * @author unknown
 */
@FacesComponent(value = "HtmlOutputText")
public class HtmlOutputText
  extends javax.faces.component.html.HtmlOutputText
{
  private Translator _translator;
  private String _translationGroup;

  public HtmlOutputText()
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
    if (!isRendered()) return;
    Object value = getValue();
    if (value != null)
    {
      String text = value.toString();
      renderText(context, text, isEscape());
    }
  }

  public void renderText(FacesContext context, 
    String text, boolean escape) throws IOException
  {
    if (text != null)
    {
      ResponseWriter writer = context.getResponseWriter();
      boolean span = false;

      if (getId() != null && 
          !getId().startsWith(UIViewRoot.UNIQUE_ID_PREFIX))
      {
        span = true;
        writer.startElement(HTML.SPAN_ELEM, this);
        HtmlRendererUtils.writeIdIfNecessary(writer, this, context);
        HtmlRendererUtils.renderHTMLAttributes(writer, this, 
          HTML.COMMON_PASSTROUGH_ATTRIBUTES);
      }
      else
      {
        span = HtmlRendererUtils.renderHTMLAttributesWithOptionalStartElement(
          writer, this, HTML.SPAN_ELEM, HTML.COMMON_PASSTROUGH_ATTRIBUTES);
      }
      Translator translator = getTranslator();
      if (escape) // text is plain text
      {
        renderPlainText(text, writer, translator);
      }
      else // text is HTML, do not escape
      {
        renderHtmlText(text, writer, translator);
      }
      if (span)
      {
        writer.endElement(HTML.SPAN_ELEM);
      }
    }
  }

  private void renderPlainText(String text, 
    ResponseWriter writer, Translator translator) throws IOException
  {
    String textToRender = null;
    if (translator != null)
    {
      String userLanguage = FacesUtils.getViewLanguage();
      String translationGroup = getTranslationGroup();
      StringWriter sw = new StringWriter();
      translator.translate(new StringReader(text), sw, "text/plain",
        userLanguage, translationGroup);
      textToRender = sw.toString();
    }
    else textToRender = text;

    String lines[] = textToRender.split("\n");
    if (lines.length > 0)
    {
      writer.writeText(lines[0], JSFAttr.VALUE_ATTR);
      for (int i = 1; i < lines.length; i++)
      {
        writer.startElement("br", this);
        writer.endElement("br");
        writer.writeText(lines[i], JSFAttr.VALUE_ATTR);
      }
    }
  }
  
  private void renderHtmlText(String text, 
    ResponseWriter writer, Translator translator) throws IOException
  {
    if (translator != null)
    {
      String userLanguage = FacesUtils.getViewLanguage();
      String translationGroup = getTranslationGroup();
      translator.translate(new StringReader(text),
        writer, "text/html", userLanguage, translationGroup);
    }
    else writer.write(text);
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
}
