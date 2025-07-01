package org.santfeliu.webapp.helpers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import java.util.function.Function;

/**
 *
 * @author lopezrj-sf
 * @param <T>
 */
public abstract class RowsFilterHelper<T>
{
  private final static String SHOW_ALL = "SHOW_ALL";
  private final static String BLANK_VALUE = "BLANK_VALUE";

  private RowsFilterHelper prevRowsFilterHelper;
  private RowsFilterHelper nextRowsFilterHelper;

  private List<SelectItem> filterValueItems;
  private String filterValue;
  private List<T> filteredRows;

  public abstract ObjectBean getObjectBean();
  public abstract List<TableProperty> getColumns();
  public abstract List<T> getRows();
  public abstract boolean isGroupedViewEnabled();
  public abstract void resetFirstRow();
  public abstract Item getFixedColumnValue(T row, String columnName);
  public abstract String getRowTypeId(T row);

  public RowsFilterHelper(RowsFilterHelper prevRowsFilterHelper)
  {
    this.prevRowsFilterHelper = prevRowsFilterHelper;
  }

  public static RowsFilterHelper create(RowsFilterHelper previous,
    Function<RowsFilterHelper, RowsFilterHelper> maker)
  {
    RowsFilterHelper helper = maker.apply(previous);
    if (previous != null)
    {
      previous.setNextRowsFilterHelper(helper);
    }
    return helper;
  }

  public RowsFilterHelper getPrevRowsFilterHelper()
  {
    return prevRowsFilterHelper;
  }

  public void setPrevRowsFilterHelper(RowsFilterHelper prevRowsFilterHelper)
  {
    this.prevRowsFilterHelper = prevRowsFilterHelper;
  }

  public RowsFilterHelper getNextRowsFilterHelper()
  {
    return nextRowsFilterHelper;
  }

