package org.santfeliu.faces.browser;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;

import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;


public class HtmlBrowserTag extends UIComponentTag
{
  private String url;
  private String port; // port to enable http connections
  private String iframe;
  private String width;
  private String height;
  private String translator;
  private String translationGroup;
  private String allowedHtmlTags;
  private String readTimeout;

  public HtmlBrowserTag()
  {
  }

  public String getComponentType()
  {
    return "Browser";
  }
  
  public String getRendererType()
  {
    return null;
  }

  public void setUrl(String url)
  {
    this.url = url;
  }

  public String getUrl()
  {
    return url;
  }

  public String getPort()
  {
    return port;
  }

  public void setPort(String port)
  {
    this.port = port;
  }

  public void setIframe(String iframe)
  {
    this.iframe = iframe;
  }

  public String getIframe()
  {
    return iframe;
  }

  public void setWidth(String width)
  {
    this.width = width;
  }

  public String getWidth()
  {
    return width;
  }

  public void setHeight(String height)
  {
    this.height = height;
  }

  public String getHeight()
  {
    return height;
  }

  public void setTranslator(String translator)
  {
    this.translator = translator;
  }

  public String getTranslator()
  {
    return translator;
  }

  public void setTranslationGroup(String translationGroup)
  {
    this.translationGroup = translationGroup;
  }

  public String getTranslationGroup()
  {
    return translationGroup;
  }

  public String getAllowedHtmlTags()
  {
    return allowedHtmlTags;
  }

  public void setAllowedHtmlTags(String allowedHtmlTags)
  {
    this.allowedHtmlTags = allowedHtmlTags;
  }

  public String getReadTimeout()
  {
    return readTimeout;
  }

  public void setReadTimeout(String readTimeout)
  {
    this.readTimeout = readTimeout;
  }

  @Override
  protected void setProperties(UIComponent component)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    super.setProperties(component);
    UIComponentTagUtils.setStringProperty(context, component, "url", url);
    UIComponentTagUtils.setStringProperty(context, component, "port", port);
    UIComponentTagUtils.setBooleanProperty(context, component, 
      "iframe", iframe);
    UIComponentTagUtils.setStringProperty(context, component, "width", width);
    UIComponentTagUtils.setStringProperty(context, component, "height", height);    
    UIComponentTagUtils.setStringProperty(context, component, 
      "translationGroup", translationGroup);
    if (translator != null)
    {
      if (isValueReference(translator))
      {
        ValueBinding vb = context.getApplication().
          createValueBinding(translator);
        component.setValueBinding("translator", vb);
      }
    }
    UIComponentTagUtils.setStringProperty(context, component, "allowedHtmlTags", allowedHtmlTags); 
    UIComponentTagUtils.setIntegerProperty(context, component, "readTimeout", readTimeout);
  }

  @Override
  public void release()
  {
    super.release();
    url = null;
    port = null;
    iframe = null;
    width = null;
    height = null;
    translator = null;
    translationGroup = null;
    allowedHtmlTags = null;
    readTimeout = null;
  }
}
