package org.santfeliu.faces.component;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import org.apache.myfaces.custom.div.DivTag;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;

/**
 *
 * @author blanquepa
 */
public class HtmlDivTag extends DivTag
{
  private String ariaHidden;  
  private String role;
  
  @Override
  public String getComponentType()
  {
      return "Div";
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
  
  @Override
  protected void setProperties(UIComponent component)
  {
    try
    {
      FacesContext context = FacesContext.getCurrentInstance();
      super.setProperties(component);

      UIComponentTagUtils.setBooleanProperty(context, component, "ariaHidden", ariaHidden);      
      UIComponentTagUtils.setStringProperty(context, component, "role", role);            
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
    ariaHidden = null;
    role = null;
  }   
  
}
