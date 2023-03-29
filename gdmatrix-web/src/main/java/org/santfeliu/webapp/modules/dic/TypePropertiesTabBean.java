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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.dic.PropertyDefinition;
import org.santfeliu.dic.Type;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;

/**
 *
 * @author blanquepa
 */
@Named
@ViewScoped
public class TypePropertiesTabBean extends TabBean
{
  @Inject
  TypeObjectBean typeObjectBean;  
  
  private int firstRow;
  private PropertyDefinition editing;
  private List<PropertyDefinition> rows = new ArrayList<>();  
  private List<Type> supertypes = new ArrayList<>();

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

  public PropertyDefinition getEditing()
  {
    return editing;
  }

  public void setEditing(PropertyDefinition editing)
  {
    this.editing = editing;
  }

  public List<PropertyDefinition> getRows()
  {
    return rows;
  }
  
  @Override
  public void load()
  {
    System.out.println("load typeProperties:" + getObjectId());
    if (!NEW_OBJECT_ID.equals(getObjectId()))
    {
      Type type = (Type) typeObjectBean.getObject();
      rows = type.getPropertyDefinition();
      supertypes = type.getSuperTypes();
    }          
    else rows = Collections.EMPTY_LIST;
  }  
  
 
}
