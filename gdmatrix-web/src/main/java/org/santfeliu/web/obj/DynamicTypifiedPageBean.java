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
package org.santfeliu.web.obj;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;
import org.matrix.dic.Property;
import org.matrix.dic.PropertyDefinition;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.dic.util.ObjectDumper;
import org.santfeliu.dic.util.PropertyConverter;
import org.santfeliu.dic.web.TypeBean;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.form.Form;
import org.santfeliu.form.FormDescriptor;
import org.santfeliu.form.FormFactory;
import org.santfeliu.form.builder.TypeFormBuilder;
import org.santfeliu.util.MapEditor;
import org.santfeliu.web.UserSessionBean;

public abstract class DynamicTypifiedPageBean extends TypifiedPageBean
{
  static final String PROPERTY_EDITOR_SELECTOR = "PROPERTY_EDITOR";
  private static final String OBJECT_ID_PROPERTY = "_objectId";
  protected String currentTypeId;
  protected List<SelectItem> formSelectItems;
  protected String selector;
  protected boolean resetSelector;
  protected Map data = new HashMap();
  boolean propertyEditorVisible; // selects form view or property editor view

  // row id -> Property list
  private Map<String, List<ObjectDumper.Property>> viewPropertiesMap;
  private Set<ObjectDumper.Property> nonVisibleProperties = new HashSet();
  protected ObjectDumper defaultViewDumper;

  public DynamicTypifiedPageBean(String rootTypeId, String adminRole)
  {
    this(rootTypeId, adminRole, true);
  }

  public DynamicTypifiedPageBean(String rootTypeId, String adminRole,
    boolean forceLoad)
  {
    super(rootTypeId, adminRole);
    if (forceLoad) load();
  }

  //abstract methods
  protected abstract void load();

  //Getters & Setters

  public String getCurrentTypeId()
  {
    return currentTypeId;
  }

  public Map<String, List<ObjectDumper.Property>> getViewPropertiesMap()
  {
    if (viewPropertiesMap == null)
    {
      viewPropertiesMap = new HashMap();
    }
    return viewPropertiesMap;
  }

  public void setCurrentTypeId(String currentTypeId)
  {
    if (isTypeUndefined() || !this.currentTypeId.equals(currentTypeId))
    {
      // force call to findForms and reset selector
      formSelectItems = null;
      resetSelector = true;
    }
    if ("SEPARATOR".equals(currentTypeId))
    {
      // if user selects SEPARATOR then currentTypeId is null
      this.currentTypeId = null;
    }
    else
    {
      this.currentTypeId = currentTypeId;
    }
  }

  public String getSelector()
  {
    if (getFacesContext().getRenderResponse() && resetSelector)
    {
      // selector reset don't takes place until render phase
      getFormSelectItems();
      // take first value of formSelectItems
      if (!formSelectItems.isEmpty())
      {
        selector = (String)formSelectItems.get(0).getValue();
      }
      else
      {
        selector = null;
      }
      resetSelector = false;
    }
    return selector;
  }

  public void setSelector(String selector)
  {
    this.selector = selector;
  }

  public boolean isTypeUndefined()
  {
    return currentTypeId == null || currentTypeId.length() == 0;
  }

  public boolean isRenderForm()
  {
    return !isTypeUndefined() && getSelector() != null;
  }

  public boolean isRenderFormSelector()
  {
    return isRenderForm() && getFormSelectItems().size() > 1;
  }

  public boolean isPropertyEditorVisible()
  {
    if (getFacesContext().getRenderResponse())
    {
      propertyEditorVisible = PROPERTY_EDITOR_SELECTOR.equals(getSelector());
    }
    return propertyEditorVisible;
  }

