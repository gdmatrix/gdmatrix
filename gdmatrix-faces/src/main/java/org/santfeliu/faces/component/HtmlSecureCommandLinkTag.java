package org.santfeliu.faces.component;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;
import static javax.faces.webapp.UIComponentTag.isValueReference;

import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;

public class HtmlSecureCommandLinkTag extends UIComponentTag
{
  private String value;
  
  // specific properties
  private String scheme;
  private String port;
  private String function;

  // UICommand attributes
  private String action;
  private String immediate;
  private String actionListener;

  // style properties
  private String style;
  private String styleClass;
  
  // accessibility properties
  private String tabindex;
  private String title;
  private String ariaLabel;
  private String ariaHidden;
  private String role;
  
  // translation properties
  private String translator;
  private String translationGroup;   

  public HtmlSecureCommandLinkTag()
  {
  }
  
  public String getComponentType()
  {
    return "SecureCommandLink";
  }
  
  public String getRendererType()
  {
    return null;
  }

  public void setScheme(String scheme)
  {
    this.scheme = scheme;
  }

  public String getScheme()
  {
    return scheme;
  }

  public void setPort(String port)
  {
    this.port = port;
  }

  public String getPort()
  {
    return port;
  }

  public void setFunction(String function)
  {
    this.function = function;
  }

  public String getFunction()
  {
    return function;
  }

  public void setValue(String value)
  {
    this.value = value;
  }

  public String getValue()
  {
    return value;
  }

  public String getTabindex()
  {
    return tabindex;
  }

  public void setTabindex(String tabindex)
  {
    this.tabindex = tabindex;
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

  public String getAriaLabel()
  {
    return ariaLabel;
  }

  public void setAriaLabel(String ariaLabel)
  {
    this.ariaLabel = ariaLabel;
  }

  public String getAriaHidden()
  {
    return ariaHidden;
  }

  public void setAriaHidden(String ariaHidden)
  {
    this.ariaHidden = ariaHidden;
  }

  public String getRole()
  {
    return role;
  }

  public void setRole(String role)
  {
    this.role = role;
  }

  public String getTranslator()
  {
    return translator;
  }

  public void setTranslator(String translator)
  {
    this.translator = translator;
  }

  public String getTranslationGroup()
  {
    return translationGroup;
  }

  public void setTranslationGroup(String translationGroup)
  {
    this.translationGroup = translationGroup;
  }

  protected void setProperties(UIComponent component)
  {
    try
    {
      FacesContext context = FacesContext.getCurrentInstance();
      super.setProperties(component);

      UIComponentTagUtils.setStringProperty(context, component, "value", value);

      UIComponentTagUtils.setStringProperty(context, component, "scheme", scheme);
      UIComponentTagUtils.setStringProperty(context, component, "port", port);
      UIComponentTagUtils.setStringProperty(context, component, "function", function);

      UIComponentTagUtils.setActionProperty(context, component, action);
      UIComponentTagUtils.setActionListenerProperty(context, component, actionListener);
      UIComponentTagUtils.setBooleanProperty(context, component, "immediate", immediate);

      UIComponentTagUtils.setStringProperty(context, component, "style", style);
      UIComponentTagUtils.setStringProperty(context, component, "styleClass", styleClass);

      UIComponentTagUtils.setIntegerProperty(context, component, "tabindex", tabindex);
      UIComponentTagUtils.setStringProperty(context, component, "title", title);
      UIComponentTagUtils.setStringProperty(context, component, "ariaLabel", ariaLabel);
      UIComponentTagUtils.setBooleanProperty(context, component, "ariaHidden", ariaHidden);
      UIComponentTagUtils.setStringProperty(context, component, "role", role);      
      UIComponentTagUtils.setStringProperty(context, component, "translationGroup", translationGroup);      
      if (translator != null)
      {
        if (isValueReference(translator))
        {
          ValueBinding vb = context.getApplication().
            createValueBinding(translator);
          component.setValueBinding("translator", vb);
        }
      }      
      
    }
    catch (Exception ex)
    {
    }
  }

  @Override
  public void release()
  {
    super.release();
    value = null;
    scheme = null;
    port = null;
    action = null;
    immediate = null;
    actionListener = null;
    style = null;
    styleClass = null;
    tabindex = null;
    title = null;
    ariaLabel = null;
    ariaHidden = null;
    role = null;
    translator = null;
    translationGroup = null;
  }
}
