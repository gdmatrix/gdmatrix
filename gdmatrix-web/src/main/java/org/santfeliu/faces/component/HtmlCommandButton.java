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
import java.util.Map;
import javax.el.ValueExpression;
import javax.faces.component.FacesComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.ActionEvent;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.Translator;

/**
 *
 * @author realor
 */
@FacesComponent(value = "HtmlCommandButton")
public class HtmlCommandButton 
  extends javax.faces.component.html.HtmlCommandButton
{
  private Translator _translator;
  private String _translationGroup;
  private Boolean _renderBox;
  private String _ariaLabel;

  public HtmlCommandButton()
  {
    setRendererType(null);
  }

  @Override
  public boolean getRendersChildren()
  {
    return true;
  }

  public void setTranslator(Translator translator)
  {
    this._translator = translator;
  }

  public Translator getTranslator()
  {
    if (_translator != null) return _translator;
    ValueExpression ve = getValueExpression("translator");
    return ve != null ? (Translator)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setTranslationGroup(String translationGroup)
  {
    this._translationGroup = translationGroup;
  }

  public String getTranslationGroup()
  {
    if (_translationGroup != null) return _translationGroup;
    ValueExpression ve = getValueExpression("translationGroup");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setRenderBox(Boolean _renderBox)
  {
    this._renderBox = _renderBox;
  }

  public Boolean getRenderBox()
  {
    if (_renderBox != null) return _renderBox;
    ValueExpression ve = getValueExpression("renderBox");
    return ve != null ? (Boolean)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setAriaLabel(String _ariaLabel)
  {
    this._ariaLabel = _ariaLabel;
  }  
  
  public String getAriaLabel()
  {
    if (_ariaLabel != null) return _ariaLabel;
    ValueExpression ve = getValueExpression("ariaLabel");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  @Override
  public void decode(FacesContext context)
  {
    String clientId = getClientId(context);
    Map parameters = context.getExternalContext().getRequestParameterMap();
    if (parameters.containsKey(clientId))
    {
      queueEvent(new ActionEvent(this));
    }
  }

  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    if (!isRendered()) return;  
    boolean renderBox = Boolean.TRUE.equals(getRenderBox()) ||
      getChildren().size() > 0;
    if (renderBox)
    {
      // encode 3 divs around input tag
      ResponseWriter writer = context.getResponseWriter();
      writer.startElement("span", this);
      encodeStyles(writer);
      writer.startElement("span", this);
      writer.startElement("span", this);
      writer.startElement("span", this);
    }
  }

  @Override
  public void encodeChildren(FacesContext context) throws IOException
  {
    RendererUtils.renderChildren(context, this);
  }

  @Override
  public void encodeEnd(FacesContext context) throws IOException
  {
    if (!isRendered()) return;  
    String clientId = getClientId(context);
    ResponseWriter writer = context.getResponseWriter();
    boolean renderBox = Boolean.TRUE.equals(getRenderBox()) ||
      getChildren().size() > 0;

    // encode input element
    writer.startElement("input", this);
    HtmlRendererUtils.writeIdIfNecessary(writer, this, context);
    writer.writeAttribute("type", "submit", null);
    writer.writeAttribute("name", clientId, null);
    if (!renderBox)
    {
      encodeStyles(writer);
    }
    // write button value
    Object value = getValue();
    if (value != null)
    {
      String text = value.toString();
      text = translate(text);
      writer.writeAttribute("value", text, null);
    }
    // write button title
    String title = getTitle();
    if (title != null)
    {
      title = translate(title);
      writer.writeAttribute("title", title, null);
    }
    // write button onclick action
    String onclick = getOnclick();
    if (onclick != null)
    {
      writer.writeAttribute("onclick", onclick, null);
    }
    // write aria attributes
    encodeAriaAttributes(writer);    
    writer.endElement("input");
    if (renderBox)
    {
      // close box
      writer.endElement("span");
      writer.endElement("span");
      writer.endElement("span");
      writer.endElement("span");
    }
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[4];
    values[0] = super.saveState(context);
    values[1] = _translationGroup;
    values[2] = _renderBox;
    values[3] = _ariaLabel;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[]) state;
    super.restoreState(context, values[0]);
    _translationGroup = (String)values[1];
    _renderBox = (Boolean)values[2];
    _ariaLabel = (String)values[3];
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

  private void encodeStyles(ResponseWriter writer) throws IOException
  {
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
  
  private void encodeAriaAttributes(ResponseWriter writer) throws IOException
  {
    String ariaLabel = getAriaLabel();
    if (ariaLabel != null)
    {
      writer.writeAttribute("aria-label", ariaLabel, null);
    }    
  }
}
