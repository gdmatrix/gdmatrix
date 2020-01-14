package org.santfeliu.faces.component;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentTag;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;

/**
 *
 * @author realor
 */
public class HtmlCommandTimerTag extends UIComponentTag
{
  // UICommand attributes
  private String action;
  private String immediate;
  private String actionListener;

  private String time;
  private String enabled;

  @Override
  public String getComponentType()
  {
    return "CommandTimer";
  }

  @Override
  public String getRendererType()
  {
    return null;
  }

  public String getAction()
  {
    return action;
  }

  public void setAction(String action)
  {
    this.action = action;
  }

  public String getActionListener()
  {
    return actionListener;
  }

  public void setActionListener(String actionListener)
  {
    this.actionListener = actionListener;
  }

  public String getEnabled()
  {
    return enabled;
  }

  public void setEnabled(String enabled)
  {
    this.enabled = enabled;
  }

  public String getImmediate()
  {
    return immediate;
  }

  public void setImmediate(String immediate)
  {
    this.immediate = immediate;
  }

  public String getTime()
  {
    return time;
  }

  public void setTime(String time)
  {
    this.time = time;
  }

  @Override
  protected void setProperties(UIComponent component)
  {
    try
    {
      FacesContext context = FacesContext.getCurrentInstance();
      super.setProperties(component);

      UIComponentTagUtils.setActionProperty(context, component, action);
      UIComponentTagUtils.setActionListenerProperty(context, component, actionListener);
      UIComponentTagUtils.setBooleanProperty(context, component, "immediate", immediate);

      UIComponentTagUtils.setStringProperty(context, component, "time", time);
      UIComponentTagUtils.setStringProperty(context, component, "enabled", enabled);
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
    action = null;
    immediate = null;
    actionListener = null;
    time = null;
    enabled = null;
  }
}
