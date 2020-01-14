/*
 * GDMatrix
 *  
 * Copyright (C) 2020, Ajuntament de Sant Feliu de Llobregat
 *  
 * This program is licensed and may be used, modified and redistributed under 
 * the terms of the European Public License (EUPL), either version 1.1 or (at 
 * your option) any later version as soon as they are approved by the European 
 * Commission.
 *  
 * Alternatively, you may redistribute and/or modify this program under the 
 * terms of the GNU Lesser General Public License as published by the Free 
 * Software Foundation; either  version 3 of the License, or (at your option) 
 * any later version. 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *    
 * See the licenses for the specific language governing permissions, limitations 
 * and more details.
 *    
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along 
 * with this program; if not, you may find them at: 
 *    
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * http://www.gnu.org/licenses/ 
 * and 
 * https://www.gnu.org/licenses/lgpl.txt
 */
package org.santfeliu.faces.task;

import java.io.IOException;
import java.util.Map;
import javax.el.ValueExpression;
import javax.faces.component.FacesComponent;
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
@FacesComponent(value = "HtmlTaskMonitor")
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
    ValueExpression ve = getValueExpression("task");
    return ve != null ? (Task)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setEnabled(String enabled)
  {
    this._enabled = enabled;
  }  
  
  public String getEnabled()
  {
    if (_enabled != null) return _enabled;
    ValueExpression ve = getValueExpression("enabled");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setStyle(String style)
  {
    this._style = style;
  }

  public String getStyle()
  {
    if (_style != null) return _style;
    ValueExpression ve = getValueExpression("style");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setStyleClass(String styleClass)
  {
    this._styleClass = styleClass;
  }

  public String getStyleClass()
  {
    if (_styleClass != null) return _styleClass;
    ValueExpression ve = getValueExpression("styleClass");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
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

