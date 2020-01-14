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
package org.santfeliu.dic.service;

import java.util.Collection;
import org.matrix.dic.EnumType;
import org.matrix.dic.PropertyType;
import org.santfeliu.jpa.JPAUtils;

/**
 *
 * @author lopezrj
 */
public class DBEnumType extends EnumType
{
  private String strItemType;
  private String strSorted;

  private Collection<DBEnumTypeItem> enumTypeItems; //Relationship

  public DBEnumType()
  {
  }
  
  public DBEnumType(EnumType enumType)
  {
    copyFrom(enumType);
  }

  public String getStrItemType()
  {
    return strItemType;
  }

  public void setStrItemType(String strItemType)
  {
    this.strItemType = strItemType;
  }

  public String getStrSorted()
  {
    return strSorted;
  }

  public void setStrSorted(String strSorted)
  {
    this.strSorted = strSorted;
  }

  public Collection<DBEnumTypeItem> getEnumTypeItems()
  {
    return enumTypeItems;
  }

  public void setEnumTypeItems(Collection<DBEnumTypeItem> enumTypeItems)
  {
    this.enumTypeItems = enumTypeItems;
  }

  public void copyTo(EnumType enumType)
  {
    JPAUtils.copy(this, enumType);
    if ("T".equals(strItemType))
      enumType.setItemType(PropertyType.TEXT);
    else if ("N".equals(strItemType))
      enumType.setItemType(PropertyType.NUMERIC);
    else if ("B".equals(strItemType))
      enumType.setItemType(PropertyType.BOOLEAN);
    else if ("D".equals(strItemType))
      enumType.setItemType(PropertyType.DATE);
    enumType.setSorted("Y".equals(strSorted));
  }

  public void copyFrom(EnumType enumType)
  {
    setEnumTypeId(enumType.getEnumTypeId());
    setSuperEnumTypeId(enumType.getSuperEnumTypeId());
    setName(enumType.getName());
    setItemType(enumType.getItemType());
    switch (enumType.getItemType())
    {
      case TEXT: setStrItemType("T"); break;
      case NUMERIC: setStrItemType("N"); break;
      case BOOLEAN: setStrItemType("B"); break;
      case DATE: setStrItemType("D"); break;
    }
    setSorted(enumType.isSorted());
    setStrSorted(enumType.isSorted() ? "Y" : "N");
  }

}
