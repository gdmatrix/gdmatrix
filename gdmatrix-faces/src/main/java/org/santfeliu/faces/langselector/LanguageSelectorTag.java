package org.santfeliu.faces.langselector;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;

public class LanguageSelectorTag extends UIComponentTag
{
  private String locales;
  private String style;
  private String styleClass;

  public LanguageSelectorTag()
  {
  }
  
  public String getComponentType()
  {
    return "LanguageSelector";
  }
  
  public String getRendererType()
  {
    return null;
  }

  public void setLocales(String locales)
  {
    this.locales = locales;
  }

  public String getLocales()
  {
    return locales;
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

  protected void setProperties(UIComponent component)
  {
    try
    {
      FacesContext context = FacesContext.getCurrentInstance();
      Application application = context.getApplication();
      super.setProperties(component);

      LanguageSelector langSel = (LanguageSelector)component;
      if (locales != null)
      {
        if (isValueReference(locales))
        {
          ValueBinding vb = application.createValueBinding(locales);
          langSel.setValueBinding("locales", vb);
        }
      }
      if (style != null)
      {
        if (isValueReference(style))
        {
          ValueBinding vb = application.createValueBinding(style);
          langSel.setValueBinding("style", vb);
        }
        else langSel.getAttributes().put("style", style);
      }
      if (styleClass != null)
      {
        if (isValueReference(styleClass))
        {
          ValueBinding vb = application.createValueBinding(styleClass);
          langSel.setValueBinding("styleClass", vb);
        }
        else langSel.getAttributes().put("styleClass", styleClass);
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
    locales = null;
    style = null;
    styleClass = null;
  }
}
