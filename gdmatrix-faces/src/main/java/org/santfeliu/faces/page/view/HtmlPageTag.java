package org.santfeliu.faces.page.view;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;


public class HtmlPageTag extends UIComponentTag
{
  private String template;
  private String match;

  public HtmlPageTag()
  {
  }

  public String getComponentType()
  {
    return "Page";
  }
  
  public String getRendererType()
  {
    return null;
  }

  public void setTemplate(String template)
  {
    this.template = template;
  }

  public String getTemplate()
  {
    return template;
  }

  public void setMatch(String match)
  {
    this.match = match;
  }

  public String getMatch()
  {
    return match;
  }

  protected void setProperties(UIComponent component)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Application application = context.getApplication();
    super.setProperties(component);
    HtmlPage page = (HtmlPage)component;
    if (template != null)
    {
      if (isValueReference(template))
      {
        ValueBinding vb = application.createValueBinding(template);
        page.setValueBinding("template", vb);
      }
      else page.getAttributes().put("template", template);
    }
    if (match != null)
    {
      page.getAttributes().put("match", Boolean.valueOf(match));
    }
  }
  
  public void release()
  {
    super.release();
    template = null;
    match = null;
  }
}
