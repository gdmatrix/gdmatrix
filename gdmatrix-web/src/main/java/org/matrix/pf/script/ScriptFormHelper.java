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
package org.matrix.pf.script;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.Property;
import org.matrix.dic.PropertyDefinition;
import org.matrix.dic.PropertyType;
import org.matrix.pf.web.PageBacking;
import org.primefaces.event.TabChangeEvent;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.util.Table;
import org.santfeliu.util.TextUtils;
import org.santfeliu.util.data.DataProvider;
import org.santfeliu.util.data.DataProviderFactory;
import org.santfeliu.web.bean.CMSProperty;

/**
 *
 * @author blanquepa
 */
public class ScriptFormHelper extends ScriptHelper<ScriptFormPage> 
  implements Serializable
{
  @CMSProperty
  public static final String SCRIPT_NAME = "scriptName";
  @CMSProperty
  public static final String PROPERTY_ORDER = "propertyOrder";
  
  public static final String AUTOFORM_NAME = "auto";
  public static final String AUTOFORM_LABEL = "Automatic";  
  public static final String AUTOFORM_URL = "/pf/common/script/autoform.xhtml";
  
  private static final String SEARCH_VALUE_BINDING = "_searchValueBinding";
  
  private static final String STRING_SEPARATOR = "::";  
    
  private Integer formTabIndex = 0;
  private List<FormTab> formTabs;
    
  private FormFields formFields;

  
  public ScriptFormHelper(ScriptFormPage backing)
  {
    super(backing);
  }

  @Override
  protected ScriptFormPage getBacking()
  {
    return backing;
  }
  
  @Override
  protected void setBacking(ScriptFormPage backing)  
  {
    this.backing = backing;
  }

  public Integer getActiveIndex()
  {
    return formTabIndex;
  }

  public void setActiveIndex(Integer activeIndex)
  {
    this.formTabIndex = activeIndex;
  }
  
  public List<FormTab> getFormTabs()
  {
    return this.formTabs;
  }
  
  public FormFields getFormFields()
  {
    return formFields;
  }

  public void setFormFields(FormFields formFields)
  {
    this.formFields = formFields;
  }  
  
  //Autoform public methods
  public List<Field> getFields()
  {
    List<String> order = backing.getMultivaluedProperty(PROPERTY_ORDER);
    if (order != null && !order.isEmpty())        
      return formFields.getFieldList(order);
    else
      return formFields.getFieldList();
  }
    
  public void refreshForms()
  {
    loadFormTabs(); 
    clearData();    
    loadFields();
  }
    
  public void onTypeIdChange(ValueChangeEvent event)
  {
    String oldTypeId = (String) event.getOldValue();
    String newTypeId = (String) event.getNewValue();
    if (isCurrentTypeUndefined() || !oldTypeId.equals(newTypeId))
      refreshForms();
  }
  
  public void onTypeIdChange()
  {
    refreshForms();
  }
    
  @Override
  public void show() throws Exception
  {
    loadFormTabs();
    clearData();        
    loadFields();
    call("load");
  }
  
  public String callStore() throws Exception
  {
    mergeProperties();
    call("preStore");
    String outcome = getBacking().save();
    call("postStore");
    return outcome; 
  }
  
  
  public void onFormTabChange(TabChangeEvent event)
  {
    try
    {
      call("load");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }    
      
  public String search(String targetObjectTypeId, String valueBinding, 
    String scriptMethod)
  {
    outdata.put(SEARCH_VALUE_BINDING, scriptMethod);
    return ((PageBacking)backing).search(targetObjectTypeId, valueBinding);
  }
  
  public void setValueBinding(String value)
  {
    String scriptMethod = (String) outdata.get(SEARCH_VALUE_BINDING);
    if (scriptMethod != null)
    {
      try
      {
        call(scriptMethod, value);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
  }
  
  public void mergeProperties()
  {
    Map<String, Property> map = new HashMap<>();
    
    //Add backing properties
    List<Property> properties = getBacking().getProperties();
    for (Property prop : properties)
    {
      map.put(prop.getName(), prop);
    }

    //Add persistent data map
    for (Object key : data.keySet())
    {
      String name = (String)key;      
      Object value = data.get(key);
      if (value != null)
      {

        Property prop = new Property();
        prop.setName(name);
        if (value instanceof Collection)
        {
          List<String> lvalue = (List<String>) value;
          if (!lvalue.isEmpty())
            prop.getValue().addAll(lvalue);
        }
        else
        {
          String svalue = (String) value;
          if (!StringUtils.isBlank(svalue))
            prop.getValue().add(svalue);
        }
        map.put(name, prop);
      }
      else
        map.remove(name);
    }

    getBacking().getProperties().clear();
    getBacking().getProperties().addAll(map.values());
  }
  
  @Override
  protected String getScriptPageName()
  {
     FormTab formTab = getCurrentFormTab();
 
     if (formTab != null && !AUTOFORM_NAME.equals(formTab.getName()))
      return formTab.getName();
     else
      return super.getScriptPageName();
  }
  
  private void clearData()
  {
    boolean allowClear = true;

    //Not refresh while search and return
    String scriptMethod = (String) outdata.get(SEARCH_VALUE_BINDING);
    if (scriptMethod != null)
    {
      outdata.remove(SEARCH_VALUE_BINDING);
      allowClear = false;
    }

    if (allowClear)
    {
      data.clear();
      outdata.clear();
    }
  }
    
  private void loadFormTabs()
  {
    formTabs = new ArrayList<>();
 
    List<String> scripts = getBacking().getMultivaluedProperty(SCRIPT_NAME);
    if (scripts == null || scripts.isEmpty())
      formTabs.add(new FormTab(AUTOFORM_NAME, AUTOFORM_LABEL, AUTOFORM_URL));
    else
    {
      for (String script : scripts)
      {
        String[] parts = script.split(STRING_SEPARATOR);
        String name = parts[0];
        String label = (parts.length == 2 ? parts[1] : parts[0]);
        if (name.equals(AUTOFORM_NAME))
          formTabs.add(new FormTab(AUTOFORM_NAME, label, AUTOFORM_URL));   
        else
        {
          FormTab formTab = 
            new FormTab(name, label, getXhtmlFormUrl(name));
          formTabs.add(formTab);
        }
      }
    }
  }  
  
  private boolean isCurrentTypeUndefined()
  {
    String currentTypeId = getBacking().getTypeId();
    return isTypeUndefined(currentTypeId);
  } 

  private boolean isTypeUndefined(String typeId)
  {
    return typeId == null || typeId.length() == 0;
  }   
  
  private void loadFields()
  {
    formFields = new FormFields(); 
    
    //Get dynamic properties from backing bean
    List<Property> properties = getBacking().getProperties();

    String typeId = getBacking().getTypeId();
    Type type = TypeCache.getInstance().getType(typeId);
    Map<String, PropertyDefinition> pds = new HashMap<>();    
    
    //For every property create its Field and add in FormFields
    if (properties != null)
    {
      for (Property property : properties)
      {
        PropertyDefinition propDef = 
          type.getPropertyDefinition(property.getName());
        Field field; 
        Object value = null;

        if (propDef != null)
        {
          if (propDef.getMaxOccurs() != 1)
            value = property.getValue();
          else if (!property.getValue().isEmpty())
            value = property.getValue().get(0);
          field = new Field(propDef, value);          
        }
        else
        {
          value = property.getValue().get(0);
          field = new Field(property.getName(), value);
        }
        pds.put(property.getName(), propDef);      
        
        formFields.add(property.getName(), field);
      }
    }
    
    //Add data map variables
    if (formTabs != null && !formTabs.isEmpty() && formTabIndex != null)
    {
      for (Object key : data.keySet())
      {
        Field field = formFields.get(key);
        if (field == null)
        {
          String name = (String) key;
          field = new Field(name, data.get(key));
        }
        else
          field.setValue(data.get(key));     
        formFields.add(key, field);           
      }
    }    
    
    //Add all PropertyDefinition not present as properties to List and Map.
    for (PropertyDefinition pd : type.getPropertyDefinition())
    {
      if (!pd.isReadOnly() && !backing.getTypedHelper().isPropertyHidden(pd))
      { 
        PropertyDefinition mapped = pds.get(pd.getName());
        if (mapped == null)
        {
          Field field = new Field(pd, null);
          formFields.add(pd.getName(), field);          
        }
      }
    }
  }  
  
  private FormTab getCurrentFormTab()
  {
    FormTab formTab = null;
    if (formTabs != null && !formTabs.isEmpty())
      formTab =  formTabs.get(formTabIndex);
    return formTab;
  }
  
  public class FormTab implements Serializable
  {
    private final String name;
    private final String label;
    private final String url;
    
    public FormTab(String name, String label, String url)
    {
      this.name = name;
      this.label = label;
      this.url = url;
    }

    public String getName()
    {
      return name;
    }

    public String getLabel()
    {
      return label;
    }

    public String getUrl()
    {
      return url;
    }
  }    
    
  public class FormFields implements Serializable
  {
    private List<Field> list;  
    private final Map<String, Field> map;

    public FormFields()
    {
      list = new ArrayList<>();
      map = new HashMap<>();
    }

    public Field get(Object key)
    {
      if (key == null)
        return null;
  
      Field field = map.get((String) key);
      if (field != null)
        return field;  
      else   
        field = (Field) add(key, null);
  
      return field;
    }
  
    public Object add(Object key, Field field)
    {
      String skey = (String) key;    
      if (field == null)
      {
        PropertyDefinition propDef = new PropertyDefinition();
        propDef.setName(skey);
        propDef.setDescription(skey);
        propDef.setReadOnly(false);
        propDef.setHidden(false);
        propDef.setType(PropertyType.TEXT);
        propDef.setMaxOccurs(1);
        field = new Field(propDef, null);      
      }

      if (map.containsKey(skey))
        list.remove(map.get(skey));

      map.put(skey, field); //Map allows TRANSIENT variables 
      if (!skey.startsWith("_"))
        list.add((Field) field);


      return field;
    }

    public List<Field> getFieldList()
    {
      //Initial sort by name
      Collections.sort(list, (Field f1, Field f2) ->
      {
        if (f1 != null && f2 != null)
          return f1.getName().compareTo(f2.getName());
        else if (f1 == null)
          return 1;
        else
          return -1;
      }); 

      return list;
    }

    public List<Field> getFieldList(List<String> order)
    {    
      //Sort by PROPERTY_ORDER 
      if (order != null && !order.isEmpty())
      {
        List<Field> sorted = new ArrayList();
        for (String propName : order)
        {
          Field field = map.get(propName);
          if (field != null)
          {
            sorted.add(field);
            list.remove(field);
          }
        }
        sorted.addAll(list);
        list = sorted;
      }     

      return list;
    }

    public List<Property> getPropertyList()
    {
      List<Property> properties = new ArrayList();

      for (Field field : list)
      {
        if (!field.getName().startsWith("_"))
        {
          Property prop = new Property();
          prop.setName(field.getName());
          if (field.isMultiple())
            prop.getValue().addAll((List<String>) field.getValue());
          else
            prop.getValue().add((String) field.getStringValue());
          properties.add(prop);
        }
      }

      return properties;
    }  
  }  
  
  public class Field implements Serializable
  {
    private final PropertyDefinition propDef;
    
    public Field(String name, Object value)
    {
      propDef = new PropertyDefinition();
      propDef.setName(name);
      propDef.setDescription(name);
      propDef.setType(PropertyType.TEXT);
      propDef.setMaxOccurs(1);        

      if (value != null)
        setDataValue(value);
      else
      {
        if (propDef.getValue() != null && !propDef.getValue().isEmpty())
        {
          if (propDef.getMaxOccurs() != 1)
            setDataValue(propDef.getValue());
          else
            setDataValue(propDef.getName());
        }
      }      
    }

    public Field(PropertyDefinition propDef, Object value)
    {
      this.propDef = propDef;
      if (value != null)
        setDataValue(value);
      else
      {
        if (propDef.getValue() != null && !propDef.getValue().isEmpty())
        {
          if (propDef.getMaxOccurs() != 1)
            setDataValue(propDef.getValue());
          else
            setDataValue(propDef.getName());
        }
      }
    }
    
    public String getName()
    {
      return propDef.getName();
    }

    public String getLabel()
    {
      return propDef.getDescription();
    }

    public boolean getBooleanValue()
    {
      String sv = String.valueOf(getDataValue());
      return Boolean.parseBoolean(sv);
    }

    public void setBooleanValue(boolean b)
    {
      setDataValue(String.valueOf(b));
    }

    public Date getDateValue()
    {
      return TextUtils.parseInternalDate((String) getDataValue());
    }

    public void setDateValue(Date date)
    {
      setDataValue(TextUtils.formatDate(date, "yyyyMMddHHmmss"));
    }

    public List getListValue()
    {
      Object value = getDataValue();
      if (value instanceof List)
        return (List) value;
      else
        return Collections.emptyList();
    }

    public void setListValue(List list)
    {
      if (list != null)
        setDataValue(list);
      else
        setDataValue(Collections.EMPTY_LIST);
    }

    public List<SelectItem> getEnumTypes() throws Exception
    {
      List<SelectItem> result = new ArrayList();

      DataProviderFactory factory = DataProviderFactory.getInstance();
      String ref = "enumtype:" + propDef.getEnumTypeId();
      DataProvider provider;

      provider = factory.createProvider(ref);
      HashMap context = new HashMap();
      Table data = provider.getData(context);
      if (data != null && !data.isEmpty())
      {
        for (int i = 0; i < data.getRowCount(); i++)
        {
          Object itemValue = data.getElementAt(i, 0);
          String itemLabel = (String) data.getElementAt(i, 1);
          SelectItem item = new SelectItem(itemValue, itemLabel);
          result.add(item);
        }
      }

      return result;
    }  

    public Object getValue()
    {
      return getDataValue();
    }

    public void setValue(Object value)
    {
      if (value != null)
        setDataValue(value);
      else
        setDataValue(null);
    }

    public String getStringValue()
    {
      Object value = getDataValue();
      if (value != null)
      {
        if (value instanceof SelectItem)
          return (String) ((SelectItem) value).getValue();
        else 
          return String.valueOf(value);
      }
      else
        return null;
    }

    public String getType()
    {
      return propDef.getType().toString();
    }

    public boolean isMultiple()
    {
      return propDef.getMaxOccurs() != 1;
    }

    public int getMaxOccurs()
    {
      return propDef.getMaxOccurs();
    }

    public boolean isEnumType()
    {
      return propDef.getEnumTypeId() != null;
    }

    public int getSize()
    {
      return propDef.getSize();
    }

    public boolean isReadOnly()
    {
      return propDef.isReadOnly();
    }
    
    private Object getDataValue()
    {
      return data.get(propDef.getName());
    }
    
    private void setDataValue(Object value)
    {
      data.put(propDef.getName(), value);
    }
  }    
}
