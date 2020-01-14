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
package org.santfeliu.dic.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.model.SelectItem;
import org.matrix.dic.PropertyDefinition;
import org.matrix.dic.PropertyType;
import org.santfeliu.web.obj.PageBean;

/**
 *
 * @author lopezrj
 */
public abstract class PropertiesPageBean extends PageBean
{
  public static String STATE_NEW = "N";
  public static String STATE_EDITED = "T";
  public static String STATE_EXISTING = "E";
  public static String STATE_REMOVED = "R";

  protected PropertyDefinitionItem editingPropertyDefinitionItem;
  protected List<PropertyDefinitionItem> rows;

  private PropertyDefinition propertyDefinitionBackup;

  public List<PropertyDefinitionItem> getRows()
  {
    if (rows == null)
    {
      rows = new ArrayList<PropertyDefinitionItem>();
      List<PropertyDefinition> propertyDefinitionList =
        getMainPropertyDefinitionList();
      for (PropertyDefinition propertyDefinition : propertyDefinitionList)
      {
        PropertyDefinitionItem item =
          new PropertyDefinitionItem(propertyDefinition, STATE_EXISTING);
        rows.add(item);
      }
    }
    return rows;
  }

  public void setRows(List<PropertyDefinitionItem> rows)
  {
    this.rows = rows;
  }

  public PropertyDefinitionItem getEditingPropertyDefinitionItem()
  {
    return editingPropertyDefinitionItem;
  }

  public void setEditingPropertyDefinitionItem(PropertyDefinitionItem
    editingPropertyDefinitionItem)
  {
    this.editingPropertyDefinitionItem = editingPropertyDefinitionItem;
  }

  public PropertyDefinition getPropertyDefinitionBackup()
  {
    return propertyDefinitionBackup;
  }

  public void setPropertyDefinitionBackup(PropertyDefinition
    propertyDefinitionBackup)
  {
    this.propertyDefinitionBackup = propertyDefinitionBackup;
  }

