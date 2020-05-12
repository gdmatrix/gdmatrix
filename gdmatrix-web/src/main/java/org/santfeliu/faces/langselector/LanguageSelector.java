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
package org.santfeliu.faces.langselector;

import java.io.IOException;

import java.util.Iterator;

import java.util.List;
import java.util.Locale;

import java.util.Map;
import javax.el.ValueExpression;
import javax.faces.component.FacesComponent;

import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.HtmlRenderUtils;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author unknown
 */
@FacesComponent(value = "LanguageSelector")
public class LanguageSelector extends UIComponentBase
{
  private List _locales;
  private String _style;
  private String _styleClass;

  public LanguageSelector()
  {
  }
  
  public String getFamily()
  {
    return "LanguageSelector";
  }

  public void setLocales(List locales)
  {
    this._locales = locales;
  }

  public List getLocales()
  {
    if (_locales != null) return _locales;
    ValueExpression ve = getValueExpression("locales");
    return ve != null ? (List)ve.getValue(getFacesContext().getELContext()) : null;
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
  public void decode(FacesContext context)
  {
    if (!isRendered()) return;
    String clientId = getClientId(context);
    Map parameterMap = context.getExternalContext().getRequestParameterMap();
    boolean activated = "y".equals(parameterMap.get(clientId + ":act")) ||
      parameterMap.containsKey(clientId + ":actb");
    if (activated)
    {
      String language = (String)parameterMap.get(clientId + ":lang");
      //Locale locale = new Locale(language);
      //context.getViewRoot().setLocale(locale);      
      UserSessionBean.getCurrentInstance().setViewLanguage(language);
      context.renderResponse();
    }
  }

  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    try
    {
      Locale currentLocale = context.getViewRoot().getLocale();
      String clientId = getClientId(context);
      ResponseWriter writer = context.getResponseWriter();

      HtmlRenderUtils.renderOverlay(writer);

      writer.startElement("input", this);
      writer.writeAttribute("type", "hidden", null);
      writer.writeAttribute("name", clientId + ":act", null);
      writer.writeAttribute("value", "n", null);
      writer.endElement("input");
    
      writer.startElement("select", this);   
      HtmlRendererUtils.writeIdIfNecessary(writer, this, context);      
      writer.writeAttribute("name", clientId + ":lang", null);
      String formId = FacesUtils.getParentFormId(this, context);
      writer.writeAttribute("onchange",
        "showOverlay(); document.forms['" + formId + "']['" + clientId +
        ":act'].value='y'; document.forms['" + formId + "'].submit(); return false;"    
         , null);

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
      Iterator iter = null;
      List locales = getLocales();
      if (locales == null)
      {
        iter = context.getApplication().getSupportedLocales();
      }
      else
      {
        iter = locales.iterator();
      }
      while (iter.hasNext())
      {
        Locale locale = (Locale)iter.next();
        String language = locale.getLanguage();
        String displayLanguage = locale.getDisplayLanguage(locale).toLowerCase();
        writer.startElement("option", this);
        writer.writeAttribute("value", language, null);
        if (locale.equals(currentLocale))
        {
          writer.writeAttribute("selected", "selected", null);
        }
        writer.writeText(displayLanguage, null);
        writer.endElement("option");
      }
      writer.endElement("select");

      writer.startElement("noscript", this);
      writer.startElement("input", this);
      writer.writeAttribute("type", "submit", null);
      writer.writeAttribute("name", clientId + ":actb", null);
      writer.writeAttribute("value", ">", null);
      if (styleClass != null)
      {
        writer.writeAttribute("class", styleClass, null);
      }
      writer.endElement("input");
      writer.endElement("noscript");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[3];
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
