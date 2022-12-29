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
package org.santfeliu.webapp.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.faces.model.SelectItem;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.webapp.NavigatorBean;
import org.santfeliu.webapp.NavigatorBean.BaseTypeInfo;
import org.santfeliu.webapp.TypeBean;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author blanquepa
 * @param <T> the type of referenced objects 
 */
public abstract class ReferenceHelper<T>
{
  private String typeId;
  private TypeBean<T, ?> typeBean;
  
  public ReferenceHelper(String typeId)
  {
    this.typeId = typeId;
    this.typeBean = TypeBean.getInstance(typeId);
  }
  
  public abstract String getId(T object);  
  
  public List<SelectItem> complete(String query)
  {
    List<SelectItem> items = new ArrayList<>();
    
    NavigatorBean navigatorBean = WebUtils.getBean("navigatorBean");
    BaseTypeInfo baseTypeInfo = navigatorBean.getBaseTypeInfo(typeId);

    if (StringUtils.isBlank(query))      
    {
      if (baseTypeInfo != null)
      {
        List<String> ids = baseTypeInfo.getRecentObjectIdList();        
        List<String> favIds = baseTypeInfo.getFavoriteObjectIdList();
        if (!favIds.isEmpty())
        {
          favIds.stream()
            .filter(i -> !ids.contains(i))
            .forEach((i) -> ids.add(i));
        }
        if (!ids.isEmpty())
        {
          ids.stream()
            .filter(i -> !StringUtils.isBlank(i))
            .forEach((i) -> items.add(getSelectItem(i)));
        }
      }    
    }
    else
    {
      List<T> objects = typeBean.find(query);
      objects.forEach((o) -> items.add(getSelectItem(o)));    
    }
    
    Collections.sort(items, (SelectItem i1, SelectItem i2) ->
    {
      if (i1 != null && i2 != null)
        return i1.getLabel().compareTo(i2.getLabel());
      else if (i1 == null)
        return 1;
      else
        return -1;
    });
    
    return items;
  }
    
  public String getDescription(String id)
  {
    return typeBean.getDescription(id);
  }
  
  public SelectItem getSelectItem(String id)
  {
    if (StringUtils.isBlank(id))
      return new SelectItem("", "");
    else 
      return new SelectItem(id, getDescription(id));
  }
  
  public SelectItem getSelectItem(T object)
  {
    return getSelectItem(getId(object));
  }
}
