package org.santfeliu.faces.component;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentTag;

import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;

public class HtmlCalendarTag extends UIComponentTag
{
  private String value;
  private String externalFormat;
  private String internalFormat;
  private String style;
  private String styleClass;
  private String buttonImage;
  private String buttonStyle;
  private String buttonStyleClass;
  private String required;
  private String disabled;

  public HtmlCalendarTag()
  {
  }
  
  public String getComponentType()
  {
    return "Calendar";
  }
  
  public String getRendererType()
  {
    return null;
  }

  public void setValue(String value)
  {
    this.value = value;
  }

  public String getValue()
  {
    return value;
  }

  public void setExternalFormat(String externalFormat)
  {
    this.externalFormat = externalFormat;
  }

  public String getExternalFormat()
  {
    return externalFormat;
  }

  public void setInternalFormat(String internalFormat)
  {
    this.internalFormat = internalFormat;
  }

  public String getInternalFormat()
  {
    return internalFormat;
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

  public void setButtonImage(String buttonImage)
  {
    this.buttonImage = buttonImage;
  }

  public String getButtonImage()
  {
    return buttonImage;
  }

  public void setButtonStyle(String buttonStyle)
  {
    this.buttonStyle = buttonStyle;
  }

  public String getButtonStyle()
  {
    return buttonStyle;
  }

  public void setButtonStyleClass(String buttonStyleClass)
  {
    this.buttonStyleClass = buttonStyleClass;
  }

  public String getButtonStyleClass()
  {
    return buttonStyleClass;
  }

  public String getRequired()
  {
    return required;
  }

  public void setRequired(String required)
  {
    this.required = required;
  }

  public String getDisabled()
  {
    return disabled;
  }

  public void setDisabled(String disabled)
  {
    this.disabled = disabled;
  }

  @Override
  protected void setProperties(UIComponent component)
  {
    try
    {
      FacesContext context = FacesContext.getCurrentInstance();
      super.setProperties(component);

      UIComponentTagUtils.setValueProperty(context, component, value);
      UIComponentTagUtils.setStringProperty(context, component, 
        "externalFormat", externalFormat);
      UIComponentTagUtils.setStringProperty(context, component, 
        "internalFormat", internalFormat);
      UIComponentTagUtils.setStringProperty(context, component, 
        "style", style);
      UIComponentTagUtils.setStringProperty(context, component, 
        "styleClass", styleClass);
      UIComponentTagUtils.setStringProperty(context, component, 
        "buttonImage", buttonImage);
      UIComponentTagUtils.setStringProperty(context, component, 
        "buttonStyle", buttonStyle);
      UIComponentTagUtils.setStringProperty(context, component, 
        "buttonStyleClass", buttonStyleClass);
      UIComponentTagUtils.setBooleanProperty(context, component, 
        "required", required);
      UIComponentTagUtils.setBooleanProperty(context, component, 
        "disabled", disabled);
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
    value = null;
    externalFormat = null;
    internalFormat = null;
    style = null;
    styleClass = null;
    buttonImage = null;
    buttonStyle = null;
    buttonStyleClass = null;
    required = null;
    disabled = null;
  }
}
