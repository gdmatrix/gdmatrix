package org.santfeliu.faces.signer;

import java.io.IOException;

import java.util.Locale;
import java.util.Map;

import java.util.ResourceBundle;

import javax.faces.component.UICommand;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.faces.event.ActionEvent;

import javax.servlet.http.HttpServletRequest;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.web.HttpUtils;

public class HtmlSigner extends UICommand
{
  private String _document;
  private String _result;
  private String _port;
  private String _localResult;
  
  private String _style;
  private String _styleClass;

  public HtmlSigner()
  {
  }
  
  @Override
  public String getFamily()
  {
    return "Signer";
  }

  public void setDocument(String document)
  {
    this._document = document;
  }

  public String getDocument()
  {
    if (_document != null) return _document;
    ValueBinding vb = getValueBinding("document");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setResult(String result)
  {
    this._result = result;
  }

  public String getResult()
  {
    if (_result != null) return _result;
    ValueBinding vb = getValueBinding("result");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setPort(String port)
  {
    this._port = port;
  }

  public String getPort()
  {
    if (_port != null) return _port;
    ValueBinding vb = getValueBinding("port");
    return vb != null ? (String)vb.getValue(getFacesContext()) : "443";
  }
  
  public String getLocalResult()
  {
    return _localResult;
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
    try
    {
      String clientId = getClientId(context);
      Map parameterMap = context.getExternalContext().getRequestParameterMap();
      Object value = parameterMap.get(clientId);
      if (value != null)
      {
        String valueString = String.valueOf(value).trim();
        if (valueString.length() > 0)
        {
          queueEvent(new ActionEvent(this));
          _localResult = valueString;
        }
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    try
    {
      Locale locale = context.getViewRoot().getLocale();
      ResourceBundle bundle = ResourceBundle.getBundle(
        "org.santfeliu.faces.signer.resources.SignerBundle", locale);

      String clientId = getClientId(context);
      ResponseWriter writer = context.getResponseWriter();

      String formId = FacesUtils.getParentFormId(this, context);
      HttpServletRequest request = 
        (HttpServletRequest)context.getExternalContext().getRequest();
      String serverName = HttpUtils.getServerName(request);
      String contextPath = request.getContextPath();

      // encode applet
      writer.startElement("applet", this);
      writer.writeAttribute("name", "MicroSigner", null);

      //TODO: depends on MicroSigner folder name
      String codebaseURL = "https://" + serverName + ":" + getPort() +
        contextPath + "/plugins/microsigner";

      writer.writeAttribute("codebase", codebaseURL, null);
      writer.writeAttribute("archive",
        "microsigner.jar,CAPIProv.jar", null);
      writer.writeAttribute("code", 
        "org.santfeliu.signature.microsigner.MicroSigner", null);
      writer.writeAttribute("width", "100%", null);
      writer.writeAttribute("height", "150", null);

      writer.startElement("param", this);
      writer.writeAttribute("name", "sigId", null);
      writer.writeAttribute("value", getDocument(), null);
      writer.endElement("param");

      String servletURL = "https://" + serverName + ":" + getPort() +
        contextPath + "/signatures";

      writer.startElement("param", this);
      writer.writeAttribute("name", "servletURL", null);
      writer.writeAttribute("value", servletURL, null);
      writer.endElement("param");

      String language = context.getViewRoot().getLocale().getLanguage();
      writer.startElement("param", this);
      writer.writeAttribute("name", "language", null);
      writer.writeAttribute("value", language, null);
      writer.endElement("param");

      // no java message
      writer.startElement("h5", this);
      writer.writeText(bundle.getString("nojava"), null);
      writer.startElement("a", this);
      writer.writeAttribute("href", "http://www.java.com", null);
      writer.writeAttribute("target", "_blank", null);
      writer.writeText("http://www.java.com", null);
      writer.endElement("a");
      writer.endElement("h5");

      writer.endElement("applet");

      // encode javascript callback function
      writer.startElement("script", this);
      writer.writeAttribute("type", "text/javascript", null);
      writer.writeText(
        "function signerCallback(){" +
        "var result=document.applets['MicroSigner'].getSignResult(); " +
        "document.forms['" + formId + "']['" + clientId + "'].value=result; " + 
        "document.forms['" + formId + "'].submit(); return false;}", null);
      writer.endElement("script");

      // encode buttons
      writer.startElement("div", this);
      writer.writeAttribute("style", "text-align:center", null);

      writer.startElement("input", this);
      writer.writeAttribute("type", "hidden", null);
      writer.writeAttribute("name", clientId, null);
      writer.writeAttribute("value", "", null);
      writer.endElement("input");

      writer.startElement("input", this);
      writer.writeAttribute("type", "button", null);
      writer.writeAttribute("value", bundle.getString("sign"), null);

      writer.writeAttribute("onclick",
        "document.applets['MicroSigner'].signDocument('signerCallback'); this.disabled=true; return false;", null);
      writer.endElement("input");

      writer.startElement("input", this);
      writer.writeAttribute("type", "button", null);
      writer.writeAttribute("value", bundle.getString("abort"), null);
      writer.writeAttribute("onclick",
        "document.forms['" + formId + "']['" + 
        clientId + "'].value='CANCEL'; document.forms['" + formId + 
        "'].submit(); return false;", null);
      writer.endElement("input");

      writer.endElement("div");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  @Override
  public void encodeEnd(FacesContext context) throws IOException
  {    
  }

  @Override
  public void processUpdates(FacesContext context)
  {
    if (context == null) throw new NullPointerException("context");
    if (!isRendered()) return;
    super.processUpdates(context);
    try
    {
      updateModel(context);
    }
    catch (RuntimeException e)
    {
      context.renderResponse();
      throw e;
    }
  }

  public void updateModel(FacesContext context)
  {
    ValueBinding vb = getValueBinding("result");
    if (vb == null) return;
    try
    {
      vb.setValue(context, getLocalResult());
    }
    catch (RuntimeException e)
    {
    }
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[7];
    values[0] = super.saveState(context);
    values[1] = _document;
    values[2] = _result;
    values[3] = _localResult;
    values[4] = _port;
    values[5] = _style;
    values[6] = _styleClass;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _document = (String)values[1];
    _result = (String)values[2];
    _localResult = (String)values[3];
    _port = (String)values[4];
    _style = (String)values[5];
    _styleClass = (String)values[6];
  }
}
