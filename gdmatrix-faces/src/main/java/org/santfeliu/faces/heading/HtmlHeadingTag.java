package org.santfeliu.faces.heading;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;

public class HtmlHeadingTag extends UIComponentTag
{
  private String level;
  private String style;
  private String styleClass;

  public HtmlHeadingTag()
  {
  }
  
  public String getComponentType()
  {
    return "Heading";
  }
  
  public String getRendererType()
  {
    return null;
  }

  public String getLevel()
  {
    return level;
  }

  public void setLevel(String level)
  {
    this.level = level;
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

  @Override
  protected void setProperties(UIComponent component)
  {
    try
    {
      FacesContext context = FacesContext.getCurrentInstance();
      Application application = context.getApplication();
      super.setProperties(component);
    
      HtmlHeading heading = (HtmlHeading)component;
      UIComponentTagUtils.setIntegerProperty(context, component, "level", level);
      if (style != null)
      {
        if (isValueReference(style))
        {
          ValueBinding vb = application.createValueBinding(style);
          heading.setValueBinding("style", vb);
        }
        else heading.getAttributes().put("style", style);
      }
      if (styleClass != null)
      {
        if (isValueReference(styleClass))
        {
          ValueBinding vb = application.createValueBinding(styleClass);
          heading.setValueBinding("styleClass", vb);
        }
        else heading.getAttributes().put("styleClass", styleClass);
      }
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
    level = null;
    style = null;
    styleClass = null;
  }
}
