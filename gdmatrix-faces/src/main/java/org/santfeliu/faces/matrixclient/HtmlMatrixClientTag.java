package org.santfeliu.faces.matrixclient;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentTag;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;

/**
 *
 * @author blanquepa
 */
public class HtmlMatrixClientTag extends UIComponentTag
{
  // UICommand attributes
  private String action;
  private String immediate;
  private String actionListener;

  private String command;
  private String result;
  private String properties;
  private String function;
  private String helpUrl;
  
  private String model;

  public HtmlMatrixClientTag()
  {
  }
  
  @Override
  public String getComponentType()
  {
    return "MatrixClient";
  }
  
  @Override
  public String getRendererType()
  {
    return null;
  }

  public void setAction(String action)
  {
    this.action = action;
  }

  public String getAction()
  {
    return action;
  }

  public void setImmediate(String immediate)
  {
    this.immediate = immediate;
  }

  public String getImmediate()
  {
    return immediate;
  }

  public void setActionListener(String actionListener)
  {
    this.actionListener = actionListener;
  }

  public String getActionListener()
  {
    return actionListener;
  }

  public void setCommand(String command)
  {
    this.command = command;
  }

  public String getCommand()
  {
    return command;
  }

  public void setResult(String result)
  {
    this.result = result;
  }

  public String getResult()
  {
    return result;
  }
  
  public void setProperties(String properties)
  {
    this.properties = properties;
  }

  public String getProperties()
  {
    return properties;
  }

  public String getFunction()
  {
    return function;
  }

  public void setFunction(String function)
  {
    this.function = function;
  }

  public String getModel()
  {
    return model;
  }

  public void setModel(String model)
  {
    this.model = model;
  }

  public String getHelpUrl() 
  {
    return helpUrl;
  }

  public void setHelpUrl(String helpUrl) 
  {
    this.helpUrl = helpUrl;
  }
  
  @Override
  protected void setProperties(UIComponent component)
  {
    try
    {
      FacesContext context = FacesContext.getCurrentInstance();
      super.setProperties(component);

      UIComponentTagUtils.setStringProperty(context, component, "command", command);
      UIComponentTagUtils.setStringProperty(context, component, "result", result);      
      UIComponentTagUtils.setStringProperty(context, component, "properties", properties);
      UIComponentTagUtils.setStringProperty(context, component, "function", function);
      UIComponentTagUtils.setStringProperty(context, component, "model", model);
      UIComponentTagUtils.setStringProperty(context, component, "helpUrl", helpUrl);
      
      UIComponentTagUtils.setActionProperty(context, component, action);
      UIComponentTagUtils.setActionListenerProperty(context, component, actionListener);
      UIComponentTagUtils.setBooleanProperty(context, component, "immediate", immediate);
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
    command = null;
    result = null;
    action = null;
    immediate = null;
    actionListener = null;
    properties = null;
    function = null;
    model = null;
    helpUrl = null;
  }  
}
