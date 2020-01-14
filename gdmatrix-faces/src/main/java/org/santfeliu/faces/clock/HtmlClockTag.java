package org.santfeliu.faces.clock;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentTag;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;

/**
 *
 * @author realor
 */
public class HtmlClockTag extends UIComponentTag
{
  private String style;
  private String styleClass;
  private String format;

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

  public String getFormat()
  {
    return format;
  }

  public void setFormat(String format)
  {
    this.format = format;
  }

  @Override
  public String getComponentType()
  {
    return "Clock";
  }

  @Override
  public String getRendererType()
  {
    return null;
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
      UIComponentTagUtils.setStringProperty(context, component, "format", format);
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
    format = null;
  }
}
