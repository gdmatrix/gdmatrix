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
package org.santfeliu.faces.widget;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import javax.el.ValueExpression;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.Translator;
import org.santfeliu.web.ApplicationBean;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author realor
 */
@FacesComponent(value = "HtmlWidget")
public class HtmlWidget extends UIPanel
{
  private String _style;
  private String _styleClass;
  private String _externalTitle;
  private Boolean _ariaHidden;
  private String _contentType;
  private String _ariaDescription;
  private String _editText;

  public HtmlWidget()
  {
    setRendererType(null);
  }

  @Override
  public String getFamily()
  {
    return "Widget";
  }

  @Override
  public boolean getRendersChildren()
  {
    return false;
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

  public void setExternalTitle(String externalTitle)
  {
    this._externalTitle = externalTitle;
  }

  public String getExternalTitle()
  {
    if (_externalTitle != null) return _externalTitle;
    ValueExpression ve = getValueExpression("externalTitle");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setAriaDescription(String ariaDescription) 
  {
    this._ariaDescription = ariaDescription;
  }

  public String getAriaDescription() 
  {
    if (_ariaDescription != null) return _ariaDescription;
    ValueExpression ve = getValueExpression("ariaDescription");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : 
      null;
  }

  public void setEditText(String editText) 
  {
    this._editText = editText;
  }

  public String getEditText() 
  {
    if (_editText != null) return _editText;
    ValueExpression ve = getValueExpression("editText");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : 
      "Edita widget";
  }
  
  public void setContentType(String contentType)
  {
    this._contentType = contentType;
  }

  public String getContentType()
  {
    if (_contentType != null) return _contentType;
    ValueExpression ve = getValueExpression("contentType");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
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
  
  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();
    // begin widget
    writer.startElement("div", this);
    String id = getId();
    if (id != null)
    {
      writer.writeAttribute("id", id, null);
    }
    // class attribute
    String classAttr = "";
    if (getStyleClass() != null)
    {
      classAttr += getStyleClass();
    }
    if (getContentType() != null)
    {
      classAttr += ((classAttr.isEmpty() ? "type_" : " type_") + getContentType());
    }
    if (!classAttr.isEmpty())
    {
      writer.writeAttribute("class", classAttr, null);
    }
    
    if (getAriaHidden())
    {
      writer.writeAttribute("aria-hidden", "true", null);
    }
    
    //Widget title
    if (getExternalTitle() != null)
    {
      writer.startElement("div", this);
      writer.writeAttribute("class", "externalTitle", null);
      renderHtmlText(getExternalTitle(), writer, 
        ApplicationBean.getCurrentInstance().getTranslator());
      writer.endElement("div");
    }    
    
    // widget padding
    writer.startElement("div", this);
    writer.writeAttribute("class", getStyleClass() + "_padding", null);

    // widget background
    writer.startElement("div", this);
    writer.writeAttribute("class", getStyleClass() + "_background", null);
    String style = getStyle();
    if (style != null)
    {
      writer.writeAttribute("style", style, null);
    }

    // header
    writer.startElement("div", this);
    writer.writeAttribute("id", "header_" + getId(), null);
    writer.writeAttribute("class", getStyleClass() + "_header", null);

    writer.startElement("div", this);
    writer.writeAttribute("class", getStyleClass() + "_header_2", null);

    writer.startElement("div", this);    

    UIComponent headerFacet = getFacet("header");
    if (headerFacet != null)
    {
      writer.writeAttribute("class", getStyleClass() + "_header_3", null);
      RendererUtils.renderChild(context, headerFacet);
    }
    else
    {
      writer.writeAttribute("class", getStyleClass() + "_header_3 empty", null);
    }
    writer.endElement("div"); // header_3
    writer.endElement("div"); // header_2
    writer.endElement("div"); // header

    // begin content
    writer.startElement("div", this);
    writer.writeAttribute("id", "content_" + getId(), null);
    writer.writeAttribute("class", getStyleClass() + "_content", null);

    writer.startElement("div", this);
    writer.writeAttribute("class", getStyleClass() + "_content_2", null);

    writer.startElement("div", this);
        
    boolean folded = false;
    if (this.getAttributes().containsKey("folded"))
    {
      folded = (Boolean)this.getAttributes().get("folded");
    }
    if (!folded)
    {      
      writer.writeAttribute("class", getStyleClass() + "_content_3", null);
    }
    else
    {
      writer.writeAttribute("class", getStyleClass() + "_content_3 empty", 
        null);      
    }
    
  }

  @Override
  public void encodeEnd(FacesContext context) throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();
    // end content
    writer.endElement("div"); // content_3
    writer.endElement("div"); // content_2
    writer.endElement("div"); // content
    
    // edit icon
    String nodeId = null;
    if (this.getAttributes().containsKey("nodeId"))
    {
      nodeId = (String)this.getAttributes().get("nodeId");
    }
    if (nodeId != null)
    {      
      String editLinkDescription = 
        translate(getEditText() + ": " + getAriaDescription());
      writer.startElement("div", this);
      writer.writeAttribute("id", "content_edit_" + getId(), null);
      writer.writeAttribute("class", "widget_edit", null);
      writer.startElement("a", this);
      writer.writeAttribute("id", "content_edit_link_" + getId(), null);
      writer.writeAttribute("title", editLinkDescription, null);
      writer.writeAttribute("href", "/go.faces?xmid=" + nodeId + "&" + 
        UserSessionBean.VIEW_MODE_PARAM + "=" + UserSessionBean.VIEW_MODE_EDIT, null);
      writer.writeAttribute("class", "widget_edit_link", null);
      writer.startElement("img", this);
      writer.writeAttribute("alt", editLinkDescription, null);
      writer.writeAttribute("src", "/themes/default/images/edit.png", null);
      writer.endElement("img");
      writer.endElement("a");
      writer.endElement("div");
    }

    // footer
    writer.startElement("div", this);
    writer.writeAttribute("id", "footer_" + getId(), null);
    writer.writeAttribute("class", getStyleClass() + "_footer", null);

    writer.startElement("div", this);
    writer.writeAttribute("class", getStyleClass() + "_footer_2", null);

    writer.startElement("div", this);   

    UIComponent footerFacet = getFacet("footer");
    if (footerFacet != null)
    {
      boolean folded = false;
      if (this.getAttributes().containsKey("folded"))
      {
        folded = (Boolean)this.getAttributes().get("folded");
      }
      if (!folded)
      {
        writer.writeAttribute("class", getStyleClass() + "_footer_3", null);
      }
      else
      {
        writer.writeAttribute("class", getStyleClass() + "_footer_3 empty", 
          null);
      }      
      RendererUtils.renderChild(context, footerFacet);
    }
    else
    {
      writer.writeAttribute("class", getStyleClass() + "_footer_3 empty", null);
    }
    writer.endElement("div"); // footer_3
    writer.endElement("div"); // footer_2
    writer.endElement("div"); // footer

    // end widget background
    writer.endElement("div");
    
    // end widget padding
    writer.endElement("div");

    // end widget
    writer.endElement("div");
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[8];
    values[0] = super.saveState(context);
    values[1] = _style;
    values[2] = _styleClass;
    values[3] = _externalTitle;
    values[4] = _ariaHidden;
    values[5] = _contentType;
    values[6] = _ariaDescription;
    values[7] = _editText;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _style = (String)values[1];
    _styleClass = (String)values[2];
    _externalTitle = (String)values[3];
    _ariaHidden = (Boolean)values[4];
    _contentType = (String)values[5];
    _ariaDescription = (String)values[6];
    _editText = (String)values[7];
  }
  
  private void renderHtmlText(String text, ResponseWriter writer, 
    Translator translator) throws IOException
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
  
  private Translator getTranslator()
  {
    return UserSessionBean.getCurrentInstance().getTranslator();
  }
  
  private String getTranslationGroup()
  {
    return UserSessionBean.getCurrentInstance().getTranslationGroup();
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
