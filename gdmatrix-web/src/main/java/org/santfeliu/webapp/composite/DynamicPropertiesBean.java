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
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.FacesEvent;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.Property;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.primefaces.component.inputtextarea.InputTextarea;
import org.primefaces.component.outputlabel.OutputLabel;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.PropertyConverter;
import org.santfeliu.form.Form;
import org.santfeliu.form.FormDescriptor;
import org.santfeliu.form.FormFactory;
import org.santfeliu.form.builder.TypeFormBuilder;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.webapp.helpers.PropertyHelper;
import org.santfeliu.webapp.modules.doc.DocModuleBean;
import org.santfeliu.webapp.util.ComponentUtils;
import org.santfeliu.webapp.util.FormImporter;
import org.santfeliu.webapp.util.WebUtils;
import org.santfeliu.webapp.validators.JsonValidator;

/**
 *
 * @author realor
 */
@Named
@ViewScoped
public class DynamicPropertiesBean implements Serializable
{
  static final String PROPERTY_EDITOR_SELECTOR = "editor";
  static final String PROPERTY_EDITOR_ID = "editor_id";
  static final String FORM_ID = "formid";
  static final String DATA_HASH = "datahash";

  static final Map<String, Object> FILTER_OPTIONS = new HashMap();
  static
  {
    FILTER_OPTIONS.put(FormImporter.STACKED_OPTION, "true");
    FILTER_OPTIONS.put(FormImporter.SEARCH_FORM_OPTION, "true");
  }
  static final JsonValidator JSON_VALIDATOR = new JsonValidator();

  private final Map<String, List<SelectItem>> selectItemMap = new HashMap<>();
  private final Map<String, Boolean> inspectModeMap = new HashMap<>();  
  private PropertyHelper propertyHelper;

