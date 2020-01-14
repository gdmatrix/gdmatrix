package org.santfeliu.faces.langselector;

import java.io.IOException;

import java.util.Iterator;

import java.util.List;
import java.util.Locale;

import java.util.Map;

import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.HtmlRenderUtils;

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
    ValueBinding vb = getValueBinding("locales");
    return vb != null ? (List)vb.getValue(getFacesContext()) : null;
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
      Locale locale = new Locale(language);
      context.getViewRoot().setLocale(locale);
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
