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
package org.santfeliu.webapp.util;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.component.ActionSource2;
import javax.faces.component.UIComponent;
import javax.faces.component.UISelectItem;
import javax.faces.component.UISelectItems;
import javax.faces.component.behavior.AjaxBehavior;
import javax.faces.component.behavior.ClientBehaviorHolder;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.AjaxBehaviorListener;
import javax.faces.event.FacesEvent;

/**
 *
 * @author realor
 */
public class ComponentBuilder
{
  Stack<Object> stack = new Stack<>();
  Map<String, String> map = new HashMap<>();

  public ComponentBuilder()
  {
  }

  public ComponentBuilder map(String tag, String componentType)
    throws Exception
  {
    map.put(tag, componentType);

    return this;
  }

  public Object current()
  {
    return stack.peek();
  }

  public ComponentBuilder component(UIComponent component) throws Exception
  {
    stack.push(component);
    return this;
  }

  public ComponentBuilder component(String tag) throws Exception
  {
    String componentType = map.get(tag);

    if (componentType == null) componentType = tag;

    UIComponent component = WebUtils.createComponent(componentType);
    if (!stack.isEmpty())
    {
      UIComponent parent = (UIComponent)stack.peek();
      parent.getChildren().add(component);
    }
    stack.push(component);

    return this;
  }

  public ComponentBuilder facet(String name) throws Exception
  {
    UIComponent component = (UIComponent)stack.peek();
    UIComponent facet = component.getFacet(name);
    stack.push(facet);

    return this;
  }

  public ComponentBuilder ajax(String event) throws Exception
  {
    UIComponent component = (UIComponent)stack.peek();
    if (!(component instanceof ClientBehaviorHolder))
      throw new Exception("Component does not support ajax.");

    ClientBehaviorHolder cbHolder = (ClientBehaviorHolder)component;

    FacesContext facesContext = FacesContext.getCurrentInstance();
    AjaxBehavior cbAjax = (AjaxBehavior)facesContext.getApplication()
      .createBehavior(AjaxBehavior.BEHAVIOR_ID);
    cbHolder.addClientBehavior(event, cbAjax);

    stack.push(cbAjax);

    return this;
  }

  public ComponentBuilder end() throws Exception
  {
    stack.pop();

    return this;
  }

  public ComponentBuilder action(String action) throws Exception
  {
    return action(action, null);
  }

  public ComponentBuilder action(String action,
    Class returnType, Class... parameterTypes)
    throws Exception
  {
    UIComponent component = (UIComponent)stack.peek();
    if (component instanceof ActionSource2)
    {
      ActionSource2 actionSource = (ActionSource2)component;
      MethodExpression expr =
        WebUtils.createMethodExpression(action, returnType, parameterTypes);
      actionSource.setActionExpression(expr);
    }
    else throw new Exception("Component is not ActionSource");

    return this;
  }

  public ComponentBuilder listener(ActionListener listener)
  {
    ActionSource2 actionSource = (ActionSource2)stack.peek();
    actionSource.addActionListener(listener);

    return this;
  }

  public ComponentBuilder listener(AjaxBehaviorListener listener)
  {
    AjaxBehavior ajaxb = (AjaxBehavior)stack.peek();
    ajaxb.addAjaxBehaviorListener(listener);

    return this;
  }

  public ComponentBuilder listener(String listener)
  {
    Object object = stack.peek();
    if (object instanceof ActionSource2)
    {
      ActionSource2 actionSource = (ActionSource2)object;
      actionSource.addActionListener(new ActionListenerImpl(listener));
    }
    else if (object instanceof AjaxBehavior)
    {
      AjaxBehavior ajaxb = (AjaxBehavior)object;
      ajaxb.addAjaxBehaviorListener(new AjaxBehaviorListenerImpl(listener));
    }
    return this;
  }

  public ComponentBuilder selectItem()
  {
    UIComponent component = (UIComponent)stack.peek();
    UISelectItem selectItem = new UISelectItem();
    component.getChildren().add(selectItem);
    stack.push(selectItem);

    return this;
  }

