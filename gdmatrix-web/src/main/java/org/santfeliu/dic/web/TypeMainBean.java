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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.matrix.dic.DictionaryManagerPort;
import org.matrix.dic.Type;
import org.matrix.dic.TypeFilter;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.obj.PageBean;

/**
 *
 * @author realor
 */
public class TypeMainBean extends PageBean
{
  private Type type;

  public TypeMainBean()
  {
    load();
  }

  @Override
  public String show()
  {
    return "type_main";
  }

  public Type getType()
  {
    return type;
  }

  public void setType(Type type)
  {
    this.type = type;
  }

  public Date getCreationDateTime()
  {
    return TextUtils.parseInternalDate(type.getCreationDateTime());
  }

  public Date getChangeDateTime()
  {
    return TextUtils.parseInternalDate(type.getChangeDateTime());
  }

  public List<org.santfeliu.dic.Type> getSuperTypes()
  {
    List<org.santfeliu.dic.Type> result =
      new ArrayList<org.santfeliu.dic.Type>();
    String typeId = type.getTypeId();
    if (typeId != null && typeId.trim().length() > 0)
    {
      TypeCache cache = TypeCache.getInstance();
      org.santfeliu.dic.Type type = cache.getType(typeId);
      if (type != null) result = type.getSuperTypes();
    }
    return result;
  }

  public String showTypeFromPath()
  {
    Type row = (Type)getValue("#{type}");
    return getControllerBean().showObject("Type", row.getTypeId());
  }

  @Override
  public String store()
  {
    try
    {
      DictionaryManagerPort port = DictionaryConfigBean.getPort();
      if (isNew())
      {
        if (type.getTypeId() == null || type.getTypeId().trim().isEmpty())
        {
          error("dform.REQUIRED_VALUE", new Object[]{"typeId"});
          return null;
        }
        TypeFilter filter = new TypeFilter();
        filter.setTypeId(type.getTypeId());
        if (port.countTypes(filter) > 0)
        {
          error("TYPE_ALREADY_EXISTS");
          return null;
        }
      }

      String superTypeId = type.getSuperTypeId();
      if (superTypeId != null && superTypeId.trim().length() == 0)
      {
        type.setSuperTypeId(null);
      }

      // store
      type = port.storeType(type);

      setObjectId(type.getTypeId());
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  private void load()
  {
    if (isNew())
    {
      type = new Type();
    }
    else
    {
      try
      {
        DictionaryManagerPort port = DictionaryConfigBean.getPort();
        type = port.loadType(getObjectId());
      }
      catch (Exception ex)
      {
        getObjectBean().clearObject();
        error(ex);
        type = new Type();
      }
    }

  }
}
