package org.santfeliu.faces.sendfile;


import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentTag;

import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;

public class HtmlSendFileTag extends UIComponentTag
{
  // UICommand attributes
  private String action;
  private String immediate;
  private String actionListener;

  private String maxFileSize;
  private String validExtensions;
  private String command;
  private String result;
  private String fileProperties;
  private String docTypes;
  private String port;

  // style properties
  private String width;
  private String height;
  private String style;
  private String styleClass;
  private String buttonClass;

  public HtmlSendFileTag()
  {
  }
  
  public String getComponentType()
  {
    return "SendFile";
  }
  
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

  public String getMaxFileSize()
  {
    return maxFileSize;
  }

  public void setMaxFileSize(String maxFileSize)
  {
    this.maxFileSize = maxFileSize;
  }

  public String getValidExtensions()
  {
    return validExtensions;
  }

  public void setValidExtensions(String validExtensions)
  {
    this.validExtensions = validExtensions;
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
  
  public void setWidth(String width)
  {
    this.width = width;
  }

  public String getWidth()
  {
    return width;
  }

  public void setHeight(String height)
  {
    this.height = height;
  }

  public String getHeight()
  {
    return height;
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
  
  public void setFileProperties(String fileProperties)
  {
    this.fileProperties = fileProperties;
  }

  public String getFileProperties()
  {
    return fileProperties;
  }

  public String getDocTypes()
  {
    return docTypes;
  }

  public void setDocTypes(String docTypes)
  {
    this.docTypes = docTypes;
  }

  public String getPort()
  {
    return port;
  }

  public void setPort(String port)
  {
    this.port = port;
  }

  public void setButtonClass(String buttonClass)
  {
    this.buttonClass = buttonClass;
  }

  public String getButtonClass()
  {
    return buttonClass;
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
      UIComponentTagUtils.setStringProperty(context, component, "fileProperties", fileProperties);
      UIComponentTagUtils.setStringProperty(context, component, "docTypes", docTypes);
      UIComponentTagUtils.setStringProperty(context, component, "port", port);
      
      UIComponentTagUtils.setActionProperty(context, component, action);
      UIComponentTagUtils.setActionListenerProperty(context, component, actionListener);
      UIComponentTagUtils.setBooleanProperty(context, component, "immediate", immediate);

      UIComponentTagUtils.setStringProperty(context, component, "maxFileSize", maxFileSize);
      UIComponentTagUtils.setStringProperty(context, component, "validExtensions", validExtensions);
      UIComponentTagUtils.setStringProperty(context, component, "style", style);
      UIComponentTagUtils.setStringProperty(context, component, "styleClass", styleClass);
      UIComponentTagUtils.setStringProperty(context, component, "buttonClass", buttonClass);      
      UIComponentTagUtils.setStringProperty(context, component, "width", width);
      UIComponentTagUtils.setStringProperty(context, component, "height", height);
      
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
    maxFileSize = null;
    validExtensions = null;
    style = null;
    styleClass = null;
    width = null;
    height = null;
    command = null;
    result = null;
    action = null;
    immediate = null;
    actionListener = null;
    fileProperties = null;
    docTypes = null;
    port = null;
    buttonClass = null;
  }
}