  @PostConstruct
  public void init()
  {
    propertyHelper = new PropertyHelper()
    {
      @Override
      public Object getObject()
      {
        return WebUtils.getValue("#{cc.attrs.object}");
      }

      @Override
      public Type getType()
      {
        return DynamicPropertiesBean.this.getType();
      }

      @Override
      public List<Property> getProperties()
      {
        List<Property> properties = WebUtils.getValue("#{cc.attrs.properties}");
        if (properties == null)
        {
          properties = super.getProperties();
        }
        return properties;
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

    Type type = getType();

    PropertyConverter converter = new PropertyConverter(type);
    Map map = converter.toPropertyMap(properties);
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    return gson.toJson(map);
  }

  public void setPropertyJson(String json)
  {
    Type type = getType();

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

  public boolean isInspectMode()
  {
    String prefix = getFormBuilderPrefix();
    if (!inspectModeMap.containsKey(prefix))
    {
      inspectModeMap.put(prefix, Boolean.FALSE);
    }
    return inspectModeMap.get(prefix);
  }

  public void setInspectMode(boolean inspectMode)
  {
    String prefix = getFormBuilderPrefix();
    inspectModeMap.put(prefix, inspectMode);
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

  public void onSelectForm(FacesEvent event)
  {
    UIComponent component = event.getComponent();
    UIComponent panel = component.findComponent("dyn_form");
    
    if (panel != null)
    {
      updateComponents(panel);
    }
  }

  public void onInspectForm(FacesEvent event)
  {
    setInspectMode(!isInspectMode());
    UIComponent component = event.getComponent();
    UIComponent panel = component.findComponent("dyn_form");
    Map<String, Object> panelAttributes = panel.getPassThroughAttributes();
    panelAttributes.put(FormImporter.INSPECT_OPTION, isInspectMode());
    onRefreshForm(event);
  }

  public void onRefreshForm(FacesEvent event)
  {
    String formSelector = getFormSelector();
    FormFactory formFactory = FormFactory.getInstance();
    formFactory.clearForm(formSelector);

    UIComponent component = event.getComponent();
    UIComponent panel = component.findComponent("dyn_form");

    Map<String, Object> panelAttributes = panel.getPassThroughAttributes();

    // reset renderedFormId
    panelAttributes.remove(FORM_ID);

    updateComponents(panel);
  }

  public void loadDynamicComponents(ComponentSystemEvent event)
  {
    UIComponent panel = ComponentUtils.postAddToView(event);
    if (panel != null)
    {
      if (isPanelRendered(panel))
      {
        Map<String, Object> panelAttributes = panel.getPassThroughAttributes();
        String renderedDataHash = (String)panelAttributes.get(DATA_HASH);
        String dataHash = getDataHash();
        String formSelector = getFormSelector();

        if (formSelector == null ||
            renderedDataHash == null || !renderedDataHash.equals(dataHash))
        {
          updateComponents(panel);
        }
      }
    }
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

  public String getFormSelector()
  {
    return WebUtils.getValue("#{cc.attrs.formSelector}");
  }

  public void setFormSelector(String formSelector)
  {
    WebUtils.setValue("#{cc.attrs.formSelector}", String.class, formSelector);
  }

  public String getDataHash()
  {
    return WebUtils.getValue("#{cc.attrs.dataHash}");
  }

  public String getTypeId()
  {
    return WebUtils.getValue("#{cc.attrs.typeId}");
  }

  public Type getType()
  {
    String typeId = getTypeId();

    Type type = StringUtils.isBlank(typeId) ?
      null : TypeCache.getInstance().getType(typeId);

    return type;
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

  public String getFormDocId()
  {
    String selector = getFormSelector();
    if (!selector.startsWith("doc:"))
    {
      return "N/A";
    }
    else
    {
      return selector.substring(4);
    }
  }

  public String getFormName()
  {
    String selector = getFormSelector();
    if (!selector.startsWith("doc:"))
    {
      return "N/A";
    }
    else
    {
      String docId = selector.substring(4);
      try
      {       
        Document document = 
          DocModuleBean.getPort(true).loadDocument(docId, 0, ContentInfo.ID);
        return document.getTitle();
      }
      catch (Exception ex)
      {
        return "";
      }      
    }    
  }
  
  // --- private methods ---

  private void updateComponents(UIComponent panel)
  {
    try
    {
      Map<String, Object> panelAttributes = panel.getPassThroughAttributes();

      List<SelectItem> selectItems = getSelectItems();
      String formSelector = getFormSelector();

      if (StringUtils.isBlank(formSelector) ||
          !isValidFormSelector(formSelector, selectItems))
      {
        // set first formSelector
        formSelector = (String)selectItems.get(0).getValue();
        setFormSelector(formSelector);
      }

      String renderedFormId = (String)panelAttributes.get(FORM_ID);

      if (StringUtils.isBlank(formSelector))
      {
        panel.getChildren().clear();
        panelAttributes.remove(FORM_ID);
      }
      else if (PROPERTY_EDITOR_SELECTOR.equals(formSelector))
      {
        if (!PROPERTY_EDITOR_ID.equals(renderedFormId))
        {
          panel.getChildren().clear();

          System.out.println(">>>> importing property editor components");
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
          textArea.getPassThroughAttributes().put("spellcheck", "false");
          textArea.setValueExpression("value",
            WebUtils.createValueExpression("#{dynamicPropertiesBean.propertyJson}", String.class));
          group.getChildren().add(textArea);
          panelAttributes.put(FORM_ID, PROPERTY_EDITOR_ID);
        }
      }
      else
      {
        FormFactory formFactory = FormFactory.getInstance();
        Form form = formFactory.getForm(formSelector,
          propertyHelper.getValue(), false);

        if (form == null)
        {
          panel.getChildren().clear();
          panelAttributes.remove(FORM_ID);
        }
        else if (form.getId().equals(renderedFormId))
        {
          System.out.println(">>>> reuse components: " + form);
        }
        else
        {
          panel.getChildren().clear();

          System.out.println(">>>> importing form components: " +
            formSelector + "/" + form);

          ComponentUtils.includeFormComponents(panel, form,
            "dynamicPropertiesBean.propertyHelper.value",
            "dynamicPropertiesBean.propertyHelper.values",
            getOptions());
          panelAttributes.put(FORM_ID, form.getId());
        }
      }
      String dataHash = getDataHash();
      if (dataHash != null)
      {
        panelAttributes.put(DATA_HASH, dataHash);
      }
      else
      {
        panelAttributes.remove(DATA_HASH);
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  private boolean isPanelRendered(UIComponent component)
  {
    UIViewRoot viewRoot = FacesContext.getCurrentInstance().getViewRoot();
    boolean rendered = component.isRendered();
    while (component != viewRoot && rendered)
    {
      component = component.getParent();
      rendered = component.isRendered();
    }
    return rendered;
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
}