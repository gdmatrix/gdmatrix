package org.santfeliu.faces.component;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentTag;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;

/**
 *
 * @author blanquepa
 */
public class HtmlObjectTag extends UIComponentTag
{
  private String value;
  private String httpsDisableOnAgent;
  private String disabledMessage;

  public String getValue()
  {
    return value;
  }

  public void setValue(String value)
  {
    this.value = value;
  }

  public String getHttpsDisableOnAgent()
  {
    return httpsDisableOnAgent;
  }

  public void setHttpsDisableOnAgent(String httpsDisableOnAgent)
  {
    this.httpsDisableOnAgent = httpsDisableOnAgent;
  }

  public String getDisabledMessage()
  {
    return disabledMessage;
  }

  public void setDisabledMessage(String disabledMessage)
  {
    this.disabledMessage = disabledMessage;
  }

  @Override
  protected void setProperties(UIComponent component)
  {
    try
    {
      FacesContext context = FacesContext.getCurrentInstance();
      super.setProperties(component);
      UIComponentTagUtils.setValueProperty(context, component, value);
      UIComponentTagUtils.setStringProperty(
        context, component, "httpsDisableOnAgent", httpsDisableOnAgent);
      UIComponentTagUtils.setStringProperty(
        context, component, "disabledMessage", disabledMessage);
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
    httpsDisableOnAgent = null;
    disabledMessage = null;
  }

  @Override
  public String getComponentType()
  {
    return "Object";
  }

  @Override
  public String getRendererType()
  {
    return null;
  }
}
