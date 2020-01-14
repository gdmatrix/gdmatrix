package org.santfeliu.faces.component.jqueryui;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentTag;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;

/**
 *
 * @author blanquepa
 */
public class HtmlAutocompleteTag extends UIComponentTag
{
  private String value;

  // UICommand attributes
  private String immediate;
  private String actionListener;
  private String disabled;

  // style properties
  private String style;
  private String styleClass;
  private String buttonStyle;
  private String buttonStyleClass;  
  private String inputStyle;
  private String inputStyleClass;

  public HtmlAutocompleteTag()
  {
  }
  
  public String getComponentType()
  {
    return "Autocomplete";
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

  public void setImmediate(String immediate)
  {
    this.immediate = immediate;
  }

  public String getImmediate()
  {
    return immediate;
  }

  public void setActionListener(String actionListener)
  {
    this.actionListener = actionListener;
  }

  public String getActionListener()
  {
    return actionListener;
  }

  public String getDisabled()
  {
    return disabled;
  }

  public void setDisabled(String disabled)
  {
    this.disabled = disabled;
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

  public void setButtonStyle(String style)
  {
    this.buttonStyle = style;
  }

  public String getButtonStyle()
  {
    return buttonStyle;
  }

  public void setButtonStyleClass(String styleClass)
  {
    this.buttonStyleClass = styleClass;
  }

  public String getButtonStyleClass()
  {
    return buttonStyleClass;
  }

  public String getInputStyle()
  {
    return inputStyle;
  }

  public void setInputStyle(String inputStyle)
  {
    this.inputStyle = inputStyle;
  }

  public String getInputStyleClass()
  {
    return inputStyleClass;
  }

  public void setInputStyleClass(String inputStyleClass)
  {
    this.inputStyleClass = inputStyleClass;
  }
  
  
  
  @Override
  protected void setProperties(UIComponent component)
  {
    try
    {
      FacesContext context = FacesContext.getCurrentInstance();
      super.setProperties(component);

      UIComponentTagUtils.setStringProperty(context, component, "value", value);
      
      UIComponentTagUtils.setActionListenerProperty(context, component, actionListener);
      UIComponentTagUtils.setBooleanProperty(context, component, "immediate", immediate);
      UIComponentTagUtils.setBooleanProperty(context, component, "disabled", disabled);

      UIComponentTagUtils.setStringProperty(context, component, "style", style);
      UIComponentTagUtils.setStringProperty(context, component, "styleClass", styleClass);
      UIComponentTagUtils.setStringProperty(context, component, "buttonStyle", buttonStyle);
      UIComponentTagUtils.setStringProperty(context, component, "buttonStyleClass", buttonStyleClass);
      UIComponentTagUtils.setStringProperty(context, component, "inputStyle", inputStyle);
      UIComponentTagUtils.setStringProperty(context, component, "inputStyleClass", inputStyleClass);
      
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
    immediate = null;
    actionListener = null;
    disabled = null;
    style = null;
    styleClass = null;
    buttonStyle = null;
    buttonStyleClass = null;
    inputStyle = null;
    inputStyleClass = null;
  }  
}
