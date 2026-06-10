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
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.FacesEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.Property;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentConstants;
import org.primefaces.PrimeFaces;
import org.primefaces.component.inputtextarea.InputTextarea;
import org.primefaces.component.outputlabel.OutputLabel;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.PropertyConverter;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.form.Form;
import org.santfeliu.form.FormDescriptor;
import org.santfeliu.form.FormFactory;
import org.santfeliu.form.builder.TypeFormBuilder;
import org.santfeliu.web.ApplicationBean;
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

  static final Map<String, Object> FILTER_OPTIONS = new HashMap();
  static
  {
    FILTER_OPTIONS.put(FormImporter.STACKED_OPTION, "true");
    FILTER_OPTIONS.put(FormImporter.SEARCH_FORM_OPTION, "true");
  }
  static final JsonValidator JSON_VALIDATOR = new JsonValidator();

  private final Map<String, List<FormDescriptor>> formDescriptorMap = new HashMap<>();
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
    Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

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

  public List<FormDescriptor> getFormDescriptors()
  {
    String prefix = getFormBuilderPrefix();
    String typeId = getTypeId();
    String formKey = prefix + ":" + typeId;
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance(); 
    ResourceBundle bundle = ResourceBundle.getBundle(
      "org.santfeliu.web.obj.resources.ObjectBundle",
      userSessionBean.getViewLocale());    
    
    List<FormDescriptor> descriptors = formDescriptorMap.get(formKey);

    if (descriptors == null)
    {
      if (StringUtils.isBlank(typeId))
      {
        descriptors = new ArrayList<>();
      }
      else
      {
        String selectorBase = formKey +
          TypeFormBuilder.USERID + userSessionBean.getUserId() +
          TypeFormBuilder.PASSWORD + userSessionBean.getPassword();

        descriptors = FormFactory.getInstance().findForms(selectorBase);
      }
      
      if (userSessionBean.isUserInRole(DocumentConstants.DOC_ADMIN_ROLE))
      {
        descriptors.add(new FormDescriptor(PROPERTY_EDITOR_SELECTOR,
          bundle.getString("property_editor")));
      }      
    }

    List<String> formSelectorsFilter = getFormSelectorsFilter(); 
    filterFormDescriptors(descriptors, formSelectorsFilter); 

    if (descriptors.isEmpty())
    {
      descriptors.add(
        new FormDescriptor("", bundle.getString("type_without_form")));
    }
    
    formDescriptorMap.put(formKey, descriptors); 
    
    //Refresh entry if has dynamic selectors    
    if (formSelectorsFilter != null && !formSelectorsFilter.isEmpty()) 
      formDescriptorMap.remove(formKey);
    
    return descriptors;
  }
  
  private List<String> getFormSelectorsFilter()
  {
    return WebUtils.getValue("#{cc.attrs.formSelectorsFilter}");
  }  
  
  private List<FormDescriptor> filterFormDescriptors(
    List<FormDescriptor> descriptors, List<String> formSelectorsFilter)
  {
    if (!descriptors.isEmpty() && formSelectorsFilter != null && 
      !formSelectorsFilter.isEmpty())
    {
      descriptors.removeIf(d -> 
        !formSelectorsFilter.contains(d.getSelector()) 
          && !d.getSelector().equals(PROPERTY_EDITOR_SELECTOR));

      //Sort as descriptorsFilter list
      Map<String, Integer> sortMap = new HashMap<>();
      for (int i = 0; i < formSelectorsFilter.size(); i++) 
      {
        sortMap.put(formSelectorsFilter.get(i), i);
      }     

      Comparator<FormDescriptor> comparator = 
        (FormDescriptor d1, FormDescriptor d2) -> 
      {
        Integer index1 = sortMap.get(d1.getSelector());
        Integer index2 = sortMap.get(d2.getSelector());
        int val1 = (index1 != null) ? index1 : Integer.MAX_VALUE;
        int val2 = (index2 != null) ? index2 : Integer.MAX_VALUE;
        return Integer.compare(val1, val2);
      };

      Collections.sort(descriptors, comparator); 
    }
    
    return descriptors;    
  }

  public void onSelectForm(FacesEvent event)
  {
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
  }

  public void loadDynamicComponents(ComponentSystemEvent event)
  {
    UIComponent panel = ComponentUtils.postAddToView(event);
    if (panel != null)
    {
      if (isPanelRendered(panel))
      {
        updateComponents(panel);
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

      List<FormDescriptor> descriptors = getFormDescriptors();
      String formSelector = getFormSelector();

      if (StringUtils.isBlank(formSelector) ||
          !isValidFormSelector(formSelector, descriptors))
      {
        // set first formSelector
        formSelector = descriptors.get(0).getSelector();
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
        boolean updateForm = "true".equals(FacesContext.getCurrentInstance()
          .getExternalContext().getRequestMap().get("updateForm"));

        FormFactory formFactory = FormFactory.getInstance();
        Form form = formFactory.getForm(formSelector,
          propertyHelper.getValue(), updateForm);

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
          PrimeFaces.current().ajax().update(panel.getClientId());
        }
      }
    }
    catch (Exception ex)
    {
      renderNoFormMessage(panel, ex);
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
    List<FormDescriptor> descriptors)
  {
    if (PROPERTY_EDITOR_SELECTOR.equals(formSelector)) return true;

    for (FormDescriptor descriptor : descriptors)
    {
      if (formSelector.equals(descriptor.getSelector())) return true;
    }
    return false;
  }

  private void renderNoFormMessage(UIComponent panel, Exception ex)
  {
    panel.getChildren().clear();
    Application application =
      FacesContext.getCurrentInstance().getApplication();
    HtmlPanelGroup group = (HtmlPanelGroup)application.
      createComponent(HtmlPanelGroup.COMPONENT_TYPE);
    group.setStyleClass("field col-12");
    group.setLayout("block");
    panel.getChildren().add(group);
    
    HtmlOutputText noInputFormText = (HtmlOutputText)application.
      createComponent(HtmlOutputText.COMPONENT_TYPE);
    String text = ApplicationBean.getCurrentInstance().translate(
      "$$objectBundle.errorInForm");
    noInputFormText.setValue(text);
    
    HtmlOutputText stackTraceText = (HtmlOutputText)application.
      createComponent(HtmlOutputText.COMPONENT_TYPE);
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    boolean isAdminUser = 
      userSessionBean.isUserInRole(DocumentConstants.DOC_ADMIN_ROLE);
    stackTraceText.setRendered(isAdminUser);
    stackTraceText.setStyleClass("code");
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    ex.printStackTrace(pw);
    stackTraceText.setValue(": " + sw.toString());

    FacesUtils.addMessage(ex);
    group.getChildren().add(noInputFormText);
    group.getChildren().add(stackTraceText);
  }

}