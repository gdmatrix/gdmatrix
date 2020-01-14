package org.santfeliu.faces.component;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;

import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;


public class HtmlOutputTextTag extends UIComponentTag
{
  private String value;
  
  // extended properties
  private String title;
  private String escape;
  private String translator;
  private String translationGroup;

  // style properties
  private String style;
  private String styleClass;

  public HtmlOutputTextTag()
  {
  }
  
  public String getComponentType()
  {
    return "OutputText";
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
        "translationGroup", translationGroup);
      UIComponentTagUtils.setStringProperty(context, component, 
        "style", style);
      UIComponentTagUtils.setStringProperty(context, component, 
        "styleClass", styleClass);
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
      ex.printStackTrace();
    }
  }

  public void release()
  {
    super.release();
    value = null;
    title = null;
    escape = null;
    translator = null;
    translationGroup = null;
    style = null;
    styleClass = null;
  }
}
