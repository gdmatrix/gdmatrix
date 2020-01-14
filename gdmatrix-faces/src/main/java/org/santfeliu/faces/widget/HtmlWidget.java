package org.santfeliu.faces.widget;

import java.io.IOException;
import java.io.StringReader;
import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.Translator;
import org.santfeliu.web.ApplicationBean;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author realor
 */
public class HtmlWidget extends UIPanel
{
  private String _style;
  private String _styleClass;
  private String _externalTitle;
  private Boolean _ariaHidden;
  private String _contentType;

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
    ValueBinding vb = getValueBinding("style");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setStyleClass(String styleClass)
  {
    this._styleClass = styleClass;
  }

  public String getStyleClass()
  {
    if (_styleClass != null) return _styleClass;
    ValueBinding vb = getValueBinding("styleClass");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setExternalTitle(String externalTitle)
  {
    this._externalTitle = externalTitle;
  }

  public String getExternalTitle()
  {
    if (_externalTitle != null) return _externalTitle;
    ValueBinding vb = getValueBinding("externalTitle");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }
  
  public String getContentType()
  {
    if (_contentType != null) return _contentType;
    ValueBinding vb = getValueBinding("contentType");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setContentType(String contentType)
  {
    this._contentType = contentType;
  }
  
  public void setAriaHidden(Boolean ariaHidden)
  {
    this._ariaHidden = ariaHidden;
  }

  public Boolean getAriaHidden()
  {
    if (_ariaHidden != null) return _ariaHidden;
    ValueBinding vb = getValueBinding("ariaHidden");
    return (vb != null ? (Boolean)vb.getValue(getFacesContext()) : 
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
      writer.startElement("div", this);
      writer.writeAttribute("id", "content_edit_" + getId(), null);
      writer.writeAttribute("class", "widget_edit", null);
      writer.startElement("a", this);
      writer.writeAttribute("id", "content_edit_link_" + getId(), null);      
      writer.writeAttribute("href", "/go.faces?xmid=" + nodeId + "&" + 
        UserSessionBean.VIEW_MODE_PARAM + "=" + UserSessionBean.VIEW_MODE_EDIT, null);
      writer.writeAttribute("class", "widget_edit_link", null);
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
    Object values[] = new Object[6];
    values[0] = super.saveState(context);
    values[1] = _style;
    values[2] = _styleClass;
    values[3] = _externalTitle;
    values[4] = _ariaHidden;
    values[5] = _contentType;
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
  
  private String getTranslationGroup()
  {
    return "widgetExternalTitle"; //TODO
  }  
  
}