  public void setNextRowsFilterHelper(RowsFilterHelper nextRowsFilterHelper)
  {
    this.nextRowsFilterHelper = nextRowsFilterHelper;
  }

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
    if (filteredRows == null)
    {
      List<T> allRows = getRows();
      if (filterValue == null || SHOW_ALL.equals(filterValue))
      {
        filteredRows = allRows;
      }
      else
      {
        List<T> auxFilteredRows = new ArrayList<>();
        for (T row : allRows)
        {
          Item eval = evaluate(row, getFilterBy());
          if (eval != null)
          {
            String evalValue = eval.getValue();
            if (StringUtils.isBlank(evalValue)) evalValue = BLANK_VALUE;
            if (evalValue.equals(filterValue))
            {
              auxFilteredRows.add(row);
            }
          }
        }
        filteredRows = auxFilteredRows;
      }
    }
    return filteredRows;
  }

  public void setFilteredRows(List<T> filteredRows)
  {
    this.filteredRows = filteredRows;
  }

  public void reset()
  {
    filterValueItems = null;
    filterValue = null;
    filteredRows = null;
  }

  public void filterValueChanged(AjaxBehaviorEvent e)
  {
    filteredRows = null;
    if (nextRowsFilterHelper != null)
    {
      nextRowsFilterHelper.reset();
    }
    resetFirstRow();
  }

  public boolean isRendered()
  {
    return (getRows() != null && !getRows().isEmpty() &&
      !isGroupedViewEnabled() && getFilterValueItems().size() > 1 &&
      checkLinked());
  }

  private boolean checkLinked()
  {
    if (prevRowsFilterHelper == null) //first filter
    {
      return true;
    }
    else //linked filter
    {
      String prevValue = prevRowsFilterHelper.getFilterValue();
      return (getActiveEditTab().getLinkedFilterByName(prevValue) != null);
    }
  }

  public String getFilterByLabel()
  {
    String filterByLabel = null;
    if (prevRowsFilterHelper == null) //first filter
    {
      filterByLabel = getActiveEditTab().getFilterByLabel();
      if (filterByLabel == null)
      {
        if ("typeId".equals(getFilterBy()))
        {
          filterByLabel = ApplicationBean.getCurrentInstance().translate(
            "$$dicBundle.type_type");
        }
        else
        {
          filterByLabel = ApplicationBean.getCurrentInstance().translate(
            "$$objectBundle.filter");
        }
      }
    }
    else //linked filter
    {
      String prevValue = prevRowsFilterHelper.getFilterValue();
      filterByLabel = getActiveEditTab().getLinkedFilterByLabel(prevValue);
      if (filterByLabel == null)
      {
        if ("typeId".equals(getFilterBy()))
        {
          filterByLabel = ApplicationBean.getCurrentInstance().translate(
            "$$dicBundle.type_type");
        }
        else
        {
          filterByLabel = ApplicationBean.getCurrentInstance().translate(
            "$$objectBundle.filter");
        }
      }
    }
    return filterByLabel;
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
    String filterBy = null;
    if (prevRowsFilterHelper == null) //first filter
    {
      filterBy = getActiveEditTab().getFilterByName();
      if (filterBy == null) filterBy = "typeId";
    }
    else //linked filter
    {
      String prevValue = prevRowsFilterHelper.getFilterValue();
      filterBy = getActiveEditTab().getLinkedFilterByName(prevValue);
    }
    return filterBy;
  }

  private EditTab getActiveEditTab()
  {
    return getObjectBean().getActiveEditTab();
  }

  private List<SelectItem> loadFilterValueItems()
  {
    List<SelectItem> itemList = new ArrayList();
    List<T> allRows = getRows();
    if (allRows != null && !allRows.isEmpty())
    {
      if (getFilterBy() != null)
      {
        Map<String, Item> propertyItems = getPropertyItems(allRows);
        for (Item propertyItem : propertyItems.values())
        {
          SelectItem selectItem = new SelectItem(propertyItem.getValue(),
            propertyItem.getLabel());
          itemList.add(selectItem);
        }
        itemList.sort(new Comparator<SelectItem>()
        {
          @Override
          public int compare(SelectItem o1, SelectItem o2)
          {
            return StringUtils.defaultString(o1.getLabel()).compareTo(
              StringUtils.defaultString(o2.getLabel()));
          }
        });
      }
    }
    return itemList;
  }

  private Map<String, Item> getPropertyItems(List<T> rows)
  {
    Map<String, Item> propertyItems = new HashMap<>();
    for (T row : rows)
    {
      Item propertyItem = evaluate(row, getFilterBy());
      if (propertyItem != null)
      {
        if (StringUtils.isBlank(propertyItem.getValue()))
        {
          propertyItem.setValue(BLANK_VALUE);
        }
        propertyItems.put(propertyItem.getValue(), propertyItem);
      }
    }
    return propertyItems;
  }

  private Item evaluate(T row, String expression)
  {
    if (row != null && expression != null)
    {
      if (expression.equals("typeId"))
      {
        Item item = createTypeItem(getRowTypeId(row));
        if (item != null)
        {
          return item;
        }
      }
      else
      {
        if (row instanceof DataTableRow) //Dynamic columns
        {
          DataTableRow dtRow = ((DataTableRow)row);
          DataTableRow.Value val = dtRow.getValueByPropertyName(
            getColumns(), expression);
          if (val != null)
          {
            return new Item(val.getLabel(), (String)val.getRawValue());
          }
        }
        else //Fixed columns
        {
          Item item = getFixedColumnValue(row, expression);
          if (item != null)
          {
            return item;
          }
        }
      }
      return createEmptyItem(); //if null value
    }
    return null;
  }

  protected static Item createEmptyItem()
  {
    return new Item("", BLANK_VALUE);
  }

  protected static Item createTypeItem(String typeId)
  {
    if (typeId != null)
    {
      Type type = TypeCache.getInstance().getType(typeId);
      if (type != null)
      {
        return new Item(type.getDescription(), typeId);
      }
    }
    return null;
  }

  public static class Item
  {
    private String label;
    private String value;

    public Item(String text)
    {
      this(text, text);
    }

    public Item(String label, String value)
    {
      this.label = label;
      this.value = value;
    }

    public String getLabel()
    {
      return label;
    }

    public void setLabel(String label)
    {
      this.label = label;
    }

    public String getValue()
    {
      return value;
    }

    public void setValue(String value)
    {
      this.value = value;
    }
  }

}