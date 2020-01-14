package org.santfeliu.faces.component;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentTag;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;

/**
 *
 * @author realor
 */
public class HtmlSaveScrollTag extends UIComponentTag
{
  private String value;
  private String resetIfError;

  public String getValue()
  {
    return value;
  }

  public void setValue(String value)
  {
    this.value = value;
  }

  public String getResetIfError()
  {
    return resetIfError;
  }

  public void setResetIfError(String resetIfError)
  {
    this.resetIfError = resetIfError;
  }

  @Override
  public String getComponentType()
  {
    return "SaveScroll";
  }

  @Override
  public String getRendererType()
  {
    return null;
  }

  @Override
  protected void setProperties(UIComponent component)
  {
    try
    {
      FacesContext context = FacesContext.getCurrentInstance();
      super.setProperties(component);

      UIComponentTagUtils.setIntegerProperty(context, component, "value", value);
      UIComponentTagUtils.setBooleanProperty(context, component, "resetIfError", resetIfError);
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
    resetIfError = null;
  }
}