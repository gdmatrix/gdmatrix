package org.santfeliu.faces.component;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;
import org.apache.myfaces.taglib.html.ext.HtmlCommandLinkTag;

/**
 *
 * @author blanquepa
 */
public class HtmlAriaCommandLinkTag extends HtmlCommandLinkTag
{
  private String ariaLabel;
  private String ariaHidden;
  
  @Override
  public String getComponentType()
  {
    return "AriaCommandLink";
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

  @Override
  protected void setProperties(UIComponent component)
  {
    try
    {
      FacesContext context = FacesContext.getCurrentInstance();
      super.setProperties(component);
      UIComponentTagUtils.setStringProperty(context, component, "ariaLabel", ariaLabel);
      UIComponentTagUtils.setBooleanProperty(context, component, "ariaHidden", ariaHidden);
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
    ariaLabel = null;
    ariaHidden = null;
  }   
}
