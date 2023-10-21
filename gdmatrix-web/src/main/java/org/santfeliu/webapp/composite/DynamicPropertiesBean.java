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
package org.santfeliu.webapp.composite;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.FacesEvent;
import javax.faces.model.SelectItem;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.Property;
import org.primefaces.component.inputtextarea.InputTextarea;
import org.primefaces.component.outputlabel.OutputLabel;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.PropertyConverter;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.form.FormDescriptor;
import org.santfeliu.form.FormFactory;
import org.santfeliu.form.builder.TypeFormBuilder;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.webapp.helpers.PropertyHelper;
import org.santfeliu.webapp.util.ComponentUtils;
import org.santfeliu.webapp.util.FormImporter;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author realor
 */
@Named
@ViewScoped
public class DynamicPropertiesBean implements Serializable
{
  static final String PROPERTY_EDITOR_SELECTOR = "editor";
  static final Map<String, Object> FILTER_OPTIONS = new HashMap();
  static
  {
    FILTER_OPTIONS.put(FormImporter.STACKED_OPTION, "true");
  }
  static final JsonValidator JSON_VALIDATOR = new JsonValidator();

  private final Map<String, List<SelectItem>> selectItemMap = new HashMap<>();
  private PropertyHelper propertyHelper;

  @PostConstruct
  public void init()
  {
    propertyHelper = new PropertyHelper()
    {
      @Override
      public List<Property> getProperties()
      {
        return WebUtils.getValue("#{cc.attrs.properties}");
      }
    };
  }

  public PropertyHelper getPropertyHelper()
  {
    return propertyHelper;
  }

  public void setPropertyHelper(PropertyHelper propertyHelper)
  {
    this.propertyHelper = propertyHelper;
  }

  public String getPropertyJson()
  {
    List<Property> properties = propertyHelper.getProperties();
    if (properties == null) return null;

    String typeId = getTypeId();

    Type type = StringUtils.isBlank(typeId) ?
      null : TypeCache.getInstance().getType(typeId);

    PropertyConverter converter = new PropertyConverter(type);
    Map map = converter.toPropertyMap(properties);
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    return gson.toJson(map);
  }

  public void setPropertyJson(String json)
  {
    String typeId = getTypeId();

    Type type = StringUtils.isBlank(typeId) ?
      null : TypeCache.getInstance().getType(typeId);

    Map map;
    if (StringUtils.isBlank(json))
    {
      map = new HashMap();
    }
    else
    {
      Gson gson = new Gson();
      map = gson.fromJson(json, Map.class);
    }

    PropertyConverter converter = new PropertyConverter(type);
    converter.setHtmlFixing(false);
    List<Property> newProperties = converter.toPropertyList(map);
    List<Property> properties = propertyHelper.getProperties();
    if (properties != null)
    {
      properties.clear();
      properties.addAll(newProperties);
    }
  }

