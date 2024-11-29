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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.dic.PropertyDefinition;
import org.matrix.dic.PropertyType;
import org.matrix.dic.Type;
import org.primefaces.PrimeFaces;
import org.santfeliu.dic.TypeCache;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;

/**
 *
 * @author blanquepa
 */
@Named
@RequestScoped
public class TypePropertiesTabBean extends TabBean
{
  @Inject
  TypeObjectBean typeObjectBean;

  private int firstRow;
  private PropertyDefinitionEdit editing;
  private PropertyDefinition backupEditing;
  private List<org.santfeliu.dic.Type> supertypes = new ArrayList<>();
  private Map<String, List<PropertyDefinitionEdit>> rowsMap = new HashMap<>();
  private List<PropertyDefinitionEdit> filteredRows;
  private String rowsVisibility = "all";

  @Override
  public ObjectBean getObjectBean()
  {
    return typeObjectBean;
  }

  public int getFirstRow()
  {
    return firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    this.firstRow = firstRow;
  }

  public PropertyDefinitionEdit getEditing()
  {
    return editing;
  }

  public void setEditing(PropertyDefinitionEdit editing)
  {
    this.editing = editing;
  }

  public List<PropertyDefinitionEdit> getFilteredRows()
  {
    return filteredRows;
  }

  public void setFilteredRows(List<PropertyDefinitionEdit> filteredRows)
  {
    this.filteredRows = filteredRows;
  }

  public String getRowsVisibility()
  {
    return rowsVisibility;
  }

  public void setRowsVisibility(String rowsVisibility)
  {
    this.rowsVisibility = rowsVisibility;
  }

  public void setEnumTypeId(String enumTypeId)
  {
    if (editing != null)
      editing.setEnumTypeId(enumTypeId);
  }

  public String getEnumTypeId()
  {
    return editing == null ? NEW_OBJECT_ID : editing.getEnumTypeId();
  }

  public List<PropertyDefinitionEdit> getRows()
  {
    Type type = typeObjectBean.getObject();
    if (type != null)
      return rowsMap.get(type.getTypeId());
    else
      return Collections.EMPTY_LIST;
  }

  public List<PropertyDefinitionEdit> getDisplayedRows()
  {
    List<PropertyDefinitionEdit> result = new ArrayList<>();
    List<PropertyDefinitionEdit> allRows = getRows();
    if (allRows != null)
    {
      for (PropertyDefinitionEdit pd : allRows)
      {
        if ("all".equals(getRowsVisibility()) ||
          ("only_visible".equals(getRowsVisibility()) && !pd.isHidden()) ||
          ("only_hidden".equals(getRowsVisibility()) && pd.isHidden()))
        {
          result.add(pd);
        }
      }
    }
    return result;
  }

  public void changeVisibility()
  {
    PrimeFaces.current().executeScript(
      "PrimeFaces.widgets['typePropertiesTable'].filter()");
  }

  public List<org.santfeliu.dic.Type> getSupertypes()
  {
    return supertypes;
  }

  public List<PropertyDefinitionEdit> getTypePropertyDefinitions(
    org.santfeliu.dic.Type type)
  {
    List<PropertyDefinitionEdit> result = new ArrayList();
    if (rowsMap != null)
      result = rowsMap.get(type.getTypeId());

    return result;
  }

  public PropertyType[] getPropertyTypes()
  {
    return PropertyType.values();
  }

  @Override
  public void load()
  {
    System.out.println("load typeProperties:" + getObjectId());
    if (!NEW_OBJECT_ID.equals(getObjectId()))
    {
      Type type = typeObjectBean.getType();
      rowsMap.put(type.getTypeId(), getPropertyDefinitionList(type));

      supertypes = TypeCache.getInstance().getType(type.getTypeId())
        .getSuperTypes();

      for (org.santfeliu.dic.Type superType : supertypes)
      {
        rowsMap.put(superType.getTypeId(), getPropertyDefinitionList(superType));
      }

      PrimeFaces.current().executeScript(
        "PrimeFaces.widgets['typePropertiesTable'].filter()");
    }
  }

  public void create()
  {
    if (editing != null) return;

    editing = new PropertyDefinitionEdit();
    editing.setModified(true);
    getRows().add(editing);
  }

  public void create(PropertyDefinitionEdit row)
  {
    editing = null;

    for (PropertyDefinitionEdit pde : getRows())
    {
      if (pde.getName().equals(row.getName()))
      {
        editing = row; //Edit the existent
      }
    }

    if (editing == null) //Not found in current rows then create as new
    {
      editing = new PropertyDefinitionEdit(row);
      editing.setModified(true);
      getRows().add(editing);
    }
  }

  public void edit(PropertyDefinitionEdit row)
  {
    if (row == null)
      return;

    if (row.rowId < 0)
      create(row);
    else
      editing = row;

    backupEditing = new PropertyDefinitionEdit(editing.rowId, editing);
  }

