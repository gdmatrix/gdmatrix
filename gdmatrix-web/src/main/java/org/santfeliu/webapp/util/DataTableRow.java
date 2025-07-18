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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.EnumTypeItem;
import org.matrix.dic.Property;
import org.matrix.dic.PropertyDefinition;
import org.matrix.dic.PropertyType;
import org.santfeliu.dic.EnumTypeCache;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.util.TextUtils;
import org.santfeliu.util.script.ScriptClient;
import org.santfeliu.web.ApplicationBean;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.webapp.BaseBean;
import org.santfeliu.webapp.FinderBean;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.helpers.TablePropertyHelper;
import org.santfeliu.webapp.setup.TableProperty;

/**
 *
 * @author blanquepa
 */
public class DataTableRow implements Serializable
{
  protected String rowId;
  protected String typeId;
  protected Value[] values;
  protected String styleClass;
  //name -> CustomProperty
  protected Map<String, CustomProperty> customPropertyMap = new HashMap<>();

  public DataTableRow(String rowId, String typeId)
  {
    this.rowId = rowId;
    this.typeId = typeId;
  }

  public String getRowId()
  {
    return rowId;
  }

  public void setRowId(String rowId)
  {
    this.rowId = rowId;
  }

  public String getTypeId()
  {
    return typeId;
  }

  public void setTypeId(String typeId)
  {
    this.typeId = typeId;
  }

  public Value[] getValues()
  {
    return values;
  }

  public void setValues(Value[] values)
  {
    this.values = values;
  }

  public String getStyleClass()
  {
    return styleClass;
  }

  public void setStyleClass(String styleClass)
  {
    this.styleClass = styleClass;
  }

  public List<CustomProperty> getCustomProperties()
  {
    List<CustomProperty> auxList = new ArrayList(customPropertyMap.values());
    Collections.sort(auxList, new Comparator<CustomProperty>()
    {
      @Override
      public int compare(CustomProperty cp1, CustomProperty cp2)
      {
        return cp1.getIndex() - cp2.getIndex();
      }
    });
    return auxList;
  }

  public void setValues(BaseBean baseBean, Object row,
    List<TableProperty> tableProperties)
    throws Exception
  {
    TypeCache typeCache = TypeCache.getInstance();
    Type rowType = typeCache.getType(typeId);
    
    //Column properties
    List<TableProperty> columnTableProperties = 
      TablePropertyHelper.getColumnTableProperties(tableProperties);
    values = new Value[columnTableProperties.size()];
    for (int i = 0; i < columnTableProperties.size(); i++)
    {
      TableProperty columnProperty = columnTableProperties.get(i);
      if (columnProperty.getTypeId() == null ||
        rowType.isDerivedFrom(columnProperty.getTypeId()))
      {
        values[i] = getTablePropertyValue(baseBean, columnProperty, row);
      }
    }

    //Row properties
    List<TableProperty> rowTableProperties = 
      TablePropertyHelper.getRowTableProperties(tableProperties);
    int index = 1;
    for (int i = 0; i < rowTableProperties.size(); i++)
    {
      TableProperty rowProperty = rowTableProperties.get(i);
      if (rowProperty.getTypeId() == null ||
        rowType.isDerivedFrom(rowProperty.getTypeId()))
      {
        Value value = getTablePropertyValue(baseBean, rowProperty, row);
        if (value != null && !StringUtils.isBlank(value.getLabel()))
        {
          String propertyName = rowProperty.getName();
          if (propertyName == null) //Set random property name
          {
            propertyName = RandomStringUtils.randomAlphanumeric(8);
          }
          customPropertyMap.put(propertyName, new CustomProperty(index++, 
            propertyName, rowProperty.getLabel(), value, 
            rowProperty.isEscape(), rowProperty.getStyleClass()));
        }
      }
    }
  }
    
  public void addCustomProperty(String name, String label, String value, 
    boolean escape, String styleClass)
  {
    int index = getMaxCustomPropertyIndex() + 1;
    customPropertyMap.put(name, new CustomProperty(index, name, label, 
      new DefaultValue(value), escape, styleClass));
  }

  public CustomProperty getCustomProperty(String name)
  {
    return customPropertyMap.get(name);
  }

  public boolean removeCustomProperty(String name)
  {
    return (customPropertyMap.remove(name) != null);
  }
  
