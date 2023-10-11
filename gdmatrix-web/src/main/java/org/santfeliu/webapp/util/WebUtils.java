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

import java.io.IOException;
import java.util.List;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.Resource;
import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.faces.view.facelets.FaceletContext;
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

  // Usage: includeCompositeComponent(column, "comp", "test.xhtml", "someUniqueId");
  public static void includeCompositeComponent(UIComponent parent,
    String libraryName, String resourceName, String id)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Application application = context.getApplication();
    FaceletContext faceletContext = (FaceletContext)context.getAttributes()
      .get(FaceletContext.FACELET_CONTEXT_KEY);

    // This basically creates <ui:component> based on <composite:interface>.
    Resource resource = application.getResourceHandler().createResource(resourceName, libraryName);
    UIComponent composite = application.createComponent(context, resource);
    composite.setId(id); // Mandatory for the case composite is part of UIForm! Otherwise JSF can't find inputs.

    // This basically creates <composite:implementation>.
    UIComponent implementation = application.createComponent(UIPanel.COMPONENT_TYPE);
    implementation.setRendererType("javax.faces.Group");
    composite.getFacets().put(UIComponent.COMPOSITE_FACET_NAME, implementation);

    // Now include the composite component file in the given parent.
    parent.getChildren().add(composite);
    parent.pushComponentToEL(context, composite); // This makes #{cc} available.
    try
    {
      faceletContext.includeFacelet(implementation, resource.getURL());
    }
    catch (IOException e)
    {
      throw new FacesException(e);
    }
    finally
    {
      parent.popComponentFromEL(context);
    }
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
}
