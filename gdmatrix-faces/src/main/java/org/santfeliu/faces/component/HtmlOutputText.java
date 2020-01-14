package org.santfeliu.faces.component;

import java.io.IOException;

import java.io.StringReader;
import java.io.StringWriter;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;

import org.apache.myfaces.shared_tomahawk.renderkit.JSFAttr;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;

import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.Translator;


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
