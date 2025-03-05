package org.santfeliu.webapp.helpers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.web.ApplicationBean;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.setup.TableProperty;
import org.santfeliu.webapp.util.DataTableRow;

/**
 *
 * @author lopezrj-sf
 */
public abstract class RowsFilterHelper<T>
{  
  private final static String SHOW_ALL = "SHOW_ALL";
  private final static String BLANK_VALUE = "BLANK_VALUE";
  
  private List<SelectItem> filterValueItems;
  private String filterValue;
  private List<T> filteredRows;
  
  public abstract ObjectBean getObjectBean();
  public abstract List<TableProperty> getColumns();  
  public abstract List<T> getRows();
  public abstract boolean isGroupedViewEnabled();
  public abstract void resetFirstRow();
  public abstract String getFixedColumnValue(T row, String columnName);
  public abstract String getRowTypeId(T row);  
  
  public List<SelectItem> getFilterValueItems() 
  {
    if (filterValueItems == null)
    {
      filterValueItems = loadFilterValueItems();      
    }
    return filterValueItems;    
  }

  public void setFilterValueItems(List<SelectItem> filterValueItems) 
  {
    this.filterValueItems = filterValueItems;
  }

  public String getFilterValue() 
  {
    return filterValue;
  }

  public void setFilterValue(String filterValue) 
  {
    this.filterValue = filterValue;
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
    filterValueItems = null;
    filterValue = null;
    setFilteredRows(getRows());    
  }
  
  public void filterValueChanged(AjaxBehaviorEvent e)
  {
    List<T> allRows = getRows();
    if (SHOW_ALL.equals(filterValue))
    {
      setFilteredRows(allRows);
    }
    else
    {
      List<T> auxFilteredRows = new ArrayList<>();
      for (T row : allRows)
      {
        String eval = evaluate(row, getFilterBy());
        if (StringUtils.isBlank(eval)) eval = BLANK_VALUE;
        if (eval.equals(filterValue))
        {
          auxFilteredRows.add(row);
        }
      }
      setFilteredRows(auxFilteredRows);
    }
    resetFirstRow();
  }
  
  public boolean isRenderFilterSelector()
  {
    return (getRows() != null && !getRows().isEmpty() && 
      !isGroupedViewEnabled() && getFilterValueItems().size() > 1);
  }
  
  public String getFilterByLabel()
  {
    String propertyValue = getFilterByPropertyValue();
    if (propertyValue != null)
    {
      if (propertyValue.contains(":"))
      {
        return ApplicationBean.getCurrentInstance().translate(
          propertyValue.split(":")[1]);
      }
      else
      {
        return ApplicationBean.getCurrentInstance().translate(
          "$$objectBundle.filter");
      }
    }
    else
    {
      return ApplicationBean.getCurrentInstance().translate(
        "$$dicBundle.type_type");
    }
  }
  
  public String getShowAllLabel()
  {
    List<SelectItem> items = getFilterValueItems();
    for (SelectItem item : items)
    {
      if (BLANK_VALUE.equals(item.getValue()))
      {
        return ApplicationBean.getCurrentInstance().translate(
          "$$objectBundle.showAll");                
      }
    }
    return "";
  }
  
  private String getFilterBy()
  {
    String propertyValue = getFilterByPropertyValue();
    if (propertyValue != null)
    {
      if (propertyValue.contains(":"))
      {
        propertyValue = propertyValue.split(":")[0];
      }
      return propertyValue;
    }
    else
    {
      return "typeId"; //default value
    }
  }

  private String getFilterByPropertyValue()
  {
    EditTab activeEditTab = getObjectBean().getActiveEditTab();
    if (activeEditTab != null)
    {
      return activeEditTab.getFilterBy();
    }
    return null;    
  }
  
  private List<SelectItem> loadFilterValueItems()
  {
    List<SelectItem> itemList = new ArrayList();
    List<T> allRows = getRows();    
    if (allRows != null && !allRows.isEmpty())
    {
      if ("typeId".equals(getFilterBy()))
      {
        TypeCache typeCache = TypeCache.getInstance();
        Set<String> propertyValues = getPropertyValues(allRows);
        for (String itemTypeId : propertyValues)
        {
          Type type = typeCache.getType(itemTypeId);
          if (type != null)
          {
            String itemTypeDescription = type.getDescription();
            SelectItem selectItem = new SelectItem(itemTypeId, 
              itemTypeDescription);
            itemList.add(selectItem);            
          }  
        }
      }
      else
      {      
        Set<String> propertyValues = getPropertyValues(allRows);
        for (String propertyValue : propertyValues)
        {
          SelectItem selectItem = new SelectItem(propertyValue, 
            BLANK_VALUE.equals(propertyValue) ? "" : propertyValue);
          itemList.add(selectItem);
        }
      }
      itemList.sort(new Comparator<SelectItem>()
      {
        @Override
        public int compare(SelectItem o1, SelectItem o2)
        {
          return o1.getLabel().compareTo(o2.getLabel());
        }
      });      
    }
    return itemList;
  }
  
  private Set<String> getPropertyValues(List<T> rows)
  {
    Set<String> propertyValues = new HashSet<>();
    for (T row : rows)
    {
      String propertyValue = evaluate(row, getFilterBy());
      if (StringUtils.isBlank(propertyValue)) propertyValue = BLANK_VALUE;
      propertyValues.add(propertyValue);
    }
    return propertyValues;
  }
 
  private String evaluate(T row, String expression)
  {
    if (row != null && expression != null)
    {      
      if (expression.equals("typeId"))
      {
        return getRowTypeId(row);
      }
      else
      {
        if (row instanceof DataTableRow) //Dynamic columns
        {
          DataTableRow dtRow = ((DataTableRow)row);
          DataTableRow.Value val = dtRow.getValueByPropertyName(
            getColumns(), expression);
          if (val != null) return val.getLabel();
        }
        else //Fixed columns
        {
          return getFixedColumnValue(row, expression);
        }
      }
    }
    return null;
  }

}