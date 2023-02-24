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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.ValueHolder;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;
import org.mozilla.javascript.Callable;
import org.primefaces.component.datepicker.DatePicker;
import org.primefaces.component.inputtext.InputText;
import org.primefaces.component.outputlabel.OutputLabel;
import org.primefaces.component.tabview.TabView;
import org.primefaces.component.toggleswitch.ToggleSwitch;
import org.santfeliu.form.Field;
import org.santfeliu.form.Form;
import org.santfeliu.form.FormFactory;
import org.santfeliu.form.View;
import org.santfeliu.util.script.ScriptClient;

/**
 *
 * @author realor
 */
public class ComponentUtils
{

  public static <T extends UIComponent> T findComponent(String expression)
  {
    UIViewRoot viewRoot = FacesContext.getCurrentInstance().getViewRoot();
    try
    {
      return (T)viewRoot.findComponent(expression);
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  public static void includeFormComponents(UIComponent parent, String selector,
    String propertyPath, Map context) throws Exception
  {
    FacesContext facesContext = FacesContext.getCurrentInstance();
    Application application = facesContext.getApplication();

    FormFactory formFactory = FormFactory.getInstance();
    formFactory.clearForm(selector);
    Form form = formFactory.getForm(selector, context, true);

    Collection<Field> fields = form.getFields();
    for (Field field : fields)
    {
      String labelText = field.getLabel();
      String reference = field.getReference();
      String fieldType = field.getType();
      View view = form.getView(reference);

      String styleClass = null;
      if (view != null)
      {
        Object styleClassValue = view.getProperty("class");
        styleClass = styleClassValue instanceof String ?
          (String) styleClassValue : null;
      }
      
      HtmlPanelGroup group = new HtmlPanelGroup();
      group.setStyleClass(styleClass == null ?
        "field col-12 md:col-6" : styleClass);
      group.setLayout("block");
      parent.getChildren().add(group);

      OutputLabel label = new OutputLabel();
      label.setValue(labelText);
      label.setFor("@next");
      group.getChildren().add(label);

      UIComponent component;

      if (Field.DATE.equals(fieldType) || Field.DATETIME.equals(fieldType))
      {
        DatePicker datePicker
          = (DatePicker) application.createComponent(DatePicker.COMPONENT_TYPE);
        datePicker.setLocale(facesContext.getViewRoot().getLocale());
        datePicker.setConverter(
          application.createConverter("datePickerConverter"));
        datePicker.setPattern(Field.DATE.equals(fieldType)
          ? "dd/MM/yyyy" : "dd/MM/yyyy HH:mm");
        datePicker.setShowIcon(true);
        datePicker.setShowOnFocus(false);
        component = datePicker;
      }
      else if (Field.BOOLEAN.equals(fieldType))
      {
        ToggleSwitch toogleSwitch =
          (ToggleSwitch) application.createComponent(ToggleSwitch.COMPONENT_TYPE);
        component = toogleSwitch;
      }
      else
      {
        InputText inputText
          = (InputText) application.createComponent(InputText.COMPONENT_TYPE);
        component = inputText;
      }

      if (component instanceof ValueHolder)
      {
        String expression = "#{" + propertyPath + "." + reference + "}";

        ValueExpression valueExpression
          = WebUtils.createValueExpression(expression, Object.class);

        component.setValueExpression("value", valueExpression);
        group.getChildren().add(component);
      }
    }
  }

  public static void includeScriptComponents(UIComponent parent,
    String scriptName) throws Exception
  {
    ScriptClient client = new ScriptClient();
    client.refreshCache();
    client.executeScript(scriptName);
    Callable callable = (Callable) client.get("includeComponents");
    if (callable != null)
    {
      client.execute(callable, parent, scriptName);
    }
  }

  public static void selectTabWithErrors(String tabViewClientId)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    if (context.isValidationFailed())
    {
      UIViewRoot viewRoot = context.getViewRoot();
      Iterator<String> iter = context.getClientIdsWithMessages();
      if (iter.hasNext())
      {
        String id = iter.next();
        UIComponent component = viewRoot.findComponent(id);

        while (component != null && component != viewRoot)
        {
          if (component instanceof org.primefaces.component.tabview.Tab)
          {
            TabView currentTabView = (TabView)component.getParent();
            int index = currentTabView.getChildren().indexOf(component);
            if (index >= 0)
            {
              currentTabView.setActiveIndex(index);
              if (currentTabView.getClientId().equals(tabViewClientId))
                return;
            }
          }
          component = component.getParent();
        }
      }
    }
  }

}