  public ComponentBuilder selectItems()
  {
    UIComponent component = (UIComponent)stack.peek();
    UISelectItems selectItems = new UISelectItems();
    component.getChildren().add(selectItems);
    stack.push(selectItems);

    return this;
  }

  public ComponentBuilder setId(String id) throws Exception
  {
    UIComponent component = (UIComponent)stack.peek();
    component.setId(id);

    return this;
  }

  public ComponentBuilder set(String attribute, Object value) throws Exception
  {
    Object object = stack.peek();

    if (value instanceof String)
    {
      String svalue = (String)value;
      if (svalue.contains("#{"))
      {
        ValueExpression expression =
          WebUtils.createValueExpression(svalue, Object.class);

        if (object instanceof UIComponent)
        {
          UIComponent component = (UIComponent)object;
          component.setValueExpression(attribute, expression);
        }
        else if (object instanceof AjaxBehavior)
        {
          AjaxBehavior ajaxb = (AjaxBehavior)object;
          ajaxb.setValueExpression(attribute, expression);
        }
        return this;
      }
    }

    Class cls = object.getClass();
    String setterName  =
      "set" + attribute.substring(0, 1).toUpperCase() + attribute.substring(1);

    Method methods[] = cls.getMethods();
    for (Method method : methods)
    {
      if (method.getName().equals(setterName))
      {
        if (method.getParameterCount() == 1)
        {
          Class<?> typeClass = method.getParameterTypes()[0];
          if (typeClass.isInstance(value))
          {
            method.invoke(object, value);
            break;
          }
          else if (Converter.class.isAssignableFrom(typeClass)
                   && value instanceof String)
          {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            Application application = facesContext.getApplication();
            Converter converter = application.createConverter(value.toString());
            method.invoke(object, converter);
          }
          else if (typeClass.equals(Collection.class)
                   && value instanceof String)
          {
            String svalue = value.toString();
            String[] values = svalue.split(" ");
            method.invoke(object, Arrays.asList(values));
            break;
          }
          else if (typeClass.equals(boolean.class) && value instanceof Boolean)
          {
            method.invoke(object, value);
            break;
          }
          else if (typeClass.equals(int.class) && value instanceof Number)
          {
            method.invoke(object, ((Number)value).intValue());
            break;
          }
          else if (typeClass.equals(long.class) && value instanceof Number)
          {
            method.invoke(object, ((Number)value).longValue());
            break;
          }
          else if (typeClass.equals(float.class) && value instanceof Number)
          {
            method.invoke(object, ((Number)value).floatValue());
            break;
          }
          else if (typeClass.equals(double.class) && value instanceof Number)
          {
            method.invoke(object, ((Number)value).doubleValue());
            break;
          }
        }
      }
    }

    return this;
  }

  public static class ActionListenerImpl
    implements ActionListener, Serializable
  {
    private final String listener;

    ActionListenerImpl(String listener)
    {
      this.listener = listener;
    }

    @Override
    public void processAction(ActionEvent event) throws AbortProcessingException
    {
      MethodExpression expr =
        WebUtils.createMethodExpression(listener, Void.class, FacesEvent.class);

      FacesContext facesContext = FacesContext.getCurrentInstance();
      expr.invoke(facesContext.getELContext(), new Object[]{ event });
    }
  }

  public static class AjaxBehaviorListenerImpl
    implements AjaxBehaviorListener, Serializable
  {
    private final String listener;

    AjaxBehaviorListenerImpl(String listener)
    {
      this.listener = listener;
    }

    @Override
    public void processAjaxBehavior(AjaxBehaviorEvent event)
      throws AbortProcessingException
    {
      MethodExpression expr =
        WebUtils.createMethodExpression(listener, Void.class, FacesEvent.class);

      FacesContext facesContext = FacesContext.getCurrentInstance();
      expr.invoke(facesContext.getELContext(), new Object[]{ event });
    }
  }

}
