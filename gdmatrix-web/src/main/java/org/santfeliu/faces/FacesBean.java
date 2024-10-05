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
package org.santfeliu.faces;


import java.util.*;

import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import org.santfeliu.web.HttpUtils;
import org.santfeliu.ws.WSExceptionFactory;

/**
 *
 * @author realor
 */

public abstract class FacesBean
{
  public FacesBean()
  {
  }

  protected Application getApplication()
  {
    return FacesContext.getCurrentInstance().getApplication();
  }

  protected Map getApplicationMap()
  {
    return getExternalContext().getApplicationMap();
  }

  protected ExternalContext getExternalContext()
  {
    return FacesContext.getCurrentInstance().getExternalContext();
  }

  protected ServletContext getServletContext()
  {
    return (ServletContext)getExternalContext().getContext();
  }

  protected FacesContext getFacesContext()
  {
    return FacesContext.getCurrentInstance();
  }

  protected String getContextPath()
  {
    return getExternalContext().getRequestContextPath();
  }

  protected String getContextURL()
  {
    ExternalContext extContext = getExternalContext();
    HttpServletRequest request = (HttpServletRequest)extContext.getRequest();
    return HttpUtils.getContextURL(request);
  }

  protected Lifecycle getLifecycle()
  {
    String lifecycleId =
      getExternalContext().getInitParameter("javax.faces.LIFECYCLE_ID");
    if (lifecycleId == null || lifecycleId.length() == 0)
      lifecycleId = "DEFAULT";
    LifecycleFactory lifecycleFactory =
      (LifecycleFactory) FactoryFinder.getFactory(
      "javax.faces.lifecycle.LifecycleFactory");
    return lifecycleFactory.getLifecycle(lifecycleId);
  }

  protected Map getRequestMap()
  {
    return getExternalContext().getRequestMap();
  }

  protected Map getSessionMap()
  {
    return getExternalContext().getSessionMap();
  }

  /**
   * Return any attribute stored in request scope, session scope,
   * or application scope under the specified name.
   * @param name
   * @return
   */
  protected Object getBean(String name)
  {
    return getApplication().getVariableResolver().
      resolveVariable(getFacesContext(), name);
  }

  /**
   * Replace the value of any attribute stored in request scope, session scope,
   * or application scope under the specified name.
   * @param name
   * @param value
   */
  protected void setBean(String name, Object value)
  {
    setValue("#{" + name + "}", value);
  }

  /**
   * Evaluate the specified value binding expression, and return
   * the value that it points at.
   * @param expr
   * @return
   */
  protected Object getValue(String expr)
  {
    return getFacesContext().getApplication()
      .evaluateExpressionGet(getFacesContext(), expr, Object.class);
  }

  /**
   * Evaluate the specified value binding expression, and update
   * the value that it points at.
   * @param expr
   * @param value
   */
  protected void setValue(String expr, Object value)
  {
    ValueBinding vb = getApplication().createValueBinding(expr);
    vb.setValue(getFacesContext(), value);
  }

  /**
   * Erase previously submitted values for all input components on this page.
   */
  protected void erase()
  {
    erase(((UIComponent) (getFacesContext().getViewRoot())));
  }

  private void erase(UIComponent component)
  {
    if (component instanceof EditableValueHolder)
      ((EditableValueHolder) component).setSubmittedValue(null);
    for (Iterator kids = component.getFacetsAndChildren(); kids.hasNext();
         erase((UIComponent) kids.next()));
  }

  /**
   * Log the specified message to the container's log file.
   * @param message
   */
  protected void log(String message)
  {
    getExternalContext().log(message);
  }

  /**
   * Log the specified message and exception to the container's log file.
   * @param message
   * @param throwable
   */
  protected void log(String message, Throwable throwable)
  {
    getExternalContext().log(message, throwable);
  }

  /**
   * Enqueue a global FacesMessage (not associated with any particular
   * component) containing the specified messageId text in the bundle specified
   * by bundleClassName and a message severity level of severity.
   * @param bundleClassName bundle class name
   * @param messageId messageId
   * @param params message params
   * @param severity
   */
  protected void message(String bundleClassName,
    String messageId, Object[] params, FacesMessage.Severity severity)
  {
    String summary;
    String detail;
    try
    {
      Locale locale = getLocale();
      ResourceBundle bundle = ResourceBundle.getBundle(bundleClassName, locale);
      summary = bundle.getString(messageId);
      String detailKey = messageId + "_detail";
      detail = bundle.containsKey(detailKey) ?
        bundle.getString(detailKey) : null;
    }
    catch (MissingResourceException ex)
    {
      summary = messageId;
      detail = null;
    }
    FacesUtils.addMessage(summary, detail, params, severity);
  }

  /**
   * Enqueue a global FacesMessage (not associated with any particular
   * component) containing the specified messageId text in default message
   * bundles and a message severity level of severity.
   * @param messageId
   * @param severity
   */
  protected void message(String messageId, FacesMessage.Severity severity)
  {
    FacesUtils.addMessage(messageId, null, severity);
  }

