package org.santfeliu.faces.task;

import java.io.IOException;
import java.util.Map;
import javax.faces.component.UICommand;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.faces.event.ActionEvent;
import org.santfeliu.util.Task;

/**
 *
 * @author realor
 */
public class HtmlTaskMonitor extends UICommand
{
  private static final String JS_INCLUDED = "task_js_included";
  private static final String COMPLETED_HIDDEN = ":completed";
  private static final String INFO_DIV = ":info";
  private static int monitorCount = 0;

  private Task _task;
  private String _enabled;
  private String _style;
  private String _styleClass;

  public HtmlTaskMonitor()
  {
    setRendererType(null);
  }

  public void setTask(Task task)
  {
    this._task = task;
  }
  
  public Task getTask()
  {
    if (_task != null) return _task;
    ValueBinding vb = getValueBinding("task");
    return vb != null ? (Task)vb.getValue(getFacesContext()) : null;
  }

  public void setEnabled(String enabled)
  {
    this._enabled = enabled;
  }  
  
  public String getEnabled()
  {
    if (_enabled != null) return _enabled;
    ValueBinding vb = getValueBinding("enabled");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setStyle(String style)
  {
    this._style = style;
  }

  public String getStyle()
  {
    if (_style != null) return _style;
    ValueBinding vb = getValueBinding("style");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setStyleClass(String styleClass)
  {
    this._styleClass = styleClass;
  }

  public String getStyleClass()
  {
    if (_styleClass != null) return _styleClass;
    ValueBinding vb = getValueBinding("styleClass");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }
  
  @Override
  public boolean getRendersChildren()
  {
    return true;
  }

  @Override
  public void decode(FacesContext context)
  {
    Map parameters = context.getExternalContext().getRequestParameterMap();
    String clientId = getClientId(context);
    String value = (String)parameters.get(clientId + COMPLETED_HIDDEN);
    if ("true".equals(value))
    {
      queueEvent(new ActionEvent(this));
    }
  }

  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    if (!isRendered()) return;

    Task task = getTask();
    if (task != null && !Task.TERMINATED.equals(task.getState()))
    {
      String clientId = getClientId(context);
      ResponseWriter writer = context.getResponseWriter();

      writer.startElement("div", this);
      writer.writeAttribute("id", clientId + INFO_DIV, null);
      String style = getStyle();
      if (style != null)
      {
        writer.writeAttribute("style", style, null);
      }
      String styleClass = getStyleClass();
      if (styleClass != null)
      {
        writer.writeAttribute("class", styleClass, null);
      }
      writer.endElement("div");

      ExternalContext externalContext = context.getExternalContext();
      String contextPath = externalContext.getRequestContextPath();
      Map requestMap = externalContext.getRequestMap();

      if (!requestMap.containsKey(JS_INCLUDED))
      {
        writer.startElement("script", this);
        writer.writeAttribute("type", "text/javascript", null);
        writer.writeAttribute("src", 
          contextPath + "/plugins/task/task.js", null);
        writer.write(" ");
        writer.endElement("script");
        requestMap.put(JS_INCLUDED, "true");
      }
      writer.startElement("input", this);
      writer.writeAttribute("type", "hidden", null);
      writer.writeAttribute("id", clientId + COMPLETED_HIDDEN, null);
      writer.writeAttribute("name", clientId + COMPLETED_HIDDEN, null);
      writer.writeAttribute("value", "", null);
      writer.endElement("input");
      
      writer.startElement("script", this);
      writer.writeAttribute("type", "text/javascript", null);
      writer.write("var baseUrl = '" + contextPath + "';");
      monitorCount++;
      if (monitorCount < 0) monitorCount = 0;
      String varName = "taskMonitor" + monitorCount;
      writer.write("var " + varName + " = new TaskMonitor('" + 
        task.getTaskId() + "', '" + clientId + COMPLETED_HIDDEN + "', '" + 
        clientId + INFO_DIV + "');");
      writer.write(varName + ".monitor();");
      writer.endElement("script");
    }
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[5];
    values[0] = super.saveState(context);
    values[1] = _task == null ? null : _task.getTaskId();
    values[2] = _enabled;
    values[3] = _style;
    values[4] = _styleClass;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[]) state;
    super.restoreState(context, values[0]);
    String taskId = (String)values[1];
    _task = taskId == null ? null : Task.getInstance(taskId);
    _enabled = (String)values[2];
    _style = (String)values[3];
    _styleClass = (String)values[4];
  }
}

