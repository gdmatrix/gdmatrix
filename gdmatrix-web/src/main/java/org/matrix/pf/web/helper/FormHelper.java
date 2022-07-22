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
package org.matrix.pf.web.helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;
import org.matrix.dic.Property;
import org.matrix.pf.web.ObjectBacking;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.dic.util.PropertyConverter;
import org.santfeliu.faces.FacesBean;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.form.Form;
import org.santfeliu.form.FormDescriptor;
import org.santfeliu.form.FormFactory;
import org.santfeliu.form.builder.TypeFormBuilder;
import org.santfeliu.form.builder.TypeFormBuilder.FormMode;
import org.santfeliu.util.MapEditor;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author blanquepa
 */
public class FormHelper extends FacesBean implements Serializable
{
  private static final String SEPARATOR = "SEPARATOR";
  private static final String PROPERTY_EDITOR_SELECTOR = "PROPERTY_EDITOR"; 
  private static final String OBJECT_ID_PROPERTY = "_objectId";
  
  protected final DynamicFormPage backing;  
  
  private String selector;
  private final List<SelectItem> formSelectItems = new ArrayList<>();
  private Map<String, String> data = new HashMap<>();  
  
  private final FormMode formMode;
  private final String selectorPrefix;
  
  public FormHelper(DynamicFormPage backing, FormMode formMode)
  {
    this.backing = backing;
    this.formMode = formMode;
    switch (formMode)
    {
      case SEARCH:
        this.selectorPrefix = TypeFormBuilder.SEARCH_PREFIX;
        break;
      case VIEW:
        this.selectorPrefix = TypeFormBuilder.VIEW_PREFIX;
        break;
      default:
        this.selectorPrefix = TypeFormBuilder.PREFIX;
    }
  } 
  
  //getter and setters
  public Map getData()
  {
    return data;
  }

  public void setData(Map data)
  {
    this.data = data;
  }
 
  public String getSelector()
  {
    return selector;
  }  
  
  public void setSelector(String selector)
  {
    this.selector = selector;
  } 
  
  public List<SelectItem> getFormSelectItems()
  {
    return formSelectItems;
  }  

  public FormMode getFormMode()
  {
    return formMode;
  }
    
