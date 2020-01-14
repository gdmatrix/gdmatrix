package org.santfeliu.faces.component;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.faces.event.ActionEvent;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.Translator;

/**
 *
 * @author realor
 */
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
    ValueBinding vb = getValueBinding("translator");
    return vb != null ? (Translator)vb.getValue(getFacesContext()) : null;
  }

  public void setTranslationGroup(String translationGroup)
  {
    this._translationGroup = translationGroup;
  }

  public String getTranslationGroup()
  {
    if (_translationGroup != null) return _translationGroup;
    ValueBinding vb = getValueBinding("translationGroup");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setRenderBox(Boolean _renderBox)
  {
    this._renderBox = _renderBox;
  }

  public Boolean getRenderBox()
  {
    if (_renderBox != null) return _renderBox;
    ValueBinding vb = getValueBinding("renderBox");
    return vb != null ? (Boolean)vb.getValue(getFacesContext()) : null;
  }

  public void setAriaLabel(String _ariaLabel)
  {
    this._ariaLabel = _ariaLabel;
  }  
  
  public String getAriaLabel()
  {
    if (_ariaLabel != null) return _ariaLabel;
    ValueBinding vb = getValueBinding("ariaLabel");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
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
