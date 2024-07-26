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

import java.util.List;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.setup.TableProperty;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.util.DataTableRow;
import org.santfeliu.webapp.util.DataTableRow.Value;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author lopezrj-sf
 */
public abstract class GroupableRowsHelper
{
  private boolean groupedView = true;
  
  public abstract ObjectBean getObjectBean();
  public abstract List<TableProperty> getColumns();
  public abstract void sortRows();
  public abstract String getRowTypeColumnName();
  public abstract String getFixedColumnValue(Object row, String columnName);
  
  public String getGroupBy()
  {
    return evaluate(getGroupByProperty());
  }
  
  public String getGroupByDescription()
  {
    return evaluate(getGroupByDescriptionProperty());
  }
  
  public boolean isColumnRendered(TableProperty column)
  {
    return isColumnRendered(column.getName());
  }
  
  public boolean isColumnRendered(String columnName)
  {
    if (columnName == null) return true;
    
    if (!isGroupedView())
    {
      if (columnName.equals(getRowTypeColumnName()))
      {
        String tabTypeId = getObjectBean().getActiveEditTab().getBaseTypeId();
        if (tabTypeId != null)
        {
          return !TypeCache.getInstance().getDerivedTypeIds(tabTypeId).
            isEmpty();
        }
      }
    }
    else
    {    
      String hideColumn = getColumnToHideWhenGrouping();
      if (hideColumn != null)
      {
        return !hideColumn.equals(columnName);
      }
    }
    return true;
  }  
    
  public boolean isGroupedView()
  {
    return isGroupedViewEnabled() && groupedView;
  }

  public void setGroupedView(boolean groupedView)
  {
    this.groupedView = groupedView;
  }

  public boolean isGroupedViewEnabled()
  {
    return getGroupByProperty() != null;
  }
  
  public void switchView()
  {
    groupedView = !groupedView;
    if (!groupedView) //Reorder
    {
      sortRows();
    }
  }

  public String getColumnToHideWhenGrouping()
  {
    String propValue = getObjectBean().getActiveEditTab().getProperties().
      getString("hideColumnWhenGrouping");
    if (propValue != null) return propValue;
   
    String groupBy = getGroupByProperty();
    if (groupBy != null && !groupBy.contains("#{"))
    {
      return groupBy;
    }
    return null;
  }
  
  private String evaluate(String propertyValue)
  {
    if (propertyValue != null)
    {
      Object row = WebUtils.evaluateExpression("#{row}");
      if (row != null)
      {
        if (propertyValue.contains("#{")) //expression
        {
          return WebUtils.evaluateExpression(propertyValue);
        }
        else
        {
          if (row instanceof DataTableRow) //Dynamic columns
          {
            Value val = ((DataTableRow)row).getValueByPropertyName(
              getColumns(), propertyValue);
            if (val != null) return val.getLabel();
          }
          else //Fixed columns
          {
            return getFixedColumnValue(row, propertyValue);
          }
        }
      }
    }
    return null;    
  }  
  
  private String getGroupByProperty()
  {
    EditTab activeEditTab = getObjectBean().getActiveEditTab();
    if (activeEditTab != null)
    {
      String value = activeEditTab.getGroupBy();
      if (value != null)
      {
        if (value.contains(":"))
        {
          value = value.split(":")[0];
        }
        return value;
      }
    }
    return null;
  }
  
  private String getGroupByDescriptionProperty()
  {
    EditTab activeEditTab = getObjectBean().getActiveEditTab();
    if (activeEditTab != null)
    {
      String value = activeEditTab.getGroupBy();
      if (value != null)
      {
        if (value.contains(":"))
        {
          value = value.split(":")[1];
        }
        return value;
      }
    }
    return null;    
  }

}
