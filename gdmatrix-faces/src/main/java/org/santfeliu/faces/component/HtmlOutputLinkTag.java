package org.santfeliu.faces.component;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;
import static javax.faces.webapp.UIComponentTag.isValueReference;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;

/**
 *
 * @author blanquepa
 */
public class HtmlOutputLinkTag extends UIComponentTag
{
  private String value;
  
  private String translator;
  private String translationGroup; 
  private String ariaLabel;
  private String ariaHidden;  
  private String title;
  private String role;
  
  private String style;
  private String styleClass;
  private String target;

  private String tabindex;
  private String accesskey;  

  private String onclick;
  private String onfocus;  

  public String getComponentType()
  {
    return "OutputLink";
  }
  
  public String getRendererType()
  {
    return null;
  }

  public String getValue()
  {
    return value;
  }

  public void setValue(String value)
  {
    this.value = value;
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

  public String getTarget()
  {
    return target;
  }

  public void setTarget(String target)
  {
    this.target = target;
  }

  public String getTabindex()
  {
    return tabindex;
  }

  public void setTabindex(String tabindex)
  {
    this.tabindex = tabindex;
  }

  public String getAccesskey()
  {
    return accesskey;
  }

  public void setAccesskey(String accesskey)
  {
    this.accesskey = accesskey;
  }

  public String getOnclick()
  {
    return onclick;
  }

  public void setOnclick(String onclick)
  {
    this.onclick = onclick;
  }

  public String getOnfocus()
  {
    return onfocus;
  }

  public void setOnfocus(String onfocus)
  {
    this.onfocus = onfocus;
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

      UIComponentTagUtils.setStringProperty(context, component, "ariaLabel", ariaLabel);
      UIComponentTagUtils.setBooleanProperty(context, component, "ariaHidden", ariaHidden);      
      UIComponentTagUtils.setStringProperty(context, component, "target", target);
      UIComponentTagUtils.setStringProperty(context, component, "value", value);
      UIComponentTagUtils.setStringProperty(context, component, "title", title);      
      UIComponentTagUtils.setStringProperty(context, component, "role", role);            
      
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
      UIComponentTagUtils.setStringProperty(context, component, "onclick", onclick);
      UIComponentTagUtils.setStringProperty(context, component, "onfocus", onfocus);
      UIComponentTagUtils.setStringProperty(context, component, "tabindex", tabindex);
      UIComponentTagUtils.setStringProperty(context, component, "accesskey", accesskey);      
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
    ariaLabel = null;
    ariaHidden = null;
    style = null;
    styleClass = null;
    target = null;
    translator = null;
    translationGroup = null;
    tabindex = null;
    accesskey = null;
    onclick = null;
    onfocus = null;
    title = null;
    role = null;
  }  
  
}