  public List<SelectItem> getSelectItems()
  {
    String prefix = getFormBuilderPrefix();
    String typeId = getTypeId();
    String formKey = prefix + ":" + typeId;

    List<SelectItem> selectItems = selectItemMap.get(formKey);
    if (selectItems == null)
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();

      selectItems = new ArrayList<>();
      if (!StringUtils.isBlank(typeId))
      {
        String selectorBase = formKey +
          TypeFormBuilder.USERID + userSessionBean.getUserId() +
          TypeFormBuilder.PASSWORD + userSessionBean.getPassword();

        List<FormDescriptor> descriptors =
          FormFactory.getInstance().findForms(selectorBase);

        for (FormDescriptor descriptor : descriptors)
        {
          String selector = descriptor.getSelector();
          String label = descriptor.getTitle();
          selectItems.add(new SelectItem(selector, label));
        }
      }
      ResourceBundle bundle = ResourceBundle.getBundle(
        "org.santfeliu.web.obj.resources.ObjectBundle",
        userSessionBean.getViewLocale());

      if (userSessionBean.isUserInRole("DOC_ADMIN"))
      {
        selectItems.add(new SelectItem(PROPERTY_EDITOR_SELECTOR,
          bundle.getString("property_editor")));
      }

      if (selectItems.isEmpty())
      {
        selectItems.add(
          new SelectItem("", bundle.getString("type_without_form")));
      }
      selectItemMap.put(formKey, selectItems);
    }
    return selectItems;
  }

  public void onItemSelect(FacesEvent event)
  {
    UIComponent component = event.getComponent();
    UIComponent panel = component.findComponent("dyn_form");
    if (panel != null)
    {
      updateComponents(panel);
    }
  }

  public void loadDynamicComponents(ComponentSystemEvent event)
  {
    UIComponent panel = event.getComponent();
    updateComponents(panel);
  }

  public Map<String, Object> getFilterOptions()
  {
    return FILTER_OPTIONS;
  }

  public Map<String, Object> getEditOptions(String submitButton)
  {
    return Collections.singletonMap(FormImporter.SUBMIT_BUTTON_OPTION,
      submitButton);
  }

  private void updateComponents(UIComponent panel)
  {
    try
    {
      List<SelectItem> selectItems = getSelectItems();
      String formSelector = getFormSelector();

      if (StringUtils.isBlank(formSelector) ||
          !isValidFormSelector(formSelector, selectItems))
      {
        // set first formSelector
        formSelector = (String)selectItems.get(0).getValue();
        setFormSelector(formSelector);
      }

      // save formSelector in styleClass property of outputText component
      HtmlOutputText hidden =
        (HtmlOutputText)panel.findComponent("form_selector");
      String actualFormSelector = (String)hidden.getStyleClass();

      if (!formSelector.equals(actualFormSelector))
      {
        hidden.setStyleClass(formSelector);

        panel.getChildren().clear();

        if (PROPERTY_EDITOR_SELECTOR.equals(formSelector))
        {
          System.out.println(">>>> property_editor");
          Application application = FacesContext.getCurrentInstance().getApplication();

          HtmlPanelGroup group =
            (HtmlPanelGroup)application.createComponent(HtmlPanelGroup.COMPONENT_TYPE);

          group.setStyleClass("field col-12");
          group.setLayout("block");
          panel.getChildren().add(group);

           OutputLabel label =
            (OutputLabel)application.createComponent(OutputLabel.COMPONENT_TYPE);

          label.setValue("JSON");
          label.setFor("@next");
          group.getChildren().add(label);

          InputTextarea textArea =
            (InputTextarea)application.createComponent(InputTextarea.COMPONENT_TYPE);
          textArea.setStyleClass("field col-12");
          textArea.setStyle("font-family:monospace");
          textArea.addValidator(JSON_VALIDATOR);
          textArea.setValueExpression("value",
            WebUtils.createValueExpression("#{dynamicPropertiesBean.propertyJson}", String.class));
          group.getChildren().add(textArea);
        }
        else if (!StringUtils.isBlank(formSelector))
        {
          System.out.println(">>>> importing form: " + formSelector);

          ComponentUtils.includeFormComponents(panel, formSelector,
             "dynamicPropertiesBean.propertyHelper.value",
            propertyHelper.getValue(), getOptions());
        }
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  public String getFormSelector()
  {
    return WebUtils.getValue("#{cc.attrs.formSelector}");
  }

  public void setFormSelector(String formSelector)
  {
    WebUtils.setValue("#{cc.attrs.formSelector}", String.class, formSelector);
  }

  public String getTypeId()
  {
    return WebUtils.getValue("#{cc.attrs.typeId}");
  }

  public String getFormBuilderPrefix()
  {
    return WebUtils.getValue("#{cc.attrs.formBuilderPrefix}");
  }

  public Map<String, Object> getOptions()
  {
    return WebUtils.getValue("#{cc.attrs.options}");
  }

  public boolean isPropertyEditorRendered()
  {
    return PROPERTY_EDITOR_SELECTOR.equals(getFormSelector());
  }

  private boolean isValidFormSelector(String formSelector,
    List<SelectItem> selectItems)
  {
    if (PROPERTY_EDITOR_SELECTOR.equals(formSelector)) return true;

    for (SelectItem selectItem : selectItems)
    {
      if (formSelector.equals(selectItem.getValue())) return true;
    }
    return false;
  }

  public static class JsonValidator implements Validator<String>
  {
    @Override
    public void validate (FacesContext facesContext, UIComponent component,
      String json)
    {
      try
      {
        if (!StringUtils.isBlank(json))
        {
          JsonParser.parseString(json);
        }
      }
      catch (Exception ex)
      {
        throw new ValidatorException(FacesUtils.getFacesMessage(ex));
      }
    }
  }
}
