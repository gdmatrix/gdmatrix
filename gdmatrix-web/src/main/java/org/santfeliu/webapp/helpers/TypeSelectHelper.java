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

/**
 *
 * @author lopezrj-sf
 */
public abstract class TypeSelectHelper<T>
{  
  private List<SelectItem> currentTypeSelectItems = new ArrayList<>();
  private String currentTypeId;
  private List<T> filteredRows;
  
  public abstract List<T> getRows();
  public abstract boolean isGroupedViewEnabled();
  public abstract String getBaseTypeId();
  public abstract void resetFirstRow();
  public abstract String getRowTypeId(T row);

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

  public List<T> getFilteredRows()
  {
    return filteredRows;
  }

  public void setFilteredRows(List<T> filteredRows)
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
    List<T> allRows = getRows();
    if (currentTypeId == null)
    {
      setFilteredRows(allRows);
    }
    else
    {
      List<T> auxFilteredRows = new ArrayList<>();      
      for (T row : allRows)
      {
        if (currentTypeId.equals(getRowTypeId(row)))
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
    
    String typeId = getBaseTypeId();
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
    List<T> allRows = getRows();    
    getCurrentTypeSelectItems().clear();
    if (allRows != null && !allRows.isEmpty())
    {
      TypeCache typeCache = TypeCache.getInstance();
      Set<String> itemTypeIds = getItemTypeIds();
      for (String itemTypeId : itemTypeIds)
      {
        Type type = typeCache.getType(itemTypeId);
        if (type != null)
        {
          String itemTypeDescription = type.getDescription();
          SelectItem selectItem = new SelectItem(itemTypeId, 
            itemTypeDescription);
          getCurrentTypeSelectItems().add(selectItem);            
        }
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
    List<T> allRows = getRows();
    Set<String> itemTypeIds = new HashSet<>();
    for (T row : allRows)
    {
      String typeId = getRowTypeId(row);
      if (typeId != null)
        itemTypeIds.add(typeId);
    }
    return itemTypeIds;    
  }
}