  public String storePropertyDefinition()
  {
    try
    {
      String name =
        editingPropertyDefinitionItem.getPropertyDefinition().getName();
      if (name == null || name.trim().length() == 0)
      {
        if (!editingPropertyDefinitionItem.isNew())
        {
          revertPropertyDefinitionChanges();
        }        
      }
      else
      {
        if (!getRows().contains(editingPropertyDefinitionItem))
        {
          getMainPropertyDefinitionList().add(
            editingPropertyDefinitionItem.getPropertyDefinition());
          getRows().add(editingPropertyDefinitionItem);
        }
        else //has been modified?
        {
          if (isPropertyDefinitionModified())
          {
            editingPropertyDefinitionItem.setState(STATE_EDITED);
          }
        }
      }
      propertyDefinitionBackup = null;
      editingPropertyDefinitionItem = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String cancelPropertyDefinition()
  {
    propertyDefinitionBackup = null;
    editingPropertyDefinitionItem = null;
    return null;
  }

  public String removePropertyDefinition()
  {
    PropertyDefinitionItem row =
      (PropertyDefinitionItem)getExternalContext().getRequestMap().get("row");
    getMainPropertyDefinitionList().remove(row.getPropertyDefinition());
    if (row.isNew())
    {
      getRows().remove(row);
    }
    else
    {
      row.setState(STATE_REMOVED);
    }
    return null;
  }
  
  public String editPropertyDefinition()
  {
    editingPropertyDefinitionItem =
      (PropertyDefinitionItem)getExternalContext().getRequestMap().get("row");
    backupPropertyDefinition();
    return null;
  }

  public String addPropertyDefinition()
  {
    editingPropertyDefinitionItem = new PropertyDefinitionItem(STATE_NEW);
    editingPropertyDefinitionItem.getPropertyDefinition().setMaxOccurs(1);
    return null;
  }

  @Override
  public String store()
  {
    editingPropertyDefinitionItem = null;
    propertyDefinitionBackup = null;
    rows = null;
    //inheritedRows = null;
    return show();
  }

  public String getPropertyDefinitionStyleClass()
  {
    PropertyDefinitionItem row =
      (PropertyDefinitionItem)getExternalContext().getRequestMap().get("row");
    if (row.isNew())
    {
      return "newPropertyDefinition";
    }
    else if (row.isRemoved())
    {
      return "removedPropertyDefinition";
    }
    else if (row.isExisting())
    {
      return "existingPropertyDefinition";
    }
    else if (row.isEdited())
    {
      return "newPropertyDefinition";
    }
    return "";
  }

  public List<SelectItem> getPropertyTypeSelectItems()
  {
    ArrayList<SelectItem> selectItems = new ArrayList<SelectItem>();
    for (PropertyType value : PropertyType.values())
    {
      selectItems.add(new SelectItem(value, value.toString()));
    }
    return selectItems;
  }

  public List<SelectItem> getEnumTypeSelectItems()
  {
    EnumTypeBean enumTypeBean = (EnumTypeBean)getBean("enumTypeBean");
    return enumTypeBean.getSelectItems(editingPropertyDefinitionItem.getPropertyDefinition().getEnumTypeId());
  }

  public String getEditingPropertyDefinitionValue()
  {
    String value = "";
    if (editingPropertyDefinitionItem.getPropertyDefinition().getValue() != null
        && editingPropertyDefinitionItem.getPropertyDefinition().getValue().
        size() > 0)
      value = editingPropertyDefinitionItem.getPropertyDefinition().getValue().
        get(0);
    return value;
  }

  public void setEditingPropertyDefinitionValue(String value)
  {
    if (editingPropertyDefinitionItem != null)
    {
      editingPropertyDefinitionItem.getPropertyDefinition().getValue().clear();
      editingPropertyDefinitionItem.getPropertyDefinition().getValue().
        add(value);
    }
  }

  protected abstract List<PropertyDefinition> getMainPropertyDefinitionList();

  private void backupPropertyDefinition()
  {
    propertyDefinitionBackup = new PropertyDefinition();
    propertyDefinitionBackup.setDescription(
      editingPropertyDefinitionItem.getPropertyDefinition().getDescription());
    propertyDefinitionBackup.setHidden(
      editingPropertyDefinitionItem.getPropertyDefinition().isHidden());
    propertyDefinitionBackup.setMaxOccurs(
      editingPropertyDefinitionItem.getPropertyDefinition().getMaxOccurs());
    propertyDefinitionBackup.setMinOccurs(
      editingPropertyDefinitionItem.getPropertyDefinition().getMinOccurs());
    propertyDefinitionBackup.setName(
      editingPropertyDefinitionItem.getPropertyDefinition().getName());
    propertyDefinitionBackup.setReadOnly(
      editingPropertyDefinitionItem.getPropertyDefinition().isReadOnly());
    propertyDefinitionBackup.setSize(
      editingPropertyDefinitionItem.getPropertyDefinition().getSize());
    propertyDefinitionBackup.setType(
      editingPropertyDefinitionItem.getPropertyDefinition().getType());
    if (editingPropertyDefinitionItem.getPropertyDefinition().getValue().
      isEmpty())
    {
      propertyDefinitionBackup.getValue().add("");
    }
    else
    {
      propertyDefinitionBackup.getValue().addAll(
        editingPropertyDefinitionItem.getPropertyDefinition().getValue());
    }
  }

  private void revertPropertyDefinitionChanges()
  {
    editingPropertyDefinitionItem.getPropertyDefinition().setDescription(
      propertyDefinitionBackup.getDescription());
    editingPropertyDefinitionItem.getPropertyDefinition().setHidden(
      propertyDefinitionBackup.isHidden());
    editingPropertyDefinitionItem.getPropertyDefinition().setMaxOccurs(
      propertyDefinitionBackup.getMaxOccurs());
    editingPropertyDefinitionItem.getPropertyDefinition().setMinOccurs(
      propertyDefinitionBackup.getMinOccurs());
    editingPropertyDefinitionItem.getPropertyDefinition().setName(
      propertyDefinitionBackup.getName());
    editingPropertyDefinitionItem.getPropertyDefinition().setReadOnly(
      propertyDefinitionBackup.isReadOnly());
    editingPropertyDefinitionItem.getPropertyDefinition().setSize(
      propertyDefinitionBackup.getSize());
    editingPropertyDefinitionItem.getPropertyDefinition().setType(
      propertyDefinitionBackup.getType());
    editingPropertyDefinitionItem.getPropertyDefinition().getValue().clear();
    editingPropertyDefinitionItem.getPropertyDefinition().getValue().addAll(
      propertyDefinitionBackup.getValue());
  }

  private boolean isPropertyDefinitionModified()
  {
    PropertyDefinition pd1 =
      editingPropertyDefinitionItem.getPropertyDefinition();
    PropertyDefinition pd2 = propertyDefinitionBackup;
    if (pd1.getMaxOccurs() == pd2.getMaxOccurs() &&
      pd1.getMinOccurs() == pd2.getMinOccurs() &&
      pd1.getSize() == pd2.getSize() &&
      pd1.isHidden() == pd2.isHidden() &&
      pd1.isReadOnly() == pd2.isReadOnly() &&
      pd1.getType().value().equals(pd2.getType().value()))
    {
      if ((pd1.getDescription() == null && pd2.getDescription() == null) ||
        (pd1.getDescription() != null && pd2.getDescription() != null &&
        pd1.getDescription().equals(pd2.getDescription())))
      {
        if ((pd1.getName() == null && pd2.getName() == null) ||
          (pd1.getName() != null && pd2.getName() != null &&
          pd1.getName().equals(pd2.getName())))
        {
          if (pd1.getValue().size() == pd2.getValue().size())
          {
            for (int i = 0; i < pd1.getValue().size(); i++)
            {
              if 
              (
                !(
                  (
                    pd1.getValue().get(0) == null
                    &&
                    pd2.getValue().get(0) == null
                  )
                  ||
                  (
                    pd1.getValue().get(0) != null
                    &&
                    pd2.getValue().get(0) != null
                    &&
                    pd1.getValue().get(0).equals(pd2.getValue().get(0))
                  )
                )
              )
              {
                return true;
              }
            }
            return false;
          }
          else return true;
        }
        else return true;
      }
      else return true;
    }
    else return true;
  }

  public class PropertyDefinitionItem implements Serializable
  {
    private PropertyDefinition propertyDefinition;
    private String state; //(N)ew, (E)xisting, (R)emoved, Edi(T)ed

    public PropertyDefinitionItem(String state)
    {
      this.propertyDefinition = new PropertyDefinition();
      this.state = state;
    }

    public PropertyDefinitionItem(PropertyDefinition propertyDefinition,
      String state)
    {
      this.propertyDefinition = propertyDefinition;
      this.state = state;
    }

    public PropertyDefinition getPropertyDefinition()
    {
      return propertyDefinition;
    }

    public void setPropertyDefinition(PropertyDefinition propertyDefinition)
    {
      this.propertyDefinition = propertyDefinition;
    }

    public String getState()
    {
      return state;
    }

    public void setState(String state)
    {
      this.state = state;
    }

    public boolean isNew()
    {
      return STATE_NEW.equals(state);
    }

    public boolean isRemoved()
    {
      return STATE_REMOVED.equals(state);
    }

    public boolean isExisting()
    {
      return STATE_EXISTING.equals(state);
    }

    public boolean isEdited()
    {
      return STATE_EDITED.equals(state);
    }
  }

}
