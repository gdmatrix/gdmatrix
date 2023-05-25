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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.UISelectItem;
import javax.faces.component.ValueHolder;
import javax.faces.component.html.HtmlOutputLink;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;
import org.apache.commons.lang.StringUtils;
import org.primefaces.component.datepicker.DatePicker;
import org.primefaces.component.inputtext.InputText;
import org.primefaces.component.inputtextarea.InputTextarea;
import org.primefaces.component.outputlabel.OutputLabel;
import org.primefaces.component.selectcheckboxmenu.SelectCheckboxMenu;
import org.primefaces.component.selectonemenu.SelectOneMenu;
import org.primefaces.component.selectoneradio.SelectOneRadio;
import org.primefaces.component.texteditor.TextEditor;
import org.primefaces.component.toggleswitch.ToggleSwitch;
import org.santfeliu.form.Field;
import org.santfeliu.form.Form;
import org.santfeliu.form.View;
import org.santfeliu.form.type.html.HtmlForm;
import org.santfeliu.form.type.html.HtmlRadioView;
import org.santfeliu.form.type.html.HtmlSelectView;
import org.santfeliu.form.type.html.HtmlView;


/**
 *
 * @author realor
 */

public class FormImporter
{
  protected Map<String, Object> options = new HashMap<>();
  protected Form form;
  protected UIComponent formRoot;
  protected String propertyPath;
  protected HashSet<String> importedReferences = new HashSet<>();

  public Map<String, Object> getOptions()
  {
    return options;
  }

  public void importForm(Form form, UIComponent formRoot, String propertyPath)
  {
    this.form = form;
    this.formRoot = formRoot;
    this.propertyPath = propertyPath;

    if (form instanceof HtmlForm)
    {
      HtmlForm htmlForm = (HtmlForm)form;
      HtmlView rootView = (HtmlView)htmlForm.getRootView();
      importHtmlView(rootView, formRoot);
    }
    else
    {
      importFields();
    }
  }

  protected void importHtmlView(HtmlView view, UIComponent parent)
  {
    String tag = view.getNativeViewType();

    if (tag == null) tag = "span";

    if (tag.equals("label"))
    {
      // discard, paint labels with fields
    }
    else if (tag.equals("input") ||
             tag.equals("textarea") ||
             tag.equals("select") ||
             tag.equals("checkbox"))
    {
      String reference = view.getReference();
      Field field = form.getField(reference);
      importField(field, parent);
    }
    else if (tag.equals("a"))
    {
      importLink(view, parent);
    }
    else if (tag.equals("script"))
    {
      importScript(view, parent);
    }
    else if (View.TEXT.equals(view.getViewType()))
    {
      importText(view, parent);
    }
    else
    {
      importChildren(view, parent);
    }
  }

  protected void importChildren(HtmlView view, UIComponent parent)
  {
    List<View> childViews = view.getChildren();
    for (View childView : childViews)
    {
      importHtmlView((HtmlView)childView, parent);
    }
  }

  protected void importText(HtmlView view, UIComponent parent)
  {
    String text = view.getProperty("text");
    if (StringUtils.isBlank(text)) return;
    if (text.endsWith(":")) return;
    if (text.toLowerCase().contains("dd/mm/")) return;

    FacesContext facesContext = FacesContext.getCurrentInstance();
    Application application = facesContext.getApplication();

    HtmlOutputText outputText =
      (HtmlOutputText)application.createComponent(HtmlOutputText.COMPONENT_TYPE);

    outputText.setValue(text);
    outputText.setStyleClass("field col-12 md:col-6");
    parent.getChildren().add(outputText);
  }

  protected void importLink(HtmlView view, UIComponent parent)
  {
    String url = view.getProperty("href");
    String target = view.getProperty("target");

    List<View> children = view.getChildren();
    if (children.size() == 1 && !StringUtils.isBlank(url))
    {
      HtmlView childView = (HtmlView)children.get(0);
      if (View.TEXT.equals(childView.getViewType()))
      {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Application application = facesContext.getApplication();

        HtmlOutputLink outputLink =
          (HtmlOutputLink)application.createComponent(HtmlOutputLink.COMPONENT_TYPE);
        outputLink.setValue(url);
        outputLink.setTarget(target);

        HtmlOutputText outputText =
          (HtmlOutputText)application.createComponent(HtmlOutputText.COMPONENT_TYPE);
        outputText.setValue(childView.getProperty("text"));
        outputLink.getChildren().add(outputText);

        parent.getChildren().add(outputLink);
      }
    }
  }

