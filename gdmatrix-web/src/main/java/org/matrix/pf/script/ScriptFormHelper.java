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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import org.matrix.dic.Property;
import org.matrix.dic.PropertyDefinition;
import org.matrix.dic.PropertyType;
import org.matrix.web.WebUtils;
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

public class ScriptFormHelper implements Serializable
{
  @CMSProperty
  public static final String SCRIPT_NAME = "scriptName";
  @CMSProperty
  public static final String PROPERTY_ORDER = "propertyOrder";
  public static final String AUTOFORM_NAME = "auto";
  public static final String AUTOFORM_URL = "/pf/common/script/autoform.xhtml";
  
  private static final String STRING_SEPARATOR = "::";  
  
  private final ScriptFormPage backing;
  
  private List<Field> fields;
  private Map<String, Field> fieldMap;
  
  public ScriptFormHelper(ScriptFormPage backing)
  {
    this.backing = backing;
  }
  
  public List<Field> getFields()
  {//TODO: Merge PropertyDefinitions with properties to allow create new.
    return fields;
  }
  
  public void reload()
  {
    loadFields();
    //TODO: clear ScriptBean scope
  }
  
  public void mergeProperties()
  {
    Map<String, Property> map = new HashMap<>();
    List<Property> properties = backing.getProperties();
    for (Property prop : properties)
    {
      map.put(prop.getName(), prop);
    }
    for (Property prop : toProperties())
    {
      map.put(prop.getName(), prop);
    }
    backing.getProperties().clear();
    backing.getProperties().addAll(map.values());
  }
  
  public List<FormTab> getFormTabs()
  {
    List<FormTab> results = new ArrayList();
 
    List<String> scripts = backing.getMultivaluedProperty(SCRIPT_NAME);
    if (scripts == null || scripts.isEmpty())
      results.add(new FormTab(AUTOFORM_NAME, AUTOFORM_URL));
    else
    {
      for (String script : scripts)
      {
        String[] parts = script.split(STRING_SEPARATOR);
        String name = parts[0];
        String label = (parts.length == 2 ? parts[1] : parts[0]);
        if (name.equals(AUTOFORM_NAME))
          results.add(new FormTab(label, AUTOFORM_URL));   
        else
        {
          ScriptBacking scriptBacking = WebUtils.getBacking("scriptBacking");
          FormTab formTab = 
            new FormTab(label, scriptBacking.getXhtmlFormUrl(name));
          results.add(formTab);
        }
      }
    }
    
    return results;
  }
  
  public void onTypeIdChange(ValueChangeEvent event)
  {
    String oldTypeId = (String) event.getOldValue();
    String newTypeId = (String) event.getNewValue();
    if (isCurrentTypeUndefined() || !oldTypeId.equals(newTypeId))
      reload();
  }
  
  public void onTypeIdChange()
  {
    reload();
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
  
  private void loadFields()
  {
    fieldMap = new HashMap();
    List<Property> properties = backing.getProperties();
    String typeId = backing.getTypeId();
    
    Map<String, PropertyDefinition> pds = new HashMap<>();
    
    if (fields == null) 
      fields = new ArrayList();
    else
      fields.clear();
    
    Type type = TypeCache.getInstance().getType(typeId);
    
    for (Property property : properties)
    {
      PropertyDefinition propDef = 
        type.getPropertyDefinition(property.getName());
      Object value;

      if (propDef != null)
      {
        if (propDef.getMaxOccurs() != 1)
          value = property.getValue();
        else
          value = property.getValue().get(0);
      }
      else
      {
        propDef = new PropertyDefinition();
        propDef.setName(property.getName());
        propDef.setDescription(property.getName());
        propDef.setType(PropertyType.TEXT);
        propDef.setMaxOccurs(1);
        value = property.getValue().get(0);
      }
      pds.put(property.getName(), propDef);      
      Field field = new Field(propDef, value);
      fields.add(field);
      fieldMap.put(property.getName(), field);
    }
    
    for (PropertyDefinition pd : type.getPropertyDefinition())
    {
      if (!pd.isHidden() && !pd.isReadOnly())
      {
        PropertyDefinition mapped = pds.get(pd.getName());
        if (mapped == null)
        {
          Field field = new Field(pd, null);
          fields.add(field);
          fieldMap.put(pd.getName(), field);          
        }
      }
    }
    
    Collections.sort(fields, (Field f1, Field f2) ->
    {
      if (f1 != null && f2 != null)
        return f1.getName().compareTo(f2.getName());
      else if (f1 == null)
        return 1;
      else
        return -1;
    });    
    
    List<String> order = backing.getMultivaluedProperty(PROPERTY_ORDER);
    if (order != null && !order.isEmpty())
    {
      List<Field> sorted = new ArrayList();
      for (String propName : order)
      {
        Field field = fieldMap.get(propName);
        if (field != null)
        {
          sorted.add(field);
          fields.remove(field);
        }
      }
      sorted.addAll(fields);
      fields = sorted;
    } 
    
    
  }
  
  public Map<String, Field> getFieldMap()
  {
    return this.fieldMap;
  }  

  public void setFieldMap(Map<String, Field> fieldMap)
  {
    this.fieldMap = fieldMap;
  }
    
  private List<Property> toProperties()
  {
    List<Property> properties = new ArrayList();
    
    for (Field field : fields)
    {
      Property prop = new Property();
      prop.setName(field.getName());
      if (field.isMultiple())
        prop.getValue().addAll((List<String>) field.getValue());
      else
        prop.getValue().add((String) field.getValue());
      properties.add(prop);
    }
    
    return properties;
  }

  public class Field implements Serializable
  {
    private final PropertyDefinition propDef;
    private Object value;

    public Field(PropertyDefinition propDef, Object value)
    {
      this.propDef = propDef;
      if (value != null)
        this.value = value;
      else
      {
        if (propDef.getValue() != null && !propDef.getValue().isEmpty())
        {
          if (propDef.getMaxOccurs() != 1)
            this.value = propDef.getValue();
          else
            this.value = propDef.getValue().get(0);
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
      String sv = String.valueOf(value);
      return Boolean.parseBoolean(sv);
    }
    
    public void setBooleanValue(boolean b)
    {
      value = String.valueOf(b);
    }
    
    public Date getDateValue()
    {
      return TextUtils.parseInternalDate((String) value);
    }
    
    public void setDateValue(Date date)
    {
      value = TextUtils.formatDate(date, "yyyyMMddHHmmss");
    }
    
    public List getListValue()
    {
      if (value instanceof List)
        return (List) value;
      else
        return Collections.emptyList();
    }
    
    public void setListValue(List list)
    {
      value = list;
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
      return value;
    }

    public void setValue(Object value)
    {
      this.value = value;
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
        
  }
  
  public class FormTab
  {
    private final String name;
    private final String url;

    public FormTab(String name, String url)
    {
      this.name = name;
      this.url = url;
    }

    public String getName()
    {
      return name;
    }

    public String getUrl()
    {
      return url;
    }
  }  
}
