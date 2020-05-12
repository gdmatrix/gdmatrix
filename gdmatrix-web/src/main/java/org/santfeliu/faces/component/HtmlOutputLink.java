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
import org.santfeliu.faces.HtmlRenderUtils;
import org.santfeliu.faces.Translator;

/**
 *
 * @author blanquepa
 */
@FacesComponent(value = "HtmlOutputLink")
public class HtmlOutputLink 
  extends javax.faces.component.html.HtmlOutputLink
{
  private Translator _translator;
  private String _translationGroup; 
  private String _ariaLabel;
  private Boolean _ariaHidden;
  private String _role;  
  
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

  public void setAriaLabel(String _ariaLabel)
  {
    this._ariaLabel = _ariaLabel;
  }
  
  public String getAriaLabel()
  {
    if (_ariaLabel != null)
      return _ariaLabel;
    ValueExpression ve = getValueExpression("ariaLabel");
    return ve != null? (String) ve.getValue(getFacesContext().getELContext()): null;
  }
  
  public void setAriaHidden(Boolean ariaHidden)
  {
    this._ariaHidden = ariaHidden;
  }

  public Boolean getAriaHidden()
  {
    if (_ariaHidden != null) return _ariaHidden;
    ValueExpression ve = getValueExpression("ariaHidden");
    return (ve != null ? (Boolean)ve.getValue(getFacesContext().getELContext()) : 
      Boolean.FALSE);
  }

  public String getRole()
  {
    if (_role != null)
      return _role;
    ValueExpression ve = getValueExpression("role");
    return ve != null? (String) ve.getValue(getFacesContext().getELContext()): null;
  }

  public void setRole(String role)
  {
    this._role = role;
  }
  
  public void encodeBegin(FacesContext context) throws IOException
  {
    if (context == null)
      throw new NullPointerException();
   
    if (!isRendered()) 
      return;

    ResponseWriter writer = context.getResponseWriter();
    
    HtmlRenderUtils.renderOverlay(writer);

    writer.startElement("a", this);

    Object value = getValue();
    writer.writeAttribute("href", (value != null ? value : ""), null);

    if (getId() != null && !getId().startsWith(UIViewRoot.UNIQUE_ID_PREFIX))    
    {
      writer.writeAttribute("id", getId(), null);
    }
    if (getStyle() != null)
    {
      writer.writeAttribute("style", getStyle(), null);
    }
    if (getStyleClass() != null)
    {
      writer.writeAttribute("class", getStyleClass(), null);
    }
    String ariaLabel = getAriaLabel();
    if (ariaLabel != null)
    {
      writer.writeAttribute("aria-label", translate(ariaLabel), null);
    }    
    if (getAriaHidden())
    {
      writer.writeAttribute("aria-hidden", "true", null);
    }
    String role = getRole();
    if (role != null)
    {
      writer.writeAttribute("role", role, null);
    }    
    if (getAccesskey() != null)
    {
      writer.writeAttribute("accesskey", getAccesskey(), null);
    }
    if (getTabindex()!= null)
    {
      writer.writeAttribute("tabindex", getTabindex(), null);
    }    
    if (getTarget() != null)
    {
      writer.writeAttribute("target", getTarget(), null);
    }
    if (getOnclick() != null)
    {
      writer.writeAttribute("onclick", getOnclick(), null);
    }
    if (getOnfocus() != null)
    {
      writer.writeAttribute("onfocus", getOnclick(), null);
    } 
    if (getTitle() != null)
    {
      writer.writeAttribute("title", translate(getTitle()), null);
    }   
   
  } 
 
  @Override
  public void encodeEnd(FacesContext context) throws IOException
  {
    if (!isRendered()) return;    
    ResponseWriter writer = context.getResponseWriter();
    writer.endElement("a");
  }  

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[5];
    values[0] = super.saveState(context);
    values[1] = _translationGroup;
    values[2] = _ariaLabel; 
    values[3] = _ariaHidden;   
    values[4] = _role;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[]) state;
    super.restoreState(context, values[0]);
    _translationGroup = (String)values[1];
    _ariaLabel = (String)values[2];
    _ariaHidden = (Boolean)values[3];
    _role = (String)values[4];
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
