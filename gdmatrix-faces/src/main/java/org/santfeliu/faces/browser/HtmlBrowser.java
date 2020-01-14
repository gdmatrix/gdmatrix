package org.santfeliu.faces.browser;

import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import javax.faces.el.ValueBinding;

import javax.servlet.http.HttpServletRequest;

import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.Translator;
import org.santfeliu.faces.browser.encoder.ContentEncoder;
import org.santfeliu.util.net.HttpClient;
import org.santfeliu.web.HttpUtils;


public class HtmlBrowser extends UIComponentBase
{
  public static final String[][] mimeTypes = 
  {
    {"text/html", "org.santfeliu.faces.browser.encoder.HtmlEncoder"},
    {"text/xhtml", "org.santfeliu.faces.browser.encoder.HtmlEncoder"},    
    {"text/plain", "org.santfeliu.faces.browser.encoder.TextEncoder"},
    {"image/jpeg", "org.santfeliu.faces.browser.encoder.ImageEncoder"},
    {"image/gif", "org.santfeliu.faces.browser.encoder.ImageEncoder"},
    {"image/png", "org.santfeliu.faces.browser.encoder.ImageEncoder"},
    {"image/tiff", "org.santfeliu.faces.browser.encoder.ImageEncoder"}
  };
  
  private static final int DEFAULT_READ_TIMEOUT = 60000;
  
  private String _url;
  private String _port;
  private String _submittedUrl;
  private Boolean _iframe;
  private String _width;
  private String _height;
  private Translator _translator;
  private String _translationGroup;
  private String _allowedHtmlTags;
  private Integer _readTimeout;

  public HtmlBrowser()
  {
  }
  
  public String getFamily()
  {
    return "Browser";
  }

  public void setUrl(String url)
  {
    this._url = url;
  }

  public String getUrl()
  {
    if (_url != null) return _url;
    ValueBinding vb = getValueBinding("url");
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
    return vb != null ? (String)vb.getValue(getFacesContext()) : "80";
  }

  public String getSubmittedUrl()
  {
    return _submittedUrl;
  }

  public void setIframe(boolean iframe)
  {
    _iframe = Boolean.valueOf(iframe);
  }

  public boolean isIframe()
  {
    if (_iframe != null) return _iframe.booleanValue();
    ValueBinding vb = getValueBinding("iframe");
    Boolean v = vb != null ? (Boolean)vb.getValue(getFacesContext()) : null;
    return v != null ? v.booleanValue() : false;
  }

  public void setWidth(String width)
  {
    this._width = width;
  }

