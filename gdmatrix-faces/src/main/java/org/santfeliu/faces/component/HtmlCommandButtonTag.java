package org.santfeliu.faces.component;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;

/**
 *
 * @author realor
 */
public class HtmlCommandButtonTag extends UIComponentTag
{
  private String value;

  // UICommand attributes
  private String action;
  private String immediate;
  private String actionListener;

  // accessibility problem
  private String title;
  private String ariaLabel;

  // style properties
  private String style;
  private String styleClass;

  // translation properties
  private String translator;
  private String translationGroup;
  private String renderBox;
  
  // events
  private String onclick;

  public HtmlCommandButtonTag()
  {
  }

  public String getComponentType()
  {
    return "CommandButton";
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

  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public String getRenderBox()
  {
    return renderBox;
  }

  public void setRenderBox(String renderBox)
  {
    this.renderBox = renderBox;
  }

  public String getOnclick()
  {
    return onclick;
  }

  public void setOnclick(String onclick)
  {
    this.onclick = onclick;
  }

  public String getAriaLabel()
  {
    return ariaLabel;
  }

  public void setAriaLabel(String ariaLabel)
  {
    this.ariaLabel = ariaLabel;
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

      UIComponentTagUtils.setBooleanProperty(context, component, "title", title);
      UIComponentTagUtils.setStringProperty(context, component, "style", style);
      UIComponentTagUtils.setStringProperty(context, component, "styleClass", styleClass);

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
      UIComponentTagUtils.setBooleanProperty(context, component, "renderBox", renderBox);
      UIComponentTagUtils.setStringProperty(context, component, "onclick", onclick);
      UIComponentTagUtils.setStringProperty(context, component, "ariaLabel", ariaLabel);      
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
    style = null;
    styleClass = null;
    title = null;
    translator = null;
    translationGroup = null;
    renderBox = null;
    onclick = null;
    ariaLabel = null;
  }
}