  protected void importScript(HtmlView view, UIComponent parent)
  {
  }

//  protected void importGroupView(HtmlView view, UIComponent parent)
//  {
//    FacesContext facesContext = FacesContext.getCurrentInstance();
//    Application application = facesContext.getApplication();
//
//    HtmlPanelGroup group =
//      (HtmlPanelGroup)application.createComponent(HtmlPanelGroup.COMPONENT_TYPE);
//    group.setStyleClass("col-12");
//    group.setLayout("block");
//
//    parent.getChildren().add(group);
//
//    importChildren(view, group);
//  }

  protected void importFields()
  {
    Collection<Field> fields = form.getFields();
    for (Field field : fields)
    {
      importField(field, formRoot);
    }
  }

  protected void importField(Field field, UIComponent parent)
  {
    boolean isStacked = "true".equals(options.get("stacked"));

    FacesContext facesContext = FacesContext.getCurrentInstance();
    Application application = facesContext.getApplication();

    String labelText = field.getLabel();
    String reference = field.getReference();
    String fieldType = field.getType();
    View view = form.getView(reference);

    UIComponent component = null;
    boolean isMultiple = false;

    if (field.isReadOnly() && view == null)
    {
      // ignore
    }
    else if (Field.DATE.equals(fieldType) || Field.DATETIME.equals(fieldType))
    {
      DatePicker datePicker =
        (DatePicker)application.createComponent(DatePicker.COMPONENT_TYPE);
      datePicker.setLocale(facesContext.getViewRoot().getLocale());
      datePicker.setConverter(
        application.createConverter("datePickerConverter"));
      String pattern = Field.DATE.equals(fieldType)
        ? "dd/MM/yyyy" : "dd/MM/yyyy HH:mm";
      datePicker.setPattern(pattern);
      datePicker.setPlaceholder(pattern);
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
          select.setAutoWidth(false);
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
      else if (view instanceof HtmlRadioView)
      {
        if (!importedReferences.contains(reference))
        {
          List<View> radioViews = new ArrayList<>();
          findViews(form.getRootView(), reference, radioViews);
          SelectOneRadio selectOneRadio =
            (SelectOneRadio)application.createComponent(SelectOneRadio.COMPONENT_TYPE);

          for (View radioView : radioViews)
          {
            String radioId = radioView.getId();
            String itemValue = String.valueOf(radioView.getProperty("value"));
            String itemLabel = itemValue;
            if (!StringUtils.isBlank(radioId))
            {
              View labelView =
                findLabelViewForId(form.getRootView(), radioId);
              if (labelView != null && labelView.getChildren().size() > 0)
              {
                View textView = labelView.getChildren().get(0);
                itemLabel = String.valueOf(textView.getProperty("text"));
              }
            }
            UISelectItem selectItem = new UISelectItem();
            selectItem.setItemValue(itemValue);
            selectItem.setItemLabel(itemLabel);
            selectOneRadio.getChildren().add(selectItem);
          }
          importedReferences.add(reference);
          labelText = null;
          component = selectOneRadio;
        }
      }
      else
      {
        if (view != null && "textarea".equalsIgnoreCase(view.getNativeViewType()))
        {
          String renderer = (String)view.getProperty("renderer");
          if ("htmlEditor".equalsIgnoreCase(renderer))
          {
            TextEditor textEditor =
              (TextEditor)application.createComponent(TextEditor.COMPONENT_TYPE);
            textEditor.setReadonly(field.isReadOnly());
            textEditor.setSecure(false);
            component = textEditor;
          }
          else
          {
            InputTextarea inputTextarea =
              (InputTextarea)application.createComponent(InputTextarea.COMPONENT_TYPE);
            inputTextarea.setReadonly(field.isReadOnly());
            component = inputTextarea;
          }
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
      if (isStacked || component instanceof InputTextarea ||
          component instanceof TextEditor)
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

      if (!StringUtils.isBlank(labelText))
      {
        OutputLabel label =
          (OutputLabel)application.createComponent(OutputLabel.COMPONENT_TYPE);

        label.setValue(labelText);
        label.setFor("@next");
        group.getChildren().add(label);
      }

      String expression = "#{" + propertyPath + (isMultiple ? "s" : "") +
        "[\"" + reference + "\"]}";

      ValueExpression valueExpression
        = WebUtils.createValueExpression(expression, Object.class);

      component.setValueExpression("value", valueExpression);
      group.getChildren().add(component);
    }
  }

  protected void findViews(View base, String reference, List<View> views)
  {
    List<View> children = base.getChildren();
    for (View child : children)
    {
      if (reference.equals(child.getReference()))
      {
        views.add(child);
      }
      else
      {
        findViews(child, reference, views);
      }
    }
  }

  protected View findLabelViewForId(View base, String id)
  {
    Object forId = base.getProperty("for");
    if (id.equals(forId)) return base;

    List<View> children = base.getChildren();
    for (View childView : children)
    {
      View view = findLabelViewForId(childView, id);
      if (view != null) return view;
    }
    return null;
  }
}