  public void remove(PropertyDefinitionEdit row)
  {
    row.setRemoved(true);
    syncRows();
  }

  public void accept()
  {
    if (editing == null) return;

    if (!editing.isModified())
      editing.setModified(!editing.equals(backupEditing));
    syncRows();

    editing = null;
    backupEditing = null;

    PrimeFaces.current().executeScript(
      "PrimeFaces.widgets['typePropertiesTable'].filter()");
  }

  public void cancel()
  {
    if (editing != null && editing.isNew())
      getRows().remove(editing);
    editing = null;
    backupEditing = null;
  }

  @Override
  public boolean isDialogVisible()
  {
    return (editing != null);
  }

  @Override
  public void store()
  {
    load();
  }

  @Override
  public boolean isModified()
  {
    return true;
  }

  private void syncRows()
  {
    //Get type
    List<PropertyDefinition> propDefList =
      typeObjectBean.getType().getPropertyDefinition();
    propDefList.clear();

    String typeId = typeObjectBean.getType().getTypeId();
    for (PropertyDefinitionEdit row : rowsMap.get(typeId))
    {
      if (!row.removed)
      {
        if (editing != null && editing.rowId == row.rowId)
        {
          int rowId = editing.rowId >= 0 ? editing.rowId : propDefList.size();
          propDefList.add(new PropertyDefinitionEdit(rowId, editing));
          row.rowId = rowId; //sync rowId
        }
        else
          propDefList.add(row);
      }
    }
  }

  private List<PropertyDefinitionEdit> getPropertyDefinitionList(Type type)
  {
    List<PropertyDefinitionEdit> results = new ArrayList();
    List<PropertyDefinition> propDefs = type.getPropertyDefinition();
    for (int i = 0; i < propDefs.size(); i++)
    {
      PropertyDefinition propDef = propDefs.get(i);
      PropertyDefinitionEdit edit =
        new PropertyDefinitionEdit(i, propDef);
      results.add(edit);
    }
    return results;
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ editing, backupEditing, rowsMap, supertypes };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[]) state;
      editing = (PropertyDefinitionEdit) stateArray[0];
      backupEditing = (PropertyDefinitionEdit) stateArray[1];
      rowsMap = (Map<String, List<PropertyDefinitionEdit>>) stateArray[2];
      supertypes = (List<org.santfeliu.dic.Type>) stateArray[3];
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public class PropertyDefinitionEdit extends PropertyDefinition
  {
    private int rowId = -1;
    private boolean modified;
    private boolean removed;

    public PropertyDefinitionEdit()
    {
      this(-1, null);
    }

    public PropertyDefinitionEdit(PropertyDefinition propDef)
    {
      this(-1, propDef);
    }

    public PropertyDefinitionEdit(int rowId,
      PropertyDefinition propDef)
    {
      this.rowId = rowId;
      this.maxOccurs = 1;
      if (propDef != null)
      {
        name = propDef.getName();
        description = propDef.getDescription();
        type = propDef.getType();
        enumTypeId = propDef.getEnumTypeId();
        size = propDef.getSize();
        minOccurs = propDef.getMinOccurs();
        maxOccurs = propDef.getMaxOccurs();
        value = new ArrayList();
        value.addAll(propDef.getValue());
        hidden = propDef.isHidden();
        readOnly = propDef.isReadOnly();
        modified = false;
      }
    }

    public boolean isNew()
    {
      return rowId == -1;
    }

    public boolean isModified()
    {
      return modified;
    }

    public void setModified(boolean modified)
    {
      this.modified = modified;
    }

    public boolean isRemoved()
    {
      return removed;
    }

    public void setRemoved(boolean removed)
    {
      this.removed = removed;
    }

    public String getStringValue()
    {
      if (value == null || value.isEmpty())
        return "";

      if (value.size() == 1)
        return value.get(0);
      else
        return value.toString();
    }

    public void setStringValue(String strValue)
    {
      if (value == null)
        value = new ArrayList();

      if (value.isEmpty())
        value.add(strValue);
      else
        value.set(0, strValue);
    }

    @Override
    public boolean equals(Object obj)
    {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      final PropertyDefinition other = (PropertyDefinition) obj;
      if (this.size != other.getSize())
        return false;
      if (this.minOccurs != other.getMinOccurs())
        return false;
      if (this.maxOccurs != other.getMaxOccurs())
        return false;
      if (this.hidden != other.isHidden())
        return false;
      if (this.readOnly != other.isReadOnly())
        return false;
      if (!Objects.equals(this.name, other.getName()))
        return false;
      if (!Objects.equals(this.description, other.getDescription()))
        return false;
      if (!Objects.equals(this.enumTypeId, other.getEnumTypeId()))
        return false;
      if (this.type != other.getType())
        return false;
      return Objects.equals(this.value, other.getValue());
    }

  }

}