  /**
   * Enqueue a global FacesMessage (not associated with any particular
   * component) containing the specified messageId text and a message severity
   * level of FacesMessage.SEVERITY_INFO.
   * @param messageId
   */
  protected void info(String messageId)
  {
    FacesUtils.addMessage(messageId, null, FacesMessage.SEVERITY_INFO);
  }

  protected void info(String messageId, Object[] params)
  {
    FacesUtils.addMessage(messageId, params, FacesMessage.SEVERITY_INFO);
  }

  /**
   * Enqueue a FacesMessage associated with the specified component,
   * containing the specified messageId text and a message severity level
   * of FacesMessage.SEVERITY_INFO.
   * @param component
   * @param messageId
   */
  protected void info(UIComponent component, String messageId)
  {
    FacesUtils.addMessage(component, messageId,
                          null, FacesMessage.SEVERITY_INFO);
  }

  /**
   * Enqueue a global FacesMessage (not associated with any particular
   * component) containing the specified messageId text and a message severity
   * level of FacesMessage.SEVERITY_WARN.
   * @param messageId
   */
  protected void warn(String messageId)
  {
    FacesUtils.addMessage(messageId, null, FacesMessage.SEVERITY_WARN);
  }

  protected void warn(String messageId, Object[] params)
  {
    FacesUtils.addMessage(messageId, params, FacesMessage.SEVERITY_WARN);
  }

  /**
   * Enqueue a FacesMessage associated with the specified component,
   * containing the specified messageId text and a message severity level
   * of FacesMessage.SEVERITY_WARN.
   * @param component
   * @param messageId
   */
  protected void warn(UIComponent component, String messageId)
  {
    FacesUtils.addMessage(component, messageId,
                          null, FacesMessage.SEVERITY_WARN);
  }

  /**
   * Enqueue a global FacesMessage (not associated with any particular
   * component) containing the specified messageId text and a message severity
   * level of FacesMessage.SEVERITY_ERROR.
   * @param messageId
   */
  protected void error(String messageId)
  {
    FacesUtils.addMessage(messageId, null, FacesMessage.SEVERITY_ERROR);
  }

  protected void error(String messageId, Object[] params)
  {
    FacesUtils.addMessage(messageId, params, FacesMessage.SEVERITY_ERROR);
  }

  protected void error(String messageId, String detail)
  {
    FacesUtils.addMessage(messageId, new Object[]{detail},
      FacesMessage.SEVERITY_ERROR);
  }

  protected void error(Exception ex)
  {
    List<String> details = WSExceptionFactory.getDetails(ex);
    if (!details.isEmpty())
      error(details);
    else
      FacesUtils.addMessage(ex);
  }

  protected boolean error(List<String> details)
  {
    if (!details.isEmpty())
    {
      Set<String> detailSet = new HashSet<>(details);
      for (String detail : detailSet)
      {
        String[] detailParts = detail.split("#");
        if (detailParts.length == 2)
          error(detailParts[0], detailParts[1]);
        else
          error(detail);
      }
      return true;
    }
    else
      return false;
  }

  /**
   * Enqueue a FacesMessage associated with the specified component,
   * containing the specified message text and a messageId severity level
   * of FacesMessage.SEVERITY_ERROR.
   * @param component
   * @param messageId
   */
  protected void error(UIComponent component, String messageId)
  {
    FacesUtils.addMessage(component, messageId,
                          null, FacesMessage.SEVERITY_ERROR);
  }

  protected void error(UIComponent component, String messageId,
    Object[] params)
  {
    FacesUtils.addMessage(component, messageId,
      params, FacesMessage.SEVERITY_ERROR);
  }

  protected void error(UIComponent component, String messageId, String detail)
  {
    FacesUtils.addMessage(component, messageId, new Object[]{detail},
      FacesMessage.SEVERITY_ERROR);
  }

  protected void error(UIComponent component, Exception ex)
  {
    FacesUtils.addMessage(component, ex);
  }

  /**
   * Enqueue a global FacesMessage (not associated with any particular
   * component) containing the specified messageId text and a message severity
   * level of FacesMessage.SEVERITY_FATAL.
   * @param messageId
   */
  protected void fatal(String messageId)
  {
    FacesUtils.addMessage(messageId, null, FacesMessage.SEVERITY_FATAL);
  }

  /**
   * Enqueue a FacesMessage associated with the specified component,
   * containing the specified messageId text and a message severity level
   * of FacesMessage.SEVERITY_FATAL.
   * @param component
   * @param messageId
   */
  protected void fatal(UIComponent component, String messageId)
  {
    FacesUtils.addMessage(component, messageId,
                          null, FacesMessage.SEVERITY_FATAL);
  }

  public void growl(String messageId)
  {
    this.growl(messageId, null);
  }

  public void growl(String messageId, Object[] params)
  {
    FacesUtils.addMessage("growl", messageId, null, params,
      FacesMessage.SEVERITY_INFO);
  }

  protected Locale getLocale()
  {
    return getFacesContext().getViewRoot().getLocale();
  }
}
