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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author blanquepa
 * @param <T>
 */
public class ResultListHelper<T extends Serializable> implements Serializable
{
  public static final int PAGE_SIZE = 10;  
  public static final String PAGE_SIZE_PROPERTY = "pageSize";
  public static final int MAX_SIZE = 0;
  public static final String MAX_SIZE_PROPERTY = "maxSize";
  public static final String COLUMNS_TEMPLATE = "columnsTemplate";
  
  protected ResultListPage pageBacking;
  
  //Rows
  protected List<T> rows;
  protected int firstRowIndex;
  
  //Columns
  protected List<ColumnModel> columns;

  public ResultListHelper(ResultListPage pageBacking)
  {
    this.pageBacking = pageBacking;
  }
  
  /**
   * @return List of POJO objects
   */
  public List<T> getRows()
  {
    return rows;
  } 
  
  /**
   * 
   * @return List of objects as Map of properties. 
   */
  public ResultList getResultList()
  {
    return new ResultList(rows);
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
     
  public void search()
  {
    firstRowIndex = 0; // reset index
    createDynamicColumns();     
    populate(); // force rows population 
  }    
  
  public void reset()
  {
    rows = null;
    firstRowIndex = 0;
    columns = null;
  }  
  
  public void refresh()
  {
    createDynamicColumns();     
    populate(); 
  }  
        
  protected void populate()
  {
    rows = pageBacking.getResults(firstRowIndex, getMaxSize());
  }
  
  public int getFirstRowIndex()
  {
    int size = getRowCount();
    if (size == 0)
    {
      firstRowIndex = 0;
    }
    else if (firstRowIndex >= size)
    {
      int pageSize = getPageSize();
      firstRowIndex = pageSize * ((size - 1) / pageSize);
    }
    return firstRowIndex;
  }  
  
  public void setFirstRowIndex(int firstRowIndex)
  {
    this.firstRowIndex = firstRowIndex;
  }  
  
  public int getRowCount()
  {
    return rows == null ? 0 : rows.size();
  }  
  
  public int getPageSize()
  {
    String pageSize = UserSessionBean.getCurrentInstance().getMenuModel()
      .getSelectedMenuItem().getProperty(PAGE_SIZE_PROPERTY);
    if (pageSize != null)
      return Integer.parseInt(pageSize);
    else
      return PAGE_SIZE;
  }   

  public int getMaxSize()
  {
    String maxSize = UserSessionBean.getCurrentInstance().getMenuModel()
      .getSelectedMenuItem().getProperty(MAX_SIZE_PROPERTY);
    if (maxSize != null)
      return Integer.parseInt(maxSize);
    else
      return MAX_SIZE;
  }   
  
  public List<ColumnModel> getColumns() 
  {
    return columns;
  } 
  
  protected void createDynamicColumns()
  {
    List<String> columnsTemplate = 
      UserSessionBean.getCurrentInstance().getMenuModel()
      .getSelectedMenuItem().getMultiValuedProperty(COLUMNS_TEMPLATE);    
    if (!columnsTemplate.isEmpty())
    {      
      columns = new ArrayList<>();
      for (String columnKey : columnsTemplate)
      {
        String[] parts = columnKey.split("::");
        String property = parts[0];
        String label = parts.length > 1 ? parts[1] : columnKey.toUpperCase();
        String style = parts.length > 2 ? parts[2] : "";  
        ColumnModel columnModel = new ColumnModel(property, label, style);
        columns.add(columnModel);
      }
    }
  }
  
  //TODO: styles, renderers, converters...JSON???
  public static class ColumnModel implements Serializable, 
    Comparable<ColumnModel> 
  {
    private String label;
    private final String property;
    private String style;
    
    public ColumnModel(String property)
    {
      this.property = property;
    }

    public ColumnModel(String property, String label, String style)
    {
      this.label = label;
      this.property = property;
      this.style = style;
    }

    public String getLabel()
    {
      return label;
    }

    public String getProperty()
    {
      return property;
    }

    public String getStyle()
    {
      return style;
    }

    @Override
    public int compareTo(ColumnModel o)
    {
      return property.compareTo(o.getProperty()); 
    }

    @Override
    public boolean equals(Object o)
    {
      if (o == null)
        return false;
      
      if (this.getClass() != o.getClass())
        return false;
      
      return property.equals(((ColumnModel)o).getProperty());
    }

    @Override
    public int hashCode()
    {
      int hash = 3;
      hash = 97 * hash + Objects.hashCode(this.property);
      return hash;
    }
  }
  
  /**
   * Allows to component <p:columns> show metadata values of List<Property> 
   * fields.
   */
  public class ResultList extends ArrayList
  {
    private List list = new ArrayList();
    private final Map<Object, Object> formattedValuesMap = new HashMap();    
    
    public ResultList(List list)
    {
      super();
      if (list != null)
        this.list = list;
    }

    @Override
    public Iterator iterator()
    {
      return list.iterator();
    }

    @Override
    public boolean isEmpty()
    {
      return list.isEmpty();
    }

    @Override
    public int size()
    {
      return list.size();
    }
    
    @Override
    public Row get(int index) 
    {
      if (list == null)
        return null;
      
      Object listItem = list.get(index);
      return new Row(listItem);
    }
    
    public class Row extends HashMap<String, Object>
    {
      private static final String PROPERTY_FIELD_NAME = "property";
      private Object object;
      
      public Row(Object object)
      {
        super();
        try
        {
          if (object != null)
          {
            this.object = object;
            putAll(PropertyUtils.describe(object));
          }
        }
        catch (Exception ex)
        {
          Logger.getLogger(ResultListHelper.class.getName()).
            log(Level.SEVERE, null, ex);
        }
      }

      @Override
      public Object get(Object key)
      {
        Object obj = super.get(key);
        if (obj == null)
        {
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
      
      private Object formatValue(Object key, Object value)
      {
        Object fValue = formattedValuesMap.get(key + "::" + value);
        
        if (fValue == null)
        {
          if (pageBacking instanceof Typed)
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
              String typeId = ((Typed) pageBacking).getTypeId();
              Type type = TypeCache.getInstance().getType(typeId);
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
          Logger.getLogger(ResultListHelper.class.getName()).
            log(Level.SEVERE, null, ex);
        }  
        
        return result;
      }
      
      private Object formatDate(Object value)
      {
        return TextUtils.formatInternalDate((String) value, "dd/MM/yyyy");
      }
         
    }
    
  } 
}
