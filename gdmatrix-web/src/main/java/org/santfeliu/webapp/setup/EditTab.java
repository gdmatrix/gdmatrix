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
  private String viewId;
  private String beanName;
  private String subviewId;
  private String dialogViewId;
  private List<Column> columns = new ArrayList<>();
  private PropertyMap properties = new PropertyMap();
  private List<String> readRoles = new ArrayList();
  private List<String> writeRoles = new ArrayList();  

  public EditTab(String label, String viewId)
  {
    this(label, viewId, null, null, null);
  }

  public EditTab(String label, String viewId, String beanName)
  {
    this(label, viewId, beanName, null, null);
  }

  public EditTab(String label, String viewId, String beanName, String subviewId)
  {
    this(label, viewId, beanName, subviewId, null);
  }

  public EditTab(String label, String viewId, String beanName,
    String subviewId, String dialogViewId)
  {
    this.label = label;
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

  public List<Column> getColumns()
  {
    return columns;
  }

  public void setColumns(List<Column> columns)
  {
    this.columns = columns;
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

  public static String createSubviewId(String viewId)
  {
    int index = viewId.lastIndexOf("/");
    if (index != -1) viewId = viewId.substring(index + 1);
    index = viewId.lastIndexOf(".");
    if (index != -1) viewId = viewId.substring(0, index);

    return viewId;
  }

}
