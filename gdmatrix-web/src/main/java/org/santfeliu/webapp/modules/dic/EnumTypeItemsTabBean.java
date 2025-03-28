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
package org.santfeliu.webapp.modules.dic;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.dic.EnumTypeItem;
import org.primefaces.event.ReorderEvent;
import org.santfeliu.dic.EnumTypeCache;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;

/**
 *
 * @author blanquepa
 */
@Named
@RequestScoped
public class EnumTypeItemsTabBean extends TabBean
{
  @Inject
  private EnumTypeObjectBean enumTypeObjectBean;

  private List<EnumTypeItem> rows;
  private int firstRow;
  private EnumTypeItem editing;
  private int rowsPerPage = 10;
  private int rowIndex;

  public EnumTypeItemsTabBean()
  {
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return enumTypeObjectBean;
  }

  public EnumTypeItem getEditing()
  {
    return editing;
  }

  public void setEditing(EnumTypeItem enumTypeItem)
  {
    this.editing = enumTypeItem;
  }

  public List<EnumTypeItem> getRows()
  {
    return rows;
  }

  public void setRows(List<EnumTypeItem> rows)
  {
    this.rows = rows;
  }

  public int getRowsPerPage()
  {
    return rowsPerPage;
  }

  public void setRowsPerPage(int rowsPerPage)
  {
    this.rowsPerPage = rowsPerPage;
  }

  public int getFirstRow()
  {
    return firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    this.firstRow = firstRow;
  }

  public int getRowIndex()
  {
    return rowIndex;
  }

  public void setRowIndex(int rowIndex)
  {
    this.rowIndex = rowIndex;
  }

  @Override
  public void load()
  {
    if (!NEW_OBJECT_ID.equals(getObjectId()))
    {
      try
      {
        rows = EnumTypeCache.getInstance().getItems(getObjectId());
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    else
    {
      rows = Collections.emptyList();
      firstRow = 0;
    }
  }

  public void create()
  {
    editing = new EnumTypeItem();
    editing.setEnumTypeId(getObjectId());
  }

  public void edit(EnumTypeItem row)
  {
    String itemId = null;
    if (row != null)
      itemId = row.getEnumTypeItemId();

    try
    {
      if (itemId != null)
      {
        editing = EnumTypeCache.getInstance().getItem(itemId);
      }
      else
      {
        create();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void insertItem(int rowIndex)
  {
    editing = new EnumTypeItem();
    editing.setEnumTypeId(getObjectId());
    int itemIndex = rowIndex + 1;
    editing.setIndex(itemIndex);
  }

  public void moveUpItem(int rowIndex)
  {
    try
    {
      if (rowIndex > 0)
      {
        swapRow(rowIndex);
        load();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void moveDownItem(int rowIndex)
  {
    try
    {
      swapRow(rowIndex + 1);
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public boolean isRenderUpButton(int rowIndex)
  {
    return isSortedEnumType() && rowIndex > 0 && rows.size() >= 2;
  }

  public boolean isRenderDownButton(int rowIndex)
  {
    return isSortedEnumType() && rowIndex < rows.size() - 1;
  }

  public boolean isSortedEnumType()
  {
    return enumTypeObjectBean.getEnumType().isSorted();
  }

  @Override
  public void store()
  {
    try
    {
      if (editing != null)
      {
        DicModuleBean.getPort(false).storeEnumTypeItem(editing);
        growl("STORE_OBJECT");
        EnumTypeCache.getInstance().clear(getObjectId());
        load();
        editing = null;
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void remove(EnumTypeItem row)
  {
    try
    {
      String rowEnumTypeItemId = row.getEnumTypeItemId();

      if (editing != null &&
        rowEnumTypeItemId.equals(editing.getEnumTypeItemId()))
        editing = null;

      DicModuleBean.getPort(false).removeEnumTypeItem(rowEnumTypeItemId);
      growl("REMOVE_OBJECT");
      EnumTypeCache.getInstance().clear(getObjectId());
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void cancel()
  {
    editing = null;
  }

  @Override
  public boolean isDialogVisible()
  {
    return (editing != null);
  }

  public void onRowReorder(ReorderEvent event)
  {
    try
    {
      EnumTypeItem row = rows.get(event.getToIndex());
      String itemId = row.getEnumTypeItemId();
      EnumTypeItem item =
        EnumTypeCache.getInstance().getItem(itemId);
      if (event.getToIndex() < event.getFromIndex())
      {
        item.setIndex(event.getToIndex() + 1);
      }
      else if (event.getToIndex() > event.getFromIndex())
      {
        item.setIndex(event.getToIndex() + 2);
      }
      else
      {
        return;
      }
      DicModuleBean.getPort(false).storeEnumTypeItem(item);
      EnumTypeCache.getInstance().clear(getObjectId());
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ editing };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      editing = (EnumTypeItem)stateArray[0];

      if (!isNew()) load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private void swapRow(int bottomRowIndex) throws Exception
  {
    if (rows != null && rows.size() >= 2)
    {
      EnumTypeItem bottomRow = rows.get(bottomRowIndex);
      String bottomItemId = bottomRow.getEnumTypeItemId();
      EnumTypeItem bottomItem =
        EnumTypeCache.getInstance().getItem(bottomItemId);
      bottomItem.setIndex(bottomRowIndex); //itemIndex = rowIndex + 1
      //store automatically shifts lower items
      DicModuleBean.getPort(false).storeEnumTypeItem(bottomItem);
      EnumTypeCache.getInstance().clear(getObjectId());
    }
  }

}
