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

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author blanquepa
 */
public class WebUtils
{
  public static final String OBJECT_BACKING = "objectBacking";
  public static final String OBJECT_TYPEID_PROPERTY = "objectTypeId";
  public static final String TOPWEB_PROPERTY = "topweb";

  public static <T> T getInstance(Class<?> clazz)
  {
    String backingName = clazz.getAnnotation(Named.class).value();
    return getBean(backingName);
  }

  public static <T> T getBean(String name)
  {
    return (T) evaluateExpression("#{" + name + "}");
  }

  public static String getBeanName(Object bean)
  {
    Class<? extends Object> clazz = bean.getClass();
    Named named = clazz.getAnnotation(Named.class);
    while (named == null)
    {
      clazz = clazz.getSuperclass();
      if (clazz == null) break;
      named = clazz.getAnnotation(Named.class);
    }
    if (named == null) return null; // bean is not a @Named bean

    String beanName = named.value();
    if (StringUtils.isBlank(beanName) && clazz != null) // get default name
    {
      beanName = clazz.getSimpleName();
      beanName = beanName.substring(0, 1).toLowerCase() + beanName.substring(1);
    }
    return beanName;
  }

  public static <T> T getValue(String expr)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Application application = context.getApplication();
    return (T) application
      .evaluateExpressionGet(context, expr, Object.class);
  }

  public static <T> void setValue(String expr, Class<T> clazz, T value)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    ELContext elContext = context.getELContext();
    Application application = context.getApplication();
    ExpressionFactory expressionFactory = application.getExpressionFactory();
    ValueExpression valueExpression =
      expressionFactory.createValueExpression(elContext, expr, clazz);
    valueExpression.setValue(elContext, value);
  }

  public static ValueExpression createValueExpression(String expr, Class type)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    ELContext elContext = context.getELContext();
    Application application = context.getApplication();
    ExpressionFactory expressionFactory = application.getExpressionFactory();
    return expressionFactory.createValueExpression(elContext, expr, type);
  }

  public static MethodExpression createMethodExpression(String expr,
    Class returnType, Class... parameterTypes)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    ELContext elContext = context.getELContext();
    Application application = context.getApplication();
    ExpressionFactory expressionFactory = application.getExpressionFactory();
    return expressionFactory.createMethodExpression(elContext, expr,
      returnType, parameterTypes);
  }

  public static <T extends UIComponent> T createComponent(String componentType)
    throws FacesException
  {
    Application application =
      FacesContext.getCurrentInstance().getApplication();

    return (T) application.createComponent(componentType);
  }

  public static boolean isRenderResponsePhase()
  {
    PhaseId phaseId = FacesContext.getCurrentInstance().getCurrentPhaseId();
    return phaseId.equals(PhaseId.RENDER_RESPONSE);
  }

  public static boolean isPostback()
  {
    return FacesContext.getCurrentInstance().isPostback();
  }

  @Deprecated
  public static <T> T getBacking(MenuItemCursor mic)
  {
    String backingName = mic.getProperty(OBJECT_BACKING);
    if (backingName != null)
      return getBean(backingName);
    else
      return getBackingFromAction(mic.getAction());
  }

  @Deprecated
  public static <T> T getBackingFromAction(String actionExpression)
  {
    String backingName = getBackingName(actionExpression);
    if (backingName != null)
      return getBean(backingName);
    else
      return null;
  }

  @Deprecated
  public static <T> T getBackingIfExists(String name)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    ExternalContext extContext = context.getExternalContext();
    Map requestMap = extContext.getRequestMap();
    return (T)requestMap.get(name);
  }

  @Deprecated
  private static String getBackingName(String action)
  {
    if (action != null)
    {
      Pattern pattern = Pattern.compile("#\\{(\\w*)\\..*\\}");
      Matcher matcher = pattern.matcher(action);
      if (matcher.find())
      {
        return matcher.group(1);
      }
    }
    return null;
  }

  public static <T> T evaluateExpression(String expr)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Application application = context.getApplication();
    return (T) application
      .evaluateExpressionGet(context, expr, Object.class);
  }

  public static MenuItemCursor getTopWebMenuItem(MenuItemCursor menuItem)
  {
    MenuItemCursor auxMenuItem = menuItem.getClone();

    while (!auxMenuItem.isRoot() &&
      !(auxMenuItem.getDirectProperty(TOPWEB_PROPERTY)!= null &&
      auxMenuItem.getDirectProperty(TOPWEB_PROPERTY).equals("true")))
    {
      auxMenuItem.moveParent();
    }

    return auxMenuItem;
  }

  public static String getMenuItemProperty(String name)
  {
    MenuItemCursor menuItem =
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
    return menuItem.getProperty(name);
  }

  public static List<String> getMultivaluedMenuItemProperty(String name)
  {
    MenuItemCursor menuItem =
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
    return menuItem.getMultiValuedProperty(name);
  }

  public static String getDirectMenuItemProperty(String name)
  {
    MenuItemCursor menuItem =
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
    return menuItem.getDirectProperty(name);
  }

  public static List<String> getDirectMultivaluedMenuItemProperty(String name)
  {
    MenuItemCursor menuItem =
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
    return menuItem.getDirectMultiValuedProperty(name);
  }

  public static boolean isPropertyHidden(String name)
  {
    if (name == null)
      return true;

    String value = getMenuItemProperty("render" + StringUtils.capitalize(name));
    return Boolean.parseBoolean(value);
  }

  @Deprecated
  public static boolean clearBacking(String backingName)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    ExternalContext extContext = context.getExternalContext();
    Map requestMap = extContext.getRequestMap();
    Object backing = requestMap.remove(backingName);
    return backing != null;
  }

}