  public void validatePropertyEditorString(FacesContext context,
    UIComponent component, Object value)
  {
    MapEditor editor = new MapEditor(new HashMap());
    try
    {
      editor.parse(value == null ? "" : value.toString());
    }
    catch (Exception ex)
    {
      FacesMessage message = FacesUtils.getFacesMessage("EVAL_ERROR",
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

  public String updateForm()
  {
    if (selector != null && !PROPERTY_EDITOR_SELECTOR.equals(selector))
    {
      FormFactory factory = FormFactory.getInstance();
      factory.clearForm(selector);
    }
    return show();
  }

  public List<SelectItem> getFormSelectItems()
  {
    if (formSelectItems == null)
    {
      findForms();
    }
    return formSelectItems;
  }

  public Map getData()
  {
    return data;
  }

  public void setData(Map data)
  {
    this.data = data;
  }

  public Form getForm()
  {
    try
    {
      if (isNew()) //Load defaults only if is a creation
        getSelectedType().loadDefaultValues(getData());

      if (selector != null && !PROPERTY_EDITOR_SELECTOR.equals(selector))
      {
        FormFactory formFactory = FormFactory.getInstance();
        return formFactory.getForm(selector, getData());
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public List<SelectItem> getSelectedTypeItems()
  {
    TypeBean typeBean = (TypeBean)getBean("typeBean");
    return typeBean.getSelectItems(rootTypeId, currentTypeId, false);
  }

  public org.santfeliu.dic.Type getCurrentType()
  {
    return getSelectedType();
  }

  @Override
  public List<SelectItem> getAllTypeItems(String ... actions)
  {
    List<SelectItem> items = super.getAllTypeItems(actions);
    Type currentType = getCurrentType();
    if (currentType != null && items != null && !items.isEmpty())
    {
      String typeId = currentType.getTypeId();
      if (typeId != null)
      {
        SelectItem newItem = new SelectItem();
        newItem.setValue(typeId);
        newItem.setLabel(currentType.getDescription());
        for (SelectItem item : items)
        {
          String value = (String)item.getValue();
          if (value != null && value.equals(typeId))
            newItem = null;
        }
        if (newItem != null) items.add(newItem);
      }
    }

    return items;
  }

  public boolean isRenderSelectedTypeItems()
  {
    return getSelectedTypeItems().size() > 1;
  }

  public boolean isPropertyHidden(String propName)
  {
    return isPropertyHidden(getCurrentTypeId(), propName);
  }

  // Internal methods
  private boolean isPropertyHidden(String typeId, String propName)
  {
    Type type = TypeCache.getInstance().getType(typeId);
    if (type != null)
    {
      List<PropertyDefinition> pds = type.getPropertyDefinition();
      for (PropertyDefinition pd : pds)
      {
        if (pd.getName().equals(propName))
        {
          return pd.isHidden();
        }
      }
      String superTypeId = type.getSuperTypeId();
      if (superTypeId != null)
        return isPropertyHidden(superTypeId, propName);
    }
    return true;
  }

  @Override
  protected org.santfeliu.dic.Type getSelectedType()
  {
    TypeCache typeCache = TypeCache.getInstance();
    if (!isTypeUndefined())
      return typeCache.getType(currentTypeId);
    else return null;
  }

  // data<->property conversion methods

  protected List<Property> getFormDataAsProperties()
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

  protected void setFormDataFromProperties(List<Property> properties)
  {
    if (properties != null)
    {
      Type type = getSelectedType();
      if (type != null)
      {
        PropertyConverter converter = new PropertyConverter(type);
        data = converter.toPropertyMap(properties);
      }
      else data = new HashMap();
    }
    if (getObjectBean() != null)
      data.put(OBJECT_ID_PROPERTY, getObjectBean().getObjectId());
  }

  private void findForms()
  {
    try
    {
      formSelectItems = new ArrayList();
      if (!isTypeUndefined())
      {
        UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
        String selectorBase =
          TypeFormBuilder.PREFIX + ":" + currentTypeId +
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
//          String label = (String)getValue("#{objectBundle.property_editor}");
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

  @Override
  public GroupExtractor getGroupExtractor()
  {
    String propertyPrefix = "script:";
    if (groupBy != null && groupBy.startsWith(propertyPrefix))
      return new ScriptPropertyGroupExtractor(
        groupBy.substring(propertyPrefix.length()));
    else
      return super.getGroupExtractor();
  }

  @Override
  public Comparator getPropertyComparator()
  {
    return new ScriptPropertyComparator();
  }

  public List<ObjectDumper.Property> getViewProperties(String rowId)
  {
    return getViewPropertiesMap().get(rowId);
  }

  public List<ObjectDumper.Property> getViewProperties()
  {
    Object row = (Object)getValue("#{row}");
    String rowId = getRowId(row);
    return getViewProperties(rowId);
  }

  protected String getRowId(Object row)
  {
    return null;
  }
/*
  protected String getRowTypeId(Object row)
  {
    return null;
  }
*/
  protected String getShowPropertiesPropertyName(Object row)
  {
    return null;
  }

  protected void loadViewPropertiesMap(List rows)
  {
    getViewPropertiesMap().clear();
    for (Object objRow : rows)
    {
      String rowId = getRowId(objRow);
      String typeId = getRowTypeId(objRow);
      getViewPropertiesMap().put(rowId, new ArrayList());
      Type type = TypeCache.getInstance().getType(typeId);
      if (type != null)
      {
        PropertyDefinition pd =
          type.getPropertyDefinition(getShowPropertiesPropertyName(objRow));
        ObjectDumper rowViewDumper = null;
        if (pd != null && pd.getValue() != null && pd.getValue().size() > 0)
        {
          rowViewDumper = new ObjectDumper(pd.getValue().get(0));
        }
        ObjectDumper viewDumper =
          (rowViewDumper != null ? rowViewDumper : defaultViewDumper);
        if (viewDumper != null)
        {
          getViewPropertiesMap().put(rowId, viewDumper.dump(objRow, type));
        }
      }
    }
  }

  public void clearViewProperties()
  {
    getViewPropertiesMap().clear();
  }

  public void addViewProperty(String rowId, String name, String description,
    String value)
  {
    addViewProperty(rowId, name, description, value, true);
  }

  public void addViewProperty(String rowId, String name, String description,
    String value, boolean visible)
  {
    if (!getViewPropertiesMap().containsKey(rowId))
    {
      getViewPropertiesMap().put(rowId, new ArrayList<ObjectDumper.Property>());
    }
    ObjectDumper.Property property = defaultViewDumper.new Property();
    property.setName(name);
    property.setDescription(description);
    property.setValue(value);
    getViewPropertiesMap().get(rowId).add(property);
    if (!visible) nonVisibleProperties.add(property);
  }

  public boolean removeViewProperty(String rowId, String name)
  {
    if (getViewPropertiesMap().containsKey(rowId))
    {
      List<ObjectDumper.Property> propertyList =
        getViewPropertiesMap().get(rowId);
      if (propertyList != null)
      {
        for (ObjectDumper.Property property : propertyList)
        {
          if (property != null && property.getName().equals(name))
          {
            propertyList.remove(property);
            return true;
          }
        }
      }
    }
    return false;
  }

  public boolean isPropertyVisible()
  {
    ObjectDumper.Property property =
      (ObjectDumper.Property)getRequestMap().get("property");
    return !nonVisibleProperties.contains(property);
  }

  public String getViewPropertyValue(String rowId, String name)
  {
    if (getViewPropertiesMap().containsKey(rowId))
    {
      List<ObjectDumper.Property> propertyList =
        getViewPropertiesMap().get(rowId);
      if (propertyList != null)
      {
        for (ObjectDumper.Property property : propertyList)
        {
          if (property != null && property.getName().equals(name))
          {
            return property.getValue();
          }
        }
      }
    }
    return null;
  }

  /**
 *
 * @author unknown
 */
public class ScriptPropertyComparator extends PropertyComparator
  {
    @Override
    protected Object getPropertyValue(Object obj, String propertyName)
    {
      if (propertyName.startsWith("script:"))
      {
        propertyName = propertyName.substring("script:".length());
        return getViewPropertyValue(getRowId(obj), propertyName);
      }
      else
      {
        return super.getPropertyValue(obj, propertyName);
      }
    }
  }

public class ScriptPropertyGroupExtractor extends DefaultGroupExtractor
{
  public ScriptPropertyGroupExtractor(String scriptPropertyName)
  {
    super(scriptPropertyName);
  }

  @Override
  protected String getName(Object view)
  {
    String rowId = getRowId(view);
    if (rowId != null)
    {
      return getViewPropertyValue(rowId, super.propertyName);
    }
    return null;
  }
}

}