  public Value getValueByPropertyName(List<TableProperty> columns,
    String propertyName)
  {
    //Search in columns
    for (int i = 0; i < columns.size(); i++)
    {
      if (propertyName.equals(columns.get(i).getName()))
      {
        return values[i];
      }
    }

    //Column not found, search in custom properties
    CustomProperty cp = getCustomProperty(propertyName);
    if (cp != null) return cp.getValue();

    //Not found
    return null;
  }

  protected ScriptClient newScriptClient() throws Exception
  {
    ScriptClient scriptClient = new ScriptClient();

    scriptClient.put("userSessionBean",
      UserSessionBean.getCurrentInstance());
    scriptClient.put("applicationBean",
      ApplicationBean.getCurrentInstance());
    scriptClient.put("WebUtils",
      WebUtils.class.getConstructor().newInstance());
    scriptClient.put("DictionaryUtils",
      DictionaryUtils.class.getConstructor().newInstance());

    return scriptClient;
  }

  protected Value getDefaultValue(String columnName)
  {
    return new DefaultValue("");
  }

  protected Value formatValue(String rowTypeId, Object key,
    List<String> values, String icon) throws Exception
  {
    Value rowValue = null;

    if (values == null || values.isEmpty())
      return new DefaultValue("");

    String skey = String.valueOf(key);

    if (rowTypeId != null)
    {
      Type type = TypeCache.getInstance().getType(rowTypeId);
      if (type != null)
      {
        PropertyDefinition pd = type.getPropertyDefinition((String) key);
        if (pd != null)
        {
          if (pd.getMaxOccurs() > 1)
          {
            rowValue = new DefaultValue(values.toString(), icon);
          }
          else
          {
            String value = values.get(0);
            if (value != null)
            {
              PropertyType propType = pd.getType();
              if (propType.equals(PropertyType.DATE))
                rowValue = new DateValue(value, icon);
              else if (pd.getEnumTypeId() != null)
              {
                rowValue =
                  new EnumTypeValue(pd.getEnumTypeId(), value, propType, icon);
              }
              else if (skey.endsWith("TypeId"))
                rowValue = new TypeValue(value, icon);
              else if (propType.equals(PropertyType.NUMERIC))
                rowValue = new NumericValue(value, icon);
              else
                rowValue = new DefaultValue(value, icon);
            }
            return rowValue;
          }
        }
        else
          rowValue = new DefaultValue(values.get(0), icon);
      }
      else
        rowValue = new DefaultValue(values.get(0), icon);
    }

    return rowValue != null ? rowValue : new DefaultValue("");
  }

  protected Value getTablePropertyValue(BaseBean baseBean,
    TableProperty tableProperty, Object row) throws Exception
  {
    if (tableProperty.getExpression() != null)
    {
      ScriptClient scriptClient = getScriptClient(baseBean);
      scriptClient.put("row", row);
      scriptClient.put("baseBean", baseBean);     
      return new DefaultValue(
        scriptClient.execute(tableProperty.getExpression()), 
          tableProperty.getIcon());
    }
    else
    {
      Property property =
        DictionaryUtils.getProperty(row, tableProperty.getName());
      if (property != null)
      {
        List<String> value = property.getValue();
        String name = tableProperty.getName();
        String icon = tableProperty.getIcon();
        return formatValue(typeId, name, value, icon);
      }
      else
        return getDefaultValue(tableProperty.getName());
    }
  }
    
  private ScriptClient getScriptClient(BaseBean baseBean)
    throws Exception
  {    
    ScriptClient scriptClient;
    String scriptName;
    
    if (baseBean == null)
    {
      scriptClient = new ScriptClient();
    }
    else if (baseBean instanceof FinderBean)
    {
      FinderBean finderBean = ((FinderBean)baseBean);
      scriptName = finderBean.getObjectSetup().getScriptName();
      if (scriptName != null)
        scriptClient = finderBean.getScriptClient(scriptName);
      else
        scriptClient = new ScriptClient();
    }    
    else 
    {
      ObjectBean objectBean;
      if (baseBean instanceof ObjectBean)
        objectBean = ((ObjectBean)baseBean);
      else
        objectBean = baseBean.getObjectBean();
      scriptName = objectBean.getObjectSetup().getScriptName();
      if (scriptName != null)
        scriptClient = objectBean.getScriptClient(scriptName);
      else
        scriptClient = new ScriptClient();
    }    
    
    return scriptClient;
  }
  
  private int getMaxCustomPropertyIndex()
  {
    int maxIndex = 0;
    List<CustomProperty> customProperties = 
      new ArrayList(customPropertyMap.values());    
    for (CustomProperty customProperty : customProperties)
    {
      if (customProperty.getIndex() > maxIndex) 
        maxIndex = customProperty.getIndex();
    }
    return maxIndex;
  }  