  public Form getForm()
  {
    try
    {
      if (isNew()) //Load defaults only if is a creation (TODO: Move to populateData?)
      {
        Type type = getSelectedType();
        if (type != null)
          type.loadDefaultValues(data);
      }

      if (!isRenderPropertyEditor())
      {
        FormFactory formFactory = FormFactory.getInstance();
        // update form only in render phase
        boolean updated = getFacesContext().getRenderResponse();
        return formFactory.getForm(selector, data, updated);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }  
  
  public void postSubmit(List<Property> properties)
  {
    refreshFormSelectors(backing.getTypeId());
    populateFormData(properties);
  }
  
  public void preSubmit(List<Property> properties)
  {
    properties.clear();
    properties.addAll(getDataAsProperties());
  }
    
  public String clearForm()
  {
    if (!isRenderPropertyEditor())
    {
      FormFactory factory = FormFactory.getInstance();
      factory.clearForm(selector);
    }
    return null;
  }

  public void onFormChange()
  {
    clearForm();
  }
  
  public void onTypeIdChange(ValueChangeEvent event)
  {
    String oldTypeId = (String) event.getOldValue();
    String newTypeId = (String) event.getNewValue();
    if (isCurrentTypeUndefined() || !oldTypeId.equals(newTypeId))
      refreshFormSelectors(newTypeId);

    if (SEPARATOR.equals(newTypeId))
    {
      // if user selects SEPARATOR then current type id set to null
      backing.setTypeId(null);
    }
  }
  
  public List<Property> getDataAsProperties()
  {
    if (data != null)
    {
      Type type = getSelectedType();
      if (type != null)
      {
        PropertyConverter converter = new PropertyConverter(type);
        List<Property> propertyList = converter.toPropertyList(data);
        Property auxObjectIdProperty =
          DictionaryUtils.getPropertyByName(propertyList, OBJECT_ID_PROPERTY);
        if (auxObjectIdProperty != null)
        {
          propertyList.remove(auxObjectIdProperty);
        }
        return propertyList;
      }
    }
    return null;
  }

  private void populateFormData(List<Property> properties)
  {
    if (properties != null)
    {
      Type type = getSelectedType();
      if (type != null)
      {
        PropertyConverter converter = new PropertyConverter(type);
        data = converter.toPropertyMap(properties);
      }
      else data = new HashMap<>();
    }
    ObjectBacking objectBacking = backing.getObjectBacking();
    if (objectBacking != null)
      data.put(OBJECT_ID_PROPERTY, objectBacking.getObjectId());
  }  
  
  public boolean isRenderPropertyEditor()
  {
    return formMode.equals(FormMode.EDIT) && 
      (selector == null || PROPERTY_EDITOR_SELECTOR.equals(selector));
  }  
  
  public boolean isRenderForm()
  {
    return !isCurrentTypeUndefined() && getSelector() != null;
  }
  
  public boolean isRenderFormSelector()
  {
    return isRenderForm() && getFormSelectItems().size() > 1;
  } 
  
  public void validatePropertyEditorString(FacesContext context,
    UIComponent component, Object value)
  {
    MapEditor editor = new MapEditor(new HashMap<>());
    try
    {
      editor.parse(value == null ? "" : value.toString());
    }
    catch (Exception ex)
    {
      FacesMessage message = 
        FacesUtils.getFacesMessage("EVAL_ERROR",
        new Object[]{ex.getMessage()}, FacesMessage.SEVERITY_ERROR);
      throw new ValidatorException(message);
    }
  }  
  
  public String getPropertyEditorString()
  {
    MapEditor editor = new MapEditor(data);
    return editor.toString();
  }

  public void setPropertyEditorString(String propertyString)
  {
    MapEditor editor = new MapEditor(data);
    data.clear();
    try
    {
      editor.parse(propertyString);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
    
  private void refreshFormSelectors(String typeId)
  {
    //Load list of form selectors.
    loadFormSelectItems(typeId);
    //Default the first selector or null.
    if (!formSelectItems.isEmpty())
      selector = (String)formSelectItems.get(0).getValue();
    else
      selector = null;
  }    

  private void loadFormSelectItems(String typeId)
  {  
    try
    {
      if (!isCurrentTypeUndefined())
      {
        formSelectItems.clear();
        UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
        String selectorBase =
          selectorPrefix + ":" + typeId +
          TypeFormBuilder.USERID + userSessionBean.getUserId() +
          TypeFormBuilder.PASSWORD + userSessionBean.getPassword();
        FormFactory formFactory = FormFactory.getInstance();
        List<FormDescriptor> descriptors = formFactory.findForms(selectorBase);
        for (FormDescriptor descriptor : descriptors)
        {
          SelectItem selectItem = new SelectItem();
          selectItem.setValue(descriptor.getSelector());
          selectItem.setDescription(descriptor.getTitle());
          selectItem.setLabel(descriptor.getTitle());
          formSelectItems.add(selectItem);
        }
        if (isUserAdmin()) // add property editor
        {
          SelectItem selectItem = new SelectItem();
          Locale locale = getFacesContext().getViewRoot().getLocale();
          ResourceBundle bundle = ResourceBundle.getBundle(
              "org.santfeliu.web.obj.resources.ObjectBundle", locale);
          String label = bundle.getString("property_editor");
          selectItem.setLabel(label.toUpperCase());
          selectItem.setValue(PROPERTY_EDITOR_SELECTOR);
          formSelectItems.add(selectItem);
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }  
  
  //TODO: Move to backing bean??
  private boolean isUserAdmin()
  {    
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    return userSessionBean.isUserInRole(backing.getAdminRole());
  }  
  
  private boolean isCurrentTypeUndefined()
  {
    String currentTypeId = backing.getTypeId();
    return isTypeUndefined(currentTypeId);
  } 
  
  private boolean isTypeUndefined(String typeId)
  {
    return typeId == null || typeId.length() == 0;
  }   
  
  private org.santfeliu.dic.Type getSelectedType()
  {
    String typeId = backing.getTypeId();
    TypeCache typeCache = TypeCache.getInstance();
    if (!isTypeUndefined(typeId))
      return typeCache.getType(typeId);
    else return null;
  }
  
  private boolean isNew()
  {
    return backing.getObjectBacking().isNew();
  }  
}
