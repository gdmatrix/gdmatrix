package org.santfeliu.faces.widget;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentTag;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;

/**
 *
 * @author realor
 */
public class HtmlWidgetTag extends UIComponentTag
{
  private String style;
  private String styleClass;
  private String externalTitle;
  private String ariaHidden;
  private String contentType;

  @Override
  public String getComponentType()
  {
    return "Widget";
  }

  @Override
  public String getRendererType()
  {
    return null;
  }

  public String getStyle()
  {
    return style;
  }

  public void setStyle(String style)
  {
    this.style = style;
  }

  public String getStyleClass()
  {
    return styleClass;
  }

  public void setStyleClass(String styleClass)
  {
    this.styleClass = styleClass;
  }

  public String getExternalTitle()
  {
    return externalTitle;
  }

  public void setExternalTitle(String externalTitle)
  {
    this.externalTitle = externalTitle;
  }

  public String getAriaHidden()
  {
    return ariaHidden;
  }

  public void setAriaHidden(String ariaHidden)
  {
    this.ariaHidden = ariaHidden;
  }

  public String getContentType()
  {
    return contentType;
  }

  public void setContentType(String contentType)
  {
    this.contentType = contentType;
  }

  @Override
  protected void setProperties(UIComponent component)
  {
    try
    {
      FacesContext context = FacesContext.getCurrentInstance();
      super.setProperties(component);
      UIComponentTagUtils.setStringProperty(context, component, "style", style);
      UIComponentTagUtils.setStringProperty(context, component, "styleClass", styleClass);
      UIComponentTagUtils.setStringProperty(context, component, "externalTitle", externalTitle);
      UIComponentTagUtils.setBooleanProperty(context, component, "ariaHidden", ariaHidden);
      UIComponentTagUtils.setStringProperty(context, component, "contentType", contentType);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  @Override
  public void release()
  {
    super.release();
    style = null;
    styleClass = null;
    externalTitle = null;
    ariaHidden = null;
    contentType = null;
  }
}
