package org.santfeliu.dic.web;

import java.util.List;
import org.matrix.dic.DictionaryManagerPort;
import org.matrix.dic.EnumTypeItem;
import org.matrix.dic.EnumTypeItemFilter;
import org.matrix.dic.PropertyType;
import org.santfeliu.web.obj.PageBean;
import org.santfeliu.web.obj.PageFinder;

public class EnumTypeItemsBean extends PageBean
{
  private List<EnumTypeItem> rows;
  private EnumTypeItem editingItem;

  private int firstRowIndex;

  public EnumTypeItemsBean()
  {
    load();
  }

  public int getFirstRowIndex()
  {
    return firstRowIndex;
  }

  public void setFirstRowIndex(int firstRowIndex)
  {
    this.firstRowIndex = firstRowIndex;
  }

  public EnumTypeItem getEditingItem()
  {
    return editingItem;
  }

  public void setEditingItem(EnumTypeItem editingItem)
  {
    this.editingItem = editingItem;
  }

  public List<EnumTypeItem> getRows()
  {
    return rows;
  }

  public void setRows(List<EnumTypeItem> rows)
  {
    this.rows = rows;
  }

  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }

  public String show()
  {
    return "enum_type_items";
  }

  public String createItem()
  {
    editingItem = new EnumTypeItem();
    editingItem.setEnumTypeId(getObjectId());
    return null;
  }

  public String editItem()
  {
    try
    {
      EnumTypeItem row =
        (EnumTypeItem)getExternalContext().getRequestMap().get("row");
      String enumTypeItemId = row.getEnumTypeItemId();
      editingItem =
        DictionaryConfigBean.getPort().loadEnumTypeItem(enumTypeItemId);
    }
    catch(Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String storeItem()
  {
    try
    {
      EnumTypeItem auxEnumTypeItem = null;
      if (isNumericType() && editingItem.getValue() != null)
      {
        editingItem.setValue(editingItem.getValue().replace(",", "."));
      }
      auxEnumTypeItem =
        DictionaryConfigBean.getPort().storeEnumTypeItem(editingItem);
      editingItem = null;
      load();
      firstRowIndex = PageFinder.findFirstRowIndex(rows, getPageSize(),
        "enumTypeItemId", auxEnumTypeItem.getEnumTypeItemId());
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String cancelItem()
  {
    editingItem = null;
    return null;
  }

  public String removeItem()
  {
    try
    {
      EnumTypeItem row =
        (EnumTypeItem)getExternalContext().getRequestMap().get("row");
      DictionaryConfigBean.getPort().removeEnumTypeItem(row.getEnumTypeItemId());
    }
    catch (Exception ex)
    {
      error(ex);
    }
    finally
    {
      load();
      if (firstRowIndex >= rows.size() && firstRowIndex > 0)
      {
        firstRowIndex -= getPageSize();
      }
    }
    return null;
  }
  
  public String createItemBeforeRow()
  {
    editingItem = new EnumTypeItem();
    editingItem.setEnumTypeId(getObjectId());
    EnumTypeItem row =
      (EnumTypeItem)getExternalContext().getRequestMap().get("row");
    editingItem.setIndex(row.getIndex());
    return null;
  }

  public String moveUpItem()
  {
    EnumTypeItem row = null;
    try
    {
      row = (EnumTypeItem)getExternalContext().getRequestMap().get("row");
      int rowPosition = getPositionInList(row);
      EnumTypeItem topRow = rows.get(rowPosition - 1);
      swapRow(topRow);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    finally
    {
      String auxEnumTypeItemId = row.getEnumTypeItemId();
      load();
      firstRowIndex = PageFinder.findFirstRowIndex(rows, getPageSize(),
        "enumTypeItemId", auxEnumTypeItemId);
    }
    return null;
  }

  public String moveDownItem()
  {
    EnumTypeItem row = null;
    try
    {
      row = (EnumTypeItem)getExternalContext().getRequestMap().get("row");
      swapRow(row);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    finally
    {
      String auxEnumTypeItemId = row.getEnumTypeItemId();
      load();
      firstRowIndex = PageFinder.findFirstRowIndex(rows, getPageSize(),
        "enumTypeItemId", auxEnumTypeItemId);
    }
    return null;
  }

  public boolean isRenderMoveUpIcon()
  {
    try
    {
      EnumTypeItem row =
        (EnumTypeItem)getExternalContext().getRequestMap().get("row");
      return getPositionInList(row) > 0 && isSortedEnumType();
    }
    catch (Exception ex)
    {
      return false;
    }
  }

  public boolean isRenderMoveDownIcon()
  {
    try
    {
      EnumTypeItem row =
        (EnumTypeItem)getExternalContext().getRequestMap().get("row");
      int position = getPositionInList(row);
      return position >= 0 && position < rows.size() - 1 && isSortedEnumType();
    }
    catch (Exception ex)
    {
      return false;
    }
  }

  public boolean isRenderInsertHereIcon()
  {
    return isSortedEnumType();
  }

  public boolean isDateType()
  {
    EnumTypeMainBean enumTypeMainBean =
      (EnumTypeMainBean)getBean("enumTypeMainBean");
    return PropertyType.DATE.equals(enumTypeMainBean.getEnumType().getItemType());
  }

  public boolean isBooleanType()
  {
    EnumTypeMainBean enumTypeMainBean =
      (EnumTypeMainBean)getBean("enumTypeMainBean");
    return PropertyType.BOOLEAN.equals(enumTypeMainBean.getEnumType().getItemType());
  }

  public boolean isNumericType()
  {
    EnumTypeMainBean enumTypeMainBean =
      (EnumTypeMainBean)getBean("enumTypeMainBean");
    return PropertyType.NUMERIC.equals(enumTypeMainBean.getEnumType().getItemType());
  }

  private int getPositionInList(EnumTypeItem row) throws Exception
  {
    for (int i = 0; i < rows.size(); i++)
    {
      EnumTypeItem auxRow = rows.get(i);
      if (auxRow.getEnumTypeItemId().equals(row.getEnumTypeItemId()))
      {
        return i;
      }
    }
    throw new Exception("ITEM_NOT_FOUND");
  }

  private void swapRow(EnumTypeItem topRow) throws Exception
  {
    DictionaryManagerPort port = DictionaryConfigBean.getPort();

    int rowPosition = getPositionInList(topRow);
    EnumTypeItem bottomRow = rows.get(rowPosition + 1);

    EnumTypeItem auxItem = port.loadEnumTypeItem(bottomRow.getEnumTypeItemId());
    auxItem.setIndex(topRow.getIndex());
    port.storeEnumTypeItem(auxItem);
  }

  private void load()
  {
    try
    {
      if (!isNew())
      {
        EnumTypeItemFilter filter = new EnumTypeItemFilter();
        filter.setEnumTypeId(getObjectId());
        rows = DictionaryConfigBean.getPort().findEnumTypeItems(filter);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private boolean isSortedEnumType()
  {
    EnumTypeMainBean enumTypeMainBean =
      (EnumTypeMainBean)getBean("enumTypeMainBean");
    return enumTypeMainBean.getEnumType().isSorted();
  }

}
