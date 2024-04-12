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
import java.util.HashMap;
import java.util.List;
import org.matrix.dic.Property;
import org.matrix.dic.PropertyDefinition;
import org.matrix.dic.PropertyType;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.util.Table;
import org.santfeliu.util.TextUtils;
import org.santfeliu.util.data.DataProvider;
import org.santfeliu.util.data.DataProviderFactory;
import org.santfeliu.util.script.ScriptClient;
import org.santfeliu.web.ApplicationBean;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.webapp.BaseBean;
import org.santfeliu.webapp.setup.Column;

/**
 *
 * @author blanquepa
 */
public class DataTableRow implements Serializable
{
  protected String rowId;
  protected String typeId;
  protected Value[] values;
  protected String[] icons;
  
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

  public String[] getIcons()
  {
    return icons;
  }

  public void setIcons(String[] icons)
  {
    this.icons = icons;
  }
  
  public Value[] getValues()
  {
    return values;
  }
  
  public void setValues(Value[] values)
  {
    this.values = values;
  }
  
  public void setValues(BaseBean baseBean, Object row, List<Column> columns) 
    throws Exception
  {   
    values = new Value[columns.size()];
    icons = new String[columns.size()];
    for (int i = 0; i < columns.size(); i++)
    {
      ScriptClient scriptClient = null;
      Column column = columns.get(i);
      if (column.getExpression() != null)
      {
        scriptClient = newScriptClient();
        scriptClient.put("row", row);
        scriptClient.put("baseBean", baseBean);    
        values[i] = new DefaultValue(scriptClient.execute(column.getExpression()));
      }
      else
      {
        Property property = DictionaryUtils.getProperty(row, column.getName());
        if (property != null)
        {
          String columnName = column.getName();
          List<String> value = property.getValue();
          values[i] = formatValue(typeId, columnName, value);            
        }
        else
          values[i] = getDefaultValue(column.getName());
      }
      
      if (column.getIcon() != null)
      {
        if (scriptClient == null) 
        {
          scriptClient = newScriptClient();
          scriptClient.put("row", row);          
        } 
        icons[i] = (String) scriptClient.execute(column.getIcon());          
      }
    }
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
    List<String> values) throws Exception
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
            rowValue = new DefaultValue(values.toString());
          }
          else
          {
            String value = values.get(0);
            if (value != null)
            {
              PropertyType propType = pd.getType();
              if (propType.equals(PropertyType.DATE))
                rowValue = new DateValue(value);
              else if (pd.getEnumTypeId() != null)
              {
                rowValue = 
                  new EnumTypeValue(pd.getEnumTypeId(), value, propType);
              }
              else if (skey.endsWith("TypeId"))
                rowValue = new TypeValue(value);
              else if (propType.equals(PropertyType.NUMERIC))
                rowValue = new NumericValue(value);
              else
                rowValue = new DefaultValue(value);
            }
            return rowValue;
          }
        }
        else
          rowValue = new DefaultValue(values.get(0));
      }
      else
        rowValue = new DefaultValue(values.get(0));
    }
    
    return rowValue != null ? rowValue : new DefaultValue("");
  }

  public abstract class Value
  {
    protected Object sorted;
    protected String label;

    public String getLabel()
    {
      return label;
    } 

    public Object getSorted()
    {
      return sorted;
    } 
  }
  
  public class DefaultValue extends Value
  {
    public DefaultValue(Object value)
    {
      if (value != null)
        this.label = value.toString();
      this.sorted = label;
    }  
  }
  
  public class DateValue extends Value
  {
    public DateValue(String value)
    {
      String pattern = (value != null && value.length() == 14 ? 
        "dd/MM/yyyy HH:mm:ss" : "dd/MM/yyyy");
      label = TextUtils.formatInternalDate(value, pattern);
      sorted = value;
    }  
  }
  
  public class EnumTypeValue extends Value
  {  
    public EnumTypeValue(String enumTypeId, Object value, PropertyType propType) 
      throws Exception
    {    
      DataProviderFactory factory = DataProviderFactory.getInstance();
      String ref = "enumtype:" + enumTypeId;
      DataProvider provider;

      provider = factory.createProvider(ref);
      HashMap context = new HashMap();
      context.put("value", value);
      Table data = provider.getData(context);
      if (data != null && !data.isEmpty())
      {
        label = (String) data.getElementAt(0, 1); //Label column of first value
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
    }
  }   

  public class TypeValue extends Value
  {
    public TypeValue(String value)
    {
      label = value;
      Type keyType = TypeCache.getInstance().getType(value); 
      if (keyType != null)
        label = keyType.getDescription();

      sorted = label;         
    }
  }  
  
  public class NumericValue extends Value
  {
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
    }
  }
  
}
