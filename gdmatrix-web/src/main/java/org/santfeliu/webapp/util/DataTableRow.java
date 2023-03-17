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
import org.santfeliu.webapp.setup.Column;

/**
 *
 * @author blanquepa
 */
public class DataTableRow implements Serializable
{
  protected String rowId;
  protected String typeId;
  protected Object[] values;
  
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
  
  public Object[] getValues()
  {
    return values;
  }

  public void setValues(Object[] values)
  {
    this.values = values;
  }
  
  public void setValues(Object row, List<Column> columns) 
    throws Exception
  {
    values = new Object[columns.size()];
    for (int i = 0; i < columns.size(); i++)
    {
      Column column = columns.get(i);
      Property property = DictionaryUtils.getProperty(row, column.getName());        
      if (property != null)
      {
        String columnName = column.getName();
        List<String> value = property.getValue();
        values[i] = formatValue(typeId, columnName, value);            
      }
    } 
  }  
  
  protected Object formatValue(String rowTypeId, Object key, 
    List<String> values) throws Exception
  {
    Object fValue = "";  
    
    if (values == null || values.isEmpty())
      return fValue;

    String skey = String.valueOf(key);
    
    if (rowTypeId != null)
    {
      Type type = TypeCache.getInstance().getType(rowTypeId);
      PropertyDefinition pd = type.getPropertyDefinition((String) key);
      if (pd != null)
      {    
        if (pd.getMaxOccurs() > 1)
        {
          fValue = values.toString();
        }
        else
        {
          fValue = values.get(0);
          PropertyType propType = pd.getType();
          if (propType.equals(PropertyType.DATE))
            fValue = formatDate(fValue);
          else if (pd.getEnumTypeId() != null)
            fValue = formatEnumType(pd.getEnumTypeId(), fValue);
          else if (skey.endsWith("TypeId"))
          {
            String svalue = String.valueOf(fValue);
            Type keyType = TypeCache.getInstance().getType(svalue); 
            if (keyType != null)                
              fValue = keyType.getDescription();            
          }
          
          return fValue;
        }
      }
      else
        fValue = values.get(0);
    }
    
    return fValue != null ? fValue : values;
  }

  private Object formatEnumType(String enumTypeId, Object value) 
    throws Exception
  {
    Object result = value;

    DataProviderFactory factory = DataProviderFactory.getInstance();
    String ref = "enumtype:" + enumTypeId;
    DataProvider provider;

    provider = factory.createProvider(ref);
    HashMap context = new HashMap();
    context.put("value", value);
    Table data = provider.getData(context);
    if (data != null && !data.isEmpty())
    {
      result = data.getElementAt(0, 1); //Label column of first value
    }

    return result;
  }

  private Object formatDate(Object value)
  {
    if (value == null) return null;

    String sValue = (String)value;        
    String pattern = (sValue.length() == 14 ? "dd/MM/yyyy HH:mm:ss" : 
      "dd/MM/yyyy");
    return TextUtils.formatInternalDate(sValue, pattern);
  }   
  
}
