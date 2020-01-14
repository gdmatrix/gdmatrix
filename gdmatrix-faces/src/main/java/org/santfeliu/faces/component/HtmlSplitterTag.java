package org.santfeliu.faces.component;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentTag;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;

/**
 *
 * @author realor
 */
public class HtmlSplitterTag extends UIComponentTag
{
  private String orientation;
  private String stretch;
  private String style;
  private String styleClass;
  private String firstStyle;
  private String firstStyleClass;
  private String lastStyle;
  private String lastStyleClass;

  @Override
  public String getComponentType()
  {
    return "Splitter";
  }

  @Override
  public String getRendererType()
  {
    return null;
  }

  public String getOrientation()
  {
    return orientation;
  }

  public void setOrientation(String orientation)
  {
    this.orientation = orientation;
  }

  public String getStretch()
  {
    return stretch;
  }

  public void setStretch(String stretch)
  {
    this.stretch = stretch;
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

  public String getFirstStyle()
  {
    return firstStyle;
  }

  public void setFirstStyle(String firstStyle)
  {
    this.firstStyle = firstStyle;
  }

  public String getFirstStyleClass()
  {
    return firstStyleClass;
  }

  public void setFirstStyleClass(String firstStyleClass)
  {
    this.firstStyleClass = firstStyleClass;
  }

  public String getLastStyle()
  {
    return lastStyle;
  }

  public void setLastStyle(String lastStyle)
  {
    this.lastStyle = lastStyle;
  }

  public String getLastStyleClass()
  {
    return lastStyleClass;
  }

  public void setLastStyleClass(String lastStyleClass)
  {
    this.lastStyleClass = lastStyleClass;
  }

  @Override
  protected void setProperties(UIComponent component)
  {
    try
    {
      FacesContext context = FacesContext.getCurrentInstance();
      super.setProperties(component);

      UIComponentTagUtils.setStringProperty(context, component,
        "orientation", orientation);
      UIComponentTagUtils.setStringProperty(context, component,
        "stretch", stretch);
      UIComponentTagUtils.setStringProperty(context, component,
        "style", style);
      UIComponentTagUtils.setStringProperty(context, component,
        "styleClass", styleClass);
      UIComponentTagUtils.setStringProperty(context, component,
        "firstStyle", firstStyle);
      UIComponentTagUtils.setStringProperty(context, component,
        "firstStyleClass", firstStyleClass);
      UIComponentTagUtils.setStringProperty(context, component,
        "lastStyle", lastStyle);
      UIComponentTagUtils.setStringProperty(context, component,
        "lastStyleClass", lastStyleClass);
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
    orientation = null;
    stretch = null;
    style = null;
    styleClass = null;
    firstStyle = null;
    firstStyleClass = null;
    lastStyle = null;
    lastStyleClass = null;
  }
}
