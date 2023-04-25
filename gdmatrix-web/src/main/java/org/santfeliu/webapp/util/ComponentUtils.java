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

import edu.emory.mathcs.backport.java.util.Collections;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.UISelectItem;
import javax.faces.component.UIViewRoot;
import javax.faces.component.ValueHolder;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;
import org.mozilla.javascript.Callable;
import org.primefaces.component.datepicker.DatePicker;
import org.primefaces.component.inputtext.InputText;
import org.primefaces.component.inputtextarea.InputTextarea;
import org.primefaces.component.outputlabel.OutputLabel;
import org.primefaces.component.selectcheckboxmenu.SelectCheckboxMenu;
import org.primefaces.component.selectonemenu.SelectOneMenu;
import org.primefaces.component.tabview.TabView;
import org.primefaces.component.toggleswitch.ToggleSwitch;
import org.santfeliu.form.Field;
import org.santfeliu.form.Form;
import org.santfeliu.form.FormFactory;
import org.santfeliu.form.View;
import org.santfeliu.util.script.ScriptClient;
import org.santfeliu.form.type.html.HtmlSelectView;

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
    includeFormComponents(parent, selector, propertyPath, context, null);
  }

  public static void includeFormComponents(UIComponent parent, String selector,
    String propertyPath, Map context, Map<String, Object> options) throws Exception
  {
    if (options == null) options = Collections.emptyMap();

    boolean isStacked = "true".equals(options.get("stacked"));

    FacesContext facesContext = FacesContext.getCurrentInstance();
    Application application = facesContext.getApplication();

    FormFactory formFactory = FormFactory.getInstance();
    formFactory.clearForm(selector);
    Form form = formFactory.getForm(selector, context, false);

    Collection<Field> fields = form.getFields();
    for (Field field : fields)
    {
      String labelText = field.getLabel();
      String reference = field.getReference();
      String fieldType = field.getType();
      View view = form.getView(reference);

      UIComponent component;
      boolean isMultiple = false;

      if (field.isReadOnly() && view == null)
      {
        // ignore
        component = null;
      }
      else if (Field.DATE.equals(fieldType) || Field.DATETIME.equals(fieldType))
      {
        DatePicker datePicker =
          (DatePicker)application.createComponent(DatePicker.COMPONENT_TYPE);
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
        if (isStacked || field.getMinOccurs() == 0)
        {
          SelectOneMenu selectOneMenu =
            (SelectOneMenu)application.createComponent(SelectOneMenu.COMPONENT_TYPE);

          UISelectItem selectItem = new UISelectItem();
          selectItem.setItemValue(null);
          selectItem.setItemLabel("");
          selectOneMenu.getChildren().add(selectItem);

          selectItem = new UISelectItem();
          selectItem.setItemValue(false);
          selectItem.setItemLabel("FALSE");
          selectOneMenu.getChildren().add(selectItem);

          selectItem = new UISelectItem();
          selectItem.setItemValue(true);
          selectItem.setItemLabel("TRUE");
          selectOneMenu.getChildren().add(selectItem);
          component = selectOneMenu;
        }
        else
        {
          ToggleSwitch toogleSwitch =
            (ToggleSwitch)application.createComponent(ToggleSwitch.COMPONENT_TYPE);
          toogleSwitch.setStyleClass("block");
          component = toogleSwitch;
        }
      }
      else
      {
        if (view instanceof HtmlSelectView)
        {
          String multipleValue = String.valueOf(view.getProperty("multiple"));
          isMultiple = "true".equals(multipleValue);

          if (isMultiple)
          {
            SelectCheckboxMenu select =
              (SelectCheckboxMenu)application.createComponent(SelectCheckboxMenu.COMPONENT_TYPE);
            select.setMultiple(true);
            select.setReadonly(field.isReadOnly());
            component = select;
          }
          else
          {
            SelectOneMenu select =
              (SelectOneMenu)application.createComponent(SelectOneMenu.COMPONENT_TYPE);
            select.setReadonly(field.isReadOnly());
            component = select;
          }

          List<View> children = view.getChildren();

          for (View child : children)
          {
            if (View.ITEM.equals(child.getViewType()))
            {
              String itemValue = (String)child.getProperty("value");
              if (!child.getChildren().isEmpty())
              {
                String itemLabel =
                  (String)child.getChildren().get(0).getProperty("text");
                UISelectItem selectItem = new UISelectItem();
                selectItem.setItemValue(itemValue);
                selectItem.setItemLabel(itemLabel);
                component.getChildren().add(selectItem);
              }
            }
          }
        }
        else
        {
          if (view != null && "textarea".equalsIgnoreCase(view.getNativeViewType()))
          {
            InputTextarea inputTextarea =
              (InputTextarea)application.createComponent(InputTextarea.COMPONENT_TYPE);
            inputTextarea.setReadonly(field.isReadOnly());
            component = inputTextarea;
          }
          else
          {
            InputText inputText =
             (InputText)application.createComponent(InputText.COMPONENT_TYPE);
            inputText.setReadonly(field.isReadOnly());
            component = inputText;
          }
        }
      }

      // add component to panel
      if (component instanceof ValueHolder)
      {
        String styleClass = null;
        if (view != null)
        {
          Object styleClassValue = view.getProperty("class");
          styleClass = styleClassValue instanceof String ?
            (String)styleClassValue : null;
        }

        HtmlPanelGroup group =
          (HtmlPanelGroup)application.createComponent(HtmlPanelGroup.COMPONENT_TYPE);
        if (isStacked)
        {
          styleClass = "col-12";
        }
        else
        {
          if (styleClass == null || !styleClass.contains("col-"))
          {
            styleClass = "col-12 md:col-6";
          }
        }
        group.setStyleClass("field " + styleClass);
        group.setLayout("block");
        parent.getChildren().add(group);

        OutputLabel label =
          (OutputLabel)application.createComponent(OutputLabel.COMPONENT_TYPE);

        label.setValue(labelText);
        label.setFor("@next");
        group.getChildren().add(label);

        String expression = "#{" + propertyPath + (isMultiple ? "s" : "") +
          "[\"" + reference + "\"]}";

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