  public String getWidth()
  {
    if (_width != null) return _width;
    ValueBinding vb = getValueBinding("width");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setHeight(String height)
  {
    this._height = height;
  }

  public String getHeight()
  {
    if (_height != null) return _height;
    ValueBinding vb = getValueBinding("height");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public Integer getReadTimeout()
  {
    if (_readTimeout != null) return _readTimeout;
    ValueBinding vb = getValueBinding("readTimeout");
    return vb != null ? (Integer)vb.getValue(getFacesContext()) : null;
  }

  public void setReadTimeout(Integer _readTimeout)
  {
    this._readTimeout = _readTimeout;
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

  public String getAllowedHtmlTags()
  {
    if (_allowedHtmlTags != null) return _allowedHtmlTags;
    ValueBinding vb = getValueBinding("allowedHtmlTags");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;    
  }

  public void setAllowedHtmlTags(String _allowedHtmlTags)
  {
    this._allowedHtmlTags = _allowedHtmlTags;
  }
  
  @Override
  public void decode(FacesContext context)
  {
    try
    {
      if (!isRendered()) return;
      String formId = FacesUtils.getParentFormId(this, context);
      String clientId = getClientId(context);
      Map parameterMap = context.getExternalContext().getRequestParameterMap();

      _submittedUrl = null;
      String href = (String)parameterMap.get(clientId + ":href");
      
      if (href != null && href.length() > 0)
      {
        char type = href.charAt(0);
        href = href.substring(2);
        if (type == 'L') // it's a link
        {
          _submittedUrl = getAbsoluteHRef(href, false, false);
        }
        else if (type == 'F') //it's a form
        {
          _submittedUrl = getAbsoluteHRef(href, true, false);
          Set entrySet = parameterMap.entrySet();
          if (entrySet.size() > 2)
          {
            _submittedUrl += '?';
            Iterator iter = entrySet.iterator();
            while (iter.hasNext())
            {
              Map.Entry entry = (Map.Entry)iter.next();
              String parameter = (String)entry.getKey();
              String value = (String)entry.getValue();
              if (!parameter.startsWith(clientId) && 
                  !parameter.equals("com.sun.faces.VIEW") &&
                  !parameter.startsWith(formId))
              {
                _submittedUrl += parameter + "=" + value + "&";
              }
            }
          }
          if (_submittedUrl.endsWith("&"))
          {
            _submittedUrl = 
              _submittedUrl.substring(0, _submittedUrl.length() - 1);
          }
          _submittedUrl = _submittedUrl.replace(' ', '+');
        }
      }
    }
    catch (Exception ex)
    {
      context.renderResponse();
    }
  }
  
  @Override
  public void processValidators(FacesContext context)
  {
    if (context == null) throw new NullPointerException("context");
    if (!isRendered()) return;
    super.processValidators(context);
    if (_submittedUrl != null) // TODO: check submittedUrl validity
    {
      _url = _submittedUrl;
      _submittedUrl = null;
    }
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
    if (_url != null)
    {
      ValueBinding vb = getValueBinding("url");
      if (vb != null)
      {
        if (!vb.isReadOnly(context))
        {
          vb.setValue(context, _url);
          _url = null;
        }
      }
    }
  }

  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    String url = null;
    if (!isRendered()) return;
    ResponseWriter writer = context.getResponseWriter();
    try
    {
      url = getSubmittedUrl();
      if (url == null) url = getUrl();
      if (isIframe())
      {
        writer.startElement("iframe", this);
        writer.writeAttribute("src", url, null);
        String w = getWidth();
        if (w != null) writer.writeAttribute("width", w, null);
        String h = getHeight();
        if (h != null) writer.writeAttribute("height", h, null);

        writer.startElement("a", this);
        writer.writeAttribute("href", url, null);
        writer.writeAttribute("target", "_blank", null);
        writer.writeText(url, null);
        writer.endElement("a");

        writer.endElement("iframe");
      }
      else // embed document
      {  
        if (url.startsWith("/"))
        {
          url = "http://localhost" + url;
        }
        String userLanguage = getUserLanguage();

        HttpClient httpClient = new HttpClient();
        httpClient.setURL(url);
        httpClient.setForceHttp(true);
        httpClient.setHttpPort(Integer.parseInt(getPort()));
        httpClient.setDownloadContentType("text/");
        httpClient.setMaxContentLength(524288);
        httpClient.setRequestProperty("User-Agent", HttpClient.USER_AGENT_IE6);
        httpClient.setRequestProperty("Accept-Charset", "utf-8");
        httpClient.setRequestProperty("Accept-Language", userLanguage);
        Integer readTimeout = getReadTimeout();
        httpClient.setReadTimeout(readTimeout != null ? 
          readTimeout : DEFAULT_READ_TIMEOUT);
        httpClient.connect();
        url = httpClient.getLastURL();

        String mimeType = httpClient.getContentType();
        if (mimeType == null) throw new IOException("Connection error");

        String encoderClassName = getContentEncoder(mimeType);
        Class encoderClass = Class.forName(encoderClassName);
        ContentEncoder encoder = (ContentEncoder)encoderClass.newInstance();

        String contentLanguage = httpClient.getContentLanguage();
        Translator translator = null;
        String translationGroup = null;
        if (contentLanguage == null) // is multilingual
        {
          translator = getTranslator();
          translationGroup = getTranslationGroup();
        }
        encoder.encode(this, httpClient, writer, translator, translationGroup);
        String browserId = getClientId(context);
        writeHidden(writer, browserId + ":href", "");
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      FacesUtils.addMessage(this, "CAN_NOT_SHOW_DOCUMENT", null, 
        FacesMessage.SEVERITY_ERROR);
    }
  }

  public String getOnclickLink(String href) throws IOException
  {
    FacesContext context = FacesContext.getCurrentInstance();
    String formId = FacesUtils.getParentFormId(this, context);
    String browserId = getClientId(context);
  
    return "document.forms['" + formId + "']['" + browserId + 
           ":href'].value='L:" + href + "'; document.forms['" + formId +
           "'].submit(); return false;";
  }

  public String getOnclickSubmit(String href) throws IOException
  {
    FacesContext context = FacesContext.getCurrentInstance();
    String formId = FacesUtils.getParentFormId(this, context);
    String browserId = getClientId(context);
  
    return "document.forms['" + formId + "']['" + browserId + 
           ":href'].value='F:" + href + "'; document.forms['" + formId +
           "'].submit(); return false;";
  }

  public String getAbsoluteHRef(String href, 
    boolean removeParameters, boolean removeHost)
  {
    if (href.startsWith("#")) return href;
    if (href.startsWith("/")) return href;
    String url = null;
    URL u = null;
    try
    {
      u = new URL(href); // If this works then href is absolute
      url = u.toString();
    }
    catch (MalformedURLException m1) // if it's relative to url
    {
      try
      {
        url = _submittedUrl;
        if (url == null) url = getUrl();
        if (url.startsWith("/"))
        {
          int index = url.lastIndexOf("/");
          {
            if (index > 0)
            {
              url = url.substring(0, index + 1);
            }
          }
          return url + href;
        }

        u = new URL(url);
        u = new URL(u, href);

        HttpServletRequest request = 
          (HttpServletRequest) FacesContext.getCurrentInstance().
          getExternalContext().getRequest();
        if (removeHost &&
             (HttpUtils.getServerName(request).equals(u.getHost()) ||
               "localhost".equals(u.getHost())))
        {
          url = u.getFile();
        }
        else
        {
          url = u.toString();
        }
      }
      catch (MalformedURLException m2)
      {
        m2.printStackTrace();
      }
    }
    if (url != null && removeParameters)
    {
      int index = url.lastIndexOf("?");
      if (index != -1) url = url.substring(0, index);
    }
    return url;
  }
  
  public boolean hasAllowedTags()
  {
    return getAllowedHtmlTags() != null;
  }
  
  public boolean isHeadTagAllowed()
  {
    return (hasAllowedTags() && getAllowedHtmlTags().contains("head"));
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[10];
    values[0] = super.saveState(context);
    values[1] = _url;
    values[2] = _submittedUrl;
    values[3] = _port;
    values[4] = _iframe;
    values[5] = _width;
    values[6] = _height;
    values[7] = _translationGroup;
    values[8] = _allowedHtmlTags;
    values[9] = _readTimeout;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _url = (String)values[1];
    _submittedUrl = (String)values[2];
    _port = (String)values[3];
    _iframe = (Boolean)values[4];
    _width = (String)values[5];
    _height = (String)values[6];
    _translationGroup = (String)values[7];
    _allowedHtmlTags = (String)values[8];
    _readTimeout = (Integer)values[9];
  }

  //****** private methods ******

  private String getUserLanguage()
  {
    Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
    return locale.getLanguage();
  }

  private String getContentEncoder(String mimeType)
  {
    String className = null;
    int i = 0;
    while (className == null && i < mimeTypes.length)
    {
      String t = mimeTypes[i][0];
      if (mimeType.startsWith(t)) className = mimeTypes[i][1];
      else i++;
    }
    return className == null ? 
      "org.santfeliu.faces.browser.encoder.LinkEncoder" : className;
  }

  private void writeHidden(ResponseWriter writer, 
    String browserId, String actionURL) throws IOException
  {
    writer.startElement("input", this);
    writer.writeAttribute("type", "hidden", null);
    writer.writeAttribute("name", browserId, null); // actionURL
    writer.writeAttribute("value", actionURL, null);
    writer.endElement("input");
  }
}
