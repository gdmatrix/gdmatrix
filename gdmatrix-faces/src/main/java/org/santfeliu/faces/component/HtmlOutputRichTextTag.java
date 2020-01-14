package org.santfeliu.faces.component;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentTag;

import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;


public class HtmlOutputRichTextTag extends UIComponentTag
{
  private String value;
  
  // extended properties
  private String title;
  private String escape;

  // style properties
  private String style;
  private String styleClass;

  public HtmlOutputRichTextTag()
  {
  }
  
  public String getComponentType()
  {
    return "javax.faces.HtmlOutputText";
  }
  
  public String getRendererType()
  {
    return "javax.faces.RichText";
  }

  public void setValue(String value)
  {
    this.value = value;
  }

  public String getValue()
  {
    return value;
  }

  public void setStyle(String style)
  {
    this.style = style;
  }

  public String getStyle()
  {
    return style;
  }

  public void setStyleClass(String styleClass)
  {
    this.styleClass = styleClass;
  }

  public String getStyleClass()
  {
    return styleClass;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public String getTitle()
  {
    return title;
  }

  public void setEscape(String escape)
  {
    this.escape = escape;
  }

  public String getEscape()
  {
    return escape;
  }

  protected void setProperties(UIComponent component)
  {
    try
    {
      FacesContext context = FacesContext.getCurrentInstance();
      super.setProperties(component);

      UIComponentTagUtils.setValueProperty(context, component, value);
      UIComponentTagUtils.setStringProperty(context, component, 
        "title", title);
      UIComponentTagUtils.setBooleanProperty(context, component, 
        "escape", escape);
      UIComponentTagUtils.setStringProperty(context, component, 
        "style", style);
      UIComponentTagUtils.setStringProperty(context, component, 
        "styleClass", styleClass);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  public void release()
  {
    super.release();
    value = null;
    title = null;
    escape = null;
    style = null;
    styleClass = null;
  }
}
