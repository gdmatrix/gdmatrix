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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author realor
 */
public class SearchTab
{
  private String label;
  private String viewId;
  
  @SerializedName(value="tableProperties", alternate={"columns"})  
  private List<TableProperty> tableProperties = new ArrayList<>();
  
  private List<String> orderBy = new ArrayList<>();
  private List<String> orderByColumns = new ArrayList<>();
  private PropertyMap properties = new PropertyMap();
  private Boolean exportable;
  private Integer rowExportLimit;

  public SearchTab(String label, String viewId)
  {
    this.label = label;
    this.viewId = viewId;
  }

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }

  public String getViewId()
  {
    return viewId;
  }

  public void setViewId(String viewId)
  {
    this.viewId = viewId;
  }

  public List<TableProperty> getTableProperties()
  {
    return tableProperties;
  }

  public void setTableProperties(List<TableProperty> tableProperties)
  {
    this.tableProperties = tableProperties;
  }
  
  public List<String> getOrderBy()
  {
    return orderBy;
  }

  public void setOrderBy(List<String> orderBy)
  {
    this.orderBy = orderBy;
  }

  public List<String> getOrderByColumns() 
  {
    return orderByColumns;
  }

  public void setOrderByColumns(List<String> orderByColumns) 
  {
    this.orderByColumns = orderByColumns;
  }

  public void setProperties(PropertyMap properties)
  {
    this.properties = properties;
  }

  public PropertyMap getProperties()
  {
    return properties;
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

}
