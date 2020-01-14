package org.santfeliu.faces.print;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentTag;

import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;

public class HtmlPrintableGroupTag extends UIComponentTag
{
  private String name;

  public HtmlPrintableGroupTag()
  {
  }
  
  public String getComponentType()
  {
    return "PrintableGroup";
  }
  
  public String getRendererType()
  {
    return null;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }

  protected void setProperties(UIComponent component)
  {
    try
    {
      FacesContext context = FacesContext.getCurrentInstance();
      super.setProperties(component);

      UIComponentTagUtils.setStringProperty(context, component, "name", name);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  public void release()
  {
    super.release();
    name = null;
  }
}
