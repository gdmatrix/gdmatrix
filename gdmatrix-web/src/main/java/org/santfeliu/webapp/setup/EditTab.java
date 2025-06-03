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
package org.santfeliu.webapp.setup;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author realor
 */
public class EditTab implements Serializable
{
  private String label;
  private String icon;
  private String viewId;
  private String beanName;
  private String subviewId;
  private String dialogViewId;
      
  @SerializedName(value="tableProperties", alternate={"columns"})  
  private List<TableProperty> tableProperties = new ArrayList<>();
  
  private PropertyMap properties = new PropertyMap();
  private List<String> readRoles = new ArrayList();
  private List<String> writeRoles = new ArrayList(); 
  private List<String> orderBy = new ArrayList();
  private String groupBy;
  private String filterBy;
  private String typeId;
  private boolean showAllTypes = false;
  private Integer pageSize;
  private List<Integer> pageSizeOptions;
  private Boolean exportable;
  private Integer rowExportLimit;

  public EditTab(String label, String icon, String viewId)
  {
    this(label, icon, viewId, null, null, null);
  }

  public EditTab(String label, String icon, String viewId, String beanName)
  {
    this(label, icon, viewId, beanName, null, null);
  }

  public EditTab(String label, String icon, String viewId, String beanName,
    String subviewId, String dialogViewId)
  {
    this.label = label;
    this.icon = icon;    
    this.viewId = viewId;
    this.beanName = beanName;
    this.subviewId = subviewId;
    this.dialogViewId= dialogViewId;
  }

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }

  public String getIcon()
  {
    return icon;
  }

  public void setIcon(String icon)
  {
    this.icon = icon;
  }

  public String getViewId()
  {
    return viewId;
  }

  public void setViewId(String viewId)
  {
    this.viewId = viewId;
  }

  public String getBeanName()
  {
    return beanName;
  }

  public void setBeanName(String beanName)
  {
    this.beanName = beanName;
  }

  public String getSubviewId()
  {
    return subviewId;
  }

  public void setSubviewId(String subviewId)
  {
    this.subviewId = subviewId;
  }

  public String getDialogViewId()
  {
    return dialogViewId;
  }

  public void setDialogViewId(String dialogViewId)
  {
    this.dialogViewId = dialogViewId;
  }

  public List<TableProperty> getTableProperties()
  {
    return tableProperties;
  }

  public void setTableProperties(List<TableProperty> tableProperties)
  {
    this.tableProperties = tableProperties;
  }

  public void setProperties(PropertyMap properties)
  {
    this.properties = properties;
  }

  public PropertyMap getProperties()
  {
    return properties;
  }

  public List<String> getReadRoles()
  {
    return readRoles;
  }

  public void setReadRoles(List<String> readRoles)
  {
    this.readRoles = readRoles;
  }

  public List<String> getWriteRoles()
  {
    return writeRoles;
  }

  public void setWriteRoles(List<String> writeRoles)
  {
    this.writeRoles = writeRoles;
  }

  public List<String> getOrderBy()
  {
    return orderBy;
  }

  public void setOrderBy(List<String> orderBy)
  {
    this.orderBy = orderBy;
  }

  public String getGroupBy()
  {
    return groupBy;
  }

  public void setGroupBy(String groupBy)
  {
    this.groupBy = groupBy;
  }

  public String getFilterBy() 
  {
    return filterBy;
  }

  public void setFilterBy(String filterBy) 
  {
    this.filterBy = filterBy;
  }

  public String getTypeId()
  {
    return typeId;
  }

  public void setTypeId(String typeId)
  {
    this.typeId = typeId;
  }

  public boolean isShowAllTypes()
  {
    return showAllTypes;
  }

  public void setShowAllTypes(boolean showAllTypes)
  {
    this.showAllTypes = showAllTypes;
  }

  public Integer getPageSize() 
  {
    return pageSize;
  }

  public void setPageSize(Integer pageSize) 
  {
    this.pageSize = pageSize;
  }

  public List<Integer> getPageSizeOptions() 
  {
    return pageSizeOptions;
  }

  public void setPageSizeOptions(List<Integer> pageSizeOptions) 
  {
    this.pageSizeOptions = pageSizeOptions;
  }

  public Boolean getExportable() 
  {
    return exportable;
  }

  public void setExportable(Boolean exportable) 
  {
    this.exportable = exportable;
  }

  public Integer getRowExportLimit() 
  {
    return rowExportLimit;
  }

  public void setRowExportLimit(Integer rowExportLimit) 
  {
    this.rowExportLimit = rowExportLimit;
  }

  public String getBaseTypeId()
  {
    if (getTypeId() != null)
    {
      return getTypeId();
    }
    else if (getProperties() != null)
    {
      return getProperties().getString("typeId");
    }
    else return null;
  }
  
  public static String createSubviewId(String viewId)
  {
    int index = viewId.lastIndexOf("/");
    if (index != -1) viewId = viewId.substring(index + 1);
    index = viewId.lastIndexOf(".");
    if (index != -1) viewId = viewId.substring(0, index);

    return viewId;
  }

}
