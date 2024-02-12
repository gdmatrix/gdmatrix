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
package org.santfeliu.faces.savebean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.faces.application.Application;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;

/**
 *
 * @author realor
 */
@FacesComponent(value = "org.gdmatrix.faces.SaveBean")
public class SaveBean extends UIParameter
{
  public SaveBean()
  {
    setRendererType(null);
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object[] values = new Object[2];
    values[0] = super.saveState(context);
    values[1] = saveBeanState(getBean());

    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object values[] = (Object[])state;
    super.restoreState(context, values[0]);
    restoreBeanState(getBean(), (Object[])values[1]);
  }

  private Object getBean()
  {
    String beanName = (String)getValue();
    FacesContext context = FacesContext.getCurrentInstance();
    Application application = context.getApplication();
    return application
      .evaluateExpressionGet(context, "#{" + beanName + "}", Object.class);
  }

  private Object[] saveBeanState(Object bean)
  {
    List<Field> savableFields = getSavableFields(bean.getClass());
    Object[] state = new Object[savableFields.size()];
    for (int i = 0; i < savableFields.size(); i++)
    {
      Field field = savableFields.get(i);
      try
      {
        state[i] = field.get(bean);
        //System.out.println(">>> Saving " + field.getName() + " = " + state[i]);
      }
      catch (Exception ex)
      {
        state[i] = null;
      }
    }
    return state;
  }

  private void restoreBeanState(Object bean, Object[] state)
  {
    List<Field> savableFields = getSavableFields(bean.getClass());
    for (int i = 0; i < savableFields.size(); i++)
    {
      Field field = savableFields.get(i);
      try
      {
        field.set(bean, state[i]);
        //System.out.println(">>> Restore " + field.getName() + " = " + state[i]);
      }
      catch (Exception ex)
      {
        // ignore
      }
    }
  }

  private List<Field> getSavableFields(Class beanClass)
  {
    ArrayList<Field> savableFields = new ArrayList<>();
    List<Field> declaredFields = getAllFields(beanClass);

    for (Field field : declaredFields)
    {
      if (!Modifier.isTransient(field.getModifiers()) &&
          !Modifier.isStatic(field.getModifiers()))
      {
        Annotation[] annotations = field.getAnnotations();
        if (annotations.length == 0)
        {
          savableFields.add(field);
          field.setAccessible(true);
        }
      }
    }
    return savableFields;
  }
  
  private List<Field> getAllFields(Class<?> type)
  {
    List<Field> fields = new ArrayList<>();
    for (Class<?> c = type; c != null; c = c.getSuperclass())
    {
      fields.addAll(Arrays.asList(c.getDeclaredFields()));
    }
    return fields;
  }
}
