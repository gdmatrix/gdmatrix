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
import java.util.List;
import java.util.Objects;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author blanquepa
 */
public class ResultListHelper<T extends Serializable> implements Serializable
{
  public static final int PAGE_SIZE = 10;
  public static final String PAGE_SIZE_PROPERTY = "pageSize";
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
  
  public List<T> getRows()
  {
    return rows;
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
  
  protected void populate()
  {
    rows = pageBacking.getResults(firstRowIndex, getPageSize());
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
 
}
