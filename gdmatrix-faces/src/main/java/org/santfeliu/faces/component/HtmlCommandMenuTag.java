package org.santfeliu.faces.component;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import javax.faces.webapp.UIComponentTag;

import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;

public class HtmlCommandMenuTag extends UIComponentTag
{
  private String value;

  // UICommand attributes
  private String action;
  private String immediate;
  private String actionListener;
  private String disabled;

  // style properties
  private String style;
  private String styleClass;

  // other properties
  private String title;

  public HtmlCommandMenuTag()
  {
  }
  
  public String getComponentType()
  {
    return "CommandMenu";
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

  public void setAction(String action)
  {
    this.action = action;
  }

  public String getAction()
  {
    return action;
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

  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  @Override
  protected void setProperties(UIComponent component)
  {
    try
    {
      FacesContext context = FacesContext.getCurrentInstance();
      super.setProperties(component);

      UIComponentTagUtils.setStringProperty(context, component, "value", value);
      
      UIComponentTagUtils.setActionProperty(context, component, action);
      UIComponentTagUtils.setActionListenerProperty(context, component, actionListener);
      UIComponentTagUtils.setBooleanProperty(context, component, "immediate", immediate);
      UIComponentTagUtils.setBooleanProperty(context, component, "disabled", disabled);

      UIComponentTagUtils.setStringProperty(context, component, "style", style);
      UIComponentTagUtils.setStringProperty(context, component, "styleClass", styleClass);

      UIComponentTagUtils.setStringProperty(context, component, "title", title);
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
    action = null;
    immediate = null;
    actionListener = null;
    disabled = null;
    style = null;
    styleClass = null;
    title = null;
  }
}
