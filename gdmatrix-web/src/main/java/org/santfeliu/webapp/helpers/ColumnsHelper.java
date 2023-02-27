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
package org.santfeliu.webapp.helpers;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.beanutils.PropertyUtils;
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
import org.santfeliu.webapp.TypeBean;
import org.santfeliu.webapp.setup.Column;

/**
 *
 * @author blanquepa
 * @param <T>
 */
public abstract class ColumnsHelper<T extends Serializable> 
  implements Serializable
{
  private TypeBean typeBean;
  private List<Column> columns;
  
  public ColumnsHelper(TypeBean typeBean)
  {
    this.typeBean = typeBean;
    this.columns = new ArrayList();
  }

  public ColumnsHelper(List<Column> columns)
  {
    this.columns = columns;
  }

  public void setColumns(List<Column> columns)
  {
    this.columns = columns;
  }
  
  public List<Column> getColumns() 
  {
    return columns;
  }   
  
  /**
   * @return List of POJO objects
   */
  public abstract List<T> getRows();
  
  public abstract T getRowData(T object);
  
  public T getRow(int i)
  {
    T row = null;
    List<T> rows = getRows();
    if (rows != null)
      row = getRows().get(i);
    
    return row;
  }
  
  /**
   * 
   * @return List of objects as Map of properties. 
   */
  public ResultList getResultList()
  {
    return new ResultList();
  }
  
  public Object getObject(Object row)
  {
    if (row instanceof HashMap)
      return getRow((ResultList.Row) row);
    else
      return row;
  }  
  
  private Object getRow(ResultList.Row resultListItem)
  {
    return resultListItem.getObject();
  }
          
  /**
   * Allows to component <p:columns> show metadata values of List<Property> 
   * fields.
   */
  public class ResultList extends AbstractList
  {
    private final Map<Object, Object> formattedValuesMap = new HashMap();    

    @Override
    public Iterator iterator()
    {
      List rows = getRows();        
      return rows != null ? getRows().iterator() : null;
    }

    @Override
    public boolean isEmpty()
    {
      List rows = getRows();      
      return rows != null ? getRows().isEmpty() : true;
    }

    @Override
    public int size()
    {
      List rows = getRows();
      return rows != null ? getRows().size() : 0;
    }
    
    @Override
    public Row get(int index) 
    {
      return new Row(getRows().get(index));
    }
    
    public class Row extends HashMap<String, Object>
    {
      private static final String PROPERTY_FIELD_NAME = "property";
      private transient Object object;
      private boolean dataLoaded = false;
      
      public Row(Object object)
      {
        super();
        this.object = object;
        describe(object);
      }

      @Override
      public Object get(Object key)
      {
        Object obj = super.get(key);
        if (obj == null)
        {
          //Look for dynamic data
          if (!dataLoaded)
          {
            object = getRowData((T) object);
            describe(object);
            dataLoaded = true;
          }
          Object properties = super.get(PROPERTY_FIELD_NAME);
          if (properties != null && properties instanceof List)
          {
            List<Property> propList = (List<Property>) properties;
            obj = DictionaryUtils.getPropertyValue(propList, (String) key);
          }
        }
        obj = formatValue(key, obj);        

        return obj;
      }
      
      public Object getObject()
      {
        return this.object;
      }
      
      private void describe(Object object)
      {
        try
        {
          if (object != null)
          {
            putAll(PropertyUtils.describe(object));
          }
        }
        catch (Exception ex)
        {
          Logger.getLogger(ColumnsHelper.class.getName()).
            log(Level.SEVERE, null, ex);
        }        
      }
      
      private Object formatValue(Object key, Object value)
      {
        Object fValue = formattedValuesMap.get(key + "::" + value);
        
        if (fValue == null)
        {
          String skey = String.valueOf(key);
          if (skey.endsWith("TypeId")) //It's a typeId field
          {
            String svalue = String.valueOf(value);
            Type keyType = TypeCache.getInstance().getType(svalue); 
            if (keyType != null)                
              fValue = keyType.getDescription();
          }
          else
          {
            String objectTypeId = typeBean.getTypeId(object);
            if (objectTypeId != null)
            {
              Type type = TypeCache.getInstance().getType(objectTypeId);
              PropertyDefinition pd = type.getPropertyDefinition((String) key);
              if (pd != null)
              {
                PropertyType propType = pd.getType();
                if (propType.equals(PropertyType.DATE))
                  fValue = formatDate(value);
                else if (pd.getEnumTypeId() != null)
                  fValue = formatEnumType(pd.getEnumTypeId(), key, value);
              }
            }
          }
          formattedValuesMap.put(key + "::" + value, fValue);
        }
        
        return fValue != null ? fValue : value;
      }
      
      private Object formatEnumType(String enumTypeId, Object key, Object value)
      {
        Object result = value;
        
        DataProviderFactory factory = DataProviderFactory.getInstance();
        String ref = "enumtype:" + enumTypeId;
        DataProvider provider;
        try 
        {
          provider = factory.createProvider(ref);
          HashMap context = new HashMap();
          context.put("value", value);
          Table data = provider.getData(context);
          if (data != null && !data.isEmpty())
          {
            result = data.getElementAt(0, 1); //Label column of first value
          }
        }
        catch (Exception ex) 
        {
          Logger.getLogger(ColumnsHelper.class.getName()).
            log(Level.SEVERE, null, ex);
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
    
  } 
}
