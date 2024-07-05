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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.webapp.util.DataTableRow;

/**
 *
 * @author lopezrj-sf
 */
public abstract class TypeSelectHelper
{  
  private List<SelectItem> currentTypeSelectItems = new ArrayList<>();
  private String currentTypeId;
  private List<? extends DataTableRow> filteredRows;
  
  public abstract List<? extends DataTableRow> getRows();
  public abstract boolean isGroupedViewEnabled();
  public abstract String getTabBaseTypeId();
  public abstract void resetFirstRow();

  public List<SelectItem> getCurrentTypeSelectItems()
  {
    return currentTypeSelectItems;
  }

  public void setCurrentTypeSelectItems(List<SelectItem> currentTypeSelectItems)
  {
    this.currentTypeSelectItems = currentTypeSelectItems;
  }
  
  public String getCurrentTypeId()
  {
    return currentTypeId;
  }

  public void setCurrentTypeId(String currentTypeId)
  {
    this.currentTypeId = currentTypeId;
  }

  public List<? extends DataTableRow> getFilteredRows()
  {
    return filteredRows;
  }

  public void setFilteredRows(List<? extends DataTableRow> filteredRows)
  {
    this.filteredRows = filteredRows;
  }

  public void load()
  {
    if (isRenderCurrentTypeSelector())
    {
      loadCurrentTypeSelectItems();
      currentTypeId = null;
    }
    setFilteredRows(getRows());    
  }
  
  public void currentTypeChanged(AjaxBehaviorEvent e)
  {
    List<? extends DataTableRow> allRows = getRows();
    if (currentTypeId == null)
    {
      setFilteredRows(allRows);
    }
    else
    {
      List<DataTableRow> auxFilteredRows = new ArrayList<>();      
      for (DataTableRow row : allRows)
      {
        if (row.getTypeId().equals(currentTypeId))
        {
          auxFilteredRows.add(row);
        }
      }    
      setFilteredRows(auxFilteredRows);
    }
    resetFirstRow();
  }
  
  public boolean isRenderCurrentTypeSelector()
  {
    if (getRows() == null ||
      getRows().isEmpty() ||
      isGroupedViewEnabled() ||
      getItemTypeIds().size() <= 1) 
    {
      return false;
    }    
    
    String typeId = getTabBaseTypeId();
    if (typeId != null)
    {
      Type type = TypeCache.getInstance().getType(typeId);
      if (type != null)
      {
        return !type.isLeaf();
      }
    }
    else
    {
      return true;
    }
    return false;
  }
  
  public void loadCurrentTypeSelectItems()
  {
    List<? extends DataTableRow> allRows = getRows();    
    getCurrentTypeSelectItems().clear();
    if (allRows != null && !allRows.isEmpty())
    {
      TypeCache typeCache = TypeCache.getInstance();
      Set<String> itemTypeIds = getItemTypeIds();
      for (String itemTypeId : itemTypeIds)
      {
        Type type = typeCache.getType(itemTypeId);
        String itemTypeDescription = type.getDescription();
        SelectItem selectItem = new SelectItem(itemTypeId, 
          itemTypeDescription);
        getCurrentTypeSelectItems().add(selectItem);            
      }
      getCurrentTypeSelectItems().sort(new Comparator<SelectItem>()
      {
        @Override
        public int compare(SelectItem o1, SelectItem o2)
        {
          return o1.getLabel().compareTo(o2.getLabel());
        }
      });
    }
  }
  
  private Set<String> getItemTypeIds()
  {
    List<? extends DataTableRow> allRows = getRows();
    Set<String> itemTypeIds = new HashSet<>();
    for (DataTableRow row : allRows)
    {
      itemTypeIds.add(row.getTypeId());
    }
    return itemTypeIds;    
  }
  
}