  public abstract class Value
  {
    protected String label;
    protected String icon;

    public String getLabel()
    {
      return label;
    }

    public String getIcon()
    {
      return icon;
    }
    
    public Object getSorted() //default sorted value
    {
      return label;
    }

    public String getRawValue() //default raw value
    {
      return (String)getSorted();
    }
  }

  public class DefaultValue extends Value
  {
    public DefaultValue(Object value)
    {
      if (value != null)
        this.label = value.toString();
    }

    public DefaultValue(Object value, String icon)
    {
      this(value);
      this.icon = icon;
    }
  }

  public class DateValue extends Value
  {
    private Object sorted;
    
    public DateValue(String value)
    {
      String pattern = (value != null && value.length() == 14 ?
        "dd/MM/yyyy HH:mm:ss" : "dd/MM/yyyy");
      label = TextUtils.formatInternalDate(value, pattern);
      sorted = value;
    }
    
    public DateValue(String value, String icon)
    {
      this(value);
      this.icon = icon;
    }

    @Override
    public Object getSorted()
    {
      return sorted;
    }    
  }

  public class EnumTypeValue extends Value
  {
    private Object sorted;
    private String rawValue;
    
    public EnumTypeValue(String enumTypeId, Object value, PropertyType propType)
      throws Exception
    {
      List<EnumTypeItem> items =
        EnumTypeCache.getInstance().getItemsByValue(enumTypeId, (String)value);
      if (items != null && !items.isEmpty())
      {
        label = items.get(0).getLabel();
      }
      if (!propType.equals(PropertyType.NUMERIC))
        sorted = label;
      else
      {
        try
        {
          Double dvalue = Double.valueOf(label);
          sorted = dvalue;
        }
        catch(NumberFormatException ex)
        {
          sorted = label;
        }
      }
      rawValue = (String)value;
    }
    
    public EnumTypeValue(String enumTypeId, Object value, PropertyType propType,
      String icon) throws Exception
    {
      this(enumTypeId, value, propType);
      this.icon = icon;
    }

    @Override
    public Object getSorted()
    {
      return sorted;
    }
    
    @Override
    public String getRawValue() 
    {
      return rawValue;
    }
  }

  public class TypeValue extends Value
  {
    private String rawValue;    
    
    public TypeValue(String value)
    {
      label = value;
      Type keyType = TypeCache.getInstance().getType(value);
      if (keyType != null)
        label = keyType.getDescription();

      rawValue = value;
    }
    
    public TypeValue(String value, String icon)
    {
      this(value);
      this.icon = icon;
    }
    
    @Override
    public String getRawValue() 
    {
      return rawValue;
    }    
  }

  public class NumericValue extends Value
  {
    private Object sorted;
    private String rawValue;    
    
    public NumericValue(String value)
    {
      label = value;
      sorted = value;
      if (value != null)
      {
        try
        {
          Double dvalue = Double.valueOf(value);
          sorted = dvalue;
        }
        catch (NumberFormatException ex) { }
      }
      rawValue = value;
    }
    
    public NumericValue(String value, String icon)
    {
      this(value);
      this.icon = icon;
    }
    
    @Override
    public Object getSorted()
    {
      return sorted;
    }    
    
    @Override
    public String getRawValue() 
    {
      return rawValue;
    }    
  }

  public class CustomProperty
  {
    private int index;
    private String name;
    private String label;
    private Value value;
    private boolean escape;
    private String styleClass;

    public CustomProperty(int index, String name, String label, Value value, 
      boolean escape, String styleClass)
    {
      this.index = index;
      this.name = name;
      this.label = label;
      this.value = value;
      this.escape = escape;
      this.styleClass = styleClass;
    }

    public int getIndex()
    {
      return index;
    }

    public void setIndex(int index)
    {
      this.index = index;
    }

    public String getName()
    {
      return name;
    }

    public void setName(String name)
    {
      this.name = name;
    }

    public String getLabel()
    {
      return label;
    }

    public void setLabel(String label)
    {
      this.label = label;
    }

    public Value getValue()
    {
      return value;
    }

    public void setValue(Value value)
    {
      this.value = value;
    }

    public boolean isEscape() 
    {
      return escape;
    }

    public void setEscape(boolean escape) 
    {
      this.escape = escape;
    }

    public String getStyleClass() 
    {
      return styleClass;
    }

    public void setStyleClass(String styleClass) 
    {
      this.styleClass = styleClass;
    }
  }

}
