package org.santfeliu.faces.component;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.HtmlRenderUtils;
import org.santfeliu.faces.Translator;

/**
 *
 * @author lopezrj
 */
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
    ValueBinding vb = getValueBinding("translator");
    return vb != null? (Translator) vb.getValue(getFacesContext()): null;
  }

  public void setTranslationGroup(String translationGroup)
  {
    this._translationGroup = translationGroup;
  }

  public String getTranslationGroup()
  {
    if (_translationGroup != null)
      return _translationGroup;
    ValueBinding vb = getValueBinding("translationGroup");
    return vb != null? (String) vb.getValue(getFacesContext()): null;
  }

  public void encodeBegin(FacesContext context) throws IOException
  {
    if (context == null)
      throw new NullPointerException();
   
    if (!isRendered()) 
      return;

    ResponseWriter writer = context.getResponseWriter();
    
    HtmlRenderUtils.renderOverlay(writer);

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
    if (getAlt() != null)
    {
      writer.writeAttribute("alt", translate(getAlt()), null);
    }
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
