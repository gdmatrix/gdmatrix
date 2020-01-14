package org.santfeliu.faces.task;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;
import static javax.faces.webapp.UIComponentTag.isValueReference;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;

/**
 *
 * @author realor
 */
public class HtmlTaskMonitorTag extends UIComponentTag
{
  // UICommand attributes
  private String action;
  private String immediate;
  private String actionListener;

  private String task;
  private String enabled;
  private String style;
  private String styleClass;

  @Override
  public String getComponentType()
  {
    return "TaskMonitor";
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

  public String getTask()
  {
    return task;
  }

  public void setTask(String task)
  {
    this.task = task;
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

      if (task != null)
      {
        if (isValueReference(task))
        {
          ValueBinding vb = context.getApplication().createValueBinding(task);
          component.setValueBinding("task", vb);
        }
      }
      UIComponentTagUtils.setStringProperty(context, component, "enabled", enabled);
    
      UIComponentTagUtils.setStringProperty(context, component, "style", style);
      UIComponentTagUtils.setStringProperty(context, component, "styleClass", styleClass);
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
    task = null;
    enabled = null;
    style = null;
    styleClass = null;
  }
}
