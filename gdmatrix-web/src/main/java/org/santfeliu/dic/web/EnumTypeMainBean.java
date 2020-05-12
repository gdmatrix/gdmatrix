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

import java.util.Date;
import org.matrix.dic.EnumType;
import org.matrix.dic.PropertyType;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.obj.PageBean;

/**
 *
 * @author unknown
 */
public class EnumTypeMainBean extends PageBean
{
  private EnumType enumType;
  private String itemTypeInput;

  public EnumTypeMainBean()
  {
    load();
  }

  public EnumType getEnumType()
  {
    return enumType;
  }

  public void setEnumType(EnumType enumType)
  {
    this.enumType = enumType;
  }

  public String getItemTypeInput()
  {
    return itemTypeInput;
  }

  public void setItemTypeInput(String itemTypeInput)
  {
    this.itemTypeInput = itemTypeInput;
  }

  public String show()
  {
    return "enum_type_main";
  }

  @Override
  public String store()
  {
    try
    {
      enumType.setItemType(getItemType());

      String superEnumTypeId = enumType.getSuperEnumTypeId();
      if (superEnumTypeId != null && superEnumTypeId.trim().length() == 0)
      {
        enumType.setSuperEnumTypeId(null);
      }

      enumType = DictionaryConfigBean.getPort().storeEnumType(enumType);
      setObjectId(enumType.getEnumTypeId());
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return show();
  }

  public Date getCreationDateTime()
  {
    return TextUtils.parseInternalDate(enumType.getCreationDateTime());
  }

  public Date getChangeDateTime()
  {
    return TextUtils.parseInternalDate(enumType.getChangeDateTime());
  }

  private void load()
  {
    if (isNew())
    {
      enumType = new EnumType();
      enumType.setSorted(true);
    }
    else
    {
      try
      {
        enumType = DictionaryConfigBean.getPort().loadEnumType(getObjectId());
        setItemType(enumType.getItemType());
      }
      catch (Exception ex)
      {
        getObjectBean().clearObject();
        error(ex);
        enumType = new EnumType();
      }
    }
  }

  private void setItemType(PropertyType itemType) throws Exception
  {
    switch (itemType)
    {
      case TEXT: setItemTypeInput("T"); break;
      case NUMERIC: setItemTypeInput("N"); break;
      case BOOLEAN: setItemTypeInput("B"); break;
      case DATE: setItemTypeInput("D"); break;
    }
  }

  private PropertyType getItemType()
  {
    if ("T".equals(itemTypeInput))
      return PropertyType.TEXT;
    else if ("N".equals(itemTypeInput))
      return PropertyType.NUMERIC;
    else if ("B".equals(itemTypeInput))
      return PropertyType.BOOLEAN;
    else if ("D".equals(itemTypeInput))
      return PropertyType.DATE;
    return null;
  }

}
