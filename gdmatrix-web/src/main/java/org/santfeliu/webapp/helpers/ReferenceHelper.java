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
  private final String typeId;
  private final String selectExpression;

  @Deprecated
  public ReferenceHelper(String typeId)
  {
    this.typeId = typeId;
    this.selectExpression = null;
    // in this case setSelectedId & getSelectedId must be overrode
  }

  public ReferenceHelper(String typeId, String selectExpression)
  {
    this.typeId = typeId;
    if (!selectExpression.startsWith("#{"))
    {
      selectExpression = "#{" + selectExpression + "}";
    }
    this.selectExpression = selectExpression;
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
        List<String> ids = new ArrayList();
        ids.addAll(baseTypeInfo.getRecentObjectIdList());
        List<String> favIds = baseTypeInfo.getFavoriteObjectIdList();
        if (!favIds.isEmpty())
        {
          favIds.stream().filter(i -> !ids.contains(i))
            .forEach((i) -> ids.add(i));
        }
        if (!ids.isEmpty())
        {
          ids.stream().filter(i -> !StringUtils.isBlank(i))
            .forEach((i) -> items.add(getSelectItem(i)));
        }
      }
    }
    else
    {
      TypeBean typeBean = TypeBean.getInstance(typeId);
      List<T> objects = typeBean.find(query, typeId);
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

  public List<SelectItem> getSelectItems()
  {
    List<SelectItem> items = new ArrayList<>();
    TypeBean typeBean = TypeBean.getInstance(typeId);
    List<T> objects = typeBean.find(getFilter());
    objects.forEach((o) -> items.add(getSelectItem(o)));
    return items;
  }

  public <F> F getFilter()
  {
    return null;
  }

  public String find()
  {
    NavigatorBean navigatorBean = WebUtils.getBean("navigatorBean");
    return navigatorBean.find(typeId, selectExpression);
  }

  public String find(String selectExpression)
  {
    NavigatorBean navigatorBean = WebUtils.getBean("navigatorBean");
    return navigatorBean.find(typeId, selectExpression);
  }

  public String getDescription(String id)
  {
    TypeBean typeBean = TypeBean.getInstance(typeId);
    return typeBean.getDescription(id);
  }

  public SelectItem getSelectedItem()
  {
    return getSelectItem(getSelectedId());
  }

  public void setSelectedItem(SelectItem selectItem)
  {
    String value = selectItem != null ? (String) selectItem.getValue() : null;
    setSelectedId(value);
  }

  public String getSelectedId()
  {
    return WebUtils.getValueExpression(selectExpression);
  }

  public void setSelectedId(String objectId)
  {
    WebUtils.setValueExpression(selectExpression, String.class, objectId);
  }

  private SelectItem getSelectItem(String id)
  {
    if (StringUtils.isBlank(id))
      return new SelectItem("", "");
    else
      return new SelectItem(id, getDescription(id));
  }

  private SelectItem getSelectItem(T object)
  {
    return getSelectItem(getId(object));
  }

}